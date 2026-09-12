package com.hxcoe.aibrain.repository;

import com.hxcoe.aibrain.entity.AiSkillParamEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AI 技能参数仓储
 */
@Repository
public interface AiSkillParamRepository extends JpaRepository<AiSkillParamEntity, Long> {

    Optional<AiSkillParamEntity> findBySkillCodeAndScopeAndParamKey(String skillCode, String scope, String paramKey);

    List<AiSkillParamEntity> findByAutoTuneTrue();
}
