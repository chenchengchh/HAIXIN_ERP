/**
 * 语音助手插件化架构
 * 
 * 支持动态加载和卸载插件，扩展语音助手功能
 */

/**
 * 插件元数据接口
 */
export interface PluginMetadata {
  id: string                // 插件唯一标识
  name: string              // 插件名称
  version: string           // 插件版本
  description?: string      // 插件描述
  author?: string           // 插件作者
  priority?: number         // 插件优先级，数值越高优先级越高
  dependencies?: string[]   // 依赖的其他插件ID
  conflicts?: string[]      // 冲突的插件ID
  enabled?: boolean         // 是否默认启用
  category?: string         // 插件分类
  permissions?: string[]    // 插件所需权限
}

/**
 * 插件事件类型
 */
export enum PluginEventType {
  REGISTER = 'register',
  UNREGISTER = 'unregister',
  START = 'start',
  STOP = 'stop',
  ERROR = 'error',
  CONFIG_CHANGE = 'config_change'
}

/**
 * 插件事件接口
 */
export interface PluginEvent {
  type: PluginEventType
  pluginId: string
  data?: any
  timestamp: number
  error?: Error
}

/**
 * 插件上下文接口
 */
export interface PluginContext {
  // 插件管理器实例
  pluginManager: PluginManager
  // 语音状态存储
  voiceStore: any
  // 日志记录
  logger: {
    info: (message: string) => void
    warn: (message: string) => void
    error: (message: string) => void
    debug: (message: string) => void
  }
  // 配置管理
  configManager: {
    get: (key: string, defaultValue?: any) => any
    set: (key: string, value: any) => Promise<void>
    getAll: () => Record<string, any>
    remove: (key: string) => Promise<void>
  }
  // 事件总线
  eventBus: {
    emit: (event: string, data?: any) => void
    on: (event: string, callback: (data?: any) => void) => void
    off: (event: string, callback: (data?: any) => void) => void
  }
}

/**
 * 插件接口
 */
export interface VoicePlugin {
  // 插件元数据
  metadata: PluginMetadata
  
  // 插件初始化
  init(context: PluginContext): Promise<void>
  
  // 插件启动
  start(): Promise<void>
  
  // 插件停止
  stop(): Promise<void>
  
  // 插件卸载
  destroy(): Promise<void>
  
  // 处理语音命令（可选）
  handleCommand?(text: string, context: any): Promise<any> | any
  
  // 处理语音识别结果（可选）
  handleSpeechResult?(result: any): Promise<void> | void
  
  // 处理语音合成（可选）
  handleTts?(text: string): Promise<string> | string
  
  // 获取插件配置（可选）
  getConfig?(): any
  
  // 设置插件配置（可选）
  setConfig?(config: any): Promise<void> | void
  
  // 处理插件事件（可选）
  handleEvent?(event: PluginEvent): Promise<void> | void
  
  // 插件健康检查（可选）
  healthCheck?(): Promise<{ status: 'healthy' | 'unhealthy'; message?: string }>
}

/**
 * 插件状态
 */
export enum PluginState {
  UNLOADED = 'unloaded',
  LOADED = 'loaded',
  INITIALIZED = 'initialized',
  STARTED = 'started',
  STOPPED = 'stopped',
  ERROR = 'error'
}

/**
 * 插件实例状态
 */
export interface PluginInstance {
  plugin: VoicePlugin
  state: PluginState
  context: PluginContext
  error?: Error
  config?: Record<string, any> // 插件配置
  startTime?: number          // 插件启动时间
  lastHealthCheck?: number    // 上次健康检查时间
}

/**
 * 插件管理器类
 */
export class PluginManager {
  private plugins: Map<string, PluginInstance> = new Map()
  private pluginContext: PluginContext
  private isInitialized: boolean = false
  private eventHandlers: Map<string, Array<(event: PluginEvent) => void>> = new Map()
  private configStore: Map<string, any> = new Map()
  private healthCheckInterval: number | null = null
  private healthCheckIntervalMs: number = 30000 // 30秒健康检查一次

