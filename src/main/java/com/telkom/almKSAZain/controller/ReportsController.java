package com.telkom.almKSAZain.controller;

import com.telkom.almKSAZain.repo.DccCombinedViewrepo;
import com.telkom.almKSAZain.repo.poviewrepo;
import com.telkom.almKSAZain.repo.dccpoviewrepo;
import com.telkom.almKSAZain.repo.uplrepo;

import com.telkom.almKSAZain.model.DccPoCombinedView;
import com.telkom.almKSAZain.model.upldata;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.telkom.almKSAZain.helper.helper;
//import com.telkom.almKSAZain.model.tb_ChargeAccount;
import com.telkom.almKSAZain.repo.tbChargeAccountRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
public class ReportsController {

    private final Logger loggger = LogManager.getLogger(ReportsController.class);
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    uplrepo uprepo;

    @Autowired
    poviewrepo povwrepo;

    @Autowired
    DccCombinedViewrepo dccpocombinedviewrp;

    @Autowired
    dccpoviewrepo dccpoviewrp;

    @Autowired
    tbChargeAccountRepo chargeAccountRepo;

    String genHeader(String msisdn, String reqid, String Channel) {
        return " | " + reqid + " | " + Channel + " | " + msisdn + " | ";
    }

    private Map<String, String> response(String result, String msg) {
        HashMap<String, String> map = new HashMap<>();
        map.put("responseCode", result.equalsIgnoreCase("success") ? "0" : "1001");
        map.put("responseMessage", msg);
        return map;
    }

