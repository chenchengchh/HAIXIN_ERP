<template>
  <div class="material-forecast-view">
    <div class="content-header">
      <h3>来料预报</h3>
      <div class="action-buttons">
        <el-button type="primary" @click="handleCreate">新增预报</el-button>
        <el-button @click="handleRefresh">刷新数据</el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-card shadow="never">
        <el-form :model="searchForm" layout="inline" size="small">
          <el-form-item label="预报单号">
            <el-input v-model="searchForm.forecastNo" placeholder="请输入预报单号" />
          </el-form-item>
          <el-form-item label="供应商名称">
            <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" />
          </el-form-item>
          <el-form-item label="订单编号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" />
          </el-form-item>
          <el-form-item label="预报状态">
            <el-select v-model="searchForm.status" placeholder="请选择预报状态">
              <el-option label="全部" value="" />
              <el-option label="待确认" value="PENDING" />
              <el-option label="已确认" value="CONFIRMED" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    
    <!-- 来料预报列表 -->
    <div class="table-area">
      <el-card shadow="never">
        <el-table v-loading="loading" :data="forecastList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="forecastNo" label="预报单号" width="180" />
          <el-table-column prop="supplierName" label="供应商名称" width="220" />
          <el-table-column prop="orderNo" label="订单编号" width="180" />
          <el-table-column prop="expectedArrivalDate" label="预计到货日期" width="180" />
          <el-table-column prop="actualArrivalDate" label="实际到货日期" width="180" />
          <el-table-column prop="totalQuantity" label="预报数量" width="120" />
          <el-table-column prop="status" label="预报状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="300" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="handleView(scope.row)">查看</el-button>
              <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="success" @click="handleConfirm(scope.row)" :disabled="scope.row.status !== 'PENDING'">
                确认预报
              </el-button>
              <el-button size="small" type="danger" @click="handleCancel(scope.row)" :disabled="scope.row.status === 'CANCELLED' || scope.row.status === 'COMPLETED'">
                取消预报
              </el-button>
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
    
    <!-- 来料预报表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="70%"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="预报单号" prop="forecastNo">
          <el-input v-model="formData.forecastNo" placeholder="请输入预报单号" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="formData.supplierId" placeholder="请选择供应商" filterable>
            <el-option v-for="s in supplierOptions" :key="s.id" :label="s.supplierName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联订单" prop="orderId">
          <el-select v-model="formData.orderId" placeholder="请选择关联订单" filterable>
            <el-option v-for="o in orderOptions" :key="o.id" :label="o.orderNo" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预计到货日期" prop="expectedArrivalDate">
          <el-date-picker
            v-model="formData.expectedArrivalDate"
            type="datetime"
            placeholder="请选择预计到货日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="预报状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择预报状态">
            <el-option label="待确认" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流信息" prop="logisticsInfo">
          <el-input
            v-model="formData.logisticsInfo"
            type="textarea"
            :rows="2"
            placeholder="请输入物流信息"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注信息"
          />
        </el-form-item>
        
        <el-divider>预报明细</el-divider>
        
        <el-table :data="formData.items" style="width: 100%">
          <el-table-column prop="materialName" label="材料名称" width="200">
            <template #default="scope">
              <el-input v-model="scope.row.materialName" placeholder="请输入材料名称" />
            </template>
          </el-table-column>
          <el-table-column prop="specification" label="规格型号" width="150">
            <template #default="scope">
              <el-input v-model="scope.row.specification" placeholder="请输入规格型号" />
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="预报数量" width="120">
            <template #default="scope">
              <el-input-number v-model="scope.row.quantity" :min="0" :step="1" placeholder="请输入数量" />
            </template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="80">
            <template #default="scope">
              <el-select v-model="scope.row.unit" placeholder="请选择单位">
                <el-option label="个" value="个" />
                <el-option label="件" value="件" />
                <el-option label="套" value="套" />
                <el-option label="kg" value="kg" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="expectedDeliveryDate" label="预计交付日期" width="180">
            <template #default="scope">
              <el-date-picker
                v-model="scope.row.expectedDeliveryDate"
                type="datetime"
                placeholder="请选择预计交付日期"
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button size="small" type="danger" @click="handleDeleteItem(scope.$index)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="action-buttons" style="margin-top: 16px;">
          <el-button type="primary" @click="handleAddItem">添加预报明细</el-button>
        </div>
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
import { materialForecastApi, supplierApi, procurementApi } from '../../../api/srm'

