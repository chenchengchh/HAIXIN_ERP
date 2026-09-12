<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="handleDialogVisibleChange"
    title="新增联系人"
    width="600px"
    @close="handleClose"
  >
    <el-form :model="contact" :rules="rules" ref="formRef" label-width="100px">
      <!-- 联系人基本信息 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="联系人姓名" prop="contactName">
            <el-input v-model="contact.contactName" placeholder="请输入联系人姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="职位" prop="position">
            <el-input v-model="contact.position" placeholder="请输入职位" />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="电话" prop="phone">
            <el-input v-model="contact.phone" placeholder="请输入电话" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机" prop="mobile">
            <el-input v-model="contact.mobile" placeholder="请输入手机" />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="contact.email" placeholder="请输入邮箱" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="微信" prop="wechat">
            <el-input v-model="contact.wechat" placeholder="请输入微信" />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="备注">
            <el-input v-model="contact.remark" placeholder="请输入备注" type="textarea" :rows="2" />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="是否主要联系人">
            <el-switch v-model="contact.isPrimary" />
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
  contact: any
}>()

// Emits
const emit = defineEmits<{
  close: []
  save: [contact: any]
}>()

// 表单引用
const formRef = ref()

// 表单数据
const contact = reactive<any>({
  contactName: '',
  position: '',
  phone: '',
  mobile: '',
  email: '',
  wechat: '',
  remark: '',
  isPrimary: false,
  customerId: 0
})

// 监听props变化，更新表单数据
watch(
  () => props.contact,
  (newVal) => {
    Object.assign(contact, newVal)
  },
  { deep: true, immediate: true }
)

// 表单验证规则
const rules = reactive({
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ],
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
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

// 保存联系人
const handleSave = () => {
  formRef.value.validate((valid: boolean) => {
    if (valid) {
      emit('save', { ...contact })
      resetForm()
    }
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(contact, {
    contactName: '',
    position: '',
    phone: '',
    mobile: '',
    email: '',
    wechat: '',
    remark: '',
    isPrimary: false,
    customerId: 0
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