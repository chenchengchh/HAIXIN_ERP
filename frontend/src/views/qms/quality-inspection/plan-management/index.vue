<template>
  <div class="plan-management-view">
    <div class="page-header">
      <h3>检验计划制定</h3>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAddPlan">
          新增检验计划
        </el-button>
        <el-button :icon="Download" @click="handleExport">
          导出计划
        </el-button>
      </div>
    </div>

    <!-- 查询条件 -->
    <el-card class="query-card">
      <el-form :model="queryForm" label-position="left" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="计划编号">
              <el-input v-model="queryForm.planNo" placeholder="请输入计划编号" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="物料名称">
              <el-input v-model="queryForm.materialName" placeholder="请输入物料名称" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="计划类型">
              <el-select v-model="queryForm.planType" placeholder="请选择计划类型">
                <el-option label="入厂检验" value="incoming" />
                <el-option label="过程检验" value="process" />
                <el-option label="出厂检验" value="outgoing" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select v-model="queryForm.status" placeholder="请选择状态">
                <el-option label="草稿" value="draft" />
                <el-option label="生效" value="effective" />
                <el-option label="失效" value="invalid" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="query-actions">
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 检验计划列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="planList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="planNo" label="计划编号" min-width="150" />
        <el-table-column prop="planName" label="计划名称" min-width="180" />
        <el-table-column prop="materialCode" label="物料编码" min-width="120" />
        <el-table-column prop="materialName" label="物料名称" min-width="180" />
        <el-table-column prop="planType" label="计划类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getPlanTypeColor(scope.row.planType)">
              {{ getPlanTypeName(scope.row.planType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="standardNo" label="关联标准" min-width="150" />
        <el-table-column prop="samplingPlan" label="抽样方案" min-width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusColor(scope.row.status)">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveDate" label="生效日期" min-width="180" align="center" />
        <el-table-column prop="expiryDate" label="失效日期" min-width="180" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditPlan(scope.row)">
              编辑
            </el-button>
            <el-button size="small" v-if="scope.row.status === 'draft'" type="success" @click="handleActivatePlan(scope.row)">
              生效
            </el-button>
            <el-button size="small" v-if="scope.row.status === 'effective'" type="warning" @click="handleInvalidatePlan(scope.row)">
              失效
            </el-button>
            <el-button size="small" type="danger" @click="handleDeletePlan(scope.row)">
              删除
            </el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
      @close="handleDialogClose"
    >
      <el-form
        ref="planFormRef"
        :model="planForm"
        label-position="top"
        :rules="formRules"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划编号" prop="planNo">
              <el-input v-model="planForm.planNo" placeholder="请输入计划编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划名称" prop="planName">
              <el-input v-model="planForm.planName" placeholder="请输入计划名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料编码" prop="materialCode">
              <el-input v-model="planForm.materialCode" placeholder="请输入物料编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料名称" prop="materialName">
              <el-input v-model="planForm.materialName" placeholder="请输入物料名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划类型" prop="planType">
              <el-select v-model="planForm.planType" placeholder="请选择计划类型">
                <el-option label="入厂检验" value="incoming" />
                <el-option label="过程检验" value="process" />
                <el-option label="出厂检验" value="outgoing" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联标准" prop="standardNo">
              <el-select v-model="planForm.standardNo" placeholder="请选择关联标准">
                <el-option label="QMS-STD-001 - 电子元器件" value="QMS-STD-001" />
                <el-option label="QMS-STD-002 - 塑料件" value="QMS-STD-002" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="抽样方案" prop="samplingPlan">
              <el-select v-model="planForm.samplingPlan" placeholder="请选择抽样方案">
                <el-option label="MIL-STD-105E" value="mil-std-105e" />
                <el-option label="GB/T 2828.1" value="gb-t-2828.1" />
                <el-option label="100%检验" value="100-inspection" />
                <el-option label="自定义" value="custom" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检验频率" prop="inspectionFrequency">
              <el-input v-model="planForm.inspectionFrequency" placeholder="请输入检验频率" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检验部门" prop="inspectionDepartment">
              <el-input v-model="planForm.inspectionDepartment" placeholder="请输入检验部门" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检验人员" prop="inspectionPerson">
              <el-input v-model="planForm.inspectionPerson" placeholder="请输入检验人员" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生效日期" prop="effectiveDate">
              <el-date-picker
                v-model="planForm.effectiveDate"
                type="date"
                placeholder="请选择生效日期"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="失效日期" prop="expiryDate">
              <el-date-picker
                v-model="planForm.expiryDate"
                type="date"
                placeholder="请选择失效日期"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="计划描述" prop="description">
          <el-input
            v-model="planForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入计划描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSavePlan">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download } from '@element-plus/icons-vue'
import { unwrapPageResponse } from '@/api'
import { InspectionPlanAPI } from '@/api/qms'

// 查询表单
const queryForm = reactive({
  planNo: '',
  materialName: '',
  planType: '',
  status: ''
})

// 加载状态
const loading = ref(false)

// 检验计划列表
const planList = ref<any[]>([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 选中项
const selectedPlans = ref<any[]>([])

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('新增检验计划')
const planFormRef = ref()

// 表单数据
const planForm = reactive({
  id: 0,
  planNo: '',
  planName: '',
  materialCode: '',
  materialName: '',
  planType: '',
  standardNo: '',
  samplingPlan: '',
  inspectionFrequency: '',
  inspectionDepartment: '',
  inspectionPerson: '',
  effectiveDate: '',
  expiryDate: '',
  status: 'draft',
  description: ''
})

// 表单验证规则
const formRules = {
  planNo: [{ required: true, message: '请输入计划编号', trigger: 'blur' }],
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  materialCode: [{ required: true, message: '请输入物料编码', trigger: 'blur' }],
  materialName: [{ required: true, message: '请输入物料名称', trigger: 'blur' }],
  planType: [{ required: true, message: '请选择计划类型', trigger: 'change' }],
  standardNo: [{ required: true, message: '请选择关联标准', trigger: 'change' }],
  samplingPlan: [{ required: true, message: '请选择抽样方案', trigger: 'change' }],
  effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }]
}

// 初始化
onMounted(handleQuery)

// 查询数据
async function handleQuery() {
  loading.value = true
  try {
    const res = await InspectionPlanAPI.getInspectionPlans({
      page: pagination.currentPage,
      size: pagination.pageSize,
      planNo: queryForm.planNo || undefined,
      materialName: queryForm.materialName || undefined,
      planType: queryForm.planType || undefined,
      status: queryForm.status || undefined
    })
    const page = unwrapPageResponse<any>(res)
    planList.value = page.list
    pagination.total = page.total
  } catch (e: any) {
    ElMessage.error(e?.message || '获取检验计划列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  Object.assign(queryForm, {
    planNo: '',
    materialName: '',
    planType: '',
    status: ''
  })
  handleQuery()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  handleQuery()
}

// 当前页变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  handleQuery()
}

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedPlans.value = selection
}

