<template>
  <div class="fixed-assets-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><Box /></el-icon>
          <span>固定资产管理</span>
        </div>
      </template>
      
      <!-- 固定资产列表 -->
      <TableComponent
        :data="fixedAssetsList"
        :columns="fixedAssetsColumns"
        :total="fixedAssetsTotal"
        :loading="fixedAssetsLoading"
        :show-index="true"
        :show-selection="true"
        :show-action="true"
        :actions="fixedAssetsActions"
        :table-actions="tableActions"
        :filters="fixedAssetsFilters"
        :show-filter="true"
        @search="handleSearch"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @selection-change="handleSelectionChange"
      >
        <!-- 状态列自定义 -->
        <template #status="{ row }">
          <el-tag
            :type="getStatusType(row.status)"
            size="small"
          >
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </TableComponent>
    </el-card>

    <!-- 折旧计提对话框 -->
    <DialogComponent
      v-model="depreciationFormVisible"
      title="计提折旧"
      width="600px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="计提"
      @confirm="handleDepreciation"
      :show-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <div class="depreciation-form">
        <el-form
          ref="depreciationFormRef"
          :model="depreciationForm"
          label-width="120px"
        >
          <el-form-item label="计提日期" prop="depreciation_date" required>
            <el-date-picker
              v-model="depreciationForm.depreciation_date"
              type="date"
              placeholder="选择计提日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="计提期间" prop="period" required>
            <el-input
              v-model="depreciationForm.period"
              placeholder="例如：2025-12"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="depreciationForm.remark"
              type="textarea"
              rows="3"
              placeholder="输入备注信息"
            />
          </el-form-item>
        </el-form>
      </div>
    </DialogComponent>

  <!-- 资产详情对话框 -->
    <DialogComponent
      v-model="detailVisible"
      title="固定资产详情"
      width="800px"
      :show-footer="false"
      :show-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <div class="asset-detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <span class="label">资产编码：</span>
              <span class="value">{{ selectedAsset?.asset_code }}</span>
            </div>
            <div class="detail-item">
              <span class="label">资产名称：</span>
              <span class="value">{{ selectedAsset?.asset_name }}</span>
            </div>
            <div class="detail-item">
              <span class="label">资产类型：</span>
              <span class="value">{{ selectedAsset?.asset_type }}</span>
            </div>
            <div class="detail-item">
              <span class="label">购置日期：</span>
              <span class="value">{{ selectedAsset?.purchase_date }}</span>
            </div>
            <div class="detail-item">
              <span class="label">原值：</span>
              <span class="value">{{ selectedAsset?.original_value }}</span>
            </div>
            <div class="detail-item">
              <span class="label">累计折旧：</span>
              <span class="value">{{ selectedAsset?.accumulated_depreciation }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <span class="label">净值：</span>
              <span class="value">{{ selectedAsset?.net_value }}</span>
            </div>
            <div class="detail-item">
              <span class="label">预计使用年限：</span>
              <span class="value">{{ selectedAsset?.useful_life }}年</span>
            </div>
            <div class="detail-item">
              <span class="label">残值率：</span>
              <span class="value">{{ selectedAsset?.residual_rate }}%</span>
            </div>
            <div class="detail-item">
              <span class="label">使用部门：</span>
              <span class="value">{{ selectedAsset?.department }}</span>
            </div>
            <div class="detail-item">
              <span class="label">存放地点：</span>
              <span class="value">{{ selectedAsset?.location }}</span>
            </div>
            <div class="detail-item">
              <span class="label">状态：</span>
              <span class="value">
                <el-tag :type="getStatusType(selectedAsset?.status || '')" size="small">
                  {{ getStatusLabel(selectedAsset?.status || '') }}
                </el-tag>
              </span>
            </div>
          </el-col>
        </el-row>
        <div class="detail-item full-width">
          <span class="label">备注：</span>
          <span class="value">{{ selectedAsset?.remark || '无' }}</span>
        </div>
      </div>
    </DialogComponent>
    
    <!-- 资产编辑对话框 -->
    <DialogComponent
      v-model="editFormVisible"
      title="编辑固定资产"
      width="800px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="保存"
      @confirm="handleEditSave"
      :show-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <div class="edit-form">
        <el-form
          ref="editFormRef"
          :model="editForm"
          :rules="editFormRules"
          label-width="120px"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="资产编码" prop="asset_code">
                <el-input v-model="editForm.asset_code" placeholder="请输入资产编码" />
              </el-form-item>
              <el-form-item label="资产名称" prop="asset_name">
                <el-input v-model="editForm.asset_name" placeholder="请输入资产名称" />
              </el-form-item>
              <el-form-item label="资产类型" prop="asset_type">
                <el-select v-model="editForm.asset_type" placeholder="请选择资产类型">
                  <el-option label="房屋建筑" value="building" />
                  <el-option label="机器设备" value="equipment" />
                  <el-option label="运输工具" value="vehicle" />
                  <el-option label="电子设备" value="electronic" />
                  <el-option label="办公家具" value="furniture" />
                </el-select>
              </el-form-item>
              <el-form-item label="购置日期" prop="purchase_date">
                <el-date-picker
                  v-model="editForm.purchase_date"
                  type="date"
                  placeholder="选择购置日期"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="原值" prop="original_value">
                <el-input-number
                  v-model="editForm.original_value"
                  :min="0"
                  :step="0.01"
                  placeholder="输入原值"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="预计使用年限" prop="useful_life">
                <el-input-number
                  v-model="editForm.useful_life"
                  :min="1"
                  :step="1"
                  placeholder="输入预计使用年限"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="残值率" prop="residual_rate">
                <el-input-number
                  v-model="editForm.residual_rate"
                  :min="0"
                  :max="100"
                  :step="0.01"
                  placeholder="输入残值率"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="使用部门" prop="department">
                <el-input v-model="editForm.department" placeholder="请输入使用部门" />
              </el-form-item>
              <el-form-item label="存放地点" prop="location">
                <el-input v-model="editForm.location" placeholder="请输入存放地点" />
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-select v-model="editForm.status" placeholder="请选择状态">
                  <el-option label="在用" value="active" />
                  <el-option label="停用" value="inactive" />
                  <el-option label="已处置" value="disposed" />
                  <el-option label="已报废" value="scrapped" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="editForm.remark"
              type="textarea"
              rows="3"
              placeholder="输入备注信息"
            />
          </el-form-item>
        </el-form>
      </div>
    </DialogComponent>
    
    <!-- 资产处置对话框 -->
    <DialogComponent
      v-model="disposalFormVisible"
      title="资产处置"
      width="600px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="处置"
      @confirm="handleDisposal"
      :show-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <div class="disposal-form">
        <div class="disposal-info">
          <div class="info-item">
            <span class="label">资产名称：</span>
            <span class="value">{{ selectedAsset?.asset_name }}</span>
          </div>
          <div class="info-item">
            <span class="label">当前净值：</span>
            <span class="value">{{ selectedAsset?.net_value }}</span>
          </div>
        </div>
        
        <el-form
          ref="disposalFormRef"
          :model="disposalForm"
          label-width="120px"
          class="mt-20"
        >
          <el-form-item label="处置日期" prop="disposal_date" required>
            <el-date-picker
              v-model="disposalForm.disposal_date"
              type="date"
              placeholder="选择处置日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="处置方式" prop="disposal_type" required>
            <el-select
              v-model="disposalForm.disposal_type"
              placeholder="选择处置方式"
              style="width: 100%"
            >
              <el-option label="出售" value="sale" />
              <el-option label="报废" value="scrap" />
              <el-option label="捐赠" value="donation" />
            </el-select>
          </el-form-item>
          <el-form-item label="处置金额" prop="disposal_amount">
            <el-input-number
              v-model="disposalForm.disposal_amount"
              :min="0"
              :step="0.01"
              placeholder="输入处置金额"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="disposalForm.remark"
              type="textarea"
              rows="3"
              placeholder="输入备注信息"
            />
          </el-form-item>
        </el-form>
      </div>
    </DialogComponent>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Box, Plus, Edit, Delete, RefreshLeft, View, DocumentChecked, Link, Download } from '@element-plus/icons-vue'
