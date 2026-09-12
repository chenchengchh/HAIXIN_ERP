<template>
  <div class="bom-editor-view">
    <h3>BOM编辑器</h3>
    
    <!-- BOM信息与操作 -->
    <div class="bom-header">
      <el-card shadow="hover">
        <div class="header-content">
          <div class="bom-info">
            <el-form :model="bomInfo" inline>
              <el-form-item label="BOM编码">
                <el-input v-model="bomInfo.bomCode" placeholder="请输入BOM编码" disabled></el-input>
              </el-form-item>
              <el-form-item label="物料编码">
                <el-select v-model="bomInfo.itemCode" placeholder="请选择物料">
                  <el-option
                    v-for="item in items"
                    :key="item.itemCode"
                    :label="item.itemCode + ' - ' + item.name"
                    :value="item.itemCode"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="版本">
                <el-input v-model="bomInfo.version" placeholder="请输入版本号"></el-input>
              </el-form-item>
              <el-form-item label="生效日期">
                <el-date-picker v-model="bomInfo.effectiveDate" type="date" placeholder="请选择生效日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD"></el-date-picker>
              </el-form-item>
            </el-form>
          </div>
          
          <div class="bom-actions">
            <el-button type="primary" @click="saveBom">保存BOM</el-button>
            <el-button type="success" @click="releaseBom">发布BOM</el-button>
            <el-button @click="checkoutBom">检出BOM</el-button>
            <el-button @click="checkinBom">检入BOM</el-button>
            <el-button @click="compareVersions">版本对比</el-button>
            <el-dropdown>
              <el-button>
                更多操作 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="importBom">导入BOM</el-dropdown-item>
                  <el-dropdown-item @click="exportBom">导出BOM</el-dropdown-item>
                  <el-dropdown-item @click="copyBom">复制BOM</el-dropdown-item>
                  <el-dropdown-item @click="deleteBom">删除BOM</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- BOM编辑区域 -->
    <div class="bom-editor-content">
      <!-- 左侧BOM树结构 -->
      <div class="bom-tree-panel">
        <el-card shadow="hover" class="panel-card">
          <template #header>
            <div class="panel-header">
              <span>BOM树结构</span>
              <div class="tree-actions">
                <el-button size="small" @click="addSubItem">添加子件</el-button>
                <el-button size="small" @click="deleteItem">删除子件</el-button>
                <el-button size="small" @click="moveItemUp">上移</el-button>
                <el-button size="small" @click="moveItemDown">下移</el-button>
              </div>
            </div>
          </template>
          <div class="bom-tree">
            <el-tree
              ref="bomTree"
              :data="bomTreeData"
              :props="treeProps"
              node-key="id"
              default-expand-all
              @node-click="handleNodeClick"
              @node-drag-start="handleNodeDragStart"
              @node-drag-enter="handleNodeDragEnter"
              @node-drag-leave="handleNodeDragLeave"
              @node-drag-over="handleNodeDragOver"
              @node-drag-end="handleNodeDragEnd"
              @node-drop="handleNodeDrop"
              draggable
              :allow-drop="allowDrop"
              :allow-drag="allowDrag"
            >
              <template #default="{ node, data }">
                <div class="tree-node-content">
                  <span>{{ data.name }}</span>
                  <span class="node-info">
                    ({{ data.itemCode }} - {{ data.quantity }} {{ data.unit }})
                  </span>
                </div>
              </template>
            </el-tree>
          </div>
        </el-card>
      </div>
      
      <!-- 右侧属性编辑 -->
      <div class="bom-properties-panel">
        <el-card shadow="hover" class="panel-card">
          <template #header>
            <div class="panel-header">
              <span>属性编辑</span>
            </div>
          </template>
          <div class="properties-content">
            <div v-if="!selectedNode" class="no-selection">
              <el-empty description="请选择BOM节点进行编辑"></el-empty>
            </div>
            <el-form v-else :model="selectedNode" label-width="120px">
              <el-form-item label="物料编码">
                <el-select v-model="selectedNode.itemCode" placeholder="请选择物料">
                  <el-option
                    v-for="item in items"
                    :key="item.itemCode"
                    :label="item.itemCode + ' - ' + item.name"
                    :value="item.itemCode"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="物料名称">
                <el-input v-model="selectedNode.name" placeholder="请输入物料名称" disabled></el-input>
              </el-form-item>
              <el-form-item label="数量">
                <el-input-number v-model="selectedNode.quantity" :min="0" :precision="3"></el-input-number>
              </el-form-item>
              <el-form-item label="单位">
                <el-input v-model="selectedNode.unit" placeholder="请输入单位"></el-input>
              </el-form-item>
              <el-form-item label="行号">
                <el-input v-model="selectedNode.lineNo" placeholder="请输入行号"></el-input>
              </el-form-item>
              <el-form-item label="位号">
                <el-input v-model="selectedNode.position" placeholder="请输入位号"></el-input>
              </el-form-item>
              <el-form-item label="是否虚拟件">
                <el-switch v-model="selectedNode.isPhantom"></el-switch>
              </el-form-item>
              <el-form-item label="备注">
                <el-input type="textarea" v-model="selectedNode.remark" placeholder="请输入备注"></el-input>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="saveNode">保存</el-button>
                <el-button @click="cancelEdit">取消</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </div>
    </div>
    
    <!-- 差异对比区域 -->
    <div class="bom-comparison" v-if="showComparison">
      <el-card shadow="hover">
        <template #header>
          <div class="panel-header">
            <span>BOM版本对比</span>
            <el-button size="small" @click="closeComparison">关闭</el-button>
          </div>
        </template>
        <div class="comparison-content">
          <el-table :data="bomComparisonData" style="width: 100%">
            <el-table-column prop="lineNo" label="行号" min-width="80"></el-table-column>
            <el-table-column prop="itemCode" label="物料编码" min-width="150"></el-table-column>
            <el-table-column prop="name" label="物料名称" min-width="200"></el-table-column>
            <el-table-column prop="version1" label="版本A" min-width="100"></el-table-column>
            <el-table-column prop="version2" label="版本B" min-width="100"></el-table-column>
            <el-table-column prop="difference" label="差异" min-width="120">
              <template #default="scope">
                <el-tag :type="getDifferenceTagType(scope.row.difference)">{{ scope.row.difference }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { unwrapListResponse, unwrapResponseData } from '@/api'
import { useProductDataStore } from '@/stores/plm/productData'
import { plmApi } from '@/api/plm'

const store = useProductDataStore()

// BOM信息
const bomInfo = ref({
  bomCode: '',
  itemCode: '',
  version: 'V1.0',
  effectiveDate: new Date().toISOString().slice(0, 10)
})

const items = computed(() => {
  return (store.materials || []).map((m: any) => ({
    id: Number(m.id),
    itemCode: m.code,
    name: m.name,
    unit: m.unit || '件',
    spec: m.spec || ''
  }))
})

// BOM树数据
const bomTreeData = ref<any[]>([])

// 树属性配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 选中的节点
const selectedNode = ref<any>(null)

// 显示差异对比
const showComparison = ref(false)

// BOM差异对比数据
const bomComparisonData = ref<any[]>([])

// 递归查找节点
const findNode = (tree: any[], id: number): { node: any, parent: any, index: number } | null => {
  for (let i = 0; i < tree.length; i++) {
    if (tree[i].id === id) {
      return { node: tree[i], parent: null, index: i }
    }
    if (tree[i].children && tree[i].children.length > 0) {
      const result = findNode(tree[i].children, id)
      if (result) {
        return { ...result, parent: tree[i] }
      }
    }
  }
  return null
}

// 节点点击事件
const handleNodeClick = (data: any) => {
  selectedNode.value = { ...data }
}

const buildTreeNodes = (children: any[], parentId: number | null, prefix: string): any[] => {
  return (children || []).map((c: any, index: number) => {
    const lineNo = prefix ? `${prefix}.${index + 1}` : `${index + 1}`
    return {
      id: Number(c.id),
      parentId,
      itemCode: c.code,
      name: c.name,
      quantity: c.qty,
      unit: c.unit,
      lineNo,
      position: '',
      isPhantom: false,
      remark: '',
      children: buildTreeNodes(c.children || [], Number(c.id), lineNo)
    }
  })
}

const loadBom = async () => {
  const product = store.materials.find((m: any) => m.code === bomInfo.value.itemCode) || store.materials?.[0]
  if (!product) {
    bomTreeData.value = []
    selectedNode.value = null
    return
  }

  store.setSelectedMaterialId(String(product.id))
  await store.fetchBoms()
  const bom = store.boms?.[0]
  const root = bom?.tree
  const children = root?.children || []
  bomTreeData.value = buildTreeNodes(children, null, '')
  bomInfo.value.version = bom?.version || bomInfo.value.version
  bomInfo.value.bomCode = bomInfo.value.bomCode || `BOM-${product.code}-${bomInfo.value.version}`
  selectedNode.value = null
}

const exportBom = () => {
  const content = JSON.stringify({ bomInfo: bomInfo.value, tree: bomTreeData.value }, null, 2)
  const blob = new Blob([content], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `bom_${bomInfo.value.itemCode || 'product'}_${bomInfo.value.version}.json`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('BOM导出成功')
}

const importBom = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'application/json'
  input.onchange = () => {
    const file = input.files?.[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = () => {
      try {
        const text = String(reader.result || '')
        const parsed = JSON.parse(text)
        if (parsed?.bomInfo) {
          bomInfo.value = { ...bomInfo.value, ...parsed.bomInfo }
        }
        if (Array.isArray(parsed?.tree)) {
          bomTreeData.value = parsed.tree
        }
        ElMessage.success('BOM导入成功')
      } catch (e) {
        ElMessage.error('导入文件格式错误')
      }
    }
    reader.readAsText(file)
  }
  input.click()
}

const copyBom = () => {
  const v = bomInfo.value.version || 'V1.0'
  const text = v.startsWith('V') || v.startsWith('v') ? v.substring(1) : v
  const parts = text.split('.')
  const major = Number(parts[0] || 1)
  const minor = Number(parts[1] || 0) + 1
  bomInfo.value.version = `V${major}.${minor}`
  bomInfo.value.bomCode = `BOM-${bomInfo.value.itemCode || 'product'}-${bomInfo.value.version}`
  ElMessage.success('BOM已复制为新版本（未保存）')
}

const deleteBom = () => {
  ElMessageBox.confirm('确定要删除当前版本的BOM吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const product = store.materials.find((m: any) => m.code === bomInfo.value.itemCode)
    if (!product) {
      ElMessage.warning('请选择物料')
      return
    }
    const response: any = await plmApi.productData.getBomsByProductId(product.id, bomInfo.value.version)
    const list = unwrapListResponse<any>(response)
    await Promise.all(list.map((r: any) => plmApi.productData.deleteBom(r.id)))
    await loadBom()
    ElMessage.success('BOM删除成功')
  }).catch(() => {})
}

watch(
  () => bomInfo.value.itemCode,
  async (value) => {
    if (!value) return
    await loadBom()
  }
)

onMounted(async () => {
  await store.fetchMaterials()
  if (!bomInfo.value.itemCode && store.materials?.[0]?.code) {
    bomInfo.value.itemCode = store.materials[0].code
  } else {
    await loadBom()
  }
})

// 保存BOM
const saveBom = async () => {
  const product = store.materials.find((m: any) => m.code === bomInfo.value.itemCode)
  if (!product) {
    ElMessage.warning('请选择物料')
    return
  }

  try {
    const response: any = await plmApi.productData.getBomsByProductId(product.id, bomInfo.value.version)
    const list = unwrapListResponse<any>(response)
    await Promise.all(list.map((r: any) => plmApi.productData.deleteBom(r.id)))

    const createChildren = async (nodes: any[], parentId: number | null, level: number) => {
      for (let i = 0; i < nodes.length; i++) {
        const node = nodes[i]
        const item = items.value.find((x: any) => x.itemCode === node.itemCode)
        const created: any = await plmApi.productData.createBom({
          productId: product.id,
          productCode: product.code,
          materialCode: node.itemCode,
          materialName: node.name,
          materialSpec: item?.spec || '',
          unit: node.unit,
          quantity: node.quantity,
          level,
          parentId,
          sortOrder: i + 1,
          isKeyPart: 0,
          status: 'DRAFT',
          version: bomInfo.value.version,
          createdBy: '当前用户'
        })
        const newId = unwrapResponseData<any>(created)?.id
        if (node.children && node.children.length > 0) {
          await createChildren(node.children, Number(newId), level + 1)
        }
      }
    }

    await createChildren(bomTreeData.value, null, 1)
    await loadBom()
    ElMessage.success('BOM保存成功')
  } catch (e) {
    ElMessage.error('BOM保存失败')
  }
}

// 发布BOM
const releaseBom = () => {
  ElMessageBox.confirm('确定要发布当前BOM吗？发布后将不可编辑。', '发布确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const product = store.materials.find((m: any) => m.code === bomInfo.value.itemCode)
    if (!product) {
      ElMessage.warning('请选择物料')
      return
    }
    await store.releaseBom(product.id)
    await loadBom()
    ElMessage.success('BOM发布成功')
  }).catch(() => {
    // 取消发布
  })
}

