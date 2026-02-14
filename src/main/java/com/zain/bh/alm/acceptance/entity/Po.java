package com.zain.bh.alm.acceptance.entity;

import java.io.Serializable;
import java.sql.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tb_Po")
public class Po implements Serializable {
	
	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long recordNo;
    private Date recordDateTime;
    private String poNumber;
    private String modelNumber;
    private String uom;
    private Integer qtyPerSite;
    private Integer totalNumberOfSites;
    private Integer totalQty;
    private double accumulatedDepreciation;
    private double salvageValue;
    private String faCategoryNew;
    private String L1;
    private String L2;
    private String L3;
    private String L4;
    private String oldFaCategory;
    private String accumulatedDepreciationCode;
    private String depreciationCode;
    private Integer lifeYearsNew;
    private String vendorName;
    private String vendorNumber;
    private String projectNumber;
    private Date datePlacedInService;
    private Date poDate;
    private String currency;
    private double unitPrice;
    private Integer poLine;
    private String level1Description;
    private String partNumber;
    private String l3Description;
    private String costCenter;
    private String approvalStatus;
    private String createdBy;
    private Date createdDateTime;
    private String updatedBy;
    private Date updatedDatetime;

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
        return level1Description;
    }

    public void setLevel1Description(String level1Description) {
        this.level1Description = level1Description;
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
}
