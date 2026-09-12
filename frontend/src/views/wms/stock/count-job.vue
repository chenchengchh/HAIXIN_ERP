<template>
  <div class="count-job-container">
    <!-- 操作栏 -->
    <el-card class="operation-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            placeholder="搜索盘点单号"
            v-model="searchForm.countNo"
            clearable
          ></el-input>
        </el-col>
        <el-col :span="6">
          <el-select
            placeholder="选择仓库"
            v-model="searchForm.warehouseCode"
            clearable
          >
            <el-option
              v-for="warehouse in warehouses"
              :key="warehouse.id"
              :label="warehouse.name"
              :value="warehouse.id"
            ></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select
            placeholder="选择状态"
            v-model="searchForm.status"
            clearable
          >
            <el-option label="待执行" value="0"></el-option>
            <el-option label="执行中" value="1"></el-option>
            <el-option label="已完成" value="2"></el-option>
            <el-option label="已取消" value="3"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-col>
        <el-col :span="4">
          <el-button type="success" @click="handleCreate">创建盘点任务</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 盘点任务列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="countJobs"
        style="width: 100%"
      >
        <el-table-column prop="countNo" label="盘点单号" width="150"></el-table-column>
        <el-table-column prop="warehouseName" label="仓库" width="120"></el-table-column>
        <el-table-column prop="countType" label="盘点类型" width="100">
          <template #default="scope">
            <el-tag
              :type="COUNT_TYPE_COLOR_MAP[scope.row.countType as CountType]"
            >
              {{ COUNT_TYPE_MAP[scope.row.countType as CountType] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag
              :type="COUNT_STATUS_COLOR_MAP[scope.row.status as CountStatus]"
            >
              {{ COUNT_STATUS_MAP[scope.row.status as CountStatus] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalItemCount" label="总物料数" width="100"></el-table-column>
        <el-table-column prop="finishedItemCount" label="已盘数量" width="100"></el-table-column>
        <el-table-column prop="diffCount" label="差异数量" width="100">
          <template #default="scope">
            <el-tag type="danger" v-if="scope.row.diffCount > 0">{{ scope.row.diffCount }}</el-tag>
            <span v-else>{{ scope.row.diffCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column prop="createUser" label="创建人" width="100"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleStart(scope.row)"
              v-if="scope.row.status === '0'"
            >
              开始盘点
            </el-button>
            <el-button
              size="small"
              type="success"
              @click="handleContinue(scope.row)"
              v-if="scope.row.status === '1'"
            >
              继续盘点
            </el-button>
            <el-button
              size="small"
              type="warning"
              @click="handleFinish(scope.row)"
              v-if="scope.row.status === '1'"
            >
              完成盘点
            </el-button>
            <el-button
              size="small"
              @click="handleView(scope.row)"
              v-if="scope.row.status !== '3'"
            >
              查看
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleCancel(scope.row)"
              v-if="scope.row.status === '0' || scope.row.status === '1'"
            >
              取消
            </el-button>
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

    <!-- 盘点执行对话框 -->
    <el-dialog
      v-model="executeDialogVisible"
      :title="executeDialogTitle"
      width="1000px"
      :before-close="handleExecuteDialogClose"
    >
      <div class="execute-dialog-content">
        <!-- 盘点基本信息 -->
        <div class="count-info">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="info-item">
                <label>盘点单号:</label>
                <span>{{ currentCountJob?.countNo }}</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="info-item">
                <label>仓库:</label>
                <span>{{ currentCountJob?.warehouseName }}</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="info-item">
                <label>盘点类型:</label>
                <el-tag :type="COUNT_TYPE_COLOR_MAP[currentCountJob?.countType || '1']">
                  {{ COUNT_TYPE_MAP[currentCountJob?.countType || '1'] }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="info-item">
                <label>状态:</label>
                <el-tag
                  :type="COUNT_STATUS_COLOR_MAP[currentCountJob?.status || '0']"
                >
                  {{ COUNT_STATUS_MAP[currentCountJob?.status || '0'] }}
                </el-tag>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 扫码区域 -->
        <el-card class="scan-card">
          <div class="scan-title">扫码盘点</div>
          <div class="scan-content">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="scan-input-area">
                  <el-input
                    v-model="scanCode"
                    placeholder="请扫描物料或库位条码"
                    clearable
                    @keyup.enter="handleScan"
                    autofocus
                    :disabled="!isEditable"
                  >
                    <template #append>
                      <el-button type="primary" @click="handleScan" :disabled="!isEditable">扫描</el-button>
                    </template>
                  </el-input>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="scan-info-area">
                  <div class="scan-info-item">
                    <label>已盘物料数:</label>
                    <span class="highlight">{{ scannedItemCount }}</span>
                  </div>
                  <div class="scan-info-item">
                    <label>差异数:</label>
                    <span :class="diffItemCount > 0 ? 'diff' : ''">{{ diffItemCount }}</span>
                  </div>
                  <div class="scan-info-item">
                    <label>盘点进度:</label>
                    <el-progress :percentage="progress" :stroke-width="10"></el-progress>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
        
        <!-- 盘点结果统计 -->
        <el-card class="stats-card">
          <div class="stats-title">盘点结果统计</div>
          <div class="stats-content">
            <el-row :gutter="20">
              <!-- 数量统计 -->
              <el-col :span="12">
                <el-row :gutter="10">
                  <el-col :span="12">
                    <div class="stat-item">
                      <div class="stat-label">系统库存总量</div>
                      <div class="stat-value">{{ totalSysQty }}</div>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div class="stat-item">
                      <div class="stat-label">实际盘点总量</div>
                      <div class="stat-value">{{ totalCountQty }}</div>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div class="stat-item">
                      <div class="stat-label">总量差异</div>
                      <div class="stat-value diff">{{ totalDiffQty }}</div>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div class="stat-item">
                      <div class="stat-label">差异率</div>
                      <div class="stat-value">{{ diffRate }}%</div>
                    </div>
                  </el-col>
                </el-row>
              </el-col>
              
              <!-- 质量统计 -->
              <el-col :span="12">
                <el-row :gutter="10">
                  <el-col :span="12">
                    <div class="stat-item">
                      <div class="stat-label">盘点准确率</div>
                      <div class="stat-value accuracy">{{ accuracy }}%</div>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div class="stat-item">
                      <div class="stat-label">正常物料</div>
                      <div class="stat-value">{{ itemStatusStats.normalCount }}</div>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div class="stat-item">
                      <div class="stat-label">短缺物料</div>
                      <div class="stat-value diff">{{ itemStatusStats.shortageCount }}</div>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div class="stat-item">
                      <div class="stat-label">超量物料</div>
                      <div class="stat-value diff">{{ itemStatusStats.overstockCount }}</div>
                    </div>
                  </el-col>
                </el-row>
              </el-col>
            </el-row>
          </div>
        </el-card>

        <!-- 盘点明细 -->
        <el-card class="detail-card">
          <div class="detail-title">盘点明细</div>
          <el-table
            :data="countItems"
            style="width: 100%"
            height="400"
          >
            <el-table-column prop="locationCode" label="库位码" width="120"></el-table-column>
            <el-table-column prop="materialCode" label="物料编码" width="150"></el-table-column>
            <el-table-column prop="materialName" label="物料名称" width="200"></el-table-column>
            <el-table-column prop="batchNo" label="批次号" width="150"></el-table-column>
            <el-table-column prop="sysQty" label="系统库存" width="100" v-if="currentCountJob?.countType === '1'">
              <template #default="scope">
                <span>{{ scope.row.sysQty }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="countQty" label="盘点数量" width="100">
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.countQty"
                  :min="0"
                  :step="1"
                  placeholder="请输入盘点数量"
                  @change="handleCountQtyChange(scope.row)"
                  :disabled="!isEditable"
                ></el-input-number>
              </template>
            </el-table-column>
            <el-table-column prop="diffQty" label="差异数量" width="100">
              <template #default="scope">
                <span :class="scope.row.diffQty !== 0 ? 'diff' : ''">{{ scope.row.diffQty }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="80"></el-table-column>
            <el-table-column prop="scanTime" label="扫描时间" width="180"></el-table-column>
          </el-table>
        </el-card>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleExecuteDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSaveCount" :disabled="!isEditable">保存盘点结果</el-button>
          <el-button type="success" @click="handleCompleteCount" :disabled="!isEditable">完成盘点</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 创建盘点任务对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建盘点任务"
      width="500px"
    >
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="仓库" required>
          <el-select v-model="createForm.warehouseCode" placeholder="请选择仓库" style="width: 100%">
            <el-option
              v-for="warehouse in warehouses"
              :key="warehouse.id"
              :label="warehouse.name"
              :value="warehouse.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="盘点类型" required>
          <el-radio-group v-model="createForm.countType">
            <el-radio value="1">明盘（显示系统库存）</el-radio>
            <el-radio value="2">盲盘（不显示系统库存）</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreate" :loading="createLoading">创建</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { stockCountApi, warehouseApi } from '@/api/wms';

// 搜索表单
const searchForm = reactive({
  countNo: '',
  warehouseCode: '',
  status: ''
});

// 仓库数据
const warehouses = ref<{ id: string; name: string }[]>([]);

// 盘点类型枚举
type CountType = '1' | '2';
const COUNT_TYPE_MAP = {
  '1': '明盘',
  '2': '盲盘'
};
const COUNT_TYPE_COLOR_MAP = {
  '1': 'success',
  '2': 'warning'
};

// 盘点状态枚举
type CountStatus = '0' | '1' | '2' | '3';
const COUNT_STATUS_MAP = {
  '0': '待执行',
  '1': '执行中',
  '2': '已完成',
  '3': '已取消'
};
const COUNT_STATUS_COLOR_MAP = {
  '0': 'info',
  '1': 'success',
  '2': 'warning',
  '3': 'danger'
};

// 盘点任务数据
interface CountItem {
  id: number;
  countId: number;
  locationCode: string;
  materialCode: string;
  materialName: string;
  batchNo: string;
  sysQty: number;
  countQty: number;
  diffQty: number;
  unit: string;
  scanTime: string;
}

interface CountJob {
  id: number;
  countNo: string;
  warehouseId: string;
  warehouseName: string;
  countType: CountType;
  status: CountStatus;
  totalItemCount: number;
  finishedItemCount: number;
  diffCount: number;
  createTime: string;
  createUser: string;
  items?: CountItem[];
}

const countJobs = ref<CountJob[]>([]);

// 分页数据
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const loading = ref(false);

// 执行盘点对话框
const executeDialogVisible = ref(false);
const currentCountJob = ref<CountJob | null>(null);
const executeDialogTitle = computed(() => {
  return currentCountJob.value ? `盘点执行 - ${currentCountJob.value.countNo}` : '盘点执行';
});

// 扫码相关
const scanCode = ref('');
const countItems = ref<CountItem[]>([]);
const isEditable = computed(() => currentCountJob.value?.status === '1');
const scannedItemCount = computed(() => countItems.value.filter(item => item.countQty > 0).length);
const diffItemCount = computed(() => countItems.value.filter(item => item.diffQty !== 0).length);
const progress = computed(() => {
  if (!currentCountJob.value || currentCountJob.value.totalItemCount === 0) return 0;
  return Math.round((scannedItemCount.value / currentCountJob.value.totalItemCount) * 100);
});

// 盘点结果统计
const totalSysQty = computed(() => {
  return countItems.value.reduce((sum, item) => sum + (item.sysQty || 0), 0);
});
const totalCountQty = computed(() => {
  return countItems.value.reduce((sum, item) => sum + (item.countQty || 0), 0);
});
const totalDiffQty = computed(() => {
  return countItems.value.reduce((sum, item) => sum + (item.diffQty || 0), 0);
});
const accuracy = computed(() => {
  if (scannedItemCount.value === 0) return 100;
  return Math.round(((scannedItemCount.value - diffItemCount.value) / scannedItemCount.value) * 100);
});
const diffRate = computed(() => {
  if (totalSysQty.value === 0) return 0;
  return Math.abs(Math.round((totalDiffQty.value / totalSysQty.value) * 100));
});

// 盘点项状态统计
const itemStatusStats = computed(() => {
  const normalCount = countItems.value.filter(item => item.diffQty === 0).length;
  const shortageCount = countItems.value.filter(item => item.diffQty < 0).length;
  const overstockCount = countItems.value.filter(item => item.diffQty > 0).length;
  
  return {
    normalCount,
    shortageCount,
    overstockCount
  };
});

// 处理搜索
const fetchCountJobs = async () => {
  loading.value = true;
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    };
    if (searchForm.countNo) params.countNo = searchForm.countNo;
    if (searchForm.warehouseCode) params.warehouseCode = searchForm.warehouseCode;
    if (searchForm.status) params.status = searchForm.status;
    const res = await stockCountApi.getList(params);
    const pageData = res.data || {};
    countJobs.value = (pageData.list || []) as any[];
    total.value = Number(pageData.total ?? 0);
  } catch (e: any) {
    ElMessage.error(e?.message || '获取盘点任务失败');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchCountJobs();
};

// 创建盘点任务
const createDialogVisible = ref(false);
const createLoading = ref(false);
const createForm = reactive({
  warehouseCode: '',
  countType: '1'
});

// 打开创建盘点任务对话框
const handleCreate = () => {
  createForm.warehouseCode = '';
  createForm.countType = '1';
  createDialogVisible.value = true;
};

// 提交创建盘点任务
const submitCreate = async () => {
  if (!createForm.warehouseCode) {
    ElMessage.warning('请选择仓库');
    return;
  }
  createLoading.value = true;
  try {
    await stockCountApi.create({
      warehouseCode: createForm.warehouseCode,
      countType: createForm.countType,
      createUser: 'admin'
    });
    ElMessage.success('盘点任务创建成功');
    createDialogVisible.value = false;
    currentPage.value = 1;
    fetchCountJobs();
  } catch (e: any) {
    ElMessage.error(e?.message || '创建盘点任务失败');
  } finally {
    createLoading.value = false;
  }
};

// 处理开始盘点
const handleStart = async (row: CountJob) => {
  const res = await stockCountApi.start(row.id);
  const data = res.data || {};
  currentCountJob.value = data as any;
  countItems.value = ((data.items || []) as any[]).map((v: any) => ({
    ...v,
    sysQty: Number(v.sysQty ?? 0),
    countQty: Number(v.countQty ?? 0),
    diffQty: Number(v.diffQty ?? 0)
  }));
  executeDialogVisible.value = true;
};

// 处理继续盘点
const handleContinue = async (row: CountJob) => {
  const res = await stockCountApi.getDetail(row.id);
  const data = res.data || {};
  currentCountJob.value = data as any;
  countItems.value = ((data.items || []) as any[]).map((v: any) => ({
    ...v,
    sysQty: Number(v.sysQty ?? 0),
    countQty: Number(v.countQty ?? 0),
    diffQty: Number(v.diffQty ?? 0)
  }));
  executeDialogVisible.value = true;
};

// 处理完成盘点
const handleFinish = async (row: CountJob) => {
  await stockCountApi.complete(row.id);
  ElMessage.success('盘点已完成');
  fetchCountJobs();
};

// 处理查看盘点
const handleView = async (row: CountJob) => {
  const res = await stockCountApi.getDetail(row.id);
  const data = res.data || {};
  currentCountJob.value = data as any;
  countItems.value = ((data.items || []) as any[]).map((v: any) => ({
    ...v,
    sysQty: Number(v.sysQty ?? 0),
    countQty: Number(v.countQty ?? 0),
    diffQty: Number(v.diffQty ?? 0)
  }));
  executeDialogVisible.value = true;
};

// 处理取消盘点
const handleCancel = async (row: CountJob) => {
  await stockCountApi.cancel(row.id);
  ElMessage.success('盘点已取消');
  fetchCountJobs();
};

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchCountJobs();
};

// 处理当前页变化
const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  fetchCountJobs();
};

// 处理执行对话框关闭
const handleExecuteDialogClose = () => {
  executeDialogVisible.value = false;
  scanCode.value = '';
  countItems.value = [];
  currentCountJob.value = null;
};

// 处理扫描
const handleScan = () => {
  if (!scanCode.value) return;
  if (!currentCountJob.value) return;
  stockCountApi
    .scan(currentCountJob.value.id, { barcode: scanCode.value, quantity: 1 })
    .then((res: any) => {
      const data = res.data || {};
      const job = data.job || null;
      const item = data.item || null;
      if (job) currentCountJob.value = job;
      if (item) {
        const idx = countItems.value.findIndex(v => v.id === item.id);
        if (idx >= 0) {
          countItems.value[idx] = {
            ...(countItems.value[idx] as any),
            ...item,
            sysQty: Number(item.sysQty ?? 0),
            countQty: Number(item.countQty ?? 0),
            diffQty: Number(item.diffQty ?? 0)
          };
        } else {
          countItems.value.unshift({
            ...(item as any),
            sysQty: Number(item.sysQty ?? 0),
            countQty: Number(item.countQty ?? 0),
            diffQty: Number(item.diffQty ?? 0)
          });
        }
      }
      ElMessage.success('扫描成功');
    })
    .catch((e: any) => {
      ElMessage.warning(e?.message || '扫描失败');
    })
    .finally(() => {
      scanCode.value = '';
    });
};

// 处理盘点数量变化
const handleCountQtyChange = (item: CountItem) => {
  if (!currentCountJob.value) return;
  stockCountApi
    .updateItem(currentCountJob.value.id, item.id, { countQty: item.countQty })
    .then((res: any) => {
      const data = res.data || {};
      const job = data.job || null;
      const updated = data.item || null;
      if (job) currentCountJob.value = job;
      if (updated) {
        const idx = countItems.value.findIndex(v => v.id === updated.id);
        if (idx >= 0) {
          countItems.value[idx] = {
            ...(countItems.value[idx] as any),
            ...updated,
            sysQty: Number(updated.sysQty ?? 0),
            countQty: Number(updated.countQty ?? 0),
            diffQty: Number(updated.diffQty ?? 0)
          };
        }
      }
    })
    .catch((e: any) => {
      ElMessage.warning(e?.message || '更新失败');
    });
};

// 处理保存盘点结果
const handleSaveCount = () => {
  if (!currentCountJob.value) return;
  stockCountApi
    .save(currentCountJob.value.id)
    .then((res: any) => {
      const data = res.data || null;
      if (data) {
        currentCountJob.value = data;
        const idx = countJobs.value.findIndex(v => v.id === data.id);
        if (idx >= 0) {
          countJobs.value[idx] = { ...(countJobs.value[idx] as any), ...data };
        }
      }
      ElMessage.success('盘点结果已保存');
      fetchCountJobs();
    })
    .catch((e: any) => {
      ElMessage.error(e?.message || '保存失败');
    });
};

// 处理完成盘点
const handleCompleteCount = () => {
  if (!currentCountJob.value) return;
  stockCountApi
    .complete(currentCountJob.value.id)
    .then(() => {
      ElMessage.success('盘点已完成');
      fetchCountJobs();
      handleExecuteDialogClose();
    })
    .catch((e: any) => {
      ElMessage.error(e?.message || '完成失败');
    });
};

// 组件挂载时执行
onMounted(() => {
  warehouseApi
    .getList({ page: 0, size: 1000 })
    .then((res: any) => {
      const pageData = res.data || {};
      const list = (pageData.content || pageData.list || pageData.records || []) as any[];
      warehouses.value = list
        .map((w: any) => ({ id: String(w?.warehouseCode ?? ''), name: String(w?.warehouseName ?? '') }))
        .filter(v => v.id);
    })
    .catch(() => {});
  fetchCountJobs();
});
</script>

<style scoped>
.count-job-container {
  padding: 20px;
}

.operation-card {
  margin-bottom: 20px;
}

.list-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.execute-dialog-content {
  padding: 20px 0;
}

.count-info {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.info-item label {
  width: 80px;
  font-weight: bold;
}

.scan-card {
  margin-bottom: 20px;
}

.scan-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
}

.scan-content {
  display: flex;
  gap: 20px;
}

.scan-input-area {
  flex: 1;
}

.scan-info-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.scan-info-item {
  display: flex;
  align-items: center;
}

.scan-info-item label {
  width: 100px;
  font-weight: bold;
}

.highlight {
  color: #409eff;
  font-weight: bold;
}

.diff {
  color: #f56c6c;
  font-weight: bold;
}

.detail-card {
  margin-bottom: 20px;
}

.detail-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
}

/* 统计卡片样式 */
.stats-card {
  margin-bottom: 20px;
}

.stats-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
}

.stats-content {
  padding: 10px 0;
}

.stat-item {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  text-align: center;
}

.stat-label {
  font-size: 12px;
  color: #606266;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.stat-value.diff {
  color: #f56c6c;
}

.stat-value.accuracy {
  color: #67c23a;
}

.stats-row {
  margin-bottom: 15px;
}
</style>
