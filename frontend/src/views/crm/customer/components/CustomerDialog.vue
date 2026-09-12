<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="handleDialogVisibleChange"
    :title="customer.id ? '编辑客户' : '新增客户'"
    width="700px"
    @close="handleClose"
  >
    <el-form :model="customer" :rules="rules" ref="formRef" label-width="100px">
      <!-- 基本信息 -->
      <el-divider content-position="left">基本信息</el-divider>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="客户名称" prop="customerName">
            <el-input v-model="customer.customerName" placeholder="请输入客户名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="客户编号" prop="customerNo">
            <el-input v-model="customer.customerNo" placeholder="请输入客户编号" :disabled="!!customer.id" />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="客户类型" prop="customerType">
            <el-select v-model="customer.customerType" placeholder="请选择客户类型">
              <el-option label="企业" value="enterprise" />
              <el-option label="个人" value="individual" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属行业" prop="industry">
            <el-input v-model="customer.industry" placeholder="请输入所属行业" />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="企业规模" prop="scale">
            <el-select v-model="customer.scale" placeholder="请选择企业规模">
              <el-option label="大型企业" value="large" />
              <el-option label="中型企业" value="medium" />
              <el-option label="小型企业" value="small" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="客户级别" prop="level">
            <el-select v-model="customer.level" placeholder="请选择客户级别">
              <el-option label="A级" value="A" />
              <el-option label="B级" value="B" />
              <el-option label="C级" value="C" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="客户状态" prop="status">
            <el-select v-model="customer.status" placeholder="请选择客户状态">
              <el-option label="潜在客户" value="potential" />
              <el-option label="活跃客户" value="active" />
              <el-option label="不活跃客户" value="inactive" />
              <el-option label="流失客户" value="lost" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="客户来源" prop="source">
            <el-input v-model="customer.source" placeholder="请输入客户来源" />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="详细地址" prop="address">
            <el-input v-model="customer.address" placeholder="请输入详细地址" type="textarea" :rows="2" />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="公司网址" prop="website">
            <el-input v-model="customer.website" placeholder="请输入公司网址" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="地区" prop="region">
            <el-input v-model="customer.region" placeholder="请输入地区" />
          </el-form-item>
        </el-col>
      </el-row>
      
      <!-- 负责人信息 -->
      <el-divider content-position="left">负责人信息</el-divider>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="负责人ID" prop="ownerId">
            <el-input v-model.number="customer.ownerId" placeholder="请输入负责人ID" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="负责人姓名" prop="ownerName">
            <el-input v-model="customer.ownerName" placeholder="请输入负责人姓名" />
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
import type { Customer } from '../../../../api/crm/customer'
import { validationRules } from '../../../../utils/validationRules'

// Props
const props = defineProps<{
  visible: boolean
  customer: Partial<Customer>
}>()

// Emits
const emit = defineEmits<{
  close: []
  save: [customer: Customer]
}>()

// 表单引用
const formRef = ref()

// 表单数据
const customer = reactive<Partial<Customer>>({
  id: undefined,
  customerNo: '',
  customerName: '',
  customerType: 'enterprise',
  industry: '',
  scale: 'medium',
  level: 'B',
  status: 'potential',
  source: '',
  tags: [],
  region: '',
  address: '',
  website: '',
  ownerId: 0,
  ownerName: '',
  createTime: '',
  updateTime: ''
})

// 监听props变化，更新表单数据
watch(
  () => props.customer,
  (newVal) => {
    Object.assign(customer, newVal)
  },
  { deep: true, immediate: true }
)

// 使用统一的表单验证规则
const rules = reactive(validationRules.customer)

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

// 保存客户
const handleSave = () => {
  formRef.value.validate((valid: boolean) => {
    if (valid) {
      emit('save', customer as Customer)
      resetForm()
    }
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(customer, {
    id: undefined,
    customerNo: '',
    customerName: '',
    customerType: 'enterprise',
    industry: '',
    scale: 'medium',
    level: 'B',
    status: 'potential',
    source: '',
    tags: [],
    region: '',
    address: '',
    website: '',
    ownerId: 0,
    ownerName: '',
    createTime: '',
    updateTime: ''
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
