<template>
  <div class="attendance-view">
    <!-- 考勤概览卡片 -->
    <div class="overview-section">
      <el-card shadow="hover">
        <div class="overview-content">
          <div class="overview-item">
            <div class="overview-label">今日状态</div>
            <div class="overview-value">
              <el-tag :type="todayStatus === 'normal' ? 'success' : 'danger'">
                {{ todayStatus === 'normal' ? '已打卡' : '未打卡' }}
              </el-tag>
            </div>
          </div>
          <div class="overview-item">
            <div class="overview-label">本月出勤</div>
            <div class="overview-value">{{ attendanceStats.monthlyAttendance }}天</div>
          </div>
          <div class="overview-item">
            <div class="overview-label">本月迟到</div>
            <div class="overview-value">{{ attendanceStats.monthlyLate }}次</div>
          </div>
          <div class="overview-item">
            <div class="overview-label">本月早退</div>
            <div class="overview-value">{{ attendanceStats.monthlyEarlyLeave }}次</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 打卡区域 -->
    <div class="clock-in-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>打卡</span>
            <div class="location-info">
              <el-icon><Location /></el-icon>
              <span>{{ currentLocation }}</span>
            </div>
          </div>
        </template>
        <div class="clock-in-content">
          <div class="clock-in-status">
            <div class="clock-in-time">
              <span class="time-label">当前时间</span>
              <span class="time-value">{{ currentTime }}</span>
            </div>
            <div class="clock-in-records">
              <div class="record-item">
                <span class="record-label">上班打卡：</span>
                <span class="record-value">{{ todayClockIn.attendanceTime || '未打卡' }}</span>
              </div>
              <div class="record-item">
                <span class="record-label">下班打卡：</span>
                <span class="record-value">{{ todayClockOut.attendanceTime || '未打卡' }}</span>
              </div>
            </div>
          </div>
          <div class="clock-in-buttons">
            <el-button
              type="primary"
              size="large"
              :disabled="!canClockIn"
              @click="clockIn"
              v-if="!todayClockIn.attendanceTime"
            >
              <el-icon><Clock /></el-icon>
              上班打卡
            </el-button>
            <el-button
              type="success"
              size="large"
              :disabled="!canClockOut"
              @click="clockOut"
              v-if="todayClockIn.attendanceTime && !todayClockOut.attendanceTime"
            >
              <el-icon><Clock /></el-icon>
              下班打卡
            </el-button>
            <el-button
              type="info"
              size="large"
              disabled
              v-else
            >
              今日已完成打卡
            </el-button>
          </div>
          <div class="clock-in-tips">
            <el-alert
              title="打卡提示"
              type="info"
              description="请确保在工作地点范围内打卡，打卡记录将自动同步到考勤系统"
              show-icon
              :closable="false"
            />
          </div>
        </div>
      </el-card>
    </div>

    <!-- 考勤记录 -->
    <div class="records-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>考勤记录</span>
            <div class="filter-actions">
              <el-select v-model="selectedMonth" placeholder="选择月份" style="width: 120px;">
                <el-option label="12月" value="12" />
                <el-option label="11月" value="11" />
                <el-option label="10月" value="10" />
              </el-select>
              <el-button size="small" @click="refreshRecords">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </div>
        </template>
        <div class="records-content">
          <el-table :data="filteredRecords" stripe style="width: 100%">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="dayOfWeek" label="星期" width="80" />
            <el-table-column prop="clockIn" label="上班打卡" width="150">
              <template #default="scope">
                <span :class="scope.row.isLate ? 'late-record' : ''">
                  {{ scope.row.clockIn }}
                  <el-tag size="small" type="danger" v-if="scope.row.isLate">迟到</el-tag>
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="clockOut" label="下班打卡" width="150">
              <template #default="scope">
                <span :class="scope.row.isEarlyLeave ? 'early-record' : ''">
                  {{ scope.row.clockOut }}
                  <el-tag size="small" type="danger" v-if="scope.row.isEarlyLeave">早退</el-tag>
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="workHours" label="工作时长" width="120" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="attendanceStatusTypeMap[scope.row.status]">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination" v-if="filteredRecords.length > 0">
            <el-pagination
              layout="prev, pager, next"
              :total="filteredRecords.length"
              :page-size="10"
              :current-page="currentPage"
              @current-change="currentPage = $event"
            />
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Clock, Refresh, Location } from '@element-plus/icons-vue'

