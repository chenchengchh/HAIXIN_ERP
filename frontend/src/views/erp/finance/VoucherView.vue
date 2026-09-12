<template>
  <div class="voucher-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><Tickets /></el-icon>
          <span>凭证管理</span>
        </div>
      </template>

      <TableComponent
        :data="voucherList"
        :columns="voucherColumns"
        :total="voucherTotal"
        :loading="voucherLoading"
        :show-index="true"
        :show-action="true"
        action-slot-name="actions"
        :table-actions="tableActions"
        :filters="voucherFilters"
        :show-filter="true"
        @search="handleSearch"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <template #status="{ row }">
          <el-tag :type="getVoucherStatusType(row.status)" size="small">
            {{ getVoucherStatusLabel(row.status) }}
          </el-tag>
        </template>
        <!-- 操作列：按凭证状态条件渲染，审批操作统一在OA审批中心进行 -->
        <template #actions="{ row }">
          <el-button type="primary" size="small" link @click="handleView(row)">
            <el-icon><View /></el-icon>查看
          </el-button>
          <!-- 草稿/已拒绝：可编辑、提交审批、删除 -->
          <template v-if="row.status === 'draft' || row.status === 'rejected'">
            <el-button type="primary" size="small" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="primary" size="small" link @click="handleSubmitApproval(row)">
              <el-icon><UploadFilled /></el-icon>提交审批
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
          <!-- 审批中（已提交OA）：提示前往OA审批中心 -->
          <el-tooltip
            v-else-if="row.status === 'submitted'"
            content="该凭证已在OA审批中心流转，请前往 OA > 审批中心 处理"
            placement="top"
          >
            <el-tag type="warning" size="small" class="approving-tag">
              <el-icon><Loading /></el-icon>OA审批中
            </el-tag>
          </el-tooltip>
          <!-- 已审核：可过账 -->
          <el-button
            v-else-if="row.status === 'approved'"
            type="warning"
            size="small"
            link
            @click="handleVoucherAction('post', row)"
          >
            <el-icon><Check /></el-icon>过账
          </el-button>
        </template>
      </TableComponent>
    </el-card>

    <DialogComponent
      v-model="editVisible"
      :title="editMode === 'create' ? '新增凭证' : '编辑凭证'"
      width="720px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="保存"
      :show-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      @confirm="handleSave"
      @cancel="handleCancel"
      @close="handleCancel"
    >
      <el-form ref="editFormRef" :model="editForm" label-width="120px">
        <el-form-item
          label="凭证日期"
          prop="voucher_date"
          :rules="[{ required: true, message: '请选择凭证日期', trigger: 'change' }]"
        >
          <el-date-picker v-model="editForm.voucher_date" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item
          label="凭证类型"
          prop="voucher_type"
          :rules="[{ required: true, message: '请选择凭证类型', trigger: 'change' }]"
        >
          <el-select v-model="editForm.voucher_type" placeholder="请选择" style="width: 100%">
            <el-option label="记账凭证" value="journal" />
            <el-option label="收款凭证" value="receipt" />
            <el-option label="付款凭证" value="payment" />
            <el-option label="转账凭证" value="transfer" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input v-model="editForm.summary" placeholder="请输入摘要" />
        </el-form-item>
      </el-form>
    </DialogComponent>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Tickets, Plus, RefreshLeft, View, Edit, Delete, Check, UploadFilled, Loading } from '@element-plus/icons-vue'
import { TableComponent, DialogComponent } from '../../../components/base'
import { unwrapPageResponse } from '../../../api'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import { useAuthStore } from '../../../stores/auth'
import type { Voucher } from '../../../types/erp/finance'

const authStore = useAuthStore()

const voucherList = ref<Voucher[]>([])
const voucherTotal = ref(0)
const voucherLoading = ref(false)
const voucherPage = ref(1)
const voucherSize = ref(10)

