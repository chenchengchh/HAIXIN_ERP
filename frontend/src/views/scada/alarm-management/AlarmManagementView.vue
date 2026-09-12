<template>
  <div class="alarm-management-view">
    <div class="page-header">
      <h2>报警管理</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/scada">SCADA系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/scada/alarm-management">报警管理</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/scada/alarm-management#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-tabs v-model="activeTab" class="function-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="报警配置" name="alarm-config" />
      <el-tab-pane label="报警触发" name="alarm-trigger" />
      <el-tab-pane label="报警处理" name="alarm-handle" />
      <el-tab-pane label="报警统计" name="alarm-stats" />
    </el-tabs>
    
    <!-- 报警配置 -->
    <div v-if="activeTab === 'alarm-config'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>报警配置</span>
            <el-button type="primary" size="small" @click="handleAddAlarmRule">添加报警规则</el-button>
          </div>
        </template>
        <div class="alarm-config">
          <el-table :data="alarmRules" style="width: 100%" height="300">
            <el-table-column prop="id" label="规则ID" width="80" />
            <el-table-column prop="tagCode" label="点位编码" width="150" />
            <el-table-column prop="alarmType" label="报警类型" width="120" />
            <el-table-column prop="threshold" label="阈值" width="100" />
            <el-table-column prop="severity" label="优先级" width="100">
              <template #default="scope">
                <el-tag :type="getSeverityColor(scope.row.severity)">
                  {{ scope.row.severity }}级
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-switch
                  v-model="scope.row.status"
                  active-text="启用"
                  inactive-text="禁用"
                  :loading="scope.row.statusLoading"
                  @change="handleToggleRuleStatus(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleEditAlarmRule(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDeleteAlarmRule(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
    
    <!-- 添加/编辑报警规则对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      center
    >
      <el-form ref="ruleFormRef" :model="ruleForm" :rules="rules" label-width="100px">
        <el-form-item label="点位编码" prop="tagCode">
          <el-select v-model="ruleForm.tagCode" placeholder="选择点位" style="width: 100%">
            <el-option label="温度" value="TEMP_001" />
            <el-option label="压力" value="PRESS_001" />
            <el-option label="流量" value="FLOW_001" />
            <el-option label="液位" value="LEVEL_001" />
          </el-select>
        </el-form-item>
        <el-form-item label="报警类型" prop="alarmType">
          <el-select v-model="ruleForm.alarmType" placeholder="选择报警类型" style="width: 100%">
            <el-option label="超上限" value="超上限" />
            <el-option label="超下限" value="超下限" />
            <el-option label="越界" value="越界" />
            <el-option label="突变" value="突变" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值" prop="threshold">
          <el-input v-model.number="ruleForm.threshold" placeholder="输入阈值" type="number" />
        </el-form-item>
        <el-form-item label="优先级" prop="severity">
          <el-select v-model="ruleForm.severity" placeholder="选择优先级" style="width: 100%">
            <el-option label="1级（紧急）" value="1" />
            <el-option label="2级（重要）" value="2" />
            <el-option label="3级（一般）" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="ruleForm.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogCancel">取消</el-button>
          <el-button type="primary" @click="handleDialogConfirm">确认</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 报警触发 -->
    <div v-if="activeTab === 'alarm-trigger'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>报警触发</span>
            <el-button type="primary" size="small" @click="handleTestAlarm">测试触发</el-button>
          </div>
        </template>
        <div class="alarm-trigger-content">
          <el-form :model="alarmTriggerConfig" label-width="120px" class="trigger-form">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="触发条件">
                  <el-select v-model="alarmTriggerConfig.triggerCondition" placeholder="选择触发条件" style="width: 100%;">
                    <el-option label="点位值超限" value="value-exceed" />
                    <el-option label="点位值突变" value="value-change" />
                    <el-option label="点位值持续" value="value-continue" />
                    <el-option label="设备离线" value="device-offline" />
                  </el-select>
                </el-form-item>
                <el-form-item label="触发点位">
                  <el-select v-model="alarmTriggerConfig.tagCode" placeholder="选择点位" style="width: 100%;">
                    <el-option label="温度" value="temperature" />
                    <el-option label="压力" value="pressure" />
                    <el-option label="流量" value="flow" />
                    <el-option label="液位" value="level" />
                  </el-select>
                </el-form-item>
                <el-form-item label="触发值">
                  <el-input v-model="alarmTriggerConfig.triggerValue" placeholder="输入触发值" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="触发持续时间">
                  <el-input-number v-model="alarmTriggerConfig.duration" :min="0" :max="3600" :step="1" style="width: 100%;" />
                  <span style="margin-left: 10px;">秒</span>
                </el-form-item>
                <el-form-item label="触发优先级">
                  <el-select v-model="alarmTriggerConfig.severity" placeholder="选择优先级" style="width: 100%;">
                    <el-option label="1级（紧急）" value="1" />
                    <el-option label="2级（重要）" value="2" />
                    <el-option label="3级（一般）" value="3" />
                  </el-select>
                </el-form-item>
                <el-form-item label="触发方式">
                  <el-checkbox-group v-model="alarmTriggerConfig.triggerMethods">
                    <el-checkbox label="声音报警" />
                    <el-checkbox label="短信通知" />
                    <el-checkbox label="邮件通知" />
                    <el-checkbox label="APP推送" />
                  </el-checkbox-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="handleSaveTriggerConfig">保存配置</el-button>
              <el-button @click="handleResetTriggerConfig">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>
    
    <!-- 报警处理 -->
    <div v-if="activeTab === 'alarm-handle'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>实时报警</span>
            <el-button type="danger" size="small" @click="handleConfirmAllAlarms">确认所有报警</el-button>
          </div>
        </template>
        <div class="alarm-list">
          <div v-for="alarm in activeAlarms" :key="alarm.id" class="alarm-item" :class="`alarm-level-${alarm.severity}`">
            <div class="alarm-info">
              <span class="alarm-tag">{{ alarm.tagCode }}</span>
              <span class="alarm-type">{{ alarm.alarmType }}</span>
              <span class="alarm-time">{{ alarm.triggerTime }}</span>
            </div>
            <div class="alarm-actions">
              <el-button size="small" type="primary" @click="handleConfirmAlarm(alarm)">确认</el-button>
              <el-button size="small" @click="handleMuteAlarm(alarm)">消音</el-button>
            </div>
          </div>
          <el-empty v-if="activeAlarms.length === 0" description="当前无报警" image-size="80" />
        </div>
      </el-card>
    </div>
    
    <!-- 报警统计 -->
    <div v-if="activeTab === 'alarm-stats'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>报警统计</span>
          </div>
        </template>
        <div class="alarm-stats">
          <el-row :gutter="10">
            <el-col :span="12">
              <div class="stat-item">
                <h4>今日报警总数</h4>
                <div class="stat-value">{{ todayAlarms }}</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="stat-item">
                <h4>未确认报警</h4>
                <div class="stat-value" style="color: #F56C6C;">{{ unconfirmedAlarms }}</div>
              </div>
            </el-col>
          </el-row>
          <div class="alarm-top-10">
            <h4>TOP 5 报警点位</h4>
            <div class="top-alarm-list">
              <div v-for="item in topAlarmPoints" :key="item.tagCode" class="top-alarm-item">
                <span>{{ item.tagCode }}</span>
                <el-progress :percentage="item.percentage" :format="topAlarmFormat" />
                <span>{{ item.count }}次</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { unwrapListResponse, unwrapResponseData } from '@/api'
