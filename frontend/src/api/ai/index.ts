import api from '@/api'

/**
 * AI 决策中心 API 封装
 * 对接 ai-brain 服务：建议列表/晨会简报/决策扫描/根因会诊/能力矩阵
 */

/** AI 建议实体 */
export interface AiSuggestion {
  id: number
  suggestionType: string
  module: string
  title: string
  severity: string
  analysis: string | null
  actionDraft: string | null
  automationLevel: string
  status: string
  feedback: string | null
  eventId: string
  createdTime: string | null
  resolvedTime: string | null
}

/** 采纳率统计（按类型分组） */
export interface AdoptionStats {
  totalCount: number
  byType: Array<{
    type: string
    acceptRate: number
    accepted: number
    rejected: number
    modified: number
    pending: number
  }>
}

/** 大脑状态（能力矩阵 + 自动化刻度盘） */
export interface BrainStatus {
  service: string
  phase: string
  automationDial: Record<string, string>
  capabilities: Array<{
    code: string
    name: string
    endpoint: string
    status: string
    phase: string
  }>
}

/**
 * 查询建议列表
 * @param status 状态过滤（默认 PENDING）
 * @param type 类型过滤（可选）
 */
export function getSuggestions(status?: string, type?: string) {
  return api.get('/api/v1/ai-brain/suggestions', { params: { status, type } })
}

/**
 * 人工处置建议（S13 反馈学习环）
 * @param id 建议ID
 * @param action ACCEPTED/REJECTED/MODIFIED/EXECUTED
 * @param feedback 处置说明（可选）
 */
export function feedbackSuggestion(id: number, action: string, feedback?: string) {
  return api.put(`/api/v1/ai-brain/suggestions/${id}/feedback`, { action, feedback })
}

/** 采纳率统计（学习环分析） */
export function getAdoptionStats() {
  return api.get('/api/v1/ai-brain/suggestions/adoption-stats')
}

/**
 * 一键执行建议动作（P4-1 动作执行引擎）
 * CONFIRM 级建议确认后调用，成功置 EXECUTED 回写业务单号，失败保持 PENDING 允许重试。
 * actionIndex（P4-6 多专家联动）：非空时执行 analysis.secondaryActions 中对应次要动作，
 * 执行成功建议保持 PENDING 仅留痕，主动作仍可执行
 * @param id 建议ID
 * @param actionIndex 次要动作下标（可空，空=主动作）
 */
export function executeSuggestion(id: number, actionIndex?: number) {
  const qs = actionIndex != null ? `?actionIndex=${actionIndex}` : ''
  return api.post(`/api/v1/ai-brain/suggestions/${id}/execute${qs}`)
}

/** 技能健康看板单技能行 */
export interface SkillHealthRow {
  skillCode: string
  runs24h: number
  errors24h: number
  successRate24h: number
  avgDurationMs24h: number
  produced24h: number
  runs7d: number
  errors7d: number
  successRate7d: number
  avgDurationMs7d: number
  produced7d: number
  consecutiveErrors: number
  health: 'GREEN' | 'RED'
  lastRunTime: string | null
}

/** 技能健康看板（P4-8 决策可观测性）：各技能近24h/7d执行统计与连续失败标红 */
export function getSkillHealth() {
  return api.get('/api/v1/ai-brain/skills/health')
}

/** AI 大脑状态（能力矩阵） */
export function getBrainStatus() {
  return api.get('/api/v1/ai-brain/status')
}

/** 手动触发晨会简报六域巡检（S05） */
export function generateBriefing() {
  return api.post('/api/v1/ai-brain/briefing/generate')
}

/** 触发设备故障预测扫描（S01） */
export function runFaultPrediction() {
  return api.post('/api/v1/ai-brain/decision/fault-prediction/run')
}

/** 触发交期风险扫描（S02） */
export function runDeliveryRisk() {
  return api.post('/api/v1/ai-brain/decision/delivery-risk/run')
}

/** 触发能耗负荷预测（S04） */
export function runEnergyLoad() {
  return api.post('/api/v1/ai-brain/decision/energy-load/run')
}

/** 触发审批预审扫描（S07） */
export function runPreauditScan() {
  return api.post('/api/v1/ai-brain/decision/preaudit/scan')
}

/** 触发动态安全库存扫描（S03） */
export function runSafetyStock() {
  return api.post('/api/v1/ai-brain/decision/safety-stock/run')
}

/** 触发备件缺货决策闭环扫描 */
export function runStockShortage() {
  return api.post('/api/v1/ai-brain/decision/stock-shortage/run')
}

/** 触发数据质量哨兵全模块对账扫描（S12） */
export function runDqSentinel() {
  return api.post('/api/v1/ai-brain/decision/dq-sentinel/run')
}

/**
 * 跨域根因追溯（S06）
 * @param faultId EAM 故障记录ID
 */
export function getRootCauseByFault(faultId: number) {
  return api.get(`/api/v1/ai-brain/decision/root-cause/fault/${faultId}`)
}

/**
 * 查询审批实例的 AI 预审意见（S07）
 * @param instanceId OA 审批实例ID
 */
export function getPreauditByInstance(instanceId: number) {
  return api.get(`/api/v1/ai-brain/decision/preaudit/${instanceId}`)
}

/**
 * 生成质量 8D 报告骨架（S09）
 * @param ncId QMS 不合格品登记单ID
 */
export function generateEightD(ncId: number) {
  return api.post(`/api/v1/ai-brain/decision/eight-d/${ncId}`)
}

/** 视觉理解提取结果（统一信封内 data） */
export interface VisionParseResult {
  success: boolean
  message: string
  fields: Record<string, any>
  confidence: number
  evidence?: Record<string, any>
}

/**
 * 单据/仪表 OCR 文本结构化（S11，规则版）
 * @param payload docType 文档类型（invoice/gauge）；ocrText OCR 纯文本；hint 可选点位编码
 */
export function parseVisionDocument(payload: { docType: string; ocrText: string; hint?: string }) {
  return api.post<VisionParseResult>('/api/v1/ai-brain/vision/parse-document', payload)
}