import { TableComponent, DialogComponent } from '../../../components/base'
import { unwrapPageResponse, unwrapResponseData } from '../../../api'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
// 固定资产列表
const fixedAssetsList = ref<any[]>([])
const fixedAssetsTotal = ref(0)
const fixedAssetsLoading = ref(false)
const fixedAssetsPage = ref(1)
const fixedAssetsSize = ref(10)
const selectedRows = ref<any[]>([])
const selectedAsset = ref<any>(null)

// 对话框状态
const depreciationFormVisible = ref(false)
const disposalFormVisible = ref(false)
const detailVisible = ref(false)
const editFormVisible = ref(false)

// 表单数据
const depreciationFormRef = ref<any>(null)
const depreciationFormRules = ref<any>({
  depreciation_date: [{ required: true, message: '请选择计提日期', trigger: 'change' }],
  period: [{ required: true, message: '请输入计提期间', trigger: 'blur' }, { pattern: /^\d{4}-\d{2}$/, message: '请输入正确的期间格式（如：2025-12）', trigger: 'blur' }]
})
const depreciationForm = ref({
  depreciation_date: '',
  period: '',
  remark: ''
})

const disposalFormRef = ref<any>(null)
const disposalFormRules = ref<any>({
  disposal_date: [{ required: true, message: '请选择处置日期', trigger: 'change' }],
  disposal_type: [{ required: true, message: '请选择处置方式', trigger: 'change' }],
  disposal_amount: [{ type: 'number', min: 0, message: '处置金额必须大于等于0', trigger: 'blur' }]
})
const disposalForm = ref({
  disposal_date: '',
  disposal_type: 'sale',
  disposal_amount: 0,
  remark: ''
})

