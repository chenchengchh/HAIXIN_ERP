<template>
  <div class="performance-management-view">
    <div class="toolbar">
      <el-button type="primary" @click="handleAddObjective">新增绩效目标</el-button>
      <el-button type="primary" @click="handleAddAppraisal">新增绩效评估</el-button>
      <el-button @click="fetchData">刷新</el-button>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 绩效目标管理 -->
      <el-tab-pane label="绩效目标管理" name="objectives">
        <div class="search-form">
          <el-form :model="searchForm.objective" inline label-width="80px">
            <el-form-item label="员工姓名">
              <el-input
                v-model="searchForm.objective.employeeName"
                placeholder="请输入员工姓名"
                clearable
                style="width: 180px"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select
                v-model="searchForm.objective.status"
                placeholder="全部"
                clearable
                style="width: 120px"
              >
                <el-option label="进行中" :value="0" />
                <el-option label="已完成" :value="1" />
                <el-option label="已取消" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch('objective')">查询</el-button>
              <el-button @click="handleReset('objective')">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table v-loading="loading.objective" :data="objectivesList" border stripe>
          <el-table-column prop="employeeName" label="员工姓名" width="120" />
          <el-table-column prop="objectiveContent" label="绩效目标" width="300" />
          <el-table-column prop="targetValue" label="目标值" width="120" />
          <el-table-column prop="weight" label="权重(%)" width="80" />
          <el-table-column prop="startDate" label="开始日期" width="120" />
          <el-table-column prop="endDate" label="结束日期" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getObjectiveStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleEditObjective(scope.row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDeleteObjective(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.objective.currentPage"
            v-model:page-size="pagination.objective.pageSize"
            :page-sizes="pageSizes"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.objective.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-tab-pane>

      <!-- 绩效评估管理 -->
      <el-tab-pane label="绩效评估管理" name="appraisals">
        <div class="search-form">
          <el-form :model="searchForm.appraisal" inline label-width="80px">
            <el-form-item label="员工姓名">
              <el-input
                v-model="searchForm.appraisal.employeeName"
                placeholder="请输入员工姓名"
                clearable
                style="width: 180px"
              />
            </el-form-item>
            <el-form-item label="评估周期">
              <el-input
                v-model="searchForm.appraisal.appraisalPeriod"
                placeholder="请输入评估周期"
                clearable
                style="width: 180px"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select
                v-model="searchForm.appraisal.status"
                placeholder="全部"
                clearable
                style="width: 120px"
              >
                <el-option label="待审批" :value="0" />
                <el-option label="已审批" :value="1" />
                <el-option label="已完成" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch('appraisal')">查询</el-button>
              <el-button @click="handleReset('appraisal')">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table v-loading="loading.appraisal" :data="appraisalsList" border stripe>
          <el-table-column prop="employeeName" label="员工姓名" width="120" />
          <el-table-column prop="appraisalPeriod" label="评估周期" width="150" />
          <el-table-column prop="objectiveScore" label="目标完成分" width="100" />
          <el-table-column prop="competencyScore" label="能力评估分" width="100" />
          <el-table-column prop="totalScore" label="总分" width="80">
            <template #default="scope">
              <span :style="{ color: getScoreColor(scope.row.totalScore) }">
                {{ scope.row.totalScore }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getAppraisalStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="appraiserName" label="评估人" width="120" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleViewAppraisal(scope.row)">查看</el-button>
              <el-button link type="success" size="small" v-if="scope.row.status === 0" @click="handleApproveAppraisal(scope.row)">审批</el-button>
              <el-button link type="danger" size="small" @click="handleDeleteAppraisal(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.appraisal.currentPage"
            v-model:page-size="pagination.appraisal.pageSize"
            :page-sizes="pageSizes"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.appraisal.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 绩效目标对话框 -->
    <el-dialog :title="objectiveDialogTitle" v-model="dialogVisible.objective" width="600px">
      <el-form :model="objectiveForm" label-width="120px" :rules="objectiveRules" ref="objectiveFormRef">
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="objectiveForm.employeeId" placeholder="请选择员工">
            <el-option
              v-for="emp in employees"
              :key="emp.id"
              :label="emp.name"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="绩效目标" prop="objectiveContent">
          <el-input v-model="objectiveForm.objectiveContent" type="textarea" rows="3" placeholder="请输入绩效目标" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="目标值" prop="targetValue">
              <el-input v-model="objectiveForm.targetValue" placeholder="请输入目标值" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权重(%)" prop="weight">
              <el-input-number v-model="objectiveForm.weight" :min="0" :max="100" :step="5" placeholder="请输入权重" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="objectiveForm.startDate" type="date" placeholder="选择开始日期" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="objectiveForm.endDate" type="date" placeholder="选择结束日期" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-select v-model="objectiveForm.status" placeholder="请选择状态">
            <el-option label="进行中" :value="0" />
            <el-option label="已完成" :value="1" />
            <el-option label="已取消" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible.objective = false">取消</el-button>
          <el-button type="primary" @click="handleSaveObjective">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 绩效评估对话框 -->
    <el-dialog :title="appraisalDialogTitle" v-model="dialogVisible.appraisal" width="600px">
      <el-form :model="appraisalForm" label-width="120px" :rules="appraisalRules" ref="appraisalFormRef">
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="appraisalForm.employeeId" placeholder="请选择员工">
            <el-option
              v-for="emp in employees"
              :key="emp.id"
              :label="emp.name"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="评估周期" prop="appraisalPeriod">
          <el-input v-model="appraisalForm.appraisalPeriod" placeholder="如：2025年第一季度" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="目标完成分" prop="objectiveScore">
              <el-input-number v-model="appraisalForm.objectiveScore" :min="0" :max="100" :step="1" placeholder="请输入目标完成分" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="能力评估分" prop="competencyScore">
              <el-input-number v-model="appraisalForm.competencyScore" :min="0" :max="100" :step="1" placeholder="请输入能力评估分" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="总分" prop="totalScore">
          <el-input-number v-model="appraisalForm.totalScore" :min="0" :max="100" :step="1" placeholder="请输入总分" />
        </el-form-item>
        <el-form-item label="评估状态" prop="appraisalStatus">
          <el-select v-model="appraisalForm.appraisalStatus" placeholder="请选择评估状态">
            <el-option label="待审批" :value="0" />
            <el-option label="已审批" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="评估人" prop="appraiserId">
          <el-select v-model="appraisalForm.appraiserId" placeholder="请选择评估人">
            <el-option
              v-for="emp in employees"
              :key="emp.id"
              :label="emp.name"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="评估备注" prop="appraisalNotes">
          <el-input v-model="appraisalForm.appraisalNotes" type="textarea" rows="3" placeholder="请输入评估备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible.appraisal = false">取消</el-button>
          <el-button type="primary" @click="handleSaveAppraisal">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { hrApi, type Employee, type PerformanceObjective, type PerformanceAppraisal } from '../../api/hr'

const activeTab = ref('objectives')

// 加载状态
const loading = ref({
  objective: false,
  appraisal: false,
  employees: false
})

// 员工数据
const employees = ref<Employee[]>([])

// 绩效目标数据
const objectives = ref<PerformanceObjective[]>([])
const appraisals = ref<PerformanceAppraisal[]>([])

// 分页相关数据
const pagination = ref({
  objective: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  appraisal: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  }
})

// 每页条数选项
const pageSizes = [10, 20, 50, 100]

// 搜索表单数据
const searchForm = ref({
  objective: {
    employeeName: '',
    status: ''
  },
  appraisal: {
    employeeName: '',
    appraisalPeriod: '',
    status: ''
  }
})

// 对话框控制
const dialogVisible = ref({
  objective: false,
  appraisal: false
})

// 表单数据
const objectiveForm = ref<PerformanceObjective>({
  employeeId: 0,
  objectiveContent: '',
  targetValue: '',
  weight: 0,
  startDate: '',
  endDate: '',
  status: 0
})

const appraisalForm = ref<PerformanceAppraisal>({
  employeeId: 0,
  appraisalPeriod: '',
  objectiveScore: 0,
  competencyScore: 0,
  totalScore: 0,
  appraisalStatus: 0,
  appraiserId: 0,
  appraisalNotes: ''
})

// 表单引用
const objectiveFormRef = ref()
const appraisalFormRef = ref()

// 编辑模式
const isEditObjective = ref(false)
const isEditAppraisal = ref(false)

// 对话框标题
const objectiveDialogTitle = computed(() => isEditObjective.value ? '编辑绩效目标' : '新增绩效目标')
const appraisalDialogTitle = computed(() => isEditAppraisal.value ? '编辑绩效评估' : '新增绩效评估')

// 表单验证规则
const objectiveRules = {
  employeeId: [
    { required: true, message: '请选择员工', trigger: 'change' }
  ],
  objectiveContent: [
    { required: true, message: '请输入绩效目标', trigger: 'blur' }
  ],
  targetValue: [
    { required: true, message: '请输入目标值', trigger: 'blur' }
  ],
  weight: [
    { required: true, message: '请输入权重', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ]
}

const appraisalRules = {
  employeeId: [
    { required: true, message: '请选择员工', trigger: 'change' }
  ],
  appraisalPeriod: [
    { required: true, message: '请输入评估周期', trigger: 'blur' }
  ],
  objectiveScore: [
    { required: true, message: '请输入目标完成分', trigger: 'blur' }
  ],
  competencyScore: [
    { required: true, message: '请输入能力评估分', trigger: 'blur' }
  ],
  totalScore: [
    { required: true, message: '请输入总分', trigger: 'blur' }
  ],
  appraiserId: [
    { required: true, message: '请选择评估人', trigger: 'change' }
  ]
}

// 获取员工数据
const fetchEmployees = async () => {
  loading.value.employees = true
  try {
    const res = await hrApi.employee.getAll()
    employees.value = Array.isArray(res.data) ? res.data : Array.isArray(res.data?.list) ? res.data.list : []
  } catch (error) {
    console.error('获取员工数据失败:', error)
    ElMessage.error('获取员工数据失败')
    employees.value = []
  } finally {
    loading.value.employees = false
  }
}

// 获取绩效目标
const fetchObjectives = async () => {
  loading.value.objective = true
  try {
    const res = await hrApi.performance.getObjectives(0) // 0表示获取所有员工的绩效目标
    objectives.value = Array.isArray(res.data) ? res.data : []
    pagination.value.objective.total = objectives.value.length
  } catch (error) {
    console.error('获取绩效目标失败:', error)
    ElMessage.error('获取绩效目标失败')
    objectives.value = []
    pagination.value.objective.total = 0
  } finally {
    loading.value.objective = false
  }
}

// 获取绩效评估
const fetchAppraisals = async () => {
  loading.value.appraisal = true
  try {
    const res = await hrApi.performance.getAppraisals(0) // 0表示获取所有员工的绩效评估
    appraisals.value = Array.isArray(res.data) ? res.data : []
    pagination.value.appraisal.total = appraisals.value.length
  } catch (error) {
    console.error('获取绩效评估失败:', error)
    ElMessage.error('获取绩效评估失败')
    appraisals.value = []
    pagination.value.appraisal.total = 0
  } finally {
    loading.value.appraisal = false
  }
}

// 初始化数据
const initData = async () => {
  await Promise.all([
    fetchEmployees(),
    fetchObjectives(),
    fetchAppraisals()
  ])
}

// 计算属性：绩效目标列表
const objectivesList = computed(() => {
  let filtered = [...objectives.value]
  
  // 搜索过滤
  if (searchForm.value.objective.employeeName) {
    const employeeName = searchForm.value.objective.employeeName.toLowerCase()
    // 由于objective中没有直接存储员工姓名，这里简化处理，只根据员工ID过滤
    // 实际项目中应该在后端实现搜索功能
  }
  
  if (searchForm.value.objective.status !== '') {
    filtered = filtered.filter(obj => obj.status === parseInt(searchForm.value.objective.status))
  }
  
  // 更新总数
  pagination.value.objective.total = filtered.length
  
  // 分页处理
  const start = (pagination.value.objective.currentPage - 1) * pagination.value.objective.pageSize
  const end = start + pagination.value.objective.pageSize
  return filtered.slice(start, end)
})

// 计算属性：绩效评估列表
const appraisalsList = computed(() => {
  let filtered = [...appraisals.value]
  
  // 搜索过滤
  if (searchForm.value.appraisal.employeeName) {
    // 由于appraisal中没有直接存储员工姓名，这里简化处理，只根据员工ID过滤
    // 实际项目中应该在后端实现搜索功能
  }
  
  if (searchForm.value.appraisal.appraisalPeriod) {
    const period = searchForm.value.appraisal.appraisalPeriod.toLowerCase()
    filtered = filtered.filter(appraisal => appraisal.appraisalPeriod.toLowerCase().includes(period))
  }
  
  if (searchForm.value.appraisal.status !== '') {
    filtered = filtered.filter(appraisal => appraisal.appraisalStatus === parseInt(searchForm.value.appraisal.status))
  }
  
  // 更新总数
  pagination.value.appraisal.total = filtered.length
  
  // 分页处理
  const start = (pagination.value.appraisal.currentPage - 1) * pagination.value.appraisal.pageSize
  const end = start + pagination.value.appraisal.pageSize
  return filtered.slice(start, end)
})

// 获取状态类型
const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning', // 待审批/进行中
    1: 'success', // 已通过/已完成
    2: 'danger',  // 已拒绝/已取消
    3: 'info'     // 其他
  }
  return typeMap[status] || 'info'
}

// 获取绩效目标状态文本
const getObjectiveStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '进行中',
    1: '已完成',
    2: '已取消'
  }
  return textMap[status] || '未知'
}

