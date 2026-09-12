<template>
  <div class="reconciliation-view">
    <div class="content-header">
      <h3>对账结算协同</h3>
      <div class="action-buttons">
        <el-button type="primary" @click="handleCreate">新增对账</el-button>
        <el-button @click="handleRefresh">刷新数据</el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-card shadow="never">
        <el-form :model="searchForm" layout="inline" size="small">
          <el-form-item label="对账单号">
            <el-input v-model="searchForm.reconciliationNo" placeholder="请输入对账单号" />
          </el-form-item>
          <el-form-item label="供应商名称">
            <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" />
          </el-form-item>
          <el-form-item label="对账状态">
            <el-select v-model="searchForm.status" placeholder="请选择对账状态">
              <el-option label="全部" value="" />
              <el-option label="待对账" value="PENDING" />
              <el-option label="对账中" value="PROCESSING" />
              <el-option label="已确认" value="CONFIRMED" />
              <el-option label="已结算" value="SETTLED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="开始日期">
            <el-date-picker
              v-model="searchForm.startDate"
              type="date"
              placeholder="选择开始日期"
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker
              v-model="searchForm.endDate"
              type="date"
              placeholder="选择结束日期"
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    
    <!-- 对账列表 -->
    <div class="table-area">
      <el-card shadow="never">
        <el-table v-loading="loading" :data="reconciliationList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="reconciliationNo" label="对账单号" width="180" />
          <el-table-column prop="supplierName" label="供应商名称" width="220" />
          <el-table-column prop="reconciliationPeriod" label="对账周期" width="180" />
          <el-table-column prop="totalAmount" label="对账金额" width="150" />
          <el-table-column prop="startDate" label="开始日期" width="150" />
          <el-table-column prop="endDate" label="结束日期" width="150" />
          <el-table-column prop="status" label="对账状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="confirmStatus" label="确认状态" width="120">
            <template #default="scope">
              <el-tag :type="scope.row.confirmStatus === 'CONFIRMED' ? 'success' : 'warning'">
                {{ scope.row.confirmStatus }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="handleView(scope.row)">查看</el-button>
              <el-button size="small" type="primary" @click="handleEdit(scope.row)" :disabled="scope.row.status === 'SETTLED' || scope.row.status === 'CANCELLED'">编辑</el-button>
              <el-button size="small" type="success" @click="handleConfirm(scope.row)" :disabled="scope.row.confirmStatus === 'CONFIRMED'">确认</el-button>
              <el-button size="small" type="warning" @click="handleSettle(scope.row)" :disabled="scope.row.status === 'SETTLED' || scope.row.status === 'CANCELLED'">结算</el-button>
              <el-button size="small" type="danger" @click="handleCancel(scope.row)" :disabled="scope.row.status === 'SETTLED' || scope.row.status === 'CANCELLED'">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
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
    </div>
    
    <!-- 对账表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="70%"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="对账单号" prop="reconciliationNo">
          <el-input v-model="formData.reconciliationNo" placeholder="请输入对账单号" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="formData.supplierId" placeholder="请选择供应商" filterable>
            <el-option v-for="s in supplierOptions" :key="s.id" :label="s.supplierName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="对账周期" prop="reconciliationPeriod">
          <el-input v-model="formData.reconciliationPeriod" placeholder="请输入对账周期，如：202501" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="formData.startDate"
            type="date"
            placeholder="请选择开始日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="formData.endDate"
            type="date"
            placeholder="请选择结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="对账金额" prop="totalAmount">
          <el-input-number v-model="formData.totalAmount" :min="0" :step="0.01" placeholder="请输入对账金额" style="width: 100%" />
        </el-form-item>
        <el-form-item label="对账状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择对账状态">
            <el-option label="待对账" value="PENDING" />
            <el-option label="对账中" value="PROCESSING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已结算" value="SETTLED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="确认状态" prop="confirmStatus">
          <el-select v-model="formData.confirmStatus" placeholder="请选择确认状态">
            <el-option label="待确认" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reconciliationApi, supplierApi } from '../../../api/srm'

