<template>
  <div class="supplier-collaboration-component">
    <h3>供应商协同</h3>
    <p>与供应商实时协同，管理采购订单、交货计划和质量信息</p>
    
    <el-card shadow="hover" class="collaboration-card">
      <template #header>
        <div class="card-header">
          <span>采购订单协同</span>
        </div>
      </template>
      <div class="card-content">
        <el-table :data="purchaseOrders" style="width: 100%">
          <el-table-column prop="orderId" label="订单编号" width="150" />
          <el-table-column prop="supplierName" label="供应商名称" min-width="200" />
          <el-table-column prop="orderDate" label="下单日期" width="150" />
          <el-table-column prop="deliveryDate" label="交货日期" width="150" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
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

const purchaseOrders = ref<any[]>([]);

const mapStatus = (v: any) => {
  const s = String(v ?? '');
  if (s === '1') return '待确认';
  if (s === '2') return '已确认';
  if (s === '3') return '已发货';
  if (s === '4') return '已完成';
  return s || '未知';
};

const loadOrders = async () => {
  try {
    const res: any = await forecastApi.getSupplierCollaborationPurchaseOrders({ page: 1, size: 20 });
    const list = res?.data?.list || res?.data?.records || [];
    purchaseOrders.value = (Array.isArray(list) ? list : []).map((o: any) => ({
      orderId: o.orderId,
      supplierName: o.supplierName,
      orderDate: o.orderDate,
      deliveryDate: o.deliveryDate,
      status: mapStatus(o.status)
    }));
  } catch (e) {
    purchaseOrders.value = [];
  }
};

// 获取状态标签类型
const getStatusTagType = (status: string): string => {
  switch (status) {
    case '已确认':
      return 'success';
    case '待确认':
      return 'warning';
    case '已发货':
      return 'info';
    case '已完成':
      return 'success';
    default:
      return 'info';
  }
};

onMounted(() => {
  loadOrders();
});
</script>

<style scoped>
.supplier-collaboration-component {
  padding: 20px;
}

.collaboration-card {
  margin-top: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}
</style>
