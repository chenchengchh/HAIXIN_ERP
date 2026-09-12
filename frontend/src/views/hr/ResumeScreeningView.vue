<template>
  <div class="resume-screening-view">
    <!-- 顶部筛选工具栏 -->
    <div class="toolbar">
      <el-form inline>
        <el-form-item label="应聘岗位">
          <el-select v-model="filterPositionId" placeholder="全部" clearable style="width: 180px">
            <el-option
              v-for="pos in positionList"
              :key="pos.id"
              :label="pos.name"
              :value="pos.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable style="width: 140px">
            <el-option label="筛选中" value="SCREENING" />
            <el-option label="面试中" value="INTERVIEW" />
            <el-option label="待录用" value="OFFER" />
            <el-option label="已录用" value="HIRED" />
            <el-option label="已淘汰" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAddResume">新增简历</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 简历列表表格 -->
    <el-table v-loading="loading" :data="filteredResumeList" border stripe>
      <el-table-column prop="candidateName" label="候选人姓名" width="120" />
      <el-table-column prop="gender" label="性别" width="80" />
      <el-table-column label="应聘岗位" width="160">
        <template #default="scope">{{ getPositionName(scope.row) }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="email" label="邮箱" width="200" />
      <el-table-column label="投递时间" width="160">
        <template #default="scope">{{ formatDateTime(scope.row.createdTime ?? scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="handleViewResume(scope.row)">查看简历</el-button>
          <el-button
            v-if="scope.row.status === 'SCREENING'"
            link
            type="success"
            size="small"
            @click="handleEnterInterview(scope.row)"
          >
            进入面试
          </el-button>
          <el-button
            v-if="scope.row.status === 'SCREENING'"
            link
            type="danger"
            size="small"
            @click="handleReject(scope.row)"
          >
            淘汰
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 查看简历弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="简历详情" width="500px">
      <el-descriptions v-if="currentResume" :column="1" border>
        <el-descriptions-item label="候选人姓名">{{ currentResume.candidateName }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ currentResume.gender }}</el-descriptions-item>
        <el-descriptions-item label="应聘岗位">{{ getPositionName(currentResume) }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentResume.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentResume.email }}</el-descriptions-item>
        <el-descriptions-item label="投递时间">{{ formatDateTime(currentResume.createdTime ?? currentResume.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusType(currentResume.status)">
            {{ getStatusText(currentResume.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="简历链接">
          <el-link v-if="currentResume.resumeUrl" type="primary" :href="currentResume.resumeUrl" target="_blank">
            查看简历附件
          </el-link>
          <span v-else>未上传</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增简历弹窗 -->
    <el-dialog v-model="addDialogVisible" title="新增简历" width="500px">
      <el-form ref="resumeFormRef" :model="resumeForm" :rules="resumeFormRules" label-width="100px">
        <el-form-item label="候选人姓名" prop="candidateName">
          <el-input v-model="resumeForm.candidateName" placeholder="请输入候选人姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="resumeForm.gender" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="resumeForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="resumeForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="应聘岗位" prop="positionId">
          <el-select v-model="resumeForm.positionId" placeholder="请选择应聘岗位" style="width: 100%">
            <el-option
              v-for="pos in positionList"
              :key="pos.id"
              :label="pos.name"
              :value="pos.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="简历URL" prop="resumeUrl">
          <el-input v-model="resumeForm.resumeUrl" placeholder="请输入简历链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitResume">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { unwrapListResponse } from '../../api'
import { hrApi } from '../../api/hr'
import type { Resume, Position } from '../../api/hr'

// 简历行数据类型：扩展后端返回中可能携带的嵌套职位对象与createdTime字段
type ResumeRow = Resume & { position?: Position; createdTime?: string }

// 列表加载状态
const loading = ref(false)
// 表单提交中状态
const submitting = ref(false)
// 简历列表数据
const resumeList = ref<ResumeRow[]>([])
// 职位列表数据（用于筛选下拉与岗位名称映射）
const positionList = ref<Position[]>([])

// 筛选条件（输入中的草稿值）
const filterPositionId = ref<number | ''>('')
const filterStatus = ref('')
// 实际生效的筛选条件（点击查询按钮后生效）
const appliedPositionId = ref<number | ''>('')
const appliedStatus = ref('')

// 查看简历弹窗可见性
const viewDialogVisible = ref(false)
// 新增简历弹窗可见性
const addDialogVisible = ref(false)
// 当前查看的简历
const currentResume = ref<ResumeRow | null>(null)
// 新增简历表单引用
const resumeFormRef = ref()
// 新增简历表单数据
const resumeForm = ref({
  candidateName: '',
  gender: '男',
  phone: '',
  email: '',
  positionId: undefined as number | undefined,
  resumeUrl: ''
})
// 新增简历表单校验规则
const resumeFormRules = {
  candidateName: [{ required: true, message: '请输入候选人姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  positionId: [{ required: true, message: '请选择应聘岗位', trigger: 'change' }],
  resumeUrl: [{ required: true, message: '请输入简历链接', trigger: 'blur' }]
}

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
 * 前端过滤后的简历列表（按已生效的岗位与状态条件过滤）
 */
const filteredResumeList = computed(() => {
  return resumeList.value.filter(item => {
    const matchPosition = appliedPositionId.value === '' || item.positionId === appliedPositionId.value
    const matchStatus = appliedStatus.value === '' || item.status === appliedStatus.value
    return matchPosition && matchStatus
  })
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
 * 获取简历状态对应的标签颜色类型
 * @param status 简历状态字符串枚举
 * @returns el-tag类型
 */
const getStatusType = (status: string): string => {
  const typeMap: Record<string, string> = {
    SCREENING: 'info',
    INTERVIEW: 'primary',
    OFFER: 'warning',
    HIRED: 'success',
    REJECTED: 'danger'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取简历状态对应的中文文本
 * @param status 简历状态字符串枚举
 * @returns 中文状态文本
 */
const getStatusText = (status: string): string => {
  const textMap: Record<string, string> = {
    SCREENING: '筛选中',
    INTERVIEW: '面试中',
    OFFER: '待录用',
    HIRED: '已录用',
    REJECTED: '已淘汰'
  }
  return textMap[status] || '未知'
}

/**
 * 加载职位列表（用于筛选下拉与岗位名称映射）
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
 * 加载简历列表数据
 */
const fetchResumes = async () => {
  loading.value = true
  try {
    const res = await hrApi.recruitment.getResumes()
    resumeList.value = unwrapListResponse<ResumeRow>(res)
  } catch (error) {
    console.error('获取简历列表失败:', error)
    ElMessage.error('获取简历列表失败')
    resumeList.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 查询按钮：将草稿筛选条件应用为生效条件
 */
const handleSearch = () => {
  appliedPositionId.value = filterPositionId.value
  appliedStatus.value = filterStatus.value
}

/**
 * 重置按钮：清空草稿与生效筛选条件
 */
const handleReset = () => {
  filterPositionId.value = ''
  filterStatus.value = ''
  appliedPositionId.value = ''
  appliedStatus.value = ''
}

/**
 * 打开查看简历弹窗
 * @param row 简历行数据
 */
const handleViewResume = (row: ResumeRow) => {
  currentResume.value = row
  viewDialogVisible.value = true
}

/**
 * 进入面试操作：将筛选中状态的简历更新为面试中
 * @param row 简历行数据
 */
const handleEnterInterview = async (row: ResumeRow) => {
  try {
    await hrApi.recruitment.updateResume(row.id!, { status: 'INTERVIEW' })
    ElMessage.success(`${row.candidateName} 已进入面试流程`)
    await fetchResumes()
  } catch (error) {
    console.error('更新简历状态失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

/**
 * 淘汰操作：确认后将简历状态更新为已淘汰
 * @param row 简历行数据
 */
const handleReject = (row: ResumeRow) => {
  ElMessageBox.confirm(`确认淘汰候选人 ${row.candidateName} 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await hrApi.recruitment.updateResume(row.id!, { status: 'REJECTED' })
      ElMessage.success(`${row.candidateName} 已淘汰`)
      await fetchResumes()
    } catch (error) {
      console.error('更新简历状态失败:', error)
      ElMessage.error('操作失败，请稍后重试')
    }
  }).catch(() => {
    // 用户取消操作
  })
}

/**
 * 打开新增简历弹窗并重置表单
 */
const handleAddResume = () => {
  resumeForm.value = {
    candidateName: '',
    gender: '男',
    phone: '',
    email: '',
    positionId: undefined,
    resumeUrl: ''
  }
  addDialogVisible.value = true
}

/**
 * 提交新增简历表单：校验通过后调用创建接口，初始状态为筛选中
 */
const handleSubmitResume = () => {
  if (!resumeFormRef.value) return
  resumeFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitting.value = true
    try {
      await hrApi.recruitment.createResume({
        candidateName: resumeForm.value.candidateName,
        gender: resumeForm.value.gender,
        phone: resumeForm.value.phone,
        email: resumeForm.value.email,
        positionId: resumeForm.value.positionId!,
        resumeUrl: resumeForm.value.resumeUrl,
        status: 'SCREENING'
      })
      addDialogVisible.value = false
      ElMessage.success('简历新增成功')
      await fetchResumes()
    } catch (error) {
      console.error('新增简历失败:', error)
      ElMessage.error('新增简历失败')
    } finally {
      submitting.value = false
    }
  })
}

// 组件挂载时加载职位与简历数据
onMounted(async () => {
  await fetchPositions()
  await fetchResumes()
})
</script>

<style scoped>
.resume-screening-view {
  padding: 20px;
}
.toolbar {
  margin-bottom: 20px;
}
</style>
