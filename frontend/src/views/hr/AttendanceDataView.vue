<template>
  <div class="attendance-data-view">
    <div class="toolbar">
      <el-form inline>
        <el-form-item label="日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.departmentId" placeholder="全部" clearable style="width: 150px">
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="员工">
          <el-input
            v-model="searchForm.employeeName"
            placeholder="请输入员工姓名"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="考勤状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="0" />
            <el-option label="迟到" :value="1" />
            <el-option label="早退" :value="2" />
            <el-option label="缺勤" :value="3" />
            <el-option label="请假" :value="4" />
            <el-option label="旷工" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleExport">导出</el-button>
          <el-button @click="handleRefresh">刷新</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="attendanceList" border stripe>
      <el-table-column prop="employeeName" label="员工姓名" width="120" />
      <el-table-column prop="departmentName" label="部门" width="120" />
      <el-table-column prop="date" label="日期" width="120" />
      <el-table-column prop="checkInTime" label="签到时间" width="120" />
      <el-table-column prop="checkOutTime" label="签退时间" width="120" />
      <el-table-column prop="workHours" label="工作时长" width="100" />
      <el-table-column prop="status" label="考勤状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="checkInMethod" label="打卡方式" width="100" />
      <el-table-column prop="location" label="打卡地点" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">详情</el-button>
          <el-button link type="danger" size="small" @click="handleEdit(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
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

    <!-- 考勤记录详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="考勤记录详情"
      width="500px"
    >
      <div class="detail-content" v-if="currentAttendanceRecord">
        <el-form label-width="120px" size="small">
          <el-form-item label="员工姓名">
            {{ currentAttendanceRecord.employeeName }}
          </el-form-item>
          <el-form-item label="部门">
            {{ currentAttendanceRecord.departmentName }}
          </el-form-item>
          <el-form-item label="日期">
            {{ currentAttendanceRecord.date }}
          </el-form-item>
          <el-form-item label="签到时间">
            {{ currentAttendanceRecord.checkInTime }}
          </el-form-item>
          <el-form-item label="签退时间">
            {{ currentAttendanceRecord.checkOutTime }}
          </el-form-item>
          <el-form-item label="工作时长">
            {{ currentAttendanceRecord.workHours }}
          </el-form-item>
          <el-form-item label="考勤状态">
            <el-tag :type="getStatusType(currentAttendanceRecord.status)">
              {{ getStatusText(currentAttendanceRecord.status) }}
            </el-tag>
          </el-form-item>
          <el-form-item label="打卡方式">
            {{ currentAttendanceRecord.checkInMethod }}
          </el-form-item>
          <el-form-item label="打卡地点">
            {{ currentAttendanceRecord.location }}
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 考勤记录编辑弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑考勤记录"
      width="500px"
    >
      <div class="edit-content" v-if="currentAttendanceRecord">
        <el-form label-width="120px" size="small" ref="editFormRef">
          <el-form-item label="员工姓名">
            {{ currentAttendanceRecord.employeeName }}
          </el-form-item>
          <el-form-item label="部门">
            {{ currentAttendanceRecord.departmentName }}
          </el-form-item>
          <el-form-item label="日期">
            {{ currentAttendanceRecord.date }}
          </el-form-item>
          <el-form-item label="签到时间">
            <el-time-picker
              v-model="editForm.checkInTime"
              format="HH:mm:ss"
              value-format="HH:mm:ss"
            />
          </el-form-item>
          <el-form-item label="签退时间">
            <el-time-picker
              v-model="editForm.checkOutTime"
              format="HH:mm:ss"
              value-format="HH:mm:ss"
            />
          </el-form-item>
          <el-form-item label="考勤状态">
            <el-select v-model="editForm.status" placeholder="请选择考勤状态">
              <el-option label="正常" :value="0" />
              <el-option label="迟到" :value="1" />
              <el-option label="早退" :value="2" />
              <el-option label="缺勤" :value="3" />
              <el-option label="请假" :value="4" />
              <el-option label="旷工" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item label="打卡方式">
            <el-input v-model="editForm.checkInMethod" placeholder="请输入打卡方式" />
          </el-form-item>
          <el-form-item label="打卡地点">
            <el-input v-model="editForm.location" placeholder="请输入打卡地点" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveEdit">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { unwrapListResponse, unwrapPageResponse } from '../../api'
import { hrApi, type AttendanceRecord, type Department, type Employee } from '../../api/hr'

// 加载状态
const loading = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 日期范围
const today = new Date().toISOString().split('T')[0] || ''
const dateRange = ref<string[]>([today, today])

// 部门数据
const departments = ref<Department[]>([])

// 员工数据
const employees = ref<Employee[]>([])

// 员工ID到员工信息的映射
const employeeMap = computed(() => {
  const map: Record<number, Employee> = {}
  employees.value.forEach(emp => {
    if (emp.id) {
      map[emp.id] = emp
    }
  })
  return map
})

