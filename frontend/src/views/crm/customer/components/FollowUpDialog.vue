<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="handleDialogVisibleChange"
    title="新增跟进记录"
    width="600px"
    @close="handleClose"
  >
    <el-form :model="followUp" :rules="rules" ref="formRef" label-width="100px">
      <!-- 跟进记录信息 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="跟进方式" prop="followUpType">
            <el-select v-model="followUp.followUpType" placeholder="请选择跟进方式">
              <el-option label="电话" value="call" />
              <el-option label="邮件" value="email" />
              <el-option label="拜访" value="visit" />
              <el-option label="微信" value="wechat" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="跟进时间" prop="followUpTime">
            <el-date-picker
              v-model="followUp.followUpTime"
              type="datetime"
              placeholder="选择跟进时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="跟进内容" prop="content">
            <el-input
              v-model="followUp.content"
              placeholder="请输入跟进内容"
              type="textarea"
              :rows="4"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="下次计划" prop="nextPlan">
            <el-input
              v-model="followUp.nextPlan"
              placeholder="请输入下次计划"
              type="textarea"
              :rows="2"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="下次跟进时间" prop="nextTime">
            <el-date-picker
              v-model="followUp.nextTime"
              type="datetime"
              placeholder="选择下次跟进时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'

// Props
const props = defineProps<{
  visible: boolean
  followUp: any
}>()

// Emits
const emit = defineEmits<{
  close: []
  save: [followUp: any]
}>()

// 表单引用
const formRef = ref()

// 表单数据
const followUp = reactive<any>({
  followUpType: 'call',
  followUpTime: new Date().toISOString().slice(0, 16),
  content: '',
  nextPlan: '',
  nextTime: '',
  customerId: 0,
  followUpUserId: 0,
  followUpUserName: ''
})

// 监听props变化，更新表单数据
watch(
  () => props.followUp,
  (newVal) => {
    Object.assign(followUp, newVal)
  },
  { deep: true, immediate: true }
)

// 表单验证规则
const rules = reactive({
  followUpType: [
    { required: true, message: '请选择跟进方式', trigger: 'change' }
  ],
  followUpTime: [
    { required: true, message: '请选择跟进时间', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入跟进内容', trigger: 'blur' },
    { min: 5, max: 500, message: '跟进内容长度在 5 到 500 个字符', trigger: 'blur' }
  ]
})

// 处理对话框可见性变化
const handleDialogVisibleChange = (newVisible: boolean) => {
  if (!newVisible) {
    emit('close')
    resetForm()
  }
}

// 关闭对话框
const handleClose = () => {
  emit('close')
  resetForm()
}

// 保存跟进记录
const handleSave = () => {
  formRef.value.validate((valid: boolean) => {
    if (valid) {
      emit('save', { ...followUp })
      resetForm()
    }
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(followUp, {
    followUpType: 'call',
    followUpTime: new Date().toISOString().slice(0, 16),
    content: '',
    nextPlan: '',
    nextTime: '',
    customerId: 0,
    followUpUserId: 0,
    followUpUserName: ''
  })
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>