import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { unwrapListResponse, unwrapResponseData } from '@/api';
import { inventoryApi, inventoryCountApi, inventoryMoveApi } from '@/api/wms';
import type { Inventory, InventoryCount, InventoryMove } from '@/types/wms';

const unwrapApiData = <T = any>(response: any): T => {
  return (unwrapResponseData<T>(response) ?? null) as T;
};

const unwrapApiList = <T = any>(response: any): T[] => {
  return unwrapListResponse<T>(response);
};

export const useWmsStockStore = defineStore('wms-stock', () => {
  // 库存相关状态
  const inventories = ref<Inventory[]>([]);
  const currentInventory = ref<Inventory | null>(null);
  const inventoryLoading = ref(false);
  const inventoryPagination = ref({
    page: 1,
    pageSize: 10,
    total: 0
  });

  // 盘点单相关状态
  const inventoryCounts = ref<InventoryCount[]>([]);
  const currentInventoryCount = ref<InventoryCount | null>(null);
  const inventoryCountLoading = ref(false);
  const inventoryCountPagination = ref({
    page: 1,
    pageSize: 10,
    total: 0
  });

  // 移库单相关状态
  const inventoryMoves = ref<InventoryMove[]>([]);
  const currentInventoryMove = ref<InventoryMove | null>(null);
  const inventoryMoveLoading = ref(false);
  const inventoryMovePagination = ref({
    page: 1,
    pageSize: 10,
    total: 0
  });

  // 按ID获取库存
  const getInventoryById = computed(() => (id: string) => {
    return inventories.value.find(inv => inv.id === id) || null;
  });

  // 按ID获取盘点单
  const getInventoryCountById = computed(() => (id: string) => {
    return inventoryCounts.value.find(count => count.id === id) || null;
  });

  // 按ID获取移库单
  const getInventoryMoveById = computed(() => (id: string) => {
    return inventoryMoves.value.find(move => move.id === id) || null;
  });

  // 按物料ID获取库存
  const getInventoriesByMaterialId = computed(() => (materialId: string) => {
    return inventories.value.filter(inv => inv.materialId === materialId);
  });

  // 按库位ID获取库存
  const getInventoriesByLocationId = computed(() => (locationId: string) => {
    return inventories.value.filter(inv => inv.locationId === locationId);
  });

  // 按仓库ID获取库存
  const getInventoriesByWarehouseId = computed(() => (warehouseId: string) => {
    return inventories.value.filter(inv => inv.warehouseId === warehouseId);
  });

  // 库存预警列表
  const inventoryAlerts = computed(() => {
    // 这里假设库存预警规则是可用数量小于安全库存
    // 实际实现中，安全库存可能存储在物料主数据中
    return inventories.value.filter(inv => inv.availableQuantity < 10); // 示例阈值
  });

  // 待盘点的盘点单列表
  const pendingInventoryCounts = computed(() => {
    return inventoryCounts.value.filter(count => count.status === 'created' || count.status === 'in_progress');
  });

  // 待处理的移库单列表
  const pendingInventoryMoves = computed(() => {
    return inventoryMoves.value.filter(move => move.status === 'created' || move.status === 'in_progress');
  });

  // 库存相关操作
  async function fetchInventories(params?: any) {
    try {
      inventoryLoading.value = true;
      const response = await inventoryApi.getList(params);
      const rows = unwrapApiList<Inventory>(response);
      inventories.value = rows;
      return rows;
    } catch (error) {
      console.error('获取库存列表失败:', error);
      throw error;
    } finally {
      inventoryLoading.value = false;
    }
  }

  async function fetchInventoryDetail(id: string) {
    try {
      inventoryLoading.value = true;
      const response = await inventoryApi.getDetail(id);
      const row = unwrapApiData<Inventory>(response);
      currentInventory.value = row;
      return row;
    } catch (error) {
      console.error(`获取库存${id}详情失败:`, error);
      throw error;
    } finally {
      inventoryLoading.value = false;
    }
  }

  async function fetchInventoryByMaterial(materialId: string, warehouseId: string) {
    try {
      inventoryLoading.value = true;
      const response = await inventoryApi.getByMaterial(materialId, warehouseId);
      const rows = unwrapApiList<Inventory>(response);
      // 更新对应物料和仓库的库存列表
      const existingIndices = inventories.value
        .map((inv, index) => ({ inv, index }))
        .filter(({ inv }) => inv.materialId === materialId && inv.warehouseId === warehouseId)
        .map(({ index }) => index);
      
      // 删除旧的库存数据
      for (let i = existingIndices.length - 1; i >= 0; i--) {
        const index = existingIndices[i];
        if (typeof index === 'number') {
          inventories.value.splice(index, 1);
        }
      }
      
      // 添加新的库存数据
      inventories.value.push(...rows);
      
      return rows;
    } catch (error) {
      console.error(`获取物料${materialId}在仓库${warehouseId}的库存失败:`, error);
      throw error;
    } finally {
      inventoryLoading.value = false;
    }
  }

  async function fetchInventoryByLocation(locationId: string) {
    try {
      inventoryLoading.value = true;
      const response = await inventoryApi.getByLocation(locationId);
      const rows = unwrapApiList<Inventory>(response);
      // 更新对应库位的库存列表
      const existingIndices = inventories.value
        .map((inv, index) => ({ inv, index }))
        .filter(({ inv }) => inv.locationId === locationId)
        .map(({ index }) => index);
      
      // 删除旧的库存数据
      for (let i = existingIndices.length - 1; i >= 0; i--) {
        const index = existingIndices[i];
        if (typeof index === 'number') {
          inventories.value.splice(index, 1);
        }
      }
      
      // 添加新的库存数据
      inventories.value.push(...rows);
      
      return rows;
    } catch (error) {
      console.error(`获取库位${locationId}的库存失败:`, error);
      throw error;
    } finally {
      inventoryLoading.value = false;
    }
  }

  async function fetchInventoryAlerts(params?: any) {
    try {
      inventoryLoading.value = true;
      const response = await inventoryApi.getAlertList(params);
      // 更新库存预警列表
      return unwrapApiList<any>(response);
    } catch (error) {
      console.error('获取库存预警列表失败:', error);
      throw error;
    } finally {
      inventoryLoading.value = false;
    }
  }

  async function freezeInventory(id: string, data: any) {
    try {
      inventoryLoading.value = true;
      const response = await inventoryApi.freeze(id, data);
      const row = unwrapApiData<Inventory>(response);
      const index = inventories.value.findIndex(inv => inv.id === id);
      if (index !== -1) {
        inventories.value[index] = { ...inventories.value[index], ...row };
      }
      if (currentInventory.value && currentInventory.value.id === id) {
        currentInventory.value = row;
      }
      return row;
    } catch (error) {
      console.error(`冻结库存${id}失败:`, error);
      throw error;
    } finally {
      inventoryLoading.value = false;
    }
  }

  async function unfreezeInventory(id: string) {
    try {
      inventoryLoading.value = true;
      const response = await inventoryApi.unfreeze(id);
      const row = unwrapApiData<Inventory>(response);
      const index = inventories.value.findIndex(inv => inv.id === id);
      if (index !== -1) {
        inventories.value[index] = { ...inventories.value[index], ...row };
      }
      if (currentInventory.value && currentInventory.value.id === id) {
        currentInventory.value = row;
      }
      return row;
    } catch (error) {
      console.error(`解冻库存${id}失败:`, error);
      throw error;
    } finally {
      inventoryLoading.value = false;
    }
  }

  async function adjustInventory(id: string, data: any) {
    try {
      inventoryLoading.value = true;
      const response = await inventoryApi.adjust(id, data);
      const row = unwrapApiData<Inventory>(response);
      const index = inventories.value.findIndex(inv => inv.id === id);
      if (index !== -1) {
        inventories.value[index] = { ...inventories.value[index], ...row };
      }
      if (currentInventory.value && currentInventory.value.id === id) {
        currentInventory.value = row;
      }
      return row;
    } catch (error) {
      console.error(`调整库存${id}失败:`, error);
      throw error;
    } finally {
      inventoryLoading.value = false;
    }
  }

  // 盘点单相关操作
  async function fetchInventoryCounts(params?: any) {
    try {
      inventoryCountLoading.value = true;
      const response = await inventoryCountApi.getList(params);
      const rows = unwrapApiList<InventoryCount>(response);
      inventoryCounts.value = rows;
      return rows;
    } catch (error) {
      console.error('获取盘点单列表失败:', error);
      throw error;
    } finally {
      inventoryCountLoading.value = false;
    }
  }

  async function fetchInventoryCountDetail(id: string) {
    try {
      inventoryCountLoading.value = true;
      const response = await inventoryCountApi.getDetail(id);
      const row = unwrapApiData<InventoryCount>(response);
      currentInventoryCount.value = row;
      return row;
    } catch (error) {
      console.error(`获取盘点单${id}详情失败:`, error);
      throw error;
    } finally {
      inventoryCountLoading.value = false;
    }
  }

  async function createInventoryCount(data: InventoryCount) {
    try {
      inventoryCountLoading.value = true;
      const response = await inventoryCountApi.create(data);
      const row = unwrapApiData<InventoryCount>(response);
      inventoryCounts.value.push(row);
      return row;
    } catch (error) {
      console.error('创建盘点单失败:', error);
      throw error;
    } finally {
      inventoryCountLoading.value = false;
    }
  }

  async function updateInventoryCount(id: string, data: Partial<InventoryCount>) {
    try {
      inventoryCountLoading.value = true;
      const response = await inventoryCountApi.update(id, data);
      const row = unwrapApiData<InventoryCount>(response);
      const index = inventoryCounts.value.findIndex(count => count.id === id);
      if (index !== -1) {
        inventoryCounts.value[index] = { ...inventoryCounts.value[index], ...row };
      }
      if (currentInventoryCount.value && currentInventoryCount.value.id === id) {
        currentInventoryCount.value = row;
      }
      return row;
    } catch (error) {
      console.error(`更新盘点单${id}失败:`, error);
      throw error;
    } finally {
      inventoryCountLoading.value = false;
    }
  }

  async function deleteInventoryCount(id: string) {
    try {
      inventoryCountLoading.value = true;
      await inventoryCountApi.delete(id);
      inventoryCounts.value = inventoryCounts.value.filter(count => count.id !== id);
      if (currentInventoryCount.value && currentInventoryCount.value.id === id) {
        currentInventoryCount.value = null;
      }
    } catch (error) {
      console.error(`删除盘点单${id}失败:`, error);
      throw error;
    } finally {
      inventoryCountLoading.value = false;
    }
  }

  async function startInventoryCount(id: string) {
    try {
      inventoryCountLoading.value = true;
      const response = await inventoryCountApi.start(id);
      const index = inventoryCounts.value.findIndex(count => count.id === id);
      if (index !== -1) {
        inventoryCounts.value[index] = { ...inventoryCounts.value[index], ...response.data };
      }
      if (currentInventoryCount.value && currentInventoryCount.value.id === id) {
        currentInventoryCount.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`开始盘点单${id}失败:`, error);
      throw error;
    } finally {
      inventoryCountLoading.value = false;
    }
  }

  async function completeInventoryCount(id: string) {
    try {
      inventoryCountLoading.value = true;
      const response = await inventoryCountApi.complete(id);
      const index = inventoryCounts.value.findIndex(count => count.id === id);
      if (index !== -1) {
        inventoryCounts.value[index] = { ...inventoryCounts.value[index], ...response.data };
      }
      if (currentInventoryCount.value && currentInventoryCount.value.id === id) {
        currentInventoryCount.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`完成盘点单${id}失败:`, error);
      throw error;
    } finally {
      inventoryCountLoading.value = false;
    }
  }

  async function cancelInventoryCount(id: string) {
    try {
      inventoryCountLoading.value = true;
      const response = await inventoryCountApi.cancel(id);
      const index = inventoryCounts.value.findIndex(count => count.id === id);
      if (index !== -1) {
        inventoryCounts.value[index] = { ...inventoryCounts.value[index], ...response.data };
      }
      if (currentInventoryCount.value && currentInventoryCount.value.id === id) {
        currentInventoryCount.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`取消盘点单${id}失败:`, error);
      throw error;
    } finally {
      inventoryCountLoading.value = false;
    }
  }

  async function updateInventoryCountItem(itemId: string, data: any) {
    try {
      inventoryCountLoading.value = true;
      const response = await inventoryCountApi.updateItem(itemId, data);
      // 更新相关盘点单中的盘点项
      for (const count of inventoryCounts.value) {
        const itemIndex = count.items.findIndex(item => item.id === itemId);
        if (itemIndex !== -1) {
          count.items[itemIndex] = { ...count.items[itemIndex], ...response.data };
        }
      }
      // 更新当前盘点单中的盘点项
      if (currentInventoryCount.value) {
        const itemIndex = currentInventoryCount.value.items.findIndex(item => item.id === itemId);
        if (itemIndex !== -1) {
          currentInventoryCount.value.items[itemIndex] = { ...currentInventoryCount.value.items[itemIndex], ...response.data };
        }
      }
      return response.data;
    } catch (error) {
      console.error(`更新盘点项${itemId}失败:`, error);
      throw error;
    } finally {
      inventoryCountLoading.value = false;
    }
  }

  async function generateInventoryCountDiffReport(id: string) {
    try {
      inventoryCountLoading.value = true;
      const response = await inventoryCountApi.generateDiffReport(id);
      return response.data;
    } catch (error) {
      console.error(`生成盘点差异报告${id}失败:`, error);
      throw error;
    } finally {
      inventoryCountLoading.value = false;
    }
  }

  async function adjustInventoryByCount(countId: string) {
    try {
      inventoryCountLoading.value = true;
      const response = await inventoryApi.countAdjust(countId);
      // 更新相关库存数据
      return response.data;
    } catch (error) {
      console.error(`根据盘点单${countId}调整库存失败:`, error);
      throw error;
    } finally {
      inventoryCountLoading.value = false;
    }
  }

  // 移库单相关操作
  async function fetchInventoryMoves(params?: any) {
    try {
      inventoryMoveLoading.value = true;
      const response = await inventoryMoveApi.getList(params);
      inventoryMoves.value = response.data;
      return response.data;
    } catch (error) {
      console.error('获取移库单列表失败:', error);
      throw error;
    } finally {
      inventoryMoveLoading.value = false;
    }
  }

  async function fetchInventoryMoveDetail(id: string) {
    try {
      inventoryMoveLoading.value = true;
      const response = await inventoryMoveApi.getDetail(id);
      currentInventoryMove.value = response.data;
      return response.data;
    } catch (error) {
      console.error(`获取移库单${id}详情失败:`, error);
      throw error;
    } finally {
      inventoryMoveLoading.value = false;
    }
  }

  async function createInventoryMove(data: InventoryMove) {
    try {
      inventoryMoveLoading.value = true;
      const response = await inventoryMoveApi.create(data);
      inventoryMoves.value.push(response.data);
      return response.data;
    } catch (error) {
      console.error('创建移库单失败:', error);
      throw error;
    } finally {
      inventoryMoveLoading.value = false;
    }
  }

  async function updateInventoryMove(id: string, data: Partial<InventoryMove>) {
    try {
      inventoryMoveLoading.value = true;
      const response = await inventoryMoveApi.update(id, data);
      const index = inventoryMoves.value.findIndex(move => move.id === id);
      if (index !== -1) {
        inventoryMoves.value[index] = { ...inventoryMoves.value[index], ...response.data };
      }
      if (currentInventoryMove.value && currentInventoryMove.value.id === id) {
        currentInventoryMove.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`更新移库单${id}失败:`, error);
      throw error;
    } finally {
      inventoryMoveLoading.value = false;
    }
  }

  async function deleteInventoryMove(id: string) {
    try {
      inventoryMoveLoading.value = true;
      await inventoryMoveApi.delete(id);
      inventoryMoves.value = inventoryMoves.value.filter(move => move.id !== id);
      if (currentInventoryMove.value && currentInventoryMove.value.id === id) {
        currentInventoryMove.value = null;
      }
    } catch (error) {
      console.error(`删除移库单${id}失败:`, error);
      throw error;
    } finally {
      inventoryMoveLoading.value = false;
    }
  }

  async function approveInventoryMove(id: string) {
    try {
      inventoryMoveLoading.value = true;
      const response = await inventoryMoveApi.approve(id);
      const index = inventoryMoves.value.findIndex(move => move.id === id);
      if (index !== -1) {
        inventoryMoves.value[index] = { ...inventoryMoves.value[index], ...response.data };
      }
      if (currentInventoryMove.value && currentInventoryMove.value.id === id) {
        currentInventoryMove.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`审核移库单${id}失败:`, error);
      throw error;
    } finally {
      inventoryMoveLoading.value = false;
    }
  }

  async function startInventoryMove(id: string) {
    try {
      inventoryMoveLoading.value = true;
      const response = await inventoryMoveApi.start(id);
      const index = inventoryMoves.value.findIndex(move => move.id === id);
      if (index !== -1) {
        inventoryMoves.value[index] = { ...inventoryMoves.value[index], ...response.data };
      }
      if (currentInventoryMove.value && currentInventoryMove.value.id === id) {
        currentInventoryMove.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`开始移库单${id}失败:`, error);
      throw error;
    } finally {
      inventoryMoveLoading.value = false;
    }
  }

  async function completeInventoryMove(id: string) {
    try {
      inventoryMoveLoading.value = true;
      const response = await inventoryMoveApi.complete(id);
      const index = inventoryMoves.value.findIndex(move => move.id === id);
      if (index !== -1) {
        inventoryMoves.value[index] = { ...inventoryMoves.value[index], ...response.data };
      }
      if (currentInventoryMove.value && currentInventoryMove.value.id === id) {
        currentInventoryMove.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`完成移库单${id}失败:`, error);
      throw error;
    } finally {
      inventoryMoveLoading.value = false;
    }
  }

  async function cancelInventoryMove(id: string) {
    try {
      inventoryMoveLoading.value = true;
      const response = await inventoryMoveApi.cancel(id);
      const index = inventoryMoves.value.findIndex(move => move.id === id);
      if (index !== -1) {
        inventoryMoves.value[index] = { ...inventoryMoves.value[index], ...response.data };
      }
      if (currentInventoryMove.value && currentInventoryMove.value.id === id) {
        currentInventoryMove.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`取消移库单${id}失败:`, error);
      throw error;
    } finally {
      inventoryMoveLoading.value = false;
    }
  }

  // 重置状态
  function resetState() {
    inventories.value = [];
    currentInventory.value = null;
    inventoryLoading.value = false;
    inventoryPagination.value = {
      page: 1,
      pageSize: 10,
      total: 0
    };

    inventoryCounts.value = [];
    currentInventoryCount.value = null;
    inventoryCountLoading.value = false;
    inventoryCountPagination.value = {
      page: 1,
      pageSize: 10,
      total: 0
    };

    inventoryMoves.value = [];
    currentInventoryMove.value = null;
    inventoryMoveLoading.value = false;
    inventoryMovePagination.value = {
      page: 1,
      pageSize: 10,
      total: 0
    };
  }

  return {
    // 库存相关
    inventories,
    currentInventory,
    inventoryLoading,
    inventoryPagination,

    // 盘点单相关
    inventoryCounts,
    currentInventoryCount,
    inventoryCountLoading,
    inventoryCountPagination,

    // 移库单相关
    inventoryMoves,
    currentInventoryMove,
    inventoryMoveLoading,
    inventoryMovePagination,

    // 计算属性
    getInventoryById,
    getInventoryCountById,
    getInventoryMoveById,
    getInventoriesByMaterialId,
    getInventoriesByLocationId,
    getInventoriesByWarehouseId,
    inventoryAlerts,
    pendingInventoryCounts,
    pendingInventoryMoves,

    // 库存操作
    fetchInventories,
    fetchInventoryDetail,
    fetchInventoryByMaterial,
    fetchInventoryByLocation,
    fetchInventoryAlerts,
    freezeInventory,
    unfreezeInventory,
    adjustInventory,

    // 盘点单操作
    fetchInventoryCounts,
    fetchInventoryCountDetail,
    createInventoryCount,
    updateInventoryCount,
    deleteInventoryCount,
    startInventoryCount,
    completeInventoryCount,
    cancelInventoryCount,
    updateInventoryCountItem,
    generateInventoryCountDiffReport,
    adjustInventoryByCount,

    // 移库单操作
    fetchInventoryMoves,
    fetchInventoryMoveDetail,
    createInventoryMove,
    updateInventoryMove,
    deleteInventoryMove,
    approveInventoryMove,
    startInventoryMove,
    completeInventoryMove,
    cancelInventoryMove,

    // 重置状态
    resetState
  };
});
