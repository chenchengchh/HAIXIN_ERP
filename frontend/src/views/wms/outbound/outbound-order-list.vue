<template>
  <div class="outbound-order-list">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :model="searchForm" inline>
        <el-form-item label="出库单号">
          <el-input v-model="searchForm.outboundNo" placeholder="请输入出库单号" clearable />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="新建" value="CREATED" />
            <el-option label="已审核" value="APPROVED" />
            <el-option label="已分配波次" value="WAVED" />
            <el-option label="拣货中" value="PICKING" />
            <el-option label="已装箱" value="PACKED" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="handleCreate">新增出库单</el-button>
    </div>

    <!-- 列表 -->
    <el-table :data="orderList" v-loading="loading" stripe style="width: 100%; margin-top: 20px;">
      <el-table-column prop="outboundNo" label="出库单号" min-width="150" />
      <el-table-column prop="sourceNo" label="来源单号" width="150" />
      <el-table-column prop="customerName" label="客户名称" min-width="150" />
      <el-table-column prop="orderType" label="类型" width="100">
        <template #default="scope">
          {{ formatOrderType(scope.row.orderType) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleView(scope.row)">查看</el-button>
          <el-button v-if="scope.row.status === 'CREATED'" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button v-if="scope.row.status === 'CREATED'" link type="success" @click="handleApprove(scope.row)">审核</el-button>
          <el-button v-if="['CREATED', 'APPROVED'].includes(scope.row.status)" link type="danger" @click="handleCancel(scope.row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="handleSearch"
      />
    </div>

    <!-- 详情/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form :model="formData" label-width="100px" :disabled="dialogType === 'view'">
        <el-row>
          <el-col :span="12">
            <el-form-item label="出库单号">
              <el-input v-model="formData.outboundNo" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源单号">
              <el-input v-model="formData.sourceNo" placeholder="请输入来源单号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户名称">
              <el-input v-model="formData.customerName" placeholder="请输入客户名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="订单类型">
              <el-select v-model="formData.orderType" placeholder="请选择类型">
                <el-option label="销售出库" value="SALES" />
                <el-option label="调拨出库" value="TRANSFER" />
                <el-option label="领料出库" value="MATERIAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" />
        </el-form-item>

        <!-- 物料明细 -->
        <div class="items-section">
          <div class="section-header">
            <span>物料明细</span>
            <el-button v-if="dialogType !== 'view'" type="primary" link @click="handleAddItem">添加物料</el-button>
          </div>
          <el-table :data="formData.items" border style="width: 100%">
            <el-table-column label="物料编码" width="150">
              <template #default="scope">
                <el-input v-if="dialogType !== 'view'" v-model="scope.row.materialCode" placeholder="编码" />
                <span v-else>{{ scope.row.materialCode }}</span>
              </template>
            </el-table-column>
            <el-table-column label="物料名称" min-width="150">
              <template #default="scope">
                <el-input v-if="dialogType !== 'view'" v-model="scope.row.materialName" placeholder="名称" />
                <span v-else>{{ scope.row.materialName }}</span>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="120">
              <template #default="scope">
                <el-input-number v-if="dialogType !== 'view'" v-model="scope.row.planQuantity" :min="1" />
                <span v-else>{{ scope.row.planQuantity }}</span>
              </template>
            </el-table-column>
            <el-table-column label="单位" width="100">
              <template #default="scope">
                <el-input v-if="dialogType !== 'view'" v-model="scope.row.unit" placeholder="单位" />
                <span v-else>{{ scope.row.unit }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="dialogType !== 'view'" label="操作" width="80">
              <template #default="scope">
                <el-button type="danger" link @click="handleDeleteItem(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="dialogType !== 'view'" type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { outboundOrderApi } from '../../../api/wms'

const loading = ref(false)
const orderList = ref<any[]>([])
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const searchForm = reactive({
  outboundNo: '',
  customerName: '',
  status: ''
})

const dialogVisible = ref(false)
const dialogType = ref<'create' | 'edit' | 'view'>('create')
const dialogTitle = computed(() => {
  if (dialogType.value === 'create') return '新增出库单'
  if (dialogType.value === 'edit') return '编辑出库单'
  return '查看出库单'
})

const formData = reactive({
  id: undefined,
  outboundNo: '',
  sourceNo: '',
  customerName: '',
  orderType: 'SALES',
  remark: '',
  items: [] as any[]
})

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      ...searchForm
    }
    const res = await outboundOrderApi.getList(params)
    // 后端统一返回PageResult（list/total），兼容旧Spring Page格式（content/totalElements）
    const pageData = (res as any).data || {}
    orderList.value = pageData.list || pageData.content || []
    pagination.total = Number(pageData.total ?? pageData.totalElements ?? 0)
  } catch (error) {
    console.error(error)
    ElMessage.error('获取出库单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchOrders()
}

const handleReset = () => {
  searchForm.outboundNo = ''
  searchForm.customerName = ''
  searchForm.status = ''
  handleSearch()
}

const handleCreate = () => {
  dialogType.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const handleView = async (row: any) => {
  dialogType.value = 'view'
  await loadDetail(row.id)
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  dialogType.value = 'edit'
  await loadDetail(row.id)
  dialogVisible.value = true
}

const loadDetail = async (id: number) => {
  try {
    const res = await outboundOrderApi.getDetail(id)
    const data = (res as any).data || {}
    // 后端实体字段（orderNo/type/items[].quantity）映射到表单字段（outboundNo/orderType/items[].planQuantity）
    formData.id = data.id
    formData.outboundNo = data.orderNo || data.outboundNo || ''
    formData.sourceNo = data.sourceNo || ''
    formData.customerName = data.customerName || ''
    formData.orderType = data.type === 'PRODUCTION' ? 'MATERIAL' : (data.type || 'SALES')
    formData.remark = data.remark || ''
    formData.items = (data.items || []).map((item: any) => ({
      materialCode: item.materialCode || '',
      materialName: item.materialName || '',
      planQuantity: item.quantity ?? item.planQuantity ?? 1,
      unit: item.unit || '个'
    }))
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

const handleSubmit = async () => {
  try {
    // 前端类型MATERIAL映射为后端存储约定PRODUCTION（fromFrontOrderType/toFrontOrderType一致）
    const payload = {
      ...formData,
      orderType: formData.orderType === 'MATERIAL' ? 'PRODUCTION' : formData.orderType
    }
    if (formData.id) {
      await outboundOrderApi.update(formData.id, payload)
      ElMessage.success('更新成功')
    } else {
      await outboundOrderApi.create(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchOrders()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleApprove = (row: any) => {
  ElMessageBox.confirm('确定审核通过该出库单吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await outboundOrderApi.approve(row.id)
      ElMessage.success('审核成功')
      fetchOrders()
    } catch (error) {
      ElMessage.error('审核失败')
    }
  })
}

const handleCancel = (row: any) => {
  ElMessageBox.confirm('确定取消该出库单吗？', '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      await outboundOrderApi.cancel(row.id)
      ElMessage.success('取消成功')
      fetchOrders()
    } catch (error) {
      ElMessage.error('取消失败')
    }
  })
}

const handleAddItem = () => {
  formData.items.push({
    materialCode: '',
    materialName: '',
    planQuantity: 1,
    unit: '个'
  })
}

const handleDeleteItem = (index: number) => {
  formData.items.splice(index, 1)
}

const resetForm = () => {
  formData.id = undefined
  formData.outboundNo = ''
  formData.sourceNo = ''
  formData.customerName = ''
  formData.orderType = 'SALES'
  formData.remark = ''
  formData.items = []
  handleAddItem()
}

const formatOrderType = (type: string) => {
  // 后端列表返回小写front类型（sales/material/transfer），同时兼容大写存储值
  const map: Record<string, string> = {
    'sales': '销售出库', 'SALES': '销售出库',
    'transfer': '调拨出库', 'TRANSFER': '调拨出库',
    'material': '领料出库', 'MATERIAL': '领料出库', 'PRODUCTION': '领料出库'
  }
  return map[type] || type
}

const formatStatus = (status: string) => {
  const map: Record<string, string> = {
    'CREATED': '新建',
    'APPROVED': '已审核',
    'WAVED': '已分配波次',
    'WAVE_ASSIGNED': '已分配波次',
    'PICKING': '拣货中',
    'PACKED': '已装箱',
    'SHIPPED': '已发货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    'CREATED': 'info',
    'APPROVED': 'primary',
    'WAVED': 'warning',
    'WAVE_ASSIGNED': 'warning',
    'PICKING': 'warning',
    'PACKED': 'warning',
    'SHIPPED': 'success',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return map[status] || 'info'
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped lang="scss">
.outbound-order-list {
  padding: 20px;
  background-color: #fff;
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .items-section {
    margin-top: 20px;
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
      font-weight: bold;
    }
  }
}
</style>
