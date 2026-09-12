package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.FinanceTaskRuleEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinanceTaskRuleRepository extends JpaRepository<FinanceTaskRuleEntity, Long> {
    Optional<FinanceTaskRuleEntity> findFirstByFactTypeAndEnabledAndResultInOrderByIdAsc(
            String factType,
            Integer enabled,
            List<String> result
    );
}