// 检出BOM
const checkoutBom = async () => {
  const product = store.materials.find((m: any) => m.code === bomInfo.value.itemCode)
  if (!product) {
    ElMessage.warning('请选择物料')
    return
  }
  await store.checkoutBom(product.id)
  await loadBom()
  ElMessage.success('BOM检出成功，可以开始编辑')
}

// 检入BOM
const checkinBom = async () => {
  const product = store.materials.find((m: any) => m.code === bomInfo.value.itemCode)
  if (!product) {
    ElMessage.warning('请选择物料')
    return
  }
  await store.checkinBom(product.id)
  await loadBom()
  ElMessage.success('BOM检入成功，版本已升级')
}

// 版本对比
const compareVersions = async () => {
  const product = store.materials.find((m: any) => m.code === bomInfo.value.itemCode)
  if (!product) {
    ElMessage.warning('请选择物料')
    return
  }

  try {
    const fromRes = await ElMessageBox.prompt('请输入对比版本A', '版本对比', {
      confirmButtonText: '下一步',
      cancelButtonText: '取消',
      inputValue: bomInfo.value.version
    })
    const toRes = await ElMessageBox.prompt('请输入对比版本B', '版本对比', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: bomInfo.value.version
    })

    const diff: any = await store.compareBomVersions(product.id, fromRes.value, toRes.value)
    const added = Array.isArray(diff?.added) ? diff.added : []
    const removed = Array.isArray(diff?.removed) ? diff.removed : []
    const changed = Array.isArray(diff?.changed) ? diff.changed : []
    const rows: any[] = []
    added.forEach((r: any, idx: number) => {
      rows.push({ lineNo: `A-${idx + 1}`, itemCode: r.materialCode, name: r.materialName, version1: '', version2: toRes.value, difference: '新增' })
    })
    removed.forEach((r: any, idx: number) => {
      rows.push({ lineNo: `R-${idx + 1}`, itemCode: r.materialCode, name: r.materialName, version1: fromRes.value, version2: '', difference: '删除' })
    })
    changed.forEach((r: any, idx: number) => {
      rows.push({ lineNo: `C-${idx + 1}`, itemCode: r.materialCode, name: r.materialName, version1: String(r.oldQuantity), version2: String(r.newQuantity), difference: '数量变更' })
    })
    bomComparisonData.value = rows
    showComparison.value = true
  } catch (e) {
  }
}

