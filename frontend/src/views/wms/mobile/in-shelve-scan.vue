<template>
  <div class="in-shelve-scan-container">
    <!-- 操作栏 -->
    <el-card class="operation-card">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="上架任务号" :error="error.task">
            <el-input 
              v-model="formData.taskNo" 
              placeholder="输入或扫码任务号"
              clearable
              @input="handleTaskNoInput"
              ref="taskNoInput"
              :loading="loading.task"
            >
              <template #append>
                <el-button @click="handleScanTaskNo" :loading="loading.task">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="仓库">
            <el-select v-model="formData.warehouseId" placeholder="选择仓库" disabled>
              <el-option label="主仓库" value="1"></el-option>
              <el-option label="辅仓库" value="2"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </el-card>

    <!-- 任务信息 -->
    <el-card class="task-info-card" v-if="currentTask">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="任务类型">{{ taskType }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentTask.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="预期数量">{{ currentTask.expectedQuantity }}</el-descriptions-item>
        <el-descriptions-item label="已上架数量">{{ currentTask.shelvedQuantity }}</el-descriptions-item>
        <el-descriptions-item label="剩余数量">{{ currentTask.remainingQuantity }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">{{ taskStatus }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 扫描区域 -->
    <el-card class="scan-area-card">
      <div class="scan-section">
        <div class="section-title">
          <el-icon><Goods /></el-icon>
          <span>商品扫描</span>
        </div>
        <div class="scan-input-group">
          <el-form-item label="商品条码" :error="error.item">
            <el-input 
              v-model="formData.itemBarcode" 
              placeholder="扫码商品条码"
              clearable
              @input="handleItemBarcodeInput"
              ref="itemBarcodeInput"
              :loading="loading.item"
            >
              <template #append>
                <el-button type="primary" @click="handleScanItemBarcode" :loading="loading.item">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="商品信息" v-if="currentItem">
            <el-descriptions :column="1" size="small" style="margin:0">
              <el-descriptions-item label="商品编码">{{ currentItem.materialCode }}</el-descriptions-item>
              <el-descriptions-item label="商品名称">{{ currentItem.materialName }}</el-descriptions-item>
              <el-descriptions-item label="规格">{{ currentItem.specification }}</el-descriptions-item>
              <el-descriptions-item label="批次号">{{ currentItem.batchNo }}</el-descriptions-item>
              <el-descriptions-item label="单位">{{ currentItem.unit }}</el-descriptions-item>
            </el-descriptions>
          </el-form-item>
        </div>
      </div>

      <div class="scan-section">
        <div class="section-title">
          <el-icon><Location /></el-icon>
          <span>库位扫描</span>
        </div>
        <div class="scan-input-group">
          <el-form-item label="库位条码" :error="error.location">
            <el-input 
              v-model="formData.locationBarcode" 
              placeholder="扫码库位条码"
              clearable
              @input="handleLocationBarcodeInput"
              ref="locationBarcodeInput"
              :loading="loading.location"
            >
              <template #append>
                <el-button type="primary" @click="handleScanLocationBarcode" :loading="loading.location">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="库位信息" v-if="currentLocation">
            <el-descriptions :column="1" size="small" style="margin:0">
              <el-descriptions-item label="库位编码">{{ currentLocation.locationCode }}</el-descriptions-item>
              <el-descriptions-item label="库区">{{ currentLocation.zoneCode }}</el-descriptions-item>
              <el-descriptions-item label="库位类型">{{ currentLocation.locationType }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ currentLocation.status }}</el-descriptions-item>
            </el-descriptions>
          </el-form-item>
        </div>
      </div>

      <div class="scan-section">
        <div class="section-title">
          <el-icon><Operation /></el-icon>
          <span>上架确认</span>
        </div>
        <div class="confirm-section">
          <el-form-item label="上架数量">
            <el-input-number 
              v-model="formData.quantity" 
              :min="1" 
              :max="currentTask ? currentTask.remainingQuantity : 99999"
              :step="1"
              :disabled="!currentItem || !currentLocation"
            ></el-input-number>
          </el-form-item>
          <el-form-item label="备注">
            <el-input 
              v-model="formData.remark" 
              type="textarea" 
              placeholder="输入备注信息"
              :rows="2"
            ></el-input>
          </el-form-item>
          <div class="action-buttons">
            <el-button type="primary" @click="handleConfirmShelve" :disabled="!currentItem?.materialCode || !currentLocation?.locationCode || !formData.quantity || loading.shelve" :loading="loading.shelve">确认上架</el-button>
            <el-button @click="handleReset" :loading="loading.shelve">重置</el-button>
            <el-button type="danger" @click="handleCancelTask" v-if="currentTask?.id" :loading="loading.shelve">取消任务</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 上架记录 -->
    <el-card class="record-card">
      <template #header>
        <div class="card-header">
          <span>上架记录</span>
          <span class="record-count">{{ shelveRecords.length }}条</span>
        </div>
      </template>
      <el-table :data="shelveRecords" stripe size="small" style="width: 100%">
        <el-table-column prop="materialCode" label="商品编码" width="120"></el-table-column>
        <el-table-column prop="materialName" label="商品名称" width="180"></el-table-column>
        <el-table-column prop="locationCode" label="库位" width="100"></el-table-column>
        <el-table-column prop="quantity" label="数量" width="80"></el-table-column>
        <el-table-column prop="shelveTime" label="上架时间" width="160"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'success' ? 'success' : 'warning'">
              {{ scope.row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { Search, Goods, Location, Operation } from '@element-plus/icons-vue';
import { ElMessage, ElLoading } from 'element-plus';
import request, { unwrapPageResponse, unwrapResponseData } from '@/api';
import { asnApi, locationApi } from '@/api/wms';

// 表单数据
const formData = ref({
  taskNo: '',
  warehouseId: '1',
  itemBarcode: '',
  locationBarcode: '',
  quantity: 1,
  remark: ''
});

// 引用
const taskNoInput = ref();
const itemBarcodeInput = ref();
const locationBarcodeInput = ref();

// 加载状态
const loading = ref({
  task: false,
  item: false,
  location: false,
  shelve: false
});

// 错误信息
const error = ref({
  task: '',
  item: '',
  location: ''
});

// 当前任务信息
const currentTask = ref({
  id: '',
  taskNo: '',
  orderNo: '',
  expectedQuantity: 100,
  shelvedQuantity: 0,
  remainingQuantity: 100,
  status: 'pending'
});

const currentAsn = ref<any | null>(null);

// 当前商品信息
const currentItem = ref({
  materialCode: '',
  materialName: '',
  specification: '',
  batchNo: '',
  unit: '个'
});

// 当前库位信息
const currentLocation = ref({
  locationCode: '',
  zoneCode: '',
  locationType: '存储',
  status: '可用'
});

// 上架记录
const shelveRecords = ref<any[]>([]);

// 防抖计时器
let debounceTimer: number | null = null;

// 计算属性
const taskType = computed(() => '上架任务');
const taskStatus = computed(() => {
  const statusMap = {
    pending: '待执行',
    in_progress: '执行中',
    completed: '已完成',
    cancelled: '已取消'
  };
  return statusMap[currentTask.value.status as keyof typeof statusMap] || currentTask.value.status;
});

// 表单验证
const validateTaskNo = (value: string) => {
  if (!value) {
    return '请输入上架任务号';
  }
  if (value.length < 6) {
    return '任务号长度不能少于6位';
  }
  return '';
};

const validateItemBarcode = (value: string) => {
  if (!value) {
    return '请输入商品条码';
  }
  return '';
};

const validateLocationBarcode = (value: string) => {
  if (!value) {
    return '请输入库位条码';
  }
  return '';
};

// 任务号输入处理
const handleTaskNoInput = () => {
  // 清除之前的错误
  error.value.task = '';
  
  // 验证输入
  const validationError = validateTaskNo(formData.value.taskNo);
  if (validationError) {
    error.value.task = validationError;
    return;
  }
  
  // 防抖处理
  if (debounceTimer) {
    clearTimeout(debounceTimer);
  }
  
  debounceTimer = window.setTimeout(() => {
    if (formData.value.taskNo) {
      // 显示加载状态
      loading.value.task = true;

      request.get('/api/v1/wms/pda/inbound/task', { params: { taskNo: formData.value.taskNo } })
        .then(async (res: any) => {
          const data = unwrapResponseData<any>(res) ?? {};
          currentTask.value = {
            id: data?.id ?? '',
            taskNo: data?.taskNo ?? formData.value.taskNo,
            orderNo: data?.orderNo ?? '',
            expectedQuantity: data?.expectedQuantity ?? 0,
            shelvedQuantity: data?.shelvedQuantity ?? 0,
            remainingQuantity: data?.remainingQuantity ?? 0,
            status: data?.status ?? 'pending'
          };

          if (currentTask.value.id) {
            const asnRes = await asnApi.getDetail(currentTask.value.id);
            // api包装层已解包，asnRes.data即为ASN详情
            currentAsn.value = asnRes?.data ?? null;
          } else {
            currentAsn.value = null;
          }
          ElMessage.success('任务加载成功');
        })
        .catch(() => {
          ElMessage.error('任务加载失败，请检查任务号是否正确');
          error.value.task = '任务加载失败，请检查任务号是否正确';
          currentAsn.value = null;
        })
        .finally(() => {
          loading.value.task = false;
        });
    }
  }, 300);
};

// 商品条码输入处理
const handleItemBarcodeInput = () => {
  // 清除之前的错误
  error.value.item = '';
  
  // 验证输入
  const validationError = validateItemBarcode(formData.value.itemBarcode);
  if (validationError) {
    error.value.item = validationError;
    return;
  }
  
  // 防抖处理
  if (debounceTimer) {
    clearTimeout(debounceTimer);
  }
  
  debounceTimer = window.setTimeout(() => {
    if (formData.value.itemBarcode) {
      // 显示加载状态
      loading.value.item = true;

      try {
        const asn = currentAsn.value;
        const items: any[] = Array.isArray(asn?.items) ? asn.items : [];
        const barcode = formData.value.itemBarcode.trim();
        const match = items.find((it) => String(it?.materialCode ?? '').toUpperCase() === barcode.toUpperCase());
        if (!match) {
          throw new Error('not_found');
        }
        currentItem.value = {
          materialCode: match.materialCode ?? barcode,
          materialName: match.materialName ?? '',
          specification: '',
          batchNo: match.batchNo ?? '',
          unit: match.unit ?? '个'
        };
        ElMessage.success('商品信息加载成功');
      } catch (err) {
        ElMessage.error('商品信息加载失败，请检查商品条码是否正确');
        error.value.item = '商品信息加载失败，请检查商品条码是否正确';
      } finally {
        loading.value.item = false;
      }
    }
  }, 300);
};

// 库位条码输入处理
const handleLocationBarcodeInput = () => {
  // 清除之前的错误
  error.value.location = '';
  
  // 验证输入
  const validationError = validateLocationBarcode(formData.value.locationBarcode);
  if (validationError) {
    error.value.location = validationError;
    return;
  }
  
  // 防抖处理
  if (debounceTimer) {
    clearTimeout(debounceTimer);
  }
  
  debounceTimer = window.setTimeout(() => {
    if (formData.value.locationBarcode) {
      // 显示加载状态
      loading.value.location = true;

      locationApi.getList({ page: 1, size: 10, keyword: formData.value.locationBarcode })
        .then((res: any) => {
          const list = unwrapPageResponse(res).list;
          const barcode = formData.value.locationBarcode.trim();
          const match = Array.isArray(list) ? list.find((it: any) => it?.locationCode === barcode) : null;
          if (!match) {
            throw new Error('not_found');
          }
          currentLocation.value = {
            locationCode: match.locationCode ?? barcode,
            zoneCode: match.zoneCode ?? '',
            locationType: match.locationTypeCode ?? '存储',
            status: match.status ?? '可用'
          };
          ElMessage.success('库位信息加载成功');
        })
        .catch(() => {
          ElMessage.error('库位信息加载失败，请检查库位条码是否正确');
          error.value.location = '库位信息加载失败，请检查库位条码是否正确';
        })
        .finally(() => {
          loading.value.location = false;
        });
    }
  }, 300);
};

// 扫码任务号
const handleScanTaskNo = () => {
  handleTaskNoInput();
};

// 扫码商品
const handleScanItemBarcode = () => {
  handleItemBarcodeInput();
};

// 扫码库位
const handleScanLocationBarcode = () => {
  handleLocationBarcodeInput();
};

// 确认上架
const handleConfirmShelve = () => {
  // 验证表单
  if (!currentTask.value.id) {
    ElMessage.warning('请先加载上架任务');
    return;
  }
  
  if (!currentItem.value.materialCode) {
    ElMessage.warning('请先扫描商品条码');
    return;
  }
  
  if (!currentLocation.value.locationCode) {
    ElMessage.warning('请先扫描库位条码');
    return;
  }
  
  if (!formData.value.quantity || formData.value.quantity <= 0) {
    ElMessage.warning('请输入有效的上架数量');
    return;
  }
  
  // 显示加载状态
  loading.value.shelve = true;

  request.post(`/api/v1/wms/pda/inbound/${currentTask.value.id}/putaway`, {
    barcode: currentItem.value.materialCode,
    locationCode: currentLocation.value.locationCode,
    quantity: formData.value.quantity,
    operator: 'PDA'
  }).then((res: any) => {
    const data = unwrapResponseData<any>(res) ?? {};
    if (data?.record) {
      shelveRecords.value.unshift(data.record);
    }
    if (data?.task) {
      currentTask.value = {
        ...currentTask.value,
        ...data.task
      };
    }
    ElMessage.success('上架成功');

    formData.value.itemBarcode = '';
    formData.value.locationBarcode = '';
    formData.value.quantity = 1;
    currentItem.value = {
      materialCode: '',
      materialName: '',
      specification: '',
      batchNo: '',
      unit: '个'
    };
    currentLocation.value = {
      locationCode: '',
      zoneCode: '',
      locationType: '存储',
      status: '可用'
    };
    error.value = {
      task: '',
      item: '',
      location: ''
    };
    itemBarcodeInput.value?.focus();
  }).catch(() => {
    ElMessage.error('上架失败，请重试');
  }).finally(() => {
    loading.value.shelve = false;
  });
};

// 重置
const handleReset = () => {
  formData.value.itemBarcode = '';
  formData.value.locationBarcode = '';
  formData.value.quantity = 1;
  formData.value.remark = '';
  currentItem.value = {
    materialCode: '',
    materialName: '',
    specification: '',
    batchNo: '',
    unit: '个'
  };
  currentLocation.value = {
    locationCode: '',
    zoneCode: '',
    locationType: '存储',
    status: '可用'
  };
  
  // 清除错误信息
  error.value = {
    task: '',
    item: '',
    location: ''
  };
  
  ElMessage.info('表单已重置');
};

// 取消任务
const handleCancelTask = () => {
  if (!currentTask.value.id) {
    ElMessage.warning('请先加载上架任务');
    return;
  }
  
  // 显示确认对话框
  if (confirm('确定要取消该上架任务吗？')) {
    // 显示加载状态
    const cancelLoading = ElLoading.service({
      lock: true,
      text: '取消任务中...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
    asnApi.cancel(currentTask.value.id)
      .then(() => {
        currentTask.value.status = 'cancelled';
        ElMessage.success('任务已取消');
      })
      .catch(() => {
        ElMessage.error('任务取消失败，请重试');
      })
      .finally(() => {
        cancelLoading.close();
      });
  }
};

// 页面加载完成后聚焦到任务号输入框
onMounted(() => {
  setTimeout(() => {
    taskNoInput.value?.focus();
  }, 100);
});
</script>

<style scoped>
.in-shelve-scan-container {
  padding: 12px;
  background-color: transparent;
  min-height: auto;
}

.operation-card,
.task-info-card,
.scan-area-card,
.record-card {
  margin-bottom: 16px;
}

.scan-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #ebeef5;
}

.scan-section:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.section-title {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.section-title .el-icon {
  margin-right: 8px;
  color: #409eff;
}

.scan-input-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.scan-input-group .el-form-item {
  flex: 1;
  min-width: 220px;
}

.confirm-section {
  padding: 15px;
  background-color: #fafafa;
  border-radius: 4px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  margin-top: 15px;
  justify-content: flex-end;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-count {
  font-size: 14px;
  color: #409eff;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .in-shelve-scan-container {
    padding: 12px;
  }
  
  .scan-input-group {
    flex-direction: column;
  }
  
  .scan-input-group .el-form-item {
    min-width: auto;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
}
</style>
