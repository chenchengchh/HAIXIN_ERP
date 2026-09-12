package com.hxcoe.oa.iam.repository;

import com.hxcoe.oa.iam.entity.OaAuditActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OaAuditActionRepository extends JpaRepository<OaAuditActionEntity, Long> {
}