import { scadaAlarmApi, type ScadaAlarmRule } from '@/api/scada'

// 路由实例
const router = useRouter()
const route = useRoute()

// 激活的标签页
const activeTab = ref('alarm-config')

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'alarm-config': '报警配置',
  'alarm-trigger': '报警触发',
  'alarm-handle': '报警处理',
  'alarm-stats': '报警统计'
}

// 报警管理模块
const activeAlarms = ref<any[]>([])

const alarmRules = ref<any[]>([])

const todayAlarms = ref(0)
const unconfirmedAlarms = ref(0)
const topAlarmPoints = ref<Array<{ tagCode: string; count: number; percentage: number }>>([])

// 报警触发配置
const alarmTriggerConfig = ref({
  triggerCondition: 'value-exceed',
  tagCode: 'temperature',
  triggerValue: '',
  duration: 5,
  severity: '2',
  triggerMethods: ['声音报警', 'APP推送']
})

// 报警规则对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('添加报警规则')
const ruleFormRef = ref<FormInstance>()
const isEdit = ref(false)

// 报警规则表单
const ruleForm = reactive({
  id: 0,
  tagCode: '',
  alarmType: '',
  threshold: '',
  severity: '',
  status: true
})

// 表单验证规则
const rules = reactive({
  tagCode: [
    { required: true, message: '请选择点位编码', trigger: 'change' }
  ],
  alarmType: [
    { required: true, message: '请选择报警类型', trigger: 'change' }
  ],
  threshold: [
    { required: true, message: '请输入阈值', trigger: 'blur' },
    { type: 'number', message: '请输入有效的数字', trigger: 'blur' }
  ],
  severity: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ]
})

