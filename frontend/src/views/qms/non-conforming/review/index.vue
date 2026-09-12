<template>
  <div class="nc-review-view">
    <div class="page-header">
      <h3>不合格品评审</h3>
      <div class="header-actions">
        <el-button type="primary" :icon="Check" @click="handleBatchReview">
          批量评审
        </el-button>
        <el-button :icon="Download" @click="handleExport">
          导出评审
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
            <el-form-item label="评审状态">
              <el-select v-model="queryForm.reviewStatus" placeholder="请选择评审状态">
                <el-option label="待评审" value="pending" />
                <el-option label="已批准" value="approved" />
                <el-option label="已驳回" value="rejected" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="评审部门">
              <el-input v-model="queryForm.reviewDepartment" placeholder="请输入评审部门" />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="query-actions">
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 不合格品评审列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="reviewList"
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
        <el-table-column prop="defectType" label="缺陷类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getDefectTypeColor(scope.row.defectType)">
              {{ getDefectTypeName(scope.row.defectType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewStatus" label="评审状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getReviewStatusColor(scope.row.reviewStatus)">
              {{ getReviewStatusName(scope.row.reviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewDepartment" label="评审部门" width="150" align="center" />
        <el-table-column prop="reviewer" label="评审人" width="120" align="center" />
        <el-table-column prop="reviewTime" label="评审时间" min-width="180" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleViewReview(scope.row)">
              查看
            </el-button>
            <el-button size="small" v-if="scope.row.reviewStatus === 'pending'" type="success" @click="handleApproveReview(scope.row)">
              批准
            </el-button>
            <el-button size="small" v-if="scope.row.reviewStatus === 'pending'" type="danger" @click="handleRejectReview(scope.row)">
              驳回
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

    <!-- 评审详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="评审详情"
      width="800px"
      @close="handleDetailDialogClose"
    >
      <div v-if="selectedReview" class="review-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="登记编号">{{ selectedReview.registrationNo }}</el-descriptions-item>
          <el-descriptions-item label="物料编码">{{ selectedReview.materialCode }}</el-descriptions-item>
          <el-descriptions-item label="物料名称">{{ selectedReview.materialName }}</el-descriptions-item>
          <el-descriptions-item label="批次号">{{ selectedReview.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="不合格数量">{{ selectedReview.quantity }}</el-descriptions-item>
          <el-descriptions-item label="缺陷类型">{{ getDefectTypeName(selectedReview.defectType) }}</el-descriptions-item>
          <el-descriptions-item label="缺陷等级">{{ getDefectLevelName(selectedReview.defectLevel) }}</el-descriptions-item>
          <el-descriptions-item label="缺陷描述">{{ selectedReview.defectDescription }}</el-descriptions-item>
          <el-descriptions-item label="发现部门">{{ selectedReview.discoveryDepartment }}</el-descriptions-item>
          <el-descriptions-item label="发现人">{{ selectedReview.discoveryPerson }}</el-descriptions-item>
          <el-descriptions-item label="发现时间">{{ selectedReview.discoveryTime }}</el-descriptions-item>
          <el-descriptions-item label="发现位置">{{ selectedReview.discoveryLocation }}</el-descriptions-item>
          <el-descriptions-item label="评审状态">{{ getReviewStatusName(selectedReview.reviewStatus) }}</el-descriptions-item>
          <el-descriptions-item label="评审部门">{{ selectedReview.reviewDepartment }}</el-descriptions-item>
          <el-descriptions-item label="评审人">{{ selectedReview.reviewer || '未评审' }}</el-descriptions-item>
          <el-descriptions-item label="评审时间">{{ selectedReview.reviewTime || '未评审' }}</el-descriptions-item>
          <el-descriptions-item label="评审意见">{{ selectedReview.reviewOpinion || '未评审' }}</el-descriptions-item>
          <el-descriptions-item label="处置方案">{{ selectedReview.disposalPlan || '未制定' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 评审表单对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="reviewDialogTitle"
      width="800px"
      @close="handleReviewDialogClose"
    >
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        label-position="top"
        :rules="reviewFormRules"
      >
        <el-form-item label="登记编号" prop="registrationNo">
          <el-input v-model="reviewForm.registrationNo" disabled />
        </el-form-item>
        <el-form-item label="物料名称" prop="materialName">
          <el-input v-model="reviewForm.materialName" disabled />
        </el-form-item>
        <el-form-item label="批次号" prop="batchNo">
          <el-input v-model="reviewForm.batchNo" disabled />
        </el-form-item>
        <el-form-item label="不合格数量" prop="quantity">
          <el-input v-model="reviewForm.quantity" disabled />
        </el-form-item>
        <el-form-item label="缺陷描述" prop="defectDescription">
          <el-input v-model="reviewForm.defectDescription" type="textarea" :rows="3" disabled />
        </el-form-item>
        <el-form-item label="评审部门" prop="reviewDepartment">
          <el-input v-model="reviewForm.reviewDepartment" placeholder="请输入评审部门" />
        </el-form-item>
        <el-form-item label="评审意见" prop="reviewOpinion">
          <el-input
            v-model="reviewForm.reviewOpinion"
            type="textarea"
            :rows="4"
            placeholder="请输入评审意见"
          />
        </el-form-item>
        <el-form-item label="处置方案" prop="disposalPlan">
          <el-select v-model="reviewForm.disposalPlan" placeholder="请选择处置方案">
            <el-option label="让步接收" value="concession" />
            <el-option label="返工" value="rework" />
            <el-option label="报废" value="scrap" />
            <el-option label="退货" value="return" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="处置说明" prop="disposalDescription">
          <el-input
            v-model="reviewForm.disposalDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入处置说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveReview">保存评审</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Download } from '@element-plus/icons-vue'
import { unwrapPageResponse } from '@/api'
import { NCReviewAPI } from '@/api/qms'

// 定义评审数据接口
interface NonConformingReview {
  id: number
  registrationNo: string
  materialCode: string
  materialName: string
  batchNo: string
  quantity: number
  defectType: string
  defectLevel: string
  defectDescription: string
  discoveryDepartment: string
  discoveryPerson: string
  discoveryTime: string
  discoveryLocation: string
  reviewStatus: string
  reviewDepartment: string
  reviewer: string
  reviewTime: string
  reviewOpinion: string
  disposalPlan: string
  disposalDescription: string
  [key: string]: any
}

// 查询表单
const queryForm = reactive({
  registrationNo: '',
  materialName: '',
  reviewStatus: '',
  reviewDepartment: ''
})

// 加载状态
const loading = ref(false)

// 不合格品评审列表
const reviewList = ref<NonConformingReview[]>([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 选中项
const selectedReviews = ref<NonConformingReview[]>([])
const selectedReview = ref<NonConformingReview | null>(null)

// 对话框状态
const detailDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const reviewDialogTitle = ref('评审不合格品')
const reviewFormRef = ref()

// 评审表单数据
const reviewForm = reactive({
  registrationNo: '',
  materialCode: '',
  materialName: '',
  batchNo: '',
  quantity: 0,
  defectType: '',
  defectLevel: '',
  defectDescription: '',
  discoveryDepartment: '',
  discoveryPerson: '',
  discoveryTime: '',
  discoveryLocation: '',
  reviewDepartment: '',
  reviewOpinion: '',
  disposalPlan: '',
  disposalDescription: '',
  reviewStatus: 'pending',
  reviewer: 'admin',
  reviewTime: new Date().toLocaleString()
})

// 评审表单验证规则
const reviewFormRules = {
  reviewDepartment: [{ required: true, message: '请输入评审部门', trigger: 'blur' }],
  reviewOpinion: [{ required: true, message: '请输入评审意见', trigger: 'blur' }],
  disposalPlan: [{ required: true, message: '请选择处置方案', trigger: 'change' }]
}

// 初始化
onMounted(() => {
  handleQuery()
})

// 查询数据
async function handleQuery() {
  loading.value = true
  try {
    const res = await NCReviewAPI.getNcReviews({
      page: pagination.currentPage,
      size: pagination.pageSize,
      registrationNo: queryForm.registrationNo || undefined,
      reviewStatus: queryForm.reviewStatus || undefined
    } as any)
    const page = unwrapPageResponse<any>(res)
    reviewList.value = page.list
    pagination.total = page.total
  } catch (e: any) {
    ElMessage.error(e?.message || '获取评审列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  Object.assign(queryForm, {
    registrationNo: '',
    materialName: '',
    reviewStatus: '',
    reviewDepartment: ''
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
  selectedReviews.value = selection
}

// 缺陷类型名称映射
const getDefectTypeName = (type: string) => {
  const typeMap: Record<string, string> = {
    'appearance': '外观缺陷',
    'dimension': '尺寸缺陷',
    'performance': '性能缺陷',
    'function': '功能缺陷',
    'other': '其他缺陷'
  }
  return typeMap[type] || type
}

// 缺陷类型颜色映射
const getDefectTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'appearance': 'primary',
    'dimension': 'success',
    'performance': 'warning',
    'function': 'danger',
    'other': 'info'
  }
  return colorMap[type] || 'default'
}

// 缺陷等级名称映射
const getDefectLevelName = (level: string) => {
  const levelMap: Record<string, string> = {
    'critical': '致命缺陷',
    'major': '严重缺陷',
    'minor': '轻微缺陷'
  }
  return levelMap[level] || level
}

// 评审状态名称映射
const getReviewStatusName = (status: string) => {
  const statusMap: Record<string, string> = {
    'pending': '待评审',
    'approved': '已批准',
    'rejected': '已驳回'
  }
  return statusMap[status] || status
}

// 评审状态颜色映射
const getReviewStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger'
  }
  return colorMap[status] || 'default'
}

// 查看评审详情
const handleViewReview = (row: any) => {
  selectedReview.value = JSON.parse(JSON.stringify(row))
  detailDialogVisible.value = true
}

// 批准评审
const handleApproveReview = (row: any) => {
  selectedReview.value = JSON.parse(JSON.stringify(row))
  Object.assign(reviewForm, {
    registrationNo: row.registrationNo,
    materialCode: row.materialCode,
    materialName: row.materialName,
    batchNo: row.batchNo,
    quantity: row.quantity,
    defectType: row.defectType,
    defectLevel: row.defectLevel,
    defectDescription: row.defectDescription,
    discoveryDepartment: row.discoveryDepartment,
    discoveryPerson: row.discoveryPerson,
    discoveryTime: row.discoveryTime,
    discoveryLocation: row.discoveryLocation,
    reviewDepartment: row.reviewDepartment || '',
    reviewOpinion: row.reviewOpinion || '',
    disposalPlan: row.disposalPlan || '',
    disposalDescription: row.disposalDescription || '',
    reviewStatus: 'approved'
  })
  reviewDialogTitle.value = '批准不合格品'
  reviewDialogVisible.value = true
}

// 驳回评审
const handleRejectReview = (row: any) => {
  selectedReview.value = JSON.parse(JSON.stringify(row))
  Object.assign(reviewForm, {
    registrationNo: row.registrationNo,
    materialCode: row.materialCode,
    materialName: row.materialName,
    batchNo: row.batchNo,
    quantity: row.quantity,
    defectType: row.defectType,
    defectLevel: row.defectLevel,
    defectDescription: row.defectDescription,
    discoveryDepartment: row.discoveryDepartment,
    discoveryPerson: row.discoveryPerson,
    discoveryTime: row.discoveryTime,
    discoveryLocation: row.discoveryLocation,
    reviewDepartment: row.reviewDepartment || '',
    reviewOpinion: row.reviewOpinion || '',
    disposalPlan: row.disposalPlan || '',
    disposalDescription: row.disposalDescription || '',
    reviewStatus: 'rejected'
  })
  reviewDialogTitle.value = '驳回不合格品'
  reviewDialogVisible.value = true
}

// 批量评审
const handleBatchReview = () => {
  if (selectedReviews.value.length === 0) {
    ElMessage.warning('请选择要评审的不合格品')
    return
  }
  handleBatchReviewInternal()
}

const handleBatchReviewInternal = async () => {
  try {
    loading.value = true
    const ids = selectedReviews.value.map((r: any) => r.id)
    await NCReviewAPI.batchApproveNcReviews({
      ids,
      reviewStatus: 'approved',
      reviewOpinion: ''
    })
    ElMessage.success(`已批量评审 ${ids.length} 个不合格品`)
    selectedReviews.value = []
    await handleQuery()
  } catch (e: any) {
    ElMessage.error(e?.message || '批量评审失败')
  } finally {
    loading.value = false
  }
}

// 保存评审
const handleSaveReview = () => {
  if (!reviewFormRef.value) return
  
  reviewFormRef.value.validate((valid: boolean) => {
    if (valid) {
      handleSaveReviewInternal()
    }
  })
}

const handleSaveReviewInternal = async () => {
  if (!selectedReview.value) return
  try {
    loading.value = true
    const payload: any = {
      registrationNo: reviewForm.registrationNo,
      reviewTeam: [],
      reviewOpinion: reviewForm.reviewOpinion,
      disposalPlan: reviewForm.disposalPlan,
      reviewStatus: reviewForm.reviewStatus,
      reviewer: reviewForm.reviewer
    }
    await NCReviewAPI.updateNcReview(selectedReview.value.id, payload)
    if (reviewForm.reviewStatus === 'approved' || reviewForm.reviewStatus === 'rejected') {
      await NCReviewAPI.approveNcReview(selectedReview.value.id, {
        reviewStatus: reviewForm.reviewStatus,
        reviewOpinion: reviewForm.reviewOpinion
      })
    }
    reviewDialogVisible.value = false
    ElMessage.success('评审成功')
    await handleQuery()
  } catch (e: any) {
    ElMessage.error(e?.message || '评审保存失败')
  } finally {
    loading.value = false
  }
}

// 关闭详情对话框
const handleDetailDialogClose = () => {
  selectedReview.value = null
}

// 关闭评审对话框
const handleReviewDialogClose = () => {
  if (reviewFormRef.value) {
    reviewFormRef.value.resetFields()
  }
}

// 导出评审
const handleExport = () => {
  if (!reviewList.value.length) {
    ElMessage.warning('暂无可导出的数据')
    return
  }
  const headers = ['登记编号', '评审状态', '处置方案', '评审人', '评审时间']
  const rows = reviewList.value.map((r: any) => [
    r.registrationNo,
    r.reviewStatus,
    r.disposalPlan,
    r.reviewer,
    r.reviewTime
  ])
  const csv = [headers, ...rows].map(line => line.map(v => `"${String(v ?? '').replace(/\"/g, '\"\"')}"`).join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `nc-reviews-${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.nc-review-view {
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

.review-detail {
  margin-bottom: 20px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .nc-review-view {
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
  .nc-review-view {
    padding: 12px;
  }
  
  .header-actions {
    flex-direction: column;
  }
}
</style>
