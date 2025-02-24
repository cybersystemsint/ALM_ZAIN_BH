package com.telkom.almBHZain.controller;

import com.telkom.almBHZain.repo.DccCombinedViewrepo;
import com.telkom.almBHZain.repo.poviewrepo;
import com.telkom.almBHZain.repo.dccpoviewrepo;
import com.telkom.almBHZain.repo.uplrepo;

import com.telkom.almBHZain.model.DccPoCombinedView;
import com.telkom.almBHZain.model.upldata;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.telkom.almBHZain.helper.helper;
import com.telkom.almBHZain.repo.tbChargeAccountRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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
    
    /////BARHAIN PO MODULE NEW END POINTS 
    
    @PostMapping(value = "/reports/getPurchaseOrders", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, Object> getPurchaseOrders(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String supplierId = obj.get("supplierId").getAsString();

        int page = obj.has("page") ? obj.get("page").getAsInt() : 1;
        int size = obj.has("size") ? obj.get("size").getAsInt() : 20000;

        page = Math.max(page, 0);
        size = Math.max(size, 0);

        String paginationSql = "";

        String countSql = "SELECT COUNT(*) FROM tb_Po PO";
        if (!supplierId.equalsIgnoreCase("0")) {
            countSql += " WHERE PO.vendorNumber='" + supplierId + "'";
        }
        int totalRecords = jdbcTemplate.queryForObject(countSql, Integer.class);

        if (page == 0 && size == 0) {
            paginationSql = "";
        } else if (page == 1 && size == 20000) {
            page = 0;
            size = totalRecords;
            page = Math.max(page, 1);
            size = Math.max(size, 1);
            int offset = (page - 1) * size;

            paginationSql = " LIMIT " + size + " OFFSET " + offset;

        } else {
            page = Math.max(page, 1);
            size = Math.max(size, 1);
            int offset = (page - 1) * size;
            paginationSql = " LIMIT " + size + " OFFSET " + offset;
        }

        String sql = "SELECT * FROM tb_Po PO";

        if (!supplierId.equalsIgnoreCase("0")) {
            sql += " WHERE PO.vendorNumber='" + supplierId + "'";
        }

        String finalSql = sql + paginationSql;

        List<Map<String, Object>> result = jdbcTemplate.queryForList(finalSql);

        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        response.put("totalRecords", totalRecords);
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalPages", (int) Math.ceil((double) totalRecords / size));

        return response;
    }
    
    
    

    @PostMapping(value = "/reports/acceptanceReport", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public List<Map<String, Object>> acceptanceReport(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String poNumber = obj.get("poNumber").getAsString();

        String sql = "SELECT * FROM `acceptanceReport` WHERE 1";

        if (!poNumber.equalsIgnoreCase("0")) {
            sql += " AND poNumber='" + poNumber + "'";
        }
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        AtomicInteger counter = new AtomicInteger(1);
        result.forEach(row -> row.put("recordNo", counter.getAndIncrement()));

        return result;
    }

    @PostMapping(value = "/reports/capitalizationReport", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public List<Map<String, Object>> capitalizationReport(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String poNumber = obj.get("poNumber").getAsString();

        jdbcTemplate.execute("SET SESSION sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))");

        String sql = "SELECT * FROM `capitalizationReport` WHERE 1";

        if (!poNumber.equalsIgnoreCase("0")) {
            sql += " AND poNumber='" + poNumber + "'";
        }

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);

        // Add an incremental column programmatically
        AtomicInteger counter = new AtomicInteger(1);
        result.forEach(row -> row.put("recordNo", counter.getAndIncrement()));

        return result;
    }

    ///GET ALL CREATED CHARGE ACCOUNTS
    @PostMapping(value = "/reports/getAllItemCodeSubstitutes", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, Object> getAllItemCodeSubstitutes(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        Integer recordNo = obj.get("recordNo").getAsInt();

        int page = obj.has("page") ? obj.get("page").getAsInt() : 1;
        int size = obj.has("size") ? obj.get("size").getAsInt() : 20000;

        page = Math.max(page, 0);
        size = Math.max(size, 0);

        String paginationSql = "";

        String countSql = "SELECT COUNT(*) FROM tb_ItemCodeSubstitute PO";
        if (recordNo != 0) {
            countSql += " WHERE recordNo = '" + recordNo + "'";
        }
        int totalRecords = jdbcTemplate.queryForObject(countSql, Integer.class);

        if (page == 0 && size == 0) {
            paginationSql = "";
        } else if (page == 1 && size == 20000) {
            page = 0;
            size = totalRecords;
            page = Math.max(page, 1);
            size = Math.max(size, 1);
            int offset = (page - 1) * size;

            paginationSql = " LIMIT " + size + " OFFSET " + offset;

        } else {
            page = Math.max(page, 1);
            size = Math.max(size, 1);
            int offset = (page - 1) * size;
            paginationSql = " LIMIT " + size + " OFFSET " + offset;
        }

        String sql = "SELECT recordNo, recordDateTime, itemCode, relatedItemCode, reciprocalFlag, createdBy, createdDatetime, updatedBy, updatedDateTime FROM tb_ItemCodeSubstitute";
        if (recordNo != 0) {
            sql += " WHERE recordNo = '" + recordNo + "'";
        }

        String finalSql = sql + paginationSql;

        List<Map<String, Object>> result = jdbcTemplate.queryForList(finalSql);

        // Create a response map to include pagination details
        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        response.put("totalRecords", totalRecords);
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalPages", (int) Math.ceil((double) totalRecords / size));

        return response;
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

    @PostMapping(value = "/reports/getAllPurchaseOrders", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, Object> getAllPurchaseOrders(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String supplierId = obj.get("supplierId").getAsString();

        int page = obj.has("page") ? obj.get("page").getAsInt() : 1;
        int size = obj.has("size") ? obj.get("size").getAsInt() : 20000;

        page = Math.max(page, 0);
        size = Math.max(size, 0);

        String paginationSql = "";

        String countSql = "SELECT COUNT(*) FROM tb_PurchaseOrder PO";
        if (!supplierId.equalsIgnoreCase("0")) {
            countSql += " WHERE PO.vendorNumber='" + supplierId + "'";
        }
        int totalRecords = jdbcTemplate.queryForObject(countSql, Integer.class);

        if (page == 0 && size == 0) {
            paginationSql = "";
        } else if (page == 1 && size == 20000) {
            page = 0;
            size = totalRecords;
            page = Math.max(page, 1);
            size = Math.max(size, 1);
            int offset = (page - 1) * size;

            paginationSql = " LIMIT " + size + " OFFSET " + offset;

        } else {
            page = Math.max(page, 1);
            size = Math.max(size, 1);
            int offset = (page - 1) * size;
            paginationSql = " LIMIT " + size + " OFFSET " + offset;
        }

        String sql = "SELECT PO.recordNo, PO.poNumber, PO.typeLookUpCode, PO.blanketTotalAmount, PO.releaseNum, PO.lineNumber, "
                + "PO.prNum, PO.projectName, PO.lineCancelFlag, PO.cancelReason, PO.itemPartNumber, PO.prSubAllow, "
                + "PO.countryOfOrigin, PO.poOrderQuantity, PO.poQtyNew, PO.quantityReceived, PO.quantityDueOld, PO.quantityDueNew, "
                + "PO.quantityBilled, PO.currencyCode, PO.unitPriceInPoCurrency, PO.unitPriceInSAR, PO.linePriceInPoCurrency, "
                + "PO.linePriceInSAR, PO.amountReceived, PO.amountDue, PO.amountDueNew, PO.amountBilled, PO.poLineDescription, "
                + "PO.organizationName, PO.organizationCode, PO.subInventoryCode, PO.receiptRouting, PO.authorisationStatus, "
                + "PO.poClosureStatus, PO.departmentName, PO.businessOwner, PO.poLineType, PO.acceptanceType, PO.costCenter, "
                + "PO.chargeAccount, PO.serialControl, PO.vendorSerialNumberYN, PO.itemType, PO.itemCategoryInventory, "
                + "PO.inventoryCategoryDescription, PO.itemCategoryFA, PO.FACategoryDescription, PO.itemCategoryPurchasing, "
                + "PO.PurchasingCategoryDescription, PO.vendorName, PO.vendorNumber, PO.approvedDate, PO.createdDate, "
                + "CASE WHEN `PO`.`lineCancelFlag` = 0 AND `PO`.`authorisationStatus` = 'APPROVED' AND `PO`.`poClosureStatus` = 'OPEN' "
                + "THEN 'YES' ELSE 'NO' END AS `canRaiseAcceptance`, PO.createdByName, PO.descopedLinePriceInPoCurrency, "
                + "PO.newLinePriceInPoCurrency FROM tb_PurchaseOrder PO";

        if (!supplierId.equalsIgnoreCase("0")) {
            sql += " WHERE PO.vendorNumber='" + supplierId + "'";
        }

        String finalSql = sql + paginationSql;

        List<Map<String, Object>> result = jdbcTemplate.queryForList(finalSql);

        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        response.put("totalRecords", totalRecords);
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalPages", (int) Math.ceil((double) totalRecords / size));

        return response;
    }

    //===========GET NESTED PO =====
    @PostMapping(value = "/reports/getNestedPurchaseOrders", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, Object> getNestedPurchaseOrders(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String supplierId = obj.get("supplierId").getAsString();
        String poID = obj.get("poNumber").getAsString();

        int page = obj.has("page") ? obj.get("page").getAsInt() : 1;
        int size = obj.has("size") ? obj.get("size").getAsInt() : 20000;

        // Validate page and size
        page = Math.max(page, 1);
        size = Math.max(size, 1);

        String conditionSql = "";

        if (!supplierId.equalsIgnoreCase("0") && !poID.equalsIgnoreCase("0")) {
            conditionSql += " WHERE PO.vendorNumber='" + supplierId + "' AND PO.poNumber='" + poID + "'";
        } else if (!supplierId.equalsIgnoreCase("0") && poID.equalsIgnoreCase("0")) {
            conditionSql += " WHERE PO.vendorNumber='" + supplierId + "'";
        } else if (supplierId.equalsIgnoreCase("0") && !poID.equalsIgnoreCase("0")) {
            conditionSql += " WHERE PO.poNumber='" + poID + "'";
        }

        // Step 1: Fetch unique POs
        String uniquePOsSql = "SELECT DISTINCT PO.poNumber FROM tb_PurchaseOrder PO " + conditionSql;
        List<String> uniquePONumbers = jdbcTemplate.queryForList(uniquePOsSql, String.class);

        // If page is 1 and size is 20000, return all unique POs
        if (page == 1 && size == 20000) {
            // No pagination needed, just fetch all unique POs
            // Fetch line items for all unique POs
            String lineItemsSql = "SELECT * FROM tb_PurchaseOrder PO WHERE PO.poNumber IN (" + String.join(",", uniquePONumbers.stream().map(po -> "'" + po + "'").collect(Collectors.toList())) + ")";
            Map<String, Object> params = new HashMap<>();
            //   params.put("poNumbers", uniquePONumbers);
            List<Map<String, Object>> lineItems = jdbcTemplate.queryForList(lineItemsSql);

            // Step 3: Group line items by PO number
            Map<String, Map<String, Object>> groupedResults = new LinkedHashMap<>();
            for (Map<String, Object> lineItem : lineItems) {
                String poNumber = (String) lineItem.get("poNumber");

                if (!groupedResults.containsKey(poNumber)) {
                    Map<String, Object> groupedRow = new LinkedHashMap<>(lineItem);
                    // Remove unnecessary fields
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

                    // Initialize totals
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
                poLineItem.put("recordNo", lineItem.get("recordNo"));
                poLineItem.put("poNumber", lineItem.get("poNumber"));
                poLineItem.put("lineNumber", lineItem.get("lineNumber"));
                poLineItem.put("itemPartNumber", lineItem.get("itemPartNumber"));
                poLineItem.put("countryOfOrigin", lineItem.get("countryOfOrigin"));
                poLineItem.put("poOrderQuantity", (lineItem.get("poOrderQuantity")));
                poLineItem.put("poQtyNew", (lineItem.get("poQtyNew")));

                poLineItem.put("quantityReceived", (lineItem.get("quantityReceived")));
                poLineItem.put("quantityDueOld", (lineItem.get("quantityDueOld")));
                poLineItem.put("quantityDueNew", (lineItem.get("quantityDueNew")));
                poLineItem.put("quantityBilled", (lineItem.get("quantityBilled")));
                poLineItem.put("unitPriceInPoCurrency", lineItem.get("unitPriceInPoCurrency"));
                poLineItem.put("unitPriceInSAR", lineItem.get("unitPriceInSAR"));
                poLineItem.put("linePriceInPoCurrency", lineItem.get("linePriceInPoCurrency"));
                poLineItem.put("linePriceInSAR", lineItem.get("linePriceInSAR"));
                poLineItem.put("amountReceived", lineItem.get("amountReceived"));
                poLineItem.put("amountDue", lineItem.get("amountDue"));
                poLineItem.put("amountDueNew", lineItem.get("amountDueNew"));
                poLineItem.put("amountBilled", lineItem.get("amountBilled"));
                poLineItem.put("poLineDescription", lineItem.get("poLineDescription"));
                poLineItem.put("vendorSerialNumberYN", lineItem.get("vendorSerialNumberYN"));
                poLineItem.put("itemCategoryInventory", lineItem.get("itemCategoryInventory"));
                poLineItem.put("inventoryCategoryDescription", lineItem.get("inventoryCategoryDescription"));
                poLineItem.put("itemCategoryFA", lineItem.get("itemCategoryFA"));
                poLineItem.put("FACategoryDescription", lineItem.get("FACategoryDescription"));
                poLineItem.put("descopedLinePriceInPoCurrency", lineItem.get("descopedLinePriceInPoCurrency"));
                poLineItem.put("newLinePriceInPoCurrency", lineItem.get("newLinePriceInPoCurrency"));

                // Add the line item to the POlineItems list
                ((List<Map<String, Object>>) groupedResults.get(poNumber).get("POlineItems")).add(poLineItem);

                // Update totals
                Double poOrderQuantity = (lineItem.get("poOrderQuantity") != null) ? ((Number) lineItem.get("poOrderQuantity")).doubleValue() : 0.0;
                Double poQtyNew = (lineItem.get("poQtyNew") != null) ? ((Number) lineItem.get("poQtyNew")).doubleValue() : 0.0;
                Double quantityReceived = (lineItem.get("quantityReceived") != null) ? ((Number) lineItem.get("quantityReceived")).doubleValue() : 0.0;
                Double quantityDueOld = (lineItem.get("quantityDueOld") != null) ? ((Number) lineItem.get("quantityDueOld")).doubleValue() : 0.0;
                Double quantityDueNew = (lineItem.get("quantityDueNew") != null) ? ((Number) lineItem.get("quantityDueNew")).doubleValue() : 0.0;
                Double quantityBilled = (lineItem.get("quantityBilled") != null) ? ((Number) lineItem.get("quantityBilled")).doubleValue() : 0.0;
                Double unitPriceInPoCurrency = (lineItem.get("unitPriceInPoCurrency") != null) ? ((Number) lineItem.get("unitPriceInPoCurrency")).doubleValue() : 0.0;
                Double unitPriceInSAR = (lineItem.get("unitPriceInSAR") != null) ? ((Number) lineItem.get("unitPriceInSAR")).doubleValue() : 0.0;
                Double linePriceInPoCurrency = (lineItem.get("linePriceInPoCurrency") != null) ? ((Number) lineItem.get("linePriceInPoCurrency")).doubleValue() : 0.0;
                Double linePriceInSAR = (lineItem.get("linePriceInSAR") != null) ? ((Number) lineItem.get("linePriceInSAR")).doubleValue() : 0.0;
                Double amountReceived = (lineItem.get("amountReceived") != null) ? ((Number) lineItem.get("amountReceived")).doubleValue() : 0.0;
                Double amountDue = (lineItem.get("amountDue") != null) ? ((Number) lineItem.get("amountDue")).doubleValue() : 0.0;
                Double amountDueNew = (lineItem.get("amountDueNew") != null) ? ((Number) lineItem.get("amountDueNew")).doubleValue() : 0.0;
                Double amountBilled = (lineItem.get("amountBilled") != null) ? ((Number) lineItem.get("amountBilled")).doubleValue() : 0.0;
                Double descopedLinePriceInPoCurrency = (lineItem.get("descopedLinePriceInPoCurrency") != null) ? ((Number) lineItem.get("descopedLinePriceInPoCurrency")).doubleValue() : 0.0;
                Double newLinePriceInPoCurrency = (lineItem.get("newLinePriceInPoCurrency") != null) ? ((Number) lineItem.get("newLinePriceInPoCurrency")).doubleValue() : 0.0;

                Map<String, Object> groupedRow = groupedResults.get(poNumber);
                groupedRow.put("totalPoQtyNew", ((Double) groupedRow.get("totalPoQtyNew") + poQtyNew));
                groupedRow.put("totalQuantityReceived", ((Double) groupedRow.get("totalQuantityReceived") + quantityReceived));
                groupedRow.put("totalQuantityDueOld", ((Double) groupedRow.get("totalQuantityDueOld") + quantityDueOld));
                groupedRow.put("totalQuantityDueNew", ((Double) groupedRow.get("totalQuantityDueNew") + quantityDueNew));
                groupedRow.put("totalQuantityBilled", ((Double) groupedRow.get("totalQuantityBilled") + quantityBilled));
                groupedRow.put("totalpoOrderQuantity", ((Double) groupedRow.get("totalpoOrderQuantity") + poOrderQuantity));
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

            // Prepare the response
            Map<String, Object> response = new HashMap<>();
            response.put("currentPage", page);
            response.put("pageSize", uniquePONumbers.size());
            response.put("totalRecords", uniquePONumbers.size());
            response.put("totalPages", 1); // Since we are returning all records
            response.put("data", new ArrayList<>(groupedResults.values()));

            return response;
        }

        // Step 1: Fetch unique POs with pagination
        String uniquePOsSql2 = "SELECT DISTINCT PO.poNumber FROM tb_PurchaseOrder PO " + conditionSql + " LIMIT " + size + " OFFSET " + (page - 1) * size;
        List<String> uniquePONumbers2 = jdbcTemplate.queryForList(uniquePOsSql2, String.class);

        // Step 2: Fetch line items for the unique POs
        if (uniquePONumbers2.isEmpty()) {
            // If no unique POs found, return an empty response
            Map<String, Object> response = new HashMap<>();
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalRecords", 0);
            response.put("totalPages", 0);
            response.put("data", new ArrayList<>());
            return response;
        }

        String lineItemsSql = "SELECT * FROM tb_PurchaseOrder PO WHERE PO.poNumber IN (" + String.join(",", uniquePONumbers2.stream().map(po -> "'" + po + "'").collect(Collectors.toList())) + ")";
        List<Map<String, Object>> lineItems = jdbcTemplate.queryForList(lineItemsSql);

        Map<String, Map<String, Object>> paginatedGroupedResults = new LinkedHashMap<>();
        for (Map<String, Object> lineItem : lineItems) {
            String poNumber = (String) lineItem.get("poNumber");

            if (!paginatedGroupedResults.containsKey(poNumber)) {
                Map<String, Object> groupedRow = new LinkedHashMap<>(lineItem);
                // Remove unnecessary fields
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

                // Initialize totals
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
                paginatedGroupedResults.put(poNumber, groupedRow);
            }

            Map<String, Object> poLineItem = new LinkedHashMap<>();
            poLineItem.put("recordNo", lineItem.get("recordNo"));
            poLineItem.put("poNumber", lineItem.get("poNumber"));
            poLineItem.put("lineNumber", lineItem.get("lineNumber"));
            poLineItem.put("itemPartNumber", lineItem.get("itemPartNumber"));
            poLineItem.put("countryOfOrigin", lineItem.get("countryOfOrigin"));
            poLineItem.put("poOrderQuantity", (lineItem.get("poOrderQuantity")));
            poLineItem.put("poQtyNew", (lineItem.get("poQtyNew")));
            poLineItem.put("quantityReceived", (lineItem.get("quantityReceived")));
            poLineItem.put("quantityDueOld", (lineItem.get("quantityDueOld")));
            poLineItem.put("quantityDueNew", (lineItem.get("quantityDueNew")));
            poLineItem.put("quantityBilled", (lineItem.get("quantityBilled")));
            poLineItem.put("unitPriceInPoCurrency", lineItem.get("unitPriceInPoCurrency"));
            poLineItem.put("unitPriceInSAR", lineItem.get("unitPriceInSAR"));
            poLineItem.put("linePriceInPoCurrency", lineItem.get("linePriceInPoCurrency"));
            poLineItem.put("linePriceInSAR", lineItem.get("linePriceInSAR"));
            poLineItem.put("amountReceived", lineItem.get("amountReceived"));
            poLineItem.put("amountDue", lineItem.get("amountDue"));
            poLineItem.put("amountDueNew", lineItem.get("amountDueNew"));
            poLineItem.put("amountBilled", lineItem.get("amountBilled"));
            poLineItem.put("poLineDescription", lineItem.get("poLineDescription"));
            poLineItem.put("vendorSerialNumberYN", lineItem.get("vendorSerialNumberYN"));
            poLineItem.put("itemCategoryInventory", lineItem.get("itemCategoryInventory"));
            poLineItem.put("inventoryCategoryDescription", lineItem.get("inventoryCategoryDescription"));
            poLineItem.put("itemCategoryFA", lineItem.get("itemCategoryFA"));
            poLineItem.put("FACategoryDescription", lineItem.get("FACategoryDescription"));
            poLineItem.put("descopedLinePriceInPoCurrency", lineItem.get("descopedLinePriceInPoCurrency"));
            poLineItem.put("newLinePriceInPoCurrency", lineItem.get("newLinePriceInPoCurrency"));

            // Add the line item to the POlineItems list
            ((List<Map<String, Object>>) paginatedGroupedResults.get(poNumber).get("POlineItems")).add(poLineItem);

            // Update totals
            Double poOrderQuantity = (lineItem.get("poOrderQuantity") != null) ? ((Number) lineItem.get("poOrderQuantity")).doubleValue() : 0.0;
            Double poQtyNew = (lineItem.get("poQtyNew") != null) ? ((Number) lineItem.get("poQtyNew")).doubleValue() : 0.0;
            Double quantityReceived = (lineItem.get("quantityReceived") != null) ? ((Number) lineItem.get("quantityReceived")).doubleValue() : 0.0;
            Double quantityDueOld = (lineItem.get("quantityDueOld") != null) ? ((Number) lineItem.get("quantityDueOld")).doubleValue() : 0.0;
            Double quantityDueNew = (lineItem.get("quantityDueNew") != null) ? ((Number) lineItem.get("quantityDueNew")).doubleValue() : 0.0;
            Double quantityBilled = (lineItem.get("quantityBilled") != null) ? ((Number) lineItem.get("quantityBilled")).doubleValue() : 0.0;
            Double unitPriceInPoCurrency = (lineItem.get("unitPriceInPoCurrency") != null) ? ((Number) lineItem.get("unitPriceInPoCurrency")).doubleValue() : 0.0;
            Double unitPriceInSAR = (lineItem.get("unitPriceInSAR") != null) ? ((Number) lineItem.get("unitPriceInSAR")).doubleValue() : 0.0;
            Double linePriceInPoCurrency = (lineItem.get("linePriceInPoCurrency") != null) ? ((Number) lineItem.get("linePriceInPoCurrency")).doubleValue() : 0.0;
            Double linePriceInSAR = (lineItem.get("linePriceInSAR") != null) ? ((Number) lineItem.get("linePriceInSAR")).doubleValue() : 0.0;
            Double amountReceived = (lineItem.get("amountReceived") != null) ? ((Number) lineItem.get("amountReceived")).doubleValue() : 0.0;
            Double amountDue = (lineItem.get("amountDue") != null) ? ((Number) lineItem.get("amountDue")).doubleValue() : 0.0;
            Double amountDueNew = (lineItem.get("amountDueNew") != null) ? ((Number) lineItem.get("amountDueNew")).doubleValue() : 0.0;
            Double amountBilled = (lineItem.get("amountBilled") != null) ? ((Number) lineItem.get("amountBilled")).doubleValue() : 0.0;
            Double descopedLinePriceInPoCurrency = (lineItem.get("descopedLinePriceInPoCurrency") != null) ? ((Number) lineItem.get("descopedLinePriceInPoCurrency")).doubleValue() : 0.0;
            Double newLinePriceInPoCurrency = (lineItem.get("newLinePriceInPoCurrency") != null) ? ((Number) lineItem.get("newLinePriceInPoCurrency")).doubleValue() : 0.0;

            Map<String, Object> groupedRow = paginatedGroupedResults.get(poNumber);
            groupedRow.put("totalPoQtyNew", ((Double) groupedRow.get("totalPoQtyNew") + poQtyNew));
            groupedRow.put("totalQuantityReceived", ((Double) groupedRow.get("totalQuantityReceived") + quantityReceived));
            groupedRow.put("totalQuantityDueOld", ((Double) groupedRow.get("totalQuantityDueOld") + quantityDueOld));
            groupedRow.put("totalQuantityDueNew", ((Double) groupedRow.get("totalQuantityDueNew") + quantityDueNew));
            groupedRow.put("totalQuantityBilled", ((Double) groupedRow.get("totalQuantityBilled") + quantityBilled));
            groupedRow.put("totalpoOrderQuantity", ((Double) groupedRow.get("totalpoOrderQuantity") + poOrderQuantity));
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

        // Prepare the response
        Map<String, Object> response = new HashMap<>();
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalRecords", uniquePONumbers.size());
        response.put("totalPages", (int) Math.ceil((double) uniquePONumbers.size() / size));
        response.put("data", new ArrayList<>(paginatedGroupedResults.values()));

        return response;
    }

    private Object getNullIfZero(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue() == 0.0 ? null : value;
        }
        return value;
    }
    //==================GET POS PER VENDOR AND PO USING NEW FORMART  =====

    @PostMapping(value = "/reports/poUplPerSupplierAndPoNumber", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    // public List<Map<String, Object>> poUplPerSupplierAndPoNumber(@RequestBody String req) {
    public Map<String, Object> poUplPerSupplierAndPoNumber(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String supplierId = obj.get("supplierId").getAsString();
        String poID = obj.get("poId").getAsString();

        int page = obj.has("page") ? obj.get("page").getAsInt() : 1;
        int size = obj.has("size") ? obj.get("size").getAsInt() : 20000;

        page = Math.max(page, 0);
        size = Math.max(size, 0);

        String paginationSql = "";

        String countSql = "SELECT COUNT(*) FROM combinedPurchaseOrderView ";
        if (!supplierId.equalsIgnoreCase("0") && !poID.equalsIgnoreCase("0")) {
            countSql += " WHERE poVendorNumber='" + supplierId + "' AND poNumber='" + poID + "'";
        } else if (!supplierId.equalsIgnoreCase("0") && poID.equalsIgnoreCase("0")) {
            countSql += " WHERE poVendorNumber='" + supplierId + "'";
        } else if (supplierId.equalsIgnoreCase("0") && !poID.equalsIgnoreCase("0")) {
            countSql += " WHERE poNumber='" + poID + "'";
        }
        int totalRecords = jdbcTemplate.queryForObject(countSql, Integer.class);

        if (page == 0 && size == 0) {
            paginationSql = "";
        } else if (page == 1 && size == 20000) {
            page = 0;
            size = totalRecords;
            page = Math.max(page, 1);
            size = Math.max(size, 1);
            int offset = (page - 1) * size;

            paginationSql = " LIMIT " + size + " OFFSET " + offset;
        } else {
            page = Math.max(page, 1);
            size = Math.max(size, 1);
            int offset = (page - 1) * size;
            paginationSql = " LIMIT " + size + " OFFSET " + offset;
        }

        String sql = "SELECT * from combinedPurchaseOrderView";

        if (!supplierId.equalsIgnoreCase("0") && !poID.equalsIgnoreCase("0")) {
            sql += " WHERE poVendorNumber='" + supplierId + "' AND poNumber='" + poID + "'";
        } else if (!supplierId.equalsIgnoreCase("0") && poID.equalsIgnoreCase("0")) {
            sql += " WHERE poVendorNumber='" + supplierId + "'";
        } else if (supplierId.equalsIgnoreCase("0") && !poID.equalsIgnoreCase("0")) {
            sql += " WHERE poNumber='" + poID + "'";
        }

        String finalSql = sql + paginationSql;

        List<Map<String, Object>> result = jdbcTemplate.queryForList(finalSql);

        // Create a response map to include pagination details
        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        response.put("totalRecords", totalRecords);
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalPages", (int) Math.ceil((double) totalRecords / size));

        return response;

    }

    //==================GET NEW UPLS CREATED  =====
    @PostMapping(value = "/reports/getAllCreatedUPLs", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, Object> getAllCreatedUPLs(@RequestBody String req) {
        //  public List<Map<String, Object>> getAllCreatedUPLs(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String poNumber = obj.get("poNumber").getAsString();

        int page = obj.has("page") ? obj.get("page").getAsInt() : 1;
        int size = obj.has("size") ? obj.get("size").getAsInt() : 20000;

        page = Math.max(page, 0);
        size = Math.max(size, 0);

        String paginationSql = "";

        String countSql = "SELECT COUNT(*) FROM tb_PurchaseOrderUPL PO";
        if (!poNumber.equalsIgnoreCase("0")) {
            countSql += " WHERE PO.poNumber='" + poNumber + "'";
        }
        int totalRecords = jdbcTemplate.queryForObject(countSql, Integer.class);

        if (page == 0 && size == 0) {
            paginationSql = "";
        } else if (page == 1 && size == 20000) {
            page = 0;
            size = totalRecords;
            page = Math.max(page, 1);
            size = Math.max(size, 1);
            int offset = (page - 1) * size;

            paginationSql = " LIMIT " + size + " OFFSET " + offset;
        } else {
            page = Math.max(page, 1);
            size = Math.max(size, 1);
            int offset = (page - 1) * size;
            paginationSql = " LIMIT " + size + " OFFSET " + offset;
        }
        String sql = "SELECT UPL.recordNo, UPL.recordDatetime, UPL.vendor, UPL.manufacturer, UPL.countryOfOrigin, UPL.projectName,  UPL.poType,  UPL.releaseNumber,  UPL.poNumber, UPL.poLineNumber,    UPL.uplLine,  UPL.poLineItemType, UPL.poLineItemCode, UPL.poLineDescription, UPL.uplLineItemType,UPL.uplLineItemCode,UPL.uplLineDescription,UPL.zainItemCategoryCode,    UPL.zainItemCategoryDescription, UPL.uplItemSerialized, UPL.activeOrPassive, UPL.uom, UPL.currency, UPL.poLineQuantity, UPL.poLineUnitPrice, UPL.uplLineQuantity,   UPL.uplLineUnitPrice,  UPL.substituteItemCode,  UPL.remarks,  UPL.dptApprover1, UPL.dptApprover2, UPL.dptApprover3, UPL.dptApprover4,UPL.regionalApprover,UPL.createdBy,\n"
                + "    UPL.createdByName, UPL.uplModifiedBy as updatedByName, UPL.uplModifiedDate AS updatedDatetime FROM tb_PurchaseOrderUPL UPL ";
        if (!poNumber.equalsIgnoreCase("0")) {
            sql = "SELECT UPL.recordNo, UPL.recordDatetime, UPL.vendor, UPL.manufacturer, UPL.countryOfOrigin, UPL.projectName,  UPL.poType,  UPL.releaseNumber,  UPL.poNumber, UPL.poLineNumber,    UPL.uplLine,  UPL.poLineItemType, UPL.poLineItemCode, UPL.poLineDescription, UPL.uplLineItemType,UPL.uplLineItemCode,UPL.uplLineDescription,UPL.zainItemCategoryCode,    UPL.zainItemCategoryDescription, UPL.uplItemSerialized, UPL.activeOrPassive, UPL.uom, UPL.currency, UPL.poLineQuantity, UPL.poLineUnitPrice, UPL.uplLineQuantity,   UPL.uplLineUnitPrice,  UPL.substituteItemCode,  UPL.remarks,  UPL.dptApprover1, UPL.dptApprover2, UPL.dptApprover3, UPL.dptApprover4,UPL.regionalApprover,UPL.createdBy,\n"
                    + "    UPL.createdByName, UPL.uplModifiedBy as updatedByName, UPL.uplModifiedDate AS updatedDatetime FROM tb_PurchaseOrderUPL UPL  WHERE UPL.poNumber='" + poNumber + "'";
        }

        String finalSql = sql + paginationSql;

        List<Map<String, Object>> result = jdbcTemplate.queryForList(finalSql);

        // Create a response map to include pagination details
        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        response.put("totalRecords", totalRecords);
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalPages", (int) Math.ceil((double) totalRecords / size));

        return response;

    }

    //==================GET ALL CREATED ACCEPTANCE PER SUPPLIER  =====
    @PostMapping(value = "/reports/getdccdata", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, Object> getdccdata(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String supplierId = obj.get("supplierId").getAsString();

        int page = obj.has("page") ? obj.get("page").getAsInt() : 1;
        int size = obj.has("size") ? obj.get("size").getAsInt() : 20000;

        page = Math.max(page, 0);
        size = Math.max(size, 0);

        String paginationSql = "";

        jdbcTemplate.execute("SET SESSION sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))");

        String countSql = "SELECT COUNT(*) FROM dccPOCombinedView PO";
        if (!supplierId.equalsIgnoreCase("0")) {
            countSql += " WHERE PO.supplierid='" + supplierId + "'";
        }
        int totalRecords = jdbcTemplate.queryForObject(countSql, Integer.class);

        if (page == 0 && size == 0) {
            paginationSql = "";
        } else if (page == 1 && size == 20000) {
            page = 0;
            size = totalRecords;
            page = Math.max(page, 1);
            size = Math.max(size, 1);
            int offset = (page - 1) * size;

            paginationSql = " LIMIT " + size + " OFFSET " + offset;
        } else {
            page = Math.max(page, 1);
            size = Math.max(size, 1);
            int offset = (page - 1) * size;
            paginationSql = " LIMIT " + size + " OFFSET " + offset;
        }
        String sql = "SELECT * FROM ALM_ZAIN_KSA.dccPOCombinedView";
        if (!supplierId.equalsIgnoreCase("0")) {
            sql = "SELECT * FROM ALM_ZAIN_KSA.dccPOCombinedView where supplierid='" + supplierId + "'";
        }

        String finalSql = sql + paginationSql;

        List<Map<String, Object>> result = jdbcTemplate.queryForList(finalSql);

        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        response.put("totalRecords", totalRecords);
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalPages", (int) Math.ceil((double) totalRecords / size));

        return response;
    }

    //==================GET ALL CREATED ACCEPTANCE PER SUPPLIER AND RECORD NO   =====
    @PostMapping(value = "/reports/getdccperrecordNo", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public List<Map<String, Object>> getdccperrecordNo(@RequestBody String req) {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String supplierId = obj.get("supplierId").getAsString();
        Integer recordNo = obj.get("recordNo").getAsInt();

        jdbcTemplate.execute("SET SESSION sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))");

        String sql = "SELECT * FROM ALM_ZAIN_KSA.dccPOCombinedView";
        if (!supplierId.equalsIgnoreCase("0")) {
            sql = "SELECT * FROM ALM_ZAIN_KSA.dccPOCombinedView where supplierid='" + supplierId + "' and dccRecordNo  = '" + recordNo + "' ";
        }
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return result;
    }

    @Autowired
    public ReportsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
