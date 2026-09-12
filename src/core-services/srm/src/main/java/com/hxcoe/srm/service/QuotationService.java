package com.hxcoe.srm.service;

import com.hxcoe.srm.entity.QuotationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface QuotationService {
    QuotationEntity createQuotation(QuotationEntity quotation);
    Page<QuotationEntity> getQuotations(Pageable pageable);
    Optional<QuotationEntity> getQuotationById(Long id);
    QuotationEntity updateQuotation(Long id, QuotationEntity quotation);
    void deleteQuotation(Long id);
    QuotationEntity acceptQuotation(Long id);
    QuotationEntity rejectQuotation(Long id);
}
