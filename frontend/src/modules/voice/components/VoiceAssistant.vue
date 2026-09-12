<template>
  <div class="voice-assistant-container">
    <!-- Voice Interaction Ball -->
    <transition name="pop">
      <div 
        v-if="isVisible" 
        class="voice-assistant-ball" 
        :class="{
            'is-listening': isListening && !isAwake && !isSuccess && !isLoadingModel, 
            'is-awake': isAwake && !isSuccess && !isLoadingModel,
            'is-processing': isProcessing,
            'is-success': isSuccess,
            'has-error': hasError,
            'is-loading-model': isLoadingModel
        }"
        @click="handleBallClick"
        @dblclick="toggleVisibility"
      >
        <div class="glow-layer"></div>
        <el-icon class="mic-icon" :size="26">
          <Loading v-if="isLoadingModel || isProcessing" class="is-loading" />
          <Check v-else-if="isSuccess" />
          <Microphone v-else-if="!hasError" />
          <Close v-else class="is-error" />
        </el-icon>
        <div v-if="isListening" class="ripple-ring" :class="{ 'awake-ripple': isAwake, 'error-ripple': hasError }"></div>
      </div>
    </transition>

    <!-- Main Conversational Panel -->
    <transition name="slide-up">
      <div v-if="isPanelVisible" class="voice-chat-panel">
        <div class="panel-header">
           <div class="status-indicator">
             <span class="dot" :class="statusClass"></span>
             <span class="status-text">{{ statusText }}</span>
           </div>
           <div class="header-actions">
             <el-button link @click="clearHistory" title="清空历史"><el-icon><Delete /></el-icon></el-button>
             <el-button link @click="minimizePanel" title="最小化"><el-icon><SemiSelect /></el-icon></el-button>
           </div>
        </div>
        
        <!-- Conversation Stream -->
        <div class="chat-viewport" ref="chatViewport">
          <div v-for="(log, index) in chatLogs" :key="index" class="msg-row" :class="log.role">
            <div class="msg-bubble">
              <div class="msg-content">{{ log.text }}</div>
              <div class="msg-time">{{ formatTime(log.timestamp) }}</div>
            </div>
          </div>
          
          <!-- Real-time / Processing Ghost Bubble -->
          <div v-if="realtimeText" class="msg-row user ghost">
            <div class="msg-bubble">
              <div class="msg-content">{{ realtimeText }}<span class="cursor">|</span></div>
            </div>
          </div>

          <div v-if="isProcessing && !realtimeText" class="msg-row assistant loading">
            <div class="msg-bubble">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Action Footer -->
        <div class="panel-footer" v-if="isAwake || hasError">
           <div v-if="hasError" class="error-strip">
             <el-icon><Warning /></el-icon> {{ errorMessage }}
           </div>
           <div class="footer-btns" v-else>
             <el-button round size="small" @click="cancelCommand">取消</el-button>
             <el-button round type="primary" size="small" @click="enterSleepMode">进入休眠</el-button>
           </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import { Microphone, Loading, Close, Check, Delete, SemiSelect, Warning } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useVoiceStore } from '../stores/voice'
import { remoteStt, type SttResult } from '../utils/remote-stt'
import { parseCommand } from '../utils/command-parser'
import { CommandExecutor } from '../utils/command-executor'

const router = useRouter()
const route = useRoute()
const voiceStore = useVoiceStore()
let commandExecutor: CommandExecutor | null = null

try {
    commandExecutor = new CommandExecutor(router, voiceStore)
    console.log('[VoiceAssistant] CommandExecutor initialized')
} catch (e) {
    console.error('[VoiceAssistant] Failed to initialize CommandExecutor:', e)
}

// UI States
const isVisible = ref(true)      // Controls the floating ball visibility
const isPanelVisible = ref(false) // Controls the dialog panel visibility
const isListening = ref(false)
const isAwake = ref(false)
const isProcessing = ref(false)
const isLoadingModel = ref(false)
const isSuccess = ref(false)
const hasError = ref(false)
const errorMessage = ref('')

