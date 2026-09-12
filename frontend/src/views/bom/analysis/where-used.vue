<template>
  <bom-layout title="BOM物料反查" :breadcrumb-items="[{ label: 'BOM分析工具' }, { label: '物料反查' }]">
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :model="searchForm" label-width="80px">
        <el-row :gutter="20" align="bottom">
          <el-col :span="8">
            <el-form-item label="物料编码">
              <el-input v-model="searchForm.materialCode" placeholder="物料编码或ID" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="物料名称">
              <el-input v-model="searchForm.materialName" placeholder="物料名称关键字" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <div class="search-btns">
              <el-button type="primary" @click="handleSearch" :loading="loading">
                <el-icon><Search /></el-icon>
                执行反查
              </el-button>
              <el-button @click="handleReset">
                <el-icon><RefreshRight /></el-icon>
                重置
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </div>
    
    <!-- 操作按钮区域 -->
    <div class="action-area" v-if="searchResult.length > 0">
      <el-button-group>
        <el-button @click="expandAll">
          <el-icon><Expand /></el-icon>
          展开全部
        </el-button>
        <el-button @click="collapseAll">
          <el-icon><Fold /></el-icon>
          折叠全部
        </el-button>
        <el-button @click="exportResult">
          <el-icon><Download /></el-icon>
          导出结果
        </el-button>
      </el-button-group>
    </div>
    
    <!-- 反查结果 -->
    <div class="result-area" v-if="searchResult.length > 0">
      <div class="result-header">
        <span class="title">反查结果树</span>
        <el-tag type="info" effect="plain" round>实时分析</el-tag>
      </div>
      <div class="result-tree-container card-glossy">
        <el-tree
          ref="treeRef"
          :data="searchResult"
          :props="treeProps"
          :default-expand-all="true"
          node-key="id"
          highlight-current
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <div class="node-info">
                <span class="node-code code-text">{{ data.bomCode || data.materialCode }}</span>
                <span class="node-name">{{ data.materialName }}</span>
                <el-tag v-if="data.level === 0" effect="dark" type="success" size="small">最终父项</el-tag>
                <el-tag v-else effect="plain" type="success" size="small">L{{ data.level }}</el-tag>
              </div>
              <div class="node-meta">
                <span class="meta-item">用量: <strong>{{ data.usageQty || '-' }}</strong></span>
                <span class="meta-item">单位: {{ data.unit || '-' }}</span>
                <span class="meta-item">版本: <el-tag size="small" type="info" effect="plain">{{ data.version || '-' }}</el-tag></span>
              </div>
            </div>
          </template>
        </el-tree>
      </div>
    </div>
    
    <!-- 无结果提示 -->
    <div class="no-result" v-else-if="searchForm.materialCode || searchForm.materialName">
      <el-empty description="未找到相关的父级引用" />
    </div>
    
    <!-- 加载状态 -->
    <div class="loading-container" v-if="loading">
      <el-skeleton :rows="6" animated />
    </div>
  </bom-layout>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { Search, RefreshRight, Expand, Fold, Download } from '@element-plus/icons-vue'
import { unwrapPageResponse, unwrapResponseData } from '@/api'
import { bomApi } from '@/api/bom'
import { ElMessage, ElTree } from 'element-plus'
import { useRoute } from 'vue-router'
import BomLayout from '../../../components/BomLayout.vue'

// 搜索表单
const searchForm = reactive({
  materialCode: '',
  materialName: ''
})

type WhereUsedNode = {
  id: string | number
  bomCode?: string
  materialCode?: string
  materialName: string
  level: number
  usageQty?: number
  unit?: string
  version?: string
  children?: WhereUsedNode[]
}

// 搜索结果
const searchResult = ref<WhereUsedNode[]>([])

// 加载状态
const loading = ref(false)

// 树引用
const treeRef = ref<InstanceType<typeof ElTree> | null>(null)

const route = useRoute()

// 树配置
const treeProps = {
  label: 'materialName',
  children: 'children'
}

