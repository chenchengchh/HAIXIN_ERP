import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { unwrapListResponse, unwrapResponseData } from '@/api';
import { asnApi, receivingOrderApi, putawayTaskApi } from '@/api/wms';
import type { ASN, ReceivingOrder, PutawayTask } from '@/types/wms';

const unwrapApiData = <T = any>(response: any): T => {
  return (unwrapResponseData<T>(response) ?? null) as T;
};

const unwrapApiList = <T = any>(response: any): T[] => {
  return unwrapListResponse<T>(response);
};

export const useWmsInboundStore = defineStore('wms-inbound', () => {
  // ASN相关状态
  const asns = ref<ASN[]>([]);
  const currentAsn = ref<ASN | null>(null);
  const asnLoading = ref(false);
  const asnPagination = ref({
    page: 1,
    pageSize: 10,
    total: 0
  });

  // 收货单相关状态
  const receivingOrders = ref<ReceivingOrder[]>([]);
  const currentReceivingOrder = ref<ReceivingOrder | null>(null);
  const receivingOrderLoading = ref(false);
  const receivingOrderPagination = ref({
    page: 1,
    pageSize: 10,
    total: 0
  });

  // 上架任务相关状态
  const putawayTasks = ref<PutawayTask[]>([]);
  const currentPutawayTask = ref<PutawayTask | null>(null);
  const putawayTaskLoading = ref(false);
  const putawayTaskPagination = ref({
    page: 1,
    pageSize: 10,
    total: 0
  });

  // 按ID获取ASN
  const getAsnById = computed(() => (id: string) => {
    return asns.value.find(asn => asn.id === id) || null;
  });

  // 按ID获取收货单
  const getReceivingOrderById = computed(() => (id: string) => {
    return receivingOrders.value.find(order => order.id === id) || null;
  });

  // 按ID获取上架任务
  const getPutawayTaskById = computed(() => (id: string) => {
    return putawayTasks.value.find(task => task.id === id) || null;
  });

  // 按ASN ID获取收货单
  const getReceivingOrdersByAsnId = computed(() => (asnId: string) => {
    return receivingOrders.value.filter(order => order.asnId === asnId);
  });

  // 按收货单ID获取上架任务
  const getPutawayTasksByReceivingOrderId = computed(() => (receivingOrderId: string) => {
    return putawayTasks.value.filter(task => task.receivingOrderId === receivingOrderId);
  });

  // 待收货的ASN列表
  const pendingReceivingAsns = computed(() => {
    return asns.value.filter(asn => asn.status === 'created' || asn.status === 'partially_received');
  });

  // 待上架的收货单列表
  const pendingPutawayReceivingOrders = computed(() => {
    return receivingOrders.value.filter(order => order.status === 'fully_received');
  });

  // 待处理的上架任务列表
  const pendingPutawayTasks = computed(() => {
    return putawayTasks.value.filter(task => task.status === 'pending' || task.status === 'assigned' || task.status === 'working');
  });

  // ASN相关操作
  async function fetchAsns(params?: any) {
    try {
      asnLoading.value = true;
      const response = await asnApi.getList(params);
      const rows = unwrapApiList<ASN>(response);
      asns.value = rows;
      return rows;
    } catch (error) {
      console.error('获取ASN列表失败:', error);
      throw error;
    } finally {
      asnLoading.value = false;
    }
  }

  async function fetchAsnDetail(id: string) {
    try {
      asnLoading.value = true;
      const response = await asnApi.getDetail(id);
      const row = unwrapApiData<ASN>(response);
      currentAsn.value = row;
      return row;
    } catch (error) {
      console.error(`获取ASN${id}详情失败:`, error);
      throw error;
    } finally {
      asnLoading.value = false;
    }
  }

  async function createAsn(data: ASN) {
    try {
      asnLoading.value = true;
      const response = await asnApi.create(data);
      const row = unwrapApiData<ASN>(response);
      asns.value.push(row);
      return row;
    } catch (error) {
      console.error('创建ASN失败:', error);
      throw error;
    } finally {
      asnLoading.value = false;
    }
  }

  async function updateAsn(id: string, data: Partial<ASN>) {
    try {
      asnLoading.value = true;
      const response = await asnApi.update(id, data);
      const row = unwrapApiData<ASN>(response);
      const index = asns.value.findIndex(asn => asn.id === id);
      if (index !== -1) {
        asns.value[index] = { ...asns.value[index], ...row };
      }
      if (currentAsn.value && currentAsn.value.id === id) {
        currentAsn.value = row;
      }
      return row;
    } catch (error) {
      console.error(`更新ASN${id}失败:`, error);
      throw error;
    } finally {
      asnLoading.value = false;
    }
  }

  async function deleteAsn(id: string) {
    try {
      asnLoading.value = true;
      await asnApi.delete(id);
      asns.value = asns.value.filter(asn => asn.id !== id);
      if (currentAsn.value && currentAsn.value.id === id) {
        currentAsn.value = null;
      }
    } catch (error) {
      console.error(`删除ASN${id}失败:`, error);
      throw error;
    } finally {
      asnLoading.value = false;
    }
  }

  async function confirmAsnArrival(id: string, data: any) {
    try {
      asnLoading.value = true;
      const response = await asnApi.confirmArrival(id, data);
      const row = unwrapApiData<ASN>(response);
      const index = asns.value.findIndex(asn => asn.id === id);
      if (index !== -1) {
        asns.value[index] = { ...asns.value[index], ...row };
      }
      if (currentAsn.value && currentAsn.value.id === id) {
        currentAsn.value = row;
      }
      return row;
    } catch (error) {
      console.error(`确认ASN${id}到货失败:`, error);
      throw error;
    } finally {
      asnLoading.value = false;
    }
  }

  // 收货单相关操作
  async function fetchReceivingOrders(params?: any) {
    try {
      receivingOrderLoading.value = true;
      const response = await receivingOrderApi.getList(params);
      const rows = unwrapApiList<ReceivingOrder>(response);
      receivingOrders.value = rows;
      return rows;
    } catch (error) {
      console.error('获取收货单列表失败:', error);
      throw error;
    } finally {
      receivingOrderLoading.value = false;
    }
  }

  async function fetchReceivingOrderDetail(id: string) {
    try {
      receivingOrderLoading.value = true;
      const response = await receivingOrderApi.getDetail(id);
      const row = unwrapApiData<ReceivingOrder>(response);
      currentReceivingOrder.value = row;
      return row;
    } catch (error) {
      console.error(`获取收货单${id}详情失败:`, error);
      throw error;
    } finally {
      receivingOrderLoading.value = false;
    }
  }

  async function createReceivingOrder(data: ReceivingOrder) {
    try {
      receivingOrderLoading.value = true;
      const response = await receivingOrderApi.create(data);
      const row = unwrapApiData<ReceivingOrder>(response);
      receivingOrders.value.push(row);
      return row;
    } catch (error) {
      console.error('创建收货单失败:', error);
      throw error;
    } finally {
      receivingOrderLoading.value = false;
    }
  }

  async function updateReceivingOrder(id: string, data: Partial<ReceivingOrder>) {
    try {
      receivingOrderLoading.value = true;
      const response = await receivingOrderApi.update(id, data);
      const row = unwrapApiData<ReceivingOrder>(response);
      const index = receivingOrders.value.findIndex(order => order.id === id);
      if (index !== -1) {
        receivingOrders.value[index] = { ...receivingOrders.value[index], ...row };
      }
      if (currentReceivingOrder.value && currentReceivingOrder.value.id === id) {
        currentReceivingOrder.value = row;
      }
      return row;
    } catch (error) {
      console.error(`更新收货单${id}失败:`, error);
      throw error;
    } finally {
      receivingOrderLoading.value = false;
    }
  }

  async function deleteReceivingOrder(id: string) {
    try {
      receivingOrderLoading.value = true;
      await receivingOrderApi.delete(id);
      receivingOrders.value = receivingOrders.value.filter(order => order.id !== id);
      if (currentReceivingOrder.value && currentReceivingOrder.value.id === id) {
        currentReceivingOrder.value = null;
      }
    } catch (error) {
      console.error(`删除收货单${id}失败:`, error);
      throw error;
    } finally {
      receivingOrderLoading.value = false;
    }
  }

  async function completeReceivingOrder(id: string) {
    try {
      receivingOrderLoading.value = true;
      const response = await receivingOrderApi.complete(id);
      const index = receivingOrders.value.findIndex(order => order.id === id);
      if (index !== -1) {
        receivingOrders.value[index] = { ...receivingOrders.value[index], ...response.data };
      }
      if (currentReceivingOrder.value && currentReceivingOrder.value.id === id) {
        currentReceivingOrder.value = response.data;
      }
      return response.data;
    } catch (error) {
      console.error(`完成收货单${id}失败:`, error);
      throw error;
    } finally {
      receivingOrderLoading.value = false;
    }
  }

  async function generatePutawayTaskFromReceivingOrder(id: string) {
    try {
      receivingOrderLoading.value = true;
      const response = await receivingOrderApi.generatePutawayTask(id);
      return unwrapApiData<any>(response);
    } catch (error) {
      console.error(`从收货单${id}生成上架任务失败:`, error);
      throw error;
    } finally {
      receivingOrderLoading.value = false;
    }
  }

  // 上架任务相关操作
  async function fetchPutawayTasks(params?: any) {
    try {
      putawayTaskLoading.value = true;
      const response = await putawayTaskApi.getList(params);
      const rows = unwrapApiList<PutawayTask>(response);
      putawayTasks.value = rows;
      return rows;
    } catch (error) {
      console.error('获取上架任务列表失败:', error);
      throw error;
    } finally {
      putawayTaskLoading.value = false;
    }
  }

  async function fetchPutawayTaskDetail(id: string) {
    try {
      putawayTaskLoading.value = true;
      const response = await putawayTaskApi.getDetail(id);
      const row = unwrapApiData<PutawayTask>(response);
      currentPutawayTask.value = row;
      return row;
    } catch (error) {
      console.error(`获取上架任务${id}详情失败:`, error);
      throw error;
    } finally {
      putawayTaskLoading.value = false;
    }
  }

  async function assignPutawayTask(id: string, data: any) {
    try {
      putawayTaskLoading.value = true;
      const response = await putawayTaskApi.assign(id, data);
      const row = unwrapApiData<PutawayTask>(response);
      const index = putawayTasks.value.findIndex(task => task.id === id);
      if (index !== -1) {
        putawayTasks.value[index] = { ...putawayTasks.value[index], ...row };
      }
      if (currentPutawayTask.value && currentPutawayTask.value.id === id) {
        currentPutawayTask.value = row;
      }
      return row;
    } catch (error) {
      console.error(`分配上架任务${id}失败:`, error);
      throw error;
    } finally {
      putawayTaskLoading.value = false;
    }
  }

  async function startPutawayTask(id: string) {
    try {
      putawayTaskLoading.value = true;
      const response = await putawayTaskApi.start(id);
      const row = unwrapApiData<PutawayTask>(response);
      const index = putawayTasks.value.findIndex(task => task.id === id);
      if (index !== -1) {
        putawayTasks.value[index] = { ...putawayTasks.value[index], ...row };
      }
      if (currentPutawayTask.value && currentPutawayTask.value.id === id) {
        currentPutawayTask.value = row;
      }
      return row;
    } catch (error) {
      console.error(`开始上架任务${id}失败:`, error);
      throw error;
    } finally {
      putawayTaskLoading.value = false;
    }
  }

  async function completePutawayTask(id: string) {
    try {
      putawayTaskLoading.value = true;
      const response = await putawayTaskApi.complete(id);
      const row = unwrapApiData<PutawayTask>(response);
      const index = putawayTasks.value.findIndex(task => task.id === id);
      if (index !== -1) {
        putawayTasks.value[index] = { ...putawayTasks.value[index], ...row };
      }
      if (currentPutawayTask.value && currentPutawayTask.value.id === id) {
        currentPutawayTask.value = row;
      }
      return row;
    } catch (error) {
      console.error(`完成上架任务${id}失败:`, error);
      throw error;
    } finally {
      putawayTaskLoading.value = false;
    }
  }

  async function cancelPutawayTask(id: string) {
    try {
      putawayTaskLoading.value = true;
      const response = await putawayTaskApi.cancel(id);
      const row = unwrapApiData<PutawayTask>(response);
      const index = putawayTasks.value.findIndex(task => task.id === id);
      if (index !== -1) {
        putawayTasks.value[index] = { ...putawayTasks.value[index], ...row };
      }
      if (currentPutawayTask.value && currentPutawayTask.value.id === id) {
        currentPutawayTask.value = row;
      }
      return row;
    } catch (error) {
      console.error(`取消上架任务${id}失败:`, error);
      throw error;
    } finally {
      putawayTaskLoading.value = false;
    }
  }

  async function pausePutawayTask(id: string) {
    try {
      putawayTaskLoading.value = true;
      const response = await putawayTaskApi.pause(id);
      const row = unwrapApiData<PutawayTask>(response);
      const index = putawayTasks.value.findIndex(task => task.id === id);
      if (index !== -1) {
        putawayTasks.value[index] = { ...putawayTasks.value[index], ...row };
      }
      if (currentPutawayTask.value && currentPutawayTask.value.id === id) {
        currentPutawayTask.value = row;
      }
      return row;
    } catch (error) {
      console.error(`暂停上架任务${id}失败:`, error);
      throw error;
    } finally {
      putawayTaskLoading.value = false;
    }
  }

  async function resumePutawayTask(id: string) {
    try {
      putawayTaskLoading.value = true;
      const response = await putawayTaskApi.resume(id);
      const row = unwrapApiData<PutawayTask>(response);
      const index = putawayTasks.value.findIndex(task => task.id === id);
      if (index !== -1) {
        putawayTasks.value[index] = { ...putawayTasks.value[index], ...row };
      }
      if (currentPutawayTask.value && currentPutawayTask.value.id === id) {
        currentPutawayTask.value = row;
      }
      return row;
    } catch (error) {
      console.error(`继续上架任务${id}失败:`, error);
      throw error;
    } finally {
      putawayTaskLoading.value = false;
    }
  }

  async function updatePutawayTaskItem(itemId: string, data: any) {
    try {
      putawayTaskLoading.value = true;
      const response = await putawayTaskApi.updateItem(itemId, data);
      const row = unwrapApiData<any>(response);
      // 更新相关上架任务中的任务项
      for (const task of putawayTasks.value) {
        const itemIndex = task.items.findIndex(item => item.id === itemId);
        if (itemIndex !== -1) {
          task.items[itemIndex] = { ...task.items[itemIndex], ...row };
        }
      }
      // 更新当前上架任务中的任务项
      if (currentPutawayTask.value) {
        const itemIndex = currentPutawayTask.value.items.findIndex(item => item.id === itemId);
        if (itemIndex !== -1) {
          currentPutawayTask.value.items[itemIndex] = { ...currentPutawayTask.value.items[itemIndex], ...row };
        }
      }
      return row;
    } catch (error) {
      console.error(`更新上架任务项${itemId}失败:`, error);
      throw error;
    } finally {
      putawayTaskLoading.value = false;
    }
  }

  // 重置状态
  function resetState() {
    asns.value = [];
    currentAsn.value = null;
    asnLoading.value = false;
    asnPagination.value = {
      page: 1,
      pageSize: 10,
      total: 0
    };

    receivingOrders.value = [];
    currentReceivingOrder.value = null;
    receivingOrderLoading.value = false;
    receivingOrderPagination.value = {
      page: 1,
      pageSize: 10,
      total: 0
    };

    putawayTasks.value = [];
    currentPutawayTask.value = null;
    putawayTaskLoading.value = false;
    putawayTaskPagination.value = {
      page: 1,
      pageSize: 10,
      total: 0
    };
  }

  return {
    // ASN相关
    asns,
    currentAsn,
    asnLoading,
    asnPagination,

    // 收货单相关
    receivingOrders,
    currentReceivingOrder,
    receivingOrderLoading,
    receivingOrderPagination,

    // 上架任务相关
    putawayTasks,
    currentPutawayTask,
    putawayTaskLoading,
    putawayTaskPagination,

    // 计算属性
    getAsnById,
    getReceivingOrderById,
    getPutawayTaskById,
    getReceivingOrdersByAsnId,
    getPutawayTasksByReceivingOrderId,
    pendingReceivingAsns,
    pendingPutawayReceivingOrders,
    pendingPutawayTasks,

    // ASN操作
    fetchAsns,
    fetchAsnDetail,
    createAsn,
    updateAsn,
    deleteAsn,
    confirmAsnArrival,

    // 收货单操作
    fetchReceivingOrders,
    fetchReceivingOrderDetail,
    createReceivingOrder,
    updateReceivingOrder,
    deleteReceivingOrder,
    completeReceivingOrder,
    generatePutawayTaskFromReceivingOrder,

    // 上架任务操作
    fetchPutawayTasks,
    fetchPutawayTaskDetail,
    assignPutawayTask,
    startPutawayTask,
    completePutawayTask,
    cancelPutawayTask,
    pausePutawayTask,
    resumePutawayTask,
    updatePutawayTaskItem,

    // 重置状态
    resetState
  };
});
