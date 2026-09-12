<template>
  <el-drawer
    v-model="visible"
    :title="isEdit ? '编辑采集任务' : '新建采集任务'"
    size="550px"
    destroy-on-close
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="task-form">
      <el-tabs v-model="activeTab">
        <!-- 基础配置 -->
        <el-tab-pane label="基础配置" name="basic">
          <el-form-item label="任务名称" prop="name">
            <el-input v-model="form.name" placeholder="例如: 装修客户采集-北京" />
          </el-form-item>
          <el-form-item label="搜索关键词" prop="searchKeyword">
            <el-input v-model="form.searchKeyword" placeholder="例如: 装修设计, 全屋定制" />
            <div class="form-tip">系统将根据此关键词在抖音搜索相关视频</div>
          </el-form-item>
        </el-tab-pane>

        <!-- 筛选规则 -->
        <el-tab-pane label="筛选规则" name="filter">
          <el-alert title="命中以下关键词的评论将被识别为意向客户" type="info" :closable="false" style="margin-bottom: 15px" />
          <el-form-item label="意向关键词">
            <el-select v-model="form.filterConfig.intentKeywords" multiple allow-create filterable default-first-option placeholder="输入并回车添加" style="width: 100%">
            </el-select>
          </el-form-item>
          <el-form-item label="排除关键词">
            <el-select v-model="form.filterConfig.excludeKeywords" multiple allow-create filterable default-first-option placeholder="输入并回车添加" style="width: 100%">
            </el-select>
          </el-form-item>
        </el-tab-pane>

        <!-- 触达配置 -->
        <el-tab-pane label="触达配置" name="message">
          <el-form-item label="私信模板">
            <el-input 
              v-model="form.messageConfig.template" 
              type="textarea" 
              :rows="4" 
              placeholder="你好，看到你在评论区对{产品}感兴趣..." 
            />
          </el-form-item>
          <el-form-item label="发送上限">
             <el-input-number v-model="form.messageConfig.maxCount" :min="1" :max="100" />
             <span class="unit">人/次</span>
          </el-form-item>
          <el-form-item label="发送间隔">
             <el-slider v-model="form.messageConfig.interval" range :min="2" :max="60" />
             <div class="form-tip">随机间隔 {{ form.messageConfig.interval[0] }} - {{ form.messageConfig.interval[1] }} 秒</div>
          </el-form-item>
        </el-tab-pane>
      </el-tabs>
    </el-form>

    <template #footer>
      <div class="drawer-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存配置</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import type { DouyinTask } from '../types'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
  taskData?: DouyinTask | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'save', task: Partial<DouyinTask>): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = ref(false)
const activeTab = ref('basic')
const formRef = ref()

// Initial State
const defaultForm = () => ({
  name: '',
  searchKeyword: '',
  filterConfig: {
    intentKeywords: ['多少钱', '怎么买', '求链接', '感兴趣', '价格'],
    excludeKeywords: ['互粉', '骗子']
  },
  messageConfig: {
    template: '你好，刚看到你在评论区问关于{产品}的问题，我是厂家直销，可以加个V详细发您资料参考下~',
    maxCount: 10,
    interval: [5, 15] as [number, number]
  }
})

const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  searchKeyword: [{ required: true, message: '请输入搜索关键词', trigger: 'blur' }]
}

// 监听抽屉打开/关闭和任务数据变化
watch(
  [() => props.modelValue, () => props.taskData],
  ([newVisible, newTaskData]) => {
    if (newVisible) {
      if (newTaskData) {
          isEdit.value = true
          // Deep copy to avoid mutating prop directly
          const data = JSON.parse(JSON.stringify(newTaskData))
          Object.assign(form, {
            name: data.name || '',
            searchKeyword: data.searchKeyword || '',
            filterConfig: {
              ...defaultForm().filterConfig,
              intentKeywords: data.filterConfig?.intentKeywords || defaultForm().filterConfig.intentKeywords,
              excludeKeywords: data.filterConfig?.excludeKeywords || defaultForm().filterConfig.excludeKeywords
            },
            messageConfig: {
              ...defaultForm().messageConfig,
              template: data.messageConfig?.template || defaultForm().messageConfig.template,
              maxCount: data.messageConfig?.maxCount || defaultForm().messageConfig.maxCount,
              interval: data.messageConfig?.interval || defaultForm().messageConfig.interval
            }
          })
          console.log('编辑模式 - 表单数据已加载:', form)
        } else {
          isEdit.value = false
          Object.assign(form, defaultForm())
          console.log('新增模式 - 表单已重置')
        }
    }
  },
  { deep: true }
)

const handleClose = () => {
  visible.value = false
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid: boolean) => {
    if (valid) {
      emit('save', JSON.parse(JSON.stringify(form)))
      handleClose()
    }
  })
}
</script>

<style scoped lang="scss">
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 4px;
}
.unit {
  margin-left: 10px;
  color: #606266;
}
.drawer-footer {
  display: flex;
  justify-content: flex-start;
}
</style>
