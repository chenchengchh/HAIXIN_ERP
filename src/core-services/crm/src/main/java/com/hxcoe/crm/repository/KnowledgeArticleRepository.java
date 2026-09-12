package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.KnowledgeArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 知识库文章数据访问接口
 */
@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long>, JpaSpecificationExecutor<KnowledgeArticle> {

    /**
     * 按关键字（匹配标题/内容）和分类分页搜索文章
     * @param keyword 关键字，为空时不限制
     * @param category 分类，为空时不限制
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT k FROM KnowledgeArticle k WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR k.title LIKE CONCAT('%', :keyword, '%') OR k.content LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:category IS NULL OR :category = '' OR k.category = :category)")
    Page<KnowledgeArticle> search(@Param("keyword") String keyword, @Param("category") String category, Pageable pageable);
}
