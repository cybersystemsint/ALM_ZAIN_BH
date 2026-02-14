package com.zain.bh.alm.acceptance.service.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.zain.bh.alm.acceptance.entity.DCCLineItem;
import com.zain.bh.alm.acceptance.entity.PurchaseOrder;
import com.zain.bh.alm.acceptance.entity.PurchaseOrderUPL;
import com.zain.bh.alm.acceptance.repository.DCCLineItemRepository;
import com.zain.bh.alm.acceptance.repository.PurchaseOrderRepository;
import com.zain.bh.alm.acceptance.repository.PurchaseOrderUPLRepository;
import com.zain.bh.alm.acceptance.service.POUPLCreationService;

@Service
public class POUPLCreationServiceImpl implements POUPLCreationService {

    private static final Logger LOGGER = LogManager.getLogger(POUPLCreationServiceImpl.class);

    private final PurchaseOrderUPLRepository purchaseOrderUPLRepo;
    private final PurchaseOrderRepository purchaseOrderRepo;
    private final DCCLineItemRepository dccLineRepo;

    public POUPLCreationServiceImpl(PurchaseOrderUPLRepository purchaseOrderUPLRepo,
                                    PurchaseOrderRepository purchaseOrderRepo,
                                    DCCLineItemRepository dccLineRepo) {
        this.purchaseOrderUPLRepo = purchaseOrderUPLRepo;
        this.purchaseOrderRepo = purchaseOrderRepo;
        this.dccLineRepo = dccLineRepo;
    }

    @Override
    public Map<String, Object> processFromJson(String jsonRequest) {
        LOGGER.debug("Processing PO UPL creation request");
        Date now = new Date(System.currentTimeMillis());
        JSONArray jsonArray = new JSONArray(jsonRequest);
        List<String> validationErrors = new ArrayList<>();
        List<String> missingPoNumbers = new ArrayList<>();
        boolean hasSuccess = false;

        // Validation phase
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            long recordNo = jsonObject.getLong("recordNo");

            if (recordNo == 0) {
                List<PurchaseOrderUPL> existing = purchaseOrderUPLRepo.findByPoNumberAndPoLineNumberAndUplLine(
                        jsonObject.getString("poNumber"),
                        jsonObject.getString("poLineNumber"),
                        jsonObject.getString("uplLine"));
                if (!existing.isEmpty()) {
                    validationErrors.add(jsonObject.getString("uplLine") + " ");
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("responseinfo", "The records having with lines: " + String.join("; ", validationErrors) + " already exists. Please check and try again. ");
            result.put("missingPoNumbers", Collections.emptyList());
            return result;
        }

        // Processing phase
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            long recordNo = jsonObject.getLong("recordNo");
            String poNumber = jsonObject.getString("poNumber");

            List<PurchaseOrder> validatePoList = purchaseOrderRepo.findByPoNumber(poNumber);
            if (!validatePoList.isEmpty()) {
                PurchaseOrderUPL existing = purchaseOrderUPLRepo.findByRecordNo(recordNo);
                if (existing != null) {
                    List<DCCLineItem> acceptanceRecords = dccLineRepo.findByPoIdAndLineNumberAndUplLineNumber(
                            poNumber.trim(),
                            jsonObject.getString("poLineNumber").trim(),
                            jsonObject.getString("uplLine").trim());
                    if (acceptanceRecords.isEmpty()) {
                        updateUPL(existing, jsonObject, now);
                        hasSuccess = true;
                    }
                } else {
                    createUPL(jsonObject, now);
                    hasSuccess = true;
                }
            } else {
                missingPoNumbers.add(poNumber);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("responseinfo", hasSuccess ? "Success" : "Failed to save data");
        result.put("missingPoNumbers", missingPoNumbers);
        return result;
    }

    private void updateUPL(PurchaseOrderUPL upl, JSONObject json, Date now) {
        upl.setRecordDatetime(now);
        setUPLFields(upl, json);
        upl.setUplModifiedBy(json.getString("modifiedBy").trim());
        upl.setUplModifiedDate(now);
        purchaseOrderUPLRepo.save(upl);
        LOGGER.debug("Updated UPL: {}", upl.getPoNumber());
    }

    private void createUPL(JSONObject json, Date now) {
        PurchaseOrderUPL upl = new PurchaseOrderUPL();
        upl.setRecordDatetime(now);
        setUPLFields(upl, json);
        purchaseOrderUPLRepo.save(upl);
        LOGGER.debug("Created UPL: {}", upl.getPoNumber());
    }

    private void setUPLFields(PurchaseOrderUPL upl, JSONObject json) {
        upl.setVendor(json.getString("vendor").trim());
        upl.setManufacturer(json.getString("manufacturer").trim());
        upl.setCountryOfOrigin(json.getString("countryOfOrigin").trim());
        upl.setProjectName(json.getString("projectName").trim());
        upl.setPoType(json.getString("poType").trim());
        upl.setReleaseNumber(json.getString("releaseNumber").trim());
        upl.setPoNumber(json.getString("poNumber").trim());
        upl.setPoLineNumber(json.getString("poLineNumber").trim());
        upl.setUplLine(json.getString("uplLine").trim());
        upl.setPoLineItemType(json.getString("poLineItemType").trim());
        upl.setPoLineItemCode(json.getString("poLineItemCode").trim());
        upl.setPoLineDescription(json.getString("poLineDescription").trim());
        upl.setUplLineItemType(json.getString("uplLineItemType").trim());
        upl.setUplLineItemCode(json.getString("uplLineItemCode").trim());
        upl.setUplLineDescription(json.getString("uplLineDescription").trim());
        upl.setZainItemCategoryCode(json.getString("zainItemCategoryCode").trim());
        upl.setZainItemCategoryDescription(json.getString("zainItemCategoryDescription").trim());
        upl.setUplItemSerialized(json.getString("uplItemSerialized").trim());
        upl.setActiveOrPassive(json.getString("activeOrPassive").trim());
        upl.setUom(json.getString("uom").trim());
        upl.setCurrency(json.getString("currency").trim());
        upl.setPoLineQuantity(json.getDouble("poLineQuantity"));
        upl.setPoLineUnitPrice(json.getDouble("poLineUnitPrice"));
        upl.setUplLineQuantity(json.getDouble("uplLineQuantity"));
        upl.setUplLineUnitPrice(json.getDouble("uplLineUnitPrice"));
        upl.setSubstituteItemCode(json.getString("substituteItemCode").trim());
        upl.setRemarks(json.getString("remarks").trim());
        upl.setCreatedBy(json.getInt("createdById"));
        upl.setCreatedByName(json.getString("createdByName").trim());
    }
}
