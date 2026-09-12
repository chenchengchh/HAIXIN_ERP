<template>
  <div class="leads-view">
    <!-- 搜索和筛选区域 -->
    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="线索名称">
          <el-input v-model="searchForm.leadName" placeholder="请输入线索名称" clearable />
        </el-form-item>
        <el-form-item label="线索状态">
          <el-select v-model="searchForm.status" placeholder="请选择线索状态" clearable>
            <el-option label="新建" value="new" />
            <el-option label="已联系" value="contacted" />
            <el-option label="已认证" value="qualified" />
            <el-option label="已转化" value="converted" />
            <el-option label="已丢失" value="lost" />
          </el-select>
        </el-form-item>
        <el-form-item label="线索评级">
          <el-select v-model="searchForm.rating" placeholder="请选择线索评级" clearable>
            <el-option label="热" value="hot" />
            <el-option label="温" value="warm" />
            <el-option label="冷" value="cold" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="searchForm.ownerName" placeholder="请输入负责人姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 线索列表区域 -->
    <el-card shadow="hover" class="list-card">
      <template #header>
        <div class="card-header">
          <span>线索列表</span>
          <el-button type="primary" @click="handleAddLead">新增线索</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="leadsList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="leadNo" label="线索编号" width="150" />
        <el-table-column prop="leadName" label="线索名称" min-width="180" />
        <el-table-column prop="companyName" label="公司名称" width="150" />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="source" label="线索来源" width="120" />
        <el-table-column prop="rating" label="评级" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.rating === 'hot' ? 'danger' : scope.row.rating === 'warm' ? 'warning' : 'info'">
              {{ scope.row.rating === 'hot' ? '热' : scope.row.rating === 'warm' ? '温' : '冷' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getLeadStatusType(scope.row.status)">
              {{ getLeadStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="负责人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleViewDetail(scope.row.id)">详情</el-button>
            <el-button size="small" @click="handleEditLead(scope.row)">编辑</el-button>
            <el-button size="small" type="success" @click="handleConvertLead(scope.row.id)">转化</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
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

    <!-- 新增/编辑线索对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditMode ? '编辑线索' : '新增线索'"
      width="600px"
    >
      <el-form :model="leadForm" :rules="leadFormRules" ref="leadFormRef" label-width="100px">
        <el-form-item label="线索名称" prop="leadName">
          <el-input v-model="leadForm.leadName" placeholder="请输入线索名称" />
        </el-form-item>
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="leadForm.companyName" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="leadForm.contactName" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="leadForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="leadForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="线索来源" prop="source">
          <el-select v-model="leadForm.source" placeholder="请选择线索来源">
            <el-option label="电话" value="phone" />
            <el-option label="邮箱" value="email" />
            <el-option label="网站" value="website" />
            <el-option label="展会" value="exhibition" />
            <el-option label="推荐" value="referral" />
          </el-select>
        </el-form-item>
        <el-form-item label="线索评级" prop="rating">
          <el-select v-model="leadForm.rating" placeholder="请选择线索评级">
            <el-option label="热" value="hot" />
            <el-option label="温" value="warm" />
            <el-option label="冷" value="cold" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="ownerId">
          <el-input v-model.number="leadForm.ownerId" placeholder="请输入负责人ID" />
        </el-form-item>
        <el-form-item label="线索状态" prop="status">
          <el-select v-model="leadForm.status" placeholder="请选择线索状态">
            <el-option label="新建" value="new" />
            <el-option label="已联系" value="contacted" />
            <el-option label="已认证" value="qualified" />
            <el-option label="已转化" value="converted" />
            <el-option label="已丢失" value="lost" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSaveLead">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { LeadEntity } from '../../../types/crm/sales'
import { salesApi } from '../../../api/crm/sales'

// 搜索表单
const searchForm = reactive({
  leadName: '',
  status: '',
  rating: '',
  ownerName: ''
})

// 线索列表数据
const leadsList = ref<LeadEntity[]>([])
const loading = ref(false)

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const isEditMode = ref(false)
const leadFormRef = ref()

// 线索表单
const leadForm = reactive<Partial<LeadEntity>>({
  id: undefined,
  leadNo: '',
  leadName: '',
  companyName: '',
  contactName: '',
  phone: '',
  email: '',
  source: '',
  industry: '',
  intent: '',
  rating: 'warm',
  status: 'new',
  ownerId: 0,
  ownerName: '',
  createTime: '',
  convertTime: undefined
})

// 线索表单验证规则
const leadFormRules = reactive({
  leadName: [{ required: true, message: '请输入线索名称', trigger: 'blur' }],
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  source: [{ required: true, message: '请选择线索来源', trigger: 'change' }],
  rating: [{ required: true, message: '请选择线索评级', trigger: 'change' }],
  status: [{ required: true, message: '请选择线索状态', trigger: 'change' }]
})

// 选中的线索
const selectedLeads = ref<LeadEntity[]>([])

// 初始化数据
onMounted(() => {
  fetchLeadsList()
})

// 获取线索列表
const fetchLeadsList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      leadName: searchForm.leadName,
      status: searchForm.status,
      rating: searchForm.rating,
      ownerName: searchForm.ownerName
    }
    const response = await salesApi.getLeadsList(params)
    leadsList.value = response.data?.list || []
    pagination.total = response.data?.total || 0
  } catch (error) {
    console.error('获取线索列表失败:', error)
    ElMessage.error('获取线索列表失败')
    // 使用模拟数据作为 fallback
    leadsList.value = Array.from({ length: 20 }, (_, i) => ({
      id: i + 1,
      leadNo: `LEAD${String(i + 1).padStart(6, '0')}`,
      leadName: `线索${i + 1}`,
      companyName: `公司${i + 1}`,
      contactName: `联系人${i + 1}`,
      phone: `1380013800${i + 1}`,
      email: `lead${i + 1}@example.com`,
      source: ['phone', 'email', 'wechat', 'website'][i % 4] || '',
      industry: ['IT', '金融', '制造', '零售'][i % 4] || '',
      intent: '产品咨询',
      rating: ['hot', 'warm', 'cold'][i % 3] as 'hot' | 'warm' | 'cold',
      status: ['new', 'contacted', 'qualified', 'converted', 'lost'][i % 5] as 'new' | 'contacted' | 'qualified' | 'converted' | 'lost',
      ownerId: 1,
      ownerName: '张三',
      createTime: new Date().toISOString(),
      convertTime: i % 5 === 3 ? new Date().toISOString() : undefined
    }))
    pagination.total = 100
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchLeadsList()
}

// 重置
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    ;(searchForm as any)[key] = ''
  })
  pagination.currentPage = 1
  fetchLeadsList()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchLeadsList()
}

