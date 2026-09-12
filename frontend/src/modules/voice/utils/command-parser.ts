import { routeKeywords } from './navigation'

/*
 * Enhanced Command Parser
 * 
 * Combines regex patterns with advanced fuzzy matching and context awareness for improved intent recognition.
 * Supports intent priority, multi-turn context, flexible command parsing, and confidence scoring.
 */

export interface ParsedCommand {
  intent: 'NAVIGATE' | 'ACTION' | 'FILL_FORM' | 'CANCEL' | 'HELP' | 'QUERY_STOCK' | 'RECORD_FAULT' | 'UNKNOWN'
  target?: string
  payload?: any
  message?: string
  confidence: number // 识别置信度 (0-1)
}

// 上下文管理接口
export interface CommandContext {
  conversationId?: string
  lastIntent?: string
  lastTarget?: string
  lastPayload?: any
  timestamp: number
  history: Array<{
    text: string
    intent: string
    target?: string
    payload?: any
    timestamp: number
  }>
  contextVariables?: Record<string, any> // 上下文变量，用于多轮对话
}

// 意图优先级枚举 (数值越高，优先级越高)
enum IntentPriority {
  NAVIGATE = 5,
  CANCEL = 4,
  HELP = 3,
  ACTION = 2,
  FILL_FORM = 1,
  UNKNOWN = 0
}

// 命令模板接口
interface CommandTemplate {
  pattern: RegExp
  intent: ParsedCommand['intent']
  priority: number
  extractor: (match: RegExpMatchArray, text: string) => {
    target?: string
    payload?: any
    message?: string
    confidence: number
  }
}

// Regex Patterns with extended coverage
const COMMAND_TEMPLATES: CommandTemplate[] = [
  // S10 语音车间·备件库存查询（AUTO 级只读）: "查一下 P-4421 库存" / "密封包库存还有多少"
  {
    // 注：语义同义词表会将"库存"替换为"仓储"，故需同时匹配两种表述
    pattern: /^(?:查一下|查询|查|看一下|看看|帮我查)?\s*(.+?)(?:的)?(?:库存|仓储)(?:查询|还有多少|有多少|是多少|情况|一下)?[?？]?\s*$/i,
    intent: 'QUERY_STOCK',
    priority: 6,
    extractor: (match) => {
      const keyword = (match[1] || match[2] || '').trim()
      if (!keyword) return { confidence: 0 }
      return {
        payload: { keyword },
        message: `正在查询 ${keyword} 的库存`,
        confidence: 0.92
      }
    }
  },

  // S10 语音车间·设备异常记录（经现有 EAM 故障端点，CONFIRM 级不开后门）: "记录一下：3号泵站有异响"
  {
    pattern: /^(?:记录一下|记录|上报异常|异常上报|报修)[:：]?\s*(.+)/i,
    intent: 'RECORD_FAULT',
    priority: 6,
    extractor: (match) => {
      const description = (match[1] || '').trim()
      if (!description) return { confidence: 0 }
      return {
        payload: { description },
        message: `正在记录设备异常`,
        confidence: 0.9
      }
    }
  },

  // Navigation: "打开/进入/跳转到 [页面名]"
  {
    pattern: /^(?:打开|进入|跳转到|转到|访问|切换到|进入页面|打开页面)\s*(.+)/i,
    intent: 'NAVIGATE',
    priority: 5,
    extractor: (match, text) => {
      const targetName = match[1]
      if (!targetName) return { confidence: 0 }
      const route = findRoute(targetName)
      return route ? {
        target: route.path,
        message: `正在为您${text}`,
        confidence: 0.95
      } : {
        confidence: 0
      }
    }
  },
  
  // Action: "新建/创建/查询/搜索/保存 [动作]"
  {
    pattern: /^(?:新建|创建|查询|搜索|保存|提交|删除|编辑|修改|导出|导入|刷新|重置|添加|清除|取消)\s*(.+)?/i,
    intent: 'ACTION',
    priority: 2,
    extractor: (match, text) => {
      const actionVerb = text.split(/\s+/)[0]
      return {
        message: `正在${actionVerb}`,
        confidence: 0.85
      }
    }
  },
  
  // Form Filling: "[字段名] (是/为/填/写) [值]" OR "把 [字段名] (设为/改成) [值]"
  {
    pattern: /^(?:把\s*)?(.+?)\s*(?:是|为|填|写|设为|改成|修改为|更新为|调整为|设置为)\s*(.+)/i,
    intent: 'FILL_FORM',
    priority: 1,
    extractor: (match) => {
      const fieldName = match[1] || ''
      const value = match[2] || ''
      return {
        payload: fieldName ? { [fieldName]: value } : {},
        confidence: 0.9
      }
    }
  },
  
  // Cancel: "取消/退出 [操作]"
  {
    pattern: /^(?:取消|退出|关闭|停止|结束|休眠|待机|再见|拜拜)\s*(.+)?/i,
    intent: 'CANCEL',
    priority: 4,
    extractor: (match, text) => {
      return {
        message: '已取消操作',
        confidence: 0.95
      }
    }
  },
  
  // Help: "帮助/怎么用 [功能]"
  {
    pattern: /^(?:帮助|怎么用|使用说明|功能介绍|求助|指导|提示|告诉我)\s*(.+)?/i,
    intent: 'HELP',
    priority: 3,
    extractor: (match) => {
      const helpTopic = match[1] || '所有功能'
      return {
        message: `为您提供${helpTopic}的帮助信息`,
        confidence: 0.9
      }
    }
  }
]

