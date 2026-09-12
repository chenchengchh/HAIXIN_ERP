<template>
  <div class="ticket-replies-view">
    <!-- 页面操作栏 -->
    <div class="page-actions">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索回复内容"
        style="width: 240px; margin-right: 10px;"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-input-number
        v-model="searchForm.ticketId"
        placeholder="工单ID"
        :min="1"
        style="width: 140px; margin-right: 10px;"
        clearable
      />
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon> 新建回复
      </el-button>
    </div>

    <!-- 回复列表 -->
    <el-card shadow="never" class="replies-table-card">
      <el-table
        v-loading="loading"
        :data="repliesList"
        style="width: 100%"
      >
        <el-table-column prop="ticketId" label="工单ID" width="100" sortable />
        <el-table-column prop="ticketNo" label="工单编号" width="160" />
        <el-table-column prop="ticketTitle" label="工单标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="content" label="回复内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="replyBy" label="回复人" width="120" />
        <el-table-column prop="replyTime" label="回复时间" width="180" sortable>
          <template #default="scope">
            {{ formatReplyTime(scope.row.replyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新建回复对话框 -->
    <el-dialog v-model="dialogVisible" title="新建工单回复" width="500px">
      <el-form :model="replyForm" :rules="replyFormRules" ref="replyFormRef" label-width="100px">
        <el-form-item label="工单ID" prop="ticketId">
          <el-input-number v-model="replyForm.ticketId" :min="1" style="width: 100%" placeholder="请输入要回复的工单ID" />
        </el-form-item>
        <el-form-item label="回复内容" prop="content">
          <el-input v-model="replyForm.content" type="textarea" :rows="4" placeholder="请输入回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSaveReply">提交回复</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看回复详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="回复详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="工单ID">{{ currentReply.ticketId }}</el-descriptions-item>
        <el-descriptions-item label="工单编号">{{ currentReply.ticketNo }}</el-descriptions-item>
        <el-descriptions-item label="工单标题">{{ currentReply.ticketTitle }}</el-descriptions-item>
        <el-descriptions-item label="回复内容">{{ currentReply.content }}</el-descriptions-item>
        <el-descriptions-item label="回复人">{{ currentReply.replyBy }}</el-descriptions-item>
        <el-descriptions-item label="回复时间">{{ formatReplyTime(currentReply.replyTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { serviceApi } from '@/api/crm/service'
import { unwrapPageResponse } from '@/api'
import { useAuthStore } from '@/stores/auth'

// 认证仓库（获取当前登录人作为回复人）
const authStore = useAuthStore()

// 回复列表数据
const repliesList = ref<any[]>([])

// 加载状态
const loading = ref(false)
const saving = ref(false)

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  ticketId: undefined as number | undefined
})

// 对话框状态
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const currentReply = ref<any>({})

// 回复表单引用与数据
const replyFormRef = ref()
const replyForm = reactive({
  ticketId: undefined as number | undefined,
  content: ''
})

// 回复表单验证规则
const replyFormRules = reactive({
  ticketId: [{ required: true, message: '请输入工单ID', trigger: 'blur' }],
  content: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
})

/**
 * 格式化回复时间（后端返回ISO字符串，截取为可读格式）
 * @param time ISO时间字符串
 * @returns 格式化后的时间字符串
 */
const formatReplyTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

/**
 * 汇总查询工单回复列表
 */
const fetchRepliesList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.ticketId) params.ticketId = searchForm.ticketId
    const response = await serviceApi.getTicketReplies(params)
    const { list, total } = unwrapPageResponse<any>(response)
    repliesList.value = list
    pagination.total = total
  } catch (error) {
    console.error('获取回复列表失败:', error)
    ElMessage.error('获取回复列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索：重置页码并重新查询
 */
const handleSearch = () => {
  pagination.currentPage = 1
  fetchRepliesList()
}

/**
 * 打开新建回复对话框
 */
const handleCreate = () => {
  replyForm.ticketId = searchForm.ticketId
  replyForm.content = ''
  dialogVisible.value = true
}

/**
 * 提交工单回复，调用工单回复接口
 */
const handleSaveReply = () => {
  replyFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    saving.value = true
    try {
      const replyBy = authStore.userInfo?.username || authStore.userInfo?.name || 'admin'
      await serviceApi.replyTicket(replyForm.ticketId!, {
        content: replyForm.content,
        replyBy
      })
      ElMessage.success('回复提交成功')
      dialogVisible.value = false
      fetchRepliesList()
    } catch (error) {
      console.error('提交回复失败:', error)
      ElMessage.error('提交回复失败，请确认工单ID存在')
    } finally {
      saving.value = false
    }
  })
}

/**
 * 查看回复详情
 * @param row 回复行数据
 */
const handleView = (row: any) => {
  currentReply.value = row
  viewDialogVisible.value = true
}

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchRepliesList()
}

// 处理页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchRepliesList()
}

// 组件挂载时初始化
onMounted(() => {
  fetchRepliesList()
})
</script>

<style scoped>
.ticket-replies-view {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-actions {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.replies-table-card {
  margin-top: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
