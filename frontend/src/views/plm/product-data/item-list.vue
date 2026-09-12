<template>
  <div class="item-list-view">
    <h3>物料库</h3>
    
    <!-- 搜索和筛选 -->
    <div class="search-filters">
      <el-card shadow="hover">
        <el-form :model="searchForm" inline>
          <el-form-item label="物料编码">
            <el-input v-model="searchForm.itemCode" placeholder="请输入物料编码"></el-input>
          </el-form-item>
          <el-form-item label="物料名称">
            <el-input v-model="searchForm.name" placeholder="请输入物料名称"></el-input>
          </el-form-item>
          <el-form-item label="物料类别">
            <el-select v-model="searchForm.category" placeholder="请选择物料类别">
              <el-option label="所有类别" value=""></el-option>
              <el-option label="原材料" value="raw_material"></el-option>
              <el-option label="半成品" value="semi_finished"></el-option>
              <el-option label="成品" value="finished_product"></el-option>
              <el-option label="标准件" value="standard_part"></el-option>
              <el-option label="外购件" value="purchased_part"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="生命周期阶段">
            <el-select v-model="searchForm.lifecyclePhase" placeholder="请选择生命周期阶段">
              <el-option label="所有阶段" value=""></el-option>
              <el-option label="概念" value="Concept"></el-option>
              <el-option label="设计" value="Design"></el-option>
              <el-option label="样机" value="Prototype"></el-option>
              <el-option label="量产" value="Production"></el-option>
              <el-option label="淘汰" value="Obsolete"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchItems">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    
    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="addItem">新增物料</el-button>
      <el-button @click="importItems">导入物料</el-button>
      <el-button @click="exportItems">导出物料</el-button>
      <el-button @click="batchDelete" :disabled="selectedItems.length === 0">批量删除</el-button>
    </div>
    
    <!-- 物料列表 -->
    <el-card shadow="hover" class="item-list-card">
      <div class="item-list">
        <el-table
          :data="filteredItems"
          style="width: 100%"
          v-loading="loading"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="itemCode" label="物料编码" min-width="150" sortable></el-table-column>
          <el-table-column prop="name" label="物料名称" min-width="200" sortable></el-table-column>
          <el-table-column prop="category" label="物料类别" min-width="120">
            <template #default="scope">
              <el-tag :type="getCategoryTagType(scope.row.category)">{{ scope.row.categoryLabel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="version" label="版本" min-width="80"></el-table-column>
          <el-table-column prop="lifecyclePhase" label="生命周期阶段" min-width="150">
            <template #default="scope">
              <el-tag :type="getLifecyclePhaseTagType(scope.row.lifecyclePhase)">{{ scope.row.lifecyclePhaseLabel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="isStandard" label="是否标准件" min-width="120">
            <template #default="scope">
              <el-switch v-model="scope.row.isStandard" disabled></el-switch>
            </template>
          </el-table-column>
          <el-table-column prop="materialType" label="材料" min-width="120"></el-table-column>
          <el-table-column label="重量" min-width="100">
            <template #default="scope">
              {{ scope.row.weight }} {{ scope.row.unit }}
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="260" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewItem(scope.row)">查看</el-button>
              <el-button size="small" @click="editItem(scope.row)">编辑</el-button>
              <el-button
                v-if="canRelease(scope.row)"
                type="warning"
                size="small"
                @click="releaseItem(scope.row)"
              >发布</el-button>
              <el-button size="small" type="danger" @click="deleteItem(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalItems"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          ></el-pagination>
        </div>
      </div>
    </el-card>
    
    <!-- 物料详情对话框 -->
    <el-dialog
      v-model="itemDetailVisible"
      title="物料详情"
      width="60%"
    >
      <el-form :model="currentItem" label-position="top" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物料编码">
              <el-input v-model="currentItem.itemCode" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料名称">
              <el-input v-model="currentItem.name" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料类别">
              <el-select v-model="currentItem.category" disabled>
                <el-option label="原材料" value="raw_material"></el-option>
                <el-option label="半成品" value="semi_finished"></el-option>
                <el-option label="成品" value="finished_product"></el-option>
                <el-option label="标准件" value="standard_part"></el-option>
                <el-option label="外购件" value="purchased_part"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本">
              <el-input v-model="currentItem.version" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生命周期阶段">
              <el-select v-model="currentItem.lifecyclePhase" disabled>
                <el-option label="概念" value="Concept"></el-option>
                <el-option label="设计" value="Design"></el-option>
                <el-option label="样机" value="Prototype"></el-option>
                <el-option label="量产" value="Production"></el-option>
                <el-option label="淘汰" value="Obsolete"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否标准件">
              <el-switch v-model="currentItem.isStandard" disabled></el-switch>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="材料">
              <el-input v-model="currentItem.materialType" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重量">
              <el-input-number v-model="currentItem.weight" :precision="2" disabled></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位">
              <el-input v-model="currentItem.unit" disabled></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="itemDetailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 添加/编辑物料对话框 -->
    <el-dialog
      v-model="itemFormVisible"
      :title="isEditMode ? '编辑物料' : '新增物料'"
      width="60%"
    >
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-position="top" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物料编码" prop="itemCode">
              <el-input v-model="itemForm.itemCode" placeholder="请输入物料编码"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料名称" prop="name">
              <el-input v-model="itemForm.name" placeholder="请输入物料名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料类别" prop="category">
              <el-select v-model="itemForm.category" placeholder="请选择物料类别">
                <el-option label="原材料" value="raw_material"></el-option>
                <el-option label="半成品" value="semi_finished"></el-option>
                <el-option label="成品" value="finished_product"></el-option>
                <el-option label="标准件" value="standard_part"></el-option>
                <el-option label="外购件" value="purchased_part"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本" prop="version">
              <el-input v-model="itemForm.version" placeholder="请输入版本"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生命周期阶段" prop="lifecyclePhase">
              <el-select v-model="itemForm.lifecyclePhase" placeholder="请选择生命周期阶段">
                <el-option label="概念" value="Concept"></el-option>
                <el-option label="设计" value="Design"></el-option>
                <el-option label="样机" value="Prototype"></el-option>
                <el-option label="量产" value="Production"></el-option>
                <el-option label="淘汰" value="Obsolete"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否标准件">
              <el-switch v-model="itemForm.isStandard"></el-switch>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="材料" prop="materialType">
              <el-input v-model="itemForm.materialType" placeholder="请输入材料类型"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重量" prop="weight">
              <el-input-number v-model="itemForm.weight" :precision="2" :min="0" placeholder="请输入重量"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="itemForm.unit" placeholder="请输入单位"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelForm">取消</el-button>
          <el-button type="primary" :loading="itemFormLoading" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 导入物料对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入物料"
      width="50%"
    >
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :on-change="handleFileChange"
        :file-list="uploadFiles"
        accept=".xlsx,.xls"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">拖拽文件到此处或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">
            支持上传 .xlsx, .xls 格式文件，大小不超过 10MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleImport" :disabled="uploadFiles.length === 0">导入</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useProductDataStore } from '@/stores/plm/productData'
import { plmApi } from '@/api/plm'

const store = useProductDataStore()

// 搜索表单
const searchForm = ref({
  itemCode: '',
  name: '',
  category: '',
  lifecyclePhase: ''
})

const currentPage = computed({
  get: () => store.pagination.page,
  set: (value) => store.setPagination(value, store.pagination.pageSize)
})
const pageSize = computed({
  get: () => store.pagination.pageSize,
  set: (value) => store.setPagination(store.pagination.page, value)
})

// 加载状态
const loading = ref(false)

// 选中的物料
const selectedItems = ref<any[]>([])

// 物料详情对话框
const itemDetailVisible = ref(false)
const currentItem = ref<any>({})

// 添加/编辑物料对话框
const itemFormVisible = ref(false)
const isEditMode = ref(false)
const itemFormRef = ref<any>(null)
const itemFormLoading = ref(false)
const itemForm = ref({
  id: 0,
  itemCode: '',
  name: '',
  category: '',
  version: '',
  lifecyclePhase: '',
  isStandard: false,
  materialType: '',
  weight: 0,
  unit: ''
})

// 表单验证规则
const itemRules = reactive({
  itemCode: [
    { required: true, message: '请输入物料编码', trigger: 'blur' },
    { min: 1, max: 20, message: '物料编码长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入物料名称', trigger: 'blur' },
    { min: 1, max: 50, message: '物料名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择物料类别', trigger: 'change' }
  ],
  version: [
    { required: true, message: '请输入版本', trigger: 'blur' },
    { min: 1, max: 10, message: '版本长度在 1 到 10 个字符', trigger: 'blur' }
  ],
  materialType: [
    { required: true, message: '请输入材料类型', trigger: 'blur' },
    { min: 1, max: 20, message: '材料类型长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  weight: [{ type: 'number', min: 0, message: '重量必须大于等于 0', trigger: 'blur' }],
  unit: [
    { required: true, message: '请输入单位', trigger: 'blur' },
    { min: 1, max: 10, message: '单位长度在 1 到 10 个字符', trigger: 'blur' }
  ]
})

// 导入物料对话框
const importDialogVisible = ref(false)
const uploadRef = ref<any>(null)
const uploadFiles = ref<any[]>([])

// 物料类别标签类型映射
const getCategoryTagType = (category: string) => {
  const categoryMap: Record<string, string> = {
    'raw_material': 'info',
    'semi_finished': 'success',
    'finished_product': 'info',
    'standard_part': 'warning',
    'purchased_part': 'danger'
  }
  return categoryMap[category] || 'info'
}

// 生命周期阶段标签类型映射
const getLifecyclePhaseTagType = (phase: string) => {
  const phaseMap: Record<string, string> = {
    'Concept': 'info',
    'Design': 'info',
    'Prototype': 'warning',
    'Production': 'success',
    'Obsolete': 'danger'
  }
  return phaseMap[phase] || 'info'
}

// 物料类别标签映射
const getCategoryLabel = (category: string) => {
  const categoryMap: Record<string, string> = {
    'raw_material': '原材料',
    'semi_finished': '半成品',
    'finished_product': '成品',
    'standard_part': '标准件',
    'purchased_part': '外购件'
  }
  return categoryMap[category] || category
}

// 生命周期阶段标签映射
const getLifecyclePhaseLabel = (phase: string) => {
  const phaseMap: Record<string, string> = {
    'Concept': '概念',
    'Design': '设计',
    'Prototype': '样机',
    'Production': '量产',
    'Obsolete': '淘汰'
  }
  return phaseMap[phase] || phase
}

const categoryToProductType = (category: string) => {
  const map: Record<string, string> = {
    raw_material: 'raw',
    semi_finished: 'semi',
    finished_product: 'finished',
    standard_part: 'standard',
    purchased_part: 'purchased'
  }
  return map[category] || category
}

const productTypeToCategory = (productType: string) => {
  const map: Record<string, string> = {
    raw: 'raw_material',
    semi: 'semi_finished',
    finished: 'finished_product',
    standard: 'standard_part',
    purchased: 'purchased_part'
  }
  return map[productType] || productType
}

const items = computed(() => {
  return (store.materials || []).map((m: any) => {
    const category = productTypeToCategory(m.type || '')
    // 后端产品状态映射为生命周期阶段（DESIGN->设计, PROTOTYPE->原型, RELEASED->生产）
    const statusPhaseMap: Record<string, string> = {
      DESIGN: 'Design',
      PROTOTYPE: 'Prototype',
      RELEASED: 'Production'
    }
    const phase = statusPhaseMap[(m.status || '').toUpperCase()] || m.status || ''
    return {
      id: Number(m.id),
      itemCode: m.code,
      name: m.name,
      category,
      categoryLabel: getCategoryLabel(category),
      version: m.version,
      lifecyclePhase: phase,
      lifecyclePhaseLabel: getLifecyclePhaseLabel(phase),
      isStandard: false,
      materialType: m.spec || '',
      weight: 0,
      unit: m.unit || '件',
      status: m.status || ''
    }
  })
})

const filteredItems = computed(() => items.value)
const totalItems = computed(() => store.pagination.total)

// 搜索物料
const searchItems = async () => {
  store.setPagination(1, store.pagination.pageSize)
  const keyword = searchForm.value.itemCode || searchForm.value.name || ''
  store.setMaterialSearchKeyword(keyword)
  store.setMaterialTypeFilter(searchForm.value.category ? categoryToProductType(searchForm.value.category) : 'all')
  await store.fetchMaterials()
}

// 重置搜索
const resetSearch = async () => {
  searchForm.value = {
    itemCode: '',
    name: '',
    category: '',
    lifecyclePhase: ''
  }
  store.resetMaterialFilters()
  await store.fetchMaterials()
}

// 查看物料详情
const viewItem = (row: any) => {
  currentItem.value = { ...row }
  itemDetailVisible.value = true
}

// 打开添加物料对话框
const addItem = () => {
  isEditMode.value = false
  itemForm.value = {
    id: 0,
    itemCode: '',
    name: '',
    category: '',
    version: '',
    lifecyclePhase: '',
    isStandard: false,
    materialType: '',
    weight: 0,
    unit: ''
  }
  itemFormVisible.value = true
}

// 打开编辑物料对话框
const editItem = (row: any) => {
  isEditMode.value = true
  itemForm.value = { ...row }
  itemFormVisible.value = true
}

// 取消表单
const cancelForm = () => {
  itemFormVisible.value = false
  if (itemFormRef.value) {
    itemFormRef.value.resetFields()
  }
}

// 提交表单
const submitForm = async () => {
  if (!itemFormRef.value) return
  
  try {
    await itemFormRef.value.validate()
    itemFormLoading.value = true
    
    if (isEditMode.value) {
      await store.updateMaterial(itemForm.value.id, {
        productCode: itemForm.value.itemCode,
        productName: itemForm.value.name,
        productType: categoryToProductType(itemForm.value.category),
        productModel: itemForm.value.materialType,
        version: itemForm.value.version,
        unit: itemForm.value.unit,
        status: 'ACTIVE'
      })
      ElMessage.success('物料更新成功')
    } else {
      await store.createMaterial({
        productCode: itemForm.value.itemCode,
        productName: itemForm.value.name,
        productType: categoryToProductType(itemForm.value.category),
        productModel: itemForm.value.materialType,
        version: itemForm.value.version,
        unit: itemForm.value.unit,
        status: 'ACTIVE'
      })
      ElMessage.success('物料添加成功')
    }
    
    itemFormVisible.value = false
    await store.fetchMaterials()
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    itemFormLoading.value = false
  }
}

// 判断产品是否可发布（设计/原型阶段的产品可发布）
const canRelease = (row: any) => {
  const status = (row.status || '').toUpperCase()
  return status === 'DESIGN' || status === 'PROTOTYPE'
}

// 发布产品（后端会同步物料到ERP并发布BOM结构到BOM服务）
const releaseItem = (row: any) => {
  ElMessageBox.confirm(`确定要发布产品「${row.name}」吗？发布后将同步物料主数据与BOM结构。`, '发布确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await plmApi.productData.releaseProduct(row.id)
    ElMessage.success('产品发布成功')
    await store.fetchMaterials()
  }).catch(() => {
    // 取消发布
  })
}

// 删除物料
const deleteItem = (id: number) => {
  ElMessageBox.confirm('确定要删除该物料吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await store.deleteMaterial(id)
    ElMessage.success('物料删除成功')
    await store.fetchMaterials()
  }).catch(() => {
    // 取消删除
  })
}

// 批量删除物料
const batchDelete = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择要删除的物料')
    return
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedItems.value.length} 个物料吗？`, '批量删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const idsToDelete = selectedItems.value.map(item => item.id)
    await Promise.all(idsToDelete.map((id: any) => store.deleteMaterial(id)))
    selectedItems.value = []
    ElMessage.success('批量删除成功')
    await store.fetchMaterials()
  }).catch(() => {
    // 取消删除
  })
}

// 导入物料
const importItems = () => {
  importDialogVisible.value = true
}

// 处理文件变化
const handleFileChange = (file: any) => {
  uploadFiles.value = [file]
}

// 处理导入
const handleImport = () => {
  if (uploadFiles.value.length === 0) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }

  const file = uploadFiles.value[0]?.raw
  if (!file) {
    ElMessage.error('文件不可用')
    return
  }
  const reader = new FileReader()
  reader.onload = async () => {
    try {
      const text = String(reader.result || '')
      const data = JSON.parse(text)
      const list = Array.isArray(data) ? data : []
      for (const row of list) {
        await store.createMaterial({
          productCode: row.productCode || row.itemCode,
          productName: row.productName || row.name,
          productType: row.productType || categoryToProductType(row.category || ''),
          productModel: row.productModel || row.materialType,
          version: row.version || 'V1.0',
          unit: row.unit || '件',
          status: row.status || 'ACTIVE'
        })
      }
      ElMessage.success('物料导入成功')
      importDialogVisible.value = false
      uploadFiles.value = []
      if (uploadRef.value) uploadRef.value.clearFiles()
      await store.fetchMaterials()
    } catch (e) {
      ElMessage.error('导入文件格式错误（请上传JSON数组）')
    }
  }
  reader.readAsText(file)
}

// 导出物料
const exportItems = () => {
  const content = JSON.stringify(store.materials || [], null, 2)
  const blob = new Blob([content], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `materials_${new Date().toISOString().slice(0, 10)}.json`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('物料导出成功')
}

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedItems.value = selection
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  store.setPagination(1, size)
  store.fetchMaterials()
}

// 当前页码变化
const handleCurrentChange = (page: number) => {
  store.setPagination(page, store.pagination.pageSize)
  store.fetchMaterials()
}

onMounted(async () => {
  await store.fetchMaterials()
})
</script>

<style scoped lang="scss">
.item-list-view {
  padding: 20px;
  height: 100%;
  overflow: auto;

  h3 {
    margin-bottom: 20px;
    color: #333;
    font-size: 1.3rem;
  }

  /* 搜索和筛选 */
  .search-filters {
    margin-bottom: 20px;
  }

  /* 操作按钮 */
  .action-buttons {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    flex-wrap: wrap;
  }

  /* 物料列表卡片 */
  .item-list-card {
    height: calc(100% - 200px);
    display: flex;
    flex-direction: column;
  }

  .item-list {
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .el-table {
    flex: 1;
  }

  /* 分页 */
  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  /* 响应式设计 */
  @media (max-width: 1024px) {
    padding: 16px;

    .search-filters {
      margin-bottom: 16px;
    }

    .action-buttons {
      margin-bottom: 16px;
      gap: 8px;
    }

    .item-list-card {
      height: calc(100% - 180px);
    }
  }

  @media (max-width: 768px) {
    padding: 12px;

    h3 {
      font-size: 1.1rem;
      margin-bottom: 16px;
    }

    .action-buttons {
      gap: 6px;
    }

    .item-list-card {
      height: calc(100% - 220px);
    }

    .el-button {
      padding: 4px 8px;
      font-size: 12px;
    }
  }
}
</style>
