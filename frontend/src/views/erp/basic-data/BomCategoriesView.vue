<template>
  <div class="bom-categories-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><Collection /></el-icon>
          <span>物料分类</span>
        </div>
      </template>

      <TableComponent
        :data="categories"
        :columns="columns"
        :total="total"
        :loading="loading"
        :show-index="true"
        :table-actions="tableActions"
        :filters="filters"
        :show-filter="true"
        @search="handleSearch"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Collection, RefreshLeft } from '@element-plus/icons-vue'
import { TableComponent } from '../../../components/base'
import { unwrapPageResponse } from '../../../api'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'

const categories = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const page = ref(1)
const size = ref(10)

const filters = [
  { prop: 'keyword', label: '关键字', type: 'input' as 'input', placeholder: '分类名称/编码' }
] as any

const columns = [
  { prop: 'categoryCode', label: '分类编码', width: 160 },
  { prop: 'categoryName', label: '分类名称', minWidth: 240 },
  { prop: 'remark', label: '备注', minWidth: 240 }
]

const tableActions = [
  { key: 'refresh', text: '刷新', icon: RefreshLeft, handler: () => handleSearch() }
]

onMounted(() => handleSearch())

const fetchList = async (params: any = {}) => {
  try {
    loading.value = true
    const res = await erpApi.basicData.getMaterialCategories({
      page: params.page ?? page.value,
      size: params.size ?? size.value,
      keyword: params.keyword
    })
    const pageResult = unwrapPageResponse<any>(res)
    categories.value = Array.isArray(pageResult.list) ? pageResult.list : []
    total.value = pageResult.total ?? categories.value.length
    page.value = pageResult.page || page.value
    size.value = pageResult.size || size.value
  } catch (e) {
    ErrorHandler.handleApiError(e)
    categories.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = (params: any = {}) => fetchList(params)
const handleSizeChange = (s: number) => fetchList({ page: page.value, size: s })
const handleCurrentChange = (p: number) => fetchList({ page: p, size: size.value })
</script>

<style scoped lang="scss">
.bom-categories-view {
  padding: 20px;
}
</style>

