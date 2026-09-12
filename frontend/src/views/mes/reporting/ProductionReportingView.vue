<template>
  <div class="mes-reporting-container">
    <!-- 面包屑导航 -->
    <div class="page-header">
      <h2>生产报工管理</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home/mes">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/mes">MES系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/mes/reporting">生产报工</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/mes/reporting#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card" class="function-tabs">
        <el-tab-pane label="生产报工概览" name="report-overview">
          <el-card shadow="hover" class="content-card">
            <div class="card-content">
              <div class="stats-grid">
                <div class="stat-item">
                  <div class="stat-value">{{ reportedReports.length }}</div>
                  <div class="stat-label">已上报</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ verifiedReports.length }}</div>
                  <div class="stat-label">已验证</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ approvedReports.length }}</div>
                  <div class="stat-label">已批准</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="生产报工列表" name="report-list">
          <el-card shadow="hover" class="content-card">
            <div class="card-header">
              <h3>生产报工列表</h3>
              <el-button type="primary" @click="showReportDialog = true">
                <el-icon><Plus /></el-icon> 新增生产报工
              </el-button>
            </div>
            <div class="card-content">
              <el-table v-loading="loading.productionReports" :data="productionReports" style="width: 100%">
                <el-table-column prop="workOrderNo" label="工单号" min-width="120" />
                <el-table-column prop="stepName" label="工序名称" min-width="120" />
                <el-table-column prop="workstationName" label="工站名称" min-width="120" />
                <el-table-column prop="operatorName" label="操作员" min-width="100" />
                <el-table-column prop="startTime" label="开始时间" width="150" />
                <el-table-column prop="endTime" label="结束时间" width="150" />
                <el-table-column prop="goodQty" label="良品数" width="80" />
                <el-table-column prop="scrapQty" label="废品数" width="80" />
                <el-table-column prop="reworkQty" label="返工数" width="80" />
                <el-table-column prop="workingHours" label="工时" width="80" />
                <el-table-column prop="machineHours" label="机时" width="80" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.status === 'reported' ? 'info' : 
                             scope.row.status === 'verified' ? 'warning' : 'success'">
                      {{ statusMap[scope.row.status] }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="scope">
                    <el-button 
                      v-if="scope.row.status === 'reported'" 
                      type="warning" 
                      size="small" 
                      @click="verifyReport(scope.row.id)">
                      验证
                    </el-button>
                    <el-button 
                      v-if="scope.row.status === 'verified'" 
                      type="success" 
                      size="small" 
                      @click="approveReport(scope.row.id)">
                      批准
                    </el-button>
                    <el-button type="info" size="small" @click="viewReportDetail(scope.row)">
                      详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="报工统计" name="report-statistics">
          <el-card shadow="hover" class="content-card">
            <div class="card-content">
              <div class="chart-container">
                <div id="reportChart" style="height: 300px; width: 100%;"></div>
              </div>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 报工对话框 -->
    <el-dialog
      v-model="showReportDialog"
      title="新增生产报工"
      width="700px"
      destroy-on-close>
      <el-form :model="newReport" label-position="top" :rules="reportRules" ref="reportFormRef">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工单号" prop="workOrderNo">
              <el-input v-model="newReport.workOrderNo" placeholder="请输入工单号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工序名称" prop="stepName">
              <el-input v-model="newReport.stepName" placeholder="请输入工序名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工站名称" prop="workstationName">
              <el-input v-model="newReport.workstationName" placeholder="请输入工站名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作员" prop="operatorName">
              <el-input v-model="newReport.operatorName" placeholder="请输入操作员姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="newReport.startTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%"
                placeholder="选择开始时间"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="newReport.endTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%"
                placeholder="选择结束时间"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="良品数" prop="goodQty">
              <el-input-number v-model="newReport.goodQty" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="废品数" prop="scrapQty">
              <el-input-number v-model="newReport.scrapQty" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="返工数" prop="reworkQty">
              <el-input-number v-model="newReport.reworkQty" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工时(小时)" prop="workingHours">
              <el-input-number v-model="newReport.workingHours" :min="0" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="机时(小时)" prop="machineHours">
              <el-input-number v-model="newReport.machineHours" :min="0" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        

      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showReportDialog = false">取消</el-button>
          <el-button type="primary" @click="submitReport">确认</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 报工详情对话框 -->
    <el-dialog
      v-model="showReportDetailDialog"
      title="报工详情"
      width="800px"
      destroy-on-close>
      <div v-if="selectedReport" class="report-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工单号" :span="1">{{ selectedReport.workOrderNo }}</el-descriptions-item>
          <el-descriptions-item label="工序名称" :span="1">{{ selectedReport.stepName }}</el-descriptions-item>
          <el-descriptions-item label="工站名称" :span="1">{{ selectedReport.workstationName }}</el-descriptions-item>
          <el-descriptions-item label="操作员" :span="1">{{ selectedReport.operatorName }}</el-descriptions-item>
          <el-descriptions-item label="开始时间" :span="1">{{ selectedReport.startTime }}</el-descriptions-item>
          <el-descriptions-item label="结束时间" :span="1">{{ selectedReport.endTime }}</el-descriptions-item>
          <el-descriptions-item label="良品数" :span="1">{{ selectedReport.goodQty }}</el-descriptions-item>
          <el-descriptions-item label="废品数" :span="1">{{ selectedReport.scrapQty }}</el-descriptions-item>
          <el-descriptions-item label="返工数" :span="1">{{ selectedReport.reworkQty }}</el-descriptions-item>
          <el-descriptions-item label="总产量" :span="1">{{ selectedReport.goodQty + selectedReport.scrapQty + selectedReport.reworkQty }}</el-descriptions-item>
          <el-descriptions-item label="合格率" :span="1">{{ selectedReport.goodQty > 0 ? ((selectedReport.goodQty / (selectedReport.goodQty + selectedReport.scrapQty + selectedReport.reworkQty)) * 100).toFixed(1) + '%' : '0%' }}</el-descriptions-item>
          <el-descriptions-item label="工时(小时)" :span="1">{{ selectedReport.workingHours }}</el-descriptions-item>
          <el-descriptions-item label="机时(小时)" :span="1">{{ selectedReport.machineHours }}</el-descriptions-item>
          <el-descriptions-item label="生产效率" :span="1">{{ selectedReport.workingHours > 0 ? ((selectedReport.goodQty + selectedReport.reworkQty) / selectedReport.workingHours).toFixed(1) + ' 件/小时' : '0 件/小时' }}</el-descriptions-item>
          <el-descriptions-item label="状态" :span="1">
            <el-tag :type="selectedReport.status === 'reported' ? 'info' : selectedReport.status === 'verified' ? 'warning' : 'success'">
              {{ statusMap[selectedReport.status] }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="1">{{ selectedReport.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="1">{{ selectedReport.updateTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showReportDetailDialog = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useMesReportingStore } from '../../../stores/mes/reporting'
import { storeToRefs } from 'pinia'
import type { ProductionReport } from '../../../api/mes/reporting'

// 状态管理
// 使用storeToRefs保持state/getters响应性，actions直接解构
const store = useMesReportingStore()
const { 
  productionReports, 
  reportedReports, 
  verifiedReports, 
  approvedReports,
  loading
} = storeToRefs(store)
const {
  fetchProductionReports,
  createProductionReport,
  updateProductionReportStatus
} = store

// 状态映射
const statusMap: Record<string, string> = {
  reported: '已上报',
  verified: '已验证',
  approved: '已批准'
}

// 报工对话框
const showReportDialog = ref(false)
const reportFormRef = ref()
const newReport = reactive<Partial<ProductionReport>>({
  workOrderNo: '',
  stepName: '',
  workstationName: '',
  operatorName: '',
  startTime: new Date().toISOString(),
  endTime: new Date().toISOString(),
  goodQty: 0,
  scrapQty: 0,
  reworkQty: 0,
  workingHours: 0,
  machineHours: 0
})

// 自动计算工时函数
const calculateWorkingHours = () => {
  if (newReport.startTime && newReport.endTime) {
    const start = new Date(newReport.startTime)
    const end = new Date(newReport.endTime)
    // 计算小时数，保留一位小数
    const hours = Math.round(((end.getTime() - start.getTime()) / (1000 * 60 * 60)) * 10) / 10
    // 确保工时为正数
    newReport.workingHours = hours > 0 ? hours : 0
    // 默认机时与工时相同
    newReport.machineHours = newReport.workingHours
  }
}

// 监听开始时间和结束时间变化，自动计算工时
watch(() => [newReport.startTime, newReport.endTime], () => {
  calculateWorkingHours()
}, { deep: true })

const reportRules = {
  workOrderNo: [{ required: true, message: '请输入工单号', trigger: 'blur' }],
  stepName: [{ required: true, message: '请输入工序名称', trigger: 'blur' }],
  workstationName: [{ required: true, message: '请输入工站名称', trigger: 'blur' }],
  operatorName: [{ required: true, message: '请输入操作员姓名', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  goodQty: [{ required: true, type: 'number', min: 0, message: '良品数必须大于等于0', trigger: 'blur' }],
  workingHours: [{ required: true, type: 'number', min: 0, message: '工时必须大于等于0', trigger: 'blur' }],
  machineHours: [{ required: true, type: 'number', min: 0, message: '机时必须大于等于0', trigger: 'blur' }]
}

// 报工详情对话框
const showReportDetailDialog = ref(false)
const selectedReport = ref<ProductionReport | null>(null)

// 导入Element Plus消息提示组件
import { ElMessage } from 'element-plus'

// 导入Element Plus图标
import { ArrowRight, Plus } from '@element-plus/icons-vue'

// 导入ECharts
import * as echarts from 'echarts'

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'report-overview': '生产报工概览',
  'report-list': '生产报工列表',
  'report-statistics': '报工统计'
}

// 活跃标签页
const activeTab = ref('report-overview')

// 图表引用
let reportChart: echarts.ECharts | null = null

// 图表初始化函数
const initReportChart = () => {
  // 如果图表已经初始化，直接更新数据
  if (reportChart) {
    updateReportChart()
    return
  }
  
  // 获取DOM元素
  const chartDom = document.getElementById('reportChart')
  if (chartDom) {
    reportChart = echarts.init(chartDom)
    
    // 更新图表数据
    updateReportChart()
    
    // 监听窗口大小变化，调整图表大小
    window.addEventListener('resize', () => {
      reportChart?.resize()
    })
  }
}

// 处理实际报工数据生成图表数据
const processChartData = () => {
  // 按日期分组统计报工数据
  const dailyData: Record<string, { goodQty: number; scrapQty: number; reworkQty: number }> = {}
  
  // 获取当前日期，并计算近7天的日期
  const now = new Date()
  const categories: string[] = []
  
  // 初始化近7天的数据
  for (let i = 6; i >= 0; i--) {
    const date = new Date(now)
    date.setDate(date.getDate() - i)
    // 格式化日期为YYYY-MM-DD，并确保类型为string
    const dateStr = date.toISOString().split('T')[0] as string
    categories.push(dateStr)
    dailyData[dateStr] = { goodQty: 0, scrapQty: 0, reworkQty: 0 }
  }
  
  // 统计实际报工数据
  productionReports.value.forEach(report => {
    if (report.createTime) {
      // 获取报工日期的YYYY-MM-DD格式，并确保类型为string
      const reportDate = new Date(report.createTime).toISOString().split('T')[0] as string
      // 如果报工日期在近7天内且dailyData中存在该日期，累加数据
      if (categories.includes(reportDate) && dailyData[reportDate]) {
        dailyData[reportDate].goodQty += report.goodQty || 0
        dailyData[reportDate].scrapQty += report.scrapQty || 0
        dailyData[reportDate].reworkQty += report.reworkQty || 0
      }
    }
  })
  
  // 提取数据用于图表
  return {
    categories,
    goodQtyData: categories.map(date => dailyData[date]?.goodQty || 0),
    scrapQtyData: categories.map(date => dailyData[date]?.scrapQty || 0),
    reworkQtyData: categories.map(date => dailyData[date]?.reworkQty || 0)
  }
}

// 更新图表数据
const updateReportChart = () => {
  if (reportChart) {
    // 处理实际报工数据
    const { categories, goodQtyData, scrapQtyData, reworkQtyData } = processChartData()
    
    // 图表配置
    const option = {
      title: {
        text: '生产报工趋势',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        bottom: 10,
        data: ['良品数', '废品数', '返工数']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: categories
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '良品数',
          type: 'line',
          data: goodQtyData,
          smooth: true,
          itemStyle: {
            color: '#67C23A'
          }
        },
        {
          name: '废品数',
          type: 'line',
          data: scrapQtyData,
          smooth: true,
          itemStyle: {
            color: '#F56C6C'
          }
        },
        {
          name: '返工数',
          type: 'line',
          data: reworkQtyData,
          smooth: true,
          itemStyle: {
            color: '#E6A23C'
          }
        }
      ]
    }
    
    // 设置图表配置
    reportChart.setOption(option)
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchProductionReports()
})

// 监听数据变化，更新图表
watch(productionReports, () => {
  // 只有在统计标签页时才更新图表
  if (activeTab.value === 'report-statistics') {
    updateReportChart()
  }
}, { deep: true })

// 监听标签页切换，初始化图表
watch(activeTab, (newTab) => {
  if (newTab === 'report-statistics') {
    // 延迟初始化，确保DOM已经渲染
    setTimeout(() => {
      initReportChart()
    }, 100)
  }
})

// 提交报工
const submitReport = async () => {
  if (reportFormRef.value) {
    await reportFormRef.value.validate(async (valid: boolean) => {
      if (valid) {
        try {
          await createProductionReport(newReport)
          showReportDialog.value = false
          // 重置表单
          Object.assign(newReport, {
            workOrderNo: '',
            stepName: '',
            workstationName: '',
            operatorName: '',
            startTime: new Date().toISOString(),
            endTime: new Date().toISOString(),
            goodQty: 0,
            scrapQty: 0,
            reworkQty: 0,
            workingHours: 0,
            machineHours: 0
          })
          if (reportFormRef.value) {
            reportFormRef.value.resetFields()
          }
          // 刷新数据
          fetchProductionReports()
          // 显示成功消息
          ElMessage.success('生产报工创建成功')
        } catch (err) {
          console.error('创建生产报工失败:', err)
          ElMessage.error('创建生产报工失败，请重试')
        }
      }
    })
  }
}

// 验证报工
const verifyReport = async (id: string) => {
  try {
    await updateProductionReportStatus(id, 'verified')
    // 刷新数据
    fetchProductionReports()
    // 显示成功消息
    ElMessage.success('报工验证成功')
  } catch (err) {
    console.error('验证报工失败:', err)
    ElMessage.error('报工验证失败，请重试')
  }
}

// 批准报工
const approveReport = async (id: string) => {
  try {
    await updateProductionReportStatus(id, 'approved')
    // 刷新数据
    fetchProductionReports()
    // 显示成功消息
    ElMessage.success('报工批准成功')
  } catch (err) {
    console.error('批准报工失败:', err)
    ElMessage.error('报工批准失败，请重试')
  }
}

// 查看报工详情
const viewReportDetail = (report: ProductionReport) => {
  selectedReport.value = report
  showReportDetailDialog.value = true
}
</script>

<style scoped>
.mes-reporting-container {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: var(--bg-color-page);
}

/* 页面头部样式 */
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 16px 0;
  margin-bottom: 24px;
  border-bottom: 1px solid var(--border-color-light);
  gap: 8px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin: 0;
}

/* 模块导航样式 */
.module-nav {
  background-color: var(--bg-color);
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
  min-height: calc(100% - 120px);
}

/* 标签页样式 */
.function-tabs {
  padding: 16px;
}

/* 覆盖默认的卡片样式，使其更符合设计规范 */
.function-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.function-tabs :deep(.el-tabs__content) {
  padding: 16px 0 0 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-content {
  padding: 0;
}

/* 统计值样式 */
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 卡片样式 */
.content-card {
  margin: 20px;
}

.info-card {
  margin: 20px;
}

/* 统计网格样式 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
  padding: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.chart-container {
  height: 300px;
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .mes-reporting-container {
    padding: 12px;
  }
  
  h1 {
    font-size: 1.2rem;
  }
  
  .module-nav {
    min-height: auto;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    padding: 10px;
  }
  
  .content-card,
  .info-card {
    margin: 10px;
  }
}
</style>
