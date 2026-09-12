import type { App } from 'vue'
import { ElMessage } from 'element-plus'
import { logger } from './logger'

type ErrorLike = {
  name?: string
  message?: string
  stack?: string
}

const getErrorMessage = (e: unknown) => {
  if (e instanceof Error) return e.message
  if (typeof e === 'string') return e
  if (e && typeof e === 'object' && 'message' in e) return String((e as any).message)
  try {
    return JSON.stringify(e)
  } catch {
    return String(e)
  }
}

const normalizeError = (e: unknown): ErrorLike => {
  if (e instanceof Error) return { name: e.name, message: e.message, stack: e.stack }
  if (e && typeof e === 'object') {
    const anyErr = e as any
    return {
      name: anyErr.name,
      message: anyErr.message ? String(anyErr.message) : undefined,
      stack: anyErr.stack ? String(anyErr.stack) : undefined
    }
  }
  return { message: getErrorMessage(e) }
}

export const installErrorCapture = (app: App) => {
  const isDev = Boolean(import.meta.env.DEV)
  let lastToastAt = 0
  const toast = (msg: string) => {
    const now = Date.now()
    if (!isDev) return
    if (now - lastToastAt < 1500) return
    lastToastAt = now
    ElMessage.error(msg.length > 180 ? msg.slice(0, 180) + '…' : msg)
  }

  app.config.errorHandler = (err, instance, info) => {
    const normalized = normalizeError(err)
    logger.error('UI Error', normalized, { info, component: (instance as any)?.$?.type?.name })
    toast(`页面异常：${getErrorMessage(err)}`)
  }

  window.addEventListener('error', (event: ErrorEvent) => {
    logger.error('Window Error', event.error ?? event.message, {
      filename: event.filename,
      lineno: event.lineno,
      colno: event.colno
    })
    toast(`运行异常：${getErrorMessage(event.error ?? event.message)}`)
  })

  window.addEventListener('unhandledrejection', (event: PromiseRejectionEvent) => {
    logger.error('Unhandled Rejection', event.reason)
    toast(`请求异常：${getErrorMessage(event.reason)}`)
  })
}