  /**
   * 构造函数
   * @param voiceStore 语音状态存储
   */
  constructor(voiceStore: any) {
    // 初始化插件上下文
    this.pluginContext = {
      pluginManager: this,
      voiceStore,
      logger: {
        info: (message: string) => console.log(`[Plugin] ${message}`),
        warn: (message: string) => console.warn(`[Plugin] ${message}`),
        error: (message: string) => console.error(`[Plugin] ${message}`),
        debug: (message: string) => console.debug(`[Plugin] ${message}`)
      },
      configManager: {
        get: (key: string, defaultValue?: any) => {
          const config = this.configStore.get(key)
          return config !== undefined ? config : defaultValue
        },
        set: async (key: string, value: any) => {
          this.configStore.set(key, value)
          this.emitEvent(PluginEventType.CONFIG_CHANGE, { key, value })
        },
        getAll: () => {
          return Object.fromEntries(this.configStore.entries())
        },
        remove: async (key: string) => {
          this.configStore.delete(key)
          this.emitEvent(PluginEventType.CONFIG_CHANGE, { key, value: undefined })
        }
      },
      eventBus: {
        emit: (event: string, data?: any) => {
          this.emitEvent(event as PluginEventType, data)
        },
        on: (event: string, callback: (data?: any) => void) => {
          this.onEvent(event as PluginEventType, callback as (event: PluginEvent) => void)
        },
        off: (event: string, callback: (data?: any) => void) => {
          this.offEvent(event as PluginEventType, callback as (event: PluginEvent) => void)
        }
      }
    }
  }

  /**
   * 初始化插件管理器
   */
  async init(): Promise<void> {
    if (this.isInitialized) return
    this.isInitialized = true
    this.pluginContext.logger.info('Plugin manager initialized')
    
    // 启动健康检查
    this.startHealthChecks()
  }

  /**
   * 注册插件
   * @param plugin 插件实例
   */
  async registerPlugin(plugin: VoicePlugin): Promise<void> {
    const id = plugin.metadata.id
    
    if (this.plugins.has(id)) {
      this.pluginContext.logger.warn(`Plugin ${id} already registered`)
      return
    }

    // 检查冲突
    if (plugin.metadata.conflicts) {
      for (const conflictId of plugin.metadata.conflicts) {
        if (this.plugins.has(conflictId)) {
          this.pluginContext.logger.error(`Plugin ${id} conflicts with ${conflictId}, which is already registered`)
          return
        }
      }
    }

    // 检查依赖
    if (plugin.metadata.dependencies) {
      for (const depId of plugin.metadata.dependencies) {
        if (!this.plugins.has(depId)) {
          this.pluginContext.logger.warn(`Plugin ${id} depends on ${depId}, which is not registered`)
          // 依赖缺失，尝试延迟初始化或跳过
        }
      }
    }

    // 创建插件实例状态
    const instance: PluginInstance = {
      plugin,
      state: PluginState.LOADED,
      context: this.pluginContext
    }

    // 初始化插件
    try {
      await plugin.init(this.pluginContext)
      instance.state = PluginState.INITIALIZED
      this.plugins.set(id, instance)
      this.pluginContext.logger.info(`Plugin ${id} registered and initialized`)
      
      // 发送注册事件
      this.emitEvent(PluginEventType.REGISTER, { pluginId: id })
      
      // 如果插件默认启用，启动插件
      if (plugin.metadata.enabled !== false) {
        await this.startPlugin(id)
      }
    } catch (error) {
      instance.state = PluginState.ERROR
      instance.error = error as Error
      this.plugins.set(id, instance)
      this.pluginContext.logger.error(`Failed to register plugin ${id}: ${error}`)
      
      // 发送错误事件
      this.emitEvent(PluginEventType.ERROR, { pluginId: id, error })
    }
  }

  /**
   * 加载并注册插件
   * @param pluginModule 插件模块
   */
  async loadPlugin(pluginModule: any): Promise<void> {
    try {
      // 假设插件模块导出一个默认的插件实例或工厂函数
      const plugin = typeof pluginModule === 'function' 
        ? new pluginModule() 
        : pluginModule.default || pluginModule
      
      if (plugin && plugin.metadata && plugin.init) {
        await this.registerPlugin(plugin)
      } else {
        this.pluginContext.logger.error('Invalid plugin module')
      }
    } catch (error) {
      this.pluginContext.logger.error(`Failed to load plugin: ${error}`)
    }
  }

  /**
   * 批量加载插件
   * @param pluginModules 插件模块列表
   */
  async loadPlugins(pluginModules: any[]): Promise<void> {
    for (const module of pluginModules) {
      await this.loadPlugin(module)
    }
  }

  /**
   * 从URL加载插件
   * @param url 插件URL
   */
  async loadPluginFromUrl(url: string): Promise<void> {
    try {
      const response = await fetch(url)
      const pluginModule = await response.json()
      await this.loadPlugin(pluginModule)
    } catch (error) {
      this.pluginContext.logger.error(`Failed to load plugin from URL ${url}: ${error}`)
    }
  }