// 关闭对比
const closeComparison = () => {
  showComparison.value = false
}

// 添加子件
const addSubItem = () => {
  if (!selectedNode.value) {
    ElMessage.warning('请先选择一个节点')
    return
  }
  
  // 生成新节点ID
  const flat = flattenTree(bomTreeData.value)
  const maxId = (flat.length ? Math.max(...flat.map(node => node.id)) : 0) + 1
  
  // 创建新节点
  const newItem = items.value[1] || items.value[0] // 默认选择物料
  if (!newItem) {
    ElMessage.warning('请先添加物料')
    return
  }
  
  const newNode = {
    id: maxId,
    itemCode: newItem.itemCode,
    name: newItem.name,
    quantity: 1,
    unit: newItem.unit,
    lineNo: `${selectedNode.value.lineNo}.${(selectedNode.value.children?.length || 0) + 1}`,
    position: '',
    isPhantom: false,
    remark: '',
    children: []
  }
  
  // 添加到选中节点的子节点中
  const nodeInfo = findNode(bomTreeData.value, selectedNode.value.id)
  if (nodeInfo) {
    if (!nodeInfo.node.children) {
      nodeInfo.node.children = []
    }
    nodeInfo.node.children.push(newNode)
    selectedNode.value = { ...newNode }
    ElMessage.success('子件添加成功')
  }
}

