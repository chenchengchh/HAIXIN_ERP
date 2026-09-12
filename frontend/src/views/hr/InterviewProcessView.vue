<template>
  <div class="interview-process-view">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 面试安排 -->
      <el-tab-pane label="面试安排" name="schedule">
        <div class="toolbar">
          <el-button type="primary" @click="handleNewInterview">安排面试</el-button>
          <el-button @click="fetchAllData">刷新</el-button>
        </div>
        <el-table v-loading="loading" :data="interviewList" border stripe>
          <el-table-column label="候选人" width="120">
            <template #default="scope">{{ getCandidateName(scope.row) }}</template>
          </el-table-column>
          <el-table-column label="应聘岗位" width="160">
            <template #default="scope">{{ getPositionNameByResume(scope.row.resumeId) }}</template>
          </el-table-column>
          <el-table-column label="面试类型" width="110">
            <template #default="scope">{{ getInterviewTypeText(scope.row.interviewType) }}</template>
          </el-table-column>
          <el-table-column label="面试时间" width="160">
            <template #default="scope">{{ formatDateTime(scope.row.interviewTime) }}</template>
          </el-table-column>
          <el-table-column label="面试官" width="120">
            <template #default="scope">{{ getInterviewerName(scope.row) }}</template>
          </el-table-column>
          <el-table-column label="面试结果" width="100">
            <template #default="scope">
              <el-tag :type="getResultTagType(scope.row.interviewResult)">
                {{ getResultText(scope.row.interviewResult) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleOpenResultDialog(scope.row)">录入结果</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 面试评价 -->
      <el-tab-pane label="面试评价" name="evaluation">
        <el-table v-loading="loading" :data="interviewList" border stripe>
          <el-table-column label="候选人" width="120">
            <template #default="scope">{{ getCandidateName(scope.row) }}</template>
          </el-table-column>
          <el-table-column label="岗位" width="160">
            <template #default="scope">{{ getPositionNameByResume(scope.row.resumeId) }}</template>
          </el-table-column>
          <el-table-column label="面试类型" width="110">
            <template #default="scope">{{ getInterviewTypeText(scope.row.interviewType) }}</template>
          </el-table-column>
          <el-table-column label="面试官" width="120">
            <template #default="scope">{{ getInterviewerName(scope.row) }}</template>
          </el-table-column>
          <el-table-column label="面试时间" width="160">
            <template #default="scope">{{ formatDateTime(scope.row.interviewTime) }}</template>
          </el-table-column>
          <el-table-column label="面试结果" width="100">
            <template #default="scope">
              <el-tag :type="getResultTagType(scope.row.interviewResult)">
                {{ getResultText(scope.row.interviewResult) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleOpenResultDialog(scope.row)">录入结果</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 安排面试弹窗 -->
    <el-dialog v-model="interviewDialogVisible" title="安排面试" width="500px">
      <el-form ref="interviewFormRef" :model="interviewForm" :rules="interviewFormRules" label-width="100px">
        <el-form-item label="候选人" prop="resumeId">
          <el-select v-model="interviewForm.resumeId" placeholder="请选择候选人" style="width: 100%">
            <el-option
              v-for="resume in screeningResumes"
              :key="resume.id"
              :label="`${resume.candidateName} - ${getPositionName(resume)}`"
              :value="resume.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="面试官" prop="interviewerId">
          <el-select v-model="interviewForm.interviewerId" placeholder="请选择面试官" style="width: 100%">
            <el-option
              v-for="emp in employeeList"
              :key="emp.id"
              :label="emp.name"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="面试时间" prop="interviewTime">
          <el-date-picker
            v-model="interviewForm.interviewTime"
            type="datetime"
            placeholder="请选择面试时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="面试类型" prop="interviewType">
          <el-select v-model="interviewForm.interviewType" placeholder="请选择面试类型" style="width: 100%">
            <el-option label="技术面试" value="TECHNICAL" />
            <el-option label="管理面试" value="MANAGERIAL" />
            <el-option label="HR面试" value="HR" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="interviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitInterview">提交</el-button>
      </template>
    </el-dialog>

    <!-- 录入面试结果弹窗 -->
    <el-dialog v-model="resultDialogVisible" title="录入面试结果" width="420px">
      <div v-if="currentInterview" class="result-dialog-content">
        <p class="result-candidate">
          候选人：{{ getCandidateName(currentInterview) }}（{{ getInterviewTypeText(currentInterview.interviewType) }}）
        </p>
        <el-radio-group v-model="resultForm.interviewResult">
          <el-radio value="PASS">通过</el-radio>
          <el-radio value="FAIL">未通过</el-radio>
          <el-radio value="OFFER">发Offer</el-radio>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button @click="resultDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitResult">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { unwrapListResponse } from '../../api'
import { hrApi } from '../../api/hr'
import type { Resume, Interview, Position, Employee } from '../../api/hr'

// 简历行数据类型：扩展后端返回中可能携带的嵌套职位对象与createdTime字段
type ResumeRow = Resume & { position?: Position; createdTime?: string }
// 面试行数据类型：扩展后端返回的createdTime字段
type InterviewRow = Interview & { createdTime?: string }

// 当前激活的Tab页
const activeTab = ref('schedule')
// 列表加载状态
const loading = ref(false)
// 表单提交中状态
const submitting = ref(false)

// 面试列表数据
const interviewList = ref<InterviewRow[]>([])
// 简历列表数据（用于候选人姓名与岗位映射）
const resumeList = ref<ResumeRow[]>([])
// 职位列表数据（用于岗位名称映射）
const positionList = ref<Position[]>([])
// 员工列表数据（用于面试官选择与姓名映射）
const employeeList = ref<Employee[]>([])

// 安排面试弹窗可见性
const interviewDialogVisible = ref(false)
// 安排面试表单引用
const interviewFormRef = ref()
// 安排面试表单数据
const interviewForm = ref({
  resumeId: undefined as number | undefined,
  interviewerId: undefined as number | undefined,
  interviewTime: '',
  interviewType: 'TECHNICAL'
})
// 安排面试表单校验规则
const interviewFormRules = {
  resumeId: [{ required: true, message: '请选择候选人', trigger: 'change' }],
  interviewerId: [{ required: true, message: '请选择面试官', trigger: 'change' }],
  interviewTime: [{ required: true, message: '请选择面试时间', trigger: 'change' }],
  interviewType: [{ required: true, message: '请选择面试类型', trigger: 'change' }]
}

// 录入结果弹窗可见性
const resultDialogVisible = ref(false)
// 当前录入结果的面试记录
const currentInterview = ref<InterviewRow | null>(null)
// 录入结果表单数据
const resultForm = ref({
  interviewResult: 'PASS'
})

/**
 * 职位ID到职位名称的映射表
 */
const positionMap = computed(() => {
  const map: Record<number, string> = {}
  positionList.value.forEach(pos => {
    if (pos.id) {
      map[pos.id] = pos.name
    }
  })
  return map
})

/**
 * 简历ID到简历对象的映射表
 */
const resumeMap = computed(() => {
  const map: Record<number, ResumeRow> = {}
  resumeList.value.forEach(resume => {
    if (resume.id) {
      map[resume.id] = resume
    }
  })
  return map
})

/**
 * 员工ID到员工对象的映射表
 */
const employeeMap = computed(() => {
  const map: Record<number, Employee> = {}
  employeeList.value.forEach(emp => {
    if (emp.id) {
      map[emp.id] = emp
    }
  })
  return map
})

/**
 * 可安排面试的候选人列表（仅筛选中状态的简历）
 */
const screeningResumes = computed(() => {
  return resumeList.value.filter(resume => resume.status === 'SCREENING')
})

/**
 * 获取简历的应聘岗位名称（优先取嵌套position对象，其次按positionId映射）
 * @param row 简历行数据
 * @returns 岗位名称
 */
const getPositionName = (row: ResumeRow): string => {
  return row.position?.name ?? positionMap.value[row.positionId] ?? '未知岗位'
}

/**
 * 按简历ID获取应聘岗位名称
 * @param resumeId 简历ID
 * @returns 岗位名称
 */
const getPositionNameByResume = (resumeId: number): string => {
  const resume = resumeMap.value[resumeId]
  return resume ? getPositionName(resume) : '未知岗位'
}

/**
 * 获取面试记录对应的候选人姓名（按resumeId在简历列表中映射）
 * @param row 面试行数据
 * @returns 候选人姓名
 */
const getCandidateName = (row: InterviewRow): string => {
  return resumeMap.value[row.resumeId]?.candidateName ?? '未知候选人'
}

/**
 * 获取面试官姓名（按interviewerId在员工列表中映射）
 * @param row 面试行数据
 * @returns 面试官姓名
 */
const getInterviewerName = (row: InterviewRow): string => {
  return employeeMap.value[row.interviewerId]?.name ?? '未知面试官'
}

/**
 * 格式化日期时间（兼容ISO字符串与Java LocalDateTime对象格式）
 * @param value 原始时间值
 * @returns 格式化后的 YYYY-MM-DD HH:mm 字符串
 */
const formatDateTime = (value: any): string => {
  if (!value) return '-'
  if (typeof value === 'object' && value !== null && 'year' in value) {
    // 处理Java LocalDateTime对象格式：{year, month, day, hour, minute, second}
    const pad = (n: number) => String(n).padStart(2, '0')
    return `${value.year}-${pad(value.month)}-${pad(value.day)} ${pad(value.hour ?? 0)}:${pad(value.minute ?? 0)}`
  }
  const str = String(value)
  if (str.includes('T')) {
    return str.replace('T', ' ').substring(0, 16)
  }
  return str.substring(0, 16)
}

/**
 * 获取面试类型对应的中文文本
 * @param type 面试类型字符串枚举
 * @returns 中文类型文本
 */
const getInterviewTypeText = (type: string): string => {
  const textMap: Record<string, string> = {
    TECHNICAL: '技术面试',
    MANAGERIAL: '管理面试',
    HR: 'HR面试'
  }
  return textMap[type] || '未知'
}

/**
 * 获取面试结果对应的标签颜色类型
 * @param result 面试结果字符串枚举
 * @returns el-tag类型
 */
const getResultTagType = (result: string): string => {
  const typeMap: Record<string, string> = {
    WAITING: 'info',
    PASS: 'success',
    FAIL: 'danger',
    OFFER: 'warning'
  }
  return typeMap[result] || 'info'
}

/**
 * 获取面试结果对应的中文文本
 * @param result 面试结果字符串枚举
 * @returns 中文结果文本
 */
const getResultText = (result: string): string => {
  const textMap: Record<string, string> = {
    WAITING: '待结果',
    PASS: '通过',
    FAIL: '未通过',
    OFFER: '发Offer'
  }
  return textMap[result] || '未知'
}

/**
 * 加载面试列表数据
 */
const fetchInterviews = async () => {
  try {
    const res = await hrApi.recruitment.getInterviews()
    interviewList.value = unwrapListResponse<InterviewRow>(res)
  } catch (error) {
    console.error('获取面试列表失败:', error)
    ElMessage.error('获取面试列表失败')
    interviewList.value = []
  }
}

/**
 * 加载简历列表数据（用于候选人姓名与岗位映射）
 */
const fetchResumes = async () => {
  try {
    const res = await hrApi.recruitment.getResumes()
    resumeList.value = unwrapListResponse<ResumeRow>(res)
  } catch (error) {
    console.error('获取简历列表失败:', error)
    ElMessage.error('获取简历列表失败')
    resumeList.value = []
  }
}

/**
 * 加载职位列表数据（用于岗位名称映射）
 */
const fetchPositions = async () => {
  try {
    const res = await hrApi.position.getAll()
    positionList.value = unwrapListResponse<Position>(res)
  } catch (error) {
    console.error('获取职位列表失败:', error)
    ElMessage.error('获取职位列表失败')
    positionList.value = []
  }
}

/**
 * 加载员工列表数据（用于面试官选择与姓名映射）
 */
const fetchEmployees = async () => {
  try {
    const res = await hrApi.employee.getAll()
    employeeList.value = unwrapListResponse<Employee>(res)
  } catch (error) {
    console.error('获取员工列表失败:', error)
    ElMessage.error('获取员工列表失败')
    employeeList.value = []
  }
}

/**
 * 加载页面所需的全部数据（面试、简历、职位、员工）
 */
const fetchAllData = async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchInterviews(),
      fetchResumes(),
      fetchPositions(),
      fetchEmployees()
    ])
  } finally {
    loading.value = false
  }
}

