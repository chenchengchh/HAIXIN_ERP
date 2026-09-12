<template>
  <div class="nps-surveys-view">
    <!-- 页面操作栏 -->
    <div class="page-actions">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索客户ID、原因"
        style="width: 300px; margin-right: 10px;"
        clearable
      >
        <template #append>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon> 新建NPS调查
      </el-button>
      <el-button @click="handleExport">
        <el-icon><Download /></el-icon> 导出
      </el-button>
    </div>

    <!-- NPS得分统计 -->
    <div class="nps-stats">
      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-label">NPS得分</div>
          <div class="stat-value">{{ npsScore }}</div>
          <div class="stat-desc">基于{{ npsTotal }}份有效调查</div>
        </div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-label">推荐者比例</div>
          <div class="stat-value">{{ promoterRatio }}%</div>
          <div class="stat-desc">{{ promoterCount }}人</div>
        </div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-label">中立者比例</div>
          <div class="stat-value">{{ passiveRatio }}%</div>
          <div class="stat-desc">{{ passiveCount }}人</div>
        </div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-label">贬低者比例</div>
          <div class="stat-value">{{ detractorRatio }}%</div>
          <div class="stat-desc">{{ detractorCount }}人</div>
        </div>
      </el-card>
    </div>

    <!-- 调查列表 -->
    <el-card shadow="never" class="surveys-table-card">
      <el-table
        v-loading="loading"
        :data="npsSurveysList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="customerId" label="客户ID" width="80" sortable />
        <el-table-column prop="customerName" label="客户名称" min-width="150" />
        <el-table-column prop="score" label="评分" width="180" align="right">
          <template #default="scope">
            <div class="score-display">
              {{ scope.row.score }}
              <el-rate
                v-model="scope.row.score"
                disabled
                :max="10"
                score-template="{value}"
                style="margin-left: 10px;"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="customerType" label="客户类型" width="120">
          <template #default="scope">
            <el-tag :type="getCustomerTypeTagType(getCustomerType(scope.row.score))">
              {{ getCustomerTypeLabel(getCustomerType(scope.row.score)) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="feedback" label="反馈意见" min-width="250" />
        <el-table-column prop="createTime" label="调查时间" width="160" sortable />
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
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

    <!-- 新建NPS调查对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="新建NPS调查"
      width="600px"
      @close="handleCreateDialogClose"
    >
      <el-form :model="surveyForm" :rules="surveyRules" ref="surveyFormRef" label-width="100px">
        <el-form-item label="客户ID" prop="customerId">
          <el-input-number v-model="surveyForm.customerId" :min="1" placeholder="请输入客户ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="surveyForm.customerName" placeholder="请输入客户名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="评分" prop="score">
          <el-rate v-model="surveyForm.score" :max="10" show-score score-template="{value}分" />
        </el-form-item>
        <el-form-item label="反馈意见" prop="feedback">
          <el-input v-model="surveyForm.feedback" type="textarea" :rows="4" placeholder="请输入反馈意见" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSaveSurvey">提交</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看NPS调查对话框 -->
    <el-dialog v-model="viewDialogVisible" title="NPS调查详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="客户ID">{{ viewSurvey.customerId }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ viewSurvey.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="评分">{{ viewSurvey.score }}分</el-descriptions-item>
        <el-descriptions-item label="客户类型">
          <el-tag :type="getCustomerTypeTagType(getCustomerType(viewSurvey.score))">
            {{ getCustomerTypeLabel(getCustomerType(viewSurvey.score)) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="反馈意见">{{ viewSurvey.feedback || '-' }}</el-descriptions-item>
        <el-descriptions-item label="调查时间">{{ viewSurvey.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, Plus, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { serviceApi } from '../../../api/crm/service'
import { unwrapPageResponse, unwrapResponseData } from '../../../api'

// NPS调查列表数据
const npsSurveysList = ref<any[]>([])

// 加载状态
const loading = ref(false)

// 提交加载状态
const submitLoading = ref(false)

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// NPS统计数据（对接后端 GET /api/v1/crm/nps/score）
const npsStats = reactive({
  npsScore: 0,
  totalCount: 0,
  promoters: 0,
  passives: 0,
  detractors: 0
})

// 新建对话框状态
const createDialogVisible = ref(false)
const surveyFormRef = ref()
const surveyForm = reactive<any>({
  customerId: undefined,
  customerName: '',
  score: 0,
  feedback: ''
})

// 查看对话框状态
const viewDialogVisible = ref(false)
const viewSurvey = ref<any>({})

// 表单校验规则
const surveyRules = {
  customerId: [{ required: true, message: '请输入客户ID', trigger: 'blur' }],
  score: [{ required: true, message: '请选择评分', trigger: 'change' }]
}

// 计算NPS统计展示数据
const promoterCount = computed(() => npsStats.promoters)
const passiveCount = computed(() => npsStats.passives)
const detractorCount = computed(() => npsStats.detractors)
const npsTotal = computed(() => npsStats.totalCount)

const promoterRatio = computed(() => {
  if (npsTotal.value === 0) return 0
  return ((promoterCount.value / npsTotal.value) * 100).toFixed(1)
})

const passiveRatio = computed(() => {
  if (npsTotal.value === 0) return 0
  return ((passiveCount.value / npsTotal.value) * 100).toFixed(1)
})

const detractorRatio = computed(() => {
  if (npsTotal.value === 0) return 0
  return ((detractorCount.value / npsTotal.value) * 100).toFixed(1)
})

const npsScore = computed(() => npsStats.npsScore)

/**
 * 根据评分推导客户类型（NPS标准：9-10推荐者，7-8中立者，0-6贬损者）
 * @param score 评分
 * @returns 客户类型编码
 */
const getCustomerType = (score: number) => {
  if (score >= 9) return 'promoter'
  if (score >= 7) return 'passive'
  return 'detractor'
}

/**
 * 获取客户类型标签
 * @param type 客户类型编码
 * @returns 类型中文标签
 */
const getCustomerTypeLabel = (type: string) => {
  const labelMap: Record<string, string> = {
    promoter: '推荐者',
    passive: '中立者',
    detractor: '贬低者'
  }
  return labelMap[type] || type
}

/**
 * 获取客户类型标签样式
 * @param type 客户类型编码
 * @returns 标签类型
 */
const getCustomerTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    promoter: 'success',
    passive: 'warning',
    detractor: 'danger'
  }
  return typeMap[type] || ''
}

/**
 * 获取NPS统计数据（对接后端 GET /api/v1/crm/nps/score）
 */
const fetchNpsScore = async () => {
  try {
    const response = await serviceApi.getNPSScore()
    const data = unwrapResponseData<any>(response)
    if (data) {
      npsStats.npsScore = data.npsScore ?? 0
      npsStats.totalCount = data.totalCount ?? 0
      npsStats.promoters = data.promoters ?? 0
      npsStats.passives = data.passives ?? 0
      npsStats.detractors = data.detractors ?? 0
    }
  } catch (error) {
    console.error('获取NPS得分失败:', error)
  }
}

/**
 * 获取NPS调查列表（对接后端分页接口 GET /api/v1/crm/nps/list）
 */
const fetchNpsSurveysList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    const response = await serviceApi.getNpsSurveyList(params)
    const { list, total } = unwrapPageResponse<any>(response)
    npsSurveysList.value = list
    pagination.total = total
  } catch (error) {
    console.error('获取NPS调查列表失败:', error)
    ElMessage.error('获取NPS调查列表失败')
    npsSurveysList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

/**
 * 处理搜索：重置页码并重新加载列表
 */
const handleSearch = () => {
  pagination.currentPage = 1
  fetchNpsSurveysList()
}

/**
 * 处理新建：打开新建调查对话框并重置表单
 */
const handleCreate = () => {
  Object.assign(surveyForm, {
    customerId: undefined,
    customerName: '',
    score: 0,
    feedback: ''
  })
  createDialogVisible.value = true
}

/**
 * 处理新建对话框关闭：重置表单校验状态
 */
const handleCreateDialogClose = () => {
  if (surveyFormRef.value) {
    surveyFormRef.value.resetFields()
  }
}

/**
 * 提交NPS调查（POST /api/v1/crm/nps）
 */
const handleSaveSurvey = async () => {
  if (!surveyFormRef.value) return
  await surveyFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await serviceApi.submitNPSSurvey(surveyForm)
      ElMessage.success('NPS调查提交成功')
      createDialogVisible.value = false
      fetchNpsSurveysList()
      fetchNpsScore()
    } catch (error) {
      console.error('提交NPS调查失败:', error)
      ElMessage.error('提交NPS调查失败')
    } finally {
      submitLoading.value = false
    }
  })
}

/**
 * 处理导出
 */
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

/**
 * 处理查看：打开调查详情对话框
 * @param row 调查行数据
 */
const handleView = (row: any) => {
  viewSurvey.value = row
  viewDialogVisible.value = true
}

/**
 * 处理分页大小变化
 * @param size 每页数量
 */
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchNpsSurveysList()
}

/**
 * 处理页码变化
 * @param current 当前页码
 */
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchNpsSurveysList()
}

/**
 * 处理选择变化
 * @param selection 选中的行
 */
const handleSelectionChange = (selection: any[]) => {
  console.log('选中的行:', selection)
}

// 组件挂载时初始化
onMounted(() => {
  fetchNpsSurveysList()
  fetchNpsScore()
})
</script>

<style scoped>
.nps-surveys-view {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-actions {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.nps-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 20px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 200px;
}

.stat-content {
  text-align: center;
}

.stat-label {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-desc {
  font-size: 14px;
  color: #909399;
}

.surveys-table-card {
  margin-top: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.score-display {
  display: flex;
  align-items: center;
}
</style>