    ///GET ALL CREATED CHARGE ACCOUNTS
    @PostMapping(value = "/reports/getAllItemCodeSubstitutes", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public List<Map<String, Object>> getAllItemCodeSubstitutes(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        Integer recordNo = obj.get("recordNo").getAsInt();

        String sql = "SELECT recordNo, recordDateTime, itemCode, relatedItemCode, reciprocalFlag, createdBy, createdDatetime, updatedBy, updatedDateTime FROM tb_ItemCodeSubstitute;";
        if (recordNo != 0) {
            sql = "SELECT recordNo, recordDateTime, itemCode, relatedItemCode, reciprocalFlag, createdBy, createdDatetime, updatedBy, updatedDateTime FROM tb_ItemCodeSubstitute WHERE recordNo='" + recordNo + "'";
        }
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return result;
    }

    ///GET ALL CREATED CHARGE ACCOUNTS
    @PostMapping(value = "/reports/getAllChargeAccounts", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public List<Map<String, Object>> getAllChargeAccounts(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        Integer recordNo = obj.get("recordNo").getAsInt();

        String sql = "SELECT recordNo, recordDatetime, chargeAccount, orgCode, orgName, subInventory, createdBy, createdDatetime, updatedBy, updatedDate AS updatedDatetime FROM tb_ChargeAccount;";
        if (recordNo != 0) {
            sql = "SELECT recordNo, recordDatetime, chargeAccount, orgCode, orgName, subInventory, createdBy, createdDatetime, updatedBy, updatedDate AS updatedDatetime FROM tb_ChargeAccount WHERE recordNo='" + recordNo + "'";
        }
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return result;
    }

    //==================GET POS PER VENDOR =====
    @PostMapping(value = "/reports/getpodata", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public List<Map<String, Object>> getpodata(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String supplierId = obj.get("supplierId").getAsString();
        //        String sql = "SELECT PO.poId, PO.poDate, PO.supplierId,PO.status,PO.modelNumber, PO.unitOfMeasure, PO.qtyPerSite, PO.totalNoOfSites, PO.newFACategory, PO.L1,PO.L2,PO.L3,PO.L4,PO.oldFACategory, PO.vendorName, PO.oldFACategory, PO.accDepreciationCode, PO.depreciationCode, PO.lifeYears, PO.vendorName, PO.vendorNumber, PO.dateInService, PO.currency, PO.projectNumber , LN.lineNumber, LN.itemCode, LN.orderQuantity,LN.unitPrice, LN.VAT, LN.UoM,LN.linePrice, PO.costCenter, PO.partNumber,INV.serialNumber ,INV.manufacturerId, INV.acquisitionDate, INV.manufactureDate, INV.locationId, UPL.uplLine, UPL.poLineItemDescription, UPL.quantity AS DeliveredQuantity, UPL.zainItemCategory, UPL.activePassive FROM tb_PO_HD PO LEFT JOIN tb_PO_LN LN ON PO.poId = LN.poId LEFT JOIN tb_Inventory INV ON PO.poId = INV.PONumber LEFT JOIN tb_UPL UPL ON PO.poId = UPL.poId;";

        String sql = "SELECT PO.poId, PO.poDate, PO.supplierId,PO.status,PO.modelNumber, PO.unitOfMeasure, PO.qtyPerSite, PO.totalNoOfSites, PO.newFACategory, PO.L1,PO.L2,PO.L3,PO.L4,PO.oldFACategory, PO.vendorName, PO.oldFACategory, PO.accDepreciationCode, PO.depreciationCode, PO.lifeYears, PO.vendorName, PO.vendorNumber, PO.dateInService, PO.currency, PO.projectNumber , LN.lineNumber, LN.itemCode, LN.orderQuantity,LN.unitPrice, LN.VAT, LN.UoM,LN.linePrice, PO.costCenter, PO.partNumber, UPL.uplLine, UPL.poLineItemDescription, UPL.quantity AS DeliveredQuantity, UPL.zainItemCategory, UPL.serialized, UPL.activePassive, PO.typeLookUpCode,PO.prNum,PO.pnSubAllow,PO.countryOfOrigin,PO.currencyCode,PO.subInventoryCode,PO.receiptRouting,PO.poClosureStatus,PO.chargeAccount,PO.serialControl,LN.unitPriceInSAR,LN.linePriceInPoCurrency,LN.linePriceInSAR,LN.poLineType,LN.itemType,LN.itemCategoryInventory,LN.categoryDescription,LN.itemCategoryFA,LN.FACategoryDescription,LN.itemCategoryPurchasing,LN.PurchasingCategoryDescription FROM tb_PO_HD PO LEFT JOIN tb_PO_LN LN ON PO.poId = LN.poId LEFT JOIN  tb_UPL UPL ON LN.poId = UPL.poId AND LN.lineNumber = UPL.poLine;";
        if (!supplierId.equalsIgnoreCase("0")) {
            sql = "SELECT PO.poId, PO.poDate, PO.supplierId,PO.status,PO.modelNumber, PO.unitOfMeasure, PO.qtyPerSite, PO.totalNoOfSites, PO.newFACategory, PO.L1,PO.L2,PO.L3,PO.L4,PO.oldFACategory, PO.vendorName, PO.oldFACategory, PO.accDepreciationCode, PO.depreciationCode, PO.lifeYears, PO.vendorName, PO.vendorNumber, PO.dateInService, PO.currency, PO.projectNumber , LN.lineNumber, LN.itemCode, LN.orderQuantity,LN.unitPrice, LN.VAT, LN.UoM,LN.linePrice, PO.costCenter, PO.partNumber, UPL.uplLine, UPL.poLineItemDescription, UPL.quantity AS DeliveredQuantity, UPL.zainItemCategory, UPL.serialized, UPL.activePassive, PO.typeLookUpCode,PO.prNum,PO.pnSubAllow,PO.countryOfOrigin,PO.currencyCode,PO.subInventoryCode,PO.receiptRouting,PO.poClosureStatus,PO.chargeAccount,PO.serialControl,LN.unitPriceInSAR,LN.linePriceInPoCurrency,LN.linePriceInSAR,LN.poLineType,LN.itemType,LN.itemCategoryInventory,LN.categoryDescription,LN.itemCategoryFA,LN.FACategoryDescription,LN.itemCategoryPurchasing,LN.PurchasingCategoryDescription FROM tb_PO_HD PO LEFT JOIN tb_PO_LN LN ON PO.poId = LN.poId LEFT JOIN  tb_UPL UPL ON LN.poId = UPL.poId AND LN.lineNumber = UPL.poLine WHERE PO.supplierid='" + supplierId + "'";
        }
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return result;
    }

    //==================GET POS PER VENDOR =====
    @PostMapping(value = "/reports/getAllPurchaseOrders", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public List<Map<String, Object>> getAllPurchaseOrders(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String supplierId = obj.get("supplierId").getAsString();
        //        String sql = "SELECT PO.poId, PO.poDate, PO.supplierId,PO.status,PO.modelNumber, PO.unitOfMeasure, PO.qtyPerSite, PO.totalNoOfSites, PO.newFACategory, PO.L1,PO.L2,PO.L3,PO.L4,PO.oldFACategory, PO.vendorName, PO.oldFACategory, PO.accDepreciationCode, PO.depreciationCode, PO.lifeYears, PO.vendorName, PO.vendorNumber, PO.dateInService, PO.currency, PO.projectNumber , LN.lineNumber, LN.itemCode, LN.orderQuantity,LN.unitPrice, LN.VAT, LN.UoM,LN.linePrice, PO.costCenter, PO.partNumber,INV.serialNumber ,INV.manufacturerId, INV.acquisitionDate, INV.manufactureDate, INV.locationId, UPL.uplLine, UPL.poLineItemDescription, UPL.quantity AS DeliveredQuantity, UPL.zainItemCategory, UPL.activePassive FROM tb_PO_HD PO LEFT JOIN tb_PO_LN LN ON PO.poId = LN.poId LEFT JOIN tb_Inventory INV ON PO.poId = INV.PONumber LEFT JOIN tb_UPL UPL ON PO.poId = UPL.poId;";
        String sql = "SELECT PO.recordNo,    PO.poNumber,    PO.typeLookUpCode,    PO.blanketTotalAmount,    PO.releaseNum,    PO.lineNumber,    PO.prNum,    PO.projectName,    PO.lineCancelFlag,    PO.cancelReason,    PO.itemPartNumber,    PO.prSubAllow,    PO.countryOfOrigin,    PO.poOrderQuantity,    PO.poQtyNew,    PO.quantityReceived,    PO.quantityDueOld,    PO.quantityDueNew,    PO.quantityBilled,    PO.currencyCode,    PO.unitPriceInPoCurrency,    PO.unitPriceInSAR,    PO.linePriceInPoCurrency,    PO.linePriceInSAR,    PO.amountReceived,    PO.amountDue,    PO.amountDueNew,    PO.amountBilled,    PO.poLineDescription,    PO.organizationName,    PO.organizationCode,    PO.subInventoryCode,    PO.receiptRouting,    PO.authorisationStatus,    PO.poClosureStatus,    PO.departmentName,    PO.businessOwner,    PO.poLineType,    PO.acceptanceType,\n"
                + "    PO.costCenter,    PO.chargeAccount,    PO.serialControl,    PO.vendorSerialNumberYN,    PO.itemType,    PO.itemCategoryInventory,    PO.inventoryCategoryDescription,    PO.itemCategoryFA,    PO.FACategoryDescription,    PO.itemCategoryPurchasing,    PO.PurchasingCategoryDescription,    PO.vendorName,    PO.vendorNumber,    PO.approvedDate,\n"
                + "    PO.createdDate, CASE  WHEN `PO`.`lineCancelFlag` = 0  AND `PO`.`authorisationStatus` = 'APPROVED'    AND `PO`.`poClosureStatus` = 'OPEN'     THEN 'YES'    ELSE 'NO'  END AS `canRaiseAcceptance`, PO.createdByName, PO.descopedLinePriceInPoCurrency, PO.newLinePriceInPoCurrency FROM  tb_PurchaseOrder PO;";
        if (!supplierId.equalsIgnoreCase("0")) {
            sql = "SELECT PO.recordNo,   PO.poNumber,    PO.typeLookUpCode,    PO.blanketTotalAmount,    PO.releaseNum,    PO.lineNumber,    PO.prNum,    PO.projectName,    PO.lineCancelFlag,    PO.cancelReason,    PO.itemPartNumber,    PO.prSubAllow,    PO.countryOfOrigin,    PO.poOrderQuantity,    PO.poQtyNew,    PO.quantityReceived,    PO.quantityDueOld,    PO.quantityDueNew,    PO.quantityBilled,    PO.currencyCode,    PO.unitPriceInPoCurrency,    PO.unitPriceInSAR,    PO.linePriceInPoCurrency,    PO.linePriceInSAR,    PO.amountReceived,    PO.amountDue,    PO.amountDueNew,    PO.amountBilled,    PO.poLineDescription,    PO.organizationName,    PO.organizationCode,    PO.subInventoryCode,    PO.receiptRouting,    PO.authorisationStatus,    PO.poClosureStatus,    PO.departmentName,    PO.businessOwner,    PO.poLineType,    PO.acceptanceType,\n"
                    + "    PO.costCenter,    PO.chargeAccount,    PO.serialControl,    PO.vendorSerialNumberYN,    PO.itemType,    PO.itemCategoryInventory,    PO.inventoryCategoryDescription,    PO.itemCategoryFA,    PO.FACategoryDescription,    PO.itemCategoryPurchasing,    PO.PurchasingCategoryDescription,    PO.vendorName,    PO.vendorNumber,    PO.approvedDate,\n"
                    + "    PO.createdDate, CASE  WHEN `PO`.`lineCancelFlag` = 0  AND `PO`.`authorisationStatus` = 'APPROVED'    AND `PO`.`poClosureStatus` = 'OPEN'     THEN 'YES'    ELSE 'NO'  END AS `canRaiseAcceptance`,  PO.createdByName, PO.descopedLinePriceInPoCurrency, PO.newLinePriceInPoCurrency FROM  tb_PurchaseOrder PO  WHERE PO.vendorNumber='" + supplierId + "'";
        }
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return result;
    }

    @PostMapping(value = "/reports/getNestedPurchaseOrders", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public List<Map<String, Object>> getNestedPurchaseOrders(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String supplierId = obj.get("supplierId").getAsString();
        String poID = obj.get("poNumber").getAsString();

        String sql = "SELECT PO.recordNo, PO.poNumber, PO.typeLookUpCode, PO.blanketTotalAmount, PO.releaseNum, PO.lineNumber, PO.prNum, PO.projectName, PO.lineCancelFlag, PO.cancelReason, PO.itemPartNumber, PO.prSubAllow, PO.countryOfOrigin, PO.poOrderQuantity, PO.poQtyNew, PO.quantityReceived, PO.quantityDueOld, PO.quantityDueNew, PO.quantityBilled, PO.currencyCode, PO.unitPriceInPoCurrency, PO.unitPriceInSAR, PO.linePriceInPoCurrency, PO.linePriceInSAR, PO.amountReceived, PO.amountDue, PO.amountDueNew, PO.amountBilled, PO.poLineDescription, PO.organizationName, PO.organizationCode, PO.subInventoryCode, PO.receiptRouting, PO.authorisationStatus, PO.poClosureStatus, PO.departmentName, PO.businessOwner, PO.poLineType, PO.acceptanceType, PO.costCenter, PO.chargeAccount, PO.serialControl, PO.vendorSerialNumberYN, PO.itemType, PO.itemCategoryInventory, PO.inventoryCategoryDescription, PO.itemCategoryFA, PO.FACategoryDescription, PO.itemCategoryPurchasing, PO.PurchasingCategoryDescription, PO.vendorName, PO.vendorNumber, PO.approvedDate, PO.createdDate, CASE  WHEN `PO`.`lineCancelFlag` = 0  AND `PO`.`authorisationStatus` = 'APPROVED'    AND `PO`.`poClosureStatus` = 'OPEN'     THEN 'YES'    ELSE 'NO'  END AS `canRaiseAcceptance`,  PO.createdByName, PO.descopedLinePriceInPoCurrency, PO.newLinePriceInPoCurrency "
                + "FROM tb_PurchaseOrder PO ";
        // + "LEFT JOIN tb_UPL UPL ON PO.poNumber = UPL.poId AND PO.lineNumber = UPL.poLine";

        if (!supplierId.equalsIgnoreCase("0") && !poID.equalsIgnoreCase("0")) {
            sql += " WHERE PO.vendorNumber='" + supplierId + "' AND PO.poNumber='" + poID + "'";
        } else if (!supplierId.equalsIgnoreCase("0") && poID.equalsIgnoreCase("0")) {
            sql += " WHERE PO.vendorNumber='" + supplierId + "'";
        } else if (supplierId.equalsIgnoreCase("0") && !poID.equalsIgnoreCase("0")) {
            sql += " WHERE PO.poNumber='" + poID + "'";
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        Map<String, Map<String, Object>> groupedResults = new LinkedHashMap<>();

        for (Map<String, Object> row : rows) {
            String poNumber = (String) row.get("poNumber");

            if (!groupedResults.containsKey(poNumber)) {
                Map<String, Object> groupedRow = new LinkedHashMap<>(row);
                groupedRow.remove("recordNo");
                groupedRow.remove("lineNumber");
                groupedRow.remove("countryOfOrigin");
                groupedRow.remove("poOrderQuantity");
                groupedRow.remove("poQtyNew");
                groupedRow.remove("quantityReceived");
                groupedRow.remove("quantityDueOld");
                groupedRow.remove("quantityDueNew");
                groupedRow.remove("quantityBilled");
                groupedRow.remove("unitPriceInPoCurrency");
                groupedRow.remove("unitPriceInSAR");
                groupedRow.remove("linePriceInPoCurrency");
                groupedRow.remove("linePriceInSAR");
                groupedRow.remove("amountReceived");
                groupedRow.remove("amountDue");
                groupedRow.remove("amountDueNew");
                groupedRow.remove("amountBilled");
                groupedRow.remove("poLineDescription");
                groupedRow.remove("vendorSerialNumberYN");
                groupedRow.remove("itemCategoryInventory");
                groupedRow.remove("inventoryCategoryDescription");
                groupedRow.remove("itemCategoryFA");
                groupedRow.remove("FACategoryDescription");
                groupedRow.remove("descopedLinePriceInPoCurrency");
                groupedRow.remove("newLinePriceInPoCurrency");
                groupedRow.put("totalPoQtyNew", 0.0);
                groupedRow.put("totalQuantityReceived", 0.0);
                groupedRow.put("totalQuantityDueOld", 0.0);
                groupedRow.put("totalQuantityDueNew", 0.0);
                groupedRow.put("totalQuantityBilled", 0.0);
                groupedRow.put("totalpoOrderQuantity", 0.0);
                groupedRow.put("totalunitPriceInPoCurrency", 0.0);
                groupedRow.put("totalunitPriceInSAR", 0.0);
                groupedRow.put("totallinePriceInPoCurrency", 0.0);
                groupedRow.put("totallinePriceInSAR", 0.0);
                groupedRow.put("totalamountReceived", 0.0);
                groupedRow.put("totalamountDue", 0.0);
                groupedRow.put("totalamountDueNew", 0.0);
                groupedRow.put("totalamountBilled", 0.0);
                groupedRow.put("totalDescopedLinePriceInPoCurrency", 0.0);
                groupedRow.put("totalNewLinePriceInPoCurrency", 0.0);
                // Add POlineItems key with an empty list
                groupedRow.put("POlineItems", new ArrayList<Map<String, Object>>());
                groupedResults.put(poNumber, groupedRow);
            }

            Map<String, Object> poLineItem = new LinkedHashMap<>();
            poLineItem.put("recordNo", row.get("recordNo"));
            poLineItem.put("poNumber", row.get("poNumber"));
            poLineItem.put("lineNumber", row.get("lineNumber"));
            poLineItem.put("itemPartNumber", row.get("itemPartNumber"));
            poLineItem.put("countryOfOrigin", row.get("countryOfOrigin"));
            poLineItem.put("poOrderQuantity", row.get("poOrderQuantity"));
            poLineItem.put("poQtyNew", row.get("poQtyNew"));
            poLineItem.put("quantityReceived", row.get("quantityReceived"));
            poLineItem.put("quantityDueOld", row.get("quantityDueOld"));
            poLineItem.put("quantityDueNew", row.get("quantityDueNew"));
            poLineItem.put("quantityBilled", row.get("quantityBilled"));
            poLineItem.put("unitPriceInPoCurrency", row.get("unitPriceInPoCurrency"));
            poLineItem.put("unitPriceInSAR", row.get("unitPriceInSAR"));
            poLineItem.put("linePriceInPoCurrency", row.get("linePriceInPoCurrency"));
            poLineItem.put("linePriceInSAR", row.get("linePriceInSAR"));
            poLineItem.put("amountReceived", row.get("amountReceived"));
            poLineItem.put("amountDue", row.get("amountDue"));
            poLineItem.put("amountDueNew", row.get("amountDueNew"));
            poLineItem.put("amountBilled", row.get("amountBilled"));
            poLineItem.put("poLineDescription", row.get("poLineDescription"));
            poLineItem.put("vendorSerialNumberYN", row.get("vendorSerialNumberYN"));
            poLineItem.put("itemCategoryInventory", row.get("itemCategoryInventory"));
            poLineItem.put("inventoryCategoryDescription", row.get("inventoryCategoryDescription"));
            poLineItem.put("itemCategoryFA", row.get("itemCategoryFA"));
            poLineItem.put("FACategoryDescription", row.get("FACategoryDescription"));
            poLineItem.put("descopedLinePriceInPoCurrency", row.get("descopedLinePriceInPoCurrency"));
            poLineItem.put("newLinePriceInPoCurrency", row.get("newLinePriceInPoCurrency"));
            // Add other POlineItem specific columns here
            ((List<Map<String, Object>>) groupedResults.get(poNumber).get("POlineItems")).add(poLineItem);

            // Update totals
            Double poOrderQuantity = (row.get("poOrderQuantity") != null) ? ((Number) row.get("poOrderQuantity")).doubleValue() : 0.0;
            Double poQtyNew = (row.get("poQtyNew") != null) ? ((Number) row.get("poQtyNew")).doubleValue() : 0;
            Double quantityReceived = (row.get("quantityReceived") != null) ? ((Number) row.get("quantityReceived")).doubleValue() : 0.0;
            Double quantityDueOld = (row.get("quantityDueOld") != null) ? ((Number) row.get("quantityDueOld")).doubleValue() : 0.0;
            Double quantityDueNew = (row.get("quantityDueNew") != null) ? ((Number) row.get("quantityDueNew")).doubleValue() : 0.0;
            Double quantityBilled = (row.get("quantityBilled") != null) ? ((Number) row.get("quantityBilled")).doubleValue() : 0.0;
            Double unitPriceInPoCurrency = (row.get("unitPriceInPoCurrency") != null) ? ((Number) row.get("unitPriceInPoCurrency")).doubleValue() : 0.0;
            Double unitPriceInSAR = (row.get("unitPriceInSAR") != null) ? ((Number) row.get("unitPriceInSAR")).doubleValue() : 0.0;
            Double linePriceInPoCurrency = (row.get("linePriceInPoCurrency") != null) ? ((Number) row.get("linePriceInPoCurrency")).doubleValue() : 0.0;
            Double linePriceInSAR = (row.get("linePriceInSAR") != null) ? ((Number) row.get("linePriceInSAR")).doubleValue() : 0.0;
            Double amountReceived = (row.get("amountReceived") != null) ? ((Number) row.get("amountReceived")).doubleValue() : 0.0;
            Double amountDue = (row.get("amountDue") != null) ? ((Number) row.get("amountDue")).doubleValue() : 0.0;
            Double amountDueNew = (row.get("amountDueNew") != null) ? ((Number) row.get("amountDueNew")).doubleValue() : 0.0;
            Double amountBilled = (row.get("amountBilled") != null) ? ((Number) row.get("amountBilled")).doubleValue() : 0.0;
            Double descopedLinePriceInPoCurrency = (row.get("descopedLinePriceInPoCurrency") != null) ? ((Number) row.get("descopedLinePriceInPoCurrency")).doubleValue() : 0.0;
            Double newLinePriceInPoCurrency = (row.get("newLinePriceInPoCurrency") != null) ? ((Number) row.get("newLinePriceInPoCurrency")).doubleValue() : 0.0;

            Map<String, Object> groupedRow = groupedResults.get(poNumber);
            groupedRow.put("totalPoQtyNew", (Double) groupedRow.get("totalPoQtyNew") + poQtyNew);
            groupedRow.put("totalQuantityReceived", (Double) groupedRow.get("totalQuantityReceived") + quantityReceived);
            groupedRow.put("totalQuantityDueOld", (Double) groupedRow.get("totalQuantityDueOld") + quantityDueOld);
            groupedRow.put("totalQuantityDueNew", (Double) groupedRow.get("totalQuantityDueNew") + quantityDueNew);
            groupedRow.put("totalQuantityBilled", (Double) groupedRow.get("totalQuantityBilled") + quantityBilled);
            groupedRow.put("totalpoOrderQuantity", (Double) groupedRow.get("totalpoOrderQuantity") + poOrderQuantity);
            groupedRow.put("totalunitPriceInPoCurrency", (Double) groupedRow.get("totalunitPriceInPoCurrency") + unitPriceInPoCurrency);
            groupedRow.put("totalunitPriceInSAR", (Double) groupedRow.get("totalunitPriceInSAR") + unitPriceInSAR);
            groupedRow.put("totallinePriceInPoCurrency", (Double) groupedRow.get("totallinePriceInPoCurrency") + linePriceInPoCurrency);
            groupedRow.put("totallinePriceInSAR", (Double) groupedRow.get("totallinePriceInSAR") + linePriceInSAR);
            groupedRow.put("totalamountReceived", (Double) groupedRow.get("totalamountReceived") + amountReceived);
            groupedRow.put("totalamountDue", (Double) groupedRow.get("totalamountDue") + amountDue);
            groupedRow.put("totalamountDueNew", (Double) groupedRow.get("totalamountDueNew") + amountDueNew);
            groupedRow.put("totalamountBilled", (Double) groupedRow.get("totalamountBilled") + amountBilled);
            groupedRow.put("totalDescopedLinePriceInPoCurrency", (Double) groupedRow.get("totalDescopedLinePriceInPoCurrency") + descopedLinePriceInPoCurrency);
            groupedRow.put("totalNewLinePriceInPoCurrency", (Double) groupedRow.get("totalNewLinePriceInPoCurrency") + newLinePriceInPoCurrency);

        }

        return new ArrayList<>(groupedResults.values());
    }
    //==================GET POS PER VENDOR AND PO USING NEW FORMART  =====

    @PostMapping(value = "/reports/poUplPerSupplierAndPoNumber", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public List<Map<String, Object>> poUplPerSupplierAndPoNumber(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String supplierId = obj.get("supplierId").getAsString();
        String poID = obj.get("poId").getAsString();
        String sql = "SELECT * from combinedPurchaseOrderView";

        if (!supplierId.equalsIgnoreCase("0") && !poID.equalsIgnoreCase("0")) {
            sql += " WHERE poVendorNumber='" + supplierId + "' AND poNumber='" + poID + "'";
        } else if (!supplierId.equalsIgnoreCase("0") && poID.equalsIgnoreCase("0")) {
            sql += " WHERE poVendorNumber='" + supplierId + "'";
        } else if (supplierId.equalsIgnoreCase("0") && !poID.equalsIgnoreCase("0")) {
            sql += " WHERE poNumber='" + poID + "'";
        }

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return result;
    }

    //==================GET NEW UPLS CREATED  =====
    @PostMapping(value = "/reports/getAllCreatedUPLs", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public List<Map<String, Object>> getAllCreatedUPLs(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String poNumber = obj.get("poNumber").getAsString();
        String sql = "SELECT UPL.recordNo, UPL.recordDatetime, UPL.vendor, UPL.manufacturer, UPL.countryOfOrigin, UPL.projectName,  UPL.poType,  UPL.releaseNumber,  UPL.poNumber, UPL.poLineNumber,    UPL.uplLine,  UPL.poLineItemType, UPL.poLineItemCode, UPL.poLineDescription, UPL.uplLineItemType,UPL.uplLineItemCode,UPL.uplLineDescription,UPL.zainItemCategoryCode,    UPL.zainItemCategoryDescription, UPL.uplItemSerialized, UPL.activeOrPassive, UPL.uom, UPL.currency, UPL.poLineQuantity, UPL.poLineUnitPrice, UPL.uplLineQuantity,   UPL.uplLineUnitPrice,  UPL.substituteItemCode,  UPL.remarks,  UPL.dptApprover1, UPL.dptApprover2, UPL.dptApprover3, UPL.dptApprover4,UPL.regionalApprover,UPL.createdBy,\n"
                + "    UPL.createdByName, UPL.uplModifiedBy as updatedByName, UPL.uplModifiedDate AS updatedDatetime FROM tb_PurchaseOrderUPL UPL;";
        if (!poNumber.equalsIgnoreCase("0")) {
            sql = "SELECT UPL.recordNo, UPL.recordDatetime, UPL.vendor, UPL.manufacturer, UPL.countryOfOrigin, UPL.projectName,  UPL.poType,  UPL.releaseNumber,  UPL.poNumber, UPL.poLineNumber,    UPL.uplLine,  UPL.poLineItemType, UPL.poLineItemCode, UPL.poLineDescription, UPL.uplLineItemType,UPL.uplLineItemCode,UPL.uplLineDescription,UPL.zainItemCategoryCode,    UPL.zainItemCategoryDescription, UPL.uplItemSerialized, UPL.activeOrPassive, UPL.uom, UPL.currency, UPL.poLineQuantity, UPL.poLineUnitPrice, UPL.uplLineQuantity,   UPL.uplLineUnitPrice,  UPL.substituteItemCode,  UPL.remarks,  UPL.dptApprover1, UPL.dptApprover2, UPL.dptApprover3, UPL.dptApprover4,UPL.regionalApprover,UPL.createdBy,\n"
                    + "    UPL.createdByName, UPL.uplModifiedBy as updatedByName, UPL.uplModifiedDate AS updatedDatetime FROM tb_PurchaseOrderUPL UPL  WHERE UPL.poNumber='" + poNumber + "'";
        }
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return result;
    }

    //==================GET ALL CREATED ACCEPTANCE PER SUPPLIER  =====
    @PostMapping(value = "/reports/getdccdata", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public List<Map<String, Object>> getdccdata(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String supplierId = obj.get("supplierId").getAsString();
        String sql = "SELECT * FROM ALM_ZAIN_KSA.dccPOCombinedView";
        if (!supplierId.equalsIgnoreCase("0")) {
            sql = "SELECT * FROM ALM_ZAIN_KSA.dccPOCombinedView where supplierid='" + supplierId + "'";
        }
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return result;
    }

    //==================GET ALL CREATED ACCEPTANCE PER SUPPLIER AND RECORD NO   =====
    @PostMapping(value = "/reports/getdccperrecordNo", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public List<Map<String, Object>> getdccperrecordNo(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String supplierId = obj.get("supplierId").getAsString();
        Integer recordNo = obj.get("recordNo").getAsInt();
        String sql = "SELECT * FROM ALM_ZAIN_KSA.dccPOCombinedView";
        if (!supplierId.equalsIgnoreCase("0")) {
            sql = "SELECT * FROM ALM_ZAIN_KSA.dccPOCombinedView where supplierid='" + supplierId + "' and dccRecordNo  = '" + recordNo + "' ";
        }
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return result;
    }

    //==================GET POS PER VENDOR AND PO =====
    @PostMapping(value = "/reports/poPerVendorandPoId", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public List<Map<String, Object>> getpopersupplierandpo(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String supplierId = obj.get("supplierId").getAsString();
        String poId = obj.get("poId").getAsString();
        String sql = "SELECT PO.poId, PO.poDate, PO.supplierId,PO.status,PO.modelNumber, PO.unitOfMeasure, PO.qtyPerSite, PO.totalNoOfSites, PO.newFACategory, PO.L1,PO.L2,PO.L3,PO.L4,PO.oldFACategory, PO.vendorName, PO.oldFACategory, PO.accDepreciationCode, PO.depreciationCode, PO.lifeYears, PO.vendorName, PO.vendorNumber, PO.dateInService, PO.currency, PO.projectNumber , LN.lineNumber, LN.itemCode, LN.orderQuantity,LN.unitPrice, LN.VAT, LN.UoM,LN.linePrice, PO.costCenter, PO.partNumber, UPL.uplLine, UPL.poLineItemDescription, UPL.quantity AS DeliveredQuantity, UPL.zainItemCategory, UPL.serialized, UPL.activePassive, PO.typeLookUpCode,PO.prNum,PO.pnSubAllow,PO.countryOfOrigin,PO.currencyCode,PO.subInventoryCode,PO.receiptRouting,PO.poClosureStatus,PO.chargeAccount,PO.serialControl,LN.unitPriceInSAR,LN.linePriceInPoCurrency,LN.linePriceInSAR,LN.poLineType,LN.itemType,LN.itemCategoryInventory,LN.categoryDescription,LN.itemCategoryFA,LN.FACategoryDescription,LN.itemCategoryPurchasing,LN.PurchasingCategoryDescription FROM tb_PO_HD PO LEFT JOIN tb_PO_LN LN ON PO.poId = LN.poId LEFT JOIN  tb_UPL UPL ON LN.poId = UPL.poId AND LN.lineNumber = UPL.poLine;";
        if (!supplierId.equalsIgnoreCase("0")) {
            sql = "SELECT PO.poId, PO.poDate, PO.supplierId,PO.status,PO.modelNumber, PO.unitOfMeasure, PO.qtyPerSite, PO.totalNoOfSites, PO.newFACategory, PO.L1,PO.L2,PO.L3,PO.L4,PO.oldFACategory, PO.vendorName, PO.oldFACategory, PO.accDepreciationCode, PO.depreciationCode, PO.lifeYears, PO.vendorName, PO.vendorNumber, PO.dateInService, PO.currency, PO.projectNumber , LN.lineNumber, LN.itemCode, LN.orderQuantity,LN.unitPrice, LN.VAT, LN.UoM,LN.linePrice, PO.costCenter, PO.partNumber, UPL.uplLine, UPL.poLineItemDescription, UPL.quantity AS DeliveredQuantity, UPL.zainItemCategory, UPL.serialized, UPL.activePassive, PO.typeLookUpCode,PO.prNum,PO.pnSubAllow,PO.countryOfOrigin,PO.currencyCode,PO.subInventoryCode,PO.receiptRouting,PO.poClosureStatus,PO.chargeAccount,PO.serialControl,LN.unitPriceInSAR,LN.linePriceInPoCurrency,LN.linePriceInSAR,LN.poLineType,LN.itemType,LN.itemCategoryInventory,LN.categoryDescription,LN.itemCategoryFA,LN.FACategoryDescription,LN.itemCategoryPurchasing,LN.PurchasingCategoryDescription FROM tb_PO_HD PO LEFT JOIN tb_PO_LN LN ON PO.poId = LN.poId LEFT JOIN  tb_UPL UPL ON LN.poId = UPL.poId AND LN.lineNumber = UPL.poLine WHERE PO.supplierid='" + supplierId + "' AND PO.poId='" + poId + "'";
        }
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return result;
    }

    //=======================GET ALL POs===
    @GetMapping(value = "/reports/getAllPos", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public List<Map<String, Object>> getAllPos() {
        String sql = "SELECT PO.poId, PO.poDate, PO.supplierId,PO.status,PO.modelNumber, PO.unitOfMeasure, PO.qtyPerSite, PO.totalNoOfSites, PO.newFACategory, PO.L1,PO.L2,PO.L3,PO.L4,PO.oldFACategory, PO.vendorName, PO.oldFACategory, PO.accDepreciationCode, PO.depreciationCode, PO.lifeYears, PO.vendorName, PO.vendorNumber, PO.dateInService, PO.currency, PO.projectNumber , LN.lineNumber, LN.itemCode, LN.orderQuantity,LN.unitPrice, LN.VAT, LN.UoM,LN.linePrice, PO.costCenter, PO.partNumber, UPL.uplLine, UPL.poLineItemDescription, UPL.quantity AS DeliveredQuantity, UPL.zainItemCategory, UPL.serialized, UPL.activePassive, PO.typeLookUpCode,PO.prNum,PO.pnSubAllow,PO.countryOfOrigin,PO.currencyCode,PO.subInventoryCode,PO.receiptRouting,PO.poClosureStatus,PO.chargeAccount,PO.serialControl,LN.unitPriceInSAR,LN.linePriceInPoCurrency,LN.linePriceInSAR,LN.poLineType,LN.itemType,LN.itemCategoryInventory,LN.categoryDescription,LN.itemCategoryFA,LN.FACategoryDescription,LN.itemCategoryPurchasing,LN.PurchasingCategoryDescription FROM tb_PO_HD PO LEFT JOIN tb_PO_LN LN ON PO.poId = LN.poId LEFT JOIN  tb_UPL UPL ON LN.poId = UPL.poId AND LN.lineNumber = UPL.poLine";

        // String sql = "SELECT PO.recordNo, PO.poId, PO.poDate, PO.supplierId,PO.status,PO.modelNumber, PO.unitOfMeasure, PO.qtyPerSite, PO.totalNoOfSites, PO.newFACategory, PO.L1,PO.L2,PO.L3,PO.L4,PO.oldFACategory, PO.vendorName, PO.oldFACategory, PO.accDepreciationCode, PO.depreciationCode, PO.lifeYears, PO.vendorName, PO.vendorNumber, PO.dateInService, PO.currency, PO.projectNumber , LN.lineNumber, LN.itemCode, LN.orderQuantity,LN.unitPrice, LN.VAT, LN.UoM,LN.linePrice, PO.costCenter, PO.partNumber,INV.serialNumber ,INV.manufacturerId, INV.acquisitionDate, INV.manufactureDate, INV.locationId, UPL.uplLine, UPL.poLineItemDescription, UPL.quantity AS DeliveredQuantity, UPL.zainItemCategory, UPL.activePassive FROM tb_PO_HD PO LEFT JOIN tb_PO_LN LN ON PO.poId = LN.poId LEFT JOIN tb_Inventory INV ON PO.poId = INV.PONumber LEFT JOIN tb_UPL UPL ON PO.poId = UPL.poId;";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return result;
    }

    @Autowired
    public ReportsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //  =========   GET APP UPLS PER VENDOR ========
    @PostMapping(value = "/reports/getUPLPerVendordata", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public List<Map<String, Object>> getUPLPerVendordata(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String supplierId = obj.get("supplierId").getAsString();
        String sql = "";
        if (!supplierId.equalsIgnoreCase("0")) {
            sql = "SELECT * FROM ALM_ZAIN_KSA.UPLPerVendor where supplierId='" + supplierId + "'";
        }
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return result;
    }

    @PostMapping(value = "/reports/getdccstatusdata")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public String getdccstatusdata(@RequestBody String req) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Gson gsondt = new Gson();
            helper.logToFile(genHeader("N/A", "GetDCCStatusData", "GetDCCStatusData") + "GetDCCStatusRequest " + req, "INFO");
            JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
            String supplierId = obj.get("supplierId").getAsString();
            List<DccPoCombinedView> dccpostatus = dccpocombinedviewrp.findBySupplierIdAndDccStatus(supplierId, "inprocess");
            if (!dccpostatus.isEmpty()) {
                return (gsondt.toJson(dccpostatus));
            } else {
                return ("No DCC Status Data found.");
            }
        } catch (JsonSyntaxException exc) {
            String err = exc.toString();
            helper.logToFile(genHeader("N/A", "GetDCCStatusData", "GetDCCStatusData") + "GetDCCStatusData error " + err, "INFO");
        }
        return null;
    }

    @PostMapping(value = "/reports/getupldata")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public String getupldata(@RequestBody String req) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Gson gsondt = new Gson();
            helper.logToFile(genHeader("N/A", "GetUPLData", "GetUPLData") + "GetUPLDataRequest " + req, "INFO");
            JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
            String poId = obj.get("poId").getAsString();
            List<upldata> upldata = uprepo.findByPoId(poId);
            if (!upldata.isEmpty()) {
                return (gsondt.toJson(upldata));
            } else {
                return ("No UPL Data found.");
            }
        } catch (JsonSyntaxException exc) {
            String err = exc.toString();
            helper.logToFile(genHeader("N/A", "GetUPLData", "GetUPLData") + "GetUPLData error " + err, "INFO");
        }
        return null;
    }

    @GetMapping(value = "/reports/getallupls")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public String getallupls() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Gson gsondt = new Gson();
            //  helper.logToFile(genHeader("N/A", "GetAllData", "GetAllData INFO"));
            // JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
            //String poId = obj.get("poId").getAsString();
            List<upldata> upldata = uprepo.findAll();
            if (!upldata.isEmpty()) {
                return (gsondt.toJson(upldata));
            } else {
                return ("No UPL Data found.");
            }
        } catch (Exception exc) {
            loggger.info("Exception " + exc.toString());
        }
        return null;
    }

}
