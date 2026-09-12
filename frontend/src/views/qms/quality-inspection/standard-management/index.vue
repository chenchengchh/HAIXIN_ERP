<template>
  <div class="standard-management-view">
    <div class="page-header">
      <h3>检验标准管理</h3>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAddStandard">
          新增检验标准
        </el-button>
        <el-button :icon="Download" @click="handleExport">
          导出标准
        </el-button>
        <el-upload
          :auto-upload="false"
          :show-file-list="false"
          action="#"
          accept=".xlsx,.xls,.json"
          @change="handleImport"
        >
          <el-button :icon="Upload">
            导入标准
          </el-button>
        </el-upload>
      </div>
    </div>

    <!-- 查询条件 -->
    <el-card class="query-card">
      <el-form :model="queryForm" label-position="left" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="标准编号">
              <el-input v-model="queryForm.standardNo" placeholder="请输入标准编号" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="物料名称">
              <el-input v-model="queryForm.materialName" placeholder="请输入物料名称" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select v-model="queryForm.status" placeholder="请选择状态">
                <el-option label="启用" value="active" />
                <el-option label="禁用" value="inactive" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="创建日期">
              <el-date-picker
                v-model="queryForm.createDate"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="query-actions">
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 标准列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="standardList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="standardNo" label="标准编号" min-width="150" />
        <el-table-column prop="materialCode" label="物料编码" min-width="120" />
        <el-table-column prop="materialName" label="物料名称" min-width="180" />
        <el-table-column prop="version" label="版本" width="80" align="center" />
        <el-table-column prop="aqlLevel" label="AQL水准" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="创建人" width="120" align="center" />
        <el-table-column prop="createTime" label="创建时间" min-width="180" align="center">
          <template #default="scope">
            <span>{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditStandard(scope.row)">
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="handleDeleteStandard(scope.row)">
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
      width="800px"
      @close="handleDialogClose"
    >
      <el-form
        ref="standardFormRef"
        :model="standardForm"
        label-position="top"
        :rules="formRules"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="标准编号" prop="standardNo">
              <el-input v-model="standardForm.standardNo" placeholder="请输入标准编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料编码" prop="materialCode">
              <el-input v-model="standardForm.materialCode" placeholder="请输入物料编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料名称" prop="materialName">
              <el-input v-model="standardForm.materialName" placeholder="请输入物料名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本" prop="version">
              <el-input v-model="standardForm.version" placeholder="请输入版本号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="AQL水准" prop="aqlLevel">
              <el-select v-model="standardForm.aqlLevel" placeholder="请选择AQL水准">
                <el-option label="0.010" value="0.010" />
                <el-option label="0.015" value="0.015" />
                <el-option label="0.025" value="0.025" />
                <el-option label="0.040" value="0.040" />
                <el-option label="0.065" value="0.065" />
                <el-option label="0.10" value="0.10" />
                <el-option label="0.15" value="0.15" />
                <el-option label="0.25" value="0.25" />
                <el-option label="0.40" value="0.40" />
                <el-option label="0.65" value="0.65" />
                <el-option label="1.0" value="1.0" />
                <el-option label="1.5" value="1.5" />
                <el-option label="2.5" value="2.5" />
                <el-option label="4.0" value="4.0" />
                <el-option label="6.5" value="6.5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="standardForm.status" placeholder="请选择状态">
                <el-option label="启用" value="active" />
                <el-option label="禁用" value="inactive" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="检验项目" prop="inspectionItems">
          <el-table
            :data="standardForm.inspectionItems"
            border
            style="width: 100%"
          >
            <el-table-column prop="itemName" label="项目名称" min-width="120">
              <template #default="scope">
                <el-input v-model="scope.row.itemName" placeholder="请输入项目名称" />
              </template>
            </el-table-column>
            <el-table-column prop="itemType" label="项目类型" width="120">
              <template #default="scope">
                <el-select v-model="scope.row.itemType" placeholder="请选择类型">
                  <el-option label="定量" value="quantitative" />
                  <el-option label="定性" value="qualitative" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="specification" label="规格要求" min-width="150">
              <template #default="scope">
                <el-input v-model="scope.row.specification" placeholder="请输入规格要求" />
              </template>
            </el-table-column>
            <el-table-column prop="tolerance" label="公差范围" min-width="120">
              <template #default="scope">
                <el-input v-model="scope.row.tolerance" placeholder="请输入公差范围" />
              </template>
            </el-table-column>
            <el-table-column prop="testMethod" label="检验方法" min-width="150">
              <template #default="scope">
                <el-input v-model="scope.row.testMethod" placeholder="请输入检验方法" />
              </template>
            </el-table-column>
            <el-table-column prop="testEquipment" label="检验设备" min-width="120">
              <template #default="scope">
                <el-input v-model="scope.row.testEquipment" placeholder="请输入检验设备" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="scope">
                <el-button
                  type="danger"
                  size="small"
                  :icon="Delete"
                  @click="handleDeleteItem(scope.$index)"
                />
              </template>
            </el-table-column>
          </el-table>
          <div class="add-item-action">
            <el-button type="dashed" :icon="Plus" @click="handleAddItem">
              新增检验项目
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveStandard">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Upload, Delete } from '@element-plus/icons-vue'
import { unwrapPageResponse } from '@/api'
import type { InspectionStandard } from '../../../../types/qms'
import { InspectionStandardAPI } from '@/api/qms'

// 查询表单
const queryForm = reactive({
  standardNo: '',
  materialName: '',
  status: '',
  createDate: [] as Date[]
})

// 加载状态
const loading = ref(false)

// 检验标准列表
const standardList = ref<any[]>([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 选中项
const selectedStandards = ref<InspectionStandard[]>([])

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('新增检验标准')
const standardFormRef = ref()

// 表单数据
const standardForm = reactive({
  id: 0,
  standardNo: '',
  materialCode: '',
  materialName: '',
  version: '',
  aqlLevel: '',
  status: 'active',
  inspectionItems: [
    {
      itemName: '',
      itemType: 'quantitative',
      specification: '',
      tolerance: '',
      testMethod: '',
      testEquipment: ''
    }
  ]
})

// 表单验证规则
const formRules = {
  standardNo: [{ required: true, message: '请输入标准编号', trigger: 'blur' }],
  materialCode: [{ required: true, message: '请输入物料编码', trigger: 'blur' }],
  materialName: [{ required: true, message: '请输入物料名称', trigger: 'blur' }],
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
  aqlLevel: [{ required: true, message: '请选择AQL水准', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  inspectionItems: [{ required: true, message: '请至少添加一个检验项目', trigger: 'change' }]
}

// 初始化
onMounted(handleQuery)

// 查询数据
async function handleQuery() {
  loading.value = true
  try {
    const res = await InspectionStandardAPI.getInspectionStandards({
      page: pagination.currentPage,
      size: pagination.pageSize,
      standardNo: queryForm.standardNo || undefined,
      materialName: queryForm.materialName || undefined,
      status: queryForm.status || undefined
    })
    const page = unwrapPageResponse<InspectionStandard>(res)
    standardList.value = page.list
    pagination.total = page.total
  } catch (e: any) {
    ElMessage.error(e?.message || '获取检验标准列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  Object.assign(queryForm, {
    standardNo: '',
    materialName: '',
    status: '',
    createDate: [] as Date[]
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
  selectedStandards.value = selection
}

// 新增检验标准
const handleAddStandard = () => {
  dialogTitle.value = '新增检验标准'
  Object.assign(standardForm, {
    id: 0,
    standardNo: '',
    materialCode: '',
    materialName: '',
    version: '',
    aqlLevel: '',
    status: 'active',
    inspectionItems: [
      {
        itemName: '',
        itemType: 'quantitative',
        specification: '',
        tolerance: '',
        testMethod: '',
        testEquipment: ''
      }
    ]
  })
  dialogVisible.value = true
}

// 编辑检验标准
const handleEditStandard = (row: any) => {
  dialogTitle.value = '编辑检验标准'
  Object.assign(standardForm, JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}

// 删除检验标准
const handleDeleteStandard = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除检验标准【${row.standardNo}】吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    await InspectionStandardAPI.deleteInspectionStandard(row.id)
    ElMessage.success('删除成功')
    await handleQuery()
  } catch (e: any) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e?.message || '删除失败')
  }
}

// 保存检验标准
const handleSaveStandard = () => {
  if (!standardFormRef.value) return
  
  standardFormRef.value.validate((valid: boolean) => {
    if (valid) {
      handleSaveStandardInternal()
    }
  })
}

const handleSaveStandardInternal = async () => {
  try {
    loading.value = true
    const payload = {
      standardNo: standardForm.standardNo,
      materialCode: standardForm.materialCode,
      materialName: standardForm.materialName,
      version: standardForm.version,
      aqlLevel: standardForm.aqlLevel,
      status: standardForm.status,
      inspectionItems: standardForm.inspectionItems,
      creator: 'admin'
    }
    if (standardForm.id) {
      await InspectionStandardAPI.updateInspectionStandard(standardForm.id, payload as any)
    } else {
      await InspectionStandardAPI.createInspectionStandard(payload as any)
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
  if (standardFormRef.value) {
    standardFormRef.value.resetFields()
  }
}

// 新增检验项目
const handleAddItem = () => {
  standardForm.inspectionItems.push({
    itemName: '',
    itemType: 'quantitative',
    specification: '',
    tolerance: '',
    testMethod: '',
    testEquipment: ''
  })
}

// 删除检验项目
const handleDeleteItem = (index: number) => {
  standardForm.inspectionItems.splice(index, 1)
}

// 导出标准
const handleExport = () => {
  if (!standardList.value.length) {
    ElMessage.warning('暂无可导出的数据')
    return
  }
  const headers = ['标准编号', '物料编码', '物料名称', '版本', 'AQL水准', '状态', '创建人', '创建时间']
  const rows = standardList.value.map((r: any) => [
    r.standardNo,
    r.materialCode,
    r.materialName,
    r.version,
    r.aqlLevel,
    r.status,
    r.creator,
    r.createTime || r.createdTime
  ])
  const csv = [headers, ...rows].map(line => line.map(v => `"${String(v ?? '').replace(/"/g, '""')}"`).join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `inspection-standards-${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

// 导入标准
const handleImport = async (file: any) => {
  const rawFile: File | undefined = file?.raw
  if (!rawFile) {
    ElMessage.error('未获取到导入文件')
    return
  }
  const name = rawFile.name.toLowerCase()
  if (!name.endsWith('.json')) {
    ElMessage.warning('当前仅支持JSON导入（.json），Excel导入待完善')
    return
  }
  try {
    const text = await rawFile.text()
    const list = JSON.parse(text)
    if (!Array.isArray(list) || list.length === 0) {
      ElMessage.warning('JSON文件内容为空或格式不正确')
      return
    }
    loading.value = true
    for (const item of list) {
      await InspectionStandardAPI.createInspectionStandard(item)
    }
    ElMessage.success('导入成功')
    await handleQuery()
  } catch (e: any) {
    ElMessage.error(e?.message || '导入失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  return dateString
}
</script>

<style scoped>
.standard-management-view {
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

.add-item-action {
  margin-top: 10px;
  display: flex;
  justify-content: flex-start;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .standard-management-view {
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
  .standard-management-view {
    padding: 12px;
  }
  
  .query-form {
    width: 100%;
  }
  
  .header-actions {
    flex-direction: column;
  }
}
</style>