// 部门ID到部门名称的映射
const departmentMap = computed(() => {
  const map: Record<number, string> = {}
  departments.value.forEach(dept => {
    if (dept.id) {
      map[dept.id] = dept.name
    }
  })
  return map
})

// 搜索和筛选条件
const searchForm = ref({
  departmentId: '',
  employeeName: '',
  status: ''
})

// 详情弹窗可见性
const detailDialogVisible = ref(false)

// 编辑弹窗可见性
const editDialogVisible = ref(false)

// 编辑表单引用
const editFormRef = ref()

// 编辑表单数据
const editForm = ref({
  checkInTime: '',
  checkOutTime: '',
  status: 0,
  checkInMethod: '',
  location: ''
})

// 当前选中的考勤记录
const currentAttendanceRecord = ref<any>(null)

// 获取部门数据
const fetchDepartments = async () => {
  try {
    console.log('调用API获取部门数据...')
    const res = await hrApi.department.getAll()
    console.log('API返回完整响应:', res)
    departments.value = unwrapListResponse<Department>(res)
    console.log('获取到的部门数据:', departments.value)
  } catch (error) {
    console.error('获取部门数据失败:', error)
    ElMessage.error('获取部门数据失败')
    // 发生错误时，确保数据是数组
    departments.value = []
  }
}

// 获取员工数据
const fetchEmployees = async () => {
  try {
    // 直接调用getAllEmployees获取所有员工，确保数据完整
    console.log('调用API获取员工数据...')
    const res = await hrApi.employee.getAll()
    console.log('API返回完整响应:', res)
    employees.value = unwrapListResponse<Employee>(res)
    console.log('获取到的员工数据:', employees.value)
  } catch (error) {
    console.error('获取员工数据失败:', error)
    ElMessage.error('获取员工数据失败')
    employees.value = []
  }
}

// 考勤记录列表
const attendanceList = ref<Array<{
  id: number;
  employeeName: string;
  departmentName: string;
  date: string;
  checkInTime: string;
  checkOutTime: string;
  workHours: string;
  status: number;
  checkInMethod: string;
  location: string;
}>>([])

// 组件挂载时获取数据
onMounted(() => {
  fetchDepartments()
  fetchEmployees()
  fetchData()
})

// 获取考勤状态类型
const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'success', // 正常
    1: 'warning', // 迟到
    2: 'warning', // 早退
    3: 'danger',  // 缺勤
    4: 'info',    // 请假
    5: 'danger'   // 旷工
  }
  return typeMap[status] || 'info'
}

// 获取考勤状态文本
const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '正常',
    1: '迟到',
    2: '早退',
    3: '缺勤',
    4: '请假',
    5: '旷工'
  }
  return textMap[status] || '未知'
}

