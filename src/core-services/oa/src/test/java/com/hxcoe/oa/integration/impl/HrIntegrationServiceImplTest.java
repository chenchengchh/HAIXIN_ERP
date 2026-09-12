package com.hxcoe.oa.integration.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hxcoe.common.result.Result;
import com.hxcoe.oa.entity.DepartmentEntity;
import com.hxcoe.oa.entity.UserEntity;
import com.hxcoe.oa.integration.client.HrDepartmentFeignClient;
import com.hxcoe.oa.integration.client.HrEmployeeFeignClient;
import com.hxcoe.oa.integration.dto.HrDepartmentDTO;
import com.hxcoe.oa.integration.dto.HrEmployeeDTO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class HrIntegrationServiceImplTest {

    @Test
    void getAllEmployeesShouldMapHrDtoToOaUserEntity() {
        HrEmployeeFeignClient employeeFeignClient = mock(HrEmployeeFeignClient.class);
        HrDepartmentFeignClient departmentFeignClient = mock(HrDepartmentFeignClient.class);

        HrEmployeeDTO employee = new HrEmployeeDTO();
        employee.setId(1001L);
        employee.setEmployeeCode("EMP-1001");
        employee.setName("张三");
        employee.setDepartmentId(20L);
        employee.setDepartmentName("研发中心");
        employee.setPositionName("开发工程师");
        employee.setEmail("zhangsan@example.com");
        employee.setPhone("13800000000");
        employee.setStatus("ACTIVE");

        when(employeeFeignClient.listAll(any())).thenReturn(Result.success(List.of(employee)));

        HrIntegrationServiceImpl service = new HrIntegrationServiceImpl();
        ReflectionTestUtils.setField(service, "hrEmployeeFeignClient", employeeFeignClient);
        ReflectionTestUtils.setField(service, "hrDepartmentFeignClient", departmentFeignClient);

        List<UserEntity> users = service.getAllEmployees();

        assertEquals(1, users.size());
        assertEquals(1001L, users.get(0).getId());
        assertEquals(1001L, users.get(0).getEmployeeId());
        assertEquals("EMP-1001", users.get(0).getUsername());
        assertEquals("张三", users.get(0).getRealName());
        assertEquals("研发中心", users.get(0).getDepartmentName());
        assertEquals("开发工程师", users.get(0).getPosition());
        assertEquals(1, users.get(0).getStatus());
    }

    @Test
    void getDepartmentByCodeShouldMapHrDtoToOaDepartmentEntity() {
        HrEmployeeFeignClient employeeFeignClient = mock(HrEmployeeFeignClient.class);
        HrDepartmentFeignClient departmentFeignClient = mock(HrDepartmentFeignClient.class);

        HrDepartmentDTO department = new HrDepartmentDTO();
        department.setId(200L);
        department.setDepartmentCode("RD");
        department.setName("研发中心");
        department.setDescription("负责产品研发");
        department.setParentId(10L);
        department.setParentName("总部");
        department.setManagerId(1001L);
        department.setManagerName("张三");
        department.setLevel(2);
        department.setSort(5);
        department.setStatus("INACTIVE");

        when(departmentFeignClient.getByCode(eq("RD"), any())).thenReturn(Result.success(department));

        HrIntegrationServiceImpl service = new HrIntegrationServiceImpl();
        ReflectionTestUtils.setField(service, "hrEmployeeFeignClient", employeeFeignClient);
        ReflectionTestUtils.setField(service, "hrDepartmentFeignClient", departmentFeignClient);

        DepartmentEntity entity = service.getDepartmentByCode("RD").orElseThrow();

        assertEquals(200L, entity.getId());
        assertEquals("RD", entity.getCode());
        assertEquals("研发中心", entity.getName());
        assertEquals("总部", entity.getParentName());
        assertEquals(1001L, entity.getManagerId());
        assertEquals(2, entity.getLevel());
        assertEquals(5, entity.getSort());
        assertEquals(0, entity.getStatus());
    }

    @Test
    void getEmployeesByDepartmentIdShouldAcceptApiResponse200() {
        HrEmployeeFeignClient employeeFeignClient = mock(HrEmployeeFeignClient.class);
        HrDepartmentFeignClient departmentFeignClient = mock(HrDepartmentFeignClient.class);

        HrEmployeeDTO employee = new HrEmployeeDTO();
        employee.setId(1002L);
        employee.setEmployeeCode("EMP-1002");
        employee.setName("李四");
        employee.setDepartmentId(30L);
        employee.setDepartmentName("供应链中心");
        employee.setStatus("ACTIVE");

        when(employeeFeignClient.listByDepartmentId(eq(30L), any())).thenReturn(Result.error(200, "请求成功", List.of(employee)));

        HrIntegrationServiceImpl service = new HrIntegrationServiceImpl();
        ReflectionTestUtils.setField(service, "hrEmployeeFeignClient", employeeFeignClient);
        ReflectionTestUtils.setField(service, "hrDepartmentFeignClient", departmentFeignClient);

        List<UserEntity> users = service.getEmployeesByDepartmentId(30L);

        assertEquals(1, users.size());
        assertEquals("EMP-1002", users.get(0).getUsername());
        assertEquals("供应链中心", users.get(0).getDepartmentName());
    }
}