// 获取绩效评估状态文本
const getAppraisalStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '待审批',
    1: '已审批',
    2: '已完成'
  }
  return textMap[status] || '未知'
}

// 获取分数颜色
const getScoreColor = (score: number) => {
  if (score >= 90) return '#67c23a'
  if (score >= 80) return '#e6a23c'
  if (score >= 60) return '#f56c6c'
  return '#909399'
}

// 搜索
const handleSearch = (tabName: string) => {
  if (tabName === 'objective') {
    pagination.value.objective.currentPage = 1
    // 重新获取数据
    fetchObjectives()
  } else if (tabName === 'appraisal') {
    pagination.value.appraisal.currentPage = 1
    // 重新获取数据
    fetchAppraisals()
  }
}

// 重置
const handleReset = (tabName: string) => {
  if (tabName === 'objective') {
    searchForm.value.objective = {
      employeeName: '',
      status: ''
    }
    pagination.value.objective.currentPage = 1
    fetchObjectives()
  } else if (tabName === 'appraisal') {
    searchForm.value.appraisal = {
      employeeName: '',
      appraisalPeriod: '',
      status: ''
    }
    pagination.value.appraisal.currentPage = 1
    fetchAppraisals()
  }
}

// 分页大小变化处理
const handleSizeChange = (size: number) => {
  if (activeTab.value === 'objectives') {
    pagination.value.objective.pageSize = size
    fetchObjectives()
  } else if (activeTab.value === 'appraisals') {
    pagination.value.appraisal.pageSize = size
    fetchAppraisals()
  }
}

