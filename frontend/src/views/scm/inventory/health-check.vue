<template>
  <div class="inventory-health-container">
    <!-- 页面标题和面包屑导航 -->
    <div class="page-header">
      <h2>库存健康度分析</h2>
      <el-breadcrumb aria-label="Breadcrumb" role="navigation">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/scm' }">SCM系统</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/scm/inventory-optimization' }">库存优化</el-breadcrumb-item>
        <el-breadcrumb-item>库存健康度分析</el-breadcrumb-item>
      </el-breadcrumb>
      <p>分析库存健康度，包括周转率、呆滞率、库存天数等指标，并展示库存分布和趋势</p>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 健康度指标概览 -->
      <div class="metrics-grid">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-content">
            <div class="metric-title">库存周转率</div>
            <div class="metric-value">5.8</div>
            <div class="metric-unit">次/年</div>
            <div class="metric-trend positive">
              <el-icon><TrendCharts /></el-icon>
              <span>较上月 +12.5%</span>
            </div>
          </div>
        </el-card>

        <el-card shadow="hover" class="metric-card">
          <div class="metric-content">
            <div class="metric-title">库存周转天数</div>
            <div class="metric-value">63</div>
            <div class="metric-unit">天</div>
            <div class="metric-trend negative">
              <el-icon><TrendCharts /></el-icon>
              <span>较上月 +3.2%</span>
            </div>
          </div>
        </el-card>

        <el-card shadow="hover" class="metric-card">
          <div class="metric-content">
            <div class="metric-title">呆滞物料占比</div>
            <div class="metric-value">8.5</div>
            <div class="metric-unit">%</div>
            <div class="metric-trend positive">
              <el-icon><TrendCharts /></el-icon>
              <span>较上月 -2.1%</span>
            </div>
          </div>
        </el-card>

        <el-card shadow="hover" class="metric-card">
          <div class="metric-content">
            <div class="metric-title">库存积压金额</div>
            <div class="metric-value">256.3</div>
            <div class="metric-unit">万元</div>
            <div class="metric-trend negative">
              <el-icon><TrendCharts /></el-icon>
              <span>较上月 +5.8%</span>
            </div>
          </div>
        </el-card>

        <el-card shadow="hover" class="metric-card">
          <div class="metric-content">
            <div class="metric-title">库存短缺金额</div>
            <div class="metric-value">45.7</div>
            <div class="metric-unit">万元</div>
            <div class="metric-trend positive">
              <el-icon><TrendCharts /></el-icon>
              <span>较上月 -15.3%</span>
            </div>
          </div>
        </el-card>

        <el-card shadow="hover" class="metric-card">
          <div class="metric-content">
            <div class="metric-title">库存准确率</div>
            <div class="metric-value">98.2</div>
            <div class="metric-unit">%</div>
            <div class="metric-trend positive">
              <el-icon><TrendCharts /></el-icon>
              <span>较上月 +0.5%</span>
            </div>
          </div>
        </el-card>
      </div>

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
              <el-form-item label="仓库">
                <el-select v-model="filterForm.warehouse" placeholder="选择仓库" style="width: 100%">
                  <el-option label="全部" value="" />
                  <el-option label="主仓库" value="main_warehouse" />
                  <el-option label="区域仓库1" value="regional_warehouse_1" />
                  <el-option label="区域仓库2" value="regional_warehouse_2" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="物料类别">
                <el-select v-model="filterForm.materialType" placeholder="选择物料类别" style="width: 100%">
                  <el-option label="全部" value="" />
                  <el-option label="原材料" value="raw_material" />
                  <el-option label="半成品" value="semi_finished" />
                  <el-option label="成品" value="finished_product" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="ABC分类">
                <el-select v-model="filterForm.abcClass" placeholder="选择ABC分类" style="width: 100%">
                  <el-option label="全部" value="" />
                  <el-option label="A类" value="A" />
                  <el-option label="B类" value="B" />
                  <el-option label="C类" value="C" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="时间范围">
                <el-date-picker
                  v-model="filterForm.timeRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12" class="filter-actions">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="resetFilter">重置</el-button>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 图表区域 -->
      <div class="charts-grid">
        <!-- 库存分布饼图 -->
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>库存分布（按物料类别）</span>
            </div>
          </template>
          <div class="chart-content">
            <div ref="inventoryDistributionChart" class="chart" style="width: 100%; height: 350px;"></div>
          </div>
        </el-card>

        <!-- 周转率趋势图 -->
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>库存周转率趋势</span>
            </div>
          </template>
          <div class="chart-content">
            <div ref="turnoverTrendChart" class="chart" style="width: 100%; height: 350px;"></div>
          </div>
        </el-card>

        <!-- 库存金额趋势图 -->
        <el-card shadow="hover" class="chart-card full-width">
          <template #header>
            <div class="card-header">
              <span>库存金额趋势</span>
            </div>
          </template>
          <div class="chart-content">
            <div ref="inventoryValueTrendChart" class="chart" style="width: 100%; height: 400px;"></div>
          </div>
        </el-card>

        <!-- 呆滞物料分析 -->
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>呆滞物料分析</span>
            </div>
          </template>
          <div class="chart-content">
            <div ref="slowMovingChart" class="chart" style="width: 100%; height: 350px;"></div>
          </div>
        </el-card>

        <!-- 库存ABC分析 -->
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>库存ABC分析</span>
            </div>
          </template>
          <div class="chart-content">
            <div ref="abcAnalysisChart" class="chart" style="width: 100%; height: 350px;"></div>
          </div>
        </el-card>
      </div>

      <!-- 库存预警列表卡片 -->
      <el-card shadow="hover" class="alert-card">
        <template #header>
          <div class="card-header">
            <span>库存预警列表</span>
            <el-button type="primary" size="small" @click="refreshAlerts">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
          </div>
        </template>

        <el-table :data="inventoryAlerts" style="width: 100%">
          <el-table-column prop="alertId" label="预警ID" width="120" />
          <el-table-column prop="materialCode" label="物料编码" width="150" />
          <el-table-column prop="materialName" label="物料名称" min-width="200" />
          <el-table-column prop="warehouseName" label="仓库" width="120" />
          <el-table-column prop="alertType" label="预警类型" width="120">
            <template #default="scope">
              <el-tag :type="getAlertTypeTag(scope.row.alertType)">
                {{ scope.row.alertType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="currentStock" label="当前库存" width="120" align="right" />
          <el-table-column prop="threshold" label="阈值" width="100" align="right" />
          <el-table-column prop="alertLevel" label="预警级别" width="100">
            <template #default="scope">
              <el-tag :type="getAlertLevelTag(scope.row.alertLevel)">
                {{ scope.row.alertLevel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alertTime" label="预警时间" width="180" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" @click="handleAlert(scope.row)">处理</el-button>
              <el-button size="small" @click="ignoreAlert(scope.row)">忽略</el-button>
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
            :total="inventoryAlerts.length"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { TrendCharts, Refresh } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { forecastApi } from '@/api/scm';

// 筛选表单数据
const filterForm = reactive({
  warehouse: '',
  materialType: '',
  abcClass: '',
  timeRange: [] as any[]
});

// 分页数据
const currentPage = ref(1);
const pageSize = ref(10);

// 图表引用
const inventoryDistributionChart = ref<HTMLElement | null>(null);
const turnoverTrendChart = ref<HTMLElement | null>(null);
const inventoryValueTrendChart = ref<HTMLElement | null>(null);
const slowMovingChart = ref<HTMLElement | null>(null);
const abcAnalysisChart = ref<HTMLElement | null>(null);

// 图表实例
let inventoryDistributionInstance: echarts.ECharts | null = null;
let turnoverTrendInstance: echarts.ECharts | null = null;
let inventoryValueTrendInstance: echarts.ECharts | null = null;
let slowMovingInstance: echarts.ECharts | null = null;
let abcAnalysisInstance: echarts.ECharts | null = null;

// 库存预警数据
const inventoryAlerts = ref<any[]>([]);
const distributionData = ref<any[]>([]);
const turnoverTrendData = ref<any[]>([]);

const loadAlerts = async () => {
  try {
    const res: any = await forecastApi.getInventoryHealthAlerts(100);
    const rows = res?.data?.list || res?.data?.records || [];
    inventoryAlerts.value = (Array.isArray(rows) ? rows : []).map((a: any, idx: number) => ({
      alertId: a.alertId || `ALERT-${a.materialCode}-${idx + 1}`,
      materialCode: a.materialCode,
      materialName: a.materialName,
      warehouseName: a.warehouseName || '全部仓库',
      alertType: a.type === 'LOW_STOCK' ? '库存短缺' : '预警',
      currentStock: a.quantity,
      threshold: a.reorderPoint,
      alertLevel: a.severity === 'HIGH' ? '严重' : '警告',
      alertTime: a.time ? String(a.time).replace('T', ' ').slice(0, 19) : ''
    }));
  } catch (e) {
    inventoryAlerts.value = [];
  }
};

const loadHealthCharts = async () => {
  try {
    const res: any = await forecastApi.getInventoryHealthDistribution('abc');
    distributionData.value = res?.data?.list || res?.data?.records || [];
  } catch (e) {
    distributionData.value = [];
  }
  try {
    const res: any = await forecastApi.getInventoryHealthTurnoverTrend(30);
    turnoverTrendData.value = res?.data?.list || res?.data?.records || [];
  } catch (e) {
    turnoverTrendData.value = [];
  }
};

// 初始化图表
onMounted(() => {
  loadAlerts();
  loadHealthCharts();
  // 使用nextTick确保DOM渲染完成
  nextTick(() => {
    initCharts();
  });
  window.addEventListener('resize', handleResize, { passive: true });
});

// 销毁图表
onUnmounted(() => {
  destroyCharts();
  window.removeEventListener('resize', handleResize);
});

// 初始化所有图表
const initCharts = () => {
  // 检查DOM尺寸并初始化图表的函数
  const checkAndInit = () => {
    // 检查所有图表容器是否都有有效的尺寸
    const hasValidSize = (el: HTMLElement | null) => {
      return el && el.clientWidth > 0 && el.clientHeight > 0;
    };
    
    // 销毁现有图表实例
    destroyCharts();
    
    // 库存分布饼图
    if (hasValidSize(inventoryDistributionChart.value)) {
      inventoryDistributionInstance = echarts.init(inventoryDistributionChart.value);
      inventoryDistributionInstance.setOption(getInventoryDistributionOption());
    }

    // 周转率趋势图
    if (hasValidSize(turnoverTrendChart.value)) {
      turnoverTrendInstance = echarts.init(turnoverTrendChart.value);
      turnoverTrendInstance.setOption(getTurnoverTrendOption());
    }

    // 库存金额趋势图
    if (hasValidSize(inventoryValueTrendChart.value)) {
      inventoryValueTrendInstance = echarts.init(inventoryValueTrendChart.value);
      inventoryValueTrendInstance.setOption(getInventoryValueTrendOption());
    }

    // 呆滞物料分析
    if (hasValidSize(slowMovingChart.value)) {
      slowMovingInstance = echarts.init(slowMovingChart.value);
      slowMovingInstance.setOption(getSlowMovingOption());
    }

    // 库存ABC分析
    if (hasValidSize(abcAnalysisChart.value)) {
      abcAnalysisInstance = echarts.init(abcAnalysisChart.value);
      abcAnalysisInstance.setOption(getAbcAnalysisOption());
    }
    
    // 检查是否所有图表都已成功初始化
    const allChartsInitialized = !!(inventoryDistributionInstance && turnoverTrendInstance && 
                                  inventoryValueTrendInstance && slowMovingInstance && 
                                  abcAnalysisInstance);
    
    if (!allChartsInitialized) {
      // 如果还有图表未初始化，延迟100ms后重试
      setTimeout(checkAndInit, 100);
    }
  };
  
  // 开始检查和初始化
  checkAndInit();
};

// 销毁所有图表
const destroyCharts = () => {
  inventoryDistributionInstance?.dispose();
  turnoverTrendInstance?.dispose();
  inventoryValueTrendInstance?.dispose();
  slowMovingInstance?.dispose();
  abcAnalysisInstance?.dispose();
};

// 处理窗口大小变化
const handleResize = () => {
  inventoryDistributionInstance?.resize();
  turnoverTrendInstance?.resize();
  inventoryValueTrendInstance?.resize();
  slowMovingInstance?.resize();
  abcAnalysisInstance?.resize();
};

// 库存分布饼图配置
const getInventoryDistributionOption = () => {
  const legendData = (distributionData.value || []).map((d: any) => d.name);
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: legendData
    },
    series: [
      {
        name: '库存分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: distributionData.value || []
      }
    ]
  };
};

// 周转率趋势图配置
const getTurnoverTrendOption = () => {
  const x = (turnoverTrendData.value || []).map((d: any) => d.date);
  const y = (turnoverTrendData.value || []).map((d: any) => d.outboundQuantity);
  return {
    tooltip: {
      trigger: 'axis'
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
      data: x
    },
    yAxis: {
      type: 'value',
      name: '出库量'
    },
    series: [
      {
        name: '库存周转率',
        type: 'line',
        stack: 'Total',
        data: y,
        smooth: true,
        itemStyle: {
          color: '#67C23A'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.5)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
          ])
        }
      }
    ]
  };
};

