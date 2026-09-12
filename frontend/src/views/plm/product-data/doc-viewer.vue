<template>
  <div class="doc-viewer-view">
    <h3>文档浏览器</h3>
    
    <!-- 搜索和筛选 -->
    <div class="search-filters">
      <el-card shadow="hover">
        <el-form :model="searchForm" inline>
          <el-form-item label="文档名称">
            <el-input v-model="searchForm.name" placeholder="请输入文档名称"></el-input>
          </el-form-item>
          <el-form-item label="文档类型">
            <el-select v-model="searchForm.type" placeholder="请选择文档类型">
              <el-option label="所有类型" value=""></el-option>
              <el-option label="图纸" value="Drawing"></el-option>
              <el-option label="规格书" value="Spec"></el-option>
              <el-option label="报告" value="Report"></el-option>
              <el-option label="工艺文件" value="Process"></el-option>
              <el-option label="测试文件" value="Test"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="文档状态">
            <el-select v-model="searchForm.status" placeholder="请选择文档状态">
              <el-option label="所有状态" value=""></el-option>
              <el-option label="草稿" value="Draft"></el-option>
              <el-option label="审核中" value="Review"></el-option>
              <el-option label="已发布" value="Released"></el-option>
              <el-option label="已取消" value="Cancelled"></el-option>
              <el-option label="已拒绝" value="Rejected"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="关联物料">
            <el-select v-model="searchForm.refItemId" placeholder="请选择关联物料">
              <el-option label="所有物料" value=""></el-option>
              <el-option
                v-for="item in items"
                :key="item.id"
                :label="item.itemCode + ' - ' + item.name"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchDocs">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    
    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="uploadDoc">上传文档</el-button>
      <el-button @click="importDocs">导入文档</el-button>
      <el-button @click="exportDocs">导出文档</el-button>
      <el-button @click="batchDeleteDocs">批量删除</el-button>
      <el-dropdown>
        <el-button>
          更多操作 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>文档分类管理</el-dropdown-item>
            <el-dropdown-item>文档权限管理</el-dropdown-item>
            <el-dropdown-item>文档版本管理</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    
    <!-- 文档列表 -->
    <el-card shadow="hover" class="doc-list-card">
      <div class="doc-list">
        <el-table
          :data="filteredDocs"
          style="width: 100%"
          v-loading="loading"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="docCode" label="文档编码" min-width="150" sortable></el-table-column>
          <el-table-column prop="name" label="文档名称" min-width="200" sortable></el-table-column>
          <el-table-column prop="type" label="文档类型" min-width="120">
            <template #default="scope">
              <el-tag :type="getDocTypeTagType(scope.row.type)">{{ scope.row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="version" label="版本" min-width="80"></el-table-column>
          <el-table-column prop="status" label="状态" min-width="120">
            <template #default="scope">
              <el-tag :type="getDocStatusTagType(scope.row.status)">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createBy" label="创建人" min-width="120"></el-table-column>
          <el-table-column label="创建时间" min-width="180" sortable>
            <template #default="scope">
              {{ scope.row.createTime }}
            </template>
          </el-table-column>
          <el-table-column label="文件大小" min-width="120">
            <template #default="scope">
              {{ formatFileSize(scope.row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="200" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="previewDoc(scope.row.id)">预览</el-button>
              <el-button size="small" @click="downloadDoc(scope.row.id)">下载</el-button>
              <el-button size="small" @click="editDoc(scope.row.id)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteDoc(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalDocs"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          ></el-pagination>
        </div>
      </div>
    </el-card>
    
    <!-- 文档预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="文档预览" width="80%" :close-on-click-modal="false">
      <div class="doc-preview">
        <div v-if="previewDocType === 'image'">
          <img :src="previewDocUrl" alt="文档预览" class="preview-image">
        </div>
        <div v-else-if="previewDocType === 'pdf'">
          <iframe :src="previewDocUrl" frameborder="0" class="preview-pdf"></iframe>
        </div>
        <div v-else-if="previewDocType === 'cad'">
          <div class="cad-preview-placeholder">
            <el-empty description="CAD文档预览功能开发中"></el-empty>
          </div>
        </div>
        <div v-else>
          <el-empty description="不支持的文档类型"></el-empty>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="previewDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="downloadDoc(previewDocId)">下载</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 上传文档对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传文档" width="60%" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="文档名称">
          <el-input v-model="uploadForm.title" placeholder="请输入文档名称"></el-input>
        </el-form-item>
        <el-form-item label="文档类型">
          <el-select v-model="uploadForm.type" placeholder="请选择文档类型">
            <el-option label="图纸" value="Drawing"></el-option>
            <el-option label="规格书" value="Spec"></el-option>
            <el-option label="报告" value="Report"></el-option>
            <el-option label="工艺文件" value="Process"></el-option>
            <el-option label="测试文件" value="Test"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="关联物料">
          <el-select v-model="uploadForm.refItemId" placeholder="请选择关联物料">
            <el-option
              v-for="item in items"
              :key="item.id"
              :label="item.itemCode + ' - ' + item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="上传文件">
          <el-upload
            action="#"
            :auto-upload="false"
            :multiple="false"
            :on-change="handleUploadFileChange"
            accept=".pdf,.doc,.docx,.xls,.xlsx,.dwg,.dxf,.jpg,.jpeg,.png,.gif"
          >
            <el-button type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传 PDF, DOC, DOCX, XLS, XLSX, DWG, DXF, JPG, JPEG, PNG, GIF 格式文件，单个文件不超过 100MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleUpload">上传</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useProductDataStore } from '@/stores/plm/productData'

const store = useProductDataStore()

// 搜索表单
const searchForm = ref({
  name: '',
  type: '',
  status: '',
  refItemId: ''
})

const items = computed(() => {
  return (store.materials || []).map((m: any) => ({
    id: Number(m.id),
    itemCode: m.code,
    name: m.name
  }))
})

const docs = computed(() => {
  return (store.documents || []).map((d: any) => ({
    id: Number(d.id),
    docCode: d.code,
    name: d.title,
    type: d.type,
    version: d.version,
    status: d.status,
    createBy: d.author,
    createTime: d.createTime,
    fileSize: Number(d.fileSize || 0),
    filePath: d.filePath || '',
    refItemId: d.refItemId
  }))
})

const currentPage = computed({
  get: () => store.pagination.page,
  set: (value) => store.setPagination(value, store.pagination.pageSize)
})
const pageSize = computed({
  get: () => store.pagination.pageSize,
  set: (value) => store.setPagination(store.pagination.page, value)
})

// 加载状态
const loading = ref(false)

// 选中的文档
const selectedDocs = ref<any[]>([])

// 文档预览对话框
const previewDialogVisible = ref(false)
const previewDocId = ref(0)
const previewDocUrl = ref('')
const previewDocType = ref('')

// 上传对话框
const uploadDialogVisible = ref(false)

const filteredDocs = computed(() => docs.value)
const totalDocs = computed(() => store.pagination.total)

// 文档类型标签类型映射
const getDocTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'Drawing': 'info',
    'Spec': 'success',
    'Report': 'info',
    'Process': 'warning',
    'Test': 'danger'
  }
  return typeMap[type] || 'info'
}

// 文档状态标签类型映射
const getDocStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    'Draft': 'info',
    'Review': 'warning',
    'Released': 'success',
    'Cancelled': 'danger',
    'Rejected': 'warning'
  }
  return statusMap[status] || 'info'
}

// 格式化文件大小
const formatFileSize = (size: number) => {
  if (size < 1024) {
    return size + ' B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + ' KB'
  } else if (size < 1024 * 1024 * 1024) {
    return (size / (1024 * 1024)).toFixed(2) + ' MB'
  } else {
    return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
  }
}

const searchDocs = async () => {
  store.setPagination(1, store.pagination.pageSize)
  store.setDocumentSearchKeyword(searchForm.value.name || '')
  store.setDocumentTypeFilter(searchForm.value.type || 'all')
  await store.fetchDocuments()
}

const resetSearch = async () => {
  searchForm.value = { name: '', type: '', status: '', refItemId: '' }
  store.resetDocumentFilters()
  await store.fetchDocuments()
}

// 上传文档
const uploadDoc = () => {
  uploadDialogVisible.value = true
}

const uploadForm = ref({
  title: '',
  type: '',
  refItemId: ''
})
const uploadFile = ref<File | null>(null)

const handleUploadFileChange = (file: any) => {
  uploadFile.value = file?.raw || null
}

const handleUpload = async () => {
  if (!uploadForm.value.title) {
    ElMessage.warning('请输入文档名称')
    return
  }
  if (!uploadForm.value.type) {
    ElMessage.warning('请选择文档类型')
    return
  }
  if (!uploadFile.value) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  const formData = new FormData()
  formData.append('file', uploadFile.value)
  formData.append('title', uploadForm.value.title)
  formData.append('docType', uploadForm.value.type)
  formData.append('category', '')
  formData.append('version', 'V1.0')
  formData.append('status', 'draft')
  formData.append('author', '当前用户')
  await store.uploadDocument(formData)
  ElMessage.success('文档上传成功')
  uploadDialogVisible.value = false
  uploadForm.value = { title: '', type: '', refItemId: '' }
  uploadFile.value = null
  await store.fetchDocuments()
}

// 导入文档
const importDocs = () => {
  ElMessage.info('请使用上传文档功能导入')
}

// 导出文档
const exportDocs = () => {
  const content = JSON.stringify(store.documents || [], null, 2)
  const blob = new Blob([content], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `documents_${new Date().toISOString().slice(0, 10)}.json`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('文档导出成功')
}

// 批量删除文档
const batchDeleteDocs = () => {
  if (selectedDocs.value.length === 0) {
    ElMessage.warning('请先选择要删除的文档')
    return
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedDocs.value.length} 个文档吗？`, '批量删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const idsToDelete = selectedDocs.value.map(doc => doc.id)
    await Promise.all(idsToDelete.map((id: any) => store.deleteDocument(id)))
    selectedDocs.value = []
    ElMessage.success('批量删除成功')
    await store.fetchDocuments()
  }).catch(() => {
    // 取消删除
  })
}

// 预览文档
const previewDoc = (id: number) => {
  const doc = docs.value.find(d => d.id === id)
  if (doc) {
    previewDocId.value = id
    previewDocUrl.value = 'https://picsum.photos/800/600'
    
    // 根据文件路径判断文档类型
    const ext = doc.filePath.split('.').pop()?.toLowerCase()
    if (['jpg', 'jpeg', 'png', 'gif'].includes(ext || '')) {
      previewDocType.value = 'image'
    } else if (ext === 'pdf') {
      previewDocType.value = 'pdf'
    } else if (['dwg', 'dxf'].includes(ext || '')) {
      previewDocType.value = 'cad'
    } else {
      previewDocType.value = 'other'
    }
    
    previewDialogVisible.value = true
  }
}

// 下载文档
const downloadDoc = async (id: number) => {
  const response: any = await store.downloadDocument(id)
  const blob = new Blob([response.data], { type: response.headers?.['content-type'] || 'application/octet-stream' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `document_${id}`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('文档下载成功')
}

// 编辑文档
const editDoc = (id: number) => {
  const doc = store.documents.find((d: any) => String(d.id) === String(id))
  if (!doc) return
  ElMessageBox.prompt('请输入新的文档标题', '编辑文档', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: doc.title
  }).then(async ({ value }) => {
    await store.updateDocument(id, { ...doc, title: value })
    ElMessage.success('文档编辑成功')
    await store.fetchDocuments()
  }).catch(() => {})
}

// 删除文档
const deleteDoc = (id: number) => {
  ElMessageBox.confirm('确定要删除该文档吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    store.deleteDocument(id).then(async () => {
      ElMessage.success('文档删除成功')
      await store.fetchDocuments()
    })
  }).catch(() => {
    // 取消删除
  })
}

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedDocs.value = selection
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  store.setPagination(1, size)
  store.fetchDocuments()
}

// 当前页码变化
const handleCurrentChange = (page: number) => {
  store.setPagination(page, store.pagination.pageSize)
  store.fetchDocuments()
}

onMounted(async () => {
  await store.fetchMaterials()
  await store.fetchDocuments()
})
</script>

<style scoped lang="scss">
.doc-viewer-view {
  padding: 20px;
  height: 100%;
  overflow: auto;

  h3 {
    margin-bottom: 20px;
    color: #333;
    font-size: 1.3rem;
  }

  /* 搜索和筛选 */
  .search-filters {
    margin-bottom: 20px;
  }

  /* 操作按钮 */
  .action-buttons {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    flex-wrap: wrap;
  }

  /* 文档列表卡片 */
  .doc-list-card {
    height: calc(100% - 200px);
    display: flex;
    flex-direction: column;
  }

  .doc-list {
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .el-table {
    flex: 1;
  }

  /* 分页 */
  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  /* 文档预览 */
  .doc-preview {
    width: 100%;
    height: 500px;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .preview-image {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
  }

  .preview-pdf {
    width: 100%;
    height: 100%;
  }

  .cad-preview-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  /* 响应式设计 */
  @media (max-width: 1024px) {
    padding: 16px;

    .search-filters {
      margin-bottom: 16px;
    }

    .action-buttons {
      margin-bottom: 16px;
      gap: 8px;
    }

    .doc-list-card {
      height: calc(100% - 180px);
    }

    .doc-preview {
      height: 400px;
    }
  }

  @media (max-width: 768px) {
    padding: 12px;

    h3 {
      font-size: 1.1rem;
      margin-bottom: 16px;
    }

    .action-buttons {
      gap: 6px;
    }

    .doc-list-card {
      height: calc(100% - 220px);
    }

    .doc-preview {
      height: 300px;
    }

    .el-button {
      padding: 4px 8px;
      font-size: 12px;
    }
  }
}
</style>
