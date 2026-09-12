<template>
  <div class="pick-monitor-page">
    <div class="page-header">
      <h2>拣货监控</h2>
      <div class="header-actions">
        <el-button type="primary" @click="refreshData">刷新数据</el-button>
        <el-button @click="exportReport">导出报表</el-button>
      </div>
    </div>
    
    <!-- 统计卡片区域 -->
    <div class="stats-cards">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ totalOrders }}</div>
          <div class="stat-label">今日订单总数</div>
        </div>
        <div class="stat-icon order-icon"></div>
      </el-card>
      
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ completedOrders }}</div>
          <div class="stat-label">今日已完成订单</div>
        </div>
        <div class="stat-icon completed-icon"></div>
      </el-card>
      
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ pendingOrders }}</div>
          <div class="stat-label">今日待处理订单</div>
        </div>
        <div class="stat-icon pending-icon"></div>
      </el-card>
      
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ avgPickTime }}s</div>
          <div class="stat-label">平均拣货时间</div>
        </div>
        <div class="stat-icon time-icon"></div>
      </el-card>
    </div>
    
    <!-- 搜索和筛选区域 -->
    <div class="search-filter-section">
      <el-card shadow="hover">
        <el-form :model="searchForm" label-width="80px" inline>
          <el-form-item label="波次单号">
            <el-input v-model="searchForm.waveNo" placeholder="输入波次单号" clearable></el-input>
          </el-form-item>
          <el-form-item label="订单号">
            <el-input v-model="searchForm.orderNo" placeholder="输入订单号" clearable></el-input>
          </el-form-item>
          <el-form-item label="操作员">
            <el-select v-model="searchForm.operator" placeholder="选择操作员" clearable>
              <el-option
                v-for="operator in operators"
                :key="operator.id"
                :label="operator.name"
                :value="operator.name"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
              <el-option label="待拣货" value="pending"></el-option>
              <el-option label="拣货中" value="picking"></el-option>
              <el-option label="已完成" value="completed"></el-option>
              <el-option label="已取消" value="cancelled"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="searchForm.timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              clearable
              :default-time="['00:00:00', '23:59:59']"
            ></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchPickTasks">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    
    <!-- 监控内容区域 -->
    <div class="monitor-content">
      <!-- 左侧：拣货任务列表 -->
      <div class="task-list-section">
        <el-card shadow="hover" class="task-list-card">
          <template #header>
            <div class="card-header">
              <h3>拣货任务列表</h3>
              <span class="list-count">共 {{ pickTasks.length }} 条记录</span>
            </div>
          </template>
          
          <el-table :data="pickTasks" stripe style="width: 100%" v-loading="loading" @row-click="viewTaskDetail">
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="taskNo" label="任务号" width="180" sortable></el-table-column>
            <el-table-column prop="waveNo" label="波次号" width="150" sortable></el-table-column>
            <el-table-column prop="orderNo" label="订单号" width="150" sortable></el-table-column>
            <el-table-column prop="operator" label="操作员" width="120"></el-table-column>
            <el-table-column prop="totalItems" label="总项数" width="100" sortable></el-table-column>
            <el-table-column prop="pickedItems" label="已拣项数" width="100" sortable></el-table-column>
            <el-table-column prop="progress" label="进度" width="150">
              <template #default="scope">
                <el-progress :percentage="scope.row.progress" :stroke-width="10" :show-text="true"></el-progress>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120" sortable>
              <template #default="scope">
                <el-tag :type="getStatusTagType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160" sortable></el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="160" sortable>
              <template #default="scope">
                {{ scope.row.startTime || '未开始' }}
              </template>
            </el-table-column>
            <el-table-column prop="endTime" label="结束时间" width="160" sortable>
              <template #default="scope">
                {{ scope.row.endTime || '未结束' }}
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
      
      <!-- 右侧：实时监控和统计 -->
      <div class="real-time-section">
        <!-- 实时监控图表 -->
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <h3>实时拣货进度</h3>
          </template>
          <div class="chart-container">
            <!-- 这里可以嵌入ECharts图表 -->
            <div class="mock-chart pick-progress-chart">
              <div class="chart-title">今日拣货进度</div>
              <div class="chart-content">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: `${todayProgress}%` }"></div>
                </div>
                <div class="progress-text">{{ todayProgress }}% 已完成</div>
              </div>
            </div>
          </div>
        </el-card>
        
        <!-- 操作员绩效 -->
        <el-card shadow="hover" class="performance-card">
          <template #header>
            <h3>操作员绩效</h3>
          </template>
          <div class="performance-list">
            <div
              v-for="(performance, index) in operatorPerformance"
              :key="index"
              class="performance-item"
            >
              <div class="performance-rank">{{ index + 1 }}</div>
              <div class="performance-info">
                <div class="operator-name">{{ performance.operator }}</div>
                <div class="performance-stats">
                  <span class="stat-item">{{ performance.completed }} 单</span>
                  <span class="stat-item">{{ performance.avgTime }}s/单</span>
                </div>
              </div>
              <div class="performance-score">{{ performance.score }}</div>
            </div>
          </div>
        </el-card>
        
        <!-- 拣货瓶颈分析 -->
        <el-card shadow="hover" class="bottleneck-card">
          <template #header>
            <h3>拣货瓶颈分析</h3>
          </template>
          <div class="bottleneck-list">
            <div
              v-for="(bottleneck, index) in bottlenecks"
              :key="index"
              class="bottleneck-item"
              :class="{ 'high-risk': bottleneck.level === 'high' }"
            >
              <div class="bottleneck-level">{{ bottleneck.level === 'high' ? '⚠️' : '⚠' }}</div>
              <div class="bottleneck-info">
                <div class="bottleneck-location">{{ bottleneck.location }}</div>
                <div class="bottleneck-description">{{ bottleneck.description }}</div>
              </div>
              <div class="bottleneck-time">{{ bottleneck.avgTime }}s</div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
    
    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="taskDetailDialogVisible"
      :title="selectedTask ? `拣货任务详情 - ${selectedTask.taskNo}` : '任务详情'"
      width="1000px"
      :before-close="handleDialogClose"
    >
      <div v-if="selectedTask" class="task-detail-content">
        <!-- 任务基本信息 -->
        <el-card class="detail-card">
          <template #header>
            <h4>任务基本信息</h4>
          </template>
          <el-form :model="selectedTask" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="任务号">
                  <el-input v-model="selectedTask.taskNo" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="波次号">
                  <el-input v-model="selectedTask.waveNo" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="订单号">
                  <el-input :value="selectedTask?.orderNo || ''" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="操作员">
                  <el-input :value="selectedTask?.operatorName || ''" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="创建时间">
                  <el-input v-model="selectedTask.createdAt" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="开始时间">
                  <el-input v-model="selectedTask.startTime" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="结束时间">
                  <el-input v-model="selectedTask.endTime" :disabled="true"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态">
                  <el-tag :type="getStatusTagType(selectedTask.status)">
                    {{ getStatusText(selectedTask.status) }}
                  </el-tag>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
        
        <!-- 拣货明细 -->
        <el-card class="detail-card">
          <template #header>
            <h4>拣货明细</h4>
          </template>
          <el-table :data="selectedTask.items" border style="width: 100%">
            <el-table-column prop="materialCode" label="物料代码" width="120"></el-table-column>
            <el-table-column prop="materialName" label="物料名称" width="180"></el-table-column>
            <el-table-column prop="specification" label="规格" width="150"></el-table-column>
            <el-table-column prop="unit" label="单位" width="80"></el-table-column>
            <el-table-column prop="quantity" label="数量" width="100"></el-table-column>
            <el-table-column prop="locationCode" label="库位" width="120"></el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'done' ? 'success' : 'info'">
                  {{ scope.row.status === 'done' ? '已拣' : '待拣' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="pickTime" label="拣货时间" width="160"></el-table-column>
          </el-table>
        </el-card>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="taskDetailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onBeforeUnmount, onMounted } from 'vue';
import { pickingTaskV1Api } from '@/api/wms';
import { unwrapListResponse } from '@/api';

const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);

const totalOrders = ref(0);
const completedOrders = ref(0);
const pendingOrders = ref(0);
const avgPickTime = ref(0);
const todayProgress = ref(0);

const searchForm = reactive({
  waveNo: '',
  orderNo: '',
  operator: '',
  status: '',
  timeRange: [] as any[]
});

const operators = ref<{ id: string; name: string }[]>([]);
const pickTasks = ref<any[]>([]);
const operatorPerformance = ref<any[]>([]);
const bottlenecks = ref<any[]>([]);

const taskDetailDialogVisible = ref(false);
const selectedTask = ref<any | null>(null);

// 状态映射
const statusMap = {
  pending: '待拣货',
  assigned: '已分配',
  picking: '拣货中',
  working: '拣货中', // 增加working状态映射
  done: '已完成', // 增加done状态映射
  completed: '已完成',
  cancelled: '已取消'
};

const statusTagTypeMap = {
  pending: 'info',
  assigned: 'primary',
  picking: 'warning',
  working: 'warning', // 增加working状态映射
  done: 'success', // 增加done状态映射
  completed: 'success',
  cancelled: 'danger'
};

// 方法
const getStatusText = (status: string) => {
  return statusMap[status as keyof typeof statusMap] || status;
};

const getStatusTagType = (status: string) => {
  return statusTagTypeMap[status as keyof typeof statusTagTypeMap] || 'info';
};

const toDateTimeParam = (v: any): string | undefined => {
  if (!v) return undefined;
  if (v instanceof Date) {
    return v.toISOString().slice(0, 19).replace('T', ' ');
  }
  const s = String(v);
  if (!s) return undefined;
  return s;
};

const fetchOperators = async () => {
  // 数组型响应会被DataTransformer改写成{records,list,total,page,size}包装对象，需用unwrapListResponse解包
  const res = await pickingTaskV1Api.operators();
  operators.value = unwrapListResponse(res);
};

const fetchStats = async () => {
  const res = await pickingTaskV1Api.stats();
  const data = res.data || {};
  totalOrders.value = Number(data.totalOrders ?? 0);
  completedOrders.value = Number(data.completedOrders ?? 0);
  pendingOrders.value = Number(data.pendingOrders ?? 0);
  avgPickTime.value = Number(data.avgPickTime ?? 0);
  todayProgress.value = Number(data.todayProgress ?? 0);
};

const fetchPerformance = async () => {
  // 数组型响应会被DataTransformer改写成{records,list,total,page,size}包装对象，需用unwrapListResponse解包
  const res = await pickingTaskV1Api.performance();
  operatorPerformance.value = unwrapListResponse(res);
};

const fetchBottlenecks = async () => {
  // 数组型响应会被DataTransformer改写成{records,list,total,page,size}包装对象，需用unwrapListResponse解包
  const res = await pickingTaskV1Api.bottlenecks({ limit: 5 });
  bottlenecks.value = unwrapListResponse(res);
};

const fetchPickTasks = async () => {
  loading.value = true;
  try {
    const start = toDateTimeParam(searchForm.timeRange?.[0]);
    const end = toDateTimeParam(searchForm.timeRange?.[1]);
    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    };
    if (searchForm.waveNo) params.waveNo = searchForm.waveNo;
    if (searchForm.orderNo) params.orderNo = searchForm.orderNo;
    if (searchForm.operator) params.operatorName = searchForm.operator;
    if (searchForm.status) params.status = searchForm.status;
    if (start) params.startTime = start;
    if (end) params.endTime = end;
    const res = await pickingTaskV1Api.getList(params);
    const pageData = res.data || {};
    pickTasks.value = (pageData.list || []) as any[];
    total.value = Number(pageData.total ?? 0);
  } catch (error: any) {
    console.error('获取拣货任务列表失败:', error);
  } finally {
    loading.value = false;
  }
};