// 定义考勤记录类型
interface AttendanceRecord {
  id: number
  date: string
  dayOfWeek: string
  clockIn: string
  clockOut: string
  workHours: string
  isLate: boolean
  isEarlyLeave: boolean
  status: 'normal' | 'late' | 'early_leave' | 'absent' | 'leave'
}

// 定义打卡记录类型
interface ClockRecord {
  id: number
  attendanceType: 'clock_in' | 'clock_out'
  attendanceTime: string
  location: string
  status: 'normal' | 'late' | 'early_leave'
}

// 模拟数据 - 考勤记录
const attendanceRecords = ref<AttendanceRecord[]>([
  {
    id: 1,
    date: '2025-12-17',
    dayOfWeek: '周三',
    clockIn: '08:30',
    clockOut: '',
    workHours: '00:00',
    isLate: false,
    isEarlyLeave: false,
    status: 'normal'
  },
  {
    id: 2,
    date: '2025-12-16',
    dayOfWeek: '周二',
    clockIn: '08:45',
    clockOut: '17:30',
    workHours: '08:45',
    isLate: true,
    isEarlyLeave: false,
    status: 'late'
  },
  {
    id: 3,
    date: '2025-12-15',
    dayOfWeek: '周一',
    clockIn: '08:30',
    clockOut: '17:00',
    workHours: '08:30',
    isLate: false,
    isEarlyLeave: true,
    status: 'early_leave'
  },
  {
    id: 4,
    date: '2025-12-14',
    dayOfWeek: '周日',
    clockIn: '',
    clockOut: '',
    workHours: '00:00',
    isLate: false,
    isEarlyLeave: false,
    status: 'absent'
  },
  {
    id: 5,
    date: '2025-12-13',
    dayOfWeek: '周六',
    clockIn: '',
    clockOut: '',
    workHours: '00:00',
    isLate: false,
    isEarlyLeave: false,
    status: 'absent'
  },
  {
    id: 6,
    date: '2025-12-12',
    dayOfWeek: '周五',
    clockIn: '08:25',
    clockOut: '17:45',
    workHours: '09:20',
    isLate: false,
    isEarlyLeave: false,
    status: 'normal'
  },
  {
    id: 7,
    date: '2025-12-11',
    dayOfWeek: '周四',
    clockIn: '08:35',
    clockOut: '17:30',
    workHours: '08:55',
    isLate: true,
    isEarlyLeave: false,
    status: 'late'
  },
  {
    id: 8,
    date: '2025-12-10',
    dayOfWeek: '周三',
    clockIn: '08:30',
    clockOut: '17:30',
    workHours: '09:00',
    isLate: false,
    isEarlyLeave: false,
    status: 'normal'
  }
])

// 当前时间
const currentTime = ref('')
// 当前位置
const currentLocation = ref('北京市朝阳区XX大厦')
// 当前月份
const selectedMonth = ref('12')
// 当前页码
const currentPage = ref(1)

// 考勤记录
const todayDate: string = (new Date().toISOString().split('T')[0]) || ''
const todayClockIn = ref<ClockRecord>({
  id: 0,
  attendanceType: 'clock_in',
  attendanceTime: '',
  location: currentLocation.value,
  status: 'normal'
})
const todayClockOut = ref<ClockRecord>({
  id: 0,
  attendanceType: 'clock_out',
  attendanceTime: '',
  location: currentLocation.value,
  status: 'normal'
})

// 初始化今日打卡记录
const initTodayClockRecords = () => {
  const todayRecord = attendanceRecords.value.find(r => r.date === todayDate)
  if (todayRecord) {
    if (todayRecord.clockIn) {
      todayClockIn.value.attendanceTime = todayRecord.clockIn
    }
    if (todayRecord.clockOut) {
      todayClockOut.value.attendanceTime = todayRecord.clockOut
    }
  }
}

// 计算今日状态
const todayStatus = computed(() => {
  return todayClockIn.value.attendanceTime ? 'normal' : 'absent'
})

