<template>
  <div class="supplier-blacklist-view">
    <SubModuleHeader title="供应商黑白名单管理" parentTitle="供应商准入管理" parentPath="/home/srm/supplier" />
    <div class="content-header">
      <div class="action-buttons">
        <el-button type="primary" @click="handleBatchRemove">批量移出黑名单</el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-card shadow="never">
        <el-form :model="searchForm" layout="inline" size="small">
          <el-form-item label="供应商编码">
            <el-input v-model="searchForm.supplierCode" placeholder="请输入供应商编码" />
          </el-form-item>
          <el-form-item label="供应商名称">
            <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    
    <!-- 黑名单列表区域 -->
    <div class="table-area">
      <el-card shadow="never">
        <el-table v-loading="loading" :data="blacklist" style="width: 100%" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="supplierCode" label="供应商编码" width="180" />
          <el-table-column prop="supplierName" label="供应商名称" width="220" />
          <el-table-column prop="type" label="供应商阶段" width="150" />
          <el-table-column prop="status" label="状态" width="150" />
          <el-table-column prop="updatedTime" label="更新时间" width="180" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="scope">
              <el-button size="small" type="success" @click="handleRemove(scope.row)">移出黑名单</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SubModuleHeader from '../../../components/common/SubModuleHeader.vue'
import { supplierApi } from '../../../api/srm'

// 搜索表单
const searchForm = reactive({
  supplierCode: '',
  supplierName: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载状态
const loading = ref(false)

// 黑名单列表
const blacklist = ref<any[]>([])

// 选中项
const selection = ref<any[]>([])

// 页面加载时获取数据
onMounted(() => {
  fetchBlacklist()
})

// 获取黑名单列表
const fetchBlacklist = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize
    }
    const res = await supplierApi.getSupplierList(params)
    const list = res.data.list || []
    const filtered = list
      .filter((s: any) => (s.type === 'BLACKLISTED' || s.status === 'BLACKLISTED' || s.status === 'BLACKLIST'))
      .filter((s: any) => !searchForm.supplierCode || (s.supplierCode || '').includes(searchForm.supplierCode))
      .filter((s: any) => !searchForm.supplierName || (s.supplierName || '').includes(searchForm.supplierName))
    blacklist.value = filtered.map((s: any) => ({
      id: s.id,
      supplierCode: s.supplierCode,
      supplierName: s.supplierName,
      type: s.type,
      status: s.status,
      updatedTime: s.updatedTime || ''
    }))
    pagination.total = filtered.length
  } catch (error) {
    ElMessage.error('获取黑名单列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchBlacklist()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    supplierCode: '',
    supplierName: ''
  })
  pagination.currentPage = 1
  fetchBlacklist()
}

// 选择变化
const handleSelectionChange = (val: any[]) => {
  selection.value = val
}

// 移出黑名单
const handleRemove = (row: any) => {
  ElMessageBox.confirm('确定要移出该供应商到黑名单吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await supplierApi.removeFromBlacklist(row.id)
      ElMessage.success('移出黑名单成功')
      fetchBlacklist()
    } catch (error) {
      ElMessage.error('移出黑名单失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 批量移出黑名单
const handleBatchRemove = () => {
  if (selection.value.length === 0) {
    ElMessage.warning('请选择要移出黑名单的供应商')
    return
  }
  ElMessageBox.confirm('确定要批量移出选中的供应商到黑名单吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await Promise.all(selection.value.map((s: any) => supplierApi.removeFromBlacklist(s.id)))
      ElMessage.success('批量移出黑名单成功')
      fetchBlacklist()
      selection.value = []
    } catch (error) {
      ElMessage.error('批量移出黑名单失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchBlacklist()
}

// 当前页码变化
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchBlacklist()
}
</script>

<style scoped>
.supplier-blacklist-view {
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
</style>
