<template>
  <div class="material-select">
    <el-select
      v-model="modelValue"
      :placeholder="placeholder"
      :clearable="clearable"
      :disabled="disabled"
      @click.stop="handleSelectClick"
    >
      <el-option
        v-for="material in selectedMaterials"
        :key="material.id"
        :label="material.name"
        :value="material.id"
      >
        <template #default="{ option }">
          <div class="option-content">
            <div class="option-name">{{ material.name }}</div>
            <div class="option-spec">{{ material.specification }}</div>
          </div>
        </template>
      </el-option>
    </el-select>

    <!-- 物料选择弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="选择物料"
      width="800px"
      @close="handleDialogClose"
    >
      <!-- 搜索条件 -->
      <div class="dialog-search">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-input
              v-model="searchQuery"
              placeholder="请输入物料名称或规格"
              clearable
              @input="handleSearch"
            >
              <template #append>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
          </el-col>
          <el-col :span="12">
            <el-select
              v-model="materialCategoryId"
              placeholder="请选择物料分类"
              clearable
              @change="handleSearch"
            >
              <el-option label="全部" value="" />
              <el-option
                v-for="category in materialCategories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-col>
        </el-row>
      </div>

      <!-- 物料列表 -->
      <el-table
        :data="materialList"
        v-loading="loading"
        style="width: 100%"
        @row-click="handleRowClick"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="物料ID" width="100" />
        <el-table-column prop="name" label="物料名称" min-width="180" />
        <el-table-column prop="specification" label="规格" min-width="150" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="price" label="参考价格" width="120">
          <template #default="scope">
            ¥{{ formatMoney(scope.row.price) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="dialog-pagination">
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

      <!-- 弹窗底部按钮 -->
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { materialApi } from '../../../api/srm'
import { DataTransformer } from '../../../utils/data-transformer'

// 模拟物料API和类型
interface Material {
  id: number
  name: string
  specification: string
  unit: string
  categoryId: number
  categoryName: string
  price: number
  status: 'ACTIVE' | 'INACTIVE'
}

interface MaterialCategory {
  id: number
  name: string
}

const formatMoney = (value: unknown) => DataTransformer.formatMoney(value)

// Props
const props = defineProps<{
  modelValue: number | undefined
  placeholder?: string
  clearable?: boolean
  disabled?: boolean
  multiple?: boolean
  categoryId?: number
}>()

// Emits
const emit = defineEmits<{
  (e: 'update:modelValue', value: number | undefined): void
  (e: 'change', value: number | undefined): void
}>()

// 物料列表
const materialList = ref<Material[]>([])
const selectedMaterials = ref<Material[]>([])

// 物料分类
const materialCategories = ref<MaterialCategory[]>([])
const materialCategoryId = ref<number | string>('')

// 弹窗控制
const dialogVisible = ref(false)

// 搜索条件
const searchQuery = ref('')
const loading = ref(false)

// 分页数据
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 选中的物料
const selectedMaterialIds = ref<number[]>([])

/**
 * 处理选择框点击事件
 */
const handleSelectClick = () => {
  if (props.disabled) return
  dialogVisible.value = true
  handleSearch()
}

/**
 * 搜索物料
 */
const handleSearch = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.currentPage - 1,
      size: pagination.value.pageSize,
      keyword: searchQuery.value,
      categoryId: materialCategoryId.value || undefined,
      status: 'ACTIVE'
    }
    const [categoryRes, materialRes] = await Promise.all([
      materialApi.getMaterialCategoryList(),
      materialApi.getMaterialList(params)
    ])
    materialCategories.value = categoryRes.data || []
    materialList.value = (materialRes.data.list || materialRes.data.records || [])
    pagination.value.total = (materialRes.data.total || 0)
  } catch (error) {
    console.error('获取物料列表失败:', error)
    ElMessage.error('获取物料列表失败')
    materialList.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

/**
 * 分页大小变化
 */
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  pagination.value.currentPage = 1
  handleSearch()
}

/**
 * 当前页码变化
 */
const handleCurrentChange = (current: number) => {
  pagination.value.currentPage = current
  handleSearch()
}

/**
 * 处理行点击事件
 */
const handleRowClick = (row: Material) => {
  if (!props.multiple) {
    selectedMaterialIds.value = [row.id]
    handleConfirm()
  }
}

/**
 * 处理选择变化
 */
const handleSelectionChange = (selection: Material[]) => {
  selectedMaterialIds.value = selection.map(item => item.id)
}

/**
 * 确认选择
 */
const handleConfirm = () => {
  if (selectedMaterialIds.value.length === 0) {
    ElMessage.warning('请选择至少一个物料')
    return
  }

  const selectedMaterial = materialList.value.find(item => item.id === selectedMaterialIds.value[0])
  if (selectedMaterial) {
    selectedMaterials.value = [selectedMaterial]
    emit('update:modelValue', selectedMaterial.id)
    emit('change', selectedMaterial.id)
  }

  dialogVisible.value = false
  selectedMaterialIds.value = []
}

/**
 * 处理弹窗关闭
 */
const handleDialogClose = () => {
  selectedMaterialIds.value = []
}

// 初始化
onMounted(() => {
  handleSearch()
})

// 监听modelValue变化
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    materialApi.getMaterialDetail(newVal).then((res: any) => {
      selectedMaterials.value = res.data ? [res.data] : []
    }).catch(() => {
      selectedMaterials.value = []
    })
  } else {
    selectedMaterials.value = []
  }
})
</script>

<style scoped>
.material-select {
  position: relative;
}

.option-content {
  display: flex;
  flex-direction: column;
}

.option-name {
  font-weight: bold;
}

.option-spec {
  font-size: 12px;
  color: #666;
}

.dialog-search {
  margin-bottom: 20px;
}

.dialog-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
