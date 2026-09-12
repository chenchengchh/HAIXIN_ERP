package com.hxcoe.oa.iam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hxcoe.oa.iam.entity.OaUserAccountEntity;
import com.hxcoe.oa.iam.repository.OaUserAccountRepository;
import com.hxcoe.oa.integration.HrEmployeeClient;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OaAccountStatusSyncServiceTest {

    @Test
    void syncFromHrEmployeesShouldEnableOrDisableAccounts() {
        HrEmployeeClient hrEmployeeClient = mock(HrEmployeeClient.class);
        OaUserAccountRepository oaUserAccountRepository = mock(OaUserAccountRepository.class);

        OaUserAccountEntity a1 = new OaUserAccountEntity();
        a1.setId(1L);
        a1.setUsername("EMP-00001");
        a1.setEmployeeId(1L);
        a1.setStatus("DISABLED");

        OaUserAccountEntity a2 = new OaUserAccountEntity();
        a2.setId(2L);
        a2.setUsername("EMP-00032");
        a2.setEmployeeId(32L);
        a2.setStatus("ACTIVE");

        when(hrEmployeeClient.listEmployees(any())).thenReturn(List.of(
                Map.of("id", 1, "status", "ACTIVE"),
                Map.of("id", 32, "status", "INACTIVE")
        ));
        when(oaUserAccountRepository.findByEmployeeIdIsNotNull()).thenReturn(List.of(a1, a2));
        when(oaUserAccountRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        OaAccountStatusSyncService service = new OaAccountStatusSyncService();
        ReflectionTestUtils.setField(service, "hrEmployeeClient", hrEmployeeClient);
        ReflectionTestUtils.setField(service, "oaUserAccountRepository", oaUserAccountRepository);

        Map<String, Object> res = service.syncFromHrEmployees();

        assertEquals("ACTIVE", a1.getStatus());
        assertEquals("DISABLED", a2.getStatus());
        assertEquals(2, res.get("accountTotal"));
        assertEquals(2, res.get("employeeTotal"));
        assertEquals(2, res.get("updated"));
        verify(oaUserAccountRepository).saveAll(any());
    }
}

