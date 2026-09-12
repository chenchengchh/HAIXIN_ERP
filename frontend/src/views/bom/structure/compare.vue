<template>
  <bom-layout title="BOM 版本差异透视 (Comparison)" :breadcrumb-items="[{ label: '结构管理' }, { label: '差异对比' }]">
    <template #action-bar>
      <el-button type="primary" @click="handleExport" class="btn-glow">
        <el-icon><Download /></el-icon>
        导出对比报告
      </el-button>
    </template>

    <!-- 对比配置矩阵 -->
    <div class="version-select-card card-glossy">
      <el-form :model="versionForm" inline class="premium-form">
        <el-row :gutter="32" align="middle">
          <el-col :span="9">
            <el-form-item label="基准版本 (Source)">
              <el-select v-model="versionForm.leftVersion" placeholder="选择基准" style="width: 100%">
                <el-option v-for="version in versionList" :key="version.id" :label="version.name" :value="version.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="2" class="compare-vs">
            <div class="vs-circle">VS</div>
          </el-col>
          <el-col :span="9">
            <el-form-item label="对比目标 (Target)">
              <el-select v-model="versionForm.rightVersion" placeholder="选择目标" style="width: 100%">
                <el-option v-for="version in versionList" :key="version.id" :label="version.name" :value="version.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="handleCompare" class="btn-glow full-width">
              <el-icon><Search /></el-icon>
              分析差异
            </el-button>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 对比统计 & 指引 -->
    <div class="stats-grid" v-if="isComparing">
      <div class="stat-card added">
        <div class="stat-icon"><el-icon><Plus /></el-icon></div>
        <div class="stat-main">
          <span class="label">新增项</span>
          <span class="value">{{ diffStats.added }}</span>
        </div>
      </div>
      <div class="stat-card removed">
        <div class="stat-icon"><el-icon><Minus /></el-icon></div>
        <div class="stat-main">
          <span class="label">移除项</span>
          <span class="value">{{ diffStats.deleted }}</span>
        </div>
      </div>
      <div class="stat-card modified">
        <div class="stat-icon"><el-icon><EditPen /></el-icon></div>
        <div class="stat-main">
          <span class="label">属性变更</span>
          <span class="value">{{ diffStats.modified }}</span>
        </div>
      </div>
    </div>

    <!-- 对比视图核心 -->
    <div class="compare-view-main" v-if="isComparing">
      <div class="compare-row">
        <!-- 左侧面板 -->
        <div class="side-panel card-glossy">
          <div class="side-header">
            <el-tag size="small" type="info" effect="dark">BASE</el-tag>
            <span class="version-badge">{{ getVersionName(versionForm.leftVersion) }}</span>
          </div>
          <el-table
            :data="leftBomTree"
            style="width: 100%"
            row-key="id"
            v-loading="loading"
            :row-class-name="(scope: { row: any }) => getRowClassName(scope.row, 'left')"
          >
            <el-table-column prop="materialCode" label="编码" width="140" />
            <el-table-column prop="materialName" label="物料描述" min-width="150" show-overflow-tooltip />
            <el-table-column prop="usageQty" label="用量" width="70" align="center" />
          </el-table>
        </div>

        <div class="vs-divider">
          <el-icon><Switch /></el-icon>
        </div>

        <!-- 右侧面板 -->
        <div class="side-panel card-glossy">
          <div class="side-header">
            <el-tag size="small" type="success" effect="dark">TARGET</el-tag>
            <span class="version-badge">{{ getVersionName(versionForm.rightVersion) }}</span>
          </div>
          <el-table
            :data="rightBomTree"
            style="width: 100%"
            row-key="id"
            v-loading="loading"
            :row-class-name="(scope: { row: any }) => getRowClassName(scope.row, 'right')"
          >
            <el-table-column prop="materialCode" label="编码" width="140" />
            <el-table-column prop="materialName" label="物料描述" min-width="150" show-overflow-tooltip />
            <el-table-column prop="usageQty" label="用量" width="70" align="center" />
          </el-table>
        </div>
      </div>
    </div>
  </bom-layout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Download, Search, Plus, Minus, EditPen, Switch } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { unwrapListResponse, unwrapPageResponse, unwrapResponseData } from '@/api'
