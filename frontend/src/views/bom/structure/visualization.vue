<template>
  <div class="bom-visualization">
    <!-- BOM结构可视化 -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><DataAnalysis /></el-icon>
          <span>BOM结构可视化</span>
          <div class="header-actions">
            <!-- BOM版本选择器 -->
            <el-select
              v-model="selectedHeaderId"
              placeholder="选择BOM版本"
              style="width: 280px"
              @change="handleBomChange"
            >
              <el-option
                v-for="item in bomOptions"
                :key="item.id"
                :label="item.label"
                :value="item.id"
              />
            </el-select>
            <el-button @click="handleZoomIn" title="放大">
              <el-icon><Plus /></el-icon>
            </el-button>
            <el-button @click="handleZoomOut" title="缩小">
              <el-icon><Minus /></el-icon>
            </el-button>
            <el-button @click="handleZoomFit" title="适应画布">
              <el-icon><FullScreen /></el-icon>
            </el-button>
            <el-button @click="handleReset" title="重置视图">
              <el-icon><RefreshRight /></el-icon>
            </el-button>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索物料编码或名称"
              prefix-icon="Search"
              clearable
              @keyup.enter="handleSearch"
              style="width: 200px"
            />
          </div>
        </div>
      </template>

      <!-- 可视化容器 -->
      <div class="visualization-container" v-loading="loading">
        <div class="g6-container" ref="g6Container"></div>
        <el-empty v-if="!loading && isEmpty" description="当前BOM暂无结构数据" class="empty-tip" />
      </div>

      <!-- 节点详情面板 -->
      <el-drawer
        v-model="detailDrawerVisible"
        title="节点详情"
        direction="rtl"
        size="30%"
      >
        <div v-if="selectedNode" class="node-detail">
          <h4>{{ selectedNode.materialName }}</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="物料编码">{{ selectedNode.materialCode }}</el-descriptions-item>
            <el-descriptions-item label="规格型号">{{ selectedNode.spec || '-' }}</el-descriptions-item>
            <el-descriptions-item label="单位">{{ selectedNode.unit || '-' }}</el-descriptions-item>
            <el-descriptions-item label="用量">{{ selectedNode.usageQty }}</el-descriptions-item>
            <el-descriptions-item label="损耗率">{{ selectedNode.scrapRate || 0 }}%</el-descriptions-item>
            <el-descriptions-item label="单位成本">¥{{ (selectedNode.unitCost || 0).toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="总成本">¥{{ (selectedNode.totalCost || 0).toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="版本">{{ selectedNode.version }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="selectedNode.status === 1 ? 'success' : selectedNode.status === 0 ? 'info' : 'danger'">
                {{ selectedNode.status === 1 ? '生效' : selectedNode.status === 0 ? '草稿' : '失效' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-drawer>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { DataAnalysis, Plus, Minus, FullScreen, RefreshRight, Search } from '@element-plus/icons-vue'
import { Graph } from '@antv/g6'
import { ElMessage } from 'element-plus'
import { unwrapPageResponse, unwrapResponseData } from '@/api'
import { bomApi } from '@/api/bom'

// 容器引用
const g6Container = ref<HTMLElement | null>(null)
// G6 实例
let graph: Graph | null = null
// 加载状态
const loading = ref(false)
// 当前BOM是否无结构数据
const isEmpty = ref(false)

// 搜索关键词
const searchKeyword = ref('')
// 节点详情抽屉
const detailDrawerVisible = ref(false)
// 选中的节点
const selectedNode = ref<any>(null)

// BOM版本选择器
const selectedHeaderId = ref<number | null>(null)
const bomOptions = ref<{ id: number; label: string; materialId: number; version: string }[]>([])

// 当前图数据（扁平 nodes/edges，供重绘与高亮使用）
let currentGraphData: { nodes: any[]; edges: any[] } = { nodes: [], edges: [] }

/**
 * 加载BOM版本列表，填充选择器。
 */
const loadBomOptions = async () => {
  try {
    const res: any = await bomApi.getBomVersions({ page: 1, size: 200 })
    const list = unwrapPageResponse<any>(res).list
    bomOptions.value = (Array.isArray(list) ? list : []).map((h: any) => ({
      id: Number(h.id),
      label: `${h.materialName || h.materialCode || ''} - ${h.bomCode || ''} (${h.version || ''})`,
      materialId: Number(h.materialId),
      version: String(h.version || '')
    }))
    // 默认选中第一个BOM版本
    const firstOption = bomOptions.value[0]
    if (firstOption && !selectedHeaderId.value) {
      selectedHeaderId.value = firstOption.id
      await handleBomChange()
    } else if (bomOptions.value.length === 0) {
      isEmpty.value = true
    }
  } catch (error) {
    console.error('加载BOM版本列表失败:', error)
    ElMessage.error('加载BOM版本列表失败')
  }
}

/**
 * 递归将后端BOM树节点转换为扁平的G6节点与边。
 * @param treeNodes 后端返回的nodes数组（元素结构为 {line, children}）
 * @param parentId 父节点ID
 * @param nodes 收集节点的数组
 * @param edges 收集边的数组
 * @param version 当前BOM版本号
 * @param status 当前BOM状态
 * @param priceMap 物料单价缓存
 */
const flattenTreeNodes = (
  treeNodes: any[],
  parentId: string,
  nodes: any[],
  edges: any[],
  version: string,
  status: number,
  priceMap: Map<number, number>
) => {
  treeNodes.forEach((item: any, index: number) => {
    const line = item?.line || {}
    const nodeId = `${parentId}-${line.id ?? index}`
    const unitCost = priceMap.get(Number(line.childMaterialId)) || 0
    const qty = Number(line.quantity ?? 0)
    nodes.push({
      id: nodeId,
      data: {
        materialCode: line.childMaterialCode || '',
        materialName: line.childMaterialName || '',
        spec: '',
        unit: line.unit || '',
        usageQty: qty,
        scrapRate: Number(line.scrapRate ?? 0),
        unitCost,
        totalCost: unitCost * qty,
        version,
        status,
        nodeType: 'sub'
      },
      style: {
        fill: '#67c23a'
      }
    })
    edges.push({ source: parentId, target: nodeId })
    if (Array.isArray(item?.children) && item.children.length > 0) {
      flattenTreeNodes(item.children, nodeId, nodes, edges, version, status, priceMap)
    }
  })
}

/**
 * 加载选中BOM的结构树并渲染图。
 */
const handleBomChange = async () => {
  const headerId = Number(selectedHeaderId.value)
  if (!headerId) return

  loading.value = true
  isEmpty.value = false
  try {
    // 获取BOM头详情，拿到物料ID与版本
    const headerRes: any = await bomApi.getBomVersionDetail(headerId)
    const header = unwrapResponseData<any>(headerRes)
    if (!header?.materialId) {
      isEmpty.value = true
      return
    }

    // 获取BOM树
    const treeRes: any = await bomApi.getBomTree(Number(header.materialId), header.version || undefined)
    const tree = unwrapResponseData<any>(treeRes)
    const treeNodes = Array.isArray(tree?.nodes) ? tree.nodes : []

    // 收集子项物料单价
    const materialIds = new Set<number>()
    const collectIds = (items: any[]) => {
      items.forEach((item: any) => {
        if (item?.line?.childMaterialId) materialIds.add(Number(item.line.childMaterialId))
        if (Array.isArray(item?.children)) collectIds(item.children)
      })
    }
    collectIds(treeNodes)

    const priceMap = new Map<number, number>()
    await Promise.all(
      Array.from(materialIds).map(async (mid) => {
        try {
          const res: any = await bomApi.getMaterialDetail(mid)
          const price = Number(unwrapResponseData<any>(res)?.unitPrice ?? 0)
          priceMap.set(mid, Number.isFinite(price) ? price : 0)
        } catch {
          priceMap.set(mid, 0)
        }
      })
    )

    // 根节点为产品本身
    const rootId = 'root'
    const nodes: any[] = [
      {
        id: rootId,
        data: {
          materialCode: header.materialCode || '',
          materialName: header.materialName || '',
          spec: '',
          unit: '',
          usageQty: 1,
          scrapRate: 0,
          unitCost: 0,
          totalCost: 0,
          version: header.version || '',
          status: header.status ?? 1,
          nodeType: 'root'
        },
        style: {
          fill: '#409eff'
        }
      }
    ]
    const edges: any[] = []
    flattenTreeNodes(treeNodes, rootId, nodes, edges, header.version || '', header.status ?? 1, priceMap)

    currentGraphData = { nodes, edges }
    isEmpty.value = treeNodes.length === 0
    renderGraph()
  } catch (error) {
    console.error('加载BOM结构失败:', error)
    ElMessage.error('加载BOM结构失败')
    isEmpty.value = true
  } finally {
    loading.value = false
  }
}

/**
 * 初始化/重绘G6树图（G6 v5 API）。
 */
const renderGraph = () => {
  if (!g6Container.value) return

  if (graph) {
    graph.destroy()
    graph = null
  }

  graph = new Graph({
    container: g6Container.value,
    autoResize: true,
    autoFit: 'view',
    data: currentGraphData,
    node: {
      type: 'rect',
      style: {
        size: [140, 52],
        radius: 8,
        stroke: '#fff',
        lineWidth: 2,
        labelText: (d: any) => `${d?.data?.materialName || ''}\n${d?.data?.materialCode || ''}`,
        labelFill: '#fff',
        labelFontSize: 12,
        labelFontWeight: 600,
        labelTextAlign: 'center',
        labelTextBaseline: 'middle',
        labelDy: -16,
        shadowColor: 'rgba(0, 0, 0, 0.15)',
        shadowBlur: 10
      },
      state: {
        highlight: {
          fill: '#f56c6c'
        }
      }
    },
    edge: {
      type: 'polyline',
      style: {
        stroke: '#aaa',
        lineWidth: 2,
        radius: 10,
        offset: 20,
        endArrow: true
      }
    },
    layout: {
      type: 'compact-box',
      direction: 'TB',
      getWidth: () => 140,
      getHeight: () => 52,
      getVGap: () => 60,
      getHGap: () => 24
    },
    behaviors: ['drag-canvas', 'zoom-canvas', 'drag-element']
  })

  // 监听节点点击事件，展示节点详情
  graph.on('node:click', (e: any) => {
    const nodeId = e?.target?.id
    if (!nodeId || !graph) return
    const nodeData: any = graph.getNodeData(nodeId)
    selectedNode.value = nodeData?.data || null
    detailDrawerVisible.value = !!selectedNode.value
  })

  // 监听画布点击事件，取消选中
  graph.on('canvas:click', () => {
    selectedNode.value = null
  })

  // 渲染完成后显式自适应画布，确保整棵树在视口内可见
  graph.render().then(() => {
    graph?.fitView()
  })
}

// 放大
const handleZoomIn = () => {
  graph?.zoomBy(1.1)
}

// 缩小
const handleZoomOut = () => {
  graph?.zoomBy(0.9)
}

// 适应画布
const handleZoomFit = () => {
  graph?.fitView()
}

/**
 * 重置视图：适应画布并清除搜索高亮。
 */
const handleReset = async () => {
  if (!graph) return
  // 清除所有节点高亮状态（G6 v5 setElementState 批量形式需传 Record<节点ID, 状态数组>）
  const ids = currentGraphData.nodes.map((n: any) => n.id)
  if (ids.length > 0) {
    await graph.setElementState(Object.fromEntries(ids.map((id: string) => [id, []])))
  }
  await graph.fitView()
  selectedNode.value = null
  searchKeyword.value = ''
}

/**
 * 搜索节点：高亮匹配节点并定位到第一个匹配项。
 */
const handleSearch = async () => {
  if (!graph || !searchKeyword.value) return

  const keyword = searchKeyword.value.toLowerCase()
  const matched: string[] = []
  const unmatched: string[] = []
  currentGraphData.nodes.forEach((n: any) => {
    const code = (n.data.materialCode || '').toLowerCase()
    const name = (n.data.materialName || '').toLowerCase()
    if (code.includes(keyword) || name.includes(keyword)) {
      matched.push(n.id)
    } else {
      unmatched.push(n.id)
    }
  })

  // 重置高亮后，高亮匹配节点（批量设置同样使用 Record 形式）
  if (unmatched.length > 0) {
    await graph.setElementState(Object.fromEntries(unmatched.map(id => [id, []])))
  }
  const firstMatched = matched[0]
  if (matched.length > 0 && firstMatched) {
    await graph.setElementState(Object.fromEntries(matched.map(id => [id, ['highlight']])))
    await graph.focusElement(firstMatched)
  } else if (matched.length === 0) {
    ElMessage.warning('未找到匹配的物料节点')
  }
}

// 监听搜索关键词变化，清空时重置视图
watch(searchKeyword, (newVal) => {
  if (!newVal) {
    handleReset()
  }
})

// 初始化
onMounted(() => {
  loadBomOptions()
})

// 销毁图实例，避免内存泄漏
onBeforeUnmount(() => {
  if (graph) {
    graph.destroy()
    graph = null
  }
})
</script>

<style scoped>
.bom-visualization {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: #f5f7fa;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: bold;
  font-size: 1.2rem;
  gap: 10px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.visualization-container {
  position: relative;
  margin-top: 20px;
  height: calc(100vh - 340px);
  min-height: 420px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
}

.g6-container {
  width: 100%;
  height: 100%;
}

.empty-tip {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.node-detail h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 1.1rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .bom-visualization {
    padding: 12px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .visualization-container {
    height: calc(100vh - 250px);
  }
}
</style>
