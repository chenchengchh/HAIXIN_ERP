<template>
  <div class="leave-management-view">
    <div class="toolbar">
      <el-button type="primary" @click="handleNewLeave">申请请假</el-button>
      <el-button @click="handleRefresh">刷新</el-button>
    </div>

    <!-- 搜索和筛选表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline label-width="80px">
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
        <el-form-item label="请假类型">
          <el-select
            v-model="searchForm.leaveType"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="年假" :value="0" />
            <el-option label="病假" :value="1" />
            <el-option label="事假" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="待审批" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="leaveList" border stripe>
      <el-table-column prop="employeeName" label="员工姓名" width="120" />
      <el-table-column prop="departmentName" label="部门" width="120" />
      <el-table-column prop="leaveType" label="请假类型" width="100">
        <template #default="scope">
          <el-tag :type="getLeaveTypeColor(scope.row.leaveType)">
            {{ getLeaveTypeText(scope.row.leaveType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="开始日期" width="120" />
      <el-table-column prop="endDate" label="结束日期" width="120" />
      <el-table-column prop="duration" label="请假天数" width="100" />
      <el-table-column prop="reason" label="请假原因" />
      <el-table-column prop="status" label="审批状态" width="100">
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
            type="info" 
            size="small" 
            @click="handleEdit(scope.row)"
          >
            编辑
          </el-button>
          <el-button 
            v-if="scope.row.status === 0" 
            link 
            type="warning" 
            size="small" 
            @click="handleRevoke(scope.row)"
          >
            撤回
          </el-button>
          <el-button 
            v-if="scope.row.status === 0" 
            link 
            type="danger" 
            size="small" 
            @click="handleDelete(scope.row)"
          >
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



    <!-- 请假申请表单弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑请假申请' : '申请请假'"
      width="500px"
    >
      <el-form ref="leaveFormRef" :model="leaveForm" :rules="rules" label-width="120px">
        <el-form-item label="请假类型" prop="leaveType">
          <el-select
            v-model="leaveForm.leaveType"
            placeholder="请选择请假类型"
          >
            <el-option label="年假" :value="0" />
            <el-option label="病假" :value="1" />
            <el-option label="事假" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="calculateDuration"
          />
        </el-form-item>
        <el-form-item label="请假天数" prop="duration">
          <el-input-number
            v-model="leaveForm.duration"
            :min="0.5"
            :step="0.5"
            placeholder="自动计算"
            readonly
          />
        </el-form-item>
        <el-form-item label="请假原因" prop="reason">
          <el-input
            v-model="leaveForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入请假原因"
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
          <el-button type="primary" @click="handleSaveLeave">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 请假详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="请假详情"
      width="500px"
    >
      <div class="detail-content" v-if="currentLeave">
        <el-form label-width="120px" size="small">
          <el-form-item label="员工姓名">
            {{ currentLeave.value.employeeName }}
          </el-form-item>
          <el-form-item label="部门">
            {{ currentLeave.value.departmentName }}
          </el-form-item>
          <el-form-item label="请假类型">
            <el-tag :type="getLeaveTypeColor(currentLeave.value.leaveType)">
              {{ getLeaveTypeText(currentLeave.value.leaveType) }}
            </el-tag>
          </el-form-item>
          <el-form-item label="开始日期">
            {{ currentLeave.value.startDate }}
          </el-form-item>
          <el-form-item label="结束日期">
            {{ currentLeave.value.endDate }}
          </el-form-item>
          <el-form-item label="请假天数">
            {{ currentLeave.value.duration }} 天
          </el-form-item>
          <el-form-item label="请假原因">
            {{ currentLeave.value.reason }}
          </el-form-item>
          <el-form-item label="审批状态">
            <el-tag :type="getStatusType(currentLeave.value.status)">
              {{ getStatusText(currentLeave.value.status) }}
            </el-tag>
          </el-form-item>
          <el-form-item label="审批人">
            {{ currentLeave.value.approverName || '未指定' }}
          </el-form-item>
          <el-form-item label="提交时间">
            {{ currentLeave.value.submitTime || '未提交' }}
          </el-form-item>
          <el-form-item label="审批时间" v-if="currentLeave.value.approveTime">
            {{ currentLeave.value.approveTime }}
          </el-form-item>
          <el-form-item label="审批备注" v-if="currentLeave.value.approveRemark">
            {{ currentLeave.value.approveRemark }}
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { unwrapListResponse, unwrapPageResponse } from '../../api'
import { hrApi } from '../../api/hr'
import type { LeaveRequest, Department, Employee } from '../../api/hr'

// 加载状态
const loading = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 请假列表数据
const leaveList = ref<Array<{
  id: number;
  employeeName: string;
  departmentName: string;
  leaveType: number;
  startDate: string;
  endDate: string;
  duration: number;
  reason: string;
  status: number;
  approverName: string;
  submitTime: string;
  approveTime?: string;
  approveRemark?: string;
}>>([])

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
  leaveType: '',
  status: ''
})

// 弹窗可见性
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)

