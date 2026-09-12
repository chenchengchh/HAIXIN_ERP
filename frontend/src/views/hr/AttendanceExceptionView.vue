<template>
  <div class="attendance-exception-view">
    <div class="toolbar">
      <el-button type="primary" @click="handleNewException">上报异常</el-button>
    </div>

    <!-- 搜索和筛选表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline label-width="80px">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="员工姓名">
          <el-input
            v-model="searchForm.employeeName"
            placeholder="请输入员工姓名"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-select
            v-model="searchForm.departmentId"
            placeholder="全部"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="异常类型">
          <el-select
            v-model="searchForm.exceptionType"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="忘记打卡" :value="0" />
            <el-option label="外出办公" :value="1" />
            <el-option label="设备故障" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="待处理" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="handleRefresh">刷新</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="exceptionList" border stripe>
      <el-table-column prop="employeeName" label="员工姓名" width="120" />
      <el-table-column prop="departmentName" label="部门" width="120" />
      <el-table-column prop="date" label="日期" width="120" />
      <el-table-column prop="exceptionType" label="异常类型" width="120">
        <template #default="scope">
          <el-tag :type="getExceptionTypeColor(scope.row.exceptionType)">
            {{ getExceptionTypeText(scope.row.exceptionType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="异常说明" />
      <el-table-column prop="status" label="处理状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="approverName" label="审批人" width="120" />
      <el-table-column prop="submitTime" label="提交时间" width="180" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">详情</el-button>
          <el-button 
            v-if="scope.row.status === 0" 
            link 
            type="success" 
            size="small" 
            @click="handleApprove(scope.row)">
            通过
          </el-button>
          <el-button 
            v-if="scope.row.status === 0" 
            link 
            type="danger" 
            size="small" 
            @click="handleReject(scope.row)">
            拒绝
          </el-button>
          <el-button 
            v-if="scope.row.status === 0" 
            link 
            type="danger" 
            size="small" 
            @click="handleDelete(scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <!-- 上报异常弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑异常' : '上报异常'"
      width="500px"
    >
      <el-form ref="exceptionFormRef" :model="exceptionForm" :rules="rules" label-width="120px">
        <el-form-item label="异常日期" prop="exceptionDate">
          <el-date-picker
            v-model="exceptionForm.exceptionDate"
            type="date"
            placeholder="请选择异常日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="异常类型" prop="exceptionType">
          <el-select
            v-model="exceptionForm.exceptionType"
            placeholder="请选择异常类型"
          >
            <el-option label="忘记打卡" :value="0" />
            <el-option label="外出办公" :value="1" />
            <el-option label="设备故障" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="异常说明" prop="description">
          <el-input
            v-model="exceptionForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入异常说明"
          />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            class="upload-demo"
            drag
            action="#"
            multiple
            :before-upload="handleBeforeUpload"
            :file-list="fileList"
            :on-remove="handleRemoveFile"
            :show-file-list="true"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">拖拽文件到此处或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">
                支持上传图片、PDF、Word等格式文件，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveException">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 异常详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="异常详情"
      width="500px"
    >
      <div class="detail-content" v-if="currentException">
        <el-form label-width="120px" size="small">
          <el-form-item label="员工姓名">
            {{ currentException.employeeName }}
          </el-form-item>
          <el-form-item label="部门">
            {{ currentException.departmentName }}
          </el-form-item>
          <el-form-item label="日期">
            {{ currentException.date }}
          </el-form-item>
          <el-form-item label="异常类型">
            <el-tag :type="getExceptionTypeColor(currentException.exceptionType)">
              {{ getExceptionTypeText(currentException.exceptionType) }}
            </el-tag>
          </el-form-item>
          <el-form-item label="异常说明">
            {{ currentException.description }}
          </el-form-item>
          <el-form-item label="处理状态">
            <el-tag :type="getStatusType(currentException.status)">
              {{ getStatusText(currentException.status) }}
            </el-tag>
          </el-form-item>
          <el-form-item label="审批人">
            {{ currentException.approverName || '未审批' }}
          </el-form-item>
          <el-form-item label="提交时间">
            {{ currentException.submitTime }}
          </el-form-item>
          <el-form-item label="审批备注" v-if="currentException.approvalRemark">
            {{ currentException.approvalRemark }}
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 审批弹窗 -->
    <el-dialog
      v-model="approveDialogVisible"
      :title="'审批异常 - ' + (approveAction === 'approve' ? '通过' : '拒绝')"
      width="400px"
    >
      <el-form :model="approveForm" label-width="80px">
        <el-form-item label="审批备注">
          <el-input
            v-model="approveForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入审批备注（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="approveDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirmApprove">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { unwrapListResponse, unwrapPageResponse } from '../../api'
import { hrApi, type AttendanceException, type Department, type Employee } from '../../api/hr'

// 加载状态
const loading = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 日期范围
const dateRange = ref<string[]>([])

// 部门数据
const departments = ref<Department[]>([])

// 员工数据
const employees = ref<Employee[]>([])

// 员工ID到员工信息的映射
const employeeMap = computed(() => {
  const map: Record<number, Employee> = {}
  employees.value.forEach(emp => {
    if (emp.id) {
      map[emp.id] = emp
    }
  })
  return map
})

// 部门ID到部门名称的映射
const departmentMap = computed(() => {
  const map: Record<number, string> = {}
  departments.value.forEach(dept => {
    if (dept.id) {
      map[dept.id] = dept.name
    }
  })
  return map
})

// 搜索和筛选条件
const searchForm = ref({
  employeeName: '',
  departmentId: '',
  exceptionType: '',
  status: ''
})

// 弹窗可见性
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const approveDialogVisible = ref(false)

// 是否为编辑模式
const isEdit = ref(false)

// 审批操作类型
const approveAction = ref<'approve' | 'reject'>('approve')

// 表单引用
const exceptionFormRef = ref()

// 表单数据
const exceptionForm = ref({
  exceptionDate: '',
  exceptionType: 0,
  description: '',
  evidence: ''
})

// 审批表单数据
const approveForm = ref({
  remark: ''
})

// 表单验证规则
const rules = {
  exceptionDate: [{ required: true, message: '请选择异常日期', trigger: 'change' }],
  exceptionType: [{ required: true, message: '请选择异常类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入异常说明', trigger: 'blur' }]
}

// 文件列表
const fileList = ref<any[]>([])

// 当前选中的异常
const currentException = ref<any>(null)

// 考勤异常列表
const exceptionList = ref<Array<{
  id: number;
  employeeName: string;
  departmentName: string;
  date: string;
  exceptionType: number;
  description: string;
  status: number;
  approverName: string;
  submitTime: string;
  evidence: string;
}>>([])

// 获取部门数据
const fetchDepartments = async () => {
  try {
    const res = await hrApi.department.getAll()
    departments.value = unwrapListResponse<Department>(res)
  } catch (error) {
    console.error('获取部门数据失败:', error)
    ElMessage.error('获取部门数据失败')
    // 发生错误时，确保数据是数组
    departments.value = []
  }
}

// 获取员工数据
const fetchEmployees = async () => {
  try {
    // 使用getAll方法获取所有员工，确保获取完整数据
    const res = await hrApi.employee.getAll()
    employees.value = unwrapListResponse<Employee>(res)
  } catch (error) {
    console.error('获取员工数据失败:', error)
    ElMessage.error('获取员工数据失败')
    // 发生错误时，确保数据是数组
    employees.value = []
  }
}

// 解析数组格式的日期（如 [2025, 12, 5]）
const parseDateArray = (date: any) => {
  if (!date) return ''
  if (Array.isArray(date) && date.length >= 3) {
    return new Date(date[0], date[1] - 1, date[2]).toISOString().split('T')[0]
  }
  return new Date(date).toISOString().split('T')[0]
}

// 获取异常类型颜色
const getExceptionTypeColor = (type: number) => {
  const colorMap: Record<number, string> = {
    0: 'warning', // 忘记打卡
    1: 'info',    // 外出办公
    2: 'danger',  // 设备故障
    3: 'info'     // 其他
  }
  return colorMap[type] || 'info'
}

// 获取异常类型文本
const getExceptionTypeText = (type: number) => {
  const textMap: Record<number, string> = {
    0: '忘记打卡',
    1: '外出办公',
    2: '设备故障',
    3: '其他'
  }
  return textMap[type] || '未知'
}

// 获取审批状态类型
const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning', // 待处理
    1: 'success', // 已通过
    2: 'danger'   // 已拒绝
  }
  return typeMap[status] || 'info'
}

// 获取审批状态文本
const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '待处理',
    1: '已通过',
    2: '已拒绝'
  }
  return textMap[status] || '未知'
}

