<template>
  <div class="quotation-view">
    <div class="content-header">
      <h3>报价管理</h3>
      <div class="action-buttons">
        <el-button type="primary" @click="handleCreate">新增报价单</el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-card shadow="never">
        <el-form :model="searchForm" layout="inline" size="small">
          <el-form-item label="报价单号">
            <el-input v-model="searchForm.quotationCode" placeholder="请输入报价单号" />
          </el-form-item>
          <el-form-item label="关联询价单">
            <el-input v-model="searchForm.inquiryCode" placeholder="请输入关联询价单号" />
          </el-form-item>
          <el-form-item label="供应商">
            <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态">
              <el-option label="全部" value="" />
              <el-option label="待提交" value="DRAFT" />
              <el-option label="已提交" value="SUBMITTED" />
              <el-option label="已审核" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    
    <!-- 表格区域 -->
    <div class="table-area">
      <el-card shadow="never">
        <el-table v-loading="loading" :data="quotations" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="quotationCode" label="报价单号" width="180" />
          <el-table-column prop="inquiryCode" label="关联询价单" width="180" />
          <el-table-column prop="supplierName" label="供应商" width="180" />
          <el-table-column prop="totalAmount" label="总金额" width="120" />
          <el-table-column prop="deliveryPeriod" label="交付周期" width="120" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column prop="submitDate" label="提交日期" width="180" />
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="handleView(scope.row)">查看</el-button>
              <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="success" @click="handleApprove(scope.row)">审核</el-button>
              <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
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
    
    <!-- 报价单表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="70%"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="报价单号" prop="quotationCode">
          <el-input v-model="formData.quotationCode" placeholder="请输入报价单号" />
        </el-form-item>
        <el-form-item label="关联询价单" prop="inquiryCode">
          <el-input v-model="formData.inquiryCode" placeholder="请输入关联询价单号" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierName">
          <el-input v-model="formData.supplierName" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="总金额" prop="totalAmount">
          <el-input v-model.number="formData.totalAmount" placeholder="请输入总金额" />
        </el-form-item>
        <el-form-item label="交付周期" prop="deliveryPeriod">
          <el-input v-model="formData.deliveryPeriod" placeholder="请输入交付周期" />
        </el-form-item>
        <el-form-item label="付款条件" prop="paymentTerms">
          <el-input v-model="formData.paymentTerms" placeholder="请输入付款条件" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态">
            <el-option label="待提交" value="DRAFT" />
            <el-option label="已提交" value="SUBMITTED" />
            <el-option label="已审核" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
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
import { quotationApi } from '../../../../api/srm'

// 活跃标签
const activeTab = ref<string>('quotation')

// 搜索表单
const searchForm = reactive({
  quotationCode: '',
  inquiryCode: '',
  supplierName: '',
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

// 报价列表
const quotations = ref<any[]>([])

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增报价单')
const formRef = ref()
const formData = reactive({
  id: undefined,
  quotationCode: '',
  inquiryCode: '',
  supplierName: '',
  totalAmount: 0,
  deliveryPeriod: '',
  paymentTerms: '',
  status: 'DRAFT',
  submitDate: new Date(),
  remark: ''
})

// 表单验证规则
const rules = reactive({
  quotationCode: [{ required: true, message: '请输入报价单号', trigger: 'blur' }],
  inquiryCode: [{ required: true, message: '请输入关联询价单号', trigger: 'blur' }],
  supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  totalAmount: [{ required: true, message: '请输入总金额', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})

// 页面加载时获取数据
onMounted(() => {
  fetchQuotations()
})

// 获取报价列表
const fetchQuotations = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      quotationCode: searchForm.quotationCode,
      inquiryCode: searchForm.inquiryCode,
      supplierName: searchForm.supplierName,
      status: searchForm.status
    }
    const res = await quotationApi.getQuotationList(params)
    quotations.value = (res.data.list || res.data.records || [])
    pagination.total = (res.data.total || 0)
  } catch (error) {
    console.error(error)
    ElMessage.error('获取报价列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchQuotations()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    quotationCode: '',
    inquiryCode: '',
    supplierName: '',
    status: ''
  })
  pagination.currentPage = 1
  fetchQuotations()
}

// 新增
const handleCreate = () => {
  dialogTitle.value = '新增报价单'
  Object.assign(formData, {
    id: undefined,
    quotationCode: '',
    inquiryCode: '',
    supplierName: '',
    totalAmount: 0,
    deliveryPeriod: '',
    paymentTerms: '',
    status: 'DRAFT',
    submitDate: new Date(),
    remark: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑报价单'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 查看
const handleView = (row: any) => {
  dialogTitle.value = '查看报价单'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该报价单吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await quotationApi.deleteQuotation(row.id)
      ElMessage.success('删除成功')
      fetchQuotations()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 审核
const handleApprove = (row: any) => {
  ElMessageBox.confirm('确定要审核该报价单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await quotationApi.acceptQuotation(row.id)
      ElMessage.success('审核成功')
      fetchQuotations()
    } catch (error) {
      ElMessage.error('审核失败')
    }
  }).catch(() => {
    // 取消审核
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    if (formData.id) {
      await quotationApi.updateQuotation(formData.id, formData)
    } else {
      await quotationApi.createQuotation(formData)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchQuotations()
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败')
  }
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchQuotations()
}

// 当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchQuotations()
}
</script>

<style scoped>
.quotation-view {
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
