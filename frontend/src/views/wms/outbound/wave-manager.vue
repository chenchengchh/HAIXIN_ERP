<template>
  <WmsPageLayout title="波次管理" class="wave-manager-page">
    <template #actions>
      <el-button type="primary" @click="handleCreate">创建波次</el-button>
      <el-button @click="generateWaveAutomatically">自动生成波次</el-button>
      <el-button @click="refreshList">刷新列表</el-button>
    </template>

    <template #filters>
      <el-form :model="searchForm" label-width="80px" inline>
        <el-form-item label="波次单号">
          <el-input v-model="searchForm.waveNo" placeholder="输入波次单号" clearable></el-input>
        </el-form-item>
        <el-form-item label="波次状态">
          <el-select v-model="searchForm.status" placeholder="选择波次状态" clearable>
            <el-option label="已创建" value="CREATED"></el-option>
            <el-option label="已分配" value="ASSIGNED"></el-option>
            <el-option label="已释放" value="RELEASED"></el-option>
            <el-option label="拣货中" value="PICKING"></el-option>
            <el-option label="已完成" value="COMPLETED"></el-option>
            <el-option label="已取消" value="CANCELLED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="订单类型">
          <el-select v-model="searchForm.orderType" placeholder="选择订单类型" clearable>
            <el-option label="销售订单" value="sales"></el-option>
            <el-option label="领料订单" value="material"></el-option>
            <el-option label="调拨订单" value="transfer"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.createTimeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            clearable
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchWaves">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </template>
    
    <!-- 列表区域 -->
    <div class="list-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <h3>波次列表</h3>
            <span class="list-count">共 {{ waves.length }} 条记录</span>
          </div>
        </template>
        
        <el-table :data="waves" stripe style="width: 100%" v-loading="loading">
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="waveNo" label="波次单号" width="180" sortable></el-table-column>
          <el-table-column prop="orderCount" label="订单数量" width="100" sortable></el-table-column>
          <el-table-column prop="totalQuantity" label="总数量" width="100" sortable></el-table-column>
          <el-table-column prop="status" label="状态" width="120" sortable>
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="160" sortable></el-table-column>
          <el-table-column prop="createdBy" label="创建人" width="120"></el-table-column>
          <el-table-column prop="assignedTo" label="分配给" width="120"></el-table-column>
          <el-table-column prop="completedAt" label="完成时间" width="160" sortable>
            <template #default="scope">
              {{ scope.row.completedAt || '未完成' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewWaveDetail(scope.row)">
                详情
              </el-button>
              <el-button size="small" @click="assignWave(scope.row)" :disabled="scope.row.status !== 'CREATED' && scope.row.status !== 'ASSIGNED'">
                分配
              </el-button>
              <el-button size="small" @click="handleRelease(scope.row)" :disabled="scope.row.status !== 'CREATED' && scope.row.status !== 'ASSIGNED'">
                释放
              </el-button>
              <el-button size="small" @click="handleComplete(scope.row)" :disabled="scope.row.status !== 'RELEASED' && scope.row.status !== 'PICKING'">
                完成
              </el-button>
              <el-button size="small" type="danger" @click="cancelWave(scope.row)" :disabled="scope.row.status === 'COMPLETED' || scope.row.status === 'CANCELLED'">
                取消
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination" v-if="total > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          ></el-pagination>
        </div>
      </el-card>
    </div>
    
    <!-- 波次详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="currentWave.waveNo ? '波次详情' : '创建波次'"
      width="1000px"
      :before-close="handleDialogClose"
    >
      <div v-if="detailDialogVisible" class="detail-dialog-content">
        <!-- 基本信息 -->
        <el-card class="detail-card">
          <template #header>
            <h4>基本信息</h4>
          </template>
          <el-form :model="currentWave" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="波次单号">
                  <el-input v-model="currentWave.waveNo" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="波次状态">
                  <el-tag :type="getStatusTagType(currentWave.status)">
                    {{ getStatusText(currentWave.status) }}
                  </el-tag>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="订单数量">
                  <el-input v-model="currentWave.orderCount" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="总数量">
                  <el-input v-model="currentWave.totalQuantity" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="分配给">
                  <el-input v-model="currentWave.assignedTo" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="创建人">
                  <el-input v-model="currentWave.createdBy" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="创建时间">
                  <el-input v-model="currentWave.createdAt" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="完成时间">
                  <el-input v-model="currentWave.completedAt" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
        
        <!-- 订单明细 -->
        <el-card class="detail-card">
          <template #header>
            <h4>订单明细</h4>
          </template>
          
          <el-table :data="waveOrders" border style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="180"></el-table-column>
            <el-table-column prop="orderType" label="订单类型" width="120">
              <template #default="scope">
                <el-tag>{{ scope.row.orderType === 'sales' ? '销售订单' : scope.row.orderType === 'material' ? '领料订单' : '调拨订单' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="customerName" label="客户名称" width="180"></el-table-column>
            <el-table-column prop="totalQuantity" label="订单数量" width="100"></el-table-column>
            <el-table-column prop="status" label="订单状态" width="120"></el-table-column>
          </el-table>
        </el-card>
        
        <!-- 波次任务 -->
        <el-card class="detail-card">
          <template #header>
            <h4>波次任务</h4>
          </template>
          
          <el-table :data="waveTasks" border style="width: 100%">
            <el-table-column prop="taskNo" label="任务号" width="180"></el-table-column>
            <el-table-column prop="materialCode" label="物料代码" width="120"></el-table-column>
            <el-table-column prop="materialName" label="物料名称" width="180"></el-table-column>
            <el-table-column prop="specification" label="规格" width="150"></el-table-column>
            <el-table-column prop="unit" label="单位" width="80"></el-table-column>
            <el-table-column prop="quantity" label="数量" width="100"></el-table-column>
            <el-table-column prop="locationCode" label="库位" width="120"></el-table-column>
            <el-table-column prop="status" label="任务状态" width="120">
              <template #default="scope">
                <el-tag :type="getTaskStatusTagType(scope.row.status)">
                  {{ getTaskStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 波次分配对话框 -->
    <el-dialog
      v-model="assignWaveDialogVisible"
      title="分配波次"
      width="600px"
    >
      <div v-if="selectedWave">
        <el-form :model="assignInfo" label-width="120px">
          <el-form-item label="波次单号">
            <el-input v-model="selectedWave.waveNo" disabled></el-input>
          </el-form-item>
          <el-form-item label="订单数量">
            <el-input v-model="selectedWave.orderCount" disabled></el-input>
          </el-form-item>
          <el-form-item label="分配给" required>
            <el-select v-model="assignInfo.assignedTo" placeholder="选择操作员" style="width: 100%">
              <el-option
                v-for="operator in operators"
                :key="operator.id"
                :label="operator.name"
                :value="operator.name"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="备注">
            <el-input
              v-model="assignInfo.remark"
              type="textarea"
              :rows="3"
              placeholder="输入备注信息"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="assignWaveDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAssignWave">分配</el-button>
        </span>
      </template>
    </el-dialog>
    <!-- 创建波次对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建波次" width="800px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="选择订单">
          <el-table :data="availableOrders" border style="width: 100%" @selection-change="(val: any[]) => createForm.selectedOrderIds = val.map(v => v.id)">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="outboundNo" label="出库单号" width="150" />
            <el-table-column prop="customerName" label="客户名称" min-width="150" />
            <el-table-column prop="orderType" label="类型" width="100" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreateWave">确定</el-button>
      </template>
    </el-dialog>
  </WmsPageLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { waveApi, outboundOrderApi, pickingTaskApi } from '../../../api/wms';
import WmsPageLayout from '../components/WmsPageLayout.vue';

// 页面状态
const loading = ref(false);
const waves = ref<any[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 搜索表单
const searchForm = reactive({
  waveNo: '',
  status: '',
  orderType: '',
  createTimeRange: [] as string[]
});

// 波次详情对话框
const detailDialogVisible = ref(false);
const currentWave = ref<any>({});
const waveOrders = ref<any[]>([]);
const waveTasks = ref<any[]>([]);

const assignWaveDialogVisible = ref(false);
const selectedWave = ref<any>(null);
const assignInfo = reactive({
  assignedTo: '',
  remark: ''
});
const operators = ref<Array<{ id: string; name: string }>>([]);

// 加载操作员列表（来自拣货任务模块的操作员接口）
const loadOperators = async () => {
  try {
    const res = await pickingTaskApi.operators();
    const list = (res?.data || []) as any[];
    operators.value = list
      .map((o: any) => ({ id: String(o?.id ?? ''), name: String(o?.name ?? '') }))
      .filter(o => o.name);
  } catch (error) {
    operators.value = [];
  }
};

// 创建波次对话框
const createDialogVisible = ref(false);
const createForm = reactive({
  remark: '',
  selectedOrderIds: [] as number[]
});
const availableOrders = ref<any[]>([]); // 可加入波次的订单

// 状态映射
const statusMap: Record<string, string> = {
  'CREATED': '已创建',
  'ASSIGNED': '已分配',
  'RELEASED': '已释放',
  'PICKING': '拣货中',
  'COMPLETED': '已完成',
  'CANCELLED': '已取消'
};

const statusTagTypeMap: Record<string, string> = {
  'CREATED': 'info',
  'ASSIGNED': 'primary',
  'RELEASED': 'primary',
  'PICKING': 'warning',
  'COMPLETED': 'success',
  'CANCELLED': 'danger'
};

// 方法
const getStatusText = (status: string) => statusMap[status] || status;
const getStatusTagType = (status: string) => statusTagTypeMap[status] || 'info';

// 拣货任务项状态与后端实体一致（小写：pending/assigned/working/done）
const taskStatusMap: Record<string, string> = {
  'pending': '待执行',
  'assigned': '已分配',
  'working': '拣货中',
  'done': '已完成',
  'cancelled': '已取消'
};

const taskStatusTagTypeMap: Record<string, string> = {
  'pending': 'info',
  'assigned': 'primary',
  'working': 'warning',
  'done': 'success',
  'cancelled': 'danger'
};

const getTaskStatusText = (status: string) => taskStatusMap[status] || status;
const getTaskStatusTagType = (status: string) => taskStatusTagTypeMap[status] || 'info';

const fetchWaves = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      waveNo: searchForm.waveNo,
      status: searchForm.status,
      orderType: searchForm.orderType,
      startTime: searchForm.createTimeRange?.[0] ? searchForm.createTimeRange[0] + ' 00:00:00' : undefined,
      endTime: searchForm.createTimeRange?.[1] ? searchForm.createTimeRange[1] + ' 23:59:59' : undefined
    };
    const res = await waveApi.getList(params);
    const pageData = res.data || {};
    const list = (pageData.list || pageData.content || []) as any[];
    waves.value = list.map((item: any) => ({
      ...item,
      createdAt: item.createTime,
      updatedAt: item.updateTime
    }));
    total.value = Number(pageData.total ?? pageData.totalElements ?? 0);
  } catch (error) {
    console.error('获取波次列表失败:', error);
    ElMessage.error('获取波次列表失败');
  } finally {
    loading.value = false;
  }
};

const searchWaves = () => {
  currentPage.value = 1;
  fetchWaves();
};

const resetSearch = () => {
  searchForm.waveNo = '';
  searchForm.status = '';
  searchForm.orderType = '';
  searchForm.createTimeRange = [];
  searchWaves();
};

const refreshList = () => {
  fetchWaves();
};

const handleCreate = async () => {
  createForm.remark = '';
  createForm.selectedOrderIds = [];
  // 获取待分配波次的订单 (状态为 APPROVED 的出库单)
  try {
    const res = await outboundOrderApi.getList({ status: 'APPROVED', page: 1, size: 100 });
    const pageData = res.data || {};
    availableOrders.value = (pageData.list || pageData.content || []) as any[];
    createDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取待分配订单失败');
  }
};

const submitCreateWave = async () => {
  if (createForm.selectedOrderIds.length === 0) {
    ElMessage.warning('请至少选择一个订单');
    return;
  }
  try {
    await waveApi.create({
      remark: createForm.remark,
      outboundOrderIds: createForm.selectedOrderIds
    });
    ElMessage.success('波次创建成功');
    createDialogVisible.value = false;
    fetchWaves();
  } catch (error) {
    ElMessage.error('创建失败');
  }
};

const generateWaveAutomatically = async () => {
  try {
    await waveApi.autoCreate()
    ElMessage.success('已触发自动生成波次')
    fetchWaves()
  } catch (error) {
    ElMessage.error('自动生成波次失败')
  }
}

const assignWave = (row: any) => {
  selectedWave.value = row
  assignInfo.assignedTo = ''
  assignInfo.remark = ''
  assignWaveDialogVisible.value = true
}

const submitAssignWave = async () => {
  if (!selectedWave.value) return
  if (!assignInfo.assignedTo) {
    ElMessage.warning('请选择操作员')
    return
  }
  try {
    await waveApi.allocate(selectedWave.value.id, { ...assignInfo })
    ElMessage.success('分配成功')
    assignWaveDialogVisible.value = false
    fetchWaves()
  } catch (error) {
    ElMessage.error('分配失败')
  }
}

const cancelWave = (row: any) => {
  ElMessageBox.confirm('确定取消该波次吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await waveApi.cancel(row.id)
      ElMessage.success('取消成功')
      fetchWaves()
    } catch (error) {
      ElMessage.error('取消失败')
    }
  })
}

const handleDialogClose = (done: () => void) => {
  done()
}

const viewWaveDetail = async (row: any) => {
  try {
    const res = await waveApi.getDetail(row.id);
    const data = res.data || {};
    currentWave.value = data;
    // 后端详情接口直接返回关联订单与拣货任务
    waveTasks.value = (data?.tasks || []) as any[];
    waveOrders.value = (data?.orders || []) as any[];
    detailDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取详情失败');
  }
};

const handleRelease = (row: any) => {
  ElMessageBox.confirm('确定释放该波次吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await waveApi.release(row.id);
      ElMessage.success('释放成功');
      fetchWaves();
    } catch (error) {
      ElMessage.error('释放失败');
    }
  });
};

const handleComplete = (row: any) => {
  ElMessageBox.confirm('确定完成该波次吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await waveApi.complete(row.id);
      ElMessage.success('操作成功');
      fetchWaves();
    } catch (error) {
      ElMessage.error('操作失败');
    }
  });
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchWaves();
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchWaves();
};

onMounted(() => {
  fetchWaves();
  loadOperators();
});
</script>

<style scoped>
.wave-manager-page {
  padding: 0;
}

.list-section {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.card-header h4 {
  margin: 0;
  font-size: 14px;
  color: #303133;
}

.list-count {
  font-size: 14px;
  color: #606266;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.detail-dialog-content {
  max-height: 600px;
  overflow-y: auto;
}

.detail-card {
  margin-bottom: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
