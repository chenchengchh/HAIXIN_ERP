import { onMounted, onUnmounted, watch } from 'vue'
import { useVoiceStore, type VoiceContext } from '../modules/voice/stores/voice'

/**
 * 语音上下文注册与管理的 Composable
 * 用于在组件中方便地注册和管理语音上下文
 * 
 * @param contextConfig 上下文配置
 * @param autoActivate 是否自动激活
 * @returns 上下文管理对象
 */
export function useVoiceContext(
  contextConfig: VoiceContext,
  autoActivate: boolean = true
) {
  const voiceStore = useVoiceStore()
  const contextId = contextConfig.id

  // 注册上下文
  const registerContext = () => {
    voiceStore.registerContext(contextConfig, autoActivate)
  }

  // 注销上下文
  const unregisterContext = () => {
    voiceStore.unregisterContext(contextId)
  }

  // 激活上下文
  const activateContext = () => {
    voiceStore.activateContext(contextId)
  }

  // 停用上下文
  const deactivateContext = () => {
    voiceStore.deactivateContext(contextId)
  }

  // 更新上下文
  const updateContext = (updates: Partial<VoiceContext>) => {
    voiceStore.updateContext(contextId, updates)
  }

  // 增加引用计数
  const incrementRef = () => {
    voiceStore.incrementContextRef(contextId)
  }

  // 减少引用计数
  const decrementRef = () => {
    voiceStore.decrementContextRef(contextId)
  }

  // 在组件挂载时注册上下文
  onMounted(() => {
    registerContext()
  })

  // 在组件卸载时注销上下文
  onUnmounted(() => {
    unregisterContext()
  })

  // 监听上下文配置变化，自动更新
  watch(
    () => contextConfig,
    (newConfig) => {
      updateContext(newConfig)
    },
    { deep: true }
  )

  return {
    contextId,
    registerContext,
    unregisterContext,
    activateContext,
    deactivateContext,
    updateContext,
    incrementRef,
    decrementRef,
  }
}
