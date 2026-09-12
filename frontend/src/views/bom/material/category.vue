<template>
  <div class="category-maintenance">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h3>物料分类维护</h3>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增分类
        </el-button>
        <el-button @click="refreshTree">
          <el-icon><RefreshRight /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 分类管理卡片 -->
    <el-card class="category-card card-glossy">
      <div class="category-container">
        <!-- 分类树 -->
        <div class="category-tree">
          <el-tree
            ref="treeRef"
            :data="categoryTree"
            :props="treeProps"
            :expand-on-click-node="false"
            @node-click="handleNodeClick"
            @node-contextmenu="handleContextMenu"
          >
            <template #default="{ node, data }">
              <span class="category-node">
                <el-icon><Folder /></el-icon>
                <span>{{ data.name }}</span>
              </span>
            </template>
          </el-tree>
        </div>

        <!-- 分类详情 -->
        <div class="category-detail">
          <!-- 显示表单的条件：有selectedCategory或者处于新增模式 -->
          <div v-if="selectedCategory || isAddMode">
            <el-form
              ref="formRef"
              :model="formData"
              :rules="formRules"
              label-position="top"
            >
              <div class="section-title">
                <span>分类基本信息</span>
                <div class="title-line"></div>
              </div>
              <el-row :gutter="32">
                <el-col :span="24">
                  <el-form-item label="父分类" prop="parentId">
                    <el-cascader
                      v-model="formData.parentId"
                      :options="categoryOptions"
                      :props="cascaderProps"
                      placeholder="选择父分类"
                      clearable
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="分类名称" prop="name">
                    <el-input v-model="formData.name" placeholder="输入分类名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="分类编码" prop="code">
                    <el-input v-model="formData.code" placeholder="输入分类编码" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="分类描述">
                    <el-input
                      v-model="formData.description"
                      type="textarea"
                      :rows="3"
                      placeholder="输入分类描述"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="分类状态" prop="status">
                    <el-radio-group v-model="formData.status" class="status-radio">
                      <el-radio-button :value="1">启用</el-radio-button>
                      <el-radio-button :value="0">停用</el-radio-button>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <!-- 操作按钮 -->
              <div class="form-actions">
                <el-button @click="handleCancel">取消</el-button>
                <el-button type="primary" @click="handleSave" :loading="submitting">保存</el-button>
              </div>
            </el-form>
          </div>
          <div v-else class="empty-state">
            <el-icon class="empty-icon"><FolderOpened /></el-icon>
            <p>请选择一个分类查看详情</p>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 自定义右键菜单 -->
    <div 
      ref="contextMenuRef" 
      :style="contextMenuStyle" 
      v-show="showContextMenu"
      class="custom-context-menu"
    >
      <div class="menu-item" @click="handleEditCategory">编辑分类</div>
      <div class="menu-divider"></div>
      <div class="menu-item" @click="handleDeleteCategory">删除分类</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, type CSSProperties } from 'vue'
import { Plus, RefreshRight, Folder, FolderOpened, Delete, Edit } from '@element-plus/icons-vue'
import type { FormInstance, CascaderOption } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { unwrapListResponse, unwrapResponseData } from '@/api'
import { bomApi } from '@/api/bom'

// 表单引用
const formRef = ref<FormInstance>()

// 提交状态
const submitting = ref(false)

// 页面加载状态
const loading = ref(false)

// 分类树引用
const treeRef = ref()

// 右键菜单引用
const contextMenuRef = ref()
const showContextMenu = ref(false)
const contextMenuStyle = ref<CSSProperties>({ left: '0px', top: '0px' })
const selectedNode = ref<any>(null)

const getSavedId = (response: any, fallback: number | null = null) => {
  const data = unwrapResponseData<any>(response)
  return data?.id ? Number(data.id) : fallback
}

const fetchCategoryTree = async () => {
  loading.value = true
  try {
    const res: any = await bomApi.getCategoryTree()
    const tree = unwrapListResponse<any>(res)
    categoryTree.value = Array.isArray(tree) ? tree : []
    categoryOptions.value = [...categoryTree.value]
  } catch (error) {
    categoryTree.value = []
    categoryOptions.value = []
    ElMessage.error('加载分类树失败')
  } finally {
    loading.value = false
  }
}

// 分类树数据
const categoryTree = ref<any[]>([])

// 分类选项（用于级联选择器）
const categoryOptions = ref<CascaderOption[]>([])

// 选择的分类
const selectedCategory = ref<any>(null)

// 是否处于新增模式
const isAddMode = ref(false)

// 表单数据
const formData = ref<any>({
  id: null,
  name: '',
  code: '',
  parentId: [],
  description: '',
  status: 1
})

