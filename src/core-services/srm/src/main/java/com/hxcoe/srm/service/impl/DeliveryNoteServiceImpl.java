package com.hxcoe.srm.service.impl;

import com.hxcoe.srm.entity.DeliveryNoteEntity;
import com.hxcoe.srm.entity.DeliveryNoteItemEntity;
import com.hxcoe.srm.repository.DeliveryNoteRepository;
import com.hxcoe.srm.service.DeliveryNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DeliveryNoteServiceImpl implements DeliveryNoteService {

    @Autowired
    private DeliveryNoteRepository deliveryNoteRepository;

    @Transactional
    @Override
    public DeliveryNoteEntity createDeliveryNote(DeliveryNoteEntity deliveryNote) {
        if (deliveryNote.getItems() != null) {
            for (DeliveryNoteItemEntity item : deliveryNote.getItems()) {
                item.setDeliveryNote(deliveryNote);
            }
        }
        deliveryNote.setStatus("CREATED");
        return deliveryNoteRepository.save(deliveryNote);
    }

    @Override
    public Page<DeliveryNoteEntity> getDeliveryNotes(Pageable pageable) {
        return deliveryNoteRepository.findAll(pageable);
    }

    @Override
    public Optional<DeliveryNoteEntity> getDeliveryNoteById(Long id) {
        return deliveryNoteRepository.findById(id);
    }

    @Transactional
    @Override
    public DeliveryNoteEntity updateDeliveryNote(Long id, DeliveryNoteEntity deliveryNote) {
        return deliveryNoteRepository.findById(id).map(existing -> {
            existing.setLogisticsCompany(deliveryNote.getLogisticsCompany());
            existing.setTrackingNumber(deliveryNote.getTrackingNumber());
            // Update other fields as needed
            return deliveryNoteRepository.save(existing);
        }).orElse(null);
    }

    @Override
    public void deleteDeliveryNote(Long id) {
        deliveryNoteRepository.deleteById(id);
    }

    @Override
    public DeliveryNoteEntity shipDeliveryNote(Long id) {
        return deliveryNoteRepository.findById(id).map(deliveryNote -> {
            deliveryNote.setStatus("SHIPPED");
            return deliveryNoteRepository.save(deliveryNote);
        }).orElse(null);
    }

    @Override
    public DeliveryNoteEntity receiveDeliveryNote(Long id) {
        return deliveryNoteRepository.findById(id).map(deliveryNote -> {
            deliveryNote.setStatus("RECEIVED");
            return deliveryNoteRepository.save(deliveryNote);
        }).orElse(null);
    }
}
