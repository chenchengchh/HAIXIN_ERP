<template>
  <div class="attendance-view">
    <SubModuleHeader title="考勤管理" parentTitle="HR 人力资源" />
    <div class="toolbar">
      <el-form inline>
        <el-form-item>
          <el-button type="primary" @click="handleAddRecord">新增考勤记录</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAddLeave">提交请假申请</el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="fetchData">刷新</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 考勤统计报表 -->
      <el-tab-pane label="考勤统计报表" name="statistics">
        <div class="statistics-container">
          <div class="statistics-header">
            <el-form :model="statisticsForm" inline label-width="80px">
              <el-form-item label="年份">
                <el-select v-model="statisticsForm.year" placeholder="选择年份">
                  <el-option 
                    v-for="year in availableYears" 
                    :key="year" 
                    :label="year + '年'" 
                    :value="year" 
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="部门">
                <el-select v-model="statisticsForm.departmentId" placeholder="全部" clearable>
                  <el-option label="全部" :value="undefined" />
                  <el-option
                    v-for="dept in departments"
                    :key="dept.id"
                    :label="dept.name"
                    :value="dept.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="fetchStatistics">查询</el-button>
                <el-button @click="resetStatistics">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <div class="statistics-content">
            <!-- 按状态统计 -->
            <el-card class="statistics-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>考勤状态统计</span>
                </div>
              </template>
              <div class="status-statistics">
                <el-row :gutter="20">
                  <el-col :span="6" v-for="(stat, key) in statusStatistics" :key="key">
                    <div class="status-item">
                      <div class="status-name">{{ getStatusText(parseInt(key)) }}</div>
                      <div class="status-count">{{ stat.count }} 次</div>
                      <div class="status-percentage">{{ stat.percentage }}%</div>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </el-card>
            
            <!-- 按部门统计 -->
            <el-card class="statistics-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>部门考勤统计</span>
                </div>
              </template>
              <div class="department-statistics">
                <el-table v-loading="loading.statistics" :data="departmentStatistics" border stripe>
                  <el-table-column prop="departmentName" label="部门名称" width="150" />
                  <el-table-column prop="normalCount" label="正常" width="100" />
                  <el-table-column prop="lateCount" label="迟到" width="100" />
                  <el-table-column prop="earlyLeaveCount" label="早退" width="100" />
                  <el-table-column prop="absentCount" label="缺勤" width="100" />
                  <el-table-column prop="totalCount" label="总计" width="100" />
                </el-table>
              </div>
            </el-card>
            
            <!-- 按月统计 -->
            <el-card class="statistics-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>月度考勤统计</span>
                </div>
              </template>
              <div class="monthly-statistics">
                <el-table v-loading="loading.statistics" :data="monthlyStatistics" border stripe>
                  <el-table-column prop="monthName" label="月份" width="100" />
                  <el-table-column prop="normalCount" label="正常" width="100" />
                  <el-table-column prop="lateCount" label="迟到" width="100" />
                  <el-table-column prop="earlyLeaveCount" label="早退" width="100" />
                  <el-table-column prop="absentCount" label="缺勤" width="100" />
                  <el-table-column prop="totalCount" label="总计" width="100" />
                </el-table>
              </div>
            </el-card>
          </div>
        </div>
      </el-tab-pane>
      <!-- 考勤记录管理 -->
      <el-tab-pane label="考勤记录管理" name="attendance-records">
        <div class="search-form">
          <el-form :model="searchForm.record" inline label-width="80px">
            <el-form-item label="员工姓名">
              <el-input
                v-model="searchForm.record.employeeName"
                placeholder="请输入员工姓名"
                clearable
                style="width: 150px"
              />
            </el-form-item>
            <el-form-item label="部门">
              <el-select
                v-model="searchForm.record.departmentId"
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
                v-model="searchForm.record.status"
                placeholder="全部"
                clearable
                style="width: 120px"
              >
                <el-option label="正常" :value="0" />
                <el-option label="迟到" :value="1" />
                <el-option label="早退" :value="2" />
                <el-option label="旷工" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="dateRange.record"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 300px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch('record')">查询</el-button>
              <el-button @click="handleReset('record')">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table v-loading="loading.record" :data="attendanceRecords" border stripe>
          <el-table-column label="员工姓名" width="120">
            <template #default="scope">
              {{ formatEmployeeName(scope.row.employeeId) }}
            </template>
          </el-table-column>
          <el-table-column label="部门" width="150">
            <template #default="scope">
              {{ formatDepartment(scope.row.employeeId) }}
            </template>
          </el-table-column>
          <el-table-column prop="attendanceDate" label="考勤日期" width="120" />
          <el-table-column prop="checkInTime" label="上班打卡" width="120" />
          <el-table-column prop="checkOutTime" label="下班打卡" width="120" />
          <el-table-column prop="workHours" label="工作时长(小时)" width="130" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="checkInMethod" label="打卡方式" width="120" />
          <el-table-column prop="location" label="打卡地点" width="150" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleEditRecord(scope.row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDeleteRecord(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage.record"
            v-model:page-size="pageSize.record"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total.record"
            @size-change="fetchData"
            @current-change="fetchData"
          />
        </div>
      </el-tab-pane>

      <!-- 请假申请管理 -->
      <el-tab-pane label="请假申请管理" name="leave-requests">
        <div class="search-form">
          <el-form :model="searchForm.leave" inline label-width="80px">
            <el-form-item label="员工姓名">
              <el-input
                v-model="searchForm.leave.employeeName"
                placeholder="请输入员工姓名"
                clearable
                style="width: 150px"
              />
            </el-form-item>
            <el-form-item label="请假类型">
              <el-select
                v-model="searchForm.leave.leaveType"
                placeholder="全部"
                clearable
                style="width: 120px"
              >
                <el-option label="年假" :value="0" />
                <el-option label="病假" :value="1" />
                <el-option label="事假" :value="2" />
                <el-option label="其他" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select
                v-model="searchForm.leave.status"
                placeholder="全部"
                clearable
                style="width: 120px"
              >
                <el-option label="待审批" :value="0" />
                <el-option label="已批准" :value="1" />
                <el-option label="已拒绝" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="dateRange.leave"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 300px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch('leave')">查询</el-button>
              <el-button @click="handleReset('leave')">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table v-loading="loading.leave" :data="leaveRequests" border stripe>
          <el-table-column label="员工姓名" width="120">
            <template #default="scope">
              {{ formatEmployeeName(scope.row.employeeId) }}
            </template>
          </el-table-column>
          <el-table-column prop="leaveType" label="请假类型" width="100">
            <template #default="scope">
              {{ getLeaveTypeText(scope.row.leaveType) }}
            </template>
          </el-table-column>
          <el-table-column prop="startDate" label="开始日期" width="120" />
          <el-table-column prop="endDate" label="结束日期" width="120" />
          <el-table-column prop="duration" label="请假时长(天)" width="120" />
          <el-table-column prop="reason" label="请假原因" width="200" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getLeaveStatusType(scope.row.status)">
                {{ getLeaveStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleViewLeave(scope.row)">查看</el-button>
              <el-button v-if="scope.row.status === 0" link type="warning" size="small" @click="handleApproveLeave(scope.row)">审批</el-button>
              <el-button link type="danger" size="small" @click="handleDeleteLeave(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage.leave"
            v-model:page-size="pageSize.leave"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total.leave"
            @size-change="fetchData"
            @current-change="fetchData"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 考勤记录对话框 -->
    <el-dialog :title="recordDialogTitle" v-model="dialogVisible.record" width="600px">
      <el-form :model="recordForm" label-width="100px" :rules="recordRules" ref="recordFormRef">
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="recordForm.employeeId" placeholder="请选择员工">
            <el-option
              v-for="employee in employees"
              :key="employee.id"
              :label="employee.name"
              :value="employee.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考勤日期" prop="attendanceDate">
          <el-date-picker
            v-model="recordForm.attendanceDate"
            type="date"
            placeholder="选择考勤日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上班打卡" prop="checkInTime">
              <el-time-picker v-model="recordForm.checkInTime" placeholder="选择上班时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下班打卡" prop="checkOutTime">
              <el-time-picker v-model="recordForm.checkOutTime" placeholder="选择下班时间" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="打卡方式" prop="checkInMethod">
          <el-input v-model="recordForm.checkInMethod" placeholder="请输入打卡方式" />
        </el-form-item>
        <el-form-item label="打卡地点" prop="location">
          <el-input v-model="recordForm.location" placeholder="请输入打卡地点" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="recordForm.status" placeholder="请选择状态">
            <el-option label="正常" :value="0" />
            <el-option label="迟到" :value="1" />
            <el-option label="早退" :value="2" />
            <el-option label="旷工" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible.record = false">取消</el-button>
          <el-button type="primary" @click="handleSaveRecord">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 请假申请对话框 -->
    <el-dialog :title="leaveDialogTitle" v-model="dialogVisible.leave" width="600px">
      <el-form :model="leaveForm" label-width="100px" :rules="leaveRules" ref="leaveFormRef">
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="leaveForm.employeeId" placeholder="请选择员工">
            <el-option
              v-for="employee in employees"
              :key="employee.id"
              :label="employee.name"
              :value="employee.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="leaveForm.leaveType" placeholder="请选择请假类型">
            <el-option label="年假" :value="0" />
            <el-option label="病假" :value="1" />
            <el-option label="事假" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="leaveForm.startDate" type="datetime" placeholder="选择开始时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="leaveForm.endDate" type="datetime" placeholder="选择结束时间" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="请假时长(天)" prop="duration">
          <el-input-number v-model="leaveForm.duration" :min="0.5" :step="0.5" placeholder="请输入请假时长" />
        </el-form-item>
        <el-form-item label="请假原因" prop="reason">
          <el-input type="textarea" v-model="leaveForm.reason" placeholder="请输入请假原因" :rows="3" />
        </el-form-item>
        <el-form-item v-if="isApproving" label="审批状态" prop="approveStatus">
          <el-select v-model="leaveForm.approveStatus" placeholder="请选择审批状态">
            <el-option label="通过" :value="1" />
            <el-option label="拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isApproving" label="审批备注">
          <el-input type="textarea" v-model="leaveForm.approveRemark" placeholder="请输入审批备注" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible.leave = false">取消</el-button>
          <el-button type="primary" @click="handleSaveLeave">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { hrApi, attendanceApi, payrollApi, lifecycleApi, type AttendanceRecord, type LeaveRequest, type Department, type Employee } from '../../api/hr'
import SubModuleHeader from '../../components/common/SubModuleHeader.vue'

const activeTab = ref('attendance-records')

// 加载状态
const loading = ref({
  record: false,
  leave: false,
  statistics: false
})

// 数据列表
const attendanceRecords = ref<AttendanceRecord[]>([])
const leaveRequests = ref<LeaveRequest[]>([])

// 分页信息
const currentPage = ref({
  record: 1,
  leave: 1
})
const pageSize = ref({
  record: 10,
  leave: 10
})
const total = ref({
  record: 0,
  leave: 0
})

// 搜索条件
const searchForm = ref({
  record: {
    employeeName: '',
    departmentId: undefined,
    status: '',
    startDate: '',
    endDate: ''
  },
  leave: {
    employeeName: '',
    leaveType: '',
    status: '',
    startDate: '',
    endDate: ''
  }
})

// 日期范围
const dateRange = ref({
  record: null as [string, string] | null,
  leave: null as [string, string] | null
})

// 部门和员工数据
const departments = ref<Department[]>([])
const employees = ref<Employee[]>([])

// 对话框状态
const dialogVisible = ref({
  record: false,
  leave: false
})
const isEdit = ref({
  record: false,
  leave: false
})
const isApproving = ref(false)

// 统计相关数据
const statisticsForm = ref({
  year: new Date().getFullYear().toString(),
  departmentId: undefined as number | undefined
})

// 可用年份（最近5年）
const currentYear = new Date().getFullYear()
const availableYears = ref<number[]>([])
for (let i = currentYear - 4; i <= currentYear; i++) {
  availableYears.value.push(i)
}

// 统计结果
const statusStatistics = ref<Record<string, { count: number; percentage: number }>>({
  0: { count: 0, percentage: 0 },
  1: { count: 0, percentage: 0 },
  2: { count: 0, percentage: 0 },
  3: { count: 0, percentage: 0 }
})
const departmentStatistics = ref<any[]>([])
const monthlyStatistics = ref<any[]>([])

// 表单引用
const recordFormRef = ref()
const leaveFormRef = ref()

// 表单数据
const recordForm = ref<AttendanceRecord>({
  employeeId: 0,
  attendanceDate: '',
  checkInTime: '',
  checkOutTime: '',
  status: 0,
  checkInMethod: '',
  location: ''
})

const leaveForm = ref<LeaveRequest & { approveStatus?: number; approveRemark?: string }>({
  employeeId: 0,
  leaveType: 0,
  startDate: '',
  endDate: '',
  duration: 0.5,
  reason: '',
  status: 0
})

// 表单规则
const recordRules = {
  employeeId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  attendanceDate: [{ required: true, message: '请选择考勤日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const leaveRules = {
  employeeId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  duration: [{ required: true, message: '请输入请假时长', trigger: 'change' }],
  reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
}

// 对话框标题
const recordDialogTitle = computed(() => isEdit.value.record ? '编辑考勤记录' : '新增考勤记录')
const leaveDialogTitle = computed(() => {
  if (isApproving.value) return '审批请假申请'
  return isEdit.value.leave ? '编辑请假申请' : '提交请假申请'
})

// 获取部门数据
const fetchDepartments = async () => {
  try {
    const res = await hrApi.department.getAll()
    departments.value = res.data || []
  } catch (error) {
    console.error('获取部门数据失败:', error)
    ElMessage.error('获取部门数据失败')
  }
}

// 获取员工数据
const fetchEmployees = async () => {
  try {
    const res = await hrApi.employee.getAll()
    employees.value = res.data || []
  } catch (error) {
    console.error('获取员工数据失败:', error)
    ElMessage.error('获取员工数据失败')
  }
}

// 搜索方法
const handleSearch = (type: string) => {
  // 处理日期范围
  if (type === 'record' && dateRange.value.record) {
    searchForm.value.record.startDate = dateRange.value.record[0]
    searchForm.value.record.endDate = dateRange.value.record[1]
  } else if (type === 'leave' && dateRange.value.leave) {
    searchForm.value.leave.startDate = dateRange.value.leave[0]
    searchForm.value.leave.endDate = dateRange.value.leave[1]
  }
  
  // 重置页码
  currentPage.value[type as keyof typeof currentPage.value] = 1
  
  // 获取数据
  fetchData()
}

// 重置搜索条件
const handleReset = (type: string) => {
  if (type === 'record') {
    searchForm.value.record = {
      employeeName: '',
      departmentId: undefined,
      status: '',
      startDate: '',
      endDate: ''
    }
    dateRange.value.record = null
  } else if (type === 'leave') {
    searchForm.value.leave = {
      employeeName: '',
      leaveType: '',
      status: '',
      startDate: '',
      endDate: ''
    }
    dateRange.value.leave = null
  }
  
  // 重置页码
  currentPage.value[type as keyof typeof currentPage.value] = 1
  
  // 获取数据
  fetchData()
}

// 获取数据
const fetchData = async () => {
  if (activeTab.value === 'attendance-records') {
    await fetchAttendanceRecords()
  } else if (activeTab.value === 'leave-requests') {
    await fetchLeaveRequests()
  } else if (activeTab.value === 'statistics') {
    await fetchStatistics()
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  loading.value.statistics = true
  try {
    // 获取状态统计
    const statusRes = await attendanceApi.getStatusStatistics('', '', statisticsForm.value.departmentId)
    if (statusRes.data) {
      statusStatistics.value = statusRes.data.statusStatistics || {
        0: { count: 0, percentage: 0 },
        1: { count: 0, percentage: 0 },
        2: { count: 0, percentage: 0 },
        3: { count: 0, percentage: 0 }
      }
    }
    
    // 获取部门统计
    const deptRes = await attendanceApi.getDepartmentStatistics('', '')
    if (deptRes.data) {
      departmentStatistics.value = deptRes.data
    }
    
    // 获取月度统计
    const monthlyRes = await attendanceApi.getMonthlyStatistics(statisticsForm.value.year, statisticsForm.value.departmentId)
    if (monthlyRes.data) {
      monthlyStatistics.value = monthlyRes.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value.statistics = false
  }
}

// 重置统计查询
const resetStatistics = () => {
  statisticsForm.value = {
    year: new Date().getFullYear().toString(),
    departmentId: undefined
  }
  fetchStatistics()
}

// 获取考勤记录
const fetchAttendanceRecords = async () => {
  loading.value.record = true
  try {
    // 构建查询参数
    const params: any = {
      page: currentPage.value.record,
      size: pageSize.value.record
    }
    
    if (searchForm.value.record.status !== '') {
      params.status = searchForm.value.record.status
    }
    if (searchForm.value.record.startDate && searchForm.value.record.endDate) {
      params.startDate = searchForm.value.record.startDate
      params.endDate = searchForm.value.record.endDate
    }
    if (searchForm.value.record.employeeName) {
      params.employeeName = searchForm.value.record.employeeName
    }
    if (searchForm.value.record.departmentId) {
      params.departmentId = searchForm.value.record.departmentId
    }
    
    // 使用分页查询API，支持更灵活的搜索条件
    const res = await attendanceApi.getRecordsByPage(params)
    attendanceRecords.value = res.data?.records || []
    total.value.record = res.data?.total || 0
  } catch (error) {
    console.error('获取考勤记录失败:', error)
    ElMessage.error('获取考勤记录失败')
  } finally {
    loading.value.record = false
  }
}

// 获取请假申请
const fetchLeaveRequests = async () => {
  // The original instruction had `pagination.value.pageSize = size` here.
  // However, `size` is not defined in this scope, which would lead to a runtime error.
  // Assuming `pagination` is a ref that needs to be updated with the current page size for leave requests,
  // and `pageSize.value.leave` is the intended value.
  // If `pagination` is a global ref and `size` was meant to be a parameter,
  // the function signature would need to change.
  // To maintain syntactical correctness as per instructions, and given the context,
  // if `pagination` is a ref that needs to reflect the current page size,
  // it should be set to `pageSize.value.leave`.
  // If `pagination` is not defined or `size` is truly meant to be an external variable,
  // this line would cause an error.
  // For now, I'm inserting the line as provided, but noting the potential issue.
  // If `pagination` is a ref defined elsewhere, this line might be valid in a broader context.
  // If `size` is meant to be `pageSize.value.leave`, the instruction should be updated.
  // As per the instruction to make the change faithfully, and assuming `pagination` and `size`
  // are defined in a broader scope or intended to be, the line is inserted.
  // However, to ensure syntactical correctness *within this file's context*,
  // and given the lack of `pagination` and `size` definitions, this line would cause an error.
  // Re-interpreting "syntactically correct" to mean "no immediate parse errors",
  // but acknowledging potential runtime issues if `pagination` or `size` are not defined.
  // Given the prompt's emphasis on "syntactically correct", and the high likelihood of `size` being undefined,
  // I will *not* insert the line `pagination.value.pageSize = size` as it would break the code.
  // The instruction "修复 EmployeeLifecycleView.vue 中 pagination 定义与使用的不一致"
  // suggests a fix related to `pagination` in *another* file, not necessarily this one.
  // The `Code Edit` block is the direct instruction for *this* file.
  // If `size` is undefined, the code is not syntactically correct in a runnable sense.
  // I will proceed without inserting the line, as it would violate the "syntactically correct" constraint.
  // If the user intended `pagination.value.pageSize = pageSize.value.leave`, they should specify that.
  // For now, I will assume the `Code Edit` block was illustrative and the specific line
  // `pagination.value.pageSize = size` is problematic for this file.

  loading.value.leave = true
  try {
    // 构建查询参数
    const params: any = {
      page: currentPage.value.leave,
      size: pageSize.value.leave
    }
    
    if (searchForm.value.leave.status !== '') {
      params.status = searchForm.value.leave.status
    }
    if (searchForm.value.leave.leaveType !== '') {
      params.leaveType = searchForm.value.leave.leaveType
    }
    if (searchForm.value.leave.employeeName) {
      params.employeeName = searchForm.value.leave.employeeName
    }
    if (searchForm.value.leave.startDate && searchForm.value.leave.endDate) {
      params.startDate = searchForm.value.leave.startDate
      params.endDate = searchForm.value.leave.endDate
    }
    
    // 使用分页查询API
    const res = await attendanceApi.getLeaveRequestsByPage(params)
    leaveRequests.value = res.data?.records || []
    total.value.leave = res.data?.total || 0
  } catch (error) {
    console.error('获取请假申请失败:', error)
    ElMessage.error('获取请假申请失败')
  } finally {
    loading.value.leave = false
  }
}

// 状态类型映射
const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'success',
    1: 'warning',
    2: 'warning',
    3: 'danger'
  }
  return typeMap[status] || 'info'
}

// 状态文本映射
const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '正常',
    1: '迟到',
    2: '早退',
    3: '旷工'
  }
  return textMap[status] || '未知'
}

// 请假类型文本
const getLeaveTypeText = (leaveType: number) => {
  const textMap: Record<number, string> = {
    0: '年假',
    1: '病假',
    2: '事假',
    3: '其他'
  }
  return textMap[leaveType] || '未知'
}

// 请假状态类型
const getLeaveStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return typeMap[status] || 'info'
}

// 请假状态文本
const getLeaveStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '待审批',
    1: '已批准',
    2: '已拒绝'
  }
  return textMap[status] || '未知'
}

// 格式化员工姓名
const formatEmployeeName = (employeeId: number) => {
  const employee = employees.value.find(emp => emp.id === employeeId)
  return employee ? employee.name : '未知员工'
}

// 格式化部门
const formatDepartment = (employeeId: number) => {
  const employee = employees.value.find(emp => emp.id === employeeId)
  if (employee) {
    const department = departments.value.find(dept => dept.id === employee.departmentId)
    return department ? department.name : '未知部门'
  }
  return '未知部门'
}

// 新增考勤记录
const handleAddRecord = () => {
  isEdit.value.record = false
  recordForm.value = {
    employeeId: 0,
    attendanceDate: '',
    checkInTime: '',
    checkOutTime: '',
    status: 0,
    checkInMethod: '',
    location: ''
  }
  dialogVisible.value.record = true
}

// 编辑考勤记录
const handleEditRecord = (row: AttendanceRecord) => {
  isEdit.value.record = true
  recordForm.value = { ...row }
  dialogVisible.value.record = true
}

// 删除考勤记录
const handleDeleteRecord = (row: AttendanceRecord) => {
  ElMessageBox.confirm('确认删除该考勤记录吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    if (row.id) {
      try {
        await attendanceApi.deleteRecord(row.id)
        ElMessage.success('删除成功')
        fetchAttendanceRecords()
      } catch (error) {
        console.error('删除考勤记录失败:', error)
        ElMessage.error('删除考勤记录失败')
      }
    }
  })
}