// 获取考勤异常数据
const fetchData = async () => {
  loading.value = true
  try {
    // 构建搜索参数
    const searchParams: any = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    // 添加日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      searchParams.startDate = dateRange.value[0]
      searchParams.endDate = dateRange.value[1]
    }
    
    // 添加搜索条件
    if (searchForm.value.employeeName) {
      searchParams.employeeName = searchForm.value.employeeName
    }
    if (searchForm.value.departmentId) {
      searchParams.departmentId = Number(searchForm.value.departmentId)
    }
    if (searchForm.value.exceptionType !== '') {
      searchParams.exceptionType = Number(searchForm.value.exceptionType)
    }
    if (searchForm.value.status !== '') {
      searchParams.status = Number(searchForm.value.status)
    }
    
    // 调用API获取数据
    const res = await hrApi.attendance.getAttendanceExceptionsByPage(searchParams)
    const page = unwrapPageResponse<any>(res)
    const rawList = page.list
    total.value = page.total
    
    // 数据转换，从API返回的数据中获取员工姓名、部门和审批人信息
    exceptionList.value = rawList.map((item: any) => {
      // 格式化日期
      const formatDate = (date: any) => {
        if (!date) return ''
        if (typeof date === 'object' && date !== null && date.year) {
          // 处理Java LocalDate格式：{year: 2025, month: 12, day: 5}
          return `${date.year}-${String(date.month).padStart(2, '0')}-${String(date.day).padStart(2, '0')}`
        }
        const parsedDate = new Date(date)
        return isNaN(parsedDate.getTime()) ? '' : parsedDate.toISOString().split('T')[0]
      }
      
      // 格式化时间
      const formatTime = (time: any) => {
        if (!time) return ''
        if (typeof time === 'object' && time !== null && time.hour) {
          // 处理Java LocalDateTime格式：{year: 2025, month: 12, day: 5, hour: 8, minute: 50, second: 0}
          return `${String(time.hour).padStart(2, '0')}:${String(time.minute).padStart(2, '0')}:${String(time.second || 0).padStart(2, '0')}`
        }
        const parsedTime = new Date(time)
        return isNaN(parsedTime.getTime()) ? '' : parsedTime.toISOString().replace('T', ' ').substring(0, 19)
      }
      
      // 获取员工姓名和部门名称
      let employeeName = '未知'
      let departmentName = '未知'
      let approverName = '未知'
      
      // 优先从关联的employee对象中获取信息
      if (item.employee) {
        employeeName = item.employee.name || '未知'
        if (item.employee.department) {
          departmentName = item.employee.department.name || '未知'
        } else if (item.employee.departmentId) {
          // 如果employee对象中只有departmentId，从departmentMap中查找
          departmentName = departmentMap.value[item.employee.departmentId] || '未知'
        }
      } 
      // 如果直接有employeeName和departmentName字段，直接使用
      else if (item.employeeName) {
        employeeName = item.employeeName
        departmentName = item.departmentName || '未知'
      } 
      // 最后尝试通过employeeId去employeeMap中查找
      else if (item.employeeId) {
        const employee = employeeMap.value[item.employeeId]
        if (employee) {
          employeeName = employee.name
          if (employee.departmentId) {
            departmentName = departmentMap.value[employee.departmentId] || '未知'
          }
        }
      }
      
      // 获取审批人信息
      if (item.approverName) {
        approverName = item.approverName
      } else if (item.approverId) {
        // 根据approverId获取审批人信息
        const approver = employeeMap.value[item.approverId]
        if (approver) {
          approverName = approver.name
        }
      }
      
      return {
        id: Number(item.id) || 0,
        employeeName,
        departmentName,
        date: formatDate(item.exceptionDate) || '',
        exceptionType: Number(item.exceptionType) || 0,
        description: String(item.description) || '',
        status: Number(item.status) || 0,
        approverName,
        submitTime: formatTime(item.createdTime || item.createTime || item.submitTime || ''),
        evidence: String(item.evidence || '')
      }
    })
    
    total.value = page?.total ?? rawList.length
  } catch (error) {
    console.error('获取考勤异常列表失败:', error)
    ElMessage.error('获取考勤异常列表失败')
    exceptionList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取数据
onMounted(async () => {
  // 先获取部门和员工数据，再获取考勤异常数据
  await fetchDepartments()
  await fetchEmployees()
  await fetchData()
})

// 上报异常
const handleNewException = () => {
  isEdit.value = false
  // 重置表单
  exceptionForm.value = {
    exceptionDate: '',
    exceptionType: 0,
    description: '',
    evidence: ''
  }
  fileList.value = []
  dialogVisible.value = true
}

// 编辑异常
const handleEdit = (row: any) => {
  isEdit.value = true
  // 填充表单数据
  exceptionForm.value = {
    exceptionDate: row.date,
    exceptionType: row.exceptionType,
    description: row.description,
    evidence: row.evidence
  }
  fileList.value = []
  dialogVisible.value = true
}

// 查看详情
const handleViewDetail = (row: any) => {
  currentException.value = row
  detailDialogVisible.value = true
}

// 审批异常
const handleApprove = (row: any) => {
  currentException.value = row
  approveAction.value = 'approve'
  approveForm.value = { remark: '' }
  approveDialogVisible.value = true
}

// 拒绝异常
const handleReject = (row: any) => {
  currentException.value = row
  approveAction.value = 'reject'
  approveForm.value = { remark: '' }
  approveDialogVisible.value = true
}

// 确认审批
const handleConfirmApprove = async () => {
  if (!currentException.value) return
  
  try {
    const status = approveAction.value === 'approve' ? 1 : 2
    await hrApi.attendance.approveAttendanceException(
      currentException.value.id,
      status,
      1, // 假设当前用户ID为1
      approveForm.value.remark
    )
    
    ElMessage.success(approveAction.value === 'approve' ? '异常已通过' : '异常已拒绝')
    approveDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('审批异常失败:', error)
    ElMessage.error('审批异常失败')
  }
}

// 删除考勤异常
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该考勤异常吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await hrApi.attendance.deleteAttendanceException(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('删除考勤异常失败:', error)
      ElMessage.error('删除考勤异常失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

// 重置搜索条件
const handleReset = () => {
  searchForm.value = {
    employeeName: '',
    departmentId: '',
    exceptionType: '',
    status: ''
  }
  dateRange.value = []
  currentPage.value = 1
  fetchData()
}

// 刷新
const handleRefresh = () => {
  fetchData()
  ElMessage.success('数据已刷新')
}

// 保存异常
const handleSaveException = () => {
  if (!exceptionFormRef.value) return
  
  exceptionFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        // 调用API保存异常
        if (isEdit.value && currentException.value) {
          await hrApi.attendance.updateAttendanceException(currentException.value.id, exceptionForm.value as AttendanceException)
        } else {
          await hrApi.attendance.createAttendanceException(exceptionForm.value as AttendanceException)
        }
        
        dialogVisible.value = false
        ElMessage.success('保存成功')
        fetchData()
      } catch (error) {
        console.error('保存异常失败:', error)
        ElMessage.error('保存异常失败')
      }
    }
  })
}

