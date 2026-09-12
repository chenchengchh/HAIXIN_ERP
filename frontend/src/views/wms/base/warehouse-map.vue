<template>
  <div class="warehouse-map-container">
    <!-- 操作栏 -->
    <el-card class="operation-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            placeholder="搜索库位码"
            v-model="searchLocationCode"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </el-col>
        <el-col :span="16">
          <el-row :gutter="10" justify="end">
            <el-col>
              <el-button type="primary" @click="handleImport">导入布局</el-button>
            </el-col>
            <el-col>
              <el-button @click="handleExport">导出布局</el-button>
            </el-col>
            <el-col>
              <el-button @click="handleZoomIn">放大</el-button>
            </el-col>
            <el-col>
              <el-button @click="handleZoomOut">缩小</el-button>
            </el-col>
            <el-col>
              <el-button @click="handleReset">重置</el-button>
            </el-col>
          </el-row>
        </el-col>
      </el-row>
    </el-card>

    <!-- 仓库平面图 -->
    <el-card class="map-card">
      <div class="canvas-container">
        <canvas
          ref="canvasRef"
          :width="canvasWidth"
          :height="canvasHeight"
          @click="handleCanvasClick"
          @mousedown="handleMouseDown"
          @mousemove="handleMouseMove"
          @mouseup="handleMouseUp"
          @mouseleave="handleMouseLeave"
        ></canvas>
        <!-- 缩放控制 -->
        <div class="zoom-control">
          <el-slider
            v-model="scale"
            :min="0.5"
            :max="2"
            :step="0.1"
            @change="handleScaleChange"
            vertical
          ></el-slider>
        </div>
      </div>
    </el-card>

    <!-- 库位详情 -->
    <el-dialog
      v-model="detailVisible"
      title="库位详情"
      width="500px"
      :before-close="handleCloseDetail"
    >
      <el-form :model="selectedLocation" label-width="100px">
        <el-form-item label="库位码">
          <el-input v-model="selectedLocation.locationCode" readonly></el-input>
        </el-form-item>
        <el-form-item label="库区">
          <el-input v-model="selectedLocation.zoneCode" readonly></el-input>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="selectedLocation.type" placeholder="请选择库位类型">
            <el-option
              v-for="t in locationTypes"
              :key="t.value"
              :label="t.label"
              :value="t.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="selectedLocation.status" placeholder="请选择库位状态">
            <el-option label="启用" value="1"></el-option>
            <el-option label="禁用" value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="是否混放">
          <el-switch v-model="selectedLocation.mixFlag" disabled></el-switch>
        </el-form-item>
        <el-form-item label="最大承重">
          <el-input-number v-model="selectedLocation.maxWeight" :min="0" :step="0.1" disabled></el-input-number>
        </el-form-item>
        <el-form-item label="当前物料">
          <el-input v-model="selectedLocation.currentMaterial" readonly></el-input>
        </el-form-item>
        <el-form-item label="当前数量">
          <el-input v-model="selectedLocation.currentQty" readonly></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDetail">取消</el-button>
          <el-button type="primary" @click="handleSaveLocation">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { inventoryV1Api, locationApi, locationTypeApi } from '@/api/wms';

// 画布相关
const canvasRef = ref<HTMLCanvasElement | null>(null);
const canvasWidth = ref(1200);
const canvasHeight = ref(800);
const scale = ref(1);
const offsetX = ref(0);
const offsetY = ref(0);
const isDragging = ref(false);
const lastMouseX = ref(0);
const lastMouseY = ref(0);

// 状态相关
const loading = ref(false);
const error = ref('');
const isInitialized = ref(false);

// 搜索相关
const searchLocationCode = ref('');

// 库位数据
interface Location {
  id: number;
  locationCode: string;
  zoneCode: string;
  type: string;
  status: string;
  mixFlag: boolean;
  maxWeight: number;
  currentWeight: number;
  capacityPercentage: number;
  currentMaterial: string;
  currentQty: number;
  warehouseCode?: string;
  x: number;
  y: number;
  width: number;
  height: number;
  highlighted?: boolean;
  recommended?: boolean;
};

const locations = ref<Location[]>([]);
const locationTypes = ref<{ label: string; value: string; mixFlag: boolean; maxWeight: number }[]>([]);
const locationTypeMetaByCode = ref<Record<string, { label: string; mixFlag: boolean; maxWeight: number }>>({});

