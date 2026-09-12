import axios from 'axios'
import type { AxiosInstance, AxiosResponse, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { DataTransformer } from '../utils/data-transformer'
import { logger } from '../utils/logger'
import { RequestCache } from '../utils/request-cache'
import { ErrorHandler } from '../utils/error-handler'
import { redirectToLoginOnce } from '../utils/auth-redirect'
import { useAppStore } from '../stores/app'
import { useAuthStore } from '../stores/auth'

const normalizeApiUrl = (url?: string) => {
  if (!url) return url
  if (/^https?:\/\//i.test(url)) return url
  if (url.startsWith('/api/v1/')) return url
  if (url.startsWith('/api/')) {
    const m = url.match(/^\/api\/(agv|les|mes|erp|crm|srm|scm|wms|aps|oa|hr|plm|bom|qms|scada|ems|opportunity|douyin)(\/|$)/i)
    if (!m) return url
    return url.replace(/^\/api\//i, '/api/v1/')
  }
  if (url.startsWith('/ws/')) return url
  if (url.startsWith('/ai-voice')) return url
  if (/^\/[a-z-]+\/v\d+(\/|$)/i.test(url)) return url

  const m = url.match(/^\/(agv|les|mes|erp|crm|srm|scm|wms|aps|oa|hr|plm|bom|qms|scada|ems|opportunity|douyin)(\/|$)/i)
  if (!m) return url
  return `/api/v1${url}`
}

const buildNetworkHint = (config?: InternalAxiosRequestConfig) => {
  const baseURL = String((config as any)?.baseURL || '')
  if (!baseURL) {
    return '服务器无响应，请检查后端网关/代理是否已启动'
  }
  const isLocalhost = /^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?/i.test(baseURL)
  if (isLocalhost) {
    return '服务器无响应：当前配置直连本机网关，请启动网关(默认9000)或清空VITE_API_BASE_URL改用同源代理'
  }
  return '服务器无响应，请检查网络连接'
}

const handleUnauthorized = () => {
  const authStore = useAuthStore()
  authStore.logout()
  void redirectToLoginOnce({ replace: true })
}

// 创建Axios实例
const axiosInstance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: Number(import.meta.env.VITE_API_TIMEOUT) || 10000, // 请求超时时间，从环境变量获取
  headers: {
    'Content-Type': 'application/json;charset=UTF-8',
    'X-Requested-With': 'XMLHttpRequest',
    'Accept': 'application/json;charset=UTF-8'
  },
  // 响应数据处理，axios默认已处理UTF-8编码
  transformResponse: [function (data, headers, status) {
    if (data === null || data === undefined) return data
    if (status === 204) {
      return {
        code: 200,
        msg: '',
        data: null
      }
    }

    if (typeof data !== 'string') return data

    const trimmed = data.trim()
    if (!trimmed) {
      return {
        code: 200,
        msg: '',
        data: null
      }
    }

    const lower = trimmed.toLowerCase()
    const isHtml = lower.startsWith('<!doctype html') || lower.startsWith('<html') || trimmed.includes('<!--')
    if (isHtml) {
      return {
        code: 500,
        msg: '后端返回HTML响应，可能是服务器错误或路由配置问题',
        data: null
      }
    }

    const contentType = String((headers as any)?.['content-type'] || (headers as any)?.['Content-Type'] || '')
    const looksLikeJson = trimmed.startsWith('{') || trimmed.startsWith('[')
    const shouldParseJson = looksLikeJson || contentType.includes('application/json') || contentType.includes('+json')

    if (!shouldParseJson) {
      return {
        code: 200,
        msg: '请求成功',
        data: trimmed
      }
    }

    try {
      const parsed = JSON.parse(trimmed)
      if (parsed !== null && typeof parsed === 'object') return parsed
      return {
        code: 200,
        msg: '请求成功',
        data: parsed
      }
    } catch (e) {
      logger.debug('API Response Invalid JSON', { contentType, status })
      return {
        code: 500,
        msg: '后端返回无效的JSON格式，无法解析',
        data: null
      }
    }
  }]
})

// 请求拦截器
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 记录请求开始时间
    (config as any).startTime = Date.now()

    config.url = normalizeApiUrl(config.url)
    
    // 认证信息统一通过 auth store 读取，避免请求层和存储层双写分叉
    const authStore = useAuthStore()
    const token = authStore.getToken()

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 统一使用标准请求头，确保网关与业务服务能串联同一条链路
    const rid = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
    config.headers['X-Request-Id'] = rid
    config.headers['X-Trace-Id'] = rid
    // 保留历史兼容头，避免旧链路在改造过渡期丢失请求ID
    config.headers['X-Request-ID'] = rid
    // 使用 metadata 存储元数据，避免直接挂载属性导致冲突
    ;(config as any).metadata = { traceId: rid, requestId: rid }
    
    // 添加时间戳，防止缓存
    if (config.method?.toUpperCase() === 'GET') {
      // 确保params是对象类型
      if (config.params === undefined || config.params === null) {
        config.params = {}
      } else if (typeof config.params !== 'object' || Array.isArray(config.params)) {
        // 如果params不是对象类型（如数字、字符串等），则将其作为id参数处理
        config.params = {
          id: config.params
        }
      }
      config.params['_t'] = Date.now()
    }
    
    // 确保Content-Type正确设置
    if (!config.headers['Content-Type'] && config.method?.toUpperCase() !== 'GET') {
      config.headers['Content-Type'] = 'application/json;charset=UTF-8'
    }
    
    // 记录请求日志
    logger.debug('API Request Config:', config)
    
    // 设置加载状态
    const appStore = useAppStore()
    const requestKey = `${config.method?.toUpperCase() || 'GET'}:${config.url}`
    appStore.setRequestLoading(requestKey, true)
    
    return config
  },
  (error: AxiosError) => {
    // 处理请求错误
    logger.error('API Request Error:', error)
    
    // 清除加载状态
    if (error.config) {
      const appStore = useAppStore()
      const requestKey = `${error.config.method?.toUpperCase() || 'GET'}:${error.config.url}`
      appStore.setRequestLoading(requestKey, false)
      
      // 设置错误信息
      appStore.setRequestError(requestKey, {
        message: error.message || '请求失败',
        code: error.code as number | undefined
      })
    }
    
    return Promise.reject(error)
  }
)

