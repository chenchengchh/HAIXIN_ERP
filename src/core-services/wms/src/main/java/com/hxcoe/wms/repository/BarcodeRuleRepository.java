package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.BarcodeRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BarcodeRuleRepository extends JpaRepository<BarcodeRuleEntity, Long>, JpaSpecificationExecutor<BarcodeRuleEntity> {
    BarcodeRuleEntity findByRuleCode(String ruleCode);
}