// Data States
const realtimeText = ref('')
const chatViewport = ref<HTMLElement | null>(null)
const chatLogs = ref<Array<{
    role: 'user' | 'assistant'
    text: string
    timestamp: number
}>>([])

type VoiceAssistantSettings = {
    isVisible?: boolean
    isPanelVisible?: boolean
    awakeInactivityMs?: number
    shutdownInactivityMs?: number
}

const SETTINGS_KEY = 'voice_assistant_settings'

const awakeInactivityMs = ref(45000)
const shutdownInactivityMs = ref(180000)

const loadSettings = () => {
    try {
        const raw = localStorage.getItem(SETTINGS_KEY)
        if (!raw) return
        const s: VoiceAssistantSettings = JSON.parse(raw)
        if (typeof s.isVisible === 'boolean') isVisible.value = s.isVisible
        if (typeof s.isPanelVisible === 'boolean') isPanelVisible.value = s.isPanelVisible
        if (typeof s.awakeInactivityMs === 'number' && Number.isFinite(s.awakeInactivityMs)) awakeInactivityMs.value = s.awakeInactivityMs
        if (typeof s.shutdownInactivityMs === 'number' && Number.isFinite(s.shutdownInactivityMs)) shutdownInactivityMs.value = s.shutdownInactivityMs
    } catch (e) {}
}

const saveSettings = () => {
    try {
        const s: VoiceAssistantSettings = {
            isVisible: isVisible.value,
            isPanelVisible: isPanelVisible.value,
            awakeInactivityMs: awakeInactivityMs.value,
            shutdownInactivityMs: shutdownInactivityMs.value
        }
        localStorage.setItem(SETTINGS_KEY, JSON.stringify(s))
    } catch (e) {}
}

// Load history from localStorage
const loadHistory = () => {
    try {
        const history = localStorage.getItem('voice_chat_history')
        if (history) {
            chatLogs.value = JSON.parse(history)
        }
    } catch (e) {
        console.error('Failed to load chat history', e)
    }
}

// Save history to localStorage
const saveHistory = () => {
    try {
        // Keep only last 50 messages
        const historyToSave = chatLogs.value.slice(-50)
        localStorage.setItem('voice_chat_history', JSON.stringify(historyToSave))
    } catch (e) {
        console.error('Failed to save chat history', e)
    }
}

watch(chatLogs, () => {
    saveHistory()
    scrollToBottom()
}, { deep: true })

const statusText = computed(() => {
    if (hasError.value) return '系统异常'
    if (isLoadingModel.value) return '初始化引擎...'
    if (isProcessing.value) return '思考中...'
    if (isAwake.value) return '聆听指令中'
    if (isListening.value) return '等待唤醒 (你好海星)'
    return '已就绪'
})

const statusClass = computed(() => ({
    'bg-green': isAwake.value,
    'bg-blue': isListening.value && !isAwake.value,
    'bg-purple': isProcessing.value,
    'bg-red': hasError.value
}))

