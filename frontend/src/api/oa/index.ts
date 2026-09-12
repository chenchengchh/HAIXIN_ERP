import api from '../index'
import type { AxiosResponse } from 'axios'

// OA审批流程定义类型
export interface ApprovalProcess {
  id?: number;
  name: string;
  code: string;
  description?: string;
  status: number;
  processType: string;
  processDefinition: string;
  formConfig?: string;
  creatorId: number;
  createTime?: string;
  updaterId?: number;
  updateTime?: string;
}

// OA审批流程实例类型
export interface ApprovalProcessInstance {
  id?: number;
  processId: number;
  processCode: string;
  title: string;
  description?: string;
  initiatorId: number;
  initiatorName: string;
  currentNodeId?: string;
  currentNodeName?: string;
  status: string;
  formData: Record<string, any>;
  processVariables?: string;
  startTime?: string;
  endTime?: string;
  createTime?: string;
  updateTime?: string;
}

// 分页结果类型
export interface PageResult<T> {
  records: T[];
  list: T[];
  total: number;
  page: number;
  size: number;
}

// OA审批任务类型
export interface ApprovalTask {
  id?: number;
  instanceId: number;
  name: string;
  description?: string;
  nodeId: string;
  nodeName: string;
  /** 审批节点要求的角色编码（权限校验用，对应流程定义 assigneeRole） */
  assigneeRole?: string;
  /** 节点序号（对应流程定义 nodes 数组下标，用于审批历史按节点排序展示） */
  nodeIndex?: number;
  assigneeId: number;
  assigneeName: string;
  /** 审批人所属部门ID（审计/统计用） */
  assigneeDeptId?: number;
  status: string;
  result?: string;
  comment?: string;
  approveTime?: string;
  createTime?: string;
  updateTime?: string;
  dueTime?: string;
}

// 会议室类型
export interface MeetingRoom {
  id?: number;
  roomCode: string;
  roomName: string;
  location?: string;
  capacity?: number;
  equipment?: string;
  status: number;
  creatorId: number;
  createTime?: string;
  updaterId?: number;
  updateTime?: string;
}