// 页码变化处理
const handleCurrentChange = (current: number) => {
  if (activeTab.value === 'objectives') {
    pagination.value.objective.currentPage = current
    fetchObjectives()
  } else if (activeTab.value === 'appraisals') {
    pagination.value.appraisal.currentPage = current
    fetchAppraisals()
  }
}

// 新增绩效目标
const handleAddObjective = () => {
  isEditObjective.value = false
  objectiveForm.value = {
    employeeId: employees.value[0]?.id || 0,
    objectiveContent: '',
    targetValue: '',
    weight: 0,
    startDate: '',
    endDate: '',
    status: 0
  }
  dialogVisible.value.objective = true
}

// 编辑绩效目标
const handleEditObjective = (row: PerformanceObjective) => {
  isEditObjective.value = true
  objectiveForm.value = { ...row }
  dialogVisible.value.objective = true
}

// 删除绩效目标
const handleDeleteObjective = (row: PerformanceObjective) => {
  ElMessageBox.confirm('确认删除该绩效目标吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    ElMessage.success('删除成功')
    fetchObjectives()
  })
}

// 保存绩效目标
const handleSaveObjective = async () => {
  if (!objectiveFormRef.value) return
  await objectiveFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await hrApi.performance.createObjective(objectiveForm.value)
        ElMessage.success('保存成功')
        dialogVisible.value.objective = false
        fetchObjectives()
      } catch (error) {
        console.error('保存绩效目标失败:', error)
        ElMessage.error('保存绩效目标失败')
      }
    }
  })
}

