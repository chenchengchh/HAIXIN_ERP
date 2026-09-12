<template>
  <div class="result-recording-view">
    <div class="page-header">
      <h3>检验结果记录</h3>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAddResult">
          录入检验结果
        </el-button>
        <el-button type="success" :icon="Check" @click="handleBatchApprove">
          批量审核
        </el-button>
        <el-button :icon="Download" @click="handleExport">
          导出结果
        </el-button>
      </div>
    </div>

    <!-- 查询条件 -->
    <el-card class="query-card">
      <el-form :model="queryForm" label-position="left" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="结果编号">
              <el-input v-model="queryForm.resultNo" placeholder="请输入结果编号" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="任务编号">
              <el-input v-model="queryForm.taskNo" placeholder="请输入任务编号" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="物料名称">
              <el-input v-model="queryForm.materialName" placeholder="请输入物料名称" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="检验人员">
              <el-input v-model="queryForm.inspector" placeholder="请输入检验人员" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="检验结果">
              <el-select v-model="queryForm.inspectionResult" placeholder="请选择检验结果">
                <el-option label="合格" value="qualified" />
                <el-option label="不合格" value="unqualified" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="审核状态">
              <el-select v-model="queryForm.auditStatus" placeholder="请选择审核状态">
                <el-option label="待审核" value="pending" />
                <el-option label="审核通过" value="approved" />
                <el-option label="审核驳回" value="rejected" />
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

    <!-- 检验结果列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="resultList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="resultNo" label="结果编号" min-width="150" />
        <el-table-column prop="taskNo" label="任务编号" min-width="150" />
        <el-table-column prop="materialCode" label="物料编码" min-width="120" />
        <el-table-column prop="materialName" label="物料名称" min-width="180" />
        <el-table-column prop="batchNo" label="批次号" min-width="150" />
        <el-table-column prop="inspectionQuantity" label="检验数量" width="120" align="center" />
        <el-table-column prop="qualifiedQuantity" label="合格数量" width="120" align="center" />
        <el-table-column prop="unqualifiedQuantity" label="不合格数量" width="120" align="center" />
        <el-table-column prop="inspectionResult" label="检验结果" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.inspectionResult === 'qualified' ? 'success' : 'danger'">
              {{ scope.row.inspectionResult === 'qualified' ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="inspector" label="检验人员" width="120" align="center" />
        <el-table-column prop="inspectionTime" label="检验时间" min-width="180" align="center" />
        <el-table-column prop="auditStatus" label="审核状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getAuditStatusColor(scope.row.auditStatus)">
              {{ getAuditStatusName(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditor" label="审核人员" width="120" align="center" />
        <el-table-column prop="auditTime" label="审核时间" min-width="180" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleViewResult(scope.row)">
              查看
            </el-button>
            <el-button size="small" v-if="scope.row.auditStatus === 'pending'" type="success" @click="handleApproveResult(scope.row)">
              审核通过
            </el-button>
            <el-button size="small" v-if="scope.row.auditStatus === 'pending'" type="danger" @click="handleRejectResult(scope.row)">
              审核驳回
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

    <!-- 结果详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="检验结果详情"
      width="800px"
      @close="handleDetailDialogClose"
    >
      <div v-if="selectedResult" class="result-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="结果编号">{{ selectedResult.resultNo }}</el-descriptions-item>
          <el-descriptions-item label="任务编号">{{ selectedResult.taskNo }}</el-descriptions-item>
          <el-descriptions-item label="物料编码">{{ selectedResult.materialCode }}</el-descriptions-item>
          <el-descriptions-item label="物料名称">{{ selectedResult.materialName }}</el-descriptions-item>
          <el-descriptions-item label="批次号">{{ selectedResult.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="检验数量">{{ selectedResult.inspectionQuantity }}</el-descriptions-item>
          <el-descriptions-item label="合格数量">{{ selectedResult.qualifiedQuantity }}</el-descriptions-item>
          <el-descriptions-item label="不合格数量">{{ selectedResult.unqualifiedQuantity }}</el-descriptions-item>
          <el-descriptions-item label="检验结果">{{ selectedResult.inspectionResult === 'qualified' ? '合格' : '不合格' }}</el-descriptions-item>
          <el-descriptions-item label="检验人员">{{ selectedResult.inspector }}</el-descriptions-item>
          <el-descriptions-item label="检验时间">{{ selectedResult.inspectionTime }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">{{ getAuditStatusName(selectedResult.auditStatus) }}</el-descriptions-item>
          <el-descriptions-item label="审核人员">{{ selectedResult.auditor || '未审核' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ selectedResult.auditTime || '未审核' }}</el-descriptions-item>
          <el-descriptions-item label="检验说明">{{ selectedResult.description }}</el-descriptions-item>
        </el-descriptions>
        
        <h4 style="margin-top: 20px;">检验项目详情</h4>
        <el-table
          :data="selectedResult.inspectionItems"
          border
          style="width: 100%; margin-top: 10px"
        >
          <el-table-column prop="itemName" label="项目名称" min-width="150" />
          <el-table-column prop="specification" label="规格要求" min-width="150" />
          <el-table-column prop="actualValue" label="实际值" min-width="120" align="center" />
          <el-table-column prop="result" label="结果" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.result === 'qualified' ? 'success' : 'danger'">
                {{ scope.row.result === 'qualified' ? '合格' : '不合格' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" />
        </el-table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 结果录入对话框 -->
    <el-dialog
      v-model="recordDialogVisible"
      title="录入检验结果"
      width="900px"
      @close="handleRecordDialogClose"
    >
      <el-form
        ref="recordFormRef"
        :model="recordForm"
        label-position="top"
        :rules="recordFormRules"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="任务编号" prop="taskNo">
              <el-select v-model="recordForm.taskNo" placeholder="请选择任务编号">
                <el-option label="QMS-TASK-001" value="QMS-TASK-001" />
                <el-option label="QMS-TASK-002" value="QMS-TASK-002" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次号" prop="batchNo">
              <el-input v-model="recordForm.batchNo" placeholder="请输入批次号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检验数量" prop="inspectionQuantity">
              <el-input v-model.number="recordForm.inspectionQuantity" placeholder="请输入检验数量" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检验人员" prop="inspector">
              <el-input v-model="recordForm.inspector" placeholder="请输入检验人员" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="检验项目" prop="inspectionItems">
          <el-table
            :data="recordForm.inspectionItems"
            border
            style="width: 100%"
          >
            <el-table-column prop="itemName" label="项目名称" min-width="150">
              <template #default="scope">
                <el-select v-model="scope.row.itemName" placeholder="请选择项目名称">
                  <el-option label="外观" value="外观" />
                  <el-option label="尺寸" value="尺寸" />
                  <el-option label="性能" value="性能" />
                  <el-option label="功能" value="功能" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="specification" label="规格要求" min-width="150">
              <template #default="scope">
                <el-input v-model="scope.row.specification" placeholder="请输入规格要求" />
              </template>
            </el-table-column>
            <el-table-column prop="actualValue" label="实际值" min-width="120" align="center">
              <template #default="scope">
                <el-input v-model="scope.row.actualValue" placeholder="请输入实际值" />
              </template>
            </el-table-column>
            <el-table-column prop="result" label="结果" width="120" align="center">
              <template #default="scope">
                <el-select v-model="scope.row.result" placeholder="请选择结果">
                  <el-option label="合格" value="qualified" />
                  <el-option label="不合格" value="unqualified" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="150">
              <template #default="scope">
                <el-input v-model="scope.row.remark" placeholder="请输入备注" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="scope">
                <el-button
                  type="danger"
                  size="small"
                  :icon="Delete"
                  @click="handleDeleteInspectionItem(scope.$index)"
                />
              </template>
            </el-table-column>
          </el-table>
          <div class="add-item-action">
            <el-button type="dashed" :icon="Plus" @click="handleAddInspectionItem">
              新增检验项目
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item label="检验说明" prop="description">
          <el-input
            v-model="recordForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入检验说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="recordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveResult">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Check, Download, Delete } from '@element-plus/icons-vue'
import { unwrapPageResponse } from '@/api'
import type { InspectionResult } from '../../../../types/qms'
import { InspectionResultAPI } from '@/api/qms'

// 查询表单
const queryForm = reactive({
  resultNo: '',
  taskNo: '',
  materialName: '',
  inspector: '',
  inspectionResult: '',
  auditStatus: ''
})

// 加载状态
const loading = ref(false)

// 检验结果列表
const resultList = ref<any[]>([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 选中项
const selectedResults = ref<InspectionResult[]>([])
const selectedResult = ref<InspectionResult | null>(null)

// 对话框状态
const detailDialogVisible = ref(false)
const recordDialogVisible = ref(false)
const recordFormRef = ref()

// 结果录入表单数据
const recordForm = reactive({
  taskNo: '',
  batchNo: '',
  inspectionQuantity: 0,
  qualifiedQuantity: 0,
  unqualifiedQuantity: 0,
  inspectionResult: 'qualified',
  inspector: '',
  inspectionTime: new Date().toLocaleString(),
  auditStatus: 'pending',
  description: '',
  inspectionItems: [
    {
      itemName: '',
      specification: '',
      actualValue: '',
      result: 'qualified',
      remark: ''
    }
  ]
})

// 结果录入表单验证规则
const recordFormRules = {
  taskNo: [{ required: true, message: '请选择任务编号', trigger: 'change' }],
  batchNo: [{ required: true, message: '请输入批次号', trigger: 'blur' }],
  inspectionQuantity: [{ required: true, message: '请输入检验数量', trigger: 'blur' }],
  inspector: [{ required: true, message: '请输入检验人员', trigger: 'blur' }],
  inspectionItems: [{ required: true, message: '请至少添加一个检验项目', trigger: 'change' }]
}

// 初始化
onMounted(() => {
  handleQuery()
})

// 查询数据
const handleQuery = async () => {
  loading.value = true
  try {
    const res = await InspectionResultAPI.getInspectionResults({
      page: pagination.currentPage,
      size: pagination.pageSize,
      resultNo: queryForm.resultNo || undefined,
      taskNo: queryForm.taskNo || undefined,
      auditStatus: queryForm.auditStatus || undefined
    } as any)
    const page = unwrapPageResponse<InspectionResult>(res)
    resultList.value = page.list
    pagination.total = page.total
  } catch (e: any) {
    ElMessage.error(e?.message || '获取检验结果列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  Object.assign(queryForm, {
    resultNo: '',
    taskNo: '',
    materialName: '',
    inspector: '',
    inspectionResult: '',
    auditStatus: ''
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
  selectedResults.value = selection
}

// 审核状态名称映射
const getAuditStatusName = (status: string) => {
  const statusMap: Record<string, string> = {
    'pending': '待审核',
    'approved': '审核通过',
    'rejected': '审核驳回'
  }
  return statusMap[status] || status
}

// 审核状态颜色映射
const getAuditStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger'
  }
  return colorMap[status] || 'default'
}

// 查看检验结果详情
const handleViewResult = (row: any) => {
  selectedResult.value = JSON.parse(JSON.stringify(row))
  detailDialogVisible.value = true
}

// 录入检验结果
const handleAddResult = () => {
  Object.assign(recordForm, {
    taskNo: '',
    batchNo: '',
    inspectionQuantity: 0,
    qualifiedQuantity: 0,
    unqualifiedQuantity: 0,
    inspectionResult: 'qualified',
    inspector: '',
    inspectionTime: new Date().toLocaleString(),
    auditStatus: 'pending',
    description: '',
    inspectionItems: [
      {
        itemName: '',
        specification: '',
        actualValue: '',
        result: 'qualified',
        remark: ''
      }
    ]
  })
  recordDialogVisible.value = true
}

// 批量审核
const handleBatchApprove = () => {
  if (selectedResults.value.length === 0) {
    ElMessage.warning('请选择要审核的结果')
    return
  }
  handleBatchApproveInternal()
}

const handleBatchApproveInternal = async () => {
  try {
    loading.value = true
    const ids = selectedResults.value.map((r: any) => r.id)
    await InspectionResultAPI.batchAuditInspectionResults({
      ids,
      auditStatus: 'approved',
      auditRemark: ''
    })
    ElMessage.success(`已批量审核 ${ids.length} 条检验结果`)
    selectedResults.value = []
    await handleQuery()
  } catch (e: any) {
    ElMessage.error(e?.message || '批量审核失败')
  } finally {
    loading.value = false
  }
}

// 审核通过
const handleApproveResult = async (row: any) => {
  try {
    await InspectionResultAPI.auditInspectionResult(row.id, {
      auditStatus: 'approved',
      auditRemark: ''
    })
    ElMessage.success(`检验结果 ${row.resultNo} 审核通过`)
    await handleQuery()
  } catch (e: any) {
    ElMessage.error(e?.message || '审核失败')
  }
}

// 审核驳回
const handleRejectResult = async (row: any) => {
  try {
    await InspectionResultAPI.auditInspectionResult(row.id, {
      auditStatus: 'rejected',
      auditRemark: '审核驳回'
    })
    ElMessage.success(`检验结果 ${row.resultNo} 审核驳回`)
    await handleQuery()
  } catch (e: any) {
    ElMessage.error(e?.message || '审核失败')
  }
}

// 新增检验项目
const handleAddInspectionItem = () => {
  recordForm.inspectionItems.push({
    itemName: '',
    specification: '',
    actualValue: '',
    result: 'qualified',
    remark: ''
  })
}

// 删除检验项目
const handleDeleteInspectionItem = (index: number) => {
  recordForm.inspectionItems.splice(index, 1)
}

// 保存检验结果
const handleSaveResult = () => {
  if (!recordFormRef.value) return
  
  recordFormRef.value.validate((valid: boolean) => {
    if (valid) {
      // 计算合格和不合格数量
      const qualifiedCount = recordForm.inspectionItems.filter(item => item.result === 'qualified').length
      const unqualifiedCount = recordForm.inspectionItems.filter(item => item.result === 'unqualified').length
      
      recordForm.qualifiedQuantity = qualifiedCount
      recordForm.unqualifiedQuantity = unqualifiedCount
      recordForm.inspectionResult = unqualifiedCount === 0 ? 'qualified' : 'unqualified'

      handleSaveResultInternal()
    }
  })
}

const handleSaveResultInternal = async () => {
  try {
    loading.value = true
    const payload: any = {
      ...recordForm,
      inspector: recordForm.inspector || 'admin',
      auditStatus: 'pending'
    }
    await InspectionResultAPI.createInspectionResult(payload)
    recordDialogVisible.value = false
    ElMessage.success('检验结果保存成功')
    await handleQuery()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    loading.value = false
  }
}

// 关闭详情对话框
const handleDetailDialogClose = () => {
  selectedResult.value = null
}

// 关闭录入对话框
const handleRecordDialogClose = () => {
  if (recordFormRef.value) {
    recordFormRef.value.resetFields()
  }
}

// 导出结果
const handleExport = () => {
  if (!resultList.value.length) {
    ElMessage.warning('暂无可导出的数据')
    return
  }
  const headers = ['结果编号', '任务编号', '物料编码', '物料名称', '批次号', '检验数量', '合格数', '不合格数', '检验结果', '审核状态']
  const rows = resultList.value.map((r: any) => [
    r.resultNo,
    r.taskNo,
    r.materialCode,
    r.materialName,
    r.batchNo,
    r.inspectionQuantity,
    r.qualifiedQuantity,
    r.unqualifiedQuantity,
    r.inspectionResult,
    r.auditStatus
  ])
  const csv = [headers, ...rows].map(line => line.map(v => `"${String(v ?? '').replace(/"/g, '""')}"`).join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `inspection-results-${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.result-recording-view {
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

.result-detail {
  margin-bottom: 20px;
}

.add-item-action {
  margin-top: 10px;
  display: flex;
  justify-content: flex-start;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .result-recording-view {
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
  .result-recording-view {
    padding: 12px;
  }
  
  .header-actions {
    flex-direction: column;
  }
}
</style>
