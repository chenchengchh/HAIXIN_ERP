<template>
  <div class="supplier-list-view">
    <SubModuleHeader title="供应商列表" parentTitle="供应商准入管理" parentPath="/home/srm/supplier" />
    <div class="content-header">
      <div class="action-buttons">
        <el-button type="primary" @click="handleCreate">新增供应商</el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-card shadow="never">
        <el-form :model="searchForm" layout="inline" size="small">
          <el-form-item label="供应商编码">
            <el-input v-model="searchForm.supplierCode" placeholder="请输入供应商编码" />
          </el-form-item>
          <el-form-item label="供应商名称">
            <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" />
          </el-form-item>
          <el-form-item label="供应商等级">
            <el-select v-model="searchForm.supplierLevel" placeholder="请选择供应商等级">
              <el-option label="全部" value="" />
              <el-option label="A级" value="A" />
              <el-option label="B级" value="B" />
              <el-option label="C级" value="C" />
            </el-select>
          </el-form-item>
          <el-form-item label="资质状态">
            <el-select v-model="searchForm.qualificationStatus" placeholder="请选择资质状态">
              <el-option label="全部" value="" />
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item label="是否黑名单">
            <el-select v-model="searchForm.isBlacklisted" placeholder="请选择">
              <el-option label="全部" value="" />
              <el-option label="是" value="true" />
              <el-option label="否" value="false" />
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
        <el-table v-loading="loading" :data="suppliers" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="supplierCode" label="供应商编码" width="180" />
          <el-table-column prop="supplierName" label="供应商名称" width="220" />
          <el-table-column prop="contactPerson" label="联系人" width="120" />
          <el-table-column prop="contactPhone" label="联系电话" width="150" />
          <el-table-column prop="supplierLevel" label="供应商等级" width="100" />
          <el-table-column prop="isBlacklisted" label="是否黑名单" width="120">
            <template #default="scope">
              <el-tag :type="scope.row.isBlacklisted ? 'danger' : 'success'">
                {{ scope.row.isBlacklisted ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="440" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="handleView(scope.row)">查看</el-button>
              <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="warning" @click="handleQualification(scope.row)">资质管理</el-button>
              <el-button
                v-if="!scope.row.isBlacklisted && scope.row.type !== 'QUALIFIED'"
                size="small"
                type="success"
                @click="handlePromoteQualified(scope.row)"
              >
                晋升合格
              </el-button>
              <el-button
                v-if="!scope.row.isBlacklisted && scope.row.type === 'QUALIFIED'"
                size="small"
                type="info"
                @click="handleDemotePotential(scope.row)"
              >
                降级潜在
              </el-button>
              <el-button 
                size="small" 
                :type="scope.row.isBlacklisted ? 'success' : 'danger'" 
                @click="handleToggleBlacklist(scope.row)"
              >
                {{ scope.row.isBlacklisted ? '移出黑名单' : '加入黑名单' }}
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
    
    <!-- 供应商表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="70%"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="供应商编码" prop="supplierCode">
          <el-input v-model="formData.supplierCode" placeholder="请输入供应商编码" />
        </el-form-item>
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="formData.supplierName" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="供应商阶段" prop="type">
          <el-select v-model="formData.type" placeholder="请选择供应商阶段">
            <el-option label="潜在" value="POTENTIAL" />
            <el-option label="合格" value="QUALIFIED" />
            <el-option label="黑名单" value="BLACKLISTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="formData.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="联系邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入联系邮箱" />
        </el-form-item>
        <el-form-item label="联系地址" prop="address">
          <el-input
            v-model="formData.address"
            type="textarea"
            :rows="2"
            placeholder="请输入联系地址"
          />
        </el-form-item>
        <el-form-item label="供应商等级" prop="supplierLevel">
          <el-select v-model="formData.supplierLevel" placeholder="请选择供应商等级">
            <el-option label="A级" value="A" />
            <el-option label="B级" value="B" />
            <el-option label="C级" value="C" />
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

    <!-- 资质管理对话框 -->
    <el-dialog
      v-model="qualificationDialogVisible"
      title="资质管理"
      width="60%"
      :close-on-click-modal="false"
    >
      <div class="qualification-list">
        <div class="action-bar" style="margin-bottom: 16px;">
          <el-button type="primary" size="small" @click="handleAddQualification">新增资质</el-button>
        </div>
        <el-table :data="qualificationList" style="width: 100%" border>
          <el-table-column prop="name" label="资质名称" />
          <el-table-column prop="type" label="资质类型" width="120" />
          <el-table-column prop="expireDate" label="有效期至" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'APPROVED' ? 'success' : 'danger'">
                {{ scope.row.status === 'APPROVED' ? '有效' : '过期' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button type="danger" link size="small" @click="handleDeleteQualification(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="qualificationDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 加入黑名单原因对话框 -->
    <el-dialog
      v-model="blacklistDialogVisible"
      title="加入黑名单"
      width="40%"
      :close-on-click-modal="false"
    >
      <el-form ref="blacklistFormRef" :model="blacklistForm" label-width="80px">
        <el-form-item 
          label="原因" 
          prop="reason"
          :rules="[{ required: true, message: '请输入加入黑名单原因', trigger: 'blur' }]"
        >
          <el-input 
            v-model="blacklistForm.reason" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入详细原因，如：多次交货延迟、质量不达标等"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="blacklistDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmBlacklist">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SubModuleHeader from '../../../components/common/SubModuleHeader.vue'
import { useVoiceContext } from '../../../composables/useVoiceContext'
import { supplierApi } from '../../../api/srm'

useVoiceContext({
  id: 'supplier-list',
  description: '供应商列表',
  actions: {
    'search': () => handleSearch(),
    'reset': () => handleReset(),
    'create': () => handleCreate(),
    'refresh': () => fetchSuppliers()
  }
})

// 搜索表单
const searchForm = reactive({
  supplierCode: '',
  supplierName: '',
  supplierLevel: '',
  qualificationStatus: '',
  isBlacklisted: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载状态
const loading = ref(false)

// 供应商列表
const suppliers = ref<any[]>([])

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增供应商')
const formRef = ref()
const formData = reactive({
  id: undefined,
  supplierCode: '',
  supplierName: '',
  type: 'POTENTIAL',
  contactPerson: '',
  contactPhone: '',
  email: '',
  address: '',
  supplierLevel: 'B'
})

// 表单验证规则
const rules = reactive({
  supplierCode: [{ required: true, message: '请输入供应商编码', trigger: 'blur' }],
  supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  type: [{ required: true, message: '请选择供应商阶段', trigger: 'change' }],
  supplierLevel: [{ required: true, message: '请选择供应商等级', trigger: 'change' }]
})

// 页面加载时获取数据
onMounted(() => {
  fetchSuppliers()
})

// 获取供应商列表
const fetchSuppliers = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize
    }
    const res = await supplierApi.getSupplierList(params)
    suppliers.value = (res.data.list || []).map((s: any) => {
      const blacklisted = s.type === 'BLACKLISTED' || s.status === 'BLACKLISTED' || s.status === 'BLACKLIST'
      return {
        id: s.id,
        supplierCode: s.supplierCode,
        supplierName: s.supplierName,
        contactPerson: s.contactPerson,
        contactPhone: s.contactPhone,
        email: s.email,
        address: s.address,
        supplierLevel: s.category || '',
        isBlacklisted: blacklisted,
        type: s.type,
        createTime: s.createdTime || s.createTime || ''
      }
    })
    pagination.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取供应商列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchSuppliers()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    supplierCode: '',
    supplierName: '',
    supplierLevel: '',
    qualificationStatus: '',
    isBlacklisted: ''
  })
  pagination.currentPage = 1
  fetchSuppliers()
}

// 新增
const handleCreate = () => {
  dialogTitle.value = '新增供应商'
  Object.assign(formData, {
    id: undefined,
    supplierCode: '',
    supplierName: '',
    type: 'POTENTIAL',
    contactPerson: '',
    contactPhone: '',
    email: '',
    address: '',
    supplierLevel: 'B'
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑供应商'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 查看
const handleView = (row: any) => {
  dialogTitle.value = '查看供应商'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 资质管理相关
const qualificationDialogVisible = ref(false)
const qualificationList = ref<any[]>([])
const currentSupplierId = ref<number | undefined>(undefined)

// 黑名单管理相关
const blacklistDialogVisible = ref(false)
const blacklistFormRef = ref()
const blacklistForm = reactive({
  reason: ''
})
const blacklistTargetRow = ref<any>(null)

// 资质管理
const handleQualification = async (row: any) => {
  currentSupplierId.value = row.id
  qualificationDialogVisible.value = true
  try {
    const res = await supplierApi.getSupplierQualifications(row.id)
    qualificationList.value = (res.data || []).map((q: any) => ({
      id: q.id,
      name: q.certificateName || '',
      type: q.qualificationType || '',
      expireDate: q.expiryDate || '',
      status: q.status || ''
    }))
  } catch (error) {
    qualificationList.value = []
    ElMessage.error('获取资质列表失败')
  }
}

const handleAddQualification = () => {
  ElMessage.info('上传资质功能待实现')
}

const handleDeleteQualification = (index: number) => {
  ElMessageBox.confirm('确定删除该资质吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    const item = qualificationList.value[index]
    if (!item?.id) return
    try {
      await supplierApi.deleteQualification(item.id)
      ElMessage.success('删除成功')
      if (currentSupplierId.value) {
        handleQualification({ id: currentSupplierId.value })
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 切换黑名单状态
const handleToggleBlacklist = (row: any) => {
  if (row.isBlacklisted) {
    // 移出黑名单
    ElMessageBox.confirm(`确定要将供应商【${row.supplierName}】移出黑名单吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await supplierApi.removeFromBlacklist(row.id)
        ElMessage.success('移出黑名单成功')
        fetchSuppliers()
      } catch (error) {
        ElMessage.error('移出黑名单失败')
      }
    }).catch(() => {})
  } else {
    // 加入黑名单
    blacklistTargetRow.value = row
    blacklistForm.reason = ''
    blacklistDialogVisible.value = true
  }
}

const confirmBlacklist = async () => {
  if (!blacklistFormRef.value) return
  await blacklistFormRef.value.validate()
  
  try {
    await supplierApi.addToBlacklist(blacklistTargetRow.value.id, blacklistForm.reason)
    ElMessage.success('加入黑名单成功')
    blacklistDialogVisible.value = false
    fetchSuppliers()
  } catch (error) {
    ElMessage.error('加入黑名单失败')
  }
}

const handlePromoteQualified = (row: any) => {
  ElMessageBox.confirm(`确定将供应商【${row.supplierName}】晋升为合格供应商吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await supplierApi.promoteToQualified(row.id)
      ElMessage.success('晋升成功')
      fetchSuppliers()
    } catch (error: any) {
      ElMessage.error(error?.data?.msg || '晋升失败')
    }
  }).catch(() => {})
}

const handleDemotePotential = (row: any) => {
  ElMessageBox.prompt('请输入降级原因（可选）', '降级为潜在供应商', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '如：绩效不达标、交付延误等'
  }).then(async ({ value }) => {
    try {
      await supplierApi.demoteSupplier(row.id, value || undefined)
      ElMessage.success('降级成功')
      fetchSuppliers()
    } catch (error: any) {
      ElMessage.error(error?.data?.msg || '降级失败')
    }
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    const payload: any = {
      supplierCode: formData.supplierCode,
      supplierName: formData.supplierName,
      contactPerson: formData.contactPerson,
      contactPhone: formData.contactPhone,
      email: formData.email,
      address: formData.address,
      category: formData.supplierLevel,
      type: formData.type,
      status: formData.type === 'BLACKLISTED' ? 'BLACKLISTED' : 'ACTIVE'
    }
    if (formData.id) {
      await supplierApi.updateSupplier(formData.id as any, payload)
    } else {
      await supplierApi.createSupplier(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchSuppliers()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchSuppliers()
}

// 当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchSuppliers()
}
</script>

<style scoped>
.supplier-list-view {
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
