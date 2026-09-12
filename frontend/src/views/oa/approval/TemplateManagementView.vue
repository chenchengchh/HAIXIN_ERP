<template>
  <div class="template-management-view">
    <!-- 页面操作栏 -->
    <div class="page-header">
      <div class="header-actions">
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          新建模板
        </el-button>
        <el-button type="info" @click="openImportDialog">
          <el-icon><Upload /></el-icon>
          导入模板
        </el-button>
      </div>
    </div>
    
    <!-- 新建模板对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="新建模板"
      width="600px"
      @close="resetCreateForm"
    >
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板编码" prop="code">
          <el-input v-model="createForm.code" placeholder="请输入模板编码" />
        </el-form-item>
        <el-form-item label="模板类型" prop="processType">
          <el-select v-model="createForm.processType" placeholder="请选择模板类型">
            <el-option label="请假申请" value="leave" />
            <el-option label="报销申请" value="expense" />
            <el-option label="采购申请" value="purchase" />
            <el-option label="合同审批" value="contract" />
            <el-option label="用章申请" value="seal" />
            <el-option label="出差申请" value="travel" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板描述">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入模板描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateTemplate">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 导入模板对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入模板"
      width="500px"
    >
      <el-upload
        class="upload-demo"
        ref="uploadRef"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        accept=".json,.xml"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将模板文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 JSON 或 XML 格式的模板文件，单个文件大小不超过 5MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeImportDialog">取消</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 搜索和筛选区域 -->
    <div class="search-filter">
      <el-form :inline="true" :model="searchParams" class="filter-form">
        <el-form-item label="模板名称">
          <el-input v-model="searchParams.name" placeholder="输入模板名称" size="small" />
        </el-form-item>
        <el-form-item label="模板类型">
          <el-select v-model="searchParams.type" placeholder="选择模板类型" size="small">
            <el-option label="全部" value="" />
            <el-option label="请假申请" value="leave" />
            <el-option label="报销申请" value="expense" />
            <el-option label="采购申请" value="purchase" />
            <el-option label="合同审批" value="contract" />
            <el-option label="用章申请" value="seal" />
            <el-option label="出差申请" value="travel" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchParams.status" placeholder="选择状态" size="small">
            <el-option label="全部" value="" />
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="active" />
            <el-option label="已归档" value="archived" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search" size="small">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="reset" size="small">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 模板列表 -->
    <div class="template-list">
      <!-- 加载状态 -->
      <el-skeleton :rows="5" animated v-if="loading" />
      
      <!-- 空状态 -->
      <el-empty v-else-if="templateList.length === 0" description="暂无模板数据">
        <template #description>
          <span>暂无模板数据</span>
        </template>
        <el-button type="primary" @click="refresh">刷新列表</el-button>
      </el-empty>
      
      <!-- 模板列表表格 -->
      <el-table
        v-else
        :data="templateList"
        stripe
        style="width: 100%">
        <el-table-column prop="id" label="模板ID" width="80" align="center" />
        <el-table-column prop="name" label="模板名称" min-width="150">
          <template #default="scope">
            <el-link type="primary" @click="viewTemplate(scope.row)">{{ scope.row?.name || '未命名模板' }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="模板编码" width="120" align="center" />
        <el-table-column prop="processType" label="模板类型" width="120" align="center">
          <template #default="scope">
            <el-tag size="small">{{ getCategoryLabel(scope.row?.processType || '') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本号" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row?.status || 0)" size="small">{{ getStatusLabel(scope.row?.status || 0) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorId" label="创建人" width="100" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
        <el-table-column prop="updateTime" label="更新时间" width="160" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editTemplate(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="success" size="small" v-if="scope.row?.status === 0" @click="publishTemplate(scope.row)">
              <el-icon><Check /></el-icon>
              发布
            </el-button>
            <el-button type="warning" size="small" v-if="scope.row?.status === 1" @click="archiveTemplate(scope.row)">
              <el-icon><Box /></el-icon>
              归档
            </el-button>
            <el-button type="danger" size="small" @click="deleteTemplate(scope.row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Plus, Upload, UploadFilled, Search, Refresh, Edit, Check, Box, Delete } from '@element-plus/icons-vue'
import { approvalProcessApi, type ApprovalProcess } from '@/api/oa'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { unwrapPageResponse } from '@/api'
import { DataTransformer } from '@/utils/data-transformer'

// 搜索参数
const searchParams = reactive({
  name: '',
  type: '',
  status: ''
})

// 分页参数
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 路由实例
const router = useRouter()

// 加载状态
const loading = ref(false)

// 新建模板对话框
const createDialogVisible = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive({
  name: '',
  code: '',
  processType: '',
  description: ''
})
const createRules = reactive({
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 2, max: 50, message: '模板名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入模板编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_-]+$/, message: '模板编码只能包含字母、数字、下划线和连字符', trigger: 'blur' }
  ],
  processType: [
    { required: true, message: '请选择模板类型', trigger: 'change' }
  ]
})

// 导入模板对话框
const importDialogVisible = ref(false)
const uploadRef = ref()
const uploadUrl = '/api/oa/approval/process/import'
const uploadHeaders = {
  // 添加必要的请求头，如认证信息
}

// 模板列表数据，使用ApprovalProcess[]类型
const templateList = ref<ApprovalProcess[]>([
  {
    id: 1,
    name: '请假申请模板',
    code: 'LEAVE-001',
    status: 1,
    processType: 'leave',
    processDefinition: '<bpmn:definitions ...>',
    formConfig: '{"formType": "leave", "fields": [...]}',
    creatorId: 1,
    createTime: '2025-12-01 10:00',
    updateTime: '2025-12-01 10:00'
  },
  {
    id: 2,
    name: '报销申请模板',
    code: 'EXPENSE-001',
    status: 1,
    processType: 'expense',
    processDefinition: '<bpmn:definitions ...>',
    formConfig: '{"formType": "expense", "fields": [...]}',
    creatorId: 1,
    createTime: '2025-12-01 11:00',
    updateTime: '2025-12-01 11:00'
  },
  {
    id: 3,
    name: '采购申请模板',
    code: 'PURCHASE-001',
    status: 1,
    processType: 'purchase',
    processDefinition: '<bpmn:definitions ...>',
    formConfig: '{"formType": "purchase", "fields": [...]}',
    creatorId: 1,
    createTime: '2025-12-01 14:00',
    updateTime: '2025-12-01 14:00'
  },
  {
    id: 4,
    name: '合同审批模板',
    code: 'CONTRACT-001',
    status: 0,
    processType: 'contract',
    processDefinition: '<bpmn:definitions ...>',
    formConfig: '{"formType": "contract", "fields": [...]}',
    creatorId: 1,
    createTime: '2025-12-15 09:00',
    updateTime: '2025-12-15 09:30'
  },
  {
    id: 5,
    name: '用章申请模板',
    code: 'SEAL-001',
    status: 1,
    processType: 'seal',
    processDefinition: '<bpmn:definitions ...>',
    formConfig: '{"formType": "seal", "fields": [...]}',
    creatorId: 1,
    createTime: '2025-11-20 16:00',
    updateTime: '2025-12-05 10:00'
  },
  {
    id: 6,
    name: '出差申请模板',
    code: 'TRAVEL-001',
    status: 1,
    processType: 'travel',
    processDefinition: '<bpmn:definitions ...>',
    formConfig: '{"formType": "travel", "fields": [...]}',
    creatorId: 1,
    createTime: '2025-12-02 15:00',
    updateTime: '2025-12-02 15:00'
  }
])

// 计算总条数
pagination.total = templateList.value.length

// 搜索
const search = () => {
  console.log('搜索参数:', searchParams)
  pagination.currentPage = 1
  fetchTemplateList()
}

// 重置
const reset = () => {
  Object.assign(searchParams, {
    name: '',
    type: '',
    status: ''
  })
  pagination.currentPage = 1
  fetchTemplateList()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchTemplateList()
}

// 页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchTemplateList()
}

// 获取模板类型标签
const getCategoryLabel = (category: string) => {
  const labelMap: Record<string, string> = {
    leave: '请假申请',
    expense: '报销申请',
    purchase: '采购申请',
    contract: '合同审批',
    seal: '用章申请',
    travel: '出差申请'
  }
  return labelMap[category] || category
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'info', // 草稿
    1: 'success', // 已发布
    2: 'warning' // 已归档
  }
  return typeMap[status] || 'info'
}

// 获取状态标签
const getStatusLabel = (status: number) => {
  const labelMap: Record<number, string> = {
    0: '草稿',
    1: '已发布',
    2: '已归档'
  }
  return labelMap[status] || String(status)
}

// 打开新建模板对话框
const openCreateDialog = () => {
  createDialogVisible.value = true
}

// 重置新建模板表单
const resetCreateForm = () => {
  if (createFormRef.value) {
    createFormRef.value.resetFields()
  }
  Object.assign(createForm, {
    name: '',
    code: '',
    processType: '',
    description: ''
  })
}

// 处理新建模板
const handleCreateTemplate = async () => {
  if (!createFormRef.value) return
  
  try {
    await createFormRef.value.validate()
    
    // 调用API创建模板
    await approvalProcessApi.create({
      name: createForm.name,
      code: createForm.code,
      processType: createForm.processType,
      description: createForm.description,
      processDefinition: '', // 这里可以根据实际情况初始化流程定义
      status: 0, // 0表示草稿状态
      creatorId: 1 // 模拟当前登录用户ID，实际项目中应该从用户信息中获取
    })
    
    ElMessage.success('模板创建成功')
    createDialogVisible.value = false
    fetchTemplateList() // 重新加载模板列表
  } catch (error: any) {
    if (error === 'cancel') return
    console.error('创建模板失败:', error)
    ElMessage.error('创建模板失败，请重试')
  }
}

// 打开导入模板对话框
const openImportDialog = () => {
  importDialogVisible.value = true
}

// 关闭导入模板对话框
const closeImportDialog = () => {
  importDialogVisible.value = false
  if (uploadRef.value) {
    // 重置上传组件
    uploadRef.value.clearFiles()
  }
}

// 上传前验证
const beforeUpload = (file: File) => {
  const fileSize = file.size / 1024 / 1024 // 转换为MB
  if (fileSize > 5) {
    ElMessage.error('上传文件大小不能超过 5MB')
    return false
  }
  
  const fileType = file.name.split('.').pop()?.toLowerCase()
  if (fileType !== 'json' && fileType !== 'xml') {
    ElMessage.error('只能上传 JSON 或 XML 格式的模板文件')
    return false
  }
  
  return true
}

// 上传成功处理
const handleUploadSuccess = (response: any) => {
  const normalized = DataTransformer.normalizeResponse(response)
  if (DataTransformer.isSuccessCode(normalized?.code)) {
    ElMessage.success('模板导入成功')
    importDialogVisible.value = false
    fetchTemplateList() // 重新加载模板列表
  } else {
    ElMessage.error(`模板导入失败: ${normalized?.msg || '未知错误'}`)
  }
}

// 上传失败处理
const handleUploadError = (error: any) => {
  console.error('模板导入失败:', error)
  ElMessage.error('模板导入失败，请重试')
}

// 查看模板
const viewTemplate = (template: any) => {
  console.log('查看模板:', template)
  // 跳转到查看模板页面
  router.push(`/home/oa/approval/template/view/${template.id}`)
}

// 编辑模板
const editTemplate = (template: any) => {
  console.log('编辑模板:', template)
  // 跳转到编辑模板页面
  router.push(`/home/oa/approval/template/edit/${template.id}`)
}

// 发布模板
const publishTemplate = async (template: ApprovalProcess) => {
  try {
    if (!template.id) return
    
    await ElMessageBox.confirm('确定要发布该模板吗？', '发布模板', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    await approvalProcessApi.enable(template.id)
    ElMessage.success('模板发布成功')
    // 重新加载数据
    fetchTemplateList()
  } catch (error: any) {
    if (error === 'cancel') {
      return // 用户取消操作
    }
    console.error('发布模板失败:', error)
    ElMessage.error('发布模板失败，请重试')
  }
}

// 归档模板
const archiveTemplate = async (template: ApprovalProcess) => {
  try {
    if (!template.id) return
    
    await ElMessageBox.confirm('确定要归档该模板吗？', '归档模板', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await approvalProcessApi.disable(template.id)
    ElMessage.success('模板归档成功')
    // 重新加载数据
    fetchTemplateList()
  } catch (error: any) {
    if (error === 'cancel') {
      return // 用户取消操作
    }
    console.error('归档模板失败:', error)
    ElMessage.error('归档模板失败，请重试')
  }
}

// 删除模板
const deleteTemplate = async (template: ApprovalProcess) => {
  try {
    if (!template.id) return
    
    await ElMessageBox.confirm('确定要删除该模板吗？删除后不可恢复', '删除模板', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    await approvalProcessApi.delete(template.id)
    ElMessage.success('模板删除成功')
    // 重新加载数据
    fetchTemplateList()
  } catch (error: any) {
    if (error === 'cancel') {
      return // 用户取消操作
    }
    console.error('删除模板失败:', error)
    ElMessage.error('删除模板失败，请重试')
  }
}

// 刷新列表
const refresh = () => {
  fetchTemplateList()
}

// 获取模板列表数据
const fetchTemplateList = async () => {
  loading.value = true
  try {
    const response = await approvalProcessApi.getList({
      page: pagination.currentPage,
      size: pagination.pageSize,
      ...searchParams
    })
    const page = unwrapPageResponse<ApprovalProcess>(response)
    templateList.value = page.list
    pagination.total = page.total || page.list.length
  } catch (error) {
    console.error('获取模板列表失败:', error)
    ElMessage.error('获取模板列表失败，请重试')
    templateList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.template-management-view {
  padding: 20px;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-filter {
  margin-bottom: 20px;
}

.template-list {
  margin-bottom: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  padding: 10px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .template-management-view {
    padding: 12px;
  }
  
  .header-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .header-actions .el-button {
    width: 100%;
  }
  
  .search-filter .filter-form {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .template-list {
    padding: 5px;
    overflow-x: auto;
  }
}
</style>
