<template>
  <div class="order-detail">
    <h3>{{ isNewOrder ? '新增采购订单' : '采购订单详情' }}</h3>
    
    <!-- 订单基本信息 -->
    <el-card class="basic-info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
        </div>
      </template>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="订单编号">
              <el-input v-model="formData.orderNo" placeholder="系统自动生成" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商名称">
              <el-select v-model="formData.supplierId" placeholder="请选择供应商">
                <el-option
                  v-for="supplier in supplierOptions"
                  :key="supplier.id"
                  :label="supplier.name"
                  :value="supplier.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下单日期" required>
              <el-date-picker v-model="formData.purchaseDate" type="datetime" placeholder="请选择下单日期" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期望交货日期" required>
              <el-date-picker v-model="formData.expectedDeliveryDate" type="datetime" placeholder="请选择期望交货日期" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="formData.remark" type="textarea" rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
    
    <!-- 订单商品列表 -->
    <el-card class="items-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>订单商品</span>
          <el-button type="primary" size="small" @click="handleAddItem">添加商品</el-button>
        </div>
      </template>
      <el-table :data="formData.items" border style="width: 100%">
        <el-table-column prop="materialId" label="物料ID" width="120" />
        <el-table-column prop="materialName" label="物料名称" min-width="200" />
        <el-table-column prop="specification" label="规格" min-width="150" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="price" label="单价" width="100" />
        <el-table-column prop="total" label="金额" width="120" />
        <el-table-column prop="requiredDate" label="需求日期" width="150" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button type="danger" size="small" @click="handleDeleteItem(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 订单总计 -->
      <div class="total-row">
        <div class="total-label">订单总计：</div>
        <div class="total-amount">¥{{ calculateTotalAmount().toFixed(2) }}</div>
      </div>
    </el-card>
    
    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSave">保存草稿</el-button>
      <el-button type="success" @click="handleSubmit">提交</el-button>
    </div>
    
    <!-- 添加商品对话框 -->
    <el-dialog v-model="itemDialogVisible" title="添加商品" width="700px">
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="100px">
        <el-form-item label="物料ID" required>
          <el-input v-model="itemForm.materialId" placeholder="请输入物料ID" />
        </el-form-item>
        <el-form-item label="物料名称" required>
          <el-input v-model="itemForm.materialName" placeholder="请输入物料名称" />
        </el-form-item>
        <el-form-item label="规格" required>
          <el-input v-model="itemForm.specification" placeholder="请输入规格" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="数量" required>
              <el-input v-model="itemForm.quantity" type="number" placeholder="请输入数量" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单位" required>
              <el-input v-model="itemForm.unit" placeholder="请输入单位" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单价" required>
              <el-input v-model="itemForm.price" type="number" placeholder="请输入单价" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="需求日期" required>
          <el-date-picker v-model="itemForm.requiredDate" type="date" placeholder="请选择需求日期" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="itemDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveItem">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useOrderStore } from '@/stores/srm/order'
import { PurchaseOrderStatus } from '@/types/srm'
import type { PurchaseOrder, PurchaseOrderItem } from '@/types/srm'

// 路由实例
const router = useRouter()
const route = useRoute()

// 订单ID
const orderId = computed(() => route.params.id as string)

// 是否为新增订单
const isNewOrder = computed(() => orderId.value === 'new')

// 订单管理状态存储
const orderStore = useOrderStore()

// 表单验证规则
const formRules = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  purchaseDate: [{ required: true, message: '请选择下单日期', trigger: 'change' }],
  expectedDeliveryDate: [{ required: true, message: '请选择期望交货日期', trigger: 'change' }]
}

// 商品表单验证规则
const itemRules = {
  materialId: [{ required: true, message: '请输入物料ID', trigger: 'blur' }],
  materialName: [{ required: true, message: '请输入物料名称', trigger: 'blur' }],
  specification: [{ required: true, message: '请输入规格', trigger: 'blur' }],
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }
  ],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  price: [
    { required: true, message: '请输入单价', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '单价必须大于0', trigger: 'blur' }
  ],
  requiredDate: [{ required: true, message: '请选择需求日期', trigger: 'change' }]
}

// 表单引用
const formRef = ref()
const itemFormRef = ref()

// 供应商选项
const supplierOptions = ref<{ id: number; name: string }[]>([])

// 表单数据
const formData = reactive({
  id: undefined as number | undefined,
  orderNo: '',
  supplierId: undefined as number | undefined,
  supplierName: '',
  status: PurchaseOrderStatus.DRAFT,
  purchaseDate: new Date().toISOString().split('T')[0],
  expectedDeliveryDate: new Date().toISOString().split('T')[0],
  totalAmount: 0,
  items: [] as PurchaseOrderItem[],
  remark: ''
})

// 添加商品对话框
const itemDialogVisible = ref(false)
const itemForm = reactive({
  materialId: '',
  materialName: '',
  specification: '',
  quantity: 0,
  unit: '',
  price: 0,
  total: 0,
  requiredDate: new Date().toISOString().split('T')[0]
})

