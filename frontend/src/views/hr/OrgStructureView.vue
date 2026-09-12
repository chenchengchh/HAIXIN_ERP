<template>
  <div class="org-structure-view">
    <div class="toolbar">
      <el-button type="primary" @click="handleAddRoot">新增一级部门</el-button>
      <el-button @click="fetchData">刷新</el-button>
    </div>

    <!-- 搜索和筛选表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline label-width="80px">
        <el-form-item label="部门名称">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入部门名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
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

    <el-table
      v-loading="loading"
      :data="filteredDepartmentTree"
      row-key="id"
      border
      default-expand-all
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column prop="name" label="部门名称" width="260" />
      <el-table-column prop="code" label="部门编码" width="150" />
      <el-table-column label="负责人" width="120">
        <template #default="scope">
          {{ scope.row.managerId ? employeeMap[scope.row.managerId] || '未知' : '无' }}
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
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="handleAddChild(scope.row)">添加子部门</el-button>
          <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Dialog -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="上级部门" prop="parentName">
          <el-input v-model="parentName" disabled placeholder="无 (一级部门)" />
        </el-form-item>
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="部门编码" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="负责人" prop="managerId">
          <el-select v-model="form.managerId" placeholder="请选择负责人" clearable>
            <el-option
              v-for="emp in employees"
              :key="emp.id"
              :label="emp.name"
              :value="emp.id"
            >
              <span style="float: left">{{ emp.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ emp.employeeNo }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="form.description" />
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
import { unwrapListResponse } from '../../api'
import { hrApi, type Department, type Employee } from '../../api/hr'
import { RequestCache } from '../../utils/request-cache'

const loading = ref(false)
const departments = ref<Department[]>([])
const departmentTree = ref<Department[]>([])
const allDepartments = ref<Department[]>([])

// 员工数据，用于显示负责人姓名
const employees = ref<Employee[]>([])
const employeeMap = ref<Record<number, string>>({})

// 搜索和筛选条件
const searchForm = ref({
  keyword: '',
  status: null as number | null
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const parentName = ref('')

const form = ref<Department>({
  name: '',
  code: '',
  parentId: undefined,
  managerId: undefined,
  status: 1,
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入部门编码', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑部门' : '新增部门')

// 转换员工数据格式，将后端字段映射到前端所需格式
  const transformEmployeeData = (employee: any): Employee => {
    return {
      id: employee.id,
      employeeNo: employee.employeeCode, // 后端是employeeCode，前端是employeeNo
      name: employee.name,
      gender: employee.gender === '男' ? 0 : employee.gender === '女' ? 1 : 2, // 字符串转数字
      birthDate: employee.birthDate,
      idCard: employee.idCard,
      phone: employee.phone,
      email: employee.email,
      departmentId: employee.departmentId,
      positionId: employee.positionId,
      hireDate: employee.hireDate,
      leaveDate: employee.leaveDate,
      status: employee.status === 'ACTIVE' ? 1 : 0, // 字符串转数字
      createdAt: employee.createdTime,
      updatedAt: employee.updatedTime
    }
  }

  // 转换部门数据格式，将后端字段映射到前端所需格式
  const transformDepartmentData = (department: any): Department => {
    if (!department) return { name: '', code: '', status: 0 }
    return {
      id: department.id,
      name: department.name || '',
      code: department.departmentCode || department.code || '', // 后端是departmentCode，前端是code
      parentId: department.parentId || department.parent_id, // 处理不同的字段名
      managerId: department.managerId,
      status: department.status === 'ACTIVE' || department.status === 1 ? 1 : 0, // 兼容字符串和数字
      description: department.description || '',
      children: [],
      createdAt: department.createdTime || department.createdAt,
      updatedAt: department.updatedTime || department.updatedAt
    }
  }

  // 获取员工数据
  const fetchEmployees = async () => {
    try {
      const cache = RequestCache.getInstance()
      cache.clearCache('/api/v1/hr/employees')
      
      const res = await hrApi.employee.getAll()
      const list = unwrapListResponse<any>(res)
      
      // 转换员工数据格式
      employees.value = list.map(transformEmployeeData)
      
      const map: Record<number, string> = {}
      employees.value.forEach((emp: Employee) => {
        if (emp.id) {
          map[emp.id] = emp.name
        }
      })
      employeeMap.value = map
    } catch (error) {
      console.error('获取员工数据失败：', error)
      // 发生错误时，清空员工列表
      employees.value = []
      employeeMap.value = {}
    }
  }

  const fetchData = async () => {
    loading.value = true
    try {
      await Promise.all([
        (async () => {
          const cache = RequestCache.getInstance()
          cache.clearCache('/api/v1/hr/departments')
          
          const res = await hrApi.department.getAll()
          const list = unwrapListResponse<any>(res)
          
          // 转换部门数据格式
          const transformedDepartments = list.map(transformDepartmentData).filter(Boolean) as Department[]
          departments.value = transformedDepartments
          allDepartments.value = [...transformedDepartments]
          departmentTree.value = buildTree(transformedDepartments)
        })(),
        fetchEmployees()
      ])
    } catch (error) {
      console.error('获取部门数据失败：', error)
      // 发生错误时，清空部门列表
      departments.value = []
      allDepartments.value = []
      departmentTree.value = []
    } finally {
      loading.value = false
    }
  }

// 搜索方法
const handleSearch = () => {
  let filtered = [...allDepartments.value]
  if (searchForm.value.keyword) {
    const keyword = searchForm.value.keyword.toLowerCase()
    filtered = filtered.filter(dept => dept.name.toLowerCase().includes(keyword))
  }
  if (searchForm.value.status !== null) {
    filtered = filtered.filter(dept => dept.status === searchForm.value.status)
  }
  departments.value = filtered
  departmentTree.value = buildTree(filtered)
}

// 重置搜索条件
const handleReset = () => {
  searchForm.value = {
    keyword: '',
    status: null
  }
  departments.value = [...allDepartments.value]
  departmentTree.value = buildTree(departments.value)
}

const filteredDepartmentTree = computed(() => departmentTree.value)

const buildTree = (items: Department[]) => {
  if (!Array.isArray(items)) return []
  const result: Department[] = []
  const itemMap: Record<number, Department> = {}
  
  items.forEach(item => {
    if (item && item.id !== undefined) {
      itemMap[item.id] = { ...item, children: [] }
    }
  })

  items.forEach(item => {
    if (!item || item.id === undefined) return
    const mappedItem = itemMap[item.id]
    if (item.parentId && itemMap[item.parentId]) {
      const parent = itemMap[item.parentId]
      if (parent && mappedItem) {
        if (!parent.children) parent.children = []
        parent.children.push(mappedItem)
      }
    } else if (mappedItem) {
      result.push(mappedItem)
    }
  })
  
  return result
}

const handleAddRoot = () => {
  isEdit.value = false
  form.value = {
    name: '',
    code: '',
    parentId: undefined,
    managerId: undefined,
    status: 1,
    description: ''
  }
  parentName.value = '无 (一级部门)'
  dialogVisible.value = true
}

const handleAddChild = (row: Department) => {
  isEdit.value = false
  form.value = {
    name: '',
    code: '',
    parentId: row.id,
    managerId: undefined,
    status: 1,
    description: ''
  }
  parentName.value = row.name
  dialogVisible.value = true
}

const handleEdit = (row: Department) => {
  isEdit.value = true
  form.value = { ...row }
  if (row.parentId) {
    const parent = allDepartments.value.find(d => d.id === row.parentId)
    parentName.value = parent ? parent.name : '未知'
  } else {
    parentName.value = '无 (一级部门)'
  }
  dialogVisible.value = true
}

const handleDelete = (row: Department) => {
  ElMessageBox.confirm('确认删除该部门及其子部门吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    if (row.id) {
      try {
        await hrApi.department.delete(row.id)
        ElMessage.success('删除成功')
        fetchData()
      } catch (e) {
        console.error(e)
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
          await hrApi.department.update(form.value.id, form.value)
          ElMessage.success('更新成功')
        } else {
          await hrApi.department.create(form.value)
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

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.org-structure-view {
  padding: 20px;
}
.toolbar {
  margin-bottom: 20px;
}
.search-form {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}
</style>
