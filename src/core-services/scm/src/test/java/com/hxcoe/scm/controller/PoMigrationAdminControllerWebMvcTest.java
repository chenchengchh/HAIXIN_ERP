package com.hxcoe.scm.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hxcoe.scm.entity.PoReconciliationDiffEntity;
import com.hxcoe.scm.repository.PoReconciliationDiffRepository;
import com.hxcoe.scm.service.PoMigrationReconciliationAdminService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PoMigrationAdminController.class)
class PoMigrationAdminControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PoMigrationReconciliationAdminService poMigrationReconciliationAdminService;

    @MockBean
    private PoReconciliationDiffRepository poReconciliationDiffRepository;

    @Test
    void migrateFromSrmShouldReturnCounts() throws Exception {
        when(poMigrationReconciliationAdminService.migrateFromSrmToScm()).thenReturn(Map.of(
                "insertedOrders", 1,
                "deletedItems", 2,
                "insertedItems", 3
        ));

        mockMvc.perform(post("/api/v1/scm/admin/po-migration/migrate-from-srm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.insertedOrders").value(1));
    }

    @Test
    void diffsPageShouldReturnPage() throws Exception {
        PoReconciliationDiffEntity e = new PoReconciliationDiffEntity();
        e.setId(1L);
        e.setOrderNo("PO-1");
        e.setMismatchReasons("STATUS_MISMATCH");
        e.setStatus("OPEN");
        Page<PoReconciliationDiffEntity> page = new PageImpl<>(List.of(e));
        when(poReconciliationDiffRepository.findByStatusOrderByUpdatedTimeDesc(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/scm/admin/po-migration/reconciliation/diffs/page")
                        .param("page", "1")
                        .param("size", "20")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("PO-1"));
    }
}

