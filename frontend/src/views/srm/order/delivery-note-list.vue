<template>
  <div class="delivery-note-list">
    <h3>发货单列表</h3>
    
    <!-- 查询条件 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" inline>
        <el-form-item label="发货单号">
          <el-input v-model="searchForm.deliveryNo" placeholder="请输入发货单号" />
        </el-form-item>
        <el-form-item label="采购订单号">
          <el-input v-model="searchForm.poNo" placeholder="请输入采购订单号" />
        </el-form-item>
        <el-form-item label="供应商名称">
          <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态">
            <el-option label="全部" value="" />
            <el-option label="待处理" value="PENDING" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="已接收" value="RECEIVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="handleAdd">新增发货单</el-button>
      <el-button @click="handleImport">导入</el-button>
      <el-button @click="handleExport">导出</el-button>
    </div>
    
    <!-- 发货单列表 -->
    <el-card class="list-card" shadow="hover">
      <el-table :data="deliveryNoteList" stripe style="width: 100%" v-loading="loading">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="deliveryNo" label="发货单号" min-width="150" />
        <el-table-column prop="poNo" label="采购订单号" min-width="150" />
        <el-table-column prop="supplierName" label="供应商名称" min-width="150" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deliveryDate" label="发货日期" width="150" />
        <el-table-column prop="totalAmount" label="发货金额" width="120" formatter="(row) => `¥${row.totalAmount?.toFixed(2) || '0.00'}`" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleDetail(scope.row)">查看详情</el-button>
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="success" size="small" @click="handleConfirm(scope.row)">
              确认收货
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
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { procurementApi, supplierApi } from '@/api/srm'
import type { DeliveryNote } from '@/types/srm'

// 路由实例
const router = useRouter()

// 搜索表单
const searchForm = reactive({
  deliveryNo: '',
  poNo: '',
  supplierName: '',
  status: ''
})

// 发货单列表数据
const deliveryNoteList = ref<DeliveryNote[]>([])

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载状态
const loading = ref(false)

/**
 * 根据状态获取标签类型
 * @param status 发货单状态
 * @returns 标签类型
 */
const getStatusTagType = (status: string) => {
  switch (status) {
    case 'PENDING':
      return 'info'
    case 'SHIPPED':
      return 'warning'
    case 'RECEIVED':
      return 'success'
    case 'CLOSED':
      return 'danger'
    default:
      return ''
  }
}

/**
 * 查询发货单列表
 */
const handleSearch = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.currentPage - 1,
      size: pagination.pageSize
    }
    // 使用 supplierApi 中定义的发货单接口
    const response = await supplierApi.getDeliveryNoteList(params)
    deliveryNoteList.value = response.data.list || []
    pagination.total = response.data.total || 0
    ElMessage.success('查询成功')
  } catch (error) {
    console.error('获取发货单列表失败:', error)
    ElMessage.error('获取发货单列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 重置搜索条件
 */
const handleReset = () => {
  Object.assign(searchForm, {
    deliveryNo: '',
    poNo: '',
    supplierName: '',
    status: ''
  })
  handleSearch()
}

/**
 * 分页大小变化处理
 * @param size 每页条数
 */
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  handleSearch()
}

/**
 * 当前页码变化处理
 * @param current 当前页码
 */
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  handleSearch()
}

/**
 * 新增发货单
 */
const handleAdd = () => {
  ElMessage.info('新增发货单功能开发中')
}

/**
 * 编辑发货单
 * @param row 发货单数据
 */
const handleEdit = (row: DeliveryNote) => {
  ElMessage.info('编辑发货单功能开发中')
}

/**
 * 查看发货单详情
 * @param row 发货单数据
 */
const handleDetail = (row: DeliveryNote) => {
  ElMessage.info('查看发货单详情功能开发中')
}

/**
 * 确认收货
 * @param row 发货单数据
 */
const handleConfirm = async (row: DeliveryNote) => {
  try {
    await ElMessageBox.confirm('确定要确认收到该发货单吗？', '确认收货', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    ElMessage.success('确认收货成功')
    handleSearch()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败')
    }
  }
}

/**
 * 导入发货单
 */
const handleImport = () => {
  ElMessage.info('导入功能开发中')
}

/**
 * 导出发货单
 */
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 组件挂载时获取数据
onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.delivery-note-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.action-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.list-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
