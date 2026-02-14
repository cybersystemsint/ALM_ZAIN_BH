package com.zain.bh.alm.acceptance.service.impl;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.zain.bh.alm.acceptance.entity.PurchaseOrderHeader;
import com.zain.bh.alm.acceptance.entity.PurchaseOrderLine;
import com.zain.bh.alm.acceptance.repository.PurchaseOrderHeaderRepository;
import com.zain.bh.alm.acceptance.repository.PurchaseOrderLineRepository;
import com.zain.bh.alm.acceptance.service.POHDLNService;
import com.zain.bh.alm.acceptance.util.DateTimeUtil;

@Service
public class POHDLNServiceImpl implements POHDLNService {

    private static final Logger LOGGER = LogManager.getLogger(POHDLNServiceImpl.class);

    private final PurchaseOrderHeaderRepository pohdRepo;
    private final PurchaseOrderLineRepository polnRepo;

    public POHDLNServiceImpl(PurchaseOrderHeaderRepository pohdRepo, PurchaseOrderLineRepository polnRepo) {
        this.pohdRepo = pohdRepo;
        this.polnRepo = polnRepo;
    }

    @Override
    public String processFromJson(String jsonRequest) {
        LOGGER.debug("Processing PO header/line request");
        JSONArray jsonArrayresponse = new JSONArray();
        JSONArray jsonArray = new JSONArray(jsonRequest);
        
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            long recordNo = jsonObject.getLong("recordNo");
            String poId = jsonObject.getString("poId");
            
            String addporesp = addEditPOHD(recordNo, DateTimeUtil.getFormattedDateTime(), poId, 
                    jsonObject.getString("poDate"), jsonObject.getString("supplierId"), 
                    jsonObject.getString("termsAndConditions"), jsonObject.getString("deliveryLocationId"), 
                    jsonObject.getString("createdBy"), jsonObject.getString("status"), jsonObject);
            
            if (addporesp.contains("Success")) {
                JSONArray po_line_data = jsonObject.getJSONArray("polineItems");
                if (po_line_data.length() > 0) {
                    postpoln(po_line_data.toString());
                }
            } else {
                JSONObject responsedata = new JSONObject();
                responsedata.put("recordNo", recordNo);
                responsedata.put("poId", poId);
                responsedata.put("DBresponse", addporesp);
                jsonArrayresponse.put(responsedata);
            }
        }
        
