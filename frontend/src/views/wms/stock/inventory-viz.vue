<template>
  <WmsPageLayout title="库存可视化" class="inventory-viz-container">
    <template #actions>
      <el-button type="primary" @click="handleRefreshData">刷新数据</el-button>
      <el-button @click="exportData">导出数据</el-button>
    </template>

    <div class="overview-section">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">总库存数量</div>
              <div class="stat-value">{{ totalInventory }}</div>
              <div class="stat-trend">
                <span class="trend-up">+5.2%</span>
                <span class="trend-text">较上月</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">总物料种类</div>
              <div class="stat-value">{{ totalMaterials }}</div>
              <div class="stat-trend">
                <span class="trend-up">+2.8%</span>
                <span class="trend-text">较上月</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">库存周转率</div>
              <div class="stat-value">{{ inventoryTurnover }}次</div>
              <div class="stat-trend">
                <span class="trend-down">-1.3%</span>
                <span class="trend-text">较上月</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">库存预警数</div>
              <div class="stat-value warning">{{ inventoryAlerts }}</div>
              <div class="stat-trend">
                <span class="trend-up">+12.5%</span>
                <span class="trend-text">较上月</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 库存趋势图 -->
    <div class="chart-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <h3>库存趋势</h3>
            <el-select v-model="trendPeriod" placeholder="选择周期" size="small">
              <el-option label="近7天" value="7d"></el-option>
              <el-option label="近30天" value="30d"></el-option>
              <el-option label="近90天" value="90d"></el-option>
            </el-select>
          </div>
        </template>
        <div class="chart-container">
          <div id="trend-chart" style="width: 100%; height: 360px;"></div>
        </div>
      </el-card>
    </div>
    
    <!-- 库存分布和预警 -->
    <div class="detail-section">
      <el-row :gutter="20">
        <!-- 库存分布 -->
        <el-col :span="16">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <h3>库存分布</h3>
                <el-select v-model="distributionType" placeholder="分布类型" size="small">
                  <el-option label="按仓库" value="warehouse"></el-option>
                  <el-option label="按物料类别" value="category"></el-option>
                  <el-option label="按库位" value="location"></el-option>
                </el-select>
              </div>
            </template>
            <div class="chart-container">
              <div id="distribution-chart" style="width: 100%; height: 400px;"></div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 库存预警 -->
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <h3>库存预警</h3>
                <span class="alert-count">{{ inventoryAlerts }}条</span>
              </div>
            </template>
            <div class="alert-list">
              <div v-if="inventoryAlerts > 0" class="alert-item" v-for="alert in alertList" :key="alert.id">
                <div class="alert-type" :class="alert.level">{{ alert.type }}</div>
                <div class="alert-content">
                  <div class="material-name">{{ alert.materialName }}</div>
                  <div class="alert-message">{{ alert.message }}</div>
                </div>
                <div class="alert-time">{{ alert.time }}</div>
              </div>
              <div v-else class="no-alerts">
                <el-empty description="暂无库存预警"></el-empty>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 库存明细 -->
    <div class="detail-table-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <h3>库存明细</h3>
          </div>
        </template>
        <!-- 搜索和筛选区域 -->
        <div class="filter-section">
          <el-form :model="searchForm" label-width="80px" inline>
            <el-form-item label="仓库">
              <el-select v-model="searchForm.warehouseCode" placeholder="选择仓库" clearable>
                <el-option label="全部" value=""></el-option>
                <el-option v-for="w in warehouses" :key="w.value" :label="w.label" :value="w.value"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="物料编码">
              <el-input v-model="searchForm.materialCode" placeholder="输入物料编码" clearable></el-input>
            </el-form-item>
            <el-form-item label="物料名称">
              <el-input v-model="searchForm.materialName" placeholder="输入物料名称" clearable></el-input>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
                <el-option label="全部" value=""></el-option>
                <el-option label="正常" value="normal"></el-option>
                <el-option label="库存不足" value="lowstock"></el-option>
                <el-option label="库存超量" value="overstock"></el-option>
                <el-option label="已过期" value="expired"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table :data="inventoryList" stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="warehouseName" label="仓库" width="120"></el-table-column>
          <el-table-column prop="locationCode" label="库位" width="100"></el-table-column>
          <el-table-column prop="materialCode" label="物料编码" width="150"></el-table-column>
          <el-table-column prop="materialName" label="物料名称" width="200"></el-table-column>
          <el-table-column prop="specification" label="规格" width="150"></el-table-column>
          <el-table-column prop="batchNo" label="批次号" width="150"></el-table-column>
          <el-table-column prop="quantity" label="库存数量" width="100"></el-table-column>
          <el-table-column prop="unit" label="单位" width="80"></el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-container">
          <el-pagination
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pageSize"
            :current-page="currentPage"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          ></el-pagination>
        </div>
      </el-card>
    </div>
  </WmsPageLayout>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount, onMounted, watch, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import { inventoryAnalyticsApi, inventoryV1Api, warehouseApi } from '@/api/wms';
