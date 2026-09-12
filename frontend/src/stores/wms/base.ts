import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { unwrapListResponse, unwrapResponseData } from '@/api';
import { warehouseApi, warehouseAreaApi, locationApi } from '@/api/wms';
import type { Warehouse, WarehouseArea, Location } from '@/types/wms';

const unwrapApiData = <T = any>(response: any): T => {
  return (unwrapResponseData<T>(response) ?? null) as T;
};

const unwrapApiList = <T = any>(response: any): T[] => {
  return unwrapListResponse<T>(response);
};

export const useWmsBaseStore = defineStore('wms-base', () => {
  // 仓库相关状态
  const warehouses = ref<Warehouse[]>([]);
  const currentWarehouse = ref<Warehouse | null>(null);
  const warehouseLoading = ref(false);
  const warehousePagination = ref({
    page: 1,
    pageSize: 10,
    total: 0
  });

  // 库区相关状态
  const warehouseAreas = ref<WarehouseArea[]>([]);
  const currentWarehouseArea = ref<WarehouseArea | null>(null);
  const warehouseAreaLoading = ref(false);
  const warehouseAreaPagination = ref({
    page: 1,
    pageSize: 10,
    total: 0
  });

  // 库位相关状态
  const locations = ref<Location[]>([]);
  const currentLocation = ref<Location | null>(null);
  const locationLoading = ref(false);
  const locationPagination = ref({
    page: 1,
    pageSize: 10,
    total: 0
  });

  // 按ID获取仓库
  const getWarehouseById = computed(() => (id: string) => {
    return warehouses.value.find(warehouse => warehouse.id === id) || null;
  });

  // 按ID获取库区
  const getWarehouseAreaById = computed(() => (id: string) => {
    return warehouseAreas.value.find(area => area.id === id) || null;
  });

  // 按ID获取库位
  const getLocationById = computed(() => (id: string) => {
    return locations.value.find(location => location.id === id) || null;
  });

  // 按仓库ID获取库区列表
  const getWarehouseAreasByWarehouseId = computed(() => (warehouseId: string) => {
    return warehouseAreas.value.filter(area => area.warehouseId === warehouseId);
  });

  // 按库区ID获取库位列表
  const getLocationsByAreaId = computed(() => (areaId: string) => {
    return locations.value.filter(location => location.areaId === areaId);
  });

  // 按仓库ID获取库位列表
  const getLocationsByWarehouseId = computed(() => (warehouseId: string) => {
    return locations.value.filter(location => location.warehouseId === warehouseId);
  });

  // 可用库位列表
  const availableLocations = computed(() => {
    return locations.value.filter(location => location.status === 'available');
  });

  // 仓库相关操作
  async function fetchWarehouses(params?: any) {
    try {
      warehouseLoading.value = true;
      const response = await warehouseApi.getList(params);
      const rows = unwrapApiList<Warehouse>(response);
      warehouses.value = rows;
      return rows;
    } catch (error) {
      console.error('获取仓库列表失败:', error);
      throw error;
    } finally {
      warehouseLoading.value = false;
    }
  }

  async function fetchWarehouseDetail(id: string) {
    try {
      warehouseLoading.value = true;
      const response = await warehouseApi.getDetail(id);
      const row = unwrapApiData<Warehouse>(response);
      currentWarehouse.value = row;
      return row;
    } catch (error) {
      console.error(`获取仓库${id}详情失败:`, error);
      throw error;
    } finally {
      warehouseLoading.value = false;
    }
  }

  async function createWarehouse(data: Warehouse) {
    try {
      warehouseLoading.value = true;
      const response = await warehouseApi.create(data);
      const row = unwrapApiData<Warehouse>(response);
      warehouses.value.push(row);
      return row;
    } catch (error) {
      console.error('创建仓库失败:', error);
      throw error;
    } finally {
      warehouseLoading.value = false;
    }
  }

  async function updateWarehouse(id: string, data: Partial<Warehouse>) {
    try {
      warehouseLoading.value = true;
      const response = await warehouseApi.update(id, data);
      const row = unwrapApiData<Warehouse>(response);
      const index = warehouses.value.findIndex(warehouse => warehouse.id === id);
      if (index !== -1) {
        warehouses.value[index] = { ...warehouses.value[index], ...row };
      }
      if (currentWarehouse.value && currentWarehouse.value.id === id) {
        currentWarehouse.value = row;
      }
      return row;
    } catch (error) {
      console.error(`更新仓库${id}失败:`, error);
      throw error;
    } finally {
      warehouseLoading.value = false;
    }
  }

  async function deleteWarehouse(id: string) {
    try {
      warehouseLoading.value = true;
      await warehouseApi.delete(id);
      warehouses.value = warehouses.value.filter(warehouse => warehouse.id !== id);
      if (currentWarehouse.value && currentWarehouse.value.id === id) {
        currentWarehouse.value = null;
      }
    } catch (error) {
      console.error(`删除仓库${id}失败:`, error);
      throw error;
    } finally {
      warehouseLoading.value = false;
    }
  }

  // 库区相关操作
  async function fetchWarehouseAreas(params?: any) {
    try {
      warehouseAreaLoading.value = true;
      const response = await warehouseAreaApi.getList(params);
      const rows = unwrapApiList<WarehouseArea>(response);
      warehouseAreas.value = rows;
      return rows;
    } catch (error) {
      console.error('获取库区列表失败:', error);
      throw error;
    } finally {
      warehouseAreaLoading.value = false;
    }
  }

  async function fetchWarehouseAreaDetail(id: string) {
    try {
      warehouseAreaLoading.value = true;
      const response = await warehouseAreaApi.getDetail(id);
      const row = unwrapApiData<WarehouseArea>(response);
      currentWarehouseArea.value = row;
      return row;
    } catch (error) {
      console.error(`获取库区${id}详情失败:`, error);
      throw error;
    } finally {
      warehouseAreaLoading.value = false;
    }
  }

  async function createWarehouseArea(data: WarehouseArea) {
    try {
      warehouseAreaLoading.value = true;
      const response = await warehouseAreaApi.create(data);
      const row = unwrapApiData<WarehouseArea>(response);
      warehouseAreas.value.push(row);
      return row;
    } catch (error) {
      console.error('创建库区失败:', error);
      throw error;
    } finally {
      warehouseAreaLoading.value = false;
    }
  }

  async function updateWarehouseArea(id: string, data: Partial<WarehouseArea>) {
    try {
      warehouseAreaLoading.value = true;
      const response = await warehouseAreaApi.update(id, data);
      const row = unwrapApiData<WarehouseArea>(response);
      const index = warehouseAreas.value.findIndex(area => area.id === id);
      if (index !== -1) {
        warehouseAreas.value[index] = { ...warehouseAreas.value[index], ...row };
      }
      if (currentWarehouseArea.value && currentWarehouseArea.value.id === id) {
        currentWarehouseArea.value = row;
      }
      return row;
    } catch (error) {
      console.error(`更新库区${id}失败:`, error);
      throw error;
    } finally {
      warehouseAreaLoading.value = false;
    }
  }

  async function deleteWarehouseArea(id: string) {
    try {
      warehouseAreaLoading.value = true;
      await warehouseAreaApi.delete(id);
      warehouseAreas.value = warehouseAreas.value.filter(area => area.id !== id);
      if (currentWarehouseArea.value && currentWarehouseArea.value.id === id) {
        currentWarehouseArea.value = null;
      }
    } catch (error) {
      console.error(`删除库区${id}失败:`, error);
      throw error;
    } finally {
      warehouseAreaLoading.value = false;
    }
  }

  // 库位相关操作
  async function fetchLocations(params?: any) {
    try {
      locationLoading.value = true;
      const response = await locationApi.getList(params);
      const rows = unwrapApiList<Location>(response);
      locations.value = rows;
      return rows;
    } catch (error) {
      console.error('获取库位列表失败:', error);
      throw error;
    } finally {
      locationLoading.value = false;
    }
  }

  async function fetchLocationDetail(id: string) {
    try {
      locationLoading.value = true;
      const response = await locationApi.getDetail(id);
      const row = unwrapApiData<Location>(response);
      currentLocation.value = row;
      return row;
    } catch (error) {
      console.error(`获取库位${id}详情失败:`, error);
      throw error;
    } finally {
      locationLoading.value = false;
    }
  }

  async function createLocation(data: Location) {
    try {
      locationLoading.value = true;
      const response = await locationApi.create(data);
      const row = unwrapApiData<Location>(response);
      locations.value.push(row);
      return row;
    } catch (error) {
      console.error('创建库位失败:', error);
      throw error;
    } finally {
      locationLoading.value = false;
    }
  }

  async function batchCreateLocations(data: Location[]) {
    try {
      locationLoading.value = true;
      const response = await locationApi.batchCreate(data);
      const rows = unwrapApiList<Location>(response);
      locations.value.push(...rows);
      return rows;
    } catch (error) {
      console.error('批量创建库位失败:', error);
      throw error;
    } finally {
      locationLoading.value = false;
    }
  }

  async function updateLocation(id: string, data: Partial<Location>) {
    try {
      locationLoading.value = true;
      const response = await locationApi.update(id, data);
      const row = unwrapApiData<Location>(response);
      const index = locations.value.findIndex(location => location.id === id);
      if (index !== -1) {
        locations.value[index] = { ...locations.value[index], ...row };
      }
      if (currentLocation.value && currentLocation.value.id === id) {
        currentLocation.value = row;
      }
      return row;
    } catch (error) {
      console.error(`更新库位${id}失败:`, error);
      throw error;
    } finally {
      locationLoading.value = false;
    }
  }

  async function deleteLocation(id: string) {
    try {
      locationLoading.value = true;
      await locationApi.delete(id);
      locations.value = locations.value.filter(location => location.id !== id);
      if (currentLocation.value && currentLocation.value.id === id) {
        currentLocation.value = null;
      }
    } catch (error) {
      console.error(`删除库位${id}失败:`, error);
      throw error;
    } finally {
      locationLoading.value = false;
    }
  }

  // 库位码生成
  async function generateLocationCodes(params?: any) {
    try {
      locationLoading.value = true;
      const response = await locationApi.generateCodes(params);
      return response;
    } catch (error) {
      console.error('生成库位码失败:', error);
      throw error;
    } finally {
      locationLoading.value = false;
    }
  }

  // 重置状态
  function resetState() {
    warehouses.value = [];
    currentWarehouse.value = null;
    warehouseLoading.value = false;
    warehousePagination.value = {
      page: 1,
      pageSize: 10,
      total: 0
    };

    warehouseAreas.value = [];
    currentWarehouseArea.value = null;
    warehouseAreaLoading.value = false;
    warehouseAreaPagination.value = {
      page: 1,
      pageSize: 10,
      total: 0
    };

    locations.value = [];
    currentLocation.value = null;
    locationLoading.value = false;
    locationPagination.value = {
      page: 1,
      pageSize: 10,
      total: 0
    };
  }

  return {
    // 仓库相关
    warehouses,
    currentWarehouse,
    warehouseLoading,
    warehousePagination,

    // 库区相关
    warehouseAreas,
    currentWarehouseArea,
    warehouseAreaLoading,
    warehouseAreaPagination,

    // 库位相关
    locations,
    currentLocation,
    locationLoading,
    locationPagination,

    // 计算属性
    getWarehouseById,
    getWarehouseAreaById,
    getLocationById,
    getWarehouseAreasByWarehouseId,
    getLocationsByAreaId,
    getLocationsByWarehouseId,
    availableLocations,

    // 仓库操作
    fetchWarehouses,
    fetchWarehouseDetail,
    createWarehouse,
    updateWarehouse,
    deleteWarehouse,

    // 库区操作
    fetchWarehouseAreas,
    fetchWarehouseAreaDetail,
    createWarehouseArea,
    updateWarehouseArea,
    deleteWarehouseArea,

    // 库位操作
    fetchLocations,
    fetchLocationDetail,
    createLocation,
    batchCreateLocations,
    updateLocation,
    deleteLocation,
    generateLocationCodes,

    // 重置状态
    resetState
  };
});
