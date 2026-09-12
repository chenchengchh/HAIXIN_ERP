<template>
  <div class="quality-objection-view">
    <div class="content-header">
      <h3>质量异议反馈</h3>
      <div class="action-buttons">
        <el-button type="primary" @click="handleCreate">新增质量异议</el-button>
        <el-button @click="handleRefresh">刷新数据</el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-card shadow="never">
        <el-form :model="searchForm" layout="inline" size="small">
          <el-form-item label="异议单号">
            <el-input v-model="searchForm.objectionNo" placeholder="请输入异议单号" />
          </el-form-item>
          <el-form-item label="供应商名称">
            <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" />
          </el-form-item>
          <el-form-item label="订单编号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" />
          </el-form-item>
          <el-form-item label="异议状态">
            <el-select v-model="searchForm.status" placeholder="请选择异议状态">
              <el-option label="全部" value="" />
              <el-option label="待处理" value="PENDING" />
              <el-option label="处理中" value="PROCESSING" />
              <el-option label="已解决" value="RESOLVED" />
              <el-option label="已关闭" value="CLOSED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    
    <!-- 质量异议列表 -->
    <div class="table-area">
      <el-card shadow="never">
        <el-table v-loading="loading" :data="objectionList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="objectionNo" label="异议单号" width="180" />
          <el-table-column prop="supplierName" label="供应商名称" width="220" />
          <el-table-column prop="orderNo" label="订单编号" width="180" />
          <el-table-column prop="objectionDate" label="异议日期" width="180" />
          <el-table-column prop="materialName" label="异议物料" width="200" />
          <el-table-column prop="objectionType" label="异议类型" width="120" />
          <el-table-column prop="status" label="异议状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="handleView(scope.row)">查看</el-button>
              <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="success" @click="handleProcess(scope.row)" :disabled="scope.row.status === 'RESOLVED' || scope.row.status === 'CLOSED'">
                处理
              </el-button>
              <el-button size="small" type="danger" @click="handleClose(scope.row)" :disabled="scope.row.status === 'CLOSED'">
                关闭
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
    
    <!-- 质量异议表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="70%"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="异议单号" prop="objectionNo">
          <el-input v-model="formData.objectionNo" placeholder="请输入异议单号" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="formData.supplierId" placeholder="请选择供应商" filterable>
            <el-option v-for="s in supplierOptions" :key="s.id" :label="s.supplierName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联订单" prop="orderId">
          <el-select v-model="formData.orderId" placeholder="请选择关联订单" filterable>
            <el-option v-for="o in orderOptions" :key="o.id" :label="o.orderNo" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="异议日期" prop="objectionDate">
          <el-date-picker
            v-model="formData.objectionDate"
            type="datetime"
            placeholder="请选择异议日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="异议物料" prop="materialName">
          <el-input v-model="formData.materialName" placeholder="请输入异议物料" />
        </el-form-item>
        <el-form-item label="异议类型" prop="objectionType">
          <el-select v-model="formData.objectionType" placeholder="请选择异议类型">
            <el-option label="外观缺陷" value="APPEARANCE_DEFECT" />
            <el-option label="性能不符" value="PERFORMANCE_ISSUE" />
            <el-option label="数量不符" value="QUANTITY_ISSUE" />
            <el-option label="包装损坏" value="PACKAGING_DAMAGE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="异议描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入异议描述"
          />
        </el-form-item>
        <el-form-item label="异议数量" prop="quantity">
          <el-input-number v-model="formData.quantity" :min="0" :step="1" placeholder="请输入异议数量" />
        </el-form-item>
        <el-form-item label="损失金额" prop="lossAmount">
          <el-input v-model.number="formData.lossAmount" placeholder="请输入损失金额" />
        </el-form-item>
        <el-form-item label="处理结果" prop="processingResult">
          <el-input
            v-model="formData.processingResult"
            type="textarea"
            :rows="4"
            placeholder="请输入处理结果"
          />
        </el-form-item>
        <el-form-item label="异议状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择异议状态">
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { qualityObjectionApi, supplierApi, procurementApi } from '../../../api/srm'

