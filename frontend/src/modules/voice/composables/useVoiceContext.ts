import { onMounted, onUnmounted } from 'vue'
import { useVoiceStore, type VoiceContext } from '../stores/voice'

/**
 * Configuration for registering a voice context
 */
export interface VoiceContextConfig {
  /**
   * Unique identifier for the context (e.g., 'purchase-view')
   */
  id: string
  
  /**
   * Human-readable description helps the AI/User know where they are
   */
  description?: string
  
  /**
   * Actions that can be triggered by voice
   * Key: command keyword (e.g., 'search', 'create')
   * Value: function to execute
   */
  actions: Record<string, () => void | Promise<void>>
  
  /**
   * Setters for form filling (Phase 3 preparation)
   */
  setters?: Record<string, (val: any) => void>
}

/**
 * Composable to register a component's voice capabilities
 * Automatically registers on mount and unregisters on unmount
 */
export function useVoiceContext(config: VoiceContextConfig) {
  const voiceStore = useVoiceStore()

  onMounted(() => {
    // Transform config to match store's VoiceContext interface
    const context: VoiceContext = {
      id: config.id,
      description: config.description,
      actions: config.actions,
      setters: config.setters || {}
    }
    
    voiceStore.registerContext(context)
  })

  onUnmounted(() => {
    voiceStore.unregisterContext(config.id)
  })

  return {
    // Return anything if needed
  }
}
