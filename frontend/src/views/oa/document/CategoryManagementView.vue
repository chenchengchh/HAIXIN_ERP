<template>
  <div class="category-management-view">
    <div class="page-header">
      <div class="header-actions">
        <el-button type="primary" @click="openCategoryDialog">
          <el-icon><Plus /></el-icon>
          新建分类
        </el-button>
      </div>
    </div>

    <!-- 分类树和列表 -->
    <div class="category-content" v-loading="loading">
      <div class="category-tree">
        <h3>分类树</h3>
        <el-tree
          :data="categoryTree"
          :props="treeProps"
          :expand-on-click-node="false"
          @node-click="handleNodeClick"
          default-expand-all
          ref="categoryTreeRef"
        >
          <template #default="{ node, data }">
            <span class="custom-tree-node">
              <span>{{ node.label }}</span>
              <span>
                <el-button
                  link
                  size="small"
                  @click="() => appendCategory(data)"
                >
                  <el-icon><Plus /></el-icon>
                </el-button>
                <el-button
                  link
                  size="small"
                  @click="() => editCategory(data)"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button
                  link
                  size="small"
                  @click="() => deleteCategory(data)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </span>
            </span>
          </template>
        </el-tree>
      </div>

      <div class="category-list">
        <h3>分类列表</h3>
        <el-table :data="categories" stripe style="width: 100%">
          <el-table-column prop="id" label="分类ID" width="80" align="center" />
          <el-table-column prop="name" label="分类名称" min-width="150" />
          <el-table-column label="父分类" width="120" align="center">
            <template #default="scope">
              {{ scope.row.parentName || (scope.row.parentId ? '-' : '顶级分类') }}
            </template>
          </el-table-column>
          <el-table-column prop="level" label="层级" width="70" align="center">
            <template #default="scope">
              {{ scope.row.level ?? 1 }}
            </template>
          </el-table-column>
          <el-table-column prop="sort" label="排序" width="70" align="center" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                {{ scope.row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160" align="center">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center">
            <template #default="scope">
              <el-button type="primary" size="small" @click="editCategory(scope.row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="deleteCategory(scope.row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="resetForm"
    >
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="分类名称" required>
          <el-input v-model="categoryForm.name" placeholder="输入分类名称" />
        </el-form-item>
        <el-form-item label="父分类">
          <el-select v-model="categoryForm.parentId" placeholder="选择父分类" style="width: 100%">
            <el-option label="无（顶级分类）" :value="0" />
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
              :disabled="category.id === categoryForm.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分类描述">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            :rows="2"
            placeholder="输入分类描述（可选）"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sort" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="categoryForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveCategory">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { documentCategoryApi } from '@/api/oa'
import type { DocumentCategory } from '@/api/oa'
import { unwrapListResponse } from '@/api'

// 分类树节点类型（在分类数据基础上扩展子节点集合）
interface CategoryTreeNode extends DocumentCategory {
  children?: CategoryTreeNode[]
}

// 分类树配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 分类平铺列表数据
const categories = ref<DocumentCategory[]>([])
// 列表加载状态
const loading = ref(false)
// 保存按钮加载状态
const saving = ref(false)

// 分类对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新建分类')
const categoryForm = reactive({
  id: 0,
  name: '',
  parentId: 0,
  description: '',
  sort: 0,
  status: 1
})

// 分类树引用
const categoryTreeRef = ref()

/**
 * 从后端加载分类列表
 */
const loadCategories = async () => {
  loading.value = true
  try {
    const response = await documentCategoryApi.getList()
    categories.value = unwrapListResponse<DocumentCategory>(response)
  } catch (error: any) {
    ElMessage.error(getErrorMessage(error, '获取分类列表失败'))
  } finally {
    loading.value = false
  }
}

/**
 * 将平铺分类列表按parentId组装为树形结构，并按sort排序
 */
const categoryTree = computed<CategoryTreeNode[]>(() => {
  const nodes: CategoryTreeNode[] = categories.value.map(item => ({ ...item, children: [] }))
  const nodeMap = new Map<number, CategoryTreeNode>()
  nodes.forEach(node => {
    if (node.id !== undefined) {
      nodeMap.set(node.id, node)
    }
  })
  const tree: CategoryTreeNode[] = []
  nodes.forEach(node => {
    if (node.parentId && nodeMap.has(node.parentId)) {
      nodeMap.get(node.parentId)!.children!.push(node)
    } else {
      tree.push(node)
    }
  })
  // 递归按排序字段排序，保证树形展示顺序与后台配置一致
  const sortNodes = (list: CategoryTreeNode[]) => {
    list.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
    list.forEach(item => {
      if (item.children && item.children.length > 0) {
        sortNodes(item.children)
      }
    })
  }
  sortNodes(tree)
  return tree
})

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
 * 打开新建分类对话框
 */
const openCategoryDialog = () => {
  dialogTitle.value = '新建分类'
  resetForm()
  dialogVisible.value = true
}

/**
 * 重置分类表单
 */
const resetForm = () => {
  Object.assign(categoryForm, {
    id: 0,
    name: '',
    parentId: 0,
    description: '',
    sort: 0,
    status: 1
  })
}

/**
 * 分类树节点点击事件
 * @param data 被点击的分类节点数据
 */
const handleNodeClick = (data: DocumentCategory) => {
  console.log('点击节点:', data)
}

/**
 * 在指定分类下追加子分类
 * @param data 父分类节点数据
 */
const appendCategory = (data: DocumentCategory) => {
  dialogTitle.value = '追加子分类'
  Object.assign(categoryForm, {
    id: 0,
    name: '',
    parentId: data.id ?? 0,
    description: '',
    sort: 0,
    status: 1
  })
  dialogVisible.value = true
}

/**
 * 打开编辑分类对话框并回显数据
 * @param data 待编辑的分类数据
 */
const editCategory = (data: DocumentCategory) => {
  dialogTitle.value = '编辑分类'
  Object.assign(categoryForm, {
    id: data.id ?? 0,
    name: data.name,
    parentId: data.parentId ?? 0,
    description: data.description ?? '',
    sort: data.sort ?? 0,
    status: data.status ?? 1
  })
  dialogVisible.value = true
}

/**
 * 删除分类（先确认，失败时展示后端返回的错误信息）
 * @param data 待删除的分类数据
 */
const deleteCategory = async (data: DocumentCategory) => {
  if (!data.id) return
  try {
    await ElMessageBox.confirm(`确定要删除分类「${data.name}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    // 用户取消删除，直接返回
    return
  }
  try {
    await documentCategoryApi.delete(data.id)
    ElMessage.success('分类删除成功')
    await loadCategories()
  } catch (error: any) {
    ElMessage.error(getErrorMessage(error, '分类删除失败'))
  }
}

/**
 * 保存分类（根据表单id区分新增或编辑）
 */
const saveCategory = async () => {
  if (!categoryForm.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  saving.value = true
  try {
    // 组装提交参数，level与creatorId由后端自动推导填充
    const payload: DocumentCategory = {
      name: categoryForm.name.trim(),
      parentId: categoryForm.parentId || 0,
      description: categoryForm.description,
      sort: categoryForm.sort,
      status: categoryForm.status
    }
    if (categoryForm.id) {
      await documentCategoryApi.update(categoryForm.id, payload)
      ElMessage.success('分类更新成功')
    } else {
      await documentCategoryApi.create(payload)
      ElMessage.success('分类创建成功')
    }
    dialogVisible.value = false
    await loadCategories()
  } catch (error: any) {
    ElMessage.error(getErrorMessage(error, '分类保存失败'))
  } finally {
    saving.value = false
  }
}

// 页面挂载后加载分类数据
onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-management-view {
  padding: 20px;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.category-content {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.category-tree h3,
.category-list h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.custom-tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .category-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .category-management-view {
    padding: 12px;
  }

  .category-content {
    padding: 12px;
  }
}
</style>
