<template>
  <bom-layout title="BOM版本列表" :breadcrumb-items="[{ label: 'BOM版本列表' }]">
    <template #action-bar>
      <!-- 新增版本按钮 -->
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增版本
      </el-button>
    </template>
      
      <!-- 搜索和筛选区域 -->
      <div class="search-area">
        <el-form :model="searchForm" label-width="80px">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="BOM编码">
                <el-input v-model="searchForm.bomCode" placeholder="BOM编码搜索" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="产品名称">
                <el-input v-model="searchForm.productName" placeholder="产品名称搜索" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="状态">
                <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 100%">
                  <el-option label="草稿" :value="0" />
                  <el-option label="生效" :value="1" />
                  <el-option label="历史" :value="2" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <div class="search-btns">
                <el-button type="primary" @click="handleSearch">
                  <el-icon><Search /></el-icon>
                  查询
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
      
      <!-- 版本列表 -->
      <el-table
        v-loading="loading"
        :data="versionList"
        style="width: 100%"
        :header-cell-style="{ background: 'rgba(242, 243, 245, 0.5)', color: 'var(--text-primary)', fontWeight: '600' }"
        row-class-name="hover-row-effect"
      >
        <el-table-column prop="bomCode" label="BOM编码" min-width="120">
          <template #default="scope">
            <span class="code-text">{{ scope.row.bomCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="materialName" label="产品项目" min-width="150">
          <template #default="scope">
            {{ scope.row.materialName || scope.row.materialCode || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="100">
          <template #default="scope">
            <el-tag effect="plain" type="info">{{ scope.row.version }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            {{ scope.row.type === 1 ? 'EBOM' : scope.row.type === 2 ? 'MBOM' : 'PBOM' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="dark" class="status-tag">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建日期" width="160">
          <template #default="scope">
            {{ scope.row.createdTime?.substring(0, 10) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button v-if="scope.row.status === 0" size="small" type="success" @click="handleActivate(scope.row)" :loading="operationLoading[scope.row.id]">
              <el-icon><Check /></el-icon>
              激活
            </el-button>
            <el-button v-if="scope.row.status === 1" size="small" type="danger" @click="handleDeactivate(scope.row)" :loading="operationLoading[scope.row.id]">
              <el-icon><Close /></el-icon>
              失效
            </el-button>
            <el-button v-if="scope.row.status === 1" size="small" type="warning" @click="handleSetDefault(scope.row)" :loading="operationLoading[scope.row.id]">
              <el-icon><Star /></el-icon>
              {{ scope.row.isDefault ? '默认版本' : '设为默认' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
  </bom-layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search, RefreshRight, View, Edit, SwitchButton, Check, Close, Star } from '@element-plus/icons-vue'
import { unwrapPageResponse } from '@/api'
import { bomApi } from '@/api/bom'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ErrorHandler } from '@/utils/error-handler'
import BomLayout from '../../../components/BomLayout.vue'

// 路由实例
const router = useRouter()

// 加载状态
const loading = ref(false)

// 操作加载状态
const operationLoading = ref<Record<number, boolean>>({})

// 搜索表单
const searchForm = ref({
  bomCode: '',
  productName: '',
  status: ''
})

// 分页信息
const pagination = ref({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// BOM版本列表
const versionList = ref<any[]>([])

// 初始化
onMounted(() => {
  fetchVersionList()
})

// 获取BOM版本列表
const fetchVersionList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.currentPage,
      size: pagination.value.pageSize,
      bomCode: searchForm.value.bomCode,
      productName: searchForm.value.productName,
      status: searchForm.value.status ? Number(searchForm.value.status) : undefined
    }
    const res = await bomApi.getBomVersions(params)
    const page = unwrapPageResponse<any>(res)
    versionList.value = page.list
    pagination.value.total = page.total
  } catch (error) {
    console.error('获取BOM版本列表失败:', error)
    ErrorHandler.handleApiError(error)
    versionList.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.currentPage = 1
  fetchVersionList()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    bomCode: '',
    productName: '',
    status: ''
  }
  pagination.value.currentPage = 1
  fetchVersionList()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  fetchVersionList()
}

// 当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.value.currentPage = current
  fetchVersionList()
}

// 获取状态类型
const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'info',
    1: 'success',
    2: 'warning'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '草稿',
    1: '生效',
    2: '历史'
  }
  return textMap[status] || '未知'
}

// 新增版本
const handleAdd = () => {
  router.push('/home/bom/version/detail')
}

// 查看版本
const handleView = (row: any) => {
  router.push(`/home/bom/version/detail?id=${row.id}`)
}

// 编辑版本
const handleEdit = (row: any) => {
  router.push(`/home/bom/version/detail?id=${row.id}&mode=edit`)
}

// 激活版本
const handleActivate = (row: any) => {
  ElMessageBox.confirm('激活该版本将自动失效其它活动版本，确定吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    operationLoading.value[row.id] = true
    try {
      await bomApi.activateVersion(row.id)
      ElMessage.success('版本已激活')
      fetchVersionList()
    } catch (error: any) {
      console.error('激活失败:', error)
      ElMessage.error('激活失败，请检查网络或联系管理员')
    } finally {
      operationLoading.value[row.id] = false
    }
  }).catch(() => {
    // 取消操作，不做任何处理
  })
}

// 失效版本
const handleDeactivate = (row: any) => {
  ElMessageBox.confirm('确定要失效该版本吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    operationLoading.value[row.id] = true
    try {
      await bomApi.deactivateVersion(row.id)
      ElMessage.success('版本已失效')
      fetchVersionList()
    } catch (error: any) {
      console.error('失效失败:', error)
      ElMessage.error('失效失败，请检查网络或联系管理员')
    } finally {
      operationLoading.value[row.id] = false
    }
  }).catch(() => {
    // 取消操作，不做任何处理
  })
}

// 设置默认版本
const handleSetDefault = (row: any) => {
  if (row.isDefault) {
    ElMessage.info('该版本已经是默认版本')
    return
  }
  
  ElMessageBox.confirm('设置此版本为默认版本后，同一产品的其他版本将自动取消默认标记，确定吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    operationLoading.value[row.id] = true
    try {
      await bomApi.setDefaultVersion(row.id)
      ElMessage.success('默认版本设置成功')
      fetchVersionList()
    } catch (error: any) {
      console.error('设置默认失败:', error)
      ElMessage.error('设置默认失败，请检查网络或联系管理员')
    } finally {
      operationLoading.value[row.id] = false
    }
  }).catch(() => {
    // 取消操作，不做任何处理
  })
}
</script>

<style scoped>
/* 样式优化 */
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

.code-text {
  font-family: 'JetBrains Mono', monospace;
  font-weight: 500;
  color: var(--primary-color);
}

.status-tag {
  border-radius: 12px;
  padding: 0 12px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table) {
  --el-table-border-color: transparent;
  background: transparent !important;
}

:deep(.el-table tr) {
  background: transparent !important;
  transition: all 0.3s ease;
}

:deep(.hover-row-effect:hover) {
  background: rgba(64, 104, 255, 0.05) !important;
  transform: scale(1.002);
}
</style>
