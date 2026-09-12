<template>
  <div class="customer-tags-view">
    <!-- 标签管理区域 -->
    <el-card shadow="hover" class="tags-management-card">
      <template #header>
        <div class="card-header">
          <span>标签管理</span>
          <el-button type="primary" @click="handleAddTag">新增标签</el-button>
        </div>
      </template>
      
      <!-- 标签列表 -->
      <el-table
        v-loading="loading"
        :data="tagsList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="标签ID" width="80" />
        <el-table-column prop="tagName" label="标签名称" width="150" />
        <el-table-column prop="tagType" label="标签类型" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.tagType === 'system' ? 'info' : 'success'">
              {{ scope.row.tagType === 'system' ? '系统标签' : '自定义标签' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tagCategory" label="标签分类" width="120" />
        <el-table-column prop="color" label="显示颜色" width="100">
          <template #default="scope">
            <div class="color-preview" :style="{ backgroundColor: scope.row.color }"></div>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
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

    <!-- 标签分配区域 -->
    <el-card shadow="hover" class="tag-assignment-card">
      <template #header>
        <div class="card-header">
          <span>标签分配</span>
        </div>
      </template>
      
      <div class="tag-assignment-content">
        <div class="customer-selector">
          <el-form :inline="true" class="customer-select-form">
            <el-form-item label="选择客户">
              <el-select v-model="selectedCustomerId" placeholder="请选择客户" clearable filterable>
                <el-option
                  v-for="customer in customersList"
                  :key="customer.id"
                  :label="customer.customerName"
                  :value="customer.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSelectCustomer">查询</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <div v-if="selectedCustomer" class="tags-assignment-area">
          <h4>{{ selectedCustomer.customerName }} - 标签分配</h4>
          
          <div class="tags-container">
            <div class="available-tags">
              <h5>可用标签</h5>
              <div class="tags-list">
                <el-tag
                  v-for="tag in availableTags"
                  :key="tag.id"
                  size="small"
                  :type="'info'"
                  @click="handleAddTagToCustomer(tag)"
                >
                  {{ tag.tagName }}
                </el-tag>
              </div>
            </div>
            
            <div class="customer-tags">
              <h5>已分配标签</h5>
              <div class="tags-list">
                <el-tag
                  v-for="tag in customerTags"
                  :key="tag.id"
                  size="small"
                  :type="'success'"
                  closable
                  @close="handleRemoveTagFromCustomer(tag)"
                >
                  {{ tag.tagName }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 新增/编辑标签对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditMode ? '编辑标签' : '新增标签'"
      width="500px"
    >
      <el-form :model="tagForm" :rules="tagFormRules" ref="tagFormRef" label-width="100px">
        <el-form-item label="标签名称" prop="tagName">
          <el-input v-model="tagForm.tagName" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="标签类型" prop="tagType">
          <el-select v-model="tagForm.tagType" placeholder="请选择标签类型">
            <el-option label="系统标签" value="system" />
            <el-option label="自定义标签" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签分类" prop="tagCategory">
          <el-input v-model="tagForm.tagCategory" placeholder="请输入标签分类" />
        </el-form-item>
        <el-form-item label="显示颜色" prop="color">
          <el-color-picker v-model="tagForm.color" show-alpha />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="tagForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saveLoading" @click="handleSaveTag">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { CustomerTagEntity } from '../../../types/crm/customer'
import type { CustomerEntity } from '../../../types/crm/customer'
import { customerApi } from '../../../api/crm/customer'
import { unwrapPageResponse, unwrapListResponse } from '../../../api'

// 表单引用
const tagFormRef = ref()

// 标签列表数据（当前页）
const tagsList = ref<CustomerTagEntity[]>([])
// 全部标签数据（后端返回数组，前端分页）
const allTagsList = ref<CustomerTagEntity[]>([])
const loading = ref(false)

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const isEditMode = ref(false)
// 保存加载状态
const saveLoading = ref(false)

// 标签表单
const tagForm = reactive({
  id: 0,
  tagName: '',
  tagType: 'custom',
  tagCategory: '',
  color: '#409EFF',
  sortOrder: 0
})

// 标签表单验证规则
const tagFormRules = reactive({
  tagName: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
  tagCategory: [{ required: true, message: '请输入标签分类', trigger: 'blur' }]
})

// 选中的标签
const selectedTags = ref<CustomerTagEntity[]>([])

// 客户相关数据
const customersList = ref<CustomerEntity[]>([])
const selectedCustomerId = ref<number>()
const selectedCustomer = ref<Partial<CustomerEntity>>({})

// 可用标签和已分配标签
const availableTags = computed(() => {
  return allTagsList.value.filter(tag => {
    return !customerTags.value.some(customerTag => customerTag.id === tag.id)
  })
})

const customerTags = ref<CustomerTagEntity[]>([])

// 初始化数据
onMounted(() => {
  fetchTagsList()
  fetchCustomersList()
})

/**
 * 根据当前分页参数刷新表格显示数据
 * 后端标签接口返回数组，分页在前端处理
 */
const refreshPagedTags = () => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  tagsList.value = allTagsList.value.slice(start, start + pagination.pageSize)
  pagination.total = allTagsList.value.length
}

/**
 * 获取标签列表
 * 调用后端 GET /api/v1/crm/tags，返回数组
 */
const fetchTagsList = async () => {
  loading.value = true
  try {
    const response = await customerApi.getCustomerTags()
    allTagsList.value = unwrapListResponse<CustomerTagEntity>(response)
    refreshPagedTags()
  } catch (error) {
    console.error('获取标签列表失败:', error)
    ElMessage.error('获取标签列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 获取客户列表（用于标签分配的客户下拉）
 * 调用后端分页接口，取前1000条
 */
const fetchCustomersList = async () => {
  try {
    const response = await customerApi.getCustomerList({ page: 1, size: 1000 })
    const { list } = unwrapPageResponse<CustomerEntity>(response)
    customersList.value = list
  } catch (error) {
    console.error('获取客户列表失败:', error)
    ElMessage.error('获取客户列表失败')
  }
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
 * 选中标签变化
 * @param selection 选中的标签行
 */
const handleSelectionChange = (selection: CustomerTagEntity[]) => {
  selectedTags.value = selection
}

/**
 * 新增标签，重置表单并打开对话框
 */
const handleAddTag = () => {
  isEditMode.value = false
  Object.assign(tagForm, {
    id: 0,
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
 * @param tag 标签行数据
 */
const handleEditTag = (tag: CustomerTagEntity) => {
  isEditMode.value = true
  Object.assign(tagForm, { ...tag })
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
      fetchTagsList()
    } catch (error) {
      console.error('删除标签失败:', error)
      ElMessage.error('删除标签失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

/**
 * 保存标签（新增或编辑）
 * 新增调用 POST /tags，编辑调用 PUT /tags
 */
const handleSaveTag = () => {
  tagFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    saveLoading.value = true
    try {
      if (isEditMode.value) {
        await customerApi.updateTag({ ...tagForm })
        ElMessage.success('更新成功')
      } else {
        // 新增时不传id，由后端生成
        await customerApi.createTag({
          tagName: tagForm.tagName,
          tagType: tagForm.tagType,
          tagCategory: tagForm.tagCategory,
          color: tagForm.color,
          sortOrder: tagForm.sortOrder
        })
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchTagsList()
    } catch (error) {
      console.error('保存标签失败:', error)
      ElMessage.error('保存标签失败')
    } finally {
      saveLoading.value = false
    }
  })
}

/**
 * 选择客户并查询该客户已关联的标签
 * 调用后端 GET /customer/{id}/tags
 */
const handleSelectCustomer = async () => {
  if (!selectedCustomerId.value) {
    ElMessage.warning('请先选择客户')
    return
  }
  try {
    selectedCustomer.value = customersList.value.find(c => c.id === selectedCustomerId.value) || {}
    const response = await customerApi.getCustomerTagRelations(selectedCustomerId.value)
    customerTags.value = unwrapListResponse<CustomerTagEntity>(response)
  } catch (error) {
    console.error('查询客户标签失败:', error)
    ElMessage.error('查询客户标签失败')
  }
}

/**
 * 持久化当前客户的标签分配
 * 调用后端 PUT /customer/{id}/tags 全量保存
 */
const saveCustomerTagRelations = async () => {
  if (!selectedCustomerId.value) return
  try {
    const tagIds = customerTags.value.map(tag => tag.id)
    await customerApi.setCustomerTags(selectedCustomerId.value, tagIds)
  } catch (error) {
    console.error('保存标签分配失败:', error)
    ElMessage.error('保存标签分配失败')
    throw error
  }
}

/**
 * 为客户添加标签并保存分配
 * @param tag 待添加的标签
 */
const handleAddTagToCustomer = async (tag: CustomerTagEntity) => {
  if (!selectedCustomerId.value) {
    ElMessage.warning('请先选择客户')
    return
  }
  customerTags.value.push(tag)
  try {
    await saveCustomerTagRelations()
    ElMessage.success('标签添加成功')
  } catch {
    // 保存失败时回滚本地状态
    customerTags.value = customerTags.value.filter(t => t.id !== tag.id)
  }
}

/**
 * 从客户移除标签并保存分配
 * @param tag 待移除的标签
 */
const handleRemoveTagFromCustomer = async (tag: CustomerTagEntity) => {
  if (!selectedCustomerId.value) {
    ElMessage.warning('请先选择客户')
    return
  }
  const index = customerTags.value.findIndex(t => t.id === tag.id)
  if (index === -1) return
  customerTags.value.splice(index, 1)
  try {
    await saveCustomerTagRelations()
    ElMessage.success('标签移除成功')
  } catch {
    // 保存失败时回滚本地状态
    customerTags.value.splice(index, 0, tag)
  }
}
</script>

<style scoped>
.customer-tags-view {
  padding: 10px;
}

.tags-management-card,
.tag-assignment-card {
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

.color-preview {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

/* 标签分配区域样式 */
.tag-assignment-content {
  padding: 10px 0;
}

.customer-selector {
  margin-bottom: 20px;
}

.customer-select-form {
  display: flex;
  align-items: center;
}

.tags-assignment-area {
  margin-top: 20px;
}

.tags-container {
  display: flex;
  gap: 20px;
  margin-top: 16px;
}

.available-tags,
.customer-tags {
  flex: 1;
  background-color: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
}

.available-tags h5,
.customer-tags h5 {
  margin-top: 0;
  margin-bottom: 12px;
  font-size: 1rem;
  font-weight: bold;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .tags-container {
    flex-direction: column;
  }
  
  .available-tags,
  .customer-tags {
    margin-bottom: 16px;
  }
}
</style>
