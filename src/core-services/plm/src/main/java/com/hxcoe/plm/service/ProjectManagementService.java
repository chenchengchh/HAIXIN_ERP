package com.hxcoe.plm.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.plm.dto.GanttDataDTO;
import com.hxcoe.plm.dto.ProjectDTO;
import com.hxcoe.plm.dto.ResourceLoadDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectManagementService {
    /**
     * 分页查询项目列表（支持关键字/状态筛选）。
     */
    Result<PageResult<ProjectDTO>> getProjects(Pageable pageable, String keyword, String status);

    /**
     * 查询项目详情。
     */
    Result<ProjectDTO> getProjectById(Long id);

    /**
     * 创建项目。
     */
    Result<ProjectDTO> createProject(ProjectDTO dto);

    /**
     * 更新项目。
     */
    Result<ProjectDTO> updateProject(Long id, ProjectDTO dto);

    /**
     * 删除项目。
     */
    Result<Void> deleteProject(Long id);

    /**
     * 获取项目甘特图数据（任务+依赖）。
     */
    Result<GanttDataDTO> getGanttData(Long projectId);

    /**
     * 批量保存项目甘特图数据（覆盖写入）。
     */
    Result<GanttDataDTO> saveGanttData(Long projectId, GanttDataDTO data);

    /**
     * 更新任务进度与状态（供看板/甘特联动）。
     */
    Result<Void> updateTaskStatus(Long taskId, Integer progress, String status);

    /**
     * 获取资源负载列表（支持按项目/资源/日期范围筛选）。
     */
    Result<List<ResourceLoadDTO>> getResourceLoad(Long projectId, String resourceId, String startDate, String endDate);
}

