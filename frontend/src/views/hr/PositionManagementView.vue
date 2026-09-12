<template>
  <div class="position-management-view">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增职位</el-button>
      <el-button @click="fetchData">刷新</el-button>
    </div>

    <!-- 搜索和筛选表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline label-width="80px">
        <el-form-item label="职位名称">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入职位名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-select
            v-model="searchForm.departmentId"
            placeholder="全部"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            >
              <span style="float: left">{{ dept.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ dept.code }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="filteredPositions" border stripe>
      <el-table-column prop="name" label="职位名称" width="180" />
      <el-table-column prop="code" label="职位编码" width="150" />
      <el-table-column label="所属部门" width="180">
        <template #default="scope">
          {{ formatDepartment(scope.row.departmentId) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" />
      <el-table-column label="创建时间" width="180" prop="createdAt" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <!-- Dialog -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="职位名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入职位名称" />
        </el-form-item>
        <el-form-item label="职位编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入职位编码" />
        </el-form-item>
        <el-form-item label="所属部门" prop="departmentId">
          <el-select
            v-model="form.departmentId"
            placeholder="请选择部门"
            style="width: 100%"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            >
              <span style="float: left">{{ dept.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ dept.code }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="form.description" placeholder="请输入职位描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { hrApi, type Position, type Department } from '../../api/hr'
import { RequestCache } from '../../utils/request-cache'
import { handleListResponse } from '../../utils/response-handler'

const loading = ref(false)
const positions = ref<Position[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 部门数据
const departments = ref<Department[]>([])
const departmentsLoading = ref(false)

// 搜索和筛选条件
const searchForm = ref({
  keyword: '',
  departmentId: undefined as number | undefined,
  status: null as number | null
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = ref<Position>({
  name: '',
  code: '',
  departmentId: 0,
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入职位名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入职位编码', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑职位' : '新增职位')

// 获取部门数据
const fetchDepartments = async () => {
  departmentsLoading.value = true
  try {
    const cache = RequestCache.getInstance()
    cache.clearCache('/api/v1/hr/departments') // 修复缓存路径
    const res = await hrApi.department.getAll()
    
    // 使用统一响应处理器处理响应
    const result = handleListResponse(res.data)
    
    if (result.success) {
      departments.value = result.list
      console.log('处理后部门数据:', departments.value)
    } else {
      // 抛出错误，让catch块处理
      throw new Error(`获取部门数据失败: ${result.message}`)
    }
  } catch (error: any) {
    console.error('获取部门数据失败详情:', error)
    // 统一错误处理
    ElMessage.error(`获取部门数据失败: ${error.message || '未知错误'}`)
    // 发生错误时，清空部门列表
    departments.value = []
  } finally {
    departmentsLoading.value = false
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    // 清除缓存，确保获取最新数据
    const cache = RequestCache.getInstance()
    cache.clearCache('/api/v1/hr/positions')
    
    // 构建搜索参数
    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    // 添加搜索条件
    if (searchForm.value.keyword) {
      params.keyword = searchForm.value.keyword
    }
    if (searchForm.value.departmentId) {
      params.departmentId = searchForm.value.departmentId
    }
    if (searchForm.value.status !== null) {
      params.status = searchForm.value.status
    }
    
    console.log('请求职位数据，参数：', params)
    const res = await hrApi.position.getByPage(params)
    console.log('获取职位数据响应:', res)
    
    // 使用统一响应处理器处理响应
    const result = handleListResponse(res.data)
    
    if (result.success) {
      positions.value = result.list
      // 获取总记录数，处理分页情况
      const responseData = res.data
      const actualData = responseData?.code !== undefined ? responseData.data : responseData
      total.value = actualData.total || 0
      console.log('处理后职位数据:', positions.value)
    } else {
      // 抛出错误，让catch块处理
      throw new Error(`获取职位数据失败: ${result.message}`)
    }
  } catch (error: any) {
    console.error('获取职位数据失败详情:', error);
    // 统一错误处理
    ElMessage.error(`获取职位数据失败: ${error.message || '未知错误'}`)
    // 发生错误时，清空职位列表
    positions.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

// 搜索方法
const handleSearch = () => {
  // 搜索时重置页码
  currentPage.value = 1
  // 重新获取数据
  fetchData()
}

// 重置搜索条件
const handleReset = () => {
  searchForm.value = {
    keyword: '',
    departmentId: undefined,
    status: null
  }
  // 搜索时重置页码
  currentPage.value = 1
  // 重新获取数据
  fetchData()
}

// 过滤后的职位列表
const filteredPositions = computed(() => {
  return positions.value
})

// 格式化部门名称
const formatDepartment = (departmentId: number) => {
  const department = departments.value.find(dept => dept.id === departmentId)
  return department ? department.name : '未知部门'
}

const handleAdd = () => {
  isEdit.value = false
  form.value = {
    name: '',
    code: '',
    departmentId: departments.value[0]?.id || 0,
    description: '',
    status: 1
  }
  dialogVisible.value = true
}

const handleEdit = (row: Position) => {
  isEdit.value = true
  form.value = { ...row } // Clone
  dialogVisible.value = true
}

const handleDelete = (row: Position) => {
  ElMessageBox.confirm('确认删除该职位吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    if (row.id) {
      try {
        await hrApi.position.delete(row.id)
        ElMessage.success('删除成功')
        fetchData()
      } catch (e) {
        console.error('删除职位失败:', e)
        ElMessage.error('删除职位失败')
      }
    }
  })
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (isEdit.value && form.value.id) {
          await hrApi.position.update(form.value.id, form.value)
          ElMessage.success('更新成功')
        } else {
          await hrApi.position.create(form.value)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (e) {
        console.error(e)
        ElMessage.error('操作失败：' + (e as any).message || '未知错误')
      }
    }
  })
}

onMounted(async () => {
  await Promise.all([
    fetchDepartments(),
    fetchData()
  ])
})
</script>

<style scoped>
.position-management-view {
  padding: 20px;
}
.toolbar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.search-form {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .el-table {
    min-width: 800px;
  }
  
  .position-management-view {
    overflow-x: auto;
  }
}

@media (max-width: 768px) {
  .position-management-view {
    padding: 12px;
    overflow-x: auto;
  }
  
  .toolbar {
    margin-bottom: 16px;
    flex-direction: column;
    gap: 8px;
  }
  
  .search-form {
    margin-bottom: 16px;
    padding: 12px;
  }
  
  .search-form .el-form {
    display: flex;
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-form .el-form-item {
    margin-right: 0;
    margin-bottom: 12px;
  }
  
  .search-form .el-form-item:last-child {
    margin-bottom: 0;
    display: flex;
    gap: 8px;
  }
  
  .search-form .el-input,
  .search-form .el-select {
    width: 100% !important;
  }
  
  .el-table {
    min-width: 600px;
    font-size: 13px;
  }
  
  .el-table-column {
    padding: 0 4px;
  }
  
  .el-table .cell {
    padding: 8px 4px;
  }
  
  .pagination {
    margin-top: 16px;
    justify-content: center;
    font-size: 13px;
  }
  
  .pagination .el-pagination__total,
  .pagination .el-pagination__sizes {
    display: none;
  }
}

@media (max-width: 480px) {
  .position-management-view {
    padding: 8px;
  }
  
  .toolbar {
    margin-bottom: 12px;
  }
  
  .search-form {
    margin-bottom: 12px;
    padding: 8px;
  }
  
  .el-table {
    min-width: 500px;
    font-size: 12px;
  }
  
  .el-table-column--fixed-right {
    right: 0;
    z-index: 10;
  }
  
  .dialog-footer {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .dialog-footer .el-button {
    width: 100%;
  }
}
</style>