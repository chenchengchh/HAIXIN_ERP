<template>
  <div class="inquiry-view">
    <div class="content-header">
      <h3>询价管理</h3>
      <div class="action-buttons">
        <el-button type="primary" @click="handleCreate">新增询价单</el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-card shadow="never">
        <el-form :model="searchForm" layout="inline" size="small">
          <el-form-item label="询价单号">
            <el-input v-model="searchForm.inquiryCode" placeholder="请输入询价单号" />
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="searchForm.title" placeholder="请输入询价标题" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态">
              <el-option label="全部" value="" />
              <el-option label="待发送" value="DRAFT" />
              <el-option label="已发送" value="SENT" />
              <el-option label="已截止" value="CLOSED" />
              <el-option label="已完成" value="COMPLETED" />
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
        <el-table v-loading="loading" :data="inquiries" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="inquiryCode" label="询价单号" width="180" />
          <el-table-column prop="title" label="标题" width="220" />
          <el-table-column prop="requestCode" label="关联采购申请" width="180" />
          <el-table-column prop="deadline" label="截止日期" width="180" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column prop="createBy" label="创建人" width="120" />
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="handleView(scope.row)">查看</el-button>
              <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="warning" @click="handleSend(scope.row)">发送</el-button>
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
    
    <!-- 询价单表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="70%"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="询价单号" prop="inquiryCode">
          <el-input v-model="formData.inquiryCode" placeholder="请输入询价单号" />
        </el-form-item>
        <el-form-item label="关联采购申请" prop="requestCode">
          <el-input v-model="formData.requestCode" placeholder="请输入关联采购申请号" />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入询价标题" />
        </el-form-item>
        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker
            v-model="formData.deadline"
            type="datetime"
            placeholder="请选择截止日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="询价说明" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入询价说明"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态">
            <el-option label="待发送" value="DRAFT" />
            <el-option label="已发送" value="SENT" />
            <el-option label="已截止" value="CLOSED" />
            <el-option label="已完成" value="COMPLETED" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { inquiryApi } from '../../../../api/srm'

// 搜索表单
const searchForm = reactive({
  inquiryCode: '',
  title: '',
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

// 询价列表
const inquiries = ref<any[]>([])

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增询价单')
const formRef = ref()
const formData = reactive({
  id: undefined,
  inquiryCode: '',
  requestCode: '',
  title: '',
  description: '',
  deadline: new Date(),
  status: 'DRAFT',
  createBy: '',
  createTime: new Date()
})

// 表单验证规则
const rules = reactive({
  inquiryCode: [{ required: true, message: '请输入询价单号', trigger: 'blur' }],
  title: [{ required: true, message: '请输入询价标题', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择截止日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})

// 页面加载时获取数据
onMounted(() => {
  fetchInquiries()
})

// 获取询价单列表
const fetchInquiries = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      inquiryCode: searchForm.inquiryCode,
      title: searchForm.title,
      status: searchForm.status
    }
    const res = await inquiryApi.getInquiryList(params)
    inquiries.value = (res.data.list || res.data.records || [])
    pagination.total = (res.data.total || 0)
  } catch (error) {
    console.error(error)
    ElMessage.error('获取询价单列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchInquiries()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    inquiryCode: '',
    title: '',
    status: ''
  })
  pagination.currentPage = 1
  fetchInquiries()
}

// 新增
const handleCreate = () => {
  dialogTitle.value = '新增询价单'
  Object.assign(formData, {
    id: undefined,
    inquiryCode: '',
    requestCode: '',
    title: '',
    description: '',
    deadline: new Date(),
    status: 'DRAFT',
    createBy: '',
    createTime: new Date()
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑询价单'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 查看
const handleView = (row: any) => {
  dialogTitle.value = '查看询价单'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该询价单吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await inquiryApi.deleteInquiry(row.id)
      ElMessage.success('删除成功')
      fetchInquiries()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 发送询价（后端语义为 publish）
const handleSend = (row: any) => {
  ElMessageBox.confirm('确定要发布该询价单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await inquiryApi.publishInquiry(row.id)
      ElMessage.success('发布成功')
      fetchInquiries()
    } catch (error) {
      ElMessage.error('发布失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 关闭询价
const handleClose = (row: any) => {
  ElMessageBox.confirm('确定要关闭该询价单吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await inquiryApi.closeInquiry(row.id)
      ElMessage.success('关闭成功')
      fetchInquiries()
    } catch (error) {
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
    if (formData.id) {
      await inquiryApi.updateInquiry(formData.id, formData)
    } else {
      await inquiryApi.createInquiry(formData)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchInquiries()
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败')
  }
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchInquiries()
}

// 当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchInquiries()
}
</script>

<style scoped>
.inquiry-view {
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
