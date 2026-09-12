<template>
  <div class="supply-chain-map-container" :class="{ embedded }">
    <!-- 页面标题 -->
    <div v-if="!embedded" class="page-header">
      <h2>供应链地图可视化</h2>
      <p>可视化展示供应链网络，包括工厂、仓库节点和物流路径，实时监控物流状态</p>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 控制区域 -->
      <el-card shadow="hover" class="control-card">
        <div class="control-content">
          <div class="control-section">
            <span class="control-label">节点类型：</span>
            <el-checkbox-group v-model="selectedNodeTypes">
              <el-checkbox label="工厂" value="工厂" />
              <el-checkbox label="仓库" value="仓库" />
            </el-checkbox-group>
          </div>
          <div class="control-section">
            <span class="control-label">物流状态：</span>
            <el-checkbox-group v-model="selectedLogisticsStatus">
              <el-checkbox label="正常" value="正常" />
              <el-checkbox label="延迟" value="延迟" />
              <el-checkbox label="异常" value="异常" />
            </el-checkbox-group>
          </div>
          <div class="control-section">
            <span class="control-label">视图切换：</span>
            <el-radio-group v-model="viewMode" @change="handleViewChange">
              <el-radio-button label="全局视图" value="全局视图" />
              <el-radio-button label="局部视图" value="局部视图" />
            </el-radio-group>
          </div>
          <div class="control-section">
            <el-button type="primary" @click="refreshMap">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
            <el-button @click="fitView">
              <el-icon><FullScreen /></el-icon> 全屏
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 地图和详情区域 -->
      <div class="map-detail-layout">
        <!-- 地图可视化 -->
        <el-card shadow="hover" class="map-card">
          <template #header>
            <div class="card-header">
              <span>供应链网络</span>
              <span class="map-legend">
                <span class="legend-item">
                  <span class="legend-dot factory-dot"></span>
                  <span class="legend-text">工厂</span>
                </span>
                <span class="legend-item">
                  <span class="legend-dot warehouse-dot"></span>
                  <span class="legend-text">仓库</span>
                </span>
                <span class="legend-item">
                  <span class="legend-line normal-line"></span>
                  <span class="legend-text">正常</span>
                </span>
                <span class="legend-item">
                  <span class="legend-line delay-line"></span>
                  <span class="legend-text">延迟</span>
                </span>
                <span class="legend-item">
                  <span class="legend-line exception-line"></span>
                  <span class="legend-text">异常</span>
                </span>
              </span>
            </div>
          </template>
          <div class="map-content">
            <div ref="supplyChainMap" class="map"></div>
          </div>
        </el-card>

        <!-- 详情面板 -->
        <el-card shadow="hover" class="detail-card">
          <template #header>
            <div class="card-header">
              <span>{{ activeDetailType === 'node' ? '节点详情' : '物流详情' }}</span>
              <el-button link @click="closeDetail">
                <el-icon><Close /></el-icon> 关闭
              </el-button>
            </div>
          </template>
          
          <!-- 节点详情 -->
          <div v-if="activeDetailType === 'node' && selectedNode" class="detail-content">
            <el-descriptions title="基本信息" :column="1" border>
              <el-descriptions-item label="节点ID">{{ selectedNode.id }}</el-descriptions-item>
              <el-descriptions-item label="节点名称">{{ selectedNode.name }}</el-descriptions-item>
              <el-descriptions-item label="节点类型">{{ selectedNode.type }}</el-descriptions-item>
              <el-descriptions-item label="位置">{{ selectedNode.location }}</el-descriptions-item>
              <el-descriptions-item label="负责人">{{ selectedNode.manager }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ selectedNode.phone }}</el-descriptions-item>
            </el-descriptions>

            <el-descriptions v-if="selectedNode.type === '仓库'" title="库存信息" :column="2" border>
              <el-descriptions-item label="总库存">{{ selectedNode.inventory.total }} {{ selectedNode.inventory.unit }}</el-descriptions-item>
              <el-descriptions-item label="可用库存">{{ selectedNode.inventory.available }} {{ selectedNode.inventory.unit }}</el-descriptions-item>
              <el-descriptions-item label="在途库存">{{ selectedNode.inventory.inTransit }} {{ selectedNode.inventory.unit }}</el-descriptions-item>
              <el-descriptions-item label="库存周转率">{{ selectedNode.inventory.turnoverRate }}次/年</el-descriptions-item>
            </el-descriptions>

            <el-descriptions v-if="selectedNode.type === '工厂'" title="生产信息" :column="2" border>
              <el-descriptions-item label="产能利用率">{{ selectedNode.production.utilization }}%</el-descriptions-item>
              <el-descriptions-item label="在制品数量">{{ selectedNode.production.workInProgress }} {{ selectedNode.production.unit }}</el-descriptions-item>
              <el-descriptions-item label="日均产量">{{ selectedNode.production.dailyOutput }} {{ selectedNode.production.unit }}</el-descriptions-item>
              <el-descriptions-item label="交付准时率">{{ selectedNode.production.onTimeRate }}%</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 物流详情 -->
          <div v-else-if="activeDetailType === 'edge' && selectedEdge" class="detail-content">
            <el-descriptions title="物流基本信息" :column="1" border>
              <el-descriptions-item label="物流ID">{{ selectedEdge.id }}</el-descriptions-item>
              <el-descriptions-item label="起始节点">{{ selectedEdge.sourceName }}</el-descriptions-item>
              <el-descriptions-item label="目标节点">{{ selectedEdge.targetName }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ selectedEdge.status }}</el-descriptions-item>
              <el-descriptions-item label="运输方式">{{ selectedEdge.transportMode }}</el-descriptions-item>
              <el-descriptions-item label="承运商">{{ selectedEdge.carrier }}</el-descriptions-item>
            </el-descriptions>

            <el-descriptions title="物流时间信息" :column="2" border>
              <el-descriptions-item label="计划出发时间">{{ selectedEdge.scheduleDeparture }}</el-descriptions-item>
              <el-descriptions-item label="实际出发时间">{{ selectedEdge.actualDeparture || '未出发' }}</el-descriptions-item>
              <el-descriptions-item label="计划到达时间">{{ selectedEdge.scheduleArrival }}</el-descriptions-item>
              <el-descriptions-item label="预计到达时间">{{ selectedEdge.estimatedArrival }}</el-descriptions-item>
            </el-descriptions>

            <el-descriptions title="货物信息" :column="2" border>
              <el-descriptions-item label="货物名称">{{ selectedEdge.goodsName }}</el-descriptions-item>
              <el-descriptions-item label="货物数量">{{ selectedEdge.quantity }} {{ selectedEdge.unit }}</el-descriptions-item>
              <el-descriptions-item label="货物重量">{{ selectedEdge.weight }} kg</el-descriptions-item>
              <el-descriptions-item label="运输单号">{{ selectedEdge.trackingNumber }}</el-descriptions-item>
            </el-descriptions>

            <el-descriptions title="物流跟踪" :column="1" border>
              <el-descriptions-item label="跟踪记录">
                <div class="tracking-records">
                  <div v-for="(record, index) in selectedEdge.trackingRecords" :key="index" class="tracking-record">
                    <div class="tracking-time">{{ record.time }}</div>
                    <div class="tracking-location">{{ record.location }}</div>
                    <div class="tracking-status">{{ record.status }}</div>
                  </div>
                </div>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 无详情时的提示 -->
          <div v-else class="no-detail">
            <el-empty description="点击节点或连线查看详情" />
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Refresh, FullScreen, Close } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { forecastApi } from '@/api/scm';

