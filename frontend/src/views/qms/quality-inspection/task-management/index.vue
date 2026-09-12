<template>
  <div class="task-management-view">
    <div class="page-header">
      <h3>检验任务分配</h3>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAddTask">
          手动分配任务
        </el-button>
        <el-button type="success" :icon="Refresh" @click="handleAutoAssign">
          智能分配
        </el-button>
        <el-button :icon="Download" @click="handleExport">
          导出任务
        </el-button>
      </div>
    </div>

    <!-- 查询条件 -->
    <el-card class="query-card">
      <el-form :model="queryForm" label-position="left" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="任务编号">
              <el-input v-model="queryForm.taskNo" placeholder="请输入任务编号" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="物料名称">
              <el-input v-model="queryForm.materialName" placeholder="请输入物料名称" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="任务状态">
              <el-select v-model="queryForm.status" placeholder="请选择任务状态">
                <el-option label="待执行" value="pending" />
                <el-option label="执行中" value="executing" />
                <el-option label="已完成" value="completed" />
                <el-option label="已驳回" value="rejected" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="分配人员">
              <el-input v-model="queryForm.assignee" placeholder="请输入分配人员" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="任务类型">
              <el-select v-model="queryForm.taskType" placeholder="请选择任务类型">
                <el-option label="入厂检验" value="incoming" />
                <el-option label="过程检验" value="process" />
                <el-option label="出厂检验" value="outgoing" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="创建日期">
              <el-date-picker
                v-model="queryForm.createDate"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="query-actions">
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 检验任务列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="taskList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="taskNo" label="任务编号" min-width="150" />
        <el-table-column prop="taskName" label="任务名称" min-width="180" />
        <el-table-column prop="materialCode" label="物料编码" min-width="120" />
        <el-table-column prop="materialName" label="物料名称" min-width="180" />
        <el-table-column prop="taskType" label="任务类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getTaskTypeColor(scope.row.taskType)">
              {{ getTaskTypeName(scope.row.taskType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="planNo" label="关联计划" min-width="150" />
        <el-table-column prop="batchNo" label="批次号" min-width="150" />
        <el-table-column prop="quantity" label="检验数量" width="100" align="center" />
        <el-table-column prop="assignee" label="分配人员" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusColor(scope.row.status)">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" align="center" />
        <el-table-column prop="deadline" label="截止时间" min-width="180" align="center" />
        <el-table-column prop="actualFinishTime" label="实际完成时间" min-width="180" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleViewTask(scope.row)">
              查看
            </el-button>
            <el-button size="small" v-if="scope.row.status === 'pending'" type="success" @click="handleAssignTask(scope.row)">
              分配
            </el-button>
            <el-button size="small" v-if="scope.row.status === 'pending'" type="danger" @click="handleCancelTask(scope.row)">
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="任务详情"
      width="800px"
      @close="handleDetailDialogClose"
    >
      <div v-if="selectedTask" class="task-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="任务编号">{{ selectedTask.taskNo }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ selectedTask.taskName }}</el-descriptions-item>
          <el-descriptions-item label="物料编码">{{ selectedTask.materialCode }}</el-descriptions-item>
          <el-descriptions-item label="物料名称">{{ selectedTask.materialName }}</el-descriptions-item>
          <el-descriptions-item label="任务类型">{{ getTaskTypeName(selectedTask.taskType) }}</el-descriptions-item>
          <el-descriptions-item label="关联计划">{{ selectedTask.planNo }}</el-descriptions-item>
          <el-descriptions-item label="批次号">{{ selectedTask.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="检验数量">{{ selectedTask.quantity }}</el-descriptions-item>
          <el-descriptions-item label="分配人员">{{ selectedTask.assignee }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ getStatusName(selectedTask.status) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ selectedTask.createTime }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ selectedTask.deadline }}</el-descriptions-item>
          <el-descriptions-item label="实际完成时间">{{ selectedTask.actualFinishTime || '未完成' }}</el-descriptions-item>
          <el-descriptions-item label="任务描述">{{ selectedTask.description }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 任务分配对话框 -->
    <el-dialog
      v-model="assignDialogVisible"
      title="分配任务"
      width="600px"
      @close="handleAssignDialogClose"
    >
      <el-form
        ref="assignFormRef"
        :model="assignForm"
        label-position="top"
        :rules="assignFormRules"
      >
        <el-form-item label="任务编号" prop="taskNo">
          <el-input v-model="assignForm.taskNo" disabled />
        </el-form-item>
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="assignForm.taskName" disabled />
        </el-form-item>
        <el-form-item label="分配人员" prop="assignee">
          <el-select v-model="assignForm.assignee" placeholder="请选择分配人员">
            <el-option label="张三" value="张三" />
            <el-option label="李四" value="李四" />
            <el-option label="王五" value="王五" />
            <el-option label="赵六" value="赵六" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker
            v-model="assignForm.deadline"
            type="datetime"
            placeholder="请选择截止时间"
          />
        </el-form-item>
        <el-form-item label="分配说明" prop="assignNote">
          <el-input
            v-model="assignForm.assignNote"
            type="textarea"
            :rows="4"
            placeholder="请输入分配说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="assignDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveAssign">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Download } from '@element-plus/icons-vue'
import { unwrapPageResponse } from '@/api'
import type { InspectionTask } from '../../../../types/qms'
import { InspectionTaskAPI } from '@/api/qms'

// 查询表单
const queryForm = reactive({
  taskNo: '',
  materialName: '',
  status: '',
  assignee: '',
  taskType: '',
  createDate: [] as Date[]
})

// 加载状态
const loading = ref(false)

// 检验任务列表
const taskList = ref<any[]>([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 选中项
const selectedTasks = ref<InspectionTask[]>([])
const selectedTask = ref<InspectionTask | null>(null)

// 对话框状态
const detailDialogVisible = ref(false)
const assignDialogVisible = ref(false)
const assignFormRef = ref()

// 分配表单数据
const assignForm = reactive({
  taskNo: '',
  taskName: '',
  assignee: '',
  deadline: '',
  assignNote: ''
})

// 分配表单验证规则
const assignFormRules = {
  assignee: [{ required: true, message: '请选择分配人员', trigger: 'change' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
}

// 初始化
onMounted(handleQuery)

// 查询数据
async function handleQuery() {
  loading.value = true
  try {
    const res = await InspectionTaskAPI.getInspectionTasks({
      page: pagination.currentPage,
      size: pagination.pageSize,
      taskNo: queryForm.taskNo || undefined,
      status: queryForm.status || undefined,
      assignee: queryForm.assignee || undefined,
      taskType: queryForm.taskType || undefined,
      materialName: queryForm.materialName || undefined
    } as any)
    const page = unwrapPageResponse<InspectionTask>(res)
    taskList.value = page.list
    pagination.total = page.total
  } catch (e: any) {
    ElMessage.error(e?.message || '获取检验任务列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  Object.assign(queryForm, {
    taskNo: '',
    materialName: '',
    status: '',
    assignee: '',
    taskType: '',
    createDate: [] as Date[]
  })
  handleQuery()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  handleQuery()
}

// 当前页变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  handleQuery()
}

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedTasks.value = selection
}

// 任务类型名称映射
const getTaskTypeName = (type: string) => {
  const typeMap: Record<string, string> = {
    'incoming': '入厂检验',
    'process': '过程检验',
    'outgoing': '出厂检验'
  }
  return typeMap[type] || type
}

// 任务类型颜色映射
const getTaskTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'incoming': 'primary',
    'process': 'success',
    'outgoing': 'warning'
  }
  return colorMap[type] || 'default'
}

// 状态名称映射
const getStatusName = (status: string) => {
  const statusMap: Record<string, string> = {
    'pending': '待执行',
    'executing': '执行中',
    'completed': '已完成',
    'rejected': '已驳回'
  }
  return statusMap[status] || status
}

// 状态颜色映射
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'pending': 'info',
    'executing': 'warning',
    'completed': 'success',
    'rejected': 'danger'
  }
  return colorMap[status] || 'default'
}

// 新增检验任务
const handleAddTask = async () => {
  try {
    const { value: taskName } = await ElMessageBox.prompt('请输入任务名称', '新增检验任务', {
      confirmButtonText: '创建',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：来料检验任务'
    })
    const payload: any = {
      taskNo: `QMS-TASK-${Date.now()}`,
      taskName,
      materialName: queryForm.materialName || '',
      taskType: queryForm.taskType || 'incoming',
      assignee: queryForm.assignee || '',
      status: 'pending',
      dueTime: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
      description: ''
    }
    await InspectionTaskAPI.createInspectionTask(payload)
    ElMessage.success('创建成功')
    await handleQuery()
  } catch (e: any) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e?.message || '创建失败')
  }
}

