<template>
  <div class="meeting-management-view">
    <!-- 会议统计概览 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">今日会议</div>
              <div class="stat-value">{{ stats.todayMeetings }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">本周会议</div>
              <div class="stat-value">{{ stats.weekMeetings }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">待开会议</div>
              <div class="stat-value">{{ stats.upcomingMeetings }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">会议总数</div>
              <div class="stat-value">{{ stats.totalMeetings }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 会议管理操作区 -->
    <div class="operation-section">
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon> 新建会议
      </el-button>
      <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px; margin-left: 10px;">
        <el-option label="全部" value="" />
        <el-option label="待开始" value="pending" />
        <el-option label="进行中" value="ongoing" />
        <el-option label="已完成" value="completed" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-date-picker
        v-model="filterDate"
        type="date"
        placeholder="选择日期"
        value-format="YYYY-MM-DD"
        clearable
        style="width: 200px; margin-left: 10px;"
      />
      <el-input
        v-model="searchKeyword"
        placeholder="搜索会议名称或会议室"
        clearable
        style="width: 300px; margin-left: 10px;"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 会议列表 -->
    <div class="meeting-list-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>会议列表</span>
          </div>
        </template>
        <el-table :data="filteredMeetings" stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="meetingNo" label="会议编号" width="150" />
          <el-table-column prop="meetingTitle" label="会议主题" min-width="200">
            <template #default="scope">
              <div class="meeting-title" @click="showMeetingDetail(scope.row)">
                {{ scope.row.meetingTitle }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="roomName" label="会议室" width="120" />
          <el-table-column prop="startTime" label="开始时间" width="180" />
          <el-table-column prop="endTime" label="结束时间" width="180" />
          <el-table-column prop="organizerName" label="组织者" width="100" />
          <el-table-column label="参与人数" width="100">
            <template #default="scope">
              {{ getAttendeeCount(scope.row) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="showMeetingDetail(scope.row)">详情</el-button>
              <el-button size="small" type="primary" @click="editMeeting(scope.row)" v-if="scope.row.status === 'pending'">编辑</el-button>
              <el-button size="small" type="success" @click="startMeeting(scope.row)" v-if="scope.row.status === 'pending'">开始会议</el-button>
              <el-button size="small" type="warning" @click="endMeeting(scope.row)" v-if="scope.row.status === 'ongoing'">结束会议</el-button>
              <el-button size="small" type="danger" @click="cancelMeeting(scope.row)" v-if="scope.row.status === 'pending'">取消会议</el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 分页器 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadMeetingList"
            @current-change="loadMeetingList"
          />
        </div>
      </el-card>
    </div>

    <!-- 会议室列表 -->
    <div class="meeting-rooms-section">
      <el-card shadow="hover" style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <span>会议室列表</span>
          </div>
        </template>
        <div class="meeting-rooms-grid" v-loading="roomLoading">
          <el-card
            v-for="room in meetingRooms"
            :key="room.id"
            shadow="hover"
            class="room-card"
            :class="{ 'room-available': room.status === 1, 'room-maintenance': room.status !== 1 }"
          >
            <template #header>
              <div class="room-header">
                <h4>{{ room.roomName }}</h4>
                <el-tag :type="room.status === 1 ? 'success' : 'danger'">{{ room.status === 1 ? '可用' : '禁用' }}</el-tag>
              </div>
            </template>
            <div class="room-info">
              <div class="info-item">
                <span class="info-label">位置：</span>
                <span class="info-value">{{ room.location }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">容量：</span>
                <span class="info-value">{{ room.capacity }}人</span>
              </div>
              <div class="info-item">
                <span class="info-label">设施：</span>
                <span class="info-value">{{ room.equipment }}</span>
              </div>
            </div>
            <div class="room-actions">
              <el-button size="small" type="primary" @click="bookMeetingRoom(room)" :disabled="room.status !== 1">
                预约
              </el-button>
            </div>
          </el-card>
        </div>
      </el-card>
    </div>

    <!-- 创建/编辑会议对话框 -->
    <el-dialog
      v-model="showCreateMeetingDialog"
      :title="editingMeeting ? '编辑会议' : '新建会议'"
      width="700px"
    >
      <el-form :model="meetingForm" label-width="100px">
        <el-form-item label="会议主题" required>
          <el-input v-model="meetingForm.meetingTitle" placeholder="请输入会议主题" />
        </el-form-item>
        <el-form-item label="会议议程">
          <el-input
            v-model="meetingForm.agenda"
            type="textarea"
            placeholder="请输入会议议程"
            :rows="4"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="会议室" required>
              <el-select v-model="meetingForm.roomId" placeholder="请选择会议室" @change="onRoomChange">
                <el-option
                  v-for="room in meetingRooms"
                  :key="room.id"
                  :label="room.roomName"
                  :value="room.id"
                  :disabled="room.status !== 1"
                >
                  {{ room.roomName }} ({{ room.capacity }}人)
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="参与人">
              <el-input v-model="meetingForm.attendees" placeholder="请输入参与人，多个用逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" required>
              <el-date-picker
                v-model="meetingForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" required>
              <el-date-picker
                v-model="meetingForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="meetingForm.remark" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateMeetingDialog = false">取消</el-button>
          <el-button type="primary" @click="saveMeeting" :loading="saving">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 会议详情对话框 -->
    <el-dialog
      v-model="showMeetingDetailDialog"
      title="会议详情"
      width="800px"
    >
      <div v-if="selectedMeeting" class="meeting-detail">
        <div class="detail-header">
          <h3>{{ selectedMeeting.meetingTitle }}</h3>
          <el-tag :type="getStatusTagType(selectedMeeting.status)">{{ getStatusLabel(selectedMeeting.status) }}</el-tag>
        </div>
        <div class="detail-info">
          <div class="info-item">
            <span class="info-label">会议编号：</span>
            <span class="info-value">{{ selectedMeeting.meetingNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">会议室：</span>
            <span class="info-value">{{ selectedMeeting.roomName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">开始时间：</span>
            <span class="info-value">{{ selectedMeeting.startTime }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">结束时间：</span>
            <span class="info-value">{{ selectedMeeting.endTime }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">组织者：</span>
            <span class="info-value">{{ selectedMeeting.organizerName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">参与人数：</span>
            <span class="info-value">{{ getAttendeeCount(selectedMeeting) }}人</span>
          </div>
          <div class="info-item" v-if="selectedMeeting.remark">
            <span class="info-label">备注：</span>
            <span class="info-value">{{ selectedMeeting.remark }}</span>
          </div>
        </div>
        <div class="detail-description">
          <h4>会议议程</h4>
          <p>{{ selectedMeeting.agenda || '暂无会议议程' }}</p>
        </div>
        <div class="detail-participants">
          <h4>参与人</h4>
          <div class="participants-list">
            <el-tag v-for="attendee in getAttendeeList(selectedMeeting)" :key="attendee" style="margin-right: 5px; margin-bottom: 5px;">
              {{ attendee }}
            </el-tag>
            <span v-if="getAttendeeList(selectedMeeting).length === 0">暂无参与人</span>
          </div>
        </div>
        <div class="detail-minutes">
          <h4>会议纪要</h4>
          <el-input
            v-model="meetingMinutes"
            type="textarea"
            placeholder="请输入会议纪要"
            :rows="6"
          />
          <el-button type="primary" @click="saveMinutes" style="margin-top: 10px;">保存会议纪要</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { meetingApi, meetingRoomApi } from '@/api/oa'
import type { Meeting, MeetingRoom } from '@/api/oa'
import { unwrapResponseData, unwrapPageResponse } from '@/api'

// 会议列表数据（来自后端API）
const meetingList = ref<Meeting[]>([])
const loading = ref(false)

// 会议室列表数据（来自后端API）
const meetingRooms = ref<MeetingRoom[]>([])
const roomLoading = ref(false)

// 分页参数
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 筛选条件
const filterStatus = ref('')
const filterDate = ref('')
const searchKeyword = ref('')

// 会议状态映射（状态值 -> 中文标签与标签类型）
const meetingStatusMap: Record<string, { label: string; type: 'warning' | 'success' | 'info' | 'danger' }> = {
  pending: { label: '待开始', type: 'warning' },
  ongoing: { label: '进行中', type: 'success' },
  completed: { label: '已完成', type: 'info' },
  cancelled: { label: '已取消', type: 'danger' }
}

/**
 * 获取会议状态的中文标签
 * @param status 会议状态值
 * @returns 中文状态标签
 */
const getStatusLabel = (status: string): string => {
  return meetingStatusMap[status]?.label || status || '未知'
}

/**
 * 获取会议状态对应的el-tag类型
 * @param status 会议状态值
 * @returns el-tag类型
 */
const getStatusTagType = (status: string): 'warning' | 'success' | 'info' | 'danger' => {
  return meetingStatusMap[status]?.type || 'info'
}

/**
 * 解析会议的参与人列表（attendees为逗号分隔字符串）
 * @param meeting 会议对象
 * @returns 参与人姓名数组
 */
const getAttendeeList = (meeting: Meeting): string[] => {
  if (!meeting.attendees) return []
  return meeting.attendees.split(/[,，]/).map(name => name.trim()).filter(Boolean)
}

/**
 * 获取会议的参与人数
 * @param meeting 会议对象
 * @returns 参与人数
 */
const getAttendeeCount = (meeting: Meeting): number => {
  return getAttendeeList(meeting).length
}

/**
 * 加载会议列表（分页）
 */
const loadMeetingList = async () => {
  loading.value = true
  try {
    const response = await meetingApi.getList({ page: pagination.value.page, size: pagination.value.size })
    const pageData = unwrapPageResponse<Meeting>(response)
    meetingList.value = pageData.list
    pagination.value.total = pageData.total
  } catch (error) {
    ElMessage.error('加载会议列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载会议室列表
 */
const loadMeetingRoomList = async () => {
  roomLoading.value = true
  try {
    const response = await meetingRoomApi.getList({ page: 1, size: 100 })
    const pageData = unwrapPageResponse<MeetingRoom>(response)
    meetingRooms.value = pageData.list
  } catch (error) {
    ElMessage.error('加载会议室列表失败')
  } finally {
    roomLoading.value = false
  }
}

// 会议统计数据（基于已加载的会议列表计算）
const stats = computed(() => {
  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
  // 计算本周一日期
  const weekStart = new Date(today)
  weekStart.setDate(today.getDate() - ((today.getDay() + 6) % 7))
  const weekStartStr = `${weekStart.getFullYear()}-${String(weekStart.getMonth() + 1).padStart(2, '0')}-${String(weekStart.getDate()).padStart(2, '0')}`
  return {
    todayMeetings: meetingList.value.filter(m => (m.startTime || '').startsWith(todayStr)).length,
    weekMeetings: meetingList.value.filter(m => (m.startTime || '') >= weekStartStr).length,
    upcomingMeetings: meetingList.value.filter(m => m.status === 'pending').length,
    totalMeetings: pagination.value.total
  }
})

/**
 * 计算筛选后的会议列表（按状态、日期、关键词前端过滤）
 */
const filteredMeetings = computed(() => {
  return meetingList.value.filter(meeting => {
    // 状态筛选
    if (filterStatus.value && meeting.status !== filterStatus.value) {
      return false
    }
    // 日期筛选
    if (filterDate.value) {
      const meetingDate = (meeting.startTime || '').split(' ')[0]
      if (meetingDate !== filterDate.value) {
        return false
      }
    }
    // 关键词搜索
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      return (
        (meeting.meetingTitle || '').toLowerCase().includes(keyword) ||
        (meeting.roomName || '').toLowerCase().includes(keyword)
      )
    }
    return true
  })
})

// 新建会议对话框显示状态
const showCreateMeetingDialog = ref(false)

// 会议详情对话框显示状态
const showMeetingDetailDialog = ref(false)
const selectedMeeting = ref<Meeting | null>(null)

// 编辑会议状态（true为编辑，false为新建）
const editingMeeting = ref(false)
// 当前编辑的会议ID
const editingMeetingId = ref<number | null>(null)
// 保存按钮加载状态
const saving = ref(false)

/**
 * 生成空白会议表单
 * @returns 空白会议表单对象
 */
const createEmptyForm = () => ({
  meetingTitle: '',
  agenda: '',
  roomId: undefined as number | undefined,
  roomName: '',
  startTime: '',
  endTime: '',
  attendees: '',
  remark: ''
})

// 会议表单
const meetingForm = ref(createEmptyForm())

// 会议纪要
const meetingMinutes = ref('')

/**
 * 打开新建会议对话框
 */
const openCreateDialog = () => {
  editingMeeting.value = false
  editingMeetingId.value = null
  meetingForm.value = createEmptyForm()
  showCreateMeetingDialog.value = true
}

/**
 * 会议室选择变化时自动带出会议室名称
 * @param roomId 选中的会议室ID
 */
const onRoomChange = (roomId: number | undefined) => {
  const room = meetingRooms.value.find(r => r.id === roomId)
  meetingForm.value.roomName = room?.roomName || ''
}

/**
 * 编辑会议（将会议数据回填到表单）
 * @param meeting 要编辑的会议对象
 */
const editMeeting = (meeting: Meeting) => {
  editingMeeting.value = true
  editingMeetingId.value = meeting.id ?? null
  meetingForm.value = {
    meetingTitle: meeting.meetingTitle,
    agenda: meeting.agenda || '',
    roomId: meeting.roomId,
    roomName: meeting.roomName,
    startTime: meeting.startTime,
    endTime: meeting.endTime,
    attendees: meeting.attendees || '',
    remark: meeting.remark || ''
  }
  showCreateMeetingDialog.value = true
}

/**
 * 保存会议（新建或编辑）
 */
const saveMeeting = async () => {
  // 表单校验
  if (!meetingForm.value.meetingTitle) {
    ElMessage.warning('请输入会议主题')
    return
  }
  if (!meetingForm.value.roomId) {
    ElMessage.warning('请选择会议室')
    return
  }
  if (!meetingForm.value.startTime || !meetingForm.value.endTime) {
    ElMessage.warning('请选择开始时间和结束时间')
    return
  }
  saving.value = true
  try {
    // 构造提交数据，organizerId/organizerName由后端自动填充
    const payload = {
      meetingTitle: meetingForm.value.meetingTitle,
      agenda: meetingForm.value.agenda,
      roomId: meetingForm.value.roomId,
      roomName: meetingForm.value.roomName,
      startTime: meetingForm.value.startTime,
      endTime: meetingForm.value.endTime,
      attendees: meetingForm.value.attendees,
      remark: meetingForm.value.remark
    } as Meeting
    if (editingMeeting.value && editingMeetingId.value !== null) {
      await meetingApi.update(editingMeetingId.value, payload)
      ElMessage.success('会议更新成功')
    } else {
      // 新建会议自动生成会议编号
      payload.meetingNo = 'MT-' + Date.now()
      payload.status = 'pending'
      await meetingApi.create(payload)
      ElMessage.success('会议创建成功')
    }
    showCreateMeetingDialog.value = false
    await loadMeetingList()
  } catch (error) {
    ElMessage.error(editingMeeting.value ? '会议更新失败' : '会议创建失败')
  } finally {
    saving.value = false
  }
}

/**
 * 显示会议详情（先从列表数据展示，再从后端拉取最新详情）
 * @param meeting 会议对象
 */
const showMeetingDetail = async (meeting: Meeting) => {
  selectedMeeting.value = meeting
  meetingMinutes.value = meeting.remark || ''
  showMeetingDetailDialog.value = true
  // 从后端获取最新会议详情，失败时仍使用列表数据展示
  if (meeting.id !== undefined) {
    try {
      const response = await meetingApi.getById(meeting.id)
      const detail = unwrapResponseData<Meeting>(response)
      if (detail) {
        selectedMeeting.value = detail
        meetingMinutes.value = detail.remark || ''
      }
    } catch {
      // 获取详情失败时静默降级，使用列表数据展示
    }
  }
}

/**
 * 开始会议
 * @param meeting 会议对象
 */
const startMeeting = async (meeting: Meeting) => {
  if (meeting.id === undefined) return
  try {
    await meetingApi.start(meeting.id)
    ElMessage.success('会议已开始')
    await loadMeetingList()
  } catch (error) {
    ElMessage.error('开始会议失败')
  }
}

/**
 * 结束会议
 * @param meeting 会议对象
 */
const endMeeting = async (meeting: Meeting) => {
  if (meeting.id === undefined) return
  try {
    await ElMessageBox.confirm('确认结束该会议吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await meetingApi.end(meeting.id)
    ElMessage.success('会议已结束')
    await loadMeetingList()
  } catch (error) {
    ElMessage.error('结束会议失败')
  }
}

/**
 * 取消会议
 * @param meeting 会议对象
 */
const cancelMeeting = async (meeting: Meeting) => {
  if (meeting.id === undefined) return
  try {
    await ElMessageBox.confirm('确认取消该会议吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await meetingApi.cancel(meeting.id)
    ElMessage.success('会议已取消')
    await loadMeetingList()
  } catch (error) {
    ElMessage.error('取消会议失败')
  }
}

/**
 * 预约会议室（打开新建会议对话框并默认选中该会议室）
 * @param room 会议室对象
 */
const bookMeetingRoom = (room: MeetingRoom) => {
  openCreateDialog()
  meetingForm.value.roomId = room.id
  meetingForm.value.roomName = room.roomName
}

/**
 * 保存会议纪要（更新会议备注字段）
 */
const saveMinutes = async () => {
  if (!selectedMeeting.value || selectedMeeting.value.id === undefined) return
  try {
    await meetingApi.update(selectedMeeting.value.id, {
      ...selectedMeeting.value,
      remark: meetingMinutes.value
    })
    selectedMeeting.value.remark = meetingMinutes.value
    ElMessage.success('会议纪要保存成功')
    await loadMeetingList()
  } catch (error) {
    ElMessage.error('会议纪要保存失败')
  }
}

// 页面挂载时加载数据
onMounted(() => {
  loadMeetingList()
  loadMeetingRoomList()
})
</script>

<style scoped>
.meeting-management-view {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: #f5f7fa;
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.operation-section {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.meeting-title {
  color: #409eff;
  cursor: pointer;
}

.meeting-title:hover {
  text-decoration: underline;
}

.pagination-wrapper {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

.meeting-rooms-section {
  margin-top: 20px;
}

.meeting-rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.room-card {
  transition: all 0.3s ease;
}

.room-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-header h4 {
  margin: 0;
}

.room-info {
  margin: 10px 0;
}

.room-info .info-item {
  margin-bottom: 5px;
  font-size: 14px;
}

.room-actions {
  margin-top: 15px;
  text-align: right;
}

.room-available {
  border-left: 4px solid #67c23a;
}

.room-occupied {
  border-left: 4px solid #e6a23c;
}

.room-maintenance {
  border-left: 4px solid #f56c6c;
}

.detail-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.detail-header h3 {
  margin: 0;
  margin-right: 20px;
}

.detail-info {
  margin-bottom: 20px;
}

.info-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.info-label {
  width: 100px;
  font-weight: bold;
}

.detail-description {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.detail-description h4 {
  margin: 0 0 10px 0;
}

.detail-participants {
  margin-bottom: 20px;
}

.detail-participants h4 {
  margin: 0 0 15px 0;
}

.participants-list {
  display: flex;
  flex-wrap: wrap;
}

.detail-minutes {
  margin-top: 20px;
}

.detail-minutes h4 {
  margin: 0 0 15px 0;
}
</style>