const props = withDefaults(defineProps<{ embedded?: boolean }>(), {
  embedded: false
});

const embedded = computed(() => props.embedded);

// 地图引用
const supplyChainMap = ref<HTMLElement | null>(null);

// 地图实例
let mapInstance: echarts.ECharts | null = null;

// 控制参数
const selectedNodeTypes = ref(['工厂', '仓库']);
const selectedLogisticsStatus = ref(['正常', '延迟', '异常']);
const viewMode = ref('全局视图');

// 详情面板
const activeDetailType = ref<string>('node'); // 'node' 或 'edge'
const selectedNode = ref<any>(null);
const selectedEdge = ref<any>(null);

// 供应链网络数据
const supplyChainData = ref<{ nodes: any[]; edges: any[] }>({ nodes: [], edges: [] });

// 初始化地图
onMounted(async () => {
  await fetchNetworkData();
  initMap();
  window.addEventListener('resize', handleResize, { passive: true });
});

// 获取网络数据
const fetchNetworkData = async () => {
  try {
    const res: any = await forecastApi.getNetworkData();
    const data = res?.data || {};
    supplyChainData.value = {
      nodes: Array.isArray(data.nodes) ? data.nodes : [],
      edges: Array.isArray(data.edges) ? data.edges : []
    };
  } catch (error) {
    supplyChainData.value = { nodes: [], edges: [] };
  }
};

// 销毁地图
onUnmounted(() => {
  if (mapInstance) {
    mapInstance.dispose();
    mapInstance = null;
  }
  window.removeEventListener('resize', handleResize);
});

