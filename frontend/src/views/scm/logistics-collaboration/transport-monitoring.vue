<template>
  <div class="transport-monitoring-component">
    <h3>实时运输监控</h3>
    <p>实时监控运输车辆的位置、状态和运输进度</p>
    
    <el-card shadow="hover" class="monitoring-card">
      <template #header>
        <div class="card-header">
          <span>运输任务列表</span>
        </div>
      </template>
      <div class="card-content">
        <el-table :data="transportTasks" style="width: 100%">
          <el-table-column prop="taskId" label="任务编号" width="150" />
          <el-table-column prop="vehicleNumber" label="车牌号" width="120" />
          <el-table-column prop="driverName" label="司机姓名" width="120" />
          <el-table-column prop="currentLocation" label="当前位置" min-width="200" />
          <el-table-column prop="destination" label="目的地" min-width="150" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="estimatedArrivalTime" label="预计到达时间" width="180" />
          <el-table-column prop="progress" label="运输进度" width="150">
            <template #default="scope">
              <el-progress :percentage="scope.row.progress" :stroke-width="10" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    
    <el-card shadow="hover" class="map-card">
      <template #header>
        <div class="card-header">
          <span>车辆位置地图</span>
        </div>
      </template>
      <div class="card-content">
        <div class="map-container">
          <!-- 这里可以集成地图组件，如百度地图或高德地图 -->
          <div class="map-placeholder">
            <el-icon class="map-icon"><Location /></el-icon>
            <p>地图组件将在此显示车辆实时位置</p>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { Location } from '@element-plus/icons-vue';
import { forecastApi } from '@/api/scm';

const transportTasks = ref<any[]>([]);

const mapStatus = (status: string) => {
  if (status === 'IN_TRANSIT') return '运输中';
  if (status === 'ARRIVED' || status === 'SIGNED') return '已完成';
  if (status === 'DELAYED') return '延迟';
  return '待发车';
};

const mapProgress = (status: string) => {
  if (status === 'IN_TRANSIT') return 60;
  if (status === 'ARRIVED' || status === 'SIGNED') return 100;
  if (status === 'DELAYED') return 40;
  return 0;
};

const loadShipments = async () => {
  try {
    const res: any = await forecastApi.getShipments({ page: 1, size: 50 });
    const records = res?.data?.records || res?.data?.list || [];
    transportTasks.value = (Array.isArray(records) ? records : []).map((s: any) => ({
      taskId: s.shipmentNo,
      vehicleNumber: s.transportMode || '-',
      driverName: '-',
      currentLocation: s.origin || '-',
      destination: s.destination || '-',
      status: mapStatus(s.status),
      estimatedArrivalTime: s.eta ? String(s.eta).replace('T', ' ').slice(0, 19) : '',
      progress: mapProgress(s.status)
    }));
  } catch (e) {
    transportTasks.value = [];
  }
};

onMounted(() => {
  loadShipments();
});

// 获取状态标签类型
const getStatusTagType = (status: string): string => {
  switch (status) {
    case '运输中':
      return 'primary';
    case '已完成':
      return 'success';
    case '待发车':
      return 'warning';
    case '已取消':
      return 'danger';
    default:
      return 'info';
  }
};
</script>

<style scoped>
.transport-monitoring-component {
  padding: 20px;
}

.monitoring-card,
.map-card {
  margin-top: 20px;
}

.map-card {
  height: 500px;
  display: flex;
  flex-direction: column;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.map-container {
  flex: 1;
  background-color: #f0f0f0;
  border-radius: 8px;
  position: relative;
  overflow: hidden;
}

.map-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.map-icon {
  font-size: 48px;
  margin-bottom: 16px;
}
</style>
