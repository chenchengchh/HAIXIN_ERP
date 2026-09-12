package com.hxcoe.scrm.controller.douyin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.result.Result;
import com.hxcoe.scrm.dto.douyin.DouyinTaskDTO;
import com.hxcoe.scrm.dto.douyin.DouyinTaskRequest;
import com.hxcoe.scrm.entity.douyin.DouyinTaskEntity;
import com.hxcoe.scrm.entity.douyin.DouyinVideoEntity;
import com.hxcoe.scrm.entity.douyin.DouyinCustomerEntity;
import com.hxcoe.scrm.exception.douyin.DouyinException;
import com.hxcoe.scrm.service.douyin.DouyinTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/douyin")
@CrossOrigin(origins = "*") // 允许前端跨域
public class DouyinTaskController {

    @Autowired
    private DouyinTaskService taskService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取任务列表
     */
    @GetMapping("/tasks")
    public Result<List<DouyinTaskDTO>> getTasks() {
        return Result.success(taskService.getAllTasks());
    }

    /**
     * 创建任务
     */
    @PostMapping("/tasks")
    public Result<DouyinTaskEntity> createTask(@RequestBody DouyinTaskRequest taskRequest) {
        try {
            // 创建实体类并转换数据格式
            DouyinTaskEntity task = convertRequestToEntity(taskRequest);
            return Result.success(taskService.createTask(task));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            return Result.error(500, "创建任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新任务
     */
    @PutMapping("/tasks/{id}")
    public Result<DouyinTaskEntity> updateTask(@PathVariable Long id, @RequestBody DouyinTaskRequest taskRequest) {
        try {
            // 创建实体类并转换数据格式
            DouyinTaskEntity task = convertRequestToEntity(taskRequest);
            return Result.success(taskService.updateTask(id, task));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            return Result.error(500, "更新任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除任务
     */
    @DeleteMapping("/tasks/{id}")
    public Result<Void> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            return Result.error(500, "删除任务失败: " + e.getMessage());
        }
    }

    /**
     * 启动任务
     */
    @PostMapping("/tasks/{id}/start")
    public Result<Void> startTask(@PathVariable Long id) {
        try {
            taskService.startTask(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            return Result.error(500, "启动任务失败: " + e.getMessage());
        }
    }

    /**
     * 停止任务
     */
    @PostMapping("/tasks/{id}/stop")
    public Result<Void> stopTask(@PathVariable Long id) {
        try {
            taskService.stopTask(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            return Result.error(500, "停止任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务视频
     */
    @GetMapping("/tasks/{id}/videos")
    public Result<List<DouyinVideoEntity>> getTaskVideos(@PathVariable Long id) {
        try {
            return Result.success(taskService.getTaskVideos(id));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            return Result.error(500, "获取视频失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务客户
     */
    @GetMapping("/tasks/{id}/customers")
    public Result<List<DouyinCustomerEntity>> getTaskCustomers(@PathVariable Long id) {
        try {
            return Result.success(taskService.getTaskCustomers(id));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            return Result.error(500, "获取客户失败: " + e.getMessage());
        }
    }
    
    /**
     * 客户转化
     */
    @PostMapping("/customers/{id}/convert")
    public Result<Void> convertCustomer(@PathVariable Long id) {
        try {
            taskService.convertCustomer(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            return Result.error(500, "客户转化失败: " + e.getMessage());
        }
    }

    /**
     * 更新客户状态
     */
    @PutMapping("/customers/{id}/status")
    public Result<Void> updateCustomerStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            if (status == null || status.trim().isEmpty()) {
                throw new IllegalArgumentException("状态不能为空");
            }
            taskService.updateCustomerStatus(id, status);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            return Result.error(500, "更新客户状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查登录状态
     */
    @GetMapping("/login/status")
    public Result<Map<String, Object>> checkLoginStatus() {
        try {
            // 模拟未登录状态，引导用户扫码
            // 实际逻辑应检查 Playwright 浏览器上下文的 Cookies
            Map<String, Object> status = Map.of(
                "isLogged", false,
                "qrCodeUrl", "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=DouyinLoginMock"
            );
            return Result.success(status);
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            return Result.error(500, "检查登录状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 将请求DTO转换为实体类
     */
    private DouyinTaskEntity convertRequestToEntity(DouyinTaskRequest taskRequest) {
        DouyinTaskEntity task = new DouyinTaskEntity();
        task.setName(taskRequest.getName());
        task.setSearchKeyword(taskRequest.getSearchKeyword());
        
        // 将嵌套的关键词列表转换为JSON字符串
        try {
            task.setIntentKeywords(objectMapper.writeValueAsString(
                taskRequest.getFilterConfig() != null ? taskRequest.getFilterConfig().getIntentKeywords() : List.of()
            ));
            task.setExcludeKeywords(objectMapper.writeValueAsString(
                taskRequest.getFilterConfig() != null ? taskRequest.getFilterConfig().getExcludeKeywords() : List.of()
            ));
            
            // 处理消息配置
            if (taskRequest.getMessageConfig() != null) {
                task.setMessageTemplate(taskRequest.getMessageConfig().getTemplate());
                task.setMaxMessageCount(taskRequest.getMessageConfig().getMaxCount());
                
                // 处理发送间隔
                if (taskRequest.getMessageConfig().getInterval() != null) {
                    task.setMessageInterval(objectMapper.writeValueAsString(taskRequest.getMessageConfig().getInterval()));
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("数据格式转换失败: " + e.getMessage());
        }
        
        return task;
    }
}
