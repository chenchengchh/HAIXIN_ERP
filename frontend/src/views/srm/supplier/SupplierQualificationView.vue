<template>
  <div class="supplier-qualification-view">
    <SubModuleHeader title="供应商资质管理" parentTitle="供应商准入管理" parentPath="/home/srm/supplier" />
    <div class="content-header">
      <div class="action-buttons">
        <el-button type="primary" @click="handleCreate">新增资质</el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-card shadow="never">
        <el-form :model="searchForm" layout="inline" size="small">
          <el-form-item label="供应商名称">
            <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" />
          </el-form-item>
          <el-form-item label="资质类型">
            <el-select v-model="searchForm.qualificationType" placeholder="请选择资质类型">
              <el-option label="全部" value="" />
              <el-option label="营业执照" value="BUSINESS_LICENSE" />
              <el-option label="生产许可证" value="PRODUCTION_LICENSE" />
              <el-option label="行业认证" value="INDUSTRY_CERTIFICATION" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item label="资质状态">
            <el-select v-model="searchForm.status" placeholder="请选择资质状态">
              <el-option label="全部" value="" />
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
              <el-option label="已过期" value="EXPIRED" />
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
        <el-table v-loading="loading" :data="qualifications" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="supplierName" label="供应商名称" width="180" />
          <el-table-column prop="qualificationType" label="资质类型" width="150" />
          <el-table-column prop="certificateName" label="资质名称" width="180" />
          <el-table-column prop="certificateNo" label="证书编号" width="200" />
          <el-table-column prop="issueDate" label="发证日期" width="150" />
          <el-table-column prop="expiryDate" label="到期日期" width="150" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag 
                :type="getStatusType(scope.row.status)"
              >
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="300" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="handleView(scope.row)">查看</el-button>
              <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="warning" @click="handleVerify(scope.row)">审核</el-button>
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
    
    <!-- 资质表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="70%"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="formData.supplierId" placeholder="请选择供应商">
            <el-option v-for="s in supplierOptions" :key="s.id" :label="s.supplierName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="资质类型" prop="qualificationType">
          <el-select v-model="formData.qualificationType" placeholder="请选择资质类型">
            <el-option label="营业执照" value="BUSINESS_LICENSE" />
            <el-option label="生产许可证" value="PRODUCTION_LICENSE" />
            <el-option label="行业认证" value="INDUSTRY_CERTIFICATION" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="资质名称" prop="certificateName">
          <el-input v-model="formData.certificateName" placeholder="请输入资质名称" />
        </el-form-item>
        <el-form-item label="证书编号" prop="certificateNo">
          <el-input v-model="formData.certificateNo" placeholder="请输入证书编号" />
        </el-form-item>
        <el-form-item label="发证日期" prop="issueDate">
          <el-date-picker
            v-model="formData.issueDate"
            type="date"
            placeholder="请选择发证日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="到期日期" prop="expiryDate">
          <el-date-picker
            v-model="formData.expiryDate"
            type="date"
            placeholder="请选择到期日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="资质状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择资质状态">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已过期" value="EXPIRED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="资质审核"
      width="40%"
      :close-on-click-modal="false"
    >
      <el-form ref="auditFormRef" :model="auditForm" label-width="80px">
        <el-form-item label="审核结果" prop="status" :rules="[{ required: true, message: '请选择审核结果', trigger: 'change' }]">
          <el-radio-group v-model="auditForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item 
          label="审核意见" 
          prop="auditOpinion"
          :rules="[{ required: true, message: '请输入审核意见', trigger: 'blur' }]"
        >
          <el-input 
            v-model="auditForm.auditOpinion" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAudit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SubModuleHeader from '../../../components/common/SubModuleHeader.vue'
import { supplierApi } from '../../../api/srm'

