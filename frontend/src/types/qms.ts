// QMS系统类型定义

// 质量检验计划模块类型

// 检验项目
interface InspectionItem {
  id?: string;
  itemName: string;
  itemType: 'quantitative' | 'qualitative';
  specification: string;
  tolerance?: string;
  testMethod: string;
  testEquipment: string;
}

// 检验标准
interface InspectionStandard {
  id: string;
  standardNo: string;
  materialCode: string;
  materialName: string;
  version: string;
  aqlLevel: string;
  status: 'active' | 'inactive';
  inspectionItems: InspectionItem[];
  creator: string;
  createTime: string;
}

// 检验计划
interface InspectionPlan {
  id: string;
  planNo: string;
  planName: string;
  planType: 'incoming' | 'process' | 'final' | 'periodic';
  materialCode: string;
  materialName: string;
  batchSize?: number;
  samplingRule: string;
  inspectionStandardId: string;
  effectiveDate: string;
  expiryDate?: string;
  status: 'draft' | 'effective' | 'invalid';
  creator: string;
  createTime: string;
}

// 检验任务
interface InspectionTask {
  id: string;
  taskNo: string;
  planId: string;
  planName: string;
  materialCode: string;
  materialName: string;
  batchNo: string;
  quantity: number;
  taskType: 'incoming' | 'process' | 'final' | 'periodic';
  assignee: string;
  assignTime: string;
  dueTime: string;
  status: 'pending' | 'in_progress' | 'completed' | 'cancelled';
  inspectionResult?: 'qualified' | 'unqualified';
  // 扩展属性，用于任务管理
  taskName?: string;
  planNo?: string;
  createTime?: string;
  deadline?: string;
  actualFinishTime?: string;
  description?: string;
}

// 检验结果
interface InspectionResult {
  id: string;
  resultNo: string;
  taskId: string;
  taskNo: string;
  materialCode: string;
  materialName: string;
  batchNo: string;
  inspector: string;
  inspectionTime: string;
  inspectionItems: {
    itemId: string;
    itemName: string;
    specification: string;
    actualValue: string;
    result: 'qualified' | 'unqualified';
    remark?: string;
  }[];
  inspectionResult: 'qualified' | 'unqualified';
  auditStatus: 'pending' | 'approved' | 'rejected';
  auditor?: string;
  auditTime?: string;
  auditRemark?: string;
  // 扩展属性，用于结果记录
  inspectionQuantity?: number;
  qualifiedQuantity?: number;
  unqualifiedQuantity?: number;
  description?: string;
}

// 不合格品管理模块类型

// 不合格品登记
interface NCRegistration {
  id: string;
  registrationNo: string;
  materialCode: string;
  materialName: string;
  batchNo: string;
  quantity: number;
  defectType: 'appearance' | 'dimension' | 'performance' | 'function' | 'other';
  defectLevel: 'critical' | 'major' | 'minor';
  defectDescription: string;
  discoveryDepartment: string;
  discoveryPerson: string;
  discoveryTime: string;
  discoveryLocation: string;
  status: 'registered' | 'reviewed' | 'processed' | 'tracked';
  registrant: string;
  registrationTime: string;
  reviewStatus?: string;
  disposalStatus?: string;
  trackingStatus?: string;
}

// 不合格品评审
interface NCReview {
  id: string;
  registrationId: string;
  registrationNo: string;
  reviewTeam: string[];
  reviewDate: string;
  reviewOpinion: string;
  disposalPlan: 'rework' | 'scrap' | 'concession' | 'return' | 'other';
  reviewStatus: 'pending' | 'approved' | 'rejected';
  reviewer: string;
  reviewTime: string;
}

// 不合格品处理
interface NCDisposal {
  id: string;
  reviewId: string;
  registrationId: string;
  registrationNo: string;
  disposalPlan: 'rework' | 'scrap' | 'concession' | 'return' | 'other';
  disposalDescription: string;
  handler: string;
  startTime: string;
  endTime?: string;
  disposalStatus: 'pending' | 'processing' | 'completed';
  processResult?: string;
}

