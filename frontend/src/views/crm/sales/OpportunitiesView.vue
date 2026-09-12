<template>
  <div class="opportunities-view">
    <!-- 搜索和筛选区域 -->
    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商机名称">
          <el-input v-model="searchForm.opportunityName" placeholder="请输入商机名称" clearable />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="商机阶段">
          <el-select v-model="searchForm.stage" placeholder="请选择商机阶段" clearable>
            <el-option label="初始阶段" value="initial" />
            <el-option label="需求确认" value="需求确认" />
            <el-option label="方案报价" value="方案报价" />
            <el-option label="谈判" value="谈判" />
            <el-option label="成交" value="成交" />
            <el-option label="失败" value="失败" />
          </el-select>
        </el-form-item>
        <el-form-item label="商机状态">
          <el-select v-model="searchForm.status" placeholder="请选择商机状态" clearable>
            <el-option label="进行中" value="ongoing" />
            <el-option label="已赢单" value="won" />
            <el-option label="已输单" value="lost" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 商机列表区域 -->
    <el-card shadow="hover" class="list-card">
      <template #header>
        <div class="card-header">
          <span>商机列表</span>
          <el-button type="primary" @click="handleAddOpportunity">新增商机</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="opportunitiesList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="商机ID" width="80" />
        <el-table-column prop="opportunityNo" label="商机编号" width="150" />
        <el-table-column prop="opportunityName" label="商机名称" min-width="200" />
        <el-table-column prop="customerName" label="客户名称" width="150" />
        <el-table-column prop="stage" label="阶段" width="120">
          <template #default="scope">
            <el-tag :type="getStageType(scope.row.stage)">{{ scope.row.stage }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="winProbability" label="赢单概率" width="120">
          <template #default="scope">
            <el-progress :percentage="scope.row.winProbability" :stroke-width="10" :color="getProgressColor(scope.row.winProbability)"></el-progress>
          </template>
        </el-table-column>
        <el-table-column prop="estimatedAmount" label="预计金额" width="120">
          <template #default="scope">
            <span style="color: #67C23A; font-weight: bold">¥{{ DataTransformer.formatMoney(scope.row.estimatedAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="expectedCloseDate" label="预计成交日期" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'won' ? 'success' : scope.row.status === 'lost' ? 'danger' : 'info'">
              {{ scope.row.status === 'won' ? '已赢单' : scope.row.status === 'lost' ? '已输单' : '进行中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerId" label="负责人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleViewDetail(scope.row.id)">详情</el-button>
            <el-button size="small" @click="handleEditOpportunity(scope.row)">编辑</el-button>
            <el-button size="small" :type="scope.row.status === 'won' ? 'warning' : 'success'" @click="handleUpdateStage(scope.row)">推进</el-button>
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

    <!-- 新增/编辑商机对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditMode ? '编辑商机' : '新增商机'"
      width="700px"
    >
      <el-form :model="opportunityForm" :rules="opportunityFormRules" ref="opportunityFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商机名称" prop="opportunityName">
              <el-input v-model="opportunityForm.opportunityName" placeholder="请输入商机名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户ID" prop="customerId">
              <el-input v-model.number="opportunityForm.customerId" placeholder="请输入客户ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerName">
              <el-input v-model="opportunityForm.customerName" placeholder="请输入客户名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源线索ID" prop="leadId">
              <el-input v-model.number="opportunityForm.leadId" placeholder="请输入来源线索ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="当前阶段" prop="stage">
              <el-select v-model="opportunityForm.stage" placeholder="请选择当前阶段">
                <el-option label="初始阶段" value="initial" />
                <el-option label="需求确认" value="需求确认" />
                <el-option label="方案报价" value="方案报价" />
                <el-option label="谈判" value="谈判" />
                <el-option label="成交" value="成交" />
                <el-option label="失败" value="失败" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="赢单概率" prop="winProbability">
              <el-slider v-model="opportunityForm.winProbability" :min="0" :max="100" show-input />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预计金额" prop="estimatedAmount">
              <el-input-number v-model="opportunityForm.estimatedAmount" :min="0" placeholder="请输入预计金额" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计成交日期" prop="expectedCloseDate">
              <el-date-picker v-model="opportunityForm.expectedCloseDate" type="date" placeholder="请选择预计成交日期" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人ID" prop="ownerId">
              <el-input v-model.number="opportunityForm.ownerId" placeholder="请输入负责人ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商机状态" prop="status">
              <el-select v-model="opportunityForm.status" placeholder="请选择商机状态">
                <el-option label="进行中" value="ongoing" />
                <el-option label="已赢单" value="won" />
                <el-option label="已输单" value="lost" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="竞争对手" prop="competitors">
              <el-input v-model="opportunityForm.competitors" placeholder="请输入竞争对手" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="关联产品" prop="products">
              <el-input v-model="opportunityForm.products" type="textarea" placeholder="请输入关联产品（JSON格式）" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="失败原因" prop="lossReason">
              <el-input v-model="opportunityForm.lossReason" type="textarea" placeholder="请输入失败原因" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSaveOpportunity">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { OpportunityEntity } from '../../../types/crm/sales'
import { validationRules } from '../../../utils/validationRules'
import { salesApi } from '../../../api/crm/sales'
import { DataTransformer } from '../../../utils/data-transformer'

// 搜索表单
const searchForm = reactive({
  opportunityName: '',
  customerName: '',
  stage: '',
  status: ''
})

// 商机列表数据
const opportunitiesList = ref<OpportunityEntity[]>([])
const loading = ref(false)

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const isEditMode = ref(false)
const opportunityFormRef = ref()

// 商机表单
const opportunityForm = reactive<Partial<OpportunityEntity>>({
  id: undefined,
  opportunityNo: '',
  opportunityName: '',
  customerId: 0,
  customerName: '',
  leadId: undefined,
  stage: 'initial',
  winProbability: 50,
  estimatedAmount: 0,
  actualAmount: undefined,
  expectedCloseDate: '',
  actualCloseDate: undefined,
  ownerId: 0,
  lossReason: '',
  status: 'ongoing',
  competitors: '',
  products: ''
})

// 商机表单验证规则
const opportunityFormRules = reactive(validationRules.sales.opportunity)

// 选中的商机
const selectedOpportunities = ref<OpportunityEntity[]>([])

// 初始化数据
onMounted(() => {
  fetchOpportunitiesList()
})

// 获取商机列表
const fetchOpportunitiesList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      opportunityName: searchForm.opportunityName,
      customerName: searchForm.customerName,
      stage: searchForm.stage,
      status: searchForm.status
    }
    const response = await salesApi.getOpportunitiesList(params)
    opportunitiesList.value = response.data?.list || []
    pagination.total = response.data?.total || 0
  } catch (error) {
    console.error('获取商机列表失败:', error)
    ElMessage.error('获取商机列表失败')
    // 使用模拟数据作为 fallback
    opportunitiesList.value = Array.from({ length: 20 }, (_, i) => ({
      id: i + 1,
      opportunityNo: `OPP${String(i + 1).padStart(6, '0')}`,
      opportunityName: `商机${i + 1}`,
      customerId: i + 1001,
      customerName: `客户${i + 1}`,
      leadId: i + 2001,
      stage: ['initial', '需求确认', '方案报价', '谈判', '成交', '失败'][i % 6] as 'initial' | '需求确认' | '方案报价' | '谈判' | '成交' | '失败',
      winProbability: Math.round(Math.random() * 100),
      estimatedAmount: Math.round(Math.random() * 1000000),
      actualAmount: i % 6 === 4 ? Math.round(Math.random() * 1000000) : undefined,
      expectedCloseDate: new Date(Date.now() + i * 86400000).toISOString().split('T')[0] || '',
      actualCloseDate: i % 6 === 4 ? new Date().toISOString().split('T')[0] : undefined,
      ownerId: 1,
      lossReason: i % 6 === 5 ? '竞争对手报价更低' : undefined,
      status: i % 6 === 4 ? 'won' : i % 6 === 5 ? 'lost' : 'ongoing',
      competitors: '竞争对手A',
      products: JSON.stringify([{ id: 1, name: '产品A' }, { id: 2, name: '产品B' }])
    }))
    pagination.total = 100
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchOpportunitiesList()
}

// 重置
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    ;(searchForm as any)[key] = ''
  })
  pagination.currentPage = 1
  fetchOpportunitiesList()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchOpportunitiesList()
}

// 当前页变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchOpportunitiesList()
}

