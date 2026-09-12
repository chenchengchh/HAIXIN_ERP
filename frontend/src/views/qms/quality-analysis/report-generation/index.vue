<template>
  <div class="report-generation-view">
    <div class="page-header">
      <h3>质量报告生成</h3>
      <el-button type="primary" @click="openGenerateDialog">
        <el-icon><DocumentAdd /></el-icon>生成新报告
      </el-button>
    </div>

    <!-- 查询表单 -->
    <el-card class="mb-4" shadow="hover">
      <el-form :model="queryForm" inline>
        <el-form-item label="报告类型">
          <el-select v-model="queryForm.reportType" placeholder="请选择报告类型" clearable>
            <el-option label="日报" value="daily" />
            <el-option label="月报" value="monthly" />
            <el-option label="季度报" value="quarterly" />
            <el-option label="年度报" value="annual" />
            <el-option label="专项报告" value="special" />
          </el-select>
        </el-form-item>
        <el-form-item label="报告周期">
          <el-date-picker
            v-model="queryForm.reportPeriod"
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
            <el-option label="生成中" value="generating" />
            <el-option label="已完成" value="completed" />
            <el-option label="已导出" value="exported" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 报告列表 -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>报告列表</span>
          <el-tag size="small" type="info">{{ total }} 条记录</el-tag>
        </div>
      </template>
      <el-table :data="reportList" border style="width: 100%">
        <el-table-column prop="reportNo" label="报告编号" min-width="120" />
        <el-table-column prop="reportType" label="报告类型" min-width="100">
          <template #default="scope">
            <el-tag :type="getReportTypeTagType(scope.row.reportType)">
              {{ getReportTypeLabel(scope.row.reportType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reportName" label="报告名称" min-width="180" />
        <el-table-column prop="reportPeriod" label="报告周期" min-width="180">
          <template #default="scope">
            {{ scope.row.reportPeriod.startDate }} 至 {{ scope.row.reportPeriod.endDate }}
          </template>
        </el-table-column>
        <el-table-column prop="generateDate" label="生成日期" min-width="120" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="generator" label="生成人" min-width="100" />
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewReport(scope.row)">
              查看
            </el-button>
            <el-button size="small" @click="exportReport(scope.row)">
              导出
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

    <!-- 生成报告对话框 -->
    <el-dialog
      v-model="generateDialogVisible"
      title="生成质量报告"
      width="60%"
      @close="resetGenerateForm"
    >
      <el-form
        ref="generateFormRef"
        :model="generateFormData"
        :rules="generateFormRules"
        label-width="120px"
      >
        <el-form-item label="报告类型" prop="reportType">
          <el-select v-model="generateFormData.reportType" placeholder="请选择报告类型">
            <el-option label="日报" value="daily" />
            <el-option label="月报" value="monthly" />
            <el-option label="季度报" value="quarterly" />
            <el-option label="年度报" value="annual" />
            <el-option label="专项报告" value="special" />
          </el-select>
        </el-form-item>
        <el-form-item label="报告名称" prop="reportName">
          <el-input v-model="generateFormData.reportName" placeholder="请输入报告名称" />
        </el-form-item>
        <el-form-item label="报告周期" prop="reportPeriod">
          <el-date-picker
            v-model="generateFormData.reportPeriod"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="报告模板" prop="template">
          <el-select v-model="generateFormData.template" placeholder="请选择报告模板">
            <el-option label="标准模板" value="standard" />
            <el-option label="详细模板" value="detailed" />
            <el-option label="简版模板" value="simple" />
          </el-select>
        </el-form-item>
        <el-form-item label="生成人" prop="generator">
          <el-input v-model="generateFormData.generator" placeholder="请输入生成人" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="generateFormData.remark"
            type="textarea"
            rows="2"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="generateDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitGenerateForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="质量报告详情"
      width="70%"
      @close="closeDetailDialog"
    >
      <div class="detail-container">
        <div class="detail-header">
          <h4 class="report-no">{{ detailData.reportNo }}</h4>
          <div class="report-info">
            <el-tag :type="getReportTypeTagType(detailData.reportType)">
              {{ getReportTypeLabel(detailData.reportType) }}
            </el-tag>
            <el-tag :type="getStatusTagType(detailData.status)">
              {{ getStatusLabel(detailData.status) }}
            </el-tag>
          </div>
        </div>
        <div class="detail-body">
          <div class="detail-row">
            <div class="detail-label">报告名称：</div>
            <div class="detail-value">{{ detailData.reportName }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">报告类型：</div>
            <div class="detail-value">{{ getReportTypeLabel(detailData.reportType) }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">报告周期：</div>
            <div class="detail-value">{{ detailData.reportPeriod.startDate }} 至 {{ detailData.reportPeriod.endDate }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">生成日期：</div>
            <div class="detail-value">{{ detailData.generateDate }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">生成人：</div>
            <div class="detail-value">{{ detailData.generator }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">状态：</div>
            <div class="detail-value">{{ getStatusLabel(detailData.status) }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">备注：</div>
            <div class="detail-value">{{ detailData.remark || '无' }}</div>
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
import { DocumentAdd } from '@element-plus/icons-vue'
import { unwrapPageResponse, unwrapResponseData } from '@/api'
import { ReportGenerationAPI } from '@/api/qms'

// 报告周期类型
interface ReportPeriod {
  startDate: string
  endDate: string
}

// 报告类型
interface Report {
  id: string | number
  reportNo: string
  reportName: string
  reportType: 'daily' | 'monthly' | 'quarterly' | 'annual' | 'special'
  reportPeriod: ReportPeriod
  generateDate: string
  status: 'generating' | 'completed' | 'exported'
  generator: string
  remark: string
}

// 查询表单
const queryForm = reactive({
  reportType: '',
  reportPeriod: [] as string[],
  status: ''
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

// 报告列表
const reportList = ref<Report[]>([])
const total = ref(0)

// 对话框数据
const generateDialogVisible = ref(false)
const generateFormRef = ref()

// 生成表单数据
const generateFormData = reactive({
  reportType: '',
  reportName: '',
  reportPeriod: [] as string[],
  template: 'standard',
  generator: '',
  remark: ''
})

// 生成表单验证规则
const generateFormRules = reactive({
  reportType: [{ required: true, message: '请选择报告类型', trigger: 'change' }],
  reportName: [{ required: true, message: '请输入报告名称', trigger: 'blur' }],
  reportPeriod: [{ required: true, message: '请选择报告周期', trigger: 'change' }],
  generator: [{ required: true, message: '请输入生成人', trigger: 'blur' }]
})

// 详情对话框数据
const detailDialogVisible = ref(false)
const detailData = ref<Report>({
  id: '',
  reportNo: '',
  reportName: '',
  reportType: 'daily',
  reportPeriod: {
    startDate: '',
    endDate: ''
  },
  generateDate: '',
  status: 'completed',
  generator: '',
  remark: ''
})

const formatDate = (value: any) => {
  if (!value) return ''
  const text = String(value)
  return text.length >= 10 ? text.slice(0, 10) : text
}

const parsePeriod = (period: any): ReportPeriod => {
  const raw = period == null ? '' : String(period)
  if (!raw) {
    return { startDate: '', endDate: '' }
  }
  const separator = raw.includes('~') ? '~' : raw.includes('至') ? '至' : raw.includes('-') ? '-' : ''
  if (!separator) {
    return { startDate: raw, endDate: raw }
  }
  const parts = raw.split(separator).map(v => v.trim()).filter(Boolean)
  return {
    startDate: parts[0] || '',
    endDate: parts[1] || parts[0] || ''
  }
}

const toReportViewModel = (entity: any): Report => {
  const content = entity?.content || {}
  const reportPeriodFromContent = content?.reportPeriod
  const reportPeriod =
    reportPeriodFromContent && typeof reportPeriodFromContent === 'object'
      ? {
          startDate: String(reportPeriodFromContent.startDate ?? ''),
          endDate: String(reportPeriodFromContent.endDate ?? '')
        }
      : parsePeriod(entity?.period)

  return {
    id: entity?.id ?? '',
    reportNo: String(entity?.reportNo ?? ''),
    reportName: String(entity?.reportName ?? ''),
    reportType: (entity?.reportType ?? 'daily') as any,
    reportPeriod,
    generateDate: formatDate(entity?.createTime || entity?.createdTime),
    status: (entity?.status ?? 'completed') as any,
    generator: String(content?.generator ?? entity?.creator ?? ''),
    remark: String(content?.remark ?? '')
  }
}

// 获取报告类型标签样式
const getReportTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    daily: 'info',
    monthly: 'warning',
    quarterly: 'success',
    annual: 'primary',
    special: 'danger'
  }
  return typeMap[type] || 'info'
}

// 获取报告类型标签文本
const getReportTypeLabel = (type: string) => {
  const typeMap: Record<string, string> = {
    daily: '日报',
    monthly: '月报',
    quarterly: '季度报',
    annual: '年度报',
    special: '专项报告'
  }
  return typeMap[type] || type
}

// 获取状态标签样式
const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    generating: 'warning',
    completed: 'success',
    exported: 'primary'
  }
  return statusMap[status] || 'info'
}

// 获取状态标签文本
const getStatusLabel = (status: string) => {
  const statusMap: Record<string, string> = {
    generating: '生成中',
    completed: '已完成',
    exported: '已导出'
  }
  return statusMap[status] || status
}

const handleQuery = async () => {
  try {
    const res = await ReportGenerationAPI.getReports({
      page: pagination.currentPage,
      size: pagination.pageSize,
      status: queryForm.status || undefined
    })
    const page = unwrapPageResponse<any>(res)
    const records = page.list as any[]
    let mapped = records.map(toReportViewModel)

    if (queryForm.reportType) {
      mapped = mapped.filter(r => r.reportType === queryForm.reportType)
    }

    const startDate = queryForm.reportPeriod?.[0]
    const endDate = queryForm.reportPeriod?.[1]
    if (startDate && endDate) {
      mapped = mapped.filter(item => item.reportPeriod.startDate >= startDate && item.reportPeriod.endDate <= endDate)
    }

    reportList.value = mapped
    total.value = page.total || mapped.length
  } catch (e: any) {
    ElMessage.error(e?.message || '获取质量报告失败')
  }
}

// 重置查询表单
const resetQuery = () => {
  Object.assign(queryForm, {
    reportType: '',
    reportPeriod: [],
    status: ''
  })
  handleQuery()
}

// 打开生成报告对话框
const openGenerateDialog = () => {
  resetGenerateForm()
  generateDialogVisible.value = true
}

const viewReport = async (row: Report) => {
  try {
    const res = await ReportGenerationAPI.getReportById(row.id)
    detailData.value = toReportViewModel(unwrapResponseData<any>(res) || {})
    detailDialogVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '获取报告详情失败')
  }
}

// 关闭详情对话框
const closeDetailDialog = () => {
  detailDialogVisible.value = false
}

const exportReport = async (row: Report) => {
  try {
    const res = await ReportGenerationAPI.exportReport(row.id, 'pdf')
    const blob = new Blob([res.data], { type: 'application/octet-stream' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${row.reportNo || 'quality-report'}.pdf`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e: any) {
    ElMessage.error(e?.message || '导出失败')
  }
}

const deleteReport = (id: string | number) => {
  ElMessageBox.confirm('确定要删除这条报告吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await ReportGenerationAPI.deleteReport(id)
      ElMessage.success('删除成功')
      await handleQuery()
    })
    .catch(() => {})
}

// 重置生成表单
const resetGenerateForm = () => {
  if (generateFormRef.value) {
    generateFormRef.value.resetFields()
  }
  Object.assign(generateFormData, {
    reportType: '',
    reportName: '',
    reportPeriod: [],
    template: 'standard',
    generator: '',
    remark: ''
  })
}

// 提交生成表单
const submitGenerateForm = async () => {
  if (!generateFormRef.value) return
  
  try {
    await generateFormRef.value.validate()
    
    if (!generateFormData.reportPeriod || generateFormData.reportPeriod.length !== 2) {
      ElMessage.error('请选择完整的报告周期')
      return
    }
    
    const startDate = generateFormData.reportPeriod[0] as string
    const endDate = generateFormData.reportPeriod[1] as string
    const payload: any = {
      reportType: generateFormData.reportType,
      reportName: generateFormData.reportName,
      period: `${startDate}~${endDate}`,
      creator: generateFormData.generator,
      status: 'draft',
      content: {
        reportPeriod: { startDate, endDate },
        generator: generateFormData.generator,
        remark: generateFormData.remark,
        template: generateFormData.template
      }
    }
    await ReportGenerationAPI.createReport(payload as any)
    ElMessage.success('报告生成成功')
    generateDialogVisible.value = false
    resetGenerateForm()
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

onMounted(handleQuery)
</script>

<style scoped>
.report-generation-view {
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
  .report-generation-view {
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