// 搜索表单
const searchForm = reactive({
  supplierName: '',
  qualificationType: '',
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

// 资质列表
const qualifications = ref<any[]>([])

const supplierOptions = ref<any[]>([])
const supplierNameById = ref<Record<number, string>>({})

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增资质')
const formRef = ref()
const formData = reactive({
  id: undefined,
  supplierId: undefined as number | undefined,
  qualificationType: 'BUSINESS_LICENSE',
  certificateName: '',
  certificateNo: '',
  issueDate: '',
  expiryDate: '',
  status: 'PENDING',
  attachmentUrl: ''
})

// 审核相关
const auditDialogVisible = ref(false)
const auditFormRef = ref()
const auditForm = reactive({
  qualificationId: 0,
  status: 'APPROVED',
  auditOpinion: ''
})

// 表单验证规则
const rules = reactive({
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  qualificationType: [{ required: true, message: '请选择资质类型', trigger: 'change' }],
  certificateName: [{ required: true, message: '请输入资质名称', trigger: 'blur' }],
  certificateNo: [{ required: true, message: '请输入证书编号', trigger: 'blur' }],
  issueDate: [{ required: true, message: '请选择发证日期', trigger: 'change' }],
  expiryDate: [{ required: true, message: '请选择到期日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择资质状态', trigger: 'change' }]
})

// 页面加载时获取数据
onMounted(() => {
  fetchSuppliers()
  fetchQualifications()
})

const fetchSuppliers = async () => {
  try {
    const res = await supplierApi.getSupplierList({ page: 0, size: 1000 })
    supplierOptions.value = res.data.list || []
    supplierNameById.value = (res.data.list || []).reduce((acc: any, s: any) => {
      acc[s.id] = s.supplierName
      return acc
    }, {})
  } catch (e) {
    supplierOptions.value = []
    supplierNameById.value = {}
  }
}

// 获取资质列表
const fetchQualifications = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize
    }
    const res = await supplierApi.getQualificationList(params)
    const content = (res.data.list || res.data.records || [])
    qualifications.value = content.map((q: any) => ({
      ...q,
      supplierName: supplierNameById.value[q.supplierId] || ''
    }))
    pagination.total = (res.data.total || 0)
  } catch (error) {
    ElMessage.error('获取资质列表失败')
    qualifications.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 根据状态获取标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'APPROVED':
      return 'success'
    case 'PENDING':
      return 'warning'
    case 'REJECTED':
    case 'EXPIRED':
      return 'danger'
    default:
      return 'info'
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchQualifications()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    supplierName: '',
    qualificationType: '',
    status: ''
  })
  pagination.currentPage = 1
  fetchQualifications()
}

// 新增
const handleCreate = () => {
  dialogTitle.value = '新增资质'
  Object.assign(formData, {
    id: undefined,
    supplierId: undefined,
    qualificationType: 'BUSINESS_LICENSE',
    certificateName: '',
    certificateNo: '',
    issueDate: '',
    expiryDate: '',
    status: 'PENDING',
    attachmentUrl: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑资质'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 查看
const handleView = (row: any) => {
  dialogTitle.value = '查看资质'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 审核
const handleVerify = (row: any) => {
  auditForm.qualificationId = row.id
  auditForm.status = 'APPROVED'
  auditForm.auditOpinion = ''
  auditDialogVisible.value = true
}

const confirmAudit = async () => {
  if (!auditFormRef.value) return
  await auditFormRef.value.validate()
  
  try {
    await supplierApi.updateQualification(auditForm.qualificationId, { status: auditForm.status })
    ElMessage.success('审核成功')
    auditDialogVisible.value = false
    fetchQualifications()
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

const toYmd = (value: any) => {
  if (!value) return value
  if (value instanceof Date) return value.toISOString().slice(0, 10)
  if (typeof value === 'string') return value.slice(0, 10)
  return value
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    const payload: any = {
      supplierId: formData.supplierId,
      qualificationType: formData.qualificationType,
      certificateName: formData.certificateName,
      certificateNo: formData.certificateNo,
      issueDate: toYmd(formData.issueDate),
      expiryDate: toYmd(formData.expiryDate),
      status: formData.status,
      attachmentUrl: formData.attachmentUrl
    }
    if (formData.id) {
      await supplierApi.updateQualification(formData.id as any, payload)
    } else {
      await supplierApi.createQualification(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchQualifications()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchQualifications()
}

// 当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchQualifications()
}
</script>

<style scoped>
.supplier-qualification-view {
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