// 选中商机变化
const handleSelectionChange = (selection: OpportunityEntity[]) => {
  selectedOpportunities.value = selection
}

// 查看详情
const handleViewDetail = async (id: number) => {
  try {
    const response = await salesApi.getOpportunityDetail(id)
    const opportunityDetail = response.data
    console.log('商机详情:', opportunityDetail)
    // 这里可以跳转到商机详情页面或弹出详情对话框
    ElMessage.success('获取商机详情成功')
  } catch (error) {
    console.error('获取商机详情失败:', error)
    ElMessage.error('获取商机详情失败')
  }
}

// 新增商机
const handleAddOpportunity = () => {
  isEditMode.value = false
  Object.assign(opportunityForm, {
    id: undefined,
    opportunityNo: '',
    opportunityName: '',
    customerId: 0,
    customerName: '',
    leadId: undefined,
    stage: 'initial',
    winProbability: 50,
    estimatedAmount: 0,
    actualAmount: undefined,
    expectedCloseDate: '',
    actualCloseDate: undefined,
    ownerId: 0,
    lossReason: '',
    status: 'ongoing',
    competitors: '',
    products: ''
  })
  dialogVisible.value = true
}

// 编辑商机
const handleEditOpportunity = (opportunity: OpportunityEntity) => {
  isEditMode.value = true
  Object.assign(opportunityForm, { ...opportunity })
  dialogVisible.value = true
}

