/**
 * WebSocket工具类，提供WebSocket连接管理功能
 * @author author
 * @date 2025-12-16
 */

// WebSocket事件回调类型
type WebSocketEvents = {
  onOpen?: () => void
  onMessage?: (message: MessageEvent) => void
  onClose?: () => void
  onError?: (error: Event) => void
}

// WebSocket实例类型
interface WebSocketInstance {
  send: (data: string) => void
  close: () => void
  readyState: number
}

/**
 * WebSocket钩子函数，用于创建和管理WebSocket连接
 * @param url WebSocket连接URL
 * @param events 事件回调函数
 * @returns WebSocket实例对象
 */
export const useWebSocket = (url: string, events: WebSocketEvents = {}): WebSocketInstance => {
  let ws: WebSocket | null = null
  let reconnectTimer: number | null = null
  const reconnectInterval = 5000 // 重连间隔5秒
  const maxReconnectAttempts = 5 // 最大重连次数
  let reconnectAttempts = 0

  // 初始化WebSocket连接
  const initWebSocket = () => {
    try {
      ws = new WebSocket(url)
      
      // 连接成功事件
      ws.onopen = () => {
        console.log('WebSocket连接已建立')
        reconnectAttempts = 0
        if (events.onOpen) {
          events.onOpen()
        }
      }
      
      // 接收消息事件
      ws.onmessage = (message) => {
        if (events.onMessage) {
          events.onMessage(message)
        }
      }
      
      // 连接关闭事件
      ws.onclose = () => {
        console.log('WebSocket连接已关闭')
        if (events.onClose) {
          events.onClose()
        }
        
        // 自动重连
        if (reconnectAttempts < maxReconnectAttempts) {
          reconnectTimer = window.setTimeout(() => {
            reconnectAttempts++
            console.log(`WebSocket尝试重连... (${reconnectAttempts}/${maxReconnectAttempts})`)
            initWebSocket()
          }, reconnectInterval)
        }
      }
      
      // 连接错误事件
      ws.onerror = (error) => {
        console.error('WebSocket连接错误:', error)
        if (events.onError) {
          events.onError(error)
        }
      }
    } catch (error) {
      console.error('WebSocket初始化失败:', error)
      if (events.onError) {
        events.onError(error as Event)
      }
    }
  }

  // 发送消息
  const send = (data: string) => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(data)
    } else {
      console.error('WebSocket连接未建立，无法发送消息')
    }
  }

  // 关闭WebSocket连接
  const close = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    
    if (ws) {
      ws.close()
      ws = null
    }
  }

  // 获取WebSocket连接状态
  const getReadyState = () => {
    return ws ? ws.readyState : WebSocket.CLOSED
  }

  // 初始化连接
  initWebSocket()

  return {
    send,
    close,
    get readyState() {
      return getReadyState()
    }
  }
}

/**
 * WebSocket状态常量
 */
export const WebSocketStatus = {
  CONNECTING: WebSocket.CONNECTING, // 0 - 连接中
  OPEN: WebSocket.OPEN, // 1 - 连接已建立
  CLOSING: WebSocket.CLOSING, // 2 - 连接正在关闭
  CLOSED: WebSocket.CLOSED // 3 - 连接已关闭
}

/**
 * WebSocket状态文本映射
 */
export const WebSocketStatusText = {
  [WebSocket.CONNECTING]: '连接中',
  [WebSocket.OPEN]: '已连接',
  [WebSocket.CLOSING]: '关闭中',
  [WebSocket.CLOSED]: '已关闭'
}
