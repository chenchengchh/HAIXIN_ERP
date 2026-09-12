<template>
  <div class="completion-fact">
    <div class="header">
      <div class="title">生产完工事实</div>
      <div class="actions">
        <el-button type="primary" @click="reload" :loading="loading">刷新</el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent>
        <el-form-item label="ERP生产单号">
          <el-input
            v-model="filter.erpProductionNo"
            placeholder="请输入ERP生产单号"
            clearable
            style="width: 200px"
            @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="MES工单号">
          <el-input
            v-model="filter.workOrderNo"
            placeholder="请输入MES工单号"
            clearable
            style="width: 200px"
            @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select
            v-model="filter.scmOrderStatus"
            placeholder="全部状态"
            clearable
            style="width: 160px"
          >
            <el-option label="已完工" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item label="完工时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never">
      <el-table :data="rows" v-loading="loading" style="width: 100%" stripe>
        <el-table-column prop="erpProductionNo" label="ERP生产单号" min-width="160" />
        <el-table-column prop="workOrderNo" label="MES工单号" min-width="160" />
        <el-table-column prop="productCode" label="产品编码" min-width="120" />
        <el-table-column prop="productName" label="产品名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="planQuantity" label="计划数量" width="120" align="right">
          <template #default="{ row }">{{ fmtNum(row.planQuantity) }}</template>
        </el-table-column>
        <el-table-column prop="completedQuantity" label="完工数量" width="120" align="right">
          <template #default="{ row }">
            <span :class="{ 'qty-highlight': row.completedQuantity > 0 }">
              {{ fmtNum(row.completedQuantity) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="completedTime" label="完工时间" min-width="180">
          <template #default="{ row }">{{ fmtTime(row.completedTime) }}</template>
        </el-table-column>
        <el-table-column prop="scmOrderStatus" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.scmOrderStatus)">
              {{ statusLabel(row.scmOrderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceSystem" label="来源系统" width="130" />
        <el-table-column prop="createdTime" label="落库时间" min-width="180">
          <template #default="{ row }">{{ fmtTime(row.createdTime) }}</template>
        </el-table-column>
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { completionFactApi, type CompletionFactQueryParams } from '@/api/scm'

/**
 * 生产完工事实查询页（B6 闭环前端 F2）
 *
 * 业务背景：MES 工单完工后通过 Outbox 推送完工事件，SCM 落库后形成制造回流。
 * 本页面查询 scm_production_completion_fact 表，验证生产链闭环（MES→SCM）是否畅通。
 */

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const rows = ref<any[]>([])

/** 筛选条件 */
const filter = reactive({
  erpProductionNo: '',
  workOrderNo: '',
  scmOrderStatus: ''
})

/** 完工时间范围（el-date-picker 绑定值，数组 [from, to]） */
const dateRange = ref<[string, string] | null>(null)

/**
 * 组装查询参数
 * @returns 查询参数对象
 */
const buildParams = (): CompletionFactQueryParams => {
  const params: CompletionFactQueryParams = {
    page: page.value,
    size: size.value
  }
  if (filter.erpProductionNo.trim()) {
    params.erpProductionNo = filter.erpProductionNo.trim()
  }
  if (filter.workOrderNo.trim()) {
    params.workOrderNo = filter.workOrderNo.trim()
  }
  if (filter.scmOrderStatus) {
    params.scmOrderStatus = filter.scmOrderStatus
  }
  if (dateRange.value && dateRange.value.length === 2) {
    params.completedTimeFrom = dateRange.value[0]
    params.completedTimeTo = dateRange.value[1]
  }
  return params
}

/**
 * 加载分页数据
 */
const loadPage = async () => {
  const { data } = await completionFactApi.fetchCompletionFacts(buildParams())
  total.value = Number(data?.total ?? 0)
  rows.value = Array.isArray(data?.list) ? data.list : []
}

/**
 * 重新加载（查询/翻页/刷新共用）
 */
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

/**
 * 点击查询按钮：回到第一页后加载
 */
const onSearch = () => {
  page.value = 1
  reload()
}

/**
 * 点击重置按钮：清空筛选条件并重新加载
 */
const onReset = () => {
  filter.erpProductionNo = ''
  filter.workOrderNo = ''
  filter.scmOrderStatus = ''
  dateRange.value = null
  page.value = 1
  reload()
}

/**
 * 每页条数变更：回到第一页后加载
 */
const onSizeChange = () => {
  page.value = 1
  reload()
}

/**
 * 格式化数字（保留 4 位小数，去除末尾多余 0）
 * @param v 数值
 * @returns 格式化后的字符串
 */
const fmtNum = (v: any): string => {
  if (v === null || v === undefined || v === '') return '-'
  const n = Number(v)
  if (Number.isNaN(n)) return '-'
  return n.toFixed(4).replace(/\.?0+$/, '') || '0'
}

/**
 * 格式化时间（兼容 ISO 字符串与空值）
 * @param v 时间值
 * @returns 格式化后的字符串
 */
const fmtTime = (v: any): string => {
  if (!v) return '-'
  const s = String(v).replace('T', ' ')
  return s.length > 19 ? s.substring(0, 19) : s
}

/**
 * 状态标签类型
 * @param status 状态值
 * @returns Element Plus tag type
 */
const statusTagType = (status: string): 'success' | 'info' | 'warning' | 'danger' => {
  switch (status) {
    case 'COMPLETED':
      return 'success'
    default:
      return 'info'
  }
}

/**
 * 状态文案
 * @param status 状态值
 * @returns 中文文案
 */
const statusLabel = (status: string): string => {
  switch (status) {
    case 'COMPLETED':
      return '已完工'
    default:
      return status || '-'
  }
}

onMounted(() => {
  reload()
})
</script>

<style scoped>
.completion-fact {
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
.filter-card {
  margin-bottom: 12px;
}
.filter-card :deep(.el-form-item) {
  margin-bottom: 8px;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}
.qty-highlight {
  color: #67c23a;
  font-weight: 600;
}
</style>