const voucherFilters = [
  { prop: 'voucher_no', label: '凭证编号', type: 'input' as const, placeholder: '请输入凭证编号' },
  { prop: 'voucher_type', label: '凭证类型', type: 'select' as const, options: [
    { label: '记账凭证', value: 'journal' },
    { label: '收款凭证', value: 'receipt' },
    { label: '付款凭证', value: 'payment' },
    { label: '转账凭证', value: 'transfer' }
  ]},
  { prop: 'status', label: '状态', type: 'select' as const, options: [
    { label: '草稿', value: 'draft' },
    { label: '已提交', value: 'submitted' },
    { label: '已审核', value: 'approved' },
    { label: '已过账', value: 'posted' },
    { label: '已拒绝', value: 'rejected' }
  ]},
  { prop: 'date_range', label: '日期范围', type: 'daterange' as const }
] as any

const voucherColumns = [
  { prop: 'voucher_no', label: '凭证编号', width: 180 },
  { prop: 'voucher_date', label: '凭证日期', width: 140 },
  { prop: 'voucher_type', label: '凭证类型', width: 120 },
  { prop: 'debit_total', label: '借方合计', width: 120, align: 'right' },
  { prop: 'credit_total', label: '贷方合计', width: 120, align: 'right' },
  { prop: 'status', label: '状态', width: 120, slotName: 'status' },
  { prop: 'summary', label: '摘要', minWidth: 200 },
  { prop: 'create_time', label: '创建时间', width: 180 }
]

const tableActions = [
  {
    key: 'create',
    text: '新增',
    type: 'primary',
    icon: Plus,
    handler: () => {
      editMode.value = 'create'
      editForm.value = {
        voucher_date: new Date(),
        voucher_type: 'journal',
        summary: '',
        status: 'draft'
      }
      editVisible.value = true
    }
  },
  {
    key: 'refresh',
    text: '刷新',
    icon: RefreshLeft,
    handler: () => handleSearch()
  }
]

/**
 * 查看凭证详情
 * @param row 凭证行数据
 */
const handleView = (row: any) => {
  ElMessageBox.alert(`<pre>${JSON.stringify(row, null, 2)}</pre>`, '凭证详情', { dangerouslyUseHTMLString: true })
}

/**
 * 打开凭证编辑对话框
 * @param row 凭证行数据
 */
const handleEdit = (row: any) => {
  editMode.value = 'edit'
  editForm.value = {
    id: row.id,
    voucher_no: row.voucher_no,
    voucher_date: row.voucher_date,
    voucher_type: row.voucher_type,
    summary: row.summary,
    status: row.status
  }
  editVisible.value = true
}

/**
 * 提交凭证审批（走OA统一审批流）。
 * <p>提交后凭证状态变为submitted（审批中），审批操作在OA审批中心进行，
 * 审批完成后OA回调自动更新凭证状态为approved/rejected。</p>
 * @param row 凭证行数据
 */
