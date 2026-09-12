<template>
  <div class="qc-hold-monitor">
    <div class="header">
      <div class="title">质检HOLD监控</div>
      <div class="actions">
        <el-button type="primary" @click="reload" :loading="loading">刷新</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table :data="rows" v-loading="loading" style="width: 100%">
        <el-table-column prop="orderNo" label="采购单号" min-width="180" />
        <el-table-column prop="supplierName" label="供应商" min-width="160" />
        <el-table-column prop="orderStatus" label="状态" width="120">
          <template #default>
            <el-tag type="danger">质检HOLD</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderAmount" label="订单金额" width="140">
          <template #default="{ row }">{{ fmtMoney(row.orderAmount) }}</template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" min-width="180" />
        <el-table-column prop="expectedDeliveryDate" label="期望交期" min-width="180" />
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @current-change="reload"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { reportApi } from '@/api/scm'

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const rows = ref<any[]>([])

const loadPage = async () => {
  const { data } = await reportApi.getQcHoldOrdersPage({ page: page.value, size: size.value })
  total.value = Number(data?.total ?? 0)
  rows.value = Array.isArray(data?.list) ? data.list : []
}

const reload = async () => {
  loading.value = true
  try {
    await loadPage()
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const onSizeChange = () => {
  page.value = 1
  reload()
}

const fmtMoney = (v: any) => {
  const n = Number(v ?? 0)
  return n.toFixed(2)
}

onMounted(() => {
  reload()
})
</script>

<style scoped>
.qc-hold-monitor {
  padding: 12px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}
</style>