const editFormRef = ref<any>(null)
const editFormRules = ref<any>({
  asset_code: [{ required: true, message: '请输入资产编码', trigger: 'blur' }],
  asset_name: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  asset_type: [{ required: true, message: '请选择资产类型', trigger: 'change' }],
  purchase_date: [{ required: true, message: '请选择购置日期', trigger: 'change' }],
  original_value: [{ required: true, message: '请输入原值', trigger: 'blur' }, { type: 'number', min: 0, message: '原值必须大于0', trigger: 'blur' }],
  useful_life: [{ required: true, message: '请输入预计使用年限', trigger: 'blur' }, { type: 'number', min: 1, message: '预计使用年限必须大于0', trigger: 'blur' }],
  residual_rate: [{ type: 'number', min: 0, max: 100, message: '残值率必须在0-100之间', trigger: 'blur' }],
  department: [{ required: true, message: '请输入使用部门', trigger: 'blur' }],
  location: [{ required: true, message: '请输入存放地点', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})
const editForm = ref({
  id: '',
  asset_code: '',
  asset_name: '',
  asset_type: '',
  purchase_date: '',
  original_value: 0,
  accumulated_depreciation: 0,
  net_value: 0,
  useful_life: 0,
  residual_rate: 0,
  department: '',
  location: '',
  status: 1, // 默认状态：在用
  remark: ''
})

// 固定资产状态映射
const statusMap: Record<string, { label: string; type: string }> = {
  '1': { label: '在用', type: 'success' },
  '2': { label: '闲置', type: 'info' },
  '3': { label: '报废', type: 'warning' },
  '4': { label: '处置', type: 'danger' }
}

// 资产类别列表
const assetCategories = ref<{ label: string; value: string }[]>([
  { label: '房屋建筑', value: '房屋建筑' },
  { label: '机器设备', value: '机器设备' },
  { label: '运输工具', value: '运输工具' },
  { label: '电子设备', value: '电子设备' },
  { label: '办公设备', value: '办公设备' },
  { label: '其他', value: '其他' }
])

// 固定资产筛选条件
const fixedAssetsFilters = [
  { prop: 'asset_code', label: '资产编码', type: 'input' as 'input', placeholder: '请输入资产编码' },
  { prop: 'asset_name', label: '资产名称', type: 'input' as 'input', placeholder: '请输入资产名称' },
  { prop: 'asset_type', label: '资产类型', type: 'select' as 'select', options: assetCategories.value },
  { prop: 'status', label: '状态', type: 'select' as 'select', options: [
    { label: '在用', value: '1' },
    { label: '闲置', value: '2' },
    { label: '报废', value: '3' },
    { label: '处置', value: '4' }
  ]},
  { prop: 'purchase_date', label: '购置日期', type: 'daterange' as 'daterange' }
] as any

// 固定资产列配置
const fixedAssetsColumns = [
  { prop: 'asset_code', label: '资产编码', width: 150 },
  { prop: 'asset_name', label: '资产名称', width: 200 },
  { 
    prop: 'asset_type', 
    label: '资产类型', 
    width: 120,
    formatter: (row: any) => getAssetTypeLabel(row.asset_type)
  },
  { 
    prop: 'purchase_date', 
    label: '购置日期', 
    width: 150,
    formatter: (row: any) => formatDate(row.purchase_date)
  },
  { 
    prop: 'original_value', 
    label: '原值', 
    width: 150, 
    align: 'right',
    formatter: (row: any) => formatMoney(row.original_value)
  },
  { 
    prop: 'accumulated_depreciation', 
    label: '累计折旧', 
    width: 150, 
    align: 'right',
    formatter: (row: any) => formatMoney(row.accumulated_depreciation)
  },
  { 
    prop: 'net_value', 
    label: '净值', 
    width: 150, 
    align: 'right',
    formatter: (row: any) => formatMoney(row.net_value)
  },
  { prop: 'status', label: '状态', width: 120, slotName: 'status' },
  { prop: 'department', label: '使用部门', width: 150 },
  { prop: 'location', label: '存放地点', width: 200 },
  { 
    prop: 'create_time', 
    label: '创建时间', 
    width: 180,
    formatter: (row: any) => formatDate(row.create_time)
  }
]

// 固定资产操作按钮
const fixedAssetsActions = [
  {
    text: '查看',
    type: 'primary',
    size: 'small',
    icon: View,
    handler: (row: any) => {
      selectedAsset.value = row
      detailVisible.value = true
    }
  },
  {
    text: '编辑',
    type: 'info',
    size: 'small',
    icon: Edit,
    handler: (row: any) => {
      selectedAsset.value = row
      Object.assign(editForm.value, { ...row })
      editFormVisible.value = true
    }
  },
  {
    text: '折旧',
    type: 'success',
    size: 'small',
    icon: DocumentChecked,
    handler: async (row: any) => {
      selectedAsset.value = row
      try {
        await erpApi.finance.calculateDepreciation(row.id, {
          depreciationDate: new Date().toISOString().slice(0, 10),
          period: new Date().toISOString().slice(0, 7)
        })
        ElMessage.success(`资产 ${row.asset_name} 折旧计提成功`)
        handleSearch()
      } catch (error) {
        ErrorHandler.handleApiError(error)
      }
    }
  },
  {
    text: '关联EAM',
    type: 'primary',
    size: 'small',
    icon: Link,
    handler: async (row: any) => {
      try {
        const res = await ElMessageBox.prompt('请输入EAM资产ID', '关联EAM资产', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /^\d+$/,
          inputErrorMessage: '请输入数字ID'
        })
        await erpApi.finance.linkFixedAssetEam(Number(row.id), Number(res.value))
        ElMessage.success('关联成功')
      } catch (e) {
        if (String(e).includes('cancel')) return
        ErrorHandler.handleApiError(e)
      }
    }
  },
  {
    text: '查看EAM',
    type: 'primary',
    size: 'small',
    icon: View,
    handler: async (row: any) => {
      try {
        const res = await erpApi.finance.getFixedAssetEam(Number(row.id))
        const eamAsset = unwrapResponseData<any>(res)
        await ElMessageBox.alert(`<pre>${JSON.stringify(eamAsset, null, 2)}</pre>`, 'EAM资产信息', { dangerouslyUseHTMLString: true })
      } catch (e) {
        ErrorHandler.handleApiError(e)
      }
    }
  },
  {
    text: '处置',
    type: 'warning',
    size: 'small',
    icon: Delete,
    handler: (row: any) => {
      selectedAsset.value = row
      disposalFormVisible.value = true
    }
  }
]

// 表格操作按钮
const tableActions = [
  {
    key: 'add',
    text: '新增固定资产',
    type: 'primary',
    icon: Plus,
    handler: () => {
      // 重置表单
      Object.assign(editForm.value, {
        id: '',
        asset_code: '',
        asset_name: '',
        asset_type: '',
        purchase_date: '',
        original_value: 0,
        accumulated_depreciation: 0,
        net_value: 0,
        useful_life: 0,
        residual_rate: 0,
        department: '',
        location: '',
        status: 'active',
        remark: ''
      })
      selectedAsset.value = null
      editFormVisible.value = true
    }
  },
  {
    key: 'sync-eam',
    text: '从EAM同步',
    type: 'warning',
    icon: Download,
    handler: async () => {
      try {
        await ElMessageBox.confirm('确认从EAM同步资产到ERP？将按资产编码去重并自动建立映射。', '同步确认', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await erpApi.finance.syncFixedAssetsFromEam()
        ElMessage.success('同步完成')
        handleSearch()
      } catch (e) {
        if (String(e).includes('cancel')) return
        ErrorHandler.handleApiError(e)
      }
    }
  },
  {
    key: 'depreciate',
    text: '计提折旧',
    type: 'success',
    icon: DocumentChecked,
    handler: () => {
      depreciationFormVisible.value = true
    }
  },
  {
    key: 'refresh',
    text: '刷新',
    icon: RefreshLeft,
    handler: () => {
      handleSearch()
    }
  }
]

// 从API获取资产类别列表
const fetchAssetCategories = async () => {
  try {
    const result = await erpApi.finance.getAssetCategories()
    if (result?.data?.length > 0) {
      assetCategories.value = result.data.map((category: string) => ({
        label: category,
        value: category
      }))
    }
  } catch (error) {
    console.warn('获取资产类别列表失败，使用默认数据:', error)
  }
}

// 初始加载
onMounted(() => {
  fetchAssetCategories()
  handleSearch()
})

// 字段映射：将后端 ERP 字段名转换为前端展示字段名
// 后端使用 SNAKE_CASE 序列化驼峰实体（assetNo -> asset_no），与前端类型（asset_code）不一致
const mapBackendToFrontend = (row: any) => {
  if (!row) return row
  return {
    ...row,
    // 资产编码
    asset_code: row.asset_code ?? row.asset_no ?? '',
    // 资产类型
    asset_type: row.asset_type ?? row.asset_category ?? '',
    // 预计使用年限
    useful_life: row.useful_life ?? row.expected_usage_years ?? 0,
    // 残值率（后端存放的是小数 0-1，前端展示为百分比 0-100）
    residual_rate: row.residual_rate ?? (
      row.expected_residual_rate != null ? Number(row.expected_residual_rate) * 100 : 0
    ),
    // 使用部门
    department: row.department ?? row.using_department ?? '',
    // 创建时间
    create_time: row.create_time ?? row.created_time ?? '',
    // 购置日期
    purchase_date: row.purchase_date ?? '',
    // 存放地点
    location: row.location ?? ''
  }
}

// 获取固定资产列表
const getFixedAssetsList = async (params: any) => {
  try {
    fixedAssetsLoading.value = true
    const result = await erpApi.finance.getFixedAssets({
      page: params.page || fixedAssetsPage.value,
      size: params.size || fixedAssetsSize.value,
      ...params
    })
    const page = unwrapPageResponse<any>(result)
    // 对每行数据进行字段映射，确保前端展示字段命名一致
    fixedAssetsList.value = (page.list || []).map(mapBackendToFrontend)
    fixedAssetsTotal.value = page.total || page.list.length || 0
    fixedAssetsPage.value = params.page || fixedAssetsPage.value
    fixedAssetsSize.value = params.size || fixedAssetsSize.value
  } catch (error) {
    ErrorHandler.handleApiError(error)
    fixedAssetsList.value = []
    fixedAssetsTotal.value = 0
  } finally {
    fixedAssetsLoading.value = false
  }
}

// 搜索固定资产
const handleSearch = (params: any = {}) => {
  getFixedAssetsList(params)
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  fixedAssetsSize.value = size
  getFixedAssetsList({ page: fixedAssetsPage.value, size })
}

// 页码改变
const handleCurrentChange = (page: number) => {
  fixedAssetsPage.value = page
  getFixedAssetsList({ page, size: fixedAssetsSize.value })
}

// 选择行改变
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

// 资产类型映射
const assetTypeMap: Record<string, string> = {
  'building': '房屋建筑',
  'equipment': '机器设备',
  'vehicle': '运输工具',
  'electronic': '电子设备',
  'furniture': '办公家具'
}

// 状态标签类型
const getStatusType = (status: string) => {
  // 确保返回有效的type值，避免ElTag组件type属性验证失败
  return statusMap[status]?.type || 'info'
}

// 状态标签文本
const getStatusLabel = (status: string) => {
  return statusMap[status]?.label || status
}

// 获取资产类型标签
const getAssetTypeLabel = (type: string) => {
  return assetTypeMap[type] || type
}

// 格式化金额
const formatMoney = (amount: number) => {
  return new Intl.NumberFormat('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount)
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 计提折旧
const handleDepreciation = async () => {
  if (!depreciationFormRef.value) return
  
  try {
    // 如果有选中的资产，执行批量折旧
    if (selectedRows.value.length > 0) {
      const assetIds = selectedRows.value.map(row => row.id)
      await erpApi.finance.batchCalculateDepreciation(assetIds, {
        depreciationDate: depreciationForm.value.depreciation_date,
        period: depreciationForm.value.period,
        remark: depreciationForm.value.remark
      })
      ElMessage.success(`成功计提 ${selectedRows.value.length} 项资产折旧`)
    } else {
      // 否则执行自动折旧
      await erpApi.finance.autoCalculateDepreciation({
        depreciationDate: depreciationForm.value.depreciation_date,
        period: depreciationForm.value.period,
        remark: depreciationForm.value.remark
      })
      ElMessage.success('自动折旧计提成功')
    }
    // 关闭对话框
    depreciationFormVisible.value = false
    // 刷新列表
    handleSearch()
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// 资产处置
const handleDisposal = async () => {
  if (!disposalFormRef.value || !selectedAsset.value) return
  
  try {
    await erpApi.finance.disposeFixedAsset(selectedAsset.value.id, disposalForm.value)
    // 关闭对话框
    disposalFormVisible.value = false
    // 刷新列表
    handleSearch()
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// 字段反向映射：前端表单字段 -> 后端 ERP 实体字段（驼峰/SNAKE_CASE）
const mapFrontendToBackend = (form: any) => {
  if (!form) return form
  return {
    id: form.id || undefined,
    assetNo: form.asset_code,
    assetName: form.asset_name,
    assetCategory: form.asset_type,
    purchaseDate: form.purchase_date || null,
    originalValue: form.original_value ?? 0,
    accumulatedDepreciation: form.accumulated_depreciation ?? 0,
    netValue: form.net_value ?? 0,
    expectedUsageYears: form.useful_life ?? 0,
    // 前端展示为百分比，后端期望小数（0-1）
    expectedResidualRate: form.residual_rate != null ? Number(form.residual_rate) / 100 : 0,
    usingDepartment: form.department,
    location: form.location,
    status: typeof form.status === 'string' ? Number(form.status) : form.status,
    remark: form.remark
  }
}

// 保存编辑
const handleEditSave = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    
    // 转换为后端实体字段，避免字段不匹配导致后端 null 字段
    const payload = mapFrontendToBackend(editForm.value)
    
    if (selectedAsset.value && selectedAsset.value.id) {
      // 更新资产
      await erpApi.finance.updateFixedAsset(Number(selectedAsset.value.id), payload as any)
      ElMessage.success('固定资产更新成功')
    } else {
      // 创建资产
      await erpApi.finance.createFixedAsset(payload as any)
      ElMessage.success('固定资产创建成功')
    }
    
    // 关闭对话框
    editFormVisible.value = false
    // 刷新列表
    handleSearch()
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}
</script>

<style scoped lang="scss">
.fixed-assets-view {
  padding: 20px;
  
  .module-card {
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .card-header {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 20px;
      font-weight: bold;
      color: #1890ff;
    }
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .fixed-assets-view {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .fixed-assets-view {
    padding: 12px;
    
    .module-card {
      .card-header {
        font-size: 18px;
      }
    }
  }
}

// 详情对话框样式
.asset-detail {
  .detail-item {
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    
    .label {
      font-weight: bold;
      width: 120px;
      color: #606266;
    }
    
    .value {
      color: #303133;
    }
  }
  
  .full-width {
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid #ebedf0;
  }
}

// 处置表单样式
.disposal-form {
  .disposal-info {
    background-color: #f5f7fa;
    padding: 16px;
    border-radius: 4px;
    margin-bottom: 20px;
    
    .info-item {
      margin-bottom: 12px;
      display: flex;
      align-items: center;
      
      .label {
        font-weight: bold;
        width: 120px;
        color: #606266;
      }
      
      .value {
        color: #303133;
      }
    }
  }
}
</style>
