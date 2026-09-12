<template>
  <div class="knowledge-view">
    <!-- 页面操作栏 -->
    <div class="page-actions">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索文章标题、关键词"
        style="width: 300px; margin-right: 10px;"
        clearable
      >
        <template #append>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon> 新建文章
      </el-button>
      <el-button @click="handleExport">
        <el-icon><Download /></el-icon> 导出
      </el-button>
    </div>

    <!-- 搜索条件展开面板 -->
    <el-collapse v-model="activeSearchPanel" style="margin-bottom: 20px;">
      <el-collapse-item title="高级搜索" name="1">
        <div class="advanced-search">
          <el-form :model="searchForm" inline>
            <el-form-item label="分类">
              <el-select v-model="searchForm.category" placeholder="选择分类" clearable>
                <el-option label="技术支持" value="tech_support" />
                <el-option label="产品使用" value="product_use" />
                <el-option label="常见问题" value="faq" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
                <el-option label="草稿" value="DRAFT" />
                <el-option label="已发布" value="PUBLISHED" />
                <el-option label="已下架" value="OFFLINE" />
              </el-select>
            </el-form-item>
            <el-form-item label="发布日期">
              <el-date-picker
                v-model="searchForm.publishDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-item>
    </el-collapse>

    <!-- 文章列表 -->
    <el-card shadow="never" class="articles-table-card">
      <el-table
        v-loading="loading"
        :data="articlesList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="title" label="标题" min-width="250" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="scope">
            {{ getCategoryLabel(scope.row.category) }}
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" sortable />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="success" @click="handlePublish(scope.row)">
              {{ scope.row.status === 'PUBLISHED' ? '下架' : '发布' }}
            </el-button>
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

    <!-- 新建/编辑文章对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isEditMode ? '编辑文章' : '新建文章'"
      width="700px"
      @close="handleEditDialogClose"
    >
      <el-form :model="articleForm" :rules="articleRules" ref="articleFormRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="articleForm.title" placeholder="请输入文章标题" maxlength="200" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="articleForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="技术支持" value="tech_support" />
            <el-option label="产品使用" value="product_use" />
            <el-option label="常见问题" value="faq" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="articleForm.tags" placeholder="多个标签用逗号分隔" maxlength="200" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="articleForm.content" type="textarea" :rows="8" placeholder="请输入文章内容" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="articleForm.author" placeholder="请输入作者名称" maxlength="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSaveArticle">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看文章对话框 -->
    <el-dialog v-model="viewDialogVisible" title="文章详情" width="700px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="标题">{{ viewArticle.title }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ getCategoryLabel(viewArticle.category) }}</el-descriptions-item>
        <el-descriptions-item label="标签">{{ viewArticle.tags || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(viewArticle.status)">{{ getStatusLabel(viewArticle.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="作者">{{ viewArticle.author || '-' }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div style="white-space: pre-wrap;">{{ viewArticle.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { serviceApi } from '../../../api/crm/service'
import { unwrapPageResponse } from '../../../api'

// 文章列表数据
const articlesList = ref<any[]>([])

// 加载状态
const loading = ref(false)

// 提交加载状态
const submitLoading = ref(false)

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  category: '',
  status: '',
  publishDateRange: [] as string[]
})

// 搜索面板展开状态
const activeSearchPanel = ref<string[]>([])

// 编辑对话框状态
const editDialogVisible = ref(false)
const isEditMode = ref(false)
const articleFormRef = ref()
const articleForm = reactive<any>({
  id: undefined,
  title: '',
  category: '',
  tags: '',
  content: '',
  author: ''
})

// 查看对话框状态
const viewDialogVisible = ref(false)
const viewArticle = ref<any>({})

// 表单校验规则
const articleRules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

/**
 * 获取分类标签
 * @param category 分类编码
 * @returns 分类中文标签
 */
const getCategoryLabel = (category: string) => {
  const labelMap: Record<string, string> = {
    tech_support: '技术支持',
    product_use: '产品使用',
    faq: '常见问题'
  }
  return labelMap[category] || category
}

/**
 * 获取状态标签（后端状态为大写：DRAFT/PUBLISHED/OFFLINE）
 * @param status 状态编码
 * @returns 状态中文标签
 */
const getStatusLabel = (status: string) => {
  const labelMap: Record<string, string> = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    OFFLINE: '已下架'
  }
  return labelMap[status] || status
}

/**
 * 获取状态标签样式类型
 * @param status 状态编码
 * @returns 标签类型
 */
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    DRAFT: 'warning',
    PUBLISHED: 'success',
    OFFLINE: 'info'
  }
  return typeMap[status] || ''
}

