<template>
  <div class="purchase-request-view">
    <div class="content-header">
      <h3>采购申请管理</h3>
      <div class="action-buttons">
        <el-button type="primary" @click="handleCreate">新增采购申请</el-button>
      </div>
    </div>

    <!-- AI 交期风险预测（S02，采购交期延期预警，空态自动隐藏） -->
    <ModuleAiSuggestionCard
      suggestion-type="DELIVERY_RISK"
      title="AI 交期风险预测"
      :max-display="5"
    />

    <!-- 搜索区域 -->
    <div class="search-area">
      <el-card shadow="never">
        <el-form :model="searchForm" layout="inline" size="small">
          <el-form-item label="采购申请编号">
            <el-input v-model="searchForm.requestCode" placeholder="请输入采购申请编号" />
          </el-form-item>
          <el-form-item label="申请人">
            <el-input v-model="searchForm.applicant" placeholder="请输入申请人" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态">
              <el-option label="全部" value="" />
              <el-option label="待审批" value="PENDING" />
              <el-option label="已审批" value="APPROVED" />
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
        <el-table v-loading="loading" :data="purchaseRequests" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="requestCode" label="采购申请编号" width="180" />
          <el-table-column prop="applicant" label="申请人" width="120" />
          <el-table-column prop="department" label="申请部门" width="120" />
          <el-table-column prop="applyDate" label="申请日期" width="180" />
          <el-table-column prop="expectedAmount" label="预计金额" width="120" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="handleView(scope.row)">查看</el-button>
              <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button
                v-if="scope.row.status === 'PENDING'"
                size="small"
                type="warning"
                @click="handleSubmitApproval(scope.row)"
              >提交审批</el-button>
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
    
    <!-- 采购申请表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="60%"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="采购申请编号" prop="requestCode">
          <el-input v-model="formData.requestCode" placeholder="请输入采购申请编号" />
        </el-form-item>
        <el-form-item label="申请人" prop="applicant">
          <el-input v-model="formData.applicant" placeholder="请输入申请人" />
        </el-form-item>
        <el-form-item label="申请部门" prop="department">
          <el-input v-model="formData.department" placeholder="请输入申请部门" />
        </el-form-item>
        <el-form-item label="预计金额" prop="expectedAmount">
          <el-input v-model.number="formData.expectedAmount" placeholder="请输入预计金额" />
        </el-form-item>
        <el-form-item label="预计交付日期" prop="expectedDeliveryDate">
          <el-date-picker
            v-model="formData.expectedDeliveryDate"
            type="datetime"
            placeholder="请选择预计交付日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="采购类型" prop="purchaseType">
          <el-select v-model="formData.purchaseType" placeholder="请选择采购类型">
            <el-option label="办公用品" value="OFFICE_SUPPLIES" />
            <el-option label="生产原料" value="PRODUCTION_MATERIALS" />
            <el-option label="设备采购" value="EQUIPMENT_PURCHASE" />
            <el-option label="服务采购" value="SERVICE_PURCHASE" />
          </el-select>
        </el-form-item>
        <el-form-item label="需求描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入需求描述"
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
import { procurementApi } from '../../../../api/srm'
import ModuleAiSuggestionCard from '@/components/ai/ModuleAiSuggestionCard.vue'

// 活跃标签
const activeTab = ref<string>('purchase-request')

// 搜索表单
const searchForm = reactive({
  requestCode: '',
  applicant: '',
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

// 采购申请列表
const purchaseRequests = ref<any[]>([])

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增采购申请')
const formRef = ref()
const formData = reactive({
  id: undefined,
  requestCode: '',
  applicant: '',
  department: '',
  applyDate: new Date(),
  expectedAmount: 0,
  expectedDeliveryDate: new Date(),
  purchaseType: '',
  description: ''
})

// 表单验证规则
const rules = reactive({
  requestCode: [{ required: true, message: '请输入采购申请编号', trigger: 'blur' }],
  applicant: [{ required: true, message: '请输入申请人', trigger: 'blur' }],
  department: [{ required: true, message: '请输入申请部门', trigger: 'blur' }],
  expectedAmount: [{ required: true, message: '请输入预计金额', trigger: 'blur' }],
  purchaseType: [{ required: true, message: '请选择采购类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入需求描述', trigger: 'blur' }]
})

// 页面加载时获取数据
onMounted(() => {
  fetchPurchaseRequests()
})

// 获取采购申请列表
const fetchPurchaseRequests = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      requestCode: searchForm.requestCode,
      applicant: searchForm.applicant,
      status: searchForm.status
    }
    const res = await procurementApi.getPurchaseRequestList(params)
    purchaseRequests.value = (res.data.list || res.data.records || [])
    pagination.total = (res.data.total || 0)
  } catch (error) {
    console.error(error)
    ElMessage.error('获取采购申请列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchPurchaseRequests()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    requestCode: '',
    applicant: '',
    status: ''
  })
  pagination.currentPage = 1
  fetchPurchaseRequests()
}

// 新增
const handleCreate = () => {
  dialogTitle.value = '新增采购申请'
  Object.assign(formData, {
    id: undefined,
    requestCode: '',
    applicant: '',
    department: '',
    applyDate: new Date(),
    expectedAmount: 0,
    expectedDeliveryDate: new Date(),
    purchaseType: '',
    description: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑采购申请'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 查看
const handleView = (row: any) => {
  dialogTitle.value = '查看采购申请'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 提交OA审批
const handleSubmitApproval = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要提交该采购申请至OA审批吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await procurementApi.submitPurchaseRequestApproval(row.id)
    ElMessage.success('审批提交成功')
    fetchPurchaseRequests()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(error?.message || '审批提交失败')
    }
  }
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该采购申请吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await procurementApi.deletePurchaseRequest(row.id)
      ElMessage.success('删除成功')
      fetchPurchaseRequests()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    if (formData.id) {
      await procurementApi.updatePurchaseRequest(formData.id, formData)
    } else {
      await procurementApi.createPurchaseRequest(formData)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchPurchaseRequests()
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败')
  }
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchPurchaseRequests()
}

// 当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchPurchaseRequests()
}
</script>

<style scoped>
.purchase-request-view {
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