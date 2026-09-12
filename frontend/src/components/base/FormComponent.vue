<template>
  <div class="form-component">
    <el-card shadow="never" class="form-card">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-position="labelPosition"
        :label-width="labelWidth"
        :size="size"
        :inline="inline"
        @submit.prevent="handleSubmit"
        class="main-form"
      >
        <el-row :gutter="24">
          <el-col
            v-for="(field, index) in fields"
            :key="index"
            :span="field.span || (inline ? undefined : 24)"
            :xs="24"
            :sm="field.span || (inline ? undefined : 12)"
            :md="field.span || (inline ? undefined : 12)"
            :lg="field.span || (inline ? undefined : 8)"
            :xl="field.span || (inline ? undefined : 6)"
          >
            <!-- 表单字段 -->
            <el-form-item
              :label="field.label"
              :prop="field.prop"
              :required="field.required"
              :rules="field.rules"
              :label-class="field.labelClass"
              :class="field.wrapperClass"
            >
              <!-- 文本输入框 -->
              <el-input
                v-if="field.type === 'input'"
                v-model="formData[field.prop]"
                :placeholder="field.placeholder || `请输入${field.label}`"
                :disabled="field.disabled"
                :readonly="field.readonly"
                :clearable="field.clearable"
                :show-password="field.showPassword"
                :maxlength="field.maxlength"
                :show-word-limit="field.showWordLimit"
                :prefix-icon="field.prefixIcon"
                :suffix-icon="field.suffixIcon"
                @input="handleInput(field.prop, $event)"
                @change="handleChange(field.prop, $event)"
                @blur="handleBlur(field.prop)"
                class="full-width"
              >
                <template #prefix v-if="field.prefixSlot">
                  <slot :name="field.prefixSlot" />
                </template>
                <template #suffix v-if="field.suffixSlot">
                  <slot :name="field.suffixSlot" />
                </template>
              </el-input>
              
              <!-- 多行文本输入框 -->
              <el-input
                v-else-if="field.type === 'textarea'"
                v-model="formData[field.prop]"
                type="textarea"
                :placeholder="field.placeholder || `请输入${field.label}`"
                :disabled="field.disabled"
                :readonly="field.readonly"
                :clearable="field.clearable"
                :rows="field.rows || 4"
                :maxlength="field.maxlength"
                :show-word-limit="field.showWordLimit"
                @input="handleInput(field.prop, $event)"
                @change="handleChange(field.prop, $event)"
                @blur="handleBlur(field.prop)"
                class="full-width"
              />
              
              <!-- 下拉选择框 -->
              <el-select
                v-else-if="field.type === 'select'"
                v-model="formData[field.prop]"
                :placeholder="field.placeholder || `请选择${field.label}`"
                :disabled="field.disabled"
                :multiple="field.multiple"
                :filterable="field.filterable"
                :clearable="field.clearable"
                :collapse-tags="field.collapseTags"
                @change="handleChange(field.prop, $event)"
                @blur="handleBlur(field.prop)"
                class="full-width"
              >
                <el-option
                  v-for="option in field.options"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
              
              <!-- 日期选择器 -->
              <el-date-picker
                v-else-if="field.type === 'date'"
                v-model="formData[field.prop]"
                type="date"
                :placeholder="field.placeholder || `请选择${field.label}`"
                :disabled="field.disabled"
                :clearable="field.clearable"
                :format="field.format || 'YYYY-MM-DD'"
                :value-format="field.valueFormat || 'YYYY-MM-DD'"
                @change="handleChange(field.prop, $event)"
                @blur="handleBlur(field.prop)"
                class="full-width"
              />
              
              <!-- 日期时间选择器 -->
              <el-date-picker
                v-else-if="field.type === 'datetime'"
                v-model="formData[field.prop]"
                type="datetime"
                :placeholder="field.placeholder || `请选择${field.label}`"
                :disabled="field.disabled"
                :clearable="field.clearable"
                :format="field.format || 'YYYY-MM-DD HH:mm:ss'"
                :value-format="field.valueFormat || 'YYYY-MM-DD HH:mm:ss'"
                @change="handleChange(field.prop, $event)"
                @blur="handleBlur(field.prop)"
                class="full-width"
              />
              
              <!-- 日期范围选择器 -->
              <el-date-picker
                v-else-if="field.type === 'daterange'"
                v-model="formData[field.prop]"
                type="daterange"
                :placeholder="field.placeholder || `请选择${field.label}`"
                :disabled="field.disabled"
                :clearable="field.clearable"
                :format="field.format || 'YYYY-MM-DD'"
                :value-format="field.valueFormat || 'YYYY-MM-DD'"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                @change="handleChange(field.prop, $event)"
                @blur="handleBlur(field.prop)"
                class="full-width"
              />
              
              <!-- 时间选择器 -->
              <el-time-picker
                v-else-if="field.type === 'time'"
                v-model="formData[field.prop]"
                :placeholder="field.placeholder || `请选择${field.label}`"
                :disabled="field.disabled"
                :clearable="field.clearable"
                :format="field.format || 'HH:mm:ss'"
                :value-format="field.valueFormat || 'HH:mm:ss'"
                @change="handleChange(field.prop, $event)"
                @blur="handleBlur(field.prop)"
                class="full-width"
              />
              
              <!-- 开关 -->
              <el-switch
                v-else-if="field.type === 'switch'"
                v-model="formData[field.prop]"
                :disabled="field.disabled"
                :active-text="field.activeText"
                :inactive-text="field.inactiveText"
                @change="handleChange(field.prop, $event)"
              />
              
              <!-- 单选框组 -->
              <el-radio-group
                v-else-if="field.type === 'radio'"
                v-model="formData[field.prop]"
                :disabled="field.disabled"
                :size="size"
                @change="handleChange(field.prop, $event)"
              >
                <el-radio
                  v-for="option in field.options"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </el-radio>
              </el-radio-group>
              
              <!-- 多选框组 -->
              <el-checkbox-group
                v-else-if="field.type === 'checkbox'"
                v-model="formData[field.prop]"
                :disabled="field.disabled"
                :size="size"
                @change="handleChange(field.prop, $event)"
              >
                <el-checkbox
                  v-for="option in field.options"
                  :key="option.value"
                  :label="option.value"
                >
                  {{ option.label }}
                </el-checkbox>
              </el-checkbox-group>
              
              <!-- 数字输入框 -->
              <el-input-number
                v-else-if="field.type === 'number'"
                v-model="formData[field.prop]"
                :placeholder="field.placeholder || `请输入${field.label}`"
                :disabled="field.disabled"
                :min="field.min"
                :max="field.max"
                :step="field.step || 1"
                :precision="field.precision"
                @change="handleChange(field.prop, $event)"
                @blur="handleBlur(field.prop)"
                class="full-width"
              />
              
              <!-- 自定义插槽 -->
              <slot v-else-if="field.slotName" :name="field.slotName" />
            </el-form-item>
          </el-col>
          
          <!-- 操作按钮 (如果不是内联表单，或者是内联表单但想在最后显示) -->
          <el-col :span="24" v-if="showButtons">
            <el-form-item>
              <div class="form-actions">
                <el-button
                  v-if="showReset"
                  @click="handleReset"
                  :icon="Refresh"
                >
                  {{ resetText }}
                </el-button>
                <el-button
                  v-if="showSubmit"
                  type="primary"
                  :loading="submitting"
                  @click="handleSubmit"
                >
                  {{ submitText }}
                </el-button>
                <slot v-if="buttonSlot" name="buttons" />
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { logger } from '../../utils/logger'
import { Refresh } from '@element-plus/icons-vue'

