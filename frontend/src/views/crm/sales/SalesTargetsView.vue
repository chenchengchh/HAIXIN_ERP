<template>
  <div class="sales-targets-view">
    <div class="page-header">
      <h3>销售目标管理</h3>
    </div>

    <!-- 筛选和控制区域 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="目标周期">
          <el-input v-model="filterForm.period" placeholder="输入目标周期（如 2026-Q1）" clearable />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="filterForm.ownerName" placeholder="输入负责人姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">
            <el-icon><Search /></el-icon> 筛选
          </el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon> 新建目标
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 销售目标数据卡片 -->
    <el-card shadow="hover" class="targets-card">
      <template #header>
        <div class="card-header">
          <span>销售目标数据</span>
        </div>
      </template>
      <div class="targets-content">
        <!-- 销售目标数据表格 -->
        <el-table
          v-loading="loading"
          :data="targetsData"
          style="width: 100%;"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="period" label="目标周期" width="140" />
          <el-table-column prop="ownerName" label="负责人" min-width="120" />
          <el-table-column prop="targetAmount" label="目标金额" align="right" min-width="140">
            <template #default="scope">
              {{ formatCurrency(scope.row.targetAmount) }}
            </template>
          </el-table-column>
          <el-table-column prop="achievedAmount" label="完成金额" align="right" min-width="140">
            <template #default="scope">
              {{ formatCurrency(scope.row.achievedAmount) }}
            </template>
          </el-table-column>
          <el-table-column prop="achievementRate" label="完成率" align="right" width="180">
            <template #default="scope">
              <div class="achievement-rate">
                <el-progress
                  :percentage="calcAchievementRate(scope.row)"
                  :stroke-width="10"
                  :color="getProgressColor(calcAchievementRate(scope.row))"
                ></el-progress>
                <span class="rate-text">{{ calcAchievementRate(scope.row) }}%</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170">
            <template #default="scope">
              {{ formatTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="handleView(scope.row)">查看</el-button>
              <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
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
      </div>
    </el-card>

    <!-- 销售目标详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="销售目标详情" width="520px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ currentTarget?.id }}</el-descriptions-item>
        <el-descriptions-item label="目标周期">{{ currentTarget?.period }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ currentTarget?.ownerName }}</el-descriptions-item>
        <el-descriptions-item label="目标金额">{{ formatCurrency(currentTarget?.targetAmount) }}</el-descriptions-item>
        <el-descriptions-item label="完成金额">{{ formatCurrency(currentTarget?.achievedAmount) }}</el-descriptions-item>
        <el-descriptions-item label="完成率">{{ calcAchievementRate(currentTarget) }}%</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentTarget?.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(currentTarget?.updateTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { salesApi } from '../../../api/crm/sales'
import { unwrapPageResponse } from '../../../api'

// 筛选表单（后端支持 ownerName 参数，period 用于前端过滤展示）
const filterForm = ref({
  period: '',
  ownerName: ''
})

// 销售目标数据
const targetsData = ref<any[]>([])
const loading = ref(false)

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 详情对话框显示状态
const detailDialogVisible = ref(false)

// 当前查看的销售目标
const currentTarget = ref<any>(null)

// 选中的销售目标
const selectedTargets = ref<any[]>([])

// 初始化数据
onMounted(() => {
  fetchTargetsData()
})

/**
 * 获取销售目标数据（对接后端分页接口 GET /api/v1/crm/targets/my）
 */
const fetchTargetsData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    // 后端仅支持 ownerName 查询参数
    if (filterForm.value.ownerName) {
      params.ownerName = filterForm.value.ownerName
    }
    const response = await salesApi.getSalesTargets(params)
    const { list, total } = unwrapPageResponse<any>(response)
    // 后端不支持周期筛选时，前端按周期关键字过滤当前结果
    const periodKeyword = filterForm.value.period?.trim()
    targetsData.value = periodKeyword
      ? list.filter(item => String(item.period || '').includes(periodKeyword))
      : list
    pagination.total = total
  } catch (error) {
    console.error('获取销售目标失败:', error)
    ElMessage.error('获取销售目标失败')
    targetsData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

/**
 * 格式化货币显示
 * @param amount 金额
 */
const formatCurrency = (amount: number | undefined | null) => {
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(Number(amount || 0))
}

/**
 * 格式化时间显示
 * @param time 后端返回的时间字符串
 */
const formatTime = (time: string | undefined | null) => {
  if (!time) return '-'
  return String(time).replace('T', ' ').substring(0, 19)
}

/**
 * 计算目标达成率（完成金额/目标金额*100，目标金额为0时返回0，保留1位小数）
 * @param row 销售目标行数据
 */
const calcAchievementRate = (row: any): number => {
  const targetAmount = Number(row?.targetAmount || 0)
  const achievedAmount = Number(row?.achievedAmount || 0)
  // 避免目标金额为0时出现除零错误
  if (!targetAmount || targetAmount <= 0) return 0
  return Math.round((achievedAmount / targetAmount) * 1000) / 10
}

/**
 * 获取进度条颜色
 * @param value 达成率数值
 */
const getProgressColor = (value: number): string => {
  if (value >= 100) return '#67C23A' // 完成
  if (value >= 80) return '#67C23A' // 优秀
  if (value >= 50) return '#E6A23C' // 良好
  return '#F56C6C' // 落后
}

/**
 * 处理筛选：重置页码并重新加载数据
 */
const handleFilter = () => {
  pagination.currentPage = 1
  fetchTargetsData()
}

/**
 * 处理重置：清空筛选条件并重新加载数据
 */
const handleReset = () => {
  filterForm.value = {
    period: '',
    ownerName: ''
  }
  pagination.currentPage = 1
  fetchTargetsData()
}

/**
 * 处理分页大小变化
 * @param size 每页条数
 */
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchTargetsData()
}

/**
 * 处理页码变化
 * @param current 页码
 */
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchTargetsData()
}

/**
 * 处理选择变化
 * @param selection 选中行数组
 */
const handleSelectionChange = (selection: any[]) => {
  selectedTargets.value = selection
}

/**
 * 处理查看：打开详情对话框展示目标数据
 * @param target 销售目标行数据
 */
const handleView = (target: any) => {
  currentTarget.value = target
  detailDialogVisible.value = true
}

/**
 * 处理编辑：后端暂未提供目标编辑接口，保留提示
 * @param _target 销售目标行数据
 */
const handleEdit = (_target: any) => {
  ElMessage.info('后端暂未提供销售目标编辑接口')
}

/**
 * 处理删除：后端暂未提供目标删除接口，确认后提示
 * @param target 销售目标行数据
 */
const handleDelete = (target: any) => {
  ElMessageBox.confirm('确定要删除该销售目标吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 后端暂未提供销售目标删除接口
    ElMessage.info('后端暂未提供销售目标删除接口')
  }).catch(() => {
    // 取消删除
  })
}

/**
 * 处理新建：后端暂未提供目标创建接口，保留提示
 */
const handleCreate = () => {
  ElMessage.info('后端暂未提供销售目标创建接口')
}
</script>

<style scoped>
.sales-targets-view {
  padding: 10px;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.targets-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1rem;
}

.targets-content {
  padding: 10px 0;
}

.achievement-rate {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rate-text {
  font-weight: bold;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
