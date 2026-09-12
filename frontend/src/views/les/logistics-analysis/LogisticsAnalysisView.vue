<template>
  <div class="logistics-analysis-view">
    <div class="page-header">
      <h2>物流分析</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home/les">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/les">LES系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/les/logistics-analysis">物流分析</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/les/logistics-analysis#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 功能标签页 -->
    <el-tabs v-model="activeTab" class="function-tabs">
      <!-- 运输效率分析 -->
      <el-tab-pane label="运输效率分析" name="efficiency">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>对比计划时长与实际运输时长，分析各路段及节点的延迟重灾区</span>
              </div>
            </template>
            <div class="efficiency-content">
              <!-- 筛选条件 -->
              <div class="filter-bar">
                <el-select v-model="efficiencyFilter.period" placeholder="选择时间段" style="width: 120px; margin-right: 16px;">
                  <el-option label="日" value="daily" />
                  <el-option label="周" value="weekly" />
                  <el-option label="月" value="monthly" />
                </el-select>
                <el-date-picker v-model="efficiencyFilter.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 300px; margin-right: 16px;"></el-date-picker>
                <el-select v-model="efficiencyFilter.routeId" placeholder="选择路线" style="width: 150px;">
                  <el-option label="全部路线" value="" />
                  <el-option v-for="r in store.routes" :key="r.id" :label="r.routeName" :value="r.id" />
                </el-select>
              </div>
              
              <!-- 效率对比图表 -->
              <div class="chart-section">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-card shadow="hover" class="chart-card">
                      <template #header>
                        <div class="chart-header">
                          <span>计划时长 vs 实际时长</span>
                        </div>
                      </template>
                      <div class="chart-content">
                        <el-empty v-if="routeTimeCompareTop.length === 0" description="暂无数据" />
                        <div v-else class="simple-chart">
                          <div class="chart-title">时间对比 (分钟)</div>
                          <div class="chart-bars">
                            <div class="chart-bar-item" v-for="item in routeTimeCompareTop" :key="item.routeName">
                              <div class="bar-label">{{ item.routeName }}</div>
                              <div class="bar-container">
                                <div class="bar" :style="{ width: item.plannedPercent + '%', background: '#409EFF' }"></div>
                                <div class="bar" :style="{ width: item.actualPercent + '%', background: item.hasDelay ? '#E6A23C' : '#67C23A', marginTop: '5px' }"></div>
                              </div>
                              <div class="bar-values">
                                <span style="color: #409EFF;">{{ item.planned }}</span>
                                <span :style="{ color: item.hasDelay ? '#E6A23C' : '#67C23A', marginLeft: '20px' }">{{ item.actual }}</span>
                              </div>
                            </div>
                            <div class="chart-bar-legend">
                              <span><span class="legend-dot" style="background: #409EFF;"></span> 计划时长</span>
                              <span><span class="legend-dot" style="background: #67C23A;"></span> 实际时长</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </el-card>
                  </el-col>
                  <el-col :span="12">
                    <el-card shadow="hover" class="chart-card">
                      <template #header>
                        <div class="chart-header">
                          <span>延迟路段分布</span>
                        </div>
                      </template>
                      <div class="chart-content">
                        <el-table :data="delayRouteDistribution" style="width: 100%" height="240">
                          <el-table-column prop="routeName" label="路线" min-width="160" />
                          <el-table-column prop="delayCount" label="延迟次数" width="100" />
                          <el-table-column prop="avgDelayMinutes" label="平均延迟(min)" width="140" />
                          <el-table-column prop="percent" label="占比" width="100">
                            <template #default="scope">
                              {{ scope.row.percent }}%
                            </template>
                          </el-table-column>
                        </el-table>
                        <div class="pie-summary">
                          <div class="summary-item">
                            <span class="summary-label">总延迟次数:</span>
                            <span class="summary-value">{{ totalDelayCount }}次</span>
                          </div>
                          <div class="summary-item">
                            <span class="summary-label">平均延迟时间:</span>
                            <span class="summary-value">{{ avgDelayMinutes }}分钟</span>
                          </div>
                        </div>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
              </div>
              
              <!-- 效率明细 -->
              <div class="efficiency-detail">
                <h3>运输效率明细</h3>
                <el-table :data="efficiencyDetail" style="width: 100%" height="300">
                  <el-table-column prop="planId" label="计划ID" width="100" />
                  <el-table-column prop="planNo" label="计划编号" width="150" />
                  <el-table-column prop="routeName" label="路线" width="120" />
                  <el-table-column prop="estimatedTime" label="预计时长(min)" width="120" />
                  <el-table-column prop="actualTime" label="实际时长(min)" width="120" />
                  <el-table-column prop="delayTime" label="延迟时长(min)" width="120">
                    <template #default="scope">
                      <el-tag :type="scope.row.delayTime > 0 ? 'danger' : 'success'">
                        {{ scope.row.delayTime > 0 ? scope.row.delayTime : 0 }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="efficiency" label="运输效率" width="100">
                    <template #default="scope">
                      {{ scope.row.efficiency }}%
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 资源利用率分析 -->
      <el-tab-pane label="资源利用率分析" name="utilization">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>统计车辆、司机的日均行驶里程、闲置天数及负载率，优化排班</span>
              </div>
            </template>
            <div class="utilization-content">
              <!-- 资源类型切换 -->
              <el-radio-group v-model="resourceType" size="large" class="resource-type-switch">
                <el-radio-button value="vehicle">车辆</el-radio-button>
                <el-radio-button value="driver">司机</el-radio-button>
              </el-radio-group>
              
              <!-- 利用率图表 -->
              <div class="chart-section">
                <el-row :gutter="20">
                  <el-col :span="8">
                    <el-card shadow="hover" class="chart-card">
                      <template #header>
                        <div class="chart-header">
                          <span>资源数量</span>
                        </div>
                      </template>
                      <div class="chart-content">
                        <el-statistic :value="utilizationSummary.count" title="数量" />
                      </div>
                    </el-card>
                  </el-col>
                  <el-col :span="8">
                    <el-card shadow="hover" class="chart-card">
                      <template #header>
                        <div class="chart-header">
                          <span>平均利用率</span>
                        </div>
                      </template>
                      <div class="chart-content">
                        <el-statistic :value="utilizationSummary.avgUtilizationRate" title="平均利用率" :precision="1">
                          <template #suffix>
                            <span style="font-size: 14px;">%</span>
                          </template>
                        </el-statistic>
                      </div>
                    </el-card>
                  </el-col>
                  <el-col :span="8">
                    <el-card shadow="hover" class="chart-card">
                      <template #header>
                        <div class="chart-header">
                          <span>{{ resourceType === 'vehicle' ? '平均负载率' : '平均工时' }}</span>
                        </div>
                      </template>
                      <div class="chart-content">
                        <el-statistic v-if="resourceType === 'vehicle'" :value="utilizationSummary.avgLoadRate" title="平均负载率" :precision="1">
                          <template #suffix>
                            <span style="font-size: 14px;">%</span>
                          </template>
                        </el-statistic>
                        <el-statistic v-else :value="utilizationSummary.avgWorkHours" title="平均工时" :precision="1">
                          <template #suffix>
                            <span style="font-size: 14px;">小时</span>
                          </template>
                        </el-statistic>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
              </div>
              
              <!-- 资源利用率列表 -->
              <div class="utilization-list">
                <h3>{{ resourceType === 'vehicle' ? '车辆' : '司机' }}利用率明细</h3>
                <el-table :data="resourceUtilization" style="width: 100%" height="400">
                  <!-- 车辆列表列 -->
                  <template v-if="resourceType === 'vehicle'">
                    <el-table-column prop="id" label="车辆ID" width="100" />
                    <el-table-column prop="licensePlate" label="车牌" width="120" />
                    <el-table-column prop="vehicleType" label="车型" width="100" />
                    <el-table-column prop="dailyMileage" label="日均里程(km)" width="120" />
                    <el-table-column prop="idleDays" label="闲置天数" width="100">
                      <template #default="scope">
                        <el-tag :type="scope.row.idleDays > 3 ? 'warning' : 'success'">
                          {{ scope.row.idleDays }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="loadRate" label="负载率" width="100">
                      <template #default="scope">
                        {{ scope.row.loadRate }}%
                      </template>
                    </el-table-column>
                    <el-table-column prop="utilizationRate" label="利用率" width="100">
                      <template #default="scope">
                        <el-progress :percentage="scope.row.utilizationRate" :format="utilizationFormat" :color="getProgressColor(scope.row.utilizationRate)" />
                      </template>
                    </el-table-column>
                  </template>
                  
                  <!-- 司机列表列 -->
                  <template v-else>
                    <el-table-column prop="id" label="司机ID" width="100" />
                    <el-table-column prop="name" label="姓名" width="100" />
                    <el-table-column prop="phone" label="电话" width="120" />
                    <el-table-column prop="dailyMileage" label="日均里程(km)" width="120" />
                    <el-table-column prop="workDays" label="工作天数" width="100" />
                    <el-table-column prop="avgWorkHours" label="日均工作时长(h)" width="130" />
                    <el-table-column prop="utilizationRate" label="利用率" width="100">
                      <template #default="scope">
                        <el-progress :percentage="scope.row.utilizationRate" :format="utilizationFormat" :color="getProgressColor(scope.row.utilizationRate)" />
                      </template>
                    </el-table-column>
                  </template>
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 服务质量评估 -->
      <el-tab-pane label="服务质量评估" name="quality">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>基于签收成功率、货损完好率等多维度对物流服务商/司机进行画像与评价</span>
              </div>
            </template>
            <div class="quality-content">
              <!-- 质量评分 -->
              <div class="quality-scores">
                <el-row :gutter="20">
                  <el-col :span="6">
                    <el-card shadow="hover" class="score-card">
                      <div class="score-header">
                        <h3>综合评分</h3>
                        <el-rate :model-value="overallScore" disabled :colors="['#909399', '#E6A23C', '#67C23A']" />
                      </div>
                      <div class="score-value">{{ overallScore.toFixed(1) }}</div>
                    </el-card>
                  </el-col>
                  <el-col :span="6">
                    <el-card shadow="hover" class="score-card">
                      <div class="score-header">
                        <h3>签收成功率</h3>
                      </div>
                      <div class="score-value">{{ signSuccessRate }}%</div>
                    </el-card>
                  </el-col>
                  <el-col :span="6">
                    <el-card shadow="hover" class="score-card">
                      <div class="score-header">
                        <h3>货损完好率</h3>
                      </div>
                      <div class="score-value">{{ goodsIntactRate }}%</div>
                    </el-card>
                  </el-col>
                  <el-col :span="6">
                    <el-card shadow="hover" class="score-card">
                      <div class="score-header">
                        <h3>客户满意度</h3>
                      </div>
                      <div class="score-value">{{ customerSatisfaction }}%</div>
                    </el-card>
                  </el-col>
                </el-row>
              </div>
              
              <!-- 质量评估图表 -->
              <div class="chart-section">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-card shadow="hover" class="chart-card">
                      <template #header>
                        <div class="chart-header">
                          <span>服务质量维度分析</span>
                        </div>
                      </template>
                      <div class="chart-content">
                        <div class="simple-radar-chart">
                          <div class="chart-title">服务质量多维度评分</div>
                          <div class="radar-container">
                            <!-- 雷达图背景 -->
                            <div class="radar-grid">
                              <div class="radar-circle"></div>
                              <div class="radar-circle"></div>
                              <div class="radar-circle"></div>
                              <div class="radar-circle"></div>
                              <div class="radar-circle"></div>
                              <!-- 雷达图轴线 -->
                              <div class="radar-axis"></div>
                              <div class="radar-axis"></div>
                              <div class="radar-axis"></div>
                              <div class="radar-axis"></div>
                              <div class="radar-axis"></div>
                            </div>
                            <!-- 雷达图数据区域 -->
                            <div class="radar-data-area"></div>
                            <!-- 雷达图标签 -->
                            <div class="radar-labels">
                              <div class="radar-label">准时率</div>
                              <div class="radar-label">完好率</div>
                              <div class="radar-label">服务态度</div>
                              <div class="radar-label">专业性</div>
                              <div class="radar-label">响应速度</div>
                            </div>
                            <!-- 评分数据 -->
                            <div class="radar-scores">
                              <div class="radar-score">92%</div>
                              <div class="radar-score">95%</div>
                              <div class="radar-score">88%</div>
                              <div class="radar-score">90%</div>
                              <div class="radar-score">85%</div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </el-card>
                  </el-col>
                  <el-col :span="12">
                    <el-card shadow="hover" class="chart-card">
                      <template #header>
                        <div class="chart-header">
                          <span>司机服务质量排名</span>
                        </div>
                      </template>
                      <div class="chart-content">
                        <div class="simple-leaderboard">
                          <div class="chart-title">司机服务排名</div>
                          <div class="leaderboard-list">
                            <div class="leaderboard-item top1">
                              <div class="rank">1</div>
                              <div class="name">张师傅</div>
                              <div class="score">98</div>
                              <div class="stars">★★★★★</div>
                            </div>
                            <div class="leaderboard-item top2">
                              <div class="rank">2</div>
                              <div class="name">李师傅</div>
                              <div class="score">96</div>
                              <div class="stars">★★★★★</div>
                            </div>
                            <div class="leaderboard-item top3">
                              <div class="rank">3</div>
                              <div class="name">王师傅</div>
                              <div class="score">94</div>
                              <div class="stars">★★★★☆</div>
                            </div>
                            <div class="leaderboard-item">
                              <div class="rank">4</div>
                              <div class="name">刘师傅</div>
                              <div class="score">92</div>
                              <div class="stars">★★★★☆</div>
                            </div>
                            <div class="leaderboard-item">
                              <div class="rank">5</div>
                              <div class="name">陈师傅</div>
                              <div class="score">90</div>
                              <div class="stars">★★★★☆</div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
              </div>
              
              <!-- 质量明细 -->
              <div class="quality-detail">
                <h3>服务质量明细</h3>
                <el-table :data="qualityDetail" style="width: 100%" height="300">
                  <el-table-column prop="driverId" label="司机ID" width="100" />
                  <el-table-column prop="driverName" label="司机姓名" width="120" />
                  <el-table-column prop="signSuccessRate" label="签收成功率" width="130">
                    <template #default="scope">
                      {{ scope.row.signSuccessRate }}%
                    </template>
                  </el-table-column>
                  <el-table-column prop="goodsIntactRate" label="货损完好率" width="130">
                    <template #default="scope">
                      {{ scope.row.goodsIntactRate }}%
                    </template>
                  </el-table-column>
                  <el-table-column prop="onTimeRate" label="准时率" width="100">
                    <template #default="scope">
                      {{ scope.row.onTimeRate }}%
                    </template>
                  </el-table-column>
                  <el-table-column prop="customerRating" label="客户评分" width="100">
                    <template #default="scope">
                      <el-rate :model-value="scope.row.customerRating" disabled :colors="['#909399', '#E6A23C', '#67C23A']" />
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 异常事件分析 -->
      <el-tab-pane label="异常事件分析" name="anomaly">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>按月/季度统计异常发生的类型与根本原因，驱动流程优化</span>
              </div>
            </template>
            <div class="anomaly-content">
              <!-- 筛选条件 -->
              <div class="filter-bar">
                <el-select v-model="anomalyFilter.period" placeholder="选择周期" style="width: 120px; margin-right: 16px;">
                  <el-option label="月" value="monthly" />
                  <el-option label="季度" value="quarterly" />
                  <el-option label="年" value="yearly" />
                </el-select>
                <el-select v-model="anomalyFilter.year" placeholder="选择年份" style="width: 120px; margin-right: 16px;">
                  <el-option v-for="y in yearOptions" :key="y" :label="y" :value="y" />
                </el-select>
                <el-select v-model="anomalyFilter.month" placeholder="选择月份" style="width: 120px;">
                  <el-option label="全部月份" value="" />
                  <el-option label="1月" value="1" />
                  <el-option label="2月" value="2" />
                  <el-option label="3月" value="3" />
                  <el-option label="4月" value="4" />
                  <el-option label="5月" value="5" />
                  <el-option label="6月" value="6" />
                  <el-option label="7月" value="7" />
                  <el-option label="8月" value="8" />
                  <el-option label="9月" value="9" />
                  <el-option label="10月" value="10" />
                  <el-option label="11月" value="11" />
                  <el-option label="12月" value="12" />
                </el-select>
              </div>
              
              <!-- 异常分析图表 -->
              <div class="chart-section">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-card shadow="hover" class="chart-card">
                      <template #header>
                        <div class="chart-header">
                          <span>异常类型分布</span>
                        </div>
                      </template>
                      <div class="chart-content">
                        <el-table :data="anomalyTypeDistribution" style="width: 100%" height="260">
                          <el-table-column prop="eventType" label="异常类型" min-width="160" />
                          <el-table-column prop="count" label="次数" width="90" />
                          <el-table-column prop="percent" label="占比" width="90">
                            <template #default="scope">
                              {{ scope.row.percent }}%
                            </template>
                          </el-table-column>
                        </el-table>
                        <div class="pie-summary">
                          <div class="summary-item">
                            <span class="summary-label">总异常次数:</span>
                            <span class="summary-value">{{ anomalyTotalCount }}次</span>
                          </div>
                        </div>
                      </div>
                    </el-card>
                  </el-col>
                  <el-col :span="12">
                    <el-card shadow="hover" class="chart-card">
                      <template #header>
                        <div class="chart-header">
                          <span>异常趋势分析</span>
                        </div>
                      </template>
                      <div class="chart-content">
                        <el-empty v-if="anomalyTrendDays.length === 0" description="暂无趋势数据" />
                        <el-table v-else :data="anomalyTrendDays" style="width: 100%" height="260">
                          <el-table-column prop="date" label="日期" width="140" />
                          <el-table-column prop="count" label="异常次数" width="120" />
                        </el-table>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
              </div>
              
              <!-- 异常原因分析 -->
              <div class="anomaly-reasons">
                <h3>异常原因分析</h3>
                <el-table :data="anomalyReasons" style="width: 100%" height="300">
                  <el-table-column prop="anomalyType" label="异常类型" width="120">
                    <template #default="scope">
                      <el-tag type="danger">{{ scope.row.anomalyType }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="reason" label="根本原因" min-width="200" />
                  <el-table-column prop="count" label="发生次数" width="100">
                    <template #default="scope">
                      <el-tag type="warning">{{ scope.row.count }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="percentage" label="占比" width="100">
                    <template #default="scope">
                      {{ scope.row.percentage }}%
                    </template>
                  </el-table-column>
                  <el-table-column prop="suggestion" label="优化建议" min-width="200" />
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import useLesStore from '../../../stores/les'
import * as types from '../../../types/les'

// 激活的标签页
const activeTab = ref('efficiency')

// 初始化store
const store = useLesStore()

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  efficiency: '运输效率分析',
  utilization: '资源利用率分析',
  quality: '服务质量评估',
  anomaly: '异常事件分析'
}

// 运输效率筛选条件
const efficiencyFilter = ref({
  period: 'monthly',
  dateRange: [] as Date[],
  routeId: '' as '' | number
})

// 资源类型
const resourceType = ref('vehicle')

const efficiencyDetailAll = computed(() => {
  return store.logisticsAnalysis.map(analysis => {
    const plan = store.transportPlans.find(p => p.id === analysis.planId)
    const route = store.routes.find(r => {
      // 简化处理，实际应通过计划关联
      const planRoute = plan?.routeId
      return r.id === planRoute
    })
    return {
      planId: analysis.planId,
        planNo: plan?.planNo || '',
      routeName: route?.routeName || '未知路线',
      estimatedTime: route?.estimatedTime || 0,
      actualTime: analysis.actualDuration,
      delayTime: analysis.delayMinutes,
      efficiency: ((route?.estimatedTime || 0) / analysis.actualDuration) * 100 || 0
    }
  })
})

const efficiencyDetail = computed(() => {
  const routeId = efficiencyFilter.value.routeId
  const [start, end] = (efficiencyFilter.value.dateRange || []) as any[]
  const startTime = start ? new Date(start).getTime() : undefined
  const endTime = end ? new Date(end).getTime() : undefined
  return efficiencyDetailAll.value.filter(row => {
    if (routeId && store.transportPlans.find(p => p.id === row.planId)?.routeId !== routeId) return false
    if (startTime || endTime) {
      const plan = store.transportPlans.find(p => p.id === row.planId)
      const t = plan?.createTime ? new Date(plan.createTime).getTime() : 0
      if (startTime && t < startTime) return false
      if (endTime && t > endTime + 24 * 60 * 60 * 1000 - 1) return false
    }
    return true
  })
})

const routeTimeCompareTop = computed(() => {
  const map = new Map<string, { routeName: string; plannedSum: number; actualSum: number; count: number; delayCount: number }>()
  for (const row of efficiencyDetail.value) {
    const key = row.routeName || '未知路线'
    const current = map.get(key) || { routeName: key, plannedSum: 0, actualSum: 0, count: 0, delayCount: 0 }
    current.plannedSum += Number(row.estimatedTime) || 0
    current.actualSum += Number(row.actualTime) || 0
    current.count += 1
    if ((Number(row.delayTime) || 0) > 0) current.delayCount += 1
    map.set(key, current)
  }
  const items = Array.from(map.values())
    .map(item => {
      const planned = item.count > 0 ? item.plannedSum / item.count : 0
      const actual = item.count > 0 ? item.actualSum / item.count : 0
      return {
        routeName: item.routeName,
        planned: Number(planned.toFixed(1)),
        actual: Number(actual.toFixed(1)),
        hasDelay: item.delayCount > 0
      }
    })
    .sort((a, b) => b.actual - a.actual)
    .slice(0, 5)
  const max = Math.max(1, ...items.map(i => Math.max(i.planned, i.actual)))
  return items.map(i => ({
    ...i,
    plannedPercent: Number(((i.planned / max) * 100).toFixed(1)),
    actualPercent: Number(((i.actual / max) * 100).toFixed(1))
  }))
})

const delayRouteDistribution = computed(() => {
  const delayed = efficiencyDetail.value.filter(r => (Number(r.delayTime) || 0) > 0)
  const total = delayed.length
  const map = new Map<string, { routeName: string; delayCount: number; delaySum: number }>()
  for (const row of delayed) {
    const key = row.routeName || '未知路线'
    const current = map.get(key) || { routeName: key, delayCount: 0, delaySum: 0 }
    current.delayCount += 1
    current.delaySum += Number(row.delayTime) || 0
    map.set(key, current)
  }
  return Array.from(map.values())
    .map(item => ({
      routeName: item.routeName,
      delayCount: item.delayCount,
      avgDelayMinutes: Number((item.delayCount > 0 ? item.delaySum / item.delayCount : 0).toFixed(1)),
      percent: total > 0 ? Number(((item.delayCount / total) * 100).toFixed(1)) : 0
    }))
    .sort((a, b) => b.delayCount - a.delayCount)
})

const totalDelayCount = computed(() => delayRouteDistribution.value.reduce((sum, r) => sum + r.delayCount, 0))
const avgDelayMinutes = computed(() => {
  const totalCount = totalDelayCount.value
  if (totalCount <= 0) return 0
  const totalMinutes = delayRouteDistribution.value.reduce((sum, r) => sum + r.avgDelayMinutes * r.delayCount, 0)
  return Number((totalMinutes / totalCount).toFixed(1))
})

// 车辆利用率数据（从store计算）
const vehicleUtilization = computed(() => {
  return store.vehicles.map(vehicle => {
    const plans = store.transportPlans.filter(p => p.vehicleId === vehicle.id)
    const dates = new Set(plans.map(p => (p.createTime || '').slice(0, 10)).filter(Boolean))
    const mileage = plans
      .map(p => store.routes.find(r => r.id === p.routeId)?.distance || 0)
      .reduce((sum, v) => sum + v, 0)
    const activeDays = dates.size
    const idleDays = Math.max(0, 30 - activeDays)
    const utilizationRate = activeDays > 0 ? (activeDays / 30) * 100 : 0
    const analysis = store.logisticsAnalysis.find(a => plans.some(p => p.id === a.planId))
    return {
      id: vehicle.id,
      licensePlate: vehicle.licensePlate,
      vehicleType: vehicle.vehicleType,
      dailyMileage: Number((mileage / 30).toFixed(1)),
      idleDays,
      loadRate: vehicle.loadCapacity ? 80 : 0,
      utilizationRate: analysis?.vehicleUtilization || Number(utilizationRate.toFixed(1))
    }
  })
})

// 司机利用率数据（从store计算）
const driverUtilization = computed(() => {
  return store.drivers.map(driver => {
    const plans = store.transportPlans.filter(p => p.driverId === driver.id)
    const dates = new Set(plans.map(p => (p.createTime || '').slice(0, 10)).filter(Boolean))
    const mileage = plans
      .map(p => store.routes.find(r => r.id === p.routeId)?.distance || 0)
      .reduce((sum, v) => sum + v, 0)
    const workDays = dates.size
    const utilizationRate = workDays > 0 ? (workDays / 30) * 100 : 0
    const avgWorkHours = workDays > 0 ? (plans.reduce((sum, p) => sum + (store.logisticsAnalysis.find(a => a.planId === p.id)?.actualDuration || 0), 0) / 60 / workDays) : 0
    return {
      id: driver.id,
      name: driver.name,
      phone: driver.phone,
      dailyMileage: Number((mileage / 30).toFixed(1)),
      workDays,
      avgWorkHours: Number(avgWorkHours.toFixed(1)),
      utilizationRate: Number(utilizationRate.toFixed(1))
    }
  })
})

// 资源利用率列表（计算属性）
const resourceUtilization = computed(() => {
  return resourceType.value === 'vehicle' ? vehicleUtilization.value : driverUtilization.value
})

const utilizationSummary = computed(() => {
  const list: any[] = resourceUtilization.value as any[]
  const count = list.length
  const avgUtilizationRate = count > 0 ? list.reduce((sum, r) => sum + (Number(r.utilizationRate) || 0), 0) / count : 0
  if (resourceType.value === 'vehicle') {
    const avgLoadRate = count > 0 ? list.reduce((sum, r) => sum + (Number(r.loadRate) || 0), 0) / count : 0
    return {
      count,
      avgUtilizationRate: Number(avgUtilizationRate.toFixed(1)),
      avgLoadRate: Number(avgLoadRate.toFixed(1)),
      avgWorkHours: 0
    }
  }
  const avgWorkHours = count > 0 ? list.reduce((sum, r) => sum + (Number(r.avgWorkHours) || 0), 0) / count : 0
  return {
    count,
    avgUtilizationRate: Number(avgUtilizationRate.toFixed(1)),
    avgLoadRate: 0,
    avgWorkHours: Number(avgWorkHours.toFixed(1))
  }
})

// 综合评分（从store计算）
const overallScore = computed(() => {
  const total = store.serviceQualities.length
  const avg = total > 0 ? store.serviceQualities.reduce((sum, q) => sum + q.customerSatisfaction, 0) / total : 0
  return Number.isFinite(avg) ? Number(avg.toFixed(1)) : 0
})
const signSuccessRate = computed(() => store.transportStats.averageSignSuccessRate)
const goodsIntactRate = computed(() => {
  const total = store.serviceQualities.length
  const avg = total > 0 ? store.serviceQualities.reduce((sum, q) => sum + q.cargoIntegrityRate, 0) / total : 0
  return Number.isFinite(avg) ? Number(avg.toFixed(1)) : 0
})
const customerSatisfaction = computed(() => {
  const total = store.serviceQualities.length
  const avg = total > 0 ? store.serviceQualities.reduce((sum, q) => sum + q.customerSatisfaction, 0) / total : 0
  return Number.isFinite(avg) ? Number(avg.toFixed(1)) : 0
})

// 服务质量明细（从store计算）
const qualityDetail = computed(() => {
  return store.serviceQualities.map(quality => {
    const plan = store.transportPlans.find(p => p.id === quality.planId)
    const driver = store.drivers.find(d => {
      const planDriver = plan?.driverId
      return d.id === planDriver
    })
    return {
      driverId: driver?.id || 0,
      driverName: driver?.name || '未知',
      signSuccessRate: quality.signSuccessRate,
      goodsIntactRate: quality.cargoIntegrityRate,
      onTimeRate: quality.onTimeRate,
      customerRating: quality.customerSatisfaction
    }
  })
})

// 异常筛选条件
const anomalyFilter = ref({
  period: 'monthly',
  year: String(new Date().getFullYear()),
  month: ''
})

const yearOptions = computed(() => {
  const y = new Date().getFullYear()
  return [String(y), String(y - 1), String(y - 2)]
})

const anomalyTypeDistribution = computed(() => {
  const filtered = store.anomalyEvents.filter(e => {
    const t = e.eventTime || e.createTime
    if (!t) return false
    const d = new Date(t)
    if (Number.isNaN(d.getTime())) return false
    const year = String(d.getFullYear())
    if (anomalyFilter.value.year && year !== anomalyFilter.value.year) return false
    if (anomalyFilter.value.month) {
      if (d.getMonth() + 1 !== Number(anomalyFilter.value.month)) return false
    }
    return true
  })

  const total = filtered.length
  const map = new Map<string, number>()
  filtered.forEach(e => {
    const key = e.eventType || '未知'
    map.set(key, (map.get(key) || 0) + 1)
  })
  return Array.from(map.entries())
    .map(([eventType, count]) => ({
      eventType,
      count,
      percent: total > 0 ? Number(((count / total) * 100).toFixed(1)) : 0
    }))
    .sort((a, b) => b.count - a.count)
})

const anomalyTotalCount = computed(() => anomalyTypeDistribution.value.reduce((sum, r) => sum + r.count, 0))

const anomalyTrendDays = computed(() => {
  const filtered = store.anomalyEvents.filter(e => {
    const t = e.eventTime || e.createTime
    if (!t) return false
    const d = new Date(t)
    if (Number.isNaN(d.getTime())) return false
    const year = String(d.getFullYear())
    if (anomalyFilter.value.year && year !== anomalyFilter.value.year) return false
    if (anomalyFilter.value.month) {
      if (d.getMonth() + 1 !== Number(anomalyFilter.value.month)) return false
    }
    return true
  })
  const map = new Map<string, number>()
  filtered.forEach(e => {
    const t = e.eventTime || e.createTime
    const key = String(t).slice(0, 10)
    if (!key) return
    map.set(key, (map.get(key) || 0) + 1)
  })
  return Array.from(map.entries())
    .map(([date, count]) => ({ date, count }))
    .sort((a, b) => (a.date < b.date ? 1 : -1))
    .slice(0, 14)
})

// 异常原因分析（从store计算）
const anomalyReasons = computed(() => {
  const filtered = store.anomalyEvents.filter(e => {
    const t = e.eventTime || e.createTime
    if (!t) return false
    const d = new Date(t)
    if (Number.isNaN(d.getTime())) return false
    const year = String(d.getFullYear())
    if (anomalyFilter.value.year && year !== anomalyFilter.value.year) return false
    if (anomalyFilter.value.month) {
      if (d.getMonth() + 1 !== Number(anomalyFilter.value.month)) return false
    }
    return true
  })
  const total = filtered.length
  const map = new Map<string, { anomalyType: string; reason: string; count: number }>()
  filtered.forEach(e => {
    const type = e.eventType || '未知'
    const reason = e.eventDescription || ''
    const key = `${type}||${reason}`
    const current = map.get(key) || { anomalyType: type, reason, count: 0 }
    current.count += 1
    map.set(key, current)
  })
  const suggestionByType: Record<string, string> = {
    '交通拥堵': '优化路线规划与发车时间窗口',
    '车辆故障': '完善保养计划与车况巡检',
    '货物异常': '加强装卸规范与异常拍照取证',
    '延误': '优化计划编排与关键节点预警'
  }
  return Array.from(map.values())
    .map(r => ({
      ...r,
      percentage: total > 0 ? Number(((r.count / total) * 100).toFixed(1)) : 0,
      suggestion: suggestionByType[r.anomalyType] || '完善异常分级与处理闭环'
    }))
    .sort((a, b) => b.count - a.count)
})

// 组件挂载时加载数据
onMounted(async () => {
  await Promise.all([
    store.fetchTransportPlans(),
    store.fetchVehicles(),
    store.fetchDrivers(),
    store.fetchRoutes(),
    store.fetchLogisticsAnalysis(),
    store.fetchServiceQualities(),
    store.fetchTransportStats(),
    store.fetchAnomalyEvents()
  ])
})

// 利用率进度条格式化
const utilizationFormat = (percentage: number) => {
  return `${percentage}%`
}

// 获取进度条颜色
const getProgressColor = (percentage: number) => {
  if (percentage < 60) {
    return '#F56C6C'
  } else if (percentage < 80) {
    return '#E6A23C'
  } else {
    return '#67C23A'
  }
}
</script>

<style scoped>
.logistics-analysis-view {
  padding: 20px;
  height: 100%;
  overflow: auto;
}

/* 页面头部样式 */
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color-light);
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-size: 1.8rem;
}

/* 覆盖默认的h2样式，确保只影响页面标题 */
h2 {
  margin-bottom: 0;
}

/* 功能标签页 */
.function-tabs {
  margin-bottom: 20px;
}

.tab-content {
  padding: 10px 0;
}

/* 卡片样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

/* 图表区域 */
.chart-section {
  margin-bottom: 20px;
}

.chart-card {
  height: 300px;
}

.chart-header {
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
}

.chart-content {
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 效率明细 */
.efficiency-detail {
  margin-top: 20px;
}

.efficiency-detail h3 {
  margin-bottom: 16px;
  font-size: 1.2rem;
}

/* 资源类型切换 */
.resource-type-switch {
  margin-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

/* 利用率列表 */
.utilization-list {
  margin-top: 20px;
}

.utilization-list h3 {
  margin-bottom: 16px;
  font-size: 1.2rem;
}

/* 质量评分卡片 */
.quality-scores {
  margin-bottom: 20px;
}

.score-card {
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.score-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 10px;
}

.score-header h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #606266;
}

.score-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
}

/* 质量明细 */
.quality-detail {
  margin-top: 20px;
}

.quality-detail h3 {
  margin-bottom: 16px;
  font-size: 1.2rem;
}

/* 异常原因分析 */
.anomaly-reasons {
  margin-top: 20px;
}

.anomaly-reasons h3 {
  margin-bottom: 16px;
  font-size: 1.2rem;
}

/* 简单图表样式 */
.simple-chart .chart-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 16px;
  text-align: center;
  color: var(--text-primary);
}