const formatTime = (ts: number) => {
    const d = new Date(ts)
    return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

const scrollToBottom = async () => {
    await nextTick()
    if (chatViewport.value) {
        chatViewport.value.scrollTo({
            top: chatViewport.value.scrollHeight,
            behavior: 'smooth'
        })
    }
}

watch(chatLogs, scrollToBottom, { deep: true })
watch(realtimeText, scrollToBottom)

// Logic
const clearHistory = () => {
    chatLogs.value = []
    realtimeText.value = ''
    resetInactivityTimer()
}

const toggleVisibility = () => {
    isVisible.value = !isVisible.value
    if (!isVisible.value) {
        stopListening(true)
        isPanelVisible.value = false
    }
}

const minimizePanel = () => {
    isPanelVisible.value = false
    if (isListening.value) {
        exitVoiceMode()
    }
}

const handleBallClick = () => {
    if (!isPanelVisible.value) {
        isPanelVisible.value = true
        // Optional: Start listening immediately when opened
        // if (!isListening.value) startListening()
    } else {
        // If panel is already open, toggle listening
        toggleListening()
    }
}

// Timer Logic
let awakeTimer: any = null
let shutdownTimer: any = null

const resetInactivityTimer = () => {
    if (awakeTimer) clearTimeout(awakeTimer)
    if (shutdownTimer) clearTimeout(shutdownTimer)
    
    if (isAwake.value) {
        awakeTimer = setTimeout(() => {
            if (isAwake.value) enterSleepMode({ silent: true })
        }, awakeInactivityMs.value)
    }
    
    shutdownTimer = setTimeout(() => {
        if (isListening.value) exitVoiceMode()
    }, shutdownInactivityMs.value)
}

const handleResult = async (result: SttResult) => {
    try {
        resetError()
        const rawText = result.text.trim()
        if (!rawText) return
        
        console.log(`[Voice] Dispatching: "${rawText}" (isFinal: ${result.isFinal})`);

        // 0. 状态保护：如果在处理中，忽略新输入
        if (isProcessing.value) {
            console.log('[Voice] Ignored input during processing:', rawText);
            return;
        }

        // 辅助函数：判断是否为噪音
        const isNoise = (text: string) => {
             // 业务过滤逻辑：移除了核心唤醒词，仅针对重合的回声进行过滤
            const echoPhrases = ['什么可以帮您', '请讲指令', '进入待机模式', '我会一直在这里', '有什么可以帮您']
            if (echoPhrases.some(phrase => text.includes(phrase))) return true;

            // 噪音/语气词过滤列表
            const noisePhrases = ['嗯', '啊', '对', '是', '哦', '嗯有', '对对对', '嗯嗯', '哦哦', '好的好的好的'];
            if (noisePhrases.includes(text)) return true;

            // 长度过滤：纯中文且长度小于2（非指令）
            if (text.length < 2 && !['退', '关'].includes(text) && /^[\u4e00-\u9fa5]+$/.test(text)) return true;

            // 乱码过滤：包含连续3个以上大写字母，或看似无意义的混合字符
            if (/[A-Z]{3,}/.test(text)) return true; // 如 XYR, SSOOO
            if (/^[a-zA-Z\s]+$/.test(text) && text.length < 4) return true; // 极短的纯英文

            return false;
        }

        if (isNoise(rawText)) {
             console.log('[Voice] Message filtered as noise:', rawText);
             return
        }

        // 1. 实时预览：仅在唤醒状态下，或检测到唤醒词时显示
        if (!result.isFinal) {
            const isWakeWord = rawText.includes('你好海星') || rawText.includes('你好海心');
            if (isAwake.value || isWakeWord) {
                realtimeText.value = rawText
                resetInactivityTimer()
            }
            return
        }

        // 2. 最终定稿 (Final)
        console.log('[Voice] Processing Final result...');
        realtimeText.value = '' 
        
        // [修复] 唤醒逻辑同步检查：必须在过滤逻辑之前执行
        // 使用正确的正则表达式匹配中文/字母/数字
        const sanitized = rawText.replace(/[^\u4e00-\u9fa5a-zA-Z0-9]/g, '').toLowerCase()
        let detectedWakeup = false
        if (sanitized.includes('你好海星') || sanitized.includes('你好海心')) {
             if (!isAwake.value) {
                console.log('[Voice] Wake-word detected -> Calling wakeUp()');
                wakeUp()
                detectedWakeup = true
             }
        }

        if (sanitized.includes('退出语音') || sanitized.includes('再见')) return exitVoiceMode()

        // 如果既没有被唤醒也不是刚才唤醒的，则忽略后续指令处理
        if (!isAwake.value && !detectedWakeup) {
            console.log('[Voice] Ignored: Not in awake state');
            return 
        }

        resetInactivityTimer()

        // 记录用户消息
        const now = Date.now()
        chatLogs.value.push({ role: 'user', text: rawText, timestamp: now })
        
        // 指令解析
        let commandText = sanitized
        if (detectedWakeup || isAwake.value) {
            commandText = sanitized.replace(/你好海星|你好海心/g, '')
        }

        if (commandText.length < 1) {
            console.log('[Voice] Command is empty after wake-word removal');
            return
        }
        
        console.log(`[Voice] Executing Command: "${commandText}"`);
        isProcessing.value = true
        try {
            // 解析指令
            const cmd = parseCommand(commandText, voiceStore.activeContext)
            let reply = ''

            // 执行指令
            let result = { success: false, message: '执行器未初始化' }
            if (commandExecutor) {
                result = await commandExecutor.execute(cmd)
            } else {
                console.error('[Voice] CommandExecutor is null')
            }
            
            if (result.success) {
                reply = result.message
                showSuccess()
                
                // 特殊处理 CANCEL 意图的 UI 状态
                if (cmd.intent === 'CANCEL') {
                    enterSleepMode()
                }
            } else {
                // 执行失败的回退逻辑（例如闲聊）
                if (commandText.includes('点') || commandText.includes('时间')) {
                    reply = `现在是 ${new Date().toLocaleTimeString()}`
                } else if (commandText.includes('谁')) {
                    reply = '我是海星智能助手'
                } else {
                    reply = result.message || `收到：${rawText}`
                }
            }

            if (reply) {
                speak(reply)
                chatLogs.value.push({ role: 'assistant', text: reply, timestamp: Date.now() })
            }
        } catch (e) { 
            console.error('[Voice] AI Action Error:', e)
            recordError('执行失败') 
        } finally { isProcessing.value = false }

    } catch (e) { console.error('[Voice] Result Handle Error:', e) }
}

const toggleListening = () => isListening.value ? exitVoiceMode() : startListening()

const startListening = async () => {
    try {
        isLoadingModel.value = true // 开始加载模型，显示灰色按钮
        await remoteStt.start(handleResult)
        isListening.value = true
        // Ensure panel is visible when listening starts
        isPanelVisible.value = true
        resetError()
        resetInactivityTimer()
    } catch (e: any) { recordError(e.message || '启动失败') }
    finally {
        isLoadingModel.value = false // 加载完成，恢复正常按钮状态
    }
}

const stopListening = (force = false) => {
    remoteStt.stop(force)
    isListening.value = false
    if (awakeTimer) clearTimeout(awakeTimer)
    if (shutdownTimer) clearTimeout(shutdownTimer)
}

const wakeUp = () => {
    if (isAwake.value) return
    isAwake.value = true
    isPanelVisible.value = true // Ensure panel is open on wakeup
    const msg = '我在，请讲指令'
    speak(msg)
    chatLogs.value.push({ role: 'assistant', text: msg, timestamp: Date.now() })
    resetInactivityTimer()
}

const enterSleepMode = (opts?: { silent?: boolean }) => {
    isAwake.value = false
    if (!opts?.silent) {
        const msg = '进入待机模式，您可以随时唤醒我'
        speak(msg)
        chatLogs.value.push({ role: 'assistant', text: msg, timestamp: Date.now() })
    }
    stopListening(true)
}

const cancelCommand = () => {
    realtimeText.value = ''
    speak('已取消')
}

const exitVoiceMode = () => {
    stopListening(true)
    isAwake.value = false
    realtimeText.value = ''
}

const resetError = () => { hasError.value = false; errorMessage.value = '' }
const recordError = (m: string) => { hasError.value = true; errorMessage.value = m }
const showSuccess = () => { isSuccess.value = true; setTimeout(() => isSuccess.value = false, 1500) }

const speak = (text: string) => {
    // [V15.0] 仅保留文本展示逻辑，不再进行语音播报
    console.log('[Voice] Feedback:', text)
}

const forceStopOnLeave = () => {
    try {
        stopListening(true)
    } catch (e) {}
}

onMounted(() => { 
    loadSettings()
    loadHistory()
    if (chatLogs.value.length === 0) {
        chatLogs.value.push({ role: 'assistant', text: '你好！我是海星，点击右下角麦克风开始交流。', timestamp: Date.now() })
    }

    window.addEventListener('pagehide', forceStopOnLeave)
    window.addEventListener('beforeunload', forceStopOnLeave)

    watch(() => route.fullPath, () => {
        if (!isPanelVisible.value && isListening.value) {
            exitVoiceMode()
        }
    })

    watch(
        () => ({
            isVisible: isVisible.value,
            isPanelVisible: isPanelVisible.value,
            awakeInactivityMs: awakeInactivityMs.value,
            shutdownInactivityMs: shutdownInactivityMs.value
        }),
        () => saveSettings(),
        { deep: true }
    )
})
onUnmounted(() => {
    window.removeEventListener('pagehide', forceStopOnLeave)
    window.removeEventListener('beforeunload', forceStopOnLeave)
    stopListening(true)
    // 在组件卸载时彻底释放所有资源
    remoteStt.destroy()
})
</script>

<style scoped>
.voice-assistant-container {
  position: fixed;
  bottom: 0px;
  right: 0px;
  z-index: 10000;
  display: flex;
  flex-direction: column-reverse; /* Key change: Ball at bottom, Panel above */
  align-items: flex-end;
  padding: 30px;
  pointer-events: none;
}

.voice-assistant-container > * { pointer-events: auto; }

/* The Sphere */
.voice-assistant-ball {
  position: relative;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00c6ff 0%, #0072ff 100%);
  box-shadow: 0 8px 32px rgba(0, 114, 255, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  overflow: visible;
  /* Ensure margins if needed, though flex-direction handles stacking */
}

.glow-layer {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: inherit;
  filter: blur(10px);
  opacity: 0.5;
  z-index: -1;
}

.voice-assistant-ball:hover { transform: scale(1.1); }

.voice-assistant-ball.is-awake {
  background: linear-gradient(135deg, #ff416c 0%, #ff4b2b 100%);
  box-shadow: 0 8px 32px rgba(255, 75, 43, 0.5);
  animation: breathe 2s infinite ease-in-out;
}

.voice-assistant-ball.is-processing {
  background: linear-gradient(135deg, #8e2de2 0%, #4a00e0 100%);
  animation: rotate-pulse 2s infinite linear;
}

.voice-assistant-ball.has-error {
  background: linear-gradient(135deg, #cf1322 0%, #ff4d4f 100%);
}

/* 加载模型时的灰色样式 */
.voice-assistant-ball.is-loading-model {
  background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%);
  cursor: not-allowed;
  animation: none;
}

.voice-assistant-ball.is-loading-model:hover {
  transform: none;
  filter: brightness(1);
}

.voice-assistant-ball.is-loading-model .glow-layer {
  opacity: 0.3;
}

/* Chat Panel (V12 Premium Layout) */
.voice-chat-panel {
  width: 420px;
  margin-bottom: 24px;
  background: rgba(15, 23, 42, 0.9); /* Deep Glass */
  backdrop-filter: blur(40px) saturate(180%);
  -webkit-backdrop-filter: blur(40px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 32px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 
    0 30px 60px -12px rgba(0, 0, 0, 0.6),
    inset 0 1px 2px rgba(255, 255, 255, 0.1);
}

.panel-header {
  padding: 18px 24px;
  background: rgba(255, 255, 255, 0.04);
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #94a3b8;
  position: relative;
}

.dot::after {
    content: '';
    position: absolute;
    inset: -3px;
    border-radius: 50%;
    background: inherit;
    opacity: 0.4;
    animation: ping 1.5s cubic-bezier(0, 0, 0.2, 1) infinite;
}

.status-text {
  font-size: 13px;
  font-weight: 600;
  color: #f1f5f9;
  letter-spacing: 0.3px;
}

.header-actions {
    display: flex;
    gap: 8px;
}

.header-actions .el-button {
    color: rgba(255,255,255,0.4);
    padding: 6px;
    font-size: 18px;
}
.header-actions .el-button:hover {
    color: white;
}

.chat-viewport {
  height: 480px;
  overflow-y: auto;
  padding: 24px 30px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  scroll-behavior: smooth;
}

/* Custom Scrollbar */
.chat-viewport::-webkit-scrollbar { width: 5px; }
.chat-viewport::-webkit-scrollbar-track { background: transparent; }
.chat-viewport::-webkit-scrollbar-thumb { 
    background: rgba(255, 255, 255, 0.1); 
    border-radius: 10px; 
}

/* Messages */
.msg-row { display: flex; width: 100%; animate: fadeIn 0.3s ease-out; }
.msg-row.user { justify-content: flex-end; }
.msg-row.assistant { justify-content: flex-start; }

.msg-bubble {
  max-width: 88%;
  padding: 14px 18px;
  border-radius: 20px;
  font-size: 14.5px;
  line-height: 1.6;
  position: relative;
  word-break: break-all;
  white-space: pre-wrap;
}

.user .msg-bubble {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: white;
  border-bottom-right-radius: 4px;
  box-shadow: 0 4px 15px rgba(37, 99, 235, 0.3);
}

.assistant .msg-bubble {
  background: rgba(30, 41, 59, 0.8);
  color: #f8fafc;
  border-bottom-left-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.msg-time {
  font-size: 10px;
  margin-top: 6px;
  opacity: 0.4;
  font-family: monospace;
}

.user .msg-time { text-align: right; }

.ghost .msg-bubble {
  background: rgba(59, 130, 246, 0.15);
  border: 1px dashed rgba(59, 130, 246, 0.4);
  color: #bfdbfe;
  backdrop-filter: blur(4px);
}

.cursor { 
    display: inline-block;
    width: 2px;
    height: 1em;
    background: currentColor;
    margin-left: 2px;
    vertical-align: middle;
    animation: blink 1s infinite; 
}

/* Indicators */
.bg-green { background: #10b981 !important; box-shadow: 0 0 12px rgba(16, 185, 129, 0.5); }
.bg-blue { background: #3b82f6 !important; box-shadow: 0 0 12px rgba(59, 130, 246, 0.5); }
.bg-purple { background: #a855f7 !important; box-shadow: 0 0 12px rgba(168, 85, 247, 0.5); }
.bg-red { background: #ef4444 !important; box-shadow: 0 0 12px rgba(239, 68, 68, 0.5); }

.typing-indicator { display: flex; gap: 5px; padding: 6px 0; }
.typing-indicator span {
  width: 6px; height: 6px; background: #94a3b8; border-radius: 50%;
  animation: bounce-wave 1.4s infinite ease-in-out;
}
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

.panel-footer {
  padding: 16px 24px;
  background: rgba(0, 0, 0, 0.2);
}

.error-strip {
    background: rgba(239, 68, 68, 0.15);
    color: #fca5a5;
    padding: 10px 14px;
    border-radius: 12px;
    font-size: 13px;
    display: flex;
    align-items: center;
    gap: 8px;
    border: 1px solid rgba(239, 68, 68, 0.2);
}

.footer-btns { display: flex; gap: 12px; justify-content: flex-end; }
.footer-btns .el-button {
    background: rgba(255,255,255,0.05);
    border: 1px solid rgba(255,255,255,0.1);
    color: #e2e8f0;
}
.footer-btns .el-button--primary {
    background: #3b82f6;
    border: none;
    color: white;
}

/* Animations */
@keyframes breathe {
  0%, 100% { transform: scale(1); filter: brightness(1); }
  50% { transform: scale(1.04); filter: brightness(1.1); }
}

@keyframes rotate-pulse {
  0% { transform: rotate(0) scale(1); }
  50% { transform: rotate(180deg) scale(1.08); }
  100% { transform: rotate(360deg) scale(1); }
}

@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

@keyframes bounce-wave {
  0%, 80%, 100% { transform: translateY(0); opacity: 0.4; }
  40% { transform: translateY(-6px); opacity: 1; }
}

@keyframes ping {
    75%, 100% { transform: scale(2.5); opacity: 0; }
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}

.slide-up-enter-active, .slide-up-leave-active { transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1); }
.slide-up-enter-from, .slide-up-leave-to { opacity: 0; transform: translateY(50px) scale(0.95); filter: blur(10px); }

.pop-enter-active, .pop-leave-active { transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1); }
.pop-enter-from, .pop-leave-to { opacity: 0; transform: scale(0); }
</style>