// 递归展平树结构
const flattenTree = (tree: any[]): any[] => {
  let result: any[] = []
  tree.forEach(node => {
    result.push(node)
    if (node.children && node.children.length > 0) {
      result = result.concat(flattenTree(node.children))
    }
  })
  return result
}

// 删除子件
const deleteItem = () => {
  if (!selectedNode.value) {
    ElMessage.warning('请先选择一个节点')
    return
  }
  
  ElMessageBox.confirm('确定要删除该节点及其子节点吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const nodeInfo = findNode(bomTreeData.value, selectedNode.value.id)
    if (!nodeInfo) return
    if (nodeInfo.parent) {
      nodeInfo.parent.children = nodeInfo.parent.children.filter((child: any) => child.id !== selectedNode.value.id)
    } else {
      bomTreeData.value = bomTreeData.value.filter((n: any) => n.id !== selectedNode.value.id)
    }
    selectedNode.value = null
    ElMessage.success('节点删除成功')
  }).catch(() => {
    // 取消删除
  })
}

// 上移
const moveItemUp = () => {
  if (!selectedNode.value) {
    ElMessage.warning('请先选择一个节点')
    return
  }
  
  const nodeInfo = findNode(bomTreeData.value, selectedNode.value.id)
  if (nodeInfo && nodeInfo.parent) {
    const siblings = nodeInfo.parent.children
    const index = siblings.findIndex((child: any) => child.id === selectedNode.value.id)
    
    if (index > 0) {
      // 交换位置
      const temp = siblings[index]
      siblings[index] = siblings[index - 1]
      siblings[index - 1] = temp
      ElMessage.success('节点上移成功')
    } else {
      ElMessage.info('已经是第一个节点')
    }
  }
}