        return jsonArrayresponse.length() > 0 ? jsonArrayresponse.toString() : "Complete";
    }

    private String addEditPOHD(long recordNo, String recordDatetime, String poId, String poDate, String supplierId,
            String termsAndConditions, String deliveryLocationId, String createdBy, String status, JSONObject jsonObject) {
        String responseinfo = "Failed to save or data";
        PurchaseOrderHeader spldt = pohdRepo.findByRecordNo(recordNo);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (spldt != null) {
            spldt.setRecordDatetime(recordDatetime);
            spldt.setPoDate(poDate);
            spldt.setPoId(poId);
            spldt.setSupplierId(supplierId);
            spldt.setTermsAndConditions(termsAndConditions);
            spldt.setDeliveryLocationId(deliveryLocationId);
            spldt.setCreatedBy(createdBy);
            spldt.setStatus("updated");
            spldt.setModelNumber(jsonObject.getString("modelNumber"));
            spldt.setUnitOfMeasure(jsonObject.getString("unitOfMeasure"));
            spldt.setQtyPerSite(jsonObject.getInt("qtyPerSite"));
            spldt.setTotalNoofSites(jsonObject.getInt("totalNoofSites"));
            spldt.setTotalQty(jsonObject.getInt("totalQty"));
            spldt.setAccDepreciation(jsonObject.getDouble("accDepreciation"));
            spldt.setSalvageValue(jsonObject.getDouble("salvageValue"));
            spldt.setNewFACategory(jsonObject.getString("newFACategory"));
            spldt.setL1(jsonObject.getString("L1"));
            spldt.setL2(jsonObject.getString("L2"));
            spldt.setL3(jsonObject.getString("L3"));
            spldt.setL4(jsonObject.getString("L4"));
            spldt.setOldFACategory(jsonObject.getString("oldFACategory"));
            spldt.setAccDepreciationCode(jsonObject.getString("accDepreciationCode"));
            spldt.setDepreciationCode(jsonObject.getString("depreciationCode"));
            spldt.setLifeYears(jsonObject.getInt("lifeYears"));
            spldt.setVendorName(jsonObject.getString("vendorName"));
            spldt.setVendorNumber(jsonObject.getString("vendorNumber"));
            spldt.setProjectNumber(jsonObject.getString("projectNumber"));
            String dateInServiceString = jsonObject.getString("dateInService");

            if (jsonObject.has("typeLookUpCode")) spldt.setTypeLookUpCode(jsonObject.getString("typeLookUpCode"));
            if (jsonObject.has("releaseNum")) spldt.setReleaseNum(jsonObject.getString("releaseNum"));
            if (jsonObject.has("prNum")) spldt.setPrNum(jsonObject.getString("prNum"));
            if (jsonObject.has("pnSubAllow")) spldt.setPnSubAllow(jsonObject.getString("pnSubAllow"));
            if (jsonObject.has("countryOfOrigin")) spldt.setCountryOfOrigin(jsonObject.getString("countryOfOrigin"));
            if (jsonObject.has("currencyCode")) spldt.setCurrencyCode(jsonObject.getString("currencyCode"));
            if (jsonObject.has("subInventoryCode")) spldt.setSubInventoryCode(jsonObject.getString("subInventoryCode"));
            if (jsonObject.has("receiptRouting")) spldt.setReceiptRouting(jsonObject.getString("receiptRouting"));
            if (jsonObject.has("poClosureStatus")) spldt.setProjectNumber(jsonObject.getString("poClosureStatus"));
            if (jsonObject.has("chargeAccount")) spldt.setChargeAccount(jsonObject.getString("chargeAccount"));
            if (jsonObject.has("serialControl")) spldt.setSerialControl(jsonObject.getString("serialControl"));
            
            try {
                spldt.setDateInService(new Date(dateFormat.parse(dateInServiceString).getTime()));
            } catch (ParseException ex) {
                LOGGER.error("Error parsing date in service", ex);
            }
            spldt.setCurrency(jsonObject.getString("currency"));
            spldt.setUnitPrice(jsonObject.getDouble("unitPrice"));
            spldt.setPartNumber(jsonObject.getString("partNumber"));
            spldt.setCostCenter(jsonObject.getString("costCenter"));

            try {
                pohdRepo.save(spldt);
                responseinfo = "Record Updated Success";
            } catch (Exception excc) {
                LOGGER.error("Error updating PO header", excc);
                responseinfo = excc.toString();
            }
        } else {
            PurchaseOrderHeader nwspldt = new PurchaseOrderHeader();
            nwspldt.setRecordDatetime(recordDatetime);
            nwspldt.setPoDate(poDate);
            nwspldt.setPoId(poId);
            nwspldt.setSupplierId(supplierId);
            nwspldt.setTermsAndConditions(termsAndConditions);
            nwspldt.setDeliveryLocationId(deliveryLocationId);
            nwspldt.setCreatedBy(createdBy);
            nwspldt.setStatus("created");
            nwspldt.setModelNumber(jsonObject.getString("modelNumber"));
            nwspldt.setUnitOfMeasure(jsonObject.getString("unitOfMeasure"));
            nwspldt.setQtyPerSite(jsonObject.getInt("qtyPerSite"));
            nwspldt.setTotalNoofSites(jsonObject.getInt("totalNoofSites"));
            nwspldt.setTotalQty(jsonObject.getInt("totalQty"));
            nwspldt.setAccDepreciation(jsonObject.getDouble("accDepreciation"));
            nwspldt.setSalvageValue(jsonObject.getDouble("salvageValue"));
            nwspldt.setNewFACategory(jsonObject.getString("newFACategory"));
            nwspldt.setL1(jsonObject.getString("L1"));
            nwspldt.setL2(jsonObject.getString("L2"));
            nwspldt.setL3(jsonObject.getString("L3"));
            nwspldt.setL4(jsonObject.getString("L4"));
            nwspldt.setOldFACategory(jsonObject.getString("oldFACategory"));
            nwspldt.setAccDepreciationCode(jsonObject.getString("accDepreciationCode"));
            nwspldt.setDepreciationCode(jsonObject.getString("depreciationCode"));
            nwspldt.setLifeYears(jsonObject.getInt("lifeYears"));
            nwspldt.setVendorName(jsonObject.getString("vendorName"));
            nwspldt.setVendorNumber(jsonObject.getString("vendorNumber"));
            nwspldt.setProjectNumber(jsonObject.getString("projectNumber"));
            
            if (jsonObject.has("typeLookUpCode")) nwspldt.setTypeLookUpCode(jsonObject.getString("typeLookUpCode"));
            if (jsonObject.has("releaseNum")) nwspldt.setReleaseNum(jsonObject.getString("releaseNum"));
            if (jsonObject.has("prNum")) nwspldt.setPrNum(jsonObject.getString("prNum"));
            if (jsonObject.has("pnSubAllow")) nwspldt.setPnSubAllow(jsonObject.getString("pnSubAllow"));
            if (jsonObject.has("countryOfOrigin")) nwspldt.setCountryOfOrigin(jsonObject.getString("countryOfOrigin"));
            if (jsonObject.has("currencyCode")) nwspldt.setCurrencyCode(jsonObject.getString("currencyCode"));
            if (jsonObject.has("subInventoryCode")) nwspldt.setSubInventoryCode(jsonObject.getString("subInventoryCode"));
            if (jsonObject.has("receiptRouting")) nwspldt.setReceiptRouting(jsonObject.getString("receiptRouting"));
            if (jsonObject.has("poClosureStatus")) nwspldt.setProjectNumber(jsonObject.getString("poClosureStatus"));
            if (jsonObject.has("chargeAccount")) nwspldt.setChargeAccount(jsonObject.getString("chargeAccount"));
            if (jsonObject.has("serialControl")) nwspldt.setSerialControl(jsonObject.getString("serialControl"));
            
            nwspldt.setCurrency(jsonObject.getString("currency"));
            nwspldt.setUnitPrice(jsonObject.getDouble("unitPrice"));
            nwspldt.setPartNumber(jsonObject.getString("partNumber"));
            nwspldt.setCostCenter(jsonObject.getString("costCenter"));
            
            try {
                pohdRepo.save(nwspldt);
                responseinfo = "Record Created Success";
            } catch (Exception excc) {
                LOGGER.error("Error creating PO header", excc);
                responseinfo = excc.toString();
            }
        }
        return responseinfo;
    }

    private String postpoln(String req) {
        JSONArray jsonArrayresponse = new JSONArray();
        
        try {
            JSONArray jsonArray = new JSONArray(req);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                long recordNo = jsonObject.getLong("recordNo");
                String poId = jsonObject.getString("poId");
                long lineNumber = jsonObject.getLong("lineNumber");
                String itemCode = jsonObject.getString("itemCode");
                String UoM = jsonObject.getString("UoM");
                int orderQuantity = jsonObject.getInt("orderQuantity");
                BigDecimal unitPrice = jsonObject.getBigDecimal("unitPrice");
                BigDecimal VAT = jsonObject.getBigDecimal("VAT");
                BigDecimal linePrice = jsonObject.getBigDecimal("linePrice");
                
                String jsresp = addEditPOLN(jsonObject, recordNo, DateTimeUtil.getFormattedDateTime(), poId, lineNumber, itemCode, UoM, orderQuantity, unitPrice, VAT, linePrice);
                if (!jsresp.contains("Success")) {
                    JSONObject responsedata = new JSONObject();
                    responsedata.put("itemCode", itemCode);
                    responsedata.put("poId", poId);
                    responsedata.put("DBresponse", jsresp);
                    jsonArrayresponse.put(responsedata);
                }
            }
        } catch (Exception exc) {
            LOGGER.error("Error processing PO lines", exc);
        }
        return jsonArrayresponse.length() > 0 ? jsonArrayresponse.toString() : "Complete";
    }

    private String addEditPOLN(JSONObject jsonObject, long recordNo, String recordDatetime, String poId, long lineNumber, String itemCode, String UoM,
            int orderQuantity, BigDecimal unitPrice, BigDecimal VAT, BigDecimal linePrice) {
        String responseinfo = "Failed to save or data";
        PurchaseOrderLine spldt = polnRepo.findByRecordNo(recordNo);
        
        if (spldt != null) {
            spldt.setRecordDatetime(recordDatetime);
            spldt.setPoId(poId);
            spldt.setLineNumber(lineNumber);
            spldt.setItemCode(itemCode);
            spldt.setUnitPrice(unitPrice);
            spldt.setOrderQuantity(orderQuantity);
            spldt.setVAT(VAT);
            spldt.setLinePrice(linePrice);
            spldt.setUoM(UoM);

            if (jsonObject.has("quantityDueOld")) spldt.setQuantityDueOld(jsonObject.getString("quantityDueOld"));
            if (jsonObject.has("quantityDueNew")) spldt.setQuantityDueNew(jsonObject.getString("quantityDueNew"));
            if (jsonObject.has("quantityBilled")) spldt.setQuantityBilled(jsonObject.getString("quantityBilled"));
            if (jsonObject.has("unitPriceInSAR")) spldt.setUnitPriceInSAR(jsonObject.getBigDecimal("unitPriceInSAR"));
            if (jsonObject.has("linePriceInPoCurrency")) spldt.setLinePriceInPoCurrency(jsonObject.getBigDecimal("linePriceInPoCurrency"));
            if (jsonObject.has("linePriceInSAR")) spldt.setLinePriceInSAR(jsonObject.getBigDecimal("linePriceInSAR"));
            if (jsonObject.has("amountReceived")) spldt.setAmountReceived(jsonObject.getBigDecimal("amountReceived"));
            if (jsonObject.has("amountDue")) spldt.setAmountDue(jsonObject.getBigDecimal("amountDue"));
            if (jsonObject.has("amountDueNew")) spldt.setAmountDue(jsonObject.getBigDecimal("amountDueNew"));
            if (jsonObject.has("amountBilled")) spldt.setAmountBilled(jsonObject.getBigDecimal("amountBilled"));
            if (jsonObject.has("poLineType")) spldt.setPoLineType(jsonObject.getString("poLineType"));
            if (jsonObject.has("itemType")) spldt.setItemType(jsonObject.getString("itemType"));
            if (jsonObject.has("itemCategoryInventory")) spldt.setItemCategoryInventory(jsonObject.getString("itemCategoryInventory"));
            if (jsonObject.has("categoryDescription")) spldt.setCategoryDescription(jsonObject.getString("categoryDescription"));
            if (jsonObject.has("itemCategoryFA")) spldt.setItemCategoryFA(jsonObject.getString("itemCategoryFA"));
            if (jsonObject.has("FACategoryDescription")) spldt.setFACategoryDescription(jsonObject.getString("FACategoryDescription"));
            if (jsonObject.has("itemCategoryPurchasing")) spldt.setItemCategoryPurchasing(jsonObject.getString("itemCategoryPurchasing"));
            if (jsonObject.has("PurchasingCategoryDescription")) spldt.setPurchasingCategoryDescription(jsonObject.getString("PurchasingCategoryDescription"));

            try {
                polnRepo.save(spldt);
                responseinfo = "Record Updated Success";
            } catch (Exception excc) {
                LOGGER.error("Error updating PO line", excc);
                responseinfo = excc.toString();
            }
        } else {
            PurchaseOrderLine nwspldt = new PurchaseOrderLine();
            nwspldt.setRecordDatetime(recordDatetime);
            nwspldt.setPoId(poId);
            nwspldt.setLineNumber(lineNumber);
            nwspldt.setItemCode(itemCode);
            nwspldt.setUnitPrice(unitPrice);
            nwspldt.setOrderQuantity(orderQuantity);
            nwspldt.setVAT(VAT);
            nwspldt.setLinePrice(linePrice);
            nwspldt.setUoM(UoM);

            if (jsonObject.has("quantityDueOld")) nwspldt.setQuantityDueOld(jsonObject.getString("quantityDueOld"));
            if (jsonObject.has("quantityDueNew")) nwspldt.setQuantityDueNew(jsonObject.getString("quantityDueNew"));
            if (jsonObject.has("quantityBilled")) nwspldt.setQuantityBilled(jsonObject.getString("quantityBilled"));
            if (jsonObject.has("unitPriceInSAR")) nwspldt.setUnitPriceInSAR(jsonObject.getBigDecimal("unitPriceInSAR"));
            if (jsonObject.has("linePriceInPoCurrency")) nwspldt.setLinePriceInPoCurrency(jsonObject.getBigDecimal("linePriceInPoCurrency"));
            if (jsonObject.has("linePriceInSAR")) nwspldt.setLinePriceInSAR(jsonObject.getBigDecimal("linePriceInSAR"));
            if (jsonObject.has("amountReceived")) nwspldt.setAmountReceived(jsonObject.getBigDecimal("amountReceived"));
            if (jsonObject.has("amountDue")) nwspldt.setAmountDue(jsonObject.getBigDecimal("amountDue"));
            if (jsonObject.has("amountDueNew")) nwspldt.setAmountDue(jsonObject.getBigDecimal("amountDueNew"));
            if (jsonObject.has("amountBilled")) nwspldt.setAmountBilled(jsonObject.getBigDecimal("amountBilled"));
            if (jsonObject.has("poLineType")) nwspldt.setPoLineType(jsonObject.getString("poLineType"));
            if (jsonObject.has("itemType")) nwspldt.setItemType(jsonObject.getString("itemType"));
            if (jsonObject.has("itemCategoryInventory")) nwspldt.setItemCategoryInventory(jsonObject.getString("itemCategoryInventory"));
            if (jsonObject.has("categoryDescription")) nwspldt.setCategoryDescription(jsonObject.getString("categoryDescription"));
            if (jsonObject.has("itemCategoryFA")) nwspldt.setItemCategoryFA(jsonObject.getString("itemCategoryFA"));
            if (jsonObject.has("FACategoryDescription")) nwspldt.setFACategoryDescription(jsonObject.getString("FACategoryDescription"));
            if (jsonObject.has("itemCategoryPurchasing")) nwspldt.setItemCategoryPurchasing(jsonObject.getString("itemCategoryPurchasing"));
            if (jsonObject.has("PurchasingCategoryDescription")) nwspldt.setPurchasingCategoryDescription(jsonObject.getString("PurchasingCategoryDescription"));

            try {
                polnRepo.save(nwspldt);
                responseinfo = "Record Created Success";
            } catch (Exception excc) {
                LOGGER.error("Error creating PO line", excc);
                responseinfo = excc.toString();
            }
        }
        return responseinfo;
    }
}