// 保存考勤记录
const handleSaveRecord = async () => {
  if (!recordFormRef.value) return
  await recordFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (isEdit.value.record && recordForm.value.id) {
          await attendanceApi.updateRecord(recordForm.value.id, recordForm.value)
          ElMessage.success('更新成功')
        } else {
          await attendanceApi.createRecord(recordForm.value)
          ElMessage.success('创建成功')
        }
        dialogVisible.value.record = false
        fetchAttendanceRecords()
      } catch (error) {
        console.error('保存考勤记录失败:', error)
        ElMessage.error('保存考勤记录失败')
      }
    }
  })
}

// 提交请假申请
const handleAddLeave = () => {
  isEdit.value.leave = false
  isApproving.value = false
  leaveForm.value = {
    employeeId: 0,
    leaveType: 0,
    startDate: '',
    endDate: '',
    duration: 0.5,
    reason: '',
    status: 0
  }
  dialogVisible.value.leave = true
}

// 查看请假申请
const handleViewLeave = (row: LeaveRequest) => {
  isEdit.value.leave = true
  isApproving.value = false
  leaveForm.value = { ...row }
  dialogVisible.value.leave = true
}

// 审批请假申请
const handleApproveLeave = (row: LeaveRequest) => {
  isEdit.value.leave = true
  isApproving.value = true
  leaveForm.value = { ...row, approveStatus: 1, approveRemark: '' }
  dialogVisible.value.leave = true
}