// 计划类型名称映射
const getPlanTypeName = (type: string) => {
  const typeMap: Record<string, string> = {
    'incoming': '入厂检验',
    'process': '过程检验',
    'outgoing': '出厂检验'
  }
  return typeMap[type] || type
}

// 计划类型颜色映射
const getPlanTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'incoming': 'primary',
    'process': 'success',
    'outgoing': 'warning'
  }
  return colorMap[type] || 'default'
}

// 状态名称映射
const getStatusName = (status: string) => {
  const statusMap: Record<string, string> = {
    'draft': '草稿',
    'effective': '生效',
    'invalid': '失效'
  }
  return statusMap[status] || status
}

// 状态颜色映射
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'draft': 'warning',
    'effective': 'success',
    'invalid': 'danger'
  }
  return colorMap[status] || 'default'
}

// 新增检验计划
const handleAddPlan = () => {
  dialogTitle.value = '新增检验计划'
  Object.assign(planForm, {
    id: 0,
    planNo: '',
    planName: '',
    materialCode: '',
    materialName: '',
    planType: '',
    standardNo: '',
    samplingPlan: '',
    inspectionFrequency: '',
    inspectionDepartment: '',
    inspectionPerson: '',
    effectiveDate: '',
    expiryDate: '',
    status: 'draft',
    description: ''
  })
  dialogVisible.value = true
}

