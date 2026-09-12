<template>
  <div class="employee-profile-view">
    <div class="toolbar">
      <el-form inline>
        <el-form-item>
          <el-button type="primary" @click="handleAdd">新增员工</el-button>
        </el-form-item>
        <el-form-item>
           <el-button @click="fetchData">刷新</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 搜索和筛选表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline label-width="80px">
        <el-form-item label="关键字">
          <el-input
            v-model="searchForm.keyword"
            placeholder="员工编号/姓名/邮箱/电话"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-select
            v-model="searchForm.departmentId"
            placeholder="全部"
            clearable
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位">
          <el-select
            v-model="searchForm.positionId"
            placeholder="全部"
            clearable
          >
            <el-option
              v-for="pos in positions"
              :key="pos.id"
              :label="pos.name"
              :value="pos.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部"
            clearable
          >
            <el-option label="试用期" :value="0" />
            <el-option label="正式" :value="1" />
            <el-option label="离职" :value="2" />
            <el-option label="请假" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-select
            v-model="searchForm.gender"
            placeholder="全部"
            clearable
          >
            <el-option label="男" :value="0" />
            <el-option label="女" :value="1" />
            <el-option label="其他" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="employees" border stripe>
      <el-table-column prop="employeeNo" label="工号" width="120" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="gender" label="性别" width="80">
        <template #default="scope">
          {{ formatGender(scope.row.gender) }}
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="电话" width="150" />
      <el-table-column label="部门" width="150">
        <template #default="scope">
          {{ formatDepartment(scope.row.departmentId) }}
        </template>
      </el-table-column>
      <el-table-column label="岗位" width="150">
        <template #default="scope">
          {{ formatPosition(scope.row.positionId) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
         <template #default="scope">
           {{ formatStatus(scope.row.status) }}
         </template>
      </el-table-column>
      <el-table-column prop="hireDate" label="入职日期" width="120" />
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
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="700px">
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工号" prop="employeeNo">
              <el-input v-model="form.employeeNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择">
                <el-option label="男" :value="0" />
                <el-option label="女" :value="1" />
                <el-option label="其他" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
             <el-form-item label="出生日期" prop="birthDate">
                <el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
             </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
             <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="form.idCard" />
             </el-form-item>
          </el-col>
           <el-col :span="12">
             <el-form-item label="联系电话" prop="phone">
                <el-input v-model="form.phone" />
             </el-form-item>
           </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门" prop="departmentId">
              <el-select
                v-model="form.departmentId"
                placeholder="请选择部门"
                style="width: 100%"
                :loading="departmentsLoading"
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
          </el-col>
          <el-col :span="12">
             <el-form-item label="岗位" prop="positionId">
               <el-select
                 v-model="form.positionId"
                 placeholder="请选择岗位"
                 style="width: 100%"
                 :loading="positionsLoading"
               >
                 <el-option
                   v-for="pos in positions"
                   :key="pos.id"
                   :label="pos.name"
                   :value="pos.id"
                 >
                   <span style="float: left">{{ pos.name }}</span>
                   <span style="float: right; color: #8492a6; font-size: 13px">{{ pos.code }}</span>
                 </el-option>
               </el-select>
             </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入职日期" prop="hireDate">
               <el-date-picker v-model="form.hireDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="员工状态" prop="status">
               <el-select v-model="form.status">
                 <el-option label="试用期" :value="0" />
                 <el-option label="正式" :value="1" />
                 <el-option label="离职" :value="2" />
                 <el-option label="请假" :value="3" />
               </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="办公地址" prop="officeAddress">
           <el-input v-model="form.officeAddress" />
        </el-form-item>
         <el-form-item label="备注" prop="remark">
           <el-input type="textarea" v-model="form.remark" />
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
import { unwrapPageResponse } from '../../api'
import { hrApi } from '../../api/hr'
import { RequestCache } from '../../utils/request-cache'
import { handleListResponse } from '../../utils/response-handler'
import type { Employee, Department, Position } from '../../api/hr'

const getEmployeesByPage = hrApi.employee.getByPage
const createEmployee = hrApi.employee.create
const updateEmployee = hrApi.employee.update
const deleteEmployee = hrApi.employee.delete
const getDepartments = hrApi.department.getAll
const getPositions = hrApi.position.getAll

const loading = ref(false)
const employees = ref<Employee[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 部门和岗位数据
const departments = ref<Department[]>([])
const positions = ref<Position[]>([])
const departmentsLoading = ref(false)
const positionsLoading = ref(false)

// 搜索和筛选条件
const searchForm = ref({
  keyword: '',
  departmentId: undefined,
  positionId: undefined,
  status: '',
  gender: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = ref<Employee>({
  employeeNo: '',
  name: '',
  gender: 0,
  departmentId: 0,
  positionId: 0,
  hireDate: '',
  status: 0
})

const rules = {
  employeeNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请输入部门ID', trigger: 'blur' }],
  hireDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑员工' : '新增员工')

// 转换员工数据格式，将后端字段映射到前端所需格式
const transformEmployeeData = (employee: any): Employee => {
  console.log('开始转换员工数据:', employee);
  
  // 直接返回转换结果，确保所有字段都被正确映射
  const result = {
    id: employee.id,
    // 兼容后端可能使用的字段名
    employeeNo: employee.employeeNo || employee.employeeCode || employee.code,
    name: employee.name,
    // 处理性别字段，确保返回数字
    gender: employee.gender === '男' || employee.gender === 0 ? 0 : 
            employee.gender === '女' || employee.gender === 1 ? 1 : 2,
    birthDate: employee.birthDate || employee.birthday,
    idCard: employee.idCard || employee.idNumber,
    phone: employee.phone || employee.mobile || employee.tel,
    email: employee.email,
    departmentId: employee.departmentId || employee.deptId,
    positionId: employee.positionId || employee.posId,
    hireDate: employee.hireDate || employee.joinDate,
    leaveDate: employee.leaveDate || employee.departureDate,
    // 处理状态字段，确保返回数字
    status: employee.status === 'ACTIVE' || employee.status === 1 || employee.status === '1' ? 1 : 
            employee.status === 'INACTIVE' || employee.status === 0 || employee.status === '0' ? 0 : 
            employee.status === 'RESIGNED' || employee.status === 2 || employee.status === '2' ? 2 : 
            employee.status === 'ON_LEAVE' || employee.status === 3 || employee.status === '3' ? 3 : 0,
    createdAt: employee.createdAt || employee.createTime,
    updatedAt: employee.updatedAt || employee.updateTime
  };
  
  console.log('转换后员工数据:', result);
  return result;
}

// 转换部门数据格式，将后端字段映射到前端所需格式
const transformDepartmentData = (department: any): Department => {
  return {
    id: department.id,
    name: department.name,
    code: department.departmentCode, // 后端是departmentCode，前端是code
    parentId: department.parentId || department.parent_id, // 处理不同的字段名
    managerId: department.managerId,
    status: department.status === 'ACTIVE' ? 1 : 0, // 字符串转数字
    description: department.description,
    children: [],
    createdAt: department.createdTime,
    updatedAt: department.updatedTime
  }
}

// 转换岗位数据格式，将后端字段映射到前端所需格式
const transformPositionData = (position: any): Position => {
  return {
    id: position.id,
    name: position.name,
    code: position.positionCode, // 后端是positionCode，前端是code
    departmentId: position.departmentId,
    description: position.description,
    status: position.status === 'ACTIVE' ? 1 : 0, // 字符串转数字
    createdAt: position.createdTime,
    updatedAt: position.updatedTime
  }
}

// 获取部门数据
const fetchDepartments = async () => {
  departmentsLoading.value = true
  try {
    const cache = RequestCache.getInstance()
    cache.clearCache('/hr/departments')
    const res = await getDepartments()
    
    // 使用统一响应处理器处理响应
    const result = handleListResponse(res.data)
    
    if (result.success) {
      departments.value = result.list.map(transformDepartmentData)
      console.log('处理后部门数据:', departments.value)
    } else {
      // 抛出错误，让catch块处理
      throw new Error(`获取部门数据失败: ${result.message}`)
    }
  } catch (error: any) {
    console.error('获取部门数据失败详情:', error)
    // 统一错误处理
    ElMessage.error(`获取部门数据失败: ${error.message || '未知错误'}`)
    departments.value = []
  } finally {
    departmentsLoading.value = false
  }
}

// 获取岗位数据
const fetchPositions = async () => {
  positionsLoading.value = true
  try {
    const cache = RequestCache.getInstance()
    cache.clearCache('/hr/positions')
    const res = await getPositions()
    
    // 使用统一响应处理器处理响应
    const result = handleListResponse(res.data)
    
    if (result.success) {
      positions.value = result.list.map(transformPositionData)
      console.log('处理后岗位数据:', positions.value)
    } else {
      // 抛出错误，让catch块处理
      throw new Error(`获取岗位数据失败: ${result.message}`)
    }
  } catch (error: any) {
    console.error('获取岗位数据失败详情:', error)
    // 统一错误处理
    ElMessage.error(`获取岗位数据失败: ${error.message || '未知错误'}`)
    positions.value = []
  } finally {
    positionsLoading.value = false
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
    positionId: undefined,
    status: '',
    gender: ''
  }
  // 搜索时重置页码
  currentPage.value = 1
  // 重新获取数据
  fetchData()
}

const fetchData = async () => {
  loading.value = true
  try {
    console.log('开始获取员工数据');
    
    // 清除缓存，确保获取最新数据
    const cache = RequestCache.getInstance()
    cache.clearCache('/hr/employees')
    
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
    if (searchForm.value.positionId) {
      params.positionId = searchForm.value.positionId
    }
    if (searchForm.value.status !== '') {
      params.status = searchForm.value.status
    }
    if (searchForm.value.gender !== '') {
      params.gender = searchForm.value.gender
    }
    
    console.log('请求员工数据，参数：', params)
    const res = await getEmployeesByPage({ page: currentPage.value, size: pageSize.value, ...params })
    console.log('获取员工数据成功，原始响应：', JSON.stringify(res))
    const page = unwrapPageResponse<any>(res)
    const employeeList = page.list
    
    console.log('员工列表：', JSON.stringify(employeeList))
    
    const transformedEmployees = employeeList.map((emp, index) => {
      const transformed = transformEmployeeData(emp)
      console.log(`转换员工数据 ${index}：`, JSON.stringify(emp), '->', JSON.stringify(transformed))
      return transformed
    })
    
    // 直接修改employees数组，确保响应式更新
    employees.value = [...transformedEmployees];
    console.log('转换后员工列表：', JSON.stringify(employees.value))
    console.log('员工列表长度：', employees.value.length)
    
    // 处理总记录数，支持多种可能的字段名
    total.value = page.total || employeeList.length;
    console.log('总记录数：', total.value)
  } catch (error: any) {
    console.error('获取员工数据失败详情:', error)
    // 区分Axios错误和业务错误
    const errorMsg = error.isAxiosError ? 
      `网络错误: ${error.message}` : 
      `业务错误: ${error.message || '获取员工数据失败'}`
    ElMessage.error(errorMsg)
    
    // 发生错误时，清空员工列表
    employees.value = [];
    total.value = 0;
  } finally {
    loading.value = false
    console.log('数据获取完成，最终员工列表：', JSON.stringify(employees.value));
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.value = {
    employeeNo: '',
    name: '',
    gender: 0,
    departmentId: 0, // Should use a picker in real app
    positionId: 0,
    hireDate: new Date().toISOString().split('T')[0] || '',
    status: 0
  }
  dialogVisible.value = true
}

const handleEdit = (row: Employee) => {
  isEdit.value = true
  
  // 深拷贝row对象，避免直接修改原数据
  const formData = JSON.parse(JSON.stringify(row))
  
  // 日期字段已经是字符串格式，不需要转换
  form.value = formData
  dialogVisible.value = true
}

const handleDelete = (row: Employee) => {
   ElMessageBox.confirm('确认删除员工吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    if (row.id) {
      try {
        await deleteEmployee(row.id)
        ElMessage.success('删除成功')
        fetchData()
      } catch (e) {
        // handled
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
          await updateEmployee(form.value.id, form.value)
          ElMessage.success('更新成功')
        } else {
          await createEmployee(form.value)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (e) {
        console.error(e)
      }
    }
  })
}

const formatGender = (val: number) => {
  const map: Record<number, string> = { 0: '男', 1: '女', 2: '其他' }
  return map[val] || '未知'
}

const formatStatus = (val: number) => {
   const map: Record<number, string> = { 0: '试用期', 1: '正式', 2: '离职', 3: '请假' }
   return map[val] || '未知'
}

// 格式化部门名称
const formatDepartment = (departmentId: number) => {
  const department = departments.value.find(dept => dept.id === departmentId)
  return department ? department.name : '未知部门'
}

// 格式化岗位名称
const formatPosition = (positionId: number) => {
  const position = positions.value.find(pos => pos.id === positionId)
  return position ? position.name : '未知岗位'
}

onMounted(async () => {
  // 先加载部门和岗位数据，再加载员工数据
  await fetchDepartments()
  await fetchPositions()
  await fetchData()
})
</script>

<style scoped>
.employee-profile-view {
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
    min-width: 1000px;
  }
  
  .employee-profile-view {
    overflow-x: auto;
  }
}

@media (max-width: 768px) {
  .employee-profile-view {
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
    min-width: 800px;
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
  .employee-profile-view {
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
    min-width: 600px;
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
