package com.hxcoe.oa.iam.bootstrap;

import com.hxcoe.oa.iam.entity.OaRoleEntity;
import com.hxcoe.oa.iam.entity.OaRolePermissionEntity;
import com.hxcoe.oa.iam.entity.OaUserAccountEntity;
import com.hxcoe.oa.iam.entity.OaUserRoleEntity;
import com.hxcoe.oa.iam.entity.OaPermissionEntity;
import com.hxcoe.oa.iam.repository.OaPermissionRepository;
import com.hxcoe.oa.iam.repository.OaRolePermissionRepository;
import com.hxcoe.oa.iam.repository.OaRoleRepository;
import com.hxcoe.oa.iam.repository.OaUserAccountRepository;
import com.hxcoe.oa.iam.repository.OaUserRoleRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "iam.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class IamBootstrapRunner implements ApplicationRunner {

    @Autowired
    private OaUserAccountRepository oaUserAccountRepository;

    @Autowired
    private OaRoleRepository oaRoleRepository;

    @Autowired
    private OaUserRoleRepository oaUserRoleRepository;

    @Autowired
    private OaPermissionRepository oaPermissionRepository;

    @Autowired
    private OaRolePermissionRepository oaRolePermissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 业务角色编码列表（对应审批流程模板各节点 assigneeRole）。
     * 格式：{角色编码, 角色名称}
     */
    private static final String[][] BUSINESS_ROLES = {
            {"sales_manager", "销售经理"},
            {"finance_manager", "财务经理"},
            {"gm", "总经理"},
            {"purchase_manager", "采购经理"},
            {"production_manager", "生产经理"},
            {"warehouse_manager", "仓库主管"},
            {"logistics_manager", "物流经理"},
            {"quality_manager", "质量经理"},
            {"legal", "法务"},
            {"service_manager", "客服主管"},
            {"scm_manager", "供应链经理"}
    };

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        OaRoleEntity adminRole = ensureRole("ADMIN", "管理员");
        ensureRole("USER", "普通用户");
        ensureDefaultPermissions(adminRole.getId());
        ensureAdminAccount(adminRole.getId());

        // 预置业务角色并将 admin 关联所有业务角色（基于权限部门的多节点会签审批流程）
        ensureBusinessRoles();
        ensureAdminAllBusinessRoles();
    }

    /**
     * 预置各业务模块审批所需的角色（防止 SQL 未执行时也能跑通）。
     */
    private void ensureBusinessRoles() {
        for (String[] role : BUSINESS_ROLES) {
            ensureRole(role[0], role[1]);
        }
    }

    /**
     * 将 admin 关联所有业务角色（兜底）。
     * <p>保证端到端测试可用 admin 一键通过所有审批节点；
     * 用 findRoleIdsByUserId 检查去重，避免重复关联。</p>
     */
    private void ensureAdminAllBusinessRoles() {
        Optional<OaUserAccountEntity> adminOpt = oaUserAccountRepository.findByUsername("admin");
        if (adminOpt.isEmpty()) {
            return;
        }
        Long adminUserId = adminOpt.get().getId();
        List<Long> existingRoleIds = oaUserRoleRepository.findRoleIdsByUserId(adminUserId);

        for (String[] role : BUSINESS_ROLES) {
            Optional<OaRoleEntity> roleOpt = oaRoleRepository.findByRoleCode(role[0]);
            if (roleOpt.isEmpty()) {
                continue;
            }
            Long roleId = roleOpt.get().getId();
            if (existingRoleIds != null && existingRoleIds.contains(roleId)) {
                continue; // 已关联，跳过
            }
            OaUserRoleEntity ur = new OaUserRoleEntity();
            ur.setUserId(adminUserId);
            ur.setRoleId(roleId);
            oaUserRoleRepository.save(ur);
        }
    }

    private OaRoleEntity ensureRole(String code, String name) {
        Optional<OaRoleEntity> existing = oaRoleRepository.findByRoleCode(code);
        if (existing.isPresent()) {
            return existing.get();
        }
        OaRoleEntity role = new OaRoleEntity();
        role.setRoleCode(code);
        role.setRoleName(name);
        role.setStatus("ACTIVE");
        return oaRoleRepository.save(role);
    }

    private void ensureAdminAccount(Long adminRoleId) {
        if (oaUserAccountRepository.existsByUsername("admin")) {
            return;
        }
        OaUserAccountEntity user = new OaUserAccountEntity();
        user.setUsername("admin");
        user.setPasswordHash(passwordEncoder.encode("admin123"));
        user.setStatus("ACTIVE");
        OaUserAccountEntity saved = oaUserAccountRepository.save(user);

        OaUserRoleEntity ur = new OaUserRoleEntity();
        ur.setUserId(saved.getId());
        ur.setRoleId(adminRoleId);
        oaUserRoleRepository.save(ur);
    }

    private void ensureDefaultPermissions(Long adminRoleId) {
        List<OaPermissionEntity> perms = List.of(
                ensurePermission("oa:approval:process:read", "审批流程-读"),
                ensurePermission("oa:approval:process:write", "审批流程-写"),
                ensurePermission("oa:approval:instance:read", "审批实例-读"),
                ensurePermission("oa:approval:instance:write", "审批实例-写"),
                ensurePermission("oa:approval:task:read", "审批任务-读"),
                ensurePermission("oa:approval:task:write", "审批任务-写")
        );

        for (OaPermissionEntity perm : perms) {
            if (perm == null || perm.getId() == null) {
                continue;
            }
            if (oaRolePermissionRepository.existsByRoleIdAndPermId(adminRoleId, perm.getId())) {
                continue;
            }
            OaRolePermissionEntity rp = new OaRolePermissionEntity();
            rp.setRoleId(adminRoleId);
            rp.setPermId(perm.getId());
            oaRolePermissionRepository.save(rp);
        }
    }

    private OaPermissionEntity ensurePermission(String code, String name) {
        Optional<OaPermissionEntity> existing = oaPermissionRepository.findByPermCode(code);
        if (existing.isPresent()) {
            return existing.get();
        }
        OaPermissionEntity p = new OaPermissionEntity();
        p.setPermCode(code);
        p.setPermName(name);
        p.setStatus("ACTIVE");
        return oaPermissionRepository.save(p);
    }
}
