package com.hxcoe.qms.event;

public class QualityInspectionCompletedEvent {
    private final Long inspectionId;

    public QualityInspectionCompletedEvent(Long inspectionId) {
        this.inspectionId = inspectionId;
    }

    public Long getInspectionId() {
        return inspectionId;
    }
}

