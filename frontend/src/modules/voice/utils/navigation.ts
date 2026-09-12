import router from '../../../router'
import type { RouteRecordRaw } from 'vue-router'
import { getAllKeywordsMap, findModuleByKeyword, getModuleDescription, knowledgeBase } from './knowledge-base'

// 从知识库动态生成关键词到路由路径的映射表
export const routeKeywords: Record<string, string> = getAllKeywordsMap();

// 添加默认回退路径
routeKeywords['主页'] = '/home';
routeKeywords['首页'] = '/home';

/**
 * 检查路径是否存在于路由配置中
 * @param path 要检查的路径
 * @returns 是否存在
 */
function pathExists(path: string): boolean {
  const routes = router.getRoutes()
  return routes.some(route => route.path === path)
}

/**
 * 尝试根据语音文本匹配并跳转路由
 * @param text 语音转录文本
 * @returns 匹配结果描述
 */
export async function navigateByVoice(text: string): Promise<{ success: boolean; message: string; target?: string; moduleInfo?: any }> {
  const cleanText = text.trim().toLowerCase()
  
  // 1. 智能关键词匹配 - 优先使用知识库进行匹配
  const matchedModule = findModuleByKeyword(cleanText)
  
  if (matchedModule) {
    try {
      // 预检查路径是否存在，避免无效跳转
      if (!pathExists(matchedModule.routePath)) {
        console.error(`路径不存在: ${matchedModule.routePath}`)
        return { 
          success: false, 
          message: `抱歉，${matchedModule.fullName} 功能尚未实现或路径配置错误`,
          moduleInfo: matchedModule
        }
      }
      
      // 记录导航开始时间
      const startTime = performance.now()
      
      await router.push(matchedModule.routePath)
      
      // 记录导航结束时间
      const endTime = performance.now()
      const navigationTime = endTime - startTime
      
      // 性能监控：如果导航时间超过3秒，记录警告
      if (navigationTime > 3000) {
        console.warn(`导航性能警告: ${matchedModule.fullName} (${matchedModule.routePath}) 加载时间过长: ${navigationTime.toFixed(2)}ms`)
      }
      
      return { 
        success: true, 
        message: `已为您打开${matchedModule.fullName}`, 
        target: matchedModule.routePath,
        moduleInfo: matchedModule
      }
    } catch (e) {
      const error = e as Error
      console.error('Navigation error:', error.message)
      
      if (error.message.includes('404')) {
        return { 
          success: false, 
          message: `抱歉，${matchedModule.fullName} 页面不存在`,
          moduleInfo: matchedModule
        }
      } else if (error.message.includes('timeout')) {
        return { 
          success: false, 
          message: `抱歉，${matchedModule.fullName} 页面加载超时`,
          moduleInfo: matchedModule
        }
      }
      
      return { 
        success: false, 
        message: `跳转失败：${error.message}`,
        moduleInfo: matchedModule
      }
    }
  }
  
  // 2. 传统关键词匹配 - 作为知识库匹配的 fallback
  const sortedKeywords = Object.entries(routeKeywords).sort(([k1], [k2]) => k2.length - k1.length)
  
  for (const [jumpto, path] of sortedKeywords) {
    const lowerKeyword = jumpto.toLowerCase()
    if (cleanText.includes(lowerKeyword)) {
      try {
        // 预检查路径是否存在，避免无效跳转
        if (!pathExists(path)) {
          console.error(`路径不存在: ${path}`)
          return { success: false, message: `抱歉，${jumpto} 功能尚未实现或路径配置错误` }
        }
        
        // 记录导航开始时间
        const startTime = performance.now()
        
        await router.push(path)
        
        // 记录导航结束时间
        const endTime = performance.now()
        const navigationTime = endTime - startTime
        
        // 性能监控：如果导航时间超过3秒，记录警告
        if (navigationTime > 3000) {
          console.warn(`导航性能警告: ${jumpto} (${path}) 加载时间过长: ${navigationTime.toFixed(2)}ms`)
        }
        
        return { success: true, message: `已为您打开${jumpto}`, target: path }
      } catch (e) {
        const error = e as Error
        console.error('Navigation error:', error.message)
        
        if (error.message.includes('404')) {
          return { success: false, message: `抱歉，${jumpto} 页面不存在` }
        } else if (error.message.includes('timeout')) {
          return { success: false, message: `抱歉，${jumpto} 页面加载超时` }
        }
        
        return { success: false, message: `跳转失败：${error.message}` }
      }
    }
  }
  
  // 3. 路由元数据模糊匹配 (最终 fallback)
  // 遍历所有路由查找 meta.title (这需要路由配置配合)
  const routes = router.getRoutes()
  const matchedRoute = routes.find(r => {
    const title = (r.meta?.title as string) || ''
    return title && cleanText.includes(title.toLowerCase())
  })

  if (matchedRoute) {
    try {
      // 记录导航开始时间
      const startTime = performance.now()
      
      await router.push(matchedRoute.path)
      
      // 记录导航结束时间
      const endTime = performance.now()
      const navigationTime = endTime - startTime
      
      // 性能监控：如果导航时间超过3秒，记录警告
      if (navigationTime > 3000) {
        console.warn(`导航性能警告: ${matchedRoute.meta?.title || '目标页面'} (${matchedRoute.path}) 加载时间过长: ${navigationTime.toFixed(2)}ms`)
      }
      
      return { 
        success: true, 
        message: `已跳转至 ${matchedRoute.meta?.title || '目标页面'}`, 
        target: matchedRoute.path 
      }
    } catch (e) {
      const error = e as Error
      console.error('Navigation fallback error:', error.message)
      return { success: false, message: `跳转异常：${error.message}` }
    }
  }
  
  // 4. 提供智能帮助
  if (cleanText.includes('帮助') || cleanText.includes('功能') || cleanText.includes('介绍')) {
    return { 
      success: false, 
      message: '我可以帮您导航到各个系统模块，例如：ERP、财务、供应链、生产、HR、OA等。请尝试说出您要访问的模块名称。' 
    }
  }

  return { success: false, message: '抱歉，我没听懂您想去哪里，请尝试说出具体的模块名称，例如：ERP、财务、供应链等。' }
}

/**
 * 获取模块的详细信息和功能描述
 * @param moduleName 模块名称或关键词
 * @returns 模块详细信息
 */
export function getModuleInfo(moduleName: string): { success: boolean; message: string; moduleInfo?: any } {
  const matchedModule = findModuleByKeyword(moduleName)
  
  if (matchedModule) {
    return { 
      success: true, 
      message: getModuleDescription(matchedModule.name),
      moduleInfo: matchedModule
    }
  }
  
  return { 
    success: false, 
    message: `抱歉，未找到与"${moduleName}"相关的模块信息` 
  }
}

/**
 * 获取系统所有模块的概览信息
 * @returns 系统模块概览
 */
export function getSystemOverview(): { success: boolean; message: string; modules?: any[] } {
  return { 
    success: true, 
    message: '系统包含21个核心模块，分为8大类：供应链与采购、数据与决策、产品与研发、企业资源、客户与销售、生产与质量、仓储与物流、设备与能源。',
    modules: knowledgeBase
  }
}