import { bomApi } from '@/api/bom'
import { ErrorHandler } from '@/utils/error-handler'
import BomLayout from '../../../components/BomLayout.vue'

// 路由实例（读取预选版本参数 id1/id2）
const route = useRoute()

// 版本列表
const versionList = ref<any[]>([])

onMounted(() => {
  fetchVersions()
})

const fetchVersions = async () => {
  try {
    const res = await bomApi.getBomVersions({ page: 1, size: 100 })
    const page = unwrapPageResponse<any>(res)
    versionList.value = page.list.map((v: any) => ({
      id: String(v.id),
      name: `${v.materialName || v.materialCode || ''} ${v.version || ''}`.trim(),
      materialId: v.materialId,
      bomCode: v.bomCode,
      version: v.version
    }))
    // 支持通过路由查询参数预选对比版本（id1/id2），来自版本详情/变更历史页跳转
    const q1 = route.query.id1 ? String(route.query.id1) : ''
    const q2 = route.query.id2 ? String(route.query.id2) : ''
    if (q1 && versionList.value.some(v => v.id === q1)) {
      versionForm.leftVersion = q1
    }
    if (q2 && versionList.value.some(v => v.id === q2)) {
      versionForm.rightVersion = q2
    }
    // 预选齐全时自动执行对比
    if (q1 && q2 && versionForm.leftVersion !== versionForm.rightVersion) {
      handleCompare()
    }
  } catch (error) {
    console.error('获取版本列表失败:', error)
    ErrorHandler.handleApiError(error)
    versionList.value = []
  }
}

// 版本选择表单
const versionForm = reactive({
  leftVersion: '1',
  rightVersion: '2'
})

// 左侧BOM结构
const leftBomTree = ref<any[]>([])

// 右侧BOM结构
const rightBomTree = ref<any[]>([])

// 加载状态
const loading = ref(false)

// 对比结果状态
const isComparing = ref(false)

// 差异统计
const diffStats = ref({
  added: 0,
  deleted: 0,
  modified: 0,
  total: 0
})

// 差异数据映射
const diffMap = ref({
  added: [] as string[],
  deleted: [] as string[],
  modified: [] as string[]
})

// 获取版本名称
const getVersionName = (versionId: string) => {
  const version = versionList.value.find(v => v.id === versionId)
  return version ? version.name : ''
}

// 获取行样式
const getRowClassName = (row: any, side: string) => {
  const materialCode = row.materialCode
  if (side === 'right') {
    if (diffMap.value.added.includes(materialCode)) {
      return 'diff-row added-row'
    }
    if (diffMap.value.modified.includes(materialCode)) {
      return 'diff-row modified-row'
    }
  } else {
    if (diffMap.value.deleted.includes(materialCode)) {
      return 'diff-row deleted-row'
    }
    if (diffMap.value.modified.includes(materialCode)) {
      return 'diff-row modified-row'
    }
  }
  return ''
}

// 开始对比
const handleCompare = async () => {
  if (!versionForm.leftVersion || !versionForm.rightVersion) {
    ElMessage.warning('请选择要对比的两个版本')
    return
  }

  loading.value = true
  try {
    const v1 = versionList.value.find(v => v.id === versionForm.leftVersion)
    const v2 = versionList.value.find(v => v.id === versionForm.rightVersion)

    if (!v1 || !v2) {
      ElMessage.warning('请选择要对比的两个版本')
      return
    }

    const [lines1Res, lines2Res, diffRes] = await Promise.all([
      bomApi.getBomLinesByHeaderId(v1.id),
      bomApi.getBomLinesByHeaderId(v2.id),
      bomApi.compareBoms(v1.id, v2.id)
    ])

    leftBomTree.value = unwrapListResponse<any>(lines1Res).map((line: any) => ({
      id: String(line.id),
      materialCode: line.childMaterialCode,
      materialName: line.childMaterialName,
      usageQty: line.quantity
    }))

    rightBomTree.value = unwrapListResponse<any>(lines2Res).map((line: any) => ({
      id: String(line.id),
      materialCode: line.childMaterialCode,
      materialName: line.childMaterialName,
      usageQty: line.quantity
    }))

    const d = unwrapResponseData<any>(diffRes) || {}
    diffMap.value.added = (d.added || []).map((e: any) => e.materialCode)
    diffMap.value.deleted = (d.removed || []).map((e: any) => e.materialCode)
    diffMap.value.modified = (d.changed || []).map((e: any) => e.materialCode)

    diffStats.value = {
      added: diffMap.value.added.length,
      deleted: diffMap.value.deleted.length,
      modified: diffMap.value.modified.length,
      total: (d.totalChanges ?? (diffMap.value.added.length + diffMap.value.deleted.length + diffMap.value.modified.length)) as number
    }
    isComparing.value = true
  } catch (error) {
    console.error('版本对比失败:', error)
    ElMessage.error('版本对比失败，请检查网络或联系管理员')
    leftBomTree.value = []
    rightBomTree.value = []
    isComparing.value = false
  } finally {
    loading.value = false
  }
}

