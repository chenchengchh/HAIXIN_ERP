<template>
  <div class="trial-report-container">
    <div class="trial-report-header">
      <h2>试产报告管理</h2>
      <div class="header-actions">
        <el-button type="danger" @click="batchDelete" :disabled="selectedReportIds.length === 0">
          <el-icon><Delete /></el-icon> 批量删除
        </el-button>
        <el-button type="primary" @click="createReport">
          <el-icon><Plus /></el-icon> 新建试产报告
        </el-button>
      </div>
    </div>

    <div class="trial-report-content">
      <!-- 搜索和筛选 -->
      <div class="search-filter">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索报告名称或编码"
          prefix-icon="Search"
          clearable
          @input="handleSearch"
        />
        <el-select
          v-model="statusFilter"
          placeholder="报告状态"
          clearable
          @change="handleFilter"
        >
          <el-option label="所有状态" value="all" />
          <el-option label="草稿" value="draft" />
          <el-option label="已审批" value="approved" />
        </el-select>
      </div>

      <!-- 试产报告列表 -->
      <el-table
        :data="filteredTrialReports"
        style="width: 100%"
        @row-click="selectReport"
        highlight-current-row
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="code" label="报告编码" width="150" />
        <el-table-column prop="planName" label="关联计划" min-width="250">
          <template #default="scope">
            <div>{{ scope.row.planName }} ({{ scope.row.planCode }})</div>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="试产产品" min-width="180">
          <template #default="scope">
            <div>{{ scope.row.productName }} ({{ scope.row.productCode }})</div>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="产品版本" width="100" />
        <el-table-column prop="trialQty" label="试产数量" width="120" />
        <el-table-column prop="passQty" label="合格数量" width="120" />
        <el-table-column prop="yield" label="良率" width="100">
          <template #default="scope">
            <div>{{ scope.row.yield }}%</div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="reportStatusMap[scope.row.status as ReportStatus]">
              {{ reportStatusText[scope.row.status as ReportStatus] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150" />
        <el-table-column prop="createUser" label="创建人" width="100" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click.stop="viewReportDetail(scope.row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
            <el-button link type="primary" @click.stop="editReport(scope.row)" :disabled="scope.row.status === 'approved'">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button link type="primary" @click.stop="approveReport(scope.row)" v-if="scope.row.status === 'draft'">
              <el-icon><Check /></el-icon> 审批
            </el-button>
            <el-button link type="primary" @click.stop="exportReport(scope.row)">
              <el-icon><Download /></el-icon> 导出
            </el-button>
            <el-button link type="danger" @click.stop="deleteReport(scope.row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="trialReports.length"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 报告详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="试产报告详情"
      width="80%"
      :close-on-click-modal="false"
    >
      <div class="report-detail" v-if="selectedReport">
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">报告编码：</span>
              <span>{{ selectedReport.code }}</span>
            </div>
            <div class="detail-item">
              <span class="label">关联计划：</span>
              <span>{{ selectedReport.planName }} ({{ selectedReport.planCode }})</span>
            </div>
            <div class="detail-item">
              <span class="label">试产产品：</span>
              <span>{{ selectedReport.productName }} ({{ selectedReport.productCode }})</span>
            </div>
            <div class="detail-item">
              <span class="label">产品版本：</span>
              <span>{{ selectedReport.version }}</span>
            </div>
            <div class="detail-item">
              <span class="label">试产数量：</span>
              <span>{{ selectedReport.trialQty }} 台</span>
            </div>
            <div class="detail-item">
              <span class="label">合格数量：</span>
              <span>{{ selectedReport.passQty }} 台</span>
            </div>
            <div class="detail-item">
              <span class="label">良率：</span>
              <span>{{ selectedReport.yield }}%</span>
            </div>
            <div class="detail-item">
              <span class="label">报告状态：</span>
              <el-tag :type="reportStatusMap[selectedReport.status as ReportStatus]">
                {{ reportStatusText[selectedReport.status as ReportStatus] }}
              </el-tag>
            </div>
            <div class="detail-item">
              <span class="label">创建人：</span>
              <span>{{ selectedReport.createUser }}</span>
            </div>
            <div class="detail-item">
              <span class="label">创建时间：</span>
              <span>{{ selectedReport.createTime }}</span>
            </div>
            <div class="detail-item" v-if="selectedReport.approveTime">
              <span class="label">审批人：</span>
              <span>{{ selectedReport.approveUser }}</span>
            </div>
            <div class="detail-item" v-if="selectedReport.approveTime">
              <span class="label">审批时间：</span>
              <span>{{ selectedReport.approveTime }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3>主要问题</h3>
          <div class="issues">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="问题列表">
                <ul>
                  <li v-for="(issue, index) in selectedReport.mainIssues" :key="index">
                    {{ issue }}
                  </li>
                </ul>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <div class="detail-section">
          <h3>改进措施</h3>
          <div class="improvements">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="改进列表">
                <ul>
                  <li v-for="(improvement, index) in selectedReport.improvements" :key="index">
                    {{ improvement }}
                  </li>
                </ul>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="exportReport(selectedReport)">
            导出报告
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新建/编辑试产报告对话框 -->
    <el-dialog
      v-model="reportDialogVisible"
      :title="isEditMode ? '编辑试产报告' : '新建试产报告'"
      width="70%"
      :close-on-click-modal="false"
    >
      <el-form :model="reportForm" ref="reportFormRef" :rules="reportRules" label-position="top">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="报告编码" prop="code">
              <el-input v-model="reportForm.code" placeholder="请输入报告编码" :disabled="isEditMode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联计划" prop="planCode">
              <el-select v-model="reportForm.planCode" placeholder="请选择关联计划">
                <el-option 
                  v-for="plan in trialPlans" 
                  :key="plan.code" 
                  :label="plan.name + ' (' + plan.code + ')'" 
                  :value="plan.code" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="试产数量" prop="trialQty">
              <el-input-number v-model="reportForm.trialQty" :min="1" placeholder="请输入试产数量" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合格数量" prop="passQty">
              <el-input-number v-model="reportForm.passQty" :min="0" placeholder="请输入合格数量" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="良率" prop="yield">
              <el-input-number v-model="reportForm.yield" :min="0" :max="100" :precision="2" placeholder="请输入良率" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="主要问题" prop="mainIssues">
              <el-input 
                v-model="reportForm.mainIssues" 
                placeholder="请输入主要问题，多个问题请换行" 
                type="textarea" 
                rows="4" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="改进措施" prop="improvements">
              <el-input 
                v-model="reportForm.improvements" 
                placeholder="请输入改进措施，多个措施请换行" 
                type="textarea" 
                rows="4" 
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelReportDialog">取消</el-button>
          <el-button type="primary" @click="submitReportForm" :loading="reportFormLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { Plus, Search, View, Edit, Check, Download, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTrialProductionStore } from '../../../stores/plm/trialProduction'

const store = useTrialProductionStore()

// 搜索和筛选
const searchKeyword = ref('')
const statusFilter = ref('all')

// 详情对话框
const detailDialogVisible = ref(false)
const selectedReport = ref<any>(null)

// 新建/编辑对话框
const reportDialogVisible = ref(false)
const isEditMode = ref(false)
const reportFormLoading = ref(false)
const reportFormRef = ref<any>(null)

// 选择的行ID
const selectedReportIds = ref<string[]>([])

// 试产报告表单
const reportForm = ref({
  id: '',
  code: '',
  planCode: '',
  planName: '',
  productCode: '',
  productName: '',
  version: 'V1.0',
  trialQty: 1000,
  passQty: 0,
  yield: 0,
  mainIssues: '',
  improvements: ''
})

// 表单验证规则
const reportRules = reactive({
  code: [
    { required: true, message: '请输入报告编码', trigger: 'blur' },
    { min: 2, max: 20, message: '报告编码长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  planCode: [
    { required: true, message: '请选择关联计划', trigger: 'change' }
  ],
  trialQty: [
    { required: true, message: '请输入试产数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '试产数量必须大于0', trigger: 'blur' }
  ],
  passQty: [
    { required: true, message: '请输入合格数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '合格数量必须大于等于0', trigger: 'blur' }
  ],
  yield: [
    { required: true, message: '请输入良率', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '良率必须在0-100之间', trigger: 'blur' }
  ],
  mainIssues: [
    { required: true, message: '请输入主要问题', trigger: 'blur' }
  ],
  improvements: [
    { required: true, message: '请输入改进措施', trigger: 'blur' }
  ]
})

// 试产报告状态类型
type ReportStatus = 'draft' | 'approved'

// 试产报告状态映射
const reportStatusMap: Record<ReportStatus, string> = {
  draft: 'warning',
  approved: 'success'
}

const reportStatusText: Record<ReportStatus, string> = {
  draft: '草稿',
  approved: '已审批'
}

// 分页信息
const pagination = computed(() => ({
  page: store.pagination.page,
  pageSize: store.pagination.pageSize
}))

// 试产报告列表
const trialReports = computed(() => store.trialReports)

// 试产计划列表（用于关联选择）
const trialPlans = computed(() => store.trialPlans)

// 筛选后的试产报告列表
const filteredTrialReports = computed(() => {
  return trialReports.value.filter(report => {
    const matchesKeyword = searchKeyword.value === '' || 
      report.planName.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      report.code.toLowerCase().includes(searchKeyword.value.toLowerCase())
    const matchesStatus = statusFilter.value === 'all' || report.status === statusFilter.value
    return matchesKeyword && matchesStatus
  })
})

// 页面加载时获取试产报告和计划数据
onMounted(async () => {
  await Promise.all([
    store.fetchTrialReports(),
    store.fetchTrialPlans()
  ])
})

// 新建试产报告
const createReport = () => {
  isEditMode.value = false
  resetReportForm()
  reportDialogVisible.value = true
}

// 编辑试产报告
const editReport = (report: any) => {
  isEditMode.value = true
  reportForm.value = {
    ...report,
    mainIssues: report.mainIssues ? report.mainIssues.join('\n') : '',
    improvements: report.improvements ? report.improvements.join('\n') : ''
  }
  reportDialogVisible.value = true
}

// 重置报告表单
const resetReportForm = () => {
  reportForm.value = {
    id: '',
    code: '',
    planCode: '',
    planName: '',
    productCode: '',
    productName: '',
    version: 'V1.0',
    trialQty: 1000,
    passQty: 0,
    yield: 0,
    mainIssues: '',
    improvements: ''
  }
  if (reportFormRef.value) {
    reportFormRef.value.resetFields()
  }
}

// 取消报告对话框
const cancelReportDialog = () => {
  reportDialogVisible.value = false
  if (reportFormRef.value) {
    reportFormRef.value.resetFields()
  }
}

// 提交报告表单
const submitReportForm = async () => {
  if (!reportFormRef.value) return
  
  try {
    await reportFormRef.value.validate()
    reportFormLoading.value = true

    // 获取关联计划信息
    const selectedPlan = trialPlans.value.find(p => p.code === reportForm.value.planCode)
    
    // 处理多行文本为数组
    const mainIssues = reportForm.value.mainIssues.split('\n').filter(item => item.trim())
    const improvements = reportForm.value.improvements.split('\n').filter(item => item.trim())
    
    const completeReport = {
      ...reportForm.value,
      planName: selectedPlan?.name || '',
      productCode: selectedPlan?.productCode || '',
      productName: selectedPlan?.productName || '',
      version: selectedPlan?.version || 'V1.0',
      mainIssues,
      improvements
    }
    
    if (isEditMode.value) {
      // 更新试产报告
      const updated = await store.updateTrialReport(completeReport)
      if (!updated) {
        ElMessage.error('试产报告更新失败')
        return
      }
      ElMessage.success('试产报告更新成功')
    } else {
      // 新建试产报告
      const created = await store.addTrialReport(completeReport)
      if (!created) {
        ElMessage.error('试产报告创建失败')
        return
      }
      ElMessage.success('试产报告创建成功')
    }
    
    reportDialogVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    reportFormLoading.value = false
  }
}

// 搜索处理
const handleSearch = async () => {
  store.setSearchKeyword(searchKeyword.value)
  store.setPagination(1, store.pagination.pageSize)
  await store.fetchTrialReports()
}

// 筛选处理
const handleFilter = async () => {
  store.setStatusFilter(statusFilter.value)
  store.setPagination(1, store.pagination.pageSize)
  await store.fetchTrialReports()
}

// 选择试产报告
const selectReport = (report: any) => {
  selectedReport.value = report
}

// 查看报告详情
const viewReportDetail = (report: any) => {
  selectedReport.value = report
  detailDialogVisible.value = true
}

// 审批报告
const approveReport = async (report: any) => {
  const ok = await store.approveTrialReport(report.id)
  if (!ok) {
    ElMessage.error('试产报告审批失败')
    return
  }
  ElMessage.success('试产报告审批成功')
}

// 导出报告
const exportReport = (report: any) => {
  const content = JSON.stringify(report, null, 2)
  const blob = new Blob([content], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `trial_report_${report.code || report.id || 'report'}.json`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('试产报告导出成功')
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  store.setPagination(1, size)
  store.fetchTrialReports()
}

// 页码变化
const handleCurrentChange = (current: number) => {
  store.setPagination(current, store.pagination.pageSize)
  store.fetchTrialReports()
}

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedReportIds.value = selection.map(item => item.id)
}

// 删除试产报告
const deleteReport = (report: any) => {
  ElMessageBox.confirm('确定要删除该试产报告吗？此操作不可恢复。', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const ok = await store.deleteTrialReport(report.id)
    if (!ok) {
      ElMessage.error('试产报告删除失败')
      return
    }
    ElMessage.success('试产报告删除成功')
  }).catch(() => {
    // 用户取消删除操作
  })
}

// 批量删除试产报告
const batchDelete = () => {
  if (selectedReportIds.value.length === 0) {
    ElMessage.warning('请选择要删除的试产报告')
    return
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedReportIds.value.length} 个试产报告吗？此操作不可恢复。`, '批量删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const results = await Promise.all(selectedReportIds.value.map(id => store.deleteTrialReport(String(id))))
    selectedReportIds.value = []
    if (results.every(Boolean)) {
      ElMessage.success('批量删除成功')
    } else {
      ElMessage.error('批量删除部分失败')
    }
  }).catch(() => {
    // 用户取消删除操作
  })
}
</script>

<style scoped>
.trial-report-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.trial-report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.trial-report-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.search-filter {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.search-filter .el-input {
  width: 300px;
}

.search-filter .el-select {
  width: 150px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 报告详情样式 */
.report-detail {
  padding: 20px;
}

.detail-section {
  margin-bottom: 25px;
}

.detail-section h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #333;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 8px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.detail-item .label {
  font-weight: 500;
  margin-right: 10px;
  color: #666;
  width: 120px;
  flex-shrink: 0;
}

.issues, .improvements {
  margin-top: 15px;
}

.issues ul, .improvements ul {
  margin: 0;
  padding-left: 20px;
}

.issues li, .improvements li {
  margin-bottom: 8px;
  color: #333;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
