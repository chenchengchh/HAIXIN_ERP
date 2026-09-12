package com.hxcoe.oa.iam.repository;

import com.hxcoe.oa.iam.entity.OaUserRoleEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OaUserRoleRepository extends JpaRepository<OaUserRoleEntity, Long> {
    @Query("select ur.roleId from OaUserRoleEntity ur where ur.userId = :userId")
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 按角色ID反查所有具备该角色的用户ID列表。
     * <p>用于会签审批人解析：根据流程节点要求的角色编码定位审批人候选集合。</p>
     *
     * @param roleId 角色ID
     * @return 用户ID列表（无关联时返回空列表）
     */
    @Query("select ur.userId from OaUserRoleEntity ur where ur.roleId = :roleId")
    List<Long> findUserIdsByRoleId(@Param("roleId") Long roleId);
}