// 组件属性定义
interface FormField {
  prop: string
  label: string
  type: 'input' | 'textarea' | 'select' | 'date' | 'datetime' | 'daterange' | 'time' | 'switch' | 'radio' | 'checkbox' | 'number'
  placeholder?: string
  disabled?: boolean
  readonly?: boolean
  required?: boolean
  clearable?: boolean
  rules?: any[]
  options?: Array<{ value: any; label: string }>
  multiple?: boolean
  filterable?: boolean
  collapseTags?: boolean
  showPassword?: boolean
  maxlength?: number
  showWordLimit?: boolean
  prefixIcon?: any
  suffixIcon?: any
  prefixSlot?: string
  suffixSlot?: string
  slotName?: string
  rows?: number
  format?: string
  valueFormat?: string
  activeText?: string
  inactiveText?: string
  min?: number
  max?: number
  step?: number
  precision?: number
  span?: number
  labelClass?: string
  wrapperClass?: string
}

// 组件属性
const props = defineProps<{
  // 表单字段配置
  fields: Array<FormField>
  // 表单数据
  modelValue?: Record<string, any>
  // 表单规则
  rules?: Record<string, any[]>
  // 标签位置
  labelPosition?: 'top' | 'left' | 'right'
  // 标签宽度
  labelWidth?: string | number
  // 表单大小
  size?: 'large' | 'default' | 'small'
  // 是否内联表单
  inline?: boolean
  // 是否显示操作按钮
  showButtons?: boolean
  // 是否显示重置按钮
  showReset?: boolean
  // 是否显示提交按钮
  showSubmit?: boolean
  // 重置按钮文本
  resetText?: string
  // 提交按钮文本
  submitText?: string
  // 按钮插槽名称
  buttonSlot?: string
  // 初始值
  initialValue?: Record<string, any>
  // 是否显示实时验证
  showRealTimeValidate?: boolean
  // 是否显示失焦验证
  showBlurValidate?: boolean
}>()

