<template>
  <div class="kpi-monitoring-component">
    <h3>关键指标监控</h3>
    <p>实时监控供应链关键指标，包括交付率、库存周转率、订单满足率等</p>
    
    <!-- KPI指标卡片 -->
    <div class="kpi-cards">
      <el-card shadow="hover" class="kpi-card">
        <div class="kpi-content">
          <div class="kpi-title">低库存风险</div>
          <div class="kpi-value">{{ kpis.lowStockRiskCount }}</div>
        </div>
      </el-card>
      
      <el-card shadow="hover" class="kpi-card">
        <div class="kpi-content">
          <div class="kpi-title">MRP待发布</div>
          <div class="kpi-value">{{ kpis.mrpPendingReleaseCount }}</div>
        </div>
      </el-card>
      
      <el-card shadow="hover" class="kpi-card">
        <div class="kpi-content">
          <div class="kpi-title">MRP发布失败</div>
          <div class="kpi-value">{{ kpis.mrpReleaseFailedCount }}</div>
        </div>
      </el-card>
      
      <el-card shadow="hover" class="kpi-card">
        <div class="kpi-content">
          <div class="kpi-title">集成失败</div>
          <div class="kpi-value">{{ kpis.integrationFailedCount }}</div>
        </div>
      </el-card>
    </div>
    
    <!-- 指标趋势图表 -->
    <el-card shadow="hover" class="trend-card">
      <template #header>
        <div class="card-header">
          <span>指标趋势分析</span>
        </div>
      </template>
      <div class="card-content">
        <div ref="trendChart" class="chart-container"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import * as echarts from 'echarts';
import { forecastApi } from '@/api/scm';

const kpis = ref<any>({
  integrationFailedCount: 0,
  mrpReleaseFailedCount: 0,
  mrpPendingReleaseCount: 0,
  lowStockRiskCount: 0
});

// 图表引用
const trendChart = ref<HTMLElement | null>(null);
let chartInstance: echarts.ECharts | null = null;
const MAX_RETRIES = 5;
const RETRY_INTERVAL = 300;

// 初始化图表
onMounted(() => {
  forecastApi.getControlTowerKpis().then((res: any) => {
    kpis.value = res?.data || kpis.value;
  }).catch(() => {
  });
  initChartWithRetry(0);
  window.addEventListener('resize', handleResize, { passive: true });
});

// 销毁图表
onUnmounted(() => {
  chartInstance?.dispose();
  window.removeEventListener('resize', handleResize);
  chartInstance = null;
});

// 处理窗口大小变化
const handleResize = () => {
  chartInstance?.resize();
};

// 带重试机制的图表初始化
const initChartWithRetry = (retryCount: number) => {
  nextTick(() => {
    if (!trendChart.value) {
      return;
    }

    // 检查DOM元素是否有有效的尺寸
    const { offsetWidth, offsetHeight } = trendChart.value;
    if (offsetWidth === 0 || offsetHeight === 0) {
      // 如果尺寸为0，重试
      if (retryCount < MAX_RETRIES) {
        setTimeout(() => initChartWithRetry(retryCount + 1), RETRY_INTERVAL);
      }
      return;
    }

    // 初始化图表
    chartInstance = echarts.init(trendChart.value);
    chartInstance.setOption(getTrendChartOption());
    loadTrendData();
  });
};

const formatDate = (d: Date) => {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

const loadTrendData = async () => {
  if (!chartInstance) return;
  const to = new Date();
  const from = new Date();
  from.setDate(to.getDate() - 29);
  const fromStr = formatDate(from);
  const toStr = formatDate(to);
  try {
    const [lowStock, mrpPending, integrationFailed] = await Promise.all([
      forecastApi.getControlTowerKpiTrend('lowStockRiskCount', fromStr, toStr),
      forecastApi.getControlTowerKpiTrend('mrpPendingReleaseCount', fromStr, toStr),
      forecastApi.getControlTowerKpiTrend('integrationFailedCount', fromStr, toStr)
    ]);
    const lowStockList = lowStock?.data?.list || lowStock?.data?.records || [];
    const mrpPendingList = mrpPending?.data?.list || mrpPending?.data?.records || [];
    const integrationFailedList = integrationFailed?.data?.list || integrationFailed?.data?.records || [];
    const x = (Array.isArray(lowStockList) ? lowStockList : []).map((r: any) => r.date);
    chartInstance.setOption({
      xAxis: { data: x },
      series: [
        { data: (Array.isArray(lowStockList) ? lowStockList : []).map((r: any) => r.value) },
        { data: (Array.isArray(mrpPendingList) ? mrpPendingList : []).map((r: any) => r.value) },
        { data: (Array.isArray(integrationFailedList) ? integrationFailedList : []).map((r: any) => r.value) }
      ]
    });
  } catch (e) {
  }
};

// 获取趋势图表配置
const getTrendChartOption = () => {
  return {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['低库存风险', 'MRP待发布', '集成失败']
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
      type: 'value',
      name: '指标值'
    },
    series: [
      {
        name: '低库存风险',
        type: 'line',
        data: [],
        smooth: true,
        itemStyle: {
          color: '#67C23A'
        },
        yAxisIndex: 0
      },
      {
        name: 'MRP待发布',
        type: 'line',
        data: [],
        smooth: true,
        itemStyle: {
          color: '#409EFF'
        },
        yAxisIndex: 0
      },
      {
        name: '集成失败',
        type: 'line',
        data: [],
        smooth: true,
        itemStyle: {
          color: '#E6A23C'
        },
        yAxisIndex: 0
      }
    ]
  };
};
</script>

<style scoped>
.kpi-monitoring-component {
  padding: 20px;
}

/* KPI卡片样式 */
.kpi-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.kpi-card {
  height: 100%;
  transition: all 0.3s ease;
}

.kpi-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.kpi-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  text-align: center;
}

.kpi-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.kpi-value {
  font-size: 36px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.kpi-trend {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
}

.kpi-trend.positive {
  color: #67c23a;
}

.kpi-trend.negative {
  color: #f56c6c;
}

/* 趋势图表样式 */
.trend-card {
  height: 500px;
  display: flex;
  flex-direction: column;
}

.chart-container {
  flex: 1;
  width: 100%;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}
</style>