const searchPickTasks = () => {
  currentPage.value = 1;
  fetchPickTasks();
};

const resetSearch = () => {
  Object.assign(searchForm, {
    waveNo: '',
    orderNo: '',
    operator: '',
    status: '',
    timeRange: []
  });
  fetchPickTasks();
};

const refreshData = () => {
  fetchPickTasks();
  fetchStats();
  fetchPerformance();
  fetchBottlenecks();
  fetchOperators();
};

const exportReport = () => {
  if (!pickTasks.value.length) {
    return;
  }
  const headers = ['任务号', '波次号', '订单号', '操作员', '状态', '总项', '已拣项', '进度(%)', '创建时间', '开始时间', '结束时间'];
  const rows = pickTasks.value.map((t: any) => [
    t.taskNo ?? '',
    t.waveNo ?? '',
    t.orderNo ?? '',
    t.operatorName ?? '',
    t.status ?? '',
    t.totalItems ?? 0,
    t.pickedItems ?? 0,
    t.progress ?? 0,
    t.createTime ?? '',
    t.startTime ?? '',
    t.endTime ?? ''
  ]);
  const csv = [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);
  link.setAttribute('href', url);
  link.setAttribute('download', `拣货监控报表_${new Date().toISOString().slice(0, 10)}.csv`);
  link.style.visibility = 'hidden';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
};

