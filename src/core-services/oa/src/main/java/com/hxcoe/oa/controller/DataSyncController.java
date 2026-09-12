package com.hxcoe.oa.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.oa.service.DataSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据同步控制器
 * 用于手动触发数据同步
 */
@RestController
@RequestMapping("/api/v1/oa/data-sync")
@Tag(name = "数据同步", description = "数据同步相关接口")
public class DataSyncController {

    private static final Logger logger = LoggerFactory.getLogger(DataSyncController.class);

    @Autowired
    private DataSyncService dataSyncService;

    /**
     * 同步所有HR数据到OA模块
     * @return 同步结果
     */
    @PostMapping("/hr/all")
    @Operation(summary = "同步所有HR数据", description = "将HR模块的部门和员工数据同步到OA模块")
    public Result<String> syncAllHrData() {
        logger.info("手动触发同步所有HR数据");
        try {
            dataSyncService.syncAllHrData();
            return Result.success("HR数据同步成功");
        } catch (Exception e) {
            logger.error("同步HR数据失败", e);
            return Result.error("同步HR数据失败：" + e.getMessage());
        }
    }

    /**
     * 同步HR部门数据到OA模块
     * @return 同步结果
     */
    @PostMapping("/hr/departments")
    @Operation(summary = "同步HR部门数据", description = "将HR模块的部门数据同步到OA模块")
    public Result<String> syncHrDepartments() {
        logger.info("手动触发同步HR部门数据");
        try {
            dataSyncService.syncHrDepartments();
            return Result.success("HR部门数据同步成功");
        } catch (Exception e) {
            logger.error("同步HR部门数据失败", e);
            return Result.error("同步HR部门数据失败：" + e.getMessage());
        }
    }

    /**
     * 同步HR员工数据到OA模块
     * @return 同步结果
     */
    @PostMapping("/hr/employees")
    @Operation(summary = "同步HR员工数据", description = "将HR模块的员工数据同步到OA模块")
    public Result<String> syncHrEmployees() {
        logger.info("手动触发同步HR员工数据");
        try {
            dataSyncService.syncHrEmployees();
            return Result.success("HR员工数据同步成功");
        } catch (Exception e) {
            logger.error("同步HR员工数据失败", e);
            return Result.error("同步HR员工数据失败：" + e.getMessage());
        }
    }

    /**
     * 根据部门ID同步该部门的员工数据
     * @param departmentId 部门ID
     * @return 同步结果
     */
    @PostMapping("/hr/employees/department")
    @Operation(summary = "同步部门员工数据", description = "根据部门ID同步该部门的员工数据到OA模块")
    public Result<String> syncEmployeesByDepartment(@RequestParam Long departmentId) {
        logger.info("手动触发同步部门ID为{}的员工数据", departmentId);
        try {
            dataSyncService.syncEmployeesByDepartment(departmentId);
            return Result.success("部门员工数据同步成功");
        } catch (Exception e) {
            logger.error("同步部门员工数据失败", e);
            return Result.error("同步部门员工数据失败：" + e.getMessage());
        }
    }
}
