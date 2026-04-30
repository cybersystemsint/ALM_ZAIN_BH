package com.telkom.almBHZain.model;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.*;

@Entity
@Table(name = "tb_Po")
public class POItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recordNo")
    private long recordNo;

    @Column(name = "recordDateTime")
    private Date recordDateTime;

    @Column(name = "poNumber")
    private String poNumber;

    @Column(name = "modelNumber")
    private String modelNumber;

    @Column(name = "uom")
    private String uom;

    @Column(name = "qtyPerSite")
    private Integer qtyPerSite;

    @Column(name = "totalNumberOfSites")
    private Integer totalNumberOfSites;

    @Column(name = "totalQty")
    private Integer totalQty;

    @Column(name = "accumulatedDepreciation")
    private double accumulatedDepreciation;

    @Column(name = "salvageValue")
    private double salvageValue;

    @Column(name = "faCategoryNew")
    private String faCategoryNew;

    @Column(name = "L1")
    private String l1;

    @Column(name = "L2")
    private String l2;

    @Column(name = "L3")
    private String l3;

    @Column(name = "L4")
    private String l4;

    @Column(name = "oldFaCategory")
    private String oldFaCategory;

    @Column(name = "accumulatedDepreciationCode")
    private String accumulatedDepreciationCode;

    @Column(name = "depreciationCode")
    private String depreciationCode;

    @Column(name = "lifeYearsNew")
    private Integer lifeYearsNew;

    @Column(name = "vendorName")
    private String vendorName;

    @Column(name = "vendorNumber")
    private String vendorNumber;

    @Column(name = "projectNumber")
    private String projectNumber;

    @Column(name = "datePlacedInService")
    private Date datePlacedInService;

    @Column(name = "poDate")
    private Date poDate;

    @Column(name = "currency")
    private String currency;

    @Column(name = "unitPrice")
    private double unitPrice;

    @Column(name = "poLine")
    private Integer poLine;

    @Column(name = "Level1Description")
    private String level1Description;

    @Column(name = "partNumber")
    private String partNumber;

    @Column(name = "L3Description")
    private String l3Description;

    @Column(name = "costCenter")
    private String costCenter;

    @Column(name = "Approval_Status")
    private String approvalStatus;

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "createdDateTime")
    private Date createdDateTime;

    @Column(name = "updatedBy")
    private String updatedBy;

    @Column(name = "updatedDatetime")
    private Date updatedDatetime;

    // Many-to-One relationship with tb_PONumber (PurchaseOrder)
    @ManyToOne
    @JoinColumn(name = "poNumber", referencedColumnName = "poNumber", insertable = false, updatable = false)
    private PurchaseOrder poNumberEntity;

    // ---------------------------
    // Standard getters & setters
    // ---------------------------

    public long getRecordNo() { return recordNo; }
    public void setRecordNo(long recordNo) { this.recordNo = recordNo; }

    public Date getRecordDateTime() { return recordDateTime; }
    public void setRecordDateTime(Date recordDateTime) { this.recordDateTime = recordDateTime; }

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

    public Date getDatePlacedInService() { return datePlacedInService; }
    public void setDatePlacedInService(Date datePlacedInService) { this.datePlacedInService = datePlacedInService; }

    public Date getPoDate() { return poDate; }
    public void setPoDate(Date poDate) { this.poDate = poDate; }

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

    public Date getCreatedDateTime() { return createdDateTime; }
    public void setCreatedDateTime(Date createdDateTime) { this.createdDateTime = createdDateTime; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public Date getUpdatedDatetime() { return updatedDatetime; }
    public void setUpdatedDatetime(Date updatedDatetime) { this.updatedDatetime = updatedDatetime; }

    public PurchaseOrder getPoNumberEntity() { return poNumberEntity; }
    public void setPoNumberEntity(PurchaseOrder poNumberEntity) {
        this.poNumberEntity = poNumberEntity;
        if (poNumberEntity != null) {
            this.poNumber = poNumberEntity.getPoNumber();
        }
    }


}