// 从路由获取标签页状态
const getActiveTabFromRoute = () => {
  const tabMap: Record<string, string> = {
    'alarm-config': 'alarm-config',
    'alarm-trigger': 'alarm-trigger',
    'alarm-handle': 'alarm-handle',
    'alarm-stats': 'alarm-stats'
  }
  const tabName = route.params.tab || 'alarm-config'
  return tabMap[tabName as string] || 'alarm-config'
}

// 组件挂载时，从路由获取标签页状态
onMounted(() => {
  activeTab.value = getActiveTabFromRoute()
  Promise.allSettled([loadActiveAlarms(), loadAlarmRules(), loadAlarmStats(), loadTriggerConfig()]).catch(() => undefined)
})

// 标签页切换事件
const handleTabChange = (tabName: string) => {
  console.log('切换标签页:', tabName)
  // 更新路由，保持标签页状态
  router.push({
    path: `/home/scada/alarm-management/${tabName}`
  })
}

// 计算属性
const getSeverityColor = (severity: number) => {
  switch (severity) {
    case 1: return 'danger'
    case 2: return 'warning'
    case 3: return 'info'
    default: return 'info'
  }
}

const topAlarmFormat = (percentage: number) => {
  return ''
}

// 方法
const handleConfirmAlarm = async (alarm: any) => {
  try {
    await scadaAlarmApi.acknowledge(String(alarm.id))
    await loadActiveAlarms()
    await loadAlarmStats()
    ElMessage({ message: `已确认报警 ${alarm.tagCode}`, type: 'success' })
  } catch (error) {
    console.error('确认报警失败:', error)
    ElMessage({ message: '确认报警失败', type: 'error' })
  }
}

const handleMuteAlarm = async (alarm: any) => {
  try {
    await scadaAlarmApi.mute(String(alarm.id))
    ElMessage({ message: `已消音报警 ${alarm.tagCode}`, type: 'success' })
  } catch (error) {
    console.error('消音报警失败:', error)
    ElMessage({ message: '消音报警失败', type: 'error' })
  }
}

// 重置表单
const resetForm = () => {
  ruleForm.id = 0
  ruleForm.tagCode = ''
  ruleForm.alarmType = ''
  ruleForm.threshold = ''
  ruleForm.severity = ''
  ruleForm.status = true
}

