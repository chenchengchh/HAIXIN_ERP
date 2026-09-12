<template>
  <div class="employee-lifecycle-view">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 入职管理 -->
      <el-tab-pane label="入职管理" name="onboarding">
        <div class="toolbar">
          <el-button type="primary" @click="handleNewOnboarding">新增入职</el-button>
        </div>
        <el-table :data="onboardingList" border stripe v-loading="loading.employees" empty-text="暂无入职管理数据">
          <el-table-column prop="employeeName" label="员工姓名" width="120" />
          <el-table-column prop="departmentName" label="部门" width="150" />
          <el-table-column prop="positionName" label="岗位" width="150" />
          <el-table-column prop="onboardingDate" label="入职日期" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">查看</el-button>
              <el-button link type="success" size="small" @click="handleApprove(scope.row)">转正审批</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页组件 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.onboarding.currentPage"
            v-model:page-size="pagination.onboarding.pageSize"
            :page-sizes="pageSizes"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.onboarding.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-tab-pane>

      <!-- 转岗管理 -->
      <el-tab-pane label="转岗管理" name="transfer">
        <div class="toolbar">
          <el-button type="primary" @click="handleNewTransfer">新增转岗</el-button>
        </div>
        <div class="search-form">
          <el-form inline>
            <el-form-item label="员工姓名">
              <el-input
                v-model="searchForm.transfer.employeeName"
                placeholder="请输入员工姓名"
                clearable
                style="width: 180px"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchForm.transfer.status" placeholder="全部" clearable style="width: 120px">
                <el-option label="待审批" :value="0" />
                <el-option label="已通过" :value="1" />
                <el-option label="已拒绝" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch('transfer')">查询</el-button>
              <el-button @click="handleReset('transfer')">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table :data="transferList" border stripe v-loading="loading.transfer" empty-text="暂无转岗记录">
          <el-table-column prop="employeeName" label="员工姓名" width="120" />
          <el-table-column prop="fromDepartment" label="原部门" width="150" />
          <el-table-column prop="toDepartment" label="新部门" width="150" />
          <el-table-column prop="fromPosition" label="原岗位" width="150" />
          <el-table-column prop="toPosition" label="新岗位" width="150" />
          <el-table-column prop="transferDate" label="转岗日期" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ scope.row.status === 0 ? '待审批' : scope.row.status === 1 ? '已通过' : '已拒绝' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">查看</el-button>
              <el-button link type="success" size="small" @click="handleApprove(scope.row)">审批</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页组件 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.transfer.currentPage"
            v-model:page-size="pagination.transfer.pageSize"
            :page-sizes="pageSizes"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.transfer.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-tab-pane>

      <!-- 调薪管理 -->
      <el-tab-pane label="调薪管理" name="adjustment">
        <div class="toolbar">
          <el-button type="primary" @click="handleNewAdjustment">新增调薪</el-button>
        </div>
        <div class="search-form">
          <el-form inline>
            <el-form-item label="员工姓名">
              <el-input
                v-model="searchForm.adjustment.employeeName"
                placeholder="请输入员工姓名"
                clearable
                style="width: 180px"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchForm.adjustment.status" placeholder="全部" clearable style="width: 120px">
                <el-option label="待审批" :value="0" />
                <el-option label="已通过" :value="1" />
                <el-option label="已拒绝" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch('adjustment')">查询</el-button>
              <el-button @click="handleReset('adjustment')">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table :data="adjustmentList" border stripe v-loading="loading.adjustment" empty-text="暂无调薪记录">
          <el-table-column prop="employeeName" label="员工姓名" width="120" />
          <el-table-column prop="departmentName" label="部门" width="150" />
          <el-table-column prop="currentSalary" label="当前薪资" width="120" />
          <el-table-column prop="newSalary" label="调整后薪资" width="120" />
          <el-table-column prop="adjustmentReason" label="调薪原因" />
          <el-table-column prop="effectiveDate" label="生效日期" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ scope.row.status === 0 ? '待审批' : scope.row.status === 1 ? '已通过' : '已拒绝' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">查看</el-button>
              <el-button link type="success" size="small" @click="handleApprove(scope.row)">审批</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页组件 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.adjustment.currentPage"
            v-model:page-size="pagination.adjustment.pageSize"
            :page-sizes="pageSizes"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.adjustment.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-tab-pane>

      <!-- 离职管理 -->
      <el-tab-pane label="离职管理" name="resignation">
        <div class="toolbar">
          <el-button type="primary" @click="handleNewResignation">新增离职申请</el-button>
        </div>
        <div class="search-form">
          <el-form inline>
            <el-form-item label="员工姓名">
              <el-input
                v-model="searchForm.resignation.employeeName"
                placeholder="请输入员工姓名"
                clearable
                style="width: 180px"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchForm.resignation.status" placeholder="全部" clearable style="width: 120px">
                <el-option label="待审批" :value="0" />
                <el-option label="已通过" :value="1" />
                <el-option label="已拒绝" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch('resignation')">查询</el-button>
              <el-button @click="handleReset('resignation')">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table :data="resignationList" border stripe v-loading="loading.resignation" empty-text="暂无离职申请">
          <el-table-column prop="employeeName" label="员工姓名" width="120" />
          <el-table-column prop="departmentName" label="部门" width="150" />
          <el-table-column prop="positionName" label="岗位" width="150" />
          <el-table-column prop="resignationType" label="离职类型" width="120">
            <template #default="scope">
              {{ getResignationTypeText(scope.row.resignationType) }}
            </template>
          </el-table-column>
          <el-table-column prop="resignationDate" label="离职日期" width="120" />
          <el-table-column prop="reason" label="离职原因" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">查看</el-button>
              <el-button link type="success" size="small" @click="handleApprove(scope.row)">审批</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页组件 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.resignation.currentPage"
            v-model:page-size="pagination.resignation.pageSize"
            :page-sizes="pageSizes"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.resignation.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 员工详情对话框 -->
    <el-dialog
      v-model="dialogVisible.detail"
      title="员工详情"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="员工编号">{{ detailData.employeeNo }}</el-descriptions-item>
        <el-descriptions-item label="员工姓名">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ detailData.gender === 0 ? '男' : detailData.gender === 1 ? '女' : '其他' }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ detailData.birthDate }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ detailData.idCard }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailData.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailData.email }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ departmentMap[detailData.departmentId] }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ positionMap[detailData.positionId] }}</el-descriptions-item>
        <el-descriptions-item label="入职日期">{{ detailData.hireDate }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="getStatusType(detailData.status)">{{ getStatusText(detailData.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="备注">{{ detailData.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 新增员工对话框 -->
    <el-dialog
      v-model="dialogVisible.newEmployee"
      title="新增入职员工"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="员工编号" prop="employeeNo">
          <el-input v-model="form.employeeNo" placeholder="请输入员工编号" />
        </el-form-item>
        <el-form-item label="员工姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入员工姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio value="0">男</el-radio>
            <el-radio value="1">女</el-radio>
            <el-radio value="2">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker
            v-model="form.birthDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择出生日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="入职日期" prop="hireDate">
          <el-date-picker
            v-model="form.hireDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择入职日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门" prop="departmentId">
          <el-select v-model="form.departmentId" placeholder="请选择部门">
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位" prop="positionId">
          <el-select v-model="form.positionId" placeholder="请选择岗位">
            <el-option
              v-for="pos in positions"
              :key="pos.id"
              :label="pos.name"
              :value="pos.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible.newEmployee = false">取消</el-button>
          <el-button type="primary" @click="handleSaveEmployee">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="dialogVisible.approve"
      title="审批"
      width="400px"
    >
      <el-form :model="approveData" label-width="80px">
        <el-form-item label="审批结果">
          <el-radio-group v-model="approveData.status">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批备注">
          <el-input v-model="approveData.remark" type="textarea" placeholder="请输入审批备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible.approve = false">取消</el-button>
          <el-button type="primary" @click="handleSaveApprove">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 转岗表单对话框 -->
    <el-dialog
      v-model="dialogVisible.transfer"
      title="新增转岗"
      width="600px"
    >
      <el-form
        ref="transferFormRef"
        :model="transferForm"
        :rules="transferRules"
        label-width="120px"
      >
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="transferForm.employeeId" placeholder="请选择员工">
            <el-option
              v-for="emp in employees"
              :key="emp.id"
              :label="emp.name"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="原部门" prop="oldDepartmentId">
          <el-select v-model="transferForm.oldDepartmentId" placeholder="请选择原部门">
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="新部门" prop="newDepartmentId">
          <el-select v-model="transferForm.newDepartmentId" placeholder="请选择新部门">
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="原岗位" prop="oldPositionId">
          <el-select v-model="transferForm.oldPositionId" placeholder="请选择原岗位">
            <el-option
              v-for="pos in positions"
              :key="pos.id"
              :label="pos.name"
              :value="pos.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="新岗位" prop="newPositionId">
          <el-select v-model="transferForm.newPositionId" placeholder="请选择新岗位">
            <el-option
              v-for="pos in positions"
              :key="pos.id"
              :label="pos.name"
              :value="pos.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="转岗日期" prop="transferDate">
          <el-date-picker
            v-model="transferForm.transferDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择转岗日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="转岗原因" prop="reason">
          <el-input v-model="transferForm.reason" type="textarea" placeholder="请输入转岗原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible.transfer = false">取消</el-button>
          <el-button type="primary" @click="handleSaveTransfer">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 调薪表单对话框 -->
    <el-dialog
      v-model="dialogVisible.adjustment"
      title="新增调薪"
      width="600px"
    >
      <el-form
        ref="adjustmentFormRef"
        :model="adjustmentForm"
        :rules="adjustmentRules"
        label-width="120px"
      >
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="adjustmentForm.employeeId" placeholder="请选择员工">
            <el-option
              v-for="emp in employees"
              :key="emp.id"
              :label="emp.name"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="当前薪资" prop="oldSalary">
          <el-input v-model.number="adjustmentForm.oldSalary" placeholder="请输入当前薪资" />
        </el-form-item>
        <el-form-item label="调整后薪资" prop="newSalary">
          <el-input v-model.number="adjustmentForm.newSalary" placeholder="请输入调整后薪资" />
        </el-form-item>
        <el-form-item label="调薪原因" prop="reason">
          <el-input v-model="adjustmentForm.reason" type="textarea" placeholder="请输入调薪原因" />
        </el-form-item>
        <el-form-item label="生效日期" prop="adjustmentDate">
          <el-date-picker
            v-model="adjustmentForm.adjustmentDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择生效日期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible.adjustment = false">取消</el-button>
          <el-button type="primary" @click="handleSaveAdjustment">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 离职表单对话框 -->
    <el-dialog
      v-model="dialogVisible.resignation"
      title="新增离职"
      width="600px"
    >
      <el-form
        ref="resignationFormRef"
        :model="resignationForm"
        :rules="resignationRules"
        label-width="120px"
      >
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="resignationForm.employeeId" placeholder="请选择员工">
            <el-option
              v-for="emp in employees"
              :key="emp.id"
              :label="emp.name"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="离职类型" prop="resignationType">
          <el-select v-model="resignationForm.resignationType" placeholder="请选择离职类型">
            <el-option label="主动离职" :value="0" />
            <el-option label="被动离职" :value="1" />
            <el-option label="协商离职" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="离职日期" prop="applyDate">
          <el-date-picker
            v-model="resignationForm.applyDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择离职日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="离职原因" prop="reason">
          <el-input v-model="resignationForm.reason" type="textarea" placeholder="请输入离职原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible.resignation = false">取消</el-button>
          <el-button type="primary" @click="handleSaveResignation">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { AxiosError } from 'axios'
import { hrApi } from '../../api/hr'
import { handleListResponse, handlePageResponse } from '../../utils/response-handler'
import type { Employee, Department, Position, TransferRecord, SalaryAdjustment, ResignationRequest } from '../../api/hr'

const getEmployees = hrApi.employee.getAll
const getDepartments = hrApi.department.getAll
const getPositions = hrApi.position.getAll
const getPayrollRecords = hrApi.payroll.getPayrollRecords
const createEmployee = hrApi.employee.create
const getEmployeeById = hrApi.employee.getById
const updateEmployee = hrApi.employee.update
const getTransferRecords = hrApi.lifecycle.transfer.getAll
const getTransferRecordsByPage = hrApi.lifecycle.transfer.getByPage
const getSalaryAdjustments = hrApi.lifecycle.salaryAdjustment.getAll
const getSalaryAdjustmentsByPage = hrApi.lifecycle.salaryAdjustment.getByPage
const getResignationRequests = hrApi.lifecycle.resignation.getAll
const getResignationRequestsByPage = hrApi.lifecycle.resignation.getByPage
const approveTransferRecord = hrApi.lifecycle.transfer.approve
const approveSalaryAdjustment = hrApi.lifecycle.salaryAdjustment.approve
const approveResignationRequest = hrApi.lifecycle.resignation.approve
const createTransferRecord = hrApi.lifecycle.transfer.create
const createSalaryAdjustment = hrApi.lifecycle.salaryAdjustment.create
const createResignationRequest = hrApi.lifecycle.resignation.create

const activeTab = ref('onboarding')

// 数据存储
const employees = ref<Employee[]>([])
const departments = ref<Department[]>([])
const positions = ref<Position[]>([])
const payrollRecords = ref<any[]>([])

// 员工生命周期数据
const transferRecords = ref<TransferRecord[]>([])
const salaryAdjustments = ref<SalaryAdjustment[]>([])
const resignationRequests = ref<ResignationRequest[]>([])

// 加载状态
const loading = ref({
  employees: false,
  departments: false,
  positions: false,
  payroll: false,
  transfer: false,
  adjustment: false,
  resignation: false
})

// 部门映射
const departmentMap = ref<Record<number, string>>({
})

// 岗位映射
const positionMap = ref<Record<number, string>>({
})

// 分页相关数据
const pagination = ref({
  onboarding: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  transfer: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  adjustment: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  resignation: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  }
})

// 每页条数选项
const pageSizes = [10, 20, 50, 100]

const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning', // 试用期/待审批
    1: 'success', // 正式/已通过
    2: 'danger',  // 离职/已拒绝
    3: 'info'     // 请假/其他
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '待审批',
    1: '已通过',
    2: '已拒绝',
    3: '请假'
  }
  return textMap[status] || '未知'
}

/**
 * 离职类型数字编码转中文（0:主动离职 1:被动离职 2:协商离职）
 */
const getResignationTypeText = (type: number | string) => {
  const textMap: Record<number, string> = {
    0: '主动离职',
    1: '被动离职',
    2: '协商离职'
  }
  return textMap[Number(type)] ?? String(type)
}

// 获取员工数据
const fetchEmployees = async () => {
  loading.value.employees = true
  try {
    const res = await getEmployees()
    console.log('获取员工数据响应:', res)
    
    // 使用统一响应处理器处理响应
    const result = handleListResponse(res.data)
    
    if (result.success) {
      employees.value = result.list as Employee[]
      console.log('处理后员工数据:', employees.value)
    } else {
      // 抛出错误，让catch块处理
      throw new Error(`获取员工数据失败: ${result.message}`)
    }
  } catch (error: any) {
    console.error('获取员工数据失败详情:', error)
    // 统一错误处理，区分Axios错误和业务错误
    let errorMessage = '获取员工数据失败'
    if (error.isAxiosError) {
      const axiosError = error as AxiosError
      if (axiosError.response) {
        // 服务器返回错误响应
        errorMessage += `: ${axiosError.response.status} - ${axiosError.response.statusText}`
      } else if (axiosError.request) {
        // 请求发送但没有收到响应
        errorMessage += ': 服务器无响应，请检查网络连接'
      } else {
        // 请求配置错误
        errorMessage += `: ${axiosError.message}`
      }
    } else {
      // 业务错误
      errorMessage += `: ${error.message || '未知错误'}`
    }
    ElMessage.error(errorMessage)
    // 发生错误时，确保数据是数组
    employees.value = []
  } finally {
    loading.value.employees = false
  }
}

// 获取部门数据
const fetchDepartments = async () => {
  loading.value.departments = true
  try {
    const res = await getDepartments()
    console.log('获取部门数据响应:', res)
    
    // 使用统一响应处理器处理响应
    const result = handleListResponse(res.data)
    
    if (result.success) {
      departments.value = result.list as Department[]
      console.log('处理后部门数据:', departments.value)
      
      // 构建部门映射
      departmentMap.value = departments.value
        .filter(dept => dept !== null && dept !== undefined)
        .reduce((map, dept) => {
          map[dept.id as number] = dept.name
          return map
        }, {} as Record<number, string>)
    } else {
      // 抛出错误，让catch块处理
      throw new Error(`获取部门数据失败: ${result.message}`)
    }
  } catch (error: any) {
    console.error('获取部门数据失败详情:', error)
    // 统一错误处理，区分Axios错误和业务错误
    let errorMessage = '获取部门数据失败'
    if (error.isAxiosError) {
      const axiosError = error as AxiosError
      if (axiosError.response) {
        // 服务器返回错误响应
        errorMessage += `: ${axiosError.response.status} - ${axiosError.response.statusText}`
      } else if (axiosError.request) {
        // 请求发送但没有收到响应
        errorMessage += ': 服务器无响应，请检查网络连接'
      } else {
        // 请求配置错误
        errorMessage += `: ${axiosError.message}`
      }
    } else {
      // 业务错误
      errorMessage += `: ${error.message || '未知错误'}`
    }
    ElMessage.error(errorMessage)
    // 发生错误时，确保数据是数组
    departments.value = []
    departmentMap.value = {}
  } finally {
    loading.value.departments = false
  }
}

// 获取岗位数据
const fetchPositions = async () => {
  loading.value.positions = true
  try {
    const res = await getPositions()
    console.log('获取岗位数据响应:', res)
    
    // 使用统一响应处理器处理响应
    const result = handleListResponse(res.data)
    
    if (result.success) {
      positions.value = result.list as Position[]
      console.log('处理后岗位数据:', positions.value)
      
      // 构建岗位映射
      positionMap.value = positions.value
        .filter(pos => pos !== null && pos !== undefined)
        .reduce((map, pos) => {
          map[pos.id as number] = pos.name
          return map
        }, {} as Record<number, string>)
    } else {
      // 抛出错误，让catch块处理
      throw new Error(`获取岗位数据失败: ${result.message}`)
    }
  } catch (error: any) {
    console.error('获取岗位数据失败详情:', error)
    // 统一错误处理，区分Axios错误和业务错误
    let errorMessage = '获取岗位数据失败'
    if (error.isAxiosError) {
      const axiosError = error as AxiosError
      if (axiosError.response) {
        // 服务器返回错误响应
        errorMessage += `: ${axiosError.response.status} - ${axiosError.response.statusText}`
      } else if (axiosError.request) {
        // 请求发送但没有收到响应
        errorMessage += ': 服务器无响应，请检查网络连接'
      } else {
        // 请求配置错误
        errorMessage += `: ${axiosError.message}`
      }
    } else {
      // 业务错误
      errorMessage += `: ${error.message || '未知错误'}`
    }
    ElMessage.error(errorMessage)
    // 发生错误时，确保数据是数组
    positions.value = []
    positionMap.value = {}
  } finally {
    loading.value.positions = false
  }
}

// 获取薪资数据
const fetchPayrollRecords = async () => {
  loading.value.payroll = true
  try {
    // 获取所有员工的薪资记录
    const res = await getPayrollRecords(null, '2025-01', '2025-12')
    // 使用统一响应处理器处理响应
    const result = handleListResponse(res.data)
    if (result.success) {
      payrollRecords.value = result.list
    } else {
      throw new Error(`获取薪资数据失败: ${result.message}`)
    }
  } catch (error: any) {
    console.error('获取薪资数据失败:', error)
    // 统一错误处理，区分Axios错误和业务错误
    if (error.isAxiosError) {
      const axiosError = error as AxiosError
      let errorMessage = '获取薪资数据失败'
      if (axiosError.response) {
        errorMessage += `: ${axiosError.response.status} - ${axiosError.response.statusText}`
      } else if (axiosError.request) {
        errorMessage += ': 服务器无响应，请检查网络连接'
      } else {
        errorMessage += `: ${axiosError.message}`
      }
      // 不显示错误，因为这可能是可选功能
      console.error(errorMessage)
    } else {
      console.error(`获取薪资数据失败: ${error.message || '未知错误'}`)
    }
    // 发生错误时，确保数据是数组
    payrollRecords.value = []
  } finally {
    loading.value.payroll = false
  }
}

// 获取转岗记录
const fetchTransferRecords = async () => {
  loading.value.transfer = true
  try {
    const res = await getTransferRecordsByPage({
      page: pagination.value.transfer.currentPage,
      size: pagination.value.transfer.pageSize,
      employeeName: searchForm.value.transfer.employeeName,
      status: searchForm.value.transfer.status ? Number(searchForm.value.transfer.status) : undefined // 字符串转数字
    })
    // 使用统一响应处理器处理响应
    const result = handlePageResponse(res.data)
    if (result.success) {
      transferRecords.value = result.list as TransferRecord[]
      pagination.value.transfer.total = result.total
    } else {
      throw new Error(`获取转岗记录失败: ${result.message}`)
    }
  } catch (error: any) {
    console.error('获取转岗记录失败:', error)
    // 统一错误处理，区分Axios错误和业务错误
    let errorMessage = '获取转岗记录失败'
    if (error.isAxiosError) {
      const axiosError = error as AxiosError
      if (axiosError.response) {
        errorMessage += `: ${axiosError.response.status} - ${axiosError.response.statusText}`
      } else if (axiosError.request) {
        errorMessage += ': 服务器无响应，请检查网络连接'
      } else {
        errorMessage += `: ${axiosError.message}`
      }
    } else {
      errorMessage += `: ${error.message || '未知错误'}`
    }
    ElMessage.error(errorMessage)
    transferRecords.value = []
    pagination.value.transfer.total = 0
  } finally {
    loading.value.transfer = false
  }
}

// 获取调薪记录
const fetchSalaryAdjustments = async () => {
  loading.value.adjustment = true
  try {
    const res = await getSalaryAdjustmentsByPage({
      page: pagination.value.adjustment.currentPage,
      size: pagination.value.adjustment.pageSize,
      employeeName: searchForm.value.adjustment.employeeName,
      status: searchForm.value.adjustment.status ? Number(searchForm.value.adjustment.status) : undefined // 字符串转数字
    })
    // 使用统一响应处理器处理响应
    const result = handlePageResponse(res.data)
    if (result.success) {
      salaryAdjustments.value = result.list as SalaryAdjustment[]
      pagination.value.adjustment.total = result.total
    } else {
      throw new Error(`获取调薪记录失败: ${result.message}`)
    }
  } catch (error: any) {
    console.error('获取调薪记录失败:', error)
    // 统一错误处理，区分Axios错误和业务错误
    let errorMessage = '获取调薪记录失败'
    if (error.isAxiosError) {
      const axiosError = error as AxiosError
      if (axiosError.response) {
        errorMessage += `: ${axiosError.response.status} - ${axiosError.response.statusText}`
      } else if (axiosError.request) {
        errorMessage += ': 服务器无响应，请检查网络连接'
      } else {
        errorMessage += `: ${axiosError.message}`
      }
    } else {
      errorMessage += `: ${error.message || '未知错误'}`
    }
    ElMessage.error(errorMessage)
    salaryAdjustments.value = []
    pagination.value.adjustment.total = 0
  } finally {
    loading.value.adjustment = false
  }
}

// 获取离职申请
const fetchResignationRequests = async () => {
  loading.value.resignation = true
  try {
    const res = await getResignationRequestsByPage({
      page: pagination.value.resignation.currentPage,
      size: pagination.value.resignation.pageSize,
      employeeName: searchForm.value.resignation.employeeName,
      status: searchForm.value.resignation.status ? Number(searchForm.value.resignation.status) : undefined, // 字符串转数字
      resignationType: searchForm.value.resignation.resignationType || undefined
    })
    // 使用统一响应处理器处理响应
    const result = handlePageResponse(res.data)
    if (result.success) {
      resignationRequests.value = result.list as ResignationRequest[]
      pagination.value.resignation.total = result.total
    } else {
      throw new Error(`获取离职申请失败: ${result.message}`)
    }
  } catch (error: any) {
    console.error('获取离职申请失败:', error)
    // 统一错误处理，区分Axios错误和业务错误
    let errorMessage = '获取离职申请失败'
    if (error.isAxiosError) {
      const axiosError = error as AxiosError
      if (axiosError.response) {
        errorMessage += `: ${axiosError.response.status} - ${axiosError.response.statusText}`
      } else if (axiosError.request) {
        errorMessage += ': 服务器无响应，请检查网络连接'
      } else {
        errorMessage += `: ${axiosError.message}`
      }
    } else {
      errorMessage += `: ${error.message || '未知错误'}`
    }
    ElMessage.error(errorMessage)
    resignationRequests.value = []
    pagination.value.resignation.total = 0
  } finally {
    loading.value.resignation = false
  }
}



// 初始化数据
const initData = async () => {
  // 使用Promise.allSettled替代Promise.all，确保部分数据请求失败不影响整体功能
  await Promise.allSettled([
    fetchEmployees(),
    fetchDepartments(),
    fetchPositions(),
    fetchPayrollRecords(),
    fetchTransferRecords(),
    fetchSalaryAdjustments(),
    fetchResignationRequests()
  ])
}

// 计算属性：入职管理列表（试用期员工）
const onboardingList = computed(() => {
  const filtered = employees.value.filter(emp => emp && emp.status === 0) // 试用期员工，过滤掉null值
  const mapped = filtered.map(emp => ({
    id: emp.id,
    employeeName: emp.name,
    departmentName: departmentMap.value[emp.departmentId] || '未知部门',
    positionName: positionMap.value[emp.positionId] || '未知岗位',
    onboardingDate: emp.hireDate,
    status: 0, // 试用期员工显示为待审批状态
    remark: emp.remark || ''
  }))
  
  // 更新总数
  pagination.value.onboarding.total = mapped.length
  
  // 分页处理
  const start = (pagination.value.onboarding.currentPage - 1) * pagination.value.onboarding.pageSize
  const end = start + pagination.value.onboarding.pageSize
  return mapped.slice(start, end)
})

// 计算属性：转岗管理列表
const transferList = computed(() => {
  // 实际应用中，使用真实的转岗记录数据
  const mapped = transferRecords.value.map(record => {
    const employee = employees.value.find(emp => emp.id === record.employeeId)
    // 即使员工数据不存在，也显示转岗记录，使用默认值
    return {
      id: record.id,
      employeeId: record.employeeId,
      employeeName: employee?.name || `未知员工(${record.employeeId})`,
      fromDepartment: departmentMap.value[record.oldDepartmentId] || '未知部门',
      toDepartment: departmentMap.value[record.newDepartmentId] || '未知部门',
      fromPosition: positionMap.value[record.oldPositionId] || '未知岗位',
      toPosition: positionMap.value[record.newPositionId] || '未知岗位',
      transferDate: record.transferDate,
      reason: record.reason,
      status: record.status
    }
  })
  
  // 更新总数
  pagination.value.transfer.total = mapped.length
  
  // 分页处理
  const start = (pagination.value.transfer.currentPage - 1) * pagination.value.transfer.pageSize
  const end = start + pagination.value.transfer.pageSize
  return mapped.slice(start, end)
})

// 计算属性：调薪管理列表
const adjustmentList = computed(() => {
  // 实际应用中，使用真实的调薪记录数据
  const mapped = salaryAdjustments.value.map(record => {
    const employee = employees.value.find(emp => emp.id === record.employeeId)
    // 即使员工数据不存在，也显示调薪记录，使用默认值
    return {
      id: record.id,
      employeeId: record.employeeId,
      employeeName: employee?.name || `未知员工(${record.employeeId})`,
      departmentName: employee ? (departmentMap.value[employee.departmentId] || '未知部门') : '未知部门',
      currentSalary: (record.oldSalary ?? 0).toString(),
      newSalary: (record.newSalary ?? 0).toString(),
      adjustmentReason: record.reason,
      effectiveDate: record.adjustmentDate,
      status: record.status
    }
  })
  
  // 更新总数
  pagination.value.adjustment.total = mapped.length
  
  // 分页处理
  const start = (pagination.value.adjustment.currentPage - 1) * pagination.value.adjustment.pageSize
  const end = start + pagination.value.adjustment.pageSize
  return mapped.slice(start, end)
})

// 计算属性：离职管理列表
const resignationList = computed(() => {
  // 实际应用中，使用真实的离职申请数据
  const mapped = resignationRequests.value.map(record => {
    const employee = employees.value.find(emp => emp.id === record.employeeId)
    // 即使员工数据不存在，也显示离职记录，使用默认值
    return {
      id: record.id,
      employeeId: record.employeeId,
      employeeName: employee?.name || `未知员工(${record.employeeId})`,
      departmentName: employee ? (departmentMap.value[employee.departmentId] || '未知部门') : '未知部门',
      positionName: employee ? (positionMap.value[employee.positionId] || '未知岗位') : '未知岗位',
      resignationType: record.resignationType,
      resignationDate: record.applyDate,
      reason: record.reason,
      status: record.status
    }
  })
  
  // 更新总数
  pagination.value.resignation.total = mapped.length
  
  // 分页处理
  const start = (pagination.value.resignation.currentPage - 1) * pagination.value.resignation.pageSize
  const end = start + pagination.value.resignation.pageSize
  return mapped.slice(start, end)
})

// 对话框控制
const dialogVisible = ref({
  detail: false,
  newEmployee: false,
  approve: false,
  transfer: false,
  adjustment: false,
  resignation: false
})

// 详情数据
const detailData = ref<any>({ })

// 表单数据
const form = ref<Employee>({
  employeeNo: '',
  name: '',
  gender: 0,
  birthDate: '',
  hireDate: new Date().toISOString().split('T')[0] || '',
  departmentId: 0,
  positionId: 0,
  status: 0
})

// 搜索表单数据
const searchForm = ref({
  transfer: {
    employeeName: '',
    status: ''
  },
  adjustment: {
    employeeName: '',
    status: ''
  },
  resignation: {
    employeeName: '',
    status: '',
    resignationType: ''
  }
})

// 审批数据
const approveData = ref({
  id: 0,
  status: 1,
  remark: ''
})

// 转岗表单数据
const transferForm = ref({
  employeeId: 0,
  oldDepartmentId: 0,
  newDepartmentId: 0,
  oldPositionId: 0,
  newPositionId: 0,
  transferDate: new Date().toISOString().split('T')[0] || '',
  reason: ''
})

// 转岗表单验证规则
const transferRules = {
  employeeId: [
    { required: true, message: '请选择员工', trigger: 'change' }
  ],
  oldDepartmentId: [
    { required: true, message: '请选择原部门', trigger: 'change' }
  ],
  newDepartmentId: [
    { required: true, message: '请选择新部门', trigger: 'change' }
  ],
  oldPositionId: [
    { required: true, message: '请选择原岗位', trigger: 'change' }
  ],
  newPositionId: [
    { required: true, message: '请选择新岗位', trigger: 'change' }
  ],
  transferDate: [
    { required: true, message: '请选择转岗日期', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入转岗原因', trigger: 'blur' }
  ]
}

// 转岗表单引用
const transferFormRef = ref<any>(null)

// 调薪表单数据
const adjustmentForm = ref({
  employeeId: 0,
  oldSalary: 0,
  newSalary: 0,
  reason: '',
  adjustmentDate: new Date().toISOString().split('T')[0] || ''
})

// 调薪表单验证规则
const adjustmentRules = {
  employeeId: [
    { required: true, message: '请选择员工', trigger: 'change' }
  ],
  oldSalary: [
    { required: true, message: '请输入当前薪资', trigger: 'blur' },
    { type: 'number', min: 0, message: '当前薪资必须大于0', trigger: 'blur' }
  ],
  newSalary: [
    { required: true, message: '请输入调整后薪资', trigger: 'blur' },
    { type: 'number', min: 0, message: '调整后薪资必须大于0', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入调薪原因', trigger: 'blur' }
  ],
  adjustmentDate: [
    { required: true, message: '请选择生效日期', trigger: 'change' }
  ]
}

// 调薪表单引用
const adjustmentFormRef = ref<any>(null)

// 离职表单数据
const resignationForm = ref({
  employeeId: 0,
  resignationType: 0,
  applyDate: new Date().toISOString().split('T')[0] || '',
  reason: ''
})

// 离职表单验证规则
const resignationRules = {
  employeeId: [
    { required: true, message: '请选择员工', trigger: 'change' }
  ],
  resignationType: [
    { required: true, message: '请选择离职类型', trigger: 'change' }
  ],
  applyDate: [
    { required: true, message: '请选择离职日期', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入离职原因', trigger: 'blur' }
  ]
}

// 离职表单引用
const resignationFormRef = ref<any>(null)

// 新增入职
const handleNewOnboarding = () => {
  // 重置表单
  form.value = {
    employeeNo: '',
    name: '',
    gender: 0,
    birthDate: '',
    hireDate: new Date().toISOString().split('T')[0] || '',
    departmentId: departments.value[0]?.id || 0,
    positionId: positions.value[0]?.id || 0,
    status: 0
  }
  // 打开新增对话框
  dialogVisible.value.newEmployee = true
}

// 表单验证规则
const rules = {
  employeeNo: [
    { required: true, message: '请输入员工编号', trigger: 'blur' },
    { min: 3, max: 20, message: '员工编号长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入员工姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '员工姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择部门', trigger: 'change' }
  ],
  positionId: [
    { required: true, message: '请选择岗位', trigger: 'change' }
  ],
  hireDate: [
    { required: true, message: '请选择入职日期', trigger: 'change' }
  ]
}

// 表单引用
const formRef = ref<any>(null)

// 保存新增员工
const handleSaveEmployee = async () => {
  // 表单验证
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await createEmployee(form.value)
        ElMessage.success('新增员工成功')
        dialogVisible.value.newEmployee = false
        // 重新加载数据
        await initData()
      } catch (error) {
        console.error('新增员工失败:', error)
        ElMessage.error('新增员工失败')
      }
    }
  })
}

// 查看详情
const handleViewDetail = async (row: any) => {
  try {
    // 根据不同列表使用正确的员工ID
    const employeeId = row.employeeId || row.id
    const res = await getEmployeeById(employeeId)
    detailData.value = res.data
    dialogVisible.value.detail = true
  } catch (error) {
    console.error('获取员工详情失败:', error)
    ElMessage.error('获取员工详情失败')
  }
}

// 审批
const handleApprove = (row: any) => {
  approveData.value = {
    id: row.id,
    status: 1, // 默认通过
    remark: ''
  }
  dialogVisible.value.approve = true
}

// 搜索
const handleSearch = (tabName: string) => {
  pagination.value[tabName as keyof typeof pagination.value].currentPage = 1
  switch (tabName) {
    case 'transfer':
      fetchTransferRecords()
      break
    case 'adjustment':
      fetchSalaryAdjustments()
      break
    case 'resignation':
      fetchResignationRequests()
      break
  }
}

// 重置
const handleReset = (tabName: string) => {
  searchForm.value[tabName as keyof typeof searchForm.value] = {
    employeeName: '',
    status: '',
    resignationType: ''
  }
  pagination.value[tabName as keyof typeof pagination.value].currentPage = 1
  switch (tabName) {
    case 'transfer':
      fetchTransferRecords()
      break
    case 'adjustment':
      fetchSalaryAdjustments()
      break
    case 'resignation':
      fetchResignationRequests()
      break
  }
}

// 保存审批结果
const handleSaveApprove = async () => {
  try {
    let approvalResult
    
    // 根据当前激活的标签页调用不同的审批API
    switch (activeTab.value) {
      case 'onboarding':
        // 试用期转正审批，更新员工状态
        const employee = employees.value.find(emp => emp.id === approveData.value.id)
        if (employee) {
          // 1: 正式员工，2: 拒绝转正（保持试用期状态或其他处理）
          const newStatus = approveData.value.status === 1 ? 1 : 0
          await updateEmployee(approveData.value.id, {
            ...employee,
            status: newStatus
          })
        } else {
          ElMessage.error('员工不存在')
          return
        }
        break
        
      case 'transfer':
        // 转岗审批
        approvalResult = await approveTransferRecord(
          approveData.value.id,
          approveData.value.status,
          1, // 暂时使用固定值1作为审批人ID，实际项目中应该从登录用户获取
          approveData.value.remark
        )
        break
        
      case 'adjustment':
        // 调薪审批
        approvalResult = await approveSalaryAdjustment(
          approveData.value.id,
          approveData.value.status,
          1, // 暂时使用固定值1作为审批人ID，实际项目中应该从登录用户获取
          approveData.value.remark
        )
        break
        
      case 'resignation':
        // 离职审批
        approvalResult = await approveResignationRequest(
          approveData.value.id,
          approveData.value.status,
          1, // 暂时使用固定值1作为审批人ID，实际项目中应该从登录用户获取
          approveData.value.remark
        )
        // 如果审批通过，更新员工状态为离职
        if (approveData.value.status === 1) {
          const emp = employees.value.find(e => e.id === approveData.value.id)
          if (emp && emp.id) {
            await updateEmployee(emp.id, {
              ...emp,
              status: 2 // 离职状态
            })
          }
        }
        break
    }
    
    ElMessage.success('审批成功')
    dialogVisible.value.approve = false
    // 重新加载当前标签页对应的数据
    switch (activeTab.value) {
      case 'onboarding':
        await fetchEmployees()
        break
      case 'transfer':
        // 转岗审批后，需要重新加载转岗记录和员工数据
        await Promise.all([fetchTransferRecords(), fetchEmployees()])
        break
      case 'adjustment':
        // 调薪审批后，需要重新加载调薪记录
        await fetchSalaryAdjustments()
        break
      case 'resignation':
        // 离职审批后，需要重新加载离职申请和员工数据
        await Promise.all([fetchResignationRequests(), fetchEmployees()])
        break
    }
  } catch (error) {
    console.error('审批失败:', error)
    ElMessage.error('审批失败')
  }
}

// 新增转岗
const handleNewTransfer = () => {
  // 重置表单
  transferForm.value = {
    employeeId: employees.value[0]?.id || 0,
    oldDepartmentId: departments.value[0]?.id || 0,
    newDepartmentId: departments.value[0]?.id || 0,
    oldPositionId: positions.value[0]?.id || 0,
    newPositionId: positions.value[0]?.id || 0,
    transferDate: new Date().toISOString().split('T')[0] || '',
    reason: ''
  }
  // 打开转岗对话框
  dialogVisible.value.transfer = true
}

// 保存转岗记录
const handleSaveTransfer = async () => {
  // 表单验证
  if (!transferFormRef.value) return
  await transferFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
    await createTransferRecord({
      ...transferForm.value,
      status: 0 // 0: 待审批
    })
    ElMessage.success('新增转岗成功')
    dialogVisible.value.transfer = false
    // 重新加载转岗记录和员工数据
    await Promise.all([fetchTransferRecords(), fetchEmployees()])
  } catch (error) {
    console.error('新增转岗失败:', error)
    ElMessage.error('新增转岗失败')
  }
    }
  })
}