// 下移
const moveItemDown = () => {
  if (!selectedNode.value) {
    ElMessage.warning('请先选择一个节点')
    return
  }
  
  const nodeInfo = findNode(bomTreeData.value, selectedNode.value.id)
  if (nodeInfo && nodeInfo.parent) {
    const siblings = nodeInfo.parent.children
    const index = siblings.findIndex((child: any) => child.id === selectedNode.value.id)
    
    if (index < siblings.length - 1) {
      // 交换位置
      const temp = siblings[index]
      siblings[index] = siblings[index + 1]
      siblings[index + 1] = temp
      ElMessage.success('节点下移成功')
    } else {
      ElMessage.info('已经是最后一个节点')
    }
  }
}

// 保存节点
const saveNode = () => {
  if (!selectedNode.value) {
    ElMessage.warning('请先选择一个节点')
    return
  }
  
  const nodeInfo = findNode(bomTreeData.value, selectedNode.value.id)
  if (nodeInfo) {
    // 更新节点信息
    Object.assign(nodeInfo.node, selectedNode.value)
    ElMessage.success('节点保存成功')
  }
}

// 取消编辑
const cancelEdit = () => {
  if (selectedNode.value) {
    // 重新从树中获取节点数据，恢复原始值
    const nodeInfo = findNode(bomTreeData.value, selectedNode.value.id)
    if (nodeInfo) {
      selectedNode.value = { ...nodeInfo.node }
    }
  }
}

