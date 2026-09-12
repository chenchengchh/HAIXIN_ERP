<template>
  <div class="anomaly-analysis-view">
    <div class="page-header">
      <h3>异常分析</h3>
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
        <el-form-item label="分析状态">
          <el-select v-model="queryForm.analysisStatus" placeholder="请选择分析状态" clearable>
            <el-option label="待分析" value="pending" />
            <el-option label="分析中" value="processing" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 异常分析列表 -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>异常分析列表</span>
          <el-tag size="small" type="info">{{ total }} 条记录</el-tag>
        </div>
      </template>
      <el-table :data="anomalyAnalyses" border style="width: 100%">
        <el-table-column prop="reportNo" label="异常编号" min-width="120" />
        <el-table-column prop="title" label="异常标题" min-width="180" />
        <el-table-column prop="anomalyType" label="异常类型" min-width="100">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.anomalyType)">
              {{ getTypeLabel(scope.row.anomalyType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="analysisStatus" label="分析状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getAnalysisStatusTagType(scope.row.analysisStatus)">
              {{ getAnalysisStatusLabel(scope.row.analysisStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="analyst" label="分析人" min-width="100" />
        <el-table-column prop="analysisDate" label="分析日期" min-width="120" />
        <el-table-column prop="rootCause" label="根本原因" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" min-width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewDetail(scope.row)">
              查看
            </el-button>
            <el-button size="small" @click="editAnalysis(scope.row)">
              编辑分析
            </el-button>
            <el-button size="small" type="success" @click="completeAnalysis(scope.row)">
              完成分析
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

    <!-- 异常分析对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="60%"
      @close="resetForm"
    >
      <el-form
        ref="analysisFormRef"
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
        <el-form-item label="分析人" prop="analyst">
          <el-input v-model="formData.analyst" placeholder="请输入分析人" />
        </el-form-item>
        <el-form-item label="分析日期" prop="analysisDate">
          <el-date-picker
            v-model="formData.analysisDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="请选择分析日期"
          />
        </el-form-item>
        <el-form-item label="根本原因" prop="rootCause">
          <el-input
            v-model="formData.rootCause"
            type="textarea"
            rows="4"
            placeholder="请输入根本原因分析"
          />
        </el-form-item>
        <el-form-item label="5M1E分析" prop="fiveM1EAnalysis">
          <el-collapse>
            <el-collapse-item title="人机料法环测分析">
              <div class="five-m1e-analysis">
                <el-form-item label="人（Man）" class="mb-2">
                  <el-input
                    v-model="formData.fiveM1EAnalysis.man"
                    type="textarea"
                    rows="2"
                    placeholder="请输入人员方面的分析"
                  />
                </el-form-item>
                <el-form-item label="机（Machine）" class="mb-2">
                  <el-input
                    v-model="formData.fiveM1EAnalysis.machine"
                    type="textarea"
                    rows="2"
                    placeholder="请输入设备方面的分析"
                  />
                </el-form-item>
                <el-form-item label="料（Material）" class="mb-2">
                  <el-input
                    v-model="formData.fiveM1EAnalysis.material"
                    type="textarea"
                    rows="2"
                    placeholder="请输入物料方面的分析"
                  />
                </el-form-item>
                <el-form-item label="法（Method）" class="mb-2">
                  <el-input
                    v-model="formData.fiveM1EAnalysis.method"
                    type="textarea"
                    rows="2"
                    placeholder="请输入方法方面的分析"
                  />
                </el-form-item>
                <el-form-item label="环（Environment）" class="mb-2">
                  <el-input
                    v-model="formData.fiveM1EAnalysis.environment"
                    type="textarea"
                    rows="2"
                    placeholder="请输入环境方面的分析"
                  />
                </el-form-item>
                <el-form-item label="测（Measurement）">
                  <el-input
                    v-model="formData.fiveM1EAnalysis.measurement"
                    type="textarea"
                    rows="2"
                    placeholder="请输入测量方面的分析"
                  />
                </el-form-item>
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-form-item>
        <el-form-item label="临时措施" prop="temporaryMeasures">
          <el-input
            v-model="formData.temporaryMeasures"
            type="textarea"
            rows="3"
            placeholder="请输入临时措施"
          />
        </el-form-item>
        <el-form-item label="分析状态" prop="analysisStatus">
          <el-select v-model="formData.analysisStatus" placeholder="请选择分析状态">
            <el-option label="待分析" value="pending" />
            <el-option label="分析中" value="processing" />
            <el-option label="已完成" value="completed" />
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
      title="异常分析详情"
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
            <el-tag :type="getAnalysisStatusTagType(detailData.analysisStatus)">
              {{ getAnalysisStatusLabel(detailData.analysisStatus) }}
            </el-tag>
          </div>
        </div>
        <div class="detail-body">
          <div class="detail-row">
            <div class="detail-label">异常标题：</div>
            <div class="detail-value">{{ detailData.title }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">分析人：</div>
            <div class="detail-value">{{ detailData.analyst }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">分析日期：</div>
            <div class="detail-value">{{ detailData.analysisDate }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">根本原因：</div>
            <div class="detail-value">{{ detailData.rootCause }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">临时措施：</div>
            <div class="detail-value">{{ detailData.temporaryMeasures }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">5M1E分析：</div>
            <div class="detail-value">
              <el-collapse>
                <el-collapse-item title="人机料法环测分析">
                  <div class="five-m1e-analysis-view">
                    <div class="five-m1e-item">
                      <div class="five-m1e-label">人（Man）：</div>
                      <div class="five-m1e-content">{{ detailData.fiveM1EAnalysis.man || '无' }}</div>
                    </div>
                    <div class="five-m1e-item">
                      <div class="five-m1e-label">机（Machine）：</div>
                      <div class="five-m1e-content">{{ detailData.fiveM1EAnalysis.machine || '无' }}</div>
                    </div>
                    <div class="five-m1e-item">
                      <div class="five-m1e-label">料（Material）：</div>
                      <div class="five-m1e-content">{{ detailData.fiveM1EAnalysis.material || '无' }}</div>
                    </div>
                    <div class="five-m1e-item">
                      <div class="five-m1e-label">法（Method）：</div>
                      <div class="five-m1e-content">{{ detailData.fiveM1EAnalysis.method || '无' }}</div>
                    </div>
                    <div class="five-m1e-item">
                      <div class="five-m1e-label">环（Environment）：</div>
                      <div class="five-m1e-content">{{ detailData.fiveM1EAnalysis.environment || '无' }}</div>
                    </div>
                    <div class="five-m1e-item">
                      <div class="five-m1e-label">测（Measurement）：</div>
                      <div class="five-m1e-content">{{ detailData.fiveM1EAnalysis.measurement || '无' }}</div>
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
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
import { AnomalyAnalysisAPI } from '@/api/qms'

// 异常分析类型
interface FiveM1EAnalysis {
  man: string
  machine: string
  material: string
  method: string
  environment: string
  measurement: string
}

interface AnomalyAnalysis {
  id: string | number
  reportNo: string
  title: string
  anomalyType: 'material' | 'production' | 'inspection' | 'complaint'
  analyst: string
  analysisDate: string
  rootCause: string
  fiveM1EAnalysis: FiveM1EAnalysis
  temporaryMeasures: string
  analysisStatus: 'pending' | 'processing' | 'completed'
}

// 查询表单
const queryForm = reactive({
  reportNo: '',
  anomalyType: '',
  analysisStatus: ''
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

// 异常分析列表
const anomalyAnalyses = ref<AnomalyAnalysis[]>([])
const total = ref(0)

// 对话框数据
const dialogVisible = ref(false)
const dialogTitle = ref('编辑异常分析')
const anomalyFormRef = ref()

// 详情对话框数据
const detailDialogVisible = ref(false)
const detailData = ref<AnomalyAnalysis>({
  id: '',
  reportNo: '',
  title: '',
  anomalyType: 'material',
  analyst: '',
  analysisDate: '',
  rootCause: '',
  fiveM1EAnalysis: {
    man: '',
    machine: '',
    material: '',
    method: '',
    environment: '',
    measurement: ''
  },
  temporaryMeasures: '',
  analysisStatus: 'pending'
})

// 表单数据
const formData = reactive<AnomalyAnalysis>({
  id: '',
  reportNo: '',
  title: '',
  anomalyType: 'material',
  analyst: '',
  analysisDate: new Date().toISOString().slice(0, 10),
  rootCause: '',
  fiveM1EAnalysis: {
    man: '',
    machine: '',
    material: '',
    method: '',
    environment: '',
    measurement: ''
  },
  temporaryMeasures: '',
  analysisStatus: 'pending'
})

// 表单验证规则
const formRules = reactive({
  analyst: [{ required: true, message: '请输入分析人', trigger: 'blur' }],
  analysisDate: [{ required: true, message: '请选择分析日期', trigger: 'change' }],
  rootCause: [{ required: true, message: '请输入根本原因分析', trigger: 'blur' }],
  analysisStatus: [{ required: true, message: '请选择分析状态', trigger: 'change' }]
})

const toViewStatus = (status: any): AnomalyAnalysis['analysisStatus'] => {
  const raw = status == null ? '' : String(status)
  if (raw === 'completed') return 'completed'
  if (raw === 'in_progress') return 'processing'
  return 'pending'
}

const toApiStatus = (status: string): string | undefined => {
  if (!status) return undefined
  if (status === 'processing') return 'in_progress'
  return status
}

const toViewModel = (entity: any): AnomalyAnalysis => {
  return {
    id: entity?.id ?? '',
    reportNo: String(entity?.reportNo ?? ''),
    title: '',
    anomalyType: 'material',
    analyst: String(entity?.analyzer ?? ''),
    analysisDate: String(entity?.analysisDate ?? ''),
    rootCause: String(entity?.rootCause ?? ''),
    fiveM1EAnalysis: {
      man: String(entity?.manFactor ?? ''),
      machine: String(entity?.machineFactor ?? ''),
      material: String(entity?.materialFactor ?? ''),
      method: String(entity?.methodFactor ?? ''),
      environment: String(entity?.environmentFactor ?? ''),
      measurement: String(entity?.measurementFactor ?? '')
    },
    temporaryMeasures: '',
    analysisStatus: toViewStatus(entity?.analysisStatus)
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

// 获取分析状态标签样式
const getAnalysisStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: 'warning',
    processing: 'info',
    completed: 'success'
  }
  return statusMap[status] || 'info'
}

// 获取分析状态标签文本
const getAnalysisStatusLabel = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '待分析',
    processing: '分析中',
    completed: '已完成'
  }
  return statusMap[status] || status
}

const handleQuery = async () => {
  try {
    const res = await AnomalyAnalysisAPI.getAnomalyAnalyses({
      page: pagination.currentPage,
      size: pagination.pageSize,
      reportNo: queryForm.reportNo || undefined,
      analysisStatus: toApiStatus(queryForm.analysisStatus)
    })
    const page = unwrapPageResponse<any>(res)
    const records = page.list as any[]
    let mapped = records.map(toViewModel)

    if (queryForm.anomalyType) {
      mapped = mapped.filter(item => item.anomalyType === queryForm.anomalyType)
    }

    if (queryForm.analysisStatus) {
      mapped = mapped.filter(item => item.analysisStatus === queryForm.analysisStatus)
    }

    anomalyAnalyses.value = mapped
    total.value = page.total || mapped.length
  } catch (e: any) {
    ElMessage.error(e?.message || '获取异常分析失败')
  }
}

// 重置查询表单
const resetQuery = () => {
  Object.assign(queryForm, {
    reportNo: '',
    anomalyType: '',
    analysisStatus: ''
  })
  handleQuery()
}

// 打开编辑对话框
const editAnalysis = (row: AnomalyAnalysis) => {
  dialogTitle.value = '编辑异常分析'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const viewDetail = async (row: AnomalyAnalysis) => {
  try {
    const res = await AnomalyAnalysisAPI.getAnomalyAnalysisById(row.id)
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

const completeAnalysis = (row: AnomalyAnalysis) => {
  ElMessageBox.confirm('确定要将这条异常分析标记为已完成吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  })
    .then(async () => {
      await AnomalyAnalysisAPI.completeAnalysis(row.id)
      ElMessage.success('分析已完成')
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
    reportNo: '',
    title: '',
    analyst: '',
    analysisDate: new Date().toISOString().slice(0, 10),
    rootCause: '',
    fiveM1EAnalysis: {
      man: '',
      machine: '',
      material: '',
      method: '',
      environment: '',
      measurement: ''
    },
    temporaryMeasures: '',
    analysisStatus: 'pending'
  })
}

// 提交表单
const submitForm = async () => {
  if (!anomalyFormRef.value) return
  
  try {
    await anomalyFormRef.value.validate()

    const payload: any = {
      reportNo: formData.reportNo,
      analyzer: formData.analyst,
      analysisDate: formData.analysisDate,
      rootCause: formData.rootCause,
      manFactor: formData.fiveM1EAnalysis?.man,
      machineFactor: formData.fiveM1EAnalysis?.machine,
      materialFactor: formData.fiveM1EAnalysis?.material,
      methodFactor: formData.fiveM1EAnalysis?.method,
      environmentFactor: formData.fiveM1EAnalysis?.environment,
      measurementFactor: formData.fiveM1EAnalysis?.measurement,
      analysisStatus: toApiStatus(String(formData.analysisStatus || 'pending'))
    }

    if (formData.id) {
      await AnomalyAnalysisAPI.updateAnomalyAnalysis(formData.id, payload)
      ElMessage.success('编辑成功')
    } else {
      await AnomalyAnalysisAPI.createAnomalyAnalysis(payload)
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
.anomaly-analysis-view {
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

.mb-2 {
  margin-bottom: 10px;
}

.five-m1e-analysis {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
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

.five-m1e-analysis-view {
  margin-top: 12px;
}

.five-m1e-item {
  margin-bottom: 12px;
  padding-left: 16px;
  border-left: 2px solid #409eff;
}

.five-m1e-item:last-child {
  margin-bottom: 0;
}

.five-m1e-label {
  font-weight: 500;
  color: #606266;
  margin-bottom: 4px;
}

.five-m1e-content {
  color: #303133;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .anomaly-analysis-view {
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
  
  .five-m1e-item {
    padding-left: 12px;
  }
}
</style>
