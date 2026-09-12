<template>
  <div class="basic-data-view">
    <div class="page-header">
      <h2>基础数据配置</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/aps' }">APS系统</el-breadcrumb-item>
        <el-breadcrumb-item>基础数据配置</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 统计卡片 -->
    <div class="statistics-cards" style="margin-bottom: 20px;">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="产能数据总数" :value="statistics.capacityCount">
              <template #prefix>
                <el-icon style="vertical-align: -0.125em;"><Setting /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="工艺约束数" :value="statistics.processCount">
              <template #prefix>
                <el-icon style="vertical-align: -0.125em;"><Operation /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="资源约束数" :value="statistics.resourceConstraintCount">
              <template #prefix>
                <el-icon style="vertical-align: -0.125em;"><Lock /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="资源负载记录" :value="statistics.resourceLoadCount">
              <template #prefix>
                <el-icon style="vertical-align: -0.125em;"><DataAnalysis /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 基础数据配置模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
        <el-tab-pane label="产能数据" name="capacity">
          <div class="tab-content">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>产能数据管理</span>
                  <el-button type="primary" size="small" @click="handleAddCapacity">添加产能数据</el-button>
                </div>
              </template>
              <el-table :data="capacityData" stripe style="width: 100%" v-loading="loading.capacity" empty-text="暂无数据">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="资源名称" />
                <el-table-column prop="capacity" label="产能值" />
                <el-table-column prop="type" label="资源类型" />
                <el-table-column prop="status" label="状态" />
                <el-table-column label="操作" width="150">
                  <template #default="scope">
                    <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
                    <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </el-tab-pane>
        <el-tab-pane label="工艺约束" name="process">
          <div class="tab-content">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>工艺约束管理</span>
                  <el-button type="primary" size="small" @click="handleAddProcess">添加工艺约束</el-button>
                </div>
              </template>
              <el-table :data="processData" stripe style="width: 100%" v-loading="loading.process" empty-text="暂无数据">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="processName" label="工艺名称" />
                <el-table-column prop="workshop" label="车间" />
                <el-table-column prop="sequence" label="工序顺序" />
                <el-table-column prop="processingTime" label="加工时间" />
                <el-table-column prop="setupTime" label="准备时间" />
                <el-table-column prop="teardownTime" label="拆卸时间" />
                <el-table-column label="操作" width="150">
                  <template #default="scope">
                    <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
                    <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </el-tab-pane>
        <el-tab-pane label="资源约束" name="resource">
          <div class="tab-content">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>资源约束管理</span>
                  <el-button type="primary" size="small" @click="handleAddResourceConstraint">添加资源约束</el-button>
                </div>
              </template>
              <!-- 搜索栏 -->
              <div class="search-bar" style="margin-bottom: 16px;">
                <el-input
                  v-model="searchForm.resource.keyword"
                  placeholder="请输入资源名称搜索"
                  style="width: 300px; margin-right: 10px;"
                  clearable
                  @keyup.enter="handleSearch('resource')"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <el-select
                  v-model="searchForm.resource.constraintType"
                  placeholder="选择约束类型"
                  style="width: 150px; margin-right: 10px;"
                  clearable
                >
                  <el-option label="能力约束" :value="0" />
                  <el-option label="数量约束" :value="1" />
                  <el-option label="时间约束" :value="2" />
                </el-select>
                <el-button type="primary" @click="handleSearch('resource')">搜索</el-button>
                <el-button @click="handleResetSearch('resource')">重置</el-button>
              </div>
              <el-table :data="filteredResourceConstraints" stripe style="width: 100%" v-loading="loading.resource" empty-text="暂无数据">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="resourceId" label="资源ID" width="100" />
                <el-table-column prop="resourceName" label="资源名称" />
                <el-table-column prop="constraintType" label="约束类型" width="120" />
                <el-table-column prop="constraintValue" label="约束值" width="100" />
                <el-table-column prop="constraintUnit" label="约束单位" width="100" />
                <el-table-column prop="effectiveDate" label="生效日期" width="120" />
                <el-table-column prop="expiryDate" label="失效日期" width="120" />
                <el-table-column prop="status" label="状态" width="80" />
                <el-table-column label="操作" width="150" fixed="right">
                  <template #default="scope">
                    <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
                    <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="资源负载分析" name="resource-load">
          <div class="tab-content">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>资源负载分析</span>
                  <el-button type="primary" size="small" @click="handleRefreshResourceLoad">刷新数据</el-button>
                </div>
              </template>
              <div class="resource-load-content">
                <el-alert title="资源负载分析功能" type="info" :closable="false" style="margin-bottom: 20px;">
                  <template #default>
                    <p>资源负载分析基于排程结果计算，展示各资源在不同时间段的负载情况。</p>
                    <p>可以使用ResourceLoadAPI获取详细的负载数据和统计信息。</p>
                  </template>
                </el-alert>
                <el-table :data="resourceLoadData" stripe style="width: 100%" v-loading="loading['resource-load']" empty-text="暂无数据">
                  <el-table-column prop="resourceId" label="资源ID" width="100" />
                  <el-table-column prop="resourceName" label="资源名称" />
                  <el-table-column prop="time" label="时间" width="180">
                    <template #default="scope">
                      {{ scope.row.time ? new Date(scope.row.time).toLocaleString() : '' }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="loadRate" label="负载率" width="100">
                    <template #default="scope">
                      <el-tag :type="scope.row.loadRate > 90 ? 'danger' : scope.row.loadRate > 70 ? 'warning' : 'success'">
                        {{ scope.row.loadRate }}%
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="workingHours" label="工作时间(小时)" width="140" />
                  <el-table-column prop="availableHours" label="可用时间(小时)" width="140" />
                  <el-table-column prop="overtimeHours" label="加班时间(小时)" width="140" />
                </el-table>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 资源约束创建/编辑对话框 -->
    <el-dialog
      v-model="constraintDialogVisible"
      :title="isEditConstraint ? '编辑资源约束' : '添加资源约束'"
      width="600px"
      :close-on-click-modal="false"
      @close="resetConstraintForm"
    >
      <el-form
        ref="constraintFormRef"
        :model="constraintForm"
        :rules="constraintFormRules"
        label-width="120px"
      >
        <el-form-item label="资源ID" prop="resourceId">
          <el-input-number
            v-model="constraintForm.resourceId"
            :min="1"
            placeholder="请输入资源ID"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="资源名称" prop="resourceName">
          <el-input v-model="constraintForm.resourceName" placeholder="请输入资源名称" />
        </el-form-item>
        <el-form-item label="约束类型" prop="constraintType">
          <el-select v-model="constraintForm.constraintType" placeholder="请选择约束类型" style="width: 100%">
            <el-option label="能力约束" :value="0" />
            <el-option label="数量约束" :value="1" />
            <el-option label="时间约束" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="约束值" prop="constraintValue">
          <el-input-number
            v-model="constraintForm.constraintValue"
            :min="0"
            :precision="2"
            placeholder="请输入约束值"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="约束单位" prop="constraintUnit">
          <el-input v-model="constraintForm.constraintUnit" placeholder="请输入约束单位（如：件、小时等）" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="constraintForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="constraintForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="(date: Date) => constraintForm.startTime ? date < new Date(constraintForm.startTime) : false"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="constraintForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="constraintDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitConstraint">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 产能数据创建/编辑对话框 -->
    <el-dialog
      v-model="capacityDialogVisible"
      :title="isEditCapacity ? '编辑产能数据' : '添加产能数据'"
      width="500px"
      :close-on-click-modal="false"
      @close="resetCapacityForm"
    >
      <el-form
        ref="capacityFormRef"
        :model="capacityForm"
        :rules="capacityFormRules"
        label-width="100px"
      >
        <el-form-item label="资源名称" prop="name">
          <el-input v-model="capacityForm.name" placeholder="请输入资源名称" />
        </el-form-item>
        <el-form-item label="产能值" prop="capacity">
          <el-input-number
            v-model="capacityForm.capacity"
            :min="0"
            :precision="2"
            placeholder="请输入产能值"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="资源类型" prop="type">
          <el-select v-model="capacityForm.type" placeholder="请选择资源类型" style="width: 100%">
            <el-option label="设备" value="设备" />
            <el-option label="人力" value="人力" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="capacityForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="可用" value="available" />
            <el-option label="不可用" value="unavailable" />
            <el-option label="维护中" value="maintenance" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="capacityDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitCapacity">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 工艺约束创建/编辑对话框 -->
    <el-dialog
      v-model="processDialogVisible"
      :title="isEditProcess ? '编辑工艺约束' : '添加工艺约束'"
      width="600px"
      :close-on-click-modal="false"
      @close="resetProcessForm"
    >
      <el-form
        ref="processFormRef"
        :model="processForm"
        :rules="processFormRules"
        label-width="120px"
      >
        <el-form-item label="工艺名称" prop="processName">
          <el-input v-model="processForm.processName" placeholder="请输入工艺名称" />
        </el-form-item>
        <el-form-item label="车间" prop="workshop">
          <el-input v-model="processForm.workshop" placeholder="请输入车间" />
        </el-form-item>
        <el-form-item label="工序顺序" prop="sequence">
          <el-input-number
            v-model="processForm.sequence"
            :min="1"
            placeholder="请输入工序顺序"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="加工时间(分钟)" prop="processingTime">
          <el-input-number
            v-model="processForm.processingTime"
            :min="0"
            :precision="0"
            placeholder="请输入加工时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="准备时间(分钟)">
          <el-input-number
            v-model="processForm.setupTime"
            :min="0"
            :precision="0"
            placeholder="请输入准备时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="拆卸时间(分钟)">
          <el-input-number
            v-model="processForm.teardownTime"
            :min="0"
            :precision="0"
            placeholder="请输入拆卸时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitProcess">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Setting, Operation, Lock, DataAnalysis } from '@element-plus/icons-vue'