/**
 * 打开安排面试弹窗并重置表单
 */
const handleNewInterview = () => {
  interviewForm.value = {
    resumeId: undefined,
    interviewerId: undefined,
    interviewTime: '',
    interviewType: 'TECHNICAL'
  }
  interviewDialogVisible.value = true
}

/**
 * 提交安排面试：创建面试记录（初始结果为待结果），并同步将简历状态更新为面试中
 */
const handleSubmitInterview = () => {
  if (!interviewFormRef.value) return
  interviewFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitting.value = true
    try {
      await hrApi.recruitment.createInterview({
        resumeId: interviewForm.value.resumeId!,
        interviewerId: interviewForm.value.interviewerId!,
        interviewTime: interviewForm.value.interviewTime,
        interviewType: interviewForm.value.interviewType,
        interviewResult: 'WAITING'
      })
      // 同步更新简历状态为面试中
      await hrApi.recruitment.updateResume(interviewForm.value.resumeId!, { status: 'INTERVIEW' })
      interviewDialogVisible.value = false
      ElMessage.success('面试安排成功')
      // 刷新面试与简历两个列表
      await Promise.all([fetchInterviews(), fetchResumes()])
    } catch (error) {
      console.error('安排面试失败:', error)
      ElMessage.error('安排面试失败')
    } finally {
      submitting.value = false
    }
  })
}

