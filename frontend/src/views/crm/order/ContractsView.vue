<template>
  <div class="contracts-view">
    <!-- 页面操作栏 -->
    <div class="page-actions">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索合同编号、合同名称"
        style="width: 300px; margin-right: 10px;"
        clearable
      >
        <template #append>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon> 新建合同
      </el-button>
      <el-button @click="handleExport">
        <el-icon><Download /></el-icon> 导出
      </el-button>
    </div>

    <!-- 搜索条件展开面板 -->
    <el-collapse v-model="activeSearchPanel" style="margin-bottom: 20px;">
      <el-collapse-item title="高级搜索" name="1">
        <div class="advanced-search">
          <el-form :model="searchForm" inline>
            <el-form-item label="合同状态">
              <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
                <el-option label="草稿" value="DRAFT" />
                <el-option label="已签订" value="SIGNED" />
                <el-option label="生效中" value="ACTIVE" />
                <el-option label="已到期" value="EXPIRED" />
                <el-option label="已终止" value="TERMINATED" />
              </el-select>
            </el-form-item>
            <el-form-item label="签订日期">
              <el-date-picker
                v-model="searchForm.signDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-item>
    </el-collapse>

    <!-- 合同列表 -->
    <el-card shadow="never" class="contracts-table-card">
      <el-table
        v-loading="loading"
        :data="contractsList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="contractNo" label="合同编号" min-width="150" sortable />
        <el-table-column prop="contractName" label="合同名称" min-width="200" />
        <el-table-column prop="customerName" label="客户名称" min-width="180" />
        <el-table-column prop="amount" label="合同金额" width="140" align="right" sortable>
          <template #default="scope">
            {{ formatCurrency(scope.row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signDate" label="签订日期" width="140" sortable />
        <el-table-column prop="startDate" label="开始日期" width="140" sortable />
        <el-table-column prop="endDate" label="结束日期" width="140" sortable />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
            <el-button
              v-if="scope.row.status === 'DRAFT'"
              size="small"
              type="success"
              @click="handleSign(scope.row)"
            >
              签署
            </el-button>
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

    <!-- 新建/编辑合同对话框 -->
    <el-dialog
      v-model="contractDialogVisible"
      :title="isEditMode ? '编辑合同' : '新建合同'"
      width="600px"
    >
      <el-form :model="contractForm" :rules="contractFormRules" ref="contractFormRef" label-width="100px">
        <el-form-item v-if="isEditMode" label="合同编号">
          <el-input v-model="contractForm.contractNo" disabled />
        </el-form-item>
        <el-form-item label="合同名称" prop="contractName">
          <el-input v-model="contractForm.contractName" placeholder="请输入合同名称" />
        </el-form-item>
        <el-form-item label="所属客户" prop="customerId">
          <el-select
            v-model="contractForm.customerId"
            placeholder="请选择客户"
            filterable
            style="width: 100%"
            @change="handleCustomerChange"
          >
            <el-option
              v-for="customer in customersList"
              :key="customer.id"
              :label="customer.customerName"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="合同金额" prop="amount">
          <el-input-number v-model="contractForm.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="签订日期" prop="signDate">
          <el-date-picker
            v-model="contractForm.signDate"
            type="date"
            placeholder="请选择签订日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="contractForm.startDate"
            type="date"
            placeholder="请选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="contractForm.endDate"
            type="date"
            placeholder="请选择结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="contractForm.ownerName" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="contractForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="contractDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSaveContract">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看合同详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="合同详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="合同编号">{{ contractDetail.contractNo }}</el-descriptions-item>
        <el-descriptions-item label="合同名称">{{ contractDetail.contractName }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ contractDetail.customerName }}</el-descriptions-item>
        <el-descriptions-item label="合同金额">{{ formatCurrency(contractDetail.amount) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(contractDetail.status)">
            {{ getStatusLabel(contractDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="负责人">{{ contractDetail.ownerName }}</el-descriptions-item>
        <el-descriptions-item label="签订日期">{{ contractDetail.signDate }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ contractDetail.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ contractDetail.endDate }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ contractDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ contractDetail.remark }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '../../../api/crm/order'
import { customerApi } from '../../../api/crm/customer'
import { unwrapPageResponse, unwrapResponseData } from '../../../api'

// 合同列表数据
const contractsList = ref<any[]>([])

// 加载状态
const loading = ref(false)

// 提交加载状态
const submitLoading = ref(false)

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  signDateRange: [] as string[]
})

// 搜索面板展开状态
const activeSearchPanel = ref<string[]>([])

// 客户下拉列表
const customersList = ref<any[]>([])

// 新建/编辑对话框状态
const contractDialogVisible = ref(false)
const isEditMode = ref(false)
const contractFormRef = ref()

// 合同表单数据
const contractForm = reactive<any>({
  id: undefined,
  contractNo: '',
  contractName: '',
  customerId: undefined,
  customerName: '',
  amount: 0,
  status: '',
  signDate: '',
  startDate: '',
  endDate: '',
  ownerName: '',
  remark: ''
})

// 合同表单验证规则
const contractFormRules = reactive({
  contractName: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  amount: [{ required: true, message: '请输入合同金额', trigger: 'blur' }]
})

// 查看详情对话框状态
const detailDialogVisible = ref(false)
const contractDetail = ref<any>({})

/**
 * 格式化货币金额
 * @param amount 金额数值
 * @returns 格式化后的货币字符串
 */
const formatCurrency = (amount: number) => {
  if (amount === null || amount === undefined) return '-'
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)
}

/**
 * 获取合同状态对应的标签类型
 * @param status 合同状态（DRAFT/SIGNED/ACTIVE/EXPIRED/TERMINATED）
 * @returns el-tag 类型
 */
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    DRAFT: 'warning',
    SIGNED: 'success',
    ACTIVE: 'primary',
    EXPIRED: 'info',
    TERMINATED: 'danger'
  }
  return typeMap[status] || ''
}

/**
 * 获取合同状态对应的中文标签
 * @param status 合同状态（DRAFT/SIGNED/ACTIVE/EXPIRED/TERMINATED）
 * @returns 状态中文名称
 */
const getStatusLabel = (status: string) => {
  const labelMap: Record<string, string> = {
    DRAFT: '草稿',
    SIGNED: '已签订',
    ACTIVE: '生效中',
    EXPIRED: '已到期',
    TERMINATED: '已终止'
  }
  return labelMap[status] || status
}

/**
 * 加载合同列表数据
 * 调用后端分页接口，关键字与签订日期范围在前端对当前页数据过滤
 */
const fetchContractsList = async () => {
  loading.value = true
  try {
    const response = await orderApi.getContractsList({
      page: pagination.currentPage,
      size: pagination.pageSize,
      status: searchForm.status || undefined
    })
    const { list, total } = unwrapPageResponse(response)
    let filteredList = list
    // 后端列表接口不支持关键字查询，对当前页数据按合同编号/名称前端过滤
    if (searchForm.keyword) {
      const keyword = searchForm.keyword.toLowerCase()
      filteredList = filteredList.filter((item: any) =>
        (item.contractNo || '').toLowerCase().includes(keyword) ||
        (item.contractName || '').toLowerCase().includes(keyword)
      )
    }
    // 后端列表接口不支持签订日期范围查询，对当前页数据前端过滤
    if (searchForm.signDateRange && searchForm.signDateRange.length === 2) {
      const [start, end] = searchForm.signDateRange
      if (start && end) {
        filteredList = filteredList.filter((item: any) => {
          if (!item.signDate) return false
          return item.signDate >= start && item.signDate <= end
        })
      }
    }
    contractsList.value = filteredList
    pagination.total = total
  } catch (error) {
    console.error('获取合同列表失败:', error)
    ElMessage.error('获取合同列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载客户下拉列表（用于新建/编辑合同选择客户）
 */
const fetchCustomersList = async () => {
  try {
    const response = await customerApi.getCustomerList({ page: 1, size: 1000 })
    const { list } = unwrapPageResponse(response)
    customersList.value = list
  } catch (error) {
    console.error('获取客户列表失败:', error)
  }
}

/**
 * 处理搜索，重置页码并重新加载列表
 */
const handleSearch = () => {
  pagination.currentPage = 1
  fetchContractsList()
}

/**
 * 处理重置，清空搜索条件并重新加载列表
 */
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    status: '',
    signDateRange: []
  })
  pagination.currentPage = 1
  fetchContractsList()
}

/**
 * 处理新建合同，重置表单并打开对话框
 */
const handleCreate = () => {
  isEditMode.value = false
  Object.assign(contractForm, {
    id: undefined,
    contractNo: '',
    contractName: '',
    customerId: undefined,
    customerName: '',
    amount: 0,
    status: '',
    signDate: '',
    startDate: '',
    endDate: '',
    ownerName: '',
    remark: ''
  })
  contractDialogVisible.value = true
}

/**
 * 处理查看合同详情
 * @param row 合同行数据
 */
const handleView = async (row: any) => {
  try {
    const response = await orderApi.getContractDetail(row.id)
    contractDetail.value = unwrapResponseData(response) || row
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取合同详情失败:', error)
    ElMessage.error('获取合同详情失败')
  }
}

/**
 * 处理编辑合同，回填表单并打开对话框
 * @param row 合同行数据
 */
const handleEdit = (row: any) => {
  isEditMode.value = true
  Object.assign(contractForm, {
    id: row.id,
    contractNo: row.contractNo,
    contractName: row.contractName,
    customerId: row.customerId,
    customerName: row.customerName,
    amount: row.amount,
    status: row.status,
    signDate: row.signDate,
    startDate: row.startDate,
    endDate: row.endDate,
    ownerName: row.ownerName,
    remark: row.remark
  })
  contractDialogVisible.value = true
}

/**
 * 客户选择变化时同步客户名称
 * @param customerId 选中的客户ID
 */
const handleCustomerChange = (customerId: number) => {
  const customer = customersList.value.find(c => c.id === customerId)
  contractForm.customerName = customer ? customer.customerName : ''
}

/**
 * 保存合同（新建或编辑）
 * 新建调用创建接口，编辑调用更新接口
 */
const handleSaveContract = () => {
  contractFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEditMode.value) {
        await orderApi.updateContract(contractForm.id, { ...contractForm })
        ElMessage.success('更新成功')
      } else {
        // 新建时不传id与状态，后端自动生成合同编号并默认草稿状态
        const createData = {
          contractNo: contractForm.contractNo,
          contractName: contractForm.contractName,
          customerId: contractForm.customerId,
          customerName: contractForm.customerName,
          amount: contractForm.amount,
          signDate: contractForm.signDate,
          startDate: contractForm.startDate,
          endDate: contractForm.endDate,
          ownerName: contractForm.ownerName,
          remark: contractForm.remark
        }
        await orderApi.createContract(createData)
        ElMessage.success('创建成功')
      }
      contractDialogVisible.value = false
      fetchContractsList()
    } catch (error) {
      console.error('保存合同失败:', error)
      ElMessage.error('保存合同失败')
    } finally {
      submitLoading.value = false
    }
  })
}

