package com.hxcoe.oa.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.result.Result;
import com.hxcoe.oa.controller.ApprovalActionRequest;
import com.hxcoe.oa.controller.UnifiedApprovalRequest;
import com.hxcoe.oa.entity.ApprovalProcessEntity;
import com.hxcoe.oa.entity.ApprovalProcessInstanceEntity;
import com.hxcoe.oa.entity.ApprovalTaskEntity;
import com.hxcoe.oa.repository.ApprovalProcessInstanceRepository;
import com.hxcoe.oa.repository.ApprovalProcessRepository;
import com.hxcoe.oa.repository.ApprovalTaskRepository;
import com.hxcoe.oa.service.ApprovalCallbackService;
import com.hxcoe.oa.service.ApprovalProcessInstanceService;
import com.hxcoe.oa.service.ApprovalTaskService;
import com.hxcoe.oa.service.ApproverResolverService;
import com.hxcoe.oa.service.ProcessDefinitionParser;
import com.hxcoe.oa.service.UnifiedApprovalService;
import com.hxcoe.oa.service.dto.ApproverCandidateDTO;
import com.hxcoe.oa.service.dto.ProcessNodeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 统一审批服务实现类。
 * <p>实现跨模块统一审批的核心逻辑，包括：</p>
 * <ul>
 *   <li>根据业务类型自动匹配审批流程模板</li>
 *   <li>创建审批实例并启动审批流程</li>
 *   <li>审批完成后触发回调通知发起方模块</li>
 *   <li>提供审批统计和详情查询</li>
 * </ul>
 */
