package com.hxcoe.srm.repository;

import com.hxcoe.srm.entity.QuotationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuotationRepository extends JpaRepository<QuotationEntity, Long> {
    List<QuotationEntity> findByInquiryId(Long inquiryId);
    List<QuotationEntity> findBySupplierId(Long supplierId);
}