import { unwrapListResponse } from '../../../api'
import { DataTransformer } from '../../../utils/data-transformer'
// 导入API服务
import { ResourceConstraintAPI, ResourceLoadAPI, ResourceCapabilityAPI, SchedulingConstraintAPI } from '../../../api/aps'

// 活跃标签
const activeTab = ref<string>('capacity')

// 数据列表
const capacityData = ref<any[]>([])
const processData = ref<any[]>([])
const resourceConstraints = ref<any[]>([])
const resourceLoadData = ref<any[]>([])

// 加载状态
const loading = ref<{ [key: string]: boolean }>({
  capacity: false,
  process: false,
  resource: false,
  'resource-load': false
})

const getResponseMessage = (response: any, fallback: string) => {
  const normalized = DataTransformer.normalizeResponse(response)
  return normalized?.msg || normalized?.message || fallback
}

// 获取产能数据
const fetchCapacityData = async () => {
  loading.value.capacity = true
  try {
    // 调用产能数据API获取产能数据
    const response = await ResourceCapabilityAPI.getResourceCapabilityData()
    const dataList = unwrapListResponse<any>(response)
    capacityData.value = dataList.map((resource: any) => ({
      id: resource.id,
      name: resource.resourceName || '未知资源',
      capacity: resource.capabilityValue || 100,
      type: resource.capabilityType || '设备',
      status: 'available'
    }))
  } catch (error: any) {
    console.error('获取产能数据失败:', error.message || error)
  } finally {
    loading.value.capacity = false
  }
}

