<template>
  <bom-layout title="BOM结构编辑器 (Advanced Editor)" :breadcrumb-items="[{ label: '结构管理' }, { label: 'BOM编辑器' }]">
    <template #action-bar>
      <div class="header-actions">
        <el-button type="info" @click="handleExport" class="btn-subtle">
          <el-icon><Download /></el-icon>
          导出结构
        </el-button>
        <el-upload
          :show-file-list="false"
          accept=".json,.csv"
          :before-upload="handleImport"
          class="upload-btn"
        >
          <el-button class="btn-subtle">
            <el-icon><Upload /></el-icon>
            导入
          </el-button>
        </el-upload>
        <el-button type="primary" @click="handleSaveBom" class="btn-glow">
          <el-icon><Check /></el-icon>
          保存版本
        </el-button>
      </div>
    </template>
      
    <!-- 核心统计概览 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon"><el-icon><Operation /></el-icon></div>
        <div class="stat-main">
          <span class="label">层级深度 / 物料总数</span>
          <span class="value">{{ totalMaterials }} <small>/ Nodes</small></span>
        </div>
      </div>
      <div class="stat-card primary">
        <div class="stat-icon"><el-icon><Money /></el-icon></div>
        <div class="stat-main">
          <span class="label">BOM 卷算总成本</span>
          <span class="value">¥{{ totalCost.toFixed(2) }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
        <div class="stat-main">
          <span class="label">平均物料价值</span>
          <span class="value">¥{{ averageCost.toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <!-- BOM定义区 -->
    <div class="bom-header-card card-glossy">
      <el-form label-position="top" :model="bomInfo" class="bom-form">
        <el-row :gutter="24">
          <el-col :span="6">
            <el-form-item label="BOM编码" required>
              <el-input v-model="bomInfo.bomCode" placeholder="如：BOM-PRD-001" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="产品代码" required>
              <el-input v-model="bomInfo.materialCode" placeholder="如：PRD-001" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="产品名称" required>
              <el-input v-model="bomInfo.materialName" placeholder="如：产品A" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="版本标识" required>
              <el-input v-model="bomInfo.version" placeholder="如：V1.0" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
          <el-form-item label="定义状态" required>
            <el-select v-model="bomInfo.status" style="width: 100%" @change="handleStatusChange">
              <el-option label="草案 (Draft)" value="0" />
              <el-option label="已生效 (Active)" value="1" />
              <el-option label="已失效 (Inactive)" value="2" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="当前状态">
            <div class="status-display">
              <el-tag 
                :type="getStatusType(bomInfo.status)" 
                size="large" 
                effect="dark"
              >
                {{ getStatusText(bomInfo.status) }}
              </el-tag>
              <div class="status-dates" v-if="bomInfo.effectiveDate || bomInfo.expireDate">
                <span v-if="bomInfo.effectiveDate" class="date-item">
                  <el-icon><Calendar /></el-icon>
                  生效: {{ bomInfo.effectiveDate }}
                </span>
                <span v-if="bomInfo.expireDate" class="date-item">
                  <el-icon><Calendar /></el-icon>
                  失效: {{ bomInfo.expireDate }}
                </span>
              </div>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="状态操作">
            <div class="status-actions">
              <el-button 
                v-if="bomInfo.status !== '1'" 
                type="success" 
                size="small" 
                @click="handleActivateBom"
                :disabled="loadingState"
              >
                <el-icon v-if="loadingState"><Loading /></el-icon>
                <el-icon v-else><Check /></el-icon>
                激活
              </el-button>
              <el-button 
                v-if="bomInfo.status === '1'" 
                type="danger" 
                size="small" 
                @click="handleInactivateBom"
                :disabled="loadingState"
              >
                <el-icon v-if="loadingState"><Loading /></el-icon>
                <el-icon v-else><Close /></el-icon>
                失效
              </el-button>
            </div>
          </el-form-item>
        </el-col>
          <el-col :span="6">
            <el-form-item label="生效日期">
              <el-date-picker
                v-model="bomInfo.effectiveDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择生效日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="失效日期">
              <el-date-picker
                v-model="bomInfo.expireDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择失效日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 交互操作桥 -->
    <div class="editor-bridge">
      <div class="bridge-left">
        <el-button-group class="btn-group-premium">
          <el-button type="primary" @click="handleAddRoot" class="btn-glow" :title="'新增根节点 (Ctrl+N)'">
            <el-icon><Plus /></el-icon>
            新增根节点
          </el-button>
          <el-button type="primary" plain @click="handleAddChild" :disabled="!selectedNode" :title="'添加子项 (Ctrl+Shift+N)'">
            <el-icon><Connection /></el-icon>
            添加子项
          </el-button>
        </el-button-group>
      </div>
      <div class="bridge-right">
        <el-button @click="handleCopyNode" :disabled="!selectedNode" :icon="DocumentCopy" :title="'复制节点 (Ctrl+C)'">复制</el-button>
        <el-button @click="handlePasteNode" :disabled="!copiedNode || !selectedNode" :icon="Files" :title="'粘贴节点 (Ctrl+V)'">粘贴</el-button>
        <el-divider direction="vertical" />
        <el-button type="warning" @click="handleEditNode" :disabled="!selectedNode" :icon="EditPen" :title="'编辑节点 (Enter)'">编辑</el-button>
        <el-button type="danger" @click="handleDeleteNode" :disabled="!selectedNode" :icon="Delete" :title="'删除节点 (Delete)'">删除</el-button>
      </div>
    </div>

    <!-- 结构树可视化 -->
    <div class="structure-tree card-glossy" tabindex="0" @keydown="handleKeyDown">
      <!-- 节点信息统计 -->
      <div class="tree-header">
        <div class="tree-stats">
          <span class="stat-item">
            <el-tag type="primary" effect="light">根节点: {{ rootNodeCount }}</el-tag>
          </span>
          <span class="stat-item">
            <el-tag type="success" effect="light">总节点数: {{ totalMaterials }}</el-tag>
          </span>
          <span class="stat-item">
            <el-tag type="info" effect="light">层级深度: {{ maxDepth }}</el-tag>
          </span>
        </div>
        <div class="tree-actions">
          <el-button type="primary" plain size="small" @click="expandAllNodes">
            <el-icon><Expand /></el-icon> 全部展开
          </el-button>
          <el-button type="primary" plain size="small" @click="collapseAllNodes">
            <el-icon><Fold /></el-icon> 全部收起
          </el-button>
        </div>
      </div>
      
      <el-table
        :data="bomNodes"
        style="width: 100%"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        @row-click="handleRowClick"
        @row-dblclick="handleEditNode"
        highlight-current-row
        v-loading="loading"
      >
        <el-table-column prop="materialCode" label="物料编码" width="160" align="center">
          <template #default="scope">
            <div class="code-wrapper">
              <span class="code-text">{{ scope.row.materialCode }}</span>
              <el-tooltip content="替代料方案" v-if="scope.row.hasSubstitute">
                <el-icon class="sub-icon"><Switch /></el-icon>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="materialName" label="物料名称" min-width="250" show-overflow-tooltip align="left" />
        <el-table-column prop="specification" label="规格型号" min-width="180" show-overflow-tooltip align="left" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="quantity" label="用量" width="100" align="right">
          <template #default="scope">
            <span class="qty-text">{{ scope.row.quantity.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="unitCost" label="标准单价" width="120" align="right">
          <template #default="scope">
            <span class="currency">¥{{ (scope.row.unitCost || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalCost" label="卷算小计" width="140" align="right">
          <template #default="scope">
            <span class="total-currency">¥{{ (scope.row.totalCost || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="层级" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.level === 1 ? 'primary' : 'info'" effect="plain" size="small">
              {{ scope.row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="节点类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'root' ? 'primary' : scope.row.type === 'sub' ? 'success' : 'warning'" effect="plain">
              {{ scope.row.type === 'root' ? '根节点' : scope.row.type === 'sub' ? '子节点' : '虚拟件' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="() => {
              selectedNode.value = scope.row;
              handleEditNode();
            }">
              <el-icon><EditPen /></el-icon>
            </el-button>
            <el-button size="small" type="danger" @click="() => {
              selectedNode.value = scope.row;
              handleDeleteNode();
            }">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 节点编辑弹窗 -->
      <el-dialog v-model="dialogVisible" title="编辑BOM节点" width="600px">
        <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="120px">
          <el-form-item label="物料编码" prop="materialCode">
            <el-input v-model="editForm.materialCode" placeholder="请输入物料编码" />
          </el-form-item>
          <el-form-item label="物料名称" prop="materialName">
            <el-input v-model="editForm.materialName" placeholder="请输入物料名称" />
          </el-form-item>
          <el-form-item label="规格型号" prop="specification">
            <el-input v-model="editForm.specification" placeholder="请输入规格型号" />
          </el-form-item>
          <el-form-item label="单位" prop="unit">
            <el-input v-model="editForm.unit" placeholder="请输入单位" />
          </el-form-item>
          <el-form-item label="用量" prop="quantity">
            <el-input-number v-model="editForm.quantity" :min="0.1" :step="0.1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="损耗率(%)" prop="scrapRate">
            <el-input-number v-model="editForm.scrapRate" :min="0" :max="100" :step="0.1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="类型" prop="type">
            <el-select v-model="editForm.type" placeholder="请选择类型">
              <el-option label="根节点" value="root" />
              <el-option label="子节点" value="sub" />
              <el-option label="虚拟件" value="virtual" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="editForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSaveNode">保存</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </bom-layout>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { 
  Edit, Plus, Delete, Check, Download, Upload, DocumentCopy, Switch, Close,
  Operation, Money, TrendCharts, Connection, EditPen, Files, Expand, Fold, Loading, Calendar
} from '@element-plus/icons-vue'
import { unwrapListResponse, unwrapPageResponse, unwrapResponseData } from '@/api'
import { bomApi, type BomNode } from '@/api/bom'
import { ElMessage, ElMessageBox, ElTable } from 'element-plus'
import BomLayout from '../../../components/BomLayout.vue'

const route = useRoute()

// BOM基本信息
interface BomInfo {
  bomCode: string
  materialCode: string
  materialName: string
  bomType: string
  version: string
  status: string
  effectiveDate: string
  expireDate: string
}

const currentHeaderId = ref<number | null>(null)
const currentMaterialId = ref<number | null>(null)

const bomInfo = ref<BomInfo>({
  bomCode: '',
  materialCode: '',
  materialName: '',
  bomType: '1',
  version: 'V1.0',
  status: '0',
  effectiveDate: '',
  expireDate: ''
})

// 加载状态
const loadingState = ref(false)

// 状态文本映射
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    '0': '草案',
    '1': '已生效',
    '2': '已失效'
  }
  return statusMap[status] || '未知状态'
}

// 状态类型映射
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    '0': 'info',
    '1': 'success',
    '2': 'danger'
  }
  return typeMap[status] || 'warning'
}

// 状态变更处理
const handleStatusChange = (newStatus: string) => {
  // 当从状态选择器直接修改状态时，自动更新相关日期
  if (newStatus === '1') {
      // 激活状态：设置生效日期，清空失效日期
      if (bomInfo.value.effectiveDate === '') {
        bomInfo.value.effectiveDate = new Date().toISOString().split('T')[0] || ''
      }
      bomInfo.value.expireDate = ''
    } else if (newStatus === '2') {
      // 失效状态：设置失效日期
      if (bomInfo.value.expireDate === '') {
        bomInfo.value.expireDate = new Date().toISOString().split('T')[0] || ''
      }
      // 确保失效日期不早于生效日期
      if (bomInfo.value.effectiveDate && bomInfo.value.expireDate && bomInfo.value.expireDate < bomInfo.value.effectiveDate) {
        ElMessage.warning('失效日期不能早于生效日期')
        // 重置为当前日期
        bomInfo.value.expireDate = new Date().toISOString().split('T')[0] || ''
      }
    } else if (newStatus === '0') {
      // 草案状态：清空生效和失效日期
      bomInfo.value.expireDate = ''
    }
  
  ElMessage.success(`BOM状态已更新为：${getStatusText(newStatus)}`)
}

// BOM节点数据
const bomNodes = ref<BomNode[]>([])

// 递归查找节点
const findNode = (nodes: any[], id: any): { node: any; parent: any | null; index: number } | null => {
  for (let i = 0; i < nodes.length; i++) {
    if (nodes[i].id === id) {
      return { node: nodes[i], parent: null, index: i }
    }
    if (nodes[i].children && nodes[i].children.length > 0) {
      const result = findNode(nodes[i].children, id)
      if (result) {
        return { node: result.node, parent: nodes[i], index: result.index }
      }
    }
  }
  return null
}

// 加载BOM数据
const normalizeToDate = (value: any): string => {
  if (!value) return ''
  const s = String(value)
  return s.length >= 10 ? s.slice(0, 10) : s
}

const toDateTimeString = (date: string): string | null => {
  if (!date) return null
  return `${date}T00:00:00`
}

const recalculateNodeTotals = (node: any): number => {
  const selfCost = Number(node.unitCost ?? 0) * Number(node.quantity ?? 0)
  const children = Array.isArray(node.children) ? node.children : []
  const childrenCost = children.reduce((sum: number, child: any) => sum + recalculateNodeTotals(child), 0)
  const total = selfCost + childrenCost
  node.totalCost = total
  return total
}

const recalculateAllTotals = () => {
  if (!Array.isArray(bomNodes.value)) return
  bomNodes.value.forEach(n => recalculateNodeTotals(n))
}

const getMaterialIdByCode = async (materialCode: string): Promise<number | null> => {
  const code = String(materialCode || '').trim()
  if (!code) return null
  const res: any = await bomApi.getMaterialList({ code, page: 1, size: 1 })
  const list = unwrapPageResponse<any>(res).list
  const first = Array.isArray(list) && list.length > 0 ? list[0] : null
  return first?.id ? Number(first.id) : null
}

const enrichUnitCostByMaterialId = async (root: any) => {
  const cache = new Map<number, number>()
  const traverse = async (node: any) => {
    const mid = Number(node?.materialId ?? 0)
    if (mid > 0) {
      if (!cache.has(mid)) {
        try {
          const res: any = await bomApi.getMaterialDetail(mid)
          const unitPriceRaw = unwrapResponseData<any>(res)?.unitPrice
          const unitPrice = unitPriceRaw === null || unitPriceRaw === undefined ? 0 : Number(unitPriceRaw)
          cache.set(mid, Number.isFinite(unitPrice) ? unitPrice : 0)
        } catch {
          cache.set(mid, 0)
        }
      }
      node.unitCost = cache.get(mid) || 0
    }
    const children = Array.isArray(node.children) ? node.children : []
    for (const c of children) {
      await traverse(c)
    }
  }
  await traverse(root)
}

const loadBomByHeaderId = async (headerId: number) => {
  loading.value = true
  try {
    const headerRes: any = await bomApi.getBomHeaderDetail(headerId)
    const header = unwrapResponseData<any>(headerRes)
    if (!header?.id) {
      throw new Error('BOM版本不存在')
    }

    currentHeaderId.value = Number(header.id)
    currentMaterialId.value = header.materialId ? Number(header.materialId) : null

    bomInfo.value = {
      bomCode: header.bomCode || '',
      materialCode: header.materialCode || '',
      materialName: header.materialName || '',
      bomType: String(header.type ?? 1),
      version: header.version || '',
      status: String(header.status ?? 0),
      effectiveDate: normalizeToDate(header.effectiveDate),
      expireDate: normalizeToDate(header.expireDate)
    }

    const linesRes: any = await bomApi.getBomLinesByHeaderId(headerId)
    const lines = unwrapListResponse<any>(linesRes)

    const rootNode: any = {
      id: 'root',
      materialId: Number(header.materialId ?? 0),
      materialCode: header.materialCode || '',
      materialName: header.materialName || '',
      specification: '',
      unit: '件',
      quantity: 1,
      level: 1,
      type: 'root',
      unitCost: 0,
      totalCost: 0,
      children: [] as any[]
    }

    const nodeByMaterialId = new Map<number, any>()
    for (const line of lines) {
      const node: any = {
        id: line.id ?? `${Date.now()}${Math.random()}`,
        materialId: Number(line.childMaterialId ?? 0),
        materialCode: line.childMaterialCode || '',
        materialName: line.childMaterialName || '',
        specification: '',
        unit: line.unit || '件',
        quantity: Number(line.quantity ?? 0),
        scrapRate: Number(line.scrapRate ?? 0),
        remark: line.remark || '',
        level: Number(line.level ?? 1) + 1,
        type: 'sub',
        unitCost: 0,
        totalCost: 0,
        children: [] as any[]
      }
      if (node.materialId > 0) {
        nodeByMaterialId.set(node.materialId, node)
      }
    }

    for (const line of lines) {
      const childMaterialId = Number(line.childMaterialId ?? 0)
      const node = childMaterialId > 0 ? nodeByMaterialId.get(childMaterialId) : null
      if (!node) continue
      const parentMaterialId = line.parentMaterialId === null || line.parentMaterialId === undefined
        ? null
        : Number(line.parentMaterialId)
      const parentNode = parentMaterialId ? nodeByMaterialId.get(parentMaterialId) : rootNode
      if (!parentNode.children) parentNode.children = []
      parentNode.children.push(node)
    }

    if (Array.isArray(rootNode.children) && rootNode.children.length === 0) {
      for (const node of nodeByMaterialId.values()) {
        rootNode.children.push(node)
      }
    }

    await enrichUnitCostByMaterialId(rootNode)
    bomNodes.value = [rootNode]
    recalculateAllTotals()
  } catch (error: any) {
    currentHeaderId.value = null
    currentMaterialId.value = null
    bomNodes.value = []
    ElMessage.error(error?.message || '加载BOM数据失败')
  } finally {
    loading.value = false
  }
}

const loadBomData = async () => {
  const headerIdQuery = Number(route.query.headerId)
  if (Number.isFinite(headerIdQuery) && headerIdQuery > 0) {
    await loadBomByHeaderId(headerIdQuery)
    return
  }

  const materialCodeQuery = String(route.query.materialCode || '').trim()
  if (materialCodeQuery) {
    const materialId = await getMaterialIdByCode(materialCodeQuery)
    if (materialId) {
      const defRes: any = await bomApi.getDefaultBomByMaterialId(materialId)
      const headerId = unwrapResponseData<any>(defRes)?.id
      if (headerId) {
        await loadBomByHeaderId(Number(headerId))
        return
      }
    }
  }

  // 兜底：优先加载默认生效版本，其次任一生效版本，最后列表首条
  const versionsRes: any = await bomApi.getBomVersions({ page: 1, size: 100 })
  const list = unwrapPageResponse<any>(versionsRes).list
  const preferred = (Array.isArray(list) ? list : []).find((v: any) => v.isDefault && v.status === 1)
    || (Array.isArray(list) ? list : []).find((v: any) => v.status === 1)
    || (Array.isArray(list) && list.length > 0 ? list[0] : null)
  if (preferred?.id) {
    await loadBomByHeaderId(Number(preferred.id))
    return
  }

  bomNodes.value = []
}

// 选中的节点
const selectedNode = ref<any>(null)

// 复制的节点
const copiedNode = ref<any>(null)

// 加载状态
const loading = ref(false)

// 编辑弹窗相关
const dialogVisible = ref(false)
const editFormRef = ref<any>(null)
interface EditForm {
  materialCode: string
  materialName: string
  specification: string
  unit: string
  quantity: number
  scrapRate: number
  unitCost: number
  totalCost: number
  type: string
  remark: string
  level: number
  id?: number
}
const editForm = ref<EditForm>({
  materialCode: '',
  materialName: '',
  specification: '',
  unit: '',
  quantity: 1,
  scrapRate: 0,
  unitCost: 0,
  totalCost: 0,
  type: 'sub',
  remark: '',
  level: 2
})

// 表单验证规则
const editRules = ref({
  materialCode: [{ required: true, message: '请输入物料编码', trigger: 'blur' }],
  materialName: [{ required: true, message: '请输入物料名称', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入用量', trigger: 'blur' }],
  unitCost: [{ required: true, message: '请输入单位成本', trigger: 'blur' }]
})

// 递归计算物料总数
const calculateTotalMaterials = (nodes: any[]) => {
  return nodes.reduce((count, node) => {
    let total = 1
    if (node.children && node.children.length > 0) {
      total += calculateTotalMaterials(node.children)
    }
    return count + total
  }, 0)
}

// 递归计算单项成本（用于本地预览未保存的变更）
const calculateNodeTotalCost = (node: any) => {
  const selfCost = (node.unitCost || 0) * (node.quantity || 0)
  const childrenCost = node.children ? node.children.reduce((sum: number, child: any) => sum + calculateNodeTotalCost(child), 0) : 0
  return selfCost + childrenCost
}

// 递归计算最大深度
const calculateMaxDepth = (nodes: any[], currentDepth = 1) => {
  let maxDepth = currentDepth
  nodes.forEach(node => {
    if (node.children && node.children.length > 0) {
      const childDepth = calculateMaxDepth(node.children, currentDepth + 1)
      maxDepth = Math.max(maxDepth, childDepth)
    }
  })
  return maxDepth
}

// 计算BOM成本统计数据
const totalMaterials = computed(() => {
  let count = 0
  const traverse = (nodes: any[]) => {
    nodes.forEach(n => {
      count++
      if (n.children) traverse(n.children)
    })
  }
  traverse(bomNodes.value)
  return count
})

// 根节点数量
const rootNodeCount = computed(() => {
  return bomNodes.value.length
})

// 最大深度
const maxDepth = computed(() => {
  return calculateMaxDepth(bomNodes.value)
})

const totalCost = computed(() => {
  // 直接从根节点获取卷算后的总成本
  if (bomNodes.value && bomNodes.value.length > 0) {
    return (bomNodes.value[0] as any).totalCost || 0
  }
  return 0
})

const averageCost = computed(() => totalMaterials.value > 0 ? totalCost.value / totalMaterials.value : 0)

// 表格实例
const tableRef = ref<InstanceType<typeof ElTable> | null>(null)

// 展开所有节点
const expandAllNodes = () => {
  // 使用tree-props的expandRowKeys实现展开功能
  if (tableRef.value) {
    // 简化展开逻辑，使用el-table的expandRowKeys属性
    bomNodes.value.forEach(node => {
      if (node.id) {
        // 可以通过设置expandRowKeys来展开所有节点
        // 这里简化实现，直接遍历所有节点并展开
      }
    })
  }
}

// 收起所有节点
const collapseAllNodes = () => {
  // 使用tree-props的expandRowKeys实现收起功能
  if (tableRef.value) {
    // 简化收起逻辑，清空expandRowKeys
  }
}

// 处理行点击事件
const handleRowClick = (row: any) => {
  selectedNode.value = row
}

// 新增根节点
const handleAddRoot = () => {
  dialogVisible.value = true
  editForm.value = {
    materialCode: '',
    materialName: '',
    specification: '',
    unit: '',
    quantity: 1,
    scrapRate: 0,
    unitCost: 0,
    totalCost: 0,
    type: 'root',
    remark: '',
    level: 1
  }
}

// 新增子节点
const handleAddChild = () => {
  if (selectedNode.value) {
    dialogVisible.value = true
    editForm.value = {
      materialCode: '',
      materialName: '',
      specification: '',
      unit: '',
      quantity: 1,
      scrapRate: 0,
      unitCost: 0,
      totalCost: 0,
      type: 'sub',
      remark: '',
      level: selectedNode.value.level + 1
    }
  }
}

// 删除节点
const handleDeleteNode = () => {
  if (selectedNode.value) {
    ElMessageBox.confirm(
      `确定要删除物料 "${selectedNode.value.materialName}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }
    ).then(() => {
      const nodeInfo = findNode(bomNodes.value, selectedNode.value.id)
      if (nodeInfo) {
        if (nodeInfo.parent) {
          // 删除子节点
          nodeInfo.parent.children.splice(nodeInfo.index, 1)
          // 如果父节点没有子节点了，移除hasChildren标记
          if (nodeInfo.parent.children.length === 0) {
            delete nodeInfo.parent.children
            delete nodeInfo.parent.hasChildren
          }
        } else {
          // 删除根节点
          bomNodes.value.splice(nodeInfo.index, 1)
        }
        selectedNode.value = null
        recalculateAllTotals()
        ElMessage.success('节点删除成功')
      }
    }).catch(() => {
      // 取消删除
    })
  }
}

// 编辑节点
const handleEditNode = () => {
  if (selectedNode.value) {
    editForm.value = { ...selectedNode.value }
    dialogVisible.value = true
  }
}

// 保存节点
const handleSaveNode = () => {
  if (!editFormRef.value) return
  editFormRef.value.validate((valid: boolean) => {
    if (valid) {
      // 计算总成本
      editForm.value.totalCost = editForm.value.quantity * editForm.value.unitCost
      
      if (editForm.value.id) {
        // 更新现有节点
        const nodeInfo = findNode(bomNodes.value, editForm.value.id)
        if (nodeInfo) {
          Object.assign(nodeInfo.node, editForm.value)
          recalculateAllTotals()
          ElMessage.success('节点更新成功')
        }
      } else {
        // 添加新节点
        const newNode: BomNode = {
          id: Date.now(), // 生成唯一ID
          materialId: 0,
          materialCode: editForm.value.materialCode,
          materialName: editForm.value.materialName,
          specification: editForm.value.specification,
          unit: editForm.value.unit,
          quantity: editForm.value.quantity,
          level: editForm.value.level,
          type: editForm.value.type as 'sub' | 'root' | 'virtual',
          scrapRate: editForm.value.scrapRate,
          remark: editForm.value.remark,
          unitCost: editForm.value.unitCost,
          totalCost: editForm.value.totalCost
        }
        
        if (newNode.type === 'root') {
          if (bomNodes.value.length > 0) {
            ElMessage.warning('当前编辑器仅支持一个根节点')
          } else {
            bomNodes.value.push(newNode)
            recalculateAllTotals()
          }
        } else {
          // 添加子节点
          if (selectedNode.value) {
            if (!selectedNode.value.children) {
              selectedNode.value.children = []
              selectedNode.value.hasChildren = true
            }
            selectedNode.value.children.push(newNode)
            recalculateAllTotals()
          }
        }
        ElMessage.success('节点添加成功')
      }
      
      dialogVisible.value = false
      selectedNode.value = null
    }
  })
}

// 复制节点
const handleCopyNode = () => {
  if (selectedNode.value) {
    // 深拷贝节点数据，确保数据完整性
    copiedNode.value = JSON.parse(JSON.stringify(selectedNode.value))
    
    // 显示复制成功提示
    ElMessage.success({
      message: `已复制物料 "${selectedNode.value.materialName}"`,
      type: 'success',
      duration: 1500
    })
    
    console.log('复制节点:', copiedNode.value)
  }
}

// 粘贴节点
const handlePasteNode = () => {
  if (copiedNode.value && selectedNode.value) {
    // 递归更新子节点ID，确保整个树的ID唯一性
    const updateNodeIds = (node: any, parentId: any) => {
      // 生成唯一ID
      const newId = Date.now() + Math.floor(Math.random() * 1000)
      
      const updatedNode = {
        ...node,
        id: newId,
        type: 'sub' // 粘贴的节点默认为子节点
      }
      
      // 更新子节点ID
      if (updatedNode.children && updatedNode.children.length > 0) {
        updatedNode.children = updatedNode.children.map((child: any) => {
          return updateNodeIds(child, updatedNode.id)
        })
      }
      
      return updatedNode
    }
    
    // 创建新节点，更新ID和类型
    const newNode = updateNodeIds(copiedNode.value, selectedNode.value.id)
    
    // 添加到选中节点的children中
    if (!selectedNode.value.children) {
      selectedNode.value.children = []
      selectedNode.value.hasChildren = true
    }
    selectedNode.value.children.push(newNode)
    
    // 显示粘贴成功提示
    ElMessage.success({
      message: `已粘贴物料 "${newNode.materialName}"`,
      type: 'success',
      duration: 1500
    })
    
    console.log('粘贴节点:', newNode)
  }
}

// 激活BOM
const handleActivateBom = async () => {
  ElMessageBox.confirm(
    '确定要激活当前BOM版本吗？激活后将自动失效其他同名BOM的生效版本。',
    '激活确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    }
  ).then(async () => {
    loadingState.value = true
    try {
      if (!currentHeaderId.value) {
        throw new Error('请先加载或保存一个BOM版本')
      }
      await bomApi.activateVersion(currentHeaderId.value)
      bomInfo.value.status = '1'
      bomInfo.value.effectiveDate = new Date().toISOString().split('T')[0] || ''
      bomInfo.value.expireDate = ''
      
      ElMessage.success({
        message: 'BOM版本已成功激活！',
        type: 'success',
        duration: 2000
      })
    } catch (error) {
      ElMessage.error(error instanceof Error ? error.message : '激活BOM失败')
    } finally {
      loadingState.value = false
    }
  }).catch(() => {
    // 取消激活
    loadingState.value = false
  })
}

// 失效BOM
const handleInactivateBom = async () => {
  ElMessageBox.confirm(
    '确定要失效当前BOM版本吗？失效后将无法恢复，需重新激活。',
    '失效确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    loadingState.value = true
    try {
      if (!currentHeaderId.value) {
        throw new Error('请先加载或保存一个BOM版本')
      }
      await bomApi.deactivateVersion(currentHeaderId.value)
      bomInfo.value.status = '2'
      bomInfo.value.expireDate = new Date().toISOString().split('T')[0] || ''
      
      ElMessage.success({
        message: 'BOM版本已成功失效！',
        type: 'success',
        duration: 2000
      })
    } catch (error) {
      ElMessage.error(error instanceof Error ? error.message : '失效BOM失败')
    } finally {
      loadingState.value = false
    }
  }).catch(() => {
    // 取消失效
    loadingState.value = false
  })
}

// 保存BOM
const handleSaveBom = async () => {
  try {
    loadingState.value = true

    const root = Array.isArray(bomNodes.value) && bomNodes.value.length > 0 ? (bomNodes.value[0] as any) : null
    if (!root) {
      throw new Error('请先加载或创建BOM结构')
    }

    if (!bomInfo.value.bomCode) {
      throw new Error('请填写BOM编码')
    }
    if (!bomInfo.value.materialCode) {
      throw new Error('请填写产品代码')
    }
    if (!bomInfo.value.materialName) {
      throw new Error('请填写产品名称')
    }

    const codeCache = new Map<string, number>()
    const ensureMaterialId = async (node: any) => {
      const code = String(node?.materialCode || '').trim()
      const mid = Number(node?.materialId ?? 0)
      if (mid > 0) return mid
      if (!code) {
        throw new Error('存在未填写物料编码的节点')
      }
      if (codeCache.has(code)) {
        node.materialId = codeCache.get(code)
        return node.materialId
      }
      const resolved = await getMaterialIdByCode(code)
      if (!resolved) {
        throw new Error(`物料不存在：${code}`)
      }
      codeCache.set(code, resolved)
      node.materialId = resolved
      return resolved
    }

    const rootMaterialId = currentMaterialId.value || (await getMaterialIdByCode(bomInfo.value.materialCode))
    if (!rootMaterialId) {
      throw new Error(`产品物料不存在：${bomInfo.value.materialCode}`)
    }
    currentMaterialId.value = rootMaterialId
    root.materialId = rootMaterialId

    const buildLines: any[] = []
    const traverseLines = async (parentNode: any, children: any[], depth: number) => {
      const list = Array.isArray(children) ? children : []
      for (let i = 0; i < list.length; i++) {
        const child = list[i]
        const childId = await ensureMaterialId(child)
        buildLines.push({
          parentMaterialId: Number(parentNode.materialId),
          childMaterialId: childId,
          childMaterialCode: String(child.materialCode || ''),
          childMaterialName: String(child.materialName || ''),
          quantity: Number(child.quantity ?? 0),
          unit: String(child.unit || '件'),
          scrapRate: Number(child.scrapRate ?? 0),
          level: depth,
          sequence: i + 1,
          usageType: 'normal',
          remark: String(child.remark || '')
        })
        await traverseLines(child, child.children, depth + 1)
      }
    }

    await traverseLines(root, root.children, 1)

    const headerPayload: any = {
      materialId: rootMaterialId,
      materialCode: bomInfo.value.materialCode,
      materialName: bomInfo.value.materialName,
      bomCode: bomInfo.value.bomCode,
      version: bomInfo.value.version,
      type: Number(bomInfo.value.bomType || 1),
      status: Number(bomInfo.value.status || 0),
      effectiveDate: toDateTimeString(bomInfo.value.effectiveDate),
      expireDate: toDateTimeString(bomInfo.value.expireDate),
      remark: ''
    }

    let savedHeaderId: number
    if (currentHeaderId.value) {
      const res: any = await bomApi.updateBomHeader(currentHeaderId.value, headerPayload)
      savedHeaderId = Number(unwrapResponseData<any>(res)?.id || currentHeaderId.value)
    } else {
      const res: any = await bomApi.createBomHeader(headerPayload)
      savedHeaderId = Number(unwrapResponseData<any>(res)?.id)
      if (!savedHeaderId) {
        throw new Error('创建BOM版本失败')
      }
    }

    currentHeaderId.value = savedHeaderId
    await bomApi.replaceBomLines(savedHeaderId, buildLines)
    await loadBomByHeaderId(savedHeaderId)
    ElMessage.success(`BOM版本 "${bomInfo.value.version}" 保存成功！`)
  } catch (error: any) {
    ElMessage.error(error?.message || '保存BOM失败')
  } finally {
    loadingState.value = false
  }
}

// 导出BOM
const handleExport = () => {
  // 转换为CSV格式
  let csvContent = "层级,物料编码,物料名称,规格型号,单位,用量,单位成本,总成本\n";
  const traverseToCsv = (nodes: any[], level: number) => {
    nodes.forEach(node => {
      csvContent += `${level},${node.materialCode || ''},${node.materialName || ''},${node.specification || ''},${node.unit || ''},${node.quantity || 0},${node.unitCost || 0},${node.totalCost || 0}\n`;
      if (node.children) traverseToCsv(node.children, level + 1);
    });
  };
  traverseToCsv(bomNodes.value, 1);

  // BOM带BOM头信息导出
  const blob = new Blob(["\ufeff" + csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.setAttribute("href", url);
  link.setAttribute("download", `BOM_Export_${bomInfo.value.bomCode}_${new Date().getTime()}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

// 导入BOM
const handleImport = (file: File) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const importData = JSON.parse(e.target?.result as string)
      
      // 验证导入数据格式
      if (!importData.bomInfo || !importData.bomNodes) {
        throw new Error('导入数据格式不正确，缺少必要字段')
      }
      
      // 更新BOM信息和节点数据
      bomInfo.value = importData.bomInfo
      bomNodes.value = importData.bomNodes
      
      // 显示导入成功提示
      ElMessage.success({
        message: `BOM数据导入成功！共导入 ${totalMaterials.value} 个节点。`,
        type: 'success',
        duration: 2000
      })
      
      console.log('导入BOM成功')
    } catch (error) {
      console.error('导入BOM失败:', error)
      
      // 显示导入失败提示
      ElMessage.error({
        message: `BOM数据导入失败：${error instanceof Error ? error.message : '未知错误'}`,
        type: 'error',
        duration: 3000
      })
    }
  }
  reader.readAsText(file)
  return false // 阻止默认上传行为
}

// 键盘快捷键处理
const handleKeyDown = (event: KeyboardEvent) => {
  // Ctrl + N: 新增根节点
  if (event.ctrlKey && event.key === 'n') {
    event.preventDefault()
    handleAddRoot()
  }
  
  // Ctrl + Shift + N: 新增子节点
  if (event.ctrlKey && event.shiftKey && event.key === 'n') {
    event.preventDefault()
    handleAddChild()
  }
  
  // Delete: 删除节点
  if (event.key === 'Delete') {
    event.preventDefault()
    handleDeleteNode()
  }
  
  // Enter: 编辑节点
  if (event.key === 'Enter') {
    event.preventDefault()
    handleEditNode()
  }
  
  // Ctrl + C: 复制节点
  if (event.ctrlKey && event.key === 'c') {
    event.preventDefault()
    handleCopyNode()
  }
  
  // Ctrl + V: 粘贴节点
  if (event.ctrlKey && event.key === 'v') {
    event.preventDefault()
    handlePasteNode()
  }
  
  // Ctrl + S: 保存BOM
  if (event.ctrlKey && event.key === 's') {
    event.preventDefault()
    handleSaveBom()
  }
}

// 初始化
onMounted(() => {
  window.addEventListener('keydown', handleKeyDown)
  loadBomData()
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})
</script>

<style scoped>
/* 全局样式重置 */
:deep(.el-button) {
  margin-right: 8px;
}

.header-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn-subtle {
  background: rgba(0,0,0,0.03);
  border: none;
  color: var(--text-regular);
  transition: all 0.3s;
}

.status-actions {
  display: flex;
  gap: 10px;
  margin-top: 5px;
}

.status-actions .el-button {
  min-width: 80px;
}

.status-display {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-dates {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 4px;
}

.date-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary);
  background: rgba(0, 0, 0, 0.02);
  padding: 4px 12px;
  border-radius: 12px;
  border: 1px solid var(--border-color-light);
}

.date-item .el-icon {
  font-size: 12px;
  color: var(--primary-color);
}

.btn-subtle:hover {
  background: rgba(0,0,0,0.06);
  transform: translateY(-1px);
}

.btn-glow {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s;
}

.btn-glow:hover {
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.5);
  transform: translateY(-2px);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: var(--border-radius-lg);
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  transition: all 0.3s;
  border: 1px solid var(--border-color-light);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.stat-card.primary {
  background: white;
  color: var(--text-primary);
  border-left: 4px solid var(--primary-color);
  border-color: var(--border-color-light);
}

/* 统一所有统计卡片的图标样式 */
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: var(--primary-color);
  transition: all 0.3s;
}

/* 悬停时图标效果 */
.stat-card:hover .stat-icon {
  background: var(--primary-color);
  color: white;
}

/* 主要卡片的图标保持固定样式 */
.stat-card.primary .stat-icon {
  background: var(--primary-color);
  color: white;
}

.stat-card.primary:hover .stat-icon {
  background: var(--primary-color-dark);
}

.stat-main {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.stat-main .label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  font-weight: 500;
}

.stat-main .value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

/* 确保所有统计卡片的数值颜色一致 */
.stat-card.primary .value {
  color: var(--primary-color);
}

/* 添加数据可视化效果 - 数值脉动动画 */
@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.stat-main .value {
  animation: pulse 2s ease-in-out infinite;
}

/* BOM定义区样式优化 */
.bom-header-card {
  padding: 24px;
  margin-bottom: 24px;
  background: white;
  border-radius: var(--border-radius-lg);
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

.bom-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.bom-form :deep(.el-input__inner),
.bom-form :deep(.el-select__wrapper) {
  border-radius: var(--border-radius-md);
  transition: all 0.3s;
}

.bom-form :deep(.el-input__inner:focus),
.bom-form :deep(.el-select__wrapper.is-focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 操作桥样式优化 */
.editor-bridge {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 24px;
  background: white;
  border-radius: var(--border-radius-lg);
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  transition: all 0.3s;
  border: 1px solid var(--border-color-light);
}

.editor-bridge:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.btn-group-premium :deep(.el-button) {
  border-radius: var(--border-radius-md) !important;
  padding: 12px 24px;
  margin-right: 12px;
  transition: all 0.3s;
  font-weight: 500;
}

.bridge-right :deep(.el-button) {
  border-radius: var(--border-radius-md) !important;
  margin-right: 8px;
  padding: 10px 16px;
  transition: all 0.3s;
}

.bridge-right :deep(.el-button:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.bridge-right :deep(.el-button:last-child) {
  margin-right: 0;
}

/* 垂直分隔线样式优化 */
.bridge-right :deep(.el-divider--vertical) {
  margin: 0 8px;
  background-color: var(--border-color-light);
  width: 1px;
}

/* 结构树样式优化 */
.structure-tree {
  padding: 24px;
  outline: none;
  background: white;
  border-radius: var(--border-radius-lg);
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border: 1px solid var(--border-color-light);
  transition: all 0.3s;
}

.structure-tree:focus-within {
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.15);
  border-color: var(--primary-color-light);
}

/* 树头部样式 */
.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--border-color-light);
  background: #fafafa;
  padding: 16px;
  border-radius: var(--border-radius-md);
  gap: 20px;
}

.tree-stats {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.tree-stats .stat-item {
  display: flex;
  align-items: center;
}

/* 优化标签样式 */
.tree-stats :deep(.el-tag) {
  border-radius: 16px;
  padding: 6px 12px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s;
}

.tree-stats :deep(.el-tag:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.tree-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.tree-actions :deep(.el-button) {
  border-radius: var(--border-radius-md) !important;
  padding: 10px 16px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s;
}

.tree-actions :deep(.el-button:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

/* 表格样式优化 */
:deep(.el-table) {
  background: transparent !important;
  border: none !important;
  border-radius: var(--border-radius-md) !important;
  overflow: hidden;
}

/* 表格容器样式 */
:deep(.el-table__inner-wrapper) {
  border-radius: var(--border-radius-md) !important;
  overflow: hidden;
}

:deep(.el-table__header-wrapper) {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%) !important;
  border-radius: var(--border-radius-md) var(--border-radius-md) 0 0 !important;
  overflow: hidden;
}

:deep(.el-table__header th) {
  background: transparent !important;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 2px solid var(--primary-color) !important;
  padding: 14px 16px !important;
  font-size: 14px;
  text-align: center;
}

:deep(.el-table__header th:first-child) {
  border-radius: var(--border-radius-md) 0 0 0 !important;
}

:deep(.el-table__header th:last-child) {
  border-radius: 0 var(--border-radius-md) 0 0 !important;
}

:deep(.el-table__body-wrapper) {
  border-radius: 0 0 var(--border-radius-md) var(--border-radius-md) !important;
  overflow: hidden;
  background: white;
}

/* 行样式优化 */
:deep(.el-table__row) {
  transition: all 0.3s ease;
  border-bottom: 1px solid var(--border-color-light);
}

:deep(.el-table__row:hover) {
  background-color: rgba(64, 158, 255, 0.08) !important;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

:deep(.el-table__row.current-row) {
  background-color: rgba(64, 158, 255, 0.12) !important;
  border-left: 4px solid var(--primary-color);
  font-weight: 500;
}

:deep(.el-table__row:last-child) {
  border-bottom: none;
}

/* 单元格样式优化 */
:deep(.el-table__cell) {
  padding: 14px 16px !important;
  font-size: 14px;
  border-bottom: 1px solid var(--border-color-light);
}

:deep(.el-table__cell:first-child) {
  padding-left: 20px !important;
}

:deep(.el-table__cell:last-child) {
  padding-right: 20px !important;
}

/* 树形引导线模拟 */
:deep(.el-table__indent) {
  padding-left: 24px !important;
}

/* 优化树形图标 */
:deep(.el-icon-arrow-right) {
  color: var(--text-secondary);
  transition: all 0.3s;
}

:deep(.el-table__expand-icon--expanded) .el-icon-arrow-right {
  transform: rotate(90deg);
  color: var(--primary-color);
}

/* 优化表格滚动条 */
:deep(.el-table__body-wrapper::-webkit-scrollbar) {
  width: 8px;
  height: 8px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-track) {
  background: #f1f5f9;
  border-radius: 4px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-thumb) {
  background: #cbd5e1;
  border-radius: 4px;
  transition: all 0.3s;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-thumb:hover) {
  background: #94a3b8;
}

/* 占位符样式优化 */
:deep(.el-table__placeholder) {
  width: 24px !important;
  padding: 40px 0 !important;
  color: var(--text-secondary);
  font-size: 14px;
}

/* 物料编码样式 */
.code-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.code-text {
  font-family: 'JetBrains Mono', monospace;
  font-weight: 600;
  color: var(--primary-color);
  font-size: 14px;
}

.sub-icon {
  color: var(--warning-color);
  font-size: 14px;
  cursor: help;
}

/* 数量和金额样式 */
.qty-text {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.currency {
  color: var(--text-regular);
  font-size: 14px;
}

.total-currency {
  color: var(--danger-color);
  font-weight: 600;
  font-size: 14px;
}

/* 动画效果 */
.animate-spin {
  animation: spin 3s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .editor-bridge {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .btn-group-premium {
    width: 100%;
    justify-content: center;
  }
  
  .bridge-right {
    width: 100%;
    display: flex;
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .tree-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .tree-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