// 保存商机
const handleSaveOpportunity = () => {
  opportunityFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (isEditMode.value && opportunityForm.id) {
          await salesApi.updateOpportunity(opportunityForm.id, opportunityForm)
          ElMessage.success('更新成功')
        } else {
          await salesApi.createOpportunity(opportunityForm)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchOpportunitiesList()
      } catch (error) {
        console.error('保存商机失败:', error)
        ElMessage.error(isEditMode.value ? '更新失败' : '创建失败')
      }
    }
  })
}

// 对话框关闭
const handleDialogClose = () => {
  dialogVisible.value = false
}

// 推进阶段
const handleUpdateStage = async (opportunity: OpportunityEntity) => {
  try {
    // 这里可以根据实际业务逻辑弹出阶段推进对话框
    // 假设我们直接将阶段推进到下一个阶段
    const nextStageMap: Record<string, string> = {
      initial: '需求确认',
      '需求确认': '方案报价',
      '方案报价': '谈判',
      '谈判': '成交'
    }
    
    const nextStage = nextStageMap[opportunity.stage]
    if (nextStage) {
      await salesApi.updateOpportunityStage(opportunity.id, { stage: nextStage })
      ElMessage.success('商机阶段推进成功')
      fetchOpportunitiesList()
    } else {
      ElMessage.warning('该商机已处于最终阶段')
    }
  } catch (error) {
    console.error('推进商机阶段失败:', error)
    ElMessage.error('推进商机阶段失败')
  }
}

// 获取阶段类型
const getStageType = (stage: string): string => {
  const stageTypeMap: Record<string, string> = {
    initial: 'info',
    '需求确认': 'primary',
    '方案报价': 'success',
    '谈判': 'warning',
    '成交': 'success',
    '失败': 'danger'
  }
  return stageTypeMap[stage] || 'info'
}

// 获取进度条颜色
const getProgressColor = (value: number): string => {
  if (value >= 80) return '#67C23A'
  if (value >= 50) return '#E6A23C'
  return '#F56C6C'
}
</script>

<style scoped>
.opportunities-view {
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
</style>