// 核心动词及其语义权重
const ACTION_WEIGHTS: Record<string, number> = {
  '打开': 1.2, '进入': 1.2, '去': 1.1, '看': 1.0, '跳转': 1.2,
  '新建': 1.5, '创建': 1.5, '添加': 1.3,
  '删除': 1.8, '撤销': 1.8, '清除': 1.5,
  '查询': 1.3, '搜索': 1.3, '查找': 1.3,
  '保存': 1.5, '提交': 1.5
}

// 动作映射表，扩展支持更多动作
const ACTION_MAP: Record<string, string> = {
  '保存': 'save', '提交': 'submit', '存储': 'save',
  '新建': 'create', '创建': 'create', '新增': 'add', '添加': 'add',
  '查询': 'search', '搜索': 'search', '查找': 'search', '检索': 'search',
  '导出': 'export', '下载': 'export',
  '导入': 'import', '上传': 'import',
  '删除': 'delete', '移除': 'delete', '撤销': 'delete',
  '编辑': 'edit', '修改': 'modify', '更正': 'modify',
  '刷新': 'refresh', '重载': 'refresh',
  '重置': 'reset', '清空': 'clear', '清除': 'clear',
  '取消': 'cancel', '退出': 'cancel', '关掉': 'cancel',
  '停止': 'stop', '结束': 'end', '完成': 'end'
}

// 字段别名映射表，扩展支持更多场景
const FIELD_ALIASES: Record<string, string> = {
  '钱': 'amount', '总额': 'amount', '花费': 'amount', '成本': 'amount',
  '单价': 'price', '价格': 'price',
  '多少个': 'quantity', '数量': 'quantity', '额度': 'quantity',
  '公司': 'supplier', '供应商': 'supplier', '厂家': 'supplier',
  '买家': 'customer', '客户': 'customer', '甲方': 'customer', '受众': 'customer',
  '哪天': 'date', '日期': 'date', '月份': 'date', '年份': 'date',
  '名字': 'name', '名称': 'name', '标题': 'name',
  '号': 'code', '编号': 'code', '单号': 'code', 'ID': 'code',
  '注': 'remark', '备注': 'remark', '说明': 'remark',
  '谁': 'contact', '联系人': 'contact', '对接人': 'contact',
  '电话': 'phone', '手机': 'phone', '联系方式': 'phone',
  '地点': 'address', '地址': 'address', '位置': 'address',
  '状态': 'status', '进度': 'status',
  '哪个类': 'type', '类型': 'type', '分类': 'type',
  '部': 'department', '部门': 'department',
  '人': 'employee', '员工': 'employee', '同事': 'employee',
  '货': 'product', '产品': 'product', '物料': 'product', '件': 'product',
  '单': 'order', '订单': 'order', '合同': 'order'
}

