package com.zain.jo.alm.pomanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class WorkflowDto {
    private Long id;
    private String poNumber;
    private Long recordNo;
    private String oldPoNumber;
    private String newPoNumber;
    private String originalStatus;
    private String updatedStatus;
    private String processId;
    private String insertedBy;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate insertDate;

    private String changedBy;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate changeDate;

    private String comments;

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public Long getRecordNo() { return recordNo; }
    public void setRecordNo(Long recordNo) { this.recordNo = recordNo; }

    public String getOldPoNumber() { return oldPoNumber; }
    public void setOldPoNumber(String oldPoNumber) { this.oldPoNumber = oldPoNumber; }

    public String getNewPoNumber() { return newPoNumber; }
    public void setNewPoNumber(String newPoNumber) { this.newPoNumber = newPoNumber; }

    public String getOriginalStatus() { return originalStatus; }
    public void setOriginalStatus(String originalStatus) { this.originalStatus = originalStatus; }

    public String getUpdatedStatus() { return updatedStatus; }
    public void setUpdatedStatus(String updatedStatus) { this.updatedStatus = updatedStatus; }

    public String getProcessId() { return processId; }
    public void setProcessId(String processId) { this.processId = processId; }

    public String getInsertedBy() { return insertedBy; }
    public void setInsertedBy(String insertedBy) { this.insertedBy = insertedBy; }

    public LocalDate getInsertDate() { return insertDate; }
    public void setInsertDate(LocalDate insertDate) { this.insertDate = insertDate; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public LocalDate getChangeDate() { return changeDate; }
    public void setChangeDate(LocalDate changeDate) { this.changeDate = changeDate; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}