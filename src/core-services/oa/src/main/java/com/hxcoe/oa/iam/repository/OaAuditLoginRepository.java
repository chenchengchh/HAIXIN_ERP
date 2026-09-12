package com.hxcoe.oa.iam.repository;

import com.hxcoe.oa.iam.entity.OaAuditLoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OaAuditLoginRepository extends JpaRepository<OaAuditLoginEntity, Long> {
}