const handleSubmitApproval = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确认提交凭证【${row.voucher_no}】到OA审批中心？审批通过前凭证将锁定为"审批中"状态。`,
      '提交审批',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    const userInfo = authStore.userInfo as any
    await erpApi.finance.submitVoucher(
      Number(row.id),
      Number(userInfo?.id) || undefined,
      userInfo?.name || userInfo?.username || undefined
    )
    ElMessage.success('已提交OA审批，请前往 OA > 审批中心 查看进度')
    handleSearch()
  } catch (error) {
    if (String(error).includes('cancel')) return
    ErrorHandler.handleApiError(error)
  }
}

const editVisible = ref(false)
const editMode = ref<'create' | 'edit'>('create')
const editFormRef = ref<any>(null)
const editForm = ref<Partial<Voucher>>({
  voucher_date: new Date(),
  voucher_type: 'journal',
  summary: '',
  status: 'draft'
})

const normalizeParams = (params: any) => {
  const out = { ...params }
  const range = out.date_range
  if (Array.isArray(range) && range.length === 2) {
    out.start_date = formatDate(range[0])
    out.end_date = formatDate(range[1])
    delete out.date_range
  }
  return out
}

const formatDate = (v: any) => {
  if (!v) return ''
  if (typeof v === 'string') return v
  try {
    return v.toISOString().slice(0, 19).replace('T', ' ')
  } catch {
    return ''
  }
}

const getVoucherStatusType = (status: string) => {
  const map: Record<string, string> = {
    draft: 'info',
    submitted: 'warning',
    approved: 'success',
    posted: 'success',
    rejected: 'danger'
  }
  return map[status] || 'info'
}

/**
 * 凭证状态中文标签
 * @param status 状态码
 * @returns 中文标签
 */
const getVoucherStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    draft: '草稿',
    submitted: '审批中',
    approved: '已审核',
    posted: '已过账',
    rejected: '已拒绝'
  }
  return map[status] || status
}

const getVoucherList = async (params: any) => {
  try {
    voucherLoading.value = true
    const result = await erpApi.finance.getVouchers(normalizeParams({
      page: params.page || voucherPage.value,
      size: params.size || voucherSize.value,
      ...params
    }))
    const page = unwrapPageResponse<Voucher>(result)
    voucherList.value = page.list
    voucherTotal.value = page.total || page.list.length || 0
    voucherPage.value = params.page || voucherPage.value
    voucherSize.value = params.size || voucherSize.value
  } catch (error) {
    ErrorHandler.handleApiError(error)
    voucherList.value = []
    voucherTotal.value = 0
  } finally {
    voucherLoading.value = false
  }
}

const handleSearch = (params: any = {}) => {
  getVoucherList(params)
}

const handleSizeChange = (size: number) => {
  voucherSize.value = size
  getVoucherList({ page: voucherPage.value, size })
}

const handleCurrentChange = (page: number) => {
  voucherPage.value = page
  getVoucherList({ page, size: voucherSize.value })
}

const handleCancel = () => {
  editVisible.value = false
}

const handleSave = async () => {
  if (!editFormRef.value) return
  try {
    await editFormRef.value.validate()
    if (editMode.value === 'create') {
      // 新增凭证初始状态固定为草稿，状态由审批流驱动
      await erpApi.finance.createVoucher({
        voucher_date: formatDate(editForm.value.voucher_date),
        voucher_type: editForm.value.voucher_type,
        summary: editForm.value.summary,
        status: 'draft'
      })
      ElMessage.success('创建成功')
    } else {
      // 编辑不传status，避免绕过OA审批流直接修改凭证状态
      const id = Number(editForm.value.id)
      await erpApi.finance.updateVoucher(id, {
        voucher_date: formatDate(editForm.value.voucher_date),
        voucher_type: editForm.value.voucher_type,
        summary: editForm.value.summary
      })
      ElMessage.success('更新成功')
    }
    editVisible.value = false
    handleSearch()
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该凭证？此操作不可恢复。', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await erpApi.finance.deleteVoucher(Number(row.id))
    ElMessage.success('删除成功')
    handleSearch()
  } catch (error) {
    if (String(error).includes('cancel')) return
    ErrorHandler.handleApiError(error)
  }
}

const handleVoucherAction = async (action: 'post', row: any) => {
  try {
    const map: Record<'post', { title: string; fn: (id: number) => Promise<any> }> = {
      post: { title: '确认过账该凭证？', fn: (id: number) => erpApi.finance.postVoucher(id) }
    }
    const config = map[action]
    await ElMessageBox.confirm(config.title, '操作确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await config.fn(Number(row.id))
    ElMessage.success('操作成功')
    handleSearch()
  } catch (error) {
    if (String(error).includes('cancel')) return
    ErrorHandler.handleApiError(error)
  }
}

handleSearch()
</script>

<style scoped lang="scss">
.voucher-view {
  padding: 20px;
  .module-card {
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    .card-header {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 20px;
      font-weight: bold;
      color: #1890ff;
    }
  }
  /* OA审批中标签样式：与操作按钮对齐 */
  .approving-tag {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    cursor: default;
  }
}
</style>