// 初始化地图
const initMap = () => {
  if (!supplyChainMap.value) return;
  
  // 检查DOM尺寸，如果为0则延迟初始化
  const checkAndInit = () => {
    if (supplyChainMap.value && supplyChainMap.value.clientWidth > 0 && supplyChainMap.value.clientHeight > 0) {
      mapInstance = echarts.init(supplyChainMap.value);
      
      // 设置地图配置
      const option = {
        title: {
          text: '供应链网络',
          left: 'center'
        },
        tooltip: {
          formatter: function(params: any) {
            if (params.dataType === 'node') {
              return `${params.data.name}<br/>类型：${params.data.type}<br/>位置：${params.data.location}`;
            } else if (params.dataType === 'edge') {
              return `${params.data.sourceName} → ${params.data.targetName}<br/>状态：${params.data.status}<br/>运输方式：${params.data.transportMode}`;
            }
            return '';
          }
        },
        series: [
          {
            type: 'graph',
            layout: 'none',
            data: supplyChainData.value.nodes.map((node: any) => ({
              ...node,
              symbolSize: node.type === '工厂' ? 50 : 40,
              itemStyle: {
                color: node.type === '工厂' ? '#67C23A' : '#409EFF',
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: true,
                formatter: '{b}',
                fontSize: 12,
                color: '#333'
              },
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowOffsetX: 0,
                  shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
              }
            })),
            links: supplyChainData.value.edges.map((edge: any) => ({
              ...edge,
              lineStyle: {
                width: 3,
                color: getEdgeColor(edge.status),
                curveness: 0.1
              },
              emphasis: {
                lineStyle: {
                  width: 5
                }
              }
            })),
            lineStyle: {
              type: 'solid'
            },
            roam: true,
            draggable: false,
            lineSymbol: ['none', 'arrow'],
            lineSymbolSize: 10,
            animationDuration: 1500,
            animationEasingUpdate: 'quinticInOut',
            selectMode: 'single',
            emphasis: {
              focus: 'adjacency'
            }
          }
        ]
      };
      
      mapInstance.setOption(option);
      
      // 添加点击事件
      mapInstance.on('click', (params: any) => {
        if (params.dataType === 'node') {
          showNodeDetail(params.data);
        } else if (params.dataType === 'edge') {
          showEdgeDetail(params.data);
        }
      });
    } else {
      // DOM尺寸为0，延迟100ms后重试
      setTimeout(checkAndInit, 100);
    }
  };
  
  // 开始检查和初始化
  checkAndInit();
};

// 获取边的颜色
const getEdgeColor = (status: string) => {
  switch (status) {
    case '正常':
      return '#67C23A';
    case '延迟':
      return '#E6A23C';
    case '异常':
      return '#F56C6C';
    default:
      return '#909399';
  }
};

// 显示节点详情
const showNodeDetail = (node: any) => {
  activeDetailType.value = 'node';
  selectedNode.value = node;
  selectedEdge.value = null;
};

// 显示边详情
const showEdgeDetail = (edge: any) => {
  activeDetailType.value = 'edge';
  selectedEdge.value = edge;
  selectedNode.value = null;
};

// 关闭详情面板
const closeDetail = () => {
  selectedNode.value = null;
  selectedEdge.value = null;
};

// 处理窗口大小变化
const handleResize = () => {
  mapInstance?.resize();
};

// 刷新地图
const refreshMap = async () => {
  await fetchNetworkData();
  if (mapInstance) {
    const option = mapInstance.getOption();
    (option as any).series[0].data = supplyChainData.value.nodes.map((node: any) => ({
      ...node,
      symbolSize: node.type === '工厂' ? 50 : 40,
      itemStyle: {
        color: node.type === '工厂' ? '#67C23A' : '#409EFF',
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}',
        fontSize: 12,
        color: '#333'
      }
    }));
    (option as any).series[0].links = supplyChainData.value.edges.map((edge: any) => ({
      ...edge,
      lineStyle: {
        width: 3,
        color: getEdgeColor(edge.status),
        curveness: 0.1
      }
    }));
    mapInstance.setOption(option);
  }
  ElMessage.success('地图已刷新');
};

// 适应视图
const fitView = () => {
  mapInstance?.dispatchAction({
    type: 'fitView'
  });
};

// 处理视图切换
const handleViewChange = () => {
  ElMessage.info(`已切换到${viewMode.value}`);
  // 实际项目中，这里会切换不同的视图数据
};
</script>

<style scoped>
.supply-chain-map-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.supply-chain-map-container.embedded {
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

.control-card {
  margin-bottom: 20px;
}

.control-content {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}

.control-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.control-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.map-detail-layout {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 20px;
}

.map-card {
  height: 700px;
  display: flex;
  flex-direction: column;
}

.map-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.map {
  width: 100%;
  height: 100%;
}

.detail-card {
  height: 700px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.detail-content {
  flex: 1;
  overflow-y: auto;
  padding: 0 10px;
}

.no-detail {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.tracking-records {
  max-height: 200px;
  overflow-y: auto;
}

.tracking-record {
  margin-bottom: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.tracking-time {
  font-weight: 600;
  font-size: 12px;
  color: #303133;
  margin-bottom: 3px;
}

.tracking-location {
  font-size: 12px;
  color: #606266;
  margin-bottom: 3px;
}

.tracking-status {
  font-size: 12px;
  color: #409EFF;
}

.map-legend {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #606266;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 1px solid #fff;
}

.factory-dot {
  background-color: #67C23A;
}

.warehouse-dot {
  background-color: #409EFF;
}

.legend-line {
  width: 20px;
  height: 3px;
}

.normal-line {
  background-color: #67C23A;
}

.delay-line {
  background-color: #E6A23C;
}

.exception-line {
  background-color: #F56C6C;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .map-detail-layout {
    grid-template-columns: 1fr;
  }
  
  .detail-card {
    height: 500px;
  }
}

@media (max-width: 768px) {
  .supply-chain-map-container {
    padding: 10px;
  }
  
  .control-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .control-section {
    width: 100%;
    flex-wrap: wrap;
  }
  
  .map-card {
    height: 500px;
  }
}
</style>
