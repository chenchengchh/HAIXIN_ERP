<template>
  <div class="contract-attachments-view">
    <!-- 页面操作栏 -->
    <div class="page-actions">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索文件名"
        style="width: 240px; margin-right: 10px;"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-input-number
        v-model="searchForm.contractId"
        placeholder="合同ID"
        :min="1"
        style="width: 140px; margin-right: 10px;"
      />
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button type="primary" @click="handleUpload">
        <el-icon><Upload /></el-icon> 登记附件
      </el-button>
    </div>

    <!-- 附件列表 -->
    <el-card shadow="never" class="attachments-table-card">
      <el-table
        v-loading="loading"
        :data="attachmentsList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="contractId" label="合同ID" width="90" sortable />
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fileUrl" label="文件路径" min-width="220" show-overflow-tooltip />
        <el-table-column prop="fileSize" label="文件大小" width="110" align="right">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploadTime" label="上传时间" width="170" sortable />
        <el-table-column prop="uploadUserName" label="上传人" width="110" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleDownload(scope.row)">下载</el-button>
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 登记附件对话框 -->
    <el-dialog v-model="dialogVisible" title="登记合同附件" width="500px">
      <el-form :model="attachmentForm" :rules="attachmentFormRules" ref="attachmentFormRef" label-width="100px">
        <el-form-item label="合同ID" prop="contractId">
          <el-input-number v-model="attachmentForm.contractId" :min="1" style="width: 100%" placeholder="请输入关联合同ID" />
        </el-form-item>
        <el-form-item label="文件名" prop="fileName">
          <el-input v-model="attachmentForm.fileName" placeholder="请输入文件名" />
        </el-form-item>
        <el-form-item label="文件路径" prop="fileUrl">
          <el-input v-model="attachmentForm.fileUrl" placeholder="请输入文件访问路径（如 /uploads/contracts/xxx.pdf）" />
        </el-form-item>
        <el-form-item label="文件大小" prop="fileSize">
          <el-input-number v-model="attachmentForm.fileSize" :min="0" style="width: 100%" placeholder="文件大小（字节）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSaveAttachment">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看附件详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="附件详情" width="520px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="附件ID">{{ currentAttachment.id }}</el-descriptions-item>
        <el-descriptions-item label="合同ID">{{ currentAttachment.contractId }}</el-descriptions-item>
        <el-descriptions-item label="文件名">{{ currentAttachment.fileName }}</el-descriptions-item>
        <el-descriptions-item label="文件路径">{{ currentAttachment.fileUrl }}</el-descriptions-item>
        <el-descriptions-item label="文件大小">{{ formatFileSize(currentAttachment.fileSize) }}</el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ currentAttachment.uploadTime }}</el-descriptions-item>
        <el-descriptions-item label="上传人">{{ currentAttachment.uploadUserName || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Upload } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api/crm/order'
import { unwrapPageResponse } from '@/api'
import { useAuthStore } from '@/stores/auth'

// 认证仓库（获取当前登录人作为上传人）
const authStore = useAuthStore()

// 附件列表数据
const attachmentsList = ref<any[]>([])

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
  contractId: undefined as number | undefined
})

// 对话框状态
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const currentAttachment = ref<any>({})

// 附件表单引用与数据
const attachmentFormRef = ref()
const attachmentForm = reactive({
  contractId: undefined as number | undefined,
  fileName: '',
  fileUrl: '',
  fileSize: 0
})

// 附件表单验证规则
const attachmentFormRules = reactive({
  contractId: [{ required: true, message: '请输入合同ID', trigger: 'blur' }],
  fileName: [{ required: true, message: '请输入文件名', trigger: 'blur' }],
  fileUrl: [{ required: true, message: '请输入文件路径', trigger: 'blur' }]
})

/**
 * 格式化文件大小
 * @param size 文件大小（字节）
 * @returns 格式化后的文件大小字符串
 */
const formatFileSize = (size: number) => {
  if (size == null) return '-'
  if (size < 1024) {
    return size + ' B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + ' KB'
  } else {
    return (size / (1024 * 1024)).toFixed(2) + ' MB'
  }
}

/**
 * 分页查询附件列表
 */
const fetchAttachmentsList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.contractId) params.contractId = searchForm.contractId
    const response = await orderApi.getContractAttachmentsList(params)
    const { list, total } = unwrapPageResponse<any>(response)
    attachmentsList.value = list
    pagination.total = total
  } catch (error) {
    console.error('获取附件列表失败:', error)
    ElMessage.error('获取附件列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索：重置页码并重新查询
 */
const handleSearch = () => {
  pagination.currentPage = 1
  fetchAttachmentsList()
}

/**
 * 打开登记附件对话框
 */
const handleUpload = () => {
  attachmentForm.contractId = searchForm.contractId
  attachmentForm.fileName = ''
  attachmentForm.fileUrl = ''
  attachmentForm.fileSize = 0
  dialogVisible.value = true
}

/**
 * 保存附件元数据登记
 */
const handleSaveAttachment = () => {
  attachmentFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    saving.value = true
    try {
      await orderApi.createContractAttachment({
        contractId: attachmentForm.contractId,
        fileName: attachmentForm.fileName,
        fileUrl: attachmentForm.fileUrl,
        fileSize: attachmentForm.fileSize,
        uploadUserId: authStore.userInfo?.id ? Number(authStore.userInfo.id) : undefined,
        uploadUserName: authStore.userInfo?.username || 'admin'
      })
      ElMessage.success('附件登记成功')
      dialogVisible.value = false
      fetchAttachmentsList()
    } catch (error) {
      console.error('附件登记失败:', error)
      ElMessage.error('附件登记失败')
    } finally {
      saving.value = false
    }
  })
}

/**
 * 下载附件：新窗口打开文件路径
 * @param row 附件行数据
 */
const handleDownload = (row: any) => {
  if (row.fileUrl) {
    window.open(row.fileUrl, '_blank')
  } else {
    ElMessage.warning('该附件缺少文件路径')
  }
}

/**
 * 查看附件详情
 * @param row 附件行数据
 */
const handleView = (row: any) => {
  currentAttachment.value = row
  viewDialogVisible.value = true
}

/**
 * 删除附件
 * @param row 附件行数据
 */
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除附件「${row.fileName}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await orderApi.deleteContractAttachment(row.id)
      ElMessage.success('删除成功')
      fetchAttachmentsList()
    } catch (error) {
      console.error('删除附件失败:', error)
      ElMessage.error('删除附件失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchAttachmentsList()
}

// 处理当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchAttachmentsList()
}

// 处理表格选择变化
const handleSelectionChange = (selection: any[]) => {
  console.log('选中的行:', selection)
}

// 组件挂载时初始化数据
onMounted(() => {
  fetchAttachmentsList()
})
</script>

<style scoped>
.contract-attachments-view {
  padding: 20px;
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.page-actions {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.attachments-table-card {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
