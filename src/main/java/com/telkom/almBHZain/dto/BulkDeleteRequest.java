package com.telkom.almBHZain.dto;

import java.util.List;

public class BulkDeleteRequest {
    private List<Long> recordNos;
    private String requestedBy;

    public List<Long> getRecordNos() { return recordNos; }
    public void setRecordNos(List<Long> recordNos) { this.recordNos = recordNos; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }
}