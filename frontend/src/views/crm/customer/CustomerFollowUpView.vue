<template>
  <div class="customer-follow-up-view">
    <!-- 搜索和筛选区域 -->
    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="跟进类型">
          <el-select v-model="searchForm.followUpType" placeholder="请选择跟进类型" clearable>
            <el-option label="电话" value="phone" />
            <el-option label="邮件" value="email" />
            <el-option label="拜访" value="visit" />
            <el-option label="微信" value="wechat" />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 跟进记录列表区域 -->
    <el-card shadow="hover" class="list-card">
      <template #header>
        <div class="card-header">
          <span>跟进记录列表</span>
          <el-button type="primary" @click="handleAddFollowUp">新增跟进记录</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="followUpList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="customerName" label="客户名称" min-width="150" />
        <el-table-column prop="followUpType" label="跟进类型" width="120">
          <template #default="scope">
            <el-tag
              :type="followUpTypeMap[scope.row.followUpType]"
            >
              {{ followUpTypeTextMap[scope.row.followUpType] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="跟进内容" min-width="200" />
        <el-table-column prop="followUpTime" label="跟进时间" width="180" />
        <el-table-column prop="nextPlan" label="下次计划" min-width="150" />
        <el-table-column prop="nextTime" label="下次跟进时间" width="180" />
        <el-table-column prop="followUpUserId" label="跟进人ID" width="100" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditFollowUp(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteFollowUp(scope.row.id)">删除</el-button>
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

    <!-- 新增/编辑跟进记录对话框 -->
    <el-dialog
      :model-value="dialogVisible"
      @update:model-value="handleDialogVisibleChange"
      :title="followUp.id ? '编辑跟进记录' : '新增跟进记录'"
      width="700px"
      @close="handleClose"
    >
      <el-form :model="followUp" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户ID" prop="customerId">
              <el-input v-model.number="followUp.customerId" placeholder="请输入客户ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerName">
              <el-input v-model="followUp.customerName" placeholder="请输入客户名称" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="跟进类型" prop="followUpType">
              <el-select v-model="followUp.followUpType" placeholder="请选择跟进类型">
                <el-option label="电话" value="phone" />
                <el-option label="邮件" value="email" />
                <el-option label="拜访" value="visit" />
                <el-option label="微信" value="wechat" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="跟进人ID" prop="followUpUserId">
              <el-input v-model.number="followUp.followUpUserId" placeholder="请输入跟进人ID" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="跟进内容" prop="content">
              <el-input v-model="followUp.content" placeholder="请输入跟进内容" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="跟进时间" prop="followUpTime">
              <el-date-picker
                v-model="followUp.followUpTime"
                type="datetime"
                placeholder="请选择跟进时间"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下次跟进时间" prop="nextTime">
              <el-date-picker
                v-model="followUp.nextTime"
                type="datetime"
                placeholder="请选择下次跟进时间"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="下次计划" prop="nextPlan">
              <el-input v-model="followUp.nextPlan" placeholder="请输入下次计划" type="textarea" :rows="2" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { customerApi } from '../../../api/crm/customer'
import { unwrapPageResponse } from '../../../api'

// 搜索表单
const searchForm = reactive({
  customerName: '',
  followUpType: ''
})

// 日期范围
const dateRange = ref<[string, string] | null>(null)

// 跟进记录列表数据
const followUpList = ref<any[]>([])
const loading = ref(false)

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const followUp = reactive<any>({
  id: undefined,
  customerId: 0,
  customerName: '',
  followUpUserId: 0,
  followUpType: '',
  content: '',
  nextPlan: '',
  followUpTime: '',
  nextTime: ''
})

// 表单引用
const formRef = ref()

// 选中的跟进记录
const selectedFollowUps = ref<any[]>([])

// 跟进类型映射（兼容 phone/call 两种电话类型编码）
const followUpTypeMap: Record<string, string> = {
  'call': 'primary',
  'phone': 'primary',
  'email': 'success',
  'visit': 'warning',
  'wechat': 'info'
}

// 跟进类型文本映射
const followUpTypeTextMap: Record<string, string> = {
  'call': '电话',
  'phone': '电话',
  'email': '邮件',
  'visit': '拜访',
  'wechat': '微信'
}

// 表单验证规则
const rules = reactive({
  customerId: [{ required: true, message: '请输入客户ID', trigger: 'blur' }],
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  followUpType: [{ required: true, message: '请选择跟进类型', trigger: 'change' }],
  followUpUserId: [{ required: true, message: '请输入跟进人ID', trigger: 'blur' }],
  content: [{ required: true, message: '请输入跟进内容', trigger: 'blur' }],
  followUpTime: [{ required: true, message: '请选择跟进时间', trigger: 'change' }]
})

// 初始化数据
onMounted(() => {
  fetchFollowUpList()
})

/**
 * 获取跟进记录列表（对接后端分页接口 GET /api/v1/crm/follow-ups）
 */
const fetchFollowUpList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    if (searchForm.customerName) {
      params.customerName = searchForm.customerName
    }
    if (searchForm.followUpType) {
      params.followUpType = searchForm.followUpType
    }
    // 日期范围转换为后端所需的 startTime/endTime（ISO 日期时间格式）
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = `${dateRange.value[0]}T00:00:00`
      params.endTime = `${dateRange.value[1]}T23:59:59`
    }
    const response = await customerApi.getFollowUpList(params)
    const { list, total } = unwrapPageResponse<any>(response)
    followUpList.value = list
    pagination.total = total
  } catch (error) {
    console.error('获取跟进记录列表失败:', error)
    ElMessage.error('获取跟进记录列表失败')
    followUpList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchFollowUpList()
}

// 重置
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    ;(searchForm as any)[key] = ''
  })
  dateRange.value = null
  pagination.currentPage = 1
  fetchFollowUpList()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchFollowUpList()
}