// 打开添加报警规则对话框
const handleAddAlarmRule = () => {
  dialogTitle.value = '添加报警规则'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 打开编辑报警规则对话框
const handleEditAlarmRule = (rule: any) => {
  dialogTitle.value = '编辑报警规则'
  isEdit.value = true
  // 填充表单数据
  ruleForm.id = rule.id
  ruleForm.tagCode = rule.tagCode
  ruleForm.alarmType = rule.alarmType
  ruleForm.threshold = rule.threshold
  ruleForm.severity = rule.severity.toString()
  ruleForm.status = rule.status
  dialogVisible.value = true
}

// 删除报警规则
const handleDeleteAlarmRule = async (rule: any) => {
  try {
    await scadaAlarmApi.removeRule(rule.id)
    await loadAlarmRules()
    ElMessage({ message: `已删除报警规则 ${rule.tagCode}`, type: 'success' })
  } catch (error) {
    console.error('删除报警规则失败:', error)
    ElMessage({ message: '删除报警规则失败', type: 'error' })
  }
}

// 对话框确认
const handleDialogConfirm = async () => {
  if (!ruleFormRef.value) return
  
  try {
    // 表单验证
    await ruleFormRef.value.validate()
    
    const payload: ScadaAlarmRule = {
      tagCode: ruleForm.tagCode,
      alarmName: ruleForm.alarmType,
      severity: Number(ruleForm.severity),
      enabled: ruleForm.status,
      high: Number(ruleForm.threshold)
    }

    if (isEdit.value) {
      await scadaAlarmApi.updateRule(ruleForm.id, payload)
      ElMessage({ message: '报警规则编辑成功', type: 'success' })
    } else {
      await scadaAlarmApi.createRule(payload)
      ElMessage({ message: '报警规则添加成功', type: 'success' })
    }

    await loadAlarmRules()
    
    // 关闭对话框
    dialogVisible.value = false
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage({ message: '操作失败', type: 'error' })
  }
}

// 对话框取消
const handleDialogCancel = () => {
  dialogVisible.value = false
  if (ruleFormRef.value) {
    ruleFormRef.value.resetFields()
  }
}

const handleConfirmAllAlarms = async () => {
  try {
    await scadaAlarmApi.acknowledgeAll()
    await loadActiveAlarms()
    await loadAlarmStats()
    ElMessage({ message: '已确认所有报警', type: 'success' })
  } catch (error) {
    console.error('确认所有报警失败:', error)
    ElMessage({ message: '确认所有报警失败', type: 'error' })
  }
}

const handleTestAlarm = async () => {
  try {
    await scadaAlarmApi.testTrigger(alarmTriggerConfig.value)
    await loadActiveAlarms()
    await loadAlarmStats()
    ElMessage({ message: '报警触发测试成功', type: 'success' })
  } catch (error) {
    console.error('测试报警触发失败:', error)
    ElMessage({ message: '报警触发测试失败', type: 'error' })
  }
}

const handleSaveTriggerConfig = async () => {
  try {
    await scadaAlarmApi.saveTriggerConfig(alarmTriggerConfig.value)
    ElMessage({ message: '报警触发配置保存成功', type: 'success' })
  } catch (error) {
    console.error('保存报警触发配置失败:', error)
    ElMessage({ message: '报警触发配置保存失败', type: 'error' })
  }
}

const extractListData = (response: any) => {
  return unwrapListResponse<any>(response)
}

const loadActiveAlarms = async () => {
  const response = await scadaAlarmApi.listActive()
  activeAlarms.value = extractListData(response)
  unconfirmedAlarms.value = activeAlarms.value.length
}

// 加载报警统计数据（今日总数/未确认数/TOP5点位）
const loadAlarmStats = async () => {
  try {
    const response = await scadaAlarmApi.getStats()
    const data = unwrapResponseData<any>(response) || {}
    todayAlarms.value = Number(data.todayTotal) || 0
    unconfirmedAlarms.value = Number(data.unconfirmed) || 0
    const topList: Array<{ tagCode: string; count: number }> = Array.isArray(data.topTags) ? data.topTags : []
    const maxCount = topList.reduce((max, item) => Math.max(max, Number(item.count) || 0), 0)
    topAlarmPoints.value = topList.map(item => ({
      tagCode: item.tagCode,
      count: Number(item.count) || 0,
      percentage: maxCount > 0 ? Math.round(((Number(item.count) || 0) / maxCount) * 1000) / 10 : 0
    }))
  } catch (error) {
    console.error('加载报警统计失败:', error)
  }
}

// 加载报警触发配置（后端持久化JSON回显）
const loadTriggerConfig = async () => {
  try {
    const response = await scadaAlarmApi.getTriggerConfig()
    const data = unwrapResponseData<any>(response)
    if (data && typeof data === 'object' && Object.keys(data).length > 0) {
      alarmTriggerConfig.value = { ...alarmTriggerConfig.value, ...data }
    }
  } catch (error) {
    console.error('加载报警触发配置失败:', error)
  }
}

const loadAlarmRules = async () => {
  const response = await scadaAlarmApi.listRules()
  alarmRules.value = extractListData(response).map((item: any) => {
    return {
      ...item,
      alarmType: item.alarmType || item.alarmName || '',
      threshold: item.threshold ?? item.high ?? item.highHigh ?? '',
      status: item.status ?? item.enabled ?? true
    }
  })
}

// 切换报警规则启用状态并持久化到后端
const handleToggleRuleStatus = async (row: any) => {
  row.statusLoading = true
  try {
    const payload: ScadaAlarmRule = {
      tagCode: row.tagCode,
      alarmName: row.alarmName || row.alarmType,
      severity: Number(row.severity),
      enabled: row.status,
      highHigh: row.highHigh,
      high: row.high,
      low: row.low,
      lowLow: row.lowLow,
      deadband: row.deadband,
      remark: row.remark
    }
    await scadaAlarmApi.updateRule(row.id, payload)
    ElMessage({ message: `规则已${row.status ? '启用' : '禁用'}`, type: 'success' })
  } catch (error) {
    console.error('切换规则状态失败:', error)
    ElMessage({ message: '切换规则状态失败', type: 'error' })
    // 失败时回滚开关状态
    row.status = !row.status
  } finally {
    row.statusLoading = false
  }
}

const handleResetTriggerConfig = () => {
  try {
    console.log('重置报警触发配置')
    // 重置表单
    alarmTriggerConfig.value = {
      triggerCondition: 'value-exceed',
      tagCode: 'temperature',
      triggerValue: '',
      duration: 5,
      severity: '2',
      triggerMethods: ['声音报警', 'APP推送']
    }
    // 显示成功消息
    ElMessage({ message: '报警触发配置已重置', type: 'success' })
  } catch (error) {
    console.error('重置报警触发配置失败:', error)
    ElMessage({ message: '报警触发配置重置失败', type: 'error' })
  }
}
</script>

<style scoped>
.alarm-management-view {
  padding: 20px;
  height: 100%;
  overflow: auto;
}

/* 页面头部样式 */
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color-light);
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-size: 1.8rem;
}

