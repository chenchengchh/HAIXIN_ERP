<template>
  <div class="attendance-rules-view">
    <div class="toolbar">
      <el-button type="primary" @click="handleAddRule">新增规则</el-button>
    </div>

    <el-table v-loading="loading" :data="rulesList" border stripe>
      <el-table-column prop="ruleName" label="规则名称" width="200" />
      <el-table-column prop="workStartTime" label="上班时间" width="100" />
      <el-table-column prop="workEndTime" label="下班时间" width="100" />
      <el-table-column prop="lateTolerance" label="迟到容忍(分钟)" width="130" />
      <el-table-column prop="earlyLeaveTolerance" label="早退容忍(分钟)" width="130" />
      <el-table-column label="适用部门">
        <template #default="scope">
          <el-tag
            v-for="dept in splitDepartments(scope.row.applicableDepartments)"
            :key="dept"
            size="small"
            style="margin-right: 4px"
          >
            {{ dept }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑规则弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑考勤规则' : '新增考勤规则'"
      width="500px"
    >
      <el-form ref="ruleFormRef" :model="ruleForm" :rules="rules" label-width="120px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="上班时间" prop="workStartTime">
          <el-time-picker
            v-model="ruleForm.workStartTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="请选择上班时间"
          />
        </el-form-item>
        <el-form-item label="下班时间" prop="workEndTime">
          <el-time-picker
            v-model="ruleForm.workEndTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="请选择下班时间"
          />
        </el-form-item>
        <el-form-item label="迟到容忍(分钟)" prop="lateTolerance">
          <el-input-number
            v-model="ruleForm.lateTolerance"
            :min="0"
            :max="60"
            placeholder="请输入迟到容忍时间"
          />
        </el-form-item>
        <el-form-item label="早退容忍(分钟)" prop="earlyLeaveTolerance">
          <el-input-number
            v-model="ruleForm.earlyLeaveTolerance"
            :min="0"
            :max="60"
            placeholder="请输入早退容忍时间"
          />
        </el-form-item>
        <el-form-item label="适用部门" prop="applicableDepartments">
          <el-select
            v-model="ruleForm.applicableDepartments"
            multiple
            collapse-tags
            placeholder="请选择适用部门"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="ruleForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveRule">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { hrApi } from '../../api/hr'
import type { AttendanceRule, Department } from '../../api/hr'

// 加载状态
const loading = ref(false)

// 部门数据
const departments = ref<Department[]>([])

// 考勤规则列表
const rulesList = ref<AttendanceRule[]>([])

// 弹窗可见性
const dialogVisible = ref(false)

// 是否为编辑模式
const isEdit = ref(false)

// 当前编辑的规则ID
const editingRuleId = ref<number | null>(null)

// 表单引用
const ruleFormRef = ref()

// 表单数据（适用部门为名称数组，提交时转换为逗号分隔字符串）
const ruleForm = ref({
  ruleName: '',
  workStartTime: '',
  workEndTime: '',
  lateTolerance: 0,
  earlyLeaveTolerance: 0,
  applicableDepartments: [] as string[],
  status: 1
})

// 表单验证规则
const rules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  workStartTime: [{ required: true, message: '请选择上班时间', trigger: 'change' }],
  workEndTime: [{ required: true, message: '请选择下班时间', trigger: 'change' }],
  applicableDepartments: [{ required: true, type: 'array', min: 1, message: '请选择适用部门', trigger: 'change' }]
}

/**
 * 将逗号分隔的适用部门字符串拆分为数组
 * @param departmentsStr 逗号分隔的部门名称字符串
 */
const splitDepartments = (departmentsStr: string) => {
  if (!departmentsStr) return []
  return departmentsStr.split(',').filter(item => item.trim() !== '')
}

/**
 * 将后端时间格式统一为HH:mm（兼容HH:mm:ss）
 * @param time 时间字符串
 */
const formatTime = (time: string) => {
  if (!time) return ''
  return time.slice(0, 5)
}

/**
 * 获取考勤规则列表
 */