// 搜索表单
const searchForm = reactive({
  forecastNo: '',
  supplierName: '',
  orderNo: '',
  status: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载状态
const loading = ref(false)

// 预报列表
const forecastList = ref<any[]>([])

const supplierOptions = ref<any[]>([])
const orderOptions = ref<any[]>([])

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增来料预报')
const formRef = ref()
const formData = reactive({
  id: undefined,
  forecastNo: '',
  supplierId: undefined as number | undefined,
  supplierName: '',
  orderId: undefined as number | undefined,
  orderNo: '',
  expectedArrivalDate: new Date(),
  actualArrivalDate: null,
  totalQuantity: 0,
  status: 'PENDING',
  logisticsInfo: '',
  remark: '',
  items: [
    {
      materialName: '',
      specification: '',
      quantity: 0,
      unit: '个',
      expectedDeliveryDate: new Date()
    }
  ]
})

// 表单验证规则
const rules = reactive({
  forecastNo: [{ required: true, message: '请输入预报单号', trigger: 'blur' }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  orderId: [{ required: true, message: '请选择关联订单', trigger: 'change' }],
  expectedArrivalDate: [{ required: true, message: '请选择预计到货日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择预报状态', trigger: 'change' }]
})

// 页面加载时获取数据
onMounted(() => {
  fetchOptions()
  fetchForecastList()
})

const fetchOptions = async () => {
  try {
    const [supplierRes, orderRes] = await Promise.all([
      supplierApi.getSupplierList({ page: 0, size: 1000 }),
      procurementApi.getPurchaseOrderList({ page: 0, size: 1000 })
    ])
    supplierOptions.value = supplierRes.data.list || []
    orderOptions.value = orderRes.data.list || []
  } catch (e) {
    supplierOptions.value = []
    orderOptions.value = []
  }
}

const toIso = (value: any) => {
  if (!value) return value
  if (value instanceof Date) return value.toISOString()
  return value
}

// 获取预报列表
const fetchForecastList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      forecastNo: searchForm.forecastNo || undefined,
      supplierName: searchForm.supplierName || undefined,
      orderNo: searchForm.orderNo || undefined,
      status: searchForm.status || undefined
    }
    const res = await materialForecastApi.getForecastList(params)
    const content = (res.data.list || res.data.records || [])
    forecastList.value = content.map((x: any) => ({
      ...x,
      createTime: x.createdTime || x.createTime || ''
    }))
    pagination.total = (res.data.total || 0)
  } catch (error) {
    ElMessage.error('获取来料预报列表失败')
    forecastList.value = []
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
    case 'CONFIRMED':
      return 'primary'
    case 'COMPLETED':
      return 'success'
    case 'CANCELLED':
      return 'danger'
    default:
      return 'info'
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchForecastList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    forecastNo: '',
    supplierName: '',
    orderNo: '',
    status: ''
  })
  pagination.currentPage = 1
  fetchForecastList()
}

// 刷新数据
const handleRefresh = () => {
  fetchForecastList()
  ElMessage.success('数据已刷新')
}

// 新增
const handleCreate = () => {
  dialogTitle.value = '新增来料预报'
  Object.assign(formData, {
    id: undefined,
    forecastNo: '',
    supplierId: undefined,
    supplierName: '',
    orderId: undefined,
    orderNo: '',
    expectedArrivalDate: new Date(),
    actualArrivalDate: null,
    totalQuantity: 0,
    status: 'PENDING',
    logisticsInfo: '',
    remark: '',
    items: [
      {
        materialName: '',
        specification: '',
        quantity: 0,
        unit: '个',
        expectedDeliveryDate: new Date()
      }
    ]
  })
  dialogVisible.value = true
}

const openDetail = (row: any, title: string) => {
  dialogTitle.value = title
  materialForecastApi.getForecastDetail(row.id).then((res: any) => {
    const d = res.data || {}
    Object.assign(formData, {
      ...d,
      expectedArrivalDate: d.expectedArrivalDate ? new Date(d.expectedArrivalDate) : new Date(),
      actualArrivalDate: d.actualArrivalDate ? new Date(d.actualArrivalDate) : null,
      items: (d.items || []).map((it: any) => ({
        ...it,
        expectedDeliveryDate: it.expectedDeliveryDate ? new Date(it.expectedDeliveryDate) : new Date()
      }))
    })
    dialogVisible.value = true
  }).catch(() => {
    ElMessage.error('获取预报详情失败')
  })
}

// 编辑
const handleEdit = (row: any) => {
  openDetail(row, '编辑来料预报')
}

// 查看
const handleView = (row: any) => {
  openDetail(row, '查看来料预报')
}

// 确认预报
const handleConfirm = (row: any) => {
  ElMessageBox.confirm('确定要确认该来料预报吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await materialForecastApi.confirmForecast(row.id)
      ElMessage.success('确认预报成功')
      fetchForecastList()
    } catch (e) {
      ElMessage.error('确认预报失败')
    }
  }).catch(() => {
    // 取消确认
  })
}

// 取消预报
const handleCancel = (row: any) => {
  ElMessageBox.confirm('确定要取消该来料预报吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await materialForecastApi.cancelForecast(row.id)
      ElMessage.success('取消预报成功')
      fetchForecastList()
    } catch (e) {
      ElMessage.error('取消预报失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 添加预报明细
const handleAddItem = () => {
  formData.items.push({
    materialName: '',
    specification: '',
    quantity: 0,
    unit: '个',
    expectedDeliveryDate: new Date()
  })
}

// 删除预报明细
const handleDeleteItem = (index: number) => {
  formData.items.splice(index, 1)
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    // 计算总数量
    formData.totalQuantity = formData.items.reduce((total, item) => total + item.quantity, 0)

    const supplier = supplierOptions.value.find((s: any) => s.id === formData.supplierId)
    const order = orderOptions.value.find((o: any) => o.id === formData.orderId)

    const payload: any = {
      forecastNo: formData.forecastNo,
      supplierId: formData.supplierId,
      supplierName: supplier?.supplierName || formData.supplierName,
      orderId: formData.orderId,
      orderNo: order?.orderNo || formData.orderNo,
      expectedArrivalDate: toIso(formData.expectedArrivalDate),
      actualArrivalDate: toIso(formData.actualArrivalDate),
      totalQuantity: formData.totalQuantity,
      status: formData.status,
      logisticsInfo: formData.logisticsInfo,
      remark: formData.remark,
      items: (formData.items || []).map((it: any) => ({
        materialId: it.materialId,
        materialName: it.materialName,
        specification: it.specification,
        quantity: it.quantity,
        unit: it.unit,
        expectedDeliveryDate: toIso(it.expectedDeliveryDate)
      }))
    }
    if (formData.id) {
      await materialForecastApi.updateForecast(formData.id as any, payload)
    } else {
      await materialForecastApi.createForecast(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchForecastList()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchForecastList()
}

// 当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchForecastList()
}
</script>

<style scoped>
.material-forecast-view {
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
