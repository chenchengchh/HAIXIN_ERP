<template>
  <div class="social-security-view">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 社保管理 -->
      <el-tab-pane label="社保管理" name="social">
        <div class="toolbar">
          <el-form inline>
            <el-form-item label="月份">
              <el-date-picker
                v-model="selectedMonth"
                type="month"
                placeholder="选择月份"
                format="YYYY-MM"
                value-format="YYYY-MM"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleCalculate">计算</el-button>
              <el-button type="success" @click="handleDeclare">申报</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table v-loading="loading" :data="records" border stripe>
          <el-table-column label="员工姓名" width="120">
            <template #default="scope">{{ getEmployeeName(scope.row.employeeId) }}</template>
          </el-table-column>
          <el-table-column label="部门" width="120">
            <template #default="scope">{{ getDepartmentName(scope.row.employeeId) }}</template>
          </el-table-column>
          <el-table-column prop="baseAmount" label="缴费基数" width="120" />
          <el-table-column prop="pensionCompany" label="养老(公司)" width="100" />
          <el-table-column prop="pensionPersonal" label="养老(个人)" width="100" />
          <el-table-column prop="medicalCompany" label="医疗(公司)" width="100" />
          <el-table-column prop="medicalPersonal" label="医疗(个人)" width="100" />
          <el-table-column prop="unemploymentCompany" label="失业(公司)" width="110" />
          <el-table-column prop="unemploymentPersonal" label="失业(个人)" width="110" />
          <el-table-column label="公司合计" width="100">
            <template #default="scope">
              <span style="color: #f56c6c; font-weight: bold;">{{ getCompanyTotal(scope.row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="个人合计" width="100">
            <template #default="scope">
              <span style="color: #409eff; font-weight: bold;">{{ getPersonalTotal(scope.row) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
                {{ scope.row.status === 1 ? '已申报' : '未申报' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 公积金管理 -->
      <el-tab-pane label="公积金管理" name="provident">
        <div class="toolbar">
          <el-form inline>
            <el-form-item label="月份">
              <el-date-picker
                v-model="selectedMonth"
                type="month"
                placeholder="选择月份"
                format="YYYY-MM"
                value-format="YYYY-MM"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleCalculate">计算</el-button>
              <el-button type="success" @click="handleDeclare">申报</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table v-loading="loading" :data="records" border stripe>
          <el-table-column label="员工姓名" width="120">
            <template #default="scope">{{ getEmployeeName(scope.row.employeeId) }}</template>
          </el-table-column>
          <el-table-column label="部门" width="120">
            <template #default="scope">{{ getDepartmentName(scope.row.employeeId) }}</template>
          </el-table-column>
          <el-table-column prop="baseAmount" label="缴费基数" width="120" />
          <el-table-column label="公积金(公司)" width="120">
            <template #default="scope">
              <span style="color: #f56c6c;">{{ scope.row.housingFundCompany }}</span>
            </template>
          </el-table-column>
          <el-table-column label="公积金(个人)" width="120">
            <template #default="scope">
              <span style="color: #409eff;">{{ scope.row.housingFundPersonal }}</span>
            </template>
          </el-table-column>
          <el-table-column label="合计" width="120">
            <template #default="scope">
              <span style="font-weight: bold;">{{ getHousingFundTotal(scope.row) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
                {{ scope.row.status === 1 ? '已申报' : '未申报' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { hrApi } from '../../api/hr'
import type { SocialSecurityRecord, Employee, Department } from '../../api/hr'

// 当前激活的Tab页
const activeTab = ref('social')
// 加载状态
const loading = ref(false)

/**
 * 获取当前月份（YYYY-MM格式）
 */
const getCurrentMonth = () => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

// 当前选择的月份（两个Tab共用）
const selectedMonth = ref(getCurrentMonth())

// 社保公积金记录列表
const records = ref<SocialSecurityRecord[]>([])
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
const getEmployeeName = (employeeId: number) => {
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
 * 计算社保公司合计（养老+医疗+失业）
 * @param row 社保公积金记录行数据
 */
const getCompanyTotal = (row: SocialSecurityRecord) => {
  const total = (Number(row.pensionCompany) || 0) + (Number(row.medicalCompany) || 0) + (Number(row.unemploymentCompany) || 0)
  return total.toFixed(2)
}

/**
 * 计算社保个人合计（养老+医疗+失业）
 * @param row 社保公积金记录行数据
 */
const getPersonalTotal = (row: SocialSecurityRecord) => {
  const total = (Number(row.pensionPersonal) || 0) + (Number(row.medicalPersonal) || 0) + (Number(row.unemploymentPersonal) || 0)
  return total.toFixed(2)
}

/**
 * 计算公积金合计（公司+个人）
 * @param row 社保公积金记录行数据
 */
const getHousingFundTotal = (row: SocialSecurityRecord) => {
  const total = (Number(row.housingFundCompany) || 0) + (Number(row.housingFundPersonal) || 0)
  return total.toFixed(2)
}

/**
 * 加载社保公积金记录（按月份过滤，后端不支持参数时前端兜底过滤）
 */
const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await hrApi.socialSecurity.getByPage({
      page: 1,
      size: 1000,
      insuranceMonth: selectedMonth.value || undefined
    })
    if (res && res.data) {
      const list = Array.isArray(res.data) ? res.data : (res.data.records ?? res.data.list ?? [])
      // 前端兜底按月份过滤，防止后端未支持insuranceMonth参数
      records.value = selectedMonth.value
        ? list.filter((item: SocialSecurityRecord) => item.insuranceMonth === selectedMonth.value)
        : list
    }
  } catch (error) {
    console.error('获取社保公积金记录失败:', error)
    ElMessage.error('获取社保公积金记录失败')
    records.value = []
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
 * 从统一响应中提取生成条数（兼容数字或对象结构）
 * @param res 接口响应对象
 */
const extractCount = (res: any) => {
  const payload = res?.data?.data
  if (typeof payload === 'number') return payload
  return payload?.count ?? payload?.total ?? ''
}

/**
 * 计算当前月份的社保公积金
 */
const handleCalculate = async () => {
  if (!selectedMonth.value) {
    ElMessage.warning('请先选择月份')
    return
  }
  try {
    const res = await hrApi.socialSecurity.calculate(selectedMonth.value)
    const count = extractCount(res)
    ElMessage.success(count !== '' ? `计算完成，生成 ${count} 条记录` : '计算完成')
    await fetchRecords()
  } catch (error) {
    console.error('计算社保公积金失败:', error)
    ElMessage.error('计算社保公积金失败')
  }
}

/**
 * 申报当前月份的社保公积金
 */
const handleDeclare = () => {
  if (!selectedMonth.value) {
    ElMessage.warning('请先选择月份')
    return
  }
  ElMessageBox.confirm(`确认申报 ${selectedMonth.value} 月份的社保公积金吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await hrApi.socialSecurity.declareMonth(selectedMonth.value)
      const count = extractCount(res)
      ElMessage.success(count !== '' ? `申报成功，共 ${count} 条记录` : '申报成功')
      await fetchRecords()
    } catch (error) {
      console.error('申报社保公积金失败:', error)
      ElMessage.error('申报社保公积金失败')
    }
  }).catch(() => {
    // 取消申报
  })
}

// 月份变化时重新加载记录
watch(selectedMonth, () => {
  fetchRecords()
})

// 组件挂载时加载员工、部门与社保公积金记录
onMounted(async () => {
  await Promise.all([fetchEmployees(), fetchDepartments()])
  await fetchRecords()
})
</script>

<style scoped>
.social-security-view {
  padding: 20px;
}
.toolbar {
  margin-bottom: 20px;
}
</style>
