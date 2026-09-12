<template>
  <div class="surveys-view">
    <!-- 页面操作栏 -->
    <div class="page-actions">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索调查内容、客户ID"
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
        <el-icon><Plus /></el-icon> 新建调查
      </el-button>
      <el-button @click="handleExport">
        <el-icon><Download /></el-icon> 导出
      </el-button>
    </div>

    <!-- 调查列表 -->
    <el-card shadow="never" class="surveys-table-card">
      <el-table
        v-loading="loading"
        :data="surveysList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="surveyType" label="调查类型" width="130">
          <template #default="scope">
            {{ getSurveyTypeLabel(scope.row.surveyType) }}
          </template>
        </el-table-column>
        <el-table-column prop="customerId" label="客户ID" width="80" sortable />
        <el-table-column prop="customerName" label="客户名称" min-width="150" />
        <el-table-column prop="ticketId" label="工单ID" width="90" sortable />
        <el-table-column prop="score" label="评分" width="180" align="right">
          <template #default="scope">
            <div class="score-display">
              {{ scope.row.score }}
              <el-rate
                v-model="scope.row.score"
                disabled
                :max="5"
                score-template="{value}"
                style="margin-left: 10px;"
              />
            </div>
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

    <!-- 新建满意度调查对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="新建满意度调查"
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
        <el-form-item label="工单ID" prop="ticketId">
          <el-input-number v-model="surveyForm.ticketId" :min="1" placeholder="关联工单ID（可选）" style="width: 100%" />
        </el-form-item>
        <el-form-item label="评分" prop="score">
          <el-rate v-model="surveyForm.score" :max="5" show-score score-template="{value}分" />
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

    <!-- 查看满意度调查对话框 -->
    <el-dialog v-model="viewDialogVisible" title="满意度调查详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="调查类型">{{ getSurveyTypeLabel(viewSurvey.surveyType) }}</el-descriptions-item>
        <el-descriptions-item label="客户ID">{{ viewSurvey.customerId }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ viewSurvey.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="工单ID">{{ viewSurvey.ticketId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="评分">{{ viewSurvey.score }}分</el-descriptions-item>
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
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { serviceApi } from '../../../api/crm/service'
import { unwrapPageResponse } from '../../../api'

// 满意度调查列表数据
const surveysList = ref<any[]>([])

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

// 新建对话框状态
const createDialogVisible = ref(false)
const surveyFormRef = ref()
const surveyForm = reactive<any>({
  customerId: undefined,
  customerName: '',
  ticketId: undefined,
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

/**
 * 获取调查类型标签（后端调查类型默认 SATISFACTION）
 * @param type 调查类型编码
 * @returns 类型中文标签
 */
const getSurveyTypeLabel = (type: string) => {
  const labelMap: Record<string, string> = {
    SATISFACTION: '满意度调查',
    after_sale: '售后调查',
    periodic: '定期调查',
    nps: 'NPS调查'
  }
  return labelMap[type] || type
}

/**
 * 获取满意度调查列表（对接后端分页接口 GET /api/v1/crm/surveys/list）
 */
const fetchSurveysList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    const response = await serviceApi.getSurveyList(params)
    const { list, total } = unwrapPageResponse<any>(response)
    surveysList.value = list
    pagination.total = total
  } catch (error) {
    console.error('获取满意度调查列表失败:', error)
    ElMessage.error('获取满意度调查列表失败')
    surveysList.value = []
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
  fetchSurveysList()
}

/**
 * 处理新建：打开新建调查对话框并重置表单
 */
const handleCreate = () => {
  Object.assign(surveyForm, {
    customerId: undefined,
    customerName: '',
    ticketId: undefined,
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
 * 提交满意度调查（POST /api/v1/crm/surveys）
 */
const handleSaveSurvey = async () => {
  if (!surveyFormRef.value) return
  await surveyFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await serviceApi.createSatisfactionSurvey(surveyForm)
      ElMessage.success('满意度调查提交成功')
      createDialogVisible.value = false
      fetchSurveysList()
    } catch (error) {
      console.error('提交满意度调查失败:', error)
      ElMessage.error('提交满意度调查失败')
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
  fetchSurveysList()
}

/**
 * 处理页码变化
 * @param current 当前页码
 */
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchSurveysList()
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
  fetchSurveysList()
})
</script>

<style scoped>
.surveys-view {
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