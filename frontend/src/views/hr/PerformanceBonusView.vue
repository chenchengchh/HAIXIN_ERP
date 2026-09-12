<template>
  <div class="performance-bonus-view">
    <div class="toolbar">
      <el-form inline>
        <el-form-item label="考核周期">
          <el-select v-model="selectedPeriod" placeholder="全部" clearable style="width: 180px">
            <el-option label="全部" value="" />
            <el-option
              v-for="period in periodOptions"
              :key="period"
              :label="period"
              :value="period"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button type="success" @click="handleBatchApprove">批量审批</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="filteredBonusList" border stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column label="员工姓名" width="120">
        <template #default="scope">{{ getEmployeeName(scope.row.employeeId) }}</template>
      </el-table-column>
      <el-table-column label="部门" width="120">
        <template #default="scope">{{ getDepartmentName(scope.row.employeeId) }}</template>
      </el-table-column>
      <el-table-column prop="appraisalPeriod" label="考核周期" width="120" />
      <el-table-column prop="performanceScore" label="绩效得分" width="100" />
      <el-table-column prop="performanceLevel" label="绩效等级" width="100">
        <template #default="scope">
          <el-tag :type="getLevelType(scope.row.performanceLevel)">
            {{ scope.row.performanceLevel }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="bonusAmount" label="奖金金额" width="120">
        <template #default="scope">
          <span style="color: #67c23a; font-weight: bold;">¥{{ scope.row.bonusAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="评价人" width="120">
        <template #default="scope">{{ getEmployeeName(scope.row.evaluatorId) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 0"
            link
            type="success"
            size="small"
            @click="handleApprove(scope.row)"
          >
            审批
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 绩效分布卡片 -->
    <el-card class="chart-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>绩效分布</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="6" v-for="item in performanceDistribution" :key="item.level">
          <div class="distribution-item">
            <div class="distribution-level">{{ item.level }}</div>
            <div class="distribution-count">{{ item.count }} 人</div>
            <div class="distribution-percentage">{{ item.percentage }}%</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { hrApi } from '../../api/hr'
import type { PerformanceBonus, Employee, Department } from '../../api/hr'

// 加载状态
const loading = ref(false)
// 选中的考核周期
const selectedPeriod = ref('')
// 实际生效的考核周期查询条件（点击"查询"后更新）
const appliedPeriod = ref('')
// 表格勾选的行
const selectedRows = ref<PerformanceBonus[]>([])

// 绩效奖金列表
const bonusList = ref<PerformanceBonus[]>([])
// 员工列表（用于姓名与部门映射）
const employees = ref<Employee[]>([])
// 部门列表（用于部门名称映射）
const departments = ref<Department[]>([])

// 员工ID到员工信息的映射
const employeeMap = computed(() => {
  const map: Record<number, Employee> = {}
  employees.value.forEach(emp => {
    if (emp.id !== undefined) {
      map[emp.id] = emp
    }
  })
  return map
})

// 部门ID到部门名称的映射
const departmentMap = computed(() => {
  const map: Record<number, string> = {}
  departments.value.forEach(dept => {
    if (dept.id !== undefined) {
      map[dept.id] = dept.name
    }
  })
  return map
})

/**
 * 根据员工ID获取员工姓名
 * @param employeeId 员工ID
 */
const getEmployeeName = (employeeId?: number) => {
  if (employeeId === undefined || employeeId === null) return '-'
  return employeeMap.value[employeeId]?.name ?? '-'
}

/**
 * 根据员工ID获取所属部门名称
 * @param employeeId 员工ID
 */
const getDepartmentName = (employeeId: number) => {
  const emp = employeeMap.value[employeeId]
  if (!emp) return '-'
  return departmentMap.value[emp.departmentId] ?? '-'
}

/**
 * 考核周期下拉选项（从已加载数据中distinct生成）
 */
const periodOptions = computed(() => {
  const periods = new Set<string>()
  bonusList.value.forEach(item => {
    if (item.appraisalPeriod) {
      periods.add(item.appraisalPeriod)
    }
  })
  return Array.from(periods)
})

/**
 * 按考核周期过滤后的绩效奖金列表
 */
const filteredBonusList = computed(() => {
  if (!appliedPeriod.value) return bonusList.value
  return bonusList.value.filter(item => item.appraisalPeriod === appliedPeriod.value)
})

/**
 * 绩效等级分布统计（按已加载数据实时计算A/B/C/D四档人数与占比）
 */
const performanceDistribution = computed(() => {
  const levels = ['A', 'B', 'C', 'D']
  const total = bonusList.value.length
  return levels.map(level => {
    const count = bonusList.value.filter(item => item.performanceLevel === level).length
    return {
      level: `${level}级`,
      count,
      percentage: total > 0 ? Math.round((count / total) * 100) : 0
    }
  })
})

/**
 * 获取绩效等级对应的标签类型
 * @param level 绩效等级（A/B/C/D）
 */
const getLevelType = (level: string) => {
  const typeMap: Record<string, string> = {
    A: 'success',
    B: 'primary',
    C: 'warning',
    D: 'danger'
  }
  return typeMap[level] || 'info'
}

/**
 * 获取状态对应的标签类型
 * @param status 状态（0待审批/1已批准/2已发放）
 */
const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'info'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取状态文本
 * @param status 状态（0待审批/1已批准/2已发放）
 */
const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '待审批',
    1: '已批准',
    2: '已发放'
  }
  return textMap[status] || '未知'
}

/**
 * 加载绩效奖金列表
 */
const fetchBonuses = async () => {
  loading.value = true
  try {
    const res = await hrApi.performanceBonus.getByPage({ page: 1, size: 1000 })
    if (res && res.data) {
      bonusList.value = Array.isArray(res.data) ? res.data : (res.data.records ?? res.data.list ?? [])
    }
  } catch (error) {
    console.error('获取绩效奖金列表失败:', error)
    ElMessage.error('获取绩效奖金列表失败')
    bonusList.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 加载员工列表
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
 * 加载部门列表
 */
const fetchDepartments = async () => {
  try {
    const res = await hrApi.department.getAll()
    if (res && res.data) {
      departments.value = Array.isArray(res.data) ? res.data : (res.data.records ?? res.data.list ?? [])
    }
  } catch (error) {
    console.error('获取部门列表失败:', error)
    ElMessage.error('获取部门列表失败')
    departments.value = []
  }
}

/**
 * 查询（应用考核周期过滤）
 */
const handleQuery = () => {
  appliedPeriod.value = selectedPeriod.value
}

/**
 * 表格勾选变化
 * @param selection 当前勾选的行数据
 */
const handleSelectionChange = (selection: PerformanceBonus[]) => {
  selectedRows.value = selection
}

/**
 * 审批单条绩效奖金
 * @param row 当前绩效奖金行数据
 */
const handleApprove = (row: PerformanceBonus) => {
  ElMessageBox.confirm('确认审批通过该绩效奖金吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await hrApi.performanceBonus.approve(row.id!, 1)
      ElMessage.success('审批成功')
      await fetchBonuses()
    } catch (error) {
      console.error('审批失败:', error)
      ElMessage.error('审批失败')
    }
  }).catch(() => {
    // 取消审批
  })
}

/**
 * 批量审批绩效奖金（仅处理状态为待审批的记录）
 */
const handleBatchApprove = () => {
  // 仅筛选出待审批状态的记录
  const pendingRows = selectedRows.value.filter(row => row.status === 0)
  if (pendingRows.length === 0) {
    ElMessage.warning('请选择待审批的记录')
    return
  }

  ElMessageBox.confirm(
    `确定要批量审批选中的 ${pendingRows.length} 条待审批记录吗？`,
    '批量审批',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await Promise.all(pendingRows.map(row => hrApi.performanceBonus.approve(row.id!, 1)))
      ElMessage.success(`成功审批 ${pendingRows.length} 条记录`)
      await fetchBonuses()
    } catch (error) {
      console.error('批量审批失败:', error)
      ElMessage.error('批量审批失败')
    }
  }).catch(() => {
    // 取消批量审批
  })
}

// 组件挂载时加载员工、部门与绩效奖金数据
onMounted(async () => {
  await Promise.all([fetchEmployees(), fetchDepartments()])
  await fetchBonuses()
})
</script>

<style scoped>
.performance-bonus-view {
  padding: 20px;
}
.toolbar {
  margin-bottom: 20px;
}
.chart-card {
  margin-top: 20px;
}
.card-header {
  font-weight: bold;
}
.distribution-item {
  text-align: center;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
.distribution-level {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}
.distribution-count {
  font-size: 20px;
  color: #409eff;
  margin-bottom: 4px;
}
.distribution-percentage {
  font-size: 14px;
  color: #909399;
}
</style>
