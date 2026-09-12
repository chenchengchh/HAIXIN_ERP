package com.hxcoe.oa.iam.repository;

import com.hxcoe.oa.iam.entity.OaUserAccountEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OaUserAccountRepository extends JpaRepository<OaUserAccountEntity, Long> {
    Optional<OaUserAccountEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    List<OaUserAccountEntity> findByEmployeeIdIsNotNull();

    /**
     * 按员工 ID 查询账号（用于 HR 事件同步账号启停）。
     *
     * @param employeeId 员工 ID
     * @return 账号实体（存在时）
     */
    Optional<OaUserAccountEntity> findByEmployeeId(Long employeeId);

    /**
     * 按账号ID集合批量查询账号列表。
     * <p>用于会签审批人解析：根据角色反查出的 userId 集合批量获取账号信息，
     * 一次查询完成候选审批人列表的加载，避免 N+1 查询。</p>
     *
     * @param ids 账号ID集合
     * @return 账号实体列表
     */
    List<OaUserAccountEntity> findByIdIn(Collection<Long> ids);
}
