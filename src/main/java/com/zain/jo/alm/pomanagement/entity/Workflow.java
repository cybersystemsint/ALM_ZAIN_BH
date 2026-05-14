package com.zain.jo.alm.pomanagement.entity;

import java.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(name = "TB_WF_PO_APPROVAL_REQUEST")
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wf_seq_gen")
    @SequenceGenerator(name = "wf_seq_gen", sequenceName = "WF_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PO_NUMBER")
    private String poNumber;

    @Column(name = "RECORD_NO")
    private Long recordNo;

    @Column(name = "OLD_PO_NUMBER")
    private String oldPoNumber;

    @Column(name = "NEW_PO_NUMBER")
    private String newPoNumber;

    @Column(name = "ORIGINAL_STATUS")
    private String originalStatus;

    @Column(name = "UPDATED_STATUS")
    private String updatedStatus;

    @Column(name = "PROCESS_ID")
    private String processId;

    @Column(name = "INSERTEDBY")
    private String insertedBy;

    @Column(name = "INSERTDATE")
    private LocalDateTime insertDate;

    @Column(name = "CHANGEDBY")
    private String changedBy;

    @Column(name = "CHANGEDATE")
    private LocalDateTime changeDate;

    @Column(name = "COMMENTS")
    private String comments;

    // Getters and setters

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

    public LocalDateTime getInsertDate() { return insertDate; }
    public void setInsertDate(LocalDateTime insertDate) { this.insertDate = insertDate; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getChangeDate() { return changeDate; }
    public void setChangeDate(LocalDateTime changeDate) { this.changeDate = changeDate; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}