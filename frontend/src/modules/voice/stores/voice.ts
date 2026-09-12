import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

export interface VoiceContext {
  id: string
  description?: string
  priority?: number  // 优先级，数值越高优先级越高
  parentId?: string  // 父上下文ID，支持上下文继承
  category?: string  // 上下文分类
  actions: Record<string, Function>
  setters?: Record<string, (val: any) => void> // 设置为可选属性
  data?: Record<string, any>  // 上下文数据
  metadata?: Record<string, any>  // 上下文元数据
  lifecycle?: {
    onActivate?: () => void
    onDeactivate?: () => void
    onDestroy?: () => void
  }  // 生命周期钩子
  permissions?: string[]  // 上下文所需权限
}

export interface ChatMessage {
  id: string
  role: 'user' | 'assistant' | 'system' | 'function'
  content: string
  timestamp: number
  metadata?: {
    intent?: string
    target?: string
    confidence?: number
    duration?: number
    audioUrl?: string
    source?: string
  }
  contextId?: string
  functionCall?: {
    name: string
    arguments: string
  }
}

export interface ContextEvent {
  type: 'register' | 'unregister' | 'activate' | 'deactivate' | 'merge' | 'update'
  contextId: string
  data?: any
  timestamp: number
}

// 上下文状态
export interface ContextState {
  context: VoiceContext
  isActive: boolean
  activationTime: number
  lastAccessTime: number
  refCount: number  // 引用计数，用于自动管理上下文生命周期
  mergedContext?: VoiceContext  // 缓存合并后的上下文
}

