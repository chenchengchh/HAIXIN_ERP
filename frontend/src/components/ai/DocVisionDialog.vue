<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    width="720px"
    :close-on-click-modal="false"
    @update:model-value="handleVisibleChange"
    @open="handleOpen"
  >
    <div class="doc-vision">
      <!-- 录入模式：拍照/上传 OCR 或 粘贴文本 -->
      <el-tabs v-model="activeTab">
        <el-tab-pane label="拍照/上传识别" name="image">
          <div class="upload-area">
            <el-upload
              drag
              accept="image/*"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleImagePick"
            >
              <el-icon :size="40"><Camera /></el-icon>
              <div class="el-upload__text">拖拽图片到此处，或 <em>点击选择图片</em>（手机可直接拍照）</div>
            </el-upload>
            <div v-if="ocrRunning" class="ocr-progress">
              <el-progress :percentage="ocrProgress" :stroke-width="10" striped striped-flow />
              <div class="ocr-status">{{ ocrStatus }}</div>
            </div>
            <div v-if="ocrError" class="ocr-error">
              <el-alert type="warning" :closable="false" show-icon
                title="OCR 引擎不可用（可能网络受限），请切换到「粘贴文本识别」手动录入" />
            </div>
            <div v-if="previewUrl" class="preview-wrap">
              <img :src="previewUrl" class="preview-img" alt="识别图片预览" />
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="粘贴文本识别" name="text">
          <el-alert type="info" :closable="false" show-icon
            title="将单据/仪表上的文字粘贴或输入到下方，系统将智能提取关键字段" />
        </el-tab-pane>
      </el-tabs>

      <!-- OCR 文本（可编辑） -->
      <div class="text-section">
        <div class="section-label">识别文本（可编辑修正）</div>
        <el-input
          v-model="ocrText"
          type="textarea"
          :rows="6"
          placeholder="OCR 识别结果将显示在此处；也可以直接粘贴/输入文本"
        />
        <div class="extract-bar">
          <el-button
            type="primary"
            :loading="parsing"
            :disabled="!ocrText.trim()"
            @click="handleParse"
          >
            智能提取字段
          </el-button>
          <span v-if="parseMessage" class="parse-message">{{ parseMessage }}</span>
        </div>
      </div>

      <!-- 结构化字段（可编辑） -->
      <div v-if="hasFields" class="fields-section">
        <div class="section-label">
          提取结果（请核对后确认预填）
          <el-tag v-if="confidence > 0" size="small" :type="confidence >= 0.75 ? 'success' : 'warning'" effect="plain">
            置信度 {{ (confidence * 100).toFixed(0) }}%
          </el-tag>
        </div>
        <el-form label-width="110px" class="fields-form">
          <template v-if="docType === 'invoice'">
            <el-form-item label="发票号码">
              <el-input v-model="fields.invoiceNo" placeholder="发票号码" />
            </el-form-item>
            <el-form-item label="开票日期">
              <el-date-picker v-model="fields.invoiceDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
            <el-form-item label="价税合计">
              <el-input-number v-model="fields.totalAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
            <el-form-item label="销售方名称">
              <el-input v-model="fields.supplierName" placeholder="供应商/销售方" />
            </el-form-item>
          </template>
          <template v-else>
            <el-form-item label="点位编码">
              <el-input v-model="fields.tagCode" placeholder="如 TEMP_001" />
            </el-form-item>
            <el-form-item label="仪表读数">
              <el-input-number v-model="fields.reading" :precision="3" style="width: 100%" />
            </el-form-item>
            <el-form-item label="单位">
              <el-input v-model="fields.unit" placeholder="如 °C / bar" />
            </el-form-item>
          </template>
        </el-form>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleVisibleChange(false)">取消</el-button>
      <el-button type="primary" :disabled="!hasFields" @click="handleConfirm">确认预填</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { parseVisionDocument } from '@/api/ai'

/**
 * 单据/仪表视觉理解对话框（S11，CONFIRM 级）
 * 拍照/上传图片经浏览器端 Tesseract.js OCR 转文本（离线不可用时降级为粘贴文本模式），
 * 文本送 ai-brain 规则结构化，提取结果仅用于预填表单——人工确认后才走业务端点写入。
 */

const props = defineProps<{
  modelValue: boolean
  /** 文档类型：invoice 发票 / gauge 仪表 */
  docType: 'invoice' | 'gauge'
  title?: string
  /** 辅助提示（如仪表点位编码） */
  hint?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  /** 人工确认预填：invoice 返回 {invoiceNo,invoiceDate,totalAmount,supplierName}；gauge 返回 {tagCode,reading,unit} */
  (e: 'confirm', fields: Record<string, any>): void
}>()

const title = computed(() => props.title || (props.docType === 'invoice' ? '单据视觉识别' : '仪表拍照识别'))

