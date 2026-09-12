<template>
  <div class="chat-view">
    <!-- 聊天布局 -->
    <div class="chat-layout">
      <!-- 左侧会话列表 -->
      <div class="chat-sidebar">
        <!-- 搜索框 -->
        <div class="search-box">
          <el-input v-model="searchQuery" placeholder="搜索会话" clearable size="small" />
        </div>
        
        <!-- 会话列表 -->
        <div class="conversation-list">
          <div
            v-for="conversation in conversations"
            :key="conversation.id"
            class="conversation-item"
            :class="{ active: activeConversationId === conversation.id }"
            @click="selectConversation(conversation)"
          >
            <div class="avatar">
              <el-avatar :size="40" :src="conversation.avatar || ''">
                {{ conversation.name.charAt(0) }}
              </el-avatar>
              <span v-if="conversation.unreadCount > 0" class="unread-count">{{ conversation.unreadCount }}</span>
            </div>
            <div class="conversation-info">
              <div class="conversation-header">
                <div class="name">{{ conversation.name }}</div>
                <div class="time">{{ conversation.lastMessageTime }}</div>
              </div>
              <div class="last-message">{{ conversation.lastMessage }}</div>
            </div>
          </div>
        </div>
        
        <!-- 新建会话按钮 -->
        <div class="new-conversation">
          <el-button type="primary" block @click="openNewConversationDialog">
            <el-icon><Plus /></el-icon>
            新建会话
          </el-button>
        </div>
      </div>
      
      <!-- 右侧聊天内容 -->
      <div class="chat-main" v-if="activeConversation">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="avatar-name">
            <el-avatar :size="40" :src="activeConversation.avatar || ''">
              {{ activeConversation.name.charAt(0) }}
            </el-avatar>
            <div class="name">{{ activeConversation.name }}</div>
          </div>
          <div class="chat-actions">
            <el-button link @click="openConversationInfo">
              <el-icon><InfoFilled /></el-icon>
            </el-button>
            <el-button link @click="openFileTransfer">
              <el-icon><Document /></el-icon>
            </el-button>
          </div>
        </div>
        
        <!-- 聊天消息 -->
        <div class="chat-messages">
          <div
            v-for="message in messages"
            :key="message.id"
            class="message-item"
            :class="message.senderId === currentUserId ? 'sent' : 'received'"
          >
            <div class="message-content">
              <div class="message-text">{{ message.content }}</div>
              <div class="message-time">{{ message.sendTime }}</div>
            </div>
          </div>
        </div>
        
        <!-- 聊天输入 -->
        <div class="chat-input">
          <el-input
            v-model="messageInput"
            type="textarea"
            placeholder="输入消息..."
            resize="none"
            :rows="3"
            @keyup.enter.ctrl="sendMessage"
          >
            <template #prepend>
              <el-button link @click="openEmojiPicker">
                😀
              </el-button>
              <el-button link @click="openFilePicker">
                <el-icon><Document /></el-icon>
              </el-button>
              <el-button link @click="openImagePicker">
                <el-icon><Picture /></el-icon>
              </el-button>
            </template>
            <template #append>
              <el-button type="primary" @click="sendMessage">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>
      
      <!-- 初始状态 -->
      <div class="chat-initial" v-else>
        <div class="initial-content">
          <el-icon class="initial-icon"><ChatDotRound /></el-icon>
          <h3>选择一个会话开始聊天</h3>
          <p>或者新建一个会话与同事交流</p>
        </div>
      </div>
    </div>
    
    <!-- 新建会话对话框 -->
    <el-dialog
      v-model="newConversationDialogVisible"
      title="新建会话"
      width="600px"
    >
      <div class="new-conversation-content">
        <el-tabs v-model="newConversationType" type="border-card">
          <el-tab-pane label="单聊" name="single">
            <div class="user-list">
              <div
                v-for="user in users"
                :key="user.id"
                class="user-item"
                :class="{ selected: selectedUsers.includes(user.id) }"
                @click="toggleUserSelection(user.id)"
              >
                <el-avatar :size="36" :src="user.avatar || ''">
                  {{ user.userName.charAt(0) }}
                </el-avatar>
                <div class="user-info">
                  <div class="user-name">{{ user.userName }}</div>
                  <div class="user-department">{{ user.department }}</div>
                </div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="群聊" name="group">
            <el-form :model="groupForm" label-width="80px">
              <el-form-item label="群名称" required>
                <el-input v-model="groupForm.groupName" placeholder="输入群名称" />
              </el-form-item>
              <el-form-item label="群描述">
                <el-input v-model="groupForm.description" type="textarea" placeholder="输入群描述" :rows="2" />
              </el-form-item>
              <el-form-item label="选择成员" required>
                <el-select
                  v-model="groupForm.memberIds"
                  multiple
                  placeholder="选择群成员"
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
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="newConversationDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createConversation">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, InfoFilled, Document, Picture, ChatDotRound } from '@element-plus/icons-vue'

