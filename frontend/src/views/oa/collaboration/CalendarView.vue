<template>
  <div class="calendar-view">
    <!-- 页面操作栏 -->
    <div class="page-header">
      <div class="header-actions">
        <el-button type="primary" @click="openEventDialog">
          <el-icon><Plus /></el-icon>
          新建日程
        </el-button>
        <el-dropdown trigger="click">
          <el-button>
            <el-icon><Calendar /></el-icon>
            {{ viewMode }}<el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="changeViewMode('day')">日视图</el-dropdown-item>
              <el-dropdown-item @click="changeViewMode('week')">周视图</el-dropdown-item>
              <el-dropdown-item @click="changeViewMode('month')">月视图</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 日历和日程列表 -->
    <div class="calendar-content">
      <!-- 日历视图 -->
      <div class="calendar-main">
        <div class="calendar-header">
          <div class="calendar-nav">
            <el-button link @click="prev">
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <span class="current-date">{{ currentDate }}</span>
            <el-button link @click="next">
              <el-icon><ArrowRight /></el-icon>
            </el-button>
            <el-button type="primary" size="small" @click="today">今天</el-button>
          </div>
        </div>
        
        <!-- 模拟日历视图 -->
        <div class="calendar-grid">
          <div class="calendar-weekdays">
            <div class="weekday" v-for="day in weekdays" :key="day">{{ day }}</div>
          </div>
          <div class="calendar-days">
            <div
              v-for="day in days"
              :key="day.date.toISOString()"
              class="day"
              :class="{ 'current-day': day.isToday, 'other-month': !day.isCurrentMonth }"
              @click="selectDay(day)"
            >
              <div class="day-number">{{ day.date.getDate() }}</div>
              <div class="events">
                <div
                  v-for="event in day.events"
                  :key="event.id"
                  class="event-item"
                  :class="`event-type-${event.eventType}`"
                  @click.stop="viewEvent(event)"
                >
                  {{ event.title }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧日程列表 -->
      <div class="calendar-sidebar">
        <div class="sidebar-section">
          <div class="section-header">
            <h3>今日日程</h3>
            <span class="event-count">{{ todayEvents.length }} 个</span>
          </div>
          <div class="event-list">
            <div
              v-for="event in todayEvents"
              :key="event.id"
              class="event-item"
              :class="`event-type-${event.eventType}`"
              @click="viewEvent(event)"
            >
              <div class="event-time">{{ formatTime(event.startTime) }} - {{ formatTime(event.endTime) }}</div>
              <div class="event-title">{{ event.title }}</div>
              <div class="event-location" v-if="event.location">
                <el-icon><Location /></el-icon>
                {{ event.location }}
              </div>
            </div>
            <div v-if="todayEvents.length === 0" class="no-events">
              <el-icon class="no-events-icon"><Calendar /></el-icon>
              <p>今日暂无日程</p>
            </div>
          </div>
        </div>
        
        <div class="sidebar-section">
          <div class="section-header">
            <h3>日程分类</h3>
          </div>
          <div class="event-categories">
            <div class="category-item">
              <el-tag type="info" size="small">会议</el-tag>
              <span class="category-count">{{ meetingCount }}</span>
            </div>
            <div class="category-item">
              <el-tag type="success" size="small">任务</el-tag>
              <span class="category-count">{{ taskCount }}</span>
            </div>
            <div class="category-item">
              <el-tag type="warning" size="small">提醒</el-tag>
              <span class="category-count">{{ reminderCount }}</span>
            </div>
            <div class="category-item">
              <el-tag type="danger" size="small">生日</el-tag>
              <span class="category-count">{{ birthdayCount }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 新建/编辑日程对话框 -->
    <el-dialog
      v-model="eventDialogVisible"
      :title="eventDialogTitle"
      width="600px"
    >
      <el-form :model="eventForm" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="eventForm.title" placeholder="输入日程标题" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="eventForm.eventType" placeholder="选择日程类型">
            <el-option label="会议" value="meeting" />
            <el-option label="任务" value="task" />
            <el-option label="提醒" value="reminder" />
            <el-option label="生日" value="birthday" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker
            v-model="eventForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker
            v-model="eventForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="eventForm.location" placeholder="输入地点" />
        </el-form-item>
        <el-form-item label="参与人">
          <el-select
            v-model="eventForm.participants"
            multiple
            placeholder="选择参与人"
            style="width: 100%"
          >
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.userName"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒方式">
          <el-select v-model="eventForm.remindType" placeholder="选择提醒方式">
            <el-option label="不提醒" value="none" />
            <el-option label="提前15分钟" value="15min" />
            <el-option label="提前30分钟" value="30min" />
            <el-option label="提前1小时" value="1hour" />
            <el-option label="提前1天" value="1day" />
          </el-select>
        </el-form-item>
        <el-form-item label="重复规则">
          <el-select v-model="eventForm.repeatRule" placeholder="选择重复规则">
            <el-option label="不重复" value="none" />
            <el-option label="每日" value="daily" />
            <el-option label="每周" value="weekly" />
            <el-option label="每月" value="monthly" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="eventForm.description"
            type="textarea"
            placeholder="输入描述"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="eventDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEvent">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Calendar, ArrowDown, ArrowLeft, ArrowRight, Location } from '@element-plus/icons-vue'

// 视图模式
const viewMode = ref('月视图')

// 当前日期
const currentDate = ref('2025年12月')

// 星期
const weekdays = ref(['日', '一', '二', '三', '四', '五', '六'])

// 模拟日历天数
const days = ref([
  { date: new Date(2025, 11, 1), isCurrentMonth: true, isToday: false, events: [] },
  { date: new Date(2025, 11, 2), isCurrentMonth: true, isToday: false, events: [] },
  { date: new Date(2025, 11, 3), isCurrentMonth: true, isToday: false, events: [{ id: 1, title: '团队会议', eventType: 'meeting' }] },
  { date: new Date(2025, 11, 4), isCurrentMonth: true, isToday: false, events: [] },
  { date: new Date(2025, 11, 5), isCurrentMonth: true, isToday: false, events: [{ id: 2, title: '项目进度', eventType: 'task' }] },
  { date: new Date(2025, 11, 6), isCurrentMonth: true, isToday: false, events: [] },
  { date: new Date(2025, 11, 7), isCurrentMonth: true, isToday: false, events: [] },
  { date: new Date(2025, 11, 8), isCurrentMonth: true, isToday: false, events: [{ id: 3, title: '客户拜访', eventType: 'meeting' }] },
  { date: new Date(2025, 11, 9), isCurrentMonth: true, isToday: false, events: [{ id: 4, title: '提交报告', eventType: 'task' }] },
  { date: new Date(2025, 11, 10), isCurrentMonth: true, isToday: false, events: [] },
  { date: new Date(2025, 11, 11), isCurrentMonth: true, isToday: false, events: [{ id: 5, title: '技术评审', eventType: 'meeting' }] },
  { date: new Date(2025, 11, 12), isCurrentMonth: true, isToday: false, events: [] },
  { date: new Date(2025, 11, 13), isCurrentMonth: true, isToday: false, events: [] },
  { date: new Date(2025, 11, 14), isCurrentMonth: true, isToday: false, events: [{ id: 6, title: '生日', eventType: 'birthday' }] },
  { date: new Date(2025, 11, 15), isCurrentMonth: true, isToday: false, events: [{ id: 7, title: '团队建设', eventType: 'meeting' }] },
  { date: new Date(2025, 11, 16), isCurrentMonth: true, isToday: true, events: [{ id: 8, title: '产品演示', eventType: 'meeting' }, { id: 9, title: '代码审查', eventType: 'task' }] },
  { date: new Date(2025, 11, 17), isCurrentMonth: true, isToday: false, events: [{ id: 10, title: '周会', eventType: 'meeting' }] },
  // 更多日期...
])

// 模拟事件列表
const events = ref([
  {
    id: 8,
    eventType: 'meeting',
    title: '产品演示',
    description: '向客户演示新产品功能',
    startTime: new Date(2025, 11, 16, 14, 0),
    endTime: new Date(2025, 11, 16, 15, 30),
    location: '会议室A',
    creatorId: 1,
    participants: [1, 2, 3],
    remindType: '1hour',
    repeatRule: 'none',
    status: 'pending'
  },
  {
    id: 9,
    eventType: 'task',
    title: '代码审查',
    description: '审查团队成员的代码',
    startTime: new Date(2025, 11, 16, 16, 0),
    endTime: new Date(2025, 11, 16, 17, 30),
    location: '办公室',
    creatorId: 1,
    participants: [1, 4],
    remindType: '30min',
    repeatRule: 'none',
    status: 'todo'
  }
])

// 今日事件
const todayEvents = computed(() => {
  return events.value
})

// 事件统计
const meetingCount = computed(() => {
  return events.value.filter(event => event.eventType === 'meeting').length
})

const taskCount = computed(() => {
  return events.value.filter(event => event.eventType === 'task').length
})

const reminderCount = computed(() => {
  return events.value.filter(event => event.eventType === 'reminder').length
})

const birthdayCount = computed(() => {
  return events.value.filter(event => event.eventType === 'birthday').length
})

// 用户列表
const users = ref([
  { id: 1, userName: '张三', department: '技术部' },
  { id: 2, userName: '李四', department: '销售部' },
  { id: 3, userName: '王五', department: '财务部' },
  { id: 4, userName: '赵六', department: '运营部' },
  { id: 5, userName: '孙七', department: '人事部' }
])

// 事件对话框
const eventDialogVisible = ref(false)
const eventDialogTitle = ref('新建日程')
const eventForm = reactive({
  id: 0,
  eventType: 'meeting',
  title: '',
  description: '',
  startTime: new Date(),
  endTime: new Date(),
  location: '',
  creatorId: 1,
  participants: [],
  remindType: '15min',
  repeatRule: 'none',
  status: 'pending'
})

// 切换视图模式
const changeViewMode = (mode: string) => {
  const modeMap: Record<string, string> = {
    day: '日视图',
    week: '周视图',
    month: '月视图'
  }
  viewMode.value = modeMap[mode] || '月视图'
  console.log('切换视图模式:', mode)
}

// 上一页
const prev = () => {
  console.log('上一页')
}

// 下一页
const next = () => {
  console.log('下一页')
}

// 今天
const today = () => {
  console.log('今天')
}

// 选择日期
const selectDay = (day: any) => {
  console.log('选择日期:', day)
}

// 打开事件对话框
const openEventDialog = () => {
  eventDialogTitle.value = '新建日程'
  resetEventForm()
  eventDialogVisible.value = true
}

// 重置事件表单
const resetEventForm = () => {
  Object.assign(eventForm, {
    id: 0,
    eventType: 'meeting',
    title: '',
    description: '',
    startTime: new Date(),
    endTime: new Date(),
    location: '',
    participants: [],
    remindType: '15min',
    repeatRule: 'none',
    status: 'pending'
  })
}

// 查看事件
const viewEvent = (event: any) => {
  eventDialogTitle.value = '编辑日程'
  Object.assign(eventForm, event)
  eventDialogVisible.value = true
}

// 保存事件
const saveEvent = () => {
  console.log('保存事件:', eventForm)
  // 这里应该调用API保存事件
  ElMessage.success('日程保存成功')
  eventDialogVisible.value = false
}

// 格式化时间
const formatTime = (date: Date) => {
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 组件挂载时初始化
onMounted(() => {
  // 初始化日历数据
})
</script>

<style scoped>
.calendar-view {
  padding: 20px;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.calendar-content {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
}

.calendar-main {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.calendar-header {
  margin-bottom: 20px;
}

.calendar-nav {
  display: flex;
  align-items: center;
  gap: 12px;
}

.current-date {
  font-size: 18px;
  font-weight: bold;
  min-width: 120px;
  text-align: center;
}

.calendar-grid {
  width: 100%;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background-color: #f0f0f0;
  margin-bottom: 1px;
}

.weekday {
  padding: 12px;
  text-align: center;
  background-color: #fafafa;
  font-weight: bold;
  color: #606266;
}

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background-color: #f0f0f0;
  height: 500px;
}

.day {
  background-color: #fff;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
}

.day:hover {
  background-color: #f5f7fa;
}

.day.current-day {
  background-color: #ecf5ff;
}

.day.other-month {
  color: #c0c4cc;
}

.day-number {
  font-size: 14px;
  text-align: right;
  font-weight: bold;
}

.events {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.event-item {
  padding: 4px 6px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: all 0.3s ease;
}

.event-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.event-type-meeting {
  background-color: #e6f7ff;
  color: #1890ff;
}

.event-type-task {
  background-color: #f6ffed;
  color: #52c41a;
}

.event-type-reminder {
  background-color: #fff7e6;
  color: #fa8c16;
}

.event-type-birthday {
  background-color: #fff2f0;
  color: #f5222d;
}

.calendar-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
}

.event-count {
  font-size: 14px;
  color: #909399;
}

.event-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.event-list .event-item {
  padding: 12px;
  border-left: 4px solid #409eff;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.event-time {
  font-weight: bold;
  color: #606266;
}

.event-title {
  color: #303133;
}

.event-location {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.no-events {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}

.no-events-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.no-events p {
  margin: 0;
}

.event-categories {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: #fafafa;
  border-radius: 4px;
}

.category-count {
  font-size: 14px;
  color: #606266;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .calendar-content {
    grid-template-columns: 1fr;
  }
  
  .calendar-sidebar {
    order: -1;
  }
}

@media (max-width: 768px) {
  .calendar-view {
    padding: 12px;
  }
  
  .calendar-main {
    padding: 12px;
  }
  
  .calendar-nav {
    gap: 8px;
  }
  
  .current-date {
    font-size: 16px;
    min-width: 100px;
  }
  
  .day {
    padding: 4px;
  }
  
  .event-item {
    font-size: 11px;
    padding: 2px 4px;
  }
}
</style>