// 详情对话框
const detailVisible = ref(false);
const selectedLocation = ref<Location>({
  id: 0,
  locationCode: '',
  zoneCode: '',
  type: '1',
  status: '1',
  mixFlag: false,
  maxWeight: 0,
  currentWeight: 0,
  capacityPercentage: 0,
  currentMaterial: '',
  currentQty: 0,
  x: 0,
  y: 0,
  width: 0,
  height: 0,
});

const fetchLocationTypes = async () => {
  const res = await locationTypeApi.getList({ page: 1, size: 1000 });
  const pageData = res.data || {};
  const list = (pageData.list || []) as any[];
  const meta: Record<string, { label: string; mixFlag: boolean; maxWeight: number }> = {};
  locationTypes.value = list
    .map((t: any) => {
      const code = String(t?.typeCode ?? '');
      const label = String(t?.typeName ?? code);
      const mixFlag = Boolean(t?.mixFlag ?? false);
      const maxWeight = Number(t?.maxWeight ?? 0);
      if (code) {
        meta[code] = { label, mixFlag, maxWeight };
      }
      return { label, value: code, mixFlag, maxWeight };
    })
    .filter(v => v.value);
  locationTypeMetaByCode.value = meta;
};

const buildInventoryByLocation = (items: any[]) => {
  const map: Record<string, { qty: number; materialName: string }> = {};
  const materialBuckets: Record<string, Record<string, number>> = {};
  for (const it of items) {
    const locationCode = String(it?.locationCode ?? '');
    if (!locationCode) continue;
    const qty = Number(it?.quantity ?? 0);
    map[locationCode] = map[locationCode] || { qty: 0, materialName: '' };
    map[locationCode].qty += Number.isFinite(qty) ? qty : 0;
    const mName = String(it?.materialName ?? it?.materialCode ?? '');
    if (mName) {
      materialBuckets[locationCode] = materialBuckets[locationCode] || {};
      materialBuckets[locationCode][mName] = (materialBuckets[locationCode][mName] || 0) + (Number.isFinite(qty) ? qty : 0);
    }
  }
  for (const k of Object.keys(materialBuckets)) {
    const bucket = materialBuckets[k] || {};
    let bestName = '';
    let bestQty = -Infinity;
    for (const name of Object.keys(bucket)) {
      const q = bucket[name] || 0;
      if (q > bestQty) {
        bestQty = q;
        bestName = name;
      }
    }
    if (map[k]) {
      map[k].materialName = bestName;
    }
  }
  return map;
};

const layoutLocations = (rows: any[], inv: Record<string, { qty: number; materialName: string }>) => {
  const byZone: Record<string, any[]> = {};
  for (const r of rows) {
    const z = String(r?.zoneCode ?? '');
    byZone[z] = byZone[z] || [];
    byZone[z].push(r);
  }
  const zones = Object.keys(byZone).sort((a, b) => a.localeCompare(b));
  const meta = locationTypeMetaByCode.value;
  const out: Location[] = [];
  zones.forEach((zone, zoneIndex) => {
    const list = (byZone[zone] || []).slice().sort((a, b) => String(a?.locationCode ?? '').localeCompare(String(b?.locationCode ?? '')));
    list.forEach((l, i) => {
      const row = Math.floor(i / 10);
      const col = i % 10;
      const locationCode = String(l?.locationCode ?? '');
      const type = String(l?.locationTypeCode ?? '');
      const m = meta[type];
      const maxWeight = Number(m?.maxWeight ?? 0);
      const qty = inv[locationCode]?.qty ?? 0;
      const pct = maxWeight > 0 ? Math.min(100, Math.round((qty / maxWeight) * 100)) : 0;
      out.push({
        id: Number(l?.id ?? 0),
        locationCode,
        zoneCode: String(l?.zoneCode ?? ''),
        type,
        status: String(l?.status ?? '1'),
        mixFlag: Boolean(m?.mixFlag ?? false),
        maxWeight,
        currentWeight: qty,
        capacityPercentage: pct,
        currentMaterial: String(inv[locationCode]?.materialName ?? ''),
        currentQty: qty,
        warehouseCode: String(l?.warehouseCode ?? ''),
        x: 80 + col * 95,
        y: 80 + zoneIndex * 140 + row * 75,
        width: 80,
        height: 60
      });
    });
  });
  return out;
};