const viewTaskDetail = (task: any) => {
  pickingTaskV1Api.getDetail(task.id).then((res: any) => {
    selectedTask.value = res.data || task;
    taskDetailDialogVisible.value = true;
  });
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchPickTasks();
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchPickTasks();
};

const handleDialogClose = () => {
  selectedTask.value = null;
};

// 生命周期
let timer: number | null = null;
onMounted(() => {
  refreshData();
  timer = window.setInterval(() => {
    fetchStats();
    fetchPerformance();
    fetchBottlenecks();
  }, 15000);
});

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
});
</script>

<style scoped>
.pick-monitor-page {
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background-color: #f5f7fa;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #e0e0e0;
  background-color: white;
  padding: 16px;
  border-radius: 8px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-top: 8px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.order-icon {
  background-color: #ecf5ff;
  color: #409eff;
}

.completed-icon {
  background-color: #f0f9eb;
  color: #67c23a;
}

.pending-icon {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.time-icon {
  background-color: #fef0f0;
  color: #f56c6c;
}

.search-filter-section {
  margin-bottom: 16px;
}

.monitor-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  flex: 1;
  overflow: hidden;
}

.task-list-section {
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.task-list-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
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

.list-count {
  font-size: 14px;
  color: #606266;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.real-time-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
}

.chart-card,
.performance-card,
.bottleneck-card {
  overflow: hidden;
}

.chart-container {
  padding: 20px 0;
}

.mock-chart {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
}

.chart-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 20px;
}

.chart-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.progress-bar {
  width: 100%;
  height: 20px;
  background-color: #e0e0e0;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 10px;
}

.progress-fill {
  height: 100%;
  background-color: #409eff;
  border-radius: 10px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 14px;
  color: #606266;
}

.performance-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.performance-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: #fafafa;
  border-radius: 8px;
}

.performance-rank {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  margin-right: 12px;
}

.performance-info {
  flex: 1;
}

.operator-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.performance-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #606266;
}

.stat-item {
  display: flex;
  align-items: center;
}

.performance-score {
  font-size: 16px;
  font-weight: bold;
  color: #67c23a;
}

.bottleneck-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.bottleneck-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: #fafafa;
  border-radius: 8px;
  border-left: 4px solid #e6a23c;
}

.bottleneck-item.high-risk {
  border-left-color: #f56c6c;
  background-color: #fef0f0;
}

.bottleneck-level {
  font-size: 20px;
  margin-right: 12px;
}

.bottleneck-info {
  flex: 1;
}

.bottleneck-location {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.bottleneck-description {
  font-size: 12px;
  color: #606266;
}

.bottleneck-time {
  font-size: 14px;
  color: #606266;
}

.task-detail-content {
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

/* 响应式设计 */
@media (max-width: 1200px) {
  .monitor-content {
    grid-template-columns: 1fr;
  }
  
  .stats-cards {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
}
</style>