// 语义同义词映射 (用于口语化理解)
const SEMANTIC_SYNONYMS: Record<string, string> = {
  '钱': '财务',
  '买东西': '采购',
  '卖东西': '销售',
  '招人': '招聘',
  '发工资': '薪酬',
  '迟到': '考勤',
  '请假': '假勤',
  '干活': '生产',
  '库存': '仓储',
  '原料': 'BOM',
  '设备': '资产',
  '电费': '能源',
  '能不能用': '质量'
}

// 改进的模糊匹配算法 (基于Levenshtein距离)
function levenshteinDistance(s: string, t: string): number {
  const m = s.length
  const n = t.length
  const d: number[][] = Array(m + 1).fill(0).map(() => Array(n + 1).fill(0))
  for (let i = 1; i <= m; i++) {
    const row = d[i]
    if (row) row[0] = i
  }
  for (let j = 1; j <= n; j++) {
    const row = d[0]
    if (row) row[j] = j
  }
  for (let j = 1; j <= n; j++) {
    for (let i = 1; i <= m; i++) {
      const substitutionCost = s[i - 1] === t[j - 1] ? 0 : 1
      const row = d[i]
      const prevRow = d[i - 1]
      if (row && prevRow) {
        const dij = row[j]!
        const di1j1 = prevRow[j - 1]!
        const di1j = prevRow[j]!
        row[j] = Math.min(
          di1j1 + 1,              // 删除
          di1j + 1,              // 插入
          dij + substitutionCost // 替换
        )
      }
    }
  }
  const finalRow = d[m]
  return finalRow ? (finalRow[n] ?? 0) : 0
}

// 改进的模糊匹配算法，返回相似度分数 (0-1)
// STRICT MODE: 移除了“包含即匹配”的逻辑，强制高相似度
function fuzzyMatch(str1: string, str2: string, threshold: number = 0.7): { match: boolean; similarity: number } {
  const s1 = str1.toLowerCase().trim()
  const s2 = str2.toLowerCase().trim()
  
  // 精确匹配
  if (s1 === s2) {
    return { match: true, similarity: 1.0 }
  }
  
  // 计算Levenshtein距离
  const distance = levenshteinDistance(s1, s2)
  const maxLength = Math.max(s1.length, s2.length)
  const similarity = maxLength > 0 ? 1 - (distance / maxLength) : 0

  // 长度差异惩罚 (Length Penalty)
  // 如果输入长度和目标长度差异过大，惩罚相似度
  const lengthDiff = Math.abs(s1.length - s2.length)
  if (lengthDiff > 2 && similarity < 0.9) {
      // 除非相似度极高，否则如果长度差2个字以上，大概率不是我们要找的
      return { match: false, similarity: 0.1 }
  }
  
  // 计算额外的相似度增强因子
  let enhancement = 0
  
  // 如果有共同的前缀，增加相似度
  const commonPrefixLength = [...s1].findIndex((char, i) => char !== s2[i])
  if (commonPrefixLength > 0) {
    enhancement += commonPrefixLength / maxLength * 0.2
  }
  
  // 调整相似度，确保在0-1范围内
  let adjustedSimilarity = Math.min(1.0, similarity + enhancement)
  
  // 针对特定关键词的精准优化
  if ((s1.includes('海星') && s2.includes('海心')) || (s1.includes('海心') && s2.includes('海星'))) {
    adjustedSimilarity = Math.max(adjustedSimilarity, 0.95)
  }
  
  return { 
    match: adjustedSimilarity >= threshold, 
    similarity: adjustedSimilarity 
  }
}

