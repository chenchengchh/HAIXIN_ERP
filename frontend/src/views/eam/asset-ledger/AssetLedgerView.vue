<template>
  <div class="eam-submodule-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>资产台账</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/eam">EAM系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/eam/asset-ledger">资产台账</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/eam/asset-ledger#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 设备信息对话框 -->
    <el-dialog
      v-model="equipmentDialogVisible"
      title="设备信息"
      width="500px"
      destroy-on-close
    >
      <el-form
        :model="equipmentForm"
        label-position="right"
        label-width="80px"
      >
        <el-form-item label="设备名称" required>
          <el-input v-model="equipmentForm.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备编码" required>
          <el-input v-model="equipmentForm.code" placeholder="请输入设备编码" />
        </el-form-item>
        <el-form-item label="设备类别" required>
          <el-select v-model="equipmentForm.categoryId" placeholder="请选择设备类别" @change="handleCategoryChange">
            <el-option
              v-for="category in equipmentCategories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备型号">
          <el-input v-model="equipmentForm.model" placeholder="请输入设备型号" />
        </el-form-item>
        <el-form-item label="制造商">
          <el-input v-model="equipmentForm.manufacturer" placeholder="请输入制造商" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseEquipmentDialog">取消</el-button>
          <el-button type="primary" @click="handleSaveEquipment">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 设备详情查看对话框 -->
    <el-dialog
      v-model="equipmentDetailDialogVisible"
      title="设备详情"
      width="500px"
      destroy-on-close
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="设备ID">{{ selectedEquipmentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ selectedEquipmentDetail.name }}</el-descriptions-item>
        <el-descriptions-item label="设备编码">{{ selectedEquipmentDetail.code }}</el-descriptions-item>
        <el-descriptions-item label="设备类别">{{ selectedEquipmentDetail.categoryName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="设备型号">{{ selectedEquipmentDetail.model || '-' }}</el-descriptions-item>
        <el-descriptions-item label="制造商">{{ selectedEquipmentDetail.manufacturer || '-' }}</el-descriptions-item>
        <el-descriptions-item label="位置">{{ selectedEquipmentDetail.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="selectedEquipmentDetail.status === 'running' ? 'success' : 'info'">
            {{ selectedEquipmentDetail.status === 'running' ? '运行中' : selectedEquipmentDetail.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="购置日期">{{ selectedEquipmentDetail.purchaseDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="启用日期">{{ selectedEquipmentDetail.startDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="原值">{{ selectedEquipmentDetail.originalValue ? `¥${selectedEquipmentDetail.originalValue}` : '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="equipmentDetailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分类信息对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      :title="isEditCategory ? '编辑分类' : '添加分类'"
      width="400px"
      destroy-on-close
    >
      <el-form
        :model="categoryForm"
        label-position="right"
        label-width="80px"
      >
        <el-form-item label="分类名称" required>
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseCategoryDialog">取消</el-button>
          <el-button type="primary" @click="handleSaveCategory">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 文档上传对话框 -->
    <el-dialog
      v-model="documentUploadDialogVisible"
      title="上传文档"
      width="500px"
      destroy-on-close
    >
      <el-form
        :model="documentForm"
        label-position="right"
        label-width="80px"
      >
        <el-form-item label="文档名称" required>
          <el-input v-model="documentForm.name" placeholder="请输入文档名称" />
        </el-form-item>
        <el-form-item label="关联设备" required>
          <el-select v-model="documentForm.assetId" placeholder="请选择关联设备">
            <el-option
              v-for="equipment in equipmentList"
              :key="equipment.id"
              :label="equipment.name"
              :value="equipment.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="文档类型" required>
          <el-input v-model="documentForm.type" placeholder="请输入文档类型" />
        </el-form-item>
        <el-form-item label="文档文件" required>
          <el-upload
            v-model:file-list="fileList"
            class="upload-demo"
            drag
            action=""
            :auto-upload="false"
            :limit="1"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              拖放文件到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                仅支持单文件上传，文件大小不超过100MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDocumentUploadDialog">取消</el-button>
          <el-button type="primary" @click="handleUploadDocumentFile">上传</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 层次结构节点对话框 -->
    <el-dialog
      v-model="hierarchyNodeDialogVisible"
      :title="isEditHierarchy ? '编辑节点' : '添加节点'"
      width="450px"
      destroy-on-close
    >
      <el-form
        :model="hierarchyNodeForm"
        label-position="right"
        label-width="90px"
      >
        <el-form-item label="父设备" required>
          <el-select
            v-model="hierarchyNodeForm.parentId"
            placeholder="请选择父设备"
            :disabled="isEditHierarchy || !!parentHierarchyNode"
            style="width: 100%"
          >
            <el-option
              v-for="equipment in equipmentList"
              :key="equipment.id"
              :label="equipment.name"
              :value="equipment.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="子设备/部件" required>
          <el-select v-model="hierarchyNodeForm.childId" placeholder="请选择子设备" :disabled="isEditHierarchy" style="width: 100%">
            <el-option
              v-for="equipment in equipmentList"
              :key="equipment.id"
              :label="equipment.name"
              :value="equipment.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="部件类型" required>
          <el-input v-model="hierarchyNodeForm.componentType" placeholder="如：主轴单元、进给系统" />
        </el-form-item>
        <el-form-item label="数量" required>
          <el-input-number v-model="hierarchyNodeForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="hierarchyNodeForm.remark" type="textarea" :rows="2" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseHierarchyNodeDialog">取消</el-button>
          <el-button type="primary" @click="handleSaveHierarchyNode">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 标签页导航区域 -->
    <el-card class="submodule-tabs-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" type="border-card">
        <el-tab-pane label="设备信息管理" name="equipment-info">
          <div class="tab-content">
            <div class="sub-card-header">
              <h3>设备信息管理</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="handleAddEquipment">添加设备</el-button>
                <el-button size="small" @click="handleBatchDeleteEquipment">批量删除</el-button>
                <el-button size="small" @click="handleExportEquipment">导出设备</el-button>
                <el-button size="small" @click="handleImportEquipment">导入设备</el-button>
              </div>
            </div>
            <div class="equipment-info-content">
              <el-table 
                :data="equipmentList" 
                style="width: 100%" 
                height="400"
                @selection-change="handleEquipmentSelectionChange"
              >
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="设备ID" width="100" />
                <el-table-column prop="name" label="设备名称" width="180" />
                <el-table-column prop="code" label="设备编码" width="150" />
                <el-table-column prop="categoryName" label="设备类别" width="120" />
                <el-table-column prop="model" label="设备型号" width="150" />
                <el-table-column prop="manufacturer" label="制造商" width="150" />
                <el-table-column prop="status" label="状态" width="120">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 'running' ? 'success' : 'info'">
                      {{ scope.row.status === 'running' ? '运行中' : '停用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200">
                  <template #default="scope">
                    <el-button size="small" type="primary" @click="handleEditEquipment(scope.row)">编辑</el-button>
                    <el-button size="small" @click="handleViewEquipment(scope.row)">查看</el-button>
                    <el-button size="small" type="danger" @click="handleDeleteEquipment(scope.row)">删除</el-button>
                    <el-button size="small" @click="handleChangeStatus(scope.row)">
                      {{ scope.row.status === 'running' ? '停用' : '启用' }}
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="设备分类管理" name="equipment-category">
          <div class="tab-content">
            <div class="sub-card-header">
              <h3>设备分类管理</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="handleAddCategory">添加分类</el-button>
                <el-button size="small" @click="handleAddSubCategory">添加子分类</el-button>
                <el-button size="small" @click="handleBatchDeleteCategory">批量删除</el-button>
                <el-button size="small" @click="handleExportCategory">导出分类</el-button>
              </div>
            </div>
            <div class="equipment-category-content">
              <el-tree 
                :data="categoryTree" 
                :props="treeProps" 
                default-expand-all
                node-key="id"
                :show-checkbox="true"
                :expand-on-click-node="false"
                @node-click="handleCategoryClick"
                @check="handleCategoryCheck"
                @check-change="handleCategoryCheckChange"
              >
                <template #default="{ node, data }">
                  <div class="tree-node-content">
                    <span>{{ node.label }}</span>
                    <span class="tree-node-actions">
                      <el-button size="small" @click="handleEditCategory(data)">编辑</el-button>
                      <el-button size="small" @click="handleDeleteCategory(data)">删除</el-button>
                      <el-button size="small" @click="handleAddChildCategory(data)">添加子分类</el-button>
                    </span>
                  </div>
                </template>
              </el-tree>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="设备层次结构" name="equipment-hierarchy">
          <div class="tab-content">
            <div class="sub-card-header">
              <h3>设备层次结构</h3>
              <div class="header-actions">
                <el-button size="small" type="primary" @click="handleAddHierarchy">添加节点</el-button>
                <el-button size="small" @click="handleImportHierarchy">导入结构</el-button>
                <el-button size="small" @click="handleExportHierarchy">导出结构</el-button>
              </div>
            </div>
            <div class="equipment-hierarchy-content">
              <el-tree
                v-if="hierarchyData.length > 0"
                :data="hierarchyData"
                :props="hierarchyTreeProps"
                default-expand-all
                node-key="treeKey"
              >
                <template #default="{ node, data }">
                  <div class="tree-node-content">
                    <span>{{ node.label }}</span>
                    <span class="tree-node-actions">
                      <el-button v-if="data.recordId" size="small" @click.stop="handleEditHierarchyNode(data)">编辑</el-button>
                      <el-button v-if="data.recordId" size="small" @click.stop="handleDeleteHierarchyNode(data)">删除</el-button>
                      <el-button size="small" @click.stop="handleAddChildHierarchyNode(data)">添加子节点</el-button>
                    </span>
                  </div>
                </template>
              </el-tree>
              <el-empty v-else description="暂无设备层次结构" :image-size="150" />
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="设备文档管理" name="equipment-document">
          <div class="tab-content">
            <div class="sub-card-header">
              <h3>设备文档管理</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="handleUploadDocument">上传文档</el-button>
                <el-button size="small" @click="handleBatchDeleteDocument">批量删除</el-button>
                <el-button size="small" @click="handleExportDocument">导出文档</el-button>
                <el-button size="small" @click="handleRefreshDocument">刷新文档</el-button>
              </div>
            </div>
            <div class="equipment-document-content">
              <el-table 
                :data="documentList" 
                style="width: 100%" 
                height="400"
                :row-selection="{ type: 'selection', reserveSelection: true }"
                @selection-change="handleDocumentSelectionChange"
              >
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="文档ID" width="100" />
                <el-table-column prop="name" label="文档名称" width="200" />
                <el-table-column prop="assetName" label="关联设备" width="150" />
                <el-table-column prop="type" label="文档类型" width="120" />
                <el-table-column prop="createdAt" label="上传日期" width="170" />
                <el-table-column prop="size" label="文档大小" width="120" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">
                      {{ scope.row.status === 'active' ? '启用' : '禁用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="220">
                  <template #default="scope">
                    <el-button size="small" type="primary" @click="handleDownloadDocument(scope.row)">下载</el-button>
                    <el-button size="small" @click="handleViewDocument(scope.row)">查看</el-button>
                    <el-button size="small" @click="handleEditDocument(scope.row)">编辑</el-button>
                    <el-button size="small" @click="handleToggleDocumentStatus(scope.row)">
                      {{ scope.row.status === 'active' ? '禁用' : '启用' }}
                    </el-button>
                    <el-button size="small" type="danger" @click="handleDeleteDocument(scope.row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAssets, createAsset, updateAsset, deleteAsset, getCategories, createCategory, updateCategory, deleteCategory, getHierarchies, createHierarchy, updateHierarchy, deleteHierarchy, getDocuments, createDocument, updateDocument, deleteDocument, downloadDocument } from '@/api/eam'
import { unwrapListResponse } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

// 路由实例
const router = useRouter()
const route = useRoute()

// 激活的标签页
const activeTab = ref('equipment-info')

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'equipment-info': '设备信息管理',
  'equipment-category': '设备分类管理',
  'equipment-hierarchy': '设备层次结构',
  'equipment-document': '设备文档管理'
}

// 设备信息管理
const equipmentList = ref<any[]>([])

// 设备分类管理
const categoryTree = ref<any[]>([])

const treeProps = {
  label: 'name',
  children: 'children'
}

// 设备层次结构树的字段映射（节点文字字段为 label，与分类树的 name 区分）
const hierarchyTreeProps = {
  label: 'label',
  children: 'children'
}

// 设备文档管理
const documentList = ref<any[]>([])

// 设备类别选项
const equipmentCategories = ref<any[]>([])

// 设备对话框相关
const equipmentDialogVisible = ref(false)
const isEditEquipment = ref(false)
// 设备详情查看对话框相关
const equipmentDetailDialogVisible = ref(false)
const selectedEquipmentDetail = ref<any>({})
const equipmentForm = ref({
  id: null,
  name: '',
  code: '',
  category: '',
  categoryId: null,
  categoryName: '',
  model: '',
  manufacturer: '',
  status: 'running'
})

// 分类对话框相关
const categoryDialogVisible = ref(false)
const isEditCategory = ref(false)
const categoryForm = ref({
  name: '',
  parentId: null,
  children: []
})
const parentCategory = ref<any>(null)

// 文档相关
const documentUploadDialogVisible = ref(false)
const isEditDocument = ref(false)
const documentForm = ref({
  id: null as number | null,
  name: '',
  assetId: null as number | null,
  type: '',
  size: '',
  status: 'active'
})
const fileList = ref<any[]>([])

// 设备层次结构相关
const hierarchyData = ref<any[]>([])
const hierarchyNodeDialogVisible = ref(false)
const isEditHierarchy = ref(false)
const hierarchyNodeForm = ref({
  id: null as number | null,
  parentId: null as number | null,
  childId: null as number | null,
  componentType: '',
  quantity: 1,
  remark: ''
})
const parentHierarchyNode = ref<any>(null)

// 选中的设备列表
const selectedEquipment = ref<any[]>([])

// 选中的分类列表
const selectedCategories = ref<any[]>([])

// 选中的文档列表
const selectedDocument = ref<any[]>([])

// 从路由获取标签页状态
const getActiveTabFromRoute = () => {
  const tabMap: Record<string, string> = {
    'equipment-info': 'equipment-info',
    'equipment-category': 'equipment-category',
    'equipment-hierarchy': 'equipment-hierarchy',
    'equipment-document': 'equipment-document'
  }
  const tabName = route.params.tab || 'equipment-info'
  return tabMap[tabName as string] || 'equipment-info'
}

// 获取设备列表
const fetchAssets = async () => {
  try {
    const res = await getAssets()
    equipmentList.value = unwrapListResponse<any>(res)
  } catch (error) {
    console.error('获取设备列表失败:', error)
    ElMessage.error('获取设备列表失败')
  }
}

// 获取分类列表并构建树
const fetchCategories = async () => {
  try {
    const res = await getCategories()
    const categories = unwrapListResponse<any>(res)
    equipmentCategories.value = categories
    // 构建简单的树结构（这里假设后端返回平铺数据，前端组装，或者后端直接返回树）
    // 为简化，暂时直接展示平铺列表或简单的树
    // 实际应根据parentId构建树
    categoryTree.value = buildCategoryTree(categories)
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  }
}

const buildCategoryTree = (categories: any[]) => {
  const map: Record<number, any> = {}
  const tree: any[] = []
  categories.forEach(c => {
    map[c.id] = { ...c, children: [] }
  })
  categories.forEach(c => {
    if (c.parentId && map[c.parentId]) {
      map[c.parentId].children.push(map[c.id])
    } else {
      tree.push(map[c.id])
    }
  })
  return tree
}

// 组件挂载时，从路由获取标签页状态
onMounted(async () => {
  activeTab.value = getActiveTabFromRoute()
  // 层次结构树依赖设备名称，需先加载设备列表
  await fetchAssets()
  fetchCategories()
  fetchHierarchies()
  fetchDocuments()
})

/**
 * 获取设备层次结构并构建树
 * 树节点：根为父设备，子节点为组成部件（带部件类型和数量）
 */
const fetchHierarchies = async () => {
  try {
    const res = await getHierarchies()
    const records = unwrapListResponse<any>(res)
    hierarchyData.value = buildHierarchyTree(records)
  } catch (error) {
    console.error('获取设备层次结构失败:', error)
    ElMessage.error('获取设备层次结构失败')
  }
}

/**
 * 由层次结构记录构建树：同一设备共享节点对象，使多层嵌套（父→子→孙）正确连接。
 * 根节点为从未作为子节点出现的父设备；recordId 对应该节点作为部件的记录（用于编辑/删除）。
 */
const buildHierarchyTree = (records: any[]) => {
  const assetName = (id: number) => equipmentList.value.find((e: any) => e.id === id)?.name || `设备#${id}`
  const nodeMap: Record<number, any> = {}
  const ensureNode = (assetId: number) => {
    if (!nodeMap[assetId]) {
      nodeMap[assetId] = { treeKey: `asset-${assetId}`, assetId, recordId: null, label: assetName(assetId), children: [] }
    }
    return nodeMap[assetId]
  }
  records.forEach((r: any) => {
    const parentNode = ensureNode(r.parentId)
    // 复用子节点：若该子设备同时是下层记录的父设备，则其下应能继续挂接子节点
    const childNode = ensureNode(r.childId)
    childNode.treeKey = `record-${r.id}`
    childNode.recordId = r.id
    childNode.parentId = r.parentId
    childNode.componentType = r.componentType
    childNode.quantity = r.quantity
    childNode.remark = r.remark
    childNode.label = `${assetName(r.childId)}（${r.componentType}×${r.quantity ?? 1}）`
    if (!parentNode.children.includes(childNode)) {
      parentNode.children.push(childNode)
    }
  })
  // 根节点：从未作为子节点出现的父设备
  const childAssetIds = new Set(records.map((r: any) => r.childId))
  return Object.values(nodeMap).filter((n: any) => !childAssetIds.has(n.assetId))
}

/**
 * 获取设备文档列表
 */
const fetchDocuments = async () => {
  try {
    const res = await getDocuments()
    documentList.value = unwrapListResponse<any>(res)
  } catch (error) {
    console.error('获取设备文档失败:', error)
    ElMessage.error('获取设备文档失败')
  }
}

// 标签页切换事件
const handleTabChange = (tabName: string) => {
  console.log('切换标签页:', tabName)
  // 更新路由，保持标签页状态
  router.push({
    path: `/home/eam/asset-ledger/${tabName}`
  })
}

// 方法
const handleCategoryChange = (value: any) => {
  const category = equipmentCategories.value.find(c => c.id === value)
  if (category) {
    equipmentForm.value.categoryName = category.name
  }
}

const handleAddEquipment = () => {
  /**
   * 处理添加设备
   */
  isEditEquipment.value = false
  equipmentForm.value = {
    id: null,
    name: '',
    code: '',
    category: '',
    categoryId: null,
    categoryName: '',
    model: '',
    manufacturer: '',
    status: 'running'
  }
  equipmentDialogVisible.value = true
}

const handleEditEquipment = (equipment: any) => {
  /**
   * 处理编辑设备
   * @param equipment 要编辑的设备信息
   */
  isEditEquipment.value = true
  equipmentForm.value = { ...equipment }
  equipmentDialogVisible.value = true
}

const handleViewEquipment = (equipment: any) => {
  /**
   * 处理查看设备
   * @param equipment 要查看的设备信息
   */
  selectedEquipmentDetail.value = equipment
  equipmentDetailDialogVisible.value = true
}

const handleDeleteEquipment = async (equipment: any) => {
  /**
   * 处理删除设备
   * @param equipment 要删除的设备信息
   */
  try {
    await deleteAsset(equipment.id)
    ElMessage.success('删除成功')
    fetchAssets()
  } catch (error) {
    console.error('删除设备失败:', error)
    ElMessage.error('删除设备失败')
  }
}

const handleChangeStatus = async (equipment: any) => {
  /**
   * 处理改变设备状态
   * @param equipment 要改变状态的设备信息
   */
  const newStatus = equipment.status === 'running' ? 'stopped' : 'running'
  try {
    await updateAsset(equipment.id, { ...equipment, status: newStatus })
    ElMessage.success('状态更新成功')
    fetchAssets()
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  }
}

const handleEquipmentSelectionChange = (selection: any[]) => {
  /**
   * 处理设备选择变化
   * @param selection 选中的设备列表
   */
  selectedEquipment.value = selection
  console.log('选中的设备:', selection)
}

const handleBatchDeleteEquipment = async () => {
  /**
   * 处理批量删除设备：确认后循环调用删除接口
   */
  if (selectedEquipment.value.length === 0) {
    ElMessage.warning('请选择要删除的设备')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedEquipment.value.length} 台设备吗？`,
      '批量删除',
      { type: 'warning' }
    )
  } catch {
    return
  }
  let successCount = 0
  for (const item of selectedEquipment.value) {
    try {
      await deleteAsset(item.id)
      successCount++
    } catch (error) {
      console.error(`删除设备失败: id=${item.id}`, error)
    }
  }
  if (successCount === selectedEquipment.value.length) {
    ElMessage.success(`成功删除 ${successCount} 台设备`)
  } else {
    ElMessage.warning(`删除完成：成功 ${successCount} 台，失败 ${selectedEquipment.value.length - successCount} 台`)
  }
  selectedEquipment.value = []
  fetchAssets()
}

/**
 * 通用CSV导出工具：将表头与数据行生成CSV文件并触发下载
 * @param filename 文件名（不含扩展名）
 * @param headers 表头数组
 * @param rows 数据行二维数组
 */
const exportCsvFile = (filename: string, headers: string[], rows: any[][]) => {
  const csv = [headers, ...rows]
    .map(line => line.map(v => `"${String(v ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\n')
  const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${filename}-${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

const handleExportEquipment = () => {
  /**
   * 处理导出设备：导出当前设备列表为CSV
   */
  if (equipmentList.value.length === 0) {
    ElMessage.warning('暂无设备数据可导出')
    return
  }
  const headers = ['设备ID', '设备名称', '设备编码', '设备类别', '设备型号', '制造商', '状态', '位置']
  const rows = equipmentList.value.map((e: any) => [
    e.id, e.name, e.code, e.categoryName, e.model, e.manufacturer,
    e.status === 'running' ? '运行中' : e.status, e.location
  ])
  exportCsvFile('equipment-list', headers, rows)
  ElMessage.success('设备列表导出成功')
}

const handleImportEquipment = () => {
  /**
   * 处理导入设备
   */
  ElMessage.info('设备导入功能开发中，请通过"添加设备"逐条录入')
}

const handleCloseEquipmentDialog = () => {
  /**
   * 处理关闭设备对话框
   */
  equipmentDialogVisible.value = false
}

const handleSaveEquipment = async () => {
  /**
   * 处理保存设备信息
   */
  try {
    if (isEditEquipment.value) {
      await updateAsset(equipmentForm.value.id!, equipmentForm.value)
      ElMessage.success('更新成功')
    } else {
      await createAsset(equipmentForm.value)
      ElMessage.success('创建成功')
    }
    equipmentDialogVisible.value = false
    fetchAssets()
  } catch (error) {
    console.error('保存设备失败:', error)
    ElMessage.error('保存设备失败')
  }
}

const handleAddCategory = () => {
  /**
   * 处理添加分类
   */
  isEditCategory.value = false
  parentCategory.value = null
  categoryForm.value = {
    name: '',
    parentId: null,
    children: []
  }
  categoryDialogVisible.value = true
}

const handleEditCategory = (category: any) => {
  /**
   * 处理编辑分类
   * @param category 要编辑的分类信息
   */
  isEditCategory.value = true
  parentCategory.value = null
  categoryForm.value = { ...category }
  categoryDialogVisible.value = true
}

const handleDeleteCategory = async (category: any) => {
  /**
   * 处理删除分类：确认后调用删除接口
   * @param category 要删除的分类信息
   */
  try {
    await ElMessageBox.confirm(`确认删除分类"${category.name}"吗？`, '删除分类', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteCategory(category.id)
    ElMessage.success('分类删除成功')
    fetchCategories()
  } catch (error) {
    console.error('删除分类失败:', error)
    ElMessage.error('删除分类失败')
  }
}

const handleAddSubCategory = () => {
  /**
   * 处理添加子分类
   */
  if (selectedCategories.value.length !== 1) {
    ElMessage.warning('请先勾选一个父分类')
    return
  }
  isEditCategory.value = false
  parentCategory.value = selectedCategories.value[0]
  categoryForm.value = {
    name: '',
    parentId: parentCategory.value.id,
    children: []
  }
  categoryDialogVisible.value = true
}

const handleAddChildCategory = (parent: any) => {
  /**
   * 为指定分类添加子分类
   * @param parent 父分类信息
   */
  isEditCategory.value = false
  parentCategory.value = parent
  categoryForm.value = {
    name: '',
    parentId: parent.id,
    children: []
  }
  categoryDialogVisible.value = true
}

const handleCategoryClick = (data: any, node: any, component: any) => {
  /**
   * 处理分类点击
   * @param data 分类数据
   * @param node 节点信息
   * @param component 组件实例
   */
  console.log('点击分类:', data, node, component)
}

const handleCategoryCheck = (data: any, checkState: any) => {
  /**
   * 处理分类勾选：同步当前勾选的分类列表，支撑批量删除/添加子分类
   * @param data 当前勾选节点数据
   * @param checkState 勾选状态对象（含checkedNodes）
   */
  selectedCategories.value = checkState?.checkedNodes ?? []
}

const handleCategoryCheckChange = (data: any, checked: boolean, indeterminate: boolean) => {
  /**
   * 处理分类勾选状态变化
   * @param data 分类数据
   * @param checked 是否选中
   * @param indeterminate 是否半选中
   */
  console.log('分类勾选状态变化:', data, checked, indeterminate)
}

const handleBatchDeleteCategory = async () => {
  /**
   * 处理批量删除分类：确认后循环调用删除接口
   */
  if (selectedCategories.value.length === 0) {
    ElMessage.warning('请先勾选要删除的分类')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认删除勾选的 ${selectedCategories.value.length} 个分类吗？`,
      '批量删除',
      { type: 'warning' }
    )
  } catch {
    return
  }
  let successCount = 0
  for (const item of selectedCategories.value) {
    try {
      await deleteCategory(item.id)
      successCount++
    } catch (error) {
      console.error(`删除分类失败: id=${item.id}`, error)
    }
  }
  ElMessage.success(`成功删除 ${successCount} 个分类`)
  selectedCategories.value = []
  fetchCategories()
}

const handleExportCategory = () => {
  /**
   * 处理导出分类：导出当前分类列表为CSV
   */
  if (equipmentCategories.value.length === 0) {
    ElMessage.warning('暂无分类数据可导出')
    return
  }
  const headers = ['分类ID', '分类名称', '父分类ID']
  const rows = equipmentCategories.value.map((c: any) => [c.id, c.name, c.parentId ?? ''])
  exportCsvFile('equipment-categories', headers, rows)
  ElMessage.success('分类列表导出成功')
}

const handleCloseCategoryDialog = () => {
  /**
   * 处理关闭分类对话框
   */
  categoryDialogVisible.value = false
}

const handleSaveCategory = async () => {
  /**
   * 处理保存分类信息：新增走创建接口，编辑走更新接口
   */
  if (!categoryForm.value.name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  try {
    if (isEditCategory.value && (categoryForm.value as any).id) {
      await updateCategory((categoryForm.value as any).id, categoryForm.value)
      ElMessage.success('分类更新成功')
    } else {
      await createCategory(categoryForm.value)
      ElMessage.success('分类创建成功')
    }
    categoryDialogVisible.value = false
    fetchCategories()
  } catch (error) {
    console.error('保存分类失败:', error)
    ElMessage.error('保存分类失败')
  }
}

const handleDownloadDocument = async (document: any) => {
  /**
   * 处理下载文档：从后端获取Base64内容并触发浏览器下载
   * @param document 要下载的文档信息
   */
  try {
    const res = await downloadDocument(document.id)
    const data = (res as any)?.data?.data ?? (res as any)?.data
    const content = data?.content as string | undefined
    if (!content || !content.startsWith('base64:')) {
      ElMessage.info(`文档"${document.name}"没有可下载的文件内容`)
      return
    }
    // Base64 解码为二进制并生成下载链接
    const byteChars = atob(content.slice('base64:'.length))
    const byteNumbers = new Array(byteChars.length)
    for (let i = 0; i < byteChars.length; i++) {
      byteNumbers[i] = byteChars.charCodeAt(i)
    }
    const blob = new Blob([new Uint8Array(byteNumbers)])
    const url = URL.createObjectURL(blob)
    const a = window.document.createElement('a')
    a.href = url
    a.download = data.fileName || document.name
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('文档下载成功')
  } catch (error) {
    console.error('下载文档失败:', error)
    ElMessage.error('下载文档失败')
  }
}

const handleDeleteDocument = async (document: any) => {
  /**
   * 处理删除文档（调用后端API）
   * @param document 要删除的文档信息
   */
  try {
    await ElMessageBox.confirm(`确认删除文档"${document.name}"吗？`, '删除文档', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteDocument(document.id)
    ElMessage.success('文档删除成功')
    fetchDocuments()
  } catch (error) {
    console.error('删除文档失败:', error)
    ElMessage.error('删除文档失败')
  }
}

const handleEditDocument = (document: any) => {
  /**
   * 处理编辑文档：打开上传对话框并回填表单（编辑模式不强制重新选择文件）
   * @param document 要编辑的文档信息
   */
  isEditDocument.value = true
  documentForm.value = {
    id: document.id,
    name: document.name,
    assetId: document.assetId ?? null,
    type: document.type,
    size: document.size,
    status: document.status ?? 'active'
  }
  fileList.value = []
  documentUploadDialogVisible.value = true
}

const handleToggleDocumentStatus = async (document: any) => {
  /**
   * 处理切换文档状态（调用后端API持久化）
   * @param document 要切换状态的文档信息
   */
  const newStatus = document.status === 'active' ? 'inactive' : 'active'
  try {
    await updateDocument(document.id, {
      name: document.name,
      assetId: document.assetId,
      assetName: document.assetName,
      type: document.type,
      size: document.size,
      status: newStatus
    })
    ElMessage.success(`文档已${newStatus === 'active' ? '启用' : '禁用'}`)
    fetchDocuments()
  } catch (error) {
    console.error('切换文档状态失败:', error)
    ElMessage.error('切换文档状态失败')
  }
}

const handleDocumentSelectionChange = (selection: any[]) => {
  /**
   * 处理文档选择变化
   * @param selection 选中的文档列表
   */
  selectedDocument.value = selection
}

const handleBatchDeleteDocument = async () => {
  /**
   * 处理批量删除文档（循环调用后端删除API）
   */
  if (selectedDocument.value.length === 0) {
    ElMessage.warning('请选择要删除的文档')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedDocument.value.length} 个文档吗？`, '批量删除', { type: 'warning' })
  } catch {
    return
  }
  let successCount = 0
  for (const item of selectedDocument.value) {
    try {
      await deleteDocument(item.id)
      successCount++
    } catch (error) {
      console.error(`删除文档失败: id=${item.id}`, error)
    }
  }
  ElMessage.success(`成功删除 ${successCount} 个文档`)
  selectedDocument.value = [] // 清空选择
  fetchDocuments()
}

const handleExportDocument = () => {
  /**
   * 处理导出文档：导出当前文档列表为CSV
   */
  if (documentList.value.length === 0) {
    ElMessage.warning('暂无文档数据可导出')
    return
  }
  const headers = ['文档ID', '文档名称', '关联设备', '文档类型', '上传日期', '文档大小', '状态']
  const rows = documentList.value.map((d: any) => [
    d.id, d.name, d.assetName, d.type, d.createdAt, d.size,
    d.status === 'active' ? '启用' : '禁用'
  ])
  exportCsvFile('equipment-documents', headers, rows)
  ElMessage.success('文档列表导出成功')
}

const handleRefreshDocument = () => {
  /**
   * 处理刷新文档列表（重新拉取后端数据）
   */
  fetchDocuments()
  ElMessage.success('文档列表已刷新')
}

const handleAddHierarchy = () => {
  /**
   * 处理添加根节点：选择父设备与子设备/部件创建组成关系
   */
  isEditHierarchy.value = false
  parentHierarchyNode.value = null
  hierarchyNodeForm.value = {
    id: null,
    parentId: null,
    childId: null,
    componentType: '',
    quantity: 1,
    remark: ''
  }
  hierarchyNodeDialogVisible.value = true
}

const handleImportHierarchy = () => {
  /**
   * 处理导入结构
   */
  ElMessage.info('层次结构导入功能开发中，请通过"添加节点"逐层构建')
}

const handleExportHierarchy = () => {
  /**
   * 处理导出结构：将层次结构树扁平化导出为CSV
   */
  if (hierarchyData.value.length === 0) {
    ElMessage.warning('暂无层次结构数据可导出')
    return
  }
  const rows: any[][] = []
  const walk = (nodes: any[], path: string) => {
    nodes.forEach(n => {
      const current = path ? `${path}/${n.label}` : n.label
      rows.push([current])
      if (n.children?.length) walk(n.children, current)
    })
  }
  walk(hierarchyData.value, '')
  exportCsvFile('equipment-hierarchy', ['节点路径'], rows)
  ElMessage.success('层次结构导出成功')
}

const handleEditHierarchyNode = (node: any) => {
  /**
   * 处理编辑层次结构节点（编辑部件类型/数量/备注）
   * @param node 要编辑的节点信息（recordId 对应后端记录）
   */
  isEditHierarchy.value = true
  parentHierarchyNode.value = null
  hierarchyNodeForm.value = {
    id: node.recordId,
    parentId: node.parentId,
    childId: node.assetId,
    componentType: node.componentType,
    quantity: node.quantity ?? 1,
    remark: node.remark ?? ''
  }
  hierarchyNodeDialogVisible.value = true
}

const handleDeleteHierarchyNode = async (node: any) => {
  /**
   * 处理删除层次结构节点（调用后端API删除记录）
   * @param node 要删除的节点信息
   */
  try {
    await ElMessageBox.confirm(`确认删除节点"${node.label}"吗？`, '删除节点', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteHierarchy(node.recordId)
    ElMessage.success('节点删除成功')
    fetchHierarchies()
  } catch (error) {
    console.error('删除节点失败:', error)
    ElMessage.error('删除节点失败')
  }
}

const handleAddChildHierarchyNode = (parent: any) => {
  /**
   * 为指定节点添加子节点（parentId 锁定为该节点设备）
   * @param parent 父节点信息
   */
  isEditHierarchy.value = false
  parentHierarchyNode.value = parent
  hierarchyNodeForm.value = {
    id: null,
    parentId: parent.assetId,
    childId: null,
    componentType: '',
    quantity: 1,
    remark: ''
  }
  hierarchyNodeDialogVisible.value = true
}

const handleCloseHierarchyNodeDialog = () => {
  /**
   * 处理关闭层次结构节点对话框
   */
  hierarchyNodeDialogVisible.value = false
}

const handleSaveHierarchyNode = async () => {
  /**
   * 处理保存层次结构节点：新增/编辑均调用后端API持久化
   */
  const form = hierarchyNodeForm.value
  if (!form.parentId || !form.childId) {
    ElMessage.warning('请选择父设备和子设备/部件')
    return
  }
  if (form.parentId === form.childId) {
    ElMessage.warning('父设备与子设备不能相同')
    return
  }
  if (!form.componentType) {
    ElMessage.warning('请输入部件类型')
    return
  }
  try {
    if (isEditHierarchy.value && form.id) {
      await updateHierarchy(form.id, {
        parentId: form.parentId,
        childId: form.childId,
        componentType: form.componentType,
        quantity: form.quantity,
        remark: form.remark
      })
      ElMessage.success('节点更新成功')
    } else {
      await createHierarchy({
        parentId: form.parentId,
        childId: form.childId,
        componentType: form.componentType,
        quantity: form.quantity,
        remark: form.remark
      })
      ElMessage.success('节点添加成功')
    }
    hierarchyNodeDialogVisible.value = false
    fetchHierarchies()
  } catch (error) {
    console.error('保存节点失败:', error)
    ElMessage.error('保存节点失败')
  }
}

const handleUploadDocument = () => {
  /**
   * 处理上传文档：重置表单并打开对话框
   */
  isEditDocument.value = false
  documentForm.value = {
    id: null,
    name: '',
    assetId: null,
    type: '',
    size: '',
    status: 'active'
  }
  fileList.value = []
  documentUploadDialogVisible.value = true
}

const handleViewDocument = (document: any) => {
  /**
   * 处理查看文档
   * @param document 要查看的文档信息
   */
  ElMessage.info(`文档"${document.name}"（${document.type}），关联设备：${document.assetName || '无'}`)
}

const handleCloseDocumentUploadDialog = () => {
  /**
   * 处理关闭文档上传对话框
   */
  documentUploadDialogVisible.value = false
  fileList.value = []
}

/**
 * 读取文件为带 "base64:" 前缀的 Base64 字符串
 * @param file 原始 File 对象
 */
const readFileAsBase64 = (file: File): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = reader.result as string
      // result 形如 data:<mime>;base64,<content>，去掉头部后加统一前缀
      const base64 = result.includes(',') ? result.split(',')[1] : result
      resolve(`base64:${base64}`)
    }
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(file)
  })
}

/**
 * 格式化文件大小展示文本
 * @param bytes 字节数
 */
const formatFileSize = (bytes: number): string => {
  if (bytes >= 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)}MB`
  return `${(bytes / 1024).toFixed(1)}KB`
}

const handleUploadDocumentFile = async () => {
  /**
   * 处理上传文档文件：读取文件Base64内容并调用后端API持久化
   */
  if (!documentForm.value.name) {
    ElMessage.warning('请输入文档名称')
    return
  }
  if (!documentForm.value.assetId) {
    ElMessage.warning('请选择关联设备')
    return
  }
  if (!documentForm.value.type) {
    ElMessage.warning('请输入文档类型')
    return
  }
  const asset = equipmentList.value.find((e: any) => e.id === documentForm.value.assetId)
  try {
    if (isEditDocument.value && documentForm.value.id) {
      // 编辑模式：仅更新元数据（不传 content，后端保留原文件）
      await updateDocument(documentForm.value.id, {
        name: documentForm.value.name,
        assetId: documentForm.value.assetId,
        assetName: asset?.name ?? '',
        type: documentForm.value.type,
        status: documentForm.value.status
      })
      ElMessage.success('文档更新成功')
    } else {
      if (fileList.value.length === 0) {
        ElMessage.warning('请选择要上传的文件')
        return
      }
      const rawFile = fileList.value[0].raw as File
      const content = await readFileAsBase64(rawFile)
      await createDocument({
        name: documentForm.value.name,
        fileName: rawFile.name,
        assetId: documentForm.value.assetId,
        assetName: asset?.name ?? '',
        type: documentForm.value.type,
        size: formatFileSize(rawFile.size),
        status: 'active',
        uploadedBy: 'admin',
        content
      })
      ElMessage.success('文档上传成功')
    }
    documentUploadDialogVisible.value = false
    fileList.value = []
    fetchDocuments()
  } catch (error) {
    console.error('保存文档失败:', error)
    ElMessage.error('保存文档失败')
  }
}
</script>

<style scoped>
.eam-submodule-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 页面头部样式 */
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color-light);
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-size: 1.8rem;
}

/* 覆盖默认的h2样式，确保只影响页面标题 */
h2 {
  margin-bottom: 0;
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.submodule-title {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
  color: #303133;
}

.submodule-description {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.submodule-tabs-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tab-content {
  padding: 20px;
}

.sub-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.sub-card-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}

.equipment-info-content {
  margin-top: 20px;
}

.equipment-category-content {
  height: 400px;
  overflow: auto;
  margin-top: 20px;
}

.equipment-hierarchy-content {
  height: 400px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-top: 20px;
  padding: 16px;
  overflow: auto;
}

.equipment-document-content {
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .eam-submodule-container {
    padding: 12px;
  }
  
  .submodule-title {
    font-size: 20px;
  }
  
  .tab-content {
    padding: 12px;
  }
  
  .sub-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