// 当前用户ID
const currentUserId = ref(1)

// 搜索查询
const searchQuery = ref('')

// 会话列表
const conversations = ref([
  {
    id: 1,
    name: '张三',
    type: 'single',
    avatar: '',
    lastMessage: '你好，请问这个需求什么时候可以完成？',
    lastMessageTime: '10:30',
    unreadCount: 2
  },
  {
    id: 2,
    name: '技术部',
    type: 'group',
    avatar: '',
    lastMessage: '明天下午3点有技术评审会议，请大家准时参加',
    lastMessageTime: '昨天',
    unreadCount: 0
  },
  {
    id: 3,
    name: '李四',
    type: 'single',
    avatar: '',
    lastMessage: '文档已经更新，请查看',
    lastMessageTime: '12-15',
    unreadCount: 0
  },
  {
    id: 4,
    name: '产品组',
    type: 'group',
    avatar: '',
    lastMessage: '产品需求已经确认，开始开发',
    lastMessageTime: '12-14',
    unreadCount: 0
  }
])

// 活动会话
const activeConversationId = ref(0)
const activeConversation = computed(() => {
  return conversations.value.find(conversation => conversation.id === activeConversationId.value) || null
})

// 消息列表
const messages = ref([
  {
    id: 1,
    senderId: 2,
    senderName: '张三',
    content: '你好，请问这个需求什么时候可以完成？',
    sendTime: '10:25',
    type: 'text'
  },
  {
    id: 2,
    senderId: 1,
    senderName: '我',
    content: '预计明天可以完成',
    sendTime: '10:26',
    type: 'text'
  },
  {
    id: 3,
    senderId: 2,
    senderName: '张三',
    content: '好的，辛苦了',
    sendTime: '10:27',
    type: 'text'
  },
  {
    id: 4,
    senderId: 2,
    senderName: '张三',
    content: '还有一个问题需要讨论',
    sendTime: '10:30',
    type: 'text'
  }
])

// 消息输入
const messageInput = ref('')

// 新建会话对话框
const newConversationDialogVisible = ref(false)
const newConversationType = ref('single')
const selectedUsers = ref<number[]>([])
const groupForm = reactive({
  groupName: '',
  description: '',
  memberIds: []
})

// 用户列表
const users = ref([
  { id: 1, userName: '张三', department: '技术部', avatar: '' },
  { id: 2, userName: '李四', department: '销售部', avatar: '' },
  { id: 3, userName: '王五', department: '财务部', avatar: '' },
  { id: 4, userName: '赵六', department: '运营部', avatar: '' },
  { id: 5, userName: '孙七', department: '人事部', avatar: '' }
])

// 选择会话
const selectConversation = (conversation: any) => {
  activeConversationId.value = conversation.id
  // 标记为已读
  conversation.unreadCount = 0
  // 这里应该调用API获取消息历史
}

// 发送消息
const sendMessage = () => {
  if (!messageInput.value.trim() || !activeConversationId.value) return
  
  const newMessage = {
    id: messages.value.length + 1,
    senderId: currentUserId.value,
    senderName: '我',
    content: messageInput.value,
    sendTime: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    type: 'text'
  }
  
  messages.value.push(newMessage)
  messageInput.value = ''
  
  // 这里应该调用API发送消息
}

