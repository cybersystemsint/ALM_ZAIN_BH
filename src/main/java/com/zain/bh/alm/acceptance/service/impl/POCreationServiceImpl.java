package com.zain.bh.alm.acceptance.service.impl;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.zain.bh.alm.acceptance.entity.PurchaseOrder;
import com.zain.bh.alm.acceptance.repository.PurchaseOrderRepository;
import com.zain.bh.alm.acceptance.service.POCreationService;

@Service
public class POCreationServiceImpl implements POCreationService {

    private static final Logger LOGGER = LogManager.getLogger(POCreationServiceImpl.class);

    private final PurchaseOrderRepository purchaseOrderRepo;

    public POCreationServiceImpl(PurchaseOrderRepository purchaseOrderRepo) {
        this.purchaseOrderRepo = purchaseOrderRepo;
    }

    @Override
    public Map<String, Object> processFromJson(String jsonRequest) {
        LOGGER.debug("Processing PO creation request");
        Map<String, Object> result = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        JSONArray jsonArray = new JSONArray(jsonRequest);
        boolean hasSuccess = false;
        List<String> validationErrors = new ArrayList<>();
        
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            long recordNo = jsonObject.getLong("recordNo");
            
            PurchaseOrder spldt = purchaseOrderRepo.findByRecordNo(recordNo);
            if (spldt != null) {
                hasSuccess = updatePO(spldt, jsonObject, dateFormat) || hasSuccess;
            } else {
                PurchaseOrder topRecord = purchaseOrderRepo.findTopByPoNumberAndLineNumberAndReleaseNum(
                    jsonObject.getString("poNumber"), 
                    String.valueOf(jsonObject.getInt("lineNumber")), 
                    jsonObject.getString("releaseNum"));
                
                if (topRecord == null) {
                    String typelookup = jsonObject.getString("typeLookUpCode").trim();
                    String releaseNum = jsonObject.getString("releaseNum").trim();
                    
                    if (!(typelookup.equalsIgnoreCase("BLANKET") && releaseNum.equalsIgnoreCase("0"))) {
                        hasSuccess = createPO(jsonObject, typelookup, releaseNum, dateFormat) || hasSuccess;
                    }
                } else {
                    validationErrors.add(jsonObject.getString("poNumber") + " " + jsonObject.getInt("lineNumber"));
                }
            }
        }
        
