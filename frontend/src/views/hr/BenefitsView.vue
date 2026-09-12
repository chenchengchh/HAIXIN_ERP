<template>
  <div class="benefits-view">
    <SubModuleHeader title="福利管理" parentTitle="HR 人力资源" />
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 福利配置 -->
      <el-tab-pane label="福利配置" name="config">
        <div class="toolbar">
          <el-button type="primary" @click="handleAddBenefit">新增福利</el-button>
        </div>

        <el-table v-loading="loading" :data="benefitConfigList" border stripe>
          <el-table-column prop="benefitName" label="福利名称" width="150" />
          <el-table-column prop="benefitType" label="福利类型" width="120">
            <template #default="scope">
              <el-tag :type="getBenefitTypeTag(scope.row.benefitType)">
                {{ getBenefitTypeText(scope.row.benefitType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="standardAmount" label="标准金额" width="120">
            <template #default="scope">
              <span style="color: #67c23a; font-weight: bold;">¥{{ scope.row.standardAmount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="frequency" label="发放频率" width="100">
            <template #default="scope">{{ getFrequencyText(scope.row.frequency) }}</template>
          </el-table-column>
          <el-table-column prop="description" label="说明" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleConfigStatusChange(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button link type="success" size="small" @click="handleDistribute(scope.row)">发放</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 福利发放 -->
      <el-tab-pane label="福利发放" name="distribution">
        <div class="toolbar">
          <el-form inline>
            <el-form-item label="福利类型">
              <el-select v-model="selectedBenefitType" placeholder="全部" clearable style="width: 150px">
                <el-option label="全部" value="" />
                <el-option label="节日福利" :value="0" />
                <el-option label="生日福利" :value="1" />
                <el-option label="健康体检" :value="2" />
                <el-option label="补贴" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQuery">查询</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table v-loading="loading" :data="filteredRecords" border stripe>
          <el-table-column label="福利名称" width="150">
            <template #default="scope">{{ getBenefitName(scope.row.benefitId) }}</template>
          </el-table-column>
          <el-table-column label="员工姓名" width="120">
            <template #default="scope">{{ getEmployeeName(scope.row.employeeId) }}</template>
          </el-table-column>
          <el-table-column prop="distributeDate" label="发放日期" width="120" />
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="scope">
              <span style="color: #67c23a; font-weight: bold;">¥{{ scope.row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
                {{ scope.row.status === 1 ? '已发放' : '待发放' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button
                v-if="scope.row.status === 0"
                link
                type="success"
                size="small"
                @click="handleConfirmDistribute(scope.row)"
              >
                确认发放
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 员工福利记录 -->
      <el-tab-pane label="员工福利记录" name="records">
        <div class="toolbar">
          <el-form inline>
            <el-form-item label="员工">
              <el-input v-model="searchEmployee" placeholder="输入员工姓名" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearchEmployee">查询</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table v-loading="loading" :data="employeeRecordsList" border stripe>
          <el-table-column label="员工姓名" width="120">
            <template #default="scope">{{ getEmployeeName(scope.row.employeeId) }}</template>
          </el-table-column>
          <el-table-column label="部门" width="120">
            <template #default="scope">{{ getDepartmentName(scope.row.employeeId) }}</template>
          </el-table-column>
          <el-table-column prop="totalBenefits" label="累计福利次数" width="130" />
          <el-table-column prop="totalAmount" label="累计金额" width="120">
            <template #default="scope">
              <span style="color: #67c23a; font-weight: bold;">¥{{ scope.row.totalAmount.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="latestBenefit" label="最近福利" width="150" />
          <el-table-column prop="latestDate" label="最近发放日期" width="130" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleViewHistory(scope.row)">查看历史</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 福利配置新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑福利' : '新增福利'"
      width="500px"
    >
      <el-form ref="benefitFormRef" :model="benefitForm" :rules="rules" label-width="100px">
        <el-form-item label="福利名称" prop="benefitName">
          <el-input v-model="benefitForm.benefitName" placeholder="请输入福利名称" />
        </el-form-item>
        <el-form-item label="福利类型" prop="benefitType">
          <el-select v-model="benefitForm.benefitType" placeholder="请选择福利类型" style="width: 100%">
            <el-option label="节日福利" :value="0" />
            <el-option label="生日福利" :value="1" />
            <el-option label="健康体检" :value="2" />
            <el-option label="补贴" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="标准金额" prop="standardAmount">
          <el-input-number v-model="benefitForm.standardAmount" :min="0" :precision="2" placeholder="请输入标准金额" style="width: 100%" />
        </el-form-item>
        <el-form-item label="发放频率" prop="frequency">
          <el-select v-model="benefitForm.frequency" placeholder="请选择发放频率" style="width: 100%">
            <el-option label="一次性" :value="0" />
            <el-option label="每月" :value="1" />
            <el-option label="每年" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input
            v-model="benefitForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入说明" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="benefitForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 员工福利历史弹窗 -->
    <el-dialog
      v-model="historyDialogVisible"
      :title="`福利历史 - ${getEmployeeName(historyEmployeeId)}`"
      width="600px"
    >
      <el-table :data="historyRecords" border stripe>
        <el-table-column label="福利名称" width="150">
          <template #default="scope">{{ getBenefitName(scope.row.benefitId) }}</template>
        </el-table-column>
        <el-table-column prop="distributeDate" label="发放日期" width="120" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="scope">
            <span style="color: #67c23a;">¥{{ scope.row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '已发放' : '待发放' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="historyDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { hrApi } from '../../api/hr'
import type { BenefitConfig, BenefitRecord, Department, Employee } from '../../api/hr'
import SubModuleHeader from '../../components/common/SubModuleHeader.vue'

// 当前激活的Tab页
const activeTab = ref('config')
// 加载状态
const loading = ref(false)
// 福利发放区选中的福利类型
const selectedBenefitType = ref<number | ''>('')
// 福利发放区实际生效的福利类型查询条件
const appliedBenefitType = ref<number | ''>('')
// 员工福利记录区搜索的员工姓名
const searchEmployee = ref('')
// 员工福利记录区实际生效的员工姓名查询条件
const appliedEmployeeName = ref('')

// 福利配置列表
const benefitConfigList = ref<BenefitConfig[]>([])
// 福利发放记录列表
const benefitRecords = ref<BenefitRecord[]>([])
// 员工列表（用于姓名与部门映射）
const employees = ref<Employee[]>([])
// 部门列表（用于部门名称映射）
const departments = ref<Department[]>([])

// 福利配置弹窗可见性
const dialogVisible = ref(false)
// 是否为编辑模式
const isEdit = ref(false)
// 当前编辑的福利配置ID
const editingConfigId = ref<number | null>(null)
// 福利配置表单引用
const benefitFormRef = ref()
// 福利配置表单数据
const benefitForm = ref<BenefitConfig>({
  benefitName: '',
  benefitType: 0,
  standardAmount: 0,
  frequency: 0,
  status: 1,
  description: ''
})

// 福利历史弹窗可见性
const historyDialogVisible = ref(false)
// 当前查看历史的员工ID
const historyEmployeeId = ref<number | null>(null)

// 福利配置表单校验规则
const rules = {
  benefitName: [{ required: true, message: '请输入福利名称', trigger: 'blur' }],
  benefitType: [{ required: true, message: '请选择福利类型', trigger: 'change' }],
  standardAmount: [{ required: true, message: '请输入标准金额', trigger: 'blur' }],
  frequency: [{ required: true, message: '请选择发放频率', trigger: 'change' }]
}

// 福利配置ID到配置信息的映射
const configMap = computed(() => {
  const map: Record<number, BenefitConfig> = {}
  benefitConfigList.value.forEach(config => {
    if (config.id !== undefined) {
      map[config.id] = config
    }
  })
  return map
})

// 员工ID到员工信息的映射
const employeeMap = computed(() => {
  const map: Record<number, Employee> = {}
  employees.value.forEach(emp => {
    if (emp.id !== undefined) {
      map[emp.id] = emp
    }
  })
  return map
})

// 部门ID到部门名称的映射
const departmentMap = computed(() => {
  const map: Record<number, string> = {}
  departments.value.forEach(dept => {
    if (dept.id !== undefined) {
      map[dept.id] = dept.name
    }
  })
  return map
})

/**
 * 根据福利配置ID获取福利名称
 * @param benefitId 福利配置ID
 */
const getBenefitName = (benefitId: number) => {
  return configMap.value[benefitId]?.benefitName ?? '-'
}

/**
 * 根据员工ID获取员工姓名
 * @param employeeId 员工ID
 */
const getEmployeeName = (employeeId: number | null) => {
  if (employeeId === null || employeeId === undefined) return '-'
  return employeeMap.value[employeeId]?.name ?? '-'
}

/**
 * 根据员工ID获取所属部门名称
 * @param employeeId 员工ID
 */
const getDepartmentName = (employeeId: number) => {
  const emp = employeeMap.value[employeeId]
  if (!emp) return '-'
  return departmentMap.value[emp.departmentId] ?? '-'
}

/**
 * 获取福利类型文本
 * @param type 福利类型（0节日福利/1生日福利/2健康体检/3补贴）
 */
const getBenefitTypeText = (type: number) => {
  const textMap: Record<number, string> = {
    0: '节日福利',
    1: '生日福利',
    2: '健康体检',
    3: '补贴'
  }
  return textMap[type] || '未知'
}

/**
 * 获取福利类型对应的标签类型
 * @param type 福利类型（0节日福利/1生日福利/2健康体检/3补贴）
 */
const getBenefitTypeTag = (type: number) => {
  const tagMap: Record<number, string> = {
    0: 'success',
    1: 'warning',
    2: 'primary',
    3: 'info'
  }
  return tagMap[type] || 'info'
}

/**
 * 获取发放频率文本
 * @param frequency 发放频率（0一次性/1每月/2每年）
 */
const getFrequencyText = (frequency: number) => {
  const textMap: Record<number, string> = {
    0: '一次性',
    1: '每月',
    2: '每年'
  }
  return textMap[frequency] || '未知'
}

/**
 * 过滤后的发放记录（按福利类型前端过滤）
 */
const filteredRecords = computed(() => {
  if (appliedBenefitType.value === '') return benefitRecords.value
  return benefitRecords.value.filter(record => {
    const config = configMap.value[record.benefitId]
    return config?.benefitType === appliedBenefitType.value
  })
})

/**
 * 员工福利汇总记录（按员工分组统计次数、金额与最近发放）
 */
const employeeRecordsList = computed(() => {
  const grouped: Record<number, {
    employeeId: number
    totalBenefits: number
    totalAmount: number
    latestBenefit: string
    latestDate: string
  }> = {}
  benefitRecords.value.forEach(record => {
    if (!grouped[record.employeeId]) {
      grouped[record.employeeId] = {
        employeeId: record.employeeId,
        totalBenefits: 0,
        totalAmount: 0,
        latestBenefit: '-',
        latestDate: ''
      }
    }
    const item = grouped[record.employeeId]!
    item.totalBenefits += 1
    item.totalAmount += Number(record.amount) || 0
    // 记录最近一次发放的福利与日期
    if (!item.latestDate || String(record.distributeDate) > item.latestDate) {
      item.latestDate = String(record.distributeDate)
      item.latestBenefit = getBenefitName(record.benefitId)
    }
  })
  return Object.values(grouped).filter(item => {
    if (!appliedEmployeeName.value) return true
    return getEmployeeName(item.employeeId).includes(appliedEmployeeName.value)
  })
})

/**
 * 当前查看历史员工的发放记录列表
 */
const historyRecords = computed(() => {
  if (historyEmployeeId.value === null) return []
  return benefitRecords.value.filter(record => record.employeeId === historyEmployeeId.value)
})

/**
 * 加载福利配置列表
 */
const fetchConfigs = async () => {
  try {
    const res = await hrApi.benefit.config.getAll()
    if (res && res.data) {
      benefitConfigList.value = Array.isArray(res.data) ? res.data : (res.data.records ?? res.data.list ?? [])
    }
  } catch (error) {
    console.error('获取福利配置失败:', error)
    ElMessage.error('获取福利配置失败')
    benefitConfigList.value = []
  }
}

/**
 * 加载福利发放记录列表
 */
const fetchRecords = async () => {
  try {
    const res = await hrApi.benefit.record.getByPage({ page: 1, size: 100 })
    if (res && res.data) {
      benefitRecords.value = Array.isArray(res.data) ? res.data : (res.data.records ?? res.data.list ?? [])
    }
  } catch (error) {
    console.error('获取福利发放记录失败:', error)
    ElMessage.error('获取福利发放记录失败')
    benefitRecords.value = []
  }
}

/**
 * 加载员工列表
 */
const fetchEmployees = async () => {
  try {
    const res = await hrApi.employee.getAll()
    if (res && res.data) {
      employees.value = Array.isArray(res.data) ? res.data : (res.data.records ?? res.data.list ?? [])
    }
  } catch (error) {
    console.error('获取员工列表失败:', error)
    ElMessage.error('获取员工列表失败')
    employees.value = []
  }
}

/**
 * 加载部门列表
 */
const fetchDepartments = async () => {
  try {
    const res = await hrApi.department.getAll()
    if (res && res.data) {
      departments.value = Array.isArray(res.data) ? res.data : (res.data.records ?? res.data.list ?? [])
    }
  } catch (error) {
    console.error('获取部门列表失败:', error)
    ElMessage.error('获取部门列表失败')
    departments.value = []
  }
}

/**
 * 重置福利配置表单
 */
const resetForm = () => {
  benefitForm.value = {
    benefitName: '',
    benefitType: 0,
    standardAmount: 0,
    frequency: 0,
    status: 1,
    description: ''
  }
  if (benefitFormRef.value) {
    benefitFormRef.value.resetFields()
  }
}

/**
 * 提交福利配置（新增或更新）
 */
const handleSubmit = () => {
  if (!benefitFormRef.value) return
  benefitFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      if (isEdit.value && editingConfigId.value !== null) {
        await hrApi.benefit.config.update(editingConfigId.value, benefitForm.value)
        ElMessage.success('福利更新成功')
      } else {
        await hrApi.benefit.config.create(benefitForm.value)
        ElMessage.success('福利新增成功')
      }
      dialogVisible.value = false
      await fetchConfigs()
    } catch (error) {
      console.error('保存福利配置失败:', error)
      ElMessage.error('保存福利配置失败')
    }
  })
}

/**
 * 打开新增福利弹窗
 */
const handleAddBenefit = () => {
  isEdit.value = false
  editingConfigId.value = null
  resetForm()
  dialogVisible.value = true
}

/**
 * 打开编辑福利弹窗
 * @param row 当前福利配置行数据
 */
const handleEdit = (row: BenefitConfig) => {
  isEdit.value = true
  editingConfigId.value = row.id ?? null
  benefitForm.value = { ...row }
  dialogVisible.value = true
}

/**
 * 删除福利配置
 * @param row 当前福利配置行数据
 */
const handleDelete = (row: BenefitConfig) => {
  ElMessageBox.confirm(`确定要删除福利 "${row.benefitName}" 吗？`, '删除确认', {
    type: 'warning'
  }).then(async () => {
    try {
      await hrApi.benefit.config.delete(row.id!)
      ElMessage.success('福利删除成功')
      await fetchConfigs()
    } catch (error) {
      console.error('删除福利配置失败:', error)
      ElMessage.error('删除福利配置失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

/**
 * 福利配置状态切换（启用/停用）
 * @param row 当前福利配置行数据
 */
const handleConfigStatusChange = async (row: BenefitConfig) => {
  try {
    await hrApi.benefit.config.update(row.id!, { status: row.status })
    ElMessage.success(row.status === 1 ? '福利已启用' : '福利已停用')
  } catch (error) {
    console.error('更新福利状态失败:', error)
    ElMessage.error('更新福利状态失败')
    // 更新失败时回滚状态
    row.status = row.status === 1 ? 0 : 1
  }
}

/**
 * 发放福利：按配置生成发放记录
 * @param row 当前福利配置行数据
 */
const handleDistribute = (row: BenefitConfig) => {
  ElMessageBox.confirm(`确认为"${row.benefitName}"生成发放记录吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await hrApi.benefit.record.distribute(row.id!)
      const payload = res?.data?.data
      const count = typeof payload === 'number' ? payload : (payload?.count ?? payload?.total ?? '')
      ElMessage.success(count !== '' ? `发放成功，生成 ${count} 条记录` : '发放成功')
      await fetchRecords()
    } catch (error) {
      console.error('发放福利失败:', error)
      ElMessage.error('发放福利失败')
    }
  }).catch(() => {
    // 取消发放
  })
}

/**
 * 确认发放（仅待发放记录可操作）
 * @param row 当前发放记录行数据
 */
const handleConfirmDistribute = async (row: BenefitRecord) => {
  try {
    await hrApi.benefit.record.update(row.id!, { status: 1 })
    ElMessage.success('发放成功')
    await fetchRecords()
  } catch (error) {
    console.error('确认发放失败:', error)
    ElMessage.error('确认发放失败')
  }
}

/**
 * 福利发放区查询（应用福利类型过滤）
 */
const handleQuery = () => {
  appliedBenefitType.value = selectedBenefitType.value
}

/**
 * 员工福利记录区查询（应用员工姓名过滤）
 */
const handleSearchEmployee = () => {
  appliedEmployeeName.value = searchEmployee.value
}

/**
 * 查看员工福利历史
 * @param row 员工福利汇总行数据
 */
const handleViewHistory = (row: { employeeId: number }) => {
  historyEmployeeId.value = row.employeeId
  historyDialogVisible.value = true
}

// 组件挂载时加载福利配置、发放记录、员工与部门数据
onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([fetchConfigs(), fetchRecords(), fetchEmployees(), fetchDepartments()])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.benefits-view {
  padding: 20px;
}
.toolbar {
  margin-bottom: 20px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
