/**
 * LES（物流执行系统）数据模型定义
 */

/**
 * 运输计划状态枚举
 */
export enum TransportPlanStatus {
  PLANNING = 1,      // 计划中
  IN_TRANSIT = 2,    // 运输中
  SIGNED = 3,        // 已签收
  ABORTED = 4        // 异常中断
}

/**
 * 准时状态枚举
 */
export enum OnTimeStatus {
  ON_TIME = 1,       // 准时
  EARLY = 2,         // 早到
  DELAYED = 3        // 延误
}

/**
 * 运输计划类型
 */
export interface TransportPlan {
  id: number;
  planNo: string;
  salesOrderNo: string; // 关联销售订单
  vehicleId: number;
  driverId: number;
  routeId: number;
  status: TransportPlanStatus;
  costEstimated: number; // 预估费用
  createTime: string;
}

/**
 * 车辆信息类型
 */
export interface Vehicle {
  id: number;
  licensePlate: string; // 车牌
  vehicleType: string;  // 车辆类型
  loadCapacity: number; // 载重
  status: string;
  createTime: string;
}

/**
 * 司机信息类型
 */
export interface Driver {
  id: number;
  name: string;
  phone: string;
  licenseNo: string; // 驾照号码
  status: string;
  createTime: string;
}

/**
 * 配送路线类型
 */
export interface Route {
  id: number;
  routeName: string;
  startLocation: string;
  endLocation: string;
  distance: number; // 距离(km)
  estimatedTime: number; // 预计时间(min)
  createTime: string;
}

/**
 * 在途监控日志类型
 */
export interface MonitorLog {
  id: number;
  planId: number;
  longitude: number;
  latitude: number;
  currentStatus: string; // 装货中/行驶中/卸货中
  isAnomaly: boolean;
  recordTime: string;
}

/**
 * 签收凭证记录类型
 */
export interface SignVoucher {
  id: number;
  planId: number;
  /** ERP 关联订单号（业务键，用于签收回流 ERP） */
  erpOrderNo?: string;
  /** CRM 关联订单号（业务键，用于签收回流 CRM） */
  crmOrderNo?: string;
  /** ERP 回写状态（PENDING/SENT/FAILED） */
  erpSyncStatus?: string;
  /** CRM 回写状态（PENDING/SENT/FAILED） */
  crmSyncStatus?: string;
  customerSign: string; // 电子签名路径
  photoUrls: string; // 现场破损/货物照片
  arrivalTime: string;
  onTimeStatus: OnTimeStatus;
}

/**
 * 异常事件类型
 */
export interface AnomalyEvent {
  id: number;
  planId: number;
  eventType: string; // 行驶偏差/长时间停留/交通事故/延误
  eventDescription: string;
  eventTime: string;
  handlingStatus: string; // 未处理/处理中/已处理
  handlingResult: string;
  createTime: string;
}

/**
 * 运输成本类型
 */
export interface TransportCost {
  id: number;
  planId: number;
  fuelCost: number; // 燃油费用
  tollCost: number; // 过路费
  driverSalary: number; // 司机提成
  otherCost: number; // 其他费用
  totalCost: number; // 总费用
  createTime: string;
}

/**
 * 物流分析数据类型
 */
export interface LogisticsAnalysis {
  id: number;
  planId: number;
  actualDuration: number; // 实际运输时长(min)
  plannedDuration: number; // 计划运输时长(min)
  delayMinutes: number; // 延误分钟数
  vehicleUtilization: number; // 车辆利用率(%)
  driverUtilization: number; // 司机利用率(%)
  costPerKm: number; // 每公里成本
  createTime: string;
}

/**
 * 服务质量评估类型
 */
export interface ServiceQuality {
  id: number;
  planId: number;
  signSuccessRate: number; // 签收成功率(%)
  cargoIntegrityRate: number; // 货损完好率(%)
  onTimeRate: number; // 准时率(%)
  customerSatisfaction: number; // 客户满意度(%)
  createTime: string;
}

/**
 * 运输任务类型
 */
export interface TransportTask {
  id: number;
  taskNo: string;
  planId: number;
  driverId: number;
  vehicleId: number;
  status: string; // 待接收/已接收/执行中/已完成/已取消
  createTime: string;
  startTime?: string;
  endTime?: string;
}

/**
 * 销售订单关联类型
 */
export interface SalesOrder {
  id: number;
  orderNo: string;
  customerName: string;
  deliveryAddress: string;
  orderDate: string;
  requiredDate: string;
  totalAmount: number;
}

/**
 * 车辆定位数据类型
 */
export interface VehicleLocation {
  vehicleId: number;
  licensePlate: string;
  longitude: number;
  latitude: number;
  speed: number; // 速度(km/h)
  direction: number; // 方向(度)
  timestamp: string;
}

/**
 * 运输统计数据类型
 */
export interface TransportStats {
  totalPlans: number; // 总计划数
  inTransitPlans: number; // 运输中计划数
  completedPlans: number; // 已完成计划数
  delayedPlans: number; // 延误计划数
  totalDistance: number; // 总运输距离(km)
  totalCost: number; // 总运输成本
  averageOnTimeRate: number; // 平均准时率(%)
  averageSignSuccessRate: number; // 平均签收成功率(%)
}