// 打开新建会话对话框
const openNewConversationDialog = () => {
  newConversationDialogVisible.value = true
  resetNewConversationForm()
}

// 重置新建会话表单
const resetNewConversationForm = () => {
  newConversationType.value = 'single'
  selectedUsers.value = []
  Object.assign(groupForm, {
    groupName: '',
    description: '',
    memberIds: []
  })
}

// 切换用户选择
const toggleUserSelection = (userId: number) => {
  const index = selectedUsers.value.indexOf(userId)
  if (index > -1) {
    selectedUsers.value.splice(index, 1)
  } else {
    selectedUsers.value.push(userId)
  }
}

// 创建会话
const createConversation = () => {
  console.log('创建会话:', newConversationType.value, selectedUsers.value, groupForm)
  // 这里应该调用API创建会话
  newConversationDialogVisible.value = false
  ElMessage.success('会话创建成功')
}

// 打开会话信息
const openConversationInfo = () => {
  console.log('打开会话信息')
}

// 打开文件传输
const openFileTransfer = () => {
  console.log('打开文件传输')
}

// 打开表情选择器
const openEmojiPicker = () => {
  console.log('打开表情选择器')
}

// 打开文件选择器
const openFilePicker = () => {
  console.log('打开文件选择器')
}

// 打开图片选择器
const openImagePicker = () => {
  console.log('打开图片选择器')
}
</script>

<style scoped>
.chat-view {
  padding: 20px;
  box-sizing: border-box;
  height: calc(100vh - 120px);
  overflow: hidden;
}

.chat-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  height: 100%;
}

.chat-sidebar {
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.search-box {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1px solid #f0f0f0;
}

.conversation-item:hover {
  background-color: #f5f7fa;
}

.conversation-item.active {
  background-color: #ecf5ff;
}

.avatar {
  position: relative;
}

.unread-count {
  position: absolute;
  top: -4px;
  right: -4px;
  background-color: #f56c6c;
  color: #fff;
  font-size: 12px;
  border-radius: 10px;
  min-width: 20px;
  height: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0 4px;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.name {
  font-weight: bold;
  font-size: 14px;
  color: #303133;
}

.time {
  font-size: 12px;
  color: #909399;
}

.last-message {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.new-conversation {
  padding: 12px;
  border-top: 1px solid #f0f0f0;
}

.chat-main {
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fafafa;
}

.avatar-name {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #fafafa;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  animation: slideIn 0.3s ease;
}

.message-item.sent {
  justify-content: flex-end;
}

.message-item.received {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
}

.message-text {
  padding: 10px 14px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.5;
  word-wrap: break-word;
}

.message-item.sent .message-text {
  background-color: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message-item.received .message-text {
  background-color: #fff;
  color: #303133;
  border: 1px solid #e4e7ed;
  border-bottom-left-radius: 4px;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  text-align: right;
}

.message-item.received .message-time {
  text-align: left;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
  background-color: #fff;
}

.chat-initial {
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.initial-content {
  text-align: center;
  color: #909399;
}

.initial-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.initial-content h3 {
  margin: 0 0 8px 0;
  color: #606266;
}

.new-conversation-content {
  max-height: 400px;
  overflow-y: auto;
}

.user-list {
  max-height: 300px;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1px solid #f0f0f0;
}

.user-item:hover {
  background-color: #f5f7fa;
}

.user-item.selected {
  background-color: #ecf5ff;
}

.user-info {
  flex: 1;
}

.user-name {
  font-weight: bold;
  font-size: 14px;
  color: #303133;
}

.user-department {
  font-size: 12px;
  color: #909399;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-view {
    padding: 12px;
    height: calc(100vh - 100px);
  }
  
  .chat-layout {
    grid-template-columns: 1fr;
  }
  
  .chat-sidebar {
    display: none;
  }
  
  .message-content {
    max-width: 85%;
  }
}
</style>