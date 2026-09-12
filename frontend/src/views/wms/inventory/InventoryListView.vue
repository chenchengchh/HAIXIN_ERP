<template>
  <div class="inventory-list-view">
    <div class="page-header">
      <h2>库存查询</h2>
    </div>

    <!-- 搜索栏 -->
    <div class="search-section">
      <!-- AI 安全库存建议（S03，低库存自动预警，空态自动隐藏） -->
      <ModuleAiSuggestionCard
        suggestion-type="SAFETY_STOCK"
        title="AI 安全库存建议"
        :max-display="5"
      />
      <el-card shadow="hover">
        <el-form :model="searchForm" label-width="80px" inline>
          <el-form-item label="物料编码">
            <el-input v-model="searchForm.materialCode" placeholder="请输入物料编码" clearable />
          </el-form-item>
          <el-form-item label="仓库编码">
            <el-input v-model="searchForm.warehouseCode" placeholder="请输入仓库编码" clearable />
          </el-form-item>
          <el-form-item label="批次号">
            <el-input v-model="searchForm.batchNo" placeholder="请输入批次号" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 列表区域 -->
    <div class="list-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>库存列表</span>
            <el-button type="success" @click="handleExport">导出</el-button>
          </div>
        </template>
        
        <el-table :data="inventoryList" v-loading="loading" stripe style="width: 100%">
          <el-table-column prop="materialCode" label="物料编码" min-width="120" />
          <el-table-column prop="materialName" label="物料名称" min-width="150" />
          <el-table-column prop="warehouseCode" label="仓库编码" width="120" />
          <el-table-column prop="locationCode" label="库位" width="120" />
          <el-table-column prop="batchNo" label="批次号" width="150" />
          <el-table-column prop="quantity" label="库存数量" width="120">
            <template #default="scope">
              <span style="font-weight: bold">{{ scope.row.quantity }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'FROZEN' ? 'danger' : 'success'">
                {{ scope.row.status === 'FROZEN' ? '冻结' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="updatedTime" label="更新时间" width="180" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
              <el-button link type="primary" @click="handleCheck(scope.row)">盘点</el-button>
              <el-button link type="warning" @click="handleAdjust(scope.row)">调整</el-button>
              <el-button v-if="scope.row.status !== 'FROZEN'" link type="danger" @click="handleFreeze(scope.row)">冻结</el-button>
              <el-button v-else link type="success" @click="handleUnfreeze(scope.row)">解冻</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            layout="total, prev, pager, next"
            @current-change="handleSearch"
          />
        </div>
      </el-card>
    </div>

    <!-- 盘点对话框 -->
    <el-dialog v-model="checkDialogVisible" title="库存盘点" width="400px">
      <el-form :model="checkForm" label-width="100px">
        <el-form-item label="当前库存">
          <span>{{ checkForm.currentQuantity }}</span>
        </el-form-item>
        <el-form-item label="实际库存">
          <el-input-number v-model="checkForm.actualQuantity" :min="0" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCheck">确定</el-button>
      </template>
    </el-dialog>

    <!-- 调整对话框 -->
    <el-dialog v-model="adjustDialogVisible" title="库存调整" width="400px">
      <el-form :model="adjustForm" label-width="100px">
        <el-form-item label="当前库存">
          <span>{{ adjustForm.currentQuantity }}</span>
        </el-form-item>
        <el-form-item label="调整后数量">
          <el-input-number v-model="adjustForm.newQuantity" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input v-model="adjustForm.reason" placeholder="请输入调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdjust">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { inventoryApi } from '../../../api/wms'
import ModuleAiSuggestionCard from '../../../components/ai/ModuleAiSuggestionCard.vue'

const loading = ref(false)
const inventoryList = ref<any[]>([])
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const searchForm = reactive({
  materialCode: '',
  warehouseCode: '',
  batchNo: ''
})

const checkDialogVisible = ref(false)
const checkForm = reactive({
  id: 0,
  currentQuantity: 0,
  actualQuantity: 0
})

const adjustDialogVisible = ref(false)
const adjustForm = reactive({
  id: 0,
  currentQuantity: 0,
  newQuantity: 0,
  reason: ''
})

/**
 * 查询库存列表（后端返回 PageResult 格式：list/total）
 */
const fetchInventory = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      ...searchForm
    }
    const res = await inventoryApi.getList(params)
    const pageData = res.data || {}
    inventoryList.value = pageData.list || []
    pagination.total = Number(pageData.total ?? 0)
  } catch (error) {
    console.error(error)
    ElMessage.error('获取库存列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchInventory()
}

const handleReset = () => {
  searchForm.materialCode = ''
  searchForm.warehouseCode = ''
  searchForm.batchNo = ''
  handleSearch()
}

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

/**
 * 打开盘点对话框
 */
const handleCheck = (row: any) => {
  checkForm.id = row.id
  checkForm.currentQuantity = row.quantity
  checkForm.actualQuantity = row.quantity
  checkDialogVisible.value = true
}

/**
 * 提交盘点结果
 */
const submitCheck = async () => {
  try {
    await inventoryApi.check(checkForm.id, checkForm.actualQuantity)
    ElMessage.success('盘点成功')
    checkDialogVisible.value = false
    fetchInventory()
  } catch (error) {
    ElMessage.error('盘点失败')
  }
}

/**
 * 打开库存调整对话框
 */
const handleAdjust = (row: any) => {
  adjustForm.id = row.id
  adjustForm.currentQuantity = row.quantity
  adjustForm.newQuantity = row.quantity
  adjustForm.reason = ''
  adjustDialogVisible.value = true
}

/**
 * 提交库存调整
 */
const submitAdjust = async () => {
  try {
    await inventoryApi.adjust(adjustForm.id, { newQuantity: adjustForm.newQuantity, reason: adjustForm.reason })
    ElMessage.success('库存调整成功')
    adjustDialogVisible.value = false
    fetchInventory()
  } catch (error) {
    ElMessage.error('库存调整失败')
  }
}

/**
 * 冻结库存（需填写冻结原因）
 */
const handleFreeze = async (row: any) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入冻结原因', '冻结库存', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '如：质检待判定'
    })
    await inventoryApi.freeze(row.id, { reason: value || '' })
    ElMessage.success('冻结成功')
    fetchInventory()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('冻结失败')
    }
  }
}

/**
 * 解冻库存
 */
const handleUnfreeze = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要解冻该库存记录吗？', '解冻库存', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await inventoryApi.unfreeze(row.id)
    ElMessage.success('解冻成功')
    fetchInventory()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('解冻失败')
    }
  }
}

onMounted(() => {
  fetchInventory()
})
</script>

<style scoped lang="scss">
.inventory-list-view {
  padding: 24px;
  
  .page-header {
    margin-bottom: 24px;
    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 500;
    }
  }

  .search-section {
    margin-bottom: 24px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