  /**
   * 卸载插件
   * @param id 插件ID
   */
  async unregisterPlugin(id: string): Promise<void> {
    const instance = this.plugins.get(id)
    if (!instance) {
      this.pluginContext.logger.warn(`Plugin ${id} not found`)
      return
    }

    try {
      // 停止插件
      if (instance.state === PluginState.STARTED) {
        await this.stopPlugin(id)
      }
      
      // 销毁插件
      await instance.plugin.destroy()
      
      // 移除插件
      this.plugins.delete(id)
      this.pluginContext.logger.info(`Plugin ${id} unregistered`)
      
      // 发送卸载事件
      this.emitEvent(PluginEventType.UNREGISTER, { pluginId: id })
    } catch (error) {
      this.pluginContext.logger.error(`Failed to unregister plugin ${id}: ${error}`)
      
      // 发送错误事件
      this.emitEvent(PluginEventType.ERROR, { pluginId: id, error })
    }
  }

  /**
   * 启动插件
   * @param id 插件ID
   */
  async startPlugin(id: string): Promise<void> {
    const instance = this.plugins.get(id)
    if (!instance) {
      this.pluginContext.logger.warn(`Plugin ${id} not found`)
      return
    }

    if (instance.state === PluginState.STARTED) {
      this.pluginContext.logger.warn(`Plugin ${id} is already started`)
      return
    }

    try {
      await instance.plugin.start()
      instance.state = PluginState.STARTED
      instance.startTime = Date.now()
      this.pluginContext.logger.info(`Plugin ${id} started`)
      
      // 发送启动事件
      this.emitEvent(PluginEventType.START, { pluginId: id })
    } catch (error) {
      instance.state = PluginState.ERROR
      instance.error = error as Error
      this.pluginContext.logger.error(`Failed to start plugin ${id}: ${error}`)
      
      // 发送错误事件
      this.emitEvent(PluginEventType.ERROR, { pluginId: id, error })
    }
  }

  /**
   * 停止插件
   * @param id 插件ID
   */
  async stopPlugin(id: string): Promise<void> {
    const instance = this.plugins.get(id)
    if (!instance) {
      this.pluginContext.logger.warn(`Plugin ${id} not found`)
      return
    }

    if (instance.state !== PluginState.STARTED) {
      this.pluginContext.logger.warn(`Plugin ${id} is not started`)
      return
    }

    try {
      await instance.plugin.stop()
      instance.state = PluginState.STOPPED
      this.pluginContext.logger.info(`Plugin ${id} stopped`)
      
      // 发送停止事件
      this.emitEvent(PluginEventType.STOP, { pluginId: id })
    } catch (error) {
      instance.state = PluginState.ERROR
      instance.error = error as Error
      this.pluginContext.logger.error(`Failed to stop plugin ${id}: ${error}`)
      
      // 发送错误事件
      this.emitEvent(PluginEventType.ERROR, { pluginId: id, error })
    }
  }

  /**
   * 重启插件
   * @param id 插件ID
   */
  async restartPlugin(id: string): Promise<void> {
    await this.stopPlugin(id)
    await this.startPlugin(id)
  }

  /**
   * 启动所有插件
   */
  async startAllPlugins(): Promise<void> {
    // 按优先级排序插件，确保依赖先启动
    const sortedPlugins = this.getSortedPlugins()

    for (const instance of sortedPlugins) {
      if (instance.state === PluginState.INITIALIZED || instance.state === PluginState.STOPPED) {
        await this.startPlugin(instance.plugin.metadata.id)
      }
    }
  }

  /**
   * 停止所有插件
   */
  async stopAllPlugins(): Promise<void> {
    // 按优先级反向排序插件，确保依赖后停止
    const sortedPlugins = this.getSortedPlugins().reverse()

    for (const instance of sortedPlugins) {
      if (instance.state === PluginState.STARTED) {
        await this.stopPlugin(instance.plugin.metadata.id)
      }
    }
  }

  /**
   * 获取插件
   * @param id 插件ID
   * @returns 插件实例
   */
  getPlugin(id: string): PluginInstance | undefined {
    return this.plugins.get(id)
  }

  /**
   * 获取所有插件
   * @returns 所有插件实例
   */
  getAllPlugins(): PluginInstance[] {
    return Array.from(this.plugins.values())
  }

