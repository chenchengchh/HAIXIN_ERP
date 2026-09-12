package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * 缂佸嫮绮愭禒鎾崇氨閹恒儱褰?
 */
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long>, JpaSpecificationExecutor<OrganizationEntity> {
    Optional<OrganizationEntity> findByOrganizationCode(String organizationCode);
}