// 新增调薪
const handleNewAdjustment = () => {
  // 重置表单
  adjustmentForm.value = {
    employeeId: employees.value[0]?.id || 0,
    oldSalary: 0,
    newSalary: 0,
    reason: '',
    adjustmentDate: new Date().toISOString().split('T')[0] || ''
  }
  // 打开调薪对话框
  dialogVisible.value.adjustment = true
}

// 保存调薪记录
const handleSaveAdjustment = async () => {
  // 表单验证
  if (!adjustmentFormRef.value) return
  await adjustmentFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await createSalaryAdjustment({
          ...adjustmentForm.value,
          status: 0 // 0: 待审批
        })
        ElMessage.success('新增调薪成功')
        dialogVisible.value.adjustment = false
        // 重新加载调薪记录
        await fetchSalaryAdjustments()
      } catch (error) {
        console.error('新增调薪失败:', error)
        ElMessage.error('新增调薪失败')
      }
    }
  })
}

// 新增离职
const handleNewResignation = () => {
  // 重置表单
  resignationForm.value = {
    employeeId: employees.value[0]?.id || 0,
    resignationType: 0,
    applyDate: new Date().toISOString().split('T')[0] || '',
    reason: ''
  }
  // 打开离职对话框
  dialogVisible.value.resignation = true
}