// 加载状态
const loading = ref(false)

/**
 * 计算订单总金额
 * @returns 订单总金额
 */
const calculateTotalAmount = () => {
  return formData.items.reduce((total, item) => total + (item.total || 0), 0)
}

/**
 * 获取供应商列表
 */
const getSupplierList = async () => {
  try {
    // 这里应该调用API获取合格供应商列表
    supplierOptions.value = [
      { id: 1, name: '供应商A' },
      { id: 2, name: '供应商B' },
      { id: 3, name: '供应商C' }
    ]
  } catch (error) {
    console.error('获取供应商列表失败:', error)
    ElMessage.error('获取供应商列表失败')
  }
}

/**
 * 获取订单详情
 */
const getOrderDetail = async () => {
  if (isNewOrder.value) return
  
  loading.value = true
  try {
    await orderStore.fetchPurchaseOrderDetail(parseInt(orderId.value))
    Object.assign(formData, { ...orderStore.purchaseOrderDetail })
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
    router.push('/home/srm/order/list')
  } finally {
    loading.value = false
  }
}

/**
 * 添加商品
 */
const handleAddItem = () => {
  Object.assign(itemForm, {
    materialId: '',
    materialName: '',
    specification: '',
    quantity: 0,
    unit: '',
    price: 0,
    total: 0,
    requiredDate: new Date().toISOString().split('T')[0]
  })
  itemDialogVisible.value = true
}

/**
 * 保存商品
 */
const handleSaveItem = async () => {
  try {
    // 表单验证
    if (itemFormRef.value) {
      await itemFormRef.value.validate()
    }
    
    // 计算商品金额
    itemForm.total = itemForm.quantity * itemForm.price
    formData.items.push({ 
      ...itemForm,
      // 确保requiredDate是字符串类型
      requiredDate: (typeof itemForm.requiredDate === 'string' ? itemForm.requiredDate : new Date().toISOString().split('T')[0]) as string
    })
    itemDialogVisible.value = false
    ElMessage.success('商品添加成功')
  } catch (error) {
    console.error('保存商品失败:', error)
  }
}

/**
 * 删除商品
 * @param index 商品索引
 */
const handleDeleteItem = (index: number) => {
  formData.items.splice(index, 1)
  ElMessage.success('商品删除成功')
}

/**
 * 保存草稿
 */
const handleSave = async () => {
  try {
    // 表单验证
    if (formRef.value) {
      await formRef.value.validate()
    }
    
    if (formData.items.length === 0) {
      ElMessage.error('请至少添加一个商品')
      return
    }
    
    formData.totalAmount = calculateTotalAmount()
    
    loading.value = true
    if (isNewOrder.value) {
      await orderStore.createPurchaseOrder({
        ...formData,
        status: PurchaseOrderStatus.DRAFT
      } as any)
    } else {
      await orderStore.updatePurchaseOrder(formData.id!, {
        ...formData,
        status: PurchaseOrderStatus.DRAFT
      } as any)
    }
    
    ElMessage.success('保存草稿成功')
    router.push('/home/srm/order/list')
  } catch (error) {
    console.error('保存订单失败:', error)
    ElMessage.error('保存订单失败')
  } finally {
    loading.value = false
  }
}

/**
 * 提交订单
 */
const handleSubmit = async () => {
  try {
    // 表单验证
    if (formRef.value) {
      await formRef.value.validate()
    }
    
    if (formData.items.length === 0) {
      ElMessage.error('请至少添加一个商品')
      return
    }
    
    formData.totalAmount = calculateTotalAmount()
    
    await ElMessageBox.confirm('确定要提交该订单吗？', '确认提交', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    if (isNewOrder.value) {
      await orderStore.createPurchaseOrder({
        ...formData,
        status: PurchaseOrderStatus.OPEN
      } as any)
    } else {
      await orderStore.updatePurchaseOrder(formData.id!, {
        ...formData,
        status: PurchaseOrderStatus.OPEN
      } as any)
    }
    
    ElMessage.success('订单提交成功')
    router.push('/home/srm/order/list')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('提交订单失败:', error)
      ElMessage.error('提交订单失败')
    }
  } finally {
    loading.value = false
  }
}

/**
 * 取消操作
 */
const handleCancel = () => {
  router.go(-1)
}

/**
 * 初始化数据
 */
const initData = async () => {
  await getSupplierList()
  
  if (!isNewOrder.value) {
    await getOrderDetail()
  } else {
    // 添加一个空的商品行
  formData.items.push({
    materialId: '',
    materialName: '',
    specification: '',
    quantity: 0,
    unit: '',
    price: 0,
    total: 0,
    requiredDate: new Date().toISOString().split('T')[0] as string
  })
  }
}

// 组件挂载时获取数据
onMounted(() => {
  initData()
})
</script>

<style scoped>
.order-detail {
  padding: 20px;
}

.basic-info-card,
.items-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.total-row {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.total-label {
  margin-right: 10px;
}

.total-amount {
  color: #ff4d4f;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>