  /**
   * 获取已启用的插件
   * @returns 已启用的插件实例
   */
  getEnabledPlugins(): PluginInstance[] {
    return Array.from(this.plugins.values())
      .filter(instance => instance.state === PluginState.STARTED)
  }

  /**
   * 按插件优先级和依赖关系排序
   * @returns 排序后的插件实例列表
   */
  private getSortedPlugins(): PluginInstance[] {
    // 简单的拓扑排序实现
    const visited = new Set<string>()
    const result: PluginInstance[] = []
    
    const visit = (instance: PluginInstance) => {
      if (visited.has(instance.plugin.metadata.id)) return
      visited.add(instance.plugin.metadata.id)
      
      // 先处理依赖
      if (instance.plugin.metadata.dependencies) {
        for (const depId of instance.plugin.metadata.dependencies) {
          const depInstance = this.plugins.get(depId)
          if (depInstance) {
            visit(depInstance)
          }
        }
      }
      
      result.push(instance)
    }
    
    // 按优先级排序
    const sortedByPriority = Array.from(this.plugins.values())
      .sort((a, b) => (b.plugin.metadata.priority || 0) - (a.plugin.metadata.priority || 0))
    
    // 进行拓扑排序
    for (const instance of sortedByPriority) {
      visit(instance)
    }
    
    return result
  }

  /**
   * 处理语音命令，通过所有插件
   * @param text 语音文本
   * @param context 上下文
   * @returns 处理结果
   */
  async processCommand(text: string, context: any): Promise<any> {
    // 按优先级排序插件
    const sortedPlugins = this.getSortedPlugins()
      .filter(instance => instance.state === PluginState.STARTED)

    for (const instance of sortedPlugins) {
      if (instance.plugin.handleCommand) {
        try {
          const result = await instance.plugin.handleCommand(text, context)
          if (result !== undefined) {
            return result
          }
        } catch (error) {
          this.pluginContext.logger.error(`Plugin ${instance.plugin.metadata.id} failed to handle command: ${error}`)
          // 错误恢复：继续执行其他插件
        }
      }
    }

    return undefined
  }

  /**
   * 处理语音识别结果，通过所有插件
   * @param result 语音识别结果
   */
  async processSpeechResult(result: any): Promise<void> {
    // 按优先级排序插件
    const sortedPlugins = this.getSortedPlugins()
      .filter(instance => instance.state === PluginState.STARTED)

    for (const instance of sortedPlugins) {
      if (instance.plugin.handleSpeechResult) {
        try {
          await instance.plugin.handleSpeechResult(result)
        } catch (error) {
          this.pluginContext.logger.error(`Plugin ${instance.plugin.metadata.id} failed to handle speech result: ${error}`)
        }
      }
    }
  }

  /**
   * 处理语音合成，通过所有插件
   * @param text 要合成的文本
   * @returns 处理后的文本
   */
  async processTtsText(text: string): Promise<string> {
    let processedText = text

    // 按优先级排序插件
    const sortedPlugins = this.getSortedPlugins()
      .filter(instance => instance.state === PluginState.STARTED)

    for (const instance of sortedPlugins) {
      if (instance.plugin.handleTts) {
        try {
          const result = await instance.plugin.handleTts(processedText)
          if (result !== undefined) {
            processedText = result
          }
        } catch (error) {
          this.pluginContext.logger.error(`Plugin ${instance.plugin.metadata.id} failed to handle TTS: ${error}`)
        }
      }
    }

    return processedText
  }
  
  /**
   * 发送插件事件
   * @param type 事件类型
   * @param data 事件数据
   */
  private emitEvent(type: PluginEventType, data?: any): void {
    const event: PluginEvent = {
      type,
      pluginId: data?.pluginId || '',
      data,
      timestamp: Date.now(),
      error: data?.error
    }
    
    // 触发所有插件的事件处理
    for (const instance of this.plugins.values()) {
      if (instance.state === PluginState.STARTED && instance.plugin.handleEvent) {
        try {
          instance.plugin.handleEvent(event)
        } catch (error) {
          this.pluginContext.logger.error(`Plugin ${instance.plugin.metadata.id} failed to handle event: ${error}`)
        }
      }
    }
    
    // 触发外部事件监听器
    const handlers = this.eventHandlers.get(type)
    if (handlers) {
      for (const handler of handlers) {
        try {
          handler(event)
        } catch (error) {
          this.pluginContext.logger.error(`Event handler failed: ${error}`)
        }
      }
    }
  }
  
