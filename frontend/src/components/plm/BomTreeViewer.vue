<template>
  <div class="bom-tree-viewer">
    <div class="bom-header">
      <div class="bom-title">
        <h3>{{ treeData.name }} ({{ treeData.code }})</h3>
        <div class="bom-meta">
          <span>{{ treeData.spec }} | {{ treeData.unit }}</span>
        </div>
      </div>
      <div class="bom-actions">
        <el-button type="primary" size="small" @click="expandAll">
          <el-icon><Expand /></el-icon> 全部展开
        </el-button>
        <el-button type="primary" size="small" @click="collapseAll">
          <el-icon><Fold /></el-icon> 全部折叠
        </el-button>
        <el-button type="success" size="small" @click="addNode">
          <el-icon><Plus /></el-icon> 添加子项
        </el-button>
      </div>
    </div>
    <div class="bom-content">
      <el-tree
        :data="[treeData]"
        :props="defaultProps"
        :expand-on-click-node="false"
        :draggable="true"
        :allow-drop="allowDrop"
        :allow-drag="allowDrag"
        @node-click="handleNodeClick"
        @node-contextmenu="handleContextMenu"
        @node-drop="handleNodeDrop"
        ref="treeRef"
        class="bom-tree"
      >
        <template #default="{ node, data }">
          <div class="tree-node-content">
            <div class="node-info">
              <div class="node-name">{{ data.name }}</div>
              <div class="node-code">{{ data.code }}</div>
            </div>
            <div class="node-actions">
              <el-input-number
                v-model="data.qty"
                :min="1"
                :precision="0"
                :step="1"
                size="small"
                class="qty-input"
                @change="handleQtyChange(data)"
              />
              <el-button
                type="text"
                size="small"
                @click.stop="editNode(data)"
              >
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button
                type="text"
                size="small"
                @click.stop="deleteNode(node, data)"
                :disabled="node.level === 1"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </div>

    <!-- 右键菜单 -->
    <div
      v-if="contextMenuVisible"
      class="context-menu"
      :style="{ left: contextMenuLeft + 'px', top: contextMenuTop + 'px' }"
      @click.stop
    >
      <div class="menu-item" v-if="contextMenuData" @click="addNode(contextMenuData)">
        <el-icon><Plus /></el-icon> 添加子项
      </div>
      <div class="menu-item" v-if="contextMenuData" @click="editNode(contextMenuData)">
        <el-icon><Edit /></el-icon> 编辑
      </div>
      <div class="menu-item" v-if="contextMenuData" @click="deleteNode(null, contextMenuData)" :class="{ 'disabled': contextMenuData.id === 'root' }">
        <el-icon><Delete /></el-icon> 删除
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Expand, Fold, Plus, Edit, Delete } from '@element-plus/icons-vue'

// 定义组件属性
interface BomNode {
  id: string
  name: string
  code: string
  spec: string
  unit: string
  qty: number
  children?: BomNode[]
}

const props = defineProps<{
  treeData: BomNode
}>()

// 定义组件事件
const emit = defineEmits<{
  (e: 'node-click', data: BomNode): void
  (e: 'node-edit', data: BomNode): void
  (e: 'node-add', parent: BomNode | null): void
  (e: 'node-delete', data: BomNode): void
  (e: 'qty-change', data: BomNode): void
  (e: 'node-drop', data: { node: any; dragNode: any; dropNode: any; dropType: any }): void
}>()

// 树属性配置
const defaultProps = {
  children: 'children',
  label: 'name'
}

// 树引用
const treeRef = ref()

// 右键菜单状态
const contextMenuVisible = ref(false)
const contextMenuLeft = ref(0)
const contextMenuTop = ref(0)
const contextMenuData = ref<BomNode | null>(null)

// 允许拖拽
const allowDrag = (node: any) => {
  // 根节点不可拖拽
  return node.level > 1
}

// 允许放置
const allowDrop = (draggingNode: any, dropNode: any, type: string) => {
  // 只能拖放到非根节点下
  return dropNode.level >= 1
}

// 处理节点点击
const handleNodeClick = (data: BomNode) => {
  emit('node-click', data)
}

// 处理右键菜单
const handleContextMenu = (event: MouseEvent, data: BomNode, node: any, tree: any) => {
  event.preventDefault()
  contextMenuLeft.value = event.clientX
  contextMenuTop.value = event.clientY
  contextMenuData.value = data
  contextMenuVisible.value = true

  // 点击其他地方关闭右键菜单
  document.addEventListener('click', closeContextMenu)
}

// 关闭右键菜单
const closeContextMenu = () => {
  contextMenuVisible.value = false
  document.removeEventListener('click', closeContextMenu)
}

// 处理节点拖拽
const handleNodeDrop = (draggingNode: any, dropNode: any, dropType: any, ev: any) => {
  emit('node-drop', { node: treeRef.value, dragNode: draggingNode, dropNode: dropNode, dropType: dropType })
  closeContextMenu()
}

// 展开全部
const expandAll = () => {
  treeRef.value?.expandAll()
}

// 折叠全部
const collapseAll = () => {
  treeRef.value?.collapseAll()
}

// 添加节点
const addNode = (parent: BomNode | null) => {
  emit('node-add', parent)
  closeContextMenu()
}

// 编辑节点
const editNode = (data: BomNode) => {
  emit('node-edit', data)
  closeContextMenu()
}

// 删除节点
const deleteNode = (node: any, data: BomNode) => {
  if (data.id === 'root') {
    return
  }
  emit('node-delete', data)
  closeContextMenu()
}

// 处理数量变化
const handleQtyChange = (data: BomNode) => {
  emit('qty-change', data)
}
</script>

<style scoped>
.bom-tree-viewer {
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  background-color: white;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.bom-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.bom-title h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #333;
}

.bom-meta {
  font-size: 14px;
  color: #666;
}

.bom-actions {
  display: flex;
  gap: 8px;
}

.bom-content {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

.bom-tree {
  height: 100%;
}

.tree-node-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 4px 0;
}

.node-info {
  display: flex;
  flex-direction: column;
}

.node-name {
  font-weight: 500;
  color: #333;
}

.node-code {
  font-size: 12px;
  color: #666;
}

.node-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.qty-input {
  width: 80px;
}

/* 右键菜单样式 */
.context-menu {
  position: fixed;
  z-index: 1000;
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  min-width: 150px;
}

.menu-item {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.menu-item:hover {
  background-color: #f5f7fa;
}

.menu-item.disabled {
  color: #999;
  cursor: not-allowed;
}

.menu-item.disabled:hover {
  background-color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .bom-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .bom-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>