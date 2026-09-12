<template>
  <div class="stock-query-scan-container">
    <!-- 扫描区域 -->
    <el-card class="scan-area-card">
      <div class="scan-section">
        <div class="section-title">
          <el-icon><Search /></el-icon>
          <span>扫码查库存</span>
        </div>
        <div class="scan-input-wrapper">
          <el-input 
            v-model="scanCode" 
            placeholder="扫码商品条码或物料编码"
            clearable
            @input="handleScanCodeInput"
            ref="scanCodeInput"
            size="large"
            class="scan-input"
            :loading="loading.query"
          >
            <template #append>
              <el-button type="primary" size="large" @click="handleScan" :loading="loading.scan">
                <el-icon class="scan-icon"><Search /></el-icon>
                扫码
              </el-button>
            </template>
          </el-input>
        </div>
        <div class="quick-actions">
          <el-button type="info" @click="queryByMaterialCode">按物料编码查询</el-button>
          <el-button type="info" @click="queryByLocation">按库位查询</el-button>
          <el-button type="info" @click="queryByBatch">按批次查询</el-button>
        </div>
        <div v-if="error" class="error-message">
          <el-alert :title="error" type="error" show-icon :closable="false" />
        </div>
      </div>
    </el-card>

    <!-- 查询条件弹窗 -->
    <el-dialog
      v-model="queryDialogVisible"
      :title="queryDialogTitle"
      width="500px"
      @close="resetQueryForm"
    >
      <el-form :model="queryForm" label-width="120px">
        <el-form-item label="物料编码" v-if="queryDialogType === 'material'">
          <el-input v-model="queryForm.materialCode" placeholder="输入物料编码"></el-input>
        </el-form-item>
        <el-form-item label="库位编码" v-if="queryDialogType === 'location'">
          <el-input v-model="queryForm.locationCode" placeholder="输入库位编码"></el-input>
        </el-form-item>
        <el-form-item label="批次号" v-if="queryDialogType === 'batch'">
          <el-input v-model="queryForm.batchNo" placeholder="输入批次号"></el-input>
        </el-form-item>
        <el-form-item label="仓库" v-if="queryDialogType !== 'material'">
          <el-select v-model="queryForm.warehouseCode" placeholder="选择仓库" clearable>
            <el-option v-for="w in warehouses" :key="w.value" :label="w.label" :value="w.value"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="queryDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 库存信息 -->
    <el-card class="stock-info-card" v-if="stockInfo">
      <template #header>
        <div class="card-header">
          <span>库存信息</span>
          <div class="header-actions">
            <span class="material-name">{{ stockInfo.materialName }}</span>
            <el-button type="primary" size="small" @click="exportStockDetails" :loading="loading.query">
              <el-icon><Document /></el-icon>
              导出明细
            </el-button>
          </div>
        </div>
      </template>
      <div class="stock-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="物料编码">{{ stockInfo.materialCode }}</el-descriptions-item>
          <el-descriptions-item label="物料名称">{{ stockInfo.materialName }}</el-descriptions-item>
          <el-descriptions-item label="规格">{{ stockInfo.specification }}</el-descriptions-item>
          <el-descriptions-item label="单位">{{ stockInfo.unit }}</el-descriptions-item>
          <el-descriptions-item label="总库存">{{ stockInfo.totalStock }}</el-descriptions-item>
          <el-descriptions-item label="可用库存">{{ stockInfo.availableStock }}</el-descriptions-item>
          <el-descriptions-item label="锁定库存">{{ stockInfo.lockedStock }}</el-descriptions-item>
          <el-descriptions-item label="仓库">{{ stockInfo.warehouseName }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 库存分布可视化 -->
      <div class="stock-visualization">
        <h3 class="visualization-title">库存分布</h3>
        <div class="distribution-cards">
          <el-statistic
            title="可用库存"
            :value="stockInfo.availableStock"
            :precision="0"
            class="stat-card success"
          >
            <template #suffix>
              <span class="unit">{{ stockInfo.unit }}</span>
            </template>
          </el-statistic>
          <el-statistic
            title="锁定库存"
            :value="stockInfo.lockedStock"
            :precision="0"
            class="stat-card warning"
          >
            <template #suffix>
              <span class="unit">{{ stockInfo.unit }}</span>
            </template>
          </el-statistic>
          <el-statistic
            title="总库存"
            :value="stockInfo.totalStock"
            :precision="0"
            class="stat-card info"
          >
            <template #suffix>
              <span class="unit">{{ stockInfo.unit }}</span>
            </template>
          </el-statistic>
        </div>
      </div>
    </el-card>

    <!-- 库存明细列表 -->
    <el-card class="stock-detail-card" v-if="stockDetails.length > 0">
      <template #header>
        <div class="card-header">
          <span>库存明细</span>
          <span class="detail-count">{{ stockDetails.length }}条记录</span>
        </div>
      </template>
      <el-table :data="stockDetails" stripe style="width: 100%">
        <el-table-column prop="locationCode" label="库位" width="120"></el-table-column>
        <el-table-column prop="batchNo" label="批次号" width="150"></el-table-column>
        <el-table-column prop="quantity" label="数量" width="100"></el-table-column>
        <el-table-column prop="availableQuantity" label="可用数量" width="120"></el-table-column>
        <el-table-column prop="lockedQuantity" label="锁定数量" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'normal' ? 'success' : 'warning'">
              {{ scope.row.status === 'normal' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expiryDate" label="有效期至" width="150"></el-table-column>
        <el-table-column prop="lastStockTime" label="最后库存时间" width="180"></el-table-column>
      </el-table>
    </el-card>

    <!-- 查询历史 -->
    <el-card class="history-card" v-if="queryHistory.length > 0">
      <template #header>
        <div class="card-header">
          <span>查询历史</span>
          <el-button link @click="clearHistory">清空历史</el-button>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item 
          v-for="(item, index) in queryHistory" 
          :key="index"
          :timestamp="item.timestamp"
        >
          <el-card shadow="hover" @click="replayQuery(item)">
            <div class="history-item">
              <div class="history-type">{{ item.type }}</div>
              <div class="history-content">
                <div class="material-name">{{ item.materialName }}</div>
                <div class="material-code">{{ item.materialCode }}</div>
              </div>
              <el-tag size="small" :type="item.success ? 'success' : 'warning'">
                {{ item.success ? '成功' : '失败' }}
              </el-tag>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- 空状态 -->
    <el-empty 
      description="请扫码或输入查询条件" 
      v-if="!stockInfo && stockDetails.length === 0"
      image="search"
      class="empty-state"
    ></el-empty>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { Search, Document } from '@element-plus/icons-vue';
import { ElMessage, ElLoading } from 'element-plus';
import { pdaApi, warehouseApi } from '@/api/wms';

// 扫描码
const scanCode = ref('');

// 引用
const scanCodeInput = ref();

// 加载状态
const loading = ref({
  query: false,
  scan: false
});

// 错误信息
const error = ref('');

// 查询条件对话框
const queryDialogVisible = ref(false);
const queryDialogType = ref('material');
const queryDialogTitle = computed(() => {
  const titleMap = {
    material: '按物料编码查询',
    location: '按库位查询',
    batch: '按批次查询'
  };
  return titleMap[queryDialogType.value as keyof typeof titleMap] || '查询库存';
});

// 查询表单
const queryForm = ref({
  materialCode: '',
  locationCode: '',
  batchNo: '',
  warehouseCode: ''
});

// 库存信息
const stockInfo = ref<any | null>(null);

// 库存明细类型定义
interface StockDetail {
  locationCode: string;
  batchNo: string;
  quantity: number;
  availableQuantity: number;
  lockedQuantity: number;
  status: string;
  expiryDate: string;
  lastStockTime: string;
}

// 库存明细
const stockDetails = ref([] as StockDetail[]);

const warehouses = ref<{ label: string; value: string }[]>([]);
const warehouseNameByCode = ref<Record<string, string>>({});

// 查询历史
const queryHistory = ref<any[]>([]);

// 防抖计时器
let debounceTimer: number | null = null;

// 扫描码输入处理
const handleScanCodeInput = () => {
  // 清除之前的错误
  error.value = '';
  
  if (scanCode.value) {
    // 防抖处理
    if (debounceTimer) {
      clearTimeout(debounceTimer);
    }
    
    debounceTimer = window.setTimeout(() => {
      queryStock();
    }, 500);
  }
};

// 扫码按钮点击
const handleScan = () => {
  if (!scanCode.value) {
    ElMessage.warning('请输入条码或物料编码');
    return;
  }
  queryStock();
};

// 按物料编码查询
const queryByMaterialCode = () => {
  queryDialogType.value = 'material';
  queryDialogVisible.value = true;
};

// 按库位查询
const queryByLocation = () => {
  queryDialogType.value = 'location';
  queryDialogVisible.value = true;
};

// 按批次查询
const queryByBatch = () => {
  queryDialogType.value = 'batch';
  queryDialogVisible.value = true;
};

// 表单验证
const validateQueryForm = () => {
  if (queryDialogType.value === 'material' && !queryForm.value.materialCode) {
    error.value = '请输入物料编码';
    return false;
  }
  
  if (queryDialogType.value === 'location' && !queryForm.value.locationCode) {
    error.value = '请输入库位编码';
    return false;
  }
  
  if (queryDialogType.value === 'batch' && !queryForm.value.batchNo) {
    error.value = '请输入批次号';
    return false;
  }

  if (queryDialogType.value !== 'material' && !queryForm.value.warehouseCode) {
    error.value = '请选择仓库';
    return false;
  }
  
  return true;
};

// 执行查询
const queryStock = async () => {
  // 清除之前的错误
  error.value = '';
  
  // 验证查询条件
  if (!scanCode.value) {
    if (!validateQueryForm()) {
      return;
    }
  }
  
  // 显示加载状态
  loading.value.query = true;

  try {
    const params: any = {};
    if (scanCode.value) {
      params.barcode = scanCode.value;
    } else if (queryDialogType.value === 'material') {
      params.materialCode = queryForm.value.materialCode;
    } else if (queryDialogType.value === 'location') {
      params.locationCode = queryForm.value.locationCode;
      params.warehouseCode = queryForm.value.warehouseCode;
    } else if (queryDialogType.value === 'batch') {
      params.batchNo = queryForm.value.batchNo;
      params.warehouseCode = queryForm.value.warehouseCode;
    }

    const res = await pdaApi.queryInventory(params);
    const data = res.data || {};
    const info = data.stockInfo || null;
    const details = (data.stockDetails || []) as StockDetail[];

    if (info && info.warehouseCode) {
      info.warehouseName = warehouseNameByCode.value[String(info.warehouseCode)] ?? info.warehouseName;
    }

    stockInfo.value = info;
    stockDetails.value = details;

    const materialCodeResolved = String(info?.materialCode ?? scanCode.value ?? queryForm.value.materialCode ?? '');
    const materialNameResolved = String(info?.materialName ?? '');
    addToHistory({
      type: scanCode.value ? '扫码查询' : queryDialogTitle.value,
      materialCode: materialCodeResolved,
      materialName: materialNameResolved,
      success: true
    });

    ElMessage.success('查询成功');
    scanCode.value = '';
  } catch (err: any) {
    const msg = err?.message || '查询失败，请重试';
    ElMessage.error(msg);
    error.value = msg;
  } finally {
    loading.value.query = false;
  }
};

// 处理对话框查询
const handleQuery = () => {
  // 清除之前的错误
  error.value = '';
  
  if (validateQueryForm()) {
    queryStock();
    queryDialogVisible.value = false;
  }
};

// 重置查询表单
const resetQueryForm = () => {
  queryForm.value = {
    materialCode: '',
    locationCode: '',
    batchNo: '',
    warehouseCode: ''
  };
  error.value = '';
};

// 添加到查询历史
const addToHistory = (item: any) => {
  queryHistory.value.unshift({
    ...item,
    timestamp: new Date().toLocaleString(),
    id: Date.now()
  });
  
  // 只保留最近15条历史
  if (queryHistory.value.length > 15) {
    queryHistory.value = queryHistory.value.slice(0, 15);
  }
};

// 清空历史
const clearHistory = () => {
  queryHistory.value = [];
  ElMessage.success('历史记录已清空');
};

// 回放查询
const replayQuery = (item: any) => {
  scanCode.value = item.materialCode;
  handleScanCodeInput();
  ElMessage.info('正在回放查询...');
};

// 导出库存明细
const exportStockDetails = () => {
  if (stockDetails.value.length === 0) {
    ElMessage.warning('没有可导出的库存明细');
    return;
  }
  
  // 显示加载状态
  const exportLoading = ElLoading.service({
    lock: true,
    text: '导出中...',
    background: 'rgba(0, 0, 0, 0.7)'
  });
  
  try {
    const headers = ['库位', '批次号', '数量', '可用数量', '锁定数量', '状态', '有效期至', '最后库存时间'];
    const rows = stockDetails.value.map(item => [
      item.locationCode || '',
      item.batchNo || '',
      item.quantity ?? 0,
      item.availableQuantity ?? 0,
      item.lockedQuantity ?? 0,
      item.status || '',
      item.expiryDate || '',
      item.lastStockTime || ''
    ]);
    const csv = [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `库存明细_${new Date().toISOString().slice(0, 10)}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    ElMessage.success('库存明细导出成功');
  } catch (err) {
    ElMessage.error('导出失败，请重试');
  } finally {
    exportLoading.close();
  }
};

// 页面加载完成后聚焦到扫描输入框
onMounted(() => {
  warehouseApi
    .getList({ page: 0, size: 1000 })
    .then(res => {
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
    })
    .catch(() => {});

  setTimeout(() => {
    scanCodeInput.value?.focus();
  }, 100);
});
</script>

<style scoped>
.stock-query-scan-container {
  padding: 12px;
  background-color: transparent;
  min-height: auto;
}

.scan-area-card,
.stock-info-card,
.stock-detail-card,
.history-card {
  margin-bottom: 16px;
}

.scan-section {
  text-align: center;
  padding: 20px 0;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.section-title .el-icon {
  margin-right: 10px;
  color: #409eff;
  font-size: 20px;
}

.scan-input-wrapper {
  margin-bottom: 20px;
  max-width: 600px;
  margin: 0 auto 20px;
}

.scan-input {
  font-size: 16px;
}

.scan-icon {
  font-size: 20px;
}

.quick-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.material-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.material-code {
  font-size: 14px;
  color: #606266;
}

.detail-count {
  font-size: 14px;
  color: #409eff;
  font-weight: 600;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.history-type {
  background-color: #409eff;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  min-width: 80px;
  text-align: center;
}

.history-content {
  flex: 1;
  min-width: 0;
}

.empty-state {
  margin: 50px 0;
}

.empty-state .el-empty__description {
  margin-top: 20px;
  font-size: 16px;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stock-query-scan-container {
    padding: 12px;
  }
  
  .section-title {
    font-size: 20px;
  }
  
  .section-title .el-icon {
    font-size: 24px;
  }
  
  .scan-input-wrapper {
    margin-bottom: 15px;
  }
  
  .quick-actions {
    gap: 8px;
  }
  
  .quick-actions .el-button {
    font-size: 13px;
    padding: 8px 12px;
  }
  
  .history-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .history-content {
    width: 100%;
  }
}
</style>
