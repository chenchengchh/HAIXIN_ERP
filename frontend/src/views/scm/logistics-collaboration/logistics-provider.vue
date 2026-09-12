<template>
  <div class="logistics-provider-component">
    <h3>物流商管理</h3>
    <p>管理物流服务商信息，包括资质、服务范围和绩效评价</p>
    
    <el-card shadow="hover" class="provider-card">
      <template #header>
        <div class="card-header">
          <span>物流商列表</span>
        </div>
      </template>
      <div class="card-content">
        <el-table :data="logisticsProviders" style="width: 100%">
          <el-table-column prop="providerId" label="物流商编号" width="150" />
          <el-table-column prop="providerName" label="物流商名称" min-width="200" />
          <el-table-column prop="serviceRange" label="服务范围" width="200" />
          <el-table-column prop="contactPerson" label="联系人" width="120" />
          <el-table-column prop="contactPhone" label="联系电话" width="150" />
          <el-table-column prop="performanceRating" label="绩效评分" width="120">
            <template #default="scope">
              <el-rate :value="scope.row.performanceRating" disabled show-score />
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { forecastApi } from '@/api/scm';

const logisticsProviders = ref<any[]>([]);

const loadProviders = async () => {
  try {
    const res: any = await forecastApi.getLogisticsProviders();
    const list = res?.data?.list || res?.data?.records || [];
    logisticsProviders.value = (Array.isArray(list) ? list : []).map((p: any) => ({
      ...p,
      performanceRating: Number(p.performanceRating || 0)
    }));
  } catch (e) {
    logisticsProviders.value = [];
  }
};

onMounted(() => {
  loadProviders();
});
</script>

<style scoped>
.logistics-provider-component {
  padding: 20px;
}

.provider-card {
  margin-top: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}
</style>
