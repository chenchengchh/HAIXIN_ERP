<template>
  <div class="customer-tag-view">
    <!-- 搜索和筛选区域 -->
    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标签名称">
          <el-input v-model="searchForm.tagName" placeholder="请输入标签名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 标签列表区域 -->
    <el-card shadow="hover" class="list-card">
      <template #header>
        <div class="card-header">
          <span>客户标签列表</span>
          <el-button type="primary" @click="handleAddTag">新增标签</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tagList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="tagName" label="标签名称" min-width="150" />
        <el-table-column prop="tagCategory" label="标签分类" min-width="150" />
        <el-table-column prop="tagType" label="标签类型" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.tagType === 'system' ? 'info' : 'success'">
              {{ scope.row.tagType === 'system' ? '系统标签' : '自定义标签' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditTag(scope.row)">编辑</el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDeleteTag(scope.row.id)"
              :disabled="scope.row.tagType === 'system'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
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

    <!-- 标签关联管理 -->
    <el-card shadow="hover" class="relation-card">
      <template #header>
        <div class="card-header">
          <span>标签关联管理</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form :inline="true" :model="relationForm" class="relation-form">
            <el-form-item label="客户ID">
              <el-input v-model.number="relationForm.customerId" placeholder="请输入客户ID" />
            </el-form-item>
            <el-form-item label="客户名称">
              <el-input v-model="relationForm.customerName" placeholder="请输入客户名称" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQueryCustomerTags">查询标签</el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 16px;">
        <el-col :span="12">
          <el-card shadow="hover" class="available-tags-card">
            <template #header>
              <span>可用标签</span>
            </template>
            <div class="tags-container">
              <el-tag
                v-for="tag in availableTags"
                :key="tag.id"
                size="medium"
                :type="'info'"
                @click="handleAddTagToCustomer(tag)"
                class="clickable-tag"
              >
                {{ tag.tagName }}
              </el-tag>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="customer-tags-card">
            <template #header>
              <span>客户已关联标签</span>
            </template>
            <div class="tags-container">
              <el-tag
                v-for="tag in customerTags"
                :key="tag.id"
                size="medium"
                :type="'success'"
                closable
                @close="handleRemoveTagFromCustomer(tag)"
              >
                {{ tag.tagName }}
              </el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 新增/编辑标签对话框 -->
    <el-dialog
      :model-value="dialogVisible"
      @update:model-value="handleDialogVisibleChange"
      :title="tag.id ? '编辑标签' : '新增标签'"
      width="500px"
      @close="handleClose"
    >
      <el-form :model="tag" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="标签名称" prop="tagName">
          <el-input v-model="tag.tagName" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="标签类型" prop="tagType">
          <el-select v-model="tag.tagType" placeholder="请选择标签类型">
            <el-option label="系统标签" value="system" />
            <el-option label="自定义标签" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签分类" prop="tagCategory">
          <el-input v-model="tag.tagCategory" placeholder="请输入标签分类" />
        </el-form-item>
        <el-form-item label="显示颜色" prop="color">
          <el-color-picker v-model="tag.color" show-alpha />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="tag.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" :loading="saveLoading" @click="handleSave">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { customerApi } from '../../../api/crm/customer'
import { unwrapListResponse } from '../../../api'

// 搜索表单
const searchForm = reactive({
  tagName: ''
})

// 标签列表数据（当前页）
const tagList = ref<any[]>([])
// 全部标签数据（后端返回数组，搜索与分页在前端处理）
const allTagList = ref<any[]>([])
const loading = ref(false)

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const tag = reactive<any>({
  id: undefined,
  tagName: '',
  tagType: 'custom',
  tagCategory: '',
  color: '#409EFF',
  sortOrder: 0
})

// 表单引用
const formRef = ref()

// 保存加载状态
const saveLoading = ref(false)

// 选中的标签
const selectedTags = ref<any[]>([])

// 标签关联表单
const relationForm = reactive({
  customerId: 0,
  customerName: ''
})

// 可用标签
const availableTags = ref<any[]>([])

// 客户已关联标签
const customerTags = ref<any[]>([])

// 表单验证规则
const rules = reactive({
  tagName: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
  tagCategory: [{ required: true, message: '请输入标签分类', trigger: 'blur' }]
})

// 初始化数据
onMounted(() => {
  fetchTagList()
  fetchAvailableTags()
})

/**
 * 根据搜索条件与分页参数刷新表格显示数据
 * 后端标签接口返回数组，搜索与分页在前端处理
 */
const refreshPagedTags = () => {
  let filtered = allTagList.value
  if (searchForm.tagName) {
    const keyword = searchForm.tagName.toLowerCase()
    filtered = filtered.filter(item => (item.tagName || '').toLowerCase().includes(keyword))
  }
  const start = (pagination.currentPage - 1) * pagination.pageSize
  tagList.value = filtered.slice(start, start + pagination.pageSize)
  pagination.total = filtered.length
}

/**
 * 获取标签列表
 * 调用后端 GET /api/v1/crm/tags，返回数组
 */
const fetchTagList = async () => {
  loading.value = true
  try {
    const response = await customerApi.getCustomerTags()
    allTagList.value = unwrapListResponse(response)
    refreshPagedTags()
  } catch (error) {
    console.error('获取标签列表失败:', error)
    ElMessage.error('获取标签列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 获取可用标签（用于标签关联管理区域）
 * 调用后端 GET /api/v1/crm/tags，返回数组
 */
const fetchAvailableTags = async () => {
  try {
    const response = await customerApi.getCustomerTags()
    availableTags.value = unwrapListResponse(response)
  } catch (error) {
    console.error('获取可用标签失败:', error)
    ElMessage.error('获取可用标签失败')
  }
}

/**
 * 查询客户已关联的标签
 * 调用后端 GET /customer/{id}/tags
 */
const handleQueryCustomerTags = async () => {
  if (!relationForm.customerId) {
    ElMessage.warning('请输入客户ID')
    return
  }

  try {
    const response = await customerApi.getCustomerTagRelations(relationForm.customerId)
    customerTags.value = unwrapListResponse(response)
    ElMessage.success('查询成功')
  } catch (error) {
    console.error('查询客户标签失败:', error)
    ElMessage.error('查询客户标签失败')
  }
}

/**
 * 搜索，重置页码并刷新列表
 */
const handleSearch = () => {
  pagination.currentPage = 1
  refreshPagedTags()
}

/**
 * 重置搜索条件并刷新列表
 */
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    ;(searchForm as any)[key] = ''
  })
  pagination.currentPage = 1
  refreshPagedTags()
}

/**
 * 分页大小变化
 * @param size 每页大小
 */
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  refreshPagedTags()
}

/**
 * 当前页变化
 * @param current 当前页码
 */
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  refreshPagedTags()
}