// 获取工艺约束数据
const fetchProcessData = async () => {
  loading.value.process = true
  try {
    // 调用工艺约束API获取工艺约束数据
    const response = await SchedulingConstraintAPI.getSchedulingConstraints()
    const dataList = unwrapListResponse<any>(response)
    processData.value = dataList.map((item: any, index: number) => ({
      id: item.id || index + 1,
      processName: item.constraintName || `工艺${index + 1}`,
      workshop: '默认车间',
      sequence: index + 1,
      processingTime: 30,
      setupTime: 10,
      teardownTime: 5
    }))
  } catch (error: any) {
    console.error('获取工艺约束数据失败:', error.message || error)
  } finally {
    loading.value.process = false
  }
}

// 获取资源约束数据
const fetchResourceConstraints = async () => {
  loading.value.resource = true
  try {
    // 调用资源约束API获取数据
    const response = await ResourceConstraintAPI.getResourceConstraints()
    const dataList = unwrapListResponse<any>(response)
    resourceConstraints.value = dataList.map((constraint: any) => ({
      ...constraint,
      constraintType: getConstraintTypeText(constraint.constraintType),
      effectiveDate: constraint.startTime ? new Date(constraint.startTime).toLocaleDateString() : '',
      expiryDate: constraint.endTime ? new Date(constraint.endTime).toLocaleDateString() : '',
      status: '生效'
    }))
  } catch (error: any) {
    console.error('获取资源约束数据失败:', error.message || error)
  } finally {
    loading.value.resource = false
  }
}