/**
 * 打开录入面试结果弹窗
 * @param row 面试行数据
 */
const handleOpenResultDialog = (row: InterviewRow) => {
  currentInterview.value = row
  // 默认选中当前已有的结果（待结果时默认选中通过）
  resultForm.value.interviewResult = row.interviewResult && row.interviewResult !== 'WAITING' ? row.interviewResult : 'PASS'
  resultDialogVisible.value = true
}

/**
 * 提交面试结果：更新面试记录，并按结果联动更新简历状态（发Offer→待录用，未通过→已淘汰）
 */
const handleSubmitResult = async () => {
  if (!currentInterview.value) return
  submitting.value = true
  try {
    const result = resultForm.value.interviewResult
    await hrApi.recruitment.updateInterview(currentInterview.value.id!, { interviewResult: result })
    // 按面试结果联动更新简历状态
    if (result === 'OFFER') {
      await hrApi.recruitment.updateResume(currentInterview.value.resumeId, { status: 'OFFER' })
    } else if (result === 'FAIL') {
      await hrApi.recruitment.updateResume(currentInterview.value.resumeId, { status: 'REJECTED' })
    }
    resultDialogVisible.value = false
    ElMessage.success('面试结果录入成功')
    // 刷新面试与简历两个列表
    await Promise.all([fetchInterviews(), fetchResumes()])
  } catch (error) {
    console.error('录入面试结果失败:', error)
    ElMessage.error('录入面试结果失败')
  } finally {
    submitting.value = false
  }
}

// 组件挂载时加载全部基础数据
onMounted(async () => {
  await fetchAllData()
})
</script>

<style scoped>
.interview-process-view {
  padding: 20px;
}
.toolbar {
  margin-bottom: 20px;
}
.result-dialog-content {
  padding: 0 10px;
}
.result-candidate {
  margin-bottom: 16px;
  font-weight: bold;
}
</style>
