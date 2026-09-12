package com.hxcoe.aibrain.repository;

import com.hxcoe.aibrain.entity.AiPromptTemplateEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AI 提示词模板仓储
 */
@Repository
public interface AiPromptTemplateRepository extends JpaRepository<AiPromptTemplateEntity, Long> {

    /** 查询指定技能启用中版本最高的模板 */
    Optional<AiPromptTemplateEntity> findTopBySkillCodeAndEnabledTrueOrderByVersionDesc(String skillCode);

    List<AiPromptTemplateEntity> findBySkillCode(String skillCode);
}
