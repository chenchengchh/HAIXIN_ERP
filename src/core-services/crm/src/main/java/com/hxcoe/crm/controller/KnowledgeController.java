package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.KnowledgeArticle;
import com.hxcoe.crm.service.KnowledgeArticleService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 知识库控制器
 */
@RestController
@RequestMapping("/api/v1/crm/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeArticleService knowledgeArticleService;

    /**
     * 创建文章
     * @param article 文章数据
     * @return 创建结果
     */
    @PostMapping
    public Result<KnowledgeArticle> createArticle(@RequestBody KnowledgeArticle article) {
        KnowledgeArticle createdArticle = knowledgeArticleService.createArticle(article);
        return Result.success("成功", createdArticle);
    }

    /**
     * 查询文章详情（浏览次数 +1）
     * @param id 文章ID
     * @return 文章详情
     */
    @GetMapping("/{id}")
    public Result<KnowledgeArticle> getArticleDetail(@PathVariable Long id) {
        KnowledgeArticle article = knowledgeArticleService.getArticleDetail(id);
        return article != null ? Result.success("成功", article) : Result.fail("未找到");
    }

    /**
     * 搜索文章（关键字匹配标题/内容，支持分类筛选）
     * @param page 页码
     * @param size 每页数量
     * @param keyword 关键字
     * @param category 分类
     * @return 分页结果
     */
    @GetMapping("/search")
    public Result<PageResult<KnowledgeArticle>> searchArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {

        // 转换页码，PageRequest从0开始
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<KnowledgeArticle> articlePage = knowledgeArticleService.searchArticles(keyword, category, pageable);
        PageResult<KnowledgeArticle> pageResult = PageResult.build(
                articlePage.getTotalElements(),
                articlePage.getSize(),
                articlePage.getNumber() + 1,
                articlePage.getContent()
        );
        return Result.success(pageResult);
    }

    /**
     * 管理端分页查询文章列表（仅查未删除数据，按id倒序）
     * @param page 页码
     * @param size 每页数量
     * @param keyword 关键字（可选，匹配标题/内容）
     * @param category 分类（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<PageResult<KnowledgeArticle>> getArticleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {

        // 转换页码，PageRequest从0开始，按id倒序
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<KnowledgeArticle> articlePage = knowledgeArticleService.getArticleList(keyword, category, status, pageable);
        PageResult<KnowledgeArticle> pageResult = PageResult.build(
                articlePage.getTotalElements(),
                articlePage.getSize(),
                articlePage.getNumber() + 1,
                articlePage.getContent()
        );
        return Result.success(pageResult);
    }

    /**
     * 更新文章（标题/分类/标签/内容/状态）
     * @param id 文章ID
     * @param article 文章数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<KnowledgeArticle> updateArticle(@PathVariable Long id, @RequestBody KnowledgeArticle article) {
        KnowledgeArticle updatedArticle = knowledgeArticleService.updateArticle(id, article);
        return updatedArticle != null ? Result.success("成功", updatedArticle) : Result.fail("未找到");
    }

    /**
     * 逻辑删除文章
     * @param id 文章ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteArticle(@PathVariable Long id) {
        boolean result = knowledgeArticleService.deleteArticle(id);
        return result ? Result.success("成功", true) : Result.fail("未找到");
    }

    /**
     * 发布文章（状态置为 PUBLISHED）
     * @param id 文章ID
     * @return 发布结果
     */
    @PostMapping("/{id}/publish")
    public Result<KnowledgeArticle> publishArticle(@PathVariable Long id) {
        KnowledgeArticle article = knowledgeArticleService.publishArticle(id);
        return article != null ? Result.success("成功", article) : Result.fail("未找到");
    }

    /**
     * 下架文章（状态置为 OFFLINE）
     * @param id 文章ID
     * @return 下架结果
     */
    @PostMapping("/{id}/offline")
    public Result<KnowledgeArticle> offlineArticle(@PathVariable Long id) {
        KnowledgeArticle article = knowledgeArticleService.offlineArticle(id);
        return article != null ? Result.success("成功", article) : Result.fail("未找到");
    }
}