// 交互状态
const activeTab = ref('image')
const ocrText = ref('')
const ocrRunning = ref(false)
const ocrProgress = ref(0)
const ocrStatus = ref('')
const ocrError = ref(false)
const previewUrl = ref('')
const parsing = ref(false)
const parseMessage = ref('')
const confidence = ref(0)
const fields = ref<Record<string, any>>({})

/** 是否已有可确认的字段 */
const hasFields = computed(() => Object.keys(fields.value).length > 0)

/** 对话框可见性变更 */
const handleVisibleChange = (value: boolean) => {
  emit('update:modelValue', value)
}

/** 打开时重置状态 */
const handleOpen = () => {
  activeTab.value = 'image'
  ocrText.value = ''
  ocrError.value = false
  ocrRunning.value = false
  ocrProgress.value = 0
  previewUrl.value = ''
  parseMessage.value = ''
  confidence.value = 0
  fields.value = {}
}

/**
 * 图片选择后执行浏览器端 OCR（Tesseract.js）
 * 语言包默认从 CDN 加载；内网离线加载失败时降级提示使用粘贴文本模式
 */
const handleImagePick = async (uploadFile: any) => {
  const file: File | undefined = uploadFile?.raw
  if (!file) return
  previewUrl.value = URL.createObjectURL(file)
  ocrError.value = false
  ocrRunning.value = true
  ocrProgress.value = 0
  ocrStatus.value = 'OCR 引擎加载中...'

  let worker: any = null
  try {
    // 动态导入，避免首屏加载 OCR 引擎
    const { createWorker } = await import('tesseract.js')
    worker = await createWorker('chi_sim+eng', 1, {
      logger: (m: any) => {
        if (m?.status === 'recognizing text' && typeof m?.progress === 'number') {
          ocrProgress.value = Math.round(m.progress * 100)
          ocrStatus.value = '正在识别文字...'
        }
      }
    })
    const { data } = await worker.recognize(file)
    const text = (data?.text || '').trim()
    if (!text) {
      ElMessage.warning('未识别到文字，请换更清晰的图片或使用粘贴文本模式')
    } else {
      ocrText.value = text
      ElMessage.success('OCR 识别完成，请核对文本后点击「智能提取字段」')
    }
  } catch (e) {
    console.error('[DocVision] OCR 失败', e)
    ocrError.value = true
    activeTab.value = 'text'
  } finally {
    if (worker) {
      try { await worker.terminate() } catch (e) { /* 忽略终止异常 */ }
    }
    ocrRunning.value = false
  }
}

/**
 * 调用 ai-brain 规则结构化端点提取字段
 */
const handleParse = async () => {
  if (!ocrText.value.trim()) return
  parsing.value = true
  parseMessage.value = ''
  try {
    const res = await parseVisionDocument({
      docType: props.docType,
      ocrText: ocrText.value.trim(),
      hint: props.hint || ''
    })
    const data = (res as any)?.data?.data ?? (res as any)?.data ?? {}
    if (data?.success) {
      fields.value = { ...(data.fields || {}) }
      confidence.value = Number(data.confidence || 0)
      parseMessage.value = data.message || '提取完成'
    } else {
      fields.value = { ...(data?.fields || {}) }
      confidence.value = 0
      parseMessage.value = data?.message || '未能提取字段，请人工录入'
      if (!hasFields.value) {
        // 提取失败也给出空字段骨架，方便人工直接填写
        fields.value = props.docType === 'invoice'
          ? { invoiceNo: '', invoiceDate: '', totalAmount: undefined, supplierName: '' }
          : { tagCode: props.hint || '', reading: undefined, unit: '' }
      }
    }
  } catch (e) {
    console.error('[DocVision] 结构化提取失败', e)
    ElMessage.error('结构化提取服务异常，请稍后重试或手工录入')
  } finally {
    parsing.value = false
  }
}

/** 人工确认：把核对后的字段交给调用方预填表单 */
const handleConfirm = () => {
  emit('confirm', { ...fields.value })
  handleVisibleChange(false)
}
</script>

<style scoped lang="scss">
.doc-vision {
  .upload-area {
    .ocr-progress {
      margin-top: 12px;

      .ocr-status {
        margin-top: 4px;
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }

    .ocr-error {
      margin-top: 12px;
    }

    .preview-wrap {
      margin-top: 12px;
      text-align: center;

      .preview-img {
        max-width: 100%;
        max-height: 220px;
        border-radius: 8px;
        border: 1px solid var(--el-border-color-lighter);
      }
    }
  }

  .text-section {
    margin-top: 12px;

    .extract-bar {
      margin-top: 10px;
      display: flex;
      align-items: center;
      gap: 12px;

      .parse-message {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }

  .section-label {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    font-size: 13px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .fields-section {
    margin-top: 14px;
    padding-top: 10px;
    border-top: 1px dashed var(--el-border-color-lighter);
  }
}
</style>