const refreshMapData = async () => {
  loading.value = true;
  try {
    await fetchLocationTypes();
    const [locRes, invRes] = await Promise.all([
      locationApi.getList({ page: 1, size: 1000 }),
      inventoryV1Api.getList({ page: 0, size: 2000 })
    ]);
    const locPage = locRes.data || {};
    const locList = (locPage.list || []) as any[];
    const invPage = invRes.data || {};
    const invList = (invPage.content || []) as any[];
    const invByLoc = buildInventoryByLocation(invList);
    locations.value = layoutLocations(locList, invByLoc);
    const maxX = locations.value.reduce((m, v) => Math.max(m, v.x + v.width), 0);
    const maxY = locations.value.reduce((m, v) => Math.max(m, v.y + v.height), 0);
    canvasWidth.value = Math.max(1000, maxX + 120);
    canvasHeight.value = Math.max(600, maxY + 120);
  } catch (e: any) {
    ElMessage.error(e?.message || '加载仓库平面图数据失败');
    locations.value = [];
  } finally {
    loading.value = false;
    nextTick(() => initCanvas());
  }
};

// 初始化画布
const initCanvas = () => {
  try {
    const canvas = canvasRef.value;
    if (!canvas) {
      // 画布元素不存在，等待DOM渲染完成
      console.warn('画布元素尚未准备好，稍后重试');
      return;
    }
    
    const ctx = canvas.getContext('2d');
    if (!ctx) {
      throw new Error('无法获取画布上下文');
    }
    
    // 清空画布
    ctx.clearRect(0, 0, canvasWidth.value, canvasHeight.value);
    
    // 保存当前状态
    ctx.save();
    
    // 应用缩放和偏移
    ctx.translate(offsetX.value, offsetY.value);
    ctx.scale(scale.value, scale.value);
    
    // 绘制网格
    drawGrid(ctx);
    
    // 绘制库位
    drawLocations(ctx);
    
    // 恢复状态
    ctx.restore();
    
    isInitialized.value = true;
    error.value = '';
  } catch (err) {
    error.value = err instanceof Error ? err.message : '初始化画布失败';
    ElMessage.error(error.value);
    console.error('初始化画布失败:', err);
  }
};

// 绘制网格
const drawGrid = (ctx: CanvasRenderingContext2D) => {
  ctx.strokeStyle = '#e0e0e0';
  ctx.lineWidth = 0.5;
  
  // 垂直线
  for (let x = 0; x < canvasWidth.value / scale.value; x += 50) {
    ctx.beginPath();
    ctx.moveTo(x, 0);
    ctx.lineTo(x, canvasHeight.value / scale.value);
    ctx.stroke();
  }
  
  // 水平线
  for (let y = 0; y < canvasHeight.value / scale.value; y += 50) {
    ctx.beginPath();
    ctx.moveTo(0, y);
    ctx.lineTo(canvasWidth.value / scale.value, y);
    ctx.stroke();
  }
};

// 绘制库位
const drawLocations = (ctx: CanvasRenderingContext2D) => {
  locations.value.forEach(location => {
    // 根据状态设置颜色
    const statusColor = {
      '1': '#67c23a',
      '0': '#f56c6c'
    }[location.status] || '#909399';
    
    // 如果库位被高亮显示，使用高亮颜色
    const fillColor = location.highlighted ? '#409eff' : statusColor;
    
    // 绘制库位矩形
    ctx.fillStyle = fillColor;
    ctx.fillRect(location.x, location.y, location.width, location.height);
    
    // 绘制容量进度条
    if (location.status === '1') {
      ctx.fillStyle = 'rgba(0, 0, 0, 0.2)';
      ctx.fillRect(location.x, location.y + location.height - 5, location.width, 5);
      
      // 根据容量百分比设置不同颜色
      let capacityColor = '#67c23a'; // 正常容量 - 绿色
      if (location.capacityPercentage > 80) {
        capacityColor = '#f56c6c'; // 高容量 - 红色
      } else if (location.capacityPercentage > 50) {
        capacityColor = '#e6a23c'; // 中容量 - 黄色
      }
      
      ctx.fillStyle = capacityColor;
      ctx.fillRect(location.x, location.y + location.height - 5, location.width * (location.capacityPercentage / 100), 5);
    }
    
    // 绘制边框
    if (location.recommended) {
      // 推荐库位使用特殊边框
      ctx.strokeStyle = '#f56c6c';
      ctx.lineWidth = 3;
      ctx.setLineDash([5, 5]);
      ctx.strokeRect(location.x, location.y, location.width, location.height);
      ctx.setLineDash([]);
    } else {
      // 普通库位边框
      ctx.strokeStyle = location.highlighted ? '#f56c6c' : '#409eff';
      ctx.lineWidth = location.highlighted ? 2 : 1;
      ctx.strokeRect(location.x, location.y, location.width, location.height);
    }
    
    // 绘制库位码
    ctx.fillStyle = '#ffffff';
    ctx.font = location.highlighted || location.recommended ? 'bold 14px Arial' : '12px Arial';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(location.locationCode, location.x + location.width / 2, location.y + location.height / 2 - 10);
    
    // 绘制容量百分比
    if (location.status === '1') {
      ctx.fillStyle = '#ffffff';
      ctx.font = '10px Arial';
      ctx.fillText(`${location.capacityPercentage}%`, location.x + location.width / 2, location.y + location.height / 2 + 8);
    }
    
    // 绘制推荐标记
    if (location.recommended) {
      ctx.fillStyle = '#f56c6c';
      ctx.font = 'bold 10px Arial';
      ctx.fillText('推荐', location.x + 15, location.y + 12);
    }
  });
};