// 组件事件
const emit = defineEmits<{
  // 表单提交事件
  submit: [formData: any]
  // 表单重置事件
  reset: [formData: any]
  // 字段值改变事件
  change: [field: string, value: any]
  // 字段输入事件
  input: [field: string, value: any]
  // 字段失焦事件
  blur: [field: string]
  // 表单数据双向绑定事件
  'update:modelValue': [modelValue: any]
}>()

// 响应式数据
// 表单引用
const formRef = ref<any>(null)
// 表单数据
const formData = reactive<Record<string, any>>({})
// 提交加载状态
const submitting = ref(false)

// 初始化
onMounted(() => {
  // 初始化表单数据
  initFormData()
})

// 监听初始值变化
watch(
  () => props.initialValue,
  (newValue) => {
    if (newValue) {
      Object.assign(formData, newValue)
    }
  },
  { immediate: true, deep: true }
)

// 监听modelValue变化
watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue) {
      Object.assign(formData, newValue)
    }
  },
  { immediate: true, deep: true }
)

// 监听formData变化
watch(
  formData,
  (newValue) => {
    emit('update:modelValue', { ...newValue })
  },
  { deep: true }
)

// 初始化表单数据
const initFormData = () => {
  // 清空表单数据
  Object.keys(formData).forEach(key => {
    delete formData[key]
  })
  
  // 初始化字段默认值
  props.fields.forEach(field => {
    // 如果有初始值，使用初始值
    if (props.initialValue && props.initialValue[field.prop] !== undefined) {
      formData[field.prop] = props.initialValue[field.prop]
    }
    // 否则根据字段类型设置默认值
    else {
      switch (field.type) {
        case 'checkbox':
          formData[field.prop] = []
          break
        case 'number':
          formData[field.prop] = null
          break
        case 'switch':
          formData[field.prop] = false
          break
        case 'select':
          formData[field.prop] = field.multiple ? [] : ''
          break
        case 'date':
        case 'datetime':
        case 'time':
          formData[field.prop] = null
          break
        case 'daterange':
          formData[field.prop] = null
          break
        default:
          formData[field.prop] = ''
      }
    }
  })
}

// 处理表单提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    submitting.value = true
    await formRef.value.validate()
    emit('submit', { ...formData })
  } catch (error) {
    logger.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

// 处理表单重置
const handleReset = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  initFormData()
  emit('reset', { ...formData })
}

// 处理字段输入事件
const handleInput = (field: string, value: any) => {
  emit('input', field, value)
  // 实时验证
  if (props.showRealTimeValidate) {
    validateField(field)
  }
}

// 处理字段值改变事件
const handleChange = (field: string, value: any) => {
  emit('change', field, value)
  // 实时验证
  if (props.showRealTimeValidate) {
    validateField(field)
  }
}

// 处理字段失去焦点事件
const handleBlur = (field: string) => {
  // 焦点验证
  if (props.showBlurValidate) {
    validateField(field)
  }
  emit('blur', field)
}

// 验证单个字段
const validateField = (field: string) => {
  if (formRef.value) {
    formRef.value.validateField(field)
  }
}

// 暴露方法给父组件
const expose = {
  // 验证表单
  validate: () => formRef.value?.validate(),
  // 重置表单
  reset: handleReset,
  // 提交表单
  submit: handleSubmit,
  // 获取表单数据
  getFormData: () => ({ ...formData }),
  // 设置表单数据
  setFormData: (data: Record<string, any>) => {
    Object.assign(formData, data)
  }
}

defineExpose(expose)
</script>

<style scoped lang="scss">
.form-component {
  width: 100%;
  
  .form-card {
    border: none;
    :deep(.el-card__body) {
      padding: 0;
    }
  }
  
  .main-form {
    padding: 20px;
    background-color: #fff;
    border-radius: 4px;
    
    .full-width {
      width: 100%;
    }
    
    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 10px;
      padding-top: 20px;
      border-top: 1px solid var(--el-border-color-lighter);
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .form-component {
    .main-form {
      padding: 15px;
      
      .form-actions {
        flex-direction: column-reverse;
        
        .el-button {
          width: 100%;
          margin-left: 0;
        }
      }
    }
  }
}
</style>