// 响应拦截器
axiosInstance.interceptors.response.use(
  (response: AxiosResponse) => {
    // 计算响应时间
    const startTime = (response.config as any).startTime || Date.now()
    const duration = Date.now() - startTime
    
    // 清除加载状态
    const appStore = useAppStore()
    const requestKey = `${response.config.method?.toUpperCase() || 'GET'}:${response.config.url}`
    
    try {
      // 记录API响应日志
      const logConfig = { ...response.config }
      logger.apiLog(logConfig, response, duration)
      
      // 处理响应数据
      let data = response.data

      const responseType = (response.config as any)?.responseType
      if (responseType === 'blob' || responseType === 'arraybuffer') {
        appStore.setRequestLoading(requestKey, false)
        appStore.setRequestError(requestKey, null)
        return response
      }
      
      // 使用数据转换工具标准化响应数据
      data = DataTransformer.normalizeResponse(data)
      
      // 统一数据格式处理
      if (DataTransformer.isSuccessCode(data.code)) {
          // 清除加载状态
          appStore.setRequestLoading(requestKey, false)
          
          // 清除错误信息
          appStore.setRequestError(requestKey, null)
          
          // 设置成功信息（如果有）
          if (data.msg) {
            appStore.setSuccessMessage(data.msg)
          }
          
          // 返回修改后的AxiosResponse对象，保持类型一致
          response.data = data
          return response
        } else {
          // 处理401未授权
          if (data.code === 401) {
            logger.warn('未授权访问，跳转到登录页面')
            handleUnauthorized()
          }

          // 业务逻辑错误
          logger.error('API Business Error:', null, { msg: data.msg, code: data.code })
          
          // 清除加载状态
          appStore.setRequestLoading(requestKey, false)
          
          // 设置错误信息
          appStore.setRequestError(requestKey, {
            message: data.msg || '请求失败',
            code: data.code
          })
          
          // 创建业务错误对象，包含data属性以保持格式一致
          const businessError = {
            data: {
              code: data.code,
              msg: data.msg || '请求失败'
            },
            isAxiosError: false
          } as any
          return Promise.reject(businessError)
        }
    } catch (error) {
      logger.error('响应处理错误:', error)
      
      // 清除加载状态
      appStore.setRequestLoading(requestKey, false)
      
      // 设置错误信息
      appStore.setRequestError(requestKey, {
        message: '响应处理错误',
        code: 500
      })
      
      // 处理响应处理错误
      const businessError = {
        code: 500,
        message: '响应处理错误',
        isAxiosError: false
      } as any
      return Promise.reject(businessError)
    }
  },
  (error: AxiosError) => {
    // 计算响应时间
    const startTime = (error.config as any)?.startTime || Date.now()
    const duration = Date.now() - startTime
    
    // 记录API错误日志
    const errorLogConfig = error.config ? { ...error.config } : undefined
    logger.apiErrorLog(errorLogConfig, error, duration)
    
    // 清除加载状态
    if (error.config) {
      const appStore = useAppStore()
      const requestKey = `${error.config.method?.toUpperCase() || 'GET'}:${error.config.url}`
      appStore.setRequestLoading(requestKey, false)
      
      // 设置错误信息
      let errorMessage = '请求失败'
      let errorCode = 500
      
      if (error.response) {
        // 服务器返回错误响应
        if (error.response.status === 401) {
          // 处理401未授权
          logger.warn('未授权访问，跳转到登录页面')
          handleUnauthorized()
        }
        errorMessage = (error.response.data as any)?.msg || error.response.statusText || errorMessage
        errorCode = error.response.status
      } else if (error.request) {
        // 请求发送但没有收到响应
        errorMessage = buildNetworkHint(error.config as any)
      } else {
        // 请求配置错误
        errorMessage = error.message || errorMessage
      }
      
      appStore.setRequestError(requestKey, {
        message: errorMessage,
        code: errorCode
      })
    }
    
    // 使用统一的错误处理工具处理错误
    ErrorHandler.handleApiError(error, false)
    
    return Promise.reject(error)
  }
)