// 会议预约类型
export interface Meeting {
  id?: number;
  meetingNo: string;
  meetingTitle: string;
  roomId: number;
  roomName: string;
  organizerId: number;
  organizerName: string;
  startTime: string;
  endTime: string;
  attendees: string;
  agenda?: string;
  status: string;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

// 文档类型
export interface OaDocument {
  id?: number;
  title: string;
  description?: string;
  categoryId?: number;
  categoryName?: string;
  type?: string;
  status?: number;
  creatorId?: number;
  creatorName?: string;
  currentVersion?: string;
  createTime?: string;
  updateTime?: string;
}

// 文档分类类型
export interface DocumentCategory {
  id?: number;
  name: string;
  description?: string;
  parentId?: number;
  parentName?: string;
  level?: number;
  sort?: number;
  status?: number;
  creatorId?: number;
  createTime?: string;
  updaterId?: number;
  updateTime?: string;
}

// 文档版本类型
export interface DocumentVersion {
  id?: number;
  documentId: number;
  version: string;
  versionName: string;
  description?: string;
  fileName: string;
  fileSize?: number;
  filePath: string;
  fileHash?: string;
  creatorId: number;
  creatorName: string;
  isCurrent: number;
  createTime?: string;
}

// 审批流程API
export const approvalProcessApi = {
  // 获取审批流程列表
  getList: (params: { page?: number; size?: number }) => {
    return api.get<AxiosResponse<{ code: number; msg: string; data: PageResult<ApprovalProcess> }>>('/api/v1/oa/approval/process', { params });
  },
  // 创建审批流程
  create: (data: ApprovalProcess) => {
    return api.post<AxiosResponse<{ code: number; msg: string; data: ApprovalProcess }>>('/api/v1/oa/approval/process', data);
  },
  // 更新审批流程
  update: (id: number, data: ApprovalProcess) => {
    return api.put<AxiosResponse<{ code: number; msg: string; data: ApprovalProcess }>>(`/api/v1/oa/approval/process/${id}`, data);
  },
  // 删除审批流程
  delete: (id: number) => {
    return api.delete<AxiosResponse<{ code: number; msg: string; data: null }>>(`/api/v1/oa/approval/process/${id}`);
  },
  // 根据ID获取审批流程
  getById: (id: number) => {
    return api.get<AxiosResponse<{ code: number; msg: string; data: ApprovalProcess }>>(`/api/v1/oa/approval/process/${id}`);
  },
  // 根据编码获取审批流程
  getByCode: (code: string) => {
    return api.get<AxiosResponse<{ code: number; msg: string; data: ApprovalProcess }>>(`/api/v1/oa/approval/process/code/${code}`);
  },
  // 启用审批流程
  enable: (id: number) => {
    return api.put<AxiosResponse<{ code: number; msg: string; data: ApprovalProcess }>>(`/api/v1/oa/approval/process/${id}/enable`);
  },
  // 禁用审批流程
  disable: (id: number) => {
    return api.put<AxiosResponse<{ code: number; msg: string; data: ApprovalProcess }>>(`/api/v1/oa/approval/process/${id}/disable`);
  },
  // 复制审批流程
  copy: (id: number) => {
    return api.post<AxiosResponse<{ code: number; msg: string; data: ApprovalProcess }>>(`/api/v1/oa/approval/process/${id}/copy`);
  }
};

// 审批任务API
export const approvalTaskApi = {
  // 获取审批任务列表
  getList: (params: { 
    page?: number; 
    size?: number; 
    status?: string; 
    assigneeId?: number;
    processType?: string;
    initiator?: string;
    result?: string;
  }) => {
    return api.get<{ code: number; msg: string; data: PageResult<ApprovalTask> }>('/api/v1/oa/approval/tasks', { params });
  },
  // 根据实例ID获取审批任务列表
  getByInstanceId: (instanceId: number, params: { page?: number; size?: number }) => {
    return api.get<{ code: number; msg: string; data: PageResult<ApprovalTask> }>(`/api/v1/oa/approval/tasks/instance/${instanceId}`, { params });
  },
  // 根据审批人ID和状态获取审批任务列表
  getByAssigneeIdAndStatus: (assigneeId: number, status: string, params: { page?: number; size?: number }) => {
    return api.get<{ code: number; msg: string; data: PageResult<ApprovalTask> }>(`/api/v1/oa/approval/tasks/assignee/${assigneeId}`, { params: { ...params, status } });
  },
  // 根据ID获取审批任务详情
  getById: (id: number) => {
    return api.get<{ code: number; msg: string; data: ApprovalTask }>(`/api/v1/oa/approval/tasks/${id}`);
  },
  // 审批任务
  approve: (id: number, result: string, comment?: string) => {
    return api.put<ApprovalTask>(`/api/v1/oa/approval/tasks/${id}/approve`, { result, comment });
  },
  // 取消审批任务
  cancel: (id: number) => {
    return api.put<ApprovalTask>(`/api/v1/oa/approval/tasks/${id}/cancel`);
  }
};

// 会议管理API
export const meetingApi = {
  // 获取会议列表
  getList: (params: { page?: number; size?: number }) => {
    return api.get<{ code: number; msg: string; data: PageResult<Meeting> }>('/api/v1/oa/meeting', { params });
  },
  // 创建会议
  create: (data: Meeting) => {
    return api.post<Meeting>('/api/v1/oa/meeting', data);
  },
  // 更新会议
  update: (id: number, data: Meeting) => {
    return api.put<Meeting>(`/api/v1/oa/meeting/${id}`, data);
  },
  // 取消会议
  cancel: (id: number) => {
    return api.put<Meeting>(`/api/v1/oa/meeting/${id}/cancel`);
  },
  // 开始会议
  start: (id: number) => {
    return api.put<Meeting>(`/api/v1/oa/meeting/${id}/start`);
  },
  // 结束会议
  end: (id: number) => {
    return api.put<Meeting>(`/api/v1/oa/meeting/${id}/end`);
  },
  // 根据ID获取会议
  getById: (id: number) => {
    return api.get<Meeting>(`/api/v1/oa/meeting/${id}`);
  },
  // 根据组织者ID获取会议
  getByOrganizerId: (organizerId: number, params: { page?: number; size?: number }) => {
    return api.get<Meeting[]>(`/api/v1/oa/meeting/organizer/${organizerId}`, { params });
  },
  // 根据状态获取会议
  getByStatus: (status: string, params: { page?: number; size?: number }) => {
    return api.get<Meeting[]>(`/api/v1/oa/meeting/status/${status}`, { params });
  },
  // 根据会议室ID获取会议
  getByRoomId: (roomId: number, params: { page?: number; size?: number }) => {
    return api.get<Meeting[]>(`/api/v1/oa/meeting/room/${roomId}`, { params });
  },
  // 根据时间范围获取会议
  getByTimeRange: (startTime: string, endTime: string) => {
    return api.get<Meeting[]>(`/api/v1/oa/meeting/time-range`, { params: { startTime, endTime } });
  },
  // 根据会议室ID和时间范围获取会议
  getByRoomIdAndTimeRange: (roomId: number, startTime: string, endTime: string) => {
    return api.get<Meeting[]>(`/api/v1/oa/meeting/room/${roomId}/time-range`, { params: { startTime, endTime } });
  }
};

// 会议室管理API
export const meetingRoomApi = {
  // 获取会议室列表
  getList: (params: { page?: number; size?: number }) => {
    return api.get<MeetingRoom[]>('/api/v1/oa/meeting/room', { params });
  },
  // 创建会议室
  create: (data: MeetingRoom) => {
    return api.post<MeetingRoom>('/api/v1/oa/meeting/room', data);
  },
  // 更新会议室
  update: (id: number, data: MeetingRoom) => {
    return api.put<MeetingRoom>(`/api/v1/oa/meeting/room/${id}`, data);
  },
  // 启用会议室
  enable: (id: number) => {
    return api.put<MeetingRoom>(`/api/v1/oa/meeting/room/${id}/enable`);
  },
  // 禁用会议室
  disable: (id: number) => {
    return api.put<MeetingRoom>(`/api/v1/oa/meeting/room/${id}/disable`);
  },
  // 根据ID获取会议室
  getById: (id: number) => {
    return api.get<MeetingRoom>(`/api/v1/oa/meeting/room/${id}`);
  },
  // 根据编码获取会议室
  getByCode: (code: string) => {
    return api.get<MeetingRoom>(`/api/v1/oa/meeting/room/code/${code}`);
  },
  // 根据状态获取会议室
  getByStatus: (status: number, params: { page?: number; size?: number }) => {
    return api.get<MeetingRoom[]>(`/api/v1/oa/meeting/room/status/${status}`, { params });
  },
  // 根据名称获取会议室
  getByName: (name: string, params: { page?: number; size?: number }) => {
    return api.get<MeetingRoom[]>(`/api/v1/oa/meeting/room/name/${name}`, { params });
  }
};

// 文档版本管理API
export const documentVersionApi = {
  // 获取文档版本列表
  getList: (params: { page?: number; size?: number }) => {
    return api.get<DocumentVersion[]>('/api/v1/oa/document/versions', { params });
  },
  // 创建文档版本
  create: (data: DocumentVersion) => {
    return api.post<DocumentVersion>('/api/v1/oa/document/versions', data);
  },
  // 根据ID获取文档版本
  getById: (id: number) => {
    return api.get<DocumentVersion>(`/api/v1/oa/document/versions/${id}`);
  },
  // 根据文档ID获取文档版本列表
  getByDocumentId: (documentId: number, params: { page?: number; size?: number }) => {
    return api.get<DocumentVersion[]>(`/api/v1/oa/document/versions/document/${documentId}`, { params });
  },
  // 获取文档当前版本
  getCurrentVersion: (documentId: number) => {
    return api.get<DocumentVersion>(`/api/v1/oa/document/versions/document/${documentId}/current`);
  },
  // 设置文档当前版本
  setCurrentVersion: (id: number) => {
    return api.put<DocumentVersion>(`/api/v1/oa/document/versions/${id}/current`);
  },
  // 删除文档版本
  delete: (id: number) => {
    return api.delete(`/api/v1/oa/document/versions/${id}`);
  }
};

// 文档管理API
export const documentApi = {
  // 分页获取文档列表
  getPage: (params: { page?: number; size?: number }) => {
    return api.get<{ code: number; msg: string; data: PageResult<OaDocument> }>('/api/v1/oa/document/page', { params });
  },
  // 获取文档列表
  getList: () => {
    return api.get<{ code: number; msg: string; data: OaDocument[] }>('/api/v1/oa/document');
  },
  // 创建文档
  create: (data: OaDocument) => {
    return api.post<{ code: number; msg: string; data: OaDocument }>('/api/v1/oa/document', data);
  },
  // 更新文档
  update: (id: number, data: OaDocument) => {
    return api.put<{ code: number; msg: string; data: OaDocument }>(`/api/v1/oa/document/${id}`, data);
  },
  // 删除文档
  delete: (id: number) => {
    return api.delete<{ code: number; msg: string; data: null }>(`/api/v1/oa/document/${id}`);
  },
  // 根据ID获取文档
  getById: (id: number) => {
    return api.get<{ code: number; msg: string; data: OaDocument }>(`/api/v1/oa/document/${id}`);
  },
  // 根据分类获取文档
  getByCategoryId: (categoryId: number) => {
    return api.get<{ code: number; msg: string; data: OaDocument[] }>(`/api/v1/oa/document/category/${categoryId}`);
  },
  // 根据状态获取文档
  getByStatus: (status: number) => {
    return api.get<{ code: number; msg: string; data: OaDocument[] }>(`/api/v1/oa/document/status/${status}`);
  },
  // 发布文档
  publish: (id: number) => {
    return api.put<{ code: number; msg: string; data: OaDocument }>(`/api/v1/oa/document/${id}/publish`);
  },
  // 撤销文档
  revoke: (id: number) => {
    return api.put<{ code: number; msg: string; data: OaDocument }>(`/api/v1/oa/document/${id}/revoke`);
  },
  // 归档文档
  archive: (id: number) => {
    return api.put<{ code: number; msg: string; data: OaDocument }>(`/api/v1/oa/document/${id}/archive`);
  }
};

// 文档分类管理API
export const documentCategoryApi = {
  // 获取分类列表
  getList: () => {
    return api.get<{ code: number; msg: string; data: DocumentCategory[] }>('/api/v1/oa/document/category');
  },
  // 创建分类
  create: (data: DocumentCategory) => {
    return api.post<{ code: number; msg: string; data: DocumentCategory }>('/api/v1/oa/document/category', data);
  },
  // 更新分类
  update: (id: number, data: DocumentCategory) => {
    return api.put<{ code: number; msg: string; data: DocumentCategory }>(`/api/v1/oa/document/category/${id}`, data);
  },
  // 删除分类
  delete: (id: number) => {
    return api.delete<{ code: number; msg: string; data: null }>(`/api/v1/oa/document/category/${id}`);
  },
  // 根据ID获取分类
  getById: (id: number) => {
    return api.get<{ code: number; msg: string; data: DocumentCategory }>(`/api/v1/oa/document/category/${id}`);
  },
  // 根据父分类ID获取子分类
  getByParentId: (parentId: number) => {
    return api.get<{ code: number; msg: string; data: DocumentCategory[] }>(`/api/v1/oa/document/category/parent/${parentId}`);
  }
};

// 待审批任务API（用于获取待审批任务列表）
export const pendingApprovalApi = {
  // 获取待审批任务列表
  getList: (params: {
    page?: number;
    size?: number;
    processType?: string;
    initiator?: string;
  }) => {
    return api.get<{ code: number; msg: string; data: PageResult<ApprovalTask> }>('/api/v1/oa/approval/tasks', { params: { ...params, status: 'pending' } });
  },
  // 审批任务
  approve: (id: number, result: string, comment?: string) => {
    return api.put<{ code: number; msg: string; data: ApprovalTask }>(`/api/v1/oa/approval/tasks/${id}/approve`, { result, comment });
  }
};

// 已审批任务API（用于获取已审批任务列表）
export const approvedApi = {
  // 获取已审批任务列表
  getList: (params: {
    page?: number;
    size?: number;
    processType?: string;
    initiator?: string;
    result?: string;
  }) => {
    return api.get<{ code: number; msg: string; data: PageResult<ApprovalTask> }>('/api/v1/oa/approval/tasks', { params: { ...params, status: 'approved' } });
  }
};

// 已发起任务API（用于获取已发起任务列表）
export const initiatedApi = {
  // 获取已发起任务列表
  getList: (params: {
    page?: number;
    size?: number;
    processType?: string;
    status?: string;
  }) => {
    return api.get<{ code: number; msg: string; data: PageResult<ApprovalTask> }>('/api/v1/oa/approval/tasks', { params });
  }
};

// 统一审批申请请求类型（跨模块审批）
export interface UnifiedApprovalRequest {
  // 来源系统（crm/scm/erp/wms/mes/les/qms/srm/eam/hr等）
  sourceSystem: string;
  // 业务类型（如：sales_order/purchase_order/production_order/warehouse_outbound等）
  businessType: string;
  // 业务ID（各模块的业务单据ID）
  businessId: string;
  // 业务单号（各模块的业务单据编号，用于展示）
  businessNo?: string;
  // 审批标题
  title: string;
  // 审批描述
  description?: string;
  // 发起人ID
  initiatorId: number;
  // 发起人名称
  initiatorName: string;
  // 表单数据（JSON格式，包含业务单据的详细信息）
  formData?: Record<string, any>;
  // 回调URL（审批完成后OA回调此URL通知发起方模块）
  callbackUrl?: string;
  // 关联ERP订单ID（可选）
  erpOrderId?: number;
  // 关联SCM供应商ID（可选）
  scmSupplierId?: number;
  // 关联MES车间ID（可选）
  mesWorkshopId?: number;
}

// 审批操作请求类型
export interface ApprovalActionRequest {
  // 审批操作：approve-通过 / reject-拒绝 / return-退回
  action: 'approve' | 'reject' | 'return';
  // 审批意见
  comment?: string;
  // 审批人ID（必填，用于权限三层校验：角色校验 + 任务归属校验）
  approverId: number;
  // 审批人名称（回调审计用）
  approverName?: string;
}

// 统一审批API（跨模块审批入口，贯穿CRM/SCM/ERP/WMS/MES等各业务模块）
export const unifiedApprovalApi = {
  // 提交审批申请（各业务模块统一提交审批申请，自动匹配流程模板）
  submit: (data: UnifiedApprovalRequest) => {
    return api.post<AxiosResponse<{ code: number; msg: string; data: ApprovalProcessInstance }>>('/api/v1/oa/unified-approval/submit', data);
  },
  // 审批操作（审批通过或拒绝指定任务）
  processTask: (taskId: number, data: ApprovalActionRequest) => {
    return api.put<AxiosResponse<{ code: number; msg: string; data: ApprovalTask }>>(`/api/v1/oa/unified-approval/tasks/${taskId}/process`, data);
  },
  // 查询待办审批列表（按审批人ID查询待办审批任务）
  getTodoList: (assigneeId: number, params: { page?: number; size?: number }) => {
    return api.get<AxiosResponse<{ code: number; msg: string; data: PageResult<ApprovalTask> }>>(`/api/v1/oa/unified-approval/todo/${assigneeId}`, { params });
  },
  // 查询已办审批列表（按审批人ID查询已办审批任务）
  getDoneList: (assigneeId: number, params: { page?: number; size?: number }) => {
    return api.get<AxiosResponse<{ code: number; msg: string; data: PageResult<ApprovalTask> }>>(`/api/v1/oa/unified-approval/done/${assigneeId}`, { params });
  },
  // 查询我发起的审批列表（按发起人ID查询审批实例列表）
  getInitiatedList: (initiatorId: number, params: { page?: number; size?: number }) => {
    return api.get<AxiosResponse<{ code: number; msg: string; data: PageResult<ApprovalProcessInstance> }>>(`/api/v1/oa/unified-approval/initiated/${initiatorId}`, { params });
  },
  // 查询审批详情（查询审批实例详情及审批历史）
  getDetail: (instanceId: number) => {
    return api.get<AxiosResponse<{ code: number; msg: string; data: Record<string, any> }>>(`/api/v1/oa/unified-approval/detail/${instanceId}`);
  },
  // 查询各模块审批统计（查询各模块审批数量统计）
  getStatistics: () => {
    return api.get<AxiosResponse<{ code: number; msg: string; data: Record<string, any> }>>('/api/v1/oa/unified-approval/statistics');
  }
};

// OA API统一导出
export const oaApi = {
  approvalProcess: approvalProcessApi,
  approvalTask: approvalTaskApi,
  pendingApproval: pendingApprovalApi, // 新增
  approved: approvedApi, // 新增
  initiated: initiatedApi, // 新增
  unifiedApproval: unifiedApprovalApi, // 跨模块统一审批
  meeting: meetingApi,
  meetingRoom: meetingRoomApi,
  document: documentApi,
  documentCategory: documentCategoryApi,
  documentVersion: documentVersionApi
};

export default oaApi;