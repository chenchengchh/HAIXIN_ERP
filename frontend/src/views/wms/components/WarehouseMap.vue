<template>
  <div class="warehouse-map-container">
    <div class="map-header">
      <h3>仓库平面图</h3>
      <div class="map-controls">
        <el-button size="small" @click="zoomIn">放大</el-button>
        <el-button size="small" @click="zoomOut">缩小</el-button>
        <el-button size="small" @click="resetZoom">重置</el-button>
        <el-select v-model="selectedWarehouse" placeholder="选择仓库" size="small" @change="loadWarehouseMap">
          <el-option
            v-for="warehouse in warehouses"
            :key="warehouse.id"
            :label="warehouse.name"
            :value="warehouse.id"
          ></el-option>
        </el-select>
      </div>
    </div>
    
    <div class="map-wrapper">
      <div
        class="map-canvas-container"
        ref="mapContainer"
        @wheel.passive="handleWheel"
        @mousedown="handleMouseDown"
        @mousemove="handleMouseMove"
        @mouseup="handleMouseUp"
        @mouseleave="handleMouseUp"
      >
        <canvas
          ref="mapCanvas"
          :width="canvasWidth"
          :height="canvasHeight"
          @click="handleCanvasClick"
        ></canvas>
        
        <!-- 库位信息弹窗 -->
        <div
          v-if="selectedLocation"
          class="location-info-popup"
          :style="popupStyle"
        >
          <div class="popup-header">
            <h4>{{ selectedLocation.code }} - 库位详情</h4>
            <el-button
              link
              size="small"
              @click="selectedLocation = null"
            >
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          <div class="popup-content">
            <p><strong>库位类型:</strong> {{ locationTypeMap[selectedLocation.type] }}</p>
            <p><strong>状态:</strong> <span :class="`status-${selectedLocation.status}`">{{ locationStatusMap[selectedLocation.status] }}</span></p>
            <p><strong>容量:</strong> {{ selectedLocation.capacity }}</p>
            <p><strong>当前库存:</strong> {{ selectedLocation.currentStock }}</p>
            <p><strong>坐标:</strong> ({{ selectedLocation.coordinates.x }}, {{ selectedLocation.coordinates.y }}, {{ selectedLocation.coordinates.z }})</p>
            <p><strong>描述:</strong> {{ selectedLocation.description }}</p>
          </div>
        </div>
      </div>
    </div>
    
    <div class="map-footer">
      <div class="legend">
        <h4>图例</h4>
        <div class="legend-items">
          <div class="legend-item">
            <div class="legend-color" style="background-color: #4CAF50;"></div>
            <span>可用</span>
          </div>
          <div class="legend-item">
            <div class="legend-color" style="background-color: #FF9800;"></div>
            <span>锁定</span>
          </div>
          <div class="legend-item">
            <div class="legend-color" style="background-color: #F44336;"></div>
            <span>禁用</span>
          </div>
          <div class="legend-item">
            <div class="legend-color" style="background-color: #2196F3;"></div>
            <span>存储库位</span>
          </div>
          <div class="legend-item">
            <div class="legend-color" style="background-color: #9C27B0;"></div>
            <span>拣货库位</span>
          </div>
          <div class="legend-item">
            <div class="legend-color" style="background-color: #FFEB3B;"></div>
            <span>暂存库位</span>
          </div>
        </div>
      </div>
      <div class="map-info">
        <p>缩放级别: {{ zoomLevel.toFixed(2) }}</p>
        <p>当前仓库: {{ currentWarehouseName }}</p>
        <p>库位总数: {{ locations.length }}</p>
        <p>可用库位: {{ availableLocationsCount }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { Close } from '@element-plus/icons-vue';
import { useWmsBaseStore } from '@/stores/wms/base';
import type { Location, Warehouse } from '@/types/wms';

const store = useWmsBaseStore();

// 画布相关
const mapCanvas = ref<HTMLCanvasElement | null>(null);
const mapContainer = ref<HTMLDivElement | null>(null);
const canvasWidth = ref(1200);
const canvasHeight = ref(800);

// 地图状态
const zoomLevel = ref(1);
const panOffset = ref({ x: 0, y: 0 });
const isDragging = ref(false);
const lastMousePos = ref({ x: 0, y: 0 });

// 数据
const warehouses = ref<Warehouse[]>([]);
const selectedWarehouse = ref('');
const locations = ref<Location[]>([]);
const selectedLocation = ref<Location | null>(null);
const popupPos = ref({ x: 0, y: 0 });

// 常量映射
const locationTypeMap = {
  storage: '存储库位',
  picking: '拣货库位',
  temporary: '暂存库位'
};

const locationStatusMap = {
  available: '可用',
  locked: '锁定',
  disabled: '禁用'
};

// 计算属性
const currentWarehouseName = computed(() => {
  const warehouse = warehouses.value.find(w => w.id === selectedWarehouse.value);
  return warehouse ? warehouse.name : '未选择';
});

const availableLocationsCount = computed(() => {
  return locations.value.filter(loc => loc.status === 'available').length;
});

const popupStyle = computed(() => {
  return {
    left: `${popupPos.value.x}px`,
    top: `${popupPos.value.y}px`
  };
});

// 方法
const initCanvas = () => {
  if (!mapCanvas.value) return;
  const ctx = mapCanvas.value.getContext('2d');
  if (!ctx) return;
  
  // 清空画布
  ctx.clearRect(0, 0, canvasWidth.value, canvasHeight.value);
  
  // 绘制网格背景
  drawGrid(ctx);
  
  // 绘制库位
  drawLocations(ctx);
};

const drawGrid = (ctx: CanvasRenderingContext2D) => {
  ctx.save();
  ctx.strokeStyle = '#e0e0e0';
  ctx.lineWidth = 0.5;
  
  const gridSize = 50 * zoomLevel.value;
  
  // 绘制垂直线
  for (let x = panOffset.value.x % gridSize; x < canvasWidth.value; x += gridSize) {
    ctx.beginPath();
    ctx.moveTo(x, 0);
    ctx.lineTo(x, canvasHeight.value);
    ctx.stroke();
  }
  
  // 绘制水平线
  for (let y = panOffset.value.y % gridSize; y < canvasHeight.value; y += gridSize) {
    ctx.beginPath();
    ctx.moveTo(0, y);
    ctx.lineTo(canvasWidth.value, y);
    ctx.stroke();
  }
  
  ctx.restore();
};

const drawLocations = (ctx: CanvasRenderingContext2D) => {
  ctx.save();
  
  const locationSize = 40 * zoomLevel.value;
  const margin = 5 * zoomLevel.value;
  
  locations.value.forEach(location => {
    const x = location.coordinates.x * (locationSize + margin) + panOffset.value.x;
    const y = location.coordinates.y * (locationSize + margin) + panOffset.value.y;
    
    // 设置填充颜色
    let fillColor = '#4CAF50'; // 可用
    if (location.status === 'locked') fillColor = '#FF9800';
    if (location.status === 'disabled') fillColor = '#F44336';
    
    // 设置边框颜色
    let borderColor = '#2196F3'; // 存储库位
    if (location.type === 'picking') borderColor = '#9C27B0';
    if (location.type === 'temporary') borderColor = '#FFEB3B';
    
    // 绘制库位
    ctx.fillStyle = fillColor;
    ctx.strokeStyle = borderColor;
    ctx.lineWidth = 2 * zoomLevel.value;
    ctx.fillRect(x, y, locationSize, locationSize);
    ctx.strokeRect(x, y, locationSize, locationSize);
    
    // 绘制库位码
    ctx.fillStyle = '#FFFFFF';
    ctx.font = `${12 * zoomLevel.value}px Arial`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(location.code, x + locationSize / 2, y + locationSize / 2);
  });
  
  ctx.restore();
};

const zoomIn = () => {
  if (zoomLevel.value < 2) {
    zoomLevel.value += 0.1;
    initCanvas();
  }
};

const zoomOut = () => {
  if (zoomLevel.value > 0.5) {
    zoomLevel.value -= 0.1;
    initCanvas();
  }
};

const resetZoom = () => {
  zoomLevel.value = 1;
  panOffset.value = { x: 0, y: 0 };
  initCanvas();
};

const handleWheel = (event: WheelEvent) => {
  const delta = event.deltaY > 0 ? -0.1 : 0.1;
  const newZoom = Math.max(0.5, Math.min(2, zoomLevel.value + delta));
  if (newZoom !== zoomLevel.value) {
    zoomLevel.value = newZoom;
    initCanvas();
  }
};

const handleMouseDown = (event: MouseEvent) => {
  isDragging.value = true;
  lastMousePos.value = {
    x: event.clientX,
    y: event.clientY
  };
  if (mapContainer.value) {
    mapContainer.value.style.cursor = 'grabbing';
  }
};

const handleMouseMove = (event: MouseEvent) => {
  if (isDragging.value) {
    const dx = event.clientX - lastMousePos.value.x;
    const dy = event.clientY - lastMousePos.value.y;
    panOffset.value.x += dx;
    panOffset.value.y += dy;
    lastMousePos.value = {
      x: event.clientX,
      y: event.clientY
    };
    initCanvas();
  }
};

const handleMouseUp = () => {
  isDragging.value = false;
  if (mapContainer.value) {
    mapContainer.value.style.cursor = 'grab';
  }
};

const handleCanvasClick = (event: MouseEvent) => {
  if (isDragging.value) return;
  
  const rect = mapCanvas.value?.getBoundingClientRect();
  if (!rect) return;
  
  const x = event.clientX - rect.left;
  const y = event.clientY - rect.top;
  
  const locationSize = 40 * zoomLevel.value;
  const margin = 5 * zoomLevel.value;
  
  // 查找点击的库位
  for (const location of locations.value) {
    const locX = location.coordinates.x * (locationSize + margin) + panOffset.value.x;
    const locY = location.coordinates.y * (locationSize + margin) + panOffset.value.y;
    
    if (x >= locX && x <= locX + locationSize && y >= locY && y <= locY + locationSize) {
      selectedLocation.value = location;
      popupPos.value = {
        x: x + 10,
        y: y + 10
      };
      return;
    }
  }
  
  // 点击空白处关闭弹窗
  selectedLocation.value = null;
};

const loadWarehouseMap = async () => {
  if (!selectedWarehouse.value) return;
  
  // 从store获取库位数据
  await store.fetchLocations({ warehouseId: selectedWarehouse.value });
  locations.value = store.locations.filter(loc => loc.warehouseId === selectedWarehouse.value);
  initCanvas();
};

const fetchWarehouses = async () => {
  await store.fetchWarehouses();
  warehouses.value = store.warehouses;
  if (warehouses.value.length > 0 && warehouses.value[0]) {
    selectedWarehouse.value = warehouses.value[0].id;
    loadWarehouseMap();
  }
};

// 生命周期
onMounted(() => {
  fetchWarehouses();
  initCanvas();
});

// 监听画布尺寸变化
watch([canvasWidth, canvasHeight], () => {
  initCanvas();
});
</script>

<style scoped>
.warehouse-map-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
  gap: 16px;
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px solid #e0e0e0;
}

.map-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.map-wrapper {
  flex: 1;
  overflow: hidden;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background-color: #fafafa;
}

.map-canvas-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  cursor: grab;
}

.map-canvas-container:active {
  cursor: grabbing;
}

.location-info-popup {
  position: absolute;
  background-color: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
  min-width: 250px;
  max-width: 350px;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e0e0e0;
}

.popup-header h4 {
  margin: 0;
  font-size: 16px;
}

.popup-content p {
  margin: 6px 0;
  font-size: 14px;
}

.popup-content strong {
  color: #333;
  margin-right: 8px;
}

.status-available {
  color: #4CAF50;
}

.status-locked {
  color: #FF9800;
}

.status-disabled {
  color: #F44336;
}

.map-footer {
  display: flex;
  justify-content: space-between;
  padding-top: 8px;
  border-top: 1px solid #e0e0e0;
  gap: 16px;
}

.legend {
  flex: 1;
}

.legend h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
}

.legend-items {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 2px;
}

.map-info {
  text-align: right;
  font-size: 14px;
  color: #606266;
}

.map-info p {
  margin: 4px 0;
}
</style>