/**
 * 获取文章列表（对接后端分页接口 GET /api/v1/crm/knowledge/list）
 */
const fetchArticlesList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    if (searchForm.category) {
      params.category = searchForm.category
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    const response = await serviceApi.getKnowledgeList(params)
    const { list, total } = unwrapPageResponse<any>(response)
    articlesList.value = list
    pagination.total = total
  } catch (error) {
    console.error('获取文章列表失败:', error)
    ElMessage.error('获取文章列表失败')
    articlesList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

/**
 * 处理搜索：重置页码并重新加载列表
 */
const handleSearch = () => {
  pagination.currentPage = 1
  fetchArticlesList()
}

/**
 * 处理重置：清空搜索条件并重新加载列表
 */
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    category: '',
    status: '',
    publishDateRange: []
  })
  pagination.currentPage = 1
  fetchArticlesList()
}

/**
 * 处理新建：打开新建文章对话框并重置表单
 */
const handleCreate = () => {
  isEditMode.value = false
  Object.assign(articleForm, {
    id: undefined,
    title: '',
    category: '',
    tags: '',
    content: '',
    author: ''
  })
  editDialogVisible.value = true
}

/**
 * 处理编辑：填充表单并打开编辑对话框
 * @param row 文章行数据
 */
const handleEdit = (row: any) => {
  isEditMode.value = true
  Object.assign(articleForm, {
    id: row.id,
    title: row.title,
    category: row.category,
    tags: row.tags,
    content: row.content,
    author: row.author
  })
  editDialogVisible.value = true
}

/**
 * 处理编辑对话框关闭：重置表单校验状态
 */
const handleEditDialogClose = () => {
  if (articleFormRef.value) {
    articleFormRef.value.resetFields()
  }
}

/**
 * 保存文章：新增走 POST /api/v1/crm/knowledge，编辑走 PUT /api/v1/crm/knowledge/{id}
 */
const handleSaveArticle = async () => {
  if (!articleFormRef.value) return
  await articleFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEditMode.value && articleForm.id) {
        await serviceApi.updateKnowledgeArticle(articleForm.id, articleForm)
        ElMessage.success('文章更新成功')
      } else {
        await serviceApi.createKnowledgeArticle(articleForm)
        ElMessage.success('文章创建成功')
      }
      editDialogVisible.value = false
      fetchArticlesList()
    } catch (error) {
      console.error('保存文章失败:', error)
      ElMessage.error('保存文章失败')
    } finally {
      submitLoading.value = false
    }
  })
}

/**
 * 处理发布/下架：根据当前状态调用发布或下架接口
 * @param row 文章行数据
 */
const handlePublish = async (row: any) => {
  const isPublished = row.status === 'PUBLISHED'
  const actionText = isPublished ? '下架' : '发布'
  try {
    await ElMessageBox.confirm(`确定要${actionText}该文章吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    if (isPublished) {
      await serviceApi.offlineKnowledgeArticle(row.id)
    } else {
      await serviceApi.publishKnowledgeArticle(row.id)
    }
    ElMessage.success(`文章${actionText}成功`)
    fetchArticlesList()
  } catch (error) {
    console.error(`文章${actionText}失败:`, error)
    ElMessage.error(`文章${actionText}失败`)
  }
}

/**
 * 处理导出
 */
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

/**
 * 处理查看：打开文章详情对话框
 * @param row 文章行数据
 */
const handleView = (row: any) => {
  viewArticle.value = row
  viewDialogVisible.value = true
}

/**
 * 处理删除：逻辑删除文章
 * @param row 文章行数据
 */
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该文章吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await serviceApi.deleteKnowledgeArticle(row.id)
      ElMessage.success('删除成功')
      fetchArticlesList()
    } catch (error) {
      console.error('删除文章失败:', error)
      ElMessage.error('删除文章失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

/**
 * 处理分页大小变化
 * @param size 每页数量
 */
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchArticlesList()
}

/**
 * 处理页码变化
 * @param current 当前页码
 */
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchArticlesList()
}

/**
 * 处理选择变化
 * @param selection 选中的行
 */
const handleSelectionChange = (selection: any[]) => {
  console.log('选中的行:', selection)
}

// 组件挂载时初始化
onMounted(() => {
  fetchArticlesList()
})
</script>

<style scoped>
.knowledge-view {
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

.advanced-search {
  background-color: #ffffff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.articles-table-card {
  margin-top: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>