import WmsPageLayout from '../components/WmsPageLayout.vue';

const loading = ref(false);

const totalInventory = ref('0');
const totalMaterials = ref(0);
const inventoryTurnover = ref(0);
const inventoryAlerts = ref(0);

const trendPeriod = ref('30d');
const distributionType = ref('warehouse');

const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(20);

const alertList = ref<any[]>([]);

const warehouses = ref<{ label: string; value: string }[]>([]);
const warehouseNameByCode = ref<Record<string, string>>({});

const searchForm = ref({
  warehouseCode: '',
  materialCode: '',
  materialName: '',
  status: ''
});

type InventoryRow = {
  id: string | number;
  warehouseCode: string;
  warehouseName: string;
  locationCode: string;
  materialCode: string;
  materialName: string;
  specification: string;
  batchNo: string;
  quantity: number;
  unit: string;
  status: string;
};

const inventoryList = ref<InventoryRow[]>([]);

const toNumber = (value: any): number => {
  const n = Number(value);
  return Number.isFinite(n) ? n : 0;
};

const toComputedStatus = (quantity: number): string => {
  if (quantity < 10) return 'lowstock';
  if (quantity > 1000) return 'overstock';
  return 'normal';
};

const toInventoryRow = (entity: any): InventoryRow => {
  const qty = toNumber(entity?.quantity);
  const warehouseCode = String(entity?.warehouseCode ?? '');
  return {
    id: entity?.id ?? '',
    warehouseCode,
    warehouseName: warehouseNameByCode.value[warehouseCode] ?? warehouseCode,
    locationCode: String(entity?.locationCode ?? ''),
    materialCode: String(entity?.materialCode ?? ''),
    materialName: String(entity?.materialName ?? ''),
    specification: '',
    batchNo: String(entity?.batchNo ?? ''),
    quantity: qty,
    unit: String(entity?.unit ?? ''),
    status: toComputedStatus(qty)
  };
};

const toList = (data: any): any[] => {
  if (Array.isArray(data)) return data;
  if (data && typeof data === 'object') {
    const list = (data as any).records || (data as any).list || (data as any).content;
    if (Array.isArray(list)) return list;
  }
  return [];
};

// 状态映射
const statusMap = {
  normal: '正常',
  lowstock: '库存不足',
  overstock: '库存超量',
  expired: '已过期'
};

const statusTagTypeMap = {
  normal: 'success',
  lowstock: 'warning',
  overstock: 'danger',
  expired: 'danger'
};

// 方法
const getStatusText = (status: string) => {
  return statusMap[status as keyof typeof statusMap] || status;
};

const getStatusTagType = (status: string) => {
  return statusTagTypeMap[status as keyof typeof statusTagTypeMap] || 'info';
};