// 查找路由，使用改进的模糊匹配
function findRoute(query: string): { path: string; similarity: number } | null {
  const cleanQuery = query.trim()
  if (!cleanQuery) return null
  
  // 1. 精确匹配
  if (routeKeywords[cleanQuery]) {
    return { path: routeKeywords[cleanQuery], similarity: 1.0 }
  }
  
  // 2. 改进的模糊匹配
  let bestMatch: { key: string; path: string; similarity: number } | null = null
  
  for (const [key, path] of Object.entries(routeKeywords)) {
    // 提高阈值到 0.8，避免误匹配
    const { match, similarity } = fuzzyMatch(key, cleanQuery, 0.8)
    if (match) {
      if (!bestMatch || similarity > bestMatch.similarity) {
        bestMatch = { key, path, similarity }
      }
    }
  }
  
  return bestMatch ? { path: bestMatch.path, similarity: bestMatch.similarity } : null
}

// 匹配字段，使用模糊匹配和别名映射
function matchField(query: string, availableFields: string[]): { field: string | null; similarity: number } {
  const cleanQuery = query.trim().toLowerCase()
  if (!cleanQuery || !availableFields.length) return { field: null, similarity: 0 }
  
  // 1. 精确匹配
  for (const field of availableFields) {
    if (field.toLowerCase() === cleanQuery) {
      return { field, similarity: 1.0 }
    }
  }
  
  // 2. 模糊匹配
  let bestFieldMatch: { field: string; similarity: number } | null = null
  for (const field of availableFields) {
    const { match, similarity } = fuzzyMatch(field, cleanQuery)
    if (match) {
      if (!bestFieldMatch || similarity > bestFieldMatch.similarity) {
        bestFieldMatch = { field, similarity }
      }
    }
  }
  
  // 3. 别名映射
  const alias = FIELD_ALIASES[cleanQuery]
  if (alias && availableFields.includes(alias) && (!bestFieldMatch || bestFieldMatch.similarity < 0.9)) {
    return { field: alias, similarity: 0.9 }
  }
  
  // 4. 别名模糊匹配
  for (const [aliasKey, field] of Object.entries(FIELD_ALIASES)) {
    if (availableFields.includes(field)) {
      const { match, similarity } = fuzzyMatch(aliasKey, cleanQuery)
      if (match && similarity > 0.8 && (!bestFieldMatch || similarity > bestFieldMatch.similarity)) {
        bestFieldMatch = { field, similarity }
      }
    }
  }
  
  return bestFieldMatch ? { field: bestFieldMatch.field, similarity: bestFieldMatch.similarity } : { field: null, similarity: 0 }
}

// 从文本中提取动作
function extractAction(text: string, availableActions: Record<string, Function>): { action: string | null; similarity: number } {
  const cleanText = text.toLowerCase().trim()
  
  // 查找动作映射
  for (const [keyword, actionId] of Object.entries(ACTION_MAP)) {
    if (cleanText.includes(keyword) && availableActions[actionId]) {
      return { action: actionId, similarity: 0.9 }
    }
  }
  
  // 模糊匹配动作
  let bestActionMatch: { action: string; similarity: number } | null = null
  for (const [keyword, actionId] of Object.entries(ACTION_MAP)) {
    if (availableActions[actionId]) {
      const { match, similarity } = fuzzyMatch(keyword, cleanText)
      if (match && (!bestActionMatch || similarity > bestActionMatch.similarity)) {
        bestActionMatch = { action: actionId, similarity }
      }
    }
  }
  
  return bestActionMatch ? { action: bestActionMatch.action, similarity: bestActionMatch.similarity } : { action: null, similarity: 0 }
}

