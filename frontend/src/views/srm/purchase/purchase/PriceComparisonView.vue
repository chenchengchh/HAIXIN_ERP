<template>
  <div class="price-comparison-view">
    <div class="content-header">
      <h3>比价分析</h3>
      <div class="action-buttons">
        <el-button type="primary" @click="handleExport">导出比价报告</el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-card shadow="never">
        <el-form :model="searchForm" layout="inline" size="small">
          <el-form-item label="询价单号">
            <el-input v-model="searchForm.inquiryCode" placeholder="请输入询价单号" />
          </el-form-item>
          <el-form-item label="项目名称">
            <el-input v-model="searchForm.projectName" placeholder="请输入项目名称" />
          </el-form-item>
          <el-form-item label="比价状态">
            <el-select v-model="searchForm.status" placeholder="请选择比价状态">
              <el-option label="全部" value="" />
              <el-option label="进行中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    
    <!-- 比价列表区域 -->
    <div class="table-area">
      <el-card shadow="never">
        <el-table v-loading="loading" :data="comparisonList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="inquiryCode" label="询价单号" width="180" />
          <el-table-column prop="projectName" label="项目名称" width="220" />
          <el-table-column prop="totalSupplier" label="参与供应商" width="120" />
          <el-table-column prop="lowestPrice" label="最低报价" width="120" />
          <el-table-column prop="highestPrice" label="最高报价" width="120" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column prop="createDate" label="创建日期" width="180" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" @click="handleViewDetail(scope.row)">查看详情</el-button>
              <el-button size="small" type="success" @click="handleConfirm(scope.row)">确认结果</el-button>
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
    </div>
    
    <!-- 比价详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="'比价详情 - ' + currentDetail?.inquiryCode"
      width="80%"
      :close-on-click-modal="false"
    >
      <div class="comparison-detail">
        <h4>基本信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="询价单号">{{ currentDetail?.inquiryCode }}</el-descriptions-item>
          <el-descriptions-item label="项目名称">{{ currentDetail?.projectName }}</el-descriptions-item>
          <el-descriptions-item label="参与供应商">{{ currentDetail?.totalSupplier }}家</el-descriptions-item>
          <el-descriptions-item label="最低报价">{{ currentDetail?.lowestPrice }}元</el-descriptions-item>
          <el-descriptions-item label="最高报价">{{ currentDetail?.highestPrice }}元</el-descriptions-item>
          <el-descriptions-item label="比价状态">{{ currentDetail?.status }}</el-descriptions-item>
        </el-descriptions>
        
        <h4 style="margin-top: 20px;">报价详情对比</h4>
        <el-table :data="currentDetail?.quotations" style="width: 100%">
          <el-table-column prop="supplierName" label="供应商" width="180" />
          <el-table-column prop="quotationCode" label="报价单号" width="180" />
          <el-table-column prop="totalAmount" label="总金额" width="120" />
          <el-table-column prop="deliveryPeriod" label="交付周期" width="120" />
          <el-table-column prop="paymentTerms" label="付款条件" width="180" />
          <el-table-column prop="score" label="综合得分" width="100" />
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button size="small" @click="handleViewQuotation(scope.row)">查看报价单</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <h4 style="margin-top: 20px;">比价分析</h4>
        <div class="analysis-chart">
          <!-- 这里可以集成图表库，如ECharts -->
          <div class="chart-placeholder">
            <el-empty description="暂无图表数据" />
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { inquiryApi, quotationApi } from '../../../../api/srm'

// 搜索表单
const searchForm = reactive({
  inquiryCode: '',
  projectName: '',
  status: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载状态
const loading = ref(false)

// 比价列表
const comparisonList = ref<any[]>([])
const quotationByInquiryId = ref<Record<number, any[]>>({})

// 详情对话框
const detailDialogVisible = ref(false)
const currentDetail = ref<any>(null)

// 页面加载时获取数据
onMounted(() => {
  fetchComparisonList()
})

// 获取比价列表
const fetchComparisonList = async () => {
  loading.value = true
  try {
    const inquiryParams = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize
    }
    const [inquiryRes, quotationRes] = await Promise.all([
      inquiryApi.getInquiryList(inquiryParams),
      quotationApi.getQuotationList({ page: 0, size: 1000 })
    ])

    const inquiries = (inquiryRes.data.list || inquiryRes.data.records || [])
    const quotations = (quotationRes.data.list || quotationRes.data.records || [])
    const grouped: Record<number, any[]> = {}
    for (const q of quotations) {
      const key = q.inquiryId
      if (key == null) continue
      if (!grouped[key]) grouped[key] = []
      grouped[key].push(q)
    }
    quotationByInquiryId.value = grouped

    comparisonList.value = inquiries.map((inq: any) => {
      const qs = grouped[inq.id] || []
      const amounts = qs.map((q: any) => Number(q.totalAmount || 0)).filter((n: number) => !Number.isNaN(n))
      const lowest = amounts.length ? Math.min(...amounts) : 0
      const highest = amounts.length ? Math.max(...amounts) : 0
      return {
        id: inq.id,
        inquiryCode: inq.inquiryCode,
        projectName: inq.title,
        totalSupplier: qs.length,
        lowestPrice: lowest,
        highestPrice: highest,
        status: inq.status,
        createDate: inq.createdTime || inq.createTime || ''
      }
    })
    pagination.total = (inquiryRes.data.total || 0)
  } catch (error) {
    ElMessage.error('获取比价列表失败')
    comparisonList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchComparisonList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    inquiryCode: '',
    projectName: '',
    status: ''
  })
  pagination.currentPage = 1
  fetchComparisonList()
}

// 导出比价报告
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 查看详情
const handleViewDetail = async (row: any) => {
  currentDetail.value = { ...row }
  const qs = quotationByInquiryId.value[row.id] || []
  currentDetail.value.quotations = [...qs].sort((a: any, b: any) => Number(a.totalAmount || 0) - Number(b.totalAmount || 0))
  detailDialogVisible.value = true
}

// 确认比价结果
const handleConfirm = (row: any) => {
  ElMessageBox.confirm('确定要确认该比价结果吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    ElMessage.success('确认成功')
    fetchComparisonList()
  }).catch(() => {
    // 取消确认
  })
}

// 查看报价单
const handleViewQuotation = (row: any) => {
  ElMessage.info('查看报价单功能开发中')
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchComparisonList()
}

// 当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchComparisonList()
}
</script>

<style scoped>
.price-comparison-view {
  padding: 16px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.content-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.search-area {
  margin-bottom: 16px;
}

.table-area {
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.dialog-footer {
  text-align: right;
}

.comparison-detail h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.analysis-chart {
  margin-top: 16px;
  height: 300px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
