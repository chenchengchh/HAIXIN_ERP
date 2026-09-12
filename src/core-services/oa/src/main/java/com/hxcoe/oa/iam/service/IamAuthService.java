package com.hxcoe.oa.iam.service;

import com.hxcoe.oa.iam.entity.OaAuditLoginEntity;
import com.hxcoe.oa.iam.entity.OaPermissionEntity;
import com.hxcoe.oa.iam.entity.OaRoleEntity;
import com.hxcoe.oa.iam.entity.OaUserAccountEntity;
import com.hxcoe.oa.iam.repository.OaAuditLoginRepository;
import com.hxcoe.oa.iam.repository.OaPermissionRepository;
import com.hxcoe.oa.iam.repository.OaRolePermissionRepository;
import com.hxcoe.oa.iam.repository.OaRoleRepository;
import com.hxcoe.oa.iam.repository.OaUserAccountRepository;
import com.hxcoe.oa.iam.repository.OaUserRoleRepository;
import com.hxcoe.oa.iam.util.OaJwtUtils;
import com.hxcoe.oa.integration.HrEmployeeClient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class IamAuthService {

    @Autowired
    private OaUserAccountRepository oaUserAccountRepository;

    @Autowired
    private OaUserRoleRepository oaUserRoleRepository;

    @Autowired
    private OaRoleRepository oaRoleRepository;

    @Autowired
    private OaRolePermissionRepository oaRolePermissionRepository;

    @Autowired
    private OaPermissionRepository oaPermissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OaJwtUtils oaJwtUtils;

    @Autowired
    private OaAuditLoginRepository oaAuditLoginRepository;

    @Autowired
    private HrEmployeeClient hrEmployeeClient;

    @Transactional
    public Map<String, Object> login(String username, String password, String ip, String ua, String traceId) {
        OaUserAccountEntity user = username == null ? null : oaUserAccountRepository.findByUsername(username).orElse(null);
        if (user == null) {
            audit(null, username, ip, ua, "FAIL", traceId);
            return null;
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            audit(user.getId(), username, ip, ua, "DISABLED", traceId);
            return null;
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            audit(user.getId(), username, ip, ua, "NO_PASSWORD", traceId);
            return null;
        }
        if (password == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            audit(user.getId(), username, ip, ua, "FAIL", traceId);
            return null;
        }

        List<Long> roleIds = oaUserRoleRepository.findRoleIdsByUserId(user.getId());
        List<String> roleCodes = new ArrayList<>();
        if (roleIds != null && !roleIds.isEmpty()) {
            for (OaRoleEntity r : oaRoleRepository.findByIdIn(roleIds)) {
                if (r != null && "ACTIVE".equalsIgnoreCase(r.getStatus()) && r.getRoleCode() != null) {
                    roleCodes.add(r.getRoleCode());
                }
            }
        }

        List<String> permCodes = new ArrayList<>();
        if (roleIds != null && !roleIds.isEmpty()) {
            List<Long> permIds = oaRolePermissionRepository.findPermIdsByRoleIds(roleIds);
            if (permIds != null && !permIds.isEmpty()) {
                for (OaPermissionEntity p : oaPermissionRepository.findByIdIn(permIds)) {
                    if (p != null && "ACTIVE".equalsIgnoreCase(p.getStatus()) && p.getPermCode() != null) {
                        permCodes.add(p.getPermCode());
                    }
                }
            }
        }

        String primaryRole = roleCodes.isEmpty() ? "USER" : roleCodes.get(0);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("employeeId", user.getEmployeeId());
        claims.put("role", primaryRole);
        claims.put("roles", roleCodes);
        claims.put("permissions", permCodes);

        String token = oaJwtUtils.generateToken(user.getUsername(), claims);
        user.setLastLoginAt(LocalDateTime.now());
        oaUserAccountRepository.save(user);
        audit(user.getId(), username, ip, ua, "OK", traceId);

        Map<String, Object> res = new HashMap<>();
        res.put("token", token);
        res.put("tokenType", "Bearer");
        res.put("userId", user.getId());
        res.put("username", user.getUsername());
        res.put("employeeId", user.getEmployeeId());
        res.put("employee", hrEmployeeClient.getEmployeeById(user.getEmployeeId(), traceId));
        res.put("role", primaryRole);
        res.put("roles", roleCodes);
        res.put("permissions", permCodes);
        return res;
    }

    private void audit(Long userId, String username, String ip, String ua, String result, String traceId) {
        try {
            OaAuditLoginEntity a = new OaAuditLoginEntity();
            a.setUserId(userId);
            a.setUsername(username);
            a.setIp(ip);
            a.setUa(ua);
            a.setResult(result);
            a.setTraceId(traceId);
            oaAuditLoginRepository.save(a);
        } catch (Exception ex) {
            log.warn("OA 登录审计写入失败 error={}", ex.getMessage());
        }
    }
}
