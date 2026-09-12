<template>
  <div class="payroll-view">
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
          <el-button type="primary" @click="handleCalculate">计算薪资</el-button>
          <el-button type="success" @click="handleExport">导出</el-button>
          <el-button type="warning" @click="handleBatchPay">批量发放</el-button>
          <el-button @click="fetchData">刷新</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="payrollList" border stripe show-summary>
      <el-table-column prop="employeeName" label="员工姓名" width="120" fixed />
      <el-table-column prop="departmentName" label="部门" width="120" />
      <el-table-column prop="basicSalary" label="基本工资" width="100" />
      <el-table-column prop="performanceBonus" label="绩效奖金" width="100" />
      <el-table-column prop="overtimePay" label="加班费" width="100" />
      <el-table-column prop="subsidy" label="补贴" width="100" />
      <el-table-column prop="socialSecurity" label="社保" width="100" />
      <el-table-column prop="housingFund" label="公积金" width="100" />
      <el-table-column prop="taxDeduction" label="税前扣除" width="100" />
      <el-table-column prop="personalIncomeTax" label="个人所得税" width="110" />
      <el-table-column prop="netSalary" label="实发工资" width="120">
        <template #default="scope">
          <span style="color: #409eff; font-weight: bold;">{{ scope.row.netSalary }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="发放状态" width="100" fixed="right">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">详情</el-button>
          <el-button 
            v-if="scope.row.status === 0" 
            link 
            type="success" 
            size="small" 
            @click="handlePay(scope.row)"
          >
            发放
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 薪资详情弹窗 -->
    <el-dialog v-model="detailVisible" title="薪资详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="员工姓名">{{ currentRow?.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ currentRow?.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="月份">{{ currentRow?.month }}</el-descriptions-item>
        <el-descriptions-item label="发放状态">
          <el-tag :type="getStatusType(currentRow?.status ?? 0)">{{ getStatusText(currentRow?.status ?? 0) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="基本工资">{{ currentRow?.basicSalary }}</el-descriptions-item>
        <el-descriptions-item label="绩效工资">{{ currentRow?.performanceBonus }}</el-descriptions-item>
        <el-descriptions-item label="补贴">{{ currentRow?.subsidy }}</el-descriptions-item>
        <el-descriptions-item label="税前扣除">{{ currentRow?.taxDeduction }}</el-descriptions-item>
        <el-descriptions-item label="实发工资">
          <span style="color: #409eff; font-weight: bold;">{{ currentRow?.netSalary }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentRow?.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Summary Card -->
    <el-card class="summary-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>薪资汇总</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-label">总人数</div>
            <div class="summary-value">{{ summary.totalEmployees }} 人</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-label">应发工资总额</div>
            <div class="summary-value">¥{{ summary.totalGrossSalary }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-label">实发工资总额</div>
            <div class="summary-value">¥{{ summary.totalNetSalary }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="summary-label">已发放人数</div>
            <div class="summary-value">{{ summary.paidEmployees }} 人</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { unwrapListResponse } from '../../api'
import { hrApi } from '../../api/hr'
import type { Employee, Department } from '../../api/hr'

const loading = ref(false)
const selectedMonth = ref(new Date().toISOString().slice(0, 7)) // 默认当前月份
const payrollList = ref<any[]>([])

// 详情弹窗状态
const detailVisible = ref(false)
const currentRow = ref<any | null>(null)

// 员工与部门映射数据
const employees = ref<Employee[]>([])
const departments = ref<Department[]>([])

// 薪资汇总数据
const summary = ref({
  totalEmployees: 0,
  totalGrossSalary: '0',
  totalNetSalary: '0',
  paidEmployees: 0
})

/**
 * 加载员工与部门数据，用于名称映射
 */
const fetchRefs = async () => {
  try {
    const [empRes, deptRes] = await Promise.all([
      hrApi.employee.getAll(),
      hrApi.department.getAll()
    ])
    employees.value = unwrapListResponse<Employee>(empRes)
    departments.value = unwrapListResponse<Department>(deptRes)
  } catch (error) {
    console.error('获取员工/部门数据失败:', error)
  }
}

/**
 * 根据员工ID获取员工姓名
 * @param employeeId 员工ID
 */
const getEmployeeName = (employeeId: number) => {
  return employees.value.find(e => e.id === employeeId)?.name ?? `员工${employeeId}`
}

/**
 * 根据员工ID获取部门名称
 * @param employeeId 员工ID
 */
const getDepartmentName = (employeeId: number) => {
  const emp = employees.value.find(e => e.id === employeeId)
  if (!emp) return '未知部门'
  return departments.value.find(d => d.id === emp.departmentId)?.name ?? '未知部门'
}

// 获取薪资记录
const fetchData = async () => {
  loading.value = true
  try {
    const res = await hrApi.payroll.getPayrollRecords(null, selectedMonth.value, selectedMonth.value)
    const records = unwrapListResponse<any>(res)

    // 处理薪资记录，字段与后端实体对齐（actualSalary为实发工资），并关联员工/部门名称
    payrollList.value = records
      .filter((record: any) => record !== null && record !== undefined)
      .map((record: any) => ({
        ...record,
        employeeName: getEmployeeName(record.employeeId),
        departmentName: getDepartmentName(record.employeeId),
        basicSalary: record.basicSalary || 0,
        performanceBonus: record.performanceSalary || 0,
        overtimePay: 0, // 暂未实现
        subsidy: record.allowance || 0,
        socialSecurity: 0, // 暂未实现
        housingFund: 0, // 暂未实现
        taxDeduction: record.deduction || 0,
        personalIncomeTax: 0, // 暂未实现
        netSalary: record.actualSalary || 0
      }))

    // 更新汇总数据
    updateSummary()
  } catch (error: any) {
    console.error('获取薪资记录失败:', error)
    // 提供更详细的错误信息
    const errorMsg = error.response?.data?.message || error.message || '获取薪资记录失败'
    ElMessage.error(errorMsg)
    payrollList.value = []
    summary.value = {
      totalEmployees: 0,
      totalGrossSalary: '0',
      totalNetSalary: '0',
      paidEmployees: 0
    }
  } finally {
    loading.value = false
  }
}

// 更新汇总数据
const updateSummary = () => {
  const totalEmployees = payrollList.value.length
  const totalGrossSalary = payrollList.value.reduce((sum, record) => sum + (parseFloat(record.basicSalary) + parseFloat(record.performanceBonus) + parseFloat(record.overtimePay) + parseFloat(record.subsidy)), 0).toFixed(2)
  const totalNetSalary = payrollList.value.reduce((sum, record) => sum + parseFloat(record.netSalary), 0).toFixed(2)
  const paidEmployees = payrollList.value.filter(record => record.status === 1).length
  
  summary.value = {
    totalEmployees,
    totalGrossSalary,
    totalNetSalary,
    paidEmployees
  }
}

const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning', // 已计算
    1: 'success', // 已发放
    2: 'info'     // 待计算
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '已计算',
    1: '已发放',
    2: '待计算'
  }
  return textMap[status] || '未知'
}

const handleCalculate = async () => {
  if (!selectedMonth.value) {
    ElMessage.warning('请选择月份')
    return
  }

  loading.value = true
  try {
    await hrApi.payroll.calculatePayroll(selectedMonth.value)
    ElMessage.success('薪资计算完成')
    // 重新获取数据
    await fetchData()
  } catch (error: any) {
    console.error('计算薪资失败:', error)
    // 提供更详细的错误信息
    const errorMsg = error.response?.data?.message || error.message || '薪资计算失败'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}

/**
 * 构造回传后端的完整薪酬实体（后端PUT为全字段替换）
 * @param row 薪资记录行数据
 * @param status 目标状态
 */
const buildPayrollEntity = (row: any, status: number) => ({
  employeeId: row.employeeId,
  month: row.month,
  basicSalary: row.basicSalary,
  performanceSalary: row.performanceSalary ?? row.performanceBonus ?? 0,
  bonus: row.bonus ?? 0,
  allowance: row.allowance ?? row.subsidy ?? 0,
  deduction: row.deduction ?? row.taxDeduction ?? 0,
  actualSalary: row.actualSalary ?? row.netSalary ?? 0,
  status,
  remark: row.remark ?? null
})

/**
 * 导出当前月份薪资列表为CSV文件
 */
const handleExport = () => {
  if (payrollList.value.length === 0) {
    ElMessage.warning('当前月份暂无薪资数据可导出')
    return
  }
  const headers = ['员工姓名', '部门', '月份', '基本工资', '绩效工资', '补贴', '税前扣除', '实发工资', '发放状态']
  const rows = payrollList.value.map(r => [
    r.employeeName,
    r.departmentName,
    r.month,
    r.basicSalary,
    r.performanceBonus,
    r.subsidy,
    r.taxDeduction,
    r.netSalary,
    getStatusText(r.status)
  ])
  // 添加BOM保证Excel打开中文不乱码
  const csvContent = '﻿' + [headers, ...rows].map(line => line.join(',')).join('\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `薪资表_${selectedMonth.value}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
  ElMessage.success('导出成功')
}

/**
 * 批量发放当前月份所有"已计算"状态的薪资
 */
const handleBatchPay = async () => {
  const pending = payrollList.value.filter(r => r.status === 0)
  if (pending.length === 0) {
    ElMessage.warning('当前月份没有待发放的薪资记录')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认批量发放 ${selectedMonth.value} 月份 ${pending.length} 条薪资记录吗？`,
      '批量发放确认',
      { type: 'warning' }
    )
  } catch {
    return // 用户取消
  }
  loading.value = true
  try {
    // 并发提交所有发放请求
    await Promise.all(
      pending.map(r => hrApi.payroll.updatePayrollRecord(r.id!, buildPayrollEntity(r, 1)))
    )
    ElMessage.success(`批量发放成功，共 ${pending.length} 条`)
    await fetchData()
  } catch (error: any) {
    console.error('批量发放失败:', error)
    ElMessage.error(error.response?.data?.message || '批量发放失败')
  } finally {
    loading.value = false
  }
}

/**
 * 查看薪资详情
 * @param row 薪资记录行数据
 */
const handleViewDetail = (row: any) => {
  currentRow.value = row
  detailVisible.value = true
}

/**
 * 发放单条薪资记录（状态置为已发放）
 * @param row 薪资记录行数据
 */
const handlePay = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认发放 ${row.employeeName} ${row.month} 月份的工资吗？`, '发放确认', { type: 'warning' })
  } catch {
    return // 用户取消
  }
  try {
    await hrApi.payroll.updatePayrollRecord(row.id!, buildPayrollEntity(row, 1))
    ElMessage.success(`${row.employeeName} 工资已发放`)
    await fetchData()
  } catch (error: any) {
    console.error('发放失败:', error)
    ElMessage.error(error.response?.data?.message || '发放失败')
  }
}

// 月份切换时自动重新加载薪资记录
watch(selectedMonth, () => {
  fetchData()
})

onMounted(async () => {
  // 先加载员工/部门映射数据，再加载薪资记录
  await fetchRefs()
  await fetchData()
})
</script>

<style scoped>
.payroll-view {
  padding: 20px;
}
.toolbar {
  margin-bottom: 20px;
}
.summary-card {
  margin-top: 20px;
}
.card-header {
  font-weight: bold;
}
.summary-item {
  text-align: center;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
.summary-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.summary-value {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}
</style>