// 智能分配任务
const handleAutoAssign = async () => {
  try {
    await InspectionTaskAPI.autoAssignInspectionTasks({
      taskType: queryForm.taskType || undefined,
      assignee: queryForm.assignee || undefined
    } as any)
    ElMessage.success('智能分配任务已执行')
    await handleQuery()
  } catch (e: any) {
    ElMessage.error(e?.message || '智能分配失败')
  }
}

// 查看任务详情
const handleViewTask = (row: any) => {
  selectedTask.value = JSON.parse(JSON.stringify(row))
  detailDialogVisible.value = true
}

// 分配任务
const handleAssignTask = (row: any) => {
  selectedTask.value = JSON.parse(JSON.stringify(row))
  Object.assign(assignForm, {
    taskNo: row.taskNo,
    taskName: row.taskName,
    assignee: row.assignee,
    deadline: row.deadline,
    assignNote: ''
  })
  assignDialogVisible.value = true
}

// 取消任务
const handleCancelTask = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认取消任务【${row.taskNo}】吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    await InspectionTaskAPI.cancelInspectionTask(row.id)
    ElMessage.success(`任务 ${row.taskNo} 已取消`)
    await handleQuery()
  } catch (e: any) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e?.message || '取消失败')
  }
}

// 保存分配
const handleSaveAssign = () => {
  if (!assignFormRef.value) return
  
  assignFormRef.value.validate((valid: boolean) => {
    if (valid) {
      handleSaveAssignInternal()
    }
  })
}

