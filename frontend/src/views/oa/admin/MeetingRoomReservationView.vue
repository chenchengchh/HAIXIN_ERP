<template>
  <div class="meeting-room-reservation-view">
    <!-- 会议室预约统计 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">总会议室</div>
              <div class="stat-value">{{ stats.totalRooms }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">可用会议室</div>
              <div class="stat-value">{{ stats.availableRooms }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">今日预约</div>
              <div class="stat-value">{{ stats.todayReservations }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">待开始会议</div>
              <div class="stat-value">{{ stats.pendingReservations }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 操作区 -->
    <div class="operation-section">
      <el-button type="primary" @click="openReservationDialog">
        <el-icon><Plus /></el-icon> 预约会议室
      </el-button>
      <el-button type="success" @click="openCreateRoomDialog" style="margin-left: 10px;">
        <el-icon><Plus /></el-icon> 新增会议室
      </el-button>
      <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px; margin-left: 10px;">
        <el-option label="全部" value="" />
        <el-option label="待开始" value="pending" />
        <el-option label="进行中" value="ongoing" />
        <el-option label="已取消" value="cancelled" />
        <el-option label="已完成" value="completed" />
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
        placeholder="搜索会议室名称"
        clearable
        style="width: 300px; margin-left: 10px;"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 会议室列表 -->
    <div class="meeting-rooms-section">
      <el-card shadow="hover">
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
              <el-button size="small" @click="showRoomReservations(room)">预订记录</el-button>
              <el-button size="small" type="primary" @click="editRoom(room)">编辑</el-button>
              <el-button
                size="small"
                :type="room.status === 1 ? 'danger' : 'success'"
                @click="toggleRoomStatus(room)"
              >
                {{ room.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" type="primary" @click="reserveRoom(room)" :disabled="room.status !== 1">
                立即预约
              </el-button>
            </div>
          </el-card>
        </div>
      </el-card>
    </div>

    <!-- 预约记录 -->
    <div class="reservations-section">
      <el-card shadow="hover" style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <span>预约记录</span>
          </div>
        </template>
        <el-table :data="filteredReservations" stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="meetingNo" label="预约编号" width="150" />
          <el-table-column prop="roomName" label="会议室" width="120" />
          <el-table-column prop="startTime" label="开始时间" width="180" />
          <el-table-column prop="endTime" label="结束时间" width="180" />
          <el-table-column prop="meetingTitle" label="用途" min-width="200" />
          <el-table-column label="参会人数" width="100">
            <template #default="scope">
              {{ getAttendeeCount(scope.row) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="showReservationDetail(scope.row)">详情</el-button>
              <el-button size="small" type="danger" @click="cancelReservation(scope.row)" v-if="scope.row.status === 'pending'">取消</el-button>
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
            @size-change="loadReservationList"
            @current-change="loadReservationList"
          />
        </div>
      </el-card>
    </div>

    <!-- 预约表单对话框 -->
    <el-dialog
      v-model="showCreateReservationDialog"
      title="预约会议室"
      width="600px"
    >
      <el-form :model="reservationForm" label-width="100px">
        <el-form-item label="会议室" required>
          <el-select v-model="reservationForm.roomId" placeholder="请选择会议室" @change="onReservationRoomChange">
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
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" required>
              <el-date-picker
                v-model="reservationForm.startTime"
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
                v-model="reservationForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="参会人数" required>
          <el-input v-model.number="reservationForm.attendeeCount" placeholder="请输入参会人数" />
        </el-form-item>
        <el-form-item label="用途" required>
          <el-input
            v-model="reservationForm.purpose"
            type="textarea"
            placeholder="请输入会议用途"
            :rows="4"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateReservationDialog = false">取消</el-button>
          <el-button type="primary" @click="submitReservation" :loading="saving">提交预约</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新增/编辑会议室对话框 -->
    <el-dialog
      v-model="showRoomDialog"
      :title="editingRoom ? '编辑会议室' : '新增会议室'"
      width="600px"
    >
      <el-form :model="roomForm" label-width="100px">
        <el-form-item label="会议室名称" required>
          <el-input v-model="roomForm.roomName" placeholder="请输入会议室名称" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="roomForm.location" placeholder="请输入会议室位置" />
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="roomForm.capacity" :min="1" :max="500" placeholder="请输入容纳人数" />
        </el-form-item>
        <el-form-item label="设施">
          <el-input
            v-model="roomForm.equipment"
            type="textarea"
            placeholder="请输入会议室设施，如：投影仪、白板"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="roomForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showRoomDialog = false">取消</el-button>
          <el-button type="primary" @click="saveRoom" :loading="saving">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 会议室预订记录对话框 -->
    <el-dialog
      v-model="showRoomReservationsDialog"
      :title="`${currentRoom?.roomName || ''} - 预订记录`"
      width="800px"
    >
      <el-table :data="roomReservations" stripe style="width: 100%" v-loading="roomReservationsLoading">
        <el-table-column prop="meetingNo" label="预约编号" width="150" />
        <el-table-column prop="meetingTitle" label="会议主题" min-width="180" />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 预约详情对话框 -->
    <el-dialog
      v-model="showReservationDetailDialog"
      title="预约详情"
      width="700px"
    >
      <div v-if="selectedReservation" class="reservation-detail">
        <div class="detail-info">
          <div class="info-item">
            <span class="info-label">预约编号：</span>
            <span class="info-value">{{ selectedReservation.meetingNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">会议室：</span>
            <span class="info-value">{{ selectedReservation.roomName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">预约人：</span>
            <span class="info-value">{{ selectedReservation.organizerName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">开始时间：</span>
            <span class="info-value">{{ selectedReservation.startTime }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">结束时间：</span>
            <span class="info-value">{{ selectedReservation.endTime }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">参会人数：</span>
            <span class="info-value">{{ getAttendeeCount(selectedReservation) }}人</span>
          </div>
          <div class="info-item">
            <span class="info-label">状态：</span>
            <el-tag :type="getStatusTagType(selectedReservation.status)">{{ getStatusLabel(selectedReservation.status) }}</el-tag>
          </div>
        </div>
        <div class="detail-purpose">
          <h4>会议用途</h4>
          <p>{{ selectedReservation.meetingTitle }}</p>
        </div>
        <div class="detail-purpose" v-if="selectedReservation.remark">
          <h4>备注信息</h4>
          <p>{{ selectedReservation.remark }}</p>
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

// 会议室列表数据（来自后端API）
const meetingRooms = ref<MeetingRoom[]>([])
const roomLoading = ref(false)

// 预约记录列表数据（预约即会议，来自后端会议API）
const reservations = ref<Meeting[]>([])
const loading = ref(false)

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
 * 获取会议的参会人数（attendees为逗号分隔字符串）
 * @param meeting 会议对象
 * @returns 参会人数
 */
const getAttendeeCount = (meeting: Meeting): number => {
  if (!meeting.attendees) return 0
  return meeting.attendees.split(/[,，]/).map(name => name.trim()).filter(Boolean).length
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

/**
 * 加载预约记录列表（分页，预约记录即会议数据）
 */
const loadReservationList = async () => {
  loading.value = true
  try {
    const response = await meetingApi.getList({ page: pagination.value.page, size: pagination.value.size })
    const pageData = unwrapPageResponse<Meeting>(response)
    reservations.value = pageData.list
    pagination.value.total = pageData.total
  } catch (error) {
    ElMessage.error('加载预约记录失败')
  } finally {
    loading.value = false
  }
}

/**
 * 计算筛选后的预约记录（按状态、日期、会议室名称前端过滤）
 */
const filteredReservations = computed(() => {
  return reservations.value.filter(reservation => {
    // 状态筛选
    if (filterStatus.value && reservation.status !== filterStatus.value) {
      return false
    }
    // 日期筛选
    if (filterDate.value) {
      const reservationDate = (reservation.startTime || '').split(' ')[0]
      if (reservationDate !== filterDate.value) {
        return false
      }
    }
    // 关键词搜索
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      return (reservation.roomName || '').toLowerCase().includes(keyword)
    }
    return true
  })
})

// 统计数据（基于已加载的会议室和预约记录计算）
const stats = computed(() => {
  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
  return {
    totalRooms: meetingRooms.value.length,
    availableRooms: meetingRooms.value.filter(r => r.status === 1).length,
    todayReservations: reservations.value.filter(r => (r.startTime || '').startsWith(todayStr)).length,
    pendingReservations: reservations.value.filter(r => r.status === 'pending').length
  }
})

// 预约表单对话框显示状态
const showCreateReservationDialog = ref(false)
// 保存按钮加载状态
const saving = ref(false)

// 预约表单
const reservationForm = ref({
  roomId: undefined as number | undefined,
  roomName: '',
  startTime: '',
  endTime: '',
  purpose: '',
  attendeeCount: 1
})

// 预约详情对话框显示状态
const showReservationDetailDialog = ref(false)
const selectedReservation = ref<Meeting | null>(null)

// 新增/编辑会议室对话框显示状态
const showRoomDialog = ref(false)
// 编辑会议室状态（true为编辑，false为新增）
const editingRoom = ref(false)
// 当前编辑的会议室ID
const editingRoomId = ref<number | null>(null)

// 会议室表单
const roomForm = ref({
  roomName: '',
  location: '',
  capacity: 10,
  equipment: '',
  status: 1
})

// 会议室预订记录对话框显示状态
const showRoomReservationsDialog = ref(false)
const roomReservationsLoading = ref(false)
// 当前查看预订记录的会议室
const currentRoom = ref<MeetingRoom | null>(null)
// 当前会议室的预订记录
const roomReservations = ref<Meeting[]>([])

/**
 * 打开预约会议室对话框（重置表单）
 */
const openReservationDialog = () => {
  reservationForm.value = {
    roomId: undefined,
    roomName: '',
    startTime: '',
    endTime: '',
    purpose: '',
    attendeeCount: 1
  }
  showCreateReservationDialog.value = true
}

/**
 * 预约表单中会议室选择变化时自动带出会议室名称
 * @param roomId 选中的会议室ID
 */
const onReservationRoomChange = (roomId: number | undefined) => {
  const room = meetingRooms.value.find(r => r.id === roomId)
  reservationForm.value.roomName = room?.roomName || ''
}

/**
 * 预约会议室（打开预约对话框并默认选中该会议室）
 * @param room 会议室对象
 */
const reserveRoom = (room: MeetingRoom) => {
  openReservationDialog()
  reservationForm.value.roomId = room.id
  reservationForm.value.roomName = room.roomName
}

/**
 * 提交预约（创建会议，organizerId/organizerName由后端自动填充）
 */
const submitReservation = async () => {
  // 表单校验
  if (!reservationForm.value.roomId) {
    ElMessage.warning('请选择会议室')
    return
  }
  if (!reservationForm.value.startTime || !reservationForm.value.endTime) {
    ElMessage.warning('请选择开始时间和结束时间')
    return
  }
  if (!reservationForm.value.purpose) {
    ElMessage.warning('请输入会议用途')
    return
  }
  saving.value = true
  try {
    // 构造会议数据，会议编号自动生成，参会人数记录在备注中
    const payload = {
      meetingNo: 'MT-' + Date.now(),
      meetingTitle: reservationForm.value.purpose,
      roomId: reservationForm.value.roomId,
      roomName: reservationForm.value.roomName,
      startTime: reservationForm.value.startTime,
      endTime: reservationForm.value.endTime,
      attendees: '',
      remark: `参会人数：${reservationForm.value.attendeeCount}人`,
      status: 'pending'
    } as Meeting
    await meetingApi.create(payload)
    ElMessage.success('预约提交成功')
    showCreateReservationDialog.value = false
    await loadReservationList()
  } catch (error) {
    ElMessage.error('预约提交失败')
  } finally {
    saving.value = false
  }
}

/**
 * 显示预约详情（先从列表数据展示，再从后端拉取最新详情）
 * @param reservation 预约记录（会议对象）
 */
const showReservationDetail = async (reservation: Meeting) => {
  selectedReservation.value = reservation
  showReservationDetailDialog.value = true
  // 从后端获取最新预约详情，失败时仍使用列表数据展示
  if (reservation.id !== undefined) {
    try {
      const response = await meetingApi.getById(reservation.id)
      const detail = unwrapResponseData<Meeting>(response)
      if (detail) {
        selectedReservation.value = detail
      }
    } catch {
      // 获取详情失败时静默降级，使用列表数据展示
    }
  }
}

/**
 * 取消预约
 * @param reservation 预约记录（会议对象）
 */
const cancelReservation = async (reservation: Meeting) => {
  if (reservation.id === undefined) return
  try {
    await ElMessageBox.confirm('确认取消该预约吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await meetingApi.cancel(reservation.id)
    ElMessage.success('预约已取消')
    await loadReservationList()
  } catch (error) {
    ElMessage.error('取消预约失败')
  }
}

/**
 * 打开新增会议室对话框
 */
const openCreateRoomDialog = () => {
  editingRoom.value = false
  editingRoomId.value = null
  roomForm.value = {
    roomName: '',
    location: '',
    capacity: 10,
    equipment: '',
    status: 1
  }
  showRoomDialog.value = true
}

/**
 * 编辑会议室（将会议室数据回填到表单）
 * @param room 会议室对象
 */
const editRoom = (room: MeetingRoom) => {
  editingRoom.value = true
  editingRoomId.value = room.id ?? null
  roomForm.value = {
    roomName: room.roomName,
    location: room.location || '',
    capacity: room.capacity || 10,
    equipment: room.equipment || '',
    status: room.status
  }
  showRoomDialog.value = true
}

/**
 * 保存会议室（新增或编辑，creatorId/creatorName由后端自动填充）
 */
const saveRoom = async () => {
  // 表单校验
  if (!roomForm.value.roomName) {
    ElMessage.warning('请输入会议室名称')
    return
  }
  saving.value = true
  try {
    if (editingRoom.value && editingRoomId.value !== null) {
      // 编辑会议室
      const payload = { ...roomForm.value } as MeetingRoom
      await meetingRoomApi.update(editingRoomId.value, payload)
      ElMessage.success('会议室更新成功')
    } else {
      // 新增会议室，会议室编码自动生成
      const payload = {
        ...roomForm.value,
        roomCode: 'MR-' + Date.now()
      } as MeetingRoom
      await meetingRoomApi.create(payload)
      ElMessage.success('会议室创建成功')
    }
    showRoomDialog.value = false
    await loadMeetingRoomList()
  } catch (error) {
    ElMessage.error(editingRoom.value ? '会议室更新失败' : '会议室创建失败')
  } finally {
    saving.value = false
  }
}

/**
 * 切换会议室启用/禁用状态
 * @param room 会议室对象
 */
const toggleRoomStatus = async (room: MeetingRoom) => {
  if (room.id === undefined) return
  const isEnable = room.status !== 1
  try {
    await ElMessageBox.confirm(`确认${isEnable ? '启用' : '禁用'}会议室"${room.roomName}"吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    if (isEnable) {
      await meetingRoomApi.enable(room.id)
      ElMessage.success('会议室已启用')
    } else {
      await meetingRoomApi.disable(room.id)
      ElMessage.success('会议室已禁用')
    }
    await loadMeetingRoomList()
  } catch (error) {
    ElMessage.error(isEnable ? '启用会议室失败' : '禁用会议室失败')
  }
}

/**
 * 查看指定会议室的预订记录
 * @param room 会议室对象
 */
const showRoomReservations = async (room: MeetingRoom) => {
  if (room.id === undefined) return
  currentRoom.value = room
  roomReservations.value = []
  showRoomReservationsDialog.value = true
  roomReservationsLoading.value = true
  try {
    const response = await meetingApi.getByRoomId(room.id, { page: 1, size: 50 })
    const pageData = unwrapPageResponse<Meeting>(response)
    roomReservations.value = pageData.list
    // 补充会议室名称（后端会议数据可能未带出）
    roomReservations.value.forEach(m => {
      if (!m.roomName) m.roomName = room.roomName
    })
  } catch (error) {
    ElMessage.error('加载预订记录失败')
  } finally {
    roomReservationsLoading.value = false
  }
}

// 页面挂载时加载数据
onMounted(() => {
  loadMeetingRoomList()
  loadReservationList()
})
</script>

<style scoped>
.meeting-room-reservation-view {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: #ffffff;
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
}

.operation-section {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
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

.reservations-section {
  margin-top: 20px;
}

.pagination-wrapper {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.detail-purpose {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.detail-purpose h4 {
  margin: 0 0 10px 0;
}

.detail-approval {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.detail-approval h4 {
  margin: 0 0 15px 0;
}

.approval-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.approval-label {
  width: 80px;
  font-weight: bold;
}
</style>