/* 柱状图样式 */
.chart-bars {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 16px;
}

.chart-bar-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-bar-item .bar-label {
  width: 80px;
  font-size: 12px;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.chart-bar-item .bar-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chart-bar-item .bar {
  height: 12px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.chart-bar-item .bar:hover {
  opacity: 0.8;
}

.chart-bar-item .bar-values {
  width: 80px;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
  display: flex;
  gap: 20px;
  justify-content: flex-end;
}

.chart-bar-legend {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-top: 12px;
  font-size: 12px;
  color: var(--text-secondary);
}

.legend-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 4px;
  vertical-align: middle;
}

/* 饼图样式 */
.simple-pie-chart .pie-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 16px;
  text-align: center;
  color: var(--text-primary);
}

.pie-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-bottom: 16px;
}

.pie-stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-circle {
  transition: all 0.3s ease;
}

.stat-circle:hover {
  transform: scale(1.1);
}

.stat-data {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-color {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  vertical-align: middle;
}

.pie-summary {
  display: flex;
  justify-content: center;
  gap: 32px;
  font-size: 12px;
  margin-top: 16px;
}

.summary-item {
  display: flex;
  gap: 8px;
}

.summary-label {
  color: var(--text-secondary);
}

.summary-value {
  font-weight: bold;
  color: var(--text-primary);
}

/* 折线图样式 */
.simple-line-chart .chart-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 16px;
  text-align: center;
  color: var(--text-primary);
}

