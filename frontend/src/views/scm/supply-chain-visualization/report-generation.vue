<template>
  <div class="report-generation-component">
    <h3>报表生成</h3>
    <p>自动生成供应链相关报表，包括库存报表、运输报表、供应商绩效报表等</p>
    
    <!-- 报表类型选择 -->
    <el-card shadow="hover" class="report-type-card">
      <template #header>
        <div class="card-header">
          <span>选择报表类型</span>
        </div>
      </template>
      <div class="card-content">
        <el-form :model="reportForm" label-position="left" label-width="120px" class="report-form">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="报表类型">
                <el-select v-model="reportForm.reportType" placeholder="选择报表类型" style="width: 100%">
                  <el-option label="库存报表" value="inventory" />
                  <el-option label="运输报表" value="transport" />
                  <el-option label="供应商绩效报表" value="supplier" />
                  <el-option label="订单报表" value="order" />
                  <el-option label="物流成本报表" value="cost" />
                  <el-option label="库存周转率报表" value="turnover" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="时间范围">
                <el-select v-model="reportForm.timeRange" placeholder="选择时间范围" style="width: 100%">
                  <el-option label="本周" value="week" />
                  <el-option label="本月" value="month" />
                  <el-option label="本季度" value="quarter" />
                  <el-option label="本年" value="year" />
                  <el-option label="自定义" value="custom" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="报表格式">
                <el-select v-model="reportForm.format" placeholder="选择报表格式" style="width: 100%">
                  <el-option label="Excel" value="excel" />
                  <el-option label="PDF" value="pdf" />
                  <el-option label="HTML" value="html" />
                  <el-option label="CSV" value="csv" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <!-- 自定义时间范围 -->
          <el-row :gutter="20" v-if="reportForm.timeRange === 'custom'">
            <el-col :span="12">
              <el-form-item label="开始日期">
                <el-date-picker
                  v-model="reportForm.customStartDate"
                  type="date"
                  placeholder="选择开始日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束日期">
                <el-date-picker
                  v-model="reportForm.customEndDate"
                  type="date"
                  placeholder="选择结束日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12" class="form-actions">
              <el-button type="primary" @click="generateReport" :loading="generating">
                <el-icon><Document /></el-icon> 生成报表
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-col>
          </el-row>
        </el-form>
      </div>
    </el-card>
    
    <!-- 报表历史记录 -->
    <el-card shadow="hover" class="report-history-card">
      <template #header>
        <div class="card-header">
          <span>报表历史记录</span>
        </div>
      </template>
      <div class="card-content">
        <el-table :data="reportHistory" style="width: 100%">
          <el-table-column prop="reportId" label="报表编号" width="150" />
          <el-table-column prop="reportType" label="报表类型" width="150" />
          <el-table-column prop="generateTime" label="生成时间" width="180" />
          <el-table-column prop="timeRange" label="时间范围" width="120" />
          <el-table-column prop="format" label="格式" width="100">
            <template #default="scope">
              <el-tag>{{ scope.row.format }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusTag(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="fileSize" label="文件大小" width="100" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" @click="downloadReport(scope.row)" v-if="scope.row.status === '已完成'">
                <el-icon><Download /></el-icon> 下载
              </el-button>
              <el-button size="small" @click="previewReport(scope.row)" v-if="scope.row.status === '已完成'">
                <el-icon><View /></el-icon> 预览
              </el-button>
              <el-button size="small" type="danger" @click="deleteReport(scope.row)">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
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
            :total="reportHistory.length"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { Document, Download, View, Delete } from '@element-plus/icons-vue';
import { forecastApi } from '@/api/scm';

// 报表生成表单数据
const reportForm = reactive({
  reportType: '',
  timeRange: 'month',
  format: 'excel',
  customStartDate: '',
  customEndDate: ''
});

// 生成状态
const generating = ref(false);

// 报表历史记录
const reportHistory = ref<any[]>([]);

// 分页数据
const currentPage = ref(1);
const pageSize = ref(10);

// 生成报表
const generateReport = async () => {
  if (!reportForm.reportType) {
    ElMessage.warning('请选择报表类型');
    return;
  }
  
  if (reportForm.timeRange === 'custom' && (!reportForm.customStartDate || !reportForm.customEndDate)) {
    ElMessage.warning('请选择自定义时间范围');
    return;
  }
  
  generating.value = true;
  try {
    const [kpis, alerts] = await Promise.all([
      forecastApi.getControlTowerKpis(),
      forecastApi.getControlTowerAlerts()
    ]);
    const payload = {
      reportType: reportForm.reportType,
      timeRange: reportForm.timeRange === 'custom' ? { start: reportForm.customStartDate, end: reportForm.customEndDate } : reportForm.timeRange,
      generatedAt: new Date().toISOString(),
      kpis,
      alerts
    };
    const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `scm-report-${new Date().toISOString().slice(0, 10)}.json`;
    a.click();
    URL.revokeObjectURL(url);

    const newReport = {
      reportId: `REPORT-${new Date().toISOString().slice(0, 10).replace(/-/g, '')}-${String(reportHistory.value.length + 1).padStart(3, '0')}`,
      reportType: reportForm.reportType,
      generateTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
      timeRange: reportForm.timeRange === 'custom' ? `${reportForm.customStartDate} 至 ${reportForm.customEndDate}` : reportForm.timeRange,
      format: 'JSON',
      status: '已完成',
      fileSize: '-'
    };
    reportHistory.value.unshift(newReport);
    ElMessage.success('报表已生成并下载（JSON）');
  } catch (e) {
    ElMessage.error('报表生成失败');
  } finally {
    generating.value = false;
  }
};

// 重置表单
const resetForm = () => {
  Object.assign(reportForm, {
    reportType: '',
    timeRange: 'month',
    format: 'excel',
    customStartDate: '',
    customEndDate: ''
  });
};

// 下载报表
const downloadReport = (row: any) => {
  ElMessage.success(`下载报表：${row.reportId}`);
};

// 预览报表
const previewReport = (row: any) => {
  ElMessage.info(`预览报表：${row.reportId}`);
};

// 删除报表
const deleteReport = (row: any) => {
  ElMessage.success(`删除报表：${row.reportId}`);
  const index = reportHistory.value.findIndex(item => item.reportId === row.reportId);
  if (index !== -1) {
    reportHistory.value.splice(index, 1);
  }
};

// 获取状态标签
const getStatusTag = (status: string): string => {
  switch (status) {
    case '已完成':
      return 'success';
    case '生成中':
      return 'warning';
    case '失败':
      return 'danger';
    default:
      return 'info';
  }
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
.report-generation-component {
  padding: 20px;
}

.report-type-card,
.report-history-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.report-form {
  width: 100%;
}

.form-actions {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  margin-top: 16px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