// 搜索表单
const searchForm = reactive({
  reconciliationNo: '',
  supplierName: '',
  status: '',
  startDate: '',
  endDate: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载状态
const loading = ref(false)

// 对账列表
const reconciliationList = ref<any[]>([])
const supplierOptions = ref<any[]>([])

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增对账')
const formRef = ref()
const formData = reactive({
  id: undefined,
  reconciliationNo: '',
  supplierId: undefined as number | undefined,
  supplierName: '',
  reconciliationPeriod: '',
  totalAmount: 0,
  startDate: new Date(),
  endDate: new Date(),
  status: 'PENDING',
  confirmStatus: 'PENDING',
  remark: ''
})

// 表单验证规则
const rules = reactive({
  reconciliationNo: [{ required: true, message: '请输入对账单号', trigger: 'blur' }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  reconciliationPeriod: [{ required: true, message: '请输入对账周期', trigger: 'blur' }],
  totalAmount: [{ required: true, message: '请输入对账金额', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择对账状态', trigger: 'change' }],
  confirmStatus: [{ required: true, message: '请选择确认状态', trigger: 'change' }]
})

// 页面加载时获取数据
onMounted(() => {
  fetchSuppliers()
  fetchReconciliations()
})

const fetchSuppliers = async () => {
  try {
    const res = await supplierApi.getSupplierList({ page: 0, size: 1000 })
    supplierOptions.value = res.data.list || []
  } catch (e) {
    supplierOptions.value = []
  }
}

const toYmd = (value: any) => {
  if (!value) return value
  if (value instanceof Date) return value.toISOString().slice(0, 10)
  if (typeof value === 'string') return value.slice(0, 10)
  return value
}

// 获取对账列表
const fetchReconciliations = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      reconciliationNo: searchForm.reconciliationNo || undefined,
      supplierName: searchForm.supplierName || undefined,
      status: searchForm.status || undefined,
      startDate: searchForm.startDate ? toYmd(searchForm.startDate) : undefined,
      endDate: searchForm.endDate ? toYmd(searchForm.endDate) : undefined
    }
    const res = await reconciliationApi.getReconciliationList(params)
    const content = (res.data.list || res.data.records || [])
    reconciliationList.value = content.map((x: any) => ({
      ...x,
      createTime: x.createdTime || x.createTime || ''
    }))
    pagination.total = (res.data.total || 0)
  } catch (error) {
    ElMessage.error('获取对账列表失败')
    reconciliationList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 根据状态获取标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'PROCESSING':
      return 'primary'
    case 'CONFIRMED':
      return 'success'
    case 'SETTLED':
      return 'info'
    case 'CANCELLED':
      return 'danger'
    default:
      return 'info'
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchReconciliations()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    reconciliationNo: '',
    supplierName: '',
    status: '',
    startDate: '',
    endDate: ''
  })
  pagination.currentPage = 1
  fetchReconciliations()
}

// 刷新数据
const handleRefresh = () => {
  fetchReconciliations()
  ElMessage.success('数据已刷新')
}

// 新增
const handleCreate = () => {
  dialogTitle.value = '新增对账'
  Object.assign(formData, {
    id: undefined,
    reconciliationNo: '',
    supplierId: undefined,
    supplierName: '',
    reconciliationPeriod: '',
    totalAmount: 0,
    startDate: new Date(),
    endDate: new Date(),
    status: 'PENDING',
    confirmStatus: 'PENDING',
    remark: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑对账'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 查看
const handleView = (row: any) => {
  dialogTitle.value = '查看对账'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 确认
const handleConfirm = (row: any) => {
  ElMessageBox.confirm('确定要确认该对账记录吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await reconciliationApi.confirmReconciliation(row.id)
      ElMessage.success('确认成功')
      fetchReconciliations()
    } catch (e) {
      ElMessage.error('确认失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 结算
const handleSettle = (row: any) => {
  ElMessageBox.confirm('确定要结算该对账记录吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await reconciliationApi.settleReconciliation(row.id)
      ElMessage.success('结算成功')
      fetchReconciliations()
    } catch (e) {
      ElMessage.error('结算失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 取消
const handleCancel = (row: any) => {
  ElMessageBox.confirm('确定要取消该对账记录吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await reconciliationApi.cancelReconciliation(row.id)
      ElMessage.success('取消成功')
      fetchReconciliations()
    } catch (e) {
      ElMessage.error('取消失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    const supplier = supplierOptions.value.find((s: any) => s.id === formData.supplierId)
    const payload: any = {
      reconciliationNo: formData.reconciliationNo,
      supplierId: formData.supplierId,
      supplierName: supplier?.supplierName || formData.supplierName,
      reconciliationPeriod: formData.reconciliationPeriod,
      totalAmount: formData.totalAmount,
      startDate: toYmd(formData.startDate),
      endDate: toYmd(formData.endDate),
      status: formData.status,
      confirmStatus: formData.confirmStatus,
      remark: formData.remark
    }
    if (formData.id) {
      await reconciliationApi.updateReconciliation(formData.id as any, payload)
    } else {
      await reconciliationApi.createReconciliation(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchReconciliations()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchReconciliations()
}

// 当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchReconciliations()
}
</script>

<style scoped>
.reconciliation-view {
  padding: 16px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.content-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.search-area {
  margin-bottom: 16px;
}

.table-area {
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.dialog-footer {
  text-align: right;
}
</style>
