package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.KnowledgeArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 知识库文章服务接口
 */
public interface KnowledgeArticleService {

    /**
     * 创建文章
     * @param article 文章数据
     * @return 创建后的文章
     */
    KnowledgeArticle createArticle(KnowledgeArticle article);

    /**
     * 查询文章详情（浏览次数 +1）
     * @param id 文章ID
     * @return 文章详情
     */
    KnowledgeArticle getArticleDetail(Long id);

    /**
     * 分页搜索文章（关键字匹配标题/内容，支持分类筛选）
     * @param keyword 关键字
     * @param category 分类
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<KnowledgeArticle> searchArticles(String keyword, String category, Pageable pageable);

    /**
     * 管理端分页查询文章列表（仅查未删除数据，支持关键字/分类/状态筛选，按id倒序）
     * @param keyword 关键字（可选，匹配标题/内容）
     * @param category 分类（可选）
     * @param status 状态（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<KnowledgeArticle> getArticleList(String keyword, String category, String status, Pageable pageable);

    /**
     * 更新文章（标题/分类/标签/内容/状态）
     * @param id 文章ID
     * @param article 文章数据
     * @return 更新后的文章，文章不存在时返回 null
     */
    KnowledgeArticle updateArticle(Long id, KnowledgeArticle article);

    /**
     * 逻辑删除文章（isDeleted 置为 1）
     * @param id 文章ID
     * @return 删除结果
     */
    boolean deleteArticle(Long id);

    /**
     * 发布文章（状态置为 PUBLISHED）
     * @param id 文章ID
     * @return 发布后的文章，文章不存在时返回 null
     */
    KnowledgeArticle publishArticle(Long id);

    /**
     * 下架文章（状态置为 OFFLINE）
     * @param id 文章ID
     * @return 下架后的文章，文章不存在时返回 null
     */
    KnowledgeArticle offlineArticle(Long id);
}
