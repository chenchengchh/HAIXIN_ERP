package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.CustomerTagEntity;
import com.hxcoe.crm.service.CustomerTagService;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户标签管理控制器
 */
@RestController
@RequestMapping("/api/v1/crm/tags")
public class CustomerTagController {

    @Autowired
    private CustomerTagService tagService;

    /**
     * 创建标签
     *
     * @param tagEntity 标签实体
     * @return 创建结果
     */
    @PostMapping
    public Result<CustomerTagEntity> createTag(@RequestBody CustomerTagEntity tagEntity) {
        CustomerTagEntity result = tagService.createTag(tagEntity);
        return Result.success("标签创建成功", result);
    }

    /**
     * 根据ID查询标签
     *
     * @param id 主键ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    public Result<CustomerTagEntity> getTagById(@PathVariable Long id) {
        CustomerTagEntity result = tagService.getTagById(id);
        if (result != null) {
            return Result.success("标签查询成功", result);
        } else {
            return Result.fail("标签不存在");
        }
    }

    /**
     * 更新标签
     *
     * @param tagEntity 标签实体
     * @return 更新结果
     */
    @PutMapping
    public Result<CustomerTagEntity> updateTag(@RequestBody CustomerTagEntity tagEntity) {
        CustomerTagEntity result = tagService.updateTag(tagEntity);
        return Result.success("标签更新成功", result);
    }

    /**
     * 删除标签
     *
     * @param id 主键ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteTag(@PathVariable Long id) {
        boolean result = tagService.deleteTag(id);
        if (result) {
            return Result.success("标签删除成功", result);
        } else {
            return Result.fail("标签不存在");
        }
    }

    /**
     * 查询所有标签
     *
     * @return 标签列表
     */
    @GetMapping
    public Result<List<CustomerTagEntity>> getAllTags() {
        List<CustomerTagEntity> result = tagService.getAllTags();
        return Result.success("标签列表查询成功", result);
    }

    /**
     * 根据类型查询标签
     *
     * @param tagType 标签类型
     * @return 标签列表
     */
    @GetMapping("/type/{tagType}")
    public Result<List<CustomerTagEntity>> getTagsByType(@PathVariable String tagType) {
        List<CustomerTagEntity> result = tagService.getTagsByType(tagType);
        return Result.success("标签列表查询成功", result);
    }

    /**
     * 根据分类查询标签
     *
     * @param tagCategory 标签分类
     * @return 标签列表
     */
    @GetMapping("/category/{tagCategory}")
    public Result<List<CustomerTagEntity>> getTagsByCategory(@PathVariable String tagCategory) {
        List<CustomerTagEntity> result = tagService.getTagsByCategory(tagCategory);
        return Result.success("标签列表查询成功", result);
    }
}
