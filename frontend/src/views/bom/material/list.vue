<template>
  <div class="material-list">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h3>物料管理</h3>
      <div class="header-actions">
        <el-tooltip :content="authorityHint" :disabled="materialWriteEnabled" placement="top">
          <span>
            <el-button type="primary" @click="handleAdd" :disabled="!materialWriteEnabled">
              <el-icon><Plus /></el-icon>
              新增物料
            </el-button>
          </span>
        </el-tooltip>
        <el-tooltip :content="authorityHint" :disabled="materialWriteEnabled" placement="top">
          <span>
            <el-button type="warning" @click="handleBatchEnable" :disabled="!materialWriteEnabled || selectedIds.length === 0">
              <el-icon><Check /></el-icon>
              批量启用 ({{ selectedIds.length }})
            </el-button>
          </span>
        </el-tooltip>
        <el-tooltip :content="authorityHint" :disabled="materialWriteEnabled" placement="top">
          <span>
            <el-button type="info" @click="handleBatchDisable" :disabled="!materialWriteEnabled || selectedIds.length === 0">
              <el-icon><CircleClose /></el-icon>
              批量停用 ({{ selectedIds.length }})
            </el-button>
          </span>
        </el-tooltip>
        <el-tooltip :content="authorityHint" :disabled="materialWriteEnabled" placement="top">
          <span>
            <el-button type="danger" @click="handleBatchDelete" :disabled="!materialWriteEnabled || selectedIds.length === 0">
              <el-icon><Delete /></el-icon>
              批量删除 ({{ selectedIds.length }})
            </el-button>
          </span>
        </el-tooltip>
        <el-tooltip :content="authorityHint" :disabled="materialWriteEnabled" placement="top">
          <span>
            <el-button @click="handleImport" :disabled="!materialWriteEnabled">
              <el-icon><Upload /></el-icon>
              导入物料
            </el-button>
          </span>
        </el-tooltip>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出物料
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <el-card class="search-card card-glossy hover-lift">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="物料编码">
              <el-input v-model="searchForm.code" placeholder="编码模糊搜索" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="物料名称">
              <el-input v-model="searchForm.name" placeholder="名称模糊搜索" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="规格型号">
              <el-input v-model="searchForm.spec" placeholder="规格模糊搜索" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="物料状态">
              <el-select v-model="searchForm.status" placeholder="状态筛选" clearable style="width: 100%">
                <el-option label="启用" value="ACTIVE" />
                <el-option label="停用" value="INACTIVE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="form-actions">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 物料列表 -->
    <el-card class="list-card card-glossy">
      <el-table 
        :data="materials" 
        style="width: 100%" 
        :header-cell-style="{ background: 'rgba(242, 243, 245, 0.5)', color: 'var(--text-primary)', fontWeight: '600' }"
        row-class-name="hover-row-effect"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="materialCode" label="编码" min-width="120">
          <template #default="scope">
            <span class="code-text">{{ scope.row.materialCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="materialName" label="名称" min-width="150" />
        <el-table-column prop="materialSpec" label="规格型号" min-width="180">
          <template #default="scope">
            {{ scope.row.materialSpec || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag 
              effect="dark"
              :type="scope.row.status === 'ACTIVE' || scope.row.status === '1' ? 'success' : 'info'"
              class="status-tag"
            >
              {{ scope.row.status === 'ACTIVE' || scope.row.status === '1' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="录入日期" width="160">
          <template #default="scope">
            {{ scope.row.createdTime?.substring(0, 10) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleView(scope.row.id)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button type="warning" size="small" @click="handleEdit(scope.row.id)" :disabled="!materialWriteEnabled">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)" :disabled="!materialWriteEnabled">
              <el-icon><Delete /></el-icon>
              删除
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

    <!-- 导入物料对话框 -->
    <el-dialog
      v-model="importVisible"
      title="导入物料"
      width="500px"
      @close="handleImportClose"
    >
      <el-upload
        v-model:file-list="fileList"
        accept=".xlsx,.xls"
        :auto-upload="false"
        :limit="1"
        :on-remove="handleFileRemove"
        drag
        class="import-upload"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            仅支持 .xlsx/.xls 格式文件，建议文件大小不超过10MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleImportClose">取消</el-button>
          <el-button type="primary" @click="handleImportSubmit" :loading="importLoading">
            确定导入
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Upload, Download, Search, RefreshRight, View, Edit, Delete, UploadFilled, Check, CircleClose } from '@element-plus/icons-vue'
import type { UploadFile } from 'element-plus'
import { unwrapPageResponse } from '@/api'
import { bomApi, type Material } from '@/api/bom'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DataTransformer } from '@/utils/data-transformer'
import { ErrorHandler } from '@/utils/error-handler'

// 路由实例
const router = useRouter()

// 搜索表单
const searchForm = ref({
  code: '',
  name: '',
  spec: '',
  status: ''
})

// 分页信息
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 物料列表数据
const materials = ref<Material[]>([])

// 选中的物料ID数组
const selectedIds = ref<(string | number)[]>([])

// 物料主数据是否可写（false 时由 ERP 权威维护，BOM 侧只读）
const materialWriteEnabled = ref(true)
// 只读提示语
const authorityHint = ref('')

const getResponseMessage = (response: any, fallback: string) => {
  const normalized = DataTransformer.normalizeResponse(response)
  return normalized?.msg || normalized?.message || fallback
}

// 处理表格选中事件
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

// 页面挂载时初始化数据
onMounted(() => {
  loadMaterialConfig()
  loadMaterials()
})

// 加载物料只读配置：写入禁用时置灰写操作按钮
const loadMaterialConfig = async () => {
  try {
    const res = await bomApi.getMaterialConfig()
    const data = DataTransformer.normalizeResponse(res)?.data
    if (data) {
      materialWriteEnabled.value = data.materialWriteEnabled !== false
      authorityHint.value = data.authorityHint || ''
    }
  } catch (error) {
    console.warn('获取物料配置失败，默认按可写处理:', error)
  }
}

// 加载物料列表
const loadMaterials = async () => {
  try {
    const params: any = {
      page: pagination.value.currentPage,
      size: pagination.value.pageSize
    }
    if (searchForm.value.code) params.code = searchForm.value.code
    if (searchForm.value.name) params.name = searchForm.value.name
    if (searchForm.value.spec) params.spec = searchForm.value.spec
    if (searchForm.value.status) params.status = searchForm.value.status

    const res = await bomApi.getMaterialList(params)
    const page = unwrapPageResponse<Material>(res)
    materials.value = page.list
    pagination.value.total = page.total
  } catch (error) {
    console.error('加载物料列表失败:', error)
    ErrorHandler.handleApiError(error)
    materials.value = []
    pagination.value.total = 0
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.currentPage = 1
  loadMaterials()
}

// 重置搜索条件
const handleReset = () => {
  searchForm.value = {
    code: '',
    name: '',
    spec: '',
    status: ''
  }
  pagination.value.currentPage = 1
  loadMaterials()
}

// 新增物料
const handleAdd = () => {
  router.push('/home/bom/material/detail')
}

// 查看物料详情
const handleView = (id: string) => {
  router.push(`/home/bom/material/detail/${id}`)
}

// 编辑物料
const handleEdit = (id: string) => {
  router.push(`/home/bom/material/detail/${id}?mode=edit`)
}

// 删除物料
const handleDelete = (id: string | number) => {
  ElMessageBox.confirm('确定要删除该物料吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await bomApi.deleteMaterial(id)
      const responseData = DataTransformer.normalizeResponse(res)
      if (!DataTransformer.isSuccessCode(responseData?.code)) {
        ElMessage.error(getResponseMessage(responseData, '删除失败'))
        return
      }
      ElMessage.success('删除成功')
      loadMaterials()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 导入物料相关
const importVisible = ref(false)
const importLoading = ref(false)
const fileList = ref<UploadFile[]>([])

// 处理文件上传成功
const handleFileSuccess = (response: any, uploadFile: UploadFile) => {
  fileList.value = []
  importVisible.value = false
  const normalized = DataTransformer.normalizeResponse(response)
  if (DataTransformer.isSuccessCode(normalized?.code)) {
    ElMessage.success('导入成功')
    loadMaterials()
  } else {
    ElMessage.error(`导入失败：${getResponseMessage(normalized, '')}`)
  }
}

// 处理文件上传错误
const handleFileError = () => {
  ElMessage.error('导入失败')
  importLoading.value = false
}

// 处理文件移除
const handleFileRemove = () => {
  fileList.value = []
}

// 导入物料
const handleImport = () => {
  importVisible.value = true
}

// 关闭导入对话框
const handleImportClose = () => {
  importVisible.value = false
  fileList.value = []
}

// 提交导入
const handleImportSubmit = () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择文件')
    return
  }
  importLoading.value = true
  const rawFile: any = (fileList.value[0] as any)?.raw
  if (!rawFile) {
    ElMessage.error('未获取到上传文件')
    importLoading.value = false
    return
  }

  const formData = new FormData()
  formData.append('file', rawFile)
  bomApi.importMaterials(formData)
    .then((res) => {
      const responseData = DataTransformer.normalizeResponse(res)
      if (!DataTransformer.isSuccessCode(responseData?.code)) {
        ElMessage.error(getResponseMessage(responseData, '导入失败'))
        return
      }
      ElMessage.success('导入成功')
      importVisible.value = false
      fileList.value = []
      loadMaterials()
    })
    .catch((error) => {
      console.error('导入失败:', error)
      ElMessage.error('导入失败')
    })
    .finally(() => {
      importLoading.value = false
    })
}

// 批量删除物料
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的物料')
    return
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个物料吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await Promise.all(selectedIds.value.map(id => bomApi.deleteMaterial(id)))
      ElMessage.success('批量删除成功')
      selectedIds.value = []
      loadMaterials()
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  })
}

// 批量启用物料
const handleBatchEnable = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要启用的物料')
    return
  }
  
  ElMessageBox.confirm(`确定要启用选中的 ${selectedIds.value.length} 个物料吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const tasks = selectedIds.value.map((id) => {
        const current = materials.value.find(m => m.id === id)
        if (!current) return Promise.resolve(null)
        return bomApi.updateMaterial(id, { ...current, status: 'ACTIVE' })
      })
      await Promise.all(tasks)
      ElMessage.success('批量启用成功')
      selectedIds.value = []
      loadMaterials()
    } catch (error) {
      console.error('批量启用失败:', error)
      ElMessage.error('批量启用失败')
    }
  })
}

// 批量停用物料
const handleBatchDisable = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要停用的物料')
    return
  }
  
  ElMessageBox.confirm(`确定要停用选中的 ${selectedIds.value.length} 个物料吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const tasks = selectedIds.value.map((id) => {
        const current = materials.value.find(m => m.id === id)
        if (!current) return Promise.resolve(null)
        return bomApi.updateMaterial(id, { ...current, status: 'INACTIVE' })
      })
      await Promise.all(tasks)
      ElMessage.success('批量停用成功')
      selectedIds.value = []
      loadMaterials()
    } catch (error) {
      console.error('批量停用失败:', error)
      ElMessage.error('批量停用失败')
    }
  })
}

