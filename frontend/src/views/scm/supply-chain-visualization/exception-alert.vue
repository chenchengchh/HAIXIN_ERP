<template>
  <div class="exception-alert-component">
    <h3>异常预警</h3>
    <p>实时监控供应链异常情况，包括库存异常、交付延迟、质量问题等</p>
    
    <!-- 异常统计卡片 -->
    <div class="alert-stats">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon danger">
            <el-icon><WarningFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">严重异常</div>
            <div class="stat-value">12</div>
          </div>
        </div>
      </el-card>
      
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon warning">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">警告</div>
            <div class="stat-value">35</div>
          </div>
        </div>
      </el-card>
      
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon info">
            <el-icon><InfoFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">提示</div>
            <div class="stat-value">28</div>
          </div>
        </div>
      </el-card>
      
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon success">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">已处理</div>
            <div class="stat-value">156</div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 异常列表卡片 -->
    <el-card shadow="hover" class="alert-list-card">
      <template #header>
        <div class="card-header">
          <span>异常列表</span>
          <el-button type="primary" size="small" @click="refreshAlerts">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>
      <div class="card-content">
        <el-table :data="exceptionAlerts" style="width: 100%">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="alertId" label="预警编号" width="150" />
          <el-table-column prop="alertType" label="预警类型" width="120">
            <template #default="scope">
              <el-tag :type="getAlertTypeTag(scope.row.alertType)">
                {{ scope.row.alertType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="source" label="来源" width="120" />
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column prop="alertLevel" label="预警级别" width="120">
            <template #default="scope">
              <el-tag :type="getAlertLevelTag(scope.row.alertLevel)">
                {{ scope.row.alertLevel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alertTime" label="预警时间" width="180" />
          <el-table-column prop="status" label="处理状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusTag(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" @click="handleAlert(scope.row)">处理</el-button>
              <el-button size="small" @click="dismissAlert(scope.row)">忽略</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="exceptionAlerts.length"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { WarningFilled, Warning, InfoFilled, Check, Refresh } from '@element-plus/icons-vue';
import { forecastApi } from '@/api/scm';

// 异常预警数据
const exceptionAlerts = ref<any[]>([]);

const mapAlert = (a: any) => {
  if (a.type === 'LOW_STOCK') {
    return {
      alertId: `ALERT-${a.refId}`,
      alertType: '库存短缺',
      source: '库存系统',
      description: `${a.materialName || a.materialCode} 库存低于再订货点`,
      alertLevel: a.severity === 'HIGH' ? '严重' : '警告',
      alertTime: a.time ? String(a.time).replace('T', ' ').slice(0, 19) : '',
      status: '待处理'
    };
  }
  if (a.type === 'MRP_RELEASE_FAILED') {
    return {
      alertId: `ALERT-${a.refId}`,
      alertType: 'MRP发布失败',
      source: 'MRP',
      description: a.message || 'MRP发布失败',
      alertLevel: '严重',
      alertTime: a.time ? String(a.time).replace('T', ' ').slice(0, 19) : '',
      status: '待处理'
    };
  }
  if (a.type === 'INTEGRATION_FAILED') {
    return {
      alertId: `ALERT-${a.refId}`,
      alertType: '集成失败',
      source: a.source || '集成',
      description: a.message || '集成任务失败',
      alertLevel: '严重',
      alertTime: a.time ? String(a.time).replace('T', ' ').slice(0, 19) : '',
      status: '待处理'
    };
  }
  return {
    alertId: `ALERT-${a.refId || ''}`,
    alertType: a.type || '异常',
    source: a.source || '',
    description: a.message || '',
    alertLevel: a.severity === 'HIGH' ? '严重' : '提示',
    alertTime: a.time ? String(a.time).replace('T', ' ').slice(0, 19) : '',
    status: '待处理'
  };
};

const loadAlerts = async () => {
  try {
    const res: any = await forecastApi.getControlTowerAlerts();
    const list = res?.data?.list || res?.data?.records || [];
    exceptionAlerts.value = (Array.isArray(list) ? list : []).map(mapAlert);
  } catch (e) {
    exceptionAlerts.value = [];
  }
};

onMounted(() => {
  loadAlerts();
});

// 分页数据
const currentPage = ref(1);
const pageSize = ref(10);

// 获取预警类型标签
const getAlertTypeTag = (type: string): string => {
  switch (type) {
    case '库存短缺':
    case '库存积压':
      return 'warning';
    case '交付延迟':
    case '运输异常':
      return 'info';
    case '质量问题':
      return 'danger';
    default:
      return 'info';
  }
};

// 获取预警级别标签
const getAlertLevelTag = (level: string): string => {
  switch (level) {
    case '严重':
      return 'danger';
    case '警告':
      return 'warning';
    case '提示':
      return 'info';
    default:
      return 'info';
  }
};

// 获取处理状态标签
const getStatusTag = (status: string): string => {
  switch (status) {
    case '待处理':
      return 'warning';
    case '处理中':
      return 'info';
    case '已处理':
      return 'success';
    case '已忽略':
      return 'danger';
    default:
      return 'info';
  }
};

// 处理预警
const handleAlert = (row: any) => {
  ElMessage.success(`处理预警：${row.alertId}`);
};

// 忽略预警
const dismissAlert = (row: any) => {
  ElMessage.info(`忽略预警：${row.alertId}`);
};

// 刷新预警列表
const refreshAlerts = () => {
  loadAlerts().finally(() => {
    ElMessage.success('预警列表已刷新');
  });
};

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
};

const handleCurrentChange = (current: number) => {
  currentPage.value = current;
};
</script>

<style scoped>
.exception-alert-component {
  padding: 20px;
}

/* 异常统计卡片 */
.alert-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  height: 100%;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-icon.danger {
  background-color: #f56c6c;
}

.stat-icon.warning {
  background-color: #e6a23c;
}

.stat-icon.info {
  background-color: #909399;
}

.stat-icon.success {
  background-color: #67c23a;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

/* 异常列表卡片 */
.alert-list-card {
  margin-top: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
