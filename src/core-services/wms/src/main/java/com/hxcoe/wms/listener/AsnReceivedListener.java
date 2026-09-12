package com.hxcoe.wms.listener;

import com.hxcoe.wms.event.AsnReceivedEvent;
import com.hxcoe.wms.repository.AsnRepository;
import com.hxcoe.wms.service.ScmReceiptSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
public class AsnReceivedListener {

    @Autowired
    private AsnRepository asnRepository;

    @Autowired
    private ScmReceiptSyncService scmReceiptSyncService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAsnReceived(AsnReceivedEvent event) {
        if (event == null || event.getAsnId() == null) return;
        asnRepository.findById(event.getAsnId()).ifPresent(scmReceiptSyncService::enqueueAsnReceivedIfAbsent);
        scmReceiptSyncService.trySendPendingBatch();
    }
}