// 获取约束类型文本
const getConstraintTypeText = (type: number) => {
  const typeMap: { [key: number]: string } = {
    0: '能力约束',
    1: '数量约束',
    2: '时间约束'
  }
  return typeMap[type] || '未知'
}

// 搜索表单
const searchForm = reactive({
  resource: {
    keyword: '',
    constraintType: undefined as number | undefined
  }
})

// 统计数据
const statistics = computed(() => {
  return {
    capacityCount: capacityData.value.length,
    processCount: processData.value.length,
    resourceConstraintCount: resourceConstraints.value.length,
    resourceLoadCount: resourceLoadData.value.length
  }
})

// 过滤后的资源约束列表
const filteredResourceConstraints = computed(() => {
  let result = resourceConstraints.value
  
  // 关键词搜索
  if (searchForm.resource.keyword) {
    const keyword = searchForm.resource.keyword.toLowerCase()
    result = result.filter((item: any) => 
      item.resourceName?.toLowerCase().includes(keyword) ||
      item.resourceId?.toString().includes(keyword)
    )
  }
  
  // 约束类型筛选
  if (searchForm.resource.constraintType !== undefined) {
    result = result.filter((item: any) => {
      const originalType = typeof item.constraintType === 'string' 
        ? getConstraintTypeNumber(item.constraintType)
        : item.constraintType
      return originalType === searchForm.resource.constraintType
    })
  }
  
  return result
})

