package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.CustomerCategoryEntity;
import com.hxcoe.crm.service.CustomerCategoryService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户分类管理控制器
 */
@RestController
@RequestMapping("/api/v1/crm/categories")
public class CustomerCategoryController {

    @Autowired
    private CustomerCategoryService categoryService;

    /**
     * 创建分类
     *
     * @param category 分类实体
     * @return 创建结果
     */
    @PostMapping
    public Result<CustomerCategoryEntity> createCategory(@RequestBody CustomerCategoryEntity category) {
        CustomerCategoryEntity created = categoryService.createCategory(category);
        return Result.success("分类创建成功", created);
    }

    /**
     * 分页查询分类列表
     *
     * @param page    页码，默认1
     * @param size    每页大小，默认10
     * @param keyword 关键词（名称/编码模糊匹配，可选）
     * @return 分类分页数据
     */
    @GetMapping("/list")
    public Result<PageResult<CustomerCategoryEntity>> getCategoryList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1),
                Sort.by(Sort.Direction.ASC, "sortOrder"));
        Page<CustomerCategoryEntity> categoryPage = categoryService.getCategoryList(keyword, pageable);
        return Result.success("成功", PageResult.build(
                categoryPage.getTotalElements(),
                categoryPage.getSize(),
                categoryPage.getNumber() + 1,
                categoryPage.getContent()
        ));
    }

    /**
     * 查询所有分类（用于树状结构展示）
     *
     * @return 分类列表
     */
    @GetMapping("/all")
    public Result<List<CustomerCategoryEntity>> getAllCategories() {
        List<CustomerCategoryEntity> categories = categoryService.getAllCategories();
        return Result.success("成功", categories);
    }

    /**
     * 查询分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    @GetMapping("/{id}")
    public Result<CustomerCategoryEntity> getCategoryById(@PathVariable Long id) {
        CustomerCategoryEntity category = categoryService.getCategoryById(id);
        return category != null ? Result.success("成功", category) : Result.fail("分类不存在");
    }

    /**
     * 更新分类
     *
     * @param id       分类ID
     * @param category 分类实体
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<CustomerCategoryEntity> updateCategory(@PathVariable Long id, @RequestBody CustomerCategoryEntity category) {
        CustomerCategoryEntity updated = categoryService.updateCategory(id, category);
        return updated != null ? Result.success("分类更新成功", updated) : Result.fail("分类不存在");
    }

    /**
     * 删除分类（存在子分类时禁止删除）
     *
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCategory(@PathVariable Long id) {
        int result = categoryService.deleteCategory(id);
        if (result == 0) {
            return Result.success("分类删除成功", true);
        } else if (result == 2) {
            return Result.fail("该分类存在子分类，无法删除");
        } else {
            return Result.fail("分类不存在");
        }
    }
}
