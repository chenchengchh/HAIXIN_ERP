<template>
  <div class="process-change-container">
    <div class="process-change-header">
      <h2>工艺变更管理</h2>
      <el-button type="primary" @click="createChange">
        <el-icon><Plus /></el-icon> 发起工艺变更
      </el-button>
    </div>

    <div class="process-change-content">
      <!-- 搜索和筛选 -->
      <div class="search-filter">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索变更标题或编码"
          prefix-icon="Search"
          clearable
          @input="handleSearch"
        />
        <el-select
          v-model="statusFilter"
          placeholder="变更状态"
          clearable
          @change="handleFilter"
        >
          <el-option label="所有状态" value="all" />
          <el-option label="进行中" value="in-progress" />
          <el-option label="已完成" value="completed" />
        </el-select>
      </div>

      <!-- 工艺变更列表 -->
      <el-table
        :data="filteredProcessChanges"
        style="width: 100%"
        @row-click="selectChange"
        highlight-current-row
      >
        <el-table-column prop="code" label="变更编码" width="150" />
        <el-table-column prop="title" label="变更标题" min-width="250" />
        <el-table-column prop="type" label="变更类型" width="120" />
        <el-table-column prop="processName" label="关联工艺" min-width="180">
          <template #default="scope">
            <div>{{ scope.row.processName }} ({{ scope.row.processCode }})</div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
              <el-tag :type="changeStatusMap[scope.row.status as ChangeStatus]">
                {{ changeStatusText[scope.row.status as ChangeStatus] }}
              </el-tag>
            </template>
        </el-table-column>
        <el-table-column prop="applyUser" label="申请人" width="100" />
        <el-table-column prop="applyTime" label="申请时间" width="150" />
        <el-table-column prop="approveUser" label="审批人" width="100" />
        <el-table-column prop="approveTime" label="审批时间" width="150" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click.stop="viewChangeDetail(scope.row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
            <el-button link type="primary" @click.stop="editChange(scope.row)" :disabled="scope.row.status === 'completed'">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="processChanges.length"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 变更详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="工艺变更详情"
      width="80%"
      :close-on-click-modal="false"
    >
      <div class="change-detail" v-if="selectedChange">
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">变更编码：</span>
              <span>{{ selectedChange.code }}</span>
            </div>
            <div class="detail-item">
              <span class="label">变更标题：</span>
              <span>{{ selectedChange.title }}</span>
            </div>
            <div class="detail-item">
              <span class="label">变更类型：</span>
              <span>{{ selectedChange.type }}</span>
            </div>
            <div class="detail-item">
              <span class="label">关联工艺：</span>
              <span>{{ selectedChange.processName }} ({{ selectedChange.processCode }})</span>
            </div>
            <div class="detail-item">
              <span class="label">变更状态：</span>
              <el-tag :type="changeStatusMap[selectedChange.status as ChangeStatus]">
                {{ changeStatusText[selectedChange.status as ChangeStatus] }}
              </el-tag>
            </div>
            <div class="detail-item">
              <span class="label">变更原因：</span>
              <span>{{ selectedChange.reason }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3>变更内容</h3>
          <div class="change-content">
            <pre>{{ selectedChange.content }}</pre>
          </div>
        </div>

        <div class="detail-section">
          <h3>变更流程</h3>
          <div class="process-flow">
            <div class="flow-item" :class="{ 'active': selectedChange.status !== 'in-progress' }">
              <div class="flow-icon">
                <el-icon><Check /></el-icon>
              </div>
              <div class="flow-content">
                <div class="flow-title">申请</div>
                <div class="flow-meta">
                  {{ selectedChange.applyUser }} | {{ selectedChange.applyTime }}
                </div>
              </div>
            </div>
            <div class="flow-line"></div>
            <div class="flow-item" :class="{ 'active': selectedChange.status === 'completed' }">
              <div class="flow-icon">
                <el-icon v-if="selectedChange.status === 'completed'">
                  <Check />
                </el-icon>
                <el-icon v-else>
                  <Clock />
                </el-icon>
              </div>
              <div class="flow-content">
                <div class="flow-title">审批</div>
                <div class="flow-meta">
                  {{ selectedChange.approveUser || '待审批' }} | {{ selectedChange.approveTime || '待审批' }}
                </div>
              </div>
            </div>
            <div class="flow-line" v-if="selectedChange.status === 'completed'"></div>
            <div class="flow-item" v-if="selectedChange.status === 'completed'">
              <div class="flow-icon">
                <el-icon><Check /></el-icon>
              </div>
              <div class="flow-content">
                <div class="flow-title">实施</div>
                <div class="flow-meta">
                  {{ selectedChange.implementTime }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  <!-- 发起/编辑工艺变更对话框 -->
  <el-dialog
    v-model="changeDialogVisible"
    :title="isEditMode ? '编辑工艺变更' : '发起工艺变更'"
    width="70%"
    :close-on-click-modal="false"
  >
    <el-form :model="changeForm" ref="changeFormRef" :rules="changeRules" label-position="top">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="变更标题" prop="title">
            <el-input v-model="changeForm.title" placeholder="请输入变更标题" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="变更编码" prop="code">
            <el-input v-model="changeForm.code" placeholder="请输入变更编码" :disabled="isEditMode" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="变更类型" prop="type">
            <el-select v-model="changeForm.type" placeholder="请选择变更类型">
              <el-option label="工艺路线变更" value="工艺路线变更" />
              <el-option label="工艺参数变更" value="工艺参数变更" />
              <el-option label="工艺文件变更" value="工艺文件变更" />
              <el-option label="检验标准变更" value="检验标准变更" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="关联工艺" prop="processCode">
            <el-select v-model="changeForm.processCode" placeholder="请选择关联工艺">
              <el-option
                v-for="route in processRoutes"
                :key="route.code"
                :label="route.name + ' (' + route.code + ')'"
                :value="route.code"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="变更原因" prop="reason">
            <el-input v-model="changeForm.reason" placeholder="请输入变更原因" type="textarea" rows="3" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="变更内容" prop="content">
            <el-input v-model="changeForm.content" placeholder="请输入变更内容" type="textarea" rows="5" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancelChangeDialog">取消</el-button>
        <el-button type="primary" @click="submitChangeForm" :loading="changeFormLoading">
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { Plus, Search, View, Edit, Check, Clock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useProcessCollaborationStore } from '../../../stores/plm/processCollaboration'

const store = useProcessCollaborationStore()

// 搜索和筛选
const searchKeyword = ref('')
const statusFilter = ref('all')

// 详情对话框
const detailDialogVisible = ref(false)
const selectedChange = ref<any>(null)

// 变更对话框
const changeDialogVisible = ref(false)
const isEditMode = ref(false)
const changeFormLoading = ref(false)
const changeFormRef = ref<any>(null)

// 工艺变更表单
const changeForm = ref({
  id: '',
  code: '',
  title: '',
  type: '工艺路线变更',
  processCode: '',
  processName: '',
  reason: '',
  content: '',
  status: 'in-progress',
  applyUser: '当前用户',
  applyTime: new Date().toISOString().split('T')[0] as string,
  approveUser: '',
  approveTime: '',
  implementTime: ''
})

// 表单验证规则
const changeRules = reactive({
  title: [
    { required: true, message: '请输入变更标题', trigger: 'blur' },
    { min: 5, max: 100, message: '变更标题长度在 5 到 100 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入变更编码', trigger: 'blur' },
    { min: 2, max: 20, message: '变更编码长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择变更类型', trigger: 'change' }
  ],
  processCode: [
    { required: true, message: '请选择关联工艺', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入变更原因', trigger: 'blur' },
    { min: 10, max: 500, message: '变更原因长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入变更内容', trigger: 'blur' },
    { min: 20, max: 1000, message: '变更内容长度在 20 到 1000 个字符', trigger: 'blur' }
  ]
})

// 工艺路线列表（用于关联工艺选择）
const processRoutes = computed(() => store.processRoutes)

// 工艺变更状态类型
type ChangeStatus = 'in-progress' | 'completed'

// 工艺变更状态映射
const changeStatusMap: Record<ChangeStatus, string> = {
  'in-progress': 'warning',
  'completed': 'success'
}

const changeStatusText: Record<ChangeStatus, string> = {
  'in-progress': '进行中',
  'completed': '已完成'
}

// 分页信息
const pagination = computed(() => ({
  page: store.pagination.page,
  pageSize: store.pagination.pageSize
}))

// 工艺变更列表
const processChanges = computed(() => store.processChanges)

// 筛选后的工艺变更列表
const filteredProcessChanges = computed(() => {
  return processChanges.value.filter(change => {
    const matchesKeyword = searchKeyword.value === '' || 
      change.title.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      change.code.toLowerCase().includes(searchKeyword.value.toLowerCase())
    const matchesStatus = statusFilter.value === 'all' || change.status === statusFilter.value
    return matchesKeyword && matchesStatus
  })
})

// 页面加载时获取数据
onMounted(async () => {
  await Promise.all([store.fetchProcessChanges(), store.fetchProcessRoutes()])
})

/**
 * 重新加载工艺变更列表（基于当前筛选/分页）。
 */
const reloadChanges = async () => {
  await store.fetchProcessChanges()
}

// 发起工艺变更
const createChange = () => {
  isEditMode.value = false
  resetChangeForm()
  changeDialogVisible.value = true
}

// 编辑工艺变更
const editChange = (change: any) => {
  isEditMode.value = true
  changeForm.value = { ...change }
  changeDialogVisible.value = true
}

// 重置变更表单
const resetChangeForm = () => {
  const currentDate = new Date().toISOString().split('T')[0] as string
  changeForm.value = {
    id: '',
    code: '',
    title: '',
    type: '工艺路线变更',
    processCode: '',
    processName: '',
    reason: '',
    content: '',
    status: 'in-progress',
    applyUser: '当前用户',
    applyTime: currentDate,
    approveUser: '',
    approveTime: '',
    implementTime: ''
  }
  if (changeFormRef.value) {
    changeFormRef.value.resetFields()
  }
}

// 取消变更对话框
const cancelChangeDialog = () => {
  changeDialogVisible.value = false
  if (changeFormRef.value) {
    changeFormRef.value.resetFields()
  }
}

// 提交变更表单
const submitChangeForm = async () => {
  if (!changeFormRef.value) return
  
  try {
    await changeFormRef.value.validate()
    changeFormLoading.value = true

    // 设置关联工艺名称
    const selectedRoute = processRoutes.value.find(r => r.code === changeForm.value.processCode)
    if (selectedRoute) {
      changeForm.value.processName = selectedRoute.name
    }
    
    // 确保applyTime有值
    const currentDate = new Date().toISOString().split('T')[0] as string
    
    if (isEditMode.value) {
      const updated = await store.updateProcessChange({
        ...changeForm.value,
        applyTime: changeForm.value.applyTime || currentDate
      })
      if (updated) ElMessage.success('工艺变更更新成功')
    } else {
      const created = await store.addProcessChange({
        ...changeForm.value,
        status: 'in-progress',
        applyUser: '当前用户',
        applyTime: currentDate,
        approveUser: '',
        approveTime: '',
        implementTime: ''
      })
      if (created) ElMessage.success('工艺变更发起成功')
    }
    
    changeDialogVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    changeFormLoading.value = false
  }
}

// 搜索处理
const handleSearch = async () => {
  store.setSearchKeyword(searchKeyword.value)
  store.setPagination(1, store.pagination.pageSize)
  await reloadChanges()
}

// 筛选处理
const handleFilter = async () => {
  store.setChangeStatusFilter(statusFilter.value)
  store.setPagination(1, store.pagination.pageSize)
  await reloadChanges()
}

// 选择工艺变更
const selectChange = (change: any) => {
  selectedChange.value = change
}

// 查看变更详情
const viewChangeDetail = (change: any) => {
  selectedChange.value = change
  detailDialogVisible.value = true
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  store.setPagination(1, size)
  reloadChanges()
}

// 页码变化
const handleCurrentChange = (current: number) => {
  store.setPagination(current, store.pagination.pageSize)
  reloadChanges()
}
</script>

<style scoped>
.process-change-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.process-change-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.process-change-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.search-filter {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.search-filter .el-input {
  width: 300px;
}

.search-filter .el-select {
  width: 150px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 变更详情样式 */
.change-detail {
  padding: 20px;
}

.detail-section {
  margin-bottom: 25px;
}

.detail-section h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #333;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 8px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.detail-item .label {
  font-weight: 500;
  margin-right: 10px;
  color: #666;
  width: 100px;
  flex-shrink: 0;
}

.change-content {
  background-color: #f9fafb;
  padding: 15px;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
}

.change-content pre {
  margin: 0;
  white-space: pre-wrap;
  font-family: inherit;
  color: #333;
}

/* 流程样式 */
.process-flow {
  display: flex;
  align-items: center;
  margin: 20px 0;
}

.flow-item {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.flow-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  margin-right: 10px;
  transition: all 0.3s ease;
}

.flow-item.active .flow-icon {
  background-color: #409eff;
  color: white;
}

.flow-content {
  flex: 1;
}

.flow-title {
  font-weight: 500;
  color: #333;
}

.flow-meta {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.flow-line {
  flex: 1;
  height: 2px;
  background-color: #e5e7eb;
  margin: 0 10px;
  transition: all 0.3s ease;
}

.flow-item.active + .flow-line {
  background-color: #409eff;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