// 资源约束表单验证规则
const constraintFormRules: FormRules = {
  resourceId: [
    { required: true, message: '请输入资源ID', trigger: 'blur' },
    { type: 'number', min: 1, message: '资源ID必须大于0', trigger: 'blur' }
  ],
  resourceName: [
    { required: true, message: '请输入资源名称', trigger: 'blur' },
    { min: 1, max: 100, message: '资源名称长度应在1到100个字符之间', trigger: 'blur' }
  ],
  constraintType: [
    { required: true, message: '请选择约束类型', trigger: 'change' }
  ],
  constraintValue: [
    { required: true, message: '请输入约束值', trigger: 'blur' },
    { type: 'number', min: 0, max: 999999, message: '约束值必须在0到999999之间', trigger: 'blur' }
  ],
  constraintUnit: [
    { required: true, message: '请输入约束单位', trigger: 'blur' },
    { min: 1, max: 20, message: '约束单位长度应在1到20个字符之间', trigger: 'blur' }
  ],
  endTime: [
    {
      validator: (rule: any, value: string, callback: any) => {
        if (constraintForm.startTime && value && new Date(value) < new Date(constraintForm.startTime)) {
          callback(new Error('结束时间不能早于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 获取资源负载数据
const fetchResourceLoadData = async () => {
  loading.value['resource-load'] = true
  try {
    const planId = 1 // 可以从上下文中获取
    const response = await ResourceLoadAPI.getResourceLoadData({
      planId,
      timeScale: 'day'
    })
    resourceLoadData.value = unwrapListResponse<any>(response)
  } catch (error: any) {
    console.error('获取资源负载数据失败:', error.message || error)
  } finally {
    loading.value['resource-load'] = false
  }
}

// 根据当前活跃标签获取对应数据
const fetchDataByTab = async (tab: string) => {
  switch (tab) {
    case 'capacity':
      await fetchCapacityData()
      break
    case 'process':
      await fetchProcessData()
      break
    case 'resource':
      await fetchResourceConstraints()
      break
    case 'resource-load':
      await fetchResourceLoadData()
      break
  }
}

// 对话框相关
const constraintDialogVisible = ref(false)
const isEditConstraint = ref(false)
const constraintFormRef = ref<any>()

// 资源约束表单数据
const constraintForm = reactive({
  id: undefined as number | undefined,
  resourceId: undefined as number | undefined,
  resourceName: '',
  constraintType: 0,
  constraintValue: 0,
  constraintUnit: '',
  startTime: '',
  endTime: '',
  description: ''
})

// 处理添加资源约束
const handleAddResourceConstraint = () => {
  isEditConstraint.value = false
  resetConstraintForm()
  constraintDialogVisible.value = true
}

// 处理刷新资源负载
const handleRefreshResourceLoad = () => {
  fetchResourceLoadData()
}

// 搜索处理
const handleSearch = (tab: string) => {
  if (tab === 'resource') {
    // 资源约束使用computed过滤，无需额外操作
    ElMessage.success('搜索完成')
  }
}

// 重置搜索
const handleResetSearch = (tab: string) => {
  if (tab === 'resource') {
    searchForm.resource.keyword = ''
    searchForm.resource.constraintType = undefined
  }
}

// 产能数据对话框相关
const capacityDialogVisible = ref(false)
const isEditCapacity = ref(false)
const capacityFormRef = ref<any>()

// 产能数据表单
const capacityForm = reactive({
  id: undefined as number | undefined,
  name: '',
  capacity: 0,
  type: '设备',
  status: 'available'
})

// 工艺约束对话框相关
const processDialogVisible = ref(false)
const isEditProcess = ref(false)
const processFormRef = ref<any>()

// 工艺约束表单
const processForm = reactive({
  id: undefined as number | undefined,
  processName: '',
  workshop: '',
  sequence: 1,
  processingTime: 0,
  setupTime: 0,
  teardownTime: 0
})

// 处理添加产能数据
const handleAddCapacity = () => {
  isEditCapacity.value = false
  resetCapacityForm()
  capacityDialogVisible.value = true
}

// 处理添加工艺约束
const handleAddProcess = () => {
  isEditProcess.value = false
  resetProcessForm()
  processDialogVisible.value = true
}

// 处理编辑
const handleEdit = (row: any) => {
  if (activeTab.value === 'capacity') {
    isEditCapacity.value = true
    capacityForm.id = row.id
    capacityForm.name = row.name || ''
    capacityForm.capacity = row.capacity || 0
    capacityForm.type = row.type || '设备'
    capacityForm.status = row.status || 'available'
    capacityDialogVisible.value = true
  } else if (activeTab.value === 'process') {
    isEditProcess.value = true
    processForm.id = row.id
    processForm.processName = row.processName || ''
    processForm.workshop = row.workshop || ''
    processForm.sequence = row.sequence || 1
    processForm.processingTime = row.processingTime || 0
    processForm.setupTime = row.setupTime || 0
    processForm.teardownTime = row.teardownTime || 0
    processDialogVisible.value = true
  } else if (activeTab.value === 'resource') {
    isEditConstraint.value = true
    constraintForm.id = row.id
    constraintForm.resourceId = row.resourceId
    constraintForm.resourceName = row.resourceName || ''
    constraintForm.constraintType = getConstraintTypeNumber(row.constraintType)
    constraintForm.constraintValue = row.constraintValue || 0
    constraintForm.constraintUnit = row.constraintUnit || ''
    constraintForm.startTime = row.startTime ? new Date(row.startTime).toISOString().slice(0, 16) : ''
    constraintForm.endTime = row.endTime ? new Date(row.endTime).toISOString().slice(0, 16) : ''
    constraintForm.description = row.description || ''
    constraintDialogVisible.value = true
  }
}

// 重置产能表单
const resetCapacityForm = () => {
  capacityForm.id = undefined
  capacityForm.name = ''
  capacityForm.capacity = 0
  capacityForm.type = '设备'
  capacityForm.status = 'available'
  // 清除表单验证状态
  capacityFormRef.value?.resetFields()
}

// 重置工艺约束表单
const resetProcessForm = () => {
  processForm.id = undefined
  processForm.processName = ''
  processForm.workshop = ''
  processForm.sequence = 1
  processForm.processingTime = 0
  processForm.setupTime = 0
  processForm.teardownTime = 0
  // 清除表单验证状态
  processFormRef.value?.resetFields()
}

// 提交产能数据
const handleSubmitCapacity = async () => {
  if (!capacityFormRef.value) return
  
  await capacityFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        // 准备提交数据
        const submitData = {
          name: capacityForm.name,
          capacity: capacityForm.capacity,
          type: capacityForm.type,
          status: capacityForm.status
        }
        
        let response: any
        if (isEditCapacity.value && capacityForm.id) {
          // 更新产能数据
          // 注意：目前API中没有专门的产能数据更新API，暂时使用资源约束API代替
          // 后续需要根据实际后端API调整
          response = await ResourceConstraintAPI.updateResourceConstraint(capacityForm.id, submitData)
        } else {
          // 添加产能数据
          // 注意：目前API中没有专门的产能数据创建API，暂时使用资源约束API代替
          // 后续需要根据实际后端API调整
          response = await ResourceConstraintAPI.createResourceConstraint(submitData)
        }
        
        const responseData = DataTransformer.normalizeResponse(response)
        if (DataTransformer.isSuccessCode(responseData?.code)) {
          ElMessage.success(isEditCapacity.value ? '更新产能数据成功' : '创建产能数据成功')
          capacityDialogVisible.value = false
          fetchCapacityData()
        } else {
          ElMessage.error(getResponseMessage(responseData, isEditCapacity.value ? '更新产能数据失败' : '创建产能数据失败'))
        }
      } catch (error: any) {
        console.error(isEditCapacity.value ? '更新产能数据失败:' : '创建产能数据失败:', error.message || error)
        ElMessage.error(isEditCapacity.value ? '更新产能数据失败' : '创建产能数据失败')
      }
    }
  })
}

// 提交工艺约束
const handleSubmitProcess = async () => {
  if (!processFormRef.value) return
  
  await processFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        // 准备提交数据
        const submitData = {
          resourceName: processForm.processName,
          workshop: processForm.workshop,
          sequence: processForm.sequence,
          constraintValue: processForm.processingTime,
          setupTime: processForm.setupTime,
          teardownTime: processForm.teardownTime,
          constraintType: 1, // 数量约束
          constraintUnit: '分钟'
        }
        
        let response: any
        if (isEditProcess.value && processForm.id) {
          // 更新工艺约束
          response = await ResourceConstraintAPI.updateResourceConstraint(processForm.id, submitData)
        } else {
          // 添加工艺约束
          response = await ResourceConstraintAPI.createResourceConstraint(submitData)
        }
        
        const responseData = DataTransformer.normalizeResponse(response)
        if (DataTransformer.isSuccessCode(responseData?.code)) {
          ElMessage.success(isEditProcess.value ? '更新工艺约束成功' : '创建工艺约束成功')
          processDialogVisible.value = false
          fetchProcessData()
        } else {
          ElMessage.error(getResponseMessage(responseData, isEditProcess.value ? '更新工艺约束失败' : '创建工艺约束失败'))
        }
      } catch (error: any) {
        console.error(isEditProcess.value ? '更新工艺约束失败:' : '创建工艺约束失败:', error.message || error)
        ElMessage.error(isEditProcess.value ? '更新工艺约束失败' : '创建工艺约束失败')
      }
    }
  })
}