// 差异标签类型映射
const getDifferenceTagType = (difference: string) => {
  const differenceMap: Record<string, string> = {
    '版本变更': 'info',
    '数量变更': 'warning',
    '新增': 'success',
    '删除': 'danger',
    '无变化': 'info'
  }
  return differenceMap[difference] || 'info'
}

// 拖拽相关事件
const handleNodeDragStart = (node: any, event: any) => {
  console.log('开始拖拽', node, event)
}

const handleNodeDragEnter = (draggingNode: any, dropNode: any, event: any) => {
  console.log('拖拽进入', draggingNode, dropNode, event)
}

const handleNodeDragLeave = (draggingNode: any, dropNode: any, event: any) => {
  console.log('拖拽离开', draggingNode, dropNode, event)
}

const handleNodeDragOver = (draggingNode: any, dropNode: any, event: any) => {
  console.log('拖拽经过', draggingNode, dropNode, event)
}

const handleNodeDragEnd = (draggingNode: any, dropNode: any, dropType: any, event: any) => {
  console.log('拖拽结束', draggingNode, dropNode, dropType, event)
}

const handleNodeDrop = (draggingNode: any, dropNode: any, dropType: any, event: any) => {
  console.log('拖拽放置', draggingNode, dropNode, dropType, event)
  // 这里可以处理节点拖拽后的逻辑
  ElMessage.success('节点位置调整成功')
}

const allowDrop = (draggingNode: any, dropNode: any, type: any) => {
  // 根节点不能成为子节点
  if (draggingNode.level === 1 && type === 'inner') {
    return false
  }
  // 允许放置
  return true
}

const allowDrag = (draggingNode: any) => {
  // 所有节点都允许拖拽
  return true
}
</script>

<style scoped lang="scss">
.bom-editor-view {
  padding: 20px;
  height: 100%;
  overflow: auto;

  h3 {
    margin-bottom: 20px;
    color: #333;
    font-size: 1.3rem;
  }

  /* BOM头部 */
  .bom-header {
    margin-bottom: 20px;
  }

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 16px;
  }

  .bom-info {
    flex: 1;
    min-width: 600px;
  }

  .bom-actions {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }

  /* BOM编辑区域 */
  .bom-editor-content {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    margin-bottom: 20px;
  }

  /* 面板样式 */
  .panel-card {
    height: 600px;
    display: flex;
    flex-direction: column;
  }

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
  }

  /* BOM树样式 */
  .bom-tree {
    flex: 1;
    overflow: auto;
  }

  .tree-node-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .node-info {
    font-size: 12px;
    color: #666;
  }

  /* 属性编辑样式 */
  .properties-content {
    flex: 1;
    overflow: auto;
  }

  .no-selection {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
  }

  /* 差异对比样式 */
  .bom-comparison {
    margin-top: 20px;
  }

  .comparison-content {
    padding: 16px 0;
  }

  /* 响应式设计 */
  @media (max-width: 1024px) {
    padding: 16px;

    .bom-editor-content {
      grid-template-columns: 1fr;
      gap: 16px;
    }

    .bom-info {
      min-width: auto;
      width: 100%;
    }

    .bom-actions {
      width: 100%;
      justify-content: flex-start;
    }

    .panel-card {
      height: 500px;
    }
  }

  @media (max-width: 768px) {
    padding: 12px;

    h3 {
      font-size: 1.1rem;
      margin-bottom: 16px;
    }

    .header-content {
      gap: 12px;
    }

    .bom-actions {
      gap: 6px;
    }

    .panel-card {
      height: 400px;
    }

    .el-button {
      padding: 4px 8px;
      font-size: 12px;
    }
  }
}
</style>
