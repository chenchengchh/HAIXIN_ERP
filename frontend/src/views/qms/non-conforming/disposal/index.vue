<template>
  <div class="nc-disposal-view">
    <div class="page-header">
      <h3>不合格品处理</h3>
      <div class="header-actions">
        <el-button type="primary" :icon="Check" @click="handleBatchProcess">
          批量处理
        </el-button>
        <el-button :icon="Download" @click="handleExport">
          导出处理记录
        </el-button>
      </div>
    </div>

    <!-- 查询条件 -->
    <el-card class="query-card">
      <el-form :model="queryForm" label-position="left" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="登记编号">
              <el-input v-model="queryForm.registrationNo" placeholder="请输入登记编号" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="物料名称">
              <el-input v-model="queryForm.materialName" placeholder="请输入物料名称" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="处理状态">
              <el-select v-model="queryForm.disposalStatus" placeholder="请选择处理状态">
                <el-option label="待处理" value="pending" />
                <el-option label="处理中" value="processing" />
                <el-option label="已完成" value="completed" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="处置方案">
              <el-select v-model="queryForm.disposalPlan" placeholder="请选择处置方案">
                <el-option label="让步接收" value="concession" />
                <el-option label="返工" value="rework" />
                <el-option label="报废" value="scrap" />
                <el-option label="退货" value="return" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="query-actions">
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 不合格品处理列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="disposalList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="registrationNo" label="登记编号" min-width="150" />
        <el-table-column prop="materialCode" label="物料编码" min-width="120" />
        <el-table-column prop="materialName" label="物料名称" min-width="180" />
        <el-table-column prop="batchNo" label="批次号" min-width="150" />
        <el-table-column prop="quantity" label="不合格数量" width="120" align="center" />
        <el-table-column prop="disposalPlan" label="处置方案" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getDisposalPlanColor(scope.row.disposalPlan)">
              {{ getDisposalPlanName(scope.row.disposalPlan) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="disposalStatus" label="处理状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getDisposalStatusColor(scope.row.disposalStatus)">
              {{ getDisposalStatusName(scope.row.disposalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handler" label="处理人" width="120" align="center" />
        <el-table-column prop="startTime" label="开始时间" min-width="180" align="center" />
        <el-table-column prop="endTime" label="完成时间" min-width="180" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleViewDisposal(scope.row)">
              查看
            </el-button>
            <el-button size="small" v-if="scope.row.disposalStatus === 'pending'" type="success" @click="handleStartProcess(scope.row)">
              开始处理
            </el-button>
            <el-button size="small" v-if="scope.row.disposalStatus === 'processing'" type="primary" @click="handleCompleteProcess(scope.row)">
              完成处理
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

    <!-- 处理详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="处理详情"
      width="800px"
      @close="handleDetailDialogClose"
    >
      <div v-if="selectedDisposal" class="disposal-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="登记编号">{{ selectedDisposal.registrationNo }}</el-descriptions-item>
          <el-descriptions-item label="物料编码">{{ selectedDisposal.materialCode }}</el-descriptions-item>
          <el-descriptions-item label="物料名称">{{ selectedDisposal.materialName }}</el-descriptions-item>
          <el-descriptions-item label="批次号">{{ selectedDisposal.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="不合格数量">{{ selectedDisposal.quantity }}</el-descriptions-item>
          <el-descriptions-item label="处置方案">{{ getDisposalPlanName(selectedDisposal.disposalPlan) }}</el-descriptions-item>
          <el-descriptions-item label="处置说明">{{ selectedDisposal.disposalDescription }}</el-descriptions-item>
          <el-descriptions-item label="处理状态">{{ getDisposalStatusName(selectedDisposal.disposalStatus) }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ selectedDisposal.handler || '未处理' }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ selectedDisposal.startTime || '未开始' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ selectedDisposal.endTime || '未完成' }}</el-descriptions-item>
          <el-descriptions-item label="处理结果">{{ selectedDisposal.processResult || '未处理' }}</el-descriptions-item>
          <el-descriptions-item label="处理说明">{{ selectedDisposal.processDescription || '未处理' }}</el-descriptions-item>
          <el-descriptions-item label="处理附件">{{ selectedDisposal.attachments?.length || 0 }} 个文件</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 处理表单对话框 -->
    <el-dialog
      v-model="processDialogVisible"
      :title="processDialogTitle"
      width="800px"
      @close="handleProcessDialogClose"
    >
      <el-form
        ref="processFormRef"
        :model="processForm"
        label-position="top"
        :rules="processFormRules"
      >
        <el-form-item label="登记编号" prop="registrationNo">
          <el-input v-model="processForm.registrationNo" disabled />
        </el-form-item>
        <el-form-item label="物料名称" prop="materialName">
          <el-input v-model="processForm.materialName" disabled />
        </el-form-item>
        <el-form-item label="批次号" prop="batchNo">
          <el-input v-model="processForm.batchNo" disabled />
        </el-form-item>
        <el-form-item label="不合格数量" prop="quantity">
          <el-input v-model="processForm.quantity" disabled />
        </el-form-item>
        <el-form-item label="处置方案" prop="disposalPlan">
          <el-input v-model="processForm.disposalPlan" disabled />
        </el-form-item>
        <el-form-item label="处置说明" prop="disposalDescription">
          <el-input v-model="processForm.disposalDescription" type="textarea" :rows="3" disabled />
        </el-form-item>
        <el-form-item label="处理人" prop="handler">
          <el-input v-model="processForm.handler" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="处理结果" prop="processResult" v-if="processForm.disposalStatus === 'processing'">
          <el-input
            v-model="processForm.processResult"
            type="textarea"
            :rows="4"
            placeholder="请输入处理结果"
          />
        </el-form-item>
        <el-form-item label="处理说明" prop="processDescription" v-if="processForm.disposalStatus === 'processing'">
          <el-input
            v-model="processForm.processDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入处理说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveProcess">保存处理</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Download } from '@element-plus/icons-vue'
import { unwrapPageResponse, unwrapResponseData } from '@/api'
import { NCDisposalAPI } from '@/api/qms'

// 定义处置数据接口
interface NonConformingDisposal {
  id: number | string
  registrationNo: string
  materialCode: string
  materialName: string
  batchNo: string
  quantity: number
  defectType: string
  defectLevel: string
  defectDescription: string
  reviewStatus: string
  reviewDepartment: string
  reviewOpinion: string
  disposalPlan: string
  disposalDescription: string
  disposalStatus: string
  handler: string
  startTime: string
  endTime: string
  processResult: string
  processDescription: string
  attachments?: any[]
}

// 查询表单
const queryForm = reactive({
  registrationNo: '',
  materialName: '',
  disposalStatus: '',
  disposalPlan: ''
})

// 加载状态
const loading = ref(false)

// 不合格品处理列表
const disposalList = ref<NonConformingDisposal[]>([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 选中项
const selectedDisposals = ref<NonConformingDisposal[]>([])
const selectedDisposal = ref<NonConformingDisposal | null>(null)

// 对话框状态
const detailDialogVisible = ref(false)
const processDialogVisible = ref(false)
const processDialogTitle = ref('开始处理')
const processFormRef = ref()

// 处理表单数据
const processForm = reactive({
  registrationNo: '',
  materialCode: '',
  materialName: '',
  batchNo: '',
  quantity: 0,
  defectType: '',
  defectLevel: '',
  defectDescription: '',
  reviewStatus: '',
  reviewDepartment: '',
  reviewOpinion: '',
  disposalPlan: '',
  disposalDescription: '',
  disposalStatus: 'pending',
  handler: 'admin',
  startTime: '',
  endTime: '',
  processResult: '',
  processDescription: ''
})

// 处理表单验证规则
const processFormRules = {
  handler: [{ required: true, message: '请输入处理人', trigger: 'blur' }],
  processResult: [{ required: true, message: '请输入处理结果', trigger: 'blur' }],
  processDescription: [{ required: true, message: '请输入处理说明', trigger: 'blur' }]
}

// 初始化
onMounted(() => {
  handleQuery()
})

// 查询数据
const toViewModel = (entity: any): NonConformingDisposal => {
  return {
    id: entity?.id ?? '',
    registrationNo: String(entity?.registrationNo ?? ''),
    materialCode: '',
    materialName: '',
    batchNo: '',
    quantity: 0,
    defectType: '',
    defectLevel: '',
    defectDescription: '',
    reviewStatus: '',
    reviewDepartment: '',
    reviewOpinion: '',
    disposalPlan: String(entity?.disposalPlan ?? ''),
    disposalDescription: String(entity?.disposalDescription ?? ''),
    disposalStatus: String(entity?.disposalStatus ?? ''),
    handler: String(entity?.handler ?? ''),
    startTime: String(entity?.startTime ?? ''),
    endTime: String(entity?.endTime ?? ''),
    processResult: String(entity?.processResult ?? ''),
    processDescription: '',
    attachments: []
  }
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await NCDisposalAPI.getNcDisposals({
      page: pagination.currentPage,
      size: pagination.pageSize,
      registrationNo: queryForm.registrationNo || undefined,
      disposalStatus: queryForm.disposalStatus || undefined
    })
    const page = unwrapPageResponse<any>(res)
    const records = page.list as any[]
    let mapped = records.map(toViewModel)

    if (queryForm.disposalPlan) {
      mapped = mapped.filter(item => item.disposalPlan === queryForm.disposalPlan)
    }

    disposalList.value = mapped
    pagination.total = page.total || mapped.length
  } catch (e: any) {
    disposalList.value = []
    pagination.total = 0
    ElMessage.error(e?.message || '获取不合格品处理失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  Object.assign(queryForm, {
    registrationNo: '',
    materialName: '',
    disposalStatus: '',
    disposalPlan: ''
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
  selectedDisposals.value = selection
}

// 处置方案名称映射
const getDisposalPlanName = (plan: string) => {
  const planMap: Record<string, string> = {
    'concession': '让步接收',
    'rework': '返工',
    'scrap': '报废',
    'return': '退货',
    'other': '其他'
  }
  return planMap[plan] || plan
}

// 处置方案颜色映射
const getDisposalPlanColor = (plan: string) => {
  const colorMap: Record<string, string> = {
    'concession': 'warning',
    'rework': 'primary',
    'scrap': 'danger',
    'return': 'info',
    'other': 'default'
  }
  return colorMap[plan] || 'default'
}

// 处理状态名称映射
const getDisposalStatusName = (status: string) => {
  const statusMap: Record<string, string> = {
    'pending': '待处理',
    'processing': '处理中',
    'completed': '已完成'
  }
  return statusMap[status] || status
}

// 处理状态颜色映射
const getDisposalStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'pending': 'warning',
    'processing': 'info',
    'completed': 'success'
  }
  return colorMap[status] || 'default'
}

// 查看处理详情
const handleViewDisposal = (row: any) => {
  NCDisposalAPI.getNcDisposalById(row.id)
    .then(res => {
      selectedDisposal.value = toViewModel(unwrapResponseData<any>(res) || {})
      detailDialogVisible.value = true
    })
    .catch((e: any) => {
      ElMessage.error(e?.message || '获取详情失败')
    })
}

// 开始处理
const handleStartProcess = (row: any) => {
  NCDisposalAPI.startProcessing(row.id)
    .then(() => {
      ElMessage.success('已开始处理')
      handleQuery()
    })
    .catch((e: any) => {
      ElMessage.error(e?.message || '开始处理失败')
    })
}

// 完成处理
const handleCompleteProcess = (row: any) => {
  selectedDisposal.value = JSON.parse(JSON.stringify(row))
  Object.assign(processForm, {
    registrationNo: row.registrationNo,
    materialCode: row.materialCode,
    materialName: row.materialName,
    batchNo: row.batchNo,
    quantity: row.quantity,
    defectType: row.defectType,
    defectLevel: row.defectLevel,
    defectDescription: row.defectDescription,
    reviewStatus: row.reviewStatus,
    reviewDepartment: row.reviewDepartment,
    reviewOpinion: row.reviewOpinion,
    disposalPlan: row.disposalPlan,
    disposalDescription: row.disposalDescription,
    disposalStatus: 'completed',
    handler: row.handler || 'admin',
    startTime: row.startTime,
    endTime: new Date().toISOString(),
    processResult: row.processResult || '',
    processDescription: row.processDescription || ''
  })
  processDialogTitle.value = '完成处理不合格品'
  processDialogVisible.value = true
}

// 批量处理
const handleBatchProcess = () => {
  if (selectedDisposals.value.length === 0) {
    ElMessage.warning('请选择要处理的不合格品')
    return
  }
  NCDisposalAPI.batchStartProcessing({ ids: selectedDisposals.value.map(v => v.id) })
    .then(() => {
      ElMessage.success(`已批量开始处理 ${selectedDisposals.value.length} 个不合格品`)
      selectedDisposals.value = []
      handleQuery()
    })
    .catch((e: any) => {
      ElMessage.error(e?.message || '批量处理失败')
    })
}

// 保存处理
const handleSaveProcess = () => {
  if (!processFormRef.value) return
  
  processFormRef.value.validate((valid: boolean) => {
    if (valid) {
      const targetId = selectedDisposal.value?.id
      if (!targetId) {
        ElMessage.error('未找到处理记录ID')
        return
      }
      const resultText = processForm.processDescription
        ? `${processForm.processResult}\n${processForm.processDescription}`
        : processForm.processResult
      NCDisposalAPI.completeProcessing(targetId, { processResult: resultText })
        .then(() => {
          processDialogVisible.value = false
          ElMessage.success('处理成功')
          handleQuery()
        })
        .catch((e: any) => {
          ElMessage.error(e?.message || '保存处理失败')
        })
    }
  })
}

// 关闭详情对话框
const handleDetailDialogClose = () => {
  selectedDisposal.value = null
}

// 关闭处理对话框
const handleProcessDialogClose = () => {
  if (processFormRef.value) {
    processFormRef.value.resetFields()
  }
}

// 导出处理记录
const handleExport = () => {
  ElMessage.warning('导出功能正在开发中')
}
</script>

<style scoped>
.nc-disposal-view {
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

.disposal-detail {
  margin-bottom: 20px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .nc-disposal-view {
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
  .nc-disposal-view {
    padding: 12px;
  }
  
  .header-actions {
    flex-direction: column;
  }
}
</style>
