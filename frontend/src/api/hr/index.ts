import api from '../index'

/**
 * 部门数据类型定义
 */
export interface Department {
  id?: number
  name: string
  code: string
  parentId?: number
  managerId?: number
  description?: string
  status: number // 0: Disabled, 1: Enabled
  children?: Department[] // For tree structure
  createdAt?: string
  updatedAt?: string
}

/**
 * 职位数据类型定义
 */
export interface Position {
  id?: number
  name: string
  code: string
  departmentId: number
  description?: string
  status: number // 0: Disabled, 1: Enabled
  createdAt?: string
  updatedAt?: string
}

/**
 * 员工数据类型定义
 */
export interface Employee {
  id?: number
  employeeNo: string
  name: string
  gender: number // 0: Male, 1: Female, 2: Other
  birthDate?: string
  idCard?: string
  phone?: string
  email?: string
  departmentId: number
  positionId: number
  rankId?: number
  hireDate: string
  leaveDate?: string
  status: number // 0: Probation, 1: Regular, 2: Resigned, 3: On Leave
  salaryLevel?: string
  officeAddress?: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

// --- Recruitment API ---

export interface RecruitmentDemand {
  id?: number
  positionName: string
  departmentId: number
  demandNumber: number
  requiredSkills: string
  expectedSalary: string
  demandDescription?: string
  status: number // 0: Draft, 1: Approved, 2: Closed
  createTime?: string
  updateTime?: string
}

export interface Resume {
  id?: number
  candidateName: string
  gender: string // 后端为字符串：男/女
  phone: string
  email: string
  positionId: number
  resumeUrl: string
  status: string // 后端为字符串枚举：SCREENING/INTERVIEW/OFFER/HIRED/REJECTED
  positionName?: string
  createTime?: string
  updateTime?: string
}

export interface Interview {
  id?: number
  resumeId: number
  interviewerId: number
  interviewTime: string
  interviewType: string // 后端为字符串枚举：TECHNICAL/MANAGERIAL/HR
  interviewResult: string // 后端为字符串枚举：WAITING/PASS/FAIL/OFFER
  interviewNotes?: string
  candidateName?: string
  createTime?: string
}

/**
 * 招聘管理API
 */
export const recruitmentApi = {
  getDemands: (params?: any) => api.get('/api/v1/hr/recruitment-demands', { params }),
  createDemand: (data: RecruitmentDemand) => api.post('/api/v1/hr/recruitment-demands', data),
  getResumes: () => api.get('/api/v1/hr/recruitment/resumes'),
  getResumesByPage: (params: { page: number; size: number; [key: string]: any }) => api.get('/api/v1/hr/recruitment/resumes/page', { params }),
  createResume: (data: Resume) => api.post('/api/v1/hr/recruitment/resumes', data),
  updateResume: (id: number, data: Partial<Resume>) => api.put(`/api/v1/hr/recruitment/resumes/${id}`, data),
  deleteResume: (id: number) => api.delete(`/api/v1/hr/recruitment/resumes/${id}`),
  getInterviews: () => api.get('/api/v1/hr/recruitment/interviews'),
  getInterviewsByPage: (params: { page: number; size: number; [key: string]: any }) => api.get('/api/v1/hr/recruitment/interviews/page', { params }),
  createInterview: (data: Interview) => api.post('/api/v1/hr/recruitment/interviews', data),
  updateInterview: (id: number, data: Partial<Interview>) => api.put(`/api/v1/hr/recruitment/interviews/${id}`, data),
  deleteInterview: (id: number) => api.delete(`/api/v1/hr/recruitment/interviews/${id}`)
}

// --- Attendance API ---

export interface AttendanceRecord {
  id?: number
  employeeId: number
  attendanceDate: string
  checkInTime?: string
  checkOutTime?: string
  status: number // 0: Normal, 1: Late, 2: Early Leave, 3: Absent, 4: Leave, 5: Absenteeism
  checkInMethod?: string
  location?: string
  createTime?: string
  updateTime?: string
}

export interface LeaveRequest {
  id?: number
  employeeId: number
  leaveType: number // 0: Annual Leave, 1: Sick Leave, 2: Personal Leave, 3: Other
  startDate: string
  endDate: string
  duration: number
  reason: string
  status: number // 0: Pending, 1: Approved, 2: Rejected
  createTime?: string
  updateTime?: string
}

export interface AttendanceException {
  id?: number
  employeeId: number
  exceptionDate: string
  exceptionType: number // 0: Forgot to clock in, 1: Business trip, 2: Equipment failure, 3: Other
  description: string
  evidence?: string
  status: number // 0: Pending, 1: Approved, 2: Rejected
  approverId?: number
  approvalRemark?: string
  createTime?: string
  updateTime?: string
}

/**
 * 考勤与假期管理API
 */
export const attendanceApi = {
  getRecords: (employeeId: number, startDate: string, endDate: string) => api.get('/api/v1/hr/attendance-records/by-employee', { params: { employeeId, startDate, endDate } }),
  getRecordsByPage: (params: { page: number; size: number; [key: string]: any }) => api.get('/api/v1/hr/attendance-records/page', { params }),
  
  createRecord: (data: AttendanceRecord) => api.post('/api/v1/hr/attendance-records', data),
  updateRecord: (id: number, data: AttendanceRecord) => api.put(`/api/v1/hr/attendance-records/${id}`, data),
  deleteRecord: (id: number) => api.delete(`/api/v1/hr/attendance-records/${id}`),
  getLeaveRequests: () => api.get('/api/v1/hr/leave-requests'),
  
  getLeaveRequestsByPage: (params: { page: number; size: number; [key: string]: any }) => api.get('/api/v1/hr/leave-requests/page', { params }),
  
  createLeaveRequest: (data: LeaveRequest) => api.post('/api/v1/hr/leave-requests', data),
  updateLeaveRequest: (id: number, data: LeaveRequest) => api.put(`/api/v1/hr/leave-requests/${id}`, data),
  deleteLeaveRequest: (id: number) => api.delete(`/api/v1/hr/leave-requests/${id}`),
  approveLeaveRequest: (id: number, status: number, remark?: string) => api.put(`/api/v1/hr/leave-requests/${id}/approve`, { status, remark }),
  getStatusStatistics: (startDate: string, endDate: string, departmentId?: number) => api.get('/api/v1/hr/attendance-statistics/status', { params: { startDate, endDate, departmentId } }),
  
  getDepartmentStatistics: (startDate: string, endDate: string) => api.get('/api/v1/hr/attendance-statistics/department', { params: { startDate, endDate } }),
  
  getMonthlyStatistics: (year: string, departmentId?: number) => api.get('/api/v1/hr/attendance-statistics/monthly', { params: { year, departmentId } }),
  
  getAttendanceExceptions: () => api.get('/api/v1/hr/attendance-exceptions'),
  
  getAttendanceExceptionsByPage: (params: { page: number; size: number; [key: string]: any }) => api.get('/api/v1/hr/attendance-exceptions/page', { params }),
  
  createAttendanceException: (data: AttendanceException) => api.post('/api/v1/hr/attendance-exceptions', data),
  updateAttendanceException: (id: number, data: AttendanceException) => api.put(`/api/v1/hr/attendance-exceptions/${id}`, data),
  deleteAttendanceException: (id: number) => api.delete(`/api/v1/hr/attendance-exceptions/${id}`),
  approveAttendanceException: (id: number, status: number, approverId: number, remark?: string) => api.put(`/api/v1/hr/attendance-exceptions/${id}/approve`, {}, { params: { status, approverId, remark } }),
  autoDetectAttendanceExceptions: (startDate?: string, endDate?: string) => api.get('/api/v1/hr/attendance-exceptions/auto-detect', { params: { startDate, endDate } })
}

// --- Payroll API ---

export interface SalaryStructure {
  id?: number
  name: string
  basicSalary: number
  bonus: number
  allowance: number
  deduction: number
  effectiveDate: string
  status: number // 0: Inactive, 1: Active
  createTime?: string
}

export interface PayrollRecord {
  id?: number
  employeeId: number
  month: string // YYYY-MM
  basicSalary: number
  performanceSalary?: number
  bonus: number
  allowance: number
  deduction: number
  actualSalary: number // 实发工资
  status: number // 0: 已计算, 1: 已发放, 2: 待计算
  remark?: string
  createTime?: string
}

/**
 * 薪酬福利管理API
 */
export const payrollApi = {
  getSalaryStructures: () => api.get('/api/v1/hr/payroll/salary-structures'),

  createSalaryStructure: (data: SalaryStructure) => api.post('/api/v1/hr/payroll/salary-structures', data),
  getPayrollRecords: (employeeId: number | null, startMonth: string, endMonth: string) => api.get('/api/v1/hr/payroll-records', { params: { startMonth, endMonth } }),

  calculatePayroll: (month: string) => api.post('/api/v1/hr/payroll/calculate', { month }),
  updatePayrollRecord: (id: number, data: Partial<PayrollRecord>) => api.put(`/api/v1/hr/payroll-records/${id}`, data)
}

// --- Employee Lifecycle API ---

export interface TransferRecord {
  id?: number
  employeeId: number
  oldDepartmentId: number
  newDepartmentId: number
  oldPositionId: number
  newPositionId: number
  transferDate: string
  reason: string
  status: number // 0: Pending, 1: Approved, 2: Rejected
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface SalaryAdjustment {
  id?: number
  employeeId: number
  oldSalary: number
  newSalary: number
  reason: string
  adjustmentDate: string
  status: number // 0: Pending, 1: Approved, 2: Rejected
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface ResignationRequest {
  id?: number
  employeeId: number
  resignationType: number // 0: 主动离职, 1: 被动离职等
  applyDate: string
  expectedResignDate?: string
  actualResignDate?: string
  reason: string
  status: number // 0: Pending, 1: Approved, 2: Rejected
  remark?: string
  createTime?: string
  updateTime?: string
}

/**
 * HR模块统一API对象
 */
export const lifecycleApi = {
  transfer: {
    getAll: () => api.get('/api/v1/hr/transfer-records'),
    getByPage: (params: { page: number; size: number; employeeName?: string; status?: number }) => api.get('/api/v1/hr/transfer-records/page', { params }),
    
    create: (data: TransferRecord) => api.post('/api/v1/hr/transfer-records', data),
    update: (id: number, data: TransferRecord) => api.put(`/api/v1/hr/transfer-records/${id}`, data),
    approve: (id: number, status: number, approverId?: number, remark?: string) => api.put(`/api/v1/hr/transfer-records/${id}/approve`, {}, { params: { status, approverId, remark } })
  },
  salaryAdjustment: {
    getAll: () => api.get('/api/v1/hr/salary-adjustments'),
    getByPage: (params: { page: number; size: number; employeeName?: string; status?: number }) => api.get('/api/v1/hr/salary-adjustments/page', { params }),
    
    create: (data: SalaryAdjustment) => api.post('/api/v1/hr/salary-adjustments', data),
    update: (id: number, data: SalaryAdjustment) => api.put(`/api/v1/hr/salary-adjustments/${id}`, data),
    approve: (id: number, status: number, approverId?: number, remark?: string) => api.put(`/api/v1/hr/salary-adjustments/${id}/approve`, {}, { params: { status, approverId, remark } })
  },
  resignation: {
    getAll: () => api.get('/api/v1/hr/resignation-requests'),
    getByPage: (params: { page: number; size: number; employeeName?: string; status?: number; resignationType?: string }) => api.get('/api/v1/hr/resignation-requests/page', { params }),
    
    create: (data: ResignationRequest) => api.post('/api/v1/hr/resignation-requests', data),
    update: (id: number, data: ResignationRequest) => api.put(`/api/v1/hr/resignation-requests/${id}`, data),
    approve: (id: number, status: number, approverId?: number, remark?: string) => api.put(`/api/v1/hr/resignation-requests/${id}/approve`, {}, { params: { status, approverId, remark } })
  }
}

// --- Performance API ---

export interface PerformanceObjective {
  id?: number
  employeeId: number
  objectiveContent: string
  targetValue: string
  weight: number
  startDate: string
  endDate: string
  status: number // 0: Active, 1: Completed, 2: Cancelled
  createTime?: string
}

export interface PerformanceAppraisal {
  id?: number
  employeeId: number
  appraisalPeriod: string
  objectiveScore: number
  competencyScore: number
  totalScore: number
  appraisalStatus: number // 0: Pending, 1: Approved, 2: Completed
  appraiserId: number
  appraisalNotes?: string
  createTime?: string
  updateTime?: string
}

/**
 * 绩效管理API
 */
export const performanceApi = {
  getObjectives: (employeeId: number) => api.get('/api/v1/hr/performance/objectives', { params: { employeeId } }),
  createObjective: (data: PerformanceObjective) => api.post('/api/v1/hr/performance/objectives', data),
  getAppraisals: (employeeId: number) => api.get('/api/v1/hr/performance/appraisals', { params: { employeeId } }),
  createAppraisal: (data: PerformanceAppraisal) => api.post('/api/v1/hr/performance/appraisals', data)
}

/**
 * HR 集成事件查询参数（F5 前端页面）
 */
export interface IntegrationEventQueryParams {
  /** 事件类型（精确匹配，如 HR_EMPLOYEE_ONBOARDED_OA） */
  eventType?: string
  /** 投递状态（精确匹配，PENDING/SENT/FAILED） */
  status?: string
  /** 业务引用号（模糊匹配，员工编号或记录ID） */
  refNo?: string
  /** 页码（从 1 开始） */
  page?: number
  /** 每页条数 */
  size?: number
}

/**
 * HR 集成事件 API（P2-C 闭环：员工事件向 OA/ERP 投递监控）
 *
 * 对应后端 GET /api/v1/hr/integration/events，供前端 F5（HR事件查询页）调用。
 */
export const integrationApi = {
  /**
   * 分页查询 HR 集成事件
   * @param params 查询参数（所有字段可选）
   * @returns 集成事件分页结果
   */
  fetchEvents: (params?: IntegrationEventQueryParams) =>
    api.get('/api/v1/hr/integration/events', { params })
}

// --- Training API（培训管理） ---

/**
 * 培训计划数据类型
 */
export interface TrainingPlan {
  id?: number
  trainingName: string
  trainer?: string
  startDate: string
  endDate: string
  location?: string
  status: number // 0: 计划中, 1: 进行中, 2: 已完成, 3: 已取消
  description?: string
  createTime?: string
}

/**
 * 培训参与记录数据类型
 */
export interface TrainingParticipant {
  id?: number
  trainingId: number
  employeeId: number
  attendanceStatus: number // 0: 已报名, 1: 已参加, 2: 未参加
  completionStatus: number // 0: 未完成, 1: 已完成
  score?: number
  createTime?: string
}

/**
 * 培训管理API
 */
export const trainingApi = {
  plan: {
    getAll: () => api.get('/api/v1/hr/training-plans'),
    getByPage: (params: { page: number; size: number; [key: string]: any }) => api.get('/api/v1/hr/training-plans/page', { params }),
    create: (data: TrainingPlan) => api.post('/api/v1/hr/training-plans', data),
    update: (id: number, data: Partial<TrainingPlan>) => api.put(`/api/v1/hr/training-plans/${id}`, data),
    delete: (id: number) => api.delete(`/api/v1/hr/training-plans/${id}`)
  },
  participant: {
    getAll: () => api.get('/api/v1/hr/training-participants'),
    getByPage: (params: { page: number; size: number; [key: string]: any }) => api.get('/api/v1/hr/training-participants/page', { params }),
    create: (data: TrainingParticipant) => api.post('/api/v1/hr/training-participants', data),
    update: (id: number, data: Partial<TrainingParticipant>) => api.put(`/api/v1/hr/training-participants/${id}`, data),
    delete: (id: number) => api.delete(`/api/v1/hr/training-participants/${id}`)
  }
}

// --- Social Security API（社保公积金） ---

/**
 * 社保公积金记录数据类型
 */
export interface SocialSecurityRecord {
  id?: number
  employeeId: number
  insuranceMonth: string // YYYY-MM
  baseAmount: number
  pensionCompany?: number
  pensionPersonal?: number
  medicalCompany?: number
  medicalPersonal?: number
  unemploymentCompany?: number
  unemploymentPersonal?: number
  housingFundCompany?: number
  housingFundPersonal?: number
  status: number // 0: 未申报, 1: 已申报
  createTime?: string
}

/**
 * 社保公积金API
 */
export const socialSecurityApi = {
  getAll: () => api.get('/api/v1/hr/social-security-records'),
  getByPage: (params: { page: number; size: number; [key: string]: any }) => api.get('/api/v1/hr/social-security-records/page', { params }),
  create: (data: SocialSecurityRecord) => api.post('/api/v1/hr/social-security-records', data),
  update: (id: number, data: Partial<SocialSecurityRecord>) => api.put(`/api/v1/hr/social-security-records/${id}`, data),
  delete: (id: number) => api.delete(`/api/v1/hr/social-security-records/${id}`),
  calculate: (month: string) => api.post('/api/v1/hr/social-security-records/calculate', null, { params: { month } }),
  declareMonth: (month: string) => api.post('/api/v1/hr/social-security-records/declare', null, { params: { month } })
}

// --- Benefit API（福利管理） ---

/**
 * 福利配置数据类型
 */
export interface BenefitConfig {
  id?: number
  benefitName: string
  benefitType: number // 0: 节日福利, 1: 生日福利, 2: 健康体检, 3: 补贴
  standardAmount: number
  frequency: number // 0: 一次性, 1: 每月, 2: 每年
  status: number // 0: 停用, 1: 启用
  description?: string
  createTime?: string
}

/**
 * 福利发放记录数据类型
 */
export interface BenefitRecord {
  id?: number
  benefitId: number
  employeeId: number
  distributeDate: string
  amount: number
  status: number // 0: 待发放, 1: 已发放
  createTime?: string
}

/**
 * 福利管理API
 */
export const benefitApi = {
  config: {
    getAll: () => api.get('/api/v1/hr/benefit-configs'),
    getByPage: (params: { page: number; size: number; [key: string]: any }) => api.get('/api/v1/hr/benefit-configs/page', { params }),
    create: (data: BenefitConfig) => api.post('/api/v1/hr/benefit-configs', data),
    update: (id: number, data: Partial<BenefitConfig>) => api.put(`/api/v1/hr/benefit-configs/${id}`, data),
    delete: (id: number) => api.delete(`/api/v1/hr/benefit-configs/${id}`)
  },
  record: {
    getAll: () => api.get('/api/v1/hr/benefit-records'),
    getByPage: (params: { page: number; size: number; [key: string]: any }) => api.get('/api/v1/hr/benefit-records/page', { params }),
    create: (data: BenefitRecord) => api.post('/api/v1/hr/benefit-records', data),
    update: (id: number, data: Partial<BenefitRecord>) => api.put(`/api/v1/hr/benefit-records/${id}`, data),
    delete: (id: number) => api.delete(`/api/v1/hr/benefit-records/${id}`),
    distribute: (benefitId: number) => api.post(`/api/v1/hr/benefit-records/distribute`, null, { params: { benefitId } })
  }
}

// --- Attendance Rule API（考勤规则） ---

/**
 * 考勤规则数据类型
 */
export interface AttendanceRule {
  id?: number
  ruleName: string
  workStartTime: string // HH:mm
  workEndTime: string // HH:mm
  lateTolerance: number // 迟到容忍分钟数
  earlyLeaveTolerance: number // 早退容忍分钟数
  applicableDepartments: string // 适用部门名称，逗号分隔
  status: number // 0: 禁用, 1: 启用
  createTime?: string
}

/**
 * 考勤规则API
 */
export const attendanceRuleApi = {
  getAll: () => api.get('/api/v1/hr/attendance-rules'),
  getByPage: (params: { page: number; size: number; [key: string]: any }) => api.get('/api/v1/hr/attendance-rules/page', { params }),
  create: (data: AttendanceRule) => api.post('/api/v1/hr/attendance-rules', data),
  update: (id: number, data: Partial<AttendanceRule>) => api.put(`/api/v1/hr/attendance-rules/${id}`, data),
  delete: (id: number) => api.delete(`/api/v1/hr/attendance-rules/${id}`)
}

// --- Performance Bonus API（绩效奖金） ---

/**
 * 绩效奖金数据类型
 */
export interface PerformanceBonus {
  id?: number
  employeeId: number
  appraisalPeriod: string // 考核周期，如2023年度
  performanceScore: number
  performanceLevel: string // A/B/C/D
  bonusAmount: number
  status: number // 0: 待审批, 1: 已批准, 2: 已发放
  evaluatorId?: number
  createTime?: string
}

/**
 * 绩效奖金API
 */
export const performanceBonusApi = {
  getAll: () => api.get('/api/v1/hr/performance-bonuses'),
  getByPage: (params: { page: number; size: number; [key: string]: any }) => api.get('/api/v1/hr/performance-bonuses/page', { params }),
  create: (data: PerformanceBonus) => api.post('/api/v1/hr/performance-bonuses', data),
  update: (id: number, data: Partial<PerformanceBonus>) => api.put(`/api/v1/hr/performance-bonuses/${id}`, data),
  delete: (id: number) => api.delete(`/api/v1/hr/performance-bonuses/${id}`),
  approve: (id: number, status: number) => api.put(`/api/v1/hr/performance-bonuses/${id}/approve`, null, { params: { status } })
}

/**
 * HR模块统一API对象
 */
export const hrApi = {
  department: {
    getAll: () => api.get('/api/v1/hr/departments'),
    getById: (id: number) => api.get(`/api/v1/hr/departments/${id}`),
    
    create: (data: Department) => api.post('/api/v1/hr/departments', data),
    update: (id: number, data: Partial<Department>) => api.put(`/api/v1/hr/departments/${id}`, data),
    delete: (id: number) => api.delete(`/api/v1/hr/departments/${id}`),
    getByPage: (params: { page: number; size: number; keyword?: string; status?: number }) => api.get('/api/v1/hr/departments/page', { params })
  },
  position: {
    getAll: () => api.get('/api/v1/hr/positions'),
    getById: (id: number) => api.get(`/api/v1/hr/positions/${id}`),
    
    create: (data: Position) => api.post('/api/v1/hr/positions', data),
    update: (id: number, data: Partial<Position>) => api.put(`/api/v1/hr/positions/${id}`, data),
    delete: (id: number) => api.delete(`/api/v1/hr/positions/${id}`),
    getByPage: (params: { page: number; size: number; keyword?: string; status?: number; departmentId?: number }) => api.get('/api/v1/hr/positions/page', { params }),
  },
  employee: {
    getAll: () => api.get('/api/v1/hr/employees'),
    getById: (id: number) => api.get(`/api/v1/hr/employees/${id}`),
    
    create: (data: Employee) => api.post('/api/v1/hr/employees', data),
    update: (id: number, data: Partial<Employee>) => api.put(`/api/v1/hr/employees/${id}`, data), // 修复路径：/hr/employees/id/${id} -> /api/v1/hr/employees/${id}
    delete: (id: number) => api.delete(`/api/v1/hr/employees/${id}`), // 修复路径：/hr/employees/id/${id} -> /api/v1/hr/employees/${id}
    getByPage: (params: any) => api.get('/api/v1/hr/employees/page', { params })
  },
  recruitment: recruitmentApi,
  attendance: attendanceApi,
  payroll: payrollApi,
  lifecycle: lifecycleApi,
  performance: performanceApi,
  integration: integrationApi,
  training: trainingApi,
  socialSecurity: socialSecurityApi,
  benefit: benefitApi,
  attendanceRule: attendanceRuleApi,
  performanceBonus: performanceBonusApi
}

// 导出旧版具名导出，通过 hrApi 的对应子模块进行兼容
export const getPositions = hrApi.position.getAll
export const getPositionsByPage = hrApi.position.getByPage
export const createPosition = hrApi.position.create
export const updatePosition = hrApi.position.update
export const deletePosition = hrApi.position.delete
export const getDepartments = hrApi.department.getAll
export const getEmployees = hrApi.employee.getAll
export const getEmployeesByPage = hrApi.employee.getByPage

export default hrApi
