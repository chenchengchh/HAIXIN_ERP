<template>
  <div class="mes-data-collection-container">
    <!-- 页面头部和面包屑导航 -->
    <div class="page-header">
      <h2>数据采集管理</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home/mes">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/mes">MES系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/mes/data-collection">数据采集</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/mes/data-collection/${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <!-- 人工报工采集 -->
        <el-tab-pane label="人工报工采集" name="manual">
          <el-card shadow="hover" class="content-card">
            <div class="card-header">
              <h2>人工报工采集</h2>
              <el-button type="primary" @click="showManualReportDialog = true">
                <el-icon><Plus /></el-icon> 新增报工
              </el-button>
            </div>
            <div class="card-content">
              <el-table v-loading="loading.manualReportings" :data="manualReportings" style="width: 100%">
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
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.status === 'submitted' ? 'info' : 
                             scope.row.status === 'verified' ? 'warning' : 'success'">
                      {{ statusMap[scope.row.status] }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="scope">
                    <el-button 
                      v-if="scope.row.status === 'submitted'" 
                      type="warning" 
                      size="small" 
                      @click="verifyManualReport(scope.row.id)">
                      验证
                    </el-button>
                    <el-button 
                      v-if="scope.row.status === 'verified'" 
                      type="success" 
                      size="small" 
                      @click="approveManualReport(scope.row.id)">
                      批准
                    </el-button>
                    <el-button type="info" size="small" @click="viewManualReportDetail(scope.row)">
                      详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-tab-pane>

        <!-- 设备数据自动采集 -->
        <el-tab-pane label="设备数据自动采集" name="equipment">
          <el-card shadow="hover" class="content-card">
            <div class="card-header">
              <h2>设备数据自动采集</h2>
              <el-select v-model="selectedEquipmentId" placeholder="选择设备" style="width: 200px; margin-right: 10px">
                <el-option 
                  v-for="equipment in equipmentList" 
                  :key="equipment.id" 
                  :label="equipment.name" 
                  :value="equipment.id" />
              </el-select>
              <el-button @click="fetchRealTimeData">获取实时数据</el-button>
            </div>
            <div class="card-content">
              <el-table v-loading="loading.equipmentData" :data="equipmentData" style="width: 100%">
                <el-table-column prop="equipmentName" label="设备名称" min-width="120" />
                <el-table-column prop="parameterName" label="参数名称" min-width="120" />
                <el-table-column prop="parameterValue" label="参数值" width="100" />
                <el-table-column prop="unit" label="单位" width="80" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.status === 'normal' ? 'success' : 
                             scope.row.status === 'warning' ? 'warning' : 'danger'">
                      {{ paramStatusMap[scope.row.status] }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="timestamp" label="采集时间" width="150" />
              </el-table>
            </div>
          </el-card>
        </el-tab-pane>

        <!-- 质量检验数据采集 -->
        <el-tab-pane label="质量检验数据采集" name="quality">
          <el-card shadow="hover" class="content-card">
            <div class="card-header">
              <h2>质量检验数据采集</h2>
              <el-button type="primary" @click="showQualityInspectionDialog = true">
                <el-icon><Plus /></el-icon> 新增检验记录
              </el-button>
            </div>
            <div class="card-content">
              <el-table v-loading="loading.qualityInspections" :data="qualityInspections" style="width: 100%">
                <el-table-column prop="inspectionName" label="检验名称" min-width="120" />
                <el-table-column prop="workOrderNo" label="工单号" min-width="120" />
                <el-table-column prop="snCode" label="产品序列号" min-width="150" />
                <el-table-column prop="stepName" label="工序名称" min-width="120" />
                <el-table-column prop="inspectorName" label="检验员" min-width="100" />
                <el-table-column prop="inspectionTime" label="检验时间" width="150" />
                <el-table-column prop="result" label="检验结果" width="100">
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.result === 'pass' ? 'success' : 'danger'">
                      {{ resultMap[scope.row.result] }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.status === 'pending' ? 'info' : 
                             scope.row.status === 'completed' ? 'success' : 'danger'">
                      {{ inspectionStatusMap[scope.row.status] }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="scope">
                    <el-button type="info" size="small" @click="viewQualityInspectionDetail(scope.row)">
                      详情
                    </el-button>
                    <el-button 
                      v-if="scope.row.status === 'pending'" 
                      type="primary" 
                      size="small" 
                      @click="completeInspection(scope.row.id)">
                      完成检验
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-tab-pane>
        
        <!-- 数据统计分析 -->
        <el-tab-pane label="数据统计分析" name="analysis">
          <el-card shadow="hover" class="content-card">
            <div class="card-header">
              <h2>质量检验统计分析</h2>
            </div>
            <div class="card-content">
              <div class="chart-container">
                <div class="chart-item">
                  <h3>质量检验结果分布</h3>
                  <div id="qualityResultChart" class="chart"></div>
                </div>
                <div class="chart-item">
                  <h3>设备运行状态统计</h3>
                  <div id="equipmentStatusChart" class="chart"></div>
                </div>
              </div>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 人工报工对话框 -->
    <el-dialog
      v-model="showManualReportDialog"
      title="新增人工报工"
      width="700px"
      destroy-on-close>
      <el-form :model="newManualReport" label-position="top" :rules="manualReportRules" ref="manualReportFormRef">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工单号" prop="workOrderNo">
              <el-input v-model="newManualReport.workOrderNo" placeholder="请输入工单号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工序名称" prop="stepName">
              <el-input v-model="newManualReport.stepName" placeholder="请输入工序名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工站名称" prop="workstationName">
              <el-input v-model="newManualReport.workstationName" placeholder="请输入工站名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作员" prop="operatorName">
              <el-input v-model="newManualReport.operatorName" placeholder="请输入操作员姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="newManualReport.startTime"
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
                v-model="newManualReport.endTime"
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
              <el-input-number v-model="newManualReport.goodQty" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="废品数" prop="scrapQty">
              <el-input-number v-model="newManualReport.scrapQty" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="返工数" prop="reworkQty">
              <el-input-number v-model="newManualReport.reworkQty" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工时(小时)" prop="workingHours">
              <el-input-number v-model="newManualReport.workingHours" :min="0" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showManualReportDialog = false">取消</el-button>
          <el-button type="primary" @click="submitManualReport">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 质量检验对话框 -->
    <el-dialog
      v-model="showQualityInspectionDialog"
      title="新增质量检验记录"
      width="700px"
      destroy-on-close>
      <el-form :model="newQualityInspection" label-position="top" :rules="qualityInspectionRules" ref="qualityInspectionFormRef">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="检验名称" prop="inspectionName">
              <el-input v-model="newQualityInspection.inspectionName" placeholder="请输入检验名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工单号" prop="workOrderNo">
              <el-input v-model="newQualityInspection.workOrderNo" placeholder="请输入工单号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品序列号" prop="snCode">
              <el-input v-model="newQualityInspection.snCode" placeholder="请输入产品序列号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工序名称" prop="stepName">
              <el-input v-model="newQualityInspection.stepName" placeholder="请输入工序名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="检验员" prop="inspectorName">
              <el-input v-model="newQualityInspection.inspectorName" placeholder="请输入检验员姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检验结果" prop="result">
              <el-radio-group v-model="newQualityInspection.result">
                <el-radio value="pass">合格</el-radio>
                <el-radio value="fail">不合格</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showQualityInspectionDialog = false">取消</el-button>
          <el-button type="primary" @click="submitQualityInspection">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 人工报工详情对话框 -->
    <el-dialog
      v-model="showManualReportDetailDialog"
      title="人工报工详情"
      width="700px"
      destroy-on-close>
      <div v-if="selectedManualReport" class="detail-dialog">
        <el-descriptions :column="1" border class="detail-descriptions">
          <el-descriptions-item label="基本信息">
            <template #children>
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">工单号：</span>
                    <span class="detail-value">{{ selectedManualReport.workOrderNo }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">工序名称：</span>
                    <span class="detail-value">{{ selectedManualReport.stepName }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">工站名称：</span>
                    <span class="detail-value">{{ selectedManualReport.workstationName }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">操作员：</span>
                    <span class="detail-value">{{ selectedManualReport.operatorName }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">状态：</span>
                    <span class="detail-value">
                      <el-tag
                        :type="selectedManualReport.status === 'submitted' ? 'info' : 
                               selectedManualReport.status === 'verified' ? 'warning' : 'success'">
                        {{ statusMap[selectedManualReport.status] }}
                      </el-tag>
                    </span>
                  </div>
                </el-col>
              </el-row>
            </template>
          </el-descriptions-item>
          <el-descriptions-item label="时间信息">
            <template #children>
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">开始时间：</span>
                    <span class="detail-value">{{ selectedManualReport.startTime }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">结束时间：</span>
                    <span class="detail-value">{{ selectedManualReport.endTime }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">工时：</span>
                    <span class="detail-value">{{ selectedManualReport.workingHours }} 小时</span>
                  </div>
                </el-col>
              </el-row>
            </template>
          </el-descriptions-item>
          <el-descriptions-item label="生产数量">
            <template #children>
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="detail-item">
                    <span class="detail-label">良品数：</span>
                    <span class="detail-value good-qty">{{ selectedManualReport.goodQty }}</span>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="detail-item">
                    <span class="detail-label">废品数：</span>
                    <span class="detail-value scrap-qty">{{ selectedManualReport.scrapQty }}</span>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="detail-item">
                    <span class="detail-label">返工数：</span>
                    <span class="detail-value rework-qty">{{ selectedManualReport.reworkQty }}</span>
                  </div>
                </el-col>
              </el-row>
            </template>
          </el-descriptions-item>
          <el-descriptions-item label="备注信息">
            <template #children>
              <div class="detail-item">
                <span class="detail-label">备注：</span>
                <span class="detail-value">{{ selectedManualReport.remarks || '无' }}</span>
              </div>
            </template>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showManualReportDetailDialog = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 质量检验详情对话框 -->
    <el-dialog
      v-model="showQualityInspectionDetailDialog"
      title="质量检验详情"
      width="700px"
      destroy-on-close>
      <div v-if="selectedQualityInspection" class="detail-dialog">
        <el-descriptions :column="1" border class="detail-descriptions">
          <el-descriptions-item label="基本信息">
            <template #children>
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">检验名称：</span>
                    <span class="detail-value">{{ selectedQualityInspection.inspectionName }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">工单号：</span>
                    <span class="detail-value">{{ selectedQualityInspection.workOrderNo }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">产品序列号：</span>
                    <span class="detail-value">{{ selectedQualityInspection.snCode }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">工序名称：</span>
                    <span class="detail-value">{{ selectedQualityInspection.stepName }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">检验员：</span>
                    <span class="detail-value">{{ selectedQualityInspection.inspectorName }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">检验时间：</span>
                    <span class="detail-value">{{ selectedQualityInspection.inspectionTime }}</span>
                  </div>
                </el-col>
              </el-row>
            </template>
          </el-descriptions-item>
          <el-descriptions-item label="检验结果">
            <template #children>
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">检验结果：</span>
                    <span class="detail-value">
                      <el-tag
                        :type="selectedQualityInspection.result === 'pass' ? 'success' : 'danger'">
                        {{ resultMap[selectedQualityInspection.result] }}
                      </el-tag>
                    </span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">状态：</span>
                    <span class="detail-value">
                      <el-tag
                        :type="selectedQualityInspection.status === 'pending' ? 'info' : 
                               selectedQualityInspection.status === 'completed' ? 'success' : 'danger'">
                        {{ inspectionStatusMap[selectedQualityInspection.status] }}
                      </el-tag>
                    </span>
                  </div>
                </el-col>
              </el-row>
            </template>
          </el-descriptions-item>
          <el-descriptions-item label="不合格信息" v-if="selectedQualityInspection.result === 'fail'">
            <template #children>
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">缺陷类型：</span>
                    <span class="detail-value">{{ selectedQualityInspection.defectType || '无' }}</span>
                  </div>
                </el-col>
                <el-col :span="24">
                  <div class="detail-item">
                    <span class="detail-label">缺陷描述：</span>
                    <span class="detail-value">{{ selectedQualityInspection.defectDescription || '无' }}</span>
                  </div>
                </el-col>
              </el-row>
            </template>
          </el-descriptions-item>
          <el-descriptions-item label="检验项目" v-if="selectedQualityInspection.inspectionItems && selectedQualityInspection.inspectionItems.length > 0">
            <template #children>
              <el-table :data="selectedQualityInspection.inspectionItems" style="width: 100%" size="small">
                <el-table-column prop="itemName" label="项目名称" min-width="120" />
                <el-table-column prop="standardValue" label="标准值" width="100" />
                <el-table-column prop="actualValue" label="实际值" width="100" />
                <el-table-column prop="result" label="结果" width="80">
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.result === 'pass' ? 'success' : 'danger'">
                      {{ resultMap[scope.row.result] }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="remarks" label="备注" min-width="150" />
              </el-table>
            </template>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showQualityInspectionDetailDialog = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useMesDataCollectionStore } from '../../../stores/mes/data-collection'
import { storeToRefs } from 'pinia'
import type { ManualReporting, QualityInspection } from '../../../api/mes/data-collection'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

// 状态管理
// 使用storeToRefs保持state响应性，actions直接解构
const store = useMesDataCollectionStore()
const { 
  manualReportings, 
  equipmentData, 
  qualityInspections,
  loading
} = storeToRefs(store)
const {
  fetchManualReportings,
  fetchEquipmentData,
  fetchQualityInspections,
  createManualReporting,
  updateManualReportingStatus,
  fetchRealTimeEquipmentData,
  createQualityInspection,
  updateQualityInspectionStatus
} = store

// 状态映射
const statusMap: Record<string, string> = {
  submitted: '已提交',
  verified: '已验证',
  approved: '已批准'
}

const paramStatusMap: Record<string, string> = {
  normal: '正常',
  warning: '警告',
  alarm: '报警'
}

const resultMap: Record<string, string> = {
  pass: '合格',
  fail: '不合格'
}

const inspectionStatusMap: Record<string, string> = {
  pending: '待处理',
  completed: '已完成',
  rejected: '已驳回'
}

// 标签页
const activeTab = ref('manual')

// 标签页名称映射，用于面包屑导航
const tabLabelMap: Record<string, string> = {
  manual: '人工报工采集',
  equipment: '设备数据自动采集',
  quality: '质量检验数据采集',
  analysis: '数据统计分析'
}

// 设备选择
const selectedEquipmentId = ref('')
const equipmentList = ref([
  { id: '1', name: '设备1' },
  { id: '2', name: '设备2' },
  { id: '3', name: '设备3' }
])

// 人工报工对话框
const showManualReportDialog = ref(false)
const manualReportFormRef = ref()
const newManualReport = reactive<Partial<ManualReporting>>({
  workOrderNo: '',
  stepName: '',
  workstationName: '',
  operatorName: '',
  startTime: new Date().toISOString(),
  endTime: new Date().toISOString(),
  goodQty: 0,
  scrapQty: 0,
  reworkQty: 0,
  workingHours: 0
})

const manualReportRules = {
  workOrderNo: [{ required: true, message: '请输入工单号', trigger: 'blur' }],
  stepName: [{ required: true, message: '请输入工序名称', trigger: 'blur' }],
  workstationName: [{ required: true, message: '请输入工站名称', trigger: 'blur' }],
  operatorName: [{ required: true, message: '请输入操作员姓名', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  goodQty: [{ required: true, type: 'number', min: 0, message: '良品数必须大于等于0', trigger: 'blur' }]
}

// 质量检验对话框
const showQualityInspectionDialog = ref(false)
const qualityInspectionFormRef = ref()
const newQualityInspection = reactive<Partial<QualityInspection>>({
  inspectionName: '',
  workOrderNo: '',
  snCode: '',
  stepName: '',
  inspectorName: '',
  inspectionTime: new Date().toISOString(),
  result: 'pass',
  defectType: '',
  defectDescription: '',
  inspectionItems: [],
  status: 'pending'
})

const qualityInspectionRules = {
  inspectionName: [{ required: true, message: '请输入检验名称', trigger: 'blur' }],
  workOrderNo: [{ required: true, message: '请输入工单号', trigger: 'blur' }],
  snCode: [{ required: true, message: '请输入产品序列号', trigger: 'blur' }],
  stepName: [{ required: true, message: '请输入工序名称', trigger: 'blur' }],
  inspectorName: [{ required: true, message: '请输入检验员姓名', trigger: 'blur' }],
  inspectionTime: [{ required: true, message: '请选择检验时间', trigger: 'change' }],
  result: [{ required: true, message: '请选择检验结果', trigger: 'change' }]
}

// 人工报工详情对话框
const showManualReportDetailDialog = ref(false)
const selectedManualReport = ref<ManualReporting | null>(null)

// 质量检验详情对话框
const showQualityInspectionDetailDialog = ref(false)
const selectedQualityInspection = ref<QualityInspection | null>(null)

// 页面加载时获取数据
onMounted(() => {
  fetchManualReportings()
  fetchEquipmentData()
  fetchQualityInspections()
})

// 提交人工报工
const submitManualReport = async () => {
  if (manualReportFormRef.value) {
    await manualReportFormRef.value.validate(async (valid: boolean) => {
      if (valid) {
        try {
          await createManualReporting(newManualReport)
          showManualReportDialog.value = false
          // 刷新数据
          fetchManualReportings()
          // 显示成功消息
          ElMessage.success('人工报工创建成功')
        } catch (err) {
          console.error('创建人工报工失败:', err)
          ElMessage.error('创建人工报工失败，请重试')
        }
      }
    })
  }
}

// 验证人工报工
const verifyManualReport = async (id: string) => {
  try {
    await updateManualReportingStatus(id, 'verified')
    await fetchManualReportings()
    ElMessage.success('人工报工验证成功')
  } catch (err) {
    console.error('验证人工报工失败:', err)
    ElMessage.error('验证人工报工失败，请重试')
  }
}

// 批准人工报工
const approveManualReport = async (id: string) => {
  try {
    await updateManualReportingStatus(id, 'approved')
    await fetchManualReportings()
    ElMessage.success('人工报工批准成功')
  } catch (err) {
    console.error('批准人工报工失败:', err)
    ElMessage.error('批准人工报工失败，请重试')
  }
}

// 查看人工报工详情
const viewManualReportDetail = (report: ManualReporting) => {
  selectedManualReport.value = report
  showManualReportDetailDialog.value = true
}

// 获取实时设备数据
const fetchRealTimeData = async () => {
  if (selectedEquipmentId.value) {
    try {
      await fetchRealTimeEquipmentData(selectedEquipmentId.value)
    } catch (err) {
      console.error('获取实时设备数据失败:', err)
    }
  }
}

// 提交质量检验
const submitQualityInspection = async () => {
  if (qualityInspectionFormRef.value) {
    await qualityInspectionFormRef.value.validate(async (valid: boolean) => {
      if (valid) {
        try {
          await createQualityInspection(newQualityInspection)
          showQualityInspectionDialog.value = false
          // 刷新数据
          fetchQualityInspections()
          // 显示成功消息
          ElMessage.success('质量检验记录创建成功')
        } catch (err) {
          console.error('创建质量检验记录失败:', err)
          ElMessage.error('创建质量检验记录失败，请重试')
        }
      }
    })
  }
}

// 完成检验
const completeInspection = async (id: string) => {
  try {
    await updateQualityInspectionStatus(id, 'completed')
    // 刷新数据
    fetchQualityInspections()
    // 显示成功消息
    ElMessage.success('检验完成成功')
  } catch (err) {
    console.error('完成检验失败:', err)
    ElMessage.error('完成检验失败，请重试')
  }
}

// 查看质量检验详情
const viewQualityInspectionDetail = (inspection: QualityInspection) => {
  selectedQualityInspection.value = inspection
  showQualityInspectionDetailDialog.value = true
}

// 图表实例
let qualityResultChart: echarts.ECharts | null = null
let equipmentStatusChart: echarts.ECharts | null = null

// 初始化质量检验结果分布图表
const initQualityResultChart = () => {
  const chartDom = document.getElementById('qualityResultChart')
  if (!chartDom) return
  
  qualityResultChart = echarts.init(chartDom)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 10,
      data: ['合格', '不合格']
    },
    series: [
      {
        name: '质量检验结果',
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          {
            value: qualityInspections.value.filter(item => item.result === 'pass').length,
            name: '合格',
            itemStyle: { color: '#67C23A' }
          },
          {
            value: qualityInspections.value.filter(item => item.result === 'fail').length,
            name: '不合格',
            itemStyle: { color: '#F56C6C' }
          }
        ]
      }
    ]
  }
  
  qualityResultChart.setOption(option)
}

// 初始化设备状态统计图表
const initEquipmentStatusChart = () => {
  const chartDom = document.getElementById('equipmentStatusChart')
  if (!chartDom) return
  
  equipmentStatusChart = echarts.init(chartDom)
  
  // 统计设备状态
  const statusStats = {
    normal: equipmentData.value.filter(item => item.status === 'normal').length,
    warning: equipmentData.value.filter(item => item.status === 'warning').length,
    alarm: equipmentData.value.filter(item => item.status === 'alarm').length
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['设备数量']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['正常', '警告', '报警']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '设备数量',
        type: 'bar',
        data: [
          statusStats.normal,
          statusStats.warning,
          statusStats.alarm
        ],
        itemStyle: {
          color: function(params: any) {
            const colors = ['#67C23A', '#E6A23C', '#F56C6C']
            return colors[params.dataIndex]
          }
        }
      }
    ]
  }
  
  equipmentStatusChart.setOption(option)
}

// 更新所有图表
const updateCharts = () => {
  if (qualityResultChart) {
    const option = qualityResultChart.getOption()
    if (option && option.series && Array.isArray(option.series)) {
      option.series[0].data = [
        {
          value: qualityInspections.value.filter(item => item.result === 'pass').length,
          name: '合格',
          itemStyle: { color: '#67C23A' }
        },
        {
          value: qualityInspections.value.filter(item => item.result === 'fail').length,
          name: '不合格',
          itemStyle: { color: '#F56C6C' }
        }
      ]
      qualityResultChart.setOption(option)
    }
  }
  
  if (equipmentStatusChart) {
    const statusStats = {
      normal: equipmentData.value.filter(item => item.status === 'normal').length,
      warning: equipmentData.value.filter(item => item.status === 'warning').length,
      alarm: equipmentData.value.filter(item => item.status === 'alarm').length
    }
    
    const option = equipmentStatusChart.getOption()
    if (option && option.series && Array.isArray(option.series)) {
      option.series[0].data = [
        statusStats.normal,
        statusStats.warning,
        statusStats.alarm
      ]
      equipmentStatusChart.setOption(option)
    }
  }
}

// 监听标签页切换
watch(activeTab, (newTab) => {
  if (newTab === 'analysis') {
    // 延迟初始化图表，确保DOM已渲染
    setTimeout(() => {
      initQualityResultChart()
      initEquipmentStatusChart()
    }, 100)
  }
})

// 监听数据变化，更新图表
watch([qualityInspections, equipmentData], () => {
  updateCharts()
}, { deep: true })

// 窗口大小变化时重新调整图表
window.addEventListener('resize', () => {
  qualityResultChart?.resize()
  equipmentStatusChart?.resize()
})
</script>

<style scoped>
.mes-data-collection-container {
  padding: var(--page-padding);
  height: 100%;
  box-sizing: border-box;
  background-color: var(--bg-color-primary);
}

/* 页面头部样式 */
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: var(--spacing-large);
}

.page-header h2 {
  font-size: var(--font-size-h2);
  font-weight: 600;
  color: var(--text-color-primary);
  margin: 0;
}

/* 模块导航样式 */
.module-nav {
  background-color: var(--bg-color-secondary);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  min-height: calc(100% - 120px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-large);
}

.card-content {
  padding: 0;
}

.content-card {
  margin: var(--spacing-large);
}

/* 图表容器样式 */
.chart-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: var(--spacing-large);
  margin-top: var(--spacing-large);
}

.chart-item {
  background-color: var(--bg-color-secondary);
  padding: var(--spacing-medium);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
}

.chart-item h3 {
  margin-top: 0;
  margin-bottom: var(--spacing-medium);
  font-size: var(--font-size-h3);
  font-weight: 600;
  color: var(--text-color-primary);
}

.chart {
  width: 100%;
  height: 300px;
}

/* 详情对话框样式 */
.detail-dialog {
  padding: 10px 0;
}

.detail-descriptions {
  margin-bottom: 20px;
}

.detail-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.detail-label {
  font-weight: 600;
  margin-right: 10px;
  color: var(--text-color-primary);
  min-width: 80px;
}

.detail-value {
  color: var(--text-color-secondary);
  word-break: break-all;
}

.detail-value.good-qty {
  color: #67C23A;
  font-weight: bold;
}

.detail-value.scrap-qty {
  color: #F56C6C;
  font-weight: bold;
}

.detail-value.rework-qty {
  color: #E6A23C;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .mes-data-collection-container {
    padding: var(--page-padding-mobile);
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-small);
  }
  
  .module-nav {
    min-height: auto;
  }
  
  .content-card {
    margin: var(--spacing-medium);
  }
  
  .chart-container {
    grid-template-columns: 1fr;
  }
  
  .chart {
    height: 250px;
  }
  
  /* 响应式详情对话框 */
  .detail-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .detail-label {
    min-width: auto;
    margin-right: 0;
    margin-bottom: 5px;
  }
}

/* 调整表格样式 */
.detail-dialog :deep(.el-table) {
  font-size: 14px;
}

.detail-dialog :deep(.el-table__header-wrapper th) {
  background-color: var(--bg-color-secondary);
  font-weight: bold;
}

.detail-dialog :deep(.el-table__body-wrapper) {
  max-height: 300px;
  overflow-y: auto;
}
</style>
