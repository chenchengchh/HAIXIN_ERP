// 仓库基础数据类型
export interface Warehouse {
  id: string;
  code: string;
  name: string;
  address: string;
  contact: string;
  phone: string;
  area: number;
  status: 'active' | 'inactive' | 'maintenance';
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface WarehouseArea {
  id: string;
  code: string;
  name: string;
  warehouseId: string;
  type: 'storage' | 'picking' | 'receiving' | 'shipping' | 'sorting' | 'maintenance';
  status: 'active' | 'inactive' | 'maintenance';
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface Location {
  id: string;
  code: string;
  warehouseId: string;
  areaId: string;
  type: 'storage' | 'picking' | 'temporary';
  status: 'available' | 'locked' | 'disabled';
  capacity: number;
  currentStock: number;
  coordinates: {
    x: number;
    y: number;
    z: number;
  };
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface Material {
  id: string;
  code: string;
  name: string;
  specification: string;
  unit: string;
  category: string;
  status: 'active' | 'inactive';
  createdAt: string;
  updatedAt: string;
}

// 入库作业类型
export interface ASN {
  id: string;
  asnNo: string;
  poNo: string;
  supplierId: string;
  supplierName: string;
  warehouseId: string;
  warehouseName: string;
  expectedArrivalTime: string;
  actualArrivalTime: string | null;
  status: 'created' | 'partially_received' | 'fully_received' | 'cancelled';
  totalQuantity: number;
  receivedQuantity: number;
  createdAt: string;
  updatedAt: string;
  items: ASNItem[];
}

export interface ASNItem {
  id: string;
  asnId: string;
  materialId: string;
  materialCode: string;
  materialName: string;
  specification: string;
  unit: string;
  expectedQuantity: number;
  receivedQuantity: number;
  status: 'expected' | 'partially_received' | 'fully_received';
  createdAt: string;
  updatedAt: string;
}

export interface ReceivingOrder {
  id: string;
  receivingNo: string;
  asnId: string;
  asnNo: string;
  poNo: string;
  supplierId: string;
  supplierName: string;
  warehouseId: string;
  warehouseName: string;
  operatorId: string;
  operatorName: string;
  receiveTime: string;
  status: 'created' | 'partially_received' | 'fully_received' | 'cancelled';
  totalQuantity: number;
  receivedQuantity: number;
  createdAt: string;
  updatedAt: string;
  items: ReceivingOrderItem[];
}

export interface ReceivingOrderItem {
  id: string;
  receivingOrderId: string;
  asnItemId: string;
  materialId: string;
  materialCode: string;
  materialName: string;
  specification: string;
  unit: string;
  expectedQuantity: number;
  receivedQuantity: number;
  actualQuantity: number;
  qualityStatus: 'good' | 'bad' | 'pending';
  batchNo: string;
  expiryDate: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface PutawayTask {
  id: string;
  taskNo: string;
  receivingOrderId: string;
  receivingNo: string;
  status: 'pending' | 'assigned' | 'working' | 'done' | 'cancelled' | 'paused';
  operatorId: string | null;
  operatorName: string | null;
  assignedTime: string | null;
  startTime: string | null;
  endTime: string | null;
  createdAt: string;
  updatedAt: string;
  items: PutawayTaskItem[];
}

export interface PutawayTaskItem {
  id: string;
  taskId: string;
  receivingOrderItemId: string;
  materialId: string;
  materialCode: string;
  materialName: string;
  specification: string;
  unit: string;
  quantity: number;
  sourceLocation: string | null;
  targetLocation: string | null;
  batchNo: string;
  expiryDate: string | null;
  status: 'pending' | 'done';
  createdAt: string;
  updatedAt: string;
}

// 出库作业类型
export interface Wave {
  id: string;
  waveNo: string;
  warehouseId: string;
  warehouseName: string;
  waveType: 'single_order' | 'batch_order' | 'priority_order';
  status: 'created' | 'allocated' | 'picking' | 'picked' | 'cancelled';
  totalOrders: number;
  totalItems: number;
  totalQuantity: number;
  allocatedQuantity: number;
  pickedQuantity: number;
  createdAt: string;
  updatedAt: string;
  allocatedTime: string | null;
  pickingStartTime: string | null;
  pickingEndTime: string | null;
  orders: OutboundOrder[];
}

export interface OutboundOrder {
  id: string;
  orderNo: string;
  soNo: string;
  customerId: string;
  customerName: string;
  warehouseId: string;
  warehouseName: string;
  status: 'created' | 'allocated' | 'picking' | 'reviewed' | 'shipped' | 'cancelled';
  totalItems: number;
  totalQuantity: number;
  allocatedQuantity: number;
  pickedQuantity: number;
  reviewedQuantity: number;
  shippedQuantity: number;
  createdAt: string;
  updatedAt: string;
  orderType: 'sales' | 'return' | 'transfer' | 'sample';
  items: OutboundOrderItem[];
}

export interface OutboundOrderItem {
  id: string;
  orderId: string;
  materialId: string;
  materialCode: string;
  materialName: string;
  specification: string;
  unit: string;
  orderedQuantity: number;
  allocatedQuantity: number;
  pickedQuantity: number;
  reviewedQuantity: number;
  shippedQuantity: number;
  status: 'ordered' | 'allocated' | 'picked' | 'reviewed' | 'shipped';
  createdAt: string;
  updatedAt: string;
}

export interface PickingTask {
  id: string;
  taskNo: string;
  waveId: string;
  waveNo: string;
  status: 'pending' | 'assigned' | 'working' | 'done' | 'cancelled' | 'paused';
  operatorId: string | null;
  operatorName: string | null;
  assignedTime: string | null;
  startTime: string | null;
  endTime: string | null;
  createdAt: string;
  updatedAt: string;
  items: PickingTaskItem[];
}

export interface PickingTaskItem {
  id: string;
  taskId: string;
  orderItemId: string;
  materialId: string;
  materialCode: string;
  materialName: string;
  specification: string;
  unit: string;
  quantity: number;
  sourceLocation: string;
  targetLocation: string;
  batchNo: string;
  expiryDate: string | null;
  status: 'pending' | 'done';
  createdAt: string;
  updatedAt: string;
}

// 库存管理类型
export interface Inventory {
  id: string;
  materialId: string;
  materialCode: string;
  materialName: string;
  specification: string;
  unit: string;
  warehouseId: string;
  warehouseName: string;
  locationId: string;
  locationCode: string;
  batchNo: string;
  expiryDate: string | null;
  quantity: number;
  allocatedQuantity: number;
  availableQuantity: number;
  status: 'good' | 'bad' | 'inspection';
  createdAt: string;
  updatedAt: string;
}

export interface InventoryCount {
  id: string;
  countNo: string;
  warehouseId: string;
  warehouseName: string;
  countType: '明盘' | '暗盘';
  status: 'created' | 'in_progress' | 'completed' | 'cancelled';
  startTime: string | null;
  endTime: string | null;
  operatorId: string;
  operatorName: string;
  createdAt: string;
  updatedAt: string;
  items: InventoryCountItem[];
}

export interface InventoryCountItem {
  id: string;
  countId: string;
  inventoryId: string;
  materialId: string;
  materialCode: string;
  materialName: string;
  specification: string;
  unit: string;
  locationId: string;
  locationCode: string;
  batchNo: string;
  expiryDate: string | null;
  systemQuantity: number;
  actualQuantity: number;
  difference: number;
  status: 'pending' | 'counted' | 'adjusted';
  adjustedBy: string | null;
  adjustedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface InventoryMove {
  id: string;
  moveNo: string;
  warehouseId: string;
  warehouseName: string;
  status: 'created' | 'in_progress' | 'completed' | 'cancelled';
  operatorId: string;
  operatorName: string;
  createdAt: string;
  updatedAt: string;
  items: InventoryMoveItem[];
}

export interface InventoryMoveItem {
  id: string;
  moveId: string;
  inventoryId: string;
  materialId: string;
  materialCode: string;
  materialName: string;
  specification: string;
  unit: string;
  quantity: number;
  sourceLocationId: string;
  sourceLocationCode: string;
  targetLocationId: string;
  targetLocationCode: string;
  batchNo: string;
  expiryDate: string | null;
  status: 'pending' | 'moved';
  createdAt: string;
  updatedAt: string;
}

// PDA作业类型
export interface PDATask {
  id: string;
  taskNo: string;
  taskType: 'putaway' | 'picking' | 'move' | 'count';
  status: 'pending' | 'working' | 'done' | 'cancelled' | 'paused';
  operatorId: string;
  operatorName: string;
  startTime: string | null;
  endTime: string | null;
  createdAt: string;
  updatedAt: string;
  taskData: any;
}

// 作业策略类型
export interface PutawayStrategy {
  id: string;
  name: string;
  code: string;
  type: 'idle_first' | 'same_category' | 'nearby' | 'capacity_first';
  description: string;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PickingStrategy {
  id: string;
  name: string;
  code: string;
  type: 'fifo' | 'lifo' | 'path_optimization' | 'batch_specified';
  description: string;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface InventoryAllocationStrategy {
  id: string;
  name: string;
  code: string;
  type: 'fifo' | 'lifo' | 'min_location' | 'max_location';
  description: string;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

// 统计分析类型
export interface InventoryAnalysis {
  warehouseId: string;
  warehouseName: string;
  totalInventoryValue: number;
  totalInventoryQuantity: number;
  totalLocationCount: number;
  usedLocationCount: number;
  locationUtilization: number;
  inventoryTurnoverRate: number;
  abcAnalysis: {
    category: 'A' | 'B' | 'C';
    materialCount: number;
    quantity: number;
    value: number;
    percentage: number;
  }[];
  expiryAnalysis: {
    period: 'within_30_days' | '30-90_days' | '90-180_days' | 'over_180_days';
    materialCount: number;
    quantity: number;
    value: number;
  }[];
  createdAt: string;
}

export interface WarehousePerformance {
  warehouseId: string;
  warehouseName: string;
  date: string;
  inboundOrders: number;
  inboundQuantity: number;
  outboundOrders: number;
  outboundQuantity: number;
  pickingEfficiency: number;
  putawayEfficiency: number;
  orderFulfillmentRate: number;
  inventoryAccuracy: number;
  createdAt: string;
}

// 设备管理类型
export interface WarehouseDevice {
  id: string;
  deviceNo: string;
  deviceName: string;
  deviceType: 'agv' | 'stacker' | 'conveyor' | 'scanner' | 'printer';
  warehouseId: string;
  warehouseName: string;
  status: 'online' | 'offline' | 'maintenance' | 'fault';
  ipAddress: string;
  location: string;
  lastMaintenanceTime: string | null;
  nextMaintenanceTime: string | null;
  createdAt: string;
  updatedAt: string;
}

// WCS集成类型
export interface WCSCommand {
  id: string;
  commandNo: string;
  deviceId: string;
  deviceNo: string;
  commandType: 'move' | 'lift' | 'scan' | 'print' | 'stop';
  status: 'pending' | 'sent' | 'executing' | 'completed' | 'failed';
  priority: 'high' | 'medium' | 'low';
  params: any;
  result: any;
  createdAt: string;
  updatedAt: string;
  executedAt: string | null;
}

export interface WCSState {
  deviceId: string;
  deviceNo: string;
  status: 'online' | 'offline' | 'maintenance' | 'fault';
  position: {
    x: number;
    y: number;
    z: number;
  };
  batteryLevel: number;
  taskId: string | null;
  taskStatus: string | null;
  lastUpdateTime: string;
}

// 系统配置类型
export interface WMSConfig {
  id: string;
  configKey: string;
  configValue: string;
  description: string;
  category: 'basic' | 'inbound' | 'outbound' | 'inventory' | 'pda' | 'wcs';
  createdAt: string;
  updatedAt: string;
}
