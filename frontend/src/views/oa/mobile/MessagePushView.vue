<template>
  <div class="message-push-view">
    <!-- 消息统计 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">全部消息</div>
              <div class="stat-value">{{ stats.total }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">未读消息</div>
              <div class="stat-value">{{ stats.unread }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">系统通知</div>
              <div class="stat-value">{{ stats.system }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-label">审批通知</div>
              <div class="stat-value">{{ stats.approval }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 消息列表 -->
    <div class="message-list-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>消息列表</span>
            <el-select v-model="messageType" placeholder="消息类型" style="width: 150px;">
              <el-option label="全部" value="all" />
              <el-option label="未读" value="unread" />
              <el-option label="系统通知" value="system" />
              <el-option label="审批通知" value="approval" />
              <el-option label="会议通知" value="meeting" />
            </el-select>
            <el-button size="small" @click="markAllRead">
              全部已读
            </el-button>
          </div>
        </template>
        <el-list v-loading="loading" :border="false" :data="filteredMessages" style="width: 100%">
          <el-list-item v-for="message in filteredMessages" :key="message.id" :class="message.isRead ? '' : 'unread-message'">
            <template #default>
              <div class="message-item">
                <div class="message-icon">
                  <el-icon :size="32" :color="messageTypeColorMap[message?.type || 'other']">
                    <component :is="messageTypeIconMap[message?.type || 'other']" />
                  </el-icon>
                </div>
                <div class="message-content">
                  <div class="message-title">
                    <span>{{ message?.title || '无标题消息' }}</span>
                    <span class="message-time">{{ message?.createTime || '' }}</span>
                  </div>
                  <div class="message-body">{{ message?.content || '' }}</div>
                </div>
              </div>
            </template>
            <template #extra>
              <div class="message-actions">
                <el-button size="small" @click="markAsRead(message)" v-if="!message.isRead">
                  已读
                </el-button>
                <el-button size="small" @click="viewDetail(message)">
                  详情
                </el-button>
              </div>
            </template>
          </el-list-item>
        </el-list>
        <div class="pagination" v-if="filteredMessages.length > 0">
          <el-pagination
            layout="prev, pager, next"
            :total="filteredMessages.length"
            :page-size="10"
            :current-page="currentPage"
            @current-change="currentPage = $event"
          />
        </div>
      </el-card>
    </div>

    <!-- 消息详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="selectedMessage ? selectedMessage.title : '消息详情'"
      width="600px"
    >
      <div v-if="selectedMessage" class="message-detail">
        <div class="detail-header">
          <div class="message-meta">
            <span class="message-type"><el-tag :type="messageTypeColorMap[selectedMessage?.type || 'other']">{{ selectedMessage?.type || 'other' }}</el-tag></span>
            <span class="message-time">{{ selectedMessage?.createTime || '' }}</span>
          </div>
        </div>
        <div class="detail-content">
          <p>{{ selectedMessage?.content || '' }}</p>
        </div>
        <div class="detail-actions" v-if="selectedMessage?.relatedId">
          <el-button type="primary" @click="viewRelatedContent(selectedMessage)">
            {{ selectedMessage?.relatedType === 'approval' ? '查看审批详情' : '查看相关内容' }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Message as MessageIcon, Bell, Document, Calendar, Check } from '@element-plus/icons-vue'

// 定义消息类型
interface Message {
  id: number
  title: string
  content: string
  type: 'system' | 'approval' | 'meeting' | 'other'
  isRead: boolean
  createTime: string
  relatedId?: number
  relatedType?: 'approval' | 'meeting' | 'document'
}

// 模拟数据 - 消息列表
const messages = ref<Message[]>([
  {
    id: 1,
    title: '审批请求通知',
    content: '张三提交了请假申请，需要您审批',
    type: 'approval',
    isRead: false,
    createTime: '2025-12-17 10:30:00',
    relatedId: 1,
    relatedType: 'approval'
  },
  {
    id: 2,
    title: '系统更新通知',
    content: 'OA系统将于今晚22:00-24:00进行系统维护，期间系统可能不可用，请提前保存数据',
    type: 'system',
    isRead: false,
    createTime: '2025-12-17 09:15:00'
  },
  {
    id: 3,
    title: '会议邀请',
    content: '您被邀请参加"Q4工作总结会议"，时间：2025-12-18 14:00-16:00，地点：会议室A',
    type: 'meeting',
    isRead: true,
    createTime: '2025-12-16 16:45:00',
    relatedId: 1,
    relatedType: 'meeting'
  },
  {
    id: 4,
    title: '审批结果通知',
    content: '您的报销申请已通过审批，报销金额¥2,500.00将在3个工作日内到账',
    type: 'approval',
    isRead: true,
    createTime: '2025-12-16 15:30:00',
    relatedId: 2,
    relatedType: 'approval'
  },
  {
    id: 5,
    title: '新文档分享',
    content: '李四分享了文档"项目计划方案.docx"给您，点击查看详情',
    type: 'other',
    isRead: false,
    createTime: '2025-12-15 14:20:00',
    relatedId: 1,
    relatedType: 'document'
  },
  {
    id: 6,
    title: '系统公告',
    content: '关于2026年元旦放假安排的通知：2026年1月1日至3日放假，共3天',
    type: 'system',
    isRead: true,
    createTime: '2025-12-14 10:00:00'
  },
  {
    id: 7,
    title: '审批请求通知',
    content: '王五提交了采购申请，需要您审批',
    type: 'approval',
    isRead: false,
    createTime: '2025-12-13 09:45:00',
    relatedId: 3,
    relatedType: 'approval'
  }
])

// 筛选条件
const messageType = ref('all')
const currentPage = ref(1)
const loading = ref(false)

// 消息类型图标映射
const messageTypeIconMap = {
  system: Bell,
  approval: Document,
  meeting: Calendar,
  other: MessageIcon
}

// 消息类型颜色映射
const messageTypeColorMap = {
  system: '#409eff',
  approval: '#67c23a',
  meeting: '#e6a23c',
  other: '#909399'
}

// 计算筛选后的消息列表
const filteredMessages = computed(() => {
  let filtered = messages.value
  if (messageType.value === 'unread') {
    filtered = filtered.filter(m => !m?.isRead)
  } else if (messageType.value !== 'all') {
    filtered = filtered.filter(m => m?.type === messageType.value)
  }
  // 按时间倒序排序
  return filtered.sort((a, b) => new Date(b?.createTime || 0).getTime() - new Date(a?.createTime || 0).getTime())
})

// 统计数据
const stats = ref({
  total: messages.value.length,
  unread: messages.value.filter(m => !m?.isRead).length,
  system: messages.value.filter(m => m?.type === 'system').length,
  approval: messages.value.filter(m => m?.type === 'approval').length
})

// 消息详情
const showDetailDialog = ref(false)
const selectedMessage = ref<Message | null>(null)

// 查看详情
const viewDetail = (message: Message) => {
  selectedMessage.value = message
  if (!message.isRead) {
    message.isRead = true
    // 更新统计数据
    stats.value.unread = messages.value.filter(m => !m.isRead).length
  }
  showDetailDialog.value = true
}

// 标记为已读
const markAsRead = (message: Message) => {
  message.isRead = true
  // 更新统计数据
  stats.value.unread = messages.value.filter(m => !m.isRead).length
}

// 全部已读
const markAllRead = () => {
  messages.value.forEach(message => {
    message.isRead = true
  })
  // 更新统计数据
  stats.value.unread = 0
}

// 查看相关内容
const viewRelatedContent = (message: Message) => {
  // 这里可以根据消息类型跳转到相关页面
  console.log('查看相关内容:', message)
  showDetailDialog.value = false
}
</script>

<style scoped>
.message-push-view {
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

.message-list-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-item {
  display: flex;
  gap: 16px;
  padding: 10px 0;
}

.unread-message {
  background-color: rgba(64, 158, 255, 0.05);
  border-left: 3px solid #409eff;
}

.message-icon {
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.message-title span {
  font-weight: bold;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}

.message-body {
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
  max-height: 3em;
}

.message-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.message-detail {
  padding: 20px 0;
}

.detail-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.message-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-type {
  margin-right: 10px;
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.detail-content {
  margin-bottom: 20px;
  line-height: 1.8;
}

.detail-actions {
  text-align: right;
}
</style>
