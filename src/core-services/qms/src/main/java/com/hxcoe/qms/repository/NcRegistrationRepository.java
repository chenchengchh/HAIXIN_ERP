package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.NcRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * 不合格品登记仓库
 */
public interface NcRegistrationRepository extends JpaRepository<NcRegistrationEntity, Long>, JpaSpecificationExecutor<NcRegistrationEntity> {

    /**
     * 根据登记编号查询
     *
     * @param registrationNo 登记编号
     * @return 登记记录
     */
    Optional<NcRegistrationEntity> findByRegistrationNo(String registrationNo);
}
