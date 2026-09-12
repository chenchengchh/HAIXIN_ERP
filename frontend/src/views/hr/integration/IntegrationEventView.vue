<template>
  <div class="integration-event">
    <div class="header">
      <div class="title">HR集成事件</div>
      <div class="actions">
        <el-button type="primary" @click="reload" :loading="loading">刷新</el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent>
        <el-form-item label="事件类型">
          <el-select
            v-model="filter.eventType"
            placeholder="全部类型"
            clearable
            style="width: 220px"
          >
            <el-option label="入职→OA" value="HR_EMPLOYEE_ONBOARDED_OA" />
            <el-option label="入职→ERP" value="HR_EMPLOYEE_ONBOARDED_ERP" />
            <el-option label="调动→OA" value="HR_EMPLOYEE_TRANSFERRED_OA" />
            <el-option label="调动→ERP" value="HR_EMPLOYEE_TRANSFERRED_ERP" />
            <el-option label="离职→OA" value="HR_EMPLOYEE_RESIGNED_OA" />
            <el-option label="离职→ERP" value="HR_EMPLOYEE_RESIGNED_ERP" />
          </el-select>
        </el-form-item>
        <el-form-item label="投递状态">
          <el-select
            v-model="filter.status"
            placeholder="全部状态"
            clearable
            style="width: 140px"
          >
            <el-option label="待投递" value="PENDING" />
            <el-option label="已投递" value="SENT" />
            <el-option label="投递失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务引用号">
          <el-input
            v-model="filter.refNo"
            placeholder="员工编号/记录ID"
            clearable
            style="width: 200px"
            @keyup.enter="onSearch"
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
        <el-table-column prop="eventType" label="事件类型" min-width="200" show-overflow-tooltip />
        <el-table-column prop="refNo" label="业务引用号" min-width="140" />
        <el-table-column prop="status" label="投递状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="retryCount" label="重试次数" width="100" align="right" />
        <el-table-column prop="producer" label="生产者" width="130" />
        <el-table-column prop="eventId" label="事件ID" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="mono-text">{{ row.eventId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="traceId" label="追踪ID" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="mono-text">{{ row.traceId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastError" label="最近错误" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.lastError" class="error-text">{{ row.lastError }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" min-width="180">
          <template #default="{ row }">{{ fmtTime(row.createdTime) }}</template>
        </el-table-column>
        <el-table-column prop="updatedTime" label="更新时间" min-width="180">
          <template #default="{ row }">{{ fmtTime(row.updatedTime) }}</template>
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
import { integrationApi, type IntegrationEventQueryParams } from '@/api/hr'

/**
 * HR 集成事件查询页（F5 前端配套）
 *
 * 业务背景：HR 员工入职/调动/离职审批通过后，通过 Outbox 向 OA 和 ERP 异步投递事件。
 * 本页面查询 hr_integration_outbox 表，监控事件投递状态，验证 HR→OA/ERP 闭环是否畅通。
 */

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const rows = ref<any[]>([])

/** 筛选条件 */
const filter = reactive({
  eventType: '',
  status: '',
  refNo: ''
})

/**
 * 组装查询参数
 * @returns 查询参数对象
 */
const buildParams = (): IntegrationEventQueryParams => {
  const params: IntegrationEventQueryParams = {
    page: page.value,
    size: size.value
  }
  if (filter.eventType) {
    params.eventType = filter.eventType
  }
  if (filter.status) {
    params.status = filter.status
  }
  if (filter.refNo.trim()) {
    params.refNo = filter.refNo.trim()
  }
  return params
}

/**
 * 加载分页数据
 */
const loadPage = async () => {
  const response = await integrationApi.fetchEvents(buildParams())
  const payload = (response as any)?.data
  const data = payload?.data ?? payload
  total.value = Number(data?.total ?? 0)
  const list = data?.list ?? data?.records ?? []
  rows.value = Array.isArray(list) ? list : []
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
  filter.eventType = ''
  filter.status = ''
  filter.refNo = ''
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
 * 投递状态标签类型
 * @param status 状态值
 * @returns Element Plus tag type
 */
const statusTagType = (status: string): 'success' | 'info' | 'warning' | 'danger' => {
  switch (status) {
    case 'SENT':
      return 'success'
    case 'PENDING':
      return 'warning'
    case 'FAILED':
      return 'danger'
    default:
      return 'info'
  }
}

/**
 * 投递状态文案
 * @param status 状态值
 * @returns 中文文案
 */
const statusLabel = (status: string): string => {
  switch (status) {
    case 'SENT':
      return '已投递'
    case 'PENDING':
      return '待投递'
    case 'FAILED':
      return '投递失败'
    default:
      return status || '-'
  }
}

onMounted(() => {
  reload()
})
</script>

<style scoped>
.integration-event {
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
.mono-text {
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
  color: #909399;
}
.error-text {
  color: #f56c6c;
  font-size: 12px;
}
.text-muted {
  color: #c0c4cc;
}
</style>
