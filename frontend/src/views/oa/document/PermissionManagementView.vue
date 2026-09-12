<template>
  <div class="permission-management-view">
    <!-- 文档选择区域 -->
    <div class="document-selection">
      <h3>选择文档</h3>
      <el-input
        v-model="selectedDocumentName"
        placeholder="输入文档名称进行搜索"
        @input="handleDocumentSearch"
        clearable
      >
        <template #append>
          <el-button @click="openDocumentDialog">
            <el-icon><Search /></el-icon>
            选择文档
          </el-button>
        </template>
      </el-input>
    </div>
    
    <!-- 权限设置区域 -->
    <div class="permission-settings" v-if="selectedDocument">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>{{ selectedDocument.documentName }} - 权限设置</span>
          </div>
        </template>
        <div class="card-content">
          <!-- 基础权限设置 -->
          <div class="basic-permissions">
            <h4>基础权限</h4>
            <el-form :model="basicPermissions" label-width="120px">
              <el-form-item label="权限类型">
                <el-select v-model="basicPermissions.permissionType" placeholder="选择权限类型">
                  <el-option label="私有" value="private" />
                  <el-option label="部门可见" value="department" />
                  <el-option label="公开" value="public" />
                </el-select>
              </el-form-item>
              <el-form-item label="允许下载">
                <el-switch v-model="basicPermissions.allowDownload" />
              </el-form-item>
              <el-form-item label="允许打印">
                <el-switch v-model="basicPermissions.allowPrint" />
              </el-form-item>
              <el-form-item label="允许复制">
                <el-switch v-model="basicPermissions.allowCopy" />
              </el-form-item>
              <el-form-item label="有效期">
                <el-date-picker
                  v-model="basicPermissions.expiryDate"
                  type="datetime"
                  placeholder="选择有效期"
                />
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 高级权限设置 -->
          <div class="advanced-permissions">
            <div class="section-header">
              <h4>高级权限</h4>
              <el-button type="primary" size="small" @click="openPermissionDialog">
                <el-icon><Plus /></el-icon>
                添加权限
              </el-button>
            </div>
            
            <!-- 权限列表 -->
            <el-table :data="documentPermissions" stripe style="width: 100%">
              <el-table-column prop="permissionType" label="权限类型" width="120" align="center">
                <template #default="scope">
                  <el-tag size="small">{{ scope.row.permissionType === 'user' ? '用户' : '角色' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="targetName" label="目标名称" min-width="150" />
              <el-table-column prop="permissions" label="权限" width="200" align="center">
                <template #default="scope">
                  <el-tag size="small" v-for="permission in scope.row.permissions" :key="permission" :type="'info'">
                    {{ getPermissionLabel(permission) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" align="center">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="editPermission(scope.row)">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button type="danger" size="small" @click="deletePermission(scope.row)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <!-- 保存按钮 -->
          <div class="save-section">
            <el-button type="primary" @click="savePermissions">
              <el-icon><Check /></el-icon>
              保存权限设置
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 文档选择对话框 -->
    <el-dialog
      v-model="documentDialogVisible"
      title="选择文档"
      width="800px"
    >
      <el-table :data="documents" stripe style="width: 100%" @row-click="selectDocument">
        <el-table-column prop="documentName" label="文档名称" min-width="200" />
        <el-table-column prop="documentNo" label="文档编号" width="150" align="center" />
        <el-table-column prop="fileType" label="文件类型" width="100" align="center">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.fileType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="uploaderName" label="上传人" width="120" align="center" />
        <el-table-column prop="uploadTime" label="上传时间" width="180" align="center" />
      </el-table>
    </el-dialog>
    
    <!-- 权限设置对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      :title="permissionDialogTitle"
      width="600px"
    >
      <el-form :model="permissionForm" label-width="100px">
        <el-form-item label="权限类型" required>
          <el-select v-model="permissionForm.permissionType" placeholder="选择权限类型">
            <el-option label="用户" value="user" />
            <el-option label="角色" value="role" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标选择" required>
          <el-select v-model="permissionForm.targetId" placeholder="选择目标" filterable>
            <template v-if="permissionForm.permissionType === 'user'">
              <el-option
                v-for="user in users"
                :key="user.id"
                :label="user.userName"
                :value="user.id"
              />
            </template>
            <template v-else>
              <el-option
                v-for="role in roles"
                :key="role.id"
                :label="role.roleName"
                :value="role.id"
              />
            </template>
          </el-select>
        </el-form-item>
        <el-form-item label="权限设置" required>
          <el-checkbox-group v-model="permissionForm.permissions">
            <el-checkbox label="view">查看</el-checkbox>
            <el-checkbox label="download">下载</el-checkbox>
            <el-checkbox label="edit">编辑</el-checkbox>
            <el-checkbox label="print">打印</el-checkbox>
            <el-checkbox label="copy">复制</el-checkbox>
            <el-checkbox label="share">分享</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="有效期">
          <el-date-picker
            v-model="permissionForm.expiryDate"
            type="datetime"
            placeholder="选择有效期"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePermission">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Edit, Delete, Check } from '@element-plus/icons-vue'

// 文档数据
const documents = ref([
  { id: 1, documentName: '产品需求文档', documentNo: 'DOC-20251201-001', fileType: 'pdf', uploaderName: '张三', uploadTime: '2025-12-01 10:00' },
  { id: 2, documentName: '技术设计文档', documentNo: 'DOC-20251202-002', fileType: 'doc', uploaderName: '李四', uploadTime: '2025-12-02 14:20' },
  { id: 3, documentName: '财务报表', documentNo: 'DOC-20251203-003', fileType: 'xls', uploaderName: '王五', uploadTime: '2025-12-03 16:45' },
  { id: 4, documentName: '运营计划', documentNo: 'DOC-20251204-004', fileType: 'ppt', uploaderName: '赵六', uploadTime: '2025-12-04 11:20' },
  { id: 5, documentName: '员工手册', documentNo: 'DOC-20251205-005', fileType: 'pdf', uploaderName: '孙七', uploadTime: '2025-12-05 09:00' }
])

// 用户数据
const users = ref([
  { id: 1, userName: '张三', department: '技术部' },
  { id: 2, userName: '李四', department: '销售部' },
  { id: 3, userName: '王五', department: '财务部' },
  { id: 4, userName: '赵六', department: '运营部' },
  { id: 5, userName: '孙七', department: '人事部' }
])

// 角色数据
const roles = ref([
  { id: 1, roleName: '管理员' },
  { id: 2, roleName: '部门经理' },
  { id: 3, roleName: '普通员工' },
  { id: 4, roleName: '技术人员' },
  { id: 5, roleName: '财务人员' }
])

// 选择的文档
const selectedDocument = ref<any>(null)
const selectedDocumentName = ref('')

// 基础权限设置
const basicPermissions = reactive({
  permissionType: 'private',
  allowDownload: true,
  allowPrint: true,
  allowCopy: true,
  expiryDate: null
})

// 文档权限列表
const documentPermissions = ref([
  {
    id: 1,
    documentId: 1,
    permissionType: 'user',
    targetId: 2,
    targetName: '李四',
    permissions: ['view', 'download'],
    expiryDate: null
  },
  {
    id: 2,
    documentId: 1,
    permissionType: 'role',
    targetId: 3,
    targetName: '普通员工',
    permissions: ['view'],
    expiryDate: null
  }
])

// 文档选择对话框
const documentDialogVisible = ref(false)

// 权限设置对话框
const permissionDialogVisible = ref(false)
const permissionDialogTitle = ref('添加权限')
const permissionForm = reactive({
  id: 0,
  documentId: 0,
  permissionType: 'user',
  targetId: 0,
  permissions: ['view'],
  expiryDate: null
})

// 打开文档选择对话框
const openDocumentDialog = () => {
  documentDialogVisible.value = true
}

// 文档搜索
const handleDocumentSearch = (query: string) => {
  console.log('搜索文档:', query)
  // 这里应该调用API搜索文档
}

// 选择文档
const selectDocument = (document: any) => {
  selectedDocument.value = document
  selectedDocumentName.value = document.documentName
  documentDialogVisible.value = false
  // 这里应该调用API获取文档权限信息
}

// 打开权限设置对话框
const openPermissionDialog = () => {
  permissionDialogTitle.value = '添加权限'
  resetPermissionForm()
  permissionDialogVisible.value = true
}

// 重置权限表单
const resetPermissionForm = () => {
  Object.assign(permissionForm, {
    id: 0,
    documentId: selectedDocument.value?.id || 0,
    permissionType: 'user',
    targetId: 0,
    permissions: ['view'],
    expiryDate: null
  })
}

// 编辑权限
const editPermission = (permission: any) => {
  permissionDialogTitle.value = '编辑权限'
  Object.assign(permissionForm, {
    id: permission.id,
    documentId: permission.documentId,
    permissionType: permission.permissionType,
    targetId: permission.targetId,
    permissions: [...permission.permissions],
    expiryDate: permission.expiryDate
  })
  permissionDialogVisible.value = true
}

// 保存权限
const savePermission = () => {
  console.log('保存权限:', permissionForm)
  // 这里应该调用API保存权限
  ElMessage.success('权限保存成功')
  permissionDialogVisible.value = false
}

// 删除权限
const deletePermission = (permission: any) => {
  console.log('删除权限:', permission)
  // 这里应该调用API删除权限
  ElMessage.success('权限删除成功')
}

// 保存所有权限设置
const savePermissions = () => {
  console.log('保存所有权限设置:', basicPermissions, documentPermissions.value)
  // 这里应该调用API保存所有权限设置
  ElMessage.success('权限设置保存成功')
}

// 获取权限标签
const getPermissionLabel = (permission: string) => {
  const labelMap: Record<string, string> = {
    view: '查看',
    download: '下载',
    edit: '编辑',
    print: '打印',
    copy: '复制',
    share: '分享'
  }
  return labelMap[permission] || permission
}
</script>

<style scoped>
.permission-management-view {
  padding: 20px;
  box-sizing: border-box;
}

.document-selection {
  margin-bottom: 20px;
}

.document-selection h3 {
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: bold;
}

.permission-settings {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.card-content {
  padding: 20px 0;
}

.basic-permissions {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.advanced-permissions {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
}

.save-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.basic-permissions h4,
.advanced-permissions h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .permission-management-view {
    padding: 12px;
  }
  
  .card-content {
    padding: 10px 0;
  }
  
  .basic-permissions,
  .advanced-permissions {
    margin-bottom: 16px;
    padding-bottom: 16px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>