// 保存离职记录
const handleSaveResignation = async () => {
  // 表单验证
  if (!resignationFormRef.value) return
  await resignationFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
    await createResignationRequest({
      ...resignationForm.value,
      status: 0 // 0: 待审批
    })
    ElMessage.success('新增离职成功')
    dialogVisible.value.resignation = false
    // 重新加载离职申请和员工数据
    await Promise.all([fetchResignationRequests(), fetchEmployees()])
  } catch (error) {
    console.error('新增离职失败:', error)
    ElMessage.error('新增离职失败')
  }
    }
  })
}

// 分页大小变化处理
const handleSizeChange = (size: number) => {
  // 根据当前激活的标签页重新加载对应的数据
  switch (activeTab.value) {
    case 'onboarding':
      pagination.value.onboarding.pageSize = size
      fetchEmployees()
      break
    case 'transfer':
      pagination.value.transfer.pageSize = size
      fetchTransferRecords()
      break
    case 'adjustment':
      pagination.value.adjustment.pageSize = size
      fetchSalaryAdjustments()
      break
    case 'resignation':
      pagination.value.resignation.pageSize = size
      fetchResignationRequests()
      break
  }
}

// 页码变化处理
const handleCurrentChange = (current: number) => {
  // 根据当前激活的标签页重新加载对应的数据
  switch (activeTab.value) {
    case 'onboarding':
      pagination.value.onboarding.currentPage = current
      fetchEmployees()
      break
    case 'transfer':
      pagination.value.transfer.currentPage = current
      fetchTransferRecords()
      break
    case 'adjustment':
      pagination.value.adjustment.currentPage = current
      fetchSalaryAdjustments()
      break
    case 'resignation':
      pagination.value.resignation.currentPage = current
      fetchResignationRequests()
      break
  }
}

onMounted(() => {
  initData()
})
</script>

<style scoped>
.employee-lifecycle-view {
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
  padding: 10px 0;
}
</style>