// 处理缩放变化
const handleScaleChange = () => {
  initCanvas();
};

// 放大
const handleZoomIn = () => {
  scale.value = Math.min(scale.value + 0.1, 2);
  initCanvas();
};

// 缩小
const handleZoomOut = () => {
  scale.value = Math.max(scale.value - 0.1, 0.5);
  initCanvas();
};

// 重置
const handleReset = () => {
  scale.value = 1;
  offsetX.value = 0;
  offsetY.value = 0;
  initCanvas();
};

// 鼠标按下事件
const handleMouseDown = (event: MouseEvent) => {
  const canvas = canvasRef.value;
  if (!canvas) return;
  
  isDragging.value = true;
  lastMouseX.value = event.clientX;
  lastMouseY.value = event.clientY;
};

// 鼠标移动事件
const handleMouseMove = (event: MouseEvent) => {
  if (!isDragging.value) return;
  
  const deltaX = event.clientX - lastMouseX.value;
  const deltaY = event.clientY - lastMouseY.value;
  
  offsetX.value += deltaX;
  offsetY.value += deltaY;
  
  lastMouseX.value = event.clientX;
  lastMouseY.value = event.clientY;
  
  initCanvas();
};

// 鼠标释放事件
const handleMouseUp = () => {
  isDragging.value = false;
};

// 鼠标离开事件
const handleMouseLeave = () => {
  isDragging.value = false;
};

// 点击画布事件
const handleCanvasClick = (event: MouseEvent) => {
  const canvas = canvasRef.value;
  if (!canvas) return;
  
  const rect = canvas.getBoundingClientRect();
  const mouseX = (event.clientX - rect.left - offsetX.value) / scale.value;
  const mouseY = (event.clientY - rect.top - offsetY.value) / scale.value;
  
  // 检查是否点击了库位
  const clickedLocation = locations.value.find(location => {
    return (
      mouseX >= location.x &&
      mouseX <= location.x + location.width &&
      mouseY >= location.y &&
      mouseY <= location.y + location.height
    );
  });
  
  if (clickedLocation) {
    selectedLocation.value = { ...clickedLocation };
    detailVisible.value = true;
  }
};

// 搜索库位
const handleSearch = () => {
  if (!searchLocationCode.value) {
    // 如果搜索为空，重置所有库位状态
    locations.value.forEach(location => {
      location.highlighted = false;
    });
    initCanvas();
    return;
  }
  
  console.log('搜索库位:', searchLocationCode.value);
  
  // 标记匹配的库位
  const foundLocation = locations.value.find(location => 
    location.locationCode === searchLocationCode.value
  );
  
  // 重置所有库位的高亮状态
  locations.value.forEach(location => {
    location.highlighted = false;
  });
  
  // 高亮显示匹配的库位
  if (foundLocation) {
    foundLocation.highlighted = true;
    // 自动滚动到匹配的库位
    offsetX.value = -foundLocation.x * scale.value + canvasWidth.value / 2 - foundLocation.width * scale.value / 2;
    offsetY.value = -foundLocation.y * scale.value + canvasHeight.value / 2 - foundLocation.height * scale.value / 2;
  }
  
  initCanvas();
};

