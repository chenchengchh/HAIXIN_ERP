<template>
  <div class="supplier-credit-view">
    <SubModuleHeader title="供应商信用管理" parentTitle="供应商准入管理" parentPath="/home/srm/supplier" />
    <div class="content-header">
      <div class="action-buttons">
        <el-button type="primary" @click="handleCreate">新增信用记录</el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-card shadow="never">
        <el-form :model="searchForm" layout="inline" size="small">
          <el-form-item label="供应商名称">
            <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" />
          </el-form-item>
          <el-form-item label="信用等级">
            <el-select v-model="searchForm.creditLevel" placeholder="请选择信用等级">
              <el-option label="全部" value="" />
              <el-option label="AAA" value="AAA" />
              <el-option label="AA" value="AA" />
              <el-option label="A" value="A" />
              <el-option label="B" value="B" />
              <el-option label="C" value="C" />
              <el-option label="D" value="D" />
            </el-select>
          </el-form-item>
          <el-form-item label="风险等级">
            <el-select v-model="searchForm.riskLevel" placeholder="请选择风险等级">
              <el-option label="全部" value="" />
              <el-option label="低风险" value="LOW" />
              <el-option label="中风险" value="MEDIUM" />
              <el-option label="高风险" value="HIGH" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    
    <!-- 信用列表区域 -->
    <div class="table-area">
      <el-card shadow="never">
        <el-table v-loading="loading" :data="creditList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="supplierName" label="供应商名称" width="180" />
          <el-table-column prop="creditScore" label="信用评分" width="120">
            <template #default="scope">
              <el-progress 
                :percentage="scope.row.creditScore" 
                :color="getScoreColor(scope.row.creditScore)"
                :stroke-width="10"
                text-inside
              />
            </template>
          </el-table-column>
          <el-table-column prop="creditLevel" label="信用等级" width="120">
            <template #default="scope">
              <el-tag :type="getCreditLevelType(scope.row.creditLevel)">
                {{ scope.row.creditLevel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="riskLevel" label="风险等级" width="120">
            <template #default="scope">
              <el-tag :type="getRiskLevelType(scope.row.riskLevel)">
                {{ scope.row.riskLevel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="evaluationDate" label="评估日期" width="180" />
          <el-table-column prop="nextEvaluationDate" label="下次评估日期" width="180" />
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="handleView(scope.row)">查看</el-button>
              <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="success" @click="handleEvaluate(scope.row)">重新评估</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
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
    </div>
    
    <!-- 信用记录表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="70%"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="供应商" prop="supplierName">
          <el-select v-model="formData.supplierName" placeholder="请选择供应商" filterable @change="handleSupplierChange">
            <el-option 
              v-for="item in supplierOptions" 
              :key="item.id" 
              :label="item.supplierName" 
              :value="item.supplierName" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="信用评分" prop="creditScore">
          <el-input-number 
            v-model="formData.creditScore" 
            :min="0" 
            :max="100" 
            :step="1"
            placeholder="请输入信用评分"
          />
        </el-form-item>
        <el-form-item label="信用等级" prop="creditLevel">
          <el-select v-model="formData.creditLevel" placeholder="请选择信用等级">
            <el-option label="AAA" value="AAA" />
            <el-option label="AA" value="AA" />
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
            <el-option label="D" value="D" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级" prop="riskLevel">
          <el-select v-model="formData.riskLevel" placeholder="请选择风险等级">
            <el-option label="低风险" value="LOW" />
            <el-option label="中风险" value="MEDIUM" />
            <el-option label="高风险" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item label="评估日期" prop="evaluationDate">
          <el-date-picker
            v-model="formData.evaluationDate"
            type="date"
            placeholder="请选择评估日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="下次评估日期" prop="nextEvaluationDate">
          <el-date-picker
            v-model="formData.nextEvaluationDate"
            type="date"
            placeholder="请选择下次评估日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="评估说明" prop="evaluationComment">
          <el-input
            v-model="formData.evaluationComment"
            type="textarea"
            :rows="4"
            placeholder="请输入评估说明"
          />
        </el-form-item>
        <el-form-item label="风险描述" prop="riskDescription">
          <el-input
            v-model="formData.riskDescription"
            type="textarea"
            :rows="4"
            placeholder="请输入风险描述"
          />
        </el-form-item>
        <el-form-item label="改进建议" prop="improvementSuggestion">
          <el-input
            v-model="formData.improvementSuggestion"
            type="textarea"
            :rows="4"
            placeholder="请输入改进建议"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SubModuleHeader from '../../../components/common/SubModuleHeader.vue'
import { supplierCreditApi, supplierApi } from '../../../api/srm'

// 搜索表单
const searchForm = reactive({
  supplierName: '',
  creditLevel: '',
  riskLevel: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载状态
const loading = ref(false)

// 信用列表
const creditList = ref<any[]>([])

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增信用记录')
const formRef = ref()
const formData = reactive({
  id: undefined,
  supplierName: '',
  creditScore: 80,
  creditLevel: 'AA',
  riskLevel: 'LOW',
  evaluationDate: '',
  nextEvaluationDate: '',
  evaluationComment: '',
  riskDescription: '',
  improvementSuggestion: ''
})

// 表单验证规则
const rules = reactive({
  supplierName: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  creditScore: [{ required: true, message: '请输入信用评分', trigger: 'blur' }],
  creditLevel: [{ required: true, message: '请选择信用等级', trigger: 'change' }],
  riskLevel: [{ required: true, message: '请选择风险等级', trigger: 'change' }],
  evaluationDate: [{ required: true, message: '请选择评估日期', trigger: 'change' }],
  nextEvaluationDate: [{ required: true, message: '请选择下次评估日期', trigger: 'change' }]
})

// 页面加载时获取数据
onMounted(() => {
  fetchCreditList()
  fetchSuppliers()
})

// 获取信用列表
const fetchCreditList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      supplierName: searchForm.supplierName,
      creditLevel: searchForm.creditLevel,
      riskLevel: searchForm.riskLevel
    }
    const res = await supplierCreditApi.getSupplierCreditList(params)
    creditList.value = (res.data.list || res.data.records || [])
    pagination.total = (res.data.total || 0)
  } catch (error) {
    console.error(error)
    ElMessage.error('获取信用列表失败')
  } finally {
    loading.value = false
  }
}

// 供应商选项
const supplierOptions = ref<any[]>([])
const fetchSuppliers = async () => {
  try {
    const res = await supplierApi.getSupplierList({ page: 0, size: 1000 })
    supplierOptions.value = res.data.list
  } catch (error) {
    console.error(error)
  }
}

// 根据评分获取进度条颜色
const getScoreColor = (score: number) => {
  if (score >= 90) return '#67c23a'
  if (score >= 80) return '#e6a23c'
  if (score >= 70) return '#f56c6c'
  return '#909399'
}

// 根据信用等级获取标签类型
const getCreditLevelType = (level: string) => {
  switch (level) {
    case 'AAA':
    case 'AA':
      return 'success'
    case 'A':
      return 'warning'
    default:
      return 'danger'
  }
}

// 根据风险等级获取标签类型
const getRiskLevelType = (level: string) => {
  switch (level) {
    case 'LOW':
      return 'success'
    case 'MEDIUM':
      return 'warning'
    case 'HIGH':
      return 'danger'
    default:
      return 'info'
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchCreditList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    supplierName: '',
    creditLevel: '',
    riskLevel: ''
  })
  pagination.currentPage = 1
  fetchCreditList()
}

// 新增
const handleCreate = () => {
  dialogTitle.value = '新增信用记录'
  Object.assign(formData, {
    id: undefined,
    supplierName: '',
    creditScore: 80,
    creditLevel: 'AA',
    riskLevel: 'LOW',
    evaluationDate: '',
    nextEvaluationDate: '',
    evaluationComment: '',
    riskDescription: '',
    improvementSuggestion: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑信用记录'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 查看
const handleView = (row: any) => {
  dialogTitle.value = '查看信用记录'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleSupplierChange = (val: string) => {
  const supplier = supplierOptions.value.find(item => item.supplierName === val)
  if (supplier) {
    formData.supplierName = supplier.supplierName
  }
}

const toYmd = (value: any) => {
  if (!value) return value
  if (value instanceof Date) return value.toISOString().slice(0, 10)
  if (typeof value === 'string') return value.slice(0, 10)
  return value
}

// 重新评估
const handleEvaluate = (row: any) => {
  ElMessageBox.confirm('确定要重新评估该供应商的信用吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await supplierCreditApi.calculateSupplierCredit(row.supplierId)
      ElMessage.success('重新评估成功')
      fetchCreditList()
    } catch (error) {
      ElMessage.error('重新评估失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    // 查找供应商ID
    const supplier = supplierOptions.value.find(item => item.supplierName === formData.supplierName)
    if (!supplier) {
      ElMessage.error('请选择有效的供应商')
      return
    }
    
    const submitData = {
      ...formData,
      supplierId: supplier.id,
      evaluationDate: toYmd(formData.evaluationDate),
      nextEvaluationDate: toYmd(formData.nextEvaluationDate)
    }
    
    await supplierCreditApi.updateSupplierCredit(submitData)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchCreditList()
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败')
  }
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchCreditList()
}

// 当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchCreditList()
}
</script>

<style scoped>
.supplier-credit-view {
  padding: 16px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.content-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.search-area {
  margin-bottom: 16px;
}

.table-area {
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.dialog-footer {
  text-align: right;
}
</style>