// 处理搜索
const handleSearch = async () => {
  if (!searchForm.materialCode && !searchForm.materialName) {
    ElMessage.warning('请输入物料编码或名称进行查询')
    return
  }

  try {
    loading.value = true
    const keywordCode = searchForm.materialCode?.trim()
    const keywordName = searchForm.materialName?.trim()

    let materialId: number | undefined
    if (keywordCode) {
      const matRes = await bomApi.getMaterialList({ code: keywordCode, page: 1, size: 1 })
      const list = unwrapPageResponse<any>(matRes).list
      materialId = list?.[0]?.id
    } else if (keywordName) {
      const matRes = await bomApi.getMaterialList({ name: keywordName, page: 1, size: 1 })
      const list = unwrapPageResponse<any>(matRes).list
      materialId = list?.[0]?.id
    }

    if (!materialId) {
      ElMessage.warning('未找到匹配的物料，请检查编码/名称')
      searchResult.value = []
      return
    }

    const res = await bomApi.getWhereUsed(materialId)
    const payload = unwrapResponseData<any>(res) || {}
    const items = Array.isArray(payload?.data) ? payload.data : Array.isArray(payload) ? payload : []
    const rootsMap = new Map<string | number, WhereUsedNode>()
    for (const item of items) {
      const header = item?.bomHeader
      const line = item?.bomLine
      if (!header) continue

      const rootKey = `H-${header.id}`
      let root = rootsMap.get(rootKey)
      if (!root) {
        root = {
          id: rootKey,
          bomCode: header.bomCode,
          materialCode: header.materialCode,
          materialName: header.materialName,
          level: 0,
          unit: '',
          version: header.version,
          children: []
        }
        rootsMap.set(rootKey, root)
      }

      if (line) {
        root.children?.push({
          id: `L-${line.id ?? `${header.id}-${line.childMaterialId}`}`,
          bomCode: header.bomCode,
          materialCode: line.childMaterialCode,
          materialName: line.childMaterialName,
          level: 1,
          usageQty: line.quantity,
          unit: line.unit,
          version: header.version,
          children: []
        })
      }
    }

    searchResult.value = Array.from(rootsMap.values())
    ElMessage.success('物料反查成功')
  } catch (error) {
    console.error('物料反查失败:', error)
    ElMessage.error('物料反查失败，请检查网络或联系管理员')
    searchResult.value = []
  } finally {
    loading.value = false
  }
}

// 处理重置
const handleReset = () => {
  searchForm.materialCode = ''
  searchForm.materialName = ''
  searchResult.value = []
}

// 展开全部
const expandAll = () => {
  const keys: Array<string | number> = []
  const walk = (nodes: WhereUsedNode[]) => {
    for (const node of nodes) {
      keys.push(node.id)
      if (node.children && node.children.length > 0) {
        walk(node.children)
      }
    }
  }
  walk(searchResult.value)
  ;(treeRef.value as any)?.setExpandedKeys?.(keys)
}

// 折叠全部
const collapseAll = () => {
  ;(treeRef.value as any)?.setExpandedKeys?.([])
}

// 导出结果
const exportResult = () => {
  if (searchResult.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }
  
  const dataStr = JSON.stringify(searchResult.value, null, 2)
  const dataBlob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(dataBlob)
  const link = document.createElement('a')
  link.href = url
  link.download = `bom_where_used_${new Date().getTime()}.json`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  
  ElMessage.success('导出成功')
}

// 初始化
onMounted(() => {
  console.log('BOM物料反查组件已挂载')
})
</script>

<style scoped>
.search-area {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.search-btns {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.result-area {
  margin-top: 24px;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.result-header .title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.result-tree-container {
  padding: 24px;
  max-height: 600px;
  overflow-y: auto;
  border-radius: var(--border-radius-lg);
}

:deep(.el-tree) {
  background: transparent !important;
}

:deep(.el-tree-node__content) {
  height: auto !important;
  padding: 4px 0;
  transition: all 0.3s ease;
}

:deep(.el-tree-node__content:hover) {
  background: rgba(64, 104, 255, 0.08) !important;
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 12px;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.node-code {
  font-size: 14px;
}

.node-name {
  color: var(--text-regular);
}

.node-meta {
  display: flex;
  gap: 16px;
  color: var(--text-secondary);
  font-size: 13px;
}

.meta-item strong {
  color: var(--primary-color);
}

.no-result {
  margin: 60px 0;
}

.code-text {
  font-family: 'JetBrains Mono', monospace;
  font-weight: 500;
  color: var(--primary-color);
}
</style>