.chart-axis {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 180px;
  padding: 0 16px;
}

.axis-labels {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  font-size: 10px;
  color: var(--text-secondary);
  flex-shrink: 0;
  width: 20px;
  text-align: right;
}

.chart-line-container {
  flex: 1;
  position: relative;
  height: 100%;
  border-bottom: 1px solid var(--border-color);
  border-left: 1px solid var(--border-color);
}

.line-chart-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: linear-gradient(to top, var(--border-color-light) 1px, transparent 1px),
                    linear-gradient(to right, var(--border-color-light) 1px, transparent 1px);
  background-size: 100% 20%, 20% 100%;
}

.line-chart-data {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.line-chart-line {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(to right, transparent 0%, #409EFF 100%);
  transform: translateY(-50%);
}

.line-chart-dots {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.line-chart-dots .dot {
  position: absolute;
  width: 8px;
  height: 8px;
  background: #409EFF;
  border: 2px solid white;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.line-chart-dots .dot:hover {
  transform: scale(1.5);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.time-labels {
  position: absolute;
  bottom: -16px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: var(--text-secondary);
}

/* 雷达图样式 */
.simple-radar-chart .chart-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 16px;
  text-align: center;
  color: var(--text-primary);
}

.radar-container {
  position: relative;
  width: 100%;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.radar-grid {
  position: absolute;
  width: 140px;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.radar-circle {
  position: absolute;
  border: 1px solid var(--border-color-light);
  border-radius: 50%;
}

.radar-circle:nth-child(1) {
  width: 100%;
  height: 100%;
}

.radar-circle:nth-child(2) {
  width: 80%;
  height: 80%;
}

.radar-circle:nth-child(3) {
  width: 60%;
  height: 60%;
}

.radar-circle:nth-child(4) {
  width: 40%;
  height: 40%;
}

.radar-circle:nth-child(5) {
  width: 20%;
  height: 20%;
}

.radar-axis {
  position: absolute;
  width: 1px;
  height: 100%;
  background: var(--border-color-light);
  transform-origin: center;
}

.radar-axis:nth-child(6) {
  transform: rotate(0deg);
}

.radar-axis:nth-child(7) {
  transform: rotate(72deg);
}

.radar-axis:nth-child(8) {
  transform: rotate(144deg);
}

.radar-axis:nth-child(9) {
  transform: rotate(216deg);
}

.radar-axis:nth-child(10) {
  transform: rotate(288deg);
}

.radar-data-area {
  position: absolute;
  width: 140px;
  height: 140px;
  background: rgba(64, 158, 255, 0.1);
  clip-path: polygon(50% 10%, 80% 35%, 70% 75%, 30% 75%, 20% 35%);
  border: 1px solid #409EFF;
}

.radar-labels {
  position: absolute;
  width: 180px;
  height: 180px;
}

.radar-label {
  position: absolute;
  font-size: 10px;
  color: var(--text-secondary);
  text-align: center;
  transform: translate(-50%, -50%);
}

.radar-label:nth-child(1) {
  top: 0;
  left: 50%;
}

.radar-label:nth-child(2) {
  top: 25%;
  right: 0;
}

.radar-label:nth-child(3) {
  bottom: 25%;
  right: 5%;
}

.radar-label:nth-child(4) {
  bottom: 5%;
  left: 30%;
}

.radar-label:nth-child(5) {
  bottom: 25%;
  left: 5%;
}

.radar-scores {
  position: absolute;
  width: 140px;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.radar-score {
  position: absolute;
  font-size: 10px;
  font-weight: bold;
  color: #409EFF;
  transform: translate(-50%, -50%);
}

.radar-score:nth-child(1) {
  top: 15%;
  left: 50%;
}

.radar-score:nth-child(2) {
  top: 30%;
  right: 20%;
}

.radar-score:nth-child(3) {
  bottom: 30%;
  right: 15%;
}

.radar-score:nth-child(4) {
  bottom: 15%;
  left: 35%;
}

.radar-score:nth-child(5) {
  bottom: 30%;
  left: 20%;
}

/* 排行榜样式 */
.simple-leaderboard .chart-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 16px;
  text-align: center;
  color: var(--text-primary);
}

.leaderboard-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 0 16px;
}

.leaderboard-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: var(--bg-color);
  border-radius: 6px;
  font-size: 12px;
}

.leaderboard-item.top1 {
  background: linear-gradient(135deg, #FFF3CD 0%, #FFF9EB 100%);
  border-left: 3px solid #E6A23C;
}

.leaderboard-item.top2 {
  background: linear-gradient(135deg, #D1ECF1 0%, #E8F5F7 100%);
  border-left: 3px solid #409EFF;
}

.leaderboard-item.top3 {
  background: linear-gradient(135deg, #D4EDDA 0%, #E9F8EB 100%);
  border-left: 3px solid #67C23A;
}

.leaderboard-item .rank {
  width: 20px;
  text-align: center;
  font-weight: bold;
  color: var(--text-secondary);
}

.leaderboard-item.top1 .rank,
.leaderboard-item.top2 .rank,
.leaderboard-item.top3 .rank {
  color: var(--primary-color);
}

.leaderboard-item .name {
  flex: 1;
  color: var(--text-primary);
}

.leaderboard-item .score {
  width: 30px;
  text-align: right;
  font-weight: bold;
  color: var(--primary-color);
}

.leaderboard-item .stars {
  width: 60px;
  text-align: right;
  color: #E6A23C;
  font-size: 10px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .logistics-analysis-view {
    padding: 16px;
  }
  
  h2 {
    font-size: 1.5rem;
  }
}

@media (max-width: 768px) {
  .logistics-analysis-view {
    padding: 12px;
  }
  
  h2 {
    font-size: 1.3rem;
    margin-bottom: 16px;
  }
  
  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .filter-bar .el-select {
    width: 100% !important;
  }
  
  .chart-section .el-col,
  .quality-scores .el-col {
    :span: 24;
  }
  
  .chart-card,
  .score-card {
    margin-bottom: 20px;
  }
}
</style>
