package com.hxcoe.crm.service.impl;

import com.hxcoe.crm.entity.KnowledgeArticle;
import com.hxcoe.crm.repository.KnowledgeArticleRepository;
import com.hxcoe.crm.service.KnowledgeArticleService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识库文章服务实现类
 */
@Service
public class KnowledgeArticleServiceImpl implements KnowledgeArticleService {

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    /**
     * 创建文章，默认状态为 PUBLISHED，浏览次数初始化为 0
     * @param article 文章数据
     * @return 创建后的文章
     */
    @Override
    public KnowledgeArticle createArticle(KnowledgeArticle article) {
        if (article.getStatus() == null || article.getStatus().isEmpty()) {
            article.setStatus("PUBLISHED");
        }
        if (article.getViews() == null) {
            article.setViews(0);
        }
        return knowledgeArticleRepository.save(article);
    }

    /**
     * 查询文章详情，同时将浏览次数 +1
     * @param id 文章ID
     * @return 文章详情
     */
    @Override
    public KnowledgeArticle getArticleDetail(Long id) {
        KnowledgeArticle article = knowledgeArticleRepository.findById(id).orElse(null);
        if (article != null) {
            // 浏览次数 +1
            article.setViews((article.getViews() == null ? 0 : article.getViews()) + 1);
            return knowledgeArticleRepository.save(article);
        }
        return null;
    }

    /**
     * 分页搜索文章（关键字匹配标题/内容，支持分类筛选）
     * @param keyword 关键字
     * @param category 分类
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Page<KnowledgeArticle> searchArticles(String keyword, String category, Pageable pageable) {
        return knowledgeArticleRepository.search(keyword, category, pageable);
    }

    /**
     * 管理端分页查询文章列表，仅查询未删除数据，按id倒序
     * @param keyword 关键字（可选，模糊匹配标题/内容）
     * @param category 分类（可选）
     * @param status 状态（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Page<KnowledgeArticle> getArticleList(String keyword, String category, String status, Pageable pageable) {
        Specification<KnowledgeArticle> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 仅查询未删除数据
            predicates.add(cb.equal(root.get("isDeleted"), 0));
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.or(
                        cb.like(root.get("title"), "%" + keyword + "%"),
                        cb.like(root.get("content"), "%" + keyword + "%")
                ));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return knowledgeArticleRepository.findAll(spec, pageable);
    }

    /**
     * 更新文章（标题/分类/标签/内容/状态）
     * @param id 文章ID
     * @param article 文章数据
     * @return 更新后的文章，文章不存在时返回 null
     */
    @Override
    public KnowledgeArticle updateArticle(Long id, KnowledgeArticle article) {
        KnowledgeArticle existingArticle = knowledgeArticleRepository.findById(id).orElse(null);
        if (existingArticle != null) {
            existingArticle.setTitle(article.getTitle());
            existingArticle.setCategory(article.getCategory());
            existingArticle.setTags(article.getTags());
            existingArticle.setContent(article.getContent());
            if (article.getStatus() != null && !article.getStatus().isEmpty()) {
                existingArticle.setStatus(article.getStatus());
            }
            return knowledgeArticleRepository.save(existingArticle);
        }
        return null;
    }

    /**
     * 逻辑删除文章（isDeleted 置为 1）
     * @param id 文章ID
     * @return 删除结果
     */
    @Override
    public boolean deleteArticle(Long id) {
        KnowledgeArticle article = knowledgeArticleRepository.findById(id).orElse(null);
        if (article != null) {
            article.setIsDeleted(1);
            knowledgeArticleRepository.save(article);
            return true;
        }
        return false;
    }

    /**
     * 发布文章（状态置为 PUBLISHED）
     * @param id 文章ID
     * @return 发布后的文章，文章不存在时返回 null
     */
    @Override
    public KnowledgeArticle publishArticle(Long id) {
        KnowledgeArticle article = knowledgeArticleRepository.findById(id).orElse(null);
        if (article != null) {
            article.setStatus("PUBLISHED");
            return knowledgeArticleRepository.save(article);
        }
        return null;
    }

    /**
     * 下架文章（状态置为 OFFLINE）
     * @param id 文章ID
     * @return 下架后的文章，文章不存在时返回 null
     */
    @Override
    public KnowledgeArticle offlineArticle(Long id) {
        KnowledgeArticle article = knowledgeArticleRepository.findById(id).orElse(null);
        if (article != null) {
            article.setStatus("OFFLINE");
            return knowledgeArticleRepository.save(article);
        }
        return null;
    }
}
