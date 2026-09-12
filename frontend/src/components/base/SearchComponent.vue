<template>
  <div class="search-component">
    <el-autocomplete
      v-model="searchText"
      :fetch-suggestions="querySearchAsync"
      :placeholder="placeholder"
      :trigger-on-focus="false"
      clearable
      class="global-search-input"
      @select="handleSelect"
      @keyup.enter="handleEnter"
    >
      <template #prefix>
        <el-icon class="search-icon"><Search /></el-icon>
      </template>
      <template #default="{ item }">
        <div class="search-item">
          <div class="item-title">{{ item.title }}</div>
          <div class="item-meta">
            <span class="item-type">{{ item.type }}</span>
            <span class="item-subtitle">{{ item.subtitle }}</span>
          </div>
        </div>
      </template>
      <template #suffix>
        <span class="search-shortcut">⌘K</span>
      </template>
    </el-autocomplete>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { erpApi } from '../../api/erp'
import { logger } from '../../utils/logger'
import { useRouter } from 'vue-router'

// Props
const props = defineProps({
  placeholder: {
    type: String,
    default: '搜索客户、订单、物料...'
  },
  debounce: {
    type: Number,
    default: 300
  }
})

// Emits
const emit = defineEmits(['search', 'result-click', 'clear'])

const router = useRouter()
const searchText = ref('')

// 搜索建议方法
const querySearchAsync = async (queryString: string, cb: (results: any[]) => void) => {
  if (!queryString.trim()) {
    cb([])
    return
  }

  try {
    // 模拟搜索API调用，实际项目中应该调用真实的搜索API
    // 这里我们模拟从各个模块获取数据
    // 注意：这里仅作演示，实际应根据API调整
    const [customers, suppliers, materials, purchaseOrders, salesOrders, productionOrders] = await Promise.all([
      erpApi.basicData.getCustomers({ keyword: queryString, page: 1, size: 5 } as any).catch(() => null),
      erpApi.basicData.getSuppliers({ keyword: queryString, page: 1, size: 5 } as any).catch(() => null),
      erpApi.basicData.getMaterials({ keyword: queryString, page: 1, size: 5 } as any).catch(() => null),
      erpApi.supplyChain.getPurchaseOrders({ keyword: queryString, page: 1, size: 5 } as any).catch(() => null),
      erpApi.supplyChain.getSalesOrders({ keyword: queryString, page: 1, size: 5 } as any).catch(() => null),
      erpApi.production.getProductionOrders({ keyword: queryString, page: 1, size: 5 } as any).catch(() => null)
    ])

    const results: any[] = []

    // 客户结果
    if (customers?.data?.list) {
      customers.data.list.forEach((customer: any) => {
        results.push({
          value: customer.customer_name,
          title: customer.customer_name,
          subtitle: customer.contact_person || customer.phone,
          type: '客户',
          route: '/erp/basic-data/customers',
          original: customer
        })
      })
    }

    // 供应商结果
    if (suppliers?.data?.list) {
      suppliers.data.list.forEach((supplier: any) => {
        results.push({
          value: supplier.supplier_name,
          title: supplier.supplier_name,
          subtitle: supplier.contact_person || supplier.phone,
          type: '供应商',
          route: '/erp/basic-data/suppliers',
          original: supplier
        })
      })
    }

    // 物料结果
    if (materials?.data?.list) {
      materials.data.list.forEach((material: any) => {
        results.push({
          value: material.material_name,
          title: material.material_name,
          subtitle: material.material_code,
          type: '物料',
          route: '/erp/basic-data/materials',
          original: material
        })
      })
    }

    // 采购订单结果
    if (purchaseOrders?.data?.list) {
      purchaseOrders.data.list.forEach((order: any) => {
        results.push({
          value: order.order_no,
          title: order.order_no,
          subtitle: `供应商: ${order.supplier_name || '未知'}`,
          type: '采购订单',
          route: '/erp/supply-chain/purchase',
          original: order
        })
      })
    }
    
    // 销售订单结果
    if (salesOrders?.data?.list) {
      salesOrders.data.list.forEach((order: any) => {
        results.push({
          value: order.order_no,
          title: order.order_no,
          subtitle: `客户: ${order.customer_name || '未知'}`,
          type: '销售订单',
          route: '/erp/supply-chain/sales',
          original: order
        })
      })
    }

    // 生产订单结果
    if (productionOrders?.data?.list) {
      productionOrders.data.list.forEach((order: any) => {
        results.push({
          value: order.order_no,
          title: order.order_no,
          subtitle: `产品: ${order.product_name || '未知'}`,
          type: '生产订单',
          route: '/erp/production/orders',
          original: order
        })
      })
    }

    cb(results)
  } catch (error) {
    logger.error('搜索失败:', error)
    cb([])
  }
}

const handleSelect = (item: any) => {
  emit('result-click', item)
  if (item.route) {
    router.push(item.route)
  }
}

const handleEnter = () => {
  if (searchText.value) {
    emit('search', searchText.value)
  }
}

// 快捷键支持
const handleKeydown = (e: KeyboardEvent) => {
  if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
    e.preventDefault()
    const input = document.querySelector('.global-search-input input') as HTMLInputElement
    if (input) {
      input.focus()
    }
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped lang="scss">
.search-component {
  width: 100%;
  max-width: 400px;

  :deep(.el-autocomplete) {
    width: 100%;
  }

  :deep(.el-input__wrapper) {
    background-color: var(--el-fill-color-light);
    box-shadow: none;
    border-radius: 8px;
    padding-left: 12px;
    transition: all 0.3s;

    &.is-focus {
      background-color: #fff;
      box-shadow: 0 0 0 1px var(--el-color-primary) inset;
    }

    &:hover {
      background-color: #fff;
    }
  }

  .search-shortcut {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    background-color: var(--el-fill-color);
    padding: 2px 6px;
    border-radius: 4px;
    margin-right: 4px;
  }
}

.search-item {
  padding: 4px 0;

  .item-title {
    font-weight: 500;
    color: var(--el-text-color-primary);
    margin-bottom: 4px;
  }

  .item-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;

    .item-type {
      background-color: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
      padding: 1px 6px;
      border-radius: 4px;
    }

    .item-subtitle {
      color: var(--el-text-color-secondary);
    }
  }
}
</style>