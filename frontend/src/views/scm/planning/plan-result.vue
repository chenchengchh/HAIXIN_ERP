<template>
  <div class="plan-result-container" :class="{ embedded }">
    <!-- 页面标题 -->
    <div v-if="!embedded" class="page-header">
      <h2>计划结果确认</h2>
      <p>查看MRP运算生成的采购申请、生产计划和调拨建议，并进行确认和下达操作</p>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 筛选条件卡片 -->
      <el-card shadow="hover" class="filter-card">
        <template #header>
          <div class="card-header">
            <span>筛选条件</span>
          </div>
        </template>

        <el-form :model="filterForm" label-position="left" label-width="100px" class="filter-form">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="计划ID">
                <el-input v-model="filterForm.planId" placeholder="输入计划ID" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="建议类型">
                <el-select v-model="filterForm.type" placeholder="选择类型" style="width: 100%">
                  <el-option label="全部" value="" />
                  <el-option label="采购建议" value="PURCHASE" />
                  <el-option label="生产建议" value="PRODUCTION" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="状态">
                <el-select v-model="filterForm.status" placeholder="选择状态" style="width: 100%">
                  <el-option label="全部" value="" />
                  <el-option label="待处理" value="PENDING" />
                  <el-option label="已确认" value="CONFIRMED" />
                  <el-option label="已拒绝" value="REJECTED" />
                  <el-option label="已发布" value="RELEASED" />
                  <el-option label="失败" value="FAILED" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="物料关键字">
                <el-input v-model="filterForm.materialKeyword" placeholder="编码/名称" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="需求日期">
                <el-date-picker
                  v-model="filterForm.requiredDateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12" class="filter-actions">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="resetFilter">重置</el-button>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 操作按钮卡片 -->
      <el-card shadow="hover" class="action-card">
        <div class="action-buttons">
          <el-button type="primary" @click="batchConfirm" :disabled="selectedRows.length === 0">
            <el-icon><Check /></el-icon> 批量确认
          </el-button>
          <el-button type="danger" @click="batchReject" :disabled="selectedRows.length === 0">
            <el-icon><Close /></el-icon> 批量拒绝
          </el-button>
          <el-button type="success" @click="releasePlan" :disabled="!planIdNumber">
            <el-icon><Upload /></el-icon> 下达计划
          </el-button>
          <el-button @click="exportResults">
            <el-icon><Download /></el-icon> 导出结果
          </el-button>
          <el-button @click="refreshResults" :disabled="!planIdNumber">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </el-card>

      <!-- 结果表格卡片 -->
      <el-card shadow="hover" class="result-card">
        <template #header>
          <div class="card-header">
            <span>MRP运算结果</span>
            <span class="result-count">共 {{ filteredResults.length }} 条记录</span>
          </div>
        </template>

        <el-table
          ref="tableRef"
          :data="paginatedResults"
          style="width: 100%"
          @selection-change="handleSelectionChange"
          row-key="id"
          v-loading="loading"
          border
          stripe
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="建议ID" width="110" />
          <el-table-column prop="planId" label="计划ID" width="110" />
          <el-table-column prop="type" label="类型" width="110">
            <template #default="scope">
              <el-tag :type="getTypeTag(scope.row.type)">
                {{ getTypeLabel(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="materialCode" label="物料编码" width="160" />
          <el-table-column prop="materialName" label="物料名称" min-width="200" />
          <el-table-column prop="quantity" label="建议数量" width="120" align="right" />
          <el-table-column prop="suggestDate" label="建议日期" width="120" />
          <el-table-column prop="requiredDate" label="需求日期" width="120" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusTag(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="confirmedBy" label="确认人" width="110" />
          <el-table-column prop="confirmedTime" label="确认时间" width="180" />
          <el-table-column prop="rejectedReason" label="拒绝原因" min-width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" @click="confirmResult(scope.row)" :disabled="scope.row.status !== 'PENDING'">
                确认
              </el-button>
              <el-button size="small" type="danger" @click="rejectResult(scope.row)" :disabled="scope.row.status === 'RELEASED'">
                拒绝
              </el-button>
              <el-button size="small" type="info" @click="copyPlanId(scope.row.planId)">
                复制计划ID
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="filteredResults.length"
          />
        </div>
      </el-card>
    </div>

  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRoute } from 'vue-router';
import { Check, Upload, Close, Download, Refresh } from '@element-plus/icons-vue';
import { forecastApi } from '@/api/scm';
import { extractArray } from '../scm-utils';

const props = withDefaults(
  defineProps<{
    embedded?: boolean;
    initialPlanId?: number;
  }>(),
  {
    embedded: false
  }
);

const embedded = computed(() => props.embedded);

const route = useRoute();

const filterForm = reactive({
  planId: '',
  type: '',
  status: '',
  materialKeyword: '',
  requiredDateRange: [] as any[]
});

const planIdNumber = computed(() => {
  const raw = filterForm.planId || (typeof props.initialPlanId === 'number' ? String(props.initialPlanId) : (route.query.planId as any));
  const n = Number(raw);
  return Number.isFinite(n) && n > 0 ? n : undefined;
});

const mrpResults = ref<any[]>([]);
const currentPage = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const selectedRows = ref<any[]>([]);
const tableRef = ref();

const normalizeDateString = (val: any): string | undefined => {
  if (!val) return undefined;
  if (typeof val === 'string') return val.slice(0, 10);
  try {
    return new Date(val).toISOString().slice(0, 10);
  } catch {
    return undefined;
  }
};

const filteredResults = computed(() => {
  const keyword = (filterForm.materialKeyword || '').trim();
  const type = filterForm.type;
  const status = filterForm.status;
  const range = filterForm.requiredDateRange;
  const start = Array.isArray(range) && range.length === 2 ? normalizeDateString(range[0]) : undefined;
  const end = Array.isArray(range) && range.length === 2 ? normalizeDateString(range[1]) : undefined;

  return mrpResults.value.filter((row) => {
    if (type && row?.type !== type) return false;
    if (status && row?.status !== status) return false;
    if (keyword) {
      const code = String(row?.materialCode ?? '');
      const name = String(row?.materialName ?? '');
      if (!code.includes(keyword) && !name.includes(keyword)) return false;
    }
    if (start && end) {
      const d = normalizeDateString(row?.requiredDate);
      if (!d) return false;
      if (d < start || d > end) return false;
    }
    return true;
  });
});

const paginatedResults = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  return filteredResults.value.slice(startIndex, startIndex + pageSize.value);
});

const fetchResults = async () => {
  if (!planIdNumber.value) return;
  loading.value = true;
  try {
    const res = await forecastApi.getMrpResults(planIdNumber.value);
    mrpResults.value = extractArray(res);
  } catch (error) {
    mrpResults.value = [];
    ElMessage.error('获取MRP结果失败');
  } finally {
    loading.value = false;
  }
};

watch(
  () => route.query.planId,
  (val) => {
    if (typeof val === 'string') filterForm.planId = val;
  },
  { immediate: true }
);

watch(
  () => props.initialPlanId,
  (val) => {
    if (typeof val === 'number' && val > 0) filterForm.planId = String(val);
  },
  { immediate: true }
);

onMounted(() => {
  if (planIdNumber.value) fetchResults();
});

const handleSearch = () => {
  currentPage.value = 1;
  fetchResults();
};

const resetFilter = () => {
  const keep = filterForm.planId;
  Object.assign(filterForm, {
    planId: keep,
    type: '',
    status: '',
    materialKeyword: '',
    requiredDateRange: []
  });
  currentPage.value = 1;
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
};

const handleCurrentChange = (current: number) => {
  currentPage.value = current;
};

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection;
};

