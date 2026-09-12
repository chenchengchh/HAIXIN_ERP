<template>
  <div class="customer-list-view">
    <!-- 搜索和筛选区域 -->
    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="客户类型">
          <el-select v-model="searchForm.customerType" placeholder="请选择客户类型" clearable>
            <el-option label="企业" value="enterprise" />
            <el-option label="个人" value="individual" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户状态">
          <el-select v-model="searchForm.status" placeholder="请选择客户状态" clearable>
            <el-option label="潜在客户" value="potential" />
            <el-option label="活跃客户" value="active" />
            <el-option label="不活跃客户" value="inactive" />
            <el-option label="流失客户" value="lost" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="searchForm.ownerName" placeholder="请输入负责人姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 客户列表区域 -->
    <el-card shadow="hover" class="list-card">
      <template #header>
        <div class="card-header">
          <span>客户列表</span>
          <el-button type="primary" @click="handleAddCustomer">新增客户</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="customerList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="customerNo" label="客户编号" width="120" />
        <el-table-column prop="customerName" label="客户名称" min-width="150" />
        <el-table-column prop="customerType" label="客户类型" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.customerType === 'enterprise'" type="success">企业</el-tag>
            <el-tag v-else type="success">个人</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="industry" label="行业" width="120" />
        <el-table-column prop="level" label="客户级别" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.level === 'A'" type="danger">A</el-tag>
            <el-tag v-else-if="scope.row.level === 'B'" type="warning">B</el-tag>
            <el-tag v-else type="info">C</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag
              :type="{
                potential: 'info',
                active: 'success',
                inactive: 'warning',
                lost: 'danger'
              }[scope.row.status as keyof typeof statusTypeMap]"
            >
              {{ {
                potential: '潜在',
                active: '活跃',
                inactive: '不活跃',
                lost: '流失'
              }[scope.row.status as keyof typeof statusMap] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="负责人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleViewDetail(scope.row.id)">详情</el-button>
            <el-button size="small" @click="handleEditCustomer(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteCustomer(scope.row.id)">删除</el-button>
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

    <!-- 新增/编辑客户对话框 -->
    <CustomerDialog
      :visible="dialogVisible"
      :customer="currentCustomer"
      @close="handleDialogClose"
      @save="handleSaveCustomer"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'// 导入子组件
import CustomerDialog from './components/CustomerDialog.vue'
import { customerApi, type Customer } from '../../../api/crm/customer'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  customerName: '',
  customerType: '',
  status: '',
  ownerName: ''
})

// 状态类型映射
const statusTypeMap = {
  potential: 'info',
  active: 'success',
  inactive: 'warning',
  lost: 'danger'
}

// 状态映射
const statusMap = {
  potential: '潜在',
  active: '活跃',
  inactive: '不活跃',
  lost: '流失'
}

// 客户列表数据
const customerList = ref<Customer[]>([])
const loading = ref(false)

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const currentCustomer = ref<Partial<Customer>>({})

// 选中的客户
const selectedCustomers = ref<Customer[]>([])

// 初始化数据
onMounted(() => {
  fetchCustomerList()
})

// 获取客户列表
const fetchCustomerList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      customerName: searchForm.customerName,
      customerType: searchForm.customerType,
      status: searchForm.status,
      ownerName: searchForm.ownerName
    }
    console.log('请求参数:', params)
    // 确保API调用方式正确，直接传递params，而不是包含params和cacheTime的对象
    const response = await customerApi.getCustomerList(params)
    console.log('完整响应对象:', response)
    console.log('响应状态:', response.status)
    console.log('响应数据:', response.data)
    console.log('响应数据结构:', Object.keys(response.data))
    
    // 尝试多种数据访问方式，确保兼容性
    const customerData = response.data?.data || response.data
    console.log('客户数据:', customerData)
    console.log('客户数据结构:', Object.keys(customerData))
    
    // 支持多种数据格式
    const records = customerData?.records || customerData?.list || []
    const total = customerData?.total || 0
    
    console.log('最终客户列表:', records)
    console.log('最终客户总数:', total)
    
    customerList.value = records
    pagination.total = total
  } catch (error) {
    console.error('获取客户列表失败:', error)
    ElMessage.error('获取客户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchCustomerList()
}

// 重置
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    ;(searchForm as any)[key] = ''
  })
  pagination.currentPage = 1
  fetchCustomerList()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchCustomerList()
}

// 当前页变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchCustomerList()
}

// 查看详情
const handleViewDetail = (id: number) => {
  router.push({ path: '/home/crm/customer', query: { tab: '360view', id } })
}

// 新增客户
const handleAddCustomer = () => {
  currentCustomer.value = {}
  dialogVisible.value = true
}

// 编辑客户
const handleEditCustomer = (customer: Customer) => {
  currentCustomer.value = { ...customer }
  dialogVisible.value = true
}

// 删除客户
const handleDeleteCustomer = (id: number) => {
  ElMessageBox.confirm('确定要删除该客户吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await customerApi.deleteCustomer(id)
      ElMessage.success('删除成功')
      fetchCustomerList()
    } catch (error) {
      console.error('删除客户失败:', error)
      ElMessage.error('删除客户失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 对话框关闭
const handleDialogClose = () => {
  dialogVisible.value = false
  currentCustomer.value = {}
}

// 保存客户
const handleSaveCustomer = async (customer: Customer) => {
  try {
    if (customer.id) {
      await customerApi.updateCustomer(customer as Customer)
      ElMessage.success('更新成功')
    } else {
      await customerApi.createCustomer(customer as Customer)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchCustomerList()
  } catch (error) {
    console.error('保存客户失败:', error)
    ElMessage.error('保存客户失败')
  }
}

// 选择客户变化
const handleSelectionChange = (selection: Customer[]) => {
  selectedCustomers.value = selection
}
</script>

<style scoped>
.customer-list-view {
  padding: 10px;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.list-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1.1rem;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
