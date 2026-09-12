package com.hxcoe.oa.service.impl;

import com.hxcoe.oa.entity.UserEntity;
import com.hxcoe.oa.iam.entity.OaRoleEntity;
import com.hxcoe.oa.iam.entity.OaUserAccountEntity;
import com.hxcoe.oa.iam.repository.OaRoleRepository;
import com.hxcoe.oa.iam.repository.OaUserAccountRepository;
import com.hxcoe.oa.iam.repository.OaUserRoleRepository;
import com.hxcoe.oa.repository.UserRepository;
import com.hxcoe.oa.service.ApproverResolverService;
import com.hxcoe.oa.service.dto.ApproverCandidateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 审批人解析服务实现。
 * <p>实现基于角色+部门优先的会签审批人解析策略：</p>
 * <ul>
 *   <li>按节点 assigneeRole 反查具备该角色的全部用户</li>
 *   <li>优先选择与发起人同部门的审批人；本部门无合适人选时全局回退</li>
 *   <li>admin 等无 employeeId 的账号作为全局兜底候选</li>
 * </ul>
 * <p>部门查询链路：oa_user_account.employee_id → hr_employee.id → department_id（无需 HR Feign）</p>
 */
@Service
public class ApproverResolverServiceImpl implements ApproverResolverService {

    private static final Logger logger = LoggerFactory.getLogger(ApproverResolverServiceImpl.class);

    @Autowired
    private OaRoleRepository oaRoleRepository;

    @Autowired
    private OaUserRoleRepository oaUserRoleRepository;

    @Autowired
    private OaUserAccountRepository oaUserAccountRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 按角色+发起人解析会签审批人列表（部门优先+全局回退）。
     *
     * @param assigneeRole    节点要求的角色编码
     * @param initiatorUserId 发起人账号ID
     * @return 审批人候选列表（非空）
     * @throws RuntimeException 当角色不存在或无可用审批人时抛出
     */
    @Override
    public List<ApproverCandidateDTO> resolveApprovers(String assigneeRole, Long initiatorUserId) {
        logger.info("解析审批人: role={}, initiatorUserId={}", assigneeRole, initiatorUserId);

        // 1. 按角色编码查角色
        OaRoleEntity role = oaRoleRepository.findByRoleCode(assigneeRole)
                .orElseThrow(() -> new RuntimeException("审批角色不存在: " + assigneeRole));

        // 2. 反查所有具备该角色的用户ID
        List<Long> userIds = oaUserRoleRepository.findUserIdsByRoleId(role.getId());
        if (userIds == null || userIds.isEmpty()) {
            throw new RuntimeException("角色[" + assigneeRole + "]无可用审批人");
        }

        // 3. 批量查账号，过滤 ACTIVE 状态
        List<OaUserAccountEntity> accounts = oaUserAccountRepository.findByIdIn(userIds);
        if (accounts == null || accounts.isEmpty()) {
            throw new RuntimeException("角色[" + assigneeRole + "]无可用审批人");
        }
        List<OaUserAccountEntity> activeAccounts = accounts.stream()
                .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.toList());
        if (activeAccounts.isEmpty()) {
            throw new RuntimeException("角色[" + assigneeRole + "]无可用审批人");
        }

        // 4. 解析发起人部门ID
        Long initiatorDeptId = resolveDepartmentId(initiatorUserId);
        logger.info("发起人部门ID: initiatorUserId={}, deptId={}", initiatorUserId, initiatorDeptId);

        // 5. 批量查 hr_employee 获取真实姓名与部门ID（避免 N+1 查询）
        List<Long> employeeIds = activeAccounts.stream()
                .map(OaUserAccountEntity::getEmployeeId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, UserEntity> userMap = new java.util.HashMap<>();
        if (!employeeIds.isEmpty()) {
            List<UserEntity> users = userRepository.findAllById(employeeIds);
            userMap = users.stream()
                    .filter(u -> u.getId() != null)
                    .collect(Collectors.toMap(UserEntity::getId, u -> u, (a, b) -> a));
        }

        // 6. 构建候选列表（displayName 优先真实姓名，无则用 username 兜底）
        List<ApproverCandidateDTO> candidates = new ArrayList<>(activeAccounts.size());
        for (OaUserAccountEntity acc : activeAccounts) {
            Long deptId = null;
            String displayName = acc.getUsername();
            if (acc.getEmployeeId() != null) {
                UserEntity user = userMap.get(acc.getEmployeeId());
                if (user != null) {
                    deptId = user.getDepartmentId();
                    if (user.getRealName() != null && !user.getRealName().isBlank()) {
                        displayName = user.getRealName();
                    }
                }
            }
            candidates.add(new ApproverCandidateDTO(acc.getId(), displayName, deptId));
        }

        // 7. 部门优先筛选：发起人部门非空时，优先取同部门候选；本部门无人则全局回退
        List<ApproverCandidateDTO> result;
        if (initiatorDeptId != null) {
            final Long finalDeptId = initiatorDeptId;
            List<ApproverCandidateDTO> sameDept = candidates.stream()
                    .filter(c -> finalDeptId.equals(c.getDepartmentId()))
                    .collect(Collectors.toList());
            if (sameDept.isEmpty()) {
                logger.info("本部门无角色[{}]审批人，全局回退", assigneeRole);
                result = candidates;
            } else {
                result = sameDept;
            }
        } else {
            // 发起人无部门（如 admin），直接使用全部候选
            result = candidates;
        }

        // 8. 按账号ID去重（LinkedHashMap 保序）
        Map<Long, ApproverCandidateDTO> dedup = new LinkedHashMap<>();
        for (ApproverCandidateDTO c : result) {
            dedup.putIfAbsent(c.getUserId(), c);
        }
        if (dedup.isEmpty()) {
            throw new RuntimeException("角色[" + assigneeRole + "]无可用审批人");
        }

        logger.info("审批人解析完成: role={}, count={}, approvers={}",
                assigneeRole, dedup.size(),
                dedup.values().stream()
                        .map(c -> c.getUserId() + "(" + c.getDisplayName() + ")")
                        .collect(Collectors.joining(",")));
        return new ArrayList<>(dedup.values());
    }

    /**
     * 校验用户是否具备指定角色。
     *
     * @param userId   用户账号ID
     * @param roleCode 角色编码
     * @return 具备角色返回 true，否则 false
     */
    @Override
    public boolean hasRole(Long userId, String roleCode) {
        if (userId == null || roleCode == null) {
            return false;
        }
        Optional<OaRoleEntity> roleOpt = oaRoleRepository.findByRoleCode(roleCode);
        if (roleOpt.isEmpty()) {
            return false;
        }
        List<Long> roleIds = oaUserRoleRepository.findRoleIdsByUserId(userId);
        return roleIds != null && roleIds.contains(roleOpt.get().getId());
    }

    /**
     * 解析用户所属部门ID。
     * <p>链路：oa_user_account.employee_id → hr_employee.id → department_id</p>
     *
     * @param userId 用户账号ID
     * @return 部门ID（账号不存在、无 employeeId 或员工无部门时返回 null）
     */
    private Long resolveDepartmentId(Long userId) {
        if (userId == null) {
            return null;
        }
        return oaUserAccountRepository.findById(userId)
                .map(OaUserAccountEntity::getEmployeeId)
                .filter(Objects::nonNull)
                .flatMap(userRepository::findById)
                .map(UserEntity::getDepartmentId)
                .orElse(null);
    }
}