// 当前页变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchFollowUpList()
}

// 新增跟进记录
const handleAddFollowUp = () => {
  Object.assign(followUp, {
    id: undefined,
    customerId: 0,
    customerName: '',
    followUpUserId: 0,
    followUpType: '',
    content: '',
    nextPlan: '',
    followUpTime: '',
    nextTime: ''
  })
  dialogVisible.value = true
}

// 编辑跟进记录
const handleEditFollowUp = (row: any) => {
  Object.assign(followUp, row)
  dialogVisible.value = true
}

// 删除跟进记录
const handleDeleteFollowUp = (id: number) => {
  ElMessageBox.confirm('确定要删除该跟进记录吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await customerApi.deleteFollowUp(id)
      ElMessage.success('删除成功')
      fetchFollowUpList()
    } catch (error) {
      console.error('删除跟进记录失败:', error)
      ElMessage.error('删除跟进记录失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 对话框可见性变化
const handleDialogVisibleChange = (newVisible: boolean) => {
  if (!newVisible) {
    handleClose()
  }
}

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 保存跟进记录
const handleSave = async () => {
  if (!formRef.value) return

  formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (followUp.id) {
          // 编辑：后端更新接口为 PUT /api/v1/crm/follow-ups（全量 body，含 id）
          await customerApi.updateFollowUp(followUp)
        } else {
          // 新增：POST /api/v1/crm/follow-ups
          await customerApi.addCustomerFollowUp(followUp.customerId, followUp)
        }
        ElMessage.success(followUp.id ? '更新成功' : '创建成功')
        dialogVisible.value = false
        fetchFollowUpList()
      } catch (error) {
        console.error('保存跟进记录失败:', error)
        ElMessage.error('保存跟进记录失败')
      }
    }
  })
}

// 选择跟进记录变化
const handleSelectionChange = (selection: any[]) => {
  selectedFollowUps.value = selection
}
</script>

<style scoped>
.customer-follow-up-view {
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>