// 新增绩效评估
const handleAddAppraisal = () => {
  isEditAppraisal.value = false
  appraisalForm.value = {
    employeeId: employees.value[0]?.id || 0,
    appraisalPeriod: '',
    objectiveScore: 0,
    competencyScore: 0,
    totalScore: 0,
    appraisalStatus: 0,
    appraiserId: employees.value[0]?.id || 0,
    appraisalNotes: ''
  }
  dialogVisible.value.appraisal = true
}

// 编辑绩效评估
const handleEditAppraisal = (row: PerformanceAppraisal) => {
  isEditAppraisal.value = true
  appraisalForm.value = { ...row }
  dialogVisible.value.appraisal = true
}

// 删除绩效评估
const handleDeleteAppraisal = (row: PerformanceAppraisal) => {
  ElMessageBox.confirm('确认删除该绩效评估吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    ElMessage.success('删除成功')
    fetchAppraisals()
  })
}

// 查看绩效评估
const handleViewAppraisal = (row: PerformanceAppraisal) => {
  ElMessage.info(`查看绩效评估: ${row.appraisalPeriod}`)
}

// 审批绩效评估
const handleApproveAppraisal = (row: PerformanceAppraisal) => {
  ElMessage.success('绩效评估已审批')
}

// 保存绩效评估
const handleSaveAppraisal = async () => {
  if (!appraisalFormRef.value) return
  await appraisalFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await hrApi.performance.createAppraisal(appraisalForm.value)
        ElMessage.success('保存成功')
        dialogVisible.value.appraisal = false
        fetchAppraisals()
      } catch (error) {
        console.error('保存绩效评估失败:', error)
        ElMessage.error('保存绩效评估失败')
      }
    }
  })
}

// 刷新数据
const fetchData = async () => {
  await initData()
}

onMounted(() => {
  initData()
})
</script>

<style scoped>
.performance-management-view {
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
  padding: 10px 0;
}
</style>