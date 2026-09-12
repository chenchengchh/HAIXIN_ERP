<template>
  <div class="nc-registration-view">
    <div class="page-header">
      <h3>不合格品登记</h3>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAddRegistration">
          新增登记
        </el-button>
        <el-button :icon="Download" @click="handleExport">
          导出登记
        </el-button>
      </div>
    </div>

    <!-- 查询条件 -->
    <el-card class="query-card">
      <el-form :model="queryForm" label-position="left" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="登记编号">
              <el-input v-model="queryForm.registrationNo" placeholder="请输入登记编号" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="物料名称">
              <el-input v-model="queryForm.materialName" placeholder="请输入物料名称" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="批次号">
              <el-input v-model="queryForm.batchNo" placeholder="请输入批次号" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="登记状态">
              <el-select v-model="queryForm.status" placeholder="请选择登记状态">
                <el-option label="已登记" value="registered" />
                <el-option label="已评审" value="reviewed" />
                <el-option label="已处理" value="processed" />
                <el-option label="已追踪" value="tracked" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="缺陷类型">
              <el-select v-model="queryForm.defectType" placeholder="请选择缺陷类型">
                <el-option label="外观缺陷" value="appearance" />
                <el-option label="尺寸缺陷" value="dimension" />
                <el-option label="性能缺陷" value="performance" />
                <el-option label="功能缺陷" value="function" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="登记日期">
              <el-date-picker
                v-model="queryForm.registrationDate"
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

    <!-- 不合格品登记列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="registrationList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="registrationNo" label="登记编号" min-width="150" />
        <el-table-column prop="materialCode" label="物料编码" min-width="120" />
        <el-table-column prop="materialName" label="物料名称" min-width="180" />
        <el-table-column prop="batchNo" label="批次号" min-width="150" />
        <el-table-column prop="quantity" label="不合格数量" width="120" align="center" />
        <el-table-column prop="defectType" label="缺陷类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getDefectTypeColor(scope.row.defectType)">
              {{ getDefectTypeName(scope.row.defectType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="defectDescription" label="缺陷描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getStatusColor(scope.row.status)">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registrant" label="登记人" width="120" align="center" />
        <el-table-column prop="registrationTime" label="登记时间" min-width="180" align="center" />
        <el-table-column label="操作" width="270" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleViewRegistration(scope.row)">
              查看
            </el-button>
            <el-button
              size="small"
              type="warning"
              :icon="MagicStick"
              :loading="eightDLoadingId === scope.row.id"
              @click="handleGenerateEightD(scope.row)"
            >
              AI 8D
            </el-button>
            <el-button size="small" v-if="scope.row.status === 'registered'" type="success" @click="handleSubmitReview(scope.row)">
              提交评审
            </el-button>
            <el-button size="small" v-if="scope.row.status === 'registered'" type="danger" @click="handleDeleteRegistration(scope.row)">
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
        ref="registrationFormRef"
        :model="registrationForm"
        label-position="top"
        :rules="registrationFormRules"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物料编码" prop="materialCode">
              <el-input v-model="registrationForm.materialCode" placeholder="请输入物料编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料名称" prop="materialName">
              <el-input v-model="registrationForm.materialName" placeholder="请输入物料名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次号" prop="batchNo">
              <el-input v-model="registrationForm.batchNo" placeholder="请输入批次号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="不合格数量" prop="quantity">
              <el-input v-model.number="registrationForm.quantity" placeholder="请输入不合格数量" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="缺陷类型" prop="defectType">
              <el-select v-model="registrationForm.defectType" placeholder="请选择缺陷类型">
                <el-option label="外观缺陷" value="appearance" />
                <el-option label="尺寸缺陷" value="dimension" />
                <el-option label="性能缺陷" value="performance" />
                <el-option label="功能缺陷" value="function" />
                <el-option label="其他缺陷" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="缺陷等级" prop="defectLevel">
              <el-select v-model="registrationForm.defectLevel" placeholder="请选择缺陷等级">
                <el-option label="致命缺陷" value="critical" />
                <el-option label="严重缺陷" value="major" />
                <el-option label="轻微缺陷" value="minor" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="缺陷描述" prop="defectDescription">
              <el-input
                v-model="registrationForm.defectDescription"
                type="textarea"
                :rows="4"
                placeholder="请输入缺陷描述"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发现部门" prop="discoveryDepartment">
              <el-input v-model="registrationForm.discoveryDepartment" placeholder="请输入发现部门" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发现人" prop="discoveryPerson">
              <el-input v-model="registrationForm.discoveryPerson" placeholder="请输入发现人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发现时间" prop="discoveryTime">
              <el-date-picker
                v-model="registrationForm.discoveryTime"
                type="datetime"
                placeholder="请选择发现时间"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发现位置" prop="discoveryLocation">
              <el-input v-model="registrationForm.discoveryLocation" placeholder="请输入发现位置" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveRegistration">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- AI 8D 报告骨架对话框 -->
    <el-dialog
      v-model="eightDDialogVisible"
      :title="`AI 8D 报告骨架 - ${eightDRegistrationNo}`"
      width="720px"
    >
      <el-alert
        type="info"
        :closable="false"
        title="骨架已落入决策中心（CONFIRM 级），需人工审核后生效"
        class="eight-d-alert"
      />
      <div v-if="eightDParsed" class="eight-d-content">
        <div v-for="section in eightDSections" :key="section.key" class="eight-d-section">
          <h4 class="eight-d-section-title">{{ section.key }}</h4>
          <!-- 数组字段渲染为列表 -->
          <ul v-if="isEightDArray(section.value)" class="eight-d-list">
            <li v-for="(item, idx) in section.value" :key="idx">{{ formatEightDItem(item) }}</li>
          </ul>
          <!-- 对象字段展开为键值对 -->
          <el-descriptions v-else-if="isEightDObject(section.value)" :column="1" border size="small">
            <el-descriptions-item v-for="(v, k) in section.value" :key="k" :label="String(k)">
              {{ formatEightDItem(v) }}
            </el-descriptions-item>
          </el-descriptions>
          <!-- 文本字段直接展示 -->
          <p v-else class="eight-d-text">{{ section.value }}</p>
        </div>
        <!-- 证据来源与关键指标 -->
        <div v-if="eightDEvidenceSources.length || eightDEvidenceMetrics.length" class="eight-d-section">
          <h4 class="eight-d-section-title">证据与指标</h4>
          <div v-if="eightDEvidenceSources.length" class="eight-d-sources">
            <span class="eight-d-sub-title">证据来源：</span>
            <el-tag v-for="(s, i) in eightDEvidenceSources" :key="i" size="small" class="eight-d-tag">
              {{ s }}
            </el-tag>
          </div>
          <el-descriptions
            v-if="eightDEvidenceMetrics.length"
            :column="2"
            border
            size="small"
            class="eight-d-metrics"
          >
            <el-descriptions-item v-for="m in eightDEvidenceMetrics" :key="m.key" :label="m.key">
              {{ m.value }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <!-- analysis 解析失败时展示原始文本 -->
      <pre v-else class="eight-d-raw">{{ eightDRawAnalysis || '暂无分析内容' }}</pre>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="eightDDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, MagicStick } from '@element-plus/icons-vue'
import { unwrapPageResponse } from '@/api'
import { NCRegistrationAPI } from '@/api/qms'
import { generateEightD, type AiSuggestion } from '@/api/ai'
import { DataTransformer } from '@/utils/data-transformer'

// 查询表单
const queryForm = reactive({
  registrationNo: '',
  materialName: '',
  batchNo: '',
  status: '',
  defectType: '',
  registrationDate: [] as Date[]
})

// 加载状态
const loading = ref(false)

// 不合格品登记列表
const registrationList = ref<any[]>([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 选中项
const selectedRegistrations = ref<any[]>([])

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('新增登记')
const registrationFormRef = ref()

// 登记表单数据
const registrationForm = reactive({
  id: 0,
  materialCode: '',
  materialName: '',
  batchNo: '',
  quantity: 0,
  defectType: '',
  defectLevel: '',
  defectDescription: '',
  discoveryDepartment: '',
  discoveryPerson: '',
  discoveryTime: new Date().toLocaleString(),
  discoveryLocation: '',
  status: 'registered',
  registrant: 'admin',
  registrationTime: new Date().toLocaleString()
})

// 登记表单验证规则
const registrationFormRules = {
  materialCode: [{ required: true, message: '请输入物料编码', trigger: 'blur' }],
  materialName: [{ required: true, message: '请输入物料名称', trigger: 'blur' }],
  batchNo: [{ required: true, message: '请输入批次号', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入不合格数量', trigger: 'blur' }],
  defectType: [{ required: true, message: '请选择缺陷类型', trigger: 'change' }],
  defectLevel: [{ required: true, message: '请选择缺陷等级', trigger: 'change' }],
  defectDescription: [{ required: true, message: '请输入缺陷描述', trigger: 'blur' }],
  discoveryDepartment: [{ required: true, message: '请输入发现部门', trigger: 'blur' }],
  discoveryPerson: [{ required: true, message: '请输入发现人', trigger: 'blur' }],
  discoveryTime: [{ required: true, message: '请选择发现时间', trigger: 'change' }]
}

// 初始化
onMounted(handleQuery)

// AI 8D 相关状态
const eightDLoadingId = ref<number | null>(null)
const eightDDialogVisible = ref(false)
const eightDRegistrationNo = ref('')
const eightDParsed = ref<any | null>(null)
const eightDRawAnalysis = ref('')

// D1→D8 分节列表（按键名排序保证顺序）
const eightDSections = computed(() => {
  const eightD = eightDParsed.value?.eightD
  if (!eightD || typeof eightD !== 'object' || Array.isArray(eightD)) return []
  return Object.keys(eightD)
    .sort()
    .map(key => ({ key, value: eightD[key] }))
})

// 证据来源列表
const eightDEvidenceSources = computed<string[]>(() => {
  const sources = eightDParsed.value?.evidence?.sources
  return Array.isArray(sources) ? sources.map((s: any) => String(s)) : []
})

// 关键指标键值对列表
const eightDEvidenceMetrics = computed<Array<{ key: string; value: string }>>(() => {
  const metrics = eightDParsed.value?.evidence?.metrics
  if (!metrics || typeof metrics !== 'object' || Array.isArray(metrics)) return []
  return Object.entries(metrics).map(([key, v]) => ({ key, value: String(v) }))
})

/** 判断 8D 字段值是否为数组 */
const isEightDArray = (v: any) => Array.isArray(v)

/** 判断 8D 字段值是否为对象（非数组） */
const isEightDObject = (v: any) => v !== null && typeof v === 'object' && !Array.isArray(v)

/** 格式化 8D 字段项显示文本 */
const formatEightDItem = (v: any): string => {
  if (v === null || v === undefined) return '-'
  if (typeof v === 'object') return JSON.stringify(v)
  return String(v)
}

/** AI 生成 8D 报告骨架并弹出结果对话框 */
const handleGenerateEightD = async (row: any) => {
  eightDLoadingId.value = row.id
  try {
    const resp = await generateEightD(row.id)
    // 对象型 data 直接解包为 AiSuggestion 实体（与决策中心消费模式一致）
    const suggestion = DataTransformer.unwrapData<AiSuggestion>(resp)
    if (!suggestion) {
      ElMessage.error('未获取到 AI 8D 生成结果')
      return
    }
    eightDRegistrationNo.value = row.registrationNo
    eightDRawAnalysis.value = suggestion.analysis || ''
    eightDParsed.value = null
    if (suggestion.analysis) {
      try {
        const parsed = JSON.parse(suggestion.analysis)
        eightDParsed.value = parsed
        eightDRegistrationNo.value = parsed?.registrationNo || row.registrationNo
      } catch {
        // 解析失败时保留原始文本，对话框直接展示
        eightDParsed.value = null
      }
    }
    eightDDialogVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || 'AI 8D 生成失败')
  } finally {
    eightDLoadingId.value = null
  }
}

// 查询数据
async function handleQuery() {
  loading.value = true
  try {
    const res = await NCRegistrationAPI.getNcRegistrations({
      page: pagination.currentPage,
      size: pagination.pageSize,
      registrationNo: queryForm.registrationNo || undefined,
      status: queryForm.status || undefined
    } as any)
    const page = unwrapPageResponse<any>(res)
    registrationList.value = page.list
    pagination.total = page.total
  } catch (e: any) {
    ElMessage.error(e?.message || '获取不合格品登记列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  Object.assign(queryForm, {
    registrationNo: '',
    materialName: '',
    batchNo: '',
    status: '',
    defectType: '',
    registrationDate: [] as Date[]
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
  selectedRegistrations.value = selection
}

// 缺陷类型名称映射
const getDefectTypeName = (type: string) => {
  const typeMap: Record<string, string> = {
    'appearance': '外观缺陷',
    'dimension': '尺寸缺陷',
    'performance': '性能缺陷',
    'function': '功能缺陷',
    'other': '其他缺陷'
  }
  return typeMap[type] || type
}

// 缺陷类型颜色映射
const getDefectTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'appearance': 'primary',
    'dimension': 'success',
    'performance': 'warning',
    'function': 'danger',
    'other': 'info'
  }
  return colorMap[type] || 'default'
}

// 状态名称映射
const getStatusName = (status: string) => {
  const statusMap: Record<string, string> = {
    'registered': '已登记',
    'reviewed': '已评审',
    'processed': '已处理',
    'tracked': '已追踪'
  }
  return statusMap[status] || status
}

// 状态颜色映射
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'registered': 'info',
    'reviewed': 'warning',
    'processed': 'success',
    'tracked': 'primary'
  }
  return colorMap[status] || 'default'
}

// 新增登记
const handleAddRegistration = () => {
  dialogTitle.value = '新增登记'
  Object.assign(registrationForm, {
    id: 0,
    materialCode: '',
    materialName: '',
    batchNo: '',
    quantity: 0,
    defectType: '',
    defectLevel: '',
    defectDescription: '',
    discoveryDepartment: '',
    discoveryPerson: '',
    discoveryTime: new Date().toLocaleString(),
    discoveryLocation: '',
    status: 'registered',
    registrant: 'admin',
    registrationTime: new Date().toLocaleString()
  })
  dialogVisible.value = true
}

// 查看登记
const handleViewRegistration = (row: any) => {
  dialogTitle.value = '查看登记'
  Object.assign(registrationForm, JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}

// 提交评审
const handleSubmitReview = (row: any) => {
  handleSubmitReviewInternal(row)
}

const handleSubmitReviewInternal = async (row: any) => {
  try {
    await NCRegistrationAPI.submitForReview(row.id)
    ElMessage.success(`不合格品登记 ${row.registrationNo} 已提交评审`)
    await handleQuery()
  } catch (e: any) {
    ElMessage.error(e?.message || '提交评审失败')
  }
}

// 删除登记
const handleDeleteRegistration = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除登记【${row.registrationNo}】吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    await NCRegistrationAPI.deleteNcRegistration(row.id)
    ElMessage.success('删除成功')
    await handleQuery()
  } catch (e: any) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e?.message || '删除失败')
  }
}

// 保存登记
const handleSaveRegistration = () => {
  if (!registrationFormRef.value) return
  
  registrationFormRef.value.validate((valid: boolean) => {
    if (valid) {
      handleSaveRegistrationInternal()
    }
  })
}

const handleSaveRegistrationInternal = async () => {
  try {
    loading.value = true
    const payload: any = { ...registrationForm }
    if (payload.id) {
      await NCRegistrationAPI.updateNcRegistration(payload.id, payload)
    } else {
      await NCRegistrationAPI.createNcRegistration(payload)
    }
    dialogVisible.value = false
    ElMessage.success('登记成功')
    await handleQuery()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    loading.value = false
  }
}

// 关闭对话框
const handleDialogClose = () => {
  if (registrationFormRef.value) {
    registrationFormRef.value.resetFields()
  }
}

// 导出登记
const handleExport = () => {
  if (!registrationList.value.length) {
    ElMessage.warning('暂无可导出的数据')
    return
  }
  const headers = ['登记编号', '物料编码', '物料名称', '批次号', '数量', '缺陷类型', '缺陷等级', '状态', '登记时间']
  const rows = registrationList.value.map((r: any) => [
    r.registrationNo,
    r.materialCode,
    r.materialName,
    r.batchNo,
    r.quantity,
    r.defectType,
    r.defectLevel,
    r.status,
    r.registrationTime
  ])
  const csv = [headers, ...rows].map(line => line.map(v => `"${String(v ?? '').replace(/"/g, '""')}"`).join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `nc-registrations-${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.nc-registration-view {
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

/* AI 8D 对话框样式 */
.eight-d-alert {
  margin-bottom: 16px;
}

.eight-d-content {
  max-height: 55vh;
  overflow-y: auto;
}

.eight-d-section {
  margin-bottom: 16px;
}

.eight-d-section-title {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.eight-d-list {
  margin: 0;
  padding-left: 20px;
  line-height: 1.8;
}

.eight-d-text {
  margin: 0;
  line-height: 1.8;
  white-space: pre-wrap;
}

.eight-d-sources {
  margin-bottom: 12px;
}

.eight-d-sub-title {
  font-weight: 600;
  margin-right: 8px;
}

.eight-d-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

.eight-d-metrics {
  margin-top: 8px;
}

.eight-d-raw {
  max-height: 55vh;
  overflow: auto;
  margin: 0;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .nc-registration-view {
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
  .nc-registration-view {
    padding: 12px;
  }
  
  .header-actions {
    flex-direction: column;
  }
}
</style>