// 是否为编辑模式
const isEdit = ref(false)

// 表单引用
const leaveFormRef = ref()

// 表单数据
const leaveForm = ref({
  leaveType: 0,
  startDate: '',
  endDate: '',
  duration: 0,
  reason: '',
  evidence: '',
  status: 0
})

// 日期范围
const dateRange = ref<string[]>([])

// 当前选中的请假申请
const currentLeave = ref<any>(null)

// 文件列表
const fileList = ref<any[]>([])

// 表单验证规则
const rules = {
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  duration: [{ required: true, message: '请输入请假天数', trigger: 'change' }],
  reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
}



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

// 获取请假类型颜色
const getLeaveTypeColor = (type: number) => {
  const colorMap: Record<number, string> = {
    0: 'success', // 年假
    1: 'warning', // 病假
    2: 'info',    // 事假
    3: 'info'     // 其他
  }
  return colorMap[type] || 'info'
}

// 获取请假类型文本
const getLeaveTypeText = (type: number) => {
  const textMap: Record<number, string> = {
    0: '年假',
    1: '病假',
    2: '事假',
    3: '其他'
  }
  return textMap[type] || '未知'
}

// 获取审批状态类型
const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning', // 待审批
    1: 'success', // 已通过
    2: 'danger'   // 已拒绝
  }
  return typeMap[status] || 'info'
}

// 获取审批状态文本
const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '待审批',
    1: '已通过',
    2: '已拒绝'
  }
  return textMap[status] || '未知'
}

// 计算请假天数
const calculateDuration = () => {
  if (dateRange.value && dateRange.value.length === 2 && dateRange.value[0] && dateRange.value[1]) {
    const start = new Date(dateRange.value[0])
    const end = new Date(dateRange.value[1])
    const diffTime = Math.abs(end.getTime() - start.getTime())
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1 // +1 包含起始日期
    leaveForm.value.startDate = dateRange.value[0]
    leaveForm.value.endDate = dateRange.value[1]
    leaveForm.value.duration = diffDays
  }
}





