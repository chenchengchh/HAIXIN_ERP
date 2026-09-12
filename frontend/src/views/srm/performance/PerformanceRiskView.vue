<template>
  <div class="performance-risk-view">
    <SubModuleHeader title="绩效与风险管理" parentTitle="SRM 供应商关系管理" parentPath="/home/srm" />
    
    <!-- 顶部KPI卡片 -->
    <div class="kpi-cards">
      <el-card shadow="hover" class="kpi-card">
        <template #header>
          <div class="card-header">
            <span>平均绩效得分</span>
            <el-tag type="success">优秀</el-tag>
          </div>
        </template>
        <div class="kpi-value">88.5</div>
        <div class="kpi-trend up">
          <el-icon><CaretTop /></el-icon> 2.1%
        </div>
      </el-card>
      
      <el-card shadow="hover" class="kpi-card">
        <template #header>
          <div class="card-header">
            <span>高风险供应商</span>
            <el-tag type="danger">预警</el-tag>
          </div>
        </template>
        <div class="kpi-value">3</div>
        <div class="kpi-trend down">
          <el-icon><CaretBottom /></el-icon> 1 家
        </div>
      </el-card>

      <el-card shadow="hover" class="kpi-card">
        <template #header>
          <div class="card-header">
            <span>考核覆盖率</span>
            <el-tag>本月</el-tag>
          </div>
        </template>
        <div class="kpi-value">92%</div>
        <div class="kpi-trend">
          <span>持平</span>
        </div>
      </el-card>
    </div>

    <!--主要内容区域 -->
    <el-card class="main-content" shadow="hover">
      <el-tabs v-model="activeTab">
        <!-- 绩效考核页签 -->
        <el-tab-pane label="绩效考核" name="performance">
          <!-- 搜索栏 -->
          <el-form :model="performanceSearch" inline>
            <el-form-item label="考核周期">
              <el-input v-model="performanceSearch.period" placeholder="例如: 2025-Q1" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="fetchScoreCards">查询</el-button>
              <el-button type="success" @click="handleCreateScoreCard">发起考核</el-button>
            </el-form-item>
          </el-form>

          <!-- 绩效列表 -->
          <el-table :data="scoreCardList" v-loading="loading" stripe style="width: 100%">
            <el-table-column prop="period" label="考核周期" width="120" />
            <el-table-column prop="supplierId" label="供应商" min-width="150">
               <template #default="scope">
                 {{ getSupplierName(scope.row.supplierId) }}
               </template>
            </el-table-column>
            <el-table-column prop="totalScore" label="总分" width="100">
              <template #default="scope">
                <span :style="{ fontWeight: 'bold', color: getScoreColor(scope.row.totalScore) }">
                  {{ scope.row.totalScore }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="levelResult" label="等级" width="80">
              <template #default="scope">
                <el-tag :type="getLevelTag(scope.row.levelResult)">{{ scope.row.levelResult }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="维度得分" min-width="250">
              <template #default="scope">
                <el-space wrap>
                  <el-tag size="small" type="info">质量: {{ scope.row.scoreQuality }}</el-tag>
                  <el-tag size="small" type="info">交付: {{ scope.row.scoreDelivery }}</el-tag>
                  <el-tag size="small" type="info">价格: {{ scope.row.scorePrice }}</el-tag>
                  <el-tag size="small" type="info">服务: {{ scope.row.scoreService }}</el-tag>
                </el-space>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="180" />
          </el-table>

          <!-- 分页 -->
          <div class="pagination">
             <el-pagination
              v-model:current-page="performancePagination.currentPage"
              v-model:page-size="performancePagination.pageSize"
              :total="performancePagination.total"
              layout="total, prev, pager, next"
              @current-change="fetchScoreCards"
            />
          </div>
        </el-tab-pane>

        <!-- 风险监控页签 -->
        <el-tab-pane label="风险监控" name="risk">
          <!-- 搜索栏 -->
          <el-form :model="riskSearch" inline>
            <el-form-item label="风险等级">
              <el-select v-model="riskSearch.riskLevel" placeholder="请选择" clearable>
                <el-option label="高" value="HIGH" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="低" value="LOW" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="fetchRiskList">查询</el-button>
            </el-form-item>
          </el-form>

          <!-- 风险列表 -->
          <el-table :data="riskList" v-loading="riskLoading" stripe style="width: 100%">
            <el-table-column prop="supplierName" label="供应商名称" min-width="180" />
            <el-table-column prop="riskLevel" label="风险等级" width="120">
              <template #default="scope">
                <el-tag :type="getRiskLevelTag(scope.row.riskLevel)">{{ scope.row.riskLevel || '未知' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="creditScore" label="信用评分" width="100" />
            <el-table-column prop="riskDescription" label="风险描述" min-width="200" show-overflow-tooltip />
            <el-table-column prop="evaluationDate" label="评估日期" width="180" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button link type="primary" @click="handleRecalculateRisk(scope.row)">重新评估</el-button>
              </template>
            </el-table-column>
          </el-table>

           <!-- 分页 -->
          <div class="pagination">
             <el-pagination
              v-model:current-page="riskPagination.currentPage"
              v-model:page-size="riskPagination.pageSize"
              :total="riskPagination.total"
              layout="total, prev, pager, next"
              @current-change="fetchRiskList"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 发起考核弹窗 -->
    <el-dialog v-model="dialogVisible" title="发起绩效考核" width="500px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="供应商">
          <el-select v-model="formData.supplierId" placeholder="请选择供应商" filterable>
            <el-option 
              v-for="item in supplierOptions" 
              :key="item.id" 
              :label="item.supplierName" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考核周期">
          <el-input v-model="formData.period" placeholder="例如: 2025-Q1" />
        </el-form-item>
        <el-form-item label="质量得分">
          <el-input-number v-model="formData.scoreQuality" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="交付得分">
          <el-input-number v-model="formData.scoreDelivery" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="价格得分">
          <el-input-number v-model="formData.scorePrice" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="服务得分">
          <el-input-number v-model="formData.scoreService" :min="0" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitScoreCard">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { CaretTop, CaretBottom } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import SubModuleHeader from '../../../components/common/SubModuleHeader.vue'
import { scoreCardApi, supplierCreditApi, supplierApi } from '../../../api/srm'

const activeTab = ref('performance')
const loading = ref(false)
const riskLoading = ref(false)
const dialogVisible = ref(false)

// 绩效相关数据
const performanceSearch = reactive({
  period: ''
})
const scoreCardList = ref<any[]>([])
const performancePagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 风险相关数据
const riskSearch = reactive({
  riskLevel: ''
})
const riskList = ref<any[]>([])
const riskPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 表单数据
const formData = reactive({
  supplierId: undefined,
  period: '2025-Q1',
  scoreQuality: 80,
  scoreDelivery: 80,
  scorePrice: 80,
  scoreService: 80
})

const supplierOptions = ref<any[]>([])

// 获取供应商列表
const fetchSuppliers = async () => {
  try {
    const res = await supplierApi.getSupplierList({ page: 0, size: 1000 })
    supplierOptions.value = res.data.list
  } catch (error) {
    console.error(error)
  }
}

const getSupplierName = (id: number) => {
  const supplier = supplierOptions.value.find(s => s.id === id)
  return supplier ? supplier.supplierName : `ID:${id}`
}

// 获取绩效列表
const fetchScoreCards = async () => {
  loading.value = true
  try {
    const params = {
      page: performancePagination.currentPage - 1,
      size: performancePagination.pageSize,
      period: performanceSearch.period
    }
    // 注意：这里如果后端支持按Period过滤更好，目前后端API可能需要调整或支持Specification
    // 假设后端 getScoreCardList 支持参数传递
    const res = await scoreCardApi.getScoreCardList(params)
    scoreCardList.value = (res.data.list || res.data.records || [])
    performancePagination.total = (res.data.total || 0)
  } catch (error) {
    console.error(error)
    ElMessage.error('获取绩效列表失败')
  } finally {
    loading.value = false
  }
}

// 发起考核
const handleCreateScoreCard = () => {
  dialogVisible.value = true
}

const submitScoreCard = async () => {
  if (!formData.supplierId) {
    ElMessage.warning('请选择供应商')
    return
  }
  try {
    // 简单计算总分
    const total = (formData.scoreQuality * 0.4) + (formData.scoreDelivery * 0.3) + (formData.scorePrice * 0.2) + (formData.scoreService * 0.1)
    
    let level = 'C'
    if (total >= 90) level = 'A'
    else if (total >= 80) level = 'B'
    else if (total >= 60) level = 'C'
    else level = 'D'

    const payload = {
      ...formData,
      totalScore: total,
      levelResult: level
    }
    
    await scoreCardApi.createScoreCard(payload)
    ElMessage.success('考核创建成功')
    dialogVisible.value = false
    fetchScoreCards()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

// 获取风险列表（复用供应商信用列表）
const fetchRiskList = async () => {
  riskLoading.value = true
  try {
    const params = {
      page: riskPagination.currentPage - 1,
      size: riskPagination.pageSize,
      riskLevel: riskSearch.riskLevel
    }
    const res = await supplierCreditApi.getSupplierCreditList(params)
    riskList.value = (res.data.list || res.data.records || [])
    riskPagination.total = (res.data.total || 0)
  } catch (error) {
    console.error(error)
    ElMessage.error('获取风险列表失败')
  } finally {
    riskLoading.value = false
  }
}

// 重新评估风险
const handleRecalculateRisk = async (row: any) => {
  try {
    await supplierCreditApi.calculateSupplierCredit(row.supplierId)
    ElMessage.success('重新评估成功')
    fetchRiskList()
  } catch (error) {
    ElMessage.error('评估失败')
  }
}

// 辅助函数
const getScoreColor = (score: number) => {
  if (score >= 90) return '#67c23a'
  if (score >= 80) return '#409eff'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

const getLevelTag = (level: string) => {
  const map: Record<string, string> = { 'A': 'success', 'B': 'primary', 'C': 'warning', 'D': 'danger' }
  return map[level] || 'info'
}

const getRiskLevelTag = (level: string) => {
  const map: Record<string, string> = { 'LOW': 'success', 'MEDIUM': 'warning', 'HIGH': 'danger' }
  return map[level] || 'info'
}

onMounted(() => {
  fetchSuppliers()
  fetchScoreCards()
  fetchRiskList()
})
</script>

<style scoped lang="scss">
.performance-risk-view {
  padding: 24px;

  .kpi-cards {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
    margin-bottom: 24px;

    .kpi-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .kpi-value {
        font-size: 32px;
        font-weight: bold;
        margin: 16px 0;
        color: #303133;
      }

      .kpi-trend {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 14px;
        
        &.up { color: #67c23a; }
        &.down { color: #f56c6c; }
      }
    }
  }
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
