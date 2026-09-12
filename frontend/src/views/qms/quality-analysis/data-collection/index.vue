<template>
  <div class="data-collection-view">
    <div class="page-header">
      <h3>质量数据采集</h3>
      <el-button type="primary" @click="openImportDialog">
        <el-icon><Upload /></el-icon>导入数据
      </el-button>
    </div>

    <!-- 查询表单 -->
    <el-card class="mb-4" shadow="hover">
      <el-form :model="queryForm" inline>
        <el-form-item label="数据类型">
          <el-select v-model="queryForm.dataType" placeholder="请选择数据类型" clearable>
            <el-option label="检验数据" value="inspection" />
            <el-option label="异常数据" value="anomaly" />
            <el-option label="不合格品数据" value="non-conforming" />
            <el-option label="客户投诉数据" value="complaint" />
          </el-select>
        </el-form-item>
        <el-form-item label="采集日期">
          <el-date-picker
            v-model="queryForm.collectionDate"
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
            <el-option label="已处理" value="processed" />
            <el-option label="已归档" value="archived" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据采集列表 -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>数据采集列表</span>
          <el-tag size="small" type="info">{{ total }} 条记录</el-tag>
        </div>
      </template>
      <el-table :data="dataCollectionList" border style="width: 100%">
        <el-table-column prop="dataNo" label="数据编号" min-width="120" />
        <el-table-column prop="dataType" label="数据类型" min-width="100">
          <template #default="scope">
            <el-tag :type="getDataTypeTagType(scope.row.dataType)">
              {{ getDataTypeLabel(scope.row.dataType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="collectionDate" label="采集日期" min-width="120" />
        <el-table-column prop="source" label="数据来源" min-width="100" />
        <el-table-column prop="quantity" label="数据数量" min-width="100" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="collector" label="采集人" min-width="100" />
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" min-width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewDetail(scope.row)">
              查看
            </el-button>
            <el-button size="small" @click="processData(scope.row)">
              处理
            </el-button>
            <el-button size="small" type="danger" @click="deleteData(scope.row.id)">
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

    <!-- 数据导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入质量数据"
      width="60%"
      @close="resetImportForm"
    >
      <el-form
        ref="importFormRef"
        :model="importFormData"
        :rules="importFormRules"
        label-width="120px"
      >
        <el-form-item label="数据类型" prop="dataType">
          <el-select v-model="importFormData.dataType" placeholder="请选择数据类型">
            <el-option label="检验数据" value="inspection" />
            <el-option label="异常数据" value="anomaly" />
            <el-option label="不合格品数据" value="non-conforming" />
            <el-option label="客户投诉数据" value="complaint" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据文件" prop="file">
          <el-upload
            v-model:file-list="fileList"
            :auto-upload="false"
            :limit="1"
            accept=".xlsx,.xls,.csv"
            :on-exceed="handleExceed"
            :before-remove="beforeRemove"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>选择文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                只能上传 .xlsx, .xls, .csv 文件，且不超过 10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="采集日期" prop="collectionDate">
          <el-date-picker
            v-model="importFormData.collectionDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="请选择采集日期"
          />
        </el-form-item>
        <el-form-item label="数据来源" prop="source">
          <el-input v-model="importFormData.source" placeholder="请输入数据来源" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="importFormData.remark"
            type="textarea"
            rows="2"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitImportForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="质量数据采集详情"
      width="70%"
      @close="closeDetailDialog"
    >
      <div class="detail-container">
        <div class="detail-header">
          <h4 class="data-no">{{ detailData.dataNo }}</h4>
          <div class="data-info">
            <el-tag :type="getDataTypeTagType(detailData.dataType)">
              {{ getDataTypeLabel(detailData.dataType) }}
            </el-tag>
            <el-tag :type="getStatusTagType(detailData.status)">
              {{ getStatusLabel(detailData.status) }}
            </el-tag>
          </div>
        </div>
        <div class="detail-body">
          <div class="detail-row">
            <div class="detail-label">数据类型：</div>
            <div class="detail-value">{{ getDataTypeLabel(detailData.dataType) }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">采集日期：</div>
            <div class="detail-value">{{ detailData.collectionDate }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">数据来源：</div>
            <div class="detail-value">{{ detailData.source }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">数据数量：</div>
            <div class="detail-value">{{ detailData.quantity }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">采集人：</div>
            <div class="detail-value">{{ detailData.collector }}</div>
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
import { Upload } from '@element-plus/icons-vue'
import type { UploadProps } from 'element-plus'
import { unwrapPageResponse, unwrapResponseData } from '@/api'
import { DataCollectionAPI } from '@/api/qms'

// 数据采集类型
interface DataCollection {
  id: string | number
  dataNo: string
  dataType: 'inspection' | 'anomaly' | 'non-conforming' | 'complaint'
  collectionDate: string
  source: string
  quantity: number
  status: 'pending' | 'processed' | 'archived'
  collector: string
  remark: string
}

// 查询表单
const queryForm = reactive({
  dataType: '',
  collectionDate: [] as string[],
  status: ''
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

// 数据采集列表
const dataCollectionList = ref<DataCollection[]>([])
const total = ref(0)

// 对话框数据
const importDialogVisible = ref(false)
const importFormRef = ref()

// 文件上传
const fileList = ref([])

// 导入表单数据
const importFormData = reactive({
  dataType: '',
  collectionDate: new Date().toISOString().slice(0, 10),
  source: '',
  remark: ''
})

// 导入表单验证规则
const importFormRules = reactive({
  dataType: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
  collectionDate: [{ required: true, message: '请选择采集日期', trigger: 'change' }],
  source: [{ required: true, message: '请输入数据来源', trigger: 'blur' }]
})

// 详情对话框数据
const detailDialogVisible = ref(false)
const detailData = ref<DataCollection>({
  id: '',
  dataNo: '',
  dataType: 'inspection',
  collectionDate: '',
  source: '',
  quantity: 0,
  status: 'pending',
  collector: '',
  remark: ''
})

const toViewStatus = (status: any): DataCollection['status'] => {
  const raw = status == null ? '' : String(status)
  if (raw === 'submitted') return 'processed'
  if (raw === 'approved') return 'archived'
  return 'pending'
}

const toApiStatus = (status: string): string | undefined => {
  if (!status) return undefined
  if (status === 'processed') return 'submitted'
  if (status === 'archived') return 'approved'
  return 'draft'
}

const toDataCollectionViewModel = (entity: any): DataCollection => {
  const items = Array.isArray(entity?.dataItems) ? entity.dataItems : []
  const meta = items.find((v: any) => v && typeof v === 'object' && v.type === 'meta')
  const metaRemark = meta?.remark ?? ''
  const rowCount = items.filter((v: any) => v && typeof v === 'object' && v.type !== 'meta').length

  return {
    id: entity?.id ?? '',
    dataNo: String(entity?.collectionNo ?? ''),
    dataType: (entity?.dataType ?? 'inspection') as any,
    collectionDate: String(entity?.collectionDate ?? ''),
    source: String(entity?.source ?? ''),
    quantity: rowCount,
    status: toViewStatus(entity?.status),
    collector: String(entity?.collector ?? ''),
    remark: String(metaRemark ?? '')
  }
}

// 文件上传限制
const handleExceed: UploadProps['onExceed'] = (files, uploadFiles) => {
  ElMessage.warning(`只能上传一个文件，请先删除已选择的文件`)
}

// 文件删除前确认
const beforeRemove: UploadProps['beforeRemove'] = (uploadFile, uploadFiles) => {
  return ElMessageBox.confirm(`确定要删除 ${uploadFile.name} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return true
  }).catch(() => {
    return false
  })
}

// 获取数据类型标签样式
const getDataTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    inspection: 'info',
    anomaly: 'warning',
    'non-conforming': 'danger',
    complaint: 'success'
  }
  return typeMap[type] || 'info'
}

// 获取数据类型标签文本
const getDataTypeLabel = (type: string) => {
  const typeMap: Record<string, string> = {
    inspection: '检验数据',
    anomaly: '异常数据',
    'non-conforming': '不合格品数据',
    complaint: '客户投诉数据'
  }
  return typeMap[type] || type
}

// 获取状态标签样式
const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: 'warning',
    processed: 'success',
    archived: 'success'
  }
  return statusMap[status] || 'info'
}

// 获取状态标签文本
const getStatusLabel = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '待处理',
    processed: '已处理',
    archived: '已归档'
  }
  return statusMap[status] || status
}

const handleQuery = async () => {
  try {
    const startDate = queryForm.collectionDate?.[0]
    const endDate = queryForm.collectionDate?.[1]
    const params: any = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      status: toApiStatus(queryForm.status)
    }
    if (queryForm.dataType) params.dataType = queryForm.dataType
    if (startDate) params.collectionDateStart = startDate
    if (endDate) params.collectionDateEnd = endDate

    const res = await DataCollectionAPI.getDataCollections(params)
    const page = unwrapPageResponse<any>(res)
    const records = page.list as any[]
    const mapped = records.map(toDataCollectionViewModel)
    dataCollectionList.value = mapped
    total.value = Number(page.total ?? 0)
  } catch (e: any) {
    ElMessage.error(e?.message || '获取质量数据采集失败')
  }
}

// 重置查询表单
const resetQuery = () => {
  Object.assign(queryForm, {
    dataType: '',
    collectionDate: [],
    status: ''
  })
  handleQuery()
}

// 打开导入对话框
const openImportDialog = () => {
  resetImportForm()
  importDialogVisible.value = true
}

const viewDetail = async (row: DataCollection) => {
  try {
    const res = await DataCollectionAPI.getDataCollectionById(row.id)
    detailData.value = toDataCollectionViewModel(unwrapResponseData<any>(res) || {})
    detailDialogVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '获取详情失败')
  }
}

// 关闭详情对话框
const closeDetailDialog = () => {
  detailDialogVisible.value = false
}

const processData = (row: DataCollection) => {
  ElMessageBox.confirm('确定要处理这条数据吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  })
    .then(async () => {
      await DataCollectionAPI.submitDataCollection(row.id)
      ElMessage.success('数据处理成功')
      await handleQuery()
    })
    .catch(() => {})
}

// 删除数据
const deleteData = (id: string | number) => {
  ElMessageBox.confirm('确定要删除这条数据吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await DataCollectionAPI.deleteDataCollection(id)
      ElMessage.success('删除成功')
      await handleQuery()
    })
    .catch(() => {})
}

// 重置导入表单
const resetImportForm = () => {
  if (importFormRef.value) {
    importFormRef.value.resetFields()
  }
  fileList.value = []
  Object.assign(importFormData, {
    dataType: '',
    collectionDate: new Date().toISOString().slice(0, 10),
    source: '',
    remark: ''
  })
}

// 提交导入表单
const submitImportForm = async () => {
  if (!importFormRef.value) return
  
  try {
    await importFormRef.value.validate()
    
    if (fileList.value.length === 0) {
      ElMessage.error('请选择要导入的文件')
      return
    }

    const rawFile = (fileList.value[0] as any)?.raw
    if (!rawFile) {
      ElMessage.error('文件读取失败，请重新选择文件')
      return
    }

    const importFileForm = new FormData()
    importFileForm.append('file', rawFile)
    const importRes = await DataCollectionAPI.importData(importFileForm)
    const importInfo = unwrapResponseData<any>(importRes) || {}

    const importedRows = Array.isArray(importInfo.dataItems) ? importInfo.dataItems : []
    const dataItems = [
      {
        type: 'meta',
        fileName: importInfo.fileName ?? rawFile.name,
        fileSize: importInfo.size ?? rawFile.size,
        imported: importInfo.imported ?? importedRows.length,
        headers: importInfo.headers ?? [],
        remark: importFormData.remark
      },
      ...importedRows.map((row: any) => ({
        type: 'row',
        ...row
      }))
    ]

    const createPayload: any = {
      collectionName: `质量数据采集-${importFormData.dataType}`,
      collectionDate: importFormData.collectionDate,
      dataType: importFormData.dataType,
      source: importFormData.source,
      collector: 'admin',
      status: 'draft',
      dataItems
    }

    await DataCollectionAPI.createDataCollection(createPayload)
    ElMessage.success('数据导入成功')
    importDialogVisible.value = false
    resetImportForm()
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
onMounted(handleQuery)
</script>

<style scoped>
.data-collection-view {
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

.data-no {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.data-info {
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
  .data-collection-view {
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
