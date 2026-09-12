<template>
  <div class="customer-categories-view">
    <el-card shadow="hover" class="categories-card">
      <template #header>
        <div class="card-header">
          <span>客户分类管理</span>
          <el-button type="primary" @click="handleAddCategory">新增分类</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索分类名称、编码"
          style="width: 300px; margin-right: 10px;"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <!-- 分类列表 -->
      <el-table
        v-loading="loading"
        :data="categoriesList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="分类ID" width="80" />
        <el-table-column prop="categoryName" label="分类名称" width="150" />
        <el-table-column prop="categoryCode" label="分类编码" width="150" />
        <el-table-column prop="parentId" label="父分类ID" width="100">
          <template #default="scope">
            {{ scope.row.parentId === 0 ? '-' : scope.row.parentId }}
          </template>
        </el-table-column>
        <el-table-column label="父分类名称" width="150">
          <template #default="scope">
            {{ getParentName(scope.row.parentId) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="分类描述" min-width="200" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditCategory(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteCategory(scope.row)">删除</el-button>
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

    <!-- 分类树状图 -->
    <el-card shadow="hover" class="category-tree-card">
      <template #header>
        <div class="card-header">
          <span>分类树状图</span>
        </div>
      </template>

      <div class="category-tree-container">
        <el-tree
          :data="categoriesTree"
          :props="treeProps"
          :expand-on-click-node="false"
          :default-expand-all="true"
          @node-click="handleNodeClick"
        >
          <template #default="{ data }">
            <span class="tree-node-content">
              <span>{{ data.categoryName }}</span>
              <span class="node-meta">({{ data.categoryCode }})</span>
            </span>
          </template>
        </el-tree>
        <el-empty v-if="!categoriesTree.length" description="暂无分类数据" />
      </div>
    </el-card>

    <!-- 新增/编辑分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditMode ? '编辑分类' : '新增分类'"
      width="500px"
    >
      <el-form :model="categoryForm" :rules="categoryFormRules" ref="categoryFormRef" label-width="100px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="categoryForm.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="categoryForm.categoryCode" placeholder="请输入分类编码" :disabled="isEditMode" />
        </el-form-item>
        <el-form-item label="父分类" prop="parentId">
          <el-select v-model="categoryForm.parentId" placeholder="请选择父分类（默认为根分类）" clearable>
            <el-option
              v-for="category in parentCategories"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分类描述" prop="description">
          <el-input v-model="categoryForm.description" type="textarea" placeholder="请输入分类描述" :rows="3" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="categoryForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSaveCategory">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { customerApi } from '@/api/crm/customer'
import { unwrapPageResponse } from '@/api'

// 表单引用
const categoryFormRef = ref()

// 分类列表数据
const categoriesList = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)

// 搜索关键词
const searchKeyword = ref('')

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const isEditMode = ref(false)

// 分类表单
const categoryForm = reactive({
  id: 0,
  categoryName: '',
  categoryCode: '',
  parentId: 0,
  description: '',
  sortOrder: 0,
  status: 1
})

// 分类表单验证规则
const categoryFormRules = reactive({
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  categoryCode: [{ required: true, message: '请输入分类编码', trigger: 'blur' }]
})

// 选中的分类
const selectedCategories = ref<any[]>([])

// 树状图配置
const treeProps = {
  label: 'categoryName',
  children: 'children'
}

/**
 * 父分类列表（仅根分类可作为父分类，且排除当前编辑的自身）
 */
const parentCategories = computed(() => {
  return categoriesList.value.filter(category =>
    category.parentId === 0 && category.id !== categoryForm.id
  )
})

/**
 * 分类树状数据：基于当前列表构建父子层级
 */
const categoriesTree = computed(() => {
  const tree: any[] = []
  const map: Record<number, any> = {}

  categoriesList.value.forEach(category => {
    map[category.id] = { ...category, children: [] }
  })

  categoriesList.value.forEach(category => {
    if (category.parentId === 0) {
      tree.push(map[category.id])
    } else if (map[category.parentId]) {
      map[category.parentId].children.push(map[category.id])
    }
  })

  return tree
})

/**
 * 根据父分类ID获取父分类名称
 * @param parentId 父分类ID
 * @returns 父分类名称，根分类显示"-"
 */
const getParentName = (parentId: number) => {
  if (parentId === 0) return '-'
  const parent = categoriesList.value.find(c => c.id === parentId)
  return parent ? parent.categoryName : `分类${parentId}`
}

// 初始化数据
onMounted(() => {
  fetchCategoriesList()
})

/**
 * 分页查询分类列表
 */
const fetchCategoriesList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const response = await customerApi.getCategoryList(params)
    const { list, total } = unwrapPageResponse<any>(response)
    categoriesList.value = list
    pagination.total = total
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索：重置页码并重新查询
 */
const handleSearch = () => {
  pagination.currentPage = 1
  fetchCategoriesList()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchCategoriesList()
}

// 当前页变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchCategoriesList()
}

// 选中分类变化
const handleSelectionChange = (selection: any[]) => {
  selectedCategories.value = selection
}

/**
 * 打开新增分类对话框
 */
const handleAddCategory = () => {
  isEditMode.value = false
  Object.assign(categoryForm, {
    id: 0,
    categoryName: '',
    categoryCode: '',
    parentId: 0,
    description: '',
    sortOrder: 0,
    status: 1
  })
  dialogVisible.value = true
}

/**
 * 打开编辑分类对话框
 * @param category 分类行数据
 */
const handleEditCategory = (category: any) => {
  isEditMode.value = true
  Object.assign(categoryForm, {
    id: category.id,
    categoryName: category.categoryName,
    categoryCode: category.categoryCode,
    parentId: category.parentId,
    description: category.description,
    sortOrder: category.sortOrder,
    status: category.status
  })
  dialogVisible.value = true
}

/**
 * 删除分类（后端校验：存在子分类时拒绝删除）
 * @param row 分类行数据
 */
const handleDeleteCategory = (row: any) => {
  ElMessageBox.confirm(`确定要删除分类「${row.categoryName}」吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await customerApi.deleteCategory(row.id)
      ElMessage.success('删除成功')
      fetchCategoriesList()
    } catch (error: any) {
      const msg = error?.response?.data?.msg || '删除失败'
      ElMessage.error(msg)
    }
  }).catch(() => {
    // 取消删除
  })
}

/**
 * 保存分类（新增或编辑）
 */
const handleSaveCategory = () => {
  categoryFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    saving.value = true
    try {
      const data = {
        categoryName: categoryForm.categoryName,
        categoryCode: categoryForm.categoryCode,
        parentId: categoryForm.parentId || 0,
        description: categoryForm.description,
        sortOrder: categoryForm.sortOrder,
        status: categoryForm.status
      }
      if (isEditMode.value) {
        await customerApi.updateCategory(categoryForm.id, data)
      } else {
        await customerApi.createCategory(data)
      }
      ElMessage.success(isEditMode.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchCategoriesList()
    } catch (error) {
      console.error('保存分类失败:', error)
      ElMessage.error('保存分类失败')
    } finally {
      saving.value = false
    }
  })
}

/**
 * 分类状态开关变更，调用更新接口
 * @param category 分类行数据
 */
const handleStatusChange = async (category: any) => {
  try {
    await customerApi.updateCategory(category.id, { status: category.status })
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
    // 失败时回滚开关状态
    category.status = category.status === 1 ? 0 : 1
  }
}

// 节点点击事件
const handleNodeClick = (data: any) => {
  console.log('节点点击:', data)
}
</script>

<style scoped>
.customer-categories-view {
  padding: 10px;
}

.categories-card,
.category-tree-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1.1rem;
}

.search-bar {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 分类树状图样式 */
.category-tree-container {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  min-height: 300px;
}

.tree-node-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-meta {
  font-size: 0.8rem;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .category-tree-container {
    padding: 10px;
  }
}
</style>
