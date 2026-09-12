package com.hxcoe.wms.event;

public class AsnReceivedEvent {
    private final Long asnId;

    public AsnReceivedEvent(Long asnId) {
        this.asnId = asnId;
    }

    public Long getAsnId() {
        return asnId;
    }
}

