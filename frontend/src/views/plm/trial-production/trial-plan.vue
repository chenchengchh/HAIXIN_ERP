<template>
  <div class="trial-plan-container">
    <div class="trial-plan-header">
      <h2>试产计划管理</h2>
      <div class="header-actions">
        <el-button type="danger" @click="batchDelete" :disabled="selectedPlanIds.length === 0">
          <el-icon><Delete /></el-icon> 批量删除
        </el-button>
        <el-button type="primary" @click="createPlan">
          <el-icon><Plus /></el-icon> 新建试产计划
        </el-button>
      </div>
    </div>

    <div class="trial-plan-content">
      <!-- 搜索和筛选 -->
      <div class="search-filter">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索计划名称或编码"
          prefix-icon="Search"
          clearable
          @input="handleSearch"
        />
        <el-select
          v-model="statusFilter"
          placeholder="计划状态"
          clearable
          @change="handleFilter"
        >
          <el-option label="所有状态" value="all" />
          <el-option label="待执行" value="pending" />
          <el-option label="进行中" value="in-progress" />
          <el-option label="已完成" value="completed" />
        </el-select>
      </div>

      <!-- 试产计划列表 -->
      <el-table
        :data="filteredTrialPlans"
        style="width: 100%"
        @row-click="selectPlan"
        highlight-current-row
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="code" label="计划编码" width="150" />
        <el-table-column prop="name" label="计划名称" min-width="250" />
        <el-table-column prop="productName" label="试产产品" min-width="180">
          <template #default="scope">
            <div>{{ scope.row.productName }} ({{ scope.row.productCode }})</div>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="产品版本" width="100" />
        <el-table-column prop="trialType" label="试产类型" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.trialType === '小批量' ? 'info' : 'success'">
              {{ scope.row.trialType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="trialQty" label="试产数量" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="planStatusMap[scope.row.status as PlanStatus]">
              {{ planStatusText[scope.row.status as PlanStatus] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="150" />
        <el-table-column prop="endTime" label="结束时间" width="150" />
        <el-table-column prop="responsiblePerson" label="负责人" width="100" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click.stop="viewPlanDetail(scope.row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
            <el-button link type="primary" @click.stop="editPlan(scope.row)" :disabled="scope.row.status === 'completed'">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button link type="primary" @click.stop="startPlan(scope.row)" v-if="scope.row.status === 'pending'">
              开始
            </el-button>
            <el-button link type="primary" @click.stop="completePlan(scope.row)" v-if="scope.row.status === 'in-progress'">
              <el-icon><Check /></el-icon> 完成
            </el-button>
            <el-button link type="danger" @click.stop="deletePlan(scope.row)">
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
        :total="trialPlans.length"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 计划详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="试产计划详情"
      width="80%"
      :close-on-click-modal="false"
    >
      <div class="plan-detail" v-if="selectedPlan">
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">计划编码：</span>
              <span>{{ selectedPlan.code }}</span>
            </div>
            <div class="detail-item">
              <span class="label">计划名称：</span>
              <span>{{ selectedPlan.name }}</span>
            </div>
            <div class="detail-item">
              <span class="label">试产产品：</span>
              <span>{{ selectedPlan.productName }} ({{ selectedPlan.productCode }})</span>
            </div>
            <div class="detail-item">
              <span class="label">产品版本：</span>
              <span>{{ selectedPlan.version }}</span>
            </div>
            <div class="detail-item">
              <span class="label">试产类型：</span>
              <el-tag :type="selectedPlan.trialType === '小批量' ? 'info' : 'success'">
                {{ selectedPlan.trialType }}
              </el-tag>
            </div>
            <div class="detail-item">
              <span class="label">试产数量：</span>
              <span>{{ selectedPlan.trialQty }} 台</span>
            </div>
            <div class="detail-item">
              <span class="label">计划状态：</span>
              <el-tag :type="planStatusMap[selectedPlan.status as PlanStatus]">
                {{ planStatusText[selectedPlan.status as PlanStatus] }}
              </el-tag>
            </div>
            <div class="detail-item">
              <span class="label">负责人：</span>
              <span>{{ selectedPlan.responsiblePerson }}</span>
            </div>
            <div class="detail-item">
              <span class="label">开始时间：</span>
              <span>{{ selectedPlan.startTime }}</span>
            </div>
            <div class="detail-item">
              <span class="label">结束时间：</span>
              <span>{{ selectedPlan.endTime }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3>参与部门</h3>
          <div class="departments">
            <el-tag v-for="dept in selectedPlan.departments" :key="dept" size="small" type="info" style="margin-right: 8px; margin-bottom: 8px;">
              {{ dept }}
            </el-tag>
          </div>
        </div>

        <div class="detail-section">
          <h3>试产阶段</h3>
          <div class="stages">
            <el-timeline>
              <el-timeline-item
                v-for="(stage, index) in selectedPlan.stages"
                :key="stage.id"
                :timestamp="stage.startTime + ' 至 ' + stage.endTime"
                :type="stage.status === 'completed' ? 'success' : stage.status === 'in-progress' ? 'warning' : 'info'"
              >
                <div class="stage-item">
                  <div class="stage-name">{{ stage.name }}</div>
                  <div class="stage-status">
                    <el-tag :type="stage.status === 'completed' ? 'success' : stage.status === 'in-progress' ? 'warning' : 'info'">
                      {{ stage.status === 'completed' ? '已完成' : stage.status === 'in-progress' ? '进行中' : '待执行' }}
                    </el-tag>
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="generateReport" v-if="selectedPlan.status === 'completed'">
            生成试产报告
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新建/编辑试产计划对话框 -->
    <el-dialog
      v-model="planDialogVisible"
      :title="isEditMode ? '编辑试产计划' : '新建试产计划'"
      width="70%"
      :close-on-click-modal="false"
    >
      <el-form :model="planForm" ref="planFormRef" :rules="planRules" label-position="top">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划名称" prop="name">
              <el-input v-model="planForm.name" placeholder="请输入计划名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划编码" prop="code">
              <el-input v-model="planForm.code" placeholder="请输入计划编码" :disabled="isEditMode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="试产产品" prop="productName">
              <el-input v-model="planForm.productName" placeholder="请输入试产产品" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品编码" prop="productCode">
              <el-input v-model="planForm.productCode" placeholder="请输入产品编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品版本" prop="version">
              <el-input v-model="planForm.version" placeholder="请输入产品版本" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="试产类型" prop="trialType">
              <el-select v-model="planForm.trialType" placeholder="请选择试产类型">
                <el-option label="小批量" value="小批量" />
                <el-option label="中批量" value="中批量" />
                <el-option label="大批量" value="大批量" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="试产数量" prop="trialQty">
              <el-input-number v-model="planForm.trialQty" :min="1" placeholder="请输入试产数量" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="responsiblePerson">
              <el-input v-model="planForm.responsiblePerson" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker v-model="planForm.startTime" type="date" placeholder="选择开始时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker v-model="planForm.endTime" type="date" placeholder="选择结束时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="参与部门" prop="departments">
              <el-select v-model="planForm.departments" placeholder="请选择参与部门" multiple>
                <el-option label="研发部" value="研发部" />
                <el-option label="生产部" value="生产部" />
                <el-option label="质量部" value="质量部" />
                <el-option label="采购部" value="采购部" />
                <el-option label="财务部" value="财务部" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelPlanDialog">取消</el-button>
          <el-button type="primary" @click="submitPlanForm" :loading="planFormLoading">
            确定
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
import { useRouter } from 'vue-router'
import { useTrialProductionStore } from '../../../stores/plm/trialProduction'

const store = useTrialProductionStore()
const router = useRouter()

// 搜索和筛选
const searchKeyword = ref('')
const statusFilter = ref('all')

// 详情对话框
const detailDialogVisible = ref(false)
const selectedPlan = ref<any>(null)

// 新建/编辑对话框
const planDialogVisible = ref(false)
const isEditMode = ref(false)
const planFormLoading = ref(false)
const planFormRef = ref<any>(null)

// 选择的行ID
const selectedPlanIds = ref<string[]>([])

// 试产计划表单
const planForm = ref({
  id: '',
  name: '',
  code: '',
  productName: '',
  productCode: '',
  version: 'V1.0',
  trialType: '小批量',
  trialQty: 1000,
  responsiblePerson: '',
  startTime: '',
  endTime: '',
  departments: [] as string[],
  stages: [] as Array<{
    id: string;
    name: string;
    status: 'pending' | 'in-progress' | 'completed';
    startTime: string;
    endTime: string;
  }>
})

// 表单验证规则
const planRules = reactive({
  name: [
    { required: true, message: '请输入计划名称', trigger: 'blur' },
    { min: 5, max: 100, message: '计划名称长度在 5 到 100 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入计划编码', trigger: 'blur' },
    { min: 2, max: 20, message: '计划编码长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  productName: [
    { required: true, message: '请输入试产产品', trigger: 'blur' }
  ],
  productCode: [
    { required: true, message: '请输入产品编码', trigger: 'blur' }
  ],
  trialType: [
    { required: true, message: '请选择试产类型', trigger: 'change' }
  ],
  trialQty: [
    { required: true, message: '请输入试产数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '试产数量必须大于0', trigger: 'blur' }
  ],
  responsiblePerson: [
    { required: true, message: '请输入负责人', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  departments: [
    { required: true, message: '请选择参与部门', trigger: 'change' }
  ]
})

// 试产计划状态类型
type PlanStatus = 'pending' | 'in-progress' | 'completed'

// 试产计划状态映射
const planStatusMap: Record<PlanStatus, string> = {
  pending: 'info',
  'in-progress': 'warning',
  completed: 'success'
}

const planStatusText: Record<PlanStatus, string> = {
  pending: '待执行',
  'in-progress': '进行中',
  completed: '已完成'
}

// 分页信息
const pagination = computed(() => ({
  page: store.pagination.page,
  pageSize: store.pagination.pageSize
}))

// 试产计划列表
const trialPlans = computed(() => store.trialPlans)

// 筛选后的试产计划列表
const filteredTrialPlans = computed(() => {
  return trialPlans.value.filter(plan => {
    const matchesKeyword = searchKeyword.value === '' || 
      plan.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      plan.code.toLowerCase().includes(searchKeyword.value.toLowerCase())
    const matchesStatus = statusFilter.value === 'all' || plan.status === statusFilter.value
    return matchesKeyword && matchesStatus
  })
})

// 页面加载时获取试产计划数据
onMounted(async () => {
  await store.fetchTrialPlans()
})

// 新建试产计划
const createPlan = () => {
  isEditMode.value = false
  resetPlanForm()
  planDialogVisible.value = true
}

// 编辑试产计划
const editPlan = (plan: any) => {
  isEditMode.value = true
  planForm.value = { ...plan }
  planDialogVisible.value = true
}

// 重置计划表单
const resetPlanForm = () => {
  planForm.value = {
    id: '',
    name: '',
    code: '',
    productName: '',
    productCode: '',
    version: 'V1.0',
    trialType: '小批量',
    trialQty: 1000,
    responsiblePerson: '',
    startTime: '',
    endTime: '',
    departments: [],
    stages: [] as Array<{
      id: string;
      name: string;
      status: 'pending' | 'in-progress' | 'completed';
      startTime: string;
      endTime: string;
    }>
  }
  if (planFormRef.value) {
    planFormRef.value.resetFields()
  }
}

// 取消计划对话框
const cancelPlanDialog = () => {
  planDialogVisible.value = false
  if (planFormRef.value) {
    planFormRef.value.resetFields()
  }
}

// 提交计划表单
const submitPlanForm = async () => {
  if (!planFormRef.value) return
  
  try {
    await planFormRef.value.validate()
    planFormLoading.value = true
    
    // 设置阶段时间
    const stages = [
      {
        id: '1',
        name: '试产前准备',
        status: 'pending',
        startTime: planForm.value.startTime,
        endTime: planForm.value.startTime
      },
      {
        id: '2',
        name: '试产执行',
        status: 'pending',
        startTime: planForm.value.startTime,
        endTime: planForm.value.endTime
      },
      {
        id: '3',
        name: '试产总结',
        status: 'pending',
        startTime: planForm.value.endTime,
        endTime: planForm.value.endTime
      }
    ]
    
    const completePlan = {
      ...planForm.value,
      stages
    }
    
    if (isEditMode.value) {
      // 更新试产计划
      const updated = await store.updateTrialPlan(completePlan)
      if (!updated) {
        ElMessage.error('试产计划更新失败')
        return
      }
      ElMessage.success('试产计划更新成功')
    } else {
      // 新建试产计划
      const created = await store.addTrialPlan(completePlan)
      if (!created) {
        ElMessage.error('试产计划创建失败')
        return
      }
      ElMessage.success('试产计划创建成功')
    }
    
    planDialogVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    planFormLoading.value = false
  }
}

// 搜索处理
const handleSearch = async () => {
  store.setSearchKeyword(searchKeyword.value)
  store.setPagination(1, store.pagination.pageSize)
  await store.fetchTrialPlans()
}

// 筛选处理
const handleFilter = async () => {
  store.setStatusFilter(statusFilter.value)
  store.setPagination(1, store.pagination.pageSize)
  await store.fetchTrialPlans()
}

// 选择试产计划
const selectPlan = (plan: any) => {
  selectedPlan.value = plan
}

// 查看计划详情
const viewPlanDetail = (plan: any) => {
  selectedPlan.value = plan
  detailDialogVisible.value = true
}

// 开始计划
const startPlan = async (plan: any) => {
  const ok = await store.startTrialPlan(plan.id)
  if (!ok) {
    ElMessage.error('试产计划开始失败')
    return
  }
  ElMessage.success('试产计划已开始')
}

// 完成计划
const completePlan = async (plan: any) => {
  const ok = await store.completeTrialPlan(plan.id)
  if (!ok) {
    ElMessage.error('试产计划完成失败')
    return
  }
  ElMessage.success('试产计划已完成')
}

// 生成试产报告
const generateReport = async () => {
  if (!selectedPlan.value) {
    ElMessage.warning('请先选择一个试产计划')
    return
  }

  const currentDate = new Date().toISOString().split('T')[0] as string
  await store.addTrialReport({
    code: `RPT-${Date.now()}`,
    planCode: selectedPlan.value.code,
    planName: selectedPlan.value.name,
    status: 'draft',
    createTime: currentDate,
    createUser: '当前用户',
    mainIssues: [],
    improvements: []
  })
  router.push('/home/plm/trial-production/trial-report')
  ElMessage.success('已生成草稿报告')
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  store.setPagination(1, size)
  store.fetchTrialPlans()
}

// 页码变化
const handleCurrentChange = (current: number) => {
  store.setPagination(current, store.pagination.pageSize)
  store.fetchTrialPlans()
}

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedPlanIds.value = selection.map(item => item.id)
}

// 删除试产计划
const deletePlan = (plan: any) => {
  ElMessageBox.confirm('确定要删除该试产计划吗？此操作不可恢复。', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await store.deleteTrialPlan(plan.id)
    ElMessage.success('试产计划删除成功')
  }).catch(() => {
    // 用户取消删除操作
  })
}

// 批量删除试产计划
const batchDelete = () => {
  if (selectedPlanIds.value.length === 0) {
    ElMessage.warning('请选择要删除的试产计划')
    return
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedPlanIds.value.length} 个试产计划吗？此操作不可恢复。`, '批量删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await Promise.all(selectedPlanIds.value.map(id => store.deleteTrialPlan(String(id))))
    selectedPlanIds.value = []
    ElMessage.success('批量删除成功')
  }).catch(() => {
    // 用户取消删除操作
  })
}
</script>

<style scoped>
.trial-plan-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.trial-plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.trial-plan-header h2 {
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

/* 计划详情样式 */
.plan-detail {
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

.departments {
  display: flex;
  flex-wrap: wrap;
  margin-top: 10px;
}

.stages {
  margin-top: 15px;
}

.stage-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stage-name {
  font-weight: 500;
  color: #333;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
