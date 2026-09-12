package com.hxcoe.erp.job;

import com.hxcoe.erp.service.FinanceTaskProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FinanceTaskProcessJob {

    @Autowired
    private FinanceTaskProcessorService financeTaskProcessorService;

    @Scheduled(fixedDelayString = "${erp.finance.task-process-delay-ms:60000}")
    public void processFinanceTasks() {
        financeTaskProcessorService.tryProcessPendingBatch();
    }
}