const handleSaveAssignInternal = async () => {
  if (!selectedTask.value) return
  try {
    loading.value = true
    await InspectionTaskAPI.assignInspectionTask(selectedTask.value.id as any, {
      assignee: assignForm.assignee
    })
    const dueTime = assignForm.deadline ? new Date(assignForm.deadline).toISOString() : undefined
    await InspectionTaskAPI.updateInspectionTask(selectedTask.value.id as any, {
      ...selectedTask.value,
      assignee: assignForm.assignee,
      dueTime
    } as any)
    assignDialogVisible.value = false
    ElMessage.success('任务分配成功')
    await handleQuery()
  } catch (e: any) {
    ElMessage.error(e?.message || '任务分配失败')
  } finally {
    loading.value = false
  }
}

// 关闭详情对话框
const handleDetailDialogClose = () => {
  selectedTask.value = null
}

// 关闭分配对话框
const handleAssignDialogClose = () => {
  if (assignFormRef.value) {
    assignFormRef.value.resetFields()
  }
}

// 导出任务
const handleExport = () => {
  if (!taskList.value.length) {
    ElMessage.warning('暂无可导出的数据')
    return
  }
  const headers = ['任务编号', '任务名称', '物料编码', '物料名称', '任务类型', '分配人', '状态', '创建时间', '截止时间']
  const rows = taskList.value.map((r: any) => [
    r.taskNo,
    r.taskName,
    r.materialCode,
    r.materialName,
    r.taskType,
    r.assignee,
    r.status,
    r.createTime || r.createdTime,
    r.deadline || r.dueTime
  ])
  const csv = [headers, ...rows].map(line => line.map(v => `"${String(v ?? '').replace(/"/g, '""')}"`).join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `inspection-tasks-${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.task-management-view {
  padding: 20px;
  height: 100%;
  overflow: auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.query-card {
  margin-bottom: 20px;
}

.query-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.task-detail {
  margin-bottom: 20px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .task-management-view {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .task-management-view {
    padding: 12px;
  }
  
  .header-actions {
    flex-direction: column;
  }
}
</style>
