// AGV系统类型定义

// AGV基本信息
export interface AGV {
  id: string;              // AGV唯一标识
  code: string;            // AGV编号
  name: string;            // AGV名称
  type: string;            // AGV类型
  model: string;           // AGV型号
  status: AGVStatus;       // AGV状态
  batteryLevel: number;    // 电池电量(0-100)
  voltage: number;         // 电压
  temperature: number;     // 温度(°C)
  speed: number;           // 当前速度(m/s)
  direction: string;       // 行驶方向
  position: string;        // 当前位置
  currentTaskId: string;   // 当前任务ID
  lastUpdate: string;      // 最后更新时间
  loadStatus: LoadStatus;  // 负载状态
}

// AGV状态类型
export type AGVStatus = 'idle' | 'running' | 'charging' | 'fault' | 'maintenance';

// 负载状态类型
export type LoadStatus = 'empty' | 'loaded' | 'partially_loaded';

// 任务类型
export interface AGVTask {
  id: string;               // 任务ID
  taskNo: string;           // 任务编号
  type: TaskType;           // 任务类型
  priority: TaskPriority;   // 优先级
  agvCode: string;          // 分配的AGV编号
  startPoint: string;       // 起点
  endPoint: string;         // 终点
  cargoInfo: string;        // 货物信息
  status: TaskStatus;       // 任务状态
  createTime: string;       // 创建时间
  startTime: string;        // 开始时间
  endTime: string;          // 结束时间
  trackStatus: string;      // 任务实时跟踪状态
  progress: number;         // 任务进度(0-100)
  estimatedCompletion: string; // 预计完成时间
}

// 任务类型
export type TaskType = 'transport' | 'charge' | 'inspection' | 'emergency';

// 任务优先级
export type TaskPriority = 'low' | 'medium' | 'high' | 'emergency';

// 任务状态
export type TaskStatus = 'pending' | 'assigned' | 'running' | 'paused' | 'completed' | 'failed' | 'cancelled';

// 路径规划
export interface PathPlan {
  id: string;               // 路径ID
  taskId: string;           // 关联任务ID
  agvCode: string;          // AGV编号
  startPoint: string;       // 起点
  endPoint: string;         // 终点
  pathPoints: PathPoint[];  // 路径点集合
  distance: number;         // 路径长度
  estimatedTime: number;    // 预计时间(分钟)
  algorithm: string;        // 使用的算法
  status: PathStatus;       // 路径状态
  createTime: string;       // 创建时间
}

// 路径点
export interface PathPoint {
  id: string;               // 点ID
  x: number;                // X坐标
  y: number;                // Y坐标
  sequence: number;         // 顺序
  type: PointType;          // 点类型
  description: string;      // 描述
}

// 点类型
export type PointType = 'start' | 'waypoint' | 'end' | 'charging' | 'turn';

// 路径状态
export type PathStatus = 'planned' | 'executing' | 'completed' | 'failed' | 'cancelled';

// 交通管制节点
export interface TrafficNode {
  id: number;               // 节点ID
  nodeId: string;           // 节点标识
  nodeName: string;         // 节点名称
  type: NodeType;           // 节点类型
  status: NodeStatus;       // 节点状态
  lockAgv: string;          // 当前占用AGV
  lockTime: string;         // 锁闭时间
  queueLength: number;      // 排队数量
  description: string;      // 节点描述
  area: string;             // 所属区域
}

// 节点类型
export type NodeType = 'intersection' | 'straight' | 'turn' | 'charging_area' | 'restricted_area';

// 节点状态
export type NodeStatus = 'normal' | 'locked' | 'warning' | 'maintenance';

// 交通规则
export interface TrafficRule {
  id: number;               // 规则ID
  area: string;             // 区域名称
  ruleType: RuleType;       // 规则类型
  direction: string;        // 行驶方向
  speedLimit: number;       // 限速(km/h)
  description: string;      // 规则描述
  status: boolean;          // 规则状态
  createTime: string;       // 创建时间
  updateTime: string;       // 更新时间
}