        result.put("responseinfo", hasSuccess ? "Success" : "Failed to save data");
        result.put("validationErrors", validationErrors);
        return result;
    }

    private boolean updatePO(PurchaseOrder spldt, JSONObject jsonObject, SimpleDateFormat dateFormat) {
        try {
            setPOFields(spldt, jsonObject, dateFormat);
            purchaseOrderRepo.save(spldt);
            LOGGER.debug("Updated PO: {}", spldt.getPoNumber());
            return true;
        } catch (Exception excc) {
            LOGGER.error("Error updating PO", excc);
            return false;
        }
    }

    private boolean createPO(JSONObject jsonObject, String typelookup, String releaseNum, SimpleDateFormat dateFormat) {
        try {
            PurchaseOrder nwspldt = new PurchaseOrder();
            if (typelookup.equalsIgnoreCase("BLANKET")) {
                nwspldt.setPoNumber(jsonObject.getString("poNumber").trim() + "-" + releaseNum);
            } else {
                nwspldt.setPoNumber(jsonObject.getString("poNumber").trim());
            }
            setPOFields(nwspldt, jsonObject, dateFormat);
            purchaseOrderRepo.save(nwspldt);
            LOGGER.debug("Created PO: {}", nwspldt.getPoNumber());
            return true;
        } catch (Exception excc) {
            LOGGER.error("Error creating PO", excc);
            return false;
        }
    }

    private void setPOFields(PurchaseOrder po, JSONObject json, SimpleDateFormat dateFormat) {
        po.setTypeLookUpCode(json.getString("typeLookUpCode").trim());
        po.setBlanketTotalAmount(json.getDouble("blanketTotalAmount"));
        po.setReleaseNum(json.getString("releaseNum").trim());
        po.setLineNumber(json.getInt("lineNumber"));
        po.setPrNum(json.getString("prNum").trim());
        po.setProjectName(json.getString("projectName").trim());
        po.setLineCancelFlag(json.getBoolean("lineCancelFlag"));
        po.setCancelReason(json.getString("cancelReason").trim());
        po.setItemPartNumber(json.getString("itemPartNumber").trim());
        po.setPrSubAllow(json.getBoolean("prSubAllow"));
        po.setCountryOfOrigin(json.getString("countryOfOrigin").trim());
        po.setPoOrderQuantity(json.getDouble("poOrderQuantity"));
        po.setPoQtyNew(json.getDouble("poQtyNew"));
        po.setQuantityReceived(json.getDouble("quantityReceived"));
        po.setQuantityDueOld(json.getDouble("quantityDueOld"));
        po.setQuantityDueNew(json.getDouble("quantityDueNew"));
        po.setQuantityBilled(json.getDouble("quantityBilled"));
        po.setCurrencyCode(json.getString("currencyCode").trim());
        po.setUnitPriceInPoCurrency(json.getDouble("unitPriceInPoCurrency"));
        po.setUnitPriceInSAR(json.getDouble("unitPriceInSAR"));
        po.setLinePriceInPoCurrency(json.getDouble("linePriceInPoCurrency"));
        po.setLinePriceInSAR(json.getDouble("linePriceInSAR"));
        po.setAmountReceived(json.getDouble("amountReceived"));
        po.setAmountDue(json.getDouble("amountDue"));
        po.setAmountDueNew(json.getDouble("amountDueNew"));
        po.setAmountBilled(json.getDouble("amountBilled"));
        po.setPoLineDescription(json.getString("poLineDescription").trim());
        po.setOrganizationName(json.getString("organizationName").trim());
        po.setOrganizationCode(json.getString("organizationCode").trim());
        po.setSubInventoryCode(json.getString("subInventoryCode").trim());
        po.setReceiptRouting(json.getString("receiptRouting").trim());
        po.setAuthorisationStatus(json.getString("authorisationStatus").trim());
        po.setPoClosureStatus(json.getString("poClosureStatus").trim());
        po.setDepartmentName(json.getString("departmentName").trim());
        po.setPoLineType(json.getString("poLineType").trim());
        po.setAcceptanceType(json.getString("acceptanceType").trim());
        po.setCostCenter(json.getString("costCenter").trim());
        po.setSerialControl(json.getString("serialControl").trim());
        po.setVendorSerialNumberYN(json.getString("vendorSerialNumberYN").trim());
        po.setItemType(json.getString("itemType").trim());
        po.setItemCategoryInventory(json.getString("itemCategoryInventory").trim());
        po.setInventoryCategoryDescription(json.getString("inventoryCategoryDescription").trim());
        po.setItemCategoryFA(json.getString("itemCategoryFA").trim());
        po.setFACategoryDescription(json.getString("FACategoryDescription").trim());
        po.setItemCategoryPurchasing(json.getString("itemCategoryPurchasing").trim());
        po.setPurchasingCategoryDescription(json.getString("purchasingCategoryDescription").trim());
        po.setVendorName(json.getString("vendorName").trim());
        po.setVendorNumber(json.getString("vendorNumber").trim());
        po.setCreatedBy(json.getInt("createdById"));
        po.setCreatedByName(json.getString("createdByName").trim());
        
        Double unitPrice = json.optDouble("unitPriceInPoCurrency", 0.0);
        Double poQtyNew = json.optDouble("poQtyNew", 0.0);
        Double poOrderQuantity = json.optDouble("poOrderQuantity", 0.0);
        if (poQtyNew > 0) {
            Double quantityDiff = poOrderQuantity - poQtyNew;
            po.setDescopedLinePriceInPoCurrency(quantityDiff * unitPrice);
            po.setNewLinePriceInPoCurrency(poQtyNew * unitPrice);
        }
        
        try {
            po.setApprovedDate(new Date(dateFormat.parse(json.getString("approvedDate")).getTime()));
            po.setCreatedDate(new Date(dateFormat.parse(json.getString("createdDate")).getTime()));
        } catch (ParseException ex) {
            LOGGER.error("Error parsing dates", ex);
        }
    }
}
