/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.telkom.almBHZain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 * @author jgithu
 */
@Entity
@Table(name = "tb_PONumber")
public class tbPoNumber implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    @Column(unique = true, nullable = false) // Ensure poNumber is unique
    public String poNumber;
    @Column(name = "Approval_Status")
    public String approvalStatus; 

    // One-to-Many relationship with tb_Po
    @OneToMany(mappedBy = "poNumberEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore 
    private List<tb_Po> poItems = new ArrayList<>();
    public List<tb_Po> getPoItems() {
        return poItems;
    }

    public void setPoItems(List<tb_Po> poItems) {
        this.poItems = poItems;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }



}