// 库存金额趋势图配置
const getInventoryValueTrendOption = () => {
  return {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['原材料', '半成品', '成品']
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
      name: '库存金额（万元）'
    },
    series: [
      {
        name: '原材料',
        type: 'line',
        stack: 'Total',
        data: [],
        smooth: true,
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '半成品',
        type: 'line',
        stack: 'Total',
        data: [],
        smooth: true,
        itemStyle: {
          color: '#E6A23C'
        }
      },
      {
        name: '成品',
        type: 'line',
        stack: 'Total',
        data: [],
        smooth: true,
        itemStyle: {
          color: '#F56C6C'
        }
      }
    ]
  };
};

// 呆滞物料分析配置
const getSlowMovingOption = () => {
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1-3个月', '3-6个月', '6-12个月', '12个月以上']
    },
    yAxis: {
      type: 'value',
      name: '呆滞金额（万元）'
    },
    series: [
      {
        name: '呆滞金额',
        type: 'bar',
        data: [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#FF6B6B' },
            { offset: 1, color: '#FF8E8E' }
          ])
        }
      }
    ]
  };
};

// 库存ABC分析配置
const getAbcAnalysisOption = () => {
  const sums: Record<string, number> = {};
  const total = (distributionData.value || []).reduce((acc: number, d: any) => acc + Number(d.value || 0), 0);
  for (const d of (distributionData.value || [])) {
    sums[String(d.name)] = Number(d.value || 0);
  }
  const cats = ['A', 'B', 'C'];
  const pct = cats.map((c) => total > 0 ? Number(((sums[c] || 0) * 100 / total).toFixed(1)) : 0);
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['A类', 'B类', 'C类']
    },
    yAxis: [
      {
        type: 'value',
        name: '库存金额占比（%）',
        axisLabel: {
          formatter: '{value} %'
        }
      },
      {
        type: 'value',
        name: '物料数量占比（%）',
        axisLabel: {
          formatter: '{value} %'
        }
      }
    ],
    series: [
      {
        name: '库存金额占比',
        type: 'bar',
        data: pct,
        itemStyle: {
          color: '#67C23A'
        }
      },
      {
        name: '物料数量占比',
        type: 'line',
        yAxisIndex: 1,
        data: pct,
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  };
};