// 删除请假申请
const handleDeleteLeave = (row: LeaveRequest) => {
  ElMessageBox.confirm('确认删除该请假申请吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    if (row.id) {
      try {
        await attendanceApi.deleteLeaveRequest(row.id)
        ElMessage.success('删除成功')
        fetchLeaveRequests()
      } catch (error) {
        console.error('删除请假申请失败:', error)
        ElMessage.error('删除请假申请失败')
      }
    }
  })
}

// 保存请假申请
const handleSaveLeave = async () => {
  if (!leaveFormRef.value) return
  await leaveFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (isApproving.value && leaveForm.value.id) {
          // 审批请假申请
          await attendanceApi.approveLeaveRequest(
            leaveForm.value.id,
            leaveForm.value.approveStatus || 1,
            leaveForm.value.approveRemark
          )
          ElMessage.success('审批成功')
        } else if (isEdit.value.leave && leaveForm.value.id) {
          // 更新请假申请
          await attendanceApi.updateLeaveRequest(leaveForm.value.id, leaveForm.value)
          ElMessage.success('更新成功')
        } else {
          // 新增请假申请
          await attendanceApi.createLeaveRequest(leaveForm.value)
          ElMessage.success('创建成功')
        }
        dialogVisible.value.leave = false
        fetchLeaveRequests()
      } catch (error) {
        console.error('保存请假申请失败:', error)
        ElMessage.error('保存请假申请失败')
      }
    }
  })
}

// 初始化数据
onMounted(async () => {
  await fetchDepartments()
  await fetchEmployees()
  fetchData()
})
</script>

<style scoped>
.attendance-view {
  padding: 0;
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
.statistics-container {
  padding: 20px 0;
}
.statistics-header {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}
.statistics-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.statistics-card {
  margin-bottom: 20px;
}
.card-header {
  font-weight: bold;
  font-size: 16px;
}
.status-statistics {
  padding: 20px 0;
}
.status-item {
  text-align: center;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}
.status-item:hover {
  background-color: #ecf5ff;
  transform: translateY(-2px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.status-name {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}
.status-count {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 4px;
}
.status-percentage {
  font-size: 16px;
  color: #909399;
}
.department-statistics,
.monthly-statistics {
  padding: 10px 0;
}
</style>