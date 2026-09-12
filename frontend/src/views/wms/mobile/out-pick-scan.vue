<template>
  <div class="out-pick-scan-container">
    <!-- 操作栏 -->
    <el-card class="operation-card">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="拣货任务号" :error="error.task">
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
          <el-form-item label="波次号">
            <el-input 
              v-model="formData.waveNo" 
              placeholder="输入或扫码波次号"
              clearable
              disabled
            ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </el-card>

    <!-- 任务信息 -->
    <el-card class="task-info-card" v-if="currentTask">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="任务类型">{{ taskType }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentTask.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="波次号">{{ currentTask.waveNo }}</el-descriptions-item>
        <el-descriptions-item label="预期数量">{{ currentTask.expectedQuantity }}</el-descriptions-item>
        <el-descriptions-item label="已拣货数量">{{ currentTask.pickedQuantity }}</el-descriptions-item>
        <el-descriptions-item label="剩余数量">{{ currentTask.remainingQuantity }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ priorityText }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">{{ taskStatus }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 商品列表 -->
    <el-card class="product-list-card" v-if="currentTask">
      <template #header>
        <div class="card-header">
          <span>待拣商品列表</span>
          <span class="product-count">{{ currentTask.products.length }}个商品</span>
        </div>
      </template>
      <el-table :data="currentTask.products" stripe size="small" style="width: 100%">
        <el-table-column prop="materialCode" label="商品编码" width="120"></el-table-column>
        <el-table-column prop="materialName" label="商品名称" width="200"></el-table-column>
        <el-table-column prop="specification" label="规格" width="150"></el-table-column>
        <el-table-column prop="expectedQuantity" label="预期数量" width="100"></el-table-column>
        <el-table-column prop="pickedQuantity" label="已拣数量" width="100"></el-table-column>
        <el-table-column prop="remainingQuantity" label="剩余数量" width="100"></el-table-column>
        <el-table-column prop="locationCode" label="目标库位" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'completed' ? 'success' : 'info'">
              {{ scope.row.status === 'completed' ? '已完成' : '待拣货' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
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
              <el-descriptions-item label="目标库位">{{ currentItem.targetLocation }}</el-descriptions-item>
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
          <el-form-item label="库位验证" v-if="formData.locationBarcode">
            <el-descriptions :column="1" size="small" style="margin:0">
              <el-descriptions-item label="扫描库位">{{ formData.locationBarcode }}</el-descriptions-item>
              <el-descriptions-item label="验证结果">
                <el-tag :type="locationValid ? 'success' : 'danger'">
                  {{ locationValid ? '验证通过' : '验证失败' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="建议库位" v-if="!locationValid">{{ currentItem.targetLocation || '请先扫描商品条码' }}</el-descriptions-item>
            </el-descriptions>
          </el-form-item>
        </div>
      </div>

      <div class="scan-section">
        <div class="section-title">
          <el-icon><Operation /></el-icon>
          <span>拣货确认</span>
        </div>
        <div class="confirm-section">
          <el-form-item label="拣货数量">
            <el-input-number 
              v-model="formData.quantity" 
              :min="1" 
              :max="currentTask ? currentTask.remainingQuantity : 99999"
              :step="1"
              :disabled="!currentItem || !locationValid"
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
            <el-button type="primary" @click="handleConfirmPick" :disabled="!currentItem?.materialCode || !currentItem?.targetLocation || !formData.locationBarcode || !locationValid || !formData.quantity || loading.pick" :loading="loading.pick">确认拣货</el-button>
            <el-button @click="handleReset" :loading="loading.pick">重置</el-button>
            <el-button type="danger" @click="handleCancelTask" v-if="currentTask?.id" :loading="loading.pick">取消任务</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 拣货记录 -->
    <el-card class="record-card">
      <template #header>
        <div class="card-header">
          <span>拣货记录</span>
          <span class="record-count">{{ pickRecords.length }}条</span>
        </div>
      </template>
      <el-table :data="pickRecords" stripe size="small" style="width: 100%">
        <el-table-column prop="materialCode" label="商品编码" width="120"></el-table-column>
        <el-table-column prop="materialName" label="商品名称" width="180"></el-table-column>
        <el-table-column prop="locationCode" label="拣货库位" width="100"></el-table-column>
        <el-table-column prop="quantity" label="数量" width="80"></el-table-column>
        <el-table-column prop="pickTime" label="拣货时间" width="160"></el-table-column>
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
import { ref, computed, watch, onMounted } from 'vue';
import { Search, Goods, Location, Operation } from '@element-plus/icons-vue';
import { ElMessage, ElLoading } from 'element-plus';
import request, { unwrapPageResponse, unwrapResponseData } from '@/api';
import { locationApi } from '@/api/wms';

// 表单数据
const formData = ref({
  taskNo: '',
  waveNo: '',
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
  pick: false
});

// 错误信息
const error = ref({
  task: '',
  item: '',
  location: ''
});

// 产品类型定义
interface Product {
  id: string;
  materialCode: string;
  materialName: string;
  specification: string;
  expectedQuantity: number;
  pickedQuantity: number;
  remainingQuantity: number;
  locationCode: string;
  status: string;
}

// 当前任务信息
const currentTask = ref({
  id: '',
  taskNo: '',
  orderNo: '',
  waveNo: '',
  expectedQuantity: 100,
  pickedQuantity: 0,
  remainingQuantity: 100,
  priority: 'medium',
  status: 'pending',
  products: [] as Product[]
});

// 当前商品信息
const currentItem = ref({
  materialCode: '',
  materialName: '',
  specification: '',
  batchNo: '',
  unit: '个',
  targetLocation: ''
});

// 库位验证结果
const locationValid = ref(false);

// 拣货记录
const pickRecords = ref<any[]>([]);

// 防抖计时器
let debounceTimer: number | null = null;

// 计算属性
const taskType = computed(() => '拣货任务');
const priorityText = computed(() => {
  const priorityMap = {
    high: '高',
    medium: '中',
    low: '低'
  };
  return priorityMap[currentTask.value.priority as keyof typeof priorityMap] || currentTask.value.priority;
});
const taskStatus = computed(() => {
  const statusMap = {
    pending: '待执行',
    in_progress: '执行中',
    completed: '已完成',
    cancelled: '已取消'
  };
  return statusMap[currentTask.value.status as keyof typeof statusMap] || currentTask.value.status;
});

// 计算已完成商品数量
const completedProductsCount = computed(() => {
  return currentTask.value.products.filter(p => p.status === 'completed').length;
});

// 计算总商品数量
const totalProductsCount = computed(() => {
  return currentTask.value.products.length;
});

// 表单验证
const validateTaskNo = (value: string) => {
  if (!value) {
    return '请输入拣货任务号';
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

      request.get('/api/v1/wms/pda/outbound/task', { params: { taskNo: formData.value.taskNo } })
        .then((res: any) => {
          const data = unwrapResponseData<any>(res) ?? {};
          const products: Product[] = Array.isArray(data?.products) ? data.products.map((p: any, idx: number) => ({
            id: String(p?.id ?? idx + 1),
            materialCode: String(p?.materialCode ?? ''),
            materialName: String(p?.materialName ?? ''),
            specification: String(p?.specification ?? ''),
            expectedQuantity: Number(p?.expectedQuantity ?? 0),
            pickedQuantity: Number(p?.pickedQuantity ?? 0),
            remainingQuantity: Number(p?.remainingQuantity ?? 0),
            locationCode: String(p?.locationCode ?? ''),
            status: String(p?.status ?? 'pending')
          })) : []

          currentTask.value = {
            id: String(data?.id ?? ''),
            taskNo: String(data?.taskNo ?? formData.value.taskNo),
            orderNo: String(data?.orderNo ?? ''),
            waveNo: String(data?.waveNo ?? ''),
            expectedQuantity: Number(data?.expectedQuantity ?? 0),
            pickedQuantity: Number(data?.pickedQuantity ?? 0),
            remainingQuantity: Number(data?.remainingQuantity ?? 0),
            priority: String(data?.priority ?? 'medium'),
            status: String(data?.status ?? 'pending'),
            products
          }
          formData.value.waveNo = currentTask.value.waveNo
          formData.value.itemBarcode = ''
          formData.value.locationBarcode = ''
          formData.value.quantity = 1
          currentItem.value = {
            materialCode: '',
            materialName: '',
            specification: '',
            batchNo: '',
            unit: '个',
            targetLocation: ''
          }
          locationValid.value = false
          ElMessage.success('任务加载成功')
        })
        .catch(() => {
          ElMessage.error('任务加载失败，请检查任务号是否正确')
          error.value.task = '任务加载失败，请检查任务号是否正确'
        })
        .finally(() => {
          loading.value.task = false
        })
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
        const barcode = formData.value.itemBarcode.trim()
        const product = currentTask.value.products.find(p => p.materialCode.toUpperCase() === barcode.toUpperCase())
        if (!product) {
          throw new Error('not_found')
        }
        currentItem.value = {
          materialCode: product.materialCode,
          materialName: product.materialName,
          specification: product.specification,
          batchNo: '',
          unit: '个',
          targetLocation: product.locationCode
        }
        locationValid.value = false
        ElMessage.success('商品信息加载成功')
      } catch (err) {
        ElMessage.error('商品信息加载失败，请检查商品条码是否正确')
        error.value.item = '商品信息加载失败，请检查商品条码是否正确'
      } finally {
        loading.value.item = false
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
          const list = unwrapPageResponse(res).list
          const barcode = formData.value.locationBarcode.trim()
          const exists = Array.isArray(list) ? list.some((it: any) => it?.locationCode === barcode) : false
          if (!exists) {
            throw new Error('not_found')
          }
          if (!currentItem.value.targetLocation) {
            locationValid.value = false
            error.value.location = '请先扫描商品条码获取目标库位'
            ElMessage.warning('请先扫描商品条码获取目标库位')
            return
          }
          if (currentItem.value.targetLocation === barcode) {
            locationValid.value = true
            ElMessage.success('库位验证通过')
            return
          }
          locationValid.value = false
          error.value.location = '库位不匹配，请检查'
          ElMessage.warning('库位不匹配，请检查')
        })
        .catch(() => {
          locationValid.value = false
          ElMessage.error('库位验证失败，请重试')
          error.value.location = '库位验证失败，请重试'
        })
        .finally(() => {
          loading.value.location = false
        })
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

// 确认拣货
const handleConfirmPick = () => {
  // 验证表单
  if (!currentTask.value.id) {
    ElMessage.warning('请先加载拣货任务');
    return;
  }
  
  if (!currentItem.value.materialCode) {
    ElMessage.warning('请先扫描商品条码');
    return;
  }
  
  if (!currentItem.value.targetLocation) {
    ElMessage.warning('商品信息未获取到目标库位');
    return;
  }
  
  if (!formData.value.locationBarcode) {
    ElMessage.warning('请先扫描库位条码');
    return;
  }
  
  if (!locationValid.value) {
    ElMessage.warning('库位验证未通过，请检查库位条码');
    return;
  }
  
  if (!formData.value.quantity || formData.value.quantity <= 0) {
    ElMessage.warning('请输入有效的拣货数量');
    return;
  }
  
  // 显示加载状态
  loading.value.pick = true;

  request.post(`/api/v1/wms/pda/outbound/${currentTask.value.id}/pick`, {
    barcode: currentItem.value.materialCode,
    locationCode: formData.value.locationBarcode,
    quantity: formData.value.quantity,
    operator: 'PDA'
  }).then((res: any) => {
    const data = unwrapResponseData<any>(res) ?? {}
    if (data?.record) {
      pickRecords.value.unshift(data.record)
    }
    if (data?.task) {
      const t = data.task
      const products: Product[] = Array.isArray(t?.products) ? t.products.map((p: any, idx: number) => ({
        id: String(p?.id ?? idx + 1),
        materialCode: String(p?.materialCode ?? ''),
        materialName: String(p?.materialName ?? ''),
        specification: String(p?.specification ?? ''),
        expectedQuantity: Number(p?.expectedQuantity ?? 0),
        pickedQuantity: Number(p?.pickedQuantity ?? 0),
        remainingQuantity: Number(p?.remainingQuantity ?? 0),
        locationCode: String(p?.locationCode ?? ''),
        status: String(p?.status ?? 'pending')
      })) : []
      currentTask.value = {
        ...currentTask.value,
        ...t,
        id: String(t?.id ?? currentTask.value.id),
        taskNo: String(t?.taskNo ?? currentTask.value.taskNo),
        orderNo: String(t?.orderNo ?? currentTask.value.orderNo),
        waveNo: String(t?.waveNo ?? currentTask.value.waveNo),
        expectedQuantity: Number(t?.expectedQuantity ?? currentTask.value.expectedQuantity),
        pickedQuantity: Number(t?.pickedQuantity ?? currentTask.value.pickedQuantity),
        remainingQuantity: Number(t?.remainingQuantity ?? currentTask.value.remainingQuantity),
        priority: String(t?.priority ?? currentTask.value.priority),
        status: String(t?.status ?? currentTask.value.status),
        products
      }
    }
    ElMessage.success('拣货成功')

    formData.value.itemBarcode = ''
    formData.value.locationBarcode = ''
    formData.value.quantity = 1
    currentItem.value = {
      materialCode: '',
      materialName: '',
      specification: '',
      batchNo: '',
      unit: '个',
      targetLocation: ''
    }
    locationValid.value = false
    error.value = {
      task: '',
      item: '',
      location: ''
    }
    itemBarcodeInput.value?.focus()
  }).catch(() => {
    ElMessage.error('拣货失败，请重试')
  }).finally(() => {
    loading.value.pick = false
  })
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
    unit: '个',
    targetLocation: ''
  };
  locationValid.value = false;
  
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
    ElMessage.warning('请先加载拣货任务');
    return;
  }
  
  // 显示确认对话框
  if (confirm('确定要取消该拣货任务吗？')) {
    // 显示加载状态
    const cancelLoading = ElLoading.service({
      lock: true,
      text: '取消任务中...',
      background: 'rgba(0, 0, 0, 0.7)'
    });

    try {
      currentTask.value.status = 'cancelled';
      ElMessage.success('任务已取消');
    } catch (err) {
      ElMessage.error('任务取消失败，请重试');
    } finally {
      cancelLoading.close();
    }
  }
};

// 监听任务状态变化，显示相应的提示
watch(() => currentTask.value.status, (newStatus) => {
  if (newStatus === 'completed') {
    ElMessage.success('拣货任务已完成');
  } else if (newStatus === 'cancelled') {
    ElMessage.info('拣货任务已取消');
  }
});

// 页面加载完成后聚焦到任务号输入框
onMounted(() => {
  setTimeout(() => {
    taskNoInput.value?.focus();
  }, 100);
});
</script>

<style scoped>
.out-pick-scan-container {
  padding: 12px;
  background-color: transparent;
  min-height: auto;
}

.operation-card,
.task-info-card,
.product-list-card,
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

.product-count,
.record-count {
  font-size: 14px;
  color: #409eff;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .out-pick-scan-container {
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