@Service
public class UnifiedApprovalServiceImpl implements UnifiedApprovalService {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedApprovalServiceImpl.class);

    /** 业务类型→流程编码映射 */
    private static final Map<String, String> BUSINESS_TYPE_PROCESS_MAP = new HashMap<>();
    static {
        // CRM模块审批
        BUSINESS_TYPE_PROCESS_MAP.put("crm_sales_order", "CRM_SALES_ORDER_APPROVAL");
        BUSINESS_TYPE_PROCESS_MAP.put("crm_contract", "CRM_CONTRACT_APPROVAL");
        BUSINESS_TYPE_PROCESS_MAP.put("crm_return", "CRM_RETURN_APPROVAL");
        // SCM模块审批
        BUSINESS_TYPE_PROCESS_MAP.put("scm_purchase_order", "SCM_PURCHASE_ORDER_APPROVAL");
        BUSINESS_TYPE_PROCESS_MAP.put("scm_supplier", "SCM_SUPPLIER_APPROVAL");
        // ERP模块审批
        BUSINESS_TYPE_PROCESS_MAP.put("erp_production_order", "ERP_PRODUCTION_ORDER_APPROVAL");
        BUSINESS_TYPE_PROCESS_MAP.put("erp_voucher", "ERP_VOUCHER_APPROVAL");
        BUSINESS_TYPE_PROCESS_MAP.put("erp_supply_chain", "ERP_SUPPLY_CHAIN_APPROVAL");
        // WMS模块审批
        BUSINESS_TYPE_PROCESS_MAP.put("wms_outbound", "WMS_OUTBOUND_APPROVAL");
        BUSINESS_TYPE_PROCESS_MAP.put("wms_inbound", "WMS_INBOUND_APPROVAL");
        // MES模块审批
        BUSINESS_TYPE_PROCESS_MAP.put("mes_work_order", "MES_WORK_ORDER_APPROVAL");
        // LES模块审批
        BUSINESS_TYPE_PROCESS_MAP.put("les_transport_plan", "LES_TRANSPORT_PLAN_APPROVAL");
        // QMS模块审批
        BUSINESS_TYPE_PROCESS_MAP.put("qms_inspection", "QMS_INSPECTION_APPROVAL");
        // SRM模块审批
        BUSINESS_TYPE_PROCESS_MAP.put("srm_purchase_order", "SRM_PURCHASE_ORDER_APPROVAL");
        // APS模块审批
        BUSINESS_TYPE_PROCESS_MAP.put("aps_production_plan", "APS_PRODUCTION_PLAN_APPROVAL");
        // 通用审批
        BUSINESS_TYPE_PROCESS_MAP.put("general", "GENERAL_APPROVAL");
    }

    @Autowired
    private ApprovalProcessRepository approvalProcessRepository;

    @Autowired
    private ApprovalProcessInstanceRepository instanceRepository;

    @Autowired
    private ApprovalTaskRepository taskRepository;

    @Autowired
    private ApprovalProcessInstanceService instanceService;

    @Autowired
    private ApprovalTaskService taskService;

    @Autowired
    private ApprovalCallbackService callbackService;

    @Autowired
    private ObjectMapper objectMapper;

    /** 流程定义解析器（解析 process_definition JSON，定位审批节点） */
    @Autowired
    private ProcessDefinitionParser processDefinitionParser;

    /** 审批人解析服务（按角色+部门优先解析会签审批人，及权限校验） */
    @Autowired
    private ApproverResolverService approverResolverService;

    /**
     * 提交审批申请。
     * <p>根据业务类型自动匹配流程模板，解析流程定义取首节点，按节点角色+发起人部门
     * 解析会签审批人并批量创建 pending 任务（会签模式）。</p>
     *
     * @param request 审批申请请求
     * @return 审批实例
     */
    @Override
    @Transactional
    public ApprovalProcessInstanceEntity submitApproval(UnifiedApprovalRequest request) {
        logger.info("提交审批申请: sourceSystem={}, businessType={}, businessId={}",
                request.getSourceSystem(), request.getBusinessType(), request.getBusinessId());

        // 1. 根据业务类型匹配流程模板
        String processCode = BUSINESS_TYPE_PROCESS_MAP.getOrDefault(
                request.getSourceSystem() + "_" + request.getBusinessType(),
                BUSINESS_TYPE_PROCESS_MAP.getOrDefault(request.getBusinessType(), "GENERAL_APPROVAL"));

        ApprovalProcessEntity processTemplate = approvalProcessRepository.findByCode(processCode);
        if (processTemplate == null) {
            // 模板不存在则使用通用审批流程
            processTemplate = approvalProcessRepository.findByCode("GENERAL_APPROVAL");
            if (processTemplate == null) {
                throw new RuntimeException("未找到审批流程模板: " + processCode + "，且通用审批流程也不存在");
            }
            logger.info("使用通用审批流程模板替代: businessType={}", request.getBusinessType());
        }

        // 2. 解析流程定义，取首节点（不再硬编码 node_1）
        List<ProcessNodeDTO> nodes = processDefinitionParser.parse(processTemplate.getProcessDefinition());
        if (nodes.isEmpty()) {
            throw new RuntimeException("审批流程模板[" + processCode + "]无有效节点定义");
        }
        ProcessNodeDTO firstNode = nodes.get(0);
        logger.info("流程模板已解析: processCode={}, nodeCount={}, firstNode={}", processCode, nodes.size(), firstNode.getId());

        // 3. 构造流程变量（包含回调信息）
        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put("sourceSystem", request.getSourceSystem());
        processVariables.put("businessType", request.getBusinessType());
        processVariables.put("businessId", request.getBusinessId());
        processVariables.put("businessNo", request.getBusinessNo());
        processVariables.put("callbackUrl", request.getCallbackUrl());

        // 4. 构造表单数据
        String formDataJson;
        try {
            formDataJson = objectMapper.writeValueAsString(request.getFormData() != null ?
                    request.getFormData() : Collections.emptyMap());
        } catch (Exception e) {
            formDataJson = "{}";
        }

        // 5. 创建审批实例（currentNodeId/Name 取自首节点）
        ApprovalProcessInstanceEntity instance = new ApprovalProcessInstanceEntity();
        instance.setProcessId(processTemplate.getId());
        instance.setProcessCode(processTemplate.getCode());
        instance.setTitle(request.getTitle() != null ? request.getTitle() :
                request.getBusinessType() + "审批-" + request.getBusinessNo());
        instance.setDescription(request.getDescription());
        instance.setInitiatorId(request.getInitiatorId());
        instance.setInitiatorName(request.getInitiatorName());
        instance.setStatus("running");
        instance.setCurrentNodeId(firstNode.getId());
        instance.setCurrentNodeName(firstNode.getName());
        instance.setFormData(formDataJson);
        instance.setErpOrderId(request.getErpOrderId());
        instance.setScmSupplierId(request.getScmSupplierId());
        instance.setMesWorkshopId(request.getMesWorkshopId());
        instance.setStartTime(LocalDateTime.now());

        try {
            instance.setProcessVariables(objectMapper.writeValueAsString(processVariables));
        } catch (Exception e) {
            instance.setProcessVariables("{}");
        }

        instance = instanceRepository.save(instance);
        logger.info("审批实例已创建: instanceId={}, processCode={}, firstNode={}",
                instance.getId(), instance.getProcessCode(), firstNode.getId());

        // 6. 按首节点角色+发起人部门解析会签审批人，批量创建 pending 任务
        createTasksForNode(instance, processTemplate, firstNode, request.getInitiatorId());

        return instance;
    }

    /**
     * 为指定节点批量创建会签审批任务。
     * <p>调用审批人解析服务，按节点角色+发起人部门得到候选审批人列表，
     * 为每个候选创建一条 pending 任务（会签：所有人通过才流转）。</p>
     *
     * @param instance        审批实例
     * @param processTemplate 流程模板（含 processDefinition）
     * @param node            目标节点
     * @param initiatorUserId 发起人账号ID（用于部门优先解析）
     */
    private void createTasksForNode(ApprovalProcessInstanceEntity instance, ApprovalProcessEntity processTemplate,
                                    ProcessNodeDTO node, Long initiatorUserId) {
        List<ApproverCandidateDTO> approvers = approverResolverService.resolveApprovers(
                node.getAssigneeRole(), initiatorUserId);
        logger.info("节点[{}]解析到审批人{}名: {}", node.getId(), approvers.size(),
                approvers.stream()
                        .map(a -> a.getUserId() + "(" + a.getDisplayName() + ")")
                        .reduce((x, y) -> x + "," + y)
                        .orElse(""));

        for (ApproverCandidateDTO approver : approvers) {
            ApprovalTaskEntity task = new ApprovalTaskEntity();
            task.setInstanceId(instance.getId());
            task.setName(instance.getTitle());
            task.setDescription(instance.getDescription());
            task.setNodeId(node.getId());
            task.setNodeName(node.getName());
            task.setAssigneeRole(node.getAssigneeRole());
            task.setNodeIndex(node.getNodeIndex());
            task.setAssigneeId(approver.getUserId());
            task.setAssigneeName(approver.getDisplayName());
            task.setAssigneeDeptId(approver.getDepartmentId());
            task.setStatus("pending");
            taskRepository.save(task);
            logger.info("审批任务已创建: taskId={}, instanceId={}, nodeId={}, assigneeId={}",
                    task.getId(), instance.getId(), node.getId(), approver.getUserId());
        }
    }

    /**
     * 处理审批操作。
     * <p>执行三层权限校验后，按审批动作（approve/reject/return）处理任务：
     * approve 时判断会签是否全部完成以决定流转下一节点或实例完成；
     * reject 时批量取消同节点其他待办任务；回调仅在实例进入终态时触发一次。</p>
     *
     * @param taskId  审批任务ID
     * @param request 审批操作请求
     * @return 审批任务
     */
    @Override
    @Transactional
    public ApprovalTaskEntity processApproval(Long taskId, ApprovalActionRequest request) {
        logger.info("处理审批操作: taskId={}, action={}, approverId={}",
                taskId, request.getAction(), request.getApproverId());

        ApprovalTaskEntity task = taskService.getApprovalTaskById(taskId);
        if (task == null) {
            throw new RuntimeException("审批任务不存在: " + taskId);
        }
        if (!"pending".equals(task.getStatus())) {
            throw new RuntimeException("审批任务已处理，无法重复操作: " + taskId);
        }

        // ===== 权限三层校验 =====
        Long approverId = request.getApproverId();
        // 1. 审批人ID非空校验
        if (approverId == null) {
            throw new RuntimeException("审批人ID不能为空");
        }
        // 2. 角色权限校验：审批人必须具备节点 assigneeRole
        if (task.getAssigneeRole() != null && !task.getAssigneeRole().isBlank()) {
            if (!approverResolverService.hasRole(approverId, task.getAssigneeRole())) {
                throw new RuntimeException("当前用户无角色[" + task.getAssigneeRole() + "]审批权限");
            }
        }
        // 3. 任务归属校验：任务须分配给当前审批人
        if (!approverId.equals(task.getAssigneeId())) {
            throw new RuntimeException("该审批任务未分配给当前用户");
        }

        // 按动作分发处理
        String action = request.getAction();
        if ("approve".equalsIgnoreCase(action)) {
            handleApprove(task, request);
        } else if ("reject".equalsIgnoreCase(action)) {
            handleReject(task, request);
        } else if ("return".equalsIgnoreCase(action)) {
            handleReturn(task, request);
        } else {
            throw new RuntimeException("不支持的审批动作: " + action);
        }

        return task;
    }

    /**
     * 处理审批通过：完成任务，判断会签是否全部完成以决定流转或结束实例。
     * <p>会签流转逻辑：</p>
     * <ol>
     *   <li>统计同节点未完成任务数，&gt;0 则实例保持 running，不回调</li>
     *   <li>=0 时取下一节点；无下一节点则实例 approved 并回调一次</li>
     *   <li>有下一节点则解析会签审批人并批量建任务，更新 currentNodeId/Name</li>
     * </ol>
     *
     * @param task    审批任务
     * @param request 审批操作请求
     */
    private void handleApprove(ApprovalTaskEntity task, ApprovalActionRequest request) {
        // 更新当前任务为已完成
        task.setStatus("completed");
        task.setResult("approved");
        task.setComment(request.getComment());
        task.setApproveTime(LocalDateTime.now());
        taskRepository.save(task);

        ApprovalProcessInstanceEntity instance = instanceService.getApprovalProcessInstanceById(task.getInstanceId());
        if (instance == null) {
            logger.warn("审批实例不存在: instanceId={}", task.getInstanceId());
            return;
        }

        // 会签判断：同节点是否还有未完成任务
        long unfinished = taskRepository.countUnfinishedByInstanceAndNode(instance.getId(), task.getNodeId());
        if (unfinished > 0) {
            logger.info("会签未完成，实例保持running: instanceId={}, nodeId={}, 剩余={}",
                    instance.getId(), task.getNodeId(), unfinished);
            // 实例保持 running，不回调
            return;
        }

        // 本节点会签全部完成，尝试流转下一节点
        ApprovalProcessEntity processTemplate = approvalProcessRepository.findByCode(instance.getProcessCode());
        if (processTemplate == null) {
            logger.warn("流程模板不存在，直接结束实例: processCode={}", instance.getProcessCode());
            instance.setStatus("approved");
            instance.setEndTime(LocalDateTime.now());
            instance = instanceRepository.save(instance);
            callbackService.sendCallback(instance, "approved", request.getComment(), request.getApproverName());
            return;
        }

        ProcessNodeDTO nextNode = processDefinitionParser.getNextNode(
                processTemplate.getProcessDefinition(), task.getNodeId());
        if (nextNode == null) {
            // 无下一节点，实例通过并回调一次
            instance.setStatus("approved");
            instance.setEndTime(LocalDateTime.now());
            instance = instanceRepository.save(instance);
            logger.info("审批实例已通过: instanceId={}", instance.getId());
            callbackService.sendCallback(instance, "approved", request.getComment(), request.getApproverName());
        } else {
            // 流转到下一节点，批量创建会签任务
            createTasksForNode(instance, processTemplate, nextNode, instance.getInitiatorId());
            instance.setCurrentNodeId(nextNode.getId());
            instance.setCurrentNodeName(nextNode.getName());
            instance = instanceRepository.save(instance);
            logger.info("流转到下一节点: instanceId={}, nextNode={}", instance.getId(), nextNode.getId());
        }
    }

    /**
     * 处理审批拒绝：拒绝当前任务，批量取消同节点其他 pending 任务，实例置为 rejected 并回调一次。
     *
     * @param task    审批任务
     * @param request 审批操作请求
     */
    private void handleReject(ApprovalTaskEntity task, ApprovalActionRequest request) {
        task.setStatus("rejected");
        task.setResult("rejected");
        task.setComment(request.getComment());
        task.setApproveTime(LocalDateTime.now());
        taskRepository.save(task);

        // 批量取消同节点其他 pending 任务（避免其他审批人继续操作已无效任务）
        int cancelled = taskRepository.cancelPendingTasksByInstanceAndNode(task.getInstanceId(), task.getNodeId());
        logger.info("已取消同节点pending任务: instanceId={}, nodeId={}, cancelled={}",
                task.getInstanceId(), task.getNodeId(), cancelled);

        ApprovalProcessInstanceEntity instance = instanceService.getApprovalProcessInstanceById(task.getInstanceId());
        if (instance == null) {
            logger.warn("审批实例不存在: instanceId={}", task.getInstanceId());
            return;
        }
        instance.setStatus("rejected");
        instance.setEndTime(LocalDateTime.now());
        instance = instanceRepository.save(instance);
        logger.info("审批实例已拒绝: instanceId={}", instance.getId());
        callbackService.sendCallback(instance, "rejected", request.getComment(), request.getApproverName());
    }

    /**
     * 处理审批退回：退回当前任务，实例保持 running，不触发回调（待发起人修改后重新提交）。
     *
     * @param task    审批任务
     * @param request 审批操作请求
     */
    private void handleReturn(ApprovalTaskEntity task, ApprovalActionRequest request) {
        task.setStatus("returned");
        task.setResult("returned");
        task.setComment(request.getComment());
        task.setApproveTime(LocalDateTime.now());
        taskRepository.save(task);
        logger.info("审批任务已退回: taskId={}, instanceId={}", task.getId(), task.getInstanceId());
        // 实例保持 running，不回调
    }

    /**
     * 查询审批详情（含审批历史）。
     *
     * @param instanceId 审批实例ID
     * @return 审批详情
     */
    @Override
    public Map<String, Object> getApprovalDetail(Long instanceId) {
        Map<String, Object> detail = new HashMap<>();

        ApprovalProcessInstanceEntity instance = instanceService.getApprovalProcessInstanceById(instanceId);
        if (instance == null) {
            throw new RuntimeException("审批实例不存在: " + instanceId);
        }

        detail.put("instance", instance);

        // 查询审批任务历史
        List<ApprovalTaskEntity> tasks = taskRepository.findByInstanceIdOrderByCreateTimeAsc(instanceId);
        detail.put("tasks", tasks);

        // 解析表单数据
        try {
            Map<String, Object> formData = objectMapper.readValue(instance.getFormData(), Map.class);
            detail.put("formData", formData);
        } catch (Exception e) {
            detail.put("formData", Collections.emptyMap());
        }

        return detail;
    }

    /**
     * 查询各模块审批统计。
     * <p>返回总体统计、按状态统计、按业务模块统计，供前端跨模块审批中心展示。</p>
     *
     * @return 统计数据
     */
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总数统计（注意：字段名避免使用total，否则前端normalizeResponse会误判为分页数据）
        long totalInstances = instanceRepository.count();
        stats.put("totalInstances", totalInstances);

        // 按状态统计
        List<Object[]> statusStats = instanceRepository.countByStatus();
        Map<String, Long> statusMap = new HashMap<>();
        long pendingCount = 0L;
        long approvedCount = 0L;
        long rejectedCount = 0L;
        for (Object[] row : statusStats) {
            String status = row[0] == null ? "unknown" : String.valueOf(row[0]);
            Long count = row[1] instanceof Number n ? n.longValue() : 0L;
            statusMap.put(status, count);
            // 汇总常用状态计数（running/pending归为待审批，approved/completed归为已通过，rejected归为已拒绝）
            if ("running".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status)) {
                pendingCount += count;
            } else if ("approved".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) {
                approvedCount += count;
            } else if ("rejected".equalsIgnoreCase(status)) {
                rejectedCount += count;
            }
        }
        stats.put("byStatus", statusMap);
        stats.put("pending", pendingCount);
        stats.put("approved", approvedCount);
        stats.put("rejected", rejectedCount);

        // 待办任务数
        long pendingTasks = taskRepository.countByStatus("pending");
        stats.put("pendingTasks", pendingTasks);

        // 已完成任务数
        long completedTasks = taskRepository.countByStatus("completed");
        stats.put("completedTasks", completedTasks);

        // 按业务模块统计（通过processCode前缀分组，体现OA贯穿全系统各模块）
        Map<String, Map<String, Long>> moduleStats = buildModuleStats();
        stats.put("moduleStats", moduleStats);

        return stats;
    }

    /**
     * 构建各业务模块的审批统计。
     * <p>遍历所有审批实例，按processCode前缀（CRM/SCM/ERP/WMS/MES等）分组统计
     * 各模块的total/pending/approved/rejected数量。</p>
     *
     * @return 模块统计映射，键为模块编码（小写），值为该模块的统计数据
     */
    private Map<String, Map<String, Long>> buildModuleStats() {
        Map<String, Map<String, Long>> moduleStats = new HashMap<>();
        List<ApprovalProcessInstanceEntity> allInstances = instanceRepository.findAll();
        for (ApprovalProcessInstanceEntity instance : allInstances) {
            String processCode = instance.getProcessCode();
            if (processCode == null || processCode.isEmpty()) {
                continue;
            }
            // 提取模块编码（processCode格式如 CRM_SALES_ORDER_APPROVAL，取下划线前的部分并转小写）
            String moduleCode = extractModuleCode(processCode);
            if (moduleCode.isEmpty()) {
                continue;
            }
            Map<String, Long> moduleStat = moduleStats.computeIfAbsent(moduleCode, k -> new HashMap<>());
            moduleStat.merge("total", 1L, Long::sum);
            String status = instance.getStatus() == null ? "unknown" : instance.getStatus();
            if ("running".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status)) {
                moduleStat.merge("pending", 1L, Long::sum);
            } else if ("approved".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) {
                moduleStat.merge("approved", 1L, Long::sum);
            } else if ("rejected".equalsIgnoreCase(status)) {
                moduleStat.merge("rejected", 1L, Long::sum);
            }
        }
        return moduleStats;
    }

    /**
     * 从流程编码中提取模块编码。
     * <p>流程编码格式为 {MODULE}_{BUSINESS_TYPE}_APPROVAL，取第一个下划线前的部分并转小写。</p>
     *
     * @param processCode 流程编码
     * @return 模块编码（小写），无法识别时返回空字符串
     */
    private String extractModuleCode(String processCode) {
        if (processCode == null || processCode.isEmpty()) {
            return "";
        }
        int underscoreIndex = processCode.indexOf('_');
        if (underscoreIndex <= 0) {
            return processCode.toLowerCase();
        }
        return processCode.substring(0, underscoreIndex).toLowerCase();
    }
}
