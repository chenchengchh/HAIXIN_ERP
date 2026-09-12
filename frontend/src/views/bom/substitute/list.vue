<template>
  <bom-layout title="物料替代管理" :breadcrumb-items="[{ label: '物料替代管理' }]">
    <template #action-bar>
      <!-- 新增替代料按钮 -->
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增替代料
      </el-button>
    </template>
      
      <!-- 搜索和筛选区域 -->
      <div class="search-area">
        <el-form :model="searchForm" label-width="90px">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="主料编码">
                <el-input v-model="searchForm.mainMaterialCode" placeholder="主料编码搜索" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="替代料编码">
                <el-input v-model="searchForm.subMaterialCode" placeholder="替代料编码搜索" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="类型">
                <el-select v-model="searchForm.substituteType" placeholder="全部类型" clearable style="width: 100%">
                  <el-option label="全局替代" :value="1" />
                  <el-option label="局部替代" :value="2" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="状态">
                <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 100%">
                  <el-option label="启用" :value="1" />
                  <el-option label="停用" :value="0" />
                </el-select>
              </el-form-item>
            </el-col>
            </el-row>
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
        </el-form>
      </div>
      
      <!-- 替代料列表 -->
      <el-table
        v-loading="loading"
        :data="substituteList"
        style="width: 100%"
        :header-cell-style="{ background: 'rgba(242, 243, 245, 0.5)', color: 'var(--text-primary)', fontWeight: '600' }"
        row-class-name="hover-row-effect"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="mainMaterialCode" label="主料编码" width="140">
          <template #default="scope">
            <span class="code-text">{{ scope.row.mainMaterialCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="mainMaterialName" label="主料名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="subMaterialCode" label="替代料编码" width="140">
          <template #default="scope">
            <span class="code-text sub-code">{{ scope.row.subMaterialCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="subMaterialName" label="替代料名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="substituteType" label="替代类型" width="100">
          <template #default="scope">
            <el-tag effect="plain" :type="scope.row.substituteType === 1 ? 'primary' : 'warning'">
              {{ scope.row.substituteType === 1 ? '全局' : '局部' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ratio" label="比例" width="90">
          <template #default="scope">
            <el-tag size="small" type="info">{{ scope.row.ratio }}:1</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" effect="dark" class="status-tag">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建日期" width="120">
          <template #default="scope">
            {{ scope.row.createdTime?.substring(0, 10) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="warning" @click="handleToggleStatus(scope.row)">
              <el-icon><SwitchButton /></el-icon>
              {{ scope.row.status === 1 ? '停用' : '启用' }}
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
import { Plus, Search, RefreshRight, View, Edit, SwitchButton } from '@element-plus/icons-vue'
import { unwrapPageResponse } from '@/api'
import { bomApi, type Substitute } from '@/api/bom'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DataTransformer } from '@/utils/data-transformer'
import BomLayout from '../../../components/BomLayout.vue'

// 路由实例
const router = useRouter()

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = ref({
  mainMaterialCode: '',
  mainMaterialName: '',
  subMaterialCode: '',
  subMaterialName: '',
  substituteType: '',
  status: ''
})

// 分页信息
const pagination = ref({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 替代料列表
const substituteList = ref<Substitute[]>([])

const getResponseMessage = (response: any, fallback: string) => {
  const normalized = DataTransformer.normalizeResponse(response)
  return normalized?.msg || normalized?.message || fallback
}

// 初始化
onMounted(() => {
  fetchSubstituteList()
})

// 获取替代料列表
const fetchSubstituteList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.currentPage,
      size: pagination.value.pageSize,
      ...searchForm.value
    }
    const res = await bomApi.getSubstituteList(params)
    const page = unwrapPageResponse<Substitute>(res)
    substituteList.value = page.list
    pagination.value.total = page.total
  } catch (error) {
    console.error('获取替代料列表失败:', error)
    ElMessage.error('获取替代料列表失败')
    substituteList.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.currentPage = 1
  fetchSubstituteList()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    mainMaterialCode: '',
    mainMaterialName: '',
    subMaterialCode: '',
    subMaterialName: '',
    substituteType: '',
    status: ''
  }
  pagination.value.currentPage = 1
  fetchSubstituteList()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  fetchSubstituteList()
}

// 当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.value.currentPage = current
  fetchSubstituteList()
}

// 新增替代料
const handleAdd = () => {
  router.push('/home/bom/substitute/detail')
}

// 查看替代料
const handleView = (row: any) => {
  router.push(`/home/bom/substitute/detail?id=${row.id}`)
}

// 编辑替代料
const handleEdit = (row: any) => {
  router.push(`/home/bom/substitute/detail?id=${row.id}&mode=edit`)
}

// 切换替代料状态
const handleToggleStatus = (row: Substitute) => {
  if (!row.id) return
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '停用'

  ElMessageBox.confirm(`确定要${statusText}该替代关系吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = newStatus === 1 ? await bomApi.enableSubstitute(row.id!) : await bomApi.disableSubstitute(row.id!)
      const responseData = DataTransformer.normalizeResponse(res)
      if (!DataTransformer.isSuccessCode(responseData?.code)) {
        ElMessage.error(getResponseMessage(responseData, `${statusText}失败`))
        return
      }
      ElMessage.success(`替代料已${statusText}`)
      fetchSubstituteList()
    } catch (error) {
      ElMessage.error(`${statusText}失败`)
    }
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
  margin-top: 12px;
}

.code-text {
  font-family: 'JetBrains Mono', monospace;
  font-weight: 500;
  color: var(--primary-color);
}

.sub-code {
  color: var(--warning-color);
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
