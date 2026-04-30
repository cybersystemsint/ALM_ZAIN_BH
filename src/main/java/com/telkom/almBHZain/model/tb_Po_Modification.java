package com.telkom.almBHZain.model;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;

@Entity
@Table(name = "tb_po_modification")
public class tb_Po_Modification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long Id;
    public long recordNo;
    public Date recordDateTime;
    public String poNumber;
    public String modelNumber;
    public String uom;
    public Integer qtyPerSite;
    public Integer totalNumberOfSites;
    public Integer totalQty;
    public double accumulatedDepreciation;
    public double salvageValue;
    public String faCategoryNew;
    public String L1;
    public String L2;
    public String L3;
    public String L4;
    public String oldFaCategory;
    public String accumulatedDepreciationCode;
    public String depreciationCode;
    public Integer lifeYearsNew;
    public String vendorName;
    public String vendorNumber;
    public String projectNumber;
    public Date datePlacedInService;
    public Date poDate;
    public String currency;
    public double unitPrice;
    public Integer poLine;
    public String Level1Description;
    public String partNumber;
    public String l3Description;
    public String costCenter;
    @Column(name = "Approval_Status")
    private String approvalStatus;
    public String createdBy;
    public Date createdDateTime;
    public String updatedBy;
    public Date updatedDatetime;
  


    // Getters and Setters
 


    public long getRecordNo() {
        return recordNo;
    }

    public void setRecordNo(long recordNo) {
        this.recordNo = recordNo;
    }

    public Date getRecordDateTime() {
        return recordDateTime;
    }

    public void setRecordDateTime(Date recordDateTime) {
        this.recordDateTime = recordDateTime;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public Integer getQtyPerSite() {
        return qtyPerSite;
    }

    public void setQtyPerSite(Integer qtyPerSite) {
        this.qtyPerSite = qtyPerSite;
    }

    public Integer getTotalNumberOfSites() {
        return totalNumberOfSites;
    }

    public void setTotalNumberOfSites(Integer totalNumberOfSites) {
        this.totalNumberOfSites = totalNumberOfSites;
    }

    public Integer getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(Integer totalQty) {
        this.totalQty = totalQty;
    }

    public double getAccumulatedDepreciation() {
        return accumulatedDepreciation;
    }

    public void setAccumulatedDepreciation(double accumulatedDepreciation) {
        this.accumulatedDepreciation = accumulatedDepreciation;
    }

    public double getSalvageValue() {
        return salvageValue;
    }

    public void setSalvageValue(double salvageValue) {
        this.salvageValue = salvageValue;
    }

    public String getFaCategoryNew() {
        return faCategoryNew;
    }

    public void setFaCategoryNew(String faCategoryNew) {
        this.faCategoryNew = faCategoryNew;
    }

    public String getL1() {
        return L1;
    }

    public void setL1(String L1) {
        this.L1 = L1;
    }

    public String getL2() {
        return L2;
    }

    public void setL2(String L2) {
        this.L2 = L2;
    }

    public String getL3() {
        return L3;
    }

    public void setL3(String L3) {
        this.L3 = L3;
    }

    public String getL4() {
        return L4;
    }

    public void setL4(String L4) {
        this.L4 = L4;
    }

    public String getOldFaCategory() {
        return oldFaCategory;
    }

    public void setOldFaCategory(String oldFaCategory) {
        this.oldFaCategory = oldFaCategory;
    }

    public String getAccumulatedDepreciationCode() {
        return accumulatedDepreciationCode;
    }

    public void setAccumulatedDepreciationCode(String accumulatedDepreciationCode) {
        this.accumulatedDepreciationCode = accumulatedDepreciationCode;
    }

    public String getDepreciationCode() {
        return depreciationCode;
    }

    public void setDepreciationCode(String depreciationCode) {
        this.depreciationCode = depreciationCode;
    }

    public Integer getLifeYearsNew() {
        return lifeYearsNew;
    }

    public void setLifeYearsNew(Integer lifeYearsNew) {
        this.lifeYearsNew = lifeYearsNew;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorNumber() {
        return vendorNumber;
    }

    public void setVendorNumber(String vendorNumber) {
        this.vendorNumber = vendorNumber;
    }

    public String getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }

    public Date getDatePlacedInService() {
        return datePlacedInService;
    }

    public void setDatePlacedInService(Date datePlacedInService) {
        this.datePlacedInService = datePlacedInService;
    }

    public Date getPoDate() {
        return poDate;
    }

    public void setPoDate(Date poDate) {
        this.poDate = poDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getPoLine() {
        return poLine;
    }

    public void setPoLine(Integer poLine) {
        this.poLine = poLine;
    }

    public String getLevel1Description() {
        return Level1Description;
    }

    public void setLevel1Description(String Level1Description) {
        this.Level1Description = Level1Description;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getL3Description() {
        return l3Description;
    }

    public void setL3Description(String l3Description) {
        this.l3Description = l3Description;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedDatetime() {
        return updatedDatetime;
    }

    public void setUpdatedDatetime(Date updatedDatetime) {
        this.updatedDatetime = updatedDatetime;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long Id) {
        this.Id = Id;
    }

    public tb_Po_Modification orElseThrow(Object object) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'orElseThrow'");
    }


}