// 搜索表单
const searchForm = reactive({
  objectionNo: '',
  supplierName: '',
  orderNo: '',
  status: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载状态
const loading = ref(false)

// 异议列表
const objectionList = ref<any[]>([])
const supplierOptions = ref<any[]>([])
const orderOptions = ref<any[]>([])

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增质量异议')
const formRef = ref()
const formData = reactive({
  id: undefined,
  objectionNo: '',
  supplierId: undefined as number | undefined,
  supplierName: '',
  orderId: undefined as number | undefined,
  orderNo: '',
  objectionDate: new Date(),
  materialId: undefined as number | undefined,
  materialName: '',
  objectionType: 'APPEARANCE_DEFECT',
  description: '',
  quantity: 0,
  lossAmount: 0,
  processingResult: '',
  status: 'PENDING',
  remark: ''
})

// 表单验证规则
const rules = reactive({
  objectionNo: [{ required: true, message: '请输入异议单号', trigger: 'blur' }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  orderId: [{ required: true, message: '请选择关联订单', trigger: 'change' }],
  objectionDate: [{ required: true, message: '请选择异议日期', trigger: 'change' }],
  materialName: [{ required: true, message: '请输入异议物料', trigger: 'blur' }],
  objectionType: [{ required: true, message: '请选择异议类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入异议描述', trigger: 'blur' }],
  status: [{ required: true, message: '请选择异议状态', trigger: 'change' }]
})

// 页面加载时获取数据
onMounted(() => {
  fetchOptions()
  fetchObjections()
})

const fetchOptions = async () => {
  try {
    const [supplierRes, orderRes] = await Promise.all([
      supplierApi.getSupplierList({ page: 0, size: 1000 }),
      procurementApi.getPurchaseOrderList({ page: 0, size: 1000 })
    ])
    supplierOptions.value = supplierRes.data.list || []
    orderOptions.value = orderRes.data.list || []
  } catch (e) {
    supplierOptions.value = []
    orderOptions.value = []
  }
}

const toIso = (value: any) => {
  if (!value) return value
  if (value instanceof Date) return value.toISOString()
  return value
}

// 获取质量异议列表
const fetchObjections = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      objectionNo: searchForm.objectionNo || undefined,
      supplierName: searchForm.supplierName || undefined,
      orderNo: searchForm.orderNo || undefined,
      status: searchForm.status || undefined
    }
    const res = await qualityObjectionApi.getObjectionList(params)
    const content = (res.data.list || res.data.records || [])
    objectionList.value = content.map((x: any) => ({
      ...x,
      createTime: x.createdTime || x.createTime || ''
    }))
    pagination.total = (res.data.total || 0)
  } catch (error) {
    ElMessage.error('获取质量异议列表失败')
    objectionList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 根据状态获取标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'PROCESSING':
      return 'primary'
    case 'RESOLVED':
      return 'success'
    case 'CLOSED':
      return 'danger'
    default:
      return 'info'
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchObjections()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    objectionNo: '',
    supplierName: '',
    orderNo: '',
    status: ''
  })
  pagination.currentPage = 1
  fetchObjections()
}

// 刷新数据
const handleRefresh = () => {
  fetchObjections()
  ElMessage.success('数据已刷新')
}

// 新增
const handleCreate = () => {
  dialogTitle.value = '新增质量异议'
  Object.assign(formData, {
    id: undefined,
    objectionNo: '',
    supplierId: undefined,
    supplierName: '',
    orderId: undefined,
    orderNo: '',
    objectionDate: new Date(),
    materialId: undefined,
    materialName: '',
    objectionType: 'APPEARANCE_DEFECT',
    description: '',
    quantity: 0,
    lossAmount: 0,
    processingResult: '',
    status: 'PENDING',
    remark: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑质量异议'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 查看
const handleView = (row: any) => {
  dialogTitle.value = '查看质量异议'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 处理
const handleProcess = (row: any) => {
  dialogTitle.value = '处理质量异议'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 关闭
const handleClose = (row: any) => {
  ElMessageBox.confirm('确定要关闭该质量异议吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await qualityObjectionApi.closeObjection(row.id)
      ElMessage.success('关闭成功')
      fetchObjections()
    } catch (e) {
      ElMessage.error('关闭失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    const supplier = supplierOptions.value.find((s: any) => s.id === formData.supplierId)
    const order = orderOptions.value.find((o: any) => o.id === formData.orderId)
    const payload: any = {
      objectionNo: formData.objectionNo,
      supplierId: formData.supplierId,
      supplierName: supplier?.supplierName || formData.supplierName,
      orderId: formData.orderId,
      orderNo: order?.orderNo || formData.orderNo,
      objectionDate: toIso(formData.objectionDate),
      materialId: formData.materialId,
      materialName: formData.materialName,
      objectionType: formData.objectionType,
      description: formData.description,
      quantity: formData.quantity,
      lossAmount: formData.lossAmount,
      processingResult: formData.processingResult,
      status: formData.status,
      remark: formData.remark
    }
    if (dialogTitle.value.startsWith('处理') && formData.id) {
      await qualityObjectionApi.processObjection(formData.id as any, {
        processingResult: payload.processingResult,
        status: payload.status
      })
    } else if (formData.id) {
      await qualityObjectionApi.updateObjection(formData.id as any, payload)
    } else {
      await qualityObjectionApi.createObjection(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchObjections()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchObjections()
}

// 当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchObjections()
}
</script>

<style scoped>
.quality-objection-view {
  padding: 16px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.content-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.search-area {
  margin-bottom: 16px;
}

.table-area {
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.dialog-footer {
  text-align: right;
}
</style>
