package com.hxcoe.srm.service.impl;

import com.hxcoe.srm.entity.QuotationEntity;
import com.hxcoe.srm.entity.QuotationItemEntity;
import com.hxcoe.srm.repository.QuotationRepository;
import com.hxcoe.srm.service.QuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class QuotationServiceImpl implements QuotationService {

    @Autowired
    private QuotationRepository quotationRepository;

    @Transactional
    @Override
    public QuotationEntity createQuotation(QuotationEntity quotation) {
        if (quotation.getItems() != null) {
            for (QuotationItemEntity item : quotation.getItems()) {
                item.setQuotation(quotation);
            }
        }
        quotation.setStatus("SUBMITTED");
        return quotationRepository.save(quotation);
    }

    @Override
    public Page<QuotationEntity> getQuotations(Pageable pageable) {
        return quotationRepository.findAll(pageable);
    }

    @Override
    public Optional<QuotationEntity> getQuotationById(Long id) {
        return quotationRepository.findById(id);
    }

    @Transactional
    @Override
    public QuotationEntity updateQuotation(Long id, QuotationEntity quotation) {
        return quotationRepository.findById(id).map(existing -> {
            // Update logic here
            existing.setTotalAmount(quotation.getTotalAmount());
            return quotationRepository.save(existing);
        }).orElse(null);
    }

    @Override
    public void deleteQuotation(Long id) {
        quotationRepository.deleteById(id);
    }

    @Override
    public QuotationEntity acceptQuotation(Long id) {
        return quotationRepository.findById(id).map(quotation -> {
            quotation.setStatus("ACCEPTED");
            return quotationRepository.save(quotation);
        }).orElse(null);
    }

    @Override
    public QuotationEntity rejectQuotation(Long id) {
        return quotationRepository.findById(id).map(quotation -> {
            quotation.setStatus("REJECTED");
            return quotationRepository.save(quotation);
        }).orElse(null);
    }
}