// 处理搜索
const handleSearch = () => {
  ElMessage.info('搜索完成');
  // 实际项目中，这里会根据筛选条件重新加载数据和图表
};

// 重置筛选条件
const resetFilter = () => {
  Object.assign(filterForm, {
    warehouse: '',
    materialType: '',
    abcClass: '',
    timeRange: []
  });
  ElMessage.info('筛选条件已重置');
};

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
};

const handleCurrentChange = (current: number) => {
  currentPage.value = current;
};

// 刷新预警列表
const refreshAlerts = () => {
  loadAlerts().finally(() => {
    ElMessage.success('预警列表已刷新');
  });
};

// 处理预警
const handleAlert = (row: any) => {
  ElMessage.success(`已处理预警 ${row.alertId}`);
};

// 忽略预警
const ignoreAlert = (row: any) => {
  ElMessage.info(`已忽略预警 ${row.alertId}`);
};

// 获取预警类型标签
const getAlertTypeTag = (type: string): string => {
  switch (type) {
    case '库存短缺':
      return 'danger';
    case '库存积压':
      return 'warning';
    case '呆滞物料':
      return 'info';
    default:
      return 'info';
  }
};

// 获取预警级别标签
const getAlertLevelTag = (level: string): string => {
  switch (level) {
    case '严重':
      return 'danger';
    case '警告':
      return 'warning';
    case '提示':
      return 'info';
    default:
      return 'info';
  }
};
</script>

<style scoped>
.inventory-health-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
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

/* 健康度指标概览 */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.metric-card {
  height: 100%;
  transition: all 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.metric-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  text-align: center;
}

.metric-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.metric-value {
  font-size: 36px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.metric-unit {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.metric-trend {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
}

.metric-trend.positive {
  color: #67c23a;
}

.metric-trend.negative {
  color: #f56c6c;
}

/* 筛选卡片 */
.filter-card {
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

/* 图表区域 */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
  display: flex;
  flex-direction: column;
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.chart-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart {
  width: 100%;
  height: 100%;
}

.chart.full-height {
  height: 400px;
}

/* 预警卡片 */
.alert-card {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .inventory-health-container {
    padding: 10px;
  }
  
  .metrics-grid {
    grid-template-columns: 1fr;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-card {
    height: 300px;
  }
  
  .chart.full-height {
    height: 300px;
  }
  
  .filter-form {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .filter-actions {
    margin-top: 10px;
  }
}

@media (max-width: 480px) {
  .metric-value {
    font-size: 28px;
  }
  
  .chart-card {
    height: 250px;
  }
  
  .chart.full-height {
    height: 250px;
  }
}
</style>