const batchConfirm = async () => {
  const ids = selectedRows.value.map((r) => r?.id).filter((v) => typeof v === 'number');
  if (ids.length === 0) return;
  try {
    await forecastApi.batchConfirmMrpResults(ids, 'SYSTEM');
    ElMessage.success(`已确认${ids.length}条`);
    selectedRows.value = [];
    tableRef.value?.clearSelection?.();
    fetchResults();
  } catch (e) {
    ElMessage.error('批量确认失败');
  }
};

const batchReject = async () => {
  const ids = selectedRows.value.map((r) => r?.id).filter((v) => typeof v === 'number');
  if (ids.length === 0) return;
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '批量拒绝', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '拒绝原因不能为空'
    });
    await forecastApi.batchRejectMrpResults(ids, value, 'SYSTEM');
    ElMessage.success(`已拒绝${ids.length}条`);
    selectedRows.value = [];
    tableRef.value?.clearSelection?.();
    fetchResults();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('批量拒绝失败');
  }
};

const confirmResult = async (row: any) => {
  const id = row?.id;
  if (typeof id !== 'number') return;
  try {
    await forecastApi.confirmMrpResult(id, 'SYSTEM');
    ElMessage.success('确认成功');
    fetchResults();
  } catch {
    ElMessage.error('确认失败');
  }
};