export const useVoiceStore = defineStore('voice', () => {
  // State
  const isListening = ref(false)
  const isProcessing = ref(false)
  const transcript = ref('')
  const contexts = ref<Map<string, ContextState>>(new Map()) // 所有注册的上下文
  const activeContextIds = ref<string[]>([]) // 所有激活的上下文ID，按优先级排序
  const activeContextId = ref<string | null>(null) // 当前激活的上下文ID
  const chatHistory = ref<ChatMessage[]>([])
  const isSupported = ref(!!(window.navigator.mediaDevices && window.AudioContext))
  const contextEvents = ref<ContextEvent[]>([])
  const isInitialized = ref(false)
  const maxHistorySize = ref(100) // 最大聊天记录数
  const contextHistory = ref<string[]>([]) // 上下文切换历史

  // 计算属性
  const activeContext = computed(() => {
    if (!activeContextId.value) return null
    return contexts.value.get(activeContextId.value)?.context || null
  })

  // 所有激活的上下文，按优先级排序
  const activeContexts = computed(() => {
    return activeContextIds.value
      .map(id => contexts.value.get(id)?.context || null)
      .filter((context): context is VoiceContext => context !== null)
  })

  // 合并后的当前上下文
  const mergedActiveContext = computed(() => {
    if (!activeContextId.value) return null
    return getMergedContext(activeContextId.value)
  })

  // 监听上下文变化，自动更新合并后的上下文缓存
  watch(
    () => Array.from(contexts.value.values()),
    () => {
      // 清除所有合并上下文缓存
      for (const [id, state] of contexts.value.entries()) {
        delete state.mergedContext
      }
    },
    { deep: true }
  )

  // Actions
  function setListening(status: boolean) {
    isListening.value = status
  }

  function setProcessing(status: boolean) {
    isProcessing.value = status
  }

  function setTranscript(text: string) {
    transcript.value = text
  }

  function addMessage(role: 'user' | 'assistant' | 'system' | 'function', content: string, metadata?: ChatMessage['metadata'], contextId?: string) {
    const message: ChatMessage = {
      id: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      role,
      content,
      timestamp: Date.now(),
      metadata,
      contextId
    }
    
    chatHistory.value.push(message)
    
    // 限制历史记录大小
    if (chatHistory.value.length > maxHistorySize.value) {
      chatHistory.value = chatHistory.value.slice(-maxHistorySize.value)
    }
  }

  /**
   * 注册上下文
   * @param context 上下文对象
   * @param autoActivate 是否自动激活
   * @param initialRefCount 初始引用计数
   */
  function registerContext(context: VoiceContext, autoActivate: boolean = true, initialRefCount: number = 1) {
    console.log('[VoiceStore] Registering context:', context.id)
    
    // 确保上下文有默认值
    const contextWithDefaults: VoiceContext = {
      priority: 0,
      ...context
    }
    
    // 注册上下文
    contexts.value.set(context.id, {
      context: contextWithDefaults,
      isActive: false,
      activationTime: Date.now(),
      lastAccessTime: Date.now(),
      refCount: initialRefCount
    })
    
    // 发送注册事件
    emitContextEvent('register', context.id, { context: contextWithDefaults })
    
    // 自动激活
    if (autoActivate) {
      activateContext(context.id)
    }
  }

  /**
   * 增加上下文引用计数
   * @param id 上下文ID
   */
  function incrementContextRef(id: string) {
    const contextState = contexts.value.get(id)
    if (contextState) {
      contextState.refCount++
      contextState.lastAccessTime = Date.now()
      contexts.value.set(id, contextState)
    }
  }

  /**
   * 减少上下文引用计数，当引用计数为0时自动注销
   * @param id 上下文ID
   */
  function decrementContextRef(id: string) {
    const contextState = contexts.value.get(id)
    if (contextState) {
      contextState.refCount--
      contextState.lastAccessTime = Date.now()
      
      if (contextState.refCount <= 0) {
        // 引用计数为0，自动注销上下文
        unregisterContext(id)
      } else {
        contexts.value.set(id, contextState)
      }
    }
  }

  /**
   * 注销上下文
   * @param id 上下文ID
   */
  function unregisterContext(id: string) {
    console.log('[VoiceStore] Unregistering context:', id)
    
    const contextState = contexts.value.get(id)
    if (!contextState) return
    
    // 调用生命周期钩子
    contextState.context.lifecycle?.onDestroy?.()
    
    // 停用上下文
    deactivateContext(id)
    
    // 移除上下文
    contexts.value.delete(id)
    
    // 更新激活上下文列表
    updateActiveContexts()
    
    // 发送注销事件
    emitContextEvent('unregister', id)
  }

  /**
   * 激活上下文
   * @param id 上下文ID
   * @param force 是否强制激活，忽略优先级
   */
  function activateContext(id: string, force: boolean = false) {
    console.log('[VoiceStore] Activating context:', id)
    
    const contextState = contexts.value.get(id)
    if (!contextState) {
      console.warn(`[VoiceStore] Context ${id} not found`)
      return
    }
    
    // 调用生命周期钩子
    contextState.context.lifecycle?.onActivate?.()
    
    // 激活上下文
    contextState.isActive = true
    contextState.activationTime = Date.now()
    contextState.lastAccessTime = Date.now()
    
    // 更新上下文状态
    contexts.value.set(id, contextState)
    
    // 更新激活上下文列表
    updateActiveContexts()
    
    // 如果是第一个激活的上下文或者强制激活，设置为当前激活的上下文
    if (force || activeContextIds.value.length === 1) {
      activeContextId.value = id
      // 记录上下文切换历史
      contextHistory.value.push(id)
      if (contextHistory.value.length > 20) {
        contextHistory.value = contextHistory.value.slice(-20)
      }
    } else {
      // 否则根据优先级自动选择
      activateHighestPriorityContext()
    }
    
    // 发送激活事件
    emitContextEvent('activate', id, { context: contextState.context })
  }

  /**
   * 停用上下文
   * @param id 上下文ID
   */
  function deactivateContext(id: string) {
    console.log('[VoiceStore] Deactivating context:', id)
    
    const contextState = contexts.value.get(id)
    if (!contextState) {
      console.warn(`[VoiceStore] Context ${id} not found`)
      return
    }
    
    // 调用生命周期钩子
    contextState.context.lifecycle?.onDeactivate?.()
    
    // 停用上下文
    contextState.isActive = false
    contexts.value.set(id, contextState)
    
    // 更新激活上下文列表
    updateActiveContexts()
    
    // 如果是当前激活的上下文，切换到其他激活的上下文
    if (activeContextId.value === id) {
      activateHighestPriorityContext()
    }
    
    // 发送停用事件
    emitContextEvent('deactivate', id, { context: contextState.context })
  }

  /**
   * 更新激活上下文列表，按优先级排序
   */
  function updateActiveContexts() {
    activeContextIds.value = Array.from(contexts.value.values())
      .filter(state => state.isActive)
      .sort((a, b) => {
        // 先按优先级排序，优先级相同按激活时间排序
        const priorityDiff = (b.context.priority || 0) - (a.context.priority || 0)
        if (priorityDiff !== 0) return priorityDiff
        return b.activationTime - a.activationTime
      })
      .map(state => state.context.id)
  }

  /**
   * 获取上下文
   * @param id 上下文ID
   * @param updateAccessTime 是否更新最后访问时间
   * @returns 上下文对象
   */
  function getContext(id: string, updateAccessTime: boolean = false): VoiceContext | null {
    const contextState = contexts.value.get(id)
    if (contextState) {
      if (updateAccessTime) {
        contextState.lastAccessTime = Date.now()
        contexts.value.set(id, contextState)
      }
      return contextState.context
    }
    return null
  }

  /**
   * 获取所有上下文
   * @returns 上下文列表
   */
  function getAllContexts(): VoiceContext[] {
    return Array.from(contexts.value.values()).map(state => state.context)
  }

  /**
   * 清除所有上下文
   */
  function clearContexts() {
    console.log('[VoiceStore] Clearing all contexts')
    
    // 调用所有上下文的销毁钩子
    for (const state of contexts.value.values()) {
      state.context.lifecycle?.onDestroy?.()
    }
    
    contexts.value.clear()
    activeContextIds.value = []
    activeContextId.value = null
  }

  /**
   * 根据优先级激活最高优先级的上下文
   */
  function activateHighestPriorityContext() {
    const highestContext = Array.from(contexts.value.values())
      .filter(state => state.isActive)
      .sort((a, b) => {
        // 先按优先级排序，优先级相同按激活时间排序
        const priorityDiff = (b.context.priority || 0) - (a.context.priority || 0)
        if (priorityDiff !== 0) return priorityDiff
        return b.activationTime - a.activationTime
      })[0]
    
    if (highestContext) {
      activeContextId.value = highestContext.context.id
      // 记录上下文切换历史
      contextHistory.value.push(highestContext.context.id)
      if (contextHistory.value.length > 20) {
        contextHistory.value = contextHistory.value.slice(-20)
      }
    } else {
      activeContextId.value = null
    }
  }

  /**
   * 合并上下文（包括继承的父上下文）
   * @param contextId 上下文ID
   * @param forceRefresh 是否强制刷新缓存
   * @returns 合并后的上下文
   */
  function getMergedContext(contextId: string, forceRefresh: boolean = false): VoiceContext | null {
    const contextState = contexts.value.get(contextId)
    if (!contextState) return null
    
    // 如果有缓存的合并上下文且不需要强制刷新，直接返回
    if (contextState.mergedContext && !forceRefresh) {
      return contextState.mergedContext
    }
    
    // 递归合并上下文
    const mergedContext = mergeContextRecursive(contextState.context)
    
    // 缓存合并后的上下文
    contextState.mergedContext = mergedContext
    contexts.value.set(contextId, contextState)
    
    // 发送合并事件
    emitContextEvent('merge', contextId, { mergedContext })
    
    return mergedContext
  }
  
  /**
   * 递归合并上下文
   * @param context 当前上下文
   * @returns 合并后的上下文
   */
  function mergeContextRecursive(context: VoiceContext): VoiceContext {
    // 如果有父上下文，递归合并
    if (context.parentId) {
      const parentContextState = contexts.value.get(context.parentId)
      if (parentContextState) {
        const parentContext = mergeContextRecursive(parentContextState.context)
        
        // 合并上下文，子上下文优先
        const mergedContext: VoiceContext = {
          ...parentContext,
          ...context,
          actions: {
            ...parentContext.actions,
            ...context.actions
          },
          setters: {
            ...parentContext.setters,
            ...context.setters
          },
          data: {
            ...parentContext.data,
            ...context.data
          },
          metadata: {
            ...parentContext.metadata,
            ...context.metadata
          },
          permissions: [
            ...(parentContext.permissions || []),
            ...(context.permissions || [])
          ],
          lifecycle: {
            ...parentContext.lifecycle,
            ...context.lifecycle
          }
        }
        return mergedContext
      }
    }
    
    return context
  }

  /**
   * 更新上下文
   * @param id 上下文ID
   * @param updates 更新内容
   */
  function updateContext(id: string, updates: Partial<VoiceContext>) {
    const contextState = contexts.value.get(id)
    if (!contextState) {
      console.warn(`[VoiceStore] Context ${id} not found for update`)
      return
    }
    
    // 更新上下文
    const updatedContext = {
      ...contextState.context,
      ...updates,
      actions: {
        ...contextState.context.actions,
        ...(updates.actions || {})
      },
      setters: {
        ...contextState.context.setters,
        ...(updates.setters || {})
      },
      data: {
        ...contextState.context.data,
        ...(updates.data || {})
      },
      metadata: {
        ...contextState.context.metadata,
        ...(updates.metadata || {})
      }
    }
    
    contextState.context = updatedContext
    contextState.lastAccessTime = Date.now()
    
    // 清除合并上下文缓存
    delete contextState.mergedContext
    
    contexts.value.set(id, contextState)
    
    // 更新激活上下文列表
    updateActiveContexts()
    
    // 发送更新事件
    emitContextEvent('update', id, { updates, context: updatedContext })
  }

  /**
   * 根据分类获取上下文
   * @param category 上下文分类
   * @returns 上下文列表
   */
  function getContextsByCategory(category: string): VoiceContext[] {
    return Array.from(contexts.value.values())
      .map(state => state.context)
      .filter(context => context.category === category)
  }

  /**
   * 发送上下文事件
   * @param type 事件类型
   * @param contextId 上下文ID
   * @param data 事件数据
   */
  function emitContextEvent(type: ContextEvent['type'], contextId: string, data?: any) {
    const event: ContextEvent = {
      type,
      contextId,
      data,
      timestamp: Date.now()
    }
    
    contextEvents.value.push(event)
    
    // 限制事件队列大小
    if (contextEvents.value.length > 100) {
      contextEvents.value = contextEvents.value.slice(-100)
    }
  }

  /**
   * 获取上下文事件
   * @param type 事件类型过滤
   * @param contextId 上下文ID过滤
   * @returns 事件列表
   */
  function getContextEvents(type?: ContextEvent['type'], contextId?: string): ContextEvent[] {
    return contextEvents.value.filter(event => {
      return (!type || event.type === type) && (!contextId || event.contextId === contextId)
    })
  }

  /**
   * 清空聊天历史
   */
  function clearHistory() {
    chatHistory.value = []
  }

  /**
   * 初始化语音状态管理
   */
  function init() {
    if (isInitialized.value) return
    isInitialized.value = true
    console.log('[VoiceStore] Initialized')
    // 可以在这里添加初始化逻辑
  }

  /**
   * 销毁语音状态管理
   */
  function destroy() {
    clearContexts()
    clearHistory()
    isListening.value = false
    isProcessing.value = false
    transcript.value = ''
    activeContextId.value = null
    activeContextIds.value = []
    contextEvents.value = []
    contextHistory.value = []
    isInitialized.value = false
    console.log('[VoiceStore] Destroyed')
  }

  return {
    // State
    isListening,
    isProcessing,
    transcript,
    contexts,
    activeContextIds,
    activeContextId,
    chatHistory,
    isSupported,
    
    // Computed
    activeContext,
    activeContexts,
    mergedActiveContext,
    
    // Actions
    setListening,
    setProcessing,
    setTranscript,
    addMessage,
    registerContext,
    unregisterContext,
    activateContext,
    deactivateContext,
    getContext,
    getAllContexts,
    clearContexts,
    activateHighestPriorityContext,
    getMergedContext,
    clearHistory,
    incrementContextRef,
    decrementContextRef,
    updateContext,
    getContextsByCategory,
    getContextEvents,
    updateActiveContexts,
    init,
    destroy
  }
})
