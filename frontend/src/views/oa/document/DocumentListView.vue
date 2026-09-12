<template>
  <div class="document-list-view">
    <!-- 搜索和筛选区域 -->
    <div class="search-filter">
      <el-form :inline="true" :model="searchParams" class="filter-form">
        <el-form-item label="文档名称">
          <el-input v-model="searchParams.title" placeholder="输入文档名称" size="small" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchParams.categoryId" placeholder="选择分类" size="small" clearable style="width: 160px">
            <el-option label="全部" value="" />
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件类型">
          <el-select v-model="searchParams.type" placeholder="选择文件类型" size="small" clearable style="width: 140px">
            <el-option label="全部" value="" />
            <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="上传人">
          <el-input v-model="searchParams.creatorName" placeholder="输入上传人姓名" size="small" clearable />
        </el-form-item>
        <el-form-item label="上传时间">
          <el-date-picker
            v-model="searchParams.uploadTime"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            size="small"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search" size="small">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="reset" size="small">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      <div class="header-actions">
        <el-button type="primary" size="small" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          新建文档
        </el-button>
      </div>
    </div>

    <!-- 文档列表 -->
    <div class="document-list">
      <el-table :data="documentList" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="文档ID" width="80" align="center" />
        <el-table-column prop="title" label="文档名称" min-width="180">
          <template #default="scope">
            <el-link type="primary" @click="viewDocument(scope.row)">{{ scope.row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="所属分类" width="120" align="center">
          <template #default="scope">
            {{ scope.row.categoryName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="文件类型" width="100" align="center">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.type || '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentVersion" label="版本号" width="80" align="center">
          <template #default="scope">
            {{ scope.row.currentVersion || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="上传人" width="100" align="center">
          <template #default="scope">
            {{ scope.row.creatorName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="160" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center">
          <template #default="scope">
            <el-button type="warning" size="small" @click="editDocument(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              v-if="scope.row.status === 0 || scope.row.status === 2"
              type="success"
              size="small"
              @click="doStatusAction(scope.row, 'publish')"
            >
              发布
            </el-button>
            <el-button
              v-if="scope.row.status === 1"
              type="warning"
              size="small"
              @click="doStatusAction(scope.row, 'revoke')"
            >
              撤销
            </el-button>
            <el-button
              v-if="scope.row.status === 1"
              type="info"
              size="small"
              @click="doStatusAction(scope.row, 'archive')"
            >
              归档
            </el-button>
            <el-button type="danger" size="small" @click="deleteDocument(scope.row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="displayTotal"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 文档详情对话框 -->
    <el-dialog v-model="detailVisible" title="文档详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="文档ID">{{ currentDocument?.id ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="文档名称">{{ currentDocument?.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属分类">{{ currentDocument?.categoryName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="文件类型">{{ currentDocument?.type || '-' }}</el-descriptions-item>
        <el-descriptions-item label="版本号">{{ currentDocument?.currentVersion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(currentDocument?.status)" size="small">
            {{ getStatusText(currentDocument?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="上传人">{{ currentDocument?.creatorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ formatDateTime(currentDocument?.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(currentDocument?.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="文档描述" :span="2">{{ currentDocument?.description || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 文档编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="resetForm"
    >
      <el-form :model="documentForm" label-width="80px">
        <el-form-item label="文档名称" required>
          <el-input v-model="documentForm.title" placeholder="输入文档名称" />
        </el-form-item>
        <el-form-item label="所属分类">
          <el-select
            v-model="documentForm.categoryId"
            placeholder="选择分类"
            style="width: 100%"
            @change="handleFormCategoryChange"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="文件类型">
          <el-select v-model="documentForm.type" placeholder="选择文件类型" style="width: 100%">
            <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="文档描述">
          <el-input
            v-model="documentForm.description"
            type="textarea"
            :rows="3"
            placeholder="输入文档描述（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveDocument">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Edit, Delete, Plus } from '@element-plus/icons-vue'
import { documentApi, documentCategoryApi } from '@/api/oa'
import type { OaDocument, DocumentCategory } from '@/api/oa'
import { unwrapListResponse, unwrapPageResponse } from '@/api'

// 文件类型选项
const typeOptions = ['制度文件', '技术文档', '合同', '其他']

// 搜索参数
const searchParams = reactive({
  title: '',
  categoryId: '' as string | number,
  type: '',
  creatorName: '',
  uploadTime: [] as string[]
})

// 分页参数
const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

// 分类下拉列表
const categories = ref<DocumentCategory[]>([])
// 后端返回的原始文档数据（服务端分页时为当前页数据，分类筛选时为全量数据）
const rawList = ref<OaDocument[]>([])
// 服务端分页总条数
const serverTotal = ref(0)
// 是否前端分页（选择分类筛选时分类接口返回全量数组，由前端分页）
const isClientPaging = ref(false)
// 列表加载状态
const loading = ref(false)
// 保存按钮加载状态
const saving = ref(false)

// 文档编辑对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新建文档')
const documentForm = reactive({
  id: 0,
  title: '',
  categoryId: undefined as number | undefined,
  categoryName: '',
  type: '制度文件',
  description: ''
})

// 文档详情对话框
const detailVisible = ref(false)
const currentDocument = ref<OaDocument | null>(null)

/**
 * 提取后端返回的错误提示信息
 * @param error 捕获的异常对象
 * @param defaultMsg 默认提示语
 */
const getErrorMessage = (error: any, defaultMsg: string): string => {
  return error?.data?.msg || error?.response?.data?.msg || error?.msg || error?.message || defaultMsg
}

/**
 * 格式化日期时间显示
 * @param value 后端返回的时间字符串
 */
const formatDateTime = (value?: string): string => {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

/**
 * 获取文档状态标签类型
 * @param status 文档状态（0草稿/1已发布/2已撤销/3已归档）
 */
const getStatusTagType = (status?: number): 'success' | 'warning' | 'danger' | 'info' => {
  switch (status) {
    case 1: return 'success'
    case 2: return 'warning'
    case 3: return 'danger'
    default: return 'info'
  }
}

/**
 * 获取文档状态显示文本
 * @param status 文档状态（0草稿/1已发布/2已撤销/3已归档）
 */
const getStatusText = (status?: number): string => {
  switch (status) {
    case 0: return '草稿'
    case 1: return '已发布'
    case 2: return '已撤销'
    case 3: return '已归档'
    default: return '未知'
  }
}

/**
 * 加载分类下拉选项
 */
const loadCategories = async () => {
  try {
    const response = await documentCategoryApi.getList()
    categories.value = unwrapListResponse<DocumentCategory>(response)
  } catch (error: any) {
    ElMessage.error(getErrorMessage(error, '获取分类列表失败'))
  }
}

/**
 * 加载文档列表（选择分类时走分类接口并前端分页，否则走分页接口）
 */
const loadDocuments = async () => {
  loading.value = true
  try {
    if (searchParams.categoryId !== '' && searchParams.categoryId !== undefined && searchParams.categoryId !== null) {
      // 按分类筛选：分类接口返回全量数组，配合前端过滤与分页
      const response = await documentApi.getByCategoryId(Number(searchParams.categoryId))
      rawList.value = unwrapListResponse<OaDocument>(response)
      isClientPaging.value = true
    } else {
      const response = await documentApi.getPage({
        page: pagination.currentPage,
        size: pagination.pageSize
      })
      const pageData = unwrapPageResponse<OaDocument>(response)
      rawList.value = pageData.list
      serverTotal.value = pageData.total
      isClientPaging.value = false
    }
  } catch (error: any) {
    ElMessage.error(getErrorMessage(error, '获取文档列表失败'))
  } finally {
    loading.value = false
  }
}

/**
 * 前端过滤后的文档列表（标题关键字、文件类型、上传人、上传时间）
 */
const filteredList = computed<OaDocument[]>(() => {
  const keyword = searchParams.title.trim().toLowerCase()
  const uploader = searchParams.creatorName.trim()
  return rawList.value.filter(item => {
    if (keyword && !(item.title || '').toLowerCase().includes(keyword)) return false
    if (searchParams.type && item.type !== searchParams.type) return false
    if (uploader && !(item.creatorName || '').includes(uploader)) return false
    if (searchParams.uploadTime && searchParams.uploadTime.length === 2) {
      const [rangeStart, rangeEnd] = searchParams.uploadTime
      const date = (item.createTime || '').slice(0, 10)
      if (rangeStart && rangeEnd && date && (date < rangeStart || date > rangeEnd)) return false
    }
    return true
  })
})

/**
 * 当前页展示的文档列表（前端分页时对过滤结果切片）
 */
const documentList = computed<OaDocument[]>(() => {
  if (isClientPaging.value) {
    const start = (pagination.currentPage - 1) * pagination.pageSize
    return filteredList.value.slice(start, start + pagination.pageSize)
  }
  return filteredList.value
})

/**
 * 分页总条数（前端分页时为过滤后总数，否则为服务端总数）
 */
const displayTotal = computed<number>(() => {
  return isClientPaging.value ? filteredList.value.length : serverTotal.value
})

/**
 * 搜索：回到第一页并重新加载数据
 */
const search = () => {
  pagination.currentPage = 1
  loadDocuments()
}

/**
 * 重置搜索条件并重新加载数据
 */
const reset = () => {
  Object.assign(searchParams, {
    title: '',
    categoryId: '',
    type: '',
    creatorName: '',
    uploadTime: []
  })
  pagination.currentPage = 1
  loadDocuments()
}

/**
 * 分页大小变化处理
 * @param size 每页条数
 */
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  // 前端分页由计算属性自动更新，仅服务端分页需要重新请求
  if (!isClientPaging.value) {
    loadDocuments()
  }
}

/**
 * 页码变化处理
 * @param current 目标页码
 */
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  if (!isClientPaging.value) {
    loadDocuments()
  }
}

/**
 * 查看文档详情
 * @param row 文档行数据
 */
const viewDocument = (row: OaDocument) => {
  currentDocument.value = row
  detailVisible.value = true
}

/**
 * 打开新建文档对话框
 */
const openCreateDialog = () => {
  dialogTitle.value = '新建文档'
  resetForm()
  dialogVisible.value = true
}

/**
 * 打开编辑文档对话框并回显数据
 * @param row 待编辑的文档数据
 */
const editDocument = (row: OaDocument) => {
  dialogTitle.value = '编辑文档'
  Object.assign(documentForm, {
    id: row.id ?? 0,
    title: row.title,
    categoryId: row.categoryId,
    categoryName: row.categoryName ?? '',
    type: row.type || '制度文件',
    description: row.description ?? ''
  })
  dialogVisible.value = true
}

/**
 * 重置文档表单
 */
const resetForm = () => {
  Object.assign(documentForm, {
    id: 0,
    title: '',
    categoryId: undefined,
    categoryName: '',
    type: '制度文件',
    description: ''
  })
}

/**
 * 编辑表单中分类选择变化时同步带出分类名称
 * @param value 选中的分类ID
 */
const handleFormCategoryChange = (value: number) => {
  const target = categories.value.find(item => item.id === value)
  documentForm.categoryName = target?.name || ''
}

/**
 * 保存文档（根据表单id区分新增或编辑，creatorId/creatorName由后端自动填充）
 */
const saveDocument = async () => {
  if (!documentForm.title.trim()) {
    ElMessage.warning('请输入文档名称')
    return
  }
  saving.value = true
  try {
    const payload: OaDocument = {
      title: documentForm.title.trim(),
      categoryId: documentForm.categoryId,
      categoryName: documentForm.categoryName,
      type: documentForm.type,
      description: documentForm.description
    }
    if (documentForm.id) {
      await documentApi.update(documentForm.id, payload)
      ElMessage.success('文档更新成功')
    } else {
      await documentApi.create(payload)
      ElMessage.success('文档创建成功')
    }
    dialogVisible.value = false
    await loadDocuments()
  } catch (error: any) {
    ElMessage.error(getErrorMessage(error, '文档保存失败'))
  } finally {
    saving.value = false
  }
}

/**
 * 执行文档状态操作（发布/撤销/归档）
 * @param row 文档行数据
 * @param action 操作类型
 */
const doStatusAction = async (row: OaDocument, action: 'publish' | 'revoke' | 'archive') => {
  if (!row.id) return
  const actionText = action === 'publish' ? '发布' : action === 'revoke' ? '撤销' : '归档'
  try {
    await documentApi[action](row.id)
    ElMessage.success(`文档${actionText}成功`)
    await loadDocuments()
  } catch (error: any) {
    ElMessage.error(getErrorMessage(error, `文档${actionText}失败`))
  }
}

/**
 * 删除文档（先确认，失败时展示后端返回的错误信息）
 * @param row 待删除的文档数据
 */
const deleteDocument = async (row: OaDocument) => {
  if (!row.id) return
  try {
    await ElMessageBox.confirm(`确定要删除文档「${row.title}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    // 用户取消删除，直接返回
    return
  }
  try {
    await documentApi.delete(row.id)
    ElMessage.success('文档删除成功')
    await loadDocuments()
  } catch (error: any) {
    ElMessage.error(getErrorMessage(error, '文档删除失败'))
  }
}

// 页面挂载后加载分类选项与文档列表
onMounted(() => {
  loadCategories()
  loadDocuments()
})
</script>

<style scoped>
.document-list-view {
  padding: 20px;
  box-sizing: border-box;
}

.search-filter {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.header-actions {
  margin-left: auto;
}

.document-list {
  margin-bottom: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  padding: 10px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .document-list-view {
    padding: 12px;
  }

  .search-filter .filter-form {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .document-list {
    padding: 5px;
    overflow-x: auto;
  }
}
</style>
