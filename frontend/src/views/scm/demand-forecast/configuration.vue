<template>
  <div class="forecast-configuration-component">
    <h3>预测配置</h3>
    <p>配置预测模型参数、数据来源和预测频率等设置</p>
    
    <!-- 配置卡片 -->
    <el-card shadow="hover" class="configuration-card">
      <template #header>
        <div class="card-header">
          <span>预测模型配置</span>
        </div>
      </template>
      <div class="card-content">
        <el-form :model="modelConfig" label-position="left" label-width="120px" class="config-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="预测模型">
                <el-select v-model="modelConfig.modelType" placeholder="选择预测模型" style="width: 100%">
                  <el-option label="移动平均" value="MOVING_AVERAGE" />
                  <el-option label="指数平滑" value="EXP_SMOOTHING" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="预测周期">
                <el-select v-model="modelConfig.forecastPeriod" placeholder="选择预测周期" style="width: 100%">
                  <el-option label="日度" value="daily" />
                  <el-option label="周度" value="weekly" />
                  <el-option label="月度" value="monthly" />
                  <el-option label="季度" value="quarterly" />
                  <el-option label="年度" value="yearly" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="历史数据跨度">
                <el-select v-model="modelConfig.historyDataSpan" placeholder="选择历史数据跨度" style="width: 100%">
                  <el-option label="6个月" value="6month" />
                  <el-option label="12个月" value="12month" />
                  <el-option label="24个月" value="24month" />
                  <el-option label="36个月" value="36month" />
                  <el-option label="自定义" value="custom" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="预测时长">
                <el-input-number v-model="modelConfig.forecastLength" :min="1" :max="36" :step="1" placeholder="输入预测时长" style="width: 100%" />
                <div class="form-tip">单位：{{ modelConfig.forecastPeriod === 'monthly' ? '月' : modelConfig.forecastPeriod === 'weekly' ? '周' : modelConfig.forecastPeriod === 'daily' ? '天' : '季度' }}</div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="模型训练频率">
                <el-select v-model="modelConfig.trainingFrequency" placeholder="选择训练频率" style="width: 100%">
                  <el-option label="每日" value="daily" />
                  <el-option label="每周" value="weekly" />
                  <el-option label="每月" value="monthly" />
                  <el-option label="手动" value="manual" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="置信区间">
                <el-slider v-model="modelConfig.confidenceInterval" :min="80" :max="99" :step="1" />
                <div class="form-tip">当前设置：{{ modelConfig.confidenceInterval }}%</div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="特征变量">
                <el-select v-model="modelConfig.features" multiple placeholder="选择特征变量" style="width: 100%">
                  <el-option label="历史销量" value="history_sales" />
                  <el-option label="价格" value="price" />
                  <el-option label="促销" value="promotion" />
                  <el-option label="节假日" value="holiday" />
                  <el-option label="季节" value="season" />
                  <el-option label="温度" value="temperature" />
                  <el-option label="GDP" value="gdp" />
                  <el-option label="CPI" value="cpi" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="特殊事件处理">
                <el-checkbox-group v-model="modelConfig.specialEvents">
                  <el-checkbox label="考虑节假日" value="考虑节假日" />
                  <el-checkbox label="考虑促销活动" value="考虑促销活动" />
                  <el-checkbox label="考虑新品发布" value="考虑新品发布" />
                  <el-checkbox label="考虑季节性波动" value="考虑季节性波动" />
                  <el-checkbox label="考虑重大事件" value="考虑重大事件" />
                </el-checkbox-group>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="24" class="form-actions">
              <el-button type="primary" @click="saveModelConfig">保存配置</el-button>
              <el-button @click="resetModelConfig">重置</el-button>
              <el-button type="warning" @click="testModel">测试模型</el-button>
            </el-col>
          </el-row>
        </el-form>
      </div>
    </el-card>
    
    <!-- 数据来源配置 -->
    <el-card shadow="hover" class="configuration-card">
      <template #header>
        <div class="card-header">
          <span>数据来源配置</span>
        </div>
      </template>
      <div class="card-content">
        <el-table :data="dataSources" style="width: 100%">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="name" label="数据源名称" width="180" />
          <el-table-column prop="sourceType" label="数据源类型" width="120">
            <template #default="scope">
              <el-tag>{{ scope.row.sourceType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'OK' ? 'success' : scope.row.status === 'FAILED' ? 'danger' : 'info'">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastTestTime" label="最近测试" width="180">
            <template #default="scope">
              {{ scope.row.lastTestTime ? String(scope.row.lastTestTime).replace('T', ' ').slice(0, 19) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" @click="testConnection(scope.row)">测试连接</el-button>
              <el-button size="small" type="danger" @click="deleteDataSource(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="table-actions">
          <el-button type="primary" @click="showAddDataSourceDialog">
            <el-icon><Plus /></el-icon> 添加数据源
          </el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 添加数据源对话框 -->
    <el-dialog v-model="addDataSourceVisible" title="添加数据源" width="70%" center>
      <div class="add-datasource-form">
        <el-form :model="newDataSourceForm" label-position="top" label-width="120px" class="datasource-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="数据源名称" prop="name">
                <el-input v-model="newDataSourceForm.name" placeholder="请输入数据源名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="数据源类型" prop="sourceType">
                <el-select v-model="newDataSourceForm.sourceType" placeholder="选择数据源类型">
                  <el-option label="CRM" value="CRM" />
                  <el-option label="WMS" value="WMS" />
                  <el-option label="ERP" value="ERP" />
                  <el-option label="BOM" value="BOM" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDataSourceVisible = false">取消</el-button>
          <el-button type="primary" @click="saveDataSource">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { forecastApi } from '@/api/scm';

// 模型配置数据
const modelConfig = reactive({
  modelType: 'MOVING_AVERAGE',
  forecastPeriod: 'monthly',
  historyDataSpan: '12month',
  forecastLength: 12,
  trainingFrequency: 'monthly',
  confidenceInterval: 95,
  features: ['history_sales', 'promotion', 'season'],
  specialEvents: ['考虑节假日', '考虑促销活动', '考虑季节性波动']
});

// 数据源数据
const dataSources = ref<any[]>([]);

// 添加数据源相关
const addDataSourceVisible = ref(false);
const newDataSourceForm = reactive({
  name: '',
  sourceType: 'CRM'
});

const spanToMonths = (span: string) => {
  if (span === '6month') return 6;
  if (span === '12month') return 12;
  if (span === '24month') return 24;
  if (span === '36month') return 36;
  return 12;
};

const monthsToSpan = (months: number) => {
  if (months === 6) return '6month';
  if (months === 12) return '12month';
  if (months === 24) return '24month';
  if (months === 36) return '36month';
  return '12month';
};

const loadAll = async () => {
  try {
    const cfgRes: any = await forecastApi.getForecastConfig();
    const cfg = cfgRes?.data || {};
    modelConfig.modelType = cfg?.modelType || 'MOVING_AVERAGE';
    modelConfig.historyDataSpan = monthsToSpan(Number(cfg?.historyMonths || 12));
  } catch (e) {
  }
  try {
    const dsRes: any = await forecastApi.getForecastDataSources();
    const list = dsRes?.data?.list || dsRes?.data?.records || [];
    dataSources.value = Array.isArray(list) ? list : [];
  } catch (e) {
    dataSources.value = [];
  }
};

// 保存模型配置
const saveModelConfig = async () => {
  try {
    await forecastApi.saveForecastConfig({
      modelType: modelConfig.modelType,
      historyMonths: spanToMonths(modelConfig.historyDataSpan),
      enabled: 1
    });
    ElMessage.success('模型配置已保存');
  } catch (e) {
    ElMessage.error('模型配置保存失败');
  }
};

// 重置模型配置
const resetModelConfig = () => {
  Object.assign(modelConfig, {
    modelType: 'MOVING_AVERAGE',
    forecastPeriod: 'monthly',
    historyDataSpan: '12month',
    forecastLength: 12,
    trainingFrequency: 'monthly',
    confidenceInterval: 95,
    features: ['history_sales', 'promotion', 'season'],
    specialEvents: ['考虑节假日', '考虑促销活动', '考虑季节性波动']
  });
  ElMessage.info('模型配置已重置');
};

// 测试模型
const testModel = async () => {
  try {
    await forecastApi.testForecastModel();
    ElMessage.success('模型测试完成');
  } catch (e) {
    ElMessage.error('模型测试失败');
  }
};

// 显示添加数据源对话框
const showAddDataSourceDialog = () => {
  // 重置表单
  Object.assign(newDataSourceForm, {
    name: '',
    sourceType: 'CRM'
  });
  
  // 打开对话框
  addDataSourceVisible.value = true;
};

// 保存数据源
const saveDataSource = async () => {
  if (!newDataSourceForm.name) {
    ElMessage.warning('请输入数据源名称');
    return;
  }
  if (!newDataSourceForm.sourceType) {
    ElMessage.warning('请选择数据源类型');
    return;
  }
  try {
    await forecastApi.createForecastDataSource({
      name: newDataSourceForm.name,
      sourceType: newDataSourceForm.sourceType,
      enabled: 1
    });
    addDataSourceVisible.value = false;
    ElMessage.success('数据源添加成功');
    loadAll();
  } catch (e) {
    ElMessage.error('数据源添加失败');
  }
};

// 测试连接
const testConnection = async (row: any) => {
  if (!row?.id) return;
  try {
    await forecastApi.testForecastDataSource(Number(row.id));
    ElMessage.success('连接测试完成');
    loadAll();
  } catch (e) {
    ElMessage.error('连接测试失败');
  }
};

// 删除数据源
const deleteDataSource = async (row: any) => {
  if (!row?.id) return;
  try {
    await forecastApi.deleteForecastDataSource(Number(row.id));
    ElMessage.success('删除成功');
    loadAll();
  } catch (e) {
    ElMessage.error('删除失败');
  }
};

onMounted(() => {
  loadAll();
});
</script>

<style scoped>
.forecast-configuration-component {
  padding: 20px;
}

.configuration-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.card-content {
  padding: 10px 0;
}

.config-form {
  width: 100%;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.form-actions {
  margin-top: 20px;
}

.table-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 添加数据源表单 */
.add-datasource-form {
  padding: 10px 0;
}

.datasource-form {
  max-width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .forecast-configuration-component {
    padding: 10px;
  }
  
  .form-actions {
    flex-direction: column;
    gap: 10px;
  }
  
  .table-actions {
    justify-content: flex-start;
  }
}
</style>