const rejectResult = async (row: any) => {
  const id = row?.id;
  if (typeof id !== 'number') return;
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '拒绝原因不能为空'
    });
    await forecastApi.rejectMrpResult(id, value, 'SYSTEM');
    ElMessage.success('拒绝成功');
    fetchResults();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('拒绝失败');
  }
};

const releasePlan = async () => {
  if (!planIdNumber.value) return;
  try {
    await ElMessageBox.confirm('将下达当前计划并尝试生成采购申请/生产任务，是否继续？', '下达计划', {
      confirmButtonText: '继续',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await forecastApi.releasePlan(planIdNumber.value);
    ElMessage.success('计划下达成功');
    fetchResults();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('计划下达失败');
  }
};

const exportResults = () => {
  const rows = filteredResults.value;
  if (rows.length === 0) return;
  const header = ['id', 'planId', 'type', 'materialCode', 'materialName', 'quantity', 'suggestDate', 'requiredDate', 'status'];
  const csv = [
    header.join(','),
    ...rows.map((r) =>
      header
        .map((k) => {
          const v = r?.[k];
          const s = v == null ? '' : String(v).replace(/\"/g, '""');
          return `"${s}"`;
        })
        .join(',')
    )
  ].join('\\n');

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `mrp-results-${planIdNumber.value ?? 'all'}.csv`;
  a.click();
  URL.revokeObjectURL(url);
};

const refreshResults = () => {
  fetchResults();
};

const copyPlanId = async (id: any) => {
  if (id == null) return;
  try {
    await navigator.clipboard.writeText(String(id));
    ElMessage.success('已复制');
  } catch {
    ElMessage.error('复制失败');
  }
};

const getTypeTag = (type: string): string => {
  if (type === 'PURCHASE') return 'warning';
  if (type === 'PRODUCTION') return 'success';
  return 'info';
};

const getTypeLabel = (type: string): string => {
  if (type === 'PURCHASE') return '采购建议';
  if (type === 'PRODUCTION') return '生产建议';
  return type || '-';
};

const getStatusTag = (status: string): string => {
  if (status === 'PENDING') return 'warning';
  if (status === 'CONFIRMED') return 'success';
  if (status === 'REJECTED') return 'danger';
  if (status === 'RELEASED') return 'info';
  if (status === 'FAILED') return 'danger';
  return '';
};

const getStatusText = (status: string): string => {
  if (status === 'PENDING') return '待处理';
  if (status === 'CONFIRMED') return '已确认';
  if (status === 'REJECTED') return '已拒绝';
  if (status === 'RELEASED') return '已发布';
  if (status === 'FAILED') return '失败';
  return status || '-';
};
</script>

<style scoped>
.plan-result-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.plan-result-container.embedded {
  padding: 0;
  background-color: transparent;
  min-height: auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.page-header p {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-card,
.action-card,
.result-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  width: 100%;
}

.filter-actions {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.result-count {
  font-size: 14px;
  color: #606266;
  margin-left: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.detail-content .el-descriptions {
  margin-bottom: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .plan-result-container {
    padding: 10px;
  }
  
  .filter-form {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .filter-actions {
    margin-top: 10px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>