  /**
   * 监听插件事件
   * @param type 事件类型
   * @param callback 事件回调
   */
  onEvent(type: PluginEventType, callback: (event: PluginEvent) => void): void {
    if (!this.eventHandlers.has(type)) {
      this.eventHandlers.set(type, [])
    }
    this.eventHandlers.get(type)?.push(callback)
  }
  
  /**
   * 取消监听插件事件
   * @param type 事件类型
   * @param callback 事件回调
   */
  offEvent(type: PluginEventType, callback: (event: PluginEvent) => void): void {
    const handlers = this.eventHandlers.get(type)
    if (handlers) {
      this.eventHandlers.set(type, handlers.filter(h => h !== callback))
    }
  }
  
  /**
   * 启动插件健康检查
   */
  private startHealthChecks(): void {
    if (this.healthCheckInterval) {
      clearInterval(this.healthCheckInterval)
    }
    
    this.healthCheckInterval = setInterval(async () => {
      await this.runHealthChecks()
    }, this.healthCheckIntervalMs)
  }
  
  /**
   * 运行插件健康检查
   */
  private async runHealthChecks(): Promise<void> {
    for (const instance of this.plugins.values()) {
      if (instance.state === PluginState.STARTED && instance.plugin.healthCheck) {
        try {
          const result = await instance.plugin.healthCheck()
          instance.lastHealthCheck = Date.now()
          
          if (result.status === 'unhealthy') {
            this.pluginContext.logger.warn(`Plugin ${instance.plugin.metadata.id} is unhealthy: ${result.message}`)
            // 尝试自动恢复
            await this.restartPlugin(instance.plugin.metadata.id)
          }
        } catch (error) {
          this.pluginContext.logger.error(`Health check failed for plugin ${instance.plugin.metadata.id}: ${error}`)
        }
      }
    }
  }
  
  /**
   * 获取插件健康状态
   * @returns 插件健康状态列表
   */
  async getHealthStatus(): Promise<Array<{
    pluginId: string
    name: string
    status: 'healthy' | 'unhealthy' | 'unknown'
    message?: string
    lastCheck?: number
  }>> {
    const result: Array<{
      pluginId: string
      name: string
      status: 'healthy' | 'unhealthy' | 'unknown'
      message?: string
      lastCheck?: number
    }> = []
    
    for (const instance of this.plugins.values()) {
      let status: 'healthy' | 'unhealthy' | 'unknown' = 'unknown'
      let message: string | undefined
      
      if (instance.state === PluginState.STARTED && instance.plugin.healthCheck) {
        try {
          const checkResult = await instance.plugin.healthCheck()
          status = checkResult.status
          message = checkResult.message
        } catch (error) {
          status = 'unhealthy'
          message = `Health check failed: ${error}`
        }
      }
      
      result.push({
        pluginId: instance.plugin.metadata.id,
        name: instance.plugin.metadata.name,
        status,
        message,
        lastCheck: instance.lastHealthCheck
      })
    }
    
    return result
  }
}

/**
 * 插件基类，提供默认实现
 */
export abstract class BaseVoicePlugin implements VoicePlugin {
  abstract metadata: PluginMetadata
  
  context: PluginContext | null = null
  
  async init(context: PluginContext): Promise<void> {
    this.context = context
    context.logger.info(`Plugin ${this.metadata.id} initialized`)
  }
  
  async start(): Promise<void> {
    this.context?.logger.info(`Plugin ${this.metadata.id} started`)
  }
  
  async stop(): Promise<void> {
    this.context?.logger.info(`Plugin ${this.metadata.id} stopped`)
  }
  
  async destroy(): Promise<void> {
    this.context?.logger.info(`Plugin ${this.metadata.id} destroyed`)
    this.context = null
  }
  
  handleCommand?(text: string, context: any): Promise<any> | any {
    return undefined
  }
  
  handleSpeechResult?(result: any): Promise<void> | void {
    return
  }
  
  handleTts?(text: string): Promise<string> | string {
    return text
  }
  
  handleEvent?(event: PluginEvent): Promise<void> | void {
    this.context?.logger.debug(`Plugin ${this.metadata.id} received event: ${event.type}`)
    return
  }
  
  async healthCheck?(): Promise<{ status: 'healthy' | 'unhealthy'; message?: string }> {
    return { status: 'healthy' }
  }
  
  getConfig?(): any {
    return null
  }
  
  setConfig?(config: any): Promise<void> | void {
    return
  }
}
