<template>
  <div class="nc-tracking-view">
    <div class="page-header">
      <h3>不合格品追踪</h3>
      <div class="header-actions">
        <el-button type="primary" :icon="Check" @click="handleBatchConfirm">
          批量确认
        </el-button>
        <el-button :icon="Download" @click="handleExport">
          导出追踪记录
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
            <el-form-item label="追踪状态">
              <el-select v-model="queryForm.trackingStatus" placeholder="请选择追踪状态">
                <el-option label="待追踪" value="pending" />
                <el-option label="追踪中" value="tracking" />
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

    <!-- 不合格品追踪列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="trackingList"
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
        <el-table-column prop="trackingStatus" label="追踪状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getTrackingStatusColor(scope.row.trackingStatus)">
              {{ getTrackingStatusName(scope.row.trackingStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tracker" label="追踪人" width="120" align="center" />
        <el-table-column prop="trackingTime" label="追踪时间" min-width="180" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleViewTracking(scope.row)">
              查看
            </el-button>
            <el-button size="small" v-if="scope.row.trackingStatus === 'pending'" type="success" @click="handleStartTracking(scope.row)">
              开始追踪
            </el-button>
            <el-button size="small" v-if="scope.row.trackingStatus === 'tracking'" type="primary" @click="handleCompleteTracking(scope.row)">
              完成追踪
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

    <!-- 追踪详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="追踪详情"
      width="800px"
      @close="handleDetailDialogClose"
    >
      <div v-if="selectedTracking" class="tracking-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="登记编号">{{ selectedTracking.registrationNo }}</el-descriptions-item>
          <el-descriptions-item label="物料编码">{{ selectedTracking.materialCode }}</el-descriptions-item>
          <el-descriptions-item label="物料名称">{{ selectedTracking.materialName }}</el-descriptions-item>
          <el-descriptions-item label="批次号">{{ selectedTracking.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="不合格数量">{{ selectedTracking.quantity }}</el-descriptions-item>
          <el-descriptions-item label="缺陷类型">{{ getDefectTypeName(selectedTracking.defectType) }}</el-descriptions-item>
          <el-descriptions-item label="缺陷描述">{{ selectedTracking.defectDescription }}</el-descriptions-item>
          <el-descriptions-item label="处置方案">{{ getDisposalPlanName(selectedTracking.disposalPlan) }}</el-descriptions-item>
          <el-descriptions-item label="处理状态">{{ getDisposalStatusName(selectedTracking.disposalStatus) }}</el-descriptions-item>
          <el-descriptions-item label="处理结果">{{ selectedTracking.processResult || '未处理' }}</el-descriptions-item>
          <el-descriptions-item label="追踪状态">{{ getTrackingStatusName(selectedTracking.trackingStatus) }}</el-descriptions-item>
          <el-descriptions-item label="追踪人">{{ selectedTracking.tracker || '未追踪' }}</el-descriptions-item>
          <el-descriptions-item label="追踪时间">{{ selectedTracking.trackingTime || '未追踪' }}</el-descriptions-item>
          <el-descriptions-item label="追踪结果">{{ selectedTracking.trackingResult || '未追踪' }}</el-descriptions-item>
          <el-descriptions-item label="追踪说明">{{ selectedTracking.trackingDescription || '未追踪' }}</el-descriptions-item>
          <el-descriptions-item label="验证结果">{{ selectedTracking.verifyResult || '未验证' }}</el-descriptions-item>
          <el-descriptions-item label="预防措施">{{ selectedTracking.preventiveMeasures || '未制定' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 追踪表单对话框 -->
    <el-dialog
      v-model="trackingDialogVisible"
      :title="trackingDialogTitle"
      width="800px"
      @close="handleTrackingDialogClose"
    >
      <el-form
        ref="trackingFormRef"
        :model="trackingForm"
        label-position="top"
        :rules="trackingFormRules"
      >
        <el-form-item label="登记编号" prop="registrationNo">
          <el-input v-model="trackingForm.registrationNo" disabled />
        </el-form-item>
        <el-form-item label="物料名称" prop="materialName">
          <el-input v-model="trackingForm.materialName" disabled />
        </el-form-item>
        <el-form-item label="批次号" prop="batchNo">
          <el-input v-model="trackingForm.batchNo" disabled />
        </el-form-item>
        <el-form-item label="处置方案" prop="disposalPlan">
          <el-input v-model="trackingForm.disposalPlan" disabled />
        </el-form-item>
        <el-form-item label="处理结果" prop="processResult">
          <el-input v-model="trackingForm.processResult" type="textarea" :rows="3" disabled />
        </el-form-item>
        <el-form-item label="追踪人" prop="tracker">
          <el-input v-model="trackingForm.tracker" placeholder="请输入追踪人" />
        </el-form-item>
        <el-form-item label="追踪结果" prop="trackingResult" v-if="trackingForm.trackingStatus === 'tracking'">
          <el-input
            v-model="trackingForm.trackingResult"
            type="textarea"
            :rows="4"
            placeholder="请输入追踪结果"
          />
        </el-form-item>
        <el-form-item label="追踪说明" prop="trackingDescription" v-if="trackingForm.trackingStatus === 'tracking'">
          <el-input
            v-model="trackingForm.trackingDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入追踪说明"
          />
        </el-form-item>
        <el-form-item label="验证结果" prop="verifyResult" v-if="trackingForm.trackingStatus === 'completed'">
          <el-input
            v-model="trackingForm.verifyResult"
            type="textarea"
            :rows="3"
            placeholder="请输入验证结果"
          />
        </el-form-item>
        <el-form-item label="预防措施" prop="preventiveMeasures" v-if="trackingForm.trackingStatus === 'completed'">
          <el-input
            v-model="trackingForm.preventiveMeasures"
            type="textarea"
            :rows="4"
            placeholder="请输入预防措施"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="trackingDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveTracking">保存追踪</el-button>
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
import { NCTrackingAPI } from '@/api/qms'

// 定义追踪数据接口
interface NonConformingTracking {
  id: number | string
  registrationNo: string
  materialCode: string
  materialName: string
  batchNo: string
  quantity: number
  defectType: string
  defectDescription: string
  reviewStatus: string
  disposalPlan: string
  disposalStatus: string
  processResult: string
  trackingStatus: string
  tracker: string
  trackingTime: string
  trackingResult: string
  trackingDescription: string
  verifyResult: string
  preventiveMeasures: string
}

// 查询表单
const queryForm = reactive({
  registrationNo: '',
  materialName: '',
  trackingStatus: '',
  disposalPlan: ''
})

// 加载状态
const loading = ref(false)

// 不合格品追踪列表
const trackingList = ref<NonConformingTracking[]>([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 选中项
const selectedTrackings = ref<NonConformingTracking[]>([])
const selectedTracking = ref<NonConformingTracking | null>(null)

// 对话框状态
const detailDialogVisible = ref(false)
const trackingDialogVisible = ref(false)
const trackingDialogTitle = ref('开始追踪')
const trackingFormRef = ref()

// 追踪表单数据
const trackingForm = reactive({
  registrationNo: '',
  materialCode: '',
  materialName: '',
  batchNo: '',
  quantity: 0,
  defectType: '',
  defectDescription: '',
  reviewStatus: '',
  disposalPlan: '',
  disposalStatus: '',
  processResult: '',
  trackingStatus: 'pending',
  tracker: 'admin',
  trackingTime: new Date().toLocaleString(),
  trackingResult: '',
  trackingDescription: '',
  verifyResult: '',
  preventiveMeasures: ''
})

// 追踪表单验证规则
const trackingFormRules = {
  tracker: [{ required: true, message: '请输入追踪人', trigger: 'blur' }],
  trackingResult: [{ required: true, message: '请输入追踪结果', trigger: 'blur' }],
  trackingDescription: [{ required: true, message: '请输入追踪说明', trigger: 'blur' }],
  verifyResult: [{ required: true, message: '请输入验证结果', trigger: 'blur' }],
  preventiveMeasures: [{ required: true, message: '请输入预防措施', trigger: 'blur' }]
}

// 初始化
onMounted(() => {
  handleQuery()
})

// 查询数据
const toViewModel = (entity: any): NonConformingTracking => {
  const effectiveness = String(entity?.effectiveness ?? '')
  const verifyResult =
    effectiveness === 'effective' ? '有效' : effectiveness === 'ineffective' ? '无效' : ''
  const trackingContent = String(entity?.trackingContent ?? '')

  return {
    id: entity?.id ?? '',
    registrationNo: String(entity?.registrationNo ?? ''),
    materialCode: '',
    materialName: '',
    batchNo: '',
    quantity: 0,
    defectType: '',
    defectDescription: '',
    reviewStatus: '',
    disposalPlan: String(entity?.disposalPlan ?? ''),
    disposalStatus: String(entity?.disposalStatus ?? ''),
    processResult: '',
    trackingStatus: String(entity?.trackingStatus ?? ''),
    tracker: String(entity?.tracker ?? ''),
    trackingTime: String(entity?.trackingTime ?? ''),
    trackingResult: trackingContent,
    trackingDescription: trackingContent,
    verifyResult,
    preventiveMeasures: String(entity?.improvementSuggestions ?? '')
  }
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await NCTrackingAPI.getNcTrackings({
      page: pagination.currentPage,
      size: pagination.pageSize,
      registrationNo: queryForm.registrationNo || undefined,
      trackingStatus: queryForm.trackingStatus || undefined
    })
    const page = unwrapPageResponse<any>(res)
    const records = page.list as any[]
    let mapped = records.map(toViewModel)

    if (queryForm.disposalPlan) {
      mapped = mapped.filter(item => item.disposalPlan === queryForm.disposalPlan)
    }

    trackingList.value = mapped
    pagination.total = page.total || mapped.length
  } catch (e: any) {
    trackingList.value = []
    pagination.total = 0
    ElMessage.error(e?.message || '获取不合格品追踪失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  Object.assign(queryForm, {
    registrationNo: '',
    materialName: '',
    trackingStatus: '',
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
  selectedTrackings.value = selection
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

// 追踪状态名称映射
const getTrackingStatusName = (status: string) => {
  const statusMap: Record<string, string> = {
    'pending': '待追踪',
    'tracking': '追踪中',
    'completed': '已完成'
  }
  return statusMap[status] || status
}

// 追踪状态颜色映射
const getTrackingStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'pending': 'warning',
    'tracking': 'info',
    'completed': 'success'
  }
  return colorMap[status] || 'default'
}

// 查看追踪详情
const handleViewTracking = (row: any) => {
  NCTrackingAPI.getNcTrackingById(row.id)
    .then(res => {
      selectedTracking.value = toViewModel(unwrapResponseData<any>(res) || {})
      detailDialogVisible.value = true
    })
    .catch((e: any) => {
      ElMessage.error(e?.message || '获取详情失败')
    })
}

// 开始追踪
const handleStartTracking = (row: any) => {
  NCTrackingAPI.startTracking(row.id)
    .then(() => {
      ElMessage.success('已开始追踪')
      handleQuery()
    })
    .catch((e: any) => {
      ElMessage.error(e?.message || '开始追踪失败')
    })
}

// 完成追踪
const handleCompleteTracking = (row: any) => {
  selectedTracking.value = JSON.parse(JSON.stringify(row))
  Object.assign(trackingForm, {
    registrationNo: row.registrationNo,
    materialCode: row.materialCode,
    materialName: row.materialName,
    batchNo: row.batchNo,
    quantity: row.quantity,
    defectType: row.defectType,
    defectDescription: row.defectDescription,
    reviewStatus: row.reviewStatus,
    disposalPlan: row.disposalPlan,
    disposalStatus: row.disposalStatus,
    processResult: row.processResult,
    trackingStatus: 'completed',
    tracker: row.tracker || 'admin',
    trackingTime: row.trackingTime,
    trackingResult: row.trackingResult || '',
    trackingDescription: row.trackingDescription || '',
    verifyResult: row.verifyResult || '',
    preventiveMeasures: row.preventiveMeasures || ''
  })
  trackingDialogTitle.value = '完成追踪'
  trackingDialogVisible.value = true
}

// 批量确认
const handleBatchConfirm = () => {
  if (selectedTrackings.value.length === 0) {
    ElMessage.warning('请选择要确认的追踪记录')
    return
  }
  Promise.all(selectedTrackings.value.map(t => NCTrackingAPI.completeTracking(t.id, { effectiveness: 'effective' })))
    .then(() => {
      ElMessage.success(`已批量确认 ${selectedTrackings.value.length} 条追踪记录`)
      selectedTrackings.value = []
      handleQuery()
    })
    .catch((e: any) => {
      ElMessage.error(e?.message || '批量确认失败')
    })
}

// 保存追踪
const handleSaveTracking = () => {
  if (!trackingFormRef.value) return
  
  trackingFormRef.value.validate((valid: boolean) => {
    if (valid) {
      const targetId = selectedTracking.value?.id
      if (!targetId) {
        ElMessage.error('未找到追踪记录ID')
        return
      }

      NCTrackingAPI.getNcTrackingById(targetId)
        .then(async res => {
          const entity = unwrapResponseData<any>(res) || {}
          const effectiveness =
            String(trackingForm.verifyResult || '').includes('有效') || String(trackingForm.verifyResult || '').includes('通过')
              ? 'effective'
              : 'ineffective'
          const merged: any = {
            ...entity,
            trackingContent: trackingForm.trackingDescription,
            trackingStatus: trackingForm.trackingStatus,
            tracker: trackingForm.tracker,
            improvementSuggestions: trackingForm.preventiveMeasures,
            effectiveness
          }

          await NCTrackingAPI.updateNcTracking(targetId, merged)
          if (trackingForm.trackingStatus === 'completed') {
            await NCTrackingAPI.completeTracking(targetId, {
              effectiveness,
              improvementSuggestions: trackingForm.preventiveMeasures
            } as any)
          }

          trackingDialogVisible.value = false
          ElMessage.success('追踪成功')
          handleQuery()
        })
        .catch((e: any) => {
          ElMessage.error(e?.message || '保存追踪失败')
        })
    }
  })
}

// 关闭详情对话框
const handleDetailDialogClose = () => {
  selectedTracking.value = null
}

// 关闭追踪对话框
const handleTrackingDialogClose = () => {
  if (trackingFormRef.value) {
    trackingFormRef.value.resetFields()
  }
}

// 导出追踪记录
const handleExport = () => {
  ElMessage.warning('导出功能正在开发中')
}
</script>

<style scoped>
.nc-tracking-view {
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

.tracking-detail {
  margin-bottom: 20px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .nc-tracking-view {
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
  .nc-tracking-view {
    padding: 12px;
  }
  
  .header-actions {
    flex-direction: column;
  }
}
</style>
