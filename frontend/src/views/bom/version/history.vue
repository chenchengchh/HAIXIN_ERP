<template>
  <div class="version-history">
    <h3>变更历史记录</h3>
    <div class="history-content">
      <!-- 筛选和搜索 -->
      <div class="history-filter">
        <el-input
          v-model="searchQuery"
          placeholder="搜索变更内容或原因"
          clearable
          prefix-icon="Search"
          size="small"
          class="search-input"
        />
        <el-select
          v-model="versionFilter"
          placeholder="按版本筛选"
          clearable
          size="small"
          class="filter-select"
        >
          <el-option
            v-for="version in versionOptions"
            :key="version"
            :label="version"
            :value="version"
          />
        </el-select>
      </div>
      
      <!-- 变更历史表格 -->
      <el-table 
        :data="filteredHistory" 
        style="width: 100%" 
        row-class-name="hover-row-effect"
        border
        stripe
        v-loading="loading"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="version" label="版本" width="100">
          <template #default="scope">
            <el-tag effect="plain" type="info">{{ scope.row.version }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeReason" label="变更动因" min-width="150" show-overflow-tooltip />
        <el-table-column prop="changedBy" label="执行人" width="100" />
        <el-table-column prop="changeTime" label="执行时间" width="160">
          <template #default="scope">
            <span>{{ new Date(scope.row.changeTime).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="changeContent" label="变更核心内容" min-width="250">
          <template #default="scope">
            <el-popover
              placement="top"
              :width="400"
              trigger="hover"
            >
              <template #reference>
                <span class="content-text">{{ scope.row.changeContent }}</span>
              </template>
              <div class="popover-content">
                <h4>变更详情</h4>
                <p>{{ scope.row.changeContent }}</p>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              text
              @click="handleViewDiff(scope.row)"
            >
              查看差异
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空状态 -->
      <el-empty 
        v-if="!loading && filteredHistory.length === 0" 
        description="暂无变更记录"
        :image-size="100"
      >
        <el-button type="primary" text @click="loadChangeHistory">刷新</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { unwrapPageResponse } from '@/api'
import { bomApi } from '@/api/bom'
import { ElMessage } from 'element-plus'

// 路由实例
const router = useRouter()

// 加载状态
const loading = ref(false)

// 搜索和筛选
const searchQuery = ref('')
const versionFilter = ref('')

// 变更历史数据
const changeHistory = ref<any[]>([])

// 版本选项（从变更历史中提取）
const versionOptions = computed(() => {
  const versions = [...new Set(changeHistory.value.map(item => item.version))]
  return versions.sort().reverse()
})

// 筛选后的变更历史
const filteredHistory = computed(() => {
  return changeHistory.value.filter(item => {
    // 版本筛选
    if (versionFilter.value && item.version !== versionFilter.value) {
      return false
    }
    // 关键词搜索
    if (searchQuery.value) {
      const searchLower = searchQuery.value.toLowerCase()
      return (
        item.changeReason?.toLowerCase().includes(searchLower) ||
        item.changeContent?.toLowerCase().includes(searchLower) ||
        item.changedBy?.toLowerCase().includes(searchLower)
      )
    }
    return true
  }).sort((a, b) => {
    // 按时间倒序排序
    return new Date(b.changeTime).getTime() - new Date(a.changeTime).getTime()
  })
})

// 加载变更历史
const loadChangeHistory = async () => {
  loading.value = true
  try {
    const res: any = await bomApi.getBomVersions({ page: 1, size: 200 })
    const page = unwrapPageResponse<any>(res)
    const list = page.list
    changeHistory.value = (Array.isArray(list) ? list : [])
      .map((v: any) => ({
        id: v.id,
        bomCode: v.bomCode,
        version: v.version,
        changeReason: '版本变更',
        changedBy: v.updatedBy || v.createdBy || '',
        changeTime: v.updatedTime || v.createdTime || '',
        changeContent: v.remark || ''
      }))
  } catch (error) {
    changeHistory.value = []
    ElMessage.error('加载变更历史失败')
  } finally {
    loading.value = false
  }
}

// 查看变更差异：跳转版本差异对比页。
// 优先选取同一BOM编码（同产品）下与目标版本时间最接近的版本作为基准，
// 避免跨产品对比产生无意义的"全增全减"结果；无同产品其他版本时给出提示。
const handleViewDiff = (row: any) => {
  const targetId = row?.id
  if (!targetId) return
  const targetTime = new Date(row.changeTime).getTime()
  const siblings = changeHistory.value
    .filter((v: any) => String(v.id) !== String(targetId) && v.bomCode && v.bomCode === row.bomCode)
    .sort((a: any, b: any) =>
      Math.abs(new Date(a.changeTime).getTime() - targetTime) -
      Math.abs(new Date(b.changeTime).getTime() - targetTime)
    )
  const base = siblings[0]
  if (!base) {
    ElMessage.warning('该BOM只有一个版本，暂无可对比的同产品版本')
    return
  }
  router.push(`/home/bom/structure/compare?id1=${base.id}&id2=${targetId}`)
}

// 初始化
onMounted(() => {
  loadChangeHistory()
})
</script>

<style scoped>
.version-history {
  padding: 20px 0;
}

.version-history h3 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 1.2rem;
}

.history-filter {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: center;
}

.search-input {
  width: 300px;
}

.filter-select {
  width: 150px;
}

.content-text {
  cursor: pointer;
  color: var(--primary-color);
  text-decoration: underline;
}

.content-text:hover {
  color: var(--primary-color-dark);
}

.popover-content {
  padding: 10px;
}

.popover-content h4 {
  margin: 0 0 10px 0;
  color: var(--text-primary);
  font-size: 16px;
}

.popover-content p {
  margin: 0;
  color: var(--text-regular);
  line-height: 1.5;
}

:deep(.el-empty) {
  margin: 40px 0;
}
</style>