/**
 * 处理删除合同（后端暂未提供删除接口，确认后提示）
 * @param row 合同行数据
 */
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除合同「${row.contractName}」吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.info('后端暂未提供合同删除接口')
  }).catch(() => {
    // 取消删除
  })
}

/**
 * 处理签署合同（仅草稿状态可签署）
 * @param row 合同行数据
 */
const handleSign = (row: any) => {
  ElMessageBox.confirm(`确定要签署合同「${row.contractName}」吗？签署后状态将变为已签订。`, '签署确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await orderApi.signContract(row.id)
      ElMessage.success('签署成功')
      fetchContractsList()
    } catch (error) {
      console.error('签署合同失败:', error)
      ElMessage.error('签署合同失败')
    }
  }).catch(() => {
    // 取消签署
  })
}

/**
 * 处理导出合同（导出功能暂未实现）
 */
const handleExport = () => {
  ElMessage.info('导出合同功能开发中')
}

/**
 * 处理分页大小变化
 * @param size 每页大小
 */
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchContractsList()
}

/**
 * 处理当前页码变化
 * @param current 当前页码
 */
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchContractsList()
}

/**
 * 处理表格选择变化
 * @param selection 选中的行数据
 */
const handleSelectionChange = (selection: any[]) => {
  console.log('选中的行:', selection)
}

// 组件挂载时初始化数据
onMounted(() => {
  fetchContractsList()
  fetchCustomersList()
})
</script>

<style scoped>
.contracts-view {
  padding: 20px;
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.page-actions {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.advanced-search {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
}

.contracts-table-card {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
