package com.hxcoe.plm.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.plm.dto.GanttDataDTO;
import com.hxcoe.plm.dto.GanttLinkDTO;
import com.hxcoe.plm.dto.GanttTaskDTO;
import com.hxcoe.plm.dto.ProjectDTO;
import com.hxcoe.plm.dto.ResourceLoadDTO;
import com.hxcoe.plm.entity.GanttLinkEntity;
import com.hxcoe.plm.entity.ProjectEntity;
import com.hxcoe.plm.entity.ProjectTaskEntity;
import com.hxcoe.plm.entity.ResourceLoadEntity;
import com.hxcoe.plm.repository.GanttLinkRepository;
import com.hxcoe.plm.repository.ProjectRepository;
import com.hxcoe.plm.repository.ProjectTaskRepository;
import com.hxcoe.plm.repository.ResourceLoadRepository;
import com.hxcoe.plm.service.ProjectManagementService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProjectManagementServiceImpl implements ProjectManagementService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private GanttLinkRepository ganttLinkRepository;

    @Autowired
    private ResourceLoadRepository resourceLoadRepository;

    /**
     * 分页查询项目列表（支持关键字/状态筛选）。
     */
    @Override
    public Result<PageResult<ProjectDTO>> getProjects(Pageable pageable, String keyword, String status) {
        Specification<ProjectEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeValue = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("projectName"), likeValue),
                        cb.like(root.get("projectCode"), likeValue)
                ));
            }

            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ProjectEntity> page = projectRepository.findAll(spec, pageable);
        List<ProjectDTO> dtos = page.getContent().stream().map(this::toProjectDTO).collect(Collectors.toList());
        PageResult<ProjectDTO> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, dtos);
        return Result.success(pageResult);
    }

    /**
     * 查询项目详情。
     */
    @Override
    public Result<ProjectDTO> getProjectById(Long id) {
        ProjectEntity entity = projectRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("项目不存在");
        }
        return Result.success(toProjectDTO(entity));
    }

    /**
     * 创建项目。
     */
    @Override
    public Result<ProjectDTO> createProject(ProjectDTO dto) {
        if (dto == null) {
            return Result.error("参数不能为空");
        }
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            return Result.error("项目编码不能为空");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return Result.error("项目名称不能为空");
        }

        ProjectEntity entity = new ProjectEntity();
        entity.setProjectCode(dto.getCode().trim());
        entity.setProjectName(dto.getName().trim());
        entity.setManager(dto.getManager());
        entity.setProjectType(dto.getType());
        entity.setStatus(dto.getStatus() == null || dto.getStatus().isEmpty() ? "planning" : dto.getStatus());
        entity.setProgress(dto.getProgress() == null ? 0 : dto.getProgress());
        entity.setStartDate(parseLocalDate(dto.getStartDate()));
        entity.setEndDate(parseLocalDate(dto.getEndDate()));
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());

        ProjectEntity saved = projectRepository.save(entity);
        return Result.success(toProjectDTO(saved));
    }

    /**
     * 更新项目。
     */
    @Override
    public Result<ProjectDTO> updateProject(Long id, ProjectDTO dto) {
        ProjectEntity existing = projectRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("项目不存在");
        }

        if (dto != null) {
            if (dto.getCode() != null && !dto.getCode().trim().isEmpty()) {
                existing.setProjectCode(dto.getCode().trim());
            }
            if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
                existing.setProjectName(dto.getName().trim());
            }
            if (dto.getManager() != null) {
                existing.setManager(dto.getManager());
            }
            if (dto.getType() != null) {
                existing.setProjectType(dto.getType());
            }
            if (dto.getStatus() != null) {
                existing.setStatus(dto.getStatus());
            }
            if (dto.getProgress() != null) {
                existing.setProgress(dto.getProgress());
            }
            if (dto.getStartDate() != null) {
                existing.setStartDate(parseLocalDate(dto.getStartDate()));
            }
            if (dto.getEndDate() != null) {
                existing.setEndDate(parseLocalDate(dto.getEndDate()));
            }
        }

        existing.setUpdatedTime(LocalDateTime.now());
        ProjectEntity saved = projectRepository.save(existing);
        return Result.success(toProjectDTO(saved));
    }

    /**
     * 删除项目。
     */
    @Transactional
    @Override
    public Result<Void> deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            return Result.error("项目不存在");
        }
        projectTaskRepository.deleteByProjectId(id);
        ganttLinkRepository.deleteByProjectId(id);
        projectRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 获取项目甘特图数据（任务+依赖）。
     */
    @Override
    public Result<GanttDataDTO> getGanttData(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            return Result.error("项目不存在");
        }

        List<ProjectTaskEntity> tasks = projectTaskRepository.findByProjectIdOrderByIdAsc(projectId);
        List<GanttLinkEntity> links = ganttLinkRepository.findByProjectIdOrderByIdAsc(projectId);

        GanttDataDTO data = new GanttDataDTO();
        data.setData(tasks.stream().map(this::toGanttTaskDTO).collect(Collectors.toList()));
        data.setLinks(links.stream().map(this::toGanttLinkDTO).collect(Collectors.toList()));
        return Result.success(data);
    }

    /**
     * 批量保存项目甘特图数据（覆盖写入）。
     */
    @Transactional
    @Override
    public Result<GanttDataDTO> saveGanttData(Long projectId, GanttDataDTO data) {
        if (!projectRepository.existsById(projectId)) {
            return Result.error("项目不存在");
        }
        if (data == null) {
            return Result.error("参数不能为空");
        }

        List<GanttTaskDTO> tasks = data.getData() == null ? Collections.emptyList() : data.getData();
        List<GanttLinkDTO> links = data.getLinks() == null ? Collections.emptyList() : data.getLinks();

        if (tasks.stream().anyMatch(t -> t.getId() == null)) {
            return Result.error("任务ID不能为空");
        }
        if (links.stream().anyMatch(l -> l.getId() == null)) {
            return Result.error("链接ID不能为空");
        }

        projectTaskRepository.deleteByProjectId(projectId);
        ganttLinkRepository.deleteByProjectId(projectId);

        List<ProjectTaskEntity> entities = tasks.stream().map(t -> toProjectTaskEntity(projectId, t)).collect(Collectors.toList());
        List<GanttLinkEntity> linkEntities = links.stream().map(l -> toGanttLinkEntity(projectId, l)).collect(Collectors.toList());

        if (!entities.isEmpty()) {
            projectTaskRepository.saveAll(entities);
        }
        if (!linkEntities.isEmpty()) {
            ganttLinkRepository.saveAll(linkEntities);
        }

        return getGanttData(projectId);
    }

    /**
     * 更新任务进度与状态（供看板/甘特联动）。
     */
    @Transactional
    @Override
    public Result<Void> updateTaskStatus(Long taskId, Integer progress, String status) {
        ProjectTaskEntity task = projectTaskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return Result.error("任务不存在");
        }
        if (progress != null) {
            task.setProgress(progress);
        }
        if (status != null) {
            task.setStatus(status);
        }
        task.setUpdatedTime(LocalDateTime.now());
        projectTaskRepository.save(task);
        return Result.success();
    }

    /**
     * 获取资源负载列表（支持按项目/资源/日期范围筛选）。
     */
    @Override
    public Result<List<ResourceLoadDTO>> getResourceLoad(Long projectId, String resourceId, String startDate, String endDate) {
        Specification<ResourceLoadEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (projectId != null) {
                predicates.add(cb.equal(root.get("projectId"), projectId));
            }
            if (resourceId != null && !resourceId.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("resourceId"), resourceId.trim()));
            }

            LocalDate start = parseLocalDate(startDate);
            LocalDate end = parseLocalDate(endDate);
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("loadDate"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("loadDate"), end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<ResourceLoadEntity> list = resourceLoadRepository.findAll(spec);
        List<ResourceLoadDTO> dtos = list.stream().map(this::toResourceLoadDTO).collect(Collectors.toList());
        return Result.success(dtos);
    }

    private ProjectDTO toProjectDTO(ProjectEntity entity) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(entity.getId() == null ? null : String.valueOf(entity.getId()));
        dto.setCode(entity.getProjectCode());
        dto.setName(entity.getProjectName());
        dto.setManager(entity.getManager());
        dto.setType(entity.getProjectType());
        dto.setStatus(entity.getStatus());
        dto.setProgress(entity.getProgress());
        dto.setStartDate(entity.getStartDate() == null ? null : entity.getStartDate().toString());
        dto.setEndDate(entity.getEndDate() == null ? null : entity.getEndDate().toString());
        return dto;
    }

    private GanttTaskDTO toGanttTaskDTO(ProjectTaskEntity entity) {
        GanttTaskDTO dto = new GanttTaskDTO();
        dto.setId(entity.getId());
        dto.setText(entity.getTaskName());
        dto.setStartDate(entity.getStartDate() == null ? null : entity.getStartDate().toString());
        dto.setDuration(entity.getDuration());
        dto.setProgress(entity.getProgress());
        dto.setParent(entity.getParentId() == null ? 0L : entity.getParentId());
        dto.setType(entity.getTaskType());
        // 补充负责人、状态与结束日期，供项目详情任务列表展示
        dto.setAssignee(entity.getAssignee());
        dto.setStatus(entity.getStatus());
        dto.setEndDate(entity.getEndDate() == null ? null : entity.getEndDate().toString());
        return dto;
    }

    private GanttLinkDTO toGanttLinkDTO(GanttLinkEntity entity) {
        GanttLinkDTO dto = new GanttLinkDTO();
        dto.setId(entity.getId());
        dto.setSource(entity.getSourceTaskId());
        dto.setTarget(entity.getTargetTaskId());
        dto.setType(entity.getLinkType());
        return dto;
    }

    private ResourceLoadDTO toResourceLoadDTO(ResourceLoadEntity entity) {
        ResourceLoadDTO dto = new ResourceLoadDTO();
        dto.setResourceId(entity.getResourceId());
        dto.setResourceName(entity.getResourceName());
        dto.setLoad(entity.getLoadValue());
        dto.setDate(entity.getLoadDate() == null ? null : entity.getLoadDate().toString());
        return dto;
    }

    private ProjectTaskEntity toProjectTaskEntity(Long projectId, GanttTaskDTO dto) {
        ProjectTaskEntity entity = new ProjectTaskEntity();
        entity.setId(dto.getId());
        entity.setProjectId(projectId);
        entity.setTaskName(dto.getText());
        entity.setProgress(dto.getProgress());
        entity.setDuration(dto.getDuration());
        entity.setParentId(dto.getParent() == null || Objects.equals(dto.getParent(), 0L) ? null : dto.getParent());
        entity.setTaskType(dto.getType());
        // 保存时透传负责人与状态，避免覆盖写入后丢失
        entity.setAssignee(dto.getAssignee());
        entity.setStatus(dto.getStatus() == null || dto.getStatus().trim().isEmpty() ? "open" : dto.getStatus());
        entity.setStartDate(parseLocalDate(dto.getStartDate()));
        // 优先使用前端显式提交的结束日期，未提交时按工期推算
        LocalDate endDate = parseLocalDate(dto.getEndDate());
        entity.setEndDate(endDate != null ? endDate : calculateEndDate(entity.getStartDate(), entity.getDuration()));
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        return entity;
    }

    private GanttLinkEntity toGanttLinkEntity(Long projectId, GanttLinkDTO dto) {
        GanttLinkEntity entity = new GanttLinkEntity();
        entity.setId(dto.getId());
        entity.setProjectId(projectId);
        entity.setSourceTaskId(dto.getSource());
        entity.setTargetTaskId(dto.getTarget());
        entity.setLinkType(dto.getType());
        entity.setCreatedTime(LocalDateTime.now());
        return entity;
    }

    private LocalDate parseLocalDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalDate calculateEndDate(LocalDate startDate, Integer duration) {
        if (startDate == null || duration == null || duration <= 0) {
            return null;
        }
        return startDate.plusDays(duration.longValue());
    }
}