// 附件上传前处理
const handleBeforeUpload = async (file: File) => {
  try {
    // 模拟文件上传API调用
    console.log('上传文件:', file)
    // 实际项目中应调用真实的文件上传API
    // const res = await uploadFile(file)
    // 模拟上传成功，将文件添加到fileList
    const mockFile = {
      name: file.name,
      url: URL.createObjectURL(file),
      size: file.size,
      type: file.type
    }
    fileList.value.push(mockFile)
    // 更新表单数据中的证据字段
    exceptionForm.value.evidence = fileList.value.map(file => file.name).join(';')
    ElMessage.success('文件上传成功')
  } catch (error) {
    console.error('文件上传失败:', error)
    ElMessage.error('文件上传失败')
  }
  return false
}

// 移除已上传的文件
const handleRemoveFile = (file: any) => {
  const index = fileList.value.indexOf(file)
  if (index > -1) {
    fileList.value.splice(index, 1)
    // 更新表单数据中的证据字段
    exceptionForm.value.evidence = fileList.value.map(file => file.name).join(';')
  }
}
</script>

<style scoped>
.attendance-exception-view {
  padding: 20px;
}
.toolbar {
  margin-bottom: 20px;
}
.search-form {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
.detail-content {
  margin-top: 20px;
}
</style>