/**
 * 新增标签，重置表单并打开对话框
 */
const handleAddTag = () => {
  Object.assign(tag, {
    id: undefined,
    tagName: '',
    tagType: 'custom',
    tagCategory: '',
    color: '#409EFF',
    sortOrder: 0
  })
  dialogVisible.value = true
}

/**
 * 编辑标签，回填表单并打开对话框
 * @param row 标签行数据
 */
const handleEditTag = (row: any) => {
  Object.assign(tag, row)
  dialogVisible.value = true
}

/**
 * 删除标签（系统标签禁止删除）
 * @param id 标签ID
 */
const handleDeleteTag = (id: number) => {
  ElMessageBox.confirm('确定要删除该标签吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await customerApi.deleteTag(id)
      ElMessage.success('删除成功')
      fetchTagList()
      fetchAvailableTags()
    } catch (error) {
      console.error('删除标签失败:', error)
      ElMessage.error('删除标签失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

/**
 * 对话框可见性变化
 * @param newVisible 新的可见性状态
 */
const handleDialogVisibleChange = (newVisible: boolean) => {
  if (!newVisible) {
    handleClose()
  }
}

/**
 * 关闭对话框并重置表单校验状态
 */
const handleClose = () => {
  dialogVisible.value = false
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

/**
 * 保存标签（新增或编辑）
 * 新增调用 POST /tags，编辑调用 PUT /tags
 */
const handleSave = async () => {
  if (!formRef.value) return

  formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    saveLoading.value = true
    try {
      if (tag.id) {
        await customerApi.updateTag({ ...tag })
        ElMessage.success('更新成功')
      } else {
        // 新增时不传id，由后端生成
        await customerApi.createTag({
          tagName: tag.tagName,
          tagType: tag.tagType,
          tagCategory: tag.tagCategory,
          color: tag.color,
          sortOrder: tag.sortOrder
        })
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchTagList()
      fetchAvailableTags()
    } catch (error) {
      console.error('保存标签失败:', error)
      ElMessage.error('保存标签失败')
    } finally {
      saveLoading.value = false
    }
  })
}

/**
 * 选择标签变化
 * @param selection 选中的标签行
 */
const handleSelectionChange = (selection: any[]) => {
  selectedTags.value = selection
}

/**
 * 持久化当前客户的标签关联
 * 调用后端 PUT /customer/{id}/tags 全量保存
 */
const saveCustomerTagRelations = async () => {
  const tagIds = customerTags.value.map(t => t.id)
  await customerApi.setCustomerTags(relationForm.customerId, tagIds)
}

/**
 * 为客户添加标签并保存关联
 * @param tag 待添加的标签
 */
const handleAddTagToCustomer = async (tag: any) => {
  if (!relationForm.customerId) {
    ElMessage.warning('请先输入客户ID并查询')
    return
  }

  // 检查标签是否已存在
  const exists = customerTags.value.some(t => t.id === tag.id)
  if (exists) {
    ElMessage.warning('该标签已关联')
    return
  }

  customerTags.value.push(tag)
  try {
    await saveCustomerTagRelations()
    ElMessage.success('添加标签成功')
  } catch (error) {
    console.error('添加标签失败:', error)
    ElMessage.error('添加标签失败')
    // 保存失败时回滚本地状态
    customerTags.value = customerTags.value.filter(t => t.id !== tag.id)
  }
}

/**
 * 从客户移除标签并保存关联
 * @param tag 待移除的标签
 */
const handleRemoveTagFromCustomer = async (tag: any) => {
  if (!relationForm.customerId) {
    ElMessage.warning('请先输入客户ID并查询')
    return
  }

  const index = customerTags.value.findIndex(t => t.id === tag.id)
  if (index === -1) return
  customerTags.value.splice(index, 1)
  try {
    await saveCustomerTagRelations()
    ElMessage.success('移除标签成功')
  } catch (error) {
    console.error('移除标签失败:', error)
    ElMessage.error('移除标签失败')
    // 保存失败时回滚本地状态
    customerTags.value.splice(index, 0, tag)
  }
}
</script>

<style scoped>
.customer-tag-view {
  padding: 10px;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.list-card {
  margin-bottom: 16px;
}

.relation-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1.1rem;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.relation-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.available-tags-card,
.customer-tags-card {
  height: 200px;
  overflow-y: auto;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 8px;
}

.clickable-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.clickable-tag:hover {
  transform: scale(1.05);
}
</style>