const fetchRules = async () => {
  loading.value = true
  try {
    const res = await hrApi.attendanceRule.getAll()
    if (res && res.data) {
      rulesList.value = Array.isArray(res.data) ? res.data : (res.data.records ?? res.data.list ?? [])
    }
  } catch (error) {
    console.error('获取考勤规则失败:', error)
    ElMessage.error('获取考勤规则失败')
    rulesList.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 获取部门数据
 */
const fetchDepartments = async () => {
  try {
    const res = await hrApi.department.getAll()
    if (res && res.data) {
      // 确保departments.value始终是数组
      departments.value = Array.isArray(res.data) ? res.data : (res.data.records ?? res.data.list ?? [])
    }
  } catch (error) {
    console.error('获取部门数据失败:', error)
    ElMessage.error('获取部门数据失败')
    departments.value = []
  }
}

/**
 * 新增规则
 */
const handleAddRule = () => {
  isEdit.value = false
  editingRuleId.value = null
  // 重置表单
  ruleForm.value = {
    ruleName: '',
    workStartTime: '',
    workEndTime: '',
    lateTolerance: 0,
    earlyLeaveTolerance: 0,
    applicableDepartments: [],
    status: 1
  }
  dialogVisible.value = true
}

/**
 * 编辑规则
 * @param row 当前考勤规则行数据
 */
const handleEdit = (row: AttendanceRule) => {
  isEdit.value = true
  editingRuleId.value = row.id ?? null
  // 填充表单数据（适用部门字符串拆分为数组，时间统一为HH:mm）
  ruleForm.value = {
    ruleName: row.ruleName,
    workStartTime: formatTime(row.workStartTime),
    workEndTime: formatTime(row.workEndTime),
    lateTolerance: row.lateTolerance,
    earlyLeaveTolerance: row.earlyLeaveTolerance,
    applicableDepartments: splitDepartments(row.applicableDepartments),
    status: row.status
  }
  dialogVisible.value = true
}

/**
 * 删除规则
 * @param row 当前考勤规则行数据
 */
const handleDelete = (row: AttendanceRule) => {
  ElMessageBox.confirm('确认删除该考勤规则吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await hrApi.attendanceRule.delete(row.id!)
      ElMessage.success('删除成功')
      await fetchRules()
    } catch (error) {
      console.error('删除规则失败:', error)
      ElMessage.error('删除规则失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

/**
 * 规则状态切换（启用/禁用）
 * @param row 当前考勤规则行数据
 */
const handleStatusChange = async (row: AttendanceRule) => {
  try {
    await hrApi.attendanceRule.update(row.id!, { status: row.status })
    ElMessage.success(row.status === 1 ? '规则已启用' : '规则已禁用')
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
    // 更新失败时回滚状态
    row.status = row.status === 1 ? 0 : 1
  }
}

/**
 * 保存规则（新增或更新）
 */
const handleSaveRule = async () => {
  if (!ruleFormRef.value) return

  await ruleFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      // 适用部门数组转换为逗号分隔字符串后提交
      const submitData = {
        ruleName: ruleForm.value.ruleName,
        workStartTime: ruleForm.value.workStartTime,
        workEndTime: ruleForm.value.workEndTime,
        lateTolerance: ruleForm.value.lateTolerance,
        earlyLeaveTolerance: ruleForm.value.earlyLeaveTolerance,
        applicableDepartments: ruleForm.value.applicableDepartments.join(','),
        status: ruleForm.value.status
      }
      if (isEdit.value && editingRuleId.value !== null) {
        await hrApi.attendanceRule.update(editingRuleId.value, submitData)
      } else {
        await hrApi.attendanceRule.create(submitData)
      }
      dialogVisible.value = false
      ElMessage.success('保存成功')
      await fetchRules()
    } catch (error) {
      console.error('保存规则失败:', error)
      ElMessage.error('保存规则失败')
    }
  })
}

// 组件挂载时获取考勤规则和部门数据
onMounted(() => {
  fetchRules()
  fetchDepartments()
})
</script>

<style scoped>
.attendance-rules-view {
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
