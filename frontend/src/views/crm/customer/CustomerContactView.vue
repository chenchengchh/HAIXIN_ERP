<template>
  <div class="customer-contact-view">
    <!-- 搜索和筛选区域 -->
    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="联系人姓名">
          <el-input v-model="searchForm.contactName" placeholder="请输入联系人姓名" clearable />
        </el-form-item>
        <el-form-item label="职位">
          <el-input v-model="searchForm.position" placeholder="请输入职位" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 联系人列表区域 -->
    <el-card shadow="hover" class="list-card">
      <template #header>
        <div class="card-header">
          <span>联系人列表</span>
          <el-button type="primary" @click="handleAddContact">新增联系人</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="contactList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="customerName" label="客户名称" min-width="150" />
        <el-table-column prop="contactName" label="联系人姓名" width="120" />
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column prop="phone" label="固定电话" width="120" />
        <el-table-column prop="mobile" label="手机号码" width="120" />
        <el-table-column prop="email" label="电子邮箱" width="150" />
        <el-table-column prop="wechat" label="微信" width="120" />
        <el-table-column prop="isPrimary" label="是否主要联系人" width="120">
          <template #default="scope">
            <el-switch v-model="scope.row.isPrimary" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditContact(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteContact(scope.row.id)">删除</el-button>
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

    <!-- 新增/编辑联系人对话框 -->
    <el-dialog
      :model-value="dialogVisible"
      @update:model-value="handleDialogVisibleChange"
      :title="contact.id ? '编辑联系人' : '新增联系人'"
      width="700px"
      @close="handleClose"
    >
      <el-form :model="contact" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户ID" prop="customerId">
              <el-input v-model.number="contact.customerId" placeholder="请输入客户ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerName">
              <el-input v-model="contact.customerName" placeholder="请输入客户名称" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人姓名" prop="contactName">
              <el-input v-model="contact.contactName" placeholder="请输入联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位" prop="position">
              <el-input v-model="contact.position" placeholder="请输入职位" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="固定电话" prop="phone">
              <el-input v-model="contact.phone" placeholder="请输入固定电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号码" prop="mobile">
              <el-input v-model="contact.mobile" placeholder="请输入手机号码" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="电子邮箱" prop="email">
              <el-input v-model="contact.email" placeholder="请输入电子邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="微信" prop="wechat">
              <el-input v-model="contact.wechat" placeholder="请输入微信" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="contact.remark" placeholder="请输入备注" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否主要联系人">
              <el-switch v-model="contact.isPrimary" />
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
  contactName: '',
  position: ''
})

// 联系人列表数据
const contactList = ref<any[]>([])
const loading = ref(false)

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const contact = reactive<any>({
  id: undefined,
  customerId: 0,
  customerName: '',
  contactName: '',
  position: '',
  phone: '',
  mobile: '',
  email: '',
  wechat: '',
  isPrimary: false,
  remark: ''
})

// 表单引用
const formRef = ref()

// 选中的联系人
const selectedContacts = ref<any[]>([])

// 表单验证规则
const rules = reactive({
  customerId: [{ required: true, message: '请输入客户ID', trigger: 'blur' }],
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  mobile: [{ required: true, message: '请输入手机号码', trigger: 'blur' }]
})

// 初始化数据
onMounted(() => {
  fetchContactList()
})

/**
 * 获取联系人列表（对接后端分页接口 GET /api/v1/crm/contacts）
 */
const fetchContactList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    if (searchForm.customerName) {
      params.customerName = searchForm.customerName
    }
    if (searchForm.contactName) {
      params.contactName = searchForm.contactName
    }
    if (searchForm.position) {
      params.position = searchForm.position
    }
    const response = await customerApi.getContactList(params)
    const { list, total } = unwrapPageResponse<any>(response)
    contactList.value = list
    pagination.total = total
  } catch (error) {
    console.error('获取联系人列表失败:', error)
    ElMessage.error('获取联系人列表失败')
    contactList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchContactList()
}

// 重置
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    ;(searchForm as any)[key] = ''
  })
  pagination.currentPage = 1
  fetchContactList()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchContactList()
}

// 当前页变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchContactList()
}

// 新增联系人
const handleAddContact = () => {
  Object.assign(contact, {
    id: undefined,
    customerId: 0,
    customerName: '',
    contactName: '',
    position: '',
    phone: '',
    mobile: '',
    email: '',
    wechat: '',
    isPrimary: false,
    remark: ''
  })
  dialogVisible.value = true
}

// 编辑联系人
const handleEditContact = (row: any) => {
  Object.assign(contact, row)
  dialogVisible.value = true
}

// 删除联系人
const handleDeleteContact = (id: number) => {
  ElMessageBox.confirm('确定要删除该联系人吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await customerApi.deleteContact(id)
      ElMessage.success('删除成功')
      fetchContactList()
    } catch (error) {
      console.error('删除联系人失败:', error)
      ElMessage.error('删除联系人失败')
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

// 保存联系人
const handleSave = async () => {
  if (!formRef.value) return
  
  formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (contact.id) {
          // 编辑：后端更新接口为 PUT /api/v1/crm/contacts（全量 body，含 id）
          await customerApi.updateContact(contact)
        } else {
          // 新增：POST /api/v1/crm/contacts
          await customerApi.addCustomerContact(contact.customerId, contact)
        }
        ElMessage.success(contact.id ? '更新成功' : '创建成功')
        dialogVisible.value = false
        fetchContactList()
      } catch (error) {
        console.error('保存联系人失败:', error)
        ElMessage.error('保存联系人失败')
      }
    }
  })
}

// 选择联系人变化
const handleSelectionChange = (selection: any[]) => {
  selectedContacts.value = selection
}
</script>

<style scoped>
.customer-contact-view {
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