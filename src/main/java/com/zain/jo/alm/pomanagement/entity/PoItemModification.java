package com.zain.jo.alm.pomanagement.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(name = "TB_PO_MODIFICATION")
public class PoItemModification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "po_mod_seq_gen")
    @SequenceGenerator(name = "po_mod_seq_gen", sequenceName = "PO_MOD_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "RECORDNO")
    private Long recordNo;

    @Column(name = "RECORDDATETIME")
    private LocalDateTime recordDateTime;

    @Column(name = "PONUMBER")
    private String poNumber;

    @Column(name = "MODELNUMBER")
    private String modelNumber;

    @Column(name = "UOM")
    private String uom;

    @Column(name = "QTYPERSITE")
    private Integer qtyPerSite;

    @Column(name = "TOTALNUMBEROFSITES")
    private Integer totalNumberOfSites;

    @Column(name = "TOTALQTY")
    private Integer totalQty;

    @Column(name = "ACCUMULATEDDEPRECIATION", precision = 18, scale = 2)
    private BigDecimal accumulatedDepreciation;

    @Column(name = "SALVAGEVALUE", precision = 18, scale = 2)
    private BigDecimal salvageValue;

    @Column(name = "FACATEGORYNEW")
    private String faCategoryNew;

    @Column(name = "L1")
    private String l1;

    @Column(name = "L2")
    private String l2;

    @Column(name = "L3")
    private String l3;

    @Column(name = "L4")
    private String l4;

    @Column(name = "OLDFACATEGORY")
    private String oldFaCategory;

    @Column(name = "ACCUMULATEDDEPRECIATIONCODE")
    private String accumulatedDepreciationCode;

    @Column(name = "DEPRECIATIONCODE")
    private String depreciationCode;

    @Column(name = "LIFEYEARSNEW")
    private Integer lifeYearsNew;

    @Column(name = "VENDORNAME")
    private String vendorName;

    @Column(name = "VENDORNUMBER")
    private String vendorNumber;

    @Column(name = "PROJECTNUMBER")
    private String projectNumber;

    @Column(name = "DATEPLACEDINSERVICE")
    private LocalDateTime datePlacedInService;

    @Column(name = "PODATE")
    private LocalDateTime poDate;

    @Column(name = "CURRENCY")
    private String currency;

    @Column(name = "UNITPRICE", precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "POLINE")
    private Integer poLine;

    @Column(name = "LEVEL1DESCRIPTION")
    private String level1Description;

    @Column(name = "PARTNUMBER")
    private String partNumber;

    @Column(name = "L3DESCRIPTION")
    private String l3Description;

    @Column(name = "COSTCENTER")
    private String costCenter;

    @Column(name = "APPROVAL_STATUS")
    private String approvalStatus;

    @Column(name = "CREATEDBY")
    private String createdBy;

    @Column(name = "CREATEDDATETIME")
    private LocalDateTime createdDateTime;

    @Column(name = "UPDATEDBY")
    private String updatedBy;

    @Column(name = "UPDATEDDATETIME")
    private LocalDateTime updatedDatetime;

    // Getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRecordNo() { return recordNo; }
    public void setRecordNo(Long recordNo) { this.recordNo = recordNo; }

    public LocalDateTime getRecordDateTime() { return recordDateTime; }
    public void setRecordDateTime(LocalDateTime recordDateTime) { this.recordDateTime = recordDateTime; }

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

    public BigDecimal getAccumulatedDepreciation() { return accumulatedDepreciation; }
    public void setAccumulatedDepreciation(BigDecimal accumulatedDepreciation) { this.accumulatedDepreciation = accumulatedDepreciation; }

    public BigDecimal getSalvageValue() { return salvageValue; }
    public void setSalvageValue(BigDecimal salvageValue) { this.salvageValue = salvageValue; }

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

    public LocalDateTime getDatePlacedInService() { return datePlacedInService; }
    public void setDatePlacedInService(LocalDateTime datePlacedInService) { this.datePlacedInService = datePlacedInService; }

    public LocalDateTime getPoDate() { return poDate; }
    public void setPoDate(LocalDateTime poDate) { this.poDate = poDate; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

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

    public LocalDateTime getCreatedDateTime() { return createdDateTime; }
    public void setCreatedDateTime(LocalDateTime createdDateTime) { this.createdDateTime = createdDateTime; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDatetime() { return updatedDatetime; }
    public void setUpdatedDatetime(LocalDateTime updatedDatetime) { this.updatedDatetime = updatedDatetime; }
}