// 导入布局
const handleImport = () => {
  // 创建文件输入元素
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = '.json';
  
  // 监听文件选择事件
  input.onchange = async (e: any) => {
    const file = e.target.files[0];
    if (!file) return;
    
    loading.value = true;
    
    try {
      // 读取文件内容
      const content = await readFile(file);
      const data = JSON.parse(content);
      
      // 验证数据格式
      if (Array.isArray(data) && data.every(item => 
        typeof item.locationCode === 'string' && 
        typeof item.x === 'number' && 
        typeof item.y === 'number' && 
        typeof item.width === 'number' && 
        typeof item.height === 'number'
      )) {
        // 清空现有库位数据
        locations.value = data;
        // 重新初始化画布
        initCanvas();
        ElMessage.success(`布局导入成功，共导入 ${data.length} 个库位`);
        console.log('布局导入成功:', data.length, '个库位');
      } else {
        throw new Error('导入数据格式错误，缺少必要字段');
      }
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : '导入布局失败';
      ElMessage.error(errorMsg);
      console.error('导入布局失败:', err);
    } finally {
      loading.value = false;
    }
  };
  
  // 触发文件选择对话框
  input.click();
};

// 读取文件内容
const readFile = (file: File): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (event) => {
      resolve(event.target?.result as string);
    };
    reader.onerror = (event) => {
      reject(new Error('读取文件失败'));
    };
    reader.readAsText(file);
  });
};

// 导出布局
const handleExport = () => {
  try {
    // 准备导出数据，只包含必要的字段
    const exportData = locations.value.map(location => ({
      id: location.id,
      locationCode: location.locationCode,
      zoneCode: location.zoneCode,
      type: location.type,
      status: location.status,
      mixFlag: location.mixFlag,
      maxWeight: location.maxWeight,
      x: location.x,
      y: location.y,
      width: location.width,
      height: location.height
    }));
    
    if (exportData.length === 0) {
      ElMessage.warning('没有可导出的库位数据');
      return;
    }
    
    // 将数据转换为JSON字符串
    const jsonString = JSON.stringify(exportData, null, 2);
    
    // 创建Blob对象
    const blob = new Blob([jsonString], { type: 'application/json' });
    
    // 创建下载链接
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `warehouse-layout-${new Date().toISOString().slice(0, 10)}.json`;
    
    // 触发下载
    document.body.appendChild(a);
    a.click();
    
    // 清理
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    
    ElMessage.success(`布局导出成功，共导出 ${exportData.length} 个库位`);
    console.log('布局导出成功:', exportData.length, '个库位');
  } catch (err) {
    const errorMsg = err instanceof Error ? err.message : '导出布局失败';
    ElMessage.error(errorMsg);
    console.error('导出布局失败:', err);
  }
};

// 关闭详情对话框
const handleCloseDetail = () => {
  detailVisible.value = false;
};

// 保存库位信息
const handleSaveLocation = () => {
  if (!selectedLocation.value.locationCode) {
    ElMessage.error('库位码不能为空');
    return;
  }
  const index = locations.value.findIndex(item => item.id === selectedLocation.value.id);
  if (index === -1) {
    ElMessage.error('未找到对应的库位');
    return;
  }
  loading.value = true;
  const payload: any = {
    warehouseCode: selectedLocation.value.warehouseCode,
    zoneCode: selectedLocation.value.zoneCode,
    locationName: selectedLocation.value.locationCode,
    locationTypeCode: selectedLocation.value.type,
    status: selectedLocation.value.status,
    remark: ''
  };
  locationApi
    .update(String(selectedLocation.value.id), payload)
    .then((res: any) => {
      const row = res.data || {};
      const meta = locationTypeMetaByCode.value[String(row?.locationTypeCode ?? selectedLocation.value.type)] || { mixFlag: false, maxWeight: 0, label: '' };
      const current = locations.value[index]!;
      locations.value[index] = {
        ...current,
        type: String(row?.locationTypeCode ?? selectedLocation.value.type),
        status: String(row?.status ?? selectedLocation.value.status),
        mixFlag: Boolean(meta.mixFlag),
        maxWeight: Number(meta.maxWeight),
        zoneCode: String(row?.zoneCode ?? selectedLocation.value.zoneCode)
      };
      initCanvas();
      detailVisible.value = false;
      ElMessage.success('库位信息保存成功');
    })
    .catch((e: any) => {
      ElMessage.error(e?.message || '保存库位信息失败');
    })
    .finally(() => {
      loading.value = false;
    });
};

// 监听窗口大小变化
const handleResize = () => {
  initCanvas();
};

// 组件挂载时初始化画布
onMounted(() => {
  refreshMapData();
  window.addEventListener('resize', handleResize);
});