// 获取考勤数据
const fetchData = async () => {
  loading.value = true
  try {
    // 构建搜索参数
    const searchParams: any = {}
    
    // 添加日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      searchParams.startDate = dateRange.value[0]
      searchParams.endDate = dateRange.value[1]
    }
    
    // 添加搜索条件
    if (searchForm.value.departmentId) {
      searchParams.departmentId = searchForm.value.departmentId
    }
    if (searchForm.value.employeeName) {
      searchParams.employeeName = searchForm.value.employeeName
    }
    if (searchForm.value.status !== '') {
      searchParams.status = Number(searchForm.value.status)
    }
    
    // 调用API获取数据
    console.log('调用API获取考勤数据:', {
      page: currentPage.value,
      size: pageSize.value,
      ...searchParams
    })
    const res = await hrApi.attendance.getRecordsByPage({
      page: currentPage.value,
      size: pageSize.value,
      ...searchParams
    })
    
    // 打印完整的API响应
    console.log('API返回完整响应:', res)
    
    const page = unwrapPageResponse<any>(res)
    const rawList = page.list
    total.value = page.total

    console.log('处理前的原始列表数据:', rawList)
    
    // 计算工作时长（小时）
    const calculateWorkHours = (checkIn: string, checkOut: string) => {
      if (!checkIn || !checkOut) return '0小时'
      
      const checkInTime = new Date(`2000-01-01T${checkIn}`)
      const checkOutTime = new Date(`2000-01-01T${checkOut}`)
      const diffMs = checkOutTime.getTime() - checkInTime.getTime()
      const hours = (diffMs / (1000 * 60 * 60)).toFixed(2)
      return `${hours}小时`
    }
    
    // 数据转换，从API返回的数据中获取员工姓名和部门名称
    attendanceList.value = rawList.map((item: any) => {
      // 格式化日期
      const formatDate = (date: any) => {
        if (!date) return ''
        if (typeof date === 'object' && date !== null && date.year) {
          // 处理Java LocalDate格式：{year: 2025, month: 12, day: 5}
          return `${date.year}-${String(date.month).padStart(2, '0')}-${String(date.day).padStart(2, '0')}`
        }
        const parsedDate = new Date(date)
        return isNaN(parsedDate.getTime()) ? '' : parsedDate.toISOString().split('T')[0]
      }
      
      // 格式化时间
      const formatTime = (time: any) => {
        if (!time) return '-'
        if (typeof time === 'object' && time !== null && time.hour) {
          // 处理Java LocalDateTime格式：{year: 2025, month: 12, day: 5, hour: 8, minute: 50, second: 0}
          return `${String(time.hour).padStart(2, '0')}:${String(time.minute).padStart(2, '0')}:${String(time.second || 0).padStart(2, '0')}`
        }
        const parsedTime = new Date(time)
        return isNaN(parsedTime.getTime()) ? '-' : parsedTime.toLocaleTimeString('zh-CN', { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' })
      }
      
      // 获取员工姓名和部门名称
      let employeeName = '未知'
      let departmentName = '未知'
      
      // 优先从关联的employee对象中获取信息
      if (item.employee) {
        employeeName = item.employee.name || '未知'
        if (item.employee.department) {
          departmentName = item.employee.department.name || '未知'
        } else if (item.employee.departmentId) {
          // 如果employee对象中只有departmentId，从departmentMap中查找
          departmentName = departmentMap.value[item.employee.departmentId] || '未知'
        }
      } 
      // 如果直接有employeeName和departmentName字段，直接使用
      else if (item.employeeName) {
        employeeName = item.employeeName
        departmentName = item.departmentName || '未知'
      } 
      // 最后尝试通过employeeId去employeeMap中查找
      else if (item.employeeId) {
        const employee = employeeMap.value[item.employeeId]
        if (employee) {
          employeeName = employee.name
          if (employee.departmentId) {
            departmentName = departmentMap.value[employee.departmentId] || '未知'
          }
        }
      }
      
      // 获取打卡时间字符串，用于计算工作时长
      const checkInTimeStr = formatTime(item.checkInTime)
      const checkOutTimeStr = formatTime(item.checkOutTime)
      
      return {
        id: item.id,
        employeeName,
        departmentName,
        date: formatDate(item.attendanceDate) || '',
        checkInTime: checkInTimeStr,
        checkOutTime: checkOutTimeStr,
        workHours: calculateWorkHours(checkInTimeStr, checkOutTimeStr),
        status: item.status,
        checkInMethod: item.checkInMethod || '-',
        location: item.location || '-'
      }
    })
    
    console.log('处理后的考勤列表:', attendanceList.value)
    console.log('总记录数:', total.value)
  } catch (error) {
    console.error('获取考勤数据失败:', error)
    ElMessage.error('获取考勤数据失败')
    attendanceList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

// 导出
const handleExport = async () => {
  try {
    // 构建搜索参数
    const searchParams: any = {}    
    // 添加日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      searchParams.startDate = dateRange.value[0]
      searchParams.endDate = dateRange.value[1]
    }
    // 添加搜索条件
    if (searchForm.value.departmentId) {
      searchParams.departmentId = searchForm.value.departmentId
    }
    if (searchForm.value.employeeName) {
      searchParams.employeeName = searchForm.value.employeeName
    }
    if (searchForm.value.status !== '') {
      searchParams.status = Number(searchForm.value.status)
    }
    
    // 调用API导出数据
    // 这里模拟导出逻辑，实际应调用后端API生成并下载文件
    console.log('导出考勤数据:', searchParams)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 刷新
const handleRefresh = () => {
  fetchData()
  ElMessage.success('数据已刷新')
}

// 查看详情
const handleViewDetail = (row: any) => {
  currentAttendanceRecord.value = row
  detailDialogVisible.value = true
}

// 编辑考勤记录
const handleEdit = (row: any) => {
  currentAttendanceRecord.value = row
  // 填充表单数据
  editForm.value = {
    checkInTime: row.checkInTime,
    checkOutTime: row.checkOutTime,
    status: row.status,
    checkInMethod: row.checkInMethod,
    location: row.location
  }
  editDialogVisible.value = true
}

// 保存编辑
const handleSaveEdit = async () => {
  try {
    // 调用API保存编辑
    if (currentAttendanceRecord.value) {
      await hrApi.attendance.updateRecord(currentAttendanceRecord.value.id, {
        ...currentAttendanceRecord.value, // 包含 employeeId 和 attendanceDate
        ...editForm.value
      } as AttendanceRecord)
      // 关闭弹窗
      editDialogVisible.value = false
      ElMessage.success('保存成功')
      // 刷新数据
      fetchData()
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}
</script>

<style scoped>
.attendance-data-view {
  padding: 20px;
}
.toolbar {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