// 创建带缓存的API实例
/**
 * 清除写操作相关的缓存。
 * 子资源写操作（如 PUT /orders/9 或 POST /orders/9/ship）的URL前缀无法命中
 * 父集合列表缓存（GET /orders），需额外清除父集合前缀，避免列表60秒内不刷新。
 * @param url 写操作请求URL
 */
const clearWriteCache = (url: string) => {
  const cache = RequestCache.getInstance()
  cache.clearCache(url)
  // 匹配以 数字ID 或 数字ID+动作段 结尾的URL，取其父集合路径一并清除
  const parentMatch = url.match(/^(.*)\/\d+(?:\/[A-Za-z][\w-]*)?\/?$/)
  const parentPath = parentMatch?.[1]
  if (parentPath) {
    cache.clearCache(parentPath)
  }
}

const api = {
  // 包装GET方法，添加缓存
  get: async (url: string, config?: any) => {
    const cache = RequestCache.getInstance()
    const params = config?.params || {}
    const cacheTime = config?.cacheTime || 60000 // 默认缓存60秒
    
    // 检查缓存
    const cachedData = cache.getCache(url, params)
    if (cachedData) {
      return cachedData
    }
    
    // 发送实际请求
    const response = await axiosInstance.get(url, config)
    
    // 缓存响应数据（只缓存data对象，与响应拦截器返回格式一致）
    cache.setCache(url, params, response, cacheTime)
    
    return response
  },
  
  // 包装POST方法，清除相关缓存
  post: async (url: string, data?: any, config?: any) => {
    const response = await axiosInstance.post(url, data, config)

    // 清除相关缓存（含父集合列表缓存）
    clearWriteCache(url)

    return response
  },

  // 包装PUT方法，清除相关缓存
  put: async (url: string, data?: any, config?: any) => {
    const response = await axiosInstance.put(url, data, config)

    // 清除相关缓存（含父集合列表缓存）
    clearWriteCache(url)

    return response
  },

  // 包装DELETE方法，清除相关缓存
  delete: async (url: string, config?: any) => {
    const response = await axiosInstance.delete(url, config)

    // 清除相关缓存（含父集合列表缓存）
    clearWriteCache(url)

    return response
  },

  // 包装PATCH方法，清除相关缓存
  patch: async (url: string, data?: any, config?: any) => {
    const response = await axiosInstance.patch(url, data, config)

    // 清除相关缓存（含父集合列表缓存）
    clearWriteCache(url)

    return response
  },
  
  // 包装HEAD方法，添加缓存
  head: async (url: string, config?: any) => {
    const cache = RequestCache.getInstance()
    const params = config?.params || {}
    const cacheTime = config?.cacheTime || 60000 // 默认缓存60秒
    
    // 检查缓存
    const cachedData = cache.getCache(url, params)
    if (cachedData) {
      return cachedData
    }
    
    // 发送实际请求
    const response = await axiosInstance.head(url, config)
    
    // 缓存响应数据
    cache.setCache(url, params, response, cacheTime)
    
    return response
  },
  
  // 包装OPTIONS方法，添加缓存
  options: async (url: string, config?: any) => {
    const cache = RequestCache.getInstance()
    const params = config?.params || {}
    const cacheTime = config?.cacheTime || 60000 // 默认缓存60秒
    
    // 检查缓存
    const cachedData = cache.getCache(url, params)
    if (cachedData) {
      return cachedData
    }
    
    // 发送实际请求
    const response = await axiosInstance.options(url, config)
    
    // 缓存响应数据
    cache.setCache(url, params, response, cacheTime)
    
    return response
  },
  
  // 添加清除缓存方法
  clearCache: (url: string, params?: any) => {
    const cache = RequestCache.getInstance()
    cache.clearCache(url, params)
  },
  
  // 添加清除所有缓存方法
  clearAllCache: () => {
    const cache = RequestCache.getInstance()
    cache.clearCache('')
  }
} as AxiosInstance & {
  clearCache: (url: string, params?: any) => void
  clearAllCache: () => void
}

export const unwrapResponseData = <T = any>(payload: any, fallback: T | null = null): T | null =>
  DataTransformer.unwrapData<T>(payload, fallback)

export const unwrapListResponse = <T = any>(payload: any): T[] =>
  DataTransformer.unwrapList<T>(payload)

export const unwrapPageResponse = <T = any>(payload: any): { list: T[]; records: T[]; total: number; page: number; size: number } =>
  DataTransformer.unwrapPage<T>(payload)

export default api