// 规则类型
export type RuleType = 'directional' | 'speed' | 'restricted';

// 故障告警
export interface FaultAlert {
  id: number;               // 告警ID
  agvCode: string;          // AGV编号
  faultType: string;        // 故障类型
  level: AlertLevel;        // 告警级别
  message: string;          // 告警消息
  position: string;         // 故障位置
  faultTime: string;        // 故障时间
  status: AlertStatus;      // 处理状态
  operator: string;         // 操作人员
  handleTime: string;       // 处理时间
  handleResult: string;     // 处理结果
}

// 告警级别
export type AlertLevel = 'info' | 'warning' | 'error' | 'critical';

// 告警状态
export type AlertStatus = 'pending' | 'handled' | 'resolved';

// 协同策略
export interface CollaborationStrategy {
  id: string;               // 策略ID
  name: string;             // 策略名称
  mode: CollaborationMode;  // 协同模式
  assignmentAlgorithm: string; // 任务分配算法
  maxConcurrentTasks: number;  // 最大并发任务数
  responseTime: number;        // 协同响应时间(ms)
  description: string;         // 策略描述
  status: boolean;             // 策略状态
  createTime: string;          // 创建时间
  updateTime: string;          // 更新时间
}

// 协同模式
export type CollaborationMode = 'centralized' | 'distributed' | 'hybrid';

// 冲突避免配置
export interface CollisionAvoidanceConfig {
  id: string;               // 配置ID
  safetyDistance: number;   // 安全距离阈值(m)
  warningDistance: number;  // 警告距离阈值(m)
  emergencyStopDistance: number; // 紧急停止距离(m)
  handlingStrategy: CollisionHandlingStrategy; // 冲突处理策略
  priorityRule: CollisionPriorityRule; // 优先级规则
  detectionFrequency: number; // 冲突检测频率(ms)
  createTime: string;       // 创建时间
  updateTime: string;       // 更新时间
}

// 冲突处理策略
export type CollisionHandlingStrategy = 'wait-first' | 'avoid-first' | 'replan';

// 冲突优先级规则
export type CollisionPriorityRule = 'task-priority' | 'agv-id' | 'distance-priority';

// 电池状态
export interface BatteryStatus {
  agvCode: string;          // AGV编号
  batteryLevel: number;     // 电池电量(0-100)
  voltage: number;          // 电压
  temperature: number;      // 温度(°C)
  chargingStatus: ChargingStatus; // 充电状态
  estimatedRuntime: number; // 预计运行时间(分钟)
  lastUpdate: string;       // 最后更新时间
}

// 充电状态
export type ChargingStatus = 'charging' | 'not_charging' | 'charged';

// 运行统计
export interface OperationStats {
  totalAgvCount: number;    // 总AGV数量
  activeAgvCount: number;   // 活跃AGV数量
  runningTaskCount: number; // 运行中任务数
  completedTaskCount: number; // 完成任务数
  faultAgvCount: number;    // 故障AGV数量
  collisionAvoidanceCount: number; // 避免冲突次数
  avgTaskCompletionTime: number; // 平均任务完成时间(分钟)
  avgBatteryLevel: number;  // 平均电池电量(%)
  uptimeRate: number;       // uptime率(%)
}

// 协同日志
export interface CollaborationLog {
  id: number;               // 日志ID
  time: string;             // 日志时间
  content: string;          // 日志内容
  type: LogType;            // 日志类型
  relatedAgv: string;       // 相关AGV
  relatedTask: string;      // 相关任务
}

// 日志类型
export type LogType = 'collision_avoidance' | 'task_assignment' | 'status_change' | 'strategy_change' | 'system_event';

// 区域信息
export interface Area {
  id: string;               // 区域ID
  name: string;             // 区域名称
  type: AreaType;           // 区域类型
  description: string;      // 区域描述
  status: boolean;          // 区域状态
  createTime: string;       // 创建时间
  updateTime: string;       // 更新时间
}

// 区域类型
export type AreaType = 'storage' | 'production' | 'charging' | 'maintenance' | 'restricted';
