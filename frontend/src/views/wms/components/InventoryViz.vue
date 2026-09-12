<template>
  <div class="inventory-viz-container">
    <div class="viz-header">
      <h3>库存可视化</h3>
      <div class="viz-controls">
        <el-select v-model="visualizationType" placeholder="选择可视化类型" size="small" @change="changeVisualizationType">
          <el-option label="3D视图" value="3d"></el-option>
          <el-option label="热力图" value="heatmap"></el-option>
        </el-select>
        <el-select v-model="selectedWarehouse" placeholder="选择仓库" size="small" @change="loadInventoryData">
          <el-option
            v-for="warehouse in warehouses"
            :key="warehouse.id"
            :label="warehouse.name"
            :value="warehouse.id"
          ></el-option>
        </el-select>
        <el-button size="small" @click="refreshData">刷新数据</el-button>
        <el-button size="small" @click="resetView">重置视图</el-button>
      </div>
    </div>
    
    <div class="viz-wrapper">
      <!-- 3D视图 -->
      <div v-if="visualizationType === '3d'" class="viz-3d-container">
        <div ref="threeContainer" class="three-container"></div>
        <div class="inventory-stats">
          <div class="stat-card">
            <span class="stat-label">总库存数量</span>
            <span class="stat-value">{{ totalInventoryQuantity }}</span>
          </div>
          <div class="stat-card">
            <span class="stat-label">库存总值</span>
            <span class="stat-value">¥{{ totalInventoryValue.toLocaleString() }}</span>
          </div>
          <div class="stat-card">
            <span class="stat-label">库位利用率</span>
            <span class="stat-value">{{ locationUtilization.toFixed(2) }}%</span>
          </div>
          <div class="stat-card">
            <span class="stat-label">物料种类</span>
            <span class="stat-value">{{ materialCount }}</span>
          </div>
        </div>
      </div>
      
      <!-- 热力图视图 -->
      <div v-else class="viz-heatmap-container">
        <div ref="heatmapContainer" class="heatmap-container"></div>
        <div class="heatmap-legend">
          <h4>库存密度</h4>
          <div class="legend-gradient">
            <div class="gradient-item" style="background-color: #4CAF50;">低</div>
            <div class="gradient-item" style="background-color: #FFC107;">中</div>
            <div class="gradient-item" style="background-color: #F44336;">高</div>
          </div>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-overlay">
        <el-spinner size="large"></el-spinner>
        <p>加载中...</p>
      </div>
    </div>
    
    <!-- 库存详情面板 -->
    <div v-if="selectedInventory" class="inventory-detail-panel">
      <div class="panel-header">
        <h4>库存详情</h4>
        <el-button
          link
          size="small"
          @click="selectedInventory = null"
        >
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      <div class="panel-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="物料代码">{{ selectedInventory.materialCode }}</el-descriptions-item>
          <el-descriptions-item label="物料名称">{{ selectedInventory.materialName }}</el-descriptions-item>
          <el-descriptions-item label="规格">{{ selectedInventory.specification }}</el-descriptions-item>
          <el-descriptions-item label="单位">{{ selectedInventory.unit }}</el-descriptions-item>
          <el-descriptions-item label="仓库">{{ selectedInventory.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="库位">{{ selectedInventory.locationCode }}</el-descriptions-item>
          <el-descriptions-item label="批次号">{{ selectedInventory.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="有效期">{{ selectedInventory.expiryDate || '无' }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ selectedInventory.quantity }}</el-descriptions-item>
          <el-descriptions-item label="已分配">{{ selectedInventory.allocatedQuantity }}</el-descriptions-item>
          <el-descriptions-item label="可用数量">{{ selectedInventory.availableQuantity }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ inventoryStatusMap[selectedInventory.status] }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, onUnmounted } from 'vue';
import { Close } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { useWmsStockStore } from '@/stores/wms/stock';
import { useWmsBaseStore } from '@/stores/wms/base';
import type { Inventory, Warehouse } from '@/types/wms';

// 导入Three.js（如果需要）
// import * as THREE from 'three';
// import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';

const stockStore = useWmsStockStore();
const baseStore = useWmsBaseStore();

// 组件状态
const visualizationType = ref('3d');
const selectedWarehouse = ref('');
const warehouses = ref<Warehouse[]>([]);
const inventories = ref<Inventory[]>([]);
const selectedInventory = ref<Inventory | null>(null);
const loading = ref(false);
const threeContainer = ref<HTMLDivElement | null>(null);
const heatmapContainer = ref<HTMLDivElement | null>(null);

// ECharts实例
let heatmapChart: echarts.ECharts | null = null;

// 常量映射
const inventoryStatusMap = {
  good: '良品',
  bad: '不良品',
  inspection: '待检'
};

// 计算属性
const totalInventoryQuantity = computed(() => {
  return inventories.value.reduce((sum, inventory) => sum + inventory.quantity, 0);
});

const totalInventoryValue = computed(() => {
  // 假设每个物料的价值为100元，实际应从物料主数据获取
  return inventories.value.reduce((sum, inventory) => sum + inventory.quantity * 100, 0);
});

const locationUtilization = computed(() => {
  // 假设库位总数为1000，实际应从库位数据获取
  const totalLocations = 1000;
  const usedLocations = new Set(inventories.value.map(inv => inv.locationId)).size;
  return (usedLocations / totalLocations) * 100;
});

const materialCount = computed(() => {
  return new Set(inventories.value.map(inv => inv.materialId)).size;
});

// 方法
const fetchWarehouses = async () => {
  await baseStore.fetchWarehouses();
  warehouses.value = baseStore.warehouses;
  if (warehouses.value.length > 0 && warehouses.value[0]) {
    selectedWarehouse.value = warehouses.value[0].id;
    loadInventoryData();
  }
};

const loadInventoryData = async () => {
  if (!selectedWarehouse.value) return;
  
  loading.value = true;
  try {
    await stockStore.fetchInventories({ warehouseId: selectedWarehouse.value });
    inventories.value = stockStore.inventories.filter(inv => inv.warehouseId === selectedWarehouse.value);
    
    // 更新可视化
    updateVisualization();
  } catch (error) {
    console.error('加载库存数据失败:', error);
  } finally {
    loading.value = false;
  }
};

const updateVisualization = () => {
  if (visualizationType.value === 'heatmap') {
    updateHeatmap();
  } else if (visualizationType.value === '3d') {
    update3DView();
  }
};

const updateHeatmap = () => {
  if (!heatmapContainer.value) return;
  
  // 初始化ECharts实例
  if (!heatmapChart) {
    heatmapChart = echarts.init(heatmapContainer.value);
  }
  
  // 准备热力图数据
  const heatmapData = inventories.value.map(inventory => {
    // 假设库位坐标为简单的数字映射，实际应从库位数据获取
    const locationCode = inventory.locationCode;
    // 简单解析库位码，例如 A01-01-01 解析为 x: 1, y: 1, z: 1
    const parts = locationCode.split('-');
    const x = parseInt(parts[0]?.substring(1) || '') || 1;
    const y = parseInt(parts[1] || '') || 1;
    const value = inventory.quantity;
    
    return [x, y, value] as [number, number, number];
  });
  
  // 热力图配置
  const xValues = Array.from(new Set(heatmapData.map(item => item[0]))).filter((v): v is number => v !== undefined);
  const yValues = Array.from(new Set(heatmapData.map(item => item[1]))).filter((v): v is number => v !== undefined);
  const values = heatmapData.map(item => item[2]).filter((v): v is number => v !== undefined);
  
  const option = {
    title: {
      text: '库存热力图',
      left: 'center'
    },
    tooltip: {
      position: 'top'
    },
    grid: {
      height: '50%',
      top: '10%'
    },
    xAxis: {
      type: 'category',
      data: xValues.sort((a, b) => a - b).map(String),
      splitArea: {
        show: true
      }
    },
    yAxis: {
      type: 'category',
      data: yValues.sort((a, b) => a - b).map(String),
      splitArea: {
        show: true
      }
    },
    visualMap: {
      min: 0,
      max: Math.max(...values, 10),
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '15%',
      inRange: {
        color: ['#4CAF50', '#FFC107', '#F44336']
      }
    },
    series: [
      {
        name: '库存数量',
        type: 'heatmap',
        data: heatmapData,
        label: {
          show: true
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };
  
  // 设置图表配置
  heatmapChart.setOption(option);
  
  // 监听点击事件
  heatmapChart.on('click', (params: any) => {
    // 查找对应的库存记录
    const locationX = parseInt(params.name);
    const locationY = parseInt(params.data[1]);
    // 简单匹配，实际应使用更精确的匹配逻辑
    const inventory = inventories.value.find(inv => {
      const parts = inv.locationCode.split('-');
      const x = parseInt(parts[0]?.substring(1) || '') || 1;
      const y = parseInt(parts[1] || '') || 1;
      return x === locationX && y === locationY;
    });
    
    if (inventory) {
      selectedInventory.value = inventory;
    }
  });
};

const update3DView = () => {
  // 3D视图实现，这里使用简单的HTML和CSS模拟，实际应使用Three.js
  if (!threeContainer.value) return;
  
  // 生成模拟货架项HTML
  const shelfItemsHtml = inventories.value.slice(0, 20).map((inventory, index) => {
    const backgroundColor = getShelfColor(inventory.status);
    const opacity = Math.min(inventory.quantity / 100, 1);
    return `
      <div 
        class="mock-shelf-item"
        style="
          backgroundColor: ${backgroundColor};
          opacity: ${opacity};
        "
        data-index="${index}"
      >
        <span>${inventory.materialCode}</span>
        <span>${inventory.quantity}</span>
      </div>
    `;
  }).join('');
  
  threeContainer.value.innerHTML = `
    <div class="mock-3d-view">
      <h4>3D库存视图</h4>
      <p>库存总量: ${totalInventoryQuantity.value}</p>
      <p>仓库: ${selectedWarehouse.value}</p>
      <p>可视化类型: 3D视图</p>
      <p class="mock-3d-hint">提示: 实际项目中应使用Three.js实现完整的3D库存可视化</p>
      <div class="mock-shelves">
        ${shelfItemsHtml}
      </div>
    </div>
  `;
  
  // 添加点击事件监听器
  threeContainer.value.querySelectorAll('.mock-shelf-item').forEach((item, index) => {
    item.addEventListener('click', () => {
      const inventory = inventories.value.slice(0, 20)[index];
      if (inventory) {
        selectedInventory.value = inventory;
      }
    });
  });
};

const getShelfColor = (status: string) => {
  switch (status) {
    case 'good':
      return '#4CAF50';
    case 'bad':
      return '#F44336';
    case 'inspection':
      return '#FFC107';
    default:
      return '#2196F3';
  }
};

const changeVisualizationType = () => {
  updateVisualization();
};

const refreshData = () => {
  loadInventoryData();
};

const resetView = () => {
  if (visualizationType.value === 'heatmap' && heatmapChart) {
    heatmapChart.dispose();
    heatmapChart = null;
    updateVisualization();
  }
  // 3D视图重置逻辑
};

const fetchWarehouseData = async () => {
  await baseStore.fetchWarehouses();
  warehouses.value = baseStore.warehouses;
};

// 生命周期
onMounted(async () => {
  await fetchWarehouseData();
  if (warehouses.value.length > 0 && warehouses.value[0]) {
    selectedWarehouse.value = warehouses.value[0].id;
    await loadInventoryData();
  }
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize, { passive: true });
});

onUnmounted(() => {
  // 销毁ECharts实例
  if (heatmapChart) {
    heatmapChart.dispose();
    heatmapChart = null;
  }
  
  // 移除事件监听器
  window.removeEventListener('resize', handleResize);
});

const handleResize = () => {
  if (heatmapChart) {
    heatmapChart.resize();
  }
};
</script>

<style scoped>
.inventory-viz-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
  gap: 16px;
}

.viz-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px solid #e0e0e0;
}

.viz-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.viz-wrapper {
  flex: 1;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  background-color: #fafafa;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.8);
  z-index: 1000;
  gap: 16px;
}

/* 3D视图样式 */
.viz-3d-container {
  display: flex;
  height: 100%;
}

.three-container {
  flex: 1;
  height: 100%;
  background-color: #f0f0f0;
}

/* 模拟3D视图样式 */
.mock-3d-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 20px;
  text-align: center;
}

.mock-3d-view h4 {
  margin-bottom: 16px;
  color: #333;
}

.mock-3d-hint {
  margin: 16px 0;
  color: #666;
  font-style: italic;
}

.mock-shelves {
  display: grid;
  grid-template-columns: repeat(5, 100px);
  grid-template-rows: repeat(4, 80px);
  gap: 10px;
  margin-top: 20px;
}

.mock-shelf-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #4CAF50;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px;
  text-align: center;
}

.mock-shelf-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.mock-shelf-item span:first-child {
  font-size: 12px;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 80px;
}

.mock-shelf-item span:last-child {
  font-size: 16px;
  font-weight: bold;
}

.inventory-stats {
  width: 200px;
  padding: 16px;
  background-color: white;
  border-left: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-card {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 8px;
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

/* 热力图样式 */
.viz-heatmap-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.heatmap-container {
  flex: 1;
  width: 100%;
  padding: 20px;
}

.heatmap-legend {
  padding: 16px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  gap: 16px;
}

.heatmap-legend h4 {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.gradient-item {
  width: 60px;
  height: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  font-size: 12px;
  font-weight: bold;
  border-radius: 2px;
}

/* 库存详情面板 */
.inventory-detail-panel {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 400px;
  background-color: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: calc(100% - 32px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e0e0e0;
}

.panel-header h4 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.panel-content {
  padding: 16px;
  overflow-y: auto;
  flex: 1;
}
</style>
