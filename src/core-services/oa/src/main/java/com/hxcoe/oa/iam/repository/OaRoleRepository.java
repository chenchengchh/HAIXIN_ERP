package com.hxcoe.oa.iam.repository;

import com.hxcoe.oa.iam.entity.OaRoleEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OaRoleRepository extends JpaRepository<OaRoleEntity, Long> {
    Optional<OaRoleEntity> findByRoleCode(String roleCode);
    List<OaRoleEntity> findByIdIn(Collection<Long> ids);
}
