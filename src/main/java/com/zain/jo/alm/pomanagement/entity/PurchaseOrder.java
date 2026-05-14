package com.zain.jo.alm.pomanagement.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "TB_PONUMBER")
public class PurchaseOrder implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "po_number_seq_gen")
    @SequenceGenerator(name = "po_number_seq_gen", sequenceName = "PO_NUMBER_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PONUMBER", unique = true, nullable = false)
    private String poNumber;

    @Column(name = "APPROVAL_STATUS")
    private String approvalStatus;

    @Column(name = "CREATEDBY")
    private String createdBy;

    @Column(name = "CREATEDATE")
    private LocalDateTime createdAt;

    @Column(name = "UPDATEDBY")
    private String updatedBy;

    @Column(name = "UPDATEDATE")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "poNumberEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PoItem> poItems = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<PoItem> getPoItems() { return poItems; }
    public void setPoItems(List<PoItem> poItems) { this.poItems = poItems; }
}