// 刷新数据
const handleRefreshData = async () => {
  await Promise.all([fetchSummary(), fetchAlerts(), fetchTrend(), fetchDistribution(), fetchInventoryList()]);
  ElMessage.success('数据刷新成功');
};

// 导出数据
const exportData = () => {
  // 将数据导出为CSV格式
  const csvContent = convertToCSV(inventoryList.value);
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  if (link.download !== undefined) {
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `库存明细_${new Date().toISOString().slice(0, 10)}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    ElMessage.success('数据导出成功');
  }
};

// 将数据转换为CSV格式
const convertToCSV = (data: any[]) => {
  if (!data || data.length === 0) return '';
  
  // 定义CSV表头
  const headers = ['仓库', '库位', '物料编码', '物料名称', '规格', '批次号', '库存数量', '单位', '状态'];
  
  // 定义CSV行数据
  const rows = data.map(item => [
    item.warehouseName || '',
    item.locationCode || '',
    item.materialCode || '',
    item.materialName || '',
    item.specification || '',
    item.batchNo || '',
    item.quantity || 0,
    item.unit || '',
    getStatusText(item.status) || ''
  ]);
  
  // 拼接CSV内容
  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.join(','))
  ].join('\n');
  
  return csvContent;
};

let trendChart: echarts.ECharts | undefined;
let distributionChart: echarts.ECharts | undefined;
let resizeHandler: (() => void) | undefined;

const initTrendChart = () => {
  const chartDom = document.getElementById('trend-chart');
  if (!chartDom) return;

  trendChart = echarts.init(chartDom);
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['库存数量']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '库存数量',
        type: 'line',
        stack: 'Total',
        data: [],
        smooth: true,
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  };

  trendChart.setOption(option);
};

// 初始化分布图表
const initDistributionChart = () => {
  const chartDom = document.getElementById('distribution-chart');
  if (!chartDom) return;

  distributionChart = echarts.init(chartDom);
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '库存分布',
        type: 'pie',
        radius: '50%',
        data: [],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        // 添加点击事件，显示详细信息
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          formatter: '{b}: {c} ({d}%)'
        }
      }
    ]
  };

  distributionChart.setOption(option);

  distributionChart.on('click', (params: any) => {
    console.log('点击了图表', params);
    ElMessage.info(`您点击了${params.name}，库存数量：${params.value}`);
  });
};

// 搜索功能
const handleSearch = () => {
  currentPage.value = 1;
  fetchInventoryList();
};

// 重置功能
const handleReset = () => {
  searchForm.value = {
    warehouseCode: '',
    materialCode: '',
    materialName: '',
    status: ''
  };
  currentPage.value = 1;
  fetchInventoryList();
};

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchInventoryList();
};

// 处理当前页变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchInventoryList();
};

const fetchWarehouses = async () => {
  const res = await warehouseApi.getList({ page: 0, size: 1000 });
  const pageData = res.data || {};
  const list = (pageData.content || pageData.list || pageData.records || []) as any[];
  const map: Record<string, string> = {};
  warehouses.value = list
    .map((w: any) => {
      const code = String(w?.warehouseCode ?? '');
      const name = String(w?.warehouseName ?? code);
      if (code) {
        map[code] = name;
      }
      return { label: name, value: code };
    })
    .filter(v => v.value);
  warehouseNameByCode.value = map;
};

const fetchSummary = async () => {
  const res = await inventoryAnalyticsApi.getSummary();
  const data = res.data || {};
  totalInventory.value = String(data.totalQuantity ?? '0');
  totalMaterials.value = Number(data.materialCount ?? 0);
  inventoryTurnover.value = Number(data.turnover ?? 0);
  inventoryAlerts.value = Number(data.alertCount ?? 0);
};

const fetchAlerts = async () => {
  const res = await inventoryAnalyticsApi.getAlerts({ limit: 20 });
  const list = toList(res.data);
  alertList.value = list;
  inventoryAlerts.value = list.length;
};

const fetchInventoryList = async () => {
  loading.value = true;
  try {
    const params: any = {
      // 后端统一1基页码
      page: Math.max(currentPage.value, 1),
      size: pageSize.value
    };
    if (searchForm.value.warehouseCode) params.warehouseCode = searchForm.value.warehouseCode;
    if (searchForm.value.materialCode) params.materialCode = searchForm.value.materialCode;
    if (searchForm.value.materialName) params.materialName = searchForm.value.materialName;
    if (searchForm.value.status) params.status = searchForm.value.status;

    const res = await inventoryV1Api.getList(params);
    const pageData = res.data || {};
    const content = toList(pageData);
    total.value = Number(pageData.totalElements ?? pageData.total ?? content.length ?? 0);
    inventoryList.value = content.map(toInventoryRow);
  } catch (e: any) {
    ElMessage.error(e?.message || '获取库存明细失败');
  } finally {
    loading.value = false;
  }
};

const fetchTrend = async () => {
  const period = trendPeriod.value;
  const days = period === '7d' ? 7 : period === '90d' ? 90 : 30;
  const res = await inventoryAnalyticsApi.getTrend({ days });
  const series = toList(res.data);
  const dates = series.map(v => String(v?.date ?? ''));
  const values = series.map(v => toNumber(v?.quantity));
  trendChart?.setOption({
    xAxis: { data: dates },
    series: [{ data: values }]
  });
};

const fetchDistribution = async () => {
  const res = await inventoryAnalyticsApi.getDistribution({ type: distributionType.value });
  const items = toList(res.data);
  distributionChart?.setOption({
    series: [
      {
        data: items.map(v => ({
          name: String(v?.name ?? ''),
          value: toNumber(v?.value)
        }))
      }
    ]
  });
};

watch(trendPeriod, () => {
  fetchTrend();
});

watch(distributionType, () => {
  fetchDistribution();
});

// 组件挂载时初始化图表
onMounted(() => {
  nextTick(async () => {
    initTrendChart();
    initDistributionChart();
    resizeHandler = () => {
      trendChart?.resize();
      distributionChart?.resize();
    };
    window.addEventListener('resize', resizeHandler);
    await fetchWarehouses();
    await Promise.all([fetchSummary(), fetchAlerts(), fetchTrend(), fetchDistribution(), fetchInventoryList()]);
  });
});

onBeforeUnmount(() => {
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler);
  }
  trendChart?.dispose();
  distributionChart?.dispose();
});
</script>

<style scoped>
.inventory-viz-container {
  padding: 0;
}

.overview-section {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.stat-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 15px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-trend {
  display: flex;
  align-items: center;
  font-size: 12px;
}

.trend-up {
  color: #67c23a;
  margin-right: 5px;
}

.trend-down {
  color: #f56c6c;
  margin-right: 5px;
}

.trend-text {
  color: #909399;
}

.chart-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.chart-container {
  margin-top: 12px;
}

.detail-section {
  margin-bottom: 20px;
}

.alert-list {
  max-height: 360px;
  overflow-y: auto;
}

.alert-item {
  display: flex;
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
}

.alert-item:last-child {
  border-bottom: none;
}

.alert-type {
  width: 80px;
  text-align: center;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  margin-right: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.alert-type.warning {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.alert-type.danger {
  background-color: #fef0f0;
  color: #f56c6c;
}

.alert-content {
  flex: 1;
}

.material-name {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 5px;
  color: #303133;
}

.alert-message {
  font-size: 12px;
  color: #606266;
}

.alert-time {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
}

.alert-count {
  color: #f56c6c;
  font-size: 12px;
  font-weight: bold;
}

.no-alerts {
  padding: 20px 0;
  text-align: center;
}

.filter-section {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #fafafa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.detail-table-section {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .el-col {
    margin-bottom: 20px;
  }
  
  .el-col:last-child {
    margin-bottom: 0;
  }
  
  .overview-section .el-col {
    margin-bottom: 0;
  }
}
</style>
