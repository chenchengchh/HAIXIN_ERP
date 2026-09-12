import axios from 'axios'

/**
 * 日志级别类型定义
 */
export type LogLevelType = 0 | 1 | 2 | 3 | 4

/**
 * 日志级别常量
 */
export const LogLevel = {
  DEBUG: 0,
  INFO: 1,
  WARN: 2,
  ERROR: 3,
  NONE: 4
} as const

/**
 * 日志级别名称映射
 */
const logLevelNames = {
  0: 'DEBUG',
  1: 'INFO',
  2: 'WARN',
  3: 'ERROR',
  4: 'NONE'
}

/**
 * 日志管理工具
 */
export class Logger {
  private static instance: Logger
  private logLevel: LogLevelType
  private prefix: string

  /**
   * 私有构造函数，防止直接实例化
   */
  private constructor() {
    this.logLevel = LogLevel.INFO
    this.prefix = '[ERP]'
    
    // 从环境变量获取日志级别
    const envLogLevel = import.meta.env.VITE_LOG_LEVEL || 'info'
    this.setLogLevel(envLogLevel)
  }

  /**
   * 获取单例实例
   * @returns Logger实例
   */
  public static getInstance(): Logger {
    if (!Logger.instance) {
      Logger.instance = new Logger()
    }
    return Logger.instance
  }

  /**
   * 设置日志级别
   * @param level 日志级别字符串或数字
   */
  public setLogLevel(level: string | number): void {
    if (typeof level === 'string') {
      const levelMap: Record<string, LogLevelType> = {
        debug: LogLevel.DEBUG,
        info: LogLevel.INFO,
        warn: LogLevel.WARN,
        error: LogLevel.ERROR,
        none: LogLevel.NONE
      }
      this.logLevel = levelMap[level.toLowerCase()] || LogLevel.INFO
    } else {
      this.logLevel = level as LogLevelType
    }
  }

  /**
   * 设置日志前缀
   * @param prefix 前缀字符串
   */
  public setPrefix(prefix: string): void {
    this.prefix = prefix
  }

  /**
   * 生成日志前缀
   * @param level 日志级别
   * @returns 格式化的日志前缀
   */
  private getLogPrefix(level: LogLevelType): string {
    const now = new Date()
    const timestamp = now.toISOString()
    const levelName = logLevelNames[level]
    return `${this.prefix} [${timestamp}] [${levelName}]`
  }

  /**
   * 调试日志
   * @param message 日志消息
   * @param data 附加数据
   */
  public debug(message: string, data?: any): void {
    if (this.logLevel <= LogLevel.DEBUG) {
      console.debug(this.getLogPrefix(LogLevel.DEBUG), message, data)
    }
  }

  /**
   * 信息日志
   * @param message 日志消息
   * @param data 附加数据
   */
  public info(message: string, data?: any): void {
    if (this.logLevel <= LogLevel.INFO) {
      console.info(this.getLogPrefix(LogLevel.INFO), message, data)
    }
  }

  /**
   * 警告日志
   * @param message 日志消息
   * @param data 附加数据
   */
  public warn(message: string, data?: any): void {
    if (this.logLevel <= LogLevel.WARN) {
      console.warn(this.getLogPrefix(LogLevel.WARN), message, data)
    }
  }

  /**
   * 错误日志
   * @param message 日志消息
   * @param error 错误对象
   * @param data 附加数据
   */
  public error(message: string, error?: Error | any, data?: any): void {
    if (this.logLevel <= LogLevel.ERROR) {
      const logData = {
        error: {
          name: error?.name,
          message: error?.message,
          stack: error?.stack,
          ...error
        },
        data
      }
      console.error(this.getLogPrefix(LogLevel.ERROR), message, logData)
    }
  }

  /**
   * API调用日志
   * @param config 请求配置
   * @param response 响应数据
   * @param duration 响应时间
   */
  public apiLog(config: any, response?: any, duration?: number): void {
    if (this.logLevel <= LogLevel.INFO) {
      const logData = {
        url: config.url,
        method: config.method?.toUpperCase(),
        params: config.params,
        data: config.data,
        headers: config.headers,
        status: response?.status,
        statusText: response?.statusText,
        responseData: response?.data,
        duration: duration || 0
      }
      
      if (response?.status && response.status >= 400) {
        // 错误响应
        this.error('API Request Failed', null, logData)
      } else {
        // 成功响应
        this.info('API Request Success', logData)
      }
    }
  }

  /**
   * API错误日志
   * @param config 请求配置
   * @param error 错误对象
   * @param duration 响应时间
   */
  public apiErrorLog(config: any, error: any, duration?: number): void {
    // 忽略取消请求的错误日志，改为debug级别
    if (axios.isCancel(error)) {
      this.debug('API Request Canceled', { url: config?.url, message: error.message })
      return
    }

    if (this.logLevel <= LogLevel.ERROR) {
      const logData = {
        url: config.url,
        method: config.method?.toUpperCase(),
        params: config.params,
        data: config.data,
        headers: config.headers,
        error: {
          name: error?.name,
          message: error?.message,
          stack: error?.stack,
          response: error?.response?.data,
          status: error?.response?.status
        },
        duration: duration || 0
      }
      
      this.error('API Request Error', error, logData)
    }
  }
}

// 导出单例实例
export const logger = Logger.getInstance()