/* 覆盖默认的h2样式，确保只影响页面标题 */
h2 {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.sub-card {
  margin-bottom: 20px;
}

.sub-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1rem;
}

.sub-card-content {
  padding: 10px 0;
}

.alarm-list {
  padding: 10px 0;
}

.alarm-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  margin-bottom: 10px;
  background-color: #f5f7fa;
  border-left: 4px solid #E6A23C;
  border-radius: 4px;
}

.alarm-item.alarm-level-1 {
  border-left-color: #F56C6C;
  background-color: rgba(245, 108, 108, 0.1);
}

.alarm-item.alarm-level-2 {
  border-left-color: #E6A23C;
  background-color: rgba(230, 162, 60, 0.1);
}

.alarm-item.alarm-level-3 {
  border-left-color: #909399;
  background-color: rgba(144, 147, 153, 0.1);
}

.alarm-info {
  display: flex;
  gap: 15px;
}

.alarm-tag {
  font-weight: bold;
}

.alarm-type {
  color: #606266;
}

.alarm-time {
  color: #909399;
  font-size: 12px;
}

.alarm-actions {
  display: flex;
  gap: 5px;
}

.alarm-stats {
  padding: 10px 0;
}

.stat-item {
  text-align: center;
}

.stat-item h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.alarm-top-10 {
  margin-top: 20px;
}

.alarm-top-10 h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}

.top-alarm-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.alarm-trigger-content {
  height: 300px;
  background-color: #f5f7fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-content {
  padding: 10px 0;
}
</style>
