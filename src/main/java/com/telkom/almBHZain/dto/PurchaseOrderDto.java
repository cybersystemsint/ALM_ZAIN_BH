package com.telkom.almBHZain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class PurchaseOrderDto {
    private long id;
    private String poNumber;
    private String approvalStatus;
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;
    private String updatedBy;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate updatedAt;

    // getters / setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }
}