package com.hxcoe.srm.service;

import com.hxcoe.srm.entity.DeliveryNoteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface DeliveryNoteService {
    DeliveryNoteEntity createDeliveryNote(DeliveryNoteEntity deliveryNote);
    Page<DeliveryNoteEntity> getDeliveryNotes(Pageable pageable);
    Optional<DeliveryNoteEntity> getDeliveryNoteById(Long id);
    DeliveryNoteEntity updateDeliveryNote(Long id, DeliveryNoteEntity deliveryNote);
    void deleteDeliveryNote(Long id);
    DeliveryNoteEntity shipDeliveryNote(Long id);
    DeliveryNoteEntity receiveDeliveryNote(Long id);
}