// 检查分类编码是否唯一
const checkCodeUnique = (code: string, excludeId?: number): boolean => {
  const allNodes = getAllNodes(categoryTree.value)
  return !allNodes.some(node => node.code === code && node.id !== excludeId)
}

// 获取所有节点
const getAllNodes = (tree: any[]): any[] => {
  let nodes: any[] = []
  for (const node of tree) {
    nodes.push(node)
    if (node.children) {
      nodes = [...nodes, ...getAllNodes(node.children)]
    }
  }
  return nodes
}

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '分类名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入分类编码', trigger: 'blur' },
    { min: 2, max: 20, message: '分类编码长度在 2 到 20 个字符', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9-_]+$/, message: '分类编码只能包含字母、数字、下划线和连字符', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: (error?: Error) => void) => {
        if (value && !checkCodeUnique(value, formData.value.id)) {
          callback(new Error('分类编码已存在'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  status: [
    { required: true, message: '请选择分类状态', trigger: 'change' }
  ]
}

// 树属性配置
const treeProps = {
  label: 'name',
  children: 'children',
  value: 'id'
}

// 级联选择器属性配置
const cascaderProps = {
  value: 'id',
  label: 'name',
  children: 'children',
  expandTrigger: 'hover'
}

// 页面挂载时初始化数据
onMounted(async () => {
  // 添加点击事件监听器，点击页面其他地方关闭右键菜单
  document.addEventListener('click', handleClickOutside)
  await fetchCategoryTree()
})

// 页面卸载时移除事件监听器
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

// 处理点击页面其他地方关闭右键菜单
const handleClickOutside = () => {
  showContextMenu.value = false
}

// 处理节点点击
const handleNodeClick = (data: any) => {
  // 退出新增模式
  isAddMode.value = false
  
  selectedCategory.value = { ...data }
  // 将parentId转换为数组格式
  const parentPath = findParentPath(categoryTree.value, data.id)
  formData.value = {
    ...data,
    parentId: parentPath
  }
}

// 查找父节点路径
const findParentPath = (tree: any[], id: number, path: number[] = []): number[] => {
  for (const node of tree) {
    if (node.id === id) {
      // 返回完整路径，不包含当前节点
      return path
    }
    if (node.children) {
      const result = findParentPath(node.children, id, [...path, node.id])
      if (result.length > 0) {
        return result
      }
    }
  }
  return []
}

// 获取节点层级
const getNodeLevel = (tree: any[], id: number, level: number = 0): number => {
  for (const node of tree) {
    if (node.id === id) {
      return level
    }
    if (node.children) {
      const result = getNodeLevel(node.children, id, level + 1)
      if (result !== -1) {
        return result
      }
    }
  }
  return -1
}

// 检查是否为子节点
const isChildNode = (tree: any[], parentId: number, childId: number): boolean => {
  const parentNode = findNode(tree, parentId)
  if (!parentNode || !parentNode.children) {
    return false
  }
  return getAllNodes(parentNode.children).some(node => node.id === childId)
}

// 处理右键菜单
const handleContextMenu = (event: MouseEvent, data: any, node: any) => {
  event.preventDefault()
  selectedNode.value = data
  showContextMenu.value = true
  contextMenuStyle.value = {
    left: `${event.clientX}px`,
    top: `${event.clientY}px`,
    position: 'fixed',
    zIndex: 10000
  }
}

// 重置表单
const resetForm = () => {
  selectedCategory.value = null
  formData.value = {
    id: null,
    name: '',
    code: '',
    parentId: [],
    description: '',
    status: 1
  }
  
  // 重置表单验证
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 处理新增分类
const handleAdd = () => {
  // 明确进入新增模式
  isAddMode.value = true
  selectedCategory.value = null
  
  // 重置表单数据
  formData.value = {
    id: null,
    name: '',
    code: '',
    parentId: [],
    description: '',
    status: 1
  }
  
  // 重置表单验证
  if (formRef.value) {
    formRef.value.clearValidate()
  }
  
  // 滚动到表单区域，确保用户能看到表单
  setTimeout(() => {
    const detailElement = document.querySelector('.category-detail')
    if (detailElement) {
      detailElement.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  }, 100)
}

// 处理保存分类
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    submitting.value = true
    await formRef.value.validate()
    
    // 处理父ID，转换为数字格式
    const parentId = formData.value.parentId.length > 0 ? formData.value.parentId[formData.value.parentId.length - 1] : 0
    
    // 检查分类层级限制（最大3级）
    if (parentId !== 0) {
      const parentLevel = getNodeLevel(categoryTree.value, parentId)
      if (parentLevel >= 2) {
        ElMessage.error('分类层级不能超过3级')
        return
      }
    }
    
    // 检查不能将自己设置为父分类
    if (formData.value.id && parentId === formData.value.id) {
      ElMessage.error('不能将自己设置为父分类')
      return
    }
    
    // 检查不能将子分类设置为父分类
    if (formData.value.id && isChildNode(categoryTree.value, formData.value.id, parentId)) {
      ElMessage.error('不能将子分类设置为父分类')
      return
    }
    
    const categoryData = {
      ...formData.value,
      parentId
    }
    
    const payload: any = {
      name: categoryData.name,
      code: categoryData.code,
      parentId: categoryData.parentId === 0 ? null : categoryData.parentId,
      description: categoryData.description,
      status: categoryData.status
    }

    let savedId: number | null = null
    if (categoryData.id) {
      const res: any = await bomApi.updateCategory(categoryData.id, payload)
      savedId = getSavedId(res, Number(categoryData.id))
    } else {
      const res: any = await bomApi.createCategory(payload)
      savedId = getSavedId(res, null)
    }

    await fetchCategoryTree()
    if (savedId) {
      const node = getAllNodes(categoryTree.value).find(n => Number(n.id) === savedId)
      if (node) {
        handleNodeClick(node)
      }
    }

    ElMessage.success(categoryData.id ? '分类更新成功' : '分类创建成功')
  } catch (error: any) {
    console.error('保存失败:', error)
    
    // 显示详细的错误信息
    if (error && error.errors && error.errors.length > 0) {
      ElMessage.error(`保存失败: ${error.errors[0].message}`)
    } else if (error && error.message) {
      ElMessage.error(`保存失败: ${error.message}`)
    } else {
      ElMessage.error('保存失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

// 查找节点
const findNode = (tree: any[], id: number): any => {
  for (const node of tree) {
    if (node.id === id) {
      return node
    }
    if (node.children) {
      const result = findNode(node.children, id)
      if (result) {
        return result
      }
    }
  }
  return null
}

// 处理编辑分类
const handleEditCategory = () => {
  if (selectedNode.value) {
    handleNodeClick(selectedNode.value)
    showContextMenu.value = false
  }
}

// 处理删除分类
const handleDeleteCategory = () => {
  if (selectedNode.value) {
    ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
      type: 'warning'
    }).then(() => {
      const id = Number(selectedNode.value.id)
      submitting.value = true
      bomApi.deleteCategory(id).then(async () => {
        ElMessage.success('分类删除成功')
        showContextMenu.value = false
        await refreshTree()
      }).catch((error: any) => {
        ElMessage.error(error?.data?.msg || error?.message || '分类删除失败')
      }).finally(() => {
        submitting.value = false
      })
    })
  }
}

// 处理取消
const handleCancel = () => {
  // 退出新增模式
  isAddMode.value = false
  
  // 重置表单数据
  formData.value = {
    id: null,
    name: '',
    code: '',
    parentId: [],
    description: '',
    status: 1
  }
  
  // 重置表单验证
  if (formRef.value) {
    formRef.value.clearValidate()
  }
  
  // 如果有选中的分类，重新加载该分类详情
  if (selectedCategory.value) {
    handleNodeClick(selectedCategory.value)
  }
}

// 刷新分类树
const refreshTree = async () => {
  await fetchCategoryTree()
  selectedCategory.value = null
  formData.value = {
    id: null,
    name: '',
    code: '',
    parentId: [],
    description: '',
    status: 1
  }
  ElMessage.success('分类树已刷新')
}
</script>

<style scoped>
.category-maintenance {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #eef2ff 100%);
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h3 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  background: linear-gradient(to right, var(--primary-color), var(--primary-active));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.category-card {
  padding: 24px;
}

.category-container {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 24px;
  height: 600px;
}

.category-tree {
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.8);
  overflow-y: auto;
}

.category-node {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-detail {
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.8);
  overflow-y: auto;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-placeholder);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.section-title {
  margin: 24px 0 24px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  position: relative;
  display: inline-block;
}

.title-line {
  position: absolute;
  bottom: -6px;
  left: 0;
  width: 40px;
  height: 4px;
  background: var(--grad-primary);
  border-radius: 2px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.status-radio {
  margin-top: 4px;
}
/* 自定义右键菜单样式 */
.custom-context-menu {
  position: fixed;
  background-color: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 10000;
  min-width: 150px;
  padding: 4px 0;
}

.menu-item {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #303133;
  transition: all 0.2s;
}

.menu-item:hover {
  background-color: #ecf5ff;
  color: #409eff;
}

.menu-divider {
  height: 1px;
  background-color: #ebeef5;
  margin: 4px 0;
}
</style>
