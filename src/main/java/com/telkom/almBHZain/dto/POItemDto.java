package com.telkom.almBHZain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class POItemDto {
    private long recordNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordDateTime;

    private String poNumber;
    private String modelNumber;
    private String uom;
    private Integer qtyPerSite;
    private Integer totalNumberOfSites;
    private Integer totalQty;
    private double accumulatedDepreciation;
    private double salvageValue;
    private String faCategoryNew;
    private String l1;
    private String l2;
    private String l3;
    private String l4;
    private String oldFaCategory;
    private String accumulatedDepreciationCode;
    private String depreciationCode;
    private Integer lifeYearsNew;
    private String vendorName;
    private String vendorNumber;
    private String projectNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate datePlacedInService;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate poDate;

    private String currency;
    private double unitPrice;
    private Integer poLine;
    private String level1Description;
    private String partNumber;
    private String l3Description;
    private String costCenter;

    private String approvalStatus;
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDateTime;

    private String updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate updatedDatetime;

    // getters / setters
    public long getRecordNo() { return recordNo; }
    public void setRecordNo(long recordNo) { this.recordNo = recordNo; }

    public LocalDate getRecordDateTime() { return recordDateTime; }
    public void setRecordDateTime(LocalDate recordDateTime) { this.recordDateTime = recordDateTime; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public String getModelNumber() { return modelNumber; }
    public void setModelNumber(String modelNumber) { this.modelNumber = modelNumber; }

    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }

    public Integer getQtyPerSite() { return qtyPerSite; }
    public void setQtyPerSite(Integer qtyPerSite) { this.qtyPerSite = qtyPerSite; }

    public Integer getTotalNumberOfSites() { return totalNumberOfSites; }
    public void setTotalNumberOfSites(Integer totalNumberOfSites) { this.totalNumberOfSites = totalNumberOfSites; }

    public Integer getTotalQty() { return totalQty; }
    public void setTotalQty(Integer totalQty) { this.totalQty = totalQty; }

    public double getAccumulatedDepreciation() { return accumulatedDepreciation; }
    public void setAccumulatedDepreciation(double accumulatedDepreciation) { this.accumulatedDepreciation = accumulatedDepreciation; }

    public double getSalvageValue() { return salvageValue; }
    public void setSalvageValue(double salvageValue) { this.salvageValue = salvageValue; }

    public String getFaCategoryNew() { return faCategoryNew; }
    public void setFaCategoryNew(String faCategoryNew) { this.faCategoryNew = faCategoryNew; }

    public String getL1() { return l1; }
    public void setL1(String l1) { this.l1 = l1; }

    public String getL2() { return l2; }
    public void setL2(String l2) { this.l2 = l2; }

    public String getL3() { return l3; }
    public void setL3(String l3) { this.l3 = l3; }

    public String getL4() { return l4; }
    public void setL4(String l4) { this.l4 = l4; }

    public String getOldFaCategory() { return oldFaCategory; }
    public void setOldFaCategory(String oldFaCategory) { this.oldFaCategory = oldFaCategory; }

    public String getAccumulatedDepreciationCode() { return accumulatedDepreciationCode; }
    public void setAccumulatedDepreciationCode(String accumulatedDepreciationCode) { this.accumulatedDepreciationCode = accumulatedDepreciationCode; }

    public String getDepreciationCode() { return depreciationCode; }
    public void setDepreciationCode(String depreciationCode) { this.depreciationCode = depreciationCode; }

    public Integer getLifeYearsNew() { return lifeYearsNew; }
    public void setLifeYearsNew(Integer lifeYearsNew) { this.lifeYearsNew = lifeYearsNew; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getVendorNumber() { return vendorNumber; }
    public void setVendorNumber(String vendorNumber) { this.vendorNumber = vendorNumber; }

    public String getProjectNumber() { return projectNumber; }
    public void setProjectNumber(String projectNumber) { this.projectNumber = projectNumber; }

    public LocalDate getDatePlacedInService() { return datePlacedInService; }
    public void setDatePlacedInService(LocalDate datePlacedInService) { this.datePlacedInService = datePlacedInService; }

    public LocalDate getPoDate() { return poDate; }
    public void setPoDate(LocalDate poDate) { this.poDate = poDate; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public Integer getPoLine() { return poLine; }
    public void setPoLine(Integer poLine) { this.poLine = poLine; }

    public String getLevel1Description() { return level1Description; }
    public void setLevel1Description(String level1Description) { this.level1Description = level1Description; }

    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }

    public String getL3Description() { return l3Description; }
    public void setL3Description(String l3Description) { this.l3Description = l3Description; }

    public String getCostCenter() { return costCenter; }
    public void setCostCenter(String costCenter) { this.costCenter = costCenter; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDate getCreatedDateTime() { return createdDateTime; }
    public void setCreatedDateTime(LocalDate createdDateTime) { this.createdDateTime = createdDateTime; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDate getUpdatedDatetime() { return updatedDatetime; }
    public void setUpdatedDatetime(LocalDate updatedDatetime) { this.updatedDatetime = updatedDatetime; }
}