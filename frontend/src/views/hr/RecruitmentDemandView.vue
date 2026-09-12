<template>
  <div class="recruitment-demand-view">
    <div class="toolbar">
      <el-form inline>
        <el-form-item>
          <el-button type="primary" @click="handleNewDemand">提交招聘需求</el-button>
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
            placeholder="需求编号/招聘岗位"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-select
            v-model="searchForm.departmentId"
            placeholder="全部"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部"
            clearable
            style="width: 150px"
          >
            <el-option label="待审批" :value="0" />
            <el-option label="已批准" :value="1" />
            <el-option label="已关闭" :value="2" />
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

    <el-table v-loading="loading" :data="demandList" border stripe>
      <el-table-column prop="id" label="需求编号" width="150" />
      <el-table-column label="需求部门" width="150">
        <template #default="scope">
          {{ formatDepartment(scope.row.departmentId) }}
        </template>
      </el-table-column>
      <el-table-column prop="positionName" label="招聘岗位" width="150" />
      <el-table-column prop="demandNumber" label="招聘人数" width="100" />
      <el-table-column prop="expectedSalary" label="期望薪资" width="150" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="updateTime" label="更新时间" width="180" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="warning" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleCancel(scope.row)">关闭</el-button>
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
            <el-form-item label="招聘岗位" prop="positionName">
              <el-input v-model="form.positionName" placeholder="请输入招聘岗位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求部门" prop="departmentId">
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
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="招聘人数" prop="demandNumber">
              <el-input-number v-model="form.demandNumber" :min="1" :max="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期望薪资" prop="expectedSalary">
              <el-input v-model="form.expectedSalary" placeholder="请输入期望薪资范围，如：10k-15k" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="所需技能" prop="requiredSkills">
          <el-input v-model="form.requiredSkills" placeholder="请输入所需技能，以逗号分隔" />
        </el-form-item>
        <el-form-item label="需求描述" prop="demandDescription">
          <el-input type="textarea" v-model="form.demandDescription" placeholder="请输入需求描述" :rows="3" />
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
import { unwrapListResponse, unwrapPageResponse } from '../../api'
import { recruitmentApi, hrApi, type RecruitmentDemand, type Department } from '../../api/hr'

const loading = ref(false)
const departmentsLoading = ref(false)
const demandList = ref<RecruitmentDemand[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 部门数据
const departments = ref<Department[]>([])

// 搜索和筛选条件
const searchForm = ref({
  keyword: '',
  departmentId: undefined,
  status: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = ref<RecruitmentDemand>({
  positionName: '',
  departmentId: 0,
  demandNumber: 1,
  requiredSkills: '',
  expectedSalary: '',
  demandDescription: '',
  status: 0
})

const rules = {
  positionName: [{ required: true, message: '请输入招聘岗位', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择需求部门', trigger: 'change' }],
  demandNumber: [{ required: true, message: '请输入招聘人数', trigger: 'change' }],
  requiredSkills: [{ required: true, message: '请输入所需技能', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑招聘需求' : '提交招聘需求')

// 获取部门数据
const fetchDepartments = async () => {
  departmentsLoading.value = true
  try {
    const res = await hrApi.department.getAll()
    departments.value = unwrapListResponse<Department>(res)
  } catch (error) {
    console.error('获取部门数据失败:', error)
    ElMessage.error('获取部门数据失败')
    departments.value = []
  } finally {
    departmentsLoading.value = false
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
    status: ''
  }
  // 搜索时重置页码
  currentPage.value = 1
  // 重新获取数据
  fetchData()
}

// 获取招聘需求列表
const fetchData = async () => {
  loading.value = true
  try {
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
    if (searchForm.value.status !== '') {
      params.status = searchForm.value.status
    }
    
    const res = await recruitmentApi.getDemands(params)
    const page = unwrapPageResponse<RecruitmentDemand>(res)
    demandList.value = page.list
    total.value = page.total
  } catch (error) {
    console.error('获取招聘需求失败:', error)
    ElMessage.error('获取招聘需求失败')
    demandList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 提交新需求
const handleNewDemand = () => {
  isEdit.value = false
  form.value = {
    positionName: '',
    departmentId: 0,
    demandNumber: 1,
    requiredSkills: '',
    expectedSalary: '',
    demandDescription: '',
    status: 0
  }
  dialogVisible.value = true
}

// 查看需求详情
const handleView = (row: RecruitmentDemand) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

// 编辑需求
const handleEdit = (row: RecruitmentDemand) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

// 关闭需求
const handleCancel = (row: RecruitmentDemand) => {
  ElMessageBox.confirm('确认关闭该招聘需求吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    if (row.id) {
      try {
        // 这里假设我们有一个关闭需求的API
        // await recruitmentApi.closeDemand(row.id)
        ElMessage.success('需求已关闭')
        fetchData()
      } catch (e) {
        console.error('关闭需求失败:', e)
      }
    }
  })
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (isEdit.value && form.value.id) {
          // await recruitmentApi.updateDemand(form.value.id, form.value)
          ElMessage.success('需求更新成功')
        } else {
          await recruitmentApi.createDemand(form.value)
          ElMessage.success('需求提交成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (e) {
        console.error('提交需求失败:', e)
        ElMessage.error('提交需求失败')
      }
    }
  })
}

// 获取状态类型
const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'info'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '草稿',
    1: '已批准',
    2: '已关闭'
  }
  return textMap[status] || '未知'
}

// 格式化部门名称
const formatDepartment = (departmentId: number) => {
  const department = departments.value.find(dept => dept.id === departmentId)
  return department ? department.name : '未知部门'
}

onMounted(() => {
  fetchData()
  fetchDepartments()
})
</script>

<style scoped>
.recruitment-demand-view {
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
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