watch(
  () => selectedLocation.value.type,
  (type) => {
    const m = locationTypeMetaByCode.value[type] || { mixFlag: false, maxWeight: 0, label: '' };
    selectedLocation.value.mixFlag = Boolean(m.mixFlag);
    selectedLocation.value.maxWeight = Number(m.maxWeight);
  }
);

// 监听scale变化
watch(scale, () => {
  initCanvas();
});
</script>

<style scoped>
.warehouse-map-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
  box-sizing: border-box;
}

.operation-card {
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: all 0.3s ease;
}

.operation-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

:deep(.operation-card .el-card__body) {
  padding: 20px;
}

.map-card {
  height: calc(100vh - 220px);
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: all 0.3s ease;
}

.map-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

:deep(.map-card .el-card__body) {
  padding: 20px;
  display: flex;
  flex: 1;
  overflow: hidden;
}

.canvas-container {
  position: relative;
  flex: 1;
  overflow: auto;
  background-color: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

canvas {
  display: block;
  margin: 0 auto;
  background-color: #ffffff;
  cursor: move;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.02);
  transition: all 0.3s ease;
}

.canvas-container:hover {
  border-color: #c6e2ff;
  box-shadow: inset 0 1px 4px rgba(0, 0, 0, 0.08);
}

/* 优化操作按钮组 */
:deep(.operation-card .el-col:nth-child(2) .el-button) {
  margin-left: 8px;
}

/* 优化缩放控制 */
.zoom-control {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  background-color: #ffffff;
  padding: 12px;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 10;
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
}

.zoom-control:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-50%) scale(1.02);
}

:deep(.zoom-control .el-slider) {
  width: 60px;
  margin: 10px 0;
}

/* 优化详情对话框 */
:deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

:deep(.el-dialog__header) {
  background-color: #fafafa;
  border-bottom: 1px solid #ebeef5;
  padding: 20px 24px;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

:deep(.el-dialog__body) {
  padding: 24px;
  font-size: 14px;
}

:deep(.el-dialog__footer) {
  background-color: #fafafa;
  border-top: 1px solid #ebeef5;
  padding: 16px 24px;
}

/* 优化表单样式 */
:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
  font-size: 14px;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-input-number__wrapper) {
  border-radius: 4px;
  border-color: #dcdfe6;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover),
:deep(.el-input-number__wrapper:hover) {
  border-color: #c6e2ff;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focus),
:deep(.el-input-number__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 优化按钮样式 */
:deep(.el-button) {
  border-radius: 4px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-button--primary) {
  background-color: #409eff;
  border-color: #409eff;
}

:deep(.el-button--primary:hover) {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

/* 响应式设计优化 */
@media (max-width: 1200px) {
  .warehouse-map-container {
    padding: 20px;
  }
  
  .map-card {
    height: calc(100vh - 200px);
  }
}

@media (max-width: 768px) {
  .warehouse-map-container {
    padding: 12px;
  }
  
  :deep(.operation-card .el-card__body) {
    padding: 16px;
  }
  
  :deep(.operation-card .el-col) {
    margin-bottom: 12px;
  }
  
  .operation-card {
    margin-bottom: 16px;
  }
  
  .map-card {
    height: calc(100vh - 180px);
    margin-bottom: 16px;
  }
  
  :deep(.map-card .el-card__body) {
    padding: 16px;
  }
  
  .zoom-control {
    right: 12px;
    padding: 8px;
  }
  
  :deep(.zoom-control .el-slider) {
    width: 50px;
    margin: 8px 0;
  }
  
  :deep(.el-dialog) {
    margin: 10px;
    border-radius: 6px;
  }
  
  :deep(.el-dialog__header) {
    padding: 16px 20px;
  }
  
  :deep(.el-dialog__body) {
    padding: 20px;
  }
  
  :deep(.el-dialog__footer) {
    padding: 12px 20px;
  }
}

@media (max-width: 480px) {
  .warehouse-map-container {
    padding: 8px;
  }
  
  :deep(.operation-card .el-card__body) {
    padding: 12px;
  }
  
  :deep(.map-card .el-card__body) {
    padding: 12px;
  }
  
  canvas {
    max-width: 100%;
    height: auto;
  }
  
  .zoom-control {
    right: 8px;
    padding: 6px;
  }
  
  :deep(.zoom-control .el-slider) {
    width: 40px;
    margin: 6px 0;
  }
}
</style>
