package com.hxcoe.qms.listener;

import com.hxcoe.qms.event.QualityInspectionCompletedEvent;
import com.hxcoe.qms.repository.QualityInspectionRepository;
import com.hxcoe.qms.service.ScmIqcSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class QualityInspectionCompletedListener {

    @Autowired
    private QualityInspectionRepository inspectionRepository;

    @Autowired
    private ScmIqcSyncService scmIqcSyncService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCompleted(QualityInspectionCompletedEvent event) {
        if (event == null || event.getInspectionId() == null) return;
        inspectionRepository.findById(event.getInspectionId()).ifPresent(scmIqcSyncService::enqueueIqcCompletedIfAbsent);
        scmIqcSyncService.trySendPendingBatch();
    }
}

