<template>
  <div class="anomaly-disposal-view">
    <div class="page-header">
      <h3>异常处理</h3>
    </div>

    <!-- 查询表单 -->
    <el-card class="mb-4" shadow="hover">
      <el-form :model="queryForm" inline>
        <el-form-item label="异常编号">
          <el-input v-model="queryForm.reportNo" placeholder="请输入异常编号" clearable />
        </el-form-item>
        <el-form-item label="异常类型">
          <el-select v-model="queryForm.anomalyType" placeholder="请选择异常类型" clearable>
            <el-option label="物料异常" value="material" />
            <el-option label="生产异常" value="production" />
            <el-option label="检验异常" value="inspection" />
            <el-option label="客户投诉" value="complaint" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="queryForm.disposalStatus" placeholder="请选择处理状态" clearable>
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 异常处理列表 -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>异常处理列表</span>
          <el-tag size="small" type="info">{{ total }} 条记录</el-tag>
        </div>
      </template>
      <el-table :data="anomalyDisposals" border style="width: 100%">
        <el-table-column prop="reportNo" label="异常编号" min-width="120" />
        <el-table-column prop="title" label="异常标题" min-width="180" />
        <el-table-column prop="anomalyType" label="异常类型" min-width="100">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.anomalyType)">
              {{ getTypeLabel(scope.row.anomalyType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="disposalPlan" label="处理方案" min-width="200" show-overflow-tooltip />
        <el-table-column prop="handler" label="处理人" min-width="100" />
        <el-table-column prop="disposalStatus" label="处理状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getDisposalStatusTagType(scope.row.disposalStatus)">
              {{ getDisposalStatusLabel(scope.row.disposalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" min-width="120" />
        <el-table-column prop="endDate" label="结束日期" min-width="120" />
        <el-table-column label="操作" min-width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewDetail(scope.row)">
              查看
            </el-button>
            <el-button size="small" @click="editDisposal(scope.row)">
              编辑处理方案
            </el-button>
            <el-button size="small" type="success" @click="completeDisposal(scope.row)">
              完成处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination mt-4">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 异常处理对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="60%"
      @close="resetForm"
    >
      <el-form
        ref="disposalFormRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="异常编号" prop="reportNo">
          <el-input v-model="formData.reportNo" placeholder="请输入异常编号" disabled />
        </el-form-item>
        <el-form-item label="异常标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入异常标题" disabled />
        </el-form-item>
        <el-form-item label="处理方案" prop="disposalPlan">
          <el-input
            v-model="formData.disposalPlan"
            type="textarea"
            rows="4"
            placeholder="请输入处理方案"
          />
        </el-form-item>
        <el-form-item label="处理人" prop="handler">
          <el-input v-model="formData.handler" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="formData.startDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="请选择开始日期"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="formData.endDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="请选择结束日期"
          />
        </el-form-item>
        <el-form-item label="处理进度" prop="progress">
          <el-progress
            v-model="formData.progress"
            :show-text="true"
            :format="progressFormat"
          />
          <el-slider
            v-model="formData.progress"
            class="mt-2"
            :min="0"
            :max="100"
            :step="5"
          />
        </el-form-item>
        <el-form-item label="处理结果" prop="disposalResult">
          <el-input
            v-model="formData.disposalResult"
            type="textarea"
            rows="3"
            placeholder="请输入处理结果"
          />
        </el-form-item>
        <el-form-item label="处理状态" prop="disposalStatus">
          <el-select v-model="formData.disposalStatus" placeholder="请选择处理状态">
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="异常处理详情"
      width="70%"
      @close="closeDetailDialog"
    >
      <div class="detail-container">
        <div class="detail-header">
          <h4 class="report-no">{{ detailData.reportNo }}</h4>
          <div class="report-info">
            <el-tag :type="getTypeTagType(detailData.anomalyType)">
              {{ getTypeLabel(detailData.anomalyType) }}
            </el-tag>
            <el-tag :type="getDisposalStatusTagType(detailData.disposalStatus)">
              {{ getDisposalStatusLabel(detailData.disposalStatus) }}
            </el-tag>
          </div>
        </div>
        <div class="detail-body">
          <div class="detail-row">
            <div class="detail-label">异常标题：</div>
            <div class="detail-value">{{ detailData.title }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">处理人：</div>
            <div class="detail-value">{{ detailData.handler }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">开始日期：</div>
            <div class="detail-value">{{ detailData.startDate }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">结束日期：</div>
            <div class="detail-value">{{ detailData.endDate || '未完成' }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">处理进度：</div>
            <div class="detail-value">
              <el-progress
              :percentage="detailData.progress"
              :show-text="true"
              :format="(percentage: number) => `${percentage}%`"
            />
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-label">处理方案：</div>
            <div class="detail-value">{{ detailData.disposalPlan }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">处理结果：</div>
            <div class="detail-value">{{ detailData.disposalResult || '暂无' }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeDetailDialog">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { unwrapPageResponse, unwrapResponseData } from '@/api'
import { QmsAnomalyDisposalAPI } from '@/api/qms'

// 异常处理类型
interface AnomalyDisposal {
  id: string | number
  reportNo: string
  title: string
  anomalyType: 'material' | 'production' | 'inspection' | 'complaint'
  disposalPlan: string
  handler: string
  startDate: string
  endDate: string
  progress: number
  disposalResult: string
  disposalStatus: 'pending' | 'processing' | 'completed' | 'cancelled'
}

// 查询表单
const queryForm = reactive({
  reportNo: '',
  anomalyType: '',
  disposalStatus: ''
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

// 异常处理列表
const anomalyDisposals = ref<AnomalyDisposal[]>([])
const total = ref(0)

// 对话框数据
const dialogVisible = ref(false)
const dialogTitle = ref('编辑异常处理')
const disposalFormRef = ref()

// 详情对话框数据
const detailDialogVisible = ref(false)
const detailData = ref<AnomalyDisposal>({
  id: '',
  reportNo: '',
  title: '',
  anomalyType: 'material',
  disposalPlan: '',
  handler: '',
  startDate: '',
  endDate: '',
  progress: 0,
  disposalResult: '',
  disposalStatus: 'pending'
})

// 表单数据
const formData = reactive<Partial<AnomalyDisposal>>({
  reportNo: '',
  title: '',
  disposalPlan: '',
  handler: '',
  startDate: '',
  endDate: '',
  progress: 0,
  disposalResult: '',
  disposalStatus: 'pending'
})

// 表单验证规则
const formRules = reactive({
  disposalPlan: [{ required: true, message: '请输入处理方案', trigger: 'blur' }],
  handler: [{ required: true, message: '请输入处理人', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  disposalStatus: [{ required: true, message: '请选择处理状态', trigger: 'change' }]
})

const formatDate = (value: any) => {
  if (!value) return ''
  const text = String(value)
  return text.length >= 10 ? text.slice(0, 10) : text
}

const calcProgressByStatus = (status: string) => {
  if (status === 'completed') return 100
  if (status === 'processing') return 50
  if (status === 'cancelled') return 0
  return 0
}

const toViewStatus = (status: any): AnomalyDisposal['disposalStatus'] => {
  const raw = status == null ? '' : String(status)
  if (raw === 'completed') return 'completed'
  if (raw === 'processing' || raw === 'in_progress') return 'processing'
  if (raw === 'cancelled') return 'cancelled'
  return 'pending'
}

const toViewModel = (entity: any): AnomalyDisposal => {
  const disposalStatus = toViewStatus(entity?.disposalStatus)
  const startDate = formatDate(entity?.startDate)
  const endDate = formatDate(entity?.actualCompletionDate || entity?.deadline)
  return {
    id: entity?.id ?? '',
    reportNo: String(entity?.reportNo ?? ''),
    title: '',
    anomalyType: 'material',
    disposalPlan: String(entity?.disposalPlan ?? ''),
    handler: String(entity?.responsiblePerson ?? ''),
    startDate,
    endDate,
    progress: calcProgressByStatus(disposalStatus),
    disposalResult: String(entity?.disposalResult ?? ''),
    disposalStatus
  }
}

// 进度条格式化函数
const progressFormat = (percentage: number) => {
  return `${percentage}%`
}

// 获取异常类型标签样式
const getTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    material: 'warning',
    production: 'danger',
    inspection: 'info',
    complaint: 'success'
  }
  return typeMap[type] || 'info'
}

// 获取异常类型标签文本
const getTypeLabel = (type: string) => {
  const typeMap: Record<string, string> = {
    material: '物料异常',
    production: '生产异常',
    inspection: '检验异常',
    complaint: '客户投诉'
  }
  return typeMap[type] || type
}

// 获取处理状态标签样式
const getDisposalStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: 'warning',
    processing: 'info',
    completed: 'success',
    cancelled: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取处理状态标签文本
const getDisposalStatusLabel = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '待处理',
    processing: '处理中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || status
}

// 查询异常处理
const handleQuery = async () => {
  try {
    const res = await QmsAnomalyDisposalAPI.getAnomalyDisposals({
      page: pagination.currentPage,
      size: pagination.pageSize,
      reportNo: queryForm.reportNo || undefined,
      disposalStatus: queryForm.disposalStatus || undefined
    })
    const page = unwrapPageResponse<any>(res)
    const records = page.list as any[]
    let mapped = records.map(toViewModel)

    if (queryForm.anomalyType) {
      mapped = mapped.filter(item => item.anomalyType === queryForm.anomalyType)
    }

    if (queryForm.disposalStatus) {
      mapped = mapped.filter(item => item.disposalStatus === queryForm.disposalStatus)
    }

    anomalyDisposals.value = mapped
    total.value = page.total || mapped.length
  } catch (e: any) {
    ElMessage.error(e?.message || '获取异常处理失败')
  }
}

// 重置查询表单
const resetQuery = () => {
  Object.assign(queryForm, {
    reportNo: '',
    anomalyType: '',
    disposalStatus: ''
  })
  handleQuery()
}

// 打开编辑对话框
const editDisposal = (row: AnomalyDisposal) => {
  dialogTitle.value = '编辑异常处理'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 查看详情
const viewDetail = async (row: AnomalyDisposal) => {
  try {
    const res = await QmsAnomalyDisposalAPI.getAnomalyDisposalById(row.id)
    detailData.value = toViewModel(unwrapResponseData<any>(res) || {})
    detailDialogVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '获取详情失败')
  }
}

// 关闭详情对话框
const closeDetailDialog = () => {
  detailDialogVisible.value = false
}

// 完成处理
const completeDisposal = (row: AnomalyDisposal) => {
  ElMessageBox.confirm('确定要将这条异常处理标记为已完成吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  })
    .then(async () => {
      const detailRes = await QmsAnomalyDisposalAPI.getAnomalyDisposalById(row.id)
      const entity = unwrapResponseData<any>(detailRes) || {}
      const today = new Date().toISOString().slice(0, 10)
      const updated: any = {
        ...entity,
        disposalStatus: 'completed',
        disposalResult: entity.disposalResult || '处理已完成',
        actualCompletionDate: today
      }
      await QmsAnomalyDisposalAPI.updateAnomalyDisposal(row.id, updated)
      ElMessage.success('处理已完成')
      await handleQuery()
    })
    .catch(() => {})
}

// 重置表单
const resetForm = () => {
  if (disposalFormRef.value) {
    disposalFormRef.value.resetFields()
  }
  Object.assign(formData, {
    reportNo: '',
    title: '',
    disposalPlan: '',
    handler: '',
    startDate: '',
    endDate: '',
    progress: 0,
    disposalResult: '',
    disposalStatus: 'pending'
  })
}

// 提交表单
const submitForm = async () => {
  if (!disposalFormRef.value) return
  
  try {
    await disposalFormRef.value.validate()

    const payload: any = {
      reportNo: formData.reportNo,
      disposalPlan: formData.disposalPlan,
      responsiblePerson: formData.handler,
      startDate: formData.startDate || undefined,
      deadline: formData.endDate || undefined,
      actualCompletionDate: formData.endDate || undefined,
      disposalStatus: formData.disposalStatus,
      disposalResult: formData.disposalResult
    }

    if (formData.id) {
      const detailRes = await QmsAnomalyDisposalAPI.getAnomalyDisposalById(formData.id)
      const entity = unwrapResponseData<any>(detailRes) || {}
      await QmsAnomalyDisposalAPI.updateAnomalyDisposal(formData.id, { ...entity, ...payload })
      ElMessage.success('编辑成功')
    } else {
      await QmsAnomalyDisposalAPI.createAnomalyDisposal(payload)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    resetForm()
    await handleQuery()
  } catch (error) {
    console.error('表单验证失败：', error)
  }
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

// 页面加载时初始化数据
onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.anomaly-disposal-view {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.mb-4 {
  margin-bottom: 20px;
}

/* 详情对话框样式 */
.detail-container {
  padding: 20px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.report-no {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.report-info {
  display: flex;
  gap: 8px;
}

.detail-body {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
}

.detail-row {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  width: 100px;
  font-weight: 500;
  color: #606266;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: #303133;
  word-break: break-word;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .anomaly-disposal-view {
    padding: 12px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .pagination {
    justify-content: center;
  }
  
  .detail-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .detail-row {
    flex-direction: column;
    gap: 4px;
  }
  
  .detail-label {
    width: auto;
  }
}
</style>
