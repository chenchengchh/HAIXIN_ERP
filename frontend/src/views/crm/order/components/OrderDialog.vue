<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    label-width="120px"
    style="max-height: 60vh; overflow-y: auto;"
  >
    <el-form-item label="客户名称" prop="customerName" required>
      <el-input v-model="formData.customerName" placeholder="请输入客户名称" />
    </el-form-item>

    <el-form-item label="关联商机" prop="opportunityId">
      <el-select v-model="formData.opportunityId" placeholder="选择关联商机" clearable>
        <el-option label="商机1" value="1" />
        <el-option label="商机2" value="2" />
      </el-select>
    </el-form-item>

    <el-form-item label="订单日期" prop="orderDate" required>
      <el-date-picker
        v-model="formData.orderDate"
        type="date"
        placeholder="选择订单日期"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        style="width: 100%;"
      />
    </el-form-item>

    <el-form-item label="交付日期" prop="deliveryDate" required>
      <el-date-picker
        v-model="formData.deliveryDate"
        type="date"
        placeholder="选择交付日期"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        style="width: 100%;"
      />
    </el-form-item>

    <el-form-item label="付款条款" prop="paymentTerms">
      <el-input v-model="formData.paymentTerms" placeholder="请输入付款条款" />
    </el-form-item>

    <el-form-item label="交付地址" prop="deliveryAddress">
      <el-input v-model="formData.deliveryAddress" placeholder="请输入交付地址" />
    </el-form-item>

    <el-form-item label="备注" prop="remark">
      <el-input
        v-model="formData.remark"
        type="textarea"
        placeholder="请输入备注信息"
        :rows="3"
      />
    </el-form-item>

    <!-- 订单明细 -->
    <el-divider content-position="left">订单明细</el-divider>
    <el-table :data="formData.items" style="width: 100%; margin-bottom: 20px;">
      <el-table-column prop="productCode" label="产品编码" min-width="120">
        <template #default="scope">
          <el-input v-model="scope.row.productCode" placeholder="编码" />
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="产品名称" min-width="150">
        <template #default="scope">
          <el-input v-model="scope.row.productName" placeholder="名称" />
        </template>
      </el-table-column>
      <el-table-column prop="specification" label="规格" min-width="100">
        <template #default="scope">
          <el-input v-model="scope.row.specification" placeholder="规格" />
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="100">
        <template #default="scope">
          <el-input-number v-model="scope.row.quantity" :min="1" style="width: 100%" />
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="80">
        <template #default="scope">
          <el-input v-model="scope.row.unit" placeholder="单位" />
        </template>
      </el-table-column>
      <el-table-column prop="unitPrice" label="单价" width="120" align="right">
        <template #default="scope">
          <el-input-number v-model="scope.row.unitPrice" :min="0" :precision="2" style="width: 100%" />
        </template>
      </el-table-column>
      <el-table-column prop="subtotal" label="金额" width="100" align="right">
        <template #default="scope">
          {{ (scope.row.quantity * scope.row.unitPrice).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template #default="scope">
          <el-button size="small" type="danger" link @click="removeOrderItem(scope.$index)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-button type="primary" @click="addOrderItem" size="small">
      <el-icon><Plus /></el-icon> 添加订单明细
    </el-button>

    <!-- 订单金额汇总 -->
    <el-divider content-position="left">订单金额汇总</el-divider>
    <div class="order-amount-summary">
      <div class="amount-item">
        <span class="label">订单总额：</span>
        <span class="value">{{ formData.totalAmount }}</span>
      </div>
      <div class="amount-item">
        <span class="label">折扣金额：</span>
        <span class="value">{{ formData.discountAmount }}</span>
      </div>
      <div class="amount-item total">
        <span class="label">最终金额：</span>
        <span class="value">{{ formData.finalAmount }}</span>
      </div>
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// 定义组件属性
const props = defineProps<{
  modelValue: any
  dialogType: 'create' | 'edit'
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'update:modelValue', value: any): void
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const formData = ref<any>({
  ...props.modelValue,
  items: props.modelValue.items || []
})

// 监听props变化，更新表单数据
watch(
  () => props.modelValue,
  (newValue) => {
    formData.value = {
      ...newValue,
      items: newValue.items || []
    }
  },
  { deep: true }
)

// 监听formData变化，同步到父组件
watch(formData, (val) => {
  emit('update:modelValue', val)
}, { deep: true })

// 表单规则
const formRules = reactive<FormRules>({
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  orderDate: [{ required: true, message: '请选择订单日期', trigger: 'change' }],
  deliveryDate: [{ required: true, message: '请选择交付日期', trigger: 'change' }]
})

// 添加订单明细
const addOrderItem = () => {
  formData.value.items.push({
    productCode: '',
    productName: '',
    specification: '',
    quantity: 1,
    unit: '',
    unitPrice: 0,
    subtotal: 0,
    discountRate: 0
  })
}

// 删除订单明细
const removeOrderItem = (index: number) => {
  formData.value.items.splice(index, 1)
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    // 计算金额
    calculateOrderAmount()
    // 发出确认事件
    emit('update:modelValue', formData.value)
    emit('confirm')
  } catch (error) {
    ElMessage.error('表单验证失败，请检查输入内容')
  }
}

// 计算订单金额
const calculateOrderAmount = () => {
  // 计算订单总额
  const totalAmount = formData.value.items.reduce((sum: number, item: any) => {
    return sum + (item.quantity * item.unitPrice || 0)
  }, 0)
  
  // 计算最终金额
  const finalAmount = totalAmount - (formData.value.discountAmount || 0)
  
  // 更新表单数据
  formData.value.totalAmount = totalAmount
  formData.value.finalAmount = finalAmount
}

// 监听订单明细变化，自动计算金额
watch(
  () => formData.value.items,
  () => {
    calculateOrderAmount()
  },
  { deep: true }
)

// 监听折扣金额变化，自动计算最终金额
watch(
  () => formData.value.discountAmount,
  () => {
    calculateOrderAmount()
  }
)

// 组件挂载时初始化
onMounted(() => {
  // 如果是新建订单，添加一条默认订单明细
  if (props.dialogType === 'create' && formData.value.items.length === 0) {
    addOrderItem()
  }
  // 计算初始金额
  calculateOrderAmount()
})
</script>

<style scoped>
.order-amount-summary {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-top: 20px;
}

.amount-item {
  margin-bottom: 10px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.amount-item:last-child {
  margin-bottom: 0;
}

.amount-item .label {
  margin-right: 10px;
  font-weight: 500;
}

.amount-item .value {
  font-weight: 600;
  color: #303133;
}

.amount-item.total {
  font-size: 16px;
}

.amount-item.total .value {
  color: #409eff;
  font-size: 18px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>