// 编辑检验计划
const handleEditPlan = (row: any) => {
  dialogTitle.value = '编辑检验计划'
  Object.assign(planForm, JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}

// 激活检验计划
const handleActivatePlan = async (row: any) => {
  try {
    await InspectionPlanAPI.activateInspectionPlan(row.id)
    ElMessage.success(`检验计划 ${row.planNo} 已生效`)
    await handleQuery()
  } catch (e: any) {
    ElMessage.error(e?.message || '生效失败')
  }
}

// 失效检验计划
const handleInvalidatePlan = async (row: any) => {
  try {
    await InspectionPlanAPI.invalidateInspectionPlan(row.id)
    ElMessage.success(`检验计划 ${row.planNo} 已失效`)
    await handleQuery()
  } catch (e: any) {
    ElMessage.error(e?.message || '失效失败')
  }
}

// 删除检验计划
const handleDeletePlan = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除检验计划【${row.planNo}】吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    await InspectionPlanAPI.deleteInspectionPlan(row.id)
    ElMessage.success('删除成功')
    await handleQuery()
  } catch (e: any) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e?.message || '删除失败')
  }
}

// 保存检验计划
const handleSavePlan = () => {
  if (!planFormRef.value) return
  
  planFormRef.value.validate((valid: boolean) => {
    if (valid) {
      handleSavePlanInternal()
    }
  })
}

const handleSavePlanInternal = async () => {
  try {
    loading.value = true
    const payload: any = {
      ...planForm,
      creator: 'admin',
      effectiveDate: formatToIsoDate(planForm.effectiveDate),
      expiryDate: formatToIsoDate(planForm.expiryDate)
    }
    if (payload.id) {
      await InspectionPlanAPI.updateInspectionPlan(payload.id, payload)
    } else {
      await InspectionPlanAPI.createInspectionPlan(payload)
    }
    dialogVisible.value = false
    ElMessage.success('保存成功')
    await handleQuery()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    loading.value = false
  }
}

// 关闭对话框
const handleDialogClose = () => {
  if (planFormRef.value) {
    planFormRef.value.resetFields()
  }
}

// 导出计划
const handleExport = () => {
  if (!planList.value.length) {
    ElMessage.warning('暂无可导出的数据')
    return
  }
  const headers = ['计划编号', '计划名称', '物料编码', '物料名称', '计划类型', '状态', '生效日期', '失效日期']
  const rows = planList.value.map((r: any) => [
    r.planNo,
    r.planName,
    r.materialCode,
    r.materialName,
    r.planType,
    r.status,
    r.effectiveDate,
    r.expiryDate
  ])
  const csv = [headers, ...rows].map(line => line.map(v => `"${String(v ?? '').replace(/"/g, '""')}"`).join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `inspection-plans-${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

const formatToIsoDate = (value: any) => {
  if (!value) return value
  if (typeof value === 'string') return value
  if (value instanceof Date) return value.toISOString().slice(0, 10)
  return String(value)
}
</script>

<style scoped>
.plan-management-view {
  padding: 20px;
  height: 100%;
  overflow: auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.query-card {
  margin-bottom: 20px;
}

.query-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .plan-management-view {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .plan-management-view {
    padding: 12px;
  }
  
  .header-actions {
    flex-direction: column;
  }
}
</style>