// 基于上下文的命令解析增强
function enhanceWithContext(parsedCommand: ParsedCommand, commandContext?: CommandContext): ParsedCommand {
  if (!commandContext) return parsedCommand
  
  // 如果意图不明确，尝试从上下文中推断
  if (parsedCommand.intent === 'UNKNOWN' && commandContext.lastIntent) {
    // 基于上一个意图进行推断
    switch (commandContext.lastIntent) {
      case 'ACTION':
        // 如果上一个意图是ACTION，当前可能是继续该动作
        parsedCommand.intent = 'ACTION'
        parsedCommand.confidence = (parsedCommand.confidence || 0) + 0.2
        break
      case 'FILL_FORM':
        // 如果上一个意图是FILL_FORM，当前可能是继续填充表单
        parsedCommand.intent = 'FILL_FORM'
        parsedCommand.confidence = (parsedCommand.confidence || 0) + 0.2
        break
    }
  }
  
  // 如果缺少目标，尝试从上下文中获取
  if (!parsedCommand.target && commandContext.lastTarget) {
    parsedCommand.target = commandContext.lastTarget
    parsedCommand.confidence = (parsedCommand.confidence || 0) + 0.1
  }
  
  return parsedCommand
}

/**
 * 解析命令，增强版
 * @param text 用户输入的文本
 * @param currentContext 当前页面上下文
 * @param commandContext 对话上下文（可选，用于多轮对话）
 * @returns 解析后的命令
 */
export function parseCommand(
  text: string, 
  currentContext: any, 
  commandContext?: CommandContext
): ParsedCommand {
  const cleanText = text.trim()
  if (!cleanText) return { intent: 'UNKNOWN', confidence: 0 }

  // 收集所有可能的意图解析结果
  const potentialCommands: Array<{
    command: ParsedCommand
    priority: number
  }> = []

  // 0. 应用语义同义词替换 (口语化转专业词汇)
  let processedText = cleanText
  for (const [synonym, term] of Object.entries(SEMANTIC_SYNONYMS)) {
    if (processedText.includes(synonym)) {
      processedText = processedText.replace(synonym, term)
    }
  }

  // 1. 使用命令模板匹配 (VERB + NOUN)
  for (const template of COMMAND_TEMPLATES) {
    const match = processedText.match(template.pattern)
    if (match) {
      const extracted = template.extractor(match, processedText)
      if (extracted.confidence > 0.5) {
        potentialCommands.push({
          command: {
            intent: template.intent,
            ...extracted
          },
          priority: template.priority
        })
      }
    }
  }

  // 2. REMOVED: 移除了“尝试直接导航匹配（回退）”
  /* 
  const directRoute = findRoute(cleanText)
  if (directRoute && directRoute.similarity > 0.7) {
     ...
  }
  */

  // 3. 处理ACTION意图的目标提取
  for (const pc of potentialCommands) {
    if (pc.command.intent === 'ACTION' && currentContext?.actions) {
      const { action, similarity } = extractAction(cleanText, currentContext.actions)
      if (action) {
        pc.command.target = action
        pc.command.confidence = Math.max(pc.command.confidence || 0, 0.7 * similarity)
      }
    }
  }

  // 4. 处理FILL_FORM意图的字段匹配
  for (const pc of potentialCommands) {
    if (pc.command.intent === 'FILL_FORM' && currentContext?.setters) {
      const fillMatch = cleanText.match(/^(?:把\s*)?(.+?)\s*(?:是|为|填|写|设为|改成|修改为|更新为|调整为|设置为)\s*(.+)/i)
      if (fillMatch && fillMatch[1] && fillMatch[2]) {
        const fieldName = fillMatch[1]
        const value = fillMatch[2]
        const { field, similarity } = matchField(fieldName, Object.keys(currentContext.setters))
        if (field) {
          pc.command.payload = { [field]: value }
          pc.command.confidence = Math.max(pc.command.confidence || 0, 0.8 * similarity)
          pc.command.message = `已将${fieldName}设为${value}`
        } else {
          // 如果字段匹配失败，降低置信度
          pc.command.confidence = (pc.command.confidence || 0) * 0.5
        }
      }
    }
  }

  // 5. 基于上下文增强命令解析
  for (const pc of potentialCommands) {
    pc.command = enhanceWithContext(pc.command, commandContext)
  }

  // 6. 过滤掉低置信度的命令 (Threshold 0.5)
  const filteredCommands = potentialCommands.filter(pc => pc.command.confidence && pc.command.confidence > 0.5)

  // 7. 按优先级和置信度排序，选择最佳结果
  if (filteredCommands.length > 0) {
    filteredCommands.sort((a, b) => {
      // 先按优先级排序
      if (a.priority !== b.priority) {
        return b.priority - a.priority
      }
      // 优先级相同时，按置信度排序
      return (b.command.confidence || 0) - (a.command.confidence || 0)
    })
    
    return (filteredCommands[0] && filteredCommands[0].command) ? filteredCommands[0].command : {
      intent: 'UNKNOWN', 
      message: '抱歉，我不理解这个指令',
      confidence: 0
    }
  }

  // 8. 最后尝试直接匹配ACTION动作 (Action Fallback)
  if (currentContext?.actions) {
    const { action, similarity } = extractAction(cleanText, currentContext.actions)
    if (action && similarity > 0.7) {
      return {
        intent: 'ACTION',
        target: action,
        message: `正在执行${cleanText}`,
        confidence: 0.7 * similarity
      }
    }
  }

  // 无匹配意图
  return { 
    intent: 'UNKNOWN', 
    message: '抱歉，我不理解这个指令',
    confidence: 0
  }
}