// 不合格品追踪
interface NCTracking {
  id: string;
  disposalId: string;
  registrationId: string;
  registrationNo: string;
  disposalPlan: 'rework' | 'scrap' | 'concession' | 'return' | 'other';
  disposalStatus: 'pending' | 'processing' | 'completed';
  trackingContent: string;
  trackingStatus: 'pending' | 'tracking' | 'completed';
  tracker: string;
  trackingTime: string;
  effectiveness: 'effective' | 'ineffective' | 'pending';
  improvementSuggestions?: string;
}

// 质量异常管理模块类型
interface AnomalyReport {
  id: string;
  reportNo: string;
  title: string;
  anomalyType: string;
  severity: 'critical' | 'major' | 'minor';
  occurrenceTime: string;
  occurrenceLocation: string;
  description: string;
  reporter: string;
  reportTime: string;
  status: 'pending' | 'investigating' | 'resolved' | 'closed';
  relatedProducts?: string[];
  impactAssessment?: string;
}

interface AnomalyAnalysis {
  id: string;
  reportId: string;
  reportNo: string;
  analysisMethod: string;
  analysisTeam: string[];
  analysisDate: string;
  manFactor?: string;
  machineFactor?: string;
  materialFactor?: string;
  methodFactor?: string;
  environmentFactor?: string;
  measurementFactor?: string;
  rootCause: string;
  analysisStatus: 'in_progress' | 'completed';
  analyzer: string;
  analysisTime: string;
}

interface AnomalyDisposal {
  id: string;
  reportId: string;
  reportNo: string;
  disposalPlan: string;
  responsiblePerson: string;
  startDate: string;
  deadline: string;
  actualCompletionDate?: string;
  disposalStatus: 'pending' | 'in_progress' | 'completed';
  disposalResult?: string;
  verifier?: string;
  verificationTime?: string;
  verificationResult?: 'passed' | 'failed';
}

interface CAPA {
  id: string;
  reportId: string;
  reportNo: string;
  preventiveMeasure: string;
  correctiveMeasure: string;
  implementationTeam: string[];
  implementationDeadline: string;
  actualCompletionDate?: string;
  implementationStatus: 'pending' | 'in_progress' | 'completed';
  verifyStatus: 'pending' | 'in_verification' | 'passed' | 'failed';
  verifyResult?: string;
  verifyDate?: string;
  reviewer?: string;
  reviewDate?: string;
  reviewResult?: 'approved' | 'rejected';
}

// 质量数据分析模块类型
interface DataCollection {
  id: string;
  collectionNo: string;
  collectionName: string;
  collectionDate: string;
  dataType: string;
  source: string;
  dataItems: any[];
  collector: string;
  status: 'draft' | 'submitted' | 'approved';
}

interface StatisticalData {
  id: string;
  dataType: string;
  period: string;
  metrics: {
    [key: string]: any;
  };
  createTime: string;
}

interface Report {
  id: string;
  reportNo: string;
  reportName: string;
  reportType: 'daily' | 'weekly' | 'monthly' | 'quarterly' | 'annual';
  period: string;
  content: any;
  creator: string;
  createTime: string;
  status: 'draft' | 'published' | 'archived';
}

interface ForecastResult {
  id: string;
  forecastType: string;
  period: string;
  forecastData: any[];
  confidenceInterval: number;
  createTime: string;
  status: 'pending' | 'completed';
}

// 导出类型
export type {
  // 质量检验计划模块
  InspectionStandard,
  InspectionPlan,
  InspectionTask,
  InspectionResult,
  // 不合格品管理模块
  NCRegistration,
  NCReview,
  NCDisposal,
  NCTracking,
  // 质量异常管理模块
  AnomalyReport,
  AnomalyAnalysis,
  AnomalyDisposal,
  CAPA,
  // 质量数据分析模块
  DataCollection,
  StatisticalData,
  Report,
  ForecastResult
};
