<template>
  <div class="training-management-view">
    <div class="toolbar">
      <el-button type="primary" @click="handleAddTraining">新增培训计划</el-button>
      <el-button type="primary" @click="handleAddParticipant">批量添加参与者</el-button>
      <el-button @click="fetchData">刷新</el-button>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 培训计划管理 -->
      <el-tab-pane label="培训计划管理" name="training-plans">
        <div class="search-form">
          <el-form :model="searchForm.plan" inline label-width="80px">
            <el-form-item label="培训名称">
              <el-input
                v-model="searchForm.plan.trainingName"
                placeholder="请输入培训名称"
                clearable
                style="width: 200px"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select
                v-model="searchForm.plan.status"
                placeholder="全部"
                clearable
                style="width: 120px"
              >
                <el-option label="计划中" :value="0" />
                <el-option label="进行中" :value="1" />
                <el-option label="已完成" :value="2" />
                <el-option label="已取消" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearchPlan">查询</el-button>
              <el-button @click="handleResetPlan">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table v-loading="loading" :data="filteredPlans" border stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="trainingName" label="培训名称" width="200" />
          <el-table-column prop="trainer" label="培训师" width="120" />
          <el-table-column prop="startDate" label="开始日期" width="120" />
          <el-table-column prop="endDate" label="结束日期" width="120" />
          <el-table-column prop="location" label="培训地点" width="150" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleViewParticipants(scope.row)">查看参与者</el-button>
              <el-button link type="warning" size="small" @click="handleEditTraining(scope.row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDeleteTraining(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 培训参与管理 -->
      <el-tab-pane label="培训参与管理" name="participants">
        <div class="search-form">
          <el-form :model="searchForm.participant" inline label-width="80px">
            <el-form-item label="员工姓名">
              <el-input
                v-model="searchForm.participant.employeeName"
                placeholder="请输入员工姓名"
                clearable
                style="width: 150px"
              />
            </el-form-item>
            <el-form-item label="参与状态">
              <el-select
                v-model="searchForm.participant.attendanceStatus"
                placeholder="全部"
                clearable
                style="width: 120px"
              >
                <el-option label="已报名" :value="0" />
                <el-option label="已参加" :value="1" />
                <el-option label="未参加" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="filterTrainingId !== null">
              <el-tag closable type="primary" @close="clearTrainingFilter">
                培训：{{ getTrainingName(filterTrainingId) }}
              </el-tag>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearchParticipant">查询</el-button>
              <el-button @click="handleResetParticipant">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table v-loading="loading" :data="filteredParticipants" border stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="培训名称" width="180">
            <template #default="scope">{{ getTrainingName(scope.row.trainingId) }}</template>
          </el-table-column>
          <el-table-column label="员工姓名" width="120">
            <template #default="scope">{{ getEmployeeName(scope.row.employeeId) }}</template>
          </el-table-column>
          <el-table-column prop="attendanceStatus" label="参与状态" width="100">
            <template #default="scope">
              <el-tag :type="getAttendanceType(scope.row.attendanceStatus)">
                {{ getAttendanceText(scope.row.attendanceStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="completionStatus" label="完成状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.completionStatus === 1 ? 'success' : 'warning'">
                {{ scope.row.completionStatus === 1 ? '已完成' : '未完成' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="成绩" width="100">
            <template #default="scope">{{ scope.row.score ?? '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button link type="warning" size="small" @click="handleEditParticipant(scope.row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDeleteParticipant(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 培训计划新增/编辑弹窗 -->
    <el-dialog
      :title="isEditTraining ? '编辑培训计划' : '新增培训计划'"
      v-model="trainingDialogVisible"
      width="600px"
    >
      <el-form :model="trainingForm" label-width="100px" :rules="trainingRules" ref="trainingFormRef">
        <el-form-item label="培训名称" prop="trainingName">
          <el-input v-model="trainingForm.trainingName" placeholder="请输入培训名称" />
        </el-form-item>
        <el-form-item label="培训师" prop="trainer">
          <el-input v-model="trainingForm.trainer" placeholder="请输入培训师" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="trainingForm.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择开始日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="trainingForm.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="培训地点" prop="location">
          <el-input v-model="trainingForm.location" placeholder="请输入培训地点" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="trainingForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="计划中" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="培训描述" prop="description">
          <el-input v-model="trainingForm.description" type="textarea" :rows="3" placeholder="请输入培训描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="trainingDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveTraining">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量添加参与者弹窗 -->
    <el-dialog title="批量添加参与者" v-model="batchDialogVisible" width="500px">
      <el-form :model="batchForm" label-width="100px" :rules="batchRules" ref="batchFormRef">
        <el-form-item label="培训计划" prop="trainingId">
          <el-select v-model="batchForm.trainingId" placeholder="请选择培训计划" style="width: 100%">
            <el-option
              v-for="plan in plans"
              :key="plan.id"
              :label="plan.trainingName"
              :value="plan.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择员工" prop="employeeIds">
          <el-select
            v-model="batchForm.employeeIds"
            multiple
            collapse-tags
            placeholder="请选择员工"
            style="width: 100%"
          >
            <el-option
              v-for="emp in employees"
              :key="emp.id"
              :label="emp.name"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveBatch">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑参与者弹窗 -->
    <el-dialog title="编辑参与者" v-model="participantDialogVisible" width="500px">
      <el-form :model="participantForm" label-width="100px">
        <el-form-item label="参与状态">
          <el-select v-model="participantForm.attendanceStatus" placeholder="请选择参与状态" style="width: 100%">
            <el-option label="已报名" :value="0" />
            <el-option label="已参加" :value="1" />
            <el-option label="未参加" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="完成状态">
          <el-select v-model="participantForm.completionStatus" placeholder="请选择完成状态" style="width: 100%">
            <el-option label="未完成" :value="0" />
            <el-option label="已完成" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="成绩">
          <el-input-number v-model="participantForm.score" :min="0" :max="100" placeholder="请输入成绩" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="participantDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveParticipant">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { hrApi } from '../../api/hr'
import type { TrainingPlan, TrainingParticipant, Employee } from '../../api/hr'

// 加载状态
const loading = ref(false)
// 当前激活的Tab页
const activeTab = ref('training-plans')

// 培训计划列表
const plans = ref<TrainingPlan[]>([])
// 培训参与者列表
const participants = ref<TrainingParticipant[]>([])
// 员工列表（用于姓名映射与批量添加下拉）
const employees = ref<Employee[]>([])

// 参与者列表按指定培训过滤的ID（点击"查看参与者"时设置）
const filterTrainingId = ref<number | null>(null)

// 搜索表单绑定值
const searchForm = ref({
  plan: {
    trainingName: '',
    status: null as number | null
  },
  participant: {
    employeeName: '',
    attendanceStatus: null as number | null
  }
})

// 实际生效的查询条件（点击"查询"按钮后更新）
const planQuery = ref({ trainingName: '', status: null as number | null })
const participantQuery = ref({ employeeName: '', attendanceStatus: null as number | null })

// 培训计划弹窗可见性
const trainingDialogVisible = ref(false)
// 培训计划弹窗是否为编辑模式
const isEditTraining = ref(false)
// 当前编辑的培训计划ID
const editingTrainingId = ref<number | null>(null)
// 培训计划表单引用
const trainingFormRef = ref()
// 培训计划表单数据
const trainingForm = ref<TrainingPlan>({
  trainingName: '',
  trainer: '',
  startDate: '',
  endDate: '',
  location: '',
  status: 0,
  description: ''
})

// 批量添加参与者弹窗可见性
const batchDialogVisible = ref(false)
// 批量添加表单引用
const batchFormRef = ref()
// 批量添加表单数据
const batchForm = ref({
  trainingId: null as number | null,
  employeeIds: [] as number[]
})

// 编辑参与者弹窗可见性
const participantDialogVisible = ref(false)
// 当前编辑的参与者ID
const editingParticipantId = ref<number | null>(null)
// 参与者编辑表单数据
const participantForm = ref({
  attendanceStatus: 0,
  completionStatus: 0,
  score: undefined as number | undefined
})

// 培训计划表单校验规则
const trainingRules = {
  trainingName: [{ required: true, message: '请输入培训名称', trigger: 'blur' }],
  trainer: [{ required: true, message: '请输入培训师', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  location: [{ required: true, message: '请输入培训地点', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 批量添加表单校验规则
const batchRules = {
  trainingId: [{ required: true, message: '请选择培训计划', trigger: 'change' }],
  employeeIds: [{ required: true, type: 'array', min: 1, message: '请选择至少一名员工', trigger: 'change' }]
}

/**
 * 根据培训ID获取培训名称
 * @param trainingId 培训计划ID
 */
const getTrainingName = (trainingId: number | null) => {
  if (trainingId === null || trainingId === undefined) return '-'
  const plan = plans.value.find(item => item.id === trainingId)
  return plan ? plan.trainingName : '-'
}

/**
 * 根据员工ID获取员工姓名
 * @param employeeId 员工ID
 */
const getEmployeeName = (employeeId: number) => {
  const emp = employees.value.find(item => item.id === employeeId)
  return emp ? emp.name : '-'
}

/**
 * 过滤后的培训计划列表（按查询条件前端过滤）
 */
const filteredPlans = computed(() => {
  return plans.value.filter(item => {
    const nameMatch = !planQuery.value.trainingName || (item.trainingName ?? '').includes(planQuery.value.trainingName)
    const statusMatch = planQuery.value.status === null || item.status === planQuery.value.status
    return nameMatch && statusMatch
  })
})

/**
 * 过滤后的参与者列表（员工姓名前端模糊过滤 + 参与状态 + 指定培训过滤）
 */
const filteredParticipants = computed(() => {
  return participants.value.filter(item => {
    const employeeName = getEmployeeName(item.employeeId)
    const nameMatch = !participantQuery.value.employeeName || employeeName.includes(participantQuery.value.employeeName)
    const statusMatch = participantQuery.value.attendanceStatus === null || item.attendanceStatus === participantQuery.value.attendanceStatus
    const trainingMatch = filterTrainingId.value === null || item.trainingId === filterTrainingId.value
    return nameMatch && statusMatch && trainingMatch
  })
})

/**
 * 获取培训状态对应的标签类型
 * @param status 培训状态（0计划中/1进行中/2已完成/3已取消）
 */
const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取培训状态文本
 * @param status 培训状态（0计划中/1进行中/2已完成/3已取消）
 */
const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '计划中',
    1: '进行中',
    2: '已完成',
    3: '已取消'
  }
  return textMap[status] || '未知'
}

/**
 * 获取参与状态对应的标签类型
 * @param status 参与状态（0已报名/1已参加/2未参加）
 */
const getAttendanceType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取参与状态文本
 * @param status 参与状态（0已报名/1已参加/2未参加）
 */
const getAttendanceText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '已报名',
    1: '已参加',
    2: '未参加'
  }
  return textMap[status] || '未知'
}

/**
 * 加载培训计划列表
 */
const fetchPlans = async () => {
  try {
    const res = await hrApi.training.plan.getByPage({ page: 1, size: 100 })
    if (res && res.data) {
      plans.value = Array.isArray(res.data) ? res.data : (res.data.records ?? res.data.list ?? [])
    }
  } catch (error) {
    console.error('获取培训计划失败:', error)
    ElMessage.error('获取培训计划失败')
    plans.value = []
  }
}

/**
 * 加载培训参与者列表
 */
const fetchParticipants = async () => {
  try {
    const res = await hrApi.training.participant.getByPage({ page: 1, size: 100 })
    if (res && res.data) {
      participants.value = Array.isArray(res.data) ? res.data : (res.data.records ?? res.data.list ?? [])
    }
  } catch (error) {
    console.error('获取培训参与者失败:', error)
    ElMessage.error('获取培训参与者失败')
    participants.value = []
  }
}

/**
 * 加载员工列表（用于姓名映射与批量添加下拉）
 */
const fetchEmployees = async () => {
  try {
    const res = await hrApi.employee.getAll()
    if (res && res.data) {
      employees.value = Array.isArray(res.data) ? res.data : (res.data.records ?? res.data.list ?? [])
    }
  } catch (error) {
    console.error('获取员工列表失败:', error)
    ElMessage.error('获取员工列表失败')
    employees.value = []
  }
}

/**
 * 刷新培训计划与参与者数据
 */
const fetchData = async () => {
  loading.value = true
  try {
    await Promise.all([fetchPlans(), fetchParticipants()])
  } finally {
    loading.value = false
  }
}

/**
 * 培训计划查询（应用搜索条件）
 */
const handleSearchPlan = () => {
  planQuery.value = { ...searchForm.value.plan }
}

/**
 * 培训计划重置（清空搜索条件）
 */
const handleResetPlan = () => {
  searchForm.value.plan = { trainingName: '', status: null }
  planQuery.value = { trainingName: '', status: null }
}

/**
 * 参与者查询（应用搜索条件）
 */
const handleSearchParticipant = () => {
  participantQuery.value = { ...searchForm.value.participant }
}

/**
 * 参与者重置（清空搜索条件与培训过滤）
 */
const handleResetParticipant = () => {
  searchForm.value.participant = { employeeName: '', attendanceStatus: null }
  participantQuery.value = { employeeName: '', attendanceStatus: null }
  filterTrainingId.value = null
}

/**
 * 打开新增培训计划弹窗
 */
const handleAddTraining = () => {
  isEditTraining.value = false
  editingTrainingId.value = null
  trainingForm.value = {
    trainingName: '',
    trainer: '',
    startDate: '',
    endDate: '',
    location: '',
    status: 0,
    description: ''
  }
  trainingDialogVisible.value = true
}

/**
 * 打开编辑培训计划弹窗
 * @param row 当前培训计划行数据
 */
const handleEditTraining = (row: TrainingPlan) => {
  isEditTraining.value = true
  editingTrainingId.value = row.id ?? null
  trainingForm.value = { ...row }
  trainingDialogVisible.value = true
}

/**
 * 保存培训计划（新增或更新）
 */
const handleSaveTraining = async () => {
  if (!trainingFormRef.value) return
  await trainingFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      if (isEditTraining.value && editingTrainingId.value !== null) {
        await hrApi.training.plan.update(editingTrainingId.value, trainingForm.value)
        ElMessage.success('更新成功')
      } else {
        await hrApi.training.plan.create(trainingForm.value)
        ElMessage.success('创建成功')
      }
      trainingDialogVisible.value = false
      await fetchPlans()
    } catch (error) {
      console.error('保存培训计划失败:', error)
      ElMessage.error('保存培训计划失败')
    }
  })
}

/**
 * 删除培训计划
 * @param row 当前培训计划行数据
 */
const handleDeleteTraining = (row: TrainingPlan) => {
  ElMessageBox.confirm(`确认删除培训计划"${row.trainingName}"吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await hrApi.training.plan.delete(row.id!)
      ElMessage.success('删除成功')
      await fetchPlans()
    } catch (error) {
      console.error('删除培训计划失败:', error)
      ElMessage.error('删除培训计划失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

/**
 * 查看参与者：切换到参与管理Tab并按该培训过滤
 * @param row 当前培训计划行数据
 */
const handleViewParticipants = (row: TrainingPlan) => {
  filterTrainingId.value = row.id ?? null
  activeTab.value = 'participants'
}

/**
 * 清除参与者列表的培训过滤
 */
const clearTrainingFilter = () => {
  filterTrainingId.value = null
}

/**
 * 打开批量添加参与者弹窗
 */
const handleAddParticipant = () => {
  batchForm.value = {
    trainingId: filterTrainingId.value,
    employeeIds: []
  }
  batchDialogVisible.value = true
}

/**
 * 提交批量添加参与者（对每个选中员工调用创建接口）
 */
const handleSaveBatch = async () => {
  if (!batchFormRef.value) return
  await batchFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    const trainingId = batchForm.value.trainingId
    if (trainingId === null) return
    try {
      await Promise.all(
        batchForm.value.employeeIds.map(employeeId =>
          hrApi.training.participant.create({
            trainingId,
            employeeId,
            attendanceStatus: 0,
            completionStatus: 0
          })
        )
      )
      ElMessage.success(`成功添加 ${batchForm.value.employeeIds.length} 名参与者`)
      batchDialogVisible.value = false
      await fetchParticipants()
    } catch (error) {
      console.error('批量添加参与者失败:', error)
      ElMessage.error('批量添加参与者失败')
    }
  })
}

/**
 * 打开编辑参与者弹窗
 * @param row 当前参与者行数据
 */
const handleEditParticipant = (row: TrainingParticipant) => {
  editingParticipantId.value = row.id ?? null
  participantForm.value = {
    attendanceStatus: row.attendanceStatus,
    completionStatus: row.completionStatus,
    score: row.score
  }
  participantDialogVisible.value = true
}

/**
 * 保存参与者编辑（参与状态/完成状态/成绩）
 */
const handleSaveParticipant = async () => {
  if (editingParticipantId.value === null) return
  try {
    await hrApi.training.participant.update(editingParticipantId.value, { ...participantForm.value })
    ElMessage.success('更新成功')
    participantDialogVisible.value = false
    await fetchParticipants()
  } catch (error) {
    console.error('更新参与者失败:', error)
    ElMessage.error('更新参与者失败')
  }
}

/**
 * 删除参与者
 * @param row 当前参与者行数据
 */
const handleDeleteParticipant = (row: TrainingParticipant) => {
  ElMessageBox.confirm('确认删除该参与者吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await hrApi.training.participant.delete(row.id!)
      ElMessage.success('删除成功')
      await fetchParticipants()
    } catch (error) {
      console.error('删除参与者失败:', error)
      ElMessage.error('删除参与者失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 组件挂载时加载员工、培训计划与参与者数据
onMounted(async () => {
  await fetchEmployees()
  await fetchData()
})
</script>

<style scoped>
.training-management-view {
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
.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
