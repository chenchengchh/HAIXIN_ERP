<template>
  <div class="capa-view">
    <div class="page-header">
      <h3>预防措施</h3>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>新增预防措施
      </el-button>
    </div>

    <!-- 查询表单 -->
    <el-card class="mb-4" shadow="hover">
      <el-form :model="queryForm" inline>
        <el-form-item label="异常编号">
          <el-input v-model="queryForm.reportNo" placeholder="请输入异常编号" clearable />
        </el-form-item>
        <el-form-item label="预防措施状态">
          <el-select v-model="queryForm.status" placeholder="请选择预防措施状态" clearable>
            <el-option label="待制定" value="pending" />
            <el-option label="执行中" value="implementing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已验证" value="verified" />
            <el-option label="已关闭" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item label="验证状态">
          <el-select v-model="queryForm.verifyStatus" placeholder="请选择验证状态" clearable>
            <el-option label="待验证" value="pending" />
            <el-option label="验证中" value="processing" />
            <el-option label="验证通过" value="passed" />
            <el-option label="验证失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预防措施列表 -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>预防措施列表</span>
          <el-tag size="small" type="info">{{ total }} 条记录</el-tag>
        </div>
      </template>
      <el-table :data="capaList" border style="width: 100%">
        <el-table-column prop="capaNo" label="预防措施编号" min-width="120" />
        <el-table-column prop="reportNo" label="异常编号" min-width="120" />
        <el-table-column prop="title" label="异常标题" min-width="180" />
        <el-table-column prop="capaContent" label="预防措施内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="implementer" label="实施人" min-width="100" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="verifyStatus" label="验证状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getVerifyStatusTagType(scope.row.verifyStatus)">
              {{ getVerifyStatusLabel(scope.row.verifyStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveness" label="有效性" min-width="100">
          <template #default="scope">
            <el-rate
              v-model="scope.row.effectiveness"
              disabled
              :colors="['#F7BA2A', '#F7BA2A', '#67C23A']"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewDetail(scope.row)">
              查看
            </el-button>
            <el-button size="small" @click="editCapa(scope.row)">
              编辑
            </el-button>
            <el-button size="small" type="success" @click="completeCapa(scope.row)">
              完成执行
            </el-button>
            <el-button size="small" type="warning" @click="verifyCapa(scope.row)">
              验证
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination mt-4">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑预防措施对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="60%"
      @close="resetForm"
    >
      <el-form
        ref="capaFormRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="异常编号" prop="reportNo">
          <el-input v-model="formData.reportNo" placeholder="请输入异常编号" />
        </el-form-item>
        <el-form-item label="异常标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入异常标题" />
        </el-form-item>
        <el-form-item label="预防措施内容" prop="capaContent">
          <el-input
            v-model="formData.capaContent"
            type="textarea"
            rows="4"
            placeholder="请输入预防措施内容"
          />
        </el-form-item>
        <el-form-item label="实施人" prop="implementer">
          <el-input v-model="formData.implementer" placeholder="请输入实施人" />
        </el-form-item>
        <el-form-item label="计划开始日期" prop="planStartDate">
          <el-date-picker
            v-model="formData.planStartDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="请选择计划开始日期"
          />
        </el-form-item>
        <el-form-item label="计划完成日期" prop="planEndDate">
          <el-date-picker
            v-model="formData.planEndDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="请选择计划完成日期"
          />
        </el-form-item>
        <el-form-item label="实际开始日期" prop="actualStartDate">
          <el-date-picker
            v-model="formData.actualStartDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="请选择实际开始日期"
          />
        </el-form-item>
        <el-form-item label="实际完成日期" prop="actualEndDate">
          <el-date-picker
            v-model="formData.actualEndDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="请选择实际完成日期"
          />
        </el-form-item>
        <el-form-item label="执行状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择执行状态">
            <el-option label="待制定" value="pending" />
            <el-option label="执行中" value="implementing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已验证" value="verified" />
            <el-option label="已关闭" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item label="验证状态" prop="verifyStatus">
          <el-select v-model="formData.verifyStatus" placeholder="请选择验证状态">
            <el-option label="待验证" value="pending" />
            <el-option label="验证中" value="processing" />
            <el-option label="验证通过" value="passed" />
            <el-option label="验证失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item label="验证结果" prop="verifyResult">
          <el-input
            v-model="formData.verifyResult"
            type="textarea"
            rows="3"
            placeholder="请输入验证结果"
          />
        </el-form-item>
        <el-form-item label="有效性" prop="effectiveness">
          <el-rate v-model="formData.effectiveness" :colors="['#F7BA2A', '#F7BA2A', '#67C23A']" />
          <div class="effectiveness-desc mt-2">
            <span>1-2分：无效</span>
            <span>3分：一般</span>
            <span>4-5分：有效</span>
          </div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            rows="2"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 验证对话框 -->
    <el-dialog
      v-model="verifyDialogVisible"
      title="验证预防措施"
      width="50%"
      @close="resetVerifyForm"
    >
      <el-form
        ref="verifyFormRef"
        :model="verifyFormData"
        :rules="verifyFormRules"
        label-width="120px"
      >
        <el-form-item label="验证状态" prop="verifyStatus">
          <el-select v-model="verifyFormData.verifyStatus" placeholder="请选择验证状态">
            <el-option label="验证通过" value="passed" />
            <el-option label="验证失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item label="验证结果" prop="verifyResult">
          <el-input
            v-model="verifyFormData.verifyResult"
            type="textarea"
            rows="4"
            placeholder="请输入验证结果"
          />
        </el-form-item>
        <el-form-item label="有效性" prop="effectiveness">
          <el-rate v-model="verifyFormData.effectiveness" :colors="['#F7BA2A', '#F7BA2A', '#67C23A']" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="verifyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitVerifyForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="预防措施详情"
      width="70%"
      @close="closeDetailDialog"
    >
      <div class="detail-container">
        <div class="detail-header">
          <h4 class="capa-no">{{ detailData.capaNo }}</h4>
          <div class="capa-info">
            <el-tag :type="getStatusTagType(detailData.status)">
              {{ getStatusLabel(detailData.status) }}
            </el-tag>
            <el-tag :type="getVerifyStatusTagType(detailData.verifyStatus)">
              {{ getVerifyStatusLabel(detailData.verifyStatus) }}
            </el-tag>
          </div>
        </div>
        <div class="detail-body">
          <div class="detail-row">
            <div class="detail-label">异常编号：</div>
            <div class="detail-value">{{ detailData.reportNo }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">异常标题：</div>
            <div class="detail-value">{{ detailData.title }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">实施人：</div>
            <div class="detail-value">{{ detailData.implementer }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">计划开始日期：</div>
            <div class="detail-value">{{ detailData.planStartDate }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">计划完成日期：</div>
            <div class="detail-value">{{ detailData.planEndDate }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">实际开始日期：</div>
            <div class="detail-value">{{ detailData.actualStartDate || '未开始' }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">实际完成日期：</div>
            <div class="detail-value">{{ detailData.actualEndDate || '未完成' }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">预防措施内容：</div>
            <div class="detail-value">{{ detailData.capaContent }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">验证结果：</div>
            <div class="detail-value">{{ detailData.verifyResult || '暂无' }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">有效性：</div>
            <div class="detail-value">
              <el-rate
                v-model="detailData.effectiveness"
                disabled
                :colors="['#F7BA2A', '#F7BA2A', '#67C23A']"
              />
              <div class="effectiveness-desc mt-2">
                <span>1-2分：无效</span>
                <span>3分：一般</span>
                <span>4-5分：有效</span>
              </div>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-label">备注：</div>
            <div class="detail-value">{{ detailData.remark || '无' }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeDetailDialog">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { unwrapPageResponse, unwrapResponseData } from '@/api'
import { CAPAAPI } from '@/api/qms'

// 预防措施类型
interface CAPA {
  id: string | number
  capaNo: string
  reportNo: string
  title: string
  capaContent: string
  implementer: string
  planStartDate: string
  planEndDate: string
  actualStartDate: string
  actualEndDate: string
  status: 'pending' | 'implementing' | 'completed' | 'verified' | 'closed'
  verifyStatus: 'pending' | 'processing' | 'passed' | 'failed'
  verifyResult: string
  effectiveness: number
  remark: string
}

// 查询表单
const queryForm = reactive({
  reportNo: '',
  status: '',
  verifyStatus: ''
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

// 预防措施列表
const capaList = ref<CAPA[]>([])
const total = ref(0)

// 对话框数据
const dialogVisible = ref(false)
const dialogTitle = ref('新增预防措施')
const capaFormRef = ref()

// 表单数据
const formData = reactive<Partial<CAPA>>({
  capaNo: '',
  reportNo: '',
  title: '',
  capaContent: '',
  implementer: '',
  planStartDate: '',
  planEndDate: '',
  actualStartDate: '',
  actualEndDate: '',
  status: 'pending',
  verifyStatus: 'pending',
  verifyResult: '',
  effectiveness: 3,
  remark: ''
})

// 表单验证规则
const formRules = reactive({
  reportNo: [{ required: true, message: '请输入异常编号', trigger: 'blur' }],
  title: [{ required: true, message: '请输入异常标题', trigger: 'blur' }],
  capaContent: [{ required: true, message: '请输入预防措施内容', trigger: 'blur' }],
  implementer: [{ required: true, message: '请输入实施人', trigger: 'blur' }],
  planStartDate: [{ required: true, message: '请选择计划开始日期', trigger: 'change' }],
  planEndDate: [{ required: true, message: '请选择计划完成日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择执行状态', trigger: 'change' }],
  verifyStatus: [{ required: true, message: '请选择验证状态', trigger: 'change' }]
})

// 验证对话框数据
const verifyDialogVisible = ref(false)
const verifyFormRef = ref()
const verifyFormData = reactive({
  verifyStatus: 'passed',
  verifyResult: '',
  effectiveness: 3
})

// 详情对话框数据
const detailDialogVisible = ref(false)
const detailData = ref<CAPA>({
  id: '',
  capaNo: '',
  reportNo: '',
  title: '',
  capaContent: '',
  implementer: '',
  planStartDate: '',
  planEndDate: '',
  actualStartDate: '',
  actualEndDate: '',
  status: 'pending',
  verifyStatus: 'pending',
  verifyResult: '',
  effectiveness: 3,
  remark: ''
})

// 验证表单验证规则
const verifyFormRules = reactive({
  verifyStatus: [{ required: true, message: '请选择验证状态', trigger: 'change' }],
  verifyResult: [{ required: true, message: '请输入验证结果', trigger: 'blur' }]
})

const formatDate = (value: any) => {
  if (!value) return ''
  const text = String(value)
  return text.length >= 10 ? text.slice(0, 10) : text
}

const calcStatus = (implementationStatus: any, verifyStatus: any): CAPA['status'] => {
  const impl = implementationStatus == null ? '' : String(implementationStatus)
  const verify = verifyStatus == null ? '' : String(verifyStatus)
  if (verify === 'passed') return 'verified'
  if (impl === 'completed') return 'completed'
  if (impl === 'implementing' || impl === 'in_progress') return 'implementing'
  return 'pending'
}

const toViewModel = (entity: any): CAPA => {
  const team = Array.isArray(entity?.implementationTeam) ? entity.implementationTeam : []
  const preventive = String(entity?.preventiveMeasure ?? '')
  const corrective = String(entity?.correctiveMeasure ?? '')
  const capaContent = [preventive, corrective].filter(Boolean).join('\n')
  const verifyStatus = String(entity?.verifyStatus ?? 'pending') as any

  return {
    id: entity?.id ?? '',
    capaNo: String(entity?.id ?? ''),
    reportNo: String(entity?.reportNo ?? ''),
    title: '',
    capaContent,
    implementer: String(team[0] ?? ''),
    planStartDate: formatDate(entity?.createdTime),
    planEndDate: formatDate(entity?.implementationDeadline),
    actualStartDate: formatDate(entity?.createdTime),
    actualEndDate: formatDate(entity?.actualCompletionDate),
    status: calcStatus(entity?.implementationStatus, verifyStatus),
    verifyStatus,
    verifyResult: String(entity?.verifyResult ?? ''),
    effectiveness: verifyStatus === 'passed' ? 4 : 0,
    remark: ''
  }
}

// 获取状态标签样式
const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: 'warning',
    implementing: 'info',
    completed: 'success',
    verified: 'success',
    closed: 'success'
  }
  return statusMap[status] || 'info'
}

// 获取状态标签文本
const getStatusLabel = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '待制定',
    implementing: '执行中',
    completed: '已完成',
    verified: '已验证',
    closed: '已关闭'
  }
  return statusMap[status] || status
}

// 获取验证状态标签样式
const getVerifyStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: 'warning',
    processing: 'info',
    passed: 'success',
    failed: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取验证状态标签文本
const getVerifyStatusLabel = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '待验证',
    processing: '验证中',
    passed: '验证通过',
    failed: '验证失败'
  }
  return statusMap[status] || status
}

const handleQuery = async () => {
  try {
    const res = await CAPAAPI.getCAPAs({
      page: pagination.currentPage,
      size: pagination.pageSize,
      reportNo: queryForm.reportNo || undefined,
      verifyStatus: queryForm.verifyStatus || undefined
    })
    const page = unwrapPageResponse<any>(res)
    const records = page.list as any[]
    let mapped = records.map(toViewModel)

    if (queryForm.status) {
      mapped = mapped.filter(item => item.status === queryForm.status)
    }
    if (queryForm.verifyStatus) {
      mapped = mapped.filter(item => item.verifyStatus === queryForm.verifyStatus)
    }

    capaList.value = mapped
    total.value = page.total || mapped.length
  } catch (e: any) {
    ElMessage.error(e?.message || '获取预防措施失败')
  }
}

// 重置查询表单
const resetQuery = () => {
  Object.assign(queryForm, {
    reportNo: '',
    status: '',
    verifyStatus: ''
  })
  handleQuery()
}

// 打开新增对话框
const openAddDialog = () => {
  dialogTitle.value = '新增预防措施'
  resetForm()
  dialogVisible.value = true
}

// 打开编辑对话框
const editCapa = (row: CAPA) => {
  dialogTitle.value = '编辑预防措施'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const viewDetail = async (row: CAPA) => {
  try {
    const res = await CAPAAPI.getCAPAById(row.id)
    detailData.value = toViewModel(unwrapResponseData<any>(res) || {})
    detailDialogVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '获取详情失败')
  }
}

// 关闭详情对话框
const closeDetailDialog = () => {
  detailDialogVisible.value = false
}

// 完成执行
const completeCapa = (row: CAPA) => {
  ElMessageBox.confirm('确定要将这条预防措施标记为已完成吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  })
    .then(async () => {
      const detailRes = await CAPAAPI.getCAPAById(row.id)
      const entity = unwrapResponseData<any>(detailRes) || {}
      const today = new Date().toISOString().slice(0, 10)
      const updated: any = {
        ...entity,
        implementationStatus: 'completed',
        actualCompletionDate: today
      }
      await CAPAAPI.updateCAPA(row.id, updated)
      ElMessage.success('执行已完成')
      await handleQuery()
    })
    .catch(() => {})
}

// 验证预防措施
const currentVerifyCapaId = ref<string | number>('')
const verifyCapa = (row: CAPA) => {
  currentVerifyCapaId.value = row.id
  Object.assign(verifyFormData, {
    verifyStatus: row.verifyStatus,
    verifyResult: row.verifyResult,
    effectiveness: row.effectiveness
  })
  verifyDialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (capaFormRef.value) {
    capaFormRef.value.resetFields()
  }
  Object.assign(formData, {
    capaNo: '',
    reportNo: '',
    title: '',
    capaContent: '',
    implementer: '',
    planStartDate: '',
    planEndDate: '',
    actualStartDate: '',
    actualEndDate: '',
    status: 'pending',
    verifyStatus: 'pending',
    verifyResult: '',
    effectiveness: 3,
    remark: ''
  })
}

// 重置验证表单
const resetVerifyForm = () => {
  if (verifyFormRef.value) {
    verifyFormRef.value.resetFields()
  }
  Object.assign(verifyFormData, {
    verifyStatus: 'passed',
    verifyResult: '',
    effectiveness: 3
  })
}

// 提交表单
const submitForm = async () => {
  if (!capaFormRef.value) return
  
  try {
    await capaFormRef.value.validate()
    
    const payload: any = {
      reportNo: formData.reportNo,
      preventiveMeasure: formData.capaContent,
      correctiveMeasure: '',
      implementationTeam: formData.implementer ? [formData.implementer] : [],
      implementationDeadline: formData.planEndDate || undefined,
      actualCompletionDate: formData.actualEndDate || undefined,
      implementationStatus: formData.status === 'implementing' ? 'implementing' : formData.status === 'completed' || formData.status === 'verified' ? 'completed' : 'pending',
      verifyStatus: formData.verifyStatus,
      verifyResult: formData.verifyResult
    }

    if (formData.id) {
      const detailRes = await CAPAAPI.getCAPAById(formData.id)
      const entity = unwrapResponseData<any>(detailRes) || {}
      await CAPAAPI.updateCAPA(formData.id, { ...entity, ...payload })
      ElMessage.success('编辑成功')
    } else {
      await CAPAAPI.createCAPA(payload)
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
    resetForm()
    await handleQuery()
  } catch (error) {
    console.error('表单验证失败：', error)
  }
}

// 提交验证表单
const submitVerifyForm = async () => {
  if (!verifyFormRef.value) return
  
  try {
    await verifyFormRef.value.validate()
    
    if (!currentVerifyCapaId.value) {
      ElMessage.error('未找到要验证的CAPA记录')
      return
    }
    const verifyStatus = verifyFormData.verifyStatus === 'failed' ? 'failed' : 'passed'
    await CAPAAPI.verifyCAPA(currentVerifyCapaId.value, {
      verifyStatus,
      verifyResult: verifyFormData.verifyResult
    } as any)
    ElMessage.success('验证成功')
    verifyDialogVisible.value = false
    resetVerifyForm()
    currentVerifyCapaId.value = ''
    await handleQuery()
  } catch (error) {
    console.error('验证表单验证失败：', error)
  }
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

// 页面加载时初始化数据
onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.capa-view {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.mb-4 {
  margin-bottom: 20px;
}

.effectiveness-desc {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #909399;
}

.mt-2 {
  margin-top: 10px;
}

/* 详情对话框样式 */
.detail-container {
  padding: 20px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.capa-no {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.capa-info {
  display: flex;
  gap: 8px;
}

.detail-body {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
}

.detail-row {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  width: 120px;
  font-weight: 500;
  color: #606266;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: #303133;
  word-break: break-word;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .capa-view {
    padding: 12px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .pagination {
    justify-content: center;
  }
  
  .effectiveness-desc {
    flex-direction: column;
    gap: 5px;
  }
  
  .detail-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .detail-row {
    flex-direction: column;
    gap: 4px;
  }
  
  .detail-label {
    width: auto;
  }
}
</style>