// 导出对比报告
const handleExport = () => {
  const exportData = {
    leftVersion: getVersionName(versionForm.leftVersion),
    rightVersion: getVersionName(versionForm.rightVersion),
    diffStats: diffStats.value,
    leftBomTree: leftBomTree.value,
    rightBomTree: rightBomTree.value,
    diffMap: diffMap.value
  }
  
  const dataStr = JSON.stringify(exportData, null, 2)
  const dataBlob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(dataBlob)
  const link = document.createElement('a')
  link.href = url
  link.download = `bom_diff_${new Date().getTime()}.json`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  
  ElMessage.success('对比报告导出成功')
}
</script>

<style scoped>
.btn-glow {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s;
}

.btn-glow:hover {
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.5);
  transform: translateY(-2px);
}

.full-width {
  width: 100%;
}

.version-select-card {
  padding: 24px;
  margin-bottom: 24px;
}

.compare-vs {
  display: flex;
  justify-content: center;
  align-items: center;
}

.vs-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF 0%, #667EEA 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 16px;
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
  border: 3px solid white;
  transition: all 0.3s ease;
}

.vs-circle:hover {
  transform: scale(1.1);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.6);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

/* 添加card-glossy类定义 */
.card-glossy {
  background: white;
  border-radius: var(--border-radius-lg);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  border: 1px solid var(--border-color-light);
  transition: all 0.3s ease;
}

.card-glossy:hover {
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.stat-card {
  background: white;
  padding: 16px 24px;
  border-radius: var(--border-radius-lg);
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
  border: 1px solid var(--border-color-light);
}

.stat-card.added {
  background: #f0f9eb;
  border-color: #e1f3d8;
}

.stat-card.added .stat-icon {
  background: #67c23a;
  color: white;
}

.stat-card.added .stat-main .label {
  color: #67c23a;
}

.stat-card.removed {
  background: #fef0f0;
  border-color: #fbc4ab;
}

.stat-card.removed .stat-icon {
  background: #f56c6c;
  color: white;
}

.stat-card.removed .stat-main .label {
  color: #f56c6c;
}

.stat-card.modified {
  background: #fdf6ec;
  border-color: #faecd8;
}

.stat-card.modified .stat-icon {
  background: #e6a23c;
  color: white;
}

.stat-card.modified .stat-main .label {
  color: #e6a23c;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.stat-main {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.stat-main .label {
  font-size: 13px;
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

.compare-view-main {
  margin-top: 10px;
}

.compare-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.side-panel {
  flex: 1;
  padding: 16px;
  min-height: 400px;
}

.side-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
}

.version-badge {
  font-weight: 600;
  color: var(--text-primary);
}

.vs-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 400px;
  color: var(--text-placeholder);
  font-size: 24px;
}

:deep(.el-table) {
  background: transparent !important;
}

/* 差异高亮 */
:deep(.added-row) {
  background-color: rgba(103, 194, 58, 0.08) !important;
}

:deep(.deleted-row) {
  background-color: rgba(245, 108, 108, 0.08) !important;
  text-decoration: line-through;
  color: var(--text-secondary);
}

:deep(.modified-row) {
  background-color: rgba(230, 162, 60, 0.08) !important;
}

@media (max-width: 1200px) {
  .compare-row {
    flex-direction: column;
  }
  .vs-divider {
    height: 40px;
    width: 100%;
    transform: rotate(90deg);
  }
}
</style>