// 当前页变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchLeadsList()
}

// 选中线索变化
const handleSelectionChange = (selection: LeadEntity[]) => {
  selectedLeads.value = selection
}

// 查看详情
const handleViewDetail = async (id: number) => {
  try {
    const response = await salesApi.getLeadDetail(id)
    const leadDetail = response.data
    console.log('线索详情:', leadDetail)
    // 这里可以跳转到线索详情页面或弹出详情对话框
    ElMessage.success('获取线索详情成功')
  } catch (error) {
    console.error('获取线索详情失败:', error)
    ElMessage.error('获取线索详情失败')
  }
}

// 新增线索
const handleAddLead = () => {
  isEditMode.value = false
  Object.assign(leadForm, {
    id: undefined,
    leadNo: '',
    leadName: '',
    companyName: '',
    contactName: '',
    phone: '',
    email: '',
    source: '',
    industry: '',
    intent: '',
    rating: 'warm',
    status: 'new',
    ownerId: 0,
    ownerName: '',
    createTime: '',
    convertTime: undefined
  })
  dialogVisible.value = true
}

// 编辑线索
const handleEditLead = (lead: LeadEntity) => {
  isEditMode.value = true
  Object.assign(leadForm, { ...lead })
  dialogVisible.value = true
}

// 保存线索
const handleSaveLead = () => {
  leadFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (isEditMode.value && leadForm.id) {
          await salesApi.updateLead(leadForm.id, leadForm)
          ElMessage.success('更新成功')
        } else {
          await salesApi.createLead(leadForm)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchLeadsList()
      } catch (error) {
        console.error('保存线索失败:', error)
        ElMessage.error(isEditMode.value ? '更新失败' : '创建失败')
      }
    }
  })
}

// 对话框关闭
const handleDialogClose = () => {
  dialogVisible.value = false
}

// 转化线索
const handleConvertLead = async (id: number) => {
  ElMessageBox.confirm('确定要将该线索转化为客户吗？', '线索转化', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await salesApi.convertLead(id, {})
      ElMessage.success('线索转化成功')
      fetchLeadsList()
    } catch (error) {
      console.error('线索转化失败:', error)
      ElMessage.error('线索转化失败')
    }
  }).catch(() => {
    // 取消转化
  })
}

// 获取线索状态类型
const getLeadStatusType = (status: string): string => {
  const statusTypeMap = {
    new: 'info',
    contacted: 'primary',
    qualified: 'success',
    converted: 'success',
    lost: 'danger'
  }
  return statusTypeMap[status as keyof typeof statusTypeMap] || 'info'
}

// 获取线索状态标签
const getLeadStatusLabel = (status: string): string => {
  const statusMap = {
    new: '新建',
    contacted: '已联系',
    qualified: '已认证',
    converted: '已转化',
    lost: '已丢失'
  }
  return statusMap[status as keyof typeof statusMap] || status
}
</script>

<style scoped>
.leads-view {
  padding: 10px;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.list-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1.1rem;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
