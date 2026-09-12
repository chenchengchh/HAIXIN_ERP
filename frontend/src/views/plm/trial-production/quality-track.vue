<template>
  <div class="quality-track-container">
    <div class="quality-track-header">
      <h2>质量跟踪管理</h2>
      <div class="header-actions">
        <el-button type="danger" @click="batchDelete" :disabled="selectedIssueIds.length === 0">
          <el-icon><Delete /></el-icon> 批量删除
        </el-button>
        <el-button type="primary" @click="createIssue">
          <el-icon><Plus /></el-icon> 记录质量问题
        </el-button>
      </div>
    </div>

    <div class="quality-track-content">
      <!-- 搜索和筛选 -->
      <div class="search-filter">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索问题描述或编码"
          prefix-icon="Search"
          clearable
          @input="handleSearch"
        />
        <el-select
          v-model="statusFilter"
          placeholder="问题状态"
          clearable
          @change="handleFilter"
        >
          <el-option label="所有状态" value="all" />
          <el-option label="进行中" value="in-progress" />
          <el-option label="已解决" value="resolved" />
        </el-select>
        <el-select
          v-model="severityFilter"
          placeholder="严重程度"
          clearable
          @change="handleFilter"
        >
          <el-option label="所有程度" value="all" />
          <el-option label="高" value="high" />
          <el-option label="中" value="medium" />
          <el-option label="低" value="low" />
        </el-select>
      </div>

      <!-- 质量问题列表 -->
      <el-table
        :data="filteredQualityTracks"
        style="width: 100%"
        @row-click="selectIssue"
        highlight-current-row
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="code" label="问题编码" width="150" />
        <el-table-column prop="issue" label="问题描述" min-width="300" />
        <el-table-column prop="productName" label="关联产品" min-width="180">
          <template #default="scope">
            <div>{{ scope.row.productName }} ({{ scope.row.productCode }})</div>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="产品版本" width="100" />
        <el-table-column prop="severity" label="严重程度" width="100">
          <template #default="scope">
            <el-tag :type="severityMap[scope.row.severity as Severity]">
              {{ severityText[scope.row.severity as Severity] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="statusMap[scope.row.status as IssueStatus]">
              {{ statusText[scope.row.status as IssueStatus] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发现时间" width="150" />
        <el-table-column prop="createUser" label="发现人" width="100" />
        <el-table-column label="解决人" width="100">
          <template #default="scope">
            <span v-if="scope.row.status === 'resolved'">{{ scope.row.resolveUser }}</span>
          </template>
        </el-table-column>
        <el-table-column label="解决时间" width="150">
          <template #default="scope">
            <span v-if="scope.row.status === 'resolved'">{{ scope.row.resolveTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click.stop="viewIssueDetail(scope.row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
            <el-button link type="primary" @click.stop="editIssue(scope.row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button link type="primary" @click.stop="resolveIssue(scope.row)" v-if="scope.row.status === 'in-progress'">
              <el-icon><Check /></el-icon> 标记解决
            </el-button>
            <el-button link type="danger" @click.stop="deleteIssue(scope.row)">
              <el-icon><Delete /></el-icon> 删除
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
        :total="qualityTracks.length"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 问题详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="质量问题详情"
      width="80%"
      :close-on-click-modal="false"
    >
      <div class="issue-detail" v-if="selectedIssue">
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">问题编码：</span>
              <span>{{ selectedIssue.code }}</span>
            </div>
            <div class="detail-item">
              <span class="label">关联报告：</span>
              <span>{{ selectedIssue.reportCode }}</span>
            </div>
            <div class="detail-item">
              <span class="label">关联产品：</span>
              <span>{{ selectedIssue.productName }} ({{ selectedIssue.productCode }})</span>
            </div>
            <div class="detail-item">
              <span class="label">产品版本：</span>
              <span>{{ selectedIssue.version }}</span>
            </div>
            <div class="detail-item">
              <span class="label">问题描述：</span>
              <span>{{ selectedIssue.issue }}</span>
            </div>
            <div class="detail-item">
              <span class="label">严重程度：</span>
              <el-tag :type="severityMap[selectedIssue.severity as Severity]">
                {{ severityText[selectedIssue.severity as Severity] }}
              </el-tag>
            </div>
            <div class="detail-item">
              <span class="label">问题状态：</span>
              <el-tag :type="statusMap[selectedIssue.status as IssueStatus]">
                {{ statusText[selectedIssue.status as IssueStatus] }}
              </el-tag>
            </div>
            <div class="detail-item">
              <span class="label">发现人：</span>
              <span>{{ selectedIssue.createUser }}</span>
            </div>
            <div class="detail-item">
              <span class="label">发现时间：</span>
              <span>{{ selectedIssue.createTime }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="selectedIssue.status === 'resolved'">
          <h3>解决方案</h3>
          <div class="resolution">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="解决人">
                {{ selectedIssue.resolveUser }}
              </el-descriptions-item>
              <el-descriptions-item label="解决时间">
                {{ selectedIssue.resolveTime }}
              </el-descriptions-item>
              <el-descriptions-item label="解决方法">
                {{ selectedIssue.resolution }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="resolveIssue(selectedIssue)" v-if="selectedIssue.status === 'in-progress'">
            标记为已解决
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新建/编辑质量问题对话框 -->
    <el-dialog
      v-model="issueDialogVisible"
      :title="isEditMode ? '编辑质量问题' : '记录质量问题'"
      width="70%"
      :close-on-click-modal="false"
    >
      <el-form :model="issueForm" ref="issueFormRef" :rules="issueRules" label-position="top">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="问题编码" prop="code">
              <el-input v-model="issueForm.code" placeholder="请输入问题编码" :disabled="isEditMode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联报告" prop="reportCode">
              <el-select v-model="issueForm.reportCode" placeholder="请选择关联报告">
                <el-option 
                  v-for="report in trialReports" 
                  :key="report.code" 
                  :label="report.planName + ' (' + report.code + ')'" 
                  :value="report.code" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联产品" prop="productName">
              <el-input v-model="issueForm.productName" placeholder="请输入关联产品" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品编码" prop="productCode">
              <el-input v-model="issueForm.productCode" placeholder="请输入产品编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品版本" prop="version">
              <el-input v-model="issueForm.version" placeholder="请输入产品版本" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="严重程度" prop="severity">
              <el-select v-model="issueForm.severity" placeholder="请选择严重程度">
                <el-option label="高" value="high" />
                <el-option label="中" value="medium" />
                <el-option label="低" value="low" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="问题描述" prop="issue">
              <el-input 
                v-model="issueForm.issue" 
                placeholder="请输入问题描述" 
                type="textarea" 
                rows="3" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="isEditMode && selectedIssue.status === 'in-progress'">
            <el-form-item label="解决方案" prop="resolution">
              <el-input 
                v-model="issueForm.resolution" 
                placeholder="请输入解决方案" 
                type="textarea" 
                rows="3" 
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelIssueDialog">取消</el-button>
          <el-button type="primary" @click="submitIssueForm" :loading="issueFormLoading">
            {{ isEditMode && selectedIssue.status === 'in-progress' ? '标记解决' : '确定' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { Plus, Search, View, Edit, Check, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTrialProductionStore } from '../../../stores/plm/trialProduction'

const store = useTrialProductionStore()

// 搜索和筛选
const searchKeyword = ref('')
const statusFilter = ref('all')
const severityFilter = ref('all')

// 详情对话框
const detailDialogVisible = ref(false)
const selectedIssue = ref<any>(null)

// 新建/编辑对话框
const issueDialogVisible = ref(false)
const isEditMode = ref(false)
const issueFormLoading = ref(false)
const issueFormRef = ref<any>(null)

// 选择的行ID
const selectedIssueIds = ref<string[]>([])

// 质量问题表单
const issueForm = ref({
  id: '',
  code: '',
  reportCode: '',
  productName: '',
  productCode: '',
  version: 'V1.0',
  issue: '',
  severity: 'medium',
  resolution: ''
})

// 表单验证规则
const issueRules = reactive({
  code: [
    { required: true, message: '请输入问题编码', trigger: 'blur' },
    { min: 2, max: 20, message: '问题编码长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  reportCode: [
    { required: true, message: '请选择关联报告', trigger: 'change' }
  ],
  productName: [
    { required: true, message: '请输入关联产品', trigger: 'blur' }
  ],
  productCode: [
    { required: true, message: '请输入产品编码', trigger: 'blur' }
  ],
  issue: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 5, max: 500, message: '问题描述长度在 5 到 500 个字符', trigger: 'blur' }
  ],
  severity: [
    { required: true, message: '请选择严重程度', trigger: 'change' }
  ],
  resolution: [
    { required: true, message: '请输入解决方案', trigger: 'blur' },
    { min: 5, max: 500, message: '解决方案长度在 5 到 500 个字符', trigger: 'blur' }
  ]
})

// 问题严重程度类型
type Severity = 'high' | 'medium' | 'low'

// 问题状态类型
type IssueStatus = 'in-progress' | 'resolved'

// 问题严重程度映射
const severityMap: Record<Severity, string> = {
  high: 'danger',
  medium: 'warning',
  low: 'info'
}

const severityText: Record<Severity, string> = {
  high: '高',
  medium: '中',
  low: '低'
}

// 问题状态映射
const statusMap: Record<IssueStatus, string> = {
  'in-progress': 'warning',
  resolved: 'success'
}

const statusText: Record<IssueStatus, string> = {
  'in-progress': '进行中',
  resolved: '已解决'
}

// 分页信息
const pagination = computed(() => ({
  page: store.pagination.page,
  pageSize: store.pagination.pageSize
}))

// 质量跟踪列表
const qualityTracks = computed(() => store.qualityTracks)

// 试产报告列表（用于关联选择）
const trialReports = computed(() => store.trialReports)

// 筛选后的质量跟踪列表
const filteredQualityTracks = computed(() => {
  return qualityTracks.value.filter(track => {
    const matchesKeyword = searchKeyword.value === '' || 
      track.issue.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      track.code.toLowerCase().includes(searchKeyword.value.toLowerCase())
    const matchesStatus = statusFilter.value === 'all' || track.status === statusFilter.value
    const matchesSeverity = severityFilter.value === 'all' || track.severity === severityFilter.value
    return matchesKeyword && matchesStatus && matchesSeverity
  })
})

// 页面加载时获取质量跟踪数据
onMounted(async () => {
  await Promise.all([
    store.fetchQualityTracks(),
    store.fetchTrialReports()
  ])
})

// 记录质量问题
const createIssue = () => {
  isEditMode.value = false
  resetIssueForm()
  issueDialogVisible.value = true
}

// 编辑质量问题
const editIssue = (issue: any) => {
  isEditMode.value = true
  selectedIssue.value = issue
  issueForm.value = {
    ...issue,
    resolution: issue.resolution || ''
  }
  issueDialogVisible.value = true
}

// 重置问题表单
const resetIssueForm = () => {
  issueForm.value = {
    id: '',
    code: '',
    reportCode: '',
    productName: '',
    productCode: '',
    version: 'V1.0',
    issue: '',
    severity: 'medium',
    resolution: ''
  }
  if (issueFormRef.value) {
    issueFormRef.value.resetFields()
  }
}

// 取消问题对话框
const cancelIssueDialog = () => {
  issueDialogVisible.value = false
  if (issueFormRef.value) {
    issueFormRef.value.resetFields()
  }
}

// 提交问题表单
const submitIssueForm = async () => {
  if (!issueFormRef.value) return
  
  try {
    await issueFormRef.value.validate()
    issueFormLoading.value = true

    // 获取关联报告信息
    const selectedReport = trialReports.value.find(r => r.code === issueForm.value.reportCode)
    
    const completeIssue = {
      ...issueForm.value,
      productName: issueForm.value.productName,
      productCode: issueForm.value.productCode,
      version: issueForm.value.version
    }
    
    if (isEditMode.value) {
      if (selectedIssue.value.status === 'in-progress' && issueForm.value.resolution) {
        // 标记问题为已解决
        const ok = await store.resolveQualityIssue(selectedIssue.value.id, issueForm.value.resolution)
        if (!ok) {
          ElMessage.error('质量问题解决失败')
          return
        }
        ElMessage.success('质量问题已解决')
      } else {
        // 更新质量问题
        const updated = await store.updateQualityIssue(completeIssue)
        if (!updated) {
          ElMessage.error('质量问题更新失败')
          return
        }
        ElMessage.success('质量问题更新成功')
      }
    } else {
      // 新建质量问题
      const created = await store.addQualityIssue(completeIssue)
      if (!created) {
        ElMessage.error('质量问题创建失败')
        return
      }
      ElMessage.success('质量问题创建成功')
    }
    
    issueDialogVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    issueFormLoading.value = false
  }
}

// 搜索处理
const handleSearch = async () => {
  store.setSearchKeyword(searchKeyword.value)
  store.setPagination(1, store.pagination.pageSize)
  await store.fetchQualityTracks()
}

// 筛选处理
const handleFilter = async () => {
  store.setStatusFilter(statusFilter.value)
  store.setPagination(1, store.pagination.pageSize)
  await store.fetchQualityTracks()
}

// 选择质量问题
const selectIssue = (issue: any) => {
  selectedIssue.value = issue
}

// 查看问题详情
const viewIssueDetail = (issue: any) => {
  selectedIssue.value = issue
  detailDialogVisible.value = true
}

// 标记问题已解决
const resolveIssue = (issue: any) => {
  selectedIssue.value = issue
  isEditMode.value = true
  issueForm.value = {
    ...issue,
    resolution: ''
  }
  issueDialogVisible.value = true
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  store.setPagination(1, size)
  store.fetchQualityTracks()
}

// 页码变化
const handleCurrentChange = (current: number) => {
  store.setPagination(current, store.pagination.pageSize)
  store.fetchQualityTracks()
}

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedIssueIds.value = selection.map(item => item.id)
}

// 删除质量问题
const deleteIssue = (issue: any) => {
  ElMessageBox.confirm('确定要删除该质量问题吗？此操作不可恢复。', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const ok = await store.deleteQualityIssue(issue.id)
    if (!ok) {
      ElMessage.error('质量问题删除失败')
      return
    }
    ElMessage.success('质量问题删除成功')
  }).catch(() => {
    // 用户取消删除操作
  })
}

// 批量删除质量问题
const batchDelete = () => {
  if (selectedIssueIds.value.length === 0) {
    ElMessage.warning('请选择要删除的质量问题')
    return
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIssueIds.value.length} 个质量问题吗？此操作不可恢复。`, '批量删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const results = await Promise.all(selectedIssueIds.value.map(id => store.deleteQualityIssue(String(id))))
    selectedIssueIds.value = []
    if (results.every(Boolean)) {
      ElMessage.success('批量删除成功')
    } else {
      ElMessage.error('批量删除部分失败')
    }
  }).catch(() => {
    // 用户取消删除操作
  })
}
</script>

<style scoped>
.quality-track-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.quality-track-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.quality-track-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 12px;
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

/* 问题详情样式 */
.issue-detail {
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
  width: 120px;
  flex-shrink: 0;
}

.resolution {
  margin-top: 15px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
