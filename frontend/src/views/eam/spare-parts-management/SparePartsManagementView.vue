<template>
  <div class="eam-submodule-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>备件管理</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/eam">EAM系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/eam/spare-parts-management">备件管理</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/eam/spare-parts-management#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 标签页导航区域 -->
    <el-card class="submodule-tabs-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" type="border-card">
        <el-tab-pane label="备件信息管理" name="spare-info">
          <div class="tab-content">
            <div class="sub-card-header">
              <h3>备件信息管理</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="handleAddSparePart">添加备件</el-button>
                <el-button size="small" @click="handleBatchDeleteSparePart">批量删除</el-button>
                <el-button size="small" @click="handleExportSparePart">导出备件</el-button>
                <el-button size="small" @click="handleImportSparePart">导入备件</el-button>
              </div>
            </div>
            <p>精细化记录备件参数、BOM关联建议及供应商信息</p>
            <!-- 表格数据 -->
            <el-table :data="sparePartsList" style="width: 100%" @selection-change="handleSparePartSelectionChange">
              <el-table-column type="selection" width="55" />
              <el-table-column prop="id" label="备件ID" width="100" />
              <el-table-column prop="name" label="备件名称" width="180" />
              <el-table-column prop="model" label="型号规格" width="150" />
              <el-table-column prop="category" label="分类" width="120" />
              <el-table-column prop="supplier" label="供应商" width="150" />
              <el-table-column prop="unit" label="单位" width="80" />
              <el-table-column prop="safetyStock" label="安全库存" width="120" />
              <el-table-column prop="currentStock" label="当前库存" width="120" />
              <el-table-column label="操作" width="180">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="handleEditSparePart(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteSparePart(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="备件库存管理" name="inventory">
          <div class="tab-content">
            <div class="sub-card-header">
              <h3>备件库存管理</h3>
              <el-button type="primary" size="small" @click="handleInventoryAdjust">库存调整</el-button>
            </div>
            <p>实时库存水位预警，支持备件的安全库存自动核算</p>
            <!-- 表格数据 -->
            <el-table :data="inventoryList" style="width: 100%">
              <el-table-column prop="id" label="库存ID" width="100" />
              <el-table-column prop="spareName" label="备件名称" width="180" />
              <el-table-column prop="location" label="库位" width="120" />
              <el-table-column prop="quantity" label="当前库存" width="120" />
              <el-table-column prop="safetyStock" label="安全库存" width="120" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">
                    {{ getStockStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="备件需求计划" name="demand-plan">
          <div class="tab-content">
            <div class="sub-card-header">
              <h3>备件需求计划</h3>
              <el-button type="primary" size="small" @click="handleGeneratePlan">生成计划</el-button>
            </div>
            <p>系统自动识别未来30天维护计划所需的备件缺口</p>
            <!-- 表格数据 -->
            <el-table :data="demandPlans" style="width: 100%">
              <el-table-column prop="id" label="计划ID" width="100" />
              <el-table-column prop="spareName" label="备件名称" width="180" />
              <el-table-column prop="requiredQty" label="需求数量" width="120" />
              <el-table-column prop="currentStock" label="当前库存" width="120" />
              <el-table-column prop="gap" label="缺口数量" width="120" />
              <el-table-column prop="suggestedDate" label="建议采购日期" width="180" />
              <el-table-column prop="maintenancePlanName" label="关联维保计划" width="180" />
              <el-table-column label="操作" width="200">
                <template #default="scope">
                  <el-button size="small" @click="handleViewPlan(scope.row)">查看</el-button>
                  <el-button size="small" type="primary" @click="handleCreatePurchasePlan(scope.row)">生成采购计划</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="备件领用管理" name="issue-management">
          <div class="tab-content">
            <div class="sub-card-header">
              <h3>备件领用管理</h3>
              <el-button type="primary" size="small" @click="handleApplySparePart()">申请领用</el-button>
            </div>
            <p>记录领用审批流、归还状态及以旧换新处理流程</p>
            <!-- 表格数据 -->
            <el-table :data="applicationList" style="width: 100%">
              <el-table-column prop="id" label="领用ID" width="100" />
              <el-table-column prop="spareName" label="备件名称" width="180" />
              <el-table-column prop="quantity" label="领用数量" width="120" />
              <el-table-column prop="applicant" label="申请人" width="120" />
              <el-table-column prop="applicationDate" label="申请日期" width="180" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">
                    {{ statusTextMap[scope.row.status] ?? scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="returnStatus" label="归还状态" width="120">
                <template #default="scope">
                  <el-tag v-if="scope.row.returnStatus" :type="scope.row.returnStatus === 'returned' ? 'success' : 'warning'">
                    {{ returnStatusTextMap[scope.row.returnStatus] ?? scope.row.returnStatus }}
                  </el-tag>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="260">
                <template #default="scope">
                  <el-button size="small" @click="handleViewIssue(scope.row)">查看</el-button>
                  <el-button v-if="scope.row.status === 'pending'" size="small" type="success" @click="handleApproveApplication(scope.row)">批准</el-button>
                  <el-button v-if="scope.row.status === 'pending'" size="small" type="danger" @click="handleRejectApplication(scope.row)">拒绝</el-button>
                  <el-button v-if="scope.row.status === 'approved' && scope.row.returnStatus !== 'returned' && scope.row.returnStatus !== 'consumed'" size="small" type="warning" @click="handleReturnSparePart(scope.row)">归还</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    <!-- 备件信息管理对话框 -->
    <el-dialog
      v-model="sparePartDialogVisible"
      :title="isEditSparePart ? '编辑备件' : '添加备件'"
      width="500px"
      destroy-on-close
    >
      <el-form
        :model="sparePartForm"
        label-position="right"
        label-width="80px"
      >
        <el-form-item label="备件名称" required>
          <el-input v-model="sparePartForm.name" placeholder="请输入备件名称" />
        </el-form-item>
        <el-form-item label="型号规格" required>
          <el-input v-model="sparePartForm.model" placeholder="请输入型号规格" />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="sparePartForm.category" placeholder="请选择分类">
            <el-option label="机械部件" value="机械部件" />
            <el-option label="电气部件" value="电气部件" />
            <el-option label="液压部件" value="液压部件" />
            <el-option label="密封部件" value="密封部件" />
            <el-option label="传感器" value="传感器" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商" required>
          <el-input v-model="sparePartForm.supplier" placeholder="请输入供应商" />
        </el-form-item>
        <el-form-item label="单位" required>
          <el-input v-model="sparePartForm.unit" placeholder="请输入单位" />
        </el-form-item>
        <el-form-item label="安全库存" required>
          <el-input-number v-model="sparePartForm.safetyStock" :min="0" placeholder="请输入安全库存" />
        </el-form-item>
        <el-form-item label="当前库存" v-if="!isEditSparePart">
          <el-input-number v-model="sparePartForm.currentStock" :min="0" placeholder="请输入当前库存" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseSparePartDialog">取消</el-button>
          <el-button type="primary" @click="handleSaveSparePart">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 备件库存管理对话框 -->
    <el-dialog
      v-model="inventoryDialogVisible"
      :title="isInventoryAdjust ? '库存调整' : '库存盘点'"
      width="500px"
      destroy-on-close
    >
      <el-form
        :model="inventoryForm"
        label-position="right"
        label-width="80px"
      >
        <el-form-item label="备件名称" required v-if="!isEditInventory">
          <el-select v-model="inventoryForm.spareId" placeholder="请选择备件">
            <el-option
              v-for="spare in sparePartsList"
              :key="spare.id"
              :label="spare.name"
              :value="spare.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="调整类型" required v-if="isInventoryAdjust">
          <el-radio-group v-model="inventoryForm.adjustType">
            <el-radio label="增加">增加</el-radio>
            <el-radio label="减少">减少</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调整数量" required v-if="isInventoryAdjust">
          <el-input-number v-model="inventoryForm.adjustQty" :min="1" placeholder="请输入调整数量" />
        </el-form-item>
        <el-form-item label="实际库存" required v-if="!isInventoryAdjust && !isEditInventory">
          <el-input-number v-model="inventoryForm.actualStock" :min="0" placeholder="请输入实际库存" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="inventoryForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseInventoryDialog">取消</el-button>
          <el-button type="primary" @click="handleSaveInventory">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 备件需求计划查看对话框 -->
    <el-dialog
      v-model="planDetailDialogVisible"
      title="需求计划详情"
      width="500px"
      destroy-on-close
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="计划ID">{{ selectedPlan.id }}</el-descriptions-item>
        <el-descriptions-item label="备件名称">{{ selectedPlan.spareName }}</el-descriptions-item>
        <el-descriptions-item label="需求数量">{{ selectedPlan.requiredQty }}</el-descriptions-item>
        <el-descriptions-item label="当前库存">{{ selectedPlan.currentStock }}</el-descriptions-item>
        <el-descriptions-item label="缺口数量">{{ selectedPlan.gap }}</el-descriptions-item>
        <el-descriptions-item label="建议采购日期">{{ selectedPlan.suggestedDate }}</el-descriptions-item>
        <el-descriptions-item label="关联维保计划">{{ selectedPlan.maintenancePlan }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClosePlanDetailDialog">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 备件领用管理对话框 -->
    <el-dialog
      v-model="applyDialogVisible"
      :title="isReturnSpare ? '归还备件' : '申请领用'"
      width="500px"
      destroy-on-close
    >
      <el-form
        :model="applyForm"
        label-position="right"
        label-width="80px"
      >
        <el-form-item label="备件名称" required>
          <el-select v-model="applyForm.spareId" placeholder="请选择备件">
            <el-option
              v-for="spare in sparePartsList"
              :key="spare.id"
              :label="spare.name"
              :value="spare.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" required>
          <el-input-number v-model="applyForm.quantity" :min="1" placeholder="请输入数量" />
        </el-form-item>
        <el-form-item label="申请人" required v-if="!isReturnSpare">
          <el-input v-model="applyForm.applicant" placeholder="请输入申请人" />
        </el-form-item>
        <el-form-item label="申请日期" required v-if="!isReturnSpare">
          <el-date-picker
            v-model="applyForm.applicationDate"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择申请日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="applyForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseApplyDialog">取消</el-button>
          <el-button type="primary" @click="handleSaveApply">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看领用申请对话框 -->
    <el-dialog
      v-model="issueDetailDialogVisible"
      title="领用申请详情"
      width="500px"
      destroy-on-close
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="领用ID">{{ selectedIssue.id }}</el-descriptions-item>
        <el-descriptions-item label="备件名称">{{ selectedIssue.spareName }}</el-descriptions-item>
        <el-descriptions-item label="领用数量">{{ selectedIssue.quantity }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ selectedIssue.applicant }}</el-descriptions-item>
        <el-descriptions-item label="申请日期">{{ selectedIssue.applicationDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(selectedIssue.status)">
            {{ statusTextMap[selectedIssue.status] ?? selectedIssue.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="归还状态">
          <el-tag v-if="selectedIssue.returnStatus" :type="selectedIssue.returnStatus === 'returned' ? 'success' : 'warning'">
            {{ returnStatusTextMap[selectedIssue.returnStatus] ?? selectedIssue.returnStatus }}
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseIssueDetailDialog">关闭</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSpareParts, createSparePart, updateSparePart, deleteSparePart, getInventory, updateInventory, getDemandPlans, getSpareIssues, createSpareIssue, approveSpareIssue, rejectSpareIssue, returnSpareIssue } from '@/api/eam'
import { unwrapListResponse } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

// 路由实例
const router = useRouter()
const route = useRoute()

// 激活的标签页
const activeTab = ref('spare-info')

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'spare-info': '备件信息管理',
  'inventory': '备件库存管理',
  'demand-plan': '备件需求计划',
  'issue-management': '备件领用管理'
}

// 备件对话框相关
const sparePartDialogVisible = ref(false)
const isEditSparePart = ref(false)
const sparePartForm = ref({
  id: null,
  name: '',
  code: '',
  model: '',
  category: '',
  supplier: '',
  unit: '',
  safetyStock: 0,
  currentStock: 0
})

// 库存对话框相关
const inventoryDialogVisible = ref(false)
const isInventoryAdjust = ref(false)
const isEditInventory = ref(false)
const inventoryForm = ref({
  id: null,
  spareId: null,
  adjustType: '增加',
  adjustQty: 1,
  actualStock: 0,
  remark: ''
})

// 需求计划详情对话框相关
const planDetailDialogVisible = ref(false)
const selectedPlan = ref<any>({})

// 领用申请对话框相关
const applyDialogVisible = ref(false)
const isReturnSpare = ref(false)
const applyForm = ref({
  spareId: null as number | null,
  quantity: 1,
  applicant: '',
  applicationDate: '',
  remark: ''
})

// 领用申请详情对话框相关
const issueDetailDialogVisible = ref(false)
const selectedIssue = ref<any>({})

// 选中备件列表
const selectedSpareParts = ref<any[]>([])

// 领用状态中文映射（后端枚举→中文显示）
const statusTextMap: Record<string, string> = {
  pending: '待审批',
  approved: '已批准',
  rejected: '已拒绝'
}

// 归还状态中文映射（后端枚举→中文显示）
const returnStatusTextMap: Record<string, string> = {
  not_returned: '未归还',
  returned: '已归还',
  consumed: '已消耗'
}

// 备件信息管理
const sparePartsList = ref<any[]>([])

// 库存监控
const inventoryList = ref<any[]>([])

// 领用申请
const applicationList = ref<any[]>([])

// 需求计划
const demandPlans = ref<any[]>([])

// 获取备件列表
const fetchSpareParts = async () => {
  try {
    const res = await getSpareParts()
    sparePartsList.value = unwrapListResponse<any>(res)
  } catch (error) {
    console.error('获取备件列表失败:', error)
    ElMessage.error('获取备件列表失败')
  }
}

// 获取库存列表
const fetchInventory = async () => {
  try {
    const res = await getInventory()
    inventoryList.value = unwrapListResponse<any>(res)
  } catch (error) {
    console.error('获取库存列表失败:', error)
    ElMessage.error('获取库存列表失败')
  }
}

// 获取领用申请列表
const fetchApplications = async () => {
  try {
    const res = await getSpareIssues()
    applicationList.value = unwrapListResponse<any>(res)
  } catch (error) {
    console.error('获取领用申请列表失败:', error)
    ElMessage.error('获取领用申请列表失败')
  }
}

// 获取需求计划
const fetchDemandPlans = async () => {
  try {
    const res = await getDemandPlans()
    demandPlans.value = unwrapListResponse<any>(res)
  } catch (error) {
    console.error('获取需求计划失败:', error)
    ElMessage.error('获取需求计划失败')
  }
}

// 从路由获取标签页状态
const getActiveTabFromRoute = () => {
  const tabMap: Record<string, string> = {
    'spare-info': 'spare-info',
    'inventory': 'inventory',
    'demand-plan': 'demand-plan',
    'issue-management': 'issue-management'
  }
  const tabName = route.params.tab || 'spare-info'
  return tabMap[tabName as string] || 'spare-info'
}

// 组件挂载时，从路由获取标签页状态
onMounted(() => {
  activeTab.value = getActiveTabFromRoute()
  fetchSpareParts()
  fetchInventory()
  fetchApplications()
  fetchDemandPlans()
})

// 标签页切换事件
const handleTabChange = (tabName: string) => {
  console.log('切换标签页:', tabName)
  // 更新路由，保持标签页状态
  router.push({
    path: `/home/eam/spare-parts-management/${tabName}`
  })
}

// 方法
const handleAddSparePart = () => {
  /**
   * 处理添加备件
   */
  isEditSparePart.value = false
  sparePartForm.value = {
    id: null,
    name: '',
    code: '',
    model: '',
    category: '',
    supplier: '',
    unit: '',
    safetyStock: 0,
    currentStock: 0
  }
  sparePartDialogVisible.value = true
}

const handleEditSparePart = (sparePart: any) => {
  /**
   * 处理编辑备件
   * @param sparePart 要编辑的备件信息
   */
  isEditSparePart.value = true
  sparePartForm.value = { ...sparePart }
  sparePartDialogVisible.value = true
}

const handleDeleteSparePart = async (sparePart: any) => {
  /**
   * 处理删除备件
   * @param sparePart 要删除的备件信息
   */
  try {
    await deleteSparePart(sparePart.id)
    ElMessage.success('删除成功')
    fetchSpareParts()
  } catch (error) {
    console.error('删除备件失败:', error)
    ElMessage.error('删除备件失败')
  }
}

const handleSparePartSelectionChange = (selection: any[]) => {
  /**
   * 处理备件选择变化
   * @param selection 选中的备件列表
   */
  selectedSpareParts.value = selection
  console.log('选中的备件:', selection)
}

const handleBatchDeleteSparePart = async () => {
  /**
   * 处理批量删除备件：确认后循环调用删除接口
   */
  if (selectedSpareParts.value.length === 0) {
    ElMessage.warning('请选择要删除的备件')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedSpareParts.value.length} 个备件吗？`,
      '批量删除',
      { type: 'warning' }
    )
  } catch {
    return
  }
  let successCount = 0
  for (const item of selectedSpareParts.value) {
    try {
      await deleteSparePart(item.id)
      successCount++
    } catch (error) {
      console.error(`删除备件失败: id=${item.id}`, error)
    }
  }
  ElMessage.success(`成功删除 ${successCount} 个备件`)
  selectedSpareParts.value = []
  fetchSpareParts()
}

const handleExportSparePart = () => {
  /**
   * 处理导出备件：导出当前备件列表为CSV
   */
  if (sparePartsList.value.length === 0) {
    ElMessage.warning('暂无备件数据可导出')
    return
  }
  const headers = ['备件ID', '备件名称', '备件编码', '型号规格', '分类', '供应商', '单位', '安全库存', '当前库存']
  const rows = sparePartsList.value.map((s: any) => [
    s.id, s.name, s.code, s.model, s.category, s.supplier, s.unit, s.safetyStock, s.currentStock
  ])
  const csv = [headers, ...rows]
    .map(line => line.map(v => `"${String(v ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\n')
  const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `spare-parts-${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('备件列表导出成功')
}

const handleImportSparePart = () => {
  /**
   * 处理导入备件
   */
  ElMessage.info('备件导入功能开发中，请通过"添加备件"逐条录入')
}

const handleCloseSparePartDialog = () => {
  /**
   * 处理关闭备件对话框
   */
  sparePartDialogVisible.value = false
}

const handleSaveSparePart = async () => {
  /**
   * 处理保存备件信息
   */
  try {
    if (isEditSparePart.value) {
      await updateSparePart(sparePartForm.value.id!, sparePartForm.value)
      ElMessage.success('更新成功')
    } else {
      await createSparePart(sparePartForm.value)
      ElMessage.success('创建成功')
    }
    sparePartDialogVisible.value = false
    fetchSpareParts()
  } catch (error) {
    console.error('保存备件失败:', error)
    ElMessage.error('保存备件失败')
  }
}

const handleApplySparePart = (sparePart?: any) => {
  /**
   * 处理申请领用
   * @param sparePart 备件信息
   */
  console.log('申请领用:', sparePart)
  // 初始申请日期取本地时间（yyyy-MM-dd HH:mm:ss），避免 toISOString() 产生UTC时间导致解析失败及时差
  const now = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  const localNow = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
  applyForm.value = {
    spareId: sparePart?.id ?? null,
    quantity: 1,
    applicant: '',
    applicationDate: localNow,
    remark: ''
  }
  isReturnSpare.value = false
  applyDialogVisible.value = true
}

const handleCloseApplyDialog = () => {
  /**
   * 处理关闭领用申请对话框
   */
  applyDialogVisible.value = false
}

const handleCloseInventoryDialog = () => {
  inventoryDialogVisible.value = false
}

const handleInventoryAdjust = () => {
  /**
   * 处理打开库存调整对话框
   */
  isInventoryAdjust.value = true
  isEditInventory.value = false
  inventoryForm.value = {
    id: null,
    spareId: null,
    adjustType: '增加',
    adjustQty: 1,
    actualStock: 0,
    remark: ''
  }
  inventoryDialogVisible.value = true
}

const handleSaveInventory = async () => {
  /**
   * 处理保存库存调整：基于现有库存记录计算调整后数量并提交
   */
  if (!inventoryForm.value.spareId) {
    ElMessage.warning('请选择备件')
    return
  }
  const existing = inventoryList.value.find((inv: any) => inv.spareId === inventoryForm.value.spareId)
  const currentQty = existing?.quantity ?? 0
  const delta = inventoryForm.value.adjustType === '增加' ? inventoryForm.value.adjustQty : -inventoryForm.value.adjustQty
  const newQty = currentQty + delta
  if (newQty < 0) {
    ElMessage.error(`调整后库存不能为负（当前${currentQty}，调整${delta}）`)
    return
  }
  try {
    await updateInventory({
      ...(existing ?? {}),
      spareId: inventoryForm.value.spareId,
      location: existing?.location ?? '备件库',
      quantity: newQty,
      status: newQty <= 0 ? 'low' : (existing?.status ?? 'normal')
    })
    ElMessage.success('库存调整成功')
    inventoryDialogVisible.value = false
    fetchInventory()
    fetchSpareParts()
  } catch (error) {
    console.error('库存调整失败:', error)
    ElMessage.error('库存调整失败')
  }
}

const handleClosePlanDetailDialog = () => {
  planDetailDialogVisible.value = false
}

const handleCloseIssueDetailDialog = () => {
  issueDetailDialogVisible.value = false
}

const handleSaveApply = async () => {
  /**
   * 处理保存领用申请
   */
  try {
    if (!applyForm.value.spareId) {
      ElMessage.error('请选择备件')
      return
    }
    if (!applyForm.value.applicant && !isReturnSpare.value) {
      ElMessage.error('请输入申请人')
      return
    }
    await createSpareIssue({
      spareId: applyForm.value.spareId,
      quantity: applyForm.value.quantity,
      applicant: applyForm.value.applicant,
      applicationDate: applyForm.value.applicationDate,
      remark: applyForm.value.remark
    })
    ElMessage.success('申请成功')
    applyDialogVisible.value = false
    fetchApplications()
  } catch (error) {
    console.error('保存领用申请失败:', error)
    ElMessage.error('保存领用申请失败')
  }
}

const getStockStatus = (current: number, safety: number) => {
  /**
   * 获取库存状态
   * @param current 当前库存
   * @param safety 安全库存
   * @returns 库存状态
   */
  if (current < safety) return 'low'
  if (current > safety * 2) return 'excess'
  return 'normal'
}

const getStockStatusType = (status: string) => {
  /**
   * 获取库存状态对应的标签类型
   * @param status 库存状态
   * @returns 标签类型
   */
  switch (status) {
    case 'low': return 'danger'
    case 'excess': return 'warning'
    case 'normal': return 'success'
    default: return 'info'
  }
}

const getStatusType = (status: string) => {
  /**
   * 获取通用状态对应的标签类型
   */
  if (['normal', 'approved', 'completed', 'returned', 'success'].includes(status)) return 'success'
  if (['pending', 'processing', 'excess', 'warning'].includes(status)) return 'warning'
  if (['low', 'rejected', 'cancelled', 'danger'].includes(status)) return 'danger'
  return 'info'
}

const getStockStatusText = (status: string) => {
  /**
   * 获取库存状态对应的文本
   * @param status 库存状态
   * @returns 状态文本
   */
  switch (status) {
    case 'low': return '库存不足'
    case 'excess': return '库存积压'
    case 'normal': return '库存正常'
    default: return '未知'
  }
}

const handleApproveApplication = async (application: any) => {
  /**
   * 处理批准申请：调用审批接口，后端扣减库存并联动WMS
   * @param application 申请信息
   */
  try {
    await approveSpareIssue(application.id)
    ElMessage.success('已批准，库存已扣减')
    fetchApplications()
    fetchSpareParts()
    fetchInventory()
  } catch (error) {
    console.error('批准申请失败:', error)
    ElMessage.error('批准申请失败')
  }
}

const handleRejectApplication = async (application: any) => {
  /**
   * 处理拒绝申请：调用拒绝接口
   * @param application 申请信息
   */
  try {
    await ElMessageBox.confirm(`确认拒绝"${application.spareName}"的领用申请吗？`, '拒绝申请', { type: 'warning' })
  } catch {
    return
  }
  try {
    await rejectSpareIssue(application.id)
    ElMessage.success('已拒绝该申请')
    fetchApplications()
  } catch (error) {
    console.error('拒绝申请失败:', error)
    ElMessage.error('拒绝申请失败')
  }
}

const handleReturnSparePart = async (application: any) => {
  /**
   * 处理归还备件：调用归还接口，后端回补库存
   * @param application 申请信息
   */
  try {
    await returnSpareIssue(application.id)
    ElMessage.success('归还成功，库存已回补')
    fetchApplications()
    fetchSpareParts()
    fetchInventory()
  } catch (error) {
    console.error('归还备件失败:', error)
    ElMessage.error('归还备件失败')
  }
}

const handleCreatePurchasePlan = (demand: any) => {
  /**
   * 处理生成采购计划
   * @param demand 需求计划信息
   */
  ElMessage.info(`需求计划#${demand.id}（${demand.spareName}）请前往SRM模块创建采购订单`)
}

const handleViewIssue = (issue: any) => {
  /**
   * 处理查看领用申请详情
   * @param issue 领用申请信息
   */
  selectedIssue.value = { ...issue }
  issueDetailDialogVisible.value = true
}

const handleViewPlan = (plan: any) => {
  /**
   * 处理查看需求计划详情
   * @param plan 需求计划信息
   */
  selectedPlan.value = { ...plan }
  planDetailDialogVisible.value = true
}

const handleGeneratePlan = () => {
  /**
   * 处理生成备件需求计划（由后端定时任务/维护计划触发，前端不提供手工生成）
   */
  ElMessage.info('需求计划由系统根据未来30天维护计划自动生成')
}
</script>

<style scoped>
.eam-submodule-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
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

.header-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.submodule-title {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
  color: #303133;
}

.submodule-description {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.submodule-tabs-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tab-content {
  padding: 20px 0;
}

.tab-content h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #303133;
}

.tab-content p {
  font-size: 14px;
  color: #606266;
  margin-bottom: 20px;
}
</style>
