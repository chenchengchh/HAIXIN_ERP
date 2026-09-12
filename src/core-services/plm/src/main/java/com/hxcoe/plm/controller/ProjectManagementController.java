package com.hxcoe.plm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResultAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.plm.dto.GanttDataDTO;
import com.hxcoe.plm.dto.GanttSaveRequest;
import com.hxcoe.plm.dto.ProjectDTO;
import com.hxcoe.plm.dto.ResourceLoadDTO;
import com.hxcoe.plm.service.ProjectManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/plm", "/api/v1/plm"})
public class ProjectManagementController {

    @Autowired
    private ProjectManagementService projectManagementService;

    /**
     * 获取项目列表（分页+筛选）。
     */
    @GetMapping("/projects")
    public ApiResponse<PageResult<ProjectDTO>> getProjects(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize);
        return ResultAdapter.fromResult(projectManagementService.getProjects(pageable, keyword, status));
    }

    /**
     * 获取项目详情。
     */
    @GetMapping("/projects/{id}")
    public ApiResponse<ProjectDTO> getProjectById(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(projectManagementService.getProjectById(id));
    }

    /**
     * 创建项目。
     */
    @PostMapping("/projects")
    public ApiResponse<ProjectDTO> createProject(@RequestBody ProjectDTO dto) {
        return ResultAdapter.fromResult(projectManagementService.createProject(dto));
    }

    /**
     * 更新项目。
     */
    @PutMapping("/projects/{id}")
    public ApiResponse<ProjectDTO> updateProject(@PathVariable("id") Long id, @RequestBody ProjectDTO dto) {
        return ResultAdapter.fromResult(projectManagementService.updateProject(id, dto));
    }

    /**
     * 删除项目。
     */
    @DeleteMapping("/projects/{id}")
    public ApiResponse<Void> deleteProject(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(projectManagementService.deleteProject(id));
    }

    /**
     * 获取项目甘特图数据。
     */
    @GetMapping("/projects/{projectId}/gantt")
    public ApiResponse<GanttDataDTO> getGanttData(@PathVariable("projectId") Long projectId) {
        return ResultAdapter.fromResult(projectManagementService.getGanttData(projectId));
    }

    /**
     * 保存项目甘特图任务与依赖（覆盖写入）。
     */
    @PostMapping("/projects/{projectId}/tasks")
    public ApiResponse<GanttDataDTO> saveGanttData(@PathVariable("projectId") Long projectId, @RequestBody GanttSaveRequest request) {
        GanttDataDTO data = new GanttDataDTO();
        data.setData(request == null ? null : request.getTasks());
        data.setLinks(request == null ? null : request.getLinks());
        return ResultAdapter.fromResult(projectManagementService.saveGanttData(projectId, data));
    }

    /**
     * 更新任务进度与状态。
     */
    @PutMapping("/tasks/{taskId}/status")
    public ApiResponse<Void> updateTaskStatus(
            @PathVariable("taskId") Long taskId,
            @RequestBody(required = false) TaskStatusRequest request) {
        Integer progress = request == null ? null : request.getProgress();
        String status = request == null ? null : request.getStatus();
        return ResultAdapter.fromResult(projectManagementService.updateTaskStatus(taskId, progress, status));
    }

    /**
     * 获取资源负载。
     */
    @GetMapping("/resource-load")
    public ApiResponse<List<ResourceLoadDTO>> getResourceLoad(
            @RequestParam(name = "projectId", required = false) Long projectId,
            @RequestParam(name = "resourceId", required = false) String resourceId,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate) {
        return ResultAdapter.fromResult(projectManagementService.getResourceLoad(projectId, resourceId, startDate, endDate));
    }

    public static class TaskStatusRequest {
        private Integer progress;
        private String status;

        public Integer getProgress() {
            return progress;
        }

        public void setProgress(Integer progress) {
            this.progress = progress;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
