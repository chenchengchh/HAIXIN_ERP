<template>
  <div class="document-upload-view">
    <!-- 上传区域 -->
    <div class="upload-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>文档上传</span>
          </div>
        </template>
        <div class="upload-content">
          <el-upload
            v-model:file-list="fileList"
            action="#"
            :auto-upload="false"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :before-remove="beforeRemove"
            multiple
            :limit="10"
            :on-exceed="handleExceed"
            accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.jpg,.jpeg,.png,.zip,.rar"
            list-type="picture-card"
          >
            <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                支持上传 PDF、Word、Excel、PPT、图片、压缩包等格式，单文件不超过 50MB
              </div>
            </template>
          </el-upload>
          
          <!-- 上传表单 -->
          <div class="upload-form-section">
            <h3>上传设置</h3>
            <el-form :model="uploadForm" label-width="100px">
              <el-form-item label="文档分类">
                <el-select v-model="uploadForm.categoryId" placeholder="选择分类" style="width: 100%">
                  <el-option label="请选择分类" value="" />
                  <el-option v-for="category in categories" :key="category.id" :label="category.categoryName" :value="category.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="文档标签">
                <el-input
                  v-model="uploadForm.tags"
                  type="textarea"
                  placeholder="请输入标签，用逗号分隔"
                  :rows="2"
                />
              </el-form-item>
              <el-form-item label="文档描述">
                <el-input
                  v-model="uploadForm.description"
                  type="textarea"
                  placeholder="请输入文档描述"
                  :rows="3"
                />
              </el-form-item>
              <el-form-item label="权限设置">
                <el-select v-model="uploadForm.permissionType" placeholder="选择权限类型" style="width: 100%">
                  <el-option label="私有" value="private" />
                  <el-option label="部门可见" value="department" />
                  <el-option label="公开" value="public" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUpload" :disabled="fileList.length === 0">
                  <el-icon><Upload /></el-icon>
                  开始上传
                </el-button>
                <el-button @click="clearFiles">清空列表</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 上传历史 -->
    <div class="upload-history">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>最近上传</span>
          </div>
        </template>
        <div class="history-content">
          <el-table :data="uploadHistory" stripe style="width: 100%">
            <el-table-column prop="documentName" label="文档名称" min-width="180" />
            <el-table-column prop="fileType" label="文件类型" width="100" align="center">
              <template #default="scope">
                <el-tag size="small">{{ scope.row.fileType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="fileSize" label="文件大小" width="120" align="center">
              <template #default="scope">
                {{ formatFileSize(scope.row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column prop="uploadTime" label="上传时间" width="180" align="center" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="getStatusTagType(scope.row.status)" size="small">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewUploadedDocument(scope.row)">
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Plus, Upload, View } from '@element-plus/icons-vue'
import type { UploadFile, UploadProps } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'

// 文件列表
const fileList = ref<UploadFile[]>([])

// 上传表单
const uploadForm = reactive({
  categoryId: '',
  tags: '',
  description: '',
  permissionType: 'private'
})

// 分类列表
const categories = ref([
  { id: 1, categoryName: '技术文档', parentId: 0, categoryPath: '/1', sortOrder: 1, icon: 'document' },
  { id: 2, categoryName: '产品文档', parentId: 0, categoryPath: '/2', sortOrder: 2, icon: 'product' },
  { id: 3, categoryName: '运营文档', parentId: 0, categoryPath: '/3', sortOrder: 3, icon: 'operation' },
  { id: 4, categoryName: '财务文档', parentId: 0, categoryPath: '/4', sortOrder: 4, icon: 'finance' },
  { id: 5, categoryName: '人事文档', parentId: 0, categoryPath: '/5', sortOrder: 5, icon: 'people' }
])

// 上传历史
const uploadHistory = ref([
  {
    id: 1,
    documentName: '产品需求文档.pdf',
    fileType: 'pdf',
    fileSize: 1024 * 1024 * 2.5,
    uploadTime: '2025-12-17 10:30',
    status: '成功'
  },
  {
    id: 2,
    documentName: '技术设计文档.docx',
    fileType: 'doc',
    fileSize: 1024 * 1024 * 1.8,
    uploadTime: '2025-12-16 14:20',
    status: '成功'
  },
  {
    id: 3,
    documentName: '财务报表.xlsx',
    fileType: 'xls',
    fileSize: 1024 * 1024 * 0.8,
    uploadTime: '2025-12-15 09:15',
    status: '成功'
  }
])

// 格式化文件大小
const formatFileSize = (size: number) => {
  if (size < 1024) {
    return size + ' B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + ' KB'
  } else {
    return (size / (1024 * 1024)).toFixed(2) + ' MB'
  }
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  return status === '成功' ? 'success' : 'danger'
}

// 预览文件
const handlePreview: UploadProps['onPreview'] = (uploadFile) => {
  console.log('预览文件:', uploadFile)
}

// 删除文件
const handleRemove: UploadProps['onRemove'] = (uploadFile, uploadFiles) => {
  console.log('删除文件:', uploadFile, uploadFiles)
}

// 超过文件限制
const handleExceed: UploadProps['onExceed'] = (files, uploadFiles) => {
  ElMessage.warning(`只能上传 10 个文件，本次选择了 ${files.length} 个文件，已自动忽略多余文件`)
}

// 删除前确认
const beforeRemove: UploadProps['beforeRemove'] = async (uploadFile, uploadFiles) => {
  try {
    await ElMessageBox.confirm(`确定要删除 ${uploadFile.name}？`)
    return true
  } catch {
    return false
  }
}

// 开始上传
const handleUpload = () => {
  console.log('开始上传:', fileList.value, uploadForm)
  // 这里应该调用API上传文件
  ElMessage.success('文件上传成功')
  fileList.value = []
}

// 清空文件列表
const clearFiles = () => {
  fileList.value = []
}

// 查看已上传文档
const viewUploadedDocument = (document: any) => {
  console.log('查看已上传文档:', document)
  // 这里应该跳转到查看文档页面
}
</script>

<style scoped>
.document-upload-view {
  padding: 20px;
  box-sizing: border-box;
}

.upload-section {
  margin-bottom: 20px;
}

.upload-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.avatar-uploader-icon:hover {
  border-color: #409eff;
}

.upload-form-section {
  background-color: #fafafa;
  padding: 20px;
  border-radius: 4px;
}

.upload-form-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.upload-history {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.history-content {
  padding: 10px 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .upload-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .document-upload-view {
    padding: 12px;
  }
  
  .upload-form-section {
    padding: 12px;
  }
}
</style>