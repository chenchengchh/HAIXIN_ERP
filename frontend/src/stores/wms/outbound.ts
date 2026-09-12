import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { unwrapListResponse, unwrapResponseData } from '@/api';
import { waveApi, outboundOrderApi, pickingTaskApi } from '@/api/wms';
import type { Wave, OutboundOrder, PickingTask } from '@/types/wms';

const unwrapApiData = <T = any>(response: any): T => {
  return (unwrapResponseData<T>(response) ?? null) as T;
};

const unwrapApiList = <T = any>(response: any): T[] => {
  return unwrapListResponse<T>(response);
};

export const useWmsOutboundStore = defineStore('wms-outbound', () => {
  // 波次相关状态
  const waves = ref<Wave[]>([]);
  const currentWave = ref<Wave | null>(null);
  const waveLoading = ref(false);
  const wavePagination = ref({
    page: 1,
    pageSize: 10,
    total: 0
  });

  // 出库单相关状态
  const outboundOrders = ref<OutboundOrder[]>([]);
  const currentOutboundOrder = ref<OutboundOrder | null>(null);
  const outboundOrderLoading = ref(false);
  const outboundOrderPagination = ref({
    page: 1,
    pageSize: 10,
    total: 0
  });

  // 拣货任务相关状态
  const pickingTasks = ref<PickingTask[]>([]);
  const currentPickingTask = ref<PickingTask | null>(null);
  const pickingTaskLoading = ref(false);
  const pickingTaskPagination = ref({
    page: 1,
    pageSize: 10,
    total: 0
  });

  // 按ID获取波次
  const getWaveById = computed(() => (id: string) => {
    return waves.value.find(wave => wave.id === id) || null;
  });

  // 按ID获取出库单
  const getOutboundOrderById = computed(() => (id: string) => {
    return outboundOrders.value.find(order => order.id === id) || null;
  });

  // 按ID获取拣货任务
  const getPickingTaskById = computed(() => (id: string) => {
    return pickingTasks.value.find(task => task.id === id) || null;
  });

  // 按波次ID获取出库单
  const getOutboundOrdersByWaveId = computed(() => (waveId: string) => {
    return outboundOrders.value.filter(order => 
      waves.value.some(wave => wave.id === waveId && wave.orders.some(o => o.id === order.id))
    );
  });

  // 按波次ID获取拣货任务
  const getPickingTasksByWaveId = computed(() => (waveId: string) => {
    return pickingTasks.value.filter(task => task.waveId === waveId);
  });

  // 待处理的波次列表
  const pendingWaves = computed(() => {
    return waves.value.filter(wave => wave.status === 'created' || wave.status === 'allocated');
  });

  // 待拣货的出库单列表
  const pendingPickingOrders = computed(() => {
    return outboundOrders.value.filter(order => order.status === 'allocated' || order.status === 'picking');
  });

  // 待处理的拣货任务列表
  const pendingPickingTasks = computed(() => {
    return pickingTasks.value.filter(task => task.status === 'pending' || task.status === 'assigned' || task.status === 'working');
  });

  // 波次相关操作
  async function fetchWaves(params?: any) {
    try {
      waveLoading.value = true;
      const response = await waveApi.getList(params);
      const rows = unwrapApiList<Wave>(response);
      waves.value = rows;
      return rows;
    } catch (error) {
      console.error('获取波次列表失败:', error);
      throw error;
    } finally {
      waveLoading.value = false;
    }
  }

  async function fetchWaveDetail(id: string) {
    try {
      waveLoading.value = true;
      const response = await waveApi.getDetail(id);
      const row = unwrapApiData<Wave>(response);
      currentWave.value = row;
      return row;
    } catch (error) {
      console.error(`获取波次${id}详情失败:`, error);
      throw error;
    } finally {
      waveLoading.value = false;
    }
  }

  async function createWave(data: Wave) {
    try {
      waveLoading.value = true;
      const response = await waveApi.create(data);
      const row = unwrapApiData<Wave>(response);
      waves.value.push(row);
      return row;
    } catch (error) {
      console.error('创建波次失败:', error);
      throw error;
    } finally {
      waveLoading.value = false;
    }
  }

  async function autoCreateWave(data: any) {
    try {
      waveLoading.value = true;
      const response = await waveApi.autoCreate(data);
      const row = unwrapApiData<Wave>(response);
      waves.value.push(row);
      return row;
    } catch (error) {
      console.error('自动创建波次失败:', error);
      throw error;
    } finally {
      waveLoading.value = false;
    }
  }

  async function updateWave(id: string, data: Partial<Wave>) {
    try {
      waveLoading.value = true;
      const response = await waveApi.update(id, data);
      const row = unwrapApiData<Wave>(response);
      const index = waves.value.findIndex(wave => wave.id === id);
      if (index !== -1) {
        waves.value[index] = { ...waves.value[index], ...row };
      }
      if (currentWave.value && currentWave.value.id === id) {
        currentWave.value = row;
      }
      return row;
    } catch (error) {
      console.error(`更新波次${id}失败:`, error);
      throw error;
    } finally {
      waveLoading.value = false;
    }
  }

  async function deleteWave(id: string) {
    try {
      waveLoading.value = true;
      await waveApi.delete(id);
      waves.value = waves.value.filter(wave => wave.id !== id);
      if (currentWave.value && currentWave.value.id === id) {
        currentWave.value = null;
      }
    } catch (error) {
      console.error(`删除波次${id}失败:`, error);
      throw error;
    } finally {
      waveLoading.value = false;
    }
  }

  async function allocateWaveInventory(id: string) {
    try {
      waveLoading.value = true;
      const response = await waveApi.allocate(id);
      const index = waves.value.findIndex(wave => wave.id === id);
      if (index !== -1) {
        waves.value[index] = { ...waves.value[index], ...response.data };
      }
      if (currentWave.value && currentWave.value.id === id) {
        currentWave.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`波次${id}库存分配失败:`, error);
      throw error;
    } finally {
      waveLoading.value = false;
    }
  }

  async function startWavePicking(id: string) {
    try {
      waveLoading.value = true;
      const response = await waveApi.startPicking(id);
      const index = waves.value.findIndex(wave => wave.id === id);
      if (index !== -1) {
        waves.value[index] = { ...waves.value[index], ...response.data };
      }
      if (currentWave.value && currentWave.value.id === id) {
        currentWave.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`波次${id}开始拣货失败:`, error);
      throw error;
    } finally {
      waveLoading.value = false;
    }
  }

  async function completeWave(id: string) {
    try {
      waveLoading.value = true;
      const response = await waveApi.complete(id);
      const index = waves.value.findIndex(wave => wave.id === id);
      if (index !== -1) {
        waves.value[index] = { ...waves.value[index], ...response.data };
      }
      if (currentWave.value && currentWave.value.id === id) {
        currentWave.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`完成波次${id}失败:`, error);
      throw error;
    } finally {
      waveLoading.value = false;
    }
  }

  async function cancelWave(id: string) {
    try {
      waveLoading.value = true;
      const response = await waveApi.cancel(id);
      const index = waves.value.findIndex(wave => wave.id === id);
      if (index !== -1) {
        waves.value[index] = { ...waves.value[index], ...response.data };
      }
      if (currentWave.value && currentWave.value.id === id) {
        currentWave.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`取消波次${id}失败:`, error);
      throw error;
    } finally {
      waveLoading.value = false;
    }
  }

  // 出库单相关操作
  async function fetchOutboundOrders(params?: any) {
    try {
      outboundOrderLoading.value = true;
      const response = await outboundOrderApi.getList(params);
      outboundOrders.value = response.data;
      return response.data;
    } catch (error) {
      console.error('获取出库单列表失败:', error);
      throw error;
    } finally {
      outboundOrderLoading.value = false;
    }
  }

  async function fetchOutboundOrderDetail(id: string) {
    try {
      outboundOrderLoading.value = true;
      const response = await outboundOrderApi.getDetail(id);
      currentOutboundOrder.value = response.data;
      return response.data;
    } catch (error) {
      console.error(`获取出库单${id}详情失败:`, error);
      throw error;
    } finally {
      outboundOrderLoading.value = false;
    }
  }

  async function createOutboundOrder(data: OutboundOrder) {
    try {
      outboundOrderLoading.value = true;
      const response = await outboundOrderApi.create(data);
      outboundOrders.value.push(response.data);
      return response.data;
    } catch (error) {
      console.error('创建出库单失败:', error);
      throw error;
    } finally {
      outboundOrderLoading.value = false;
    }
  }

  async function updateOutboundOrder(id: string, data: Partial<OutboundOrder>) {
    try {
      outboundOrderLoading.value = true;
      const response = await outboundOrderApi.update(id, data);
      const index = outboundOrders.value.findIndex(order => order.id === id);
      if (index !== -1) {
        outboundOrders.value[index] = { ...outboundOrders.value[index], ...response.data };
      }
      if (currentOutboundOrder.value && currentOutboundOrder.value.id === id) {
        currentOutboundOrder.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`更新出库单${id}失败:`, error);
      throw error;
    } finally {
      outboundOrderLoading.value = false;
    }
  }

  async function deleteOutboundOrder(id: string) {
    try {
      outboundOrderLoading.value = true;
      await outboundOrderApi.delete(id);
      outboundOrders.value = outboundOrders.value.filter(order => order.id !== id);
      if (currentOutboundOrder.value && currentOutboundOrder.value.id === id) {
        currentOutboundOrder.value = null;
      }
    } catch (error) {
      console.error(`删除出库单${id}失败:`, error);
      throw error;
    } finally {
      outboundOrderLoading.value = false;
    }
  }

  async function approveOutboundOrder(id: string) {
    try {
      outboundOrderLoading.value = true;
      const response = await outboundOrderApi.approve(id);
      const index = outboundOrders.value.findIndex(order => order.id === id);
      if (index !== -1) {
        outboundOrders.value[index] = { ...outboundOrders.value[index], ...response.data };
      }
      if (currentOutboundOrder.value && currentOutboundOrder.value.id === id) {
        currentOutboundOrder.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`审核出库单${id}失败:`, error);
      throw error;
    } finally {
      outboundOrderLoading.value = false;
    }
  }

  async function cancelOutboundOrder(id: string) {
    try {
      outboundOrderLoading.value = true;
      const response = await outboundOrderApi.cancel(id);
      const index = outboundOrders.value.findIndex(order => order.id === id);
      if (index !== -1) {
        outboundOrders.value[index] = { ...outboundOrders.value[index], ...response.data };
      }
      if (currentOutboundOrder.value && currentOutboundOrder.value.id === id) {
        currentOutboundOrder.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`取消出库单${id}失败:`, error);
      throw error;
    } finally {
      outboundOrderLoading.value = false;
    }
  }

  async function completeOutboundOrder(id: string) {
    try {
      outboundOrderLoading.value = true;
      const response = await outboundOrderApi.complete(id);
      const index = outboundOrders.value.findIndex(order => order.id === id);
      if (index !== -1) {
        outboundOrders.value[index] = { ...outboundOrders.value[index], ...response.data };
      }
      if (currentOutboundOrder.value && currentOutboundOrder.value.id === id) {
        currentOutboundOrder.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`完成出库单${id}失败:`, error);
      throw error;
    } finally {
      outboundOrderLoading.value = false;
    }
  }

  // 拣货任务相关操作
  async function fetchPickingTasks(params?: any) {
    try {
      pickingTaskLoading.value = true;
      const response = await pickingTaskApi.getList(params);
      pickingTasks.value = response.data;
      return response.data;
    } catch (error) {
      console.error('获取拣货任务列表失败:', error);
      throw error;
    } finally {
      pickingTaskLoading.value = false;
    }
  }

  async function fetchPickingTaskDetail(id: string) {
    try {
      pickingTaskLoading.value = true;
      const response = await pickingTaskApi.getDetail(id);
      currentPickingTask.value = response.data;
      return response.data;
    } catch (error) {
      console.error(`获取拣货任务${id}详情失败:`, error);
      throw error;
    } finally {
      pickingTaskLoading.value = false;
    }
  }

  async function assignPickingTask(id: string, data: any) {
    try {
      pickingTaskLoading.value = true;
      const response = await pickingTaskApi.assign(id, data);
      const index = pickingTasks.value.findIndex(task => task.id === id);
      if (index !== -1) {
        pickingTasks.value[index] = { ...pickingTasks.value[index], ...response.data };
      }
      if (currentPickingTask.value && currentPickingTask.value.id === id) {
        currentPickingTask.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`分配拣货任务${id}失败:`, error);
      throw error;
    } finally {
      pickingTaskLoading.value = false;
    }
  }

  async function startPickingTask(id: string) {
    try {
      pickingTaskLoading.value = true;
      const response = await pickingTaskApi.start(id);
      const index = pickingTasks.value.findIndex(task => task.id === id);
      if (index !== -1) {
        pickingTasks.value[index] = { ...pickingTasks.value[index], ...response.data };
      }
      if (currentPickingTask.value && currentPickingTask.value.id === id) {
        currentPickingTask.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`开始拣货任务${id}失败:`, error);
      throw error;
    } finally {
      pickingTaskLoading.value = false;
    }
  }

  async function completePickingTask(id: string) {
    try {
      pickingTaskLoading.value = true;
      const response = await pickingTaskApi.complete(id);
      const index = pickingTasks.value.findIndex(task => task.id === id);
      if (index !== -1) {
        pickingTasks.value[index] = { ...pickingTasks.value[index], ...response.data };
      }
      if (currentPickingTask.value && currentPickingTask.value.id === id) {
        currentPickingTask.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`完成拣货任务${id}失败:`, error);
      throw error;
    } finally {
      pickingTaskLoading.value = false;
    }
  }

  async function cancelPickingTask(id: string) {
    try {
      pickingTaskLoading.value = true;
      const response = await pickingTaskApi.cancel(id);
      const index = pickingTasks.value.findIndex(task => task.id === id);
      if (index !== -1) {
        pickingTasks.value[index] = { ...pickingTasks.value[index], ...response.data };
      }
      if (currentPickingTask.value && currentPickingTask.value.id === id) {
        currentPickingTask.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`取消拣货任务${id}失败:`, error);
      throw error;
    } finally {
      pickingTaskLoading.value = false;
    }
  }

  async function pausePickingTask(id: string) {
    try {
      pickingTaskLoading.value = true;
      const response = await pickingTaskApi.pause(id);
      const index = pickingTasks.value.findIndex(task => task.id === id);
      if (index !== -1) {
        pickingTasks.value[index] = { ...pickingTasks.value[index], ...response.data };
      }
      if (currentPickingTask.value && currentPickingTask.value.id === id) {
        currentPickingTask.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`暂停拣货任务${id}失败:`, error);
      throw error;
    } finally {
      pickingTaskLoading.value = false;
    }
  }

  async function resumePickingTask(id: string) {
    try {
      pickingTaskLoading.value = true;
      const response = await pickingTaskApi.resume(id);
      const index = pickingTasks.value.findIndex(task => task.id === id);
      if (index !== -1) {
        pickingTasks.value[index] = { ...pickingTasks.value[index], ...response.data };
      }
      if (currentPickingTask.value && currentPickingTask.value.id === id) {
        currentPickingTask.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`继续拣货任务${id}失败:`, error);
      throw error;
    } finally {
      pickingTaskLoading.value = false;
    }
  }

  async function updatePickingTaskItem(itemId: string, data: any) {
    try {
      pickingTaskLoading.value = true;
      const response = await pickingTaskApi.updateItem(itemId, data);
      // 更新相关拣货任务中的任务项
      for (const task of pickingTasks.value) {
        const itemIndex = task.items.findIndex(item => item.id === itemId);
        if (itemIndex !== -1) {
          task.items[itemIndex] = { ...task.items[itemIndex], ...response.data };
        }
      }
      // 更新当前拣货任务中的任务项
      if (currentPickingTask.value) {
        const itemIndex = currentPickingTask.value.items.findIndex(item => item.id === itemId);
        if (itemIndex !== -1) {
          currentPickingTask.value.items[itemIndex] = { ...currentPickingTask.value.items[itemIndex], ...response.data };
        }
      }
      return response.data;
    } catch (error) {
      console.error(`更新拣货任务项${itemId}失败:`, error);
      throw error;
    } finally {
      pickingTaskLoading.value = false;
    }
  }

  // 重置状态
  function resetState() {
    waves.value = [];
    currentWave.value = null;
    waveLoading.value = false;
    wavePagination.value = {
      page: 1,
      pageSize: 10,
      total: 0
    };

    outboundOrders.value = [];
    currentOutboundOrder.value = null;
    outboundOrderLoading.value = false;
    outboundOrderPagination.value = {
      page: 1,
      pageSize: 10,
      total: 0
    };

    pickingTasks.value = [];
    currentPickingTask.value = null;
    pickingTaskLoading.value = false;
    pickingTaskPagination.value = {
      page: 1,
      pageSize: 10,
      total: 0
    };
  }

  return {
    // 波次相关
    waves,
    currentWave,
    waveLoading,
    wavePagination,

    // 出库单相关
    outboundOrders,
    currentOutboundOrder,
    outboundOrderLoading,
    outboundOrderPagination,

    // 拣货任务相关
    pickingTasks,
    currentPickingTask,
    pickingTaskLoading,
    pickingTaskPagination,

    // 计算属性
    getWaveById,
    getOutboundOrderById,
    getPickingTaskById,
    getOutboundOrdersByWaveId,
    getPickingTasksByWaveId,
    pendingWaves,
    pendingPickingOrders,
    pendingPickingTasks,

    // 波次操作
    fetchWaves,
    fetchWaveDetail,
    createWave,
    autoCreateWave,
    updateWave,
    deleteWave,
    allocateWaveInventory,
    startWavePicking,
    completeWave,
    cancelWave,

    // 出库单操作
    fetchOutboundOrders,
    fetchOutboundOrderDetail,
    createOutboundOrder,
    updateOutboundOrder,
    deleteOutboundOrder,
    approveOutboundOrder,
    cancelOutboundOrder,
    completeOutboundOrder,

    // 拣货任务操作
    fetchPickingTasks,
    fetchPickingTaskDetail,
    assignPickingTask,
    startPickingTask,
    completePickingTask,
    cancelPickingTask,
    pausePickingTask,
    resumePickingTask,
    updatePickingTaskItem,

    // 重置状态
    resetState
  };
});
