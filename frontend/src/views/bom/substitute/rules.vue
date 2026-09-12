<template>
  <bom-layout title="替代规则配置" :breadcrumb-items="[{ label: 'BOM系统' }, { label: '替代管理' }, { label: '规则配置' }]">
    <template #action-bar>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增规则
      </el-button>
    </template>

    <!-- 搜索和筛选区域 -->
    <div class="search-area">
      <el-form :model="searchForm" label-width="90px" inline>
        <el-form-item label="规则名称">
          <el-input v-model="searchForm.ruleName" placeholder="规则名称搜索" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="规则类型">
          <el-select v-model="searchForm.ruleType" placeholder="全部类型" clearable style="width: 140px">
            <el-option label="优先级策略" :value="1" />
            <el-option label="比例策略" :value="2" />
            <el-option label="生效条件" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 规则列表表格 -->
    <el-table :data="ruleList" style="width: 100%" border stripe v-loading="loading">
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="ruleName" label="规则名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="ruleType" label="规则类型" width="120">
        <template #default="scope">
          <el-tag effect="plain" :type="ruleTypeTag(scope.row.ruleType)">
            {{ ruleTypeText(scope.row.ruleType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetCategoryName" label="适用分类" width="120">
        <template #default="scope">
          {{ scope.row.targetCategoryName || '全局' }}
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="effectiveDate" label="生效日期" width="160">
        <template #default="scope">
          {{ scope.row.effectiveDate?.substring(0, 16) || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="expireDate" label="失效日期" width="160">
        <template #default="scope">
          {{ scope.row.expireDate?.substring(0, 16) || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button
            v-if="scope.row.status === 1"
            size="small"
            type="warning"
            @click="handleToggleStatus(scope.row)"
          >
            禁用
          </el-button>
          <el-button
            v-else
            size="small"
            type="success"
            @click="handleToggleStatus(scope.row)"
          >
            启用
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑规则' : '新增规则'"
      width="650px"
      @close="resetForm"
    >
      <el-form :model="ruleForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="ruleForm.ruleType" placeholder="请选择规则类型" style="width: 100%">
            <el-option label="优先级策略" :value="1" />
            <el-option label="比例策略" :value="2" />
            <el-option label="生效条件" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用分类">
          <el-select v-model="ruleForm.targetCategoryId" placeholder="全局（不选分类）" clearable style="width: 100%">
            <el-option
              v-for="cat in categoryList"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="ruleForm.priority" :min="0" :max="999" controls-position="right" />
          <span class="form-tip">数字越小越优先</span>
        </el-form-item>
        <el-form-item label="生效日期">
          <el-date-picker
            v-model="ruleForm.effectiveDate"
            type="datetime"
            placeholder="选择生效日期"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="失效日期">
          <el-date-picker
            v-model="ruleForm.expireDate"
            type="datetime"
            placeholder="选择失效日期"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="策略配置">
          <el-input
            v-model="ruleForm.strategyConfig"
            type="textarea"
            :rows="3"
            placeholder='JSON格式，如 {"preferLowCost":true,"maxAlternatives":3}'
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="ruleForm.remark" type="textarea" :rows="2" placeholder="规则说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </bom-layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus, Search, RefreshRight, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api, { unwrapPageResponse, unwrapResponseData } from '@/api'
import { bomApi } from '@/api/bom'
import BomLayout from '../../../components/BomLayout.vue'

// 替代规则列表查询的URL，用于写操作后手动清除GET缓存
// （api包装层对子资源URL的PUT/DELETE不会清除父资源列表缓存，需手动处理）
const RULES_LIST_URL = '/api/v1/bom/substitute-rules'

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  ruleName: '',
  ruleType: undefined as number | undefined,
  status: undefined as number | undefined
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 规则列表
const ruleList = ref<any[]>([])

// 分类列表（用于下拉选择）
const categoryList = ref<any[]>([])

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<any>(null)

// 表单数据
const ruleForm = reactive({
  id: undefined as number | undefined,
  ruleName: '',
  ruleType: 1,
  targetCategoryId: undefined as number | undefined,
  strategyConfig: '',
  priority: 99,
  effectiveDate: '',
  expireDate: '',
  status: 1,
  remark: ''
})

// 表单校验规则
const formRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }]
}

// 规则类型文本映射
const ruleTypeText = (type: number) => {
  const map: Record<number, string> = { 1: '优先级策略', 2: '比例策略', 3: '生效条件' }
  return map[type] || '未知'
}

// 规则类型标签颜色映射
const ruleTypeTag = (type: number) => {
  const map: Record<number, string> = { 1: 'primary', 2: 'warning', 3: 'success' }
  return map[type] || 'info'
}

// 加载规则列表
const loadRuleList = async () => {
  loading.value = true
  try {
    const res: any = await bomApi.getSubstituteRules({
      page: pagination.currentPage,
      size: pagination.pageSize,
      ruleName: searchForm.ruleName || undefined,
      ruleType: searchForm.ruleType,
      status: searchForm.status
    })
    const page = unwrapPageResponse<any>(res)
    ruleList.value = page.list || []
    pagination.total = page.total || 0
  } catch {
    ruleList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 加载分类列表（用于下拉选择，从树形结构扁平化）
const loadCategoryList = async () => {
  try {
    const res: any = await bomApi.getCategoryTree()
    const tree = unwrapResponseData<any>(res)
    // 递归扁平化分类树为一维数组
    const flatten: any[] = []
    const traverse = (nodes: any[]) => {
      if (!Array.isArray(nodes)) return
      for (const n of nodes) {
        flatten.push({ id: n.id, name: n.name, code: n.code })
        if (Array.isArray(n.children) && n.children.length > 0) {
          traverse(n.children)
        }
      }
    }
    traverse(tree)
    categoryList.value = flatten
  } catch {
    categoryList.value = []
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadRuleList()
}

// 重置
const handleReset = () => {
  searchForm.ruleName = ''
  searchForm.ruleType = undefined
  searchForm.status = undefined
  pagination.currentPage = 1
  loadRuleList()
}

// 分页变更
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  loadRuleList()
}

const handleCurrentChange = (page: number) => {
  pagination.currentPage = page
  loadRuleList()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(ruleForm, {
    id: row.id,
    ruleName: row.ruleName || '',
    ruleType: row.ruleType,
    targetCategoryId: row.targetCategoryId,
    strategyConfig: row.strategyConfig || '',
    priority: row.priority ?? 99,
    effectiveDate: row.effectiveDate || '',
    expireDate: row.expireDate || '',
    status: row.status ?? 1,
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(ruleForm, {
    id: undefined,
    ruleName: '',
    ruleType: 1,
    targetCategoryId: undefined,
    strategyConfig: '',
    priority: 99,
    effectiveDate: '',
    expireDate: '',
    status: 1,
    remark: ''
  })
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const data = { ...ruleForm }
      // 清空空字符串的日期，避免后端解析异常
      if (!data.effectiveDate) data.effectiveDate = null as any
      if (!data.expireDate) data.expireDate = null as any
      if (!data.targetCategoryId) data.targetCategoryId = null as any

      if (isEdit.value && data.id) {
        await bomApi.updateSubstituteRule(data.id, data)
        // PUT /substitute-rules/{id} 不会自动清除列表GET缓存，需手动清除
        api.clearCache(RULES_LIST_URL)
        ElMessage.success('规则更新成功')
      } else {
        delete data.id
        await bomApi.createSubstituteRule(data)
        ElMessage.success('规则创建成功')
      }
      dialogVisible.value = false
      loadRuleList()
    } catch {
      ElMessage.error(isEdit.value ? '规则更新失败' : '规则创建失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 启用/禁用切换
const handleToggleStatus = (row: any) => {
  const action = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${action}该规则吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      if (row.status === 1) {
        await bomApi.disableSubstituteRule(row.id)
      } else {
        await bomApi.enableSubstituteRule(row.id)
      }
      // PUT /substitute-rules/{id}/disable|enable 不会自动清除列表GET缓存，需手动清除
      api.clearCache(RULES_LIST_URL)
      ElMessage.success(`规则已${action}`)
      loadRuleList()
    } catch {
      ElMessage.error(`${action}失败`)
    }
  }).catch(() => {})
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该规则吗？删除后不可恢复。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await bomApi.deleteSubstituteRule(row.id)
      // DELETE /substitute-rules/{id} 不会自动清除列表GET缓存，需手动清除
      api.clearCache(RULES_LIST_URL)
      ElMessage.success('规则已删除')
      loadRuleList()
    } catch {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 初始化
onMounted(() => {
  loadRuleList()
  loadCategoryList()
})
</script>

<style scoped>
.search-area {
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
