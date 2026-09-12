<template>
  <div class="anomaly-report-view">
    <div class="page-header">
      <h3>异常报告</h3>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>新增异常报告
      </el-button>
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
        <el-form-item label="报告日期">
          <el-date-picker
            v-model="queryForm.reportDate"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已解决" value="resolved" />
            <el-option label="已关闭" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 异常报告列表 -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>异常报告列表</span>
          <el-tag size="small" type="info">{{ total }} 条记录</el-tag>
        </div>
      </template>
      <el-table :data="anomalyReports" border style="width: 100%">
        <el-table-column prop="reportNo" label="异常编号" min-width="120" />
        <el-table-column prop="title" label="异常标题" min-width="180" />
        <el-table-column prop="anomalyType" label="异常类型" min-width="100">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.anomalyType)">
              {{ getTypeLabel(scope.row.anomalyType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reportDate" label="报告日期" min-width="120" />
        <el-table-column prop="reporter" label="报告人" min-width="100" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="异常描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" min-width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewDetail(scope.row)">
              查看
            </el-button>
            <el-button size="small" @click="editReport(scope.row)">
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="deleteReport(scope.row.id)">
              删除
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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="异常报告详情"
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
            <el-tag :type="getStatusTagType(detailData.status)">
              {{ getStatusLabel(detailData.status) }}
            </el-tag>
          </div>
        </div>
        <div class="detail-body">
          <div class="detail-row">
            <div class="detail-label">异常标题：</div>
            <div class="detail-value">{{ detailData.title }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">报告日期：</div>
            <div class="detail-value">{{ detailData.reportDate }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">报告人：</div>
            <div class="detail-value">{{ detailData.reporter }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">异常描述：</div>
            <div class="detail-value">{{ detailData.description }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">异常影响：</div>
            <div class="detail-value">{{ detailData.impact }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeDetailDialog">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 新增/编辑异常报告对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="60%"
      @close="resetForm"
    >
      <el-form
        ref="anomalyFormRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="异常标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入异常标题" />
        </el-form-item>
        <el-form-item label="异常类型" prop="anomalyType">
          <el-select v-model="formData.anomalyType" placeholder="请选择异常类型">
            <el-option label="物料异常" value="material" />
            <el-option label="生产异常" value="production" />
            <el-option label="检验异常" value="inspection" />
            <el-option label="客户投诉" value="complaint" />
          </el-select>
        </el-form-item>
        <el-form-item label="报告日期" prop="reportDate">
          <el-date-picker
            v-model="formData.reportDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="请选择报告日期"
          />
        </el-form-item>
        <el-form-item label="报告人" prop="reporter">
          <el-input v-model="formData.reporter" placeholder="请输入报告人" />
        </el-form-item>
        <el-form-item label="异常描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            rows="4"
            placeholder="请输入异常描述"
          />
        </el-form-item>
        <el-form-item label="异常影响" prop="impact">
          <el-input
            v-model="formData.impact"
            type="textarea"
            rows="3"
            placeholder="请输入异常影响"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态">
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已解决" value="resolved" />
            <el-option label="已关闭" value="closed" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { unwrapPageResponse, unwrapResponseData } from '@/api'
import { AnomalyReportAPI } from '@/api/qms'

// 异常报告类型
interface AnomalyReport {
  id: string | number
  reportNo: string
  title: string
  anomalyType: 'material' | 'production' | 'inspection' | 'complaint'
  reportDate: string
  reporter: string
  description: string
  impact: string
  status: 'pending' | 'processing' | 'resolved' | 'closed'
}

// 查询表单
const queryForm = reactive({
  reportNo: '',
  anomalyType: '',
  reportDate: [] as string[],
  status: ''
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

// 异常报告列表
const anomalyReports = ref<AnomalyReport[]>([])
const total = ref(0)

// 对话框数据
const dialogVisible = ref(false)
const dialogTitle = ref('新增异常报告')
const anomalyFormRef = ref()

// 详情对话框数据
const detailDialogVisible = ref(false)
const detailData = ref<AnomalyReport>({
  id: '',
  reportNo: '',
  title: '',
  anomalyType: 'material',
  reportDate: '',
  reporter: '',
  description: '',
  impact: '',
  status: 'pending'
})

// 表单数据
const formData = reactive<Partial<AnomalyReport>>({
  title: '',
  anomalyType: 'material',
  reportDate: '',
  reporter: '',
  description: '',
  impact: '',
  status: 'pending'
})

// 表单验证规则
const formRules = reactive({
  title: [{ required: true, message: '请输入异常标题', trigger: 'blur' }],
  anomalyType: [{ required: true, message: '请选择异常类型', trigger: 'change' }],
  reportDate: [{ required: true, message: '请选择报告日期', trigger: 'change' }],
  reporter: [{ required: true, message: '请输入报告人', trigger: 'blur' }],
  description: [{ required: true, message: '请输入异常描述', trigger: 'blur' }]
})

const formatDate = (value: any) => {
  if (!value) return ''
  const text = String(value)
  return text.length >= 10 ? text.slice(0, 10) : text
}

const toApiDateTime = (dateText: string) => {
  if (!dateText) return undefined
  return dateText.includes('T') ? dateText : `${dateText}T00:00:00`
}

const toViewStatus = (status: any): AnomalyReport['status'] => {
  const raw = status == null ? '' : String(status)
  if (raw === 'investigating') return 'processing'
  if (raw === 'resolved') return 'resolved'
  if (raw === 'closed') return 'closed'
  return 'pending'
}

const toApiStatus = (status: string): string | undefined => {
  if (!status) return undefined
  if (status === 'processing') return 'investigating'
  return status
}

const toViewModel = (entity: any): AnomalyReport => {
  return {
    id: entity?.id ?? '',
    reportNo: String(entity?.reportNo ?? ''),
    title: String(entity?.title ?? ''),
    anomalyType: (entity?.anomalyType ?? 'material') as any,
    reportDate: formatDate(entity?.reportTime || entity?.occurrenceTime),
    reporter: String(entity?.reporter ?? ''),
    description: String(entity?.description ?? ''),
    impact: String(entity?.impactAssessment ?? ''),
    status: toViewStatus(entity?.status)
  }
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

// 获取状态标签样式
const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: 'warning',
    processing: 'info',
    resolved: 'success',
    closed: 'success'
  }
  return statusMap[status] || 'info'
}

// 获取状态标签文本
const getStatusLabel = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '待处理',
    processing: '处理中',
    resolved: '已解决',
    closed: '已关闭'
  }
  return statusMap[status] || status
}

const handleQuery = async () => {
  try {
    const res = await AnomalyReportAPI.getAnomalyReports({
      page: pagination.currentPage,
      size: pagination.pageSize,
      reportNo: queryForm.reportNo || undefined,
      status: toApiStatus(queryForm.status)
    })
    const page = unwrapPageResponse<any>(res)
    const records = page.list as any[]
    let mapped = records.map(toViewModel)

    if (queryForm.anomalyType) {
      mapped = mapped.filter(item => item.anomalyType === queryForm.anomalyType)
    }

    if (queryForm.reportDate && queryForm.reportDate.length === 2) {
      const startDate = queryForm.reportDate[0] as string
      const endDate = queryForm.reportDate[1] as string
      mapped = mapped.filter(item => item.reportDate >= startDate && item.reportDate <= endDate)
    }

    if (queryForm.status) {
      mapped = mapped.filter(item => item.status === queryForm.status)
    }

    anomalyReports.value = mapped
    total.value = page.total || mapped.length
  } catch (e: any) {
    ElMessage.error(e?.message || '获取异常报告失败')
  }
}

// 重置查询表单
const resetQuery = () => {
  Object.assign(queryForm, {
    reportNo: '',
    anomalyType: '',
    reportDate: [],
    status: ''
  })
  handleQuery()
}

// 打开新增对话框
const openAddDialog = () => {
  dialogTitle.value = '新增异常报告'
  resetForm()
  dialogVisible.value = true
}

// 打开编辑对话框
const editReport = (row: AnomalyReport) => {
  dialogTitle.value = '编辑异常报告'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const viewDetail = async (row: AnomalyReport) => {
  try {
    const res = await AnomalyReportAPI.getAnomalyReportById(row.id)
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

const deleteReport = (id: string | number) => {
  ElMessageBox.confirm('确定要删除这条异常报告吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await AnomalyReportAPI.deleteAnomalyReport(id)
      ElMessage.success('删除成功')
      await handleQuery()
    })
    .catch(() => {})
}

// 重置表单
const resetForm = () => {
  if (anomalyFormRef.value) {
    anomalyFormRef.value.resetFields()
  }
  Object.assign(formData, {
    title: '',
    anomalyType: 'material',
    reportDate: '',
    reporter: '',
    description: '',
    impact: '',
    status: 'pending'
  })
}

// 提交表单
const submitForm = async () => {
  if (!anomalyFormRef.value) return
  
  try {
    await anomalyFormRef.value.validate()

    const payload: any = {
      reportNo: formData.reportNo,
      title: formData.title,
      anomalyType: formData.anomalyType,
      description: formData.description,
      reporter: formData.reporter,
      reportTime: toApiDateTime(String(formData.reportDate || '')),
      impactAssessment: formData.impact,
      status: toApiStatus(String(formData.status || 'pending'))
    }

    if (formData.id) {
      await AnomalyReportAPI.updateAnomalyReport(formData.id, payload)
      ElMessage.success('编辑成功')
    } else {
      await AnomalyReportAPI.createAnomalyReport(payload)
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
.anomaly-report-view {
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
  .anomaly-report-view {
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
