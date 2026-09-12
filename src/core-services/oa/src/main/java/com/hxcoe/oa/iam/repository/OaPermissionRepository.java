package com.hxcoe.oa.iam.repository;

import com.hxcoe.oa.iam.entity.OaPermissionEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OaPermissionRepository extends JpaRepository<OaPermissionEntity, Long> {
    Optional<OaPermissionEntity> findByPermCode(String permCode);
    List<OaPermissionEntity> findByIdIn(Collection<Long> ids);
}
