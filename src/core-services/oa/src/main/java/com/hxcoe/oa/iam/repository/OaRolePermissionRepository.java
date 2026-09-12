package com.hxcoe.oa.iam.repository;

import com.hxcoe.oa.iam.entity.OaRolePermissionEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OaRolePermissionRepository extends JpaRepository<OaRolePermissionEntity, Long> {
    @Query("select rp.permId from OaRolePermissionEntity rp where rp.roleId in :roleIds")
    List<Long> findPermIdsByRoleIds(@Param("roleIds") Collection<Long> roleIds);

    boolean existsByRoleIdAndPermId(Long roleId, Long permId);
}
