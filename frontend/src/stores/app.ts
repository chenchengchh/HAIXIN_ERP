import { defineStore } from 'pinia'

// 定义全局应用状态管理
export const useAppStore = defineStore('app', {
  state: () => ({
    // 加载状态管理
    loading: {
      // 全局加载状态
      global: false,
      // 请求加载状态，使用Map存储每个请求的加载状态
      requests: new Map<string, boolean>()
    },
    // 错误信息管理
    error: {
      // 全局错误信息
      global: null as { message: string; code?: number } | null,
      // 请求错误信息，使用Map存储每个请求的错误信息
      requests: new Map<string, { message: string; code?: number }>()
    },
    // 成功提示信息
    successMessage: null as string | null
  }),

  getters: {
    // 获取全局加载状态
    isGlobalLoading: (state) => state.loading.global,
    // 获取是否有任何请求正在加载
    isAnyRequestLoading: (state) => {
      for (const loadingState of state.loading.requests.values()) {
        if (loadingState) {
          return true
        }
      }
      return false
    }
  },

  actions: {
    // 设置全局加载状态
    setGlobalLoading(loading: boolean) {
      this.loading.global = loading
    },

    // 设置特定请求的加载状态
    setRequestLoading(requestKey: string, loading: boolean) {
      this.loading.requests.set(requestKey, loading)
    },

    // 清除所有请求的加载状态
    clearAllRequestLoading() {
      this.loading.requests.clear()
    },

    // 设置全局错误信息
    setGlobalError(error: { message: string; code?: number } | null) {
      this.error.global = error
    },

    // 设置特定请求的错误信息
    setRequestError(requestKey: string, error: { message: string; code?: number } | null) {
      if (error) {
        this.error.requests.set(requestKey, error)
      } else {
        this.error.requests.delete(requestKey)
      }
    },

    // 清除全局错误信息
    clearGlobalError() {
      this.error.global = null
    },

    // 清除所有请求的错误信息
    clearAllRequestErrors() {
      this.error.requests.clear()
    },

    // 设置成功提示信息
    setSuccessMessage(message: string | null) {
      this.successMessage = message
    },

    // 清除成功提示信息
    clearSuccessMessage() {
      this.successMessage = null
    },

    // 显示成功提示
    showSuccessMessage(message: string) {
      this.setSuccessMessage(message)
      // 3秒后自动清除
      setTimeout(() => {
        this.clearSuccessMessage()
      }, 3000)
    },

    // 显示错误提示
    showErrorMessage(message: string, code?: number) {
      this.setGlobalError({ message, code })
      // 3秒后自动清除
      setTimeout(() => {
        this.clearGlobalError()
      }, 3000)
    }
  }
})
