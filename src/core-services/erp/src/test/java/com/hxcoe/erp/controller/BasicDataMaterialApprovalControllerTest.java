package com.hxcoe.erp.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.erp.client.BomCategoryClient;
import com.hxcoe.erp.client.BomStructureClient;
import com.hxcoe.erp.client.CrmCustomerClient;
import com.hxcoe.erp.client.HrDepartmentClient;
import com.hxcoe.erp.client.HrEmployeeClient;
import com.hxcoe.erp.client.HrPositionClient;
import com.hxcoe.erp.client.SrmSupplierClient;
import com.hxcoe.erp.client.WmsLocationClient;
import com.hxcoe.erp.client.WmsWarehouseClient;
import com.hxcoe.erp.entity.MaterialEntity;
import com.hxcoe.erp.service.AccountService;
import com.hxcoe.erp.service.MaterialService;
import com.hxcoe.erp.service.OrganizationService;
import com.hxcoe.erp.service.SupplierService;
import com.hxcoe.erp.service.WarehouseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BasicDataController.class)
@TestPropertySource(properties = {
        "spring.jackson.property-naming-strategy=SNAKE_CASE"
})
public class BasicDataMaterialApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrganizationService organizationService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private SupplierService supplierService;

    @MockBean
    private MaterialService materialService;

    @MockBean
    private WarehouseService warehouseService;

    @MockBean
    private WmsWarehouseClient wmsWarehouseClient;

    @MockBean
    private WmsLocationClient wmsLocationClient;

    @MockBean
    private SrmSupplierClient srmSupplierClient;

    @MockBean
    private CrmCustomerClient crmCustomerClient;

    @MockBean
    private HrDepartmentClient hrDepartmentClient;

    @MockBean
    private HrPositionClient hrPositionClient;

    @MockBean
    private HrEmployeeClient hrEmployeeClient;

    @MockBean
    private BomCategoryClient bomCategoryClient;

    @MockBean
    private BomStructureClient bomStructureClient;

    @MockBean
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void shouldApproveMaterial() throws Exception {
        MaterialEntity approved = new MaterialEntity();
        approved.setId(1L);
        approved.setApprovalStatus("approved");
        when(materialService.approveMaterial(anyLong())).thenReturn(approved);

        mockMvc.perform(post("/api/v1/erp/basic-data/materials/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.approval_status").value("approved"));
    }

    @Test
    void shouldBatchApproveMaterials() throws Exception {
        when(materialService.batchApproveMaterials(anyList())).thenReturn(2);

        mockMvc.perform(post("/api/v1/erp/basic-data/materials/batch-approve")
                        .contentType("application/json")
                        .content("{\"ids\":[1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.approved_count").value(2));
    }

    @Test
    void shouldReturn404WhenMaterialMissing() throws Exception {
        when(materialService.approveMaterial(anyLong())).thenReturn(null);

        mockMvc.perform(post("/api/v1/erp/basic-data/materials/99/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("物料不存在"));
    }

    @Test
    void shouldAdaptCrmCustomerResultToApiResponse() throws Exception {
        when(crmCustomerClient.getById(anyLong())).thenReturn(Result.success(Map.of("id", 7, "customerName", "客户A")));

        mockMvc.perform(get("/api/v1/erp/basic-data/customers/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(7));
    }

    @Test
    void shouldAdaptHrDepartmentDeleteNotFound() throws Exception {
        when(hrDepartmentClient.delete(anyLong())).thenReturn(Result.notFound("部门不存在"));

        mockMvc.perform(delete("/api/v1/erp/basic-data/departments/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("部门不存在"));
    }

    @Test
    void shouldReturn400WhenWarehouseWriteDisabled() throws Exception {
        mockMvc.perform(post("/api/v1/erp/basic-data/warehouses")
                        .contentType("application/json")
                        .content("{\"warehouse_code\":\"WH-1\",\"warehouse_name\":\"主仓\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("仓库/库位权威归 WMS，ERP 禁止写入"));
    }
}
