<template>
  <div class="hiring-approval-view">
    <!-- 顶部筛选工具栏 -->
    <div class="toolbar">
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable style="width: 160px">
            <el-option label="待录用" value="OFFER" />
            <el-option label="已录用" value="HIRED" />
            <el-option label="已淘汰" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="fetchResumes">刷新</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 录用审批列表表格 -->
    <el-table v-loading="loading" :data="filteredApprovalList" border stripe>
      <el-table-column prop="candidateName" label="候选人" width="120" />
      <el-table-column label="应聘岗位" width="160">
        <template #default="scope">{{ getPositionName(scope.row) }}</template>
      </el-table-column>
      <el-table-column prop="gender" label="性别" width="80" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="email" label="邮箱" width="200" />
      <el-table-column label="当前状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="投递时间" width="160">
        <template #default="scope">{{ formatDateTime(scope.row.createdTime ?? scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">详情</el-button>
          <el-button
            v-if="scope.row.status === 'OFFER'"
            link
            type="success"
            size="small"
            @click="handleApprove(scope.row)"
          >
            通过录用
          </el-button>
          <el-button
            v-if="scope.row.status === 'OFFER'"
            link
            type="danger"
            size="small"
            @click="handleReject(scope.row)"
          >
            拒绝录用
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 审批流程卡片 -->
    <el-card class="approval-flow-card" v-if="selectedApproval" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>审批流程 - {{ selectedApproval.candidateName }}</span>
        </div>
      </template>
      <el-steps :active="getApprovalStep(selectedApproval.status)" align-center>
        <el-step title="HR提交" description="HR专员" />
        <el-step title="部门审批" description="用人部门经理" />
        <el-step title="HR审批" description="HR经理" />
        <el-step title="总经理审批" description="总经理" />
        <el-step title="完成" description="办理入职" />
      </el-steps>
    </el-card>
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
// 简历列表数据（仅保留录用审批相关状态：待录用/已录用/已淘汰）
const resumeList = ref<ResumeRow[]>([])
// 职位列表数据（用于岗位名称映射）
const positionList = ref<Position[]>([])

// 筛选条件（输入中的草稿值）
const filterStatus = ref('')
// 实际生效的筛选条件（点击查询按钮后生效）
const appliedStatus = ref('')

// 当前选中查看详情的审批记录
const selectedApproval = ref<ResumeRow | null>(null)

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
 * 前端过滤后的录用审批列表（按已生效的状态条件过滤）
 */
const filteredApprovalList = computed(() => {
  if (appliedStatus.value === '') {
    return resumeList.value
  }
  return resumeList.value.filter(item => item.status === appliedStatus.value)
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
 * 获取录用状态对应的标签颜色类型
 * @param status 简历状态字符串枚举
 * @returns el-tag类型
 */
const getStatusType = (status: string): string => {
  const typeMap: Record<string, string> = {
    OFFER: 'warning',
    HIRED: 'success',
    REJECTED: 'danger'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取录用状态对应的中文文本
 * @param status 简历状态字符串枚举
 * @returns 中文状态文本
 */
const getStatusText = (status: string): string => {
  const textMap: Record<string, string> = {
    OFFER: '待录用',
    HIRED: '已录用',
    REJECTED: '已淘汰'
  }
  return textMap[status] || '未知'
}

/**
 * 按状态获取审批流程当前步骤（待录用→总经理审批环节，已录用→全部完成，已淘汰→未开始）
 * @param status 简历状态字符串枚举
 * @returns el-steps的active值
 */
const getApprovalStep = (status: string): number => {
  const stepMap: Record<string, number> = {
    OFFER: 3,
    HIRED: 5,
    REJECTED: 0
  }
  return stepMap[status] ?? 0
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
 * 加载简历列表数据，仅保留录用审批相关状态（待录用/已录用/已淘汰）
 */
const fetchResumes = async () => {
  loading.value = true
  try {
    const res = await hrApi.recruitment.getResumes()
    const allResumes = unwrapListResponse<ResumeRow>(res)
    // 仅保留进入录用审批环节的简历
    resumeList.value = allResumes.filter(item => ['OFFER', 'HIRED', 'REJECTED'].includes(item.status))
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
  appliedStatus.value = filterStatus.value
}

/**
 * 重置按钮：清空草稿与生效筛选条件
 */
const handleReset = () => {
  filterStatus.value = ''
  appliedStatus.value = ''
}

/**
 * 查看详情：展示该候选人的审批流程卡片
 * @param row 简历行数据
 */
const handleViewDetail = (row: ResumeRow) => {
  selectedApproval.value = row
}

/**
 * 通过录用：确认后将简历状态更新为已录用
 * @param row 简历行数据
 */
const handleApprove = (row: ResumeRow) => {
  ElMessageBox.confirm('确认录用该候选人并办理入职？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await hrApi.recruitment.updateResume(row.id!, { status: 'HIRED' })
      ElMessage.success(`${row.candidateName} 已录用`)
      await fetchResumes()
    } catch (error) {
      console.error('录用操作失败:', error)
      ElMessage.error('操作失败，请稍后重试')
    }
  }).catch(() => {
    // 用户取消操作
  })
}

/**
 * 拒绝录用：确认后将简历状态更新为已淘汰
 * @param row 简历行数据
 */
const handleReject = (row: ResumeRow) => {
  ElMessageBox.confirm(`确认拒绝录用候选人 ${row.candidateName} 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await hrApi.recruitment.updateResume(row.id!, { status: 'REJECTED' })
      ElMessage.success(`${row.candidateName} 已拒绝录用`)
      await fetchResumes()
    } catch (error) {
      console.error('拒绝录用操作失败:', error)
      ElMessage.error('操作失败，请稍后重试')
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 组件挂载时加载职位与简历数据
onMounted(async () => {
  await fetchPositions()
  await fetchResumes()
})
</script>

<style scoped>
.hiring-approval-view {
  padding: 20px;
}
.toolbar {
  margin-bottom: 20px;
}
.approval-flow-card {
  margin-top: 20px;
}
.card-header {
  font-weight: bold;
}
</style>
