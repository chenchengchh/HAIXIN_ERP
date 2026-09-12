<template>
  <div class="route-optimization-component">
    <h3>运输路径优化</h3>
    <p>优化运输路径，降低运输成本，提高配送效率</p>
    
    <el-card shadow="hover" class="route-card">
      <template #header>
        <div class="card-header">
          <span>路径优化配置</span>
        </div>
      </template>
      <div class="card-content">
        <el-form :model="routeForm" label-position="left" label-width="120px" class="route-form">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="出发地">
                <el-input v-model="routeForm.startLocation" placeholder="输入出发地" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="目的地">
                <el-input v-model="routeForm.endLocation" placeholder="输入目的地" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="运输方式">
                <el-select v-model="routeForm.transportMode" placeholder="选择运输方式" style="width: 100%">
                  <el-option label="公路运输" value="road" />
                  <el-option label="铁路运输" value="rail" />
                  <el-option label="航空运输" value="air" />
                  <el-option label="水路运输" value="water" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="货物重量">
                <el-input-number v-model="routeForm.weight" :min="0" :precision="2" placeholder="输入货物重量" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="优化目标">
                <el-select v-model="routeForm.optimizationGoal" placeholder="选择优化目标" style="width: 100%">
                  <el-option label="成本最低" value="cost" />
                  <el-option label="时间最短" value="time" />
                  <el-option label="距离最短" value="distance" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="" style="margin-top: 24px;">
                <el-button type="primary" @click="optimizeRoute">优化路径</el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
    </el-card>
    
    <el-card shadow="hover" class="result-card" v-if="optimizationResult">
      <template #header>
        <div class="card-header">
          <span>优化结果</span>
        </div>
      </template>
      <div class="card-content">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="优化路径">
            {{ optimizationResult.route }}
          </el-descriptions-item>
          <el-descriptions-item label="预计时间">
            {{ optimizationResult.estimatedTime }}
          </el-descriptions-item>
          <el-descriptions-item label="预计成本">
            {{ optimizationResult.estimatedCost }}元
          </el-descriptions-item>
          <el-descriptions-item label="距离">
            {{ optimizationResult.distance }}公里
          </el-descriptions-item>
          <el-descriptions-item label="油耗">
            {{ optimizationResult.fuelConsumption }}升
          </el-descriptions-item>
          <el-descriptions-item label="碳排放量">
            {{ optimizationResult.carbonEmission }}千克
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { forecastApi } from '@/api/scm';

// 路径优化表单数据
const routeForm = reactive({
  startLocation: '北京',
  endLocation: '上海',
  transportMode: 'road',
  weight: 10.5,
  optimizationGoal: 'cost'
});

// 优化结果
const optimizationResult = ref<any>(null);

// 优化路径
const optimizeRoute = async () => {
  optimizationResult.value = null;
  try {
    const res: any = await forecastApi.optimizeRoute(routeForm);
    optimizationResult.value = res?.data || null;
    ElMessage.success('路径优化完成');
  } catch (e) {
    ElMessage.error('路径优化失败');
  }
};
</script>

<style scoped>
.route-optimization-component {
  padding: 20px;
}

.route-card,
.result-card {
  margin-top: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.route-form {
  width: 100%;
}
</style>
