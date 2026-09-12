<template>
  <div class="process-file-container">
    <div class="process-file-header">
      <h2>工艺文件管理</h2>
      <el-button type="primary" @click="uploadFile">
        <el-icon><Upload /></el-icon> 上传工艺文件
      </el-button>
    </div>

    <div class="process-file-content">
      <!-- 搜索和筛选 -->
      <div class="search-filter">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文件名或编码"
          prefix-icon="Search"
          clearable
          @input="handleSearch"
        />
        <el-select
          v-model="typeFilter"
          placeholder="文件类型"
          clearable
          @change="handleFilter"
        >
          <el-option label="所有类型" value="all" />
          <el-option label="工艺指导书" value="guide" />
          <el-option label="工艺规范" value="spec" />
          <el-option label="工艺参数表" value="param" />
          <el-option label="检验标准" value="standard" />
        </el-select>
      </div>

      <!-- 工艺文件列表 -->
      <el-table
        :data="filteredProcessFiles"
        style="width: 100%"
        @row-click="selectFile"
        highlight-current-row
      >
        <el-table-column prop="code" label="文件编码" width="150" />
        <el-table-column prop="title" label="文件名称" min-width="200" />
        <el-table-column label="文件类型" width="120">
          <template #default="scope">
            <el-tag :type="getFileTypeTagType(scope.row.type)">
              {{ getFileTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="所属分类" width="120" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
              <el-tag :type="fileStatusMap[scope.row.status as FileStatus]">
                {{ fileStatusText[scope.row.status as FileStatus] }}
              </el-tag>
            </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="100" />
        <el-table-column prop="updateTime" label="更新时间" width="150" />
        <el-table-column label="文件大小" width="100">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click.stop="previewFile(scope.row)">
              <el-icon><View /></el-icon> 预览
            </el-button>
            <el-button link type="primary" @click.stop="downloadFile(scope.row)">
              <el-icon><Download /></el-icon> 下载
            </el-button>
            <el-button link type="primary" @click.stop="editFile(scope.row)" :disabled="scope.row.status === 'approved'">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="processFiles.length"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 文件预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="工艺文件预览"
      width="80%"
      :close-on-click-modal="false"
    >
      <div class="file-preview" v-if="selectedFile">
        <div class="file-info">
          <h3>{{ selectedFile.title }}</h3>
          <div class="file-meta">
            <span>编码：{{ selectedFile.code }}</span>
            <span>版本：{{ selectedFile.version }}</span>
            <span>作者：{{ selectedFile.author }}</span>
            <span>更新时间：{{ selectedFile.updateTime }}</span>
          </div>
        </div>
        <div class="file-content">
          <div v-if="selectedFile.fileType === 'pdf'" class="pdf-preview">
            <el-empty description="PDF预览功能开发中" />
          </div>
          <div v-else-if="['jpg', 'jpeg', 'png', 'gif'].includes(selectedFile.fileType)" class="image-preview">
            <img :src="selectedFile.filePath" alt="文件预览" style="max-width: 100%; max-height: 600px;" />
          </div>
          <div v-else-if="selectedFile.fileType === 'dwg'" class="dwg-preview">
            <el-empty description="CAD图纸预览功能开发中" />
          </div>
          <div v-else-if="['doc', 'docx'].includes(selectedFile.fileType)" class="doc-preview">
            <el-empty description="Word文档预览功能开发中" />
          </div>
          <div v-else-if="['xls', 'xlsx'].includes(selectedFile.fileType)" class="xls-preview">
            <el-empty description="Excel文档预览功能开发中" />
          </div>
          <div v-else class="other-preview">
            <el-empty description="不支持的文件类型" />
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="previewDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="downloadFile(selectedFile)">下载文件</el-button>
        </div>
      </template>
    </el-dialog>
  <!-- 上传工艺文件对话框 -->
  <el-dialog
    v-model="uploadDialogVisible"
    title="上传工艺文件"
    width="60%"
    :close-on-click-modal="false"
  >
    <el-form :model="uploadForm" ref="uploadFormRef" :rules="uploadRules" label-position="top">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="文件名称" prop="title">
            <el-input v-model="uploadForm.title" placeholder="请输入文件名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="文件编码" prop="code">
            <el-input v-model="uploadForm.code" placeholder="请输入文件编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="文件类型" prop="type">
            <el-select v-model="uploadForm.type" placeholder="请选择文件类型">
              <el-option label="工艺指导书" value="guide" />
              <el-option label="工艺规范" value="spec" />
              <el-option label="工艺参数表" value="param" />
              <el-option label="检验标准" value="standard" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属分类" prop="category">
            <el-input v-model="uploadForm.category" placeholder="请输入所属分类" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="上传文件" prop="file">
            <el-upload
              ref="fileUploadRef"
              v-model:file-list="uploadFileList"
              :auto-upload="false"
              :on-change="handleFileChange"
              :limit="1"
              accept=".pdf,.doc,.docx,.xls,.xlsx,.dwg,.dxf,.jpg,.jpeg,.png,.gif"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon> 点击上传
              </el-button>
              <template #tip>
                <div class="el-upload__tip">
                  支持上传 PDF, DOC, DOCX, XLS, XLSX, DWG, DXF, JPG, JPEG, PNG, GIF 格式文件，单个文件不超过 100MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancelUpload">取消</el-button>
        <el-button type="primary" @click="submitUpload" :loading="uploadLoading">
          确定上传
        </el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 编辑工艺文件对话框 -->
  <el-dialog
    v-model="editDialogVisible"
    title="编辑工艺文件"
    width="60%"
    :close-on-click-modal="false"
  >
    <el-form :model="editForm" ref="editFormRef" :rules="editRules" label-position="top">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="文件名称" prop="title">
            <el-input v-model="editForm.title" placeholder="请输入文件名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="文件编码" prop="code">
            <el-input v-model="editForm.code" placeholder="请输入文件编码" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="文件类型" prop="type">
            <el-select v-model="editForm.type" placeholder="请选择文件类型">
              <el-option label="工艺指导书" value="guide" />
              <el-option label="工艺规范" value="spec" />
              <el-option label="工艺参数表" value="param" />
              <el-option label="检验标准" value="standard" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属分类" prop="category">
            <el-input v-model="editForm.category" placeholder="请输入所属分类" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="版本" prop="version">
            <el-input v-model="editForm.version" placeholder="请输入版本号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="文件状态" prop="status">
            <el-select v-model="editForm.status" placeholder="请选择文件状态">
              <el-option label="草稿" value="draft" />
              <el-option label="待审批" value="pending" />
              <el-option label="已批准" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注">
            <el-input v-model="editForm.remark" placeholder="请输入备注" type="textarea" rows="3" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">
          确定保存
        </el-button>
      </span>
    </template>
  </el-dialog>
</div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { Upload, Search, View, Download, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useProcessCollaborationStore } from '../../../stores/plm/processCollaboration'

const store = useProcessCollaborationStore()

// 搜索和筛选
const searchKeyword = ref('')
const typeFilter = ref('all')

// 预览对话框
const previewDialogVisible = ref(false)
const selectedFile = ref<any>(null)

// 上传对话框
const uploadDialogVisible = ref(false)
const uploadForm = ref({
  title: '',
  code: '',
  type: 'guide',
  category: '',
  file: null
})
const uploadFormRef = ref<any>(null)
const uploadFileList = ref<any[]>([])
const uploadLoading = ref(false)
const fileUploadRef = ref<any>(null)

// 编辑对话框
const editDialogVisible = ref(false)
const editForm = ref({
  id: '',
  title: '',
  code: '',
  type: 'guide',
  category: '',
  version: 'V1.0',
  status: 'draft',
  remark: ''
})
const editFormRef = ref<any>(null)
const editLoading = ref(false)

// 工艺文件状态类型
type FileStatus = 'approved' | 'draft' | 'pending' | 'rejected'

// 工艺文件类型
type FileType = 'guide' | 'spec' | 'param' | 'standard'

// 工艺文件状态映射
const fileStatusMap: Record<FileStatus, string> = {
  approved: 'success',
  draft: 'warning',
  pending: 'info',
  rejected: 'danger'
}

const fileStatusText: Record<FileStatus, string> = {
  approved: '已批准',
  draft: '草稿',
  pending: '待审批',
  rejected: '已拒绝'
}

// 文件大小格式化：字节数转换为 KB/MB 可读格式
const formatFileSize = (size: number | string) => {
  const bytes = Number(size)
  if (!bytes || isNaN(bytes) || bytes <= 0) return '0 B'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
}

// 工艺文件类型映射
const fileTypeMap: Record<FileType, string> = {
  guide: '工艺指导书',
  spec: '工艺规范',
  param: '工艺参数表',
  standard: '检验标准'
}

// 获取工艺文件类型标签类型
const getFileTypeTagType = (type: string) => {
  const typeMap = {
    guide: 'info',
    spec: 'success',
    param: 'warning',
    standard: 'info'
  }
  return typeMap[type as keyof typeof typeMap] || 'info'
}

// 获取工艺文件类型文本
const getFileTypeText = (type: string) => {
  return fileTypeMap[type as keyof typeof fileTypeMap] || type
}

// 分页信息
const pagination = computed(() => ({
  page: store.pagination.page,
  pageSize: store.pagination.pageSize
}))

// 工艺文件列表
const processFiles = computed(() => store.processFiles)

// 筛选后的工艺文件列表（由后端分页/筛选返回）
const filteredProcessFiles = computed(() => processFiles.value)

/**
 * 重新加载工艺文件列表（基于当前筛选/分页）。
 */
const reloadFiles = async () => {
  await store.fetchProcessFiles()
}

// 表单验证规则
const uploadRules = reactive({
  title: [
    { required: true, message: '请输入文件名称', trigger: 'blur' },
    { min: 2, max: 50, message: '文件名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入文件编码', trigger: 'blur' },
    { min: 2, max: 20, message: '文件编码长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择文件类型', trigger: 'change' }
  ],
  category: [
    { required: true, message: '请输入所属分类', trigger: 'blur' }
  ]
})

const editRules = reactive({
  title: [
    { required: true, message: '请输入文件名称', trigger: 'blur' },
    { min: 2, max: 50, message: '文件名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择文件类型', trigger: 'change' }
  ],
  category: [
    { required: true, message: '请输入所属分类', trigger: 'blur' }
  ],
  version: [
    { required: true, message: '请输入版本号', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择文件状态', trigger: 'change' }
  ]
})

// 页面加载时获取工艺文件数据
onMounted(async () => {
  await store.fetchProcessFiles()
})

// 上传工艺文件
const uploadFile = () => {
  uploadDialogVisible.value = true
}

// 文件变更处理
const handleFileChange = (file: any) => {
  uploadForm.value.file = file.raw
}

// 提交上传
const submitUpload = async () => {
  if (!uploadFormRef.value) return
  
  try {
    await uploadFormRef.value.validate()
    if (!uploadForm.value.file) {
      ElMessage.warning('请选择要上传的文件')
      return
    }
    uploadLoading.value = true

    const formData = new FormData()
    formData.append('file', uploadForm.value.file as any)
    formData.append('title', uploadForm.value.title)
    formData.append('code', uploadForm.value.code)
    formData.append('type', uploadForm.value.type)
    formData.append('category', uploadForm.value.category)
    formData.append('version', 'V1.0')
    formData.append('status', 'draft')
    formData.append('author', '当前用户')
    const created = await store.uploadProcessFile(formData)
    if (created) {
      ElMessage.success('工艺文件上传成功')
      uploadDialogVisible.value = false
      resetUploadForm()
      await reloadFiles()
    } else {
      ElMessage.error('工艺文件上传失败')
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    uploadLoading.value = false
  }
}

// 重置上传表单
const resetUploadForm = () => {
  uploadForm.value = {
    title: '',
    code: '',
    type: 'guide',
    category: '',
    file: null
  }
  uploadFileList.value = []
  if (uploadFormRef.value) {
    uploadFormRef.value.resetFields()
  }
  if (fileUploadRef.value) {
    fileUploadRef.value.clearFiles()
  }
}

// 取消上传
const cancelUpload = () => {
  uploadDialogVisible.value = false
  resetUploadForm()
}

// 编辑工艺文件
const editFile = (file: any) => {
  editForm.value = {
    id: file.id,
    title: file.title,
    code: file.code,
    type: file.type,
    category: file.category,
    version: file.version,
    status: file.status,
    remark: ''
  }
  editDialogVisible.value = true
}

// 提交编辑
const submitEdit = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    editLoading.value = true

    const updated = await store.updateProcessFile({ ...editForm.value })
    if (updated) {
      ElMessage.success('工艺文件更新成功')
      editDialogVisible.value = false
    } else {
      ElMessage.error('工艺文件更新失败')
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    editLoading.value = false
  }
}

// 取消编辑
const cancelEdit = () => {
  editDialogVisible.value = false
  if (editFormRef.value) {
    editFormRef.value.resetFields()
  }
}

// 搜索处理
const handleSearch = async () => {
  store.setSearchKeyword(searchKeyword.value)
  store.setPagination(1, store.pagination.pageSize)
  await reloadFiles()
}

// 筛选处理
const handleFilter = async () => {
  store.setFileTypeFilter(typeFilter.value)
  store.setPagination(1, store.pagination.pageSize)
  await reloadFiles()
}

// 选择工艺文件
const selectFile = (file: any) => {
  selectedFile.value = file
}

// 预览工艺文件
const previewFile = (file: any) => {
  selectedFile.value = file
  previewDialogVisible.value = true
}

// 下载工艺文件
const downloadFile = async (file: any) => {
  const response: any = await store.downloadProcessFile(file.id)
  const blob = new Blob([response.data], { type: response.headers?.['content-type'] || 'application/octet-stream' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = file?.title ? String(file.title) : `process_file_${file?.id || ''}`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('工艺文件下载成功')
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  store.setPagination(1, size)
  reloadFiles()
}

// 页码变化
const handleCurrentChange = (current: number) => {
  store.setPagination(current, store.pagination.pageSize)
  reloadFiles()
}
</script>

<style scoped>
.process-file-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.process-file-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.process-file-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.search-filter {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.search-filter .el-input {
  width: 300px;
}

.search-filter .el-select {
  width: 150px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.file-preview {
  padding: 20px;
}

.file-info {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e5e7eb;
}

.file-info h3 {
  margin: 0 0 10px 0;
  font-size: 20px;
  color: #333;
}

.file-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
}

.file-content {
  margin-top: 20px;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f9fafb;
  border-radius: 4px;
  padding: 20px;
}

.pdf-preview,
.dwg-preview,
.doc-preview,
.xls-preview,
.other-preview {
  width: 100%;
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
}

.image-preview {
  text-align: center;
}

.image-preview img {
  max-width: 100%;
  max-height: 500px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
</style>
