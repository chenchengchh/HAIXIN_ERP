<template>
  <div class="report-management-view">
    <div class="page-header">
      <h2>报表管理</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/ems">EMS系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/ems/report-management">报表管理</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/ems/report-management#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-tabs v-model="activeTab" class="function-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="标准报表" name="standard-reports" />
      <el-tab-pane label="自定义报表" name="custom-reports" />
      <el-tab-pane label="自动生成" name="auto-generation" />
      <el-tab-pane label="报表导出" name="report-export" />
    </el-tabs>
    
    <!-- 标准报表 -->
    <div v-if="activeTab === 'standard-reports'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>标准报表</span>
            <div class="filter-controls">
              <el-button type="primary" size="small" @click="refreshData">刷新数据</el-button>
            </div>
          </div>
        </template>
        <div class="standard-reports-content">
          <el-table v-loading="emsStore.loading.standardReports" :data="emsStore.standardReports" style="width: 100%" height="400">
            <el-table-column prop="id" label="报表ID" width="100" />
            <el-table-column prop="name" label="报表名称" width="180" />
            <el-table-column prop="type" label="报表类型" width="120" />
            <el-table-column prop="frequency" label="生成频率" width="120" />
            <el-table-column prop="lastGenerated" label="最后生成时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">
                  {{ scope.row.status === 'active' ? '激活' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleGenerateReport(scope.row)">生成</el-button>
                <el-button size="small" @click="handleDownloadReport(scope.row)">下载</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
    
    <!-- 自定义报表 -->
    <div v-if="activeTab === 'custom-reports'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>自定义报表</span>
            <div class="filter-controls">
              <el-button type="primary" size="small" @click="handleCreateCustomReport">创建报表</el-button>
              <el-button type="primary" size="small" @click="refreshData" style="margin-left: 10px;">刷新数据</el-button>
            </div>
          </div>
        </template>
        <div class="custom-reports-content">
          <el-table :data="emsStore.customReports" style="width: 100%" height="400">
            <el-table-column prop="id" label="报表ID" width="100" />
            <el-table-column prop="name" label="报表名称" width="180" />
            <el-table-column prop="creator" label="创建人" width="120" />
            <el-table-column prop="createdDate" label="创建日期" width="150" />
            <el-table-column prop="lastModified" label="最后修改" width="150" />
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleEditCustomReport(scope.row)">编辑</el-button>
                <el-button size="small" @click="handleDeleteCustomReport(scope.row)">删除</el-button>
                <el-button size="small" @click="handleExecuteCustomReport(scope.row)">执行</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
    
    <!-- 自动生成 -->
    <div v-if="activeTab === 'auto-generation'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>自动生成</span>
            <div class="filter-controls">
              <el-button type="primary" size="small" @click="refreshData">刷新数据</el-button>
            </div>
          </div>
        </template>
        <div class="auto-generation-content">
          <el-table :data="emsStore.autoGenerationTasks" style="width: 100%" height="400">
            <el-table-column prop="id" label="任务ID" width="100" />
            <el-table-column prop="reportName" label="报表名称" width="180" />
            <el-table-column prop="frequency" label="执行频率" width="120" />
            <el-table-column prop="nextExecution" label="下次执行时间" width="180" />
            <el-table-column prop="recipients" label="接收人" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'enabled' ? 'success' : 'danger'">
                  {{ scope.row.status === 'enabled' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleToggleTask(scope.row)">
                  {{ scope.row.status === 'enabled' ? '停用' : '启用' }}
                </el-button>
                <el-button size="small" @click="handleEditTask(scope.row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
    
    <!-- 报表导出 -->
    <div v-if="activeTab === 'report-export'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>报表导出</span>
            <div class="filter-controls">
              <el-button type="primary" size="small" @click="refreshData">刷新数据</el-button>
            </div>
          </div>
        </template>
        <div class="export-content">
          <el-form :model="exportForm" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="报表选择">
                  <el-select v-model="exportForm.reportId" placeholder="选择报表" style="width: 100%;">
                    <el-option 
                      v-for="report in emsStore.standardReports" 
                      :key="report.id" 
                      :label="report.name" 
                      :value="report.id.toString()" 
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="导出格式">
                  <el-select v-model="exportForm.format" placeholder="选择格式" style="width: 100%;">
                    <el-option label="Excel" value="excel" />
                    <el-option label="PDF" value="pdf" />
                    <el-option label="CSV" value="csv" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="日期范围">
                  <el-date-picker v-model="exportForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 100%;"></el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="导出范围">
                  <el-select v-model="exportForm.range" placeholder="选择范围" style="width: 100%;">
                    <el-option label="全部数据" value="all" />
                    <el-option label="指定区域" value="specific" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item>
                  <el-button type="primary" @click="handleExport">立即导出</el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          
          <!-- 导出历史 -->
          <div class="export-history">
            <h4>导出历史</h4>
            <el-table :data="emsStore.exportHistory" style="width: 100%" height="200">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="reportName" label="报表名称" width="180" />
              <el-table-column prop="format" label="格式" width="100" />
              <el-table-column prop="exportTime" label="导出时间" width="180" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'">
                    {{ scope.row.status === 'success' ? '成功' : '失败' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="handleDownload(scope.row)" v-if="scope.row.status === 'success'">下载</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="customDialogVisible" :title="customDialogMode === 'create' ? '创建自定义报表' : '编辑自定义报表'" width="600px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="报表名称">
          <el-input v-model="customReportForm.name" />
        </el-form-item>
        <el-form-item label="创建人">
          <el-input v-model="customReportForm.creator" />
        </el-form-item>
        <el-form-item label="SQL">
          <el-input v-model="customReportForm.sqlQuery" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="模板配置">
          <el-input v-model="customReportForm.templateConfig" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="customDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCustomReport">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="taskDialogVisible" title="编辑自动生成任务" width="600px" destroy-on-close>
      <el-form label-width="120px">
        <el-form-item label="报表名称">
          <el-input v-model="taskForm.reportName" />
        </el-form-item>
        <el-form-item label="执行频率">
          <el-input v-model="taskForm.frequency" />
        </el-form-item>
        <el-form-item label="下次执行时间">
          <el-input v-model="taskForm.nextExecution" placeholder="例如 2026-01-01T08:00:00" />
        </el-form-item>
        <el-form-item label="接收人">
          <el-input v-model="taskForm.recipients" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="taskForm.status" style="width: 100%;">
            <el-option label="启用" value="enabled" />
            <el-option label="停用" value="disabled" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTask">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useEmsStore } from '@/stores/ems'
import api from '@/api'

// 路由实例
const router = useRouter()
const route = useRoute()

// 激活的标签页
const activeTab = ref('standard-reports')

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'standard-reports': '标准报表',
  'custom-reports': '自定义报表',
  'auto-generation': '自动生成',
  'report-export': '报表导出'
}

// 状态管理
const emsStore = useEmsStore()

// 报表导出表单
const exportForm = ref({
  reportId: '',
  format: 'excel' as 'excel' | 'pdf' | 'csv',
  dateRange: [] as string[],
  range: 'all' as 'all' | 'specific'
})

// 从路由获取标签页状态
const getActiveTabFromRoute = () => {
  const tabMap: Record<string, string> = {
    'standard-reports': 'standard-reports',
    'custom-reports': 'custom-reports',
    'auto-generation': 'auto-generation',
    'report-export': 'report-export'
  }
  const tabName = route.params.tab || 'standard-reports'
  return tabMap[tabName as string] || 'standard-reports'
}

// 组件挂载时，从路由获取标签页状态并加载数据
onMounted(async () => {
  activeTab.value = getActiveTabFromRoute()
  
  // 加载报表管理相关数据
  await Promise.all([
    emsStore.fetchStandardReports(),
    emsStore.fetchCustomReports(),
    emsStore.fetchAutoGenerationTasks(),
    emsStore.fetchExportHistory()
  ])
})

// 标签页切换事件
const handleTabChange = (tabName: string) => {
  console.log('切换标签页:', tabName)
  // 更新路由，保持标签页状态
  router.push({
    path: `/home/ems/report-management/${tabName}`
  })
}

// 方法
/**
 * 生成标准报表
 * @param report 报表信息
 */
const handleGenerateReport = async (report: any) => {
  console.log('生成报表:', report)
  try {
    const result = await emsStore.generateStandardReport(report.id)
    if (result) {
      ElMessage.success('报表生成成功')
    } else {
      ElMessage.error('报表生成失败')
    }
  } catch (error) {
    console.error('生成报表失败:', error)
    ElMessage.error('报表生成失败')
  }
}

/**
 * 下载报表
 * @param report 报表信息
 */
const handleDownloadReport = async (report: any) => {
  try {
    const response = await emsStore.downloadReport(report.id, 'csv')
    if (!response) {
      ElMessage.error('下载失败')
      return
    }
    downloadBlobResponse(response, `EMS_${report.name || '报表'}_${new Date().toISOString().split('T')[0]}.csv`)
    ElMessage.success(`下载成功: ${report.name}`)
  } catch (error) {
    console.error('下载报表失败:', error)
    ElMessage.error('下载报表失败')
  }
}

/**
 * 创建自定义报表
 */
const customDialogVisible = ref(false)
const customDialogMode = ref<'create' | 'edit'>('create')
const customReportForm = reactive({
  id: 0,
  name: '',
  creator: '管理员',
  sqlQuery: '',
  templateConfig: ''
})

const handleCreateCustomReport = () => {
  customDialogMode.value = 'create'
  customReportForm.id = 0
  customReportForm.name = ''
  customReportForm.creator = '管理员'
  customReportForm.sqlQuery = ''
  customReportForm.templateConfig = ''
  customDialogVisible.value = true
}

/**
 * 编辑自定义报表
 * @param report 自定义报表信息
 */
const handleEditCustomReport = (report: any) => {
  customDialogMode.value = 'edit'
  customReportForm.id = Number(report.id)
  customReportForm.name = report.name || ''
  customReportForm.creator = report.creator || '管理员'
  customReportForm.sqlQuery = report.sqlQuery || ''
  customReportForm.templateConfig = report.templateConfig || ''
  customDialogVisible.value = true
}

/**
 * 删除自定义报表
 * @param report 自定义报表信息
 */
const handleDeleteCustomReport = async (report: any) => {
  try {
    await ElMessageBox.confirm(`确定删除自定义报表「${report.name}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const ok = await emsStore.deleteCustomReport(Number(report.id))
    if (!ok) {
      ElMessage.error('删除失败')
      return
    }
    ElMessage.success('删除成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除自定义报表失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

/**
 * 执行自定义报表
 * @param report 自定义报表信息
 */
const handleExecuteCustomReport = async (report: any) => {
  try {
    const response = await emsStore.executeCustomReport(Number(report.id), {})
    if (!response) {
      ElMessage.error('执行失败')
      return
    }
    downloadBlobResponse(response, `EMS_自定义报表_${report.name || report.id}_${new Date().toISOString().split('T')[0]}.csv`)
    ElMessage.success('执行成功')
  } catch (error) {
    console.error('执行自定义报表失败:', error)
    ElMessage.error('执行失败')
  }
}

const handleSaveCustomReport = async () => {
  try {
    const payload = {
      name: customReportForm.name,
      creator: customReportForm.creator,
      sqlQuery: customReportForm.sqlQuery,
      templateConfig: customReportForm.templateConfig
    }
    const result =
      customDialogMode.value === 'create'
        ? await emsStore.createCustomReport(payload)
        : await emsStore.updateCustomReport(customReportForm.id, payload)
    if (!result) {
      ElMessage.error('保存失败')
      return
    }
    customDialogVisible.value = false
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存自定义报表失败:', error)
    ElMessage.error('保存失败')
  }
}

/**
 * 切换自动生成任务状态
 * @param task 自动生成任务信息
 */
const handleToggleTask = async (task: any) => {
  console.log('切换任务状态:', task)
  try {
    await emsStore.toggleTaskStatus(task.id, task.status === 'enabled' ? 'disabled' : 'enabled')
    ElMessage.success('任务状态切换成功')
  } catch (error) {
    console.error('切换任务状态失败:', error)
    ElMessage.error('切换任务状态失败')
  }
}

/**
 * 编辑自动生成任务
 * @param task 自动生成任务信息
 */
const taskDialogVisible = ref(false)
const taskForm = reactive({
  id: 0,
  reportName: '',
  frequency: '',
  nextExecution: '',
  recipients: '',
  status: 'enabled'
})

const handleEditTask = (task: any) => {
  taskForm.id = Number(task.id)
  taskForm.reportName = task.reportName || ''
  taskForm.frequency = task.frequency || ''
  taskForm.nextExecution = task.nextExecution || ''
  taskForm.recipients = task.recipients || ''
  taskForm.status = task.status || 'enabled'
  taskDialogVisible.value = true
}

const handleSaveTask = async () => {
  try {
    const payload = {
      reportName: taskForm.reportName,
      frequency: taskForm.frequency,
      nextExecution: taskForm.nextExecution || null,
      recipients: taskForm.recipients,
      status: taskForm.status
    }
    const result = await emsStore.updateAutoGenerationTask(taskForm.id, payload)
    if (!result) {
      ElMessage.error('保存失败')
      return
    }
    taskDialogVisible.value = false
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存任务失败:', error)
    ElMessage.error('保存失败')
  }
}

/**
 * 立即导出报表
 */
const handleExport = async () => {
  console.log('立即导出:', exportForm.value)
  if (!exportForm.value.reportId) {
    ElMessage.warning('请选择要导出的报表')
    return
  }
  
  try {
    await emsStore.exportReport({
      reportId: parseInt(exportForm.value.reportId),
      format: exportForm.value.format,
      dateRange: exportForm.value.dateRange,
      range: exportForm.value.range
    })
    ElMessage.success('报表导出成功')
    // 导出成功后清除导出历史列表缓存并刷新（避免GET缓存导致新记录不显示）
    api.clearCache('/api/v1/ems/reports/export-history')
    await emsStore.fetchExportHistory()
  } catch (error) {
    console.error('立即导出失败:', error)
    ElMessage.error('报表导出失败')
  }
}

/**
 * 重置导出表单
 */
const handleReset = () => {
  exportForm.value = {
    reportId: '',
    format: 'excel',
    dateRange: [],
    range: 'all'
  }
}

/**
 * 下载历史报表
 * @param history 导出历史记录
 */
const handleDownload = async (history: any) => {
  try {
    const response = await emsStore.downloadFromHistory(Number(history.id))
    if (!response) {
      ElMessage.error('下载失败')
      return
    }
    const name = history.reportName || 'EMS_历史报表'
    const ext = history.format || 'csv'
    downloadBlobResponse(response, `${name}_${new Date().toISOString().split('T')[0]}.${ext}`)
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载历史报表失败:', error)
    ElMessage.error('下载失败')
  }
}

/**
 * 刷新数据
 */
const refreshData = async () => {
  await Promise.all([
    emsStore.fetchStandardReports(),
    emsStore.fetchCustomReports(),
    emsStore.fetchAutoGenerationTasks(),
    emsStore.fetchExportHistory()
  ])
  ElMessage.success('数据刷新成功')
}

const downloadBlobResponse = (response: any, fileName: string) => {
  const blob = new Blob([response.data], { type: response.headers?.['content-type'] || 'application/octet-stream' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.report-management-view {
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

.tab-content {
  padding: 10px 0;
}

.standard-reports-content {
  padding: 10px 0;
}

.custom-reports-content {
  padding: 10px 0;
}

.auto-generation-content {
  padding: 10px 0;
}

.export-content {
  padding: 10px 0;
}

.export-history {
  margin-top: 20px;
}

.export-history h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .report-management-view {
    padding: 12px;
  }
  
  .sub-card {
    margin-bottom: 12px;
  }
  
  .sub-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
