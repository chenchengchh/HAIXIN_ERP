package com.hxcoe.oa.iam.service;

import com.hxcoe.oa.iam.entity.OaUserAccountEntity;
import com.hxcoe.oa.iam.repository.OaUserAccountRepository;
import com.hxcoe.oa.integration.HrEmployeeClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class OaAccountStatusSyncService {

    @Autowired
    private HrEmployeeClient hrEmployeeClient;

    @Autowired
    private OaUserAccountRepository oaUserAccountRepository;

    @Transactional
    public Map<String, Object> syncFromHrEmployees() {
        String traceId = UUID.randomUUID().toString();
        List<Map<String, Object>> employees = hrEmployeeClient.listEmployees(traceId);
        Map<Long, String> statusByEmployeeId = new HashMap<>();
        for (Map<String, Object> e : employees) {
            Long id = parseLong(e == null ? null : e.get("id"));
            if (id == null) {
                continue;
            }
            String st = e.get("status") == null ? null : String.valueOf(e.get("status"));
            statusByEmployeeId.put(id, st);
        }

        List<OaUserAccountEntity> accounts = oaUserAccountRepository.findByEmployeeIdIsNotNull();
        int total = accounts == null ? 0 : accounts.size();
        int updated = 0;
        int disabled = 0;
        int enabled = 0;
        List<OaUserAccountEntity> changed = new ArrayList<>();
        if (accounts != null) {
            for (OaUserAccountEntity a : accounts) {
                if (a == null || a.getEmployeeId() == null) {
                    continue;
                }
                String hrStatus = statusByEmployeeId.get(a.getEmployeeId());
                String desired = desiredAccountStatus(hrStatus);
                if (a.getStatus() != null && a.getStatus().equalsIgnoreCase(desired)) {
                    continue;
                }
                a.setStatus(desired);
                changed.add(a);
                updated++;
                if ("ACTIVE".equalsIgnoreCase(desired)) {
                    enabled++;
                } else {
                    disabled++;
                }
            }
        }
        if (!changed.isEmpty()) {
            oaUserAccountRepository.saveAll(changed);
        }
        if (updated > 0) {
            log.info("OA 账号启停同步完成 total={} updated={} enabled={} disabled={}", total, updated, enabled, disabled);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("accountTotal", total);
        res.put("employeeTotal", employees == null ? 0 : employees.size());
        res.put("updated", updated);
        res.put("enabled", enabled);
        res.put("disabled", disabled);
        return res;
    }

    private String desiredAccountStatus(String hrStatus) {
        if (hrStatus == null || hrStatus.isBlank()) {
            return "DISABLED";
        }
        if ("ACTIVE".equalsIgnoreCase(hrStatus.trim())) {
            return "ACTIVE";
        }
        return "DISABLED";
    }

    private Long parseLong(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        String s = String.valueOf(v).trim();
        if (s.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(s);
        } catch (Exception ignored) {
            return null;
        }
    }
}

