package com.hxcoe.wms.job;

import com.hxcoe.wms.service.QmsReceiptTriggerSyncService;
import com.hxcoe.wms.service.ScmReceiptSyncService;
import com.hxcoe.wms.service.CrmOutboundShipmentSyncService;
import com.hxcoe.wms.service.ErpOutboundShipmentSyncService;
import com.hxcoe.wms.service.ScmOutboundShipmentSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * WMS 集成事件重试任务。
 * <p>统一调度各下游 Outbox 投递：SCM 收货回写、QMS 来料检验触发、ERP/SCM/CRM 出库发货。</p>
 */
@Component
public class IntegrationRetryJob {

    @Autowired
    private ScmReceiptSyncService scmReceiptSyncService;

    @Autowired
    private ErpOutboundShipmentSyncService erpOutboundShipmentSyncService;

    @Autowired
    private ScmOutboundShipmentSyncService scmOutboundShipmentSyncService;

    @Autowired(required = false)
    private CrmOutboundShipmentSyncService crmOutboundShipmentSyncService;

    @Autowired
    private QmsReceiptTriggerSyncService qmsReceiptTriggerSyncService;

    @Scheduled(fixedDelayString = "${wms.integration.retry-delay-ms:60000}")
    public void retry() {
        scmReceiptSyncService.trySendPendingBatch();
        // P2-B: 触发 QMS 来料检验（IQC）事件投递
        qmsReceiptTriggerSyncService.trySendPendingBatch();
        erpOutboundShipmentSyncService.trySendPendingBatch();
        scmOutboundShipmentSyncService.trySendPendingBatch();
        if (crmOutboundShipmentSyncService != null) {
            crmOutboundShipmentSyncService.trySendPendingBatch();
        }
    }
}