/**
 * 更新命令上下文
 * @param context 现有上下文
 * @param text 用户输入文本
 * @param parsedCommand 解析后的命令
 * @returns 更新后的上下文
 */
export function updateCommandContext(
  context: CommandContext | undefined,
  text: string,
  parsedCommand: ParsedCommand
): CommandContext {
  const now = Date.now()
  
  // 如果没有上下文，创建新的
  if (!context) {
    return {
      conversationId: `conv_${Math.random().toString(36).substr(2, 9)}`,
      lastIntent: parsedCommand.intent,
      lastTarget: parsedCommand.target,
      lastPayload: parsedCommand.payload,
      timestamp: now,
      history: [{
        text,
        intent: parsedCommand.intent,
        target: parsedCommand.target,
        payload: parsedCommand.payload,
        timestamp: now
      }],
      contextVariables: {}
    }
  }
  
  // 更新现有上下文
  return {
    ...context,
    lastIntent: parsedCommand.intent,
    lastTarget: parsedCommand.target,
    lastPayload: parsedCommand.payload,
    timestamp: now,
    history: [
      ...context.history.slice(-9), // 保留最近10条历史记录
      {
        text,
        intent: parsedCommand.intent,
        target: parsedCommand.target,
        payload: parsedCommand.payload,
        timestamp: now
      }
    ],
    contextVariables: {
      ...context.contextVariables
      // 可以在这里添加上下文变量的更新逻辑
    }
  }
}

/**
 * 清理过期上下文
 * @param context 现有上下文
 * @param expirationTime 过期时间（毫秒），默认5分钟
 * @returns 是否过期
 */
export function isContextExpired(
  context: CommandContext,
  expirationTime: number = 5 * 60 * 1000
): boolean {
  return Date.now() - context.timestamp > expirationTime
}

/**
 * 获取上下文变量
 * @param context 对话上下文
 * @param variableName 变量名
 * @returns 变量值
 */
export function getContextVariable(context: CommandContext | undefined, variableName: string): any {
  return context?.contextVariables?.[variableName]
}

/**
 * 设置上下文变量
 * @param context 对话上下文
 * @param variableName 变量名
 * @param value 变量值
 * @returns 更新后的上下文
 */
export function setContextVariable(
  context: CommandContext | undefined,
  variableName: string,
  value: any
): CommandContext {
  const now = Date.now()
  const updatedContext = context || {
    conversationId: `conv_${Math.random().toString(36).substr(2, 9)}`,
    timestamp: now,
    history: [],
    contextVariables: {}
  }
  
  return {
    ...updatedContext,
    timestamp: now,
    contextVariables: {
      ...updatedContext.contextVariables,
      [variableName]: value
    }
  }
}