// 导出物料
const handleExport = () => {
  const params = { ...searchForm.value }
  bomApi.exportMaterials(params)
    .then((res) => {
      const blob = new Blob([res.data], { type: 'application/vnd.ms-excel' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `物料清单_${new Date().toISOString().split('T')[0]}.xlsx`
      link.click()
      URL.revokeObjectURL(url)
      ElMessage.success('导出成功')
    })
    .catch((error) => {
      console.error('导出失败:', error)
      ElMessage.error('导出失败')
    })
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  loadMaterials()
}

// 当前页变化
const handleCurrentChange = (current: number) => {
  pagination.value.currentPage = current
  loadMaterials()
}
</script>

<style scoped>
.material-list {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #eef2ff 100%);
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h3 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  background: linear-gradient(to right, var(--primary-color), var(--primary-active));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.search-card {
  margin-bottom: 24px;
  padding-bottom: 0;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(0,0,0,0.05);
}

.code-text {
  font-family: 'JetBrains Mono', monospace;
  font-weight: 500;
  color: var(--primary-color);
}

.status-tag {
  border-radius: 12px;
  padding: 0 12px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table) {
  --el-table-border-color: transparent;
  background: transparent !important;
}

:deep(.el-table tr) {
  background: transparent !important;
  transition: all 0.3s ease;
}

:deep(.hover-row-effect:hover) {
  background: rgba(64, 104, 255, 0.05) !important;
  transform: scale(1.002);
}
</style>