// 计算是否可以打卡
const canClockIn = ref(true)
const canClockOut = ref(true)

// 考勤统计
const attendanceStats = ref({
  monthlyAttendance: 12,
  monthlyLate: 2,
  monthlyEarlyLeave: 1
})

// 考勤状态类型映射
const attendanceStatusTypeMap: Record<string, string> = {
  normal: 'success',
  late: 'warning',
  early_leave: 'warning',
  absent: 'danger',
  leave: 'info'
}

// 计算筛选后的考勤记录
const filteredRecords = computed(() => {
  return attendanceRecords.value.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
})

// 更新当前时间
const updateCurrentTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString()
}

// 上班打卡
const clockIn = () => {
  const now = new Date()
  const timeString = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  todayClockIn.value.attendanceTime = timeString
  todayClockIn.value.location = currentLocation.value
  
  // 更新考勤记录
  const todayRecord = attendanceRecords.value.find(r => r.date === todayDate)
  if (todayRecord) {
    todayRecord.clockIn = timeString
    todayRecord.isLate = now.getHours() > 8 || (now.getHours() === 8 && now.getMinutes() > 30)
  } else {
    attendanceRecords.value.unshift({
      id: attendanceRecords.value.length + 1,
      date: todayDate,
      dayOfWeek: '周三',
      clockIn: timeString,
      clockOut: '',
      workHours: '00:00',
      isLate: now.getHours() > 8 || (now.getHours() === 8 && now.getMinutes() > 30),
      isEarlyLeave: false,
      status: 'normal'
    })
  }
  
  // 更新统计数据
  attendanceStats.value.monthlyAttendance++
}

// 下班打卡
const clockOut = () => {
  const now = new Date()
  const timeString = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  todayClockOut.value.attendanceTime = timeString
  todayClockOut.value.location = currentLocation.value
  
  // 更新考勤记录
  const todayRecord = attendanceRecords.value.find(r => r.date === todayDate)
  if (todayRecord) {
    todayRecord.clockOut = timeString
    todayRecord.isEarlyLeave = now.getHours() < 17 || (now.getHours() === 17 && now.getMinutes() < 30)
    // 计算工作时长
    const clockInTime = new Date(`2025-12-17T${todayRecord.clockIn}`)
    const clockOutTime = now
    const diffHours = Math.floor((clockOutTime.getTime() - clockInTime.getTime()) / (1000 * 60 * 60))
    const diffMinutes = Math.floor((clockOutTime.getTime() - clockInTime.getTime()) / (1000 * 60)) % 60
    todayRecord.workHours = `${diffHours.toString().padStart(2, '0')}:${diffMinutes.toString().padStart(2, '0')}`
  }
}

// 刷新记录
const refreshRecords = () => {
  // 这里可以添加刷新逻辑
  console.log('刷新考勤记录')
}

// 初始化
onMounted(() => {
  updateCurrentTime()
  setInterval(updateCurrentTime, 1000)
  initTodayClockRecords()
})
</script>

<style scoped>
.attendance-view {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: #f5f7fa;
}

.overview-section {
  margin-bottom: 20px;
}

.overview-content {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.overview-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.overview-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.overview-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.clock-in-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.location-info {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.location-info .el-icon {
  margin-right: 5px;
}

.clock-in-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.clock-in-status {
  text-align: center;
  margin-bottom: 30px;
}

.clock-in-time {
  margin-bottom: 20px;
}

.time-label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.time-value {
  font-size: 48px;
  font-weight: bold;
  color: #303133;
}

.clock-in-records {
  margin-top: 20px;
}

.record-item {
  margin-bottom: 10px;
  font-size: 16px;
}

.record-label {
  margin-right: 10px;
  color: #606266;
}

.record-value {
  font-weight: bold;
  color: #303133;
}

.clock-in-buttons {
  margin-bottom: 20px;
  width: 100%;
  display: flex;
  justify-content: center;
}

.clock-in-buttons .el-button {
  width: 200px;
}

.clock-in-tips {
  width: 100%;
  margin-top: 20px;
}

.records-section {
  margin-bottom: 20px;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.late-record {
  color: #f56c6c;
}

.early-record {
  color: #e6a23c;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