// 获取请假列表数据
const fetchData = async () => {
  loading.value = true
  try {
    // 构建搜索参数
    const searchParams: any = {}
    
    // 添加搜索条件
    if (searchForm.value.employeeName) {
      searchParams.employeeName = searchForm.value.employeeName
    }
    if (searchForm.value.departmentId) {
      searchParams.departmentId = searchForm.value.departmentId
    }
    if (searchForm.value.leaveType !== '') {
      searchParams.leaveType = Number(searchForm.value.leaveType)
    }
    if (searchForm.value.status !== '') {
      searchParams.status = Number(searchForm.value.status)
    }
    
    // 调用API获取数据
    const res = await hrApi.attendance.getLeaveRequestsByPage({
      page: currentPage.value,
      size: pageSize.value,
      ...searchParams
    })
    const page = unwrapPageResponse<any>(res)
    const rawList = page.list
    total.value = page.total
    
    // 数据转换，从API返回的数据中获取员工姓名、部门和审批人信息
    leaveList.value = rawList.map((item: any) => {
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
        leaveType: Number(item.leaveType) || 0,
        startDate: formatDate(item.startDate) || '',
        endDate: formatDate(item.endDate) || '',
        duration: Number(item.duration) || 0,
        reason: String(item.reason) || '',
        status: Number(item.status) || 0,
        approverName,
        submitTime: formatTime(item.createdTime || item.createTime || item.submitTime || ''),
        approveTime: item.approveTime ? formatTime(item.approveTime) : undefined,
        approveRemark: item.approveRemark ? String(item.approveRemark) : undefined
      }
    })
    
    total.value = page?.total ?? rawList.length
  } catch (error) {
    console.error('获取请假列表失败:', error)
    ElMessage.error('获取请假列表失败')
    leaveList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 申请请假
const handleNewLeave = () => {
  isEdit.value = false
  // 重置表单
  leaveForm.value = {
    leaveType: 0,
    startDate: '',
    endDate: '',
    duration: 0,
    reason: '',
    evidence: '',
    status: 0
  }
  dateRange.value = []
  fileList.value = []
  dialogVisible.value = true
}

// 编辑请假申请
const handleEdit = (row: any) => {
  isEdit.value = true
  currentLeave.value = row
  // 填充表单数据
  leaveForm.value = {
    leaveType: row.leaveType,
    startDate: row.startDate,
    endDate: row.endDate,
    duration: row.duration,
    reason: row.reason,
    evidence: row.evidence || '',
    status: row.status
  }
  // 设置日期范围
  dateRange.value = [row.startDate, row.endDate]
  // 重置文件列表
  fileList.value = []
  // 如果有证据文件，模拟填充文件列表
  if (row.evidence) {
    const fileNames = row.evidence.split(';')
    fileNames.forEach((name: string) => {
      if (name) {
        fileList.value.push({
          name: name,
          url: '#',
          size: 0,
          type: ''
        })
      }
    })
  }
  dialogVisible.value = true
}

// 保存请假申请
const handleSaveLeave = () => {
  if (!leaveFormRef.value) return
  
  leaveFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {

        
        // 添加员工ID（模拟当前登录员工）
        const leaveRequestData = {
          ...leaveForm.value,
          employeeId: 1 // 模拟当前登录员工ID
        } as LeaveRequest
        
        // 调用API保存请假申请
        if (isEdit.value && currentLeave.value) {
          await hrApi.attendance.updateLeaveRequest(currentLeave.value.id, leaveRequestData)
        } else {
          await hrApi.attendance.createLeaveRequest(leaveRequestData)
        }
        
        dialogVisible.value = false
        // 刷新数据，确保用户看到最新的数据
        await fetchData()
        ElMessage.success('保存成功')
      } catch (error) {
        console.error('保存请假申请失败:', error)
        ElMessage.error('保存请假申请失败')
      }
    }
  })
}

// 刷新数据
const handleRefresh = () => {
  fetchData()
  ElMessage.success('数据已刷新')
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
    leaveType: '',
    status: ''
  }
  currentPage.value = 1
  fetchData()
}

// 查看详情
const handleViewDetail = (row: any) => {
  currentLeave.value = row
  detailDialogVisible.value = true
}

// 撤回申请
const handleRevoke = (row: any) => {
  ElMessageBox.confirm('确认撤回该请假申请吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      // 调用API撤回申请
      // await revokeLeaveRequest(row.id)
      // 刷新数据，确保用户看到最新的数据
      await fetchData()
      ElMessage.success('撤回成功')
    } catch (error) {
      console.error('撤回申请失败:', error)
      ElMessage.error('撤回申请失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 删除申请
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该请假申请吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await hrApi.attendance.deleteLeaveRequest(row.id)
      // 刷新数据，确保用户看到最新的数据
      await fetchData()
      ElMessage.success('删除成功')
    } catch (error) {
      console.error('删除申请失败:', error)
      ElMessage.error('删除申请失败')
    }
  }).catch(() => {
    // 取消操作
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
    leaveForm.value.evidence = fileList.value.map(file => file.name).join(';')
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
    leaveForm.value.evidence = fileList.value.map(file => file.name).join(';')
  }
}



// 组件挂载时获取数据
onMounted(async () => {
  // 先获取部门和员工数据，再获取请假数据
  await fetchDepartments()
  await fetchEmployees()
  await fetchData()
})
</script>

<style scoped>
.leave-management-view {
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
.detail-content {
  margin-top: 20px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
