/**
 * 统一异常处理工具
 */

import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { logger } from './logger'
import { redirectToLoginOnce } from './auth-redirect'

/**
 * 错误类型枚举
 */
export enum ErrorType {
  /** 网络错误 */
  NETWORK_ERROR = 'NETWORK_ERROR',
  /** 超时错误 */
  TIMEOUT_ERROR = 'TIMEOUT_ERROR',
  /** 服务器错误 */
  SERVER_ERROR = 'SERVER_ERROR',
  /** 客户端错误 */
  CLIENT_ERROR = 'CLIENT_ERROR',
  /** 认证错误 */
  AUTH_ERROR = 'AUTH_ERROR',
  /** 权限错误 */
  PERMISSION_ERROR = 'PERMISSION_ERROR',
  /** 业务逻辑错误 */
  BUSINESS_ERROR = 'BUSINESS_ERROR',
  /** 未知错误 */
  UNKNOWN_ERROR = 'UNKNOWN_ERROR'
}

/**
 * 错误信息配置
 */
const errorMessages: Record<ErrorType, string> = {
  [ErrorType.NETWORK_ERROR]: '网络连接失败，请检查您的网络设置',
  [ErrorType.TIMEOUT_ERROR]: '请求超时，请稍后重试',
  [ErrorType.SERVER_ERROR]: '服务器内部错误，请联系管理员',
  [ErrorType.CLIENT_ERROR]: '请求参数错误，请检查输入',
  [ErrorType.AUTH_ERROR]: '登录已过期，请重新登录',
  [ErrorType.PERMISSION_ERROR]: '您没有权限执行此操作',
  [ErrorType.BUSINESS_ERROR]: '业务逻辑错误',
  [ErrorType.UNKNOWN_ERROR]: '发生未知错误，请联系管理员'
}

/**
 * API错误响应接口
 */
export interface ApiErrorResponse {
  /** 错误码 */
  code: string | number
  /** 错误消息 */
  message: string
  /** 错误详情 */
  detail?: string
  /** 错误数据 */
  data?: any
}

/**
 * 错误处理器类
 */
export class ErrorHandler {
  /**
   * 处理API错误
   * @param error 错误对象
   * @param showMessage 是否显示错误信息
   * @returns 错误类型
   */
  static handleApiError(error: any, showMessage = true): ErrorType {
    let errorType = ErrorType.UNKNOWN_ERROR
    let errorMessage = errorMessages[ErrorType.UNKNOWN_ERROR]

    // 忽略请求取消错误
    if (axios.isCancel(error)) {
      logger.debug('Request canceled', error.message)
      return ErrorType.UNKNOWN_ERROR
    }

    try {
      // 处理Axios错误
      if (error && (error.isAxiosError || error.response)) {
        const axiosError = error
        if (!axiosError.response) {
          if (axiosError.code === 'ECONNABORTED') {
            errorType = ErrorType.TIMEOUT_ERROR
            errorMessage = errorMessages[ErrorType.TIMEOUT_ERROR]
          } else {
            errorType = ErrorType.NETWORK_ERROR
            errorMessage = axiosError.message || errorMessages[ErrorType.NETWORK_ERROR]
          }
        } else {
          const { status, data } = axiosError.response
          const errorInfo = data?.message || data?.msg || data?.errorMessage || axiosError.response.statusText || '未知错误'
          
          switch (status) {
            case 400:
              errorType = ErrorType.CLIENT_ERROR
              errorMessage = `请求参数错误：${errorInfo}`
              break
            case 401:
              errorType = ErrorType.AUTH_ERROR
              errorMessage = errorMessages[ErrorType.AUTH_ERROR]
              this.handleLoginExpired()
              break
            case 403:
              errorType = ErrorType.PERMISSION_ERROR
              errorMessage = errorMessages[ErrorType.PERMISSION_ERROR]
              break
            case 404:
              errorType = ErrorType.CLIENT_ERROR
              errorMessage = `请求资源不存在：${axiosError.config?.url}`
              break
            case 500:
              errorType = ErrorType.SERVER_ERROR
              errorMessage = `服务器错误 (500)：${errorInfo}`
              break
            default:
              if (status >= 500) {
                errorType = ErrorType.SERVER_ERROR
                errorMessage = `服务器错误 (${status})：${errorInfo}`
              } else {
                errorType = ErrorType.CLIENT_ERROR
                errorMessage = `客户端错误 (${status})：${errorInfo}`
              }
          }
        }
      } 
      // 处理业务错误
      else if (error && error.code) {
        errorType = ErrorType.BUSINESS_ERROR
        errorMessage = error.message || error.msg || errorMessages[ErrorType.BUSINESS_ERROR]
      }
      // 处理普通错误
      else if (error instanceof Error) {
        errorMessage = error.message || errorMessages[ErrorType.UNKNOWN_ERROR]
      }
      // 处理字符串错误
      else if (typeof error === 'string') {
        errorMessage = error
      }
    } catch (e) {
      logger.error('Error handling error:', e)
    }

    // 显示错误消息
    if (showMessage && errorType !== ErrorType.AUTH_ERROR) {
      ElMessage.error(errorMessage)
    }

    // 记录详细日志
    this.logError(error, errorMessage)

    return errorType
  }

  /**
   * 显示错误提示
   */
  static showErrorMessage(message: string, type: ErrorType = ErrorType.UNKNOWN_ERROR): void {
    if (type === ErrorType.AUTH_ERROR) {
      this.handleLoginExpired()
    } else {
      ElMessage.error(message)
    }
  }

  /**
   * 处理登录过期
   */
  static handleLoginExpired(): void {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')

    ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      void redirectToLoginOnce({ replace: true })
    }).catch(() => {})
  }

  /**
   * 处理表单验证错误
   */
  static handleFormError(errors: any): string {
    if (typeof errors === 'string') return errors
    if (Array.isArray(errors)) return errors[0] || '表单验证失败'
    if (typeof errors === 'object' && errors !== null) {
      const firstError = Object.values(errors)[0]
      if (Array.isArray(firstError)) return firstError[0] || '表单验证失败'
      return String(firstError) || '表单验证失败'
    }
    return '表单验证失败'
  }

  /**
   * 处理网络错误
   */
  static handleNetworkError(): void {
    ElMessage.warning('网络连接异常，请检查网络设置')
  }

  /**
   * 记录错误日志
   */
  static logError(error: any, context?: string): void {
    const logData = {
      timestamp: new Date().toISOString(),
      error: error,
      context,
      url: typeof window !== 'undefined' ? window.location.href : 'unknown'
    }
    logger.error('Exception Detected', error, logData)
  }
}

/**
 * API错误处理装饰器
 */
export function handleApiError(_target: any, _propertyKey: string, descriptor: PropertyDescriptor): void {
  const originalMethod = descriptor.value
  descriptor.value = async function (...args: any[]) {
    try {
      return await originalMethod.apply(this, args)
    } catch (error) {
      ErrorHandler.handleApiError(error)
      throw error
    }
  }
}