// 产能表单验证规则
const capacityFormRules: FormRules = {
  name: [
    { required: true, message: '请输入资源名称', trigger: 'blur' },
    { min: 1, max: 100, message: '资源名称长度应在1到100个字符之间', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请输入产能值', trigger: 'blur' },
    { type: 'number', min: 0, max: 999999, message: '产能值必须在0到999999之间', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择资源类型', trigger: 'change' }
  ]
}

// 工艺约束表单验证规则
const processFormRules: FormRules = {
  processName: [
    { required: true, message: '请输入工艺名称', trigger: 'blur' },
    { min: 1, max: 100, message: '工艺名称长度应在1到100个字符之间', trigger: 'blur' }
  ],
  workshop: [
    { required: true, message: '请输入车间', trigger: 'blur' },
    { min: 1, max: 50, message: '车间名称长度应在1到50个字符之间', trigger: 'blur' }
  ],
  sequence: [
    { required: true, message: '请输入工序顺序', trigger: 'blur' },
    { type: 'number', min: 1, max: 9999, message: '工序顺序必须在1到9999之间', trigger: 'blur' }
  ],
  processingTime: [
    { required: true, message: '请输入加工时间', trigger: 'blur' },
    { type: 'number', min: 0, max: 999999, message: '加工时间必须在0到999999分钟之间', trigger: 'blur' }
  ],
  setupTime: [
    { type: 'number', min: 0, max: 999999, message: '准备时间必须在0到999999分钟之间', trigger: 'blur' }
  ],
  teardownTime: [
    { type: 'number', min: 0, max: 999999, message: '拆卸时间必须在0到999999分钟之间', trigger: 'blur' }
  ]
}

// 获取约束类型数字
const getConstraintTypeNumber = (typeText: string): number => {
  const typeMap: { [key: string]: number } = {
    '能力约束': 0,
    '数量约束': 1,
    '时间约束': 2
  }
  return typeMap[typeText] ?? 0
}

// 重置资源约束表单
const resetConstraintForm = () => {
  constraintForm.id = undefined
  constraintForm.resourceId = undefined
  constraintForm.resourceName = ''
  constraintForm.constraintType = 0
  constraintForm.constraintValue = 0
  constraintForm.constraintUnit = ''
  constraintForm.startTime = ''
  constraintForm.endTime = ''
  constraintForm.description = ''
  // 清除表单验证状态
  constraintFormRef.value?.resetFields()
}

// 提交资源约束
const handleSubmitConstraint = async () => {
  if (!constraintFormRef.value) return
  
  await constraintFormRef.value.validate((valid: boolean) => {
    if (valid) {
      const submitData = {
        resourceId: constraintForm.resourceId,
        resourceName: constraintForm.resourceName,
        constraintType: constraintForm.constraintType,
        constraintValue: constraintForm.constraintValue,
        constraintUnit: constraintForm.constraintUnit || '件',
        startTime: constraintForm.startTime ? new Date(constraintForm.startTime).toISOString() : null,
        endTime: constraintForm.endTime ? new Date(constraintForm.endTime).toISOString() : null,
        description: constraintForm.description
      }
      
      const apiCall = isEditConstraint.value
        ? ResourceConstraintAPI.updateResourceConstraint(constraintForm.id!, submitData)
        : ResourceConstraintAPI.createResourceConstraint(submitData)
      
      apiCall.then((response: any) => {
        const responseData = DataTransformer.normalizeResponse(response)
        if (DataTransformer.isSuccessCode(responseData?.code)) {
          ElMessage.success({
            message: isEditConstraint.value ? '更新成功' : '创建成功',
            duration: 2000
          })
          constraintDialogVisible.value = false
          // 刷新数据
          fetchResourceConstraints()
        } else {
          ElMessage.warning({
            message: getResponseMessage(responseData, '操作失败'),
            duration: 3000
          })
        }
      }).catch((error: any) => {
        console.error('提交失败:', error)
        ElMessage.error({
          message: error.message || '提交失败',
          duration: 3000
        })
      })
    }
  })
}

// 处理删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该数据吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 根据当前标签页调用相应的删除API
    let response: any
    let successMessage = ''
    
    if (activeTab.value === 'capacity') {
      // 删除产能数据
      response = await ResourceConstraintAPI.deleteResourceConstraint(row.id)
      successMessage = '删除产能数据成功'
    } else if (activeTab.value === 'process') {
      // 删除工艺约束
      response = await ResourceConstraintAPI.deleteResourceConstraint(row.id)
      successMessage = '删除工艺约束成功'
    } else if (activeTab.value === 'resource') {
      // 删除资源约束
      response = await ResourceConstraintAPI.deleteResourceConstraint(row.id)
      successMessage = '删除资源约束成功'
    } else {
      throw new Error('未知的标签页类型')
    }
    
    const responseData = DataTransformer.normalizeResponse(response)
    if (DataTransformer.isSuccessCode(responseData?.code)) {
      ElMessage.success({
        message: successMessage,
        duration: 2000
      })
      // 刷新对应标签页的数据
      await fetchDataByTab(activeTab.value)
    } else {
      ElMessage.warning({
        message: getResponseMessage(responseData, '删除失败'),
        duration: 3000
      })
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除数据失败:', error)
      ElMessage.error(error.message || '删除数据失败')
    }
  }
}

// 组件挂载时获取默认标签数据
onMounted(() => {
  fetchDataByTab(activeTab.value)
})

// 监听标签切换，获取对应数据
const handleTabChange = (tab: string) => {
  fetchDataByTab(tab)
}
</script>

<style scoped>
.basic-data-view {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: #f5f7fa;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.page-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 1.5rem;
}

.module-nav {
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  min-height: calc(100% - 80px);
}

/* 标签内容样式 */
.tab-content {
  padding: 20px;
}

/* 卡片头部样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1.1rem;
}

.resource-load-content {
  padding: 0;
}

.statistics-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-card :deep(.el-statistic__head) {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-card :deep(.el-statistic__number) {
  font-size: 24px;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .basic-data-view {
    padding: 12px;
  }
  
  .page-header h2 {
    font-size: 1.2rem;
  }
  
  .module-nav {
    min-height: auto;
  }
  
  .tab-content {
    padding: 12px;
  }
}
</style>
