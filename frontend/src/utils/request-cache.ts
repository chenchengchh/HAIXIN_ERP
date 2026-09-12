/**
 * 请求缓存工具
 */
export class RequestCache {
  private cache: Map<string, { data: any; timestamp: number; ttl: number }> = new Map()
  private static instance: RequestCache

  /**
   * 获取单例实例
   */
  static getInstance(): RequestCache {
    if (!RequestCache.instance) {
      RequestCache.instance = new RequestCache()
    }
    return RequestCache.instance
  }

  /**
   * 生成缓存键
   * @param url 请求URL
   * @param params 请求参数
   * @returns 缓存键
   */
  private generateKey(url: string, params?: any): string {
    const key = `${url}_${JSON.stringify(params || {})}`
    return key
  }

  /**
   * 检查缓存是否有效
   * @param key 缓存键
   * @returns 缓存是否有效
   */
  private isCacheValid(key: string): boolean {
    const cachedItem = this.cache.get(key)
    if (!cachedItem) return false
    return Date.now() - cachedItem.timestamp < cachedItem.ttl
  }

  /**
   * 设置缓存
   * @param url 请求URL
   * @param params 请求参数
   * @param data 响应数据
   * @param ttl 缓存有效期（毫秒），默认60000ms
   */
  setCache(url: string, params: any, data: any, ttl: number = 60000): void {
    const key = this.generateKey(url, params)
    this.cache.set(key, {
      data,
      timestamp: Date.now(),
      ttl
    })
  }

  /**
   * 获取缓存
   * @param url 请求URL
   * @param params 请求参数
   * @returns 缓存数据或null
   */
  getCache(url: string, params: any): any | null {
    const key = this.generateKey(url, params)
    if (this.isCacheValid(key)) {
      return this.cache.get(key)?.data || null
    } else {
      this.cache.delete(key)
      return null
    }
  }

  /**
   * 清除缓存
   * @param url 请求URL
   * @param params 请求参数
   */
  clearCache(url: string, params?: any): void {
    if (url && params) {
      const key = this.generateKey(url, params)
      this.cache.delete(key)
    } else if (url) {
      // 清除所有包含该URL的缓存
      for (const key of this.cache.keys()) {
        if (key.startsWith(`${url}_`)) {
          this.cache.delete(key)
        }
      }
    } else {
      // 清除所有缓存
      this.cache.clear()
    }
  }
}

/**
 * 防抖函数
 * @param func 要执行的函数
 * @param delay 延迟时间（毫秒）
 * @returns 防抖处理后的函数
 */
export function debounce<T extends (...args: any[]) => any>(func: T, delay: number): (...args: Parameters<T>) => void {
  let timeoutId: ReturnType<typeof setTimeout> | null = null
  return (...args: Parameters<T>) => {
    if (timeoutId) {
      clearTimeout(timeoutId)
    }
    timeoutId = setTimeout(() => {
      func(...args)
      timeoutId = null
    }, delay)
  }
}

/**
 * 节流函数
 * @param func 要执行的函数
 * @param limit 时间间隔（毫秒）
 * @returns 节流处理后的函数
 */
export function throttle<T extends (...args: any[]) => any>(func: T, limit: number): (...args: Parameters<T>) => ReturnType<T> {
  let inThrottle = false
  let result: any
  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      inThrottle = true
      result = func(...args)
      setTimeout(() => {
        inThrottle = false
      }, limit)
    }
    return result
  }
}
