import type { Router } from 'vue-router'
import type { ParsedCommand } from './command-parser'
import { useVoiceStore } from '../stores/voice'
import { getSpareParts, createFault, getAssets } from '@/api/eam'
import { DataTransformer } from '@/utils/data-transformer'

export interface ExecutionResult {
    success: boolean
    message: string
}

export class CommandExecutor {
    private router: Router
    private voiceStore: ReturnType<typeof useVoiceStore>

    constructor(router: Router, voiceStore: ReturnType<typeof useVoiceStore>) {
        this.router = router
        this.voiceStore = voiceStore
    }

    async execute(cmd: ParsedCommand): Promise<ExecutionResult> {
        console.log('[CommandExecutor] Executing:', cmd)

        try {
            switch (cmd.intent) {
                case 'NAVIGATE':
                    return await this.handleNavigate(cmd)
                case 'ACTION':
                    return await this.handleAction(cmd)
                case 'CANCEL':
                    return this.handleCancel(cmd)
                case 'FILL_FORM':
                    return this.handleFillForm(cmd)
                case 'HELP':
                    return this.handleHelp(cmd)
                case 'QUERY_STOCK':
                    return await this.handleQueryStock(cmd)
                case 'RECORD_FAULT':
                    return await this.handleRecordFault(cmd)
                default:
                    return this.handleDefault(cmd)
            }
        } catch (error: any) {
            console.error('[CommandExecutor] Execution failed:', error)
            return {
                success: false,
                message: error.message || '执行出错'
            }
        }
    }

    private async handleNavigate(cmd: ParsedCommand): Promise<ExecutionResult> {
        if (cmd.target) {
            try {
                await this.router.push(cmd.target)
                return {
                    success: true,
                    message: cmd.message || '正在为您跳转'
                }
            } catch (err) {
                return {
                    success: false,
                    message: '抱歉，跳转失败，请确认页面是否存在。'
                }
            }
        }
        return { success: false, message: '未指定跳转目标' }
    }

    private async handleAction(cmd: ParsedCommand): Promise<ExecutionResult> {
        if (cmd.target) {
            // 从当前激活的合并上下文中查找动作
            const activeContext = this.voiceStore.mergedActiveContext
            const action = activeContext?.actions?.[cmd.target]
            
            if (action) {
                await action(cmd.payload)
                return {
                    success: true,
                    message: cmd.message || '操作已执行'
                }
            } else {
                return {
                    success: false,
                    message: '当前页面无法执行该操作'
                }
            }
        }
        return { success: false, message: '未指定操作动作' }
    }

    private handleCancel(cmd: ParsedCommand): ExecutionResult {
        // 取消操作通常由调用方（组件）处理UI状态变更，这里只返回消息
        return {
            success: true,
            message: cmd.message || '好的，已取消'
        }
    }

    private handleFillForm(cmd: ParsedCommand): ExecutionResult {
        if (cmd.payload) {
             const activeContext = this.voiceStore.mergedActiveContext
             if (activeContext?.setters) {
                 let filledCount = 0
                 for (const [key, value] of Object.entries(cmd.payload)) {
                     const setter = activeContext.setters[key]
                     if (setter) {
                         setter(value)
                         filledCount++
                     }
                 }
                 if (filledCount > 0) {
                     return { success: true, message: cmd.message || `已更新 ${filledCount} 个字段` }
                 }
                 return { success: false, message: '当前页面无法设置该字段' }
             }
        }
        return { success: false, message: '无需填充数据' }
    }

    private handleHelp(cmd: ParsedCommand): ExecutionResult {
        return {
            success: true,
            message: cmd.message || '您可以说“打开某页面”或“执行某操作”'
        }
    }

    /**
     * S10 语音车间：备件库存查询（AUTO 级只读，语音播报结果）
     * 支持按备件编码/名称模糊匹配，如"查一下 P-4421 库存"
     */
    private async handleQueryStock(cmd: ParsedCommand): Promise<ExecutionResult> {
        const keyword = String(cmd.payload?.keyword || '').trim()
        if (!keyword) {
            return { success: false, message: '请告诉我要查询的备件名称或编码' }
        }
        const res = await getSpareParts()
        const list = DataTransformer.unwrapList<any>(res)
        if (!list.length) {
            return { success: false, message: '未获取到备件库存数据' }
        }
        const lower = keyword.toLowerCase()
        // 先精确匹配编码/名称，再模糊包含匹配
        const exact = list.find((s: any) =>
            String(s.code || '').toLowerCase() === lower || String(s.name || '') === keyword)
        const matches = exact
            ? [exact]
            : list.filter((s: any) =>
                String(s.code || '').toLowerCase().includes(lower) || String(s.name || '').includes(keyword))
        if (!matches.length) {
            return { success: false, message: `没有找到与 ${keyword} 相关的备件` }
        }
        const summaries = matches.slice(0, 3).map((s: any) => {
            const stock = s.currentStock ?? 0
            const safety = s.safetyStock ?? 0
            const warn = stock < safety ? `，低于安全库存 ${safety}` : ''
            return `${s.name}（${s.code}）当前库存 ${stock} ${s.unit || '件'}${warn}`
        })
        const suffix = matches.length > 3 ? `，另有 ${matches.length - 3} 种相关备件` : ''
        return { success: true, message: summaries.join('；') + suffix }
    }

    /**
     * S10 语音车间：设备异常语音记录（CONFIRM 级，经现有 EAM 故障端点，不开后门）
     * 解析"记录一下：3号泵站有异响"→ 匹配设备名 → 创建故障记录
     */
    private async handleRecordFault(cmd: ParsedCommand): Promise<ExecutionResult> {
        const description = String(cmd.payload?.description || '').trim()
        if (!description) {
            return { success: false, message: '请说明要记录的异常内容' }
        }
        // 尝试从描述中匹配设备名称（如"3号泵站有异响"中的"3号泵站"）
        let equipmentId: number | undefined
        let equipmentName = '语音上报'
        try {
            const res = await getAssets()
            const assets = DataTransformer.unwrapList<any>(res)
            const hit = assets.find((a: any) => a.name && description.includes(a.name))
            if (hit) {
                equipmentId = hit.id
                equipmentName = hit.name
            }
        } catch (e) {
            console.warn('[Voice] 设备匹配失败，按未关联设备记录', e)
        }
        await createFault({
            equipmentId,
            equipmentName,
            type: '语音上报',
            description,
            reporter: '语音助手',
            status: 'reported',
            reportTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
        })
        return {
            success: true,
            message: `已记录${equipmentName !== '语音上报' ? `设备 ${equipmentName} 的` : ''}异常：${description}，故障单已提交`
        }
    }

    private handleDefault(cmd: ParsedCommand): ExecutionResult {
        // 对于闲聊或未识别指令，提供默认回复
        if (cmd.message && cmd.message !== '抱歉，我不理解这个指令') {
            return { success: true, message: cmd.message }
        }
        return { success: true, message: '收到' }
    }
}
