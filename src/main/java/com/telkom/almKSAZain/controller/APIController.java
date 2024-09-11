package com.telkom.almKSAZain.controller;

import com.telkom.almKSAZain.repo.DCCRepository;
import com.telkom.almKSAZain.repo.dccstatusrepo;
import com.telkom.almKSAZain.repo.DccLineRepo;
import com.telkom.almKSAZain.repo.uplrepo;
import com.telkom.almKSAZain.repo.polnrepo;
//import com.telkom.almKSAZain.repo.inventoryrepo;
import com.telkom.almKSAZain.repo.supplierrepo;
import com.telkom.almKSAZain.repo.pohdrepo;
import com.telkom.almKSAZain.model.pohddata;
import com.telkom.almKSAZain.model.tbPurchaseOrder;
import com.telkom.almKSAZain.model.DCCLineItem;
//import com.telkom.almKSAZain.model.inventorydata;
import com.telkom.almKSAZain.model.dccstatus;
import com.telkom.almKSAZain.model.supplierdata;
import com.telkom.almKSAZain.model.DCC;
import com.telkom.almKSAZain.model.polndata;
import com.telkom.almKSAZain.model.upldata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.telkom.almKSAZain.helper.helper;
import com.telkom.almKSAZain.model.FileRecord;
import com.telkom.almKSAZain.model.tb_Approval_Log;
import com.telkom.almKSAZain.model.tb_PurchaseOrderUPL;
import com.telkom.almKSAZain.repo.fileRecordRepo;
import com.telkom.almKSAZain.repo.tbApprovalLogRepo;
import com.telkom.almKSAZain.repo.tbArcApprovalRecordsRepo;
import com.telkom.almKSAZain.repo.tbPurchaseOrderRepo;
import com.telkom.almKSAZain.repo.tbPurchaseOrderUPLRepo;
import com.telkom.almKSAZain.model.tb_Arc_ApprovalRecords;
import com.telkom.almKSAZain.utlities.Httpcall;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.json.JSONException;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class APIController {

    private final org.apache.logging.log4j.Logger loggger = LogManager.getLogger(APIController.class);

    MasterDataController mstdt = new MasterDataController();

    @Autowired
    pohdrepo pohd_repo;

    @Autowired
    polnrepo poln_repo;

    @Autowired
    DCCRepository dccrepo;

    @Autowired
    DccLineRepo dcclnrepo;

    @Autowired
    uplrepo uprepo;

    @Autowired
    dccstatusrepo dccstatrepo;
    @Autowired
    fileRecordRepo fileRepo;

    @Autowired
    tbPurchaseOrderRepo PurchaseOrderRepo;

    @Autowired
    tbApprovalLogRepo approvalLogRepo;

    @Autowired
    tbPurchaseOrderUPLRepo purchaseOrderUPLRepo;

    @Autowired
    tbArcApprovalRecordsRepo arcApprovalRecordsRepo;

    Httpcall utils = new Httpcall();

    HashMap requestMap = new HashMap();

    String genHeader(String msisdn, String reqid, String Channel) {
        return " | " + reqid + " | " + Channel + " | " + msisdn + " | ";
    }

    private Map<String, String> response(String result, String msg) {
        HashMap<String, String> map = new HashMap<>();
        map.put("responseCode", result.equalsIgnoreCase("success") ? "0" : "1001");
        map.put("responseMessage", msg);
        return map;
    }

    int countsubrack = 0;
    int ccountbts = 0;
    int countAntenna = 0;
    String startsubrack = "";
    String endsubrack = "";
    String startbts = "";
    String endbts = "";
    String startantenna = "";
    String endantenna = "";
    String vendorfile = "";
    String domainfile = "";
    String subdomainfile = "";

    //=================================NEW PO END POINT FOR NEW PO FORMAT ====
    @PostMapping(value = "/createpo")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> createpo(@RequestBody String req) throws ParseException, ParseException, ParseException {
        String batchfilename = "";
        JSONArray jsonArrayresponse = new JSONArray();
        long recordNo = 0;
        String poId = "";
        String poDate = "";
        String supplierId = "";
        String termsAndConditions = "";
        String deliveryLocationId = "";
        String createdBy = "";
        String status = "";

        helper.logToFile(genHeader("N/A", "ErrorAddingPO_HD", "API") + "EINCOMING REQUEST"
                + req, "INFO");

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the pattern as per your date format
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String requesttime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            JSONArray jsonArray = new JSONArray(req);
            String responseinfo = "Failed to save or data";

            List<String> validationErrors = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                recordNo = Integer.parseInt(jsonObject.getString("recordNo"));

                tbPurchaseOrder spldt = PurchaseOrderRepo.findByRecordNo(recordNo);
                if (spldt != null) {

                    spldt.setPoNumber(jsonObject.getString("poNumber").trim());
                    spldt.setTypeLookUpCode(jsonObject.getString("typeLookUpCode").trim());
                    spldt.setBlanketTotalAmount(jsonObject.getDouble("blanketTotalAmount"));
                    spldt.setReleaseNum(jsonObject.getString("releaseNum").trim());
                    spldt.setLineNumber(jsonObject.getInt("lineNumber"));
                    spldt.setPrNum(jsonObject.getString("prNum").trim());
                    spldt.setProjectName(jsonObject.getString("projectName").trim());
                    spldt.setLineCancelFlag(jsonObject.getBoolean("lineCancelFlag"));
                    spldt.setCancelReason(jsonObject.getString("cancelReason").trim());
                    spldt.setItemPartNumber(jsonObject.getString("itemPartNumber").trim());
                    spldt.setPrSubAllow(jsonObject.getBoolean("prSubAllow"));
                    spldt.setCountryOfOrigin(jsonObject.getString("countryOfOrigin").trim());
                    spldt.setPoOrderQuantity(jsonObject.getInt("poOrderQuantity"));
                    spldt.setPoQtyNew(jsonObject.getInt("poQtyNew"));
                    spldt.setQuantityReceived(jsonObject.getInt("quantityReceived"));
                    spldt.setQuantityDueOld(jsonObject.getInt("quantityDueOld"));
                    spldt.setQuantityDueNew(jsonObject.getInt("quantityDueNew"));
                    spldt.setQuantityBilled(jsonObject.getInt("quantityBilled"));
                    spldt.setCurrencyCode(jsonObject.getString("currencyCode").trim());
                    spldt.setUnitPriceInPoCurrency(jsonObject.getDouble("unitPriceInPoCurrency"));
                    spldt.setUnitPriceInSAR(jsonObject.getDouble("unitPriceInSAR"));
                    spldt.setLinePriceInPoCurrency(jsonObject.getDouble("linePriceInPoCurrency"));
                    spldt.setLinePriceInSAR(jsonObject.getDouble("linePriceInSAR"));
                    spldt.setAmountReceived(jsonObject.getDouble("amountReceived"));
                    spldt.setAmountDue(jsonObject.getDouble("amountDue"));
                    spldt.setAmountDueNew(jsonObject.getDouble("amountDueNew"));
                    spldt.setAmountBilled(jsonObject.getDouble("amountBilled"));
                    spldt.setPoLineDescription(jsonObject.getString("poLineDescription").trim());
                    spldt.setOrganizationName(jsonObject.getString("organizationName").trim());
                    spldt.setOrganizationCode(jsonObject.getString("organizationCode").trim());
                    spldt.setSubInventoryCode(jsonObject.getString("subInventoryCode").trim());
                    spldt.setReceiptRouting(jsonObject.getString("receiptRouting").trim());
                    spldt.setAuthorisationStatus(jsonObject.getString("authorisationStatus").trim());
                    spldt.setPoClosureStatus(jsonObject.getString("poClosureStatus").trim());
                    spldt.setDepartmentName(jsonObject.getString("departmentName").trim());
                    spldt.setBusinessOwner(jsonObject.getString("businessOwner").trim());
                    spldt.setPoLineType(jsonObject.getString("poLineType").trim());
                    spldt.setAcceptanceType(jsonObject.getString("acceptanceType").trim());
                    spldt.setCostCenter(jsonObject.getString("costCenter").trim());
                    spldt.setSerialControl(jsonObject.getString("serialControl").trim());
                    spldt.setVendorSerialNumberYN(jsonObject.getString("vendorSerialNumberYN").trim());
                    spldt.setItemType(jsonObject.getString("itemType").trim());
                    spldt.setItemCategoryInventory(jsonObject.getString("itemCategoryInventory").trim());
                    spldt.setInventoryCategoryDescription(jsonObject.getString("inventoryCategoryDescription").trim());
                    spldt.setItemCategoryFA(jsonObject.getString("itemCategoryFA").trim());
                    spldt.setFACategoryDescription(jsonObject.getString("FACategoryDescription").trim());
                    spldt.setItemCategoryPurchasing(jsonObject.getString("itemCategoryPurchasing").trim());
                    spldt.setPurchasingCategoryDescription(jsonObject.getString("purchasingCategoryDescription").trim());
                    spldt.setVendorName(jsonObject.getString("vendorName").trim());
                    spldt.setVendorNumber(jsonObject.getString("vendorNumber").trim());
                    spldt.setCreatedBy(jsonObject.getInt("createdById"));
                    spldt.setCreatedByName(jsonObject.getString("createdByName").trim());
                    String dateApproved = jsonObject.getString("approvedDate");
                    String dateCreated = jsonObject.getString("createdDate");

                    try {
                        java.util.Date parsedDate = dateFormat.parse(dateApproved);
                        java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
                        java.util.Date newDate = dateFormat.parse(dateCreated);
                        java.sql.Date sqlcreatedDate = new java.sql.Date(newDate.getTime());
                        spldt.setApprovedDate(sqlDate);
                        spldt.setCreatedDate(sqlcreatedDate);
                    } catch (ParseException ex) {
                        java.util.logging.Logger.getLogger(APIController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    try {
                        PurchaseOrderRepo.save(spldt);
                        responseinfo = "Record Updated Success";
                    } catch (Exception excc) {

                        System.out.println(excc.getMessage());
                        helper.logToFile(genHeader("N/A", "ErrorAddingPO_HD", "API") + "ErrorAddingPO_HD PO_HD RecordNo "
                                + recordNo, "INFO");
                        responseinfo = excc.toString();
                    }

                } else {

                    tbPurchaseOrder topRecord = PurchaseOrderRepo.findTopByPoNumberAndLineNumber(jsonObject.getString("poNumber"), String.valueOf(jsonObject.getInt("lineNumber")));
                    String poNum = topRecord != null ? String.valueOf(topRecord.getPoNumber()) : "";

                    if (poNum.length() < 1) {
                        tbPurchaseOrder nwspldt = new tbPurchaseOrder();
                        nwspldt.setPoNumber(jsonObject.getString("poNumber").trim());
                        nwspldt.setTypeLookUpCode(jsonObject.getString("typeLookUpCode").trim());
                        nwspldt.setBlanketTotalAmount(jsonObject.getDouble("blanketTotalAmount"));
                        nwspldt.setReleaseNum(jsonObject.getString("releaseNum").trim());
                        nwspldt.setLineNumber(jsonObject.getInt("lineNumber"));
                        nwspldt.setPrNum(jsonObject.getString("prNum").trim());
                        nwspldt.setProjectName(jsonObject.getString("projectName").trim());
                        nwspldt.setLineCancelFlag(jsonObject.getBoolean("lineCancelFlag"));
                        nwspldt.setCancelReason(jsonObject.getString("cancelReason").trim());
                        nwspldt.setItemPartNumber(jsonObject.getString("itemPartNumber").trim());
                        nwspldt.setPrSubAllow(jsonObject.getBoolean("prSubAllow"));
                        nwspldt.setCountryOfOrigin(jsonObject.getString("countryOfOrigin").trim());
                        nwspldt.setPoOrderQuantity(jsonObject.getInt("poOrderQuantity"));
                        nwspldt.setPoQtyNew(jsonObject.getInt("poQtyNew"));
                        nwspldt.setQuantityReceived(jsonObject.getInt("quantityReceived"));
                        nwspldt.setQuantityDueOld(jsonObject.getInt("quantityDueOld"));
                        nwspldt.setQuantityDueNew(jsonObject.getInt("quantityDueNew"));
                        nwspldt.setQuantityBilled(jsonObject.getInt("quantityBilled"));
                        nwspldt.setCurrencyCode(jsonObject.getString("currencyCode").trim());
                        nwspldt.setUnitPriceInPoCurrency(jsonObject.getDouble("unitPriceInPoCurrency"));
                        nwspldt.setUnitPriceInSAR(jsonObject.getDouble("unitPriceInSAR"));
                        nwspldt.setLinePriceInPoCurrency(jsonObject.getDouble("linePriceInPoCurrency"));
                        nwspldt.setLinePriceInSAR(jsonObject.getDouble("linePriceInSAR"));
                        nwspldt.setAmountReceived(jsonObject.getDouble("amountReceived"));
                        nwspldt.setAmountDue(jsonObject.getDouble("amountDue"));
                        nwspldt.setAmountDueNew(jsonObject.getDouble("amountDueNew"));
                        nwspldt.setAmountBilled(jsonObject.getDouble("amountBilled"));
                        nwspldt.setPoLineDescription(jsonObject.getString("poLineDescription").trim());
                        nwspldt.setOrganizationName(jsonObject.getString("organizationName").trim());
                        nwspldt.setOrganizationCode(jsonObject.getString("organizationCode").trim());
                        nwspldt.setSubInventoryCode(jsonObject.getString("subInventoryCode").trim());
                        nwspldt.setReceiptRouting(jsonObject.getString("receiptRouting").trim());
                        nwspldt.setAuthorisationStatus(jsonObject.getString("authorisationStatus").trim());
                        nwspldt.setPoClosureStatus(jsonObject.getString("poClosureStatus").trim());
                        nwspldt.setDepartmentName(jsonObject.getString("departmentName").trim());
                        nwspldt.setBusinessOwner(jsonObject.getString("businessOwner").trim());
                        nwspldt.setPoLineType(jsonObject.getString("poLineType").trim());
                        nwspldt.setAcceptanceType(jsonObject.getString("acceptanceType").trim());
                        nwspldt.setCostCenter(jsonObject.getString("costCenter").trim());
                        nwspldt.setSerialControl(jsonObject.getString("serialControl").trim());
                        nwspldt.setVendorSerialNumberYN(jsonObject.getString("vendorSerialNumberYN").trim());
                        nwspldt.setItemType(jsonObject.getString("itemType").trim());
                        nwspldt.setItemCategoryInventory(jsonObject.getString("itemCategoryInventory").trim());
                        nwspldt.setInventoryCategoryDescription(jsonObject.getString("inventoryCategoryDescription").trim());
                        nwspldt.setItemCategoryFA(jsonObject.getString("itemCategoryFA").trim());
                        nwspldt.setFACategoryDescription(jsonObject.getString("FACategoryDescription").trim());
                        nwspldt.setItemCategoryPurchasing(jsonObject.getString("itemCategoryPurchasing").trim());
                        nwspldt.setPurchasingCategoryDescription(jsonObject.getString("purchasingCategoryDescription").trim());
                        nwspldt.setVendorName(jsonObject.getString("vendorName").trim());
                        nwspldt.setVendorNumber(jsonObject.getString("vendorNumber").trim());
                        nwspldt.setCreatedBy(jsonObject.getInt("createdById"));
                        nwspldt.setCreatedByName(jsonObject.getString("createdByName").trim());
                        String dateApproved = jsonObject.getString("approvedDate");
                        String dateCreated = jsonObject.getString("createdDate");

                        try {
                            java.util.Date parsedDate = dateFormat.parse(dateApproved);
                            java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
                            java.util.Date newDate = dateFormat.parse(dateCreated);
                            java.sql.Date sqlcreatedDate = new java.sql.Date(newDate.getTime());
                            nwspldt.setApprovedDate(sqlDate);
                            nwspldt.setCreatedDate(sqlcreatedDate);
                        } catch (ParseException ex) {
                            java.util.logging.Logger.getLogger(APIController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        try {
                            PurchaseOrderRepo.save(nwspldt);
                            responseinfo = "Record Created Success";
//                            tbPurchaseOrder topRecord = PurchaseOrderRepo.findTopByPoNumber(jsonObject.getString("poNumber"));
//                            String storedrecordId = topRecord != null ? String.valueOf(topRecord.getRecordNo()) : "";

//                        tb_Approval_Log newApprovalLog = new tb_Approval_Log();
//                        java.util.Date parsedDate = dateFormat.parse(now.toString());
//                        java.sql.Date newDate = new java.sql.Date(parsedDate.getTime());
//                        newApprovalLog.setRecordDatetime(newDate);
//                        newApprovalLog.setApprovalRecordId(Integer.parseInt(storedrecordId));
//                        newApprovalLog.setRecordType("PO");
//                        newApprovalLog.setPONumber(jsonObject.getString("poNumber").trim());
//                        newApprovalLog.setStatus("Pending");
//                        newApprovalLog.setRegion("");
//                        newApprovalLog.setCreatedBy(jsonObject.getInt("createdById"));
//                        newApprovalLog.setApprover(jsonObject.getString("businessOwner").trim());
//                        approvalLogRepo.save(newApprovalLog);
                        } catch (JSONException excc) {

                            System.out.println(excc.getMessage());
                            helper.logToFile(genHeader("N/A", "ErrorAddingPO_HD", "API") + "ErrorAddingPO_HD PO_HD RecordNo "
                                    + recordNo, "INFO");
                            responseinfo = excc.toString();
                        }
                    } else {
                        validationErrors.add(jsonObject.getString("poNumber") + " " + jsonObject.getString("lineNumber"));
                    }

                }

            }
            if (!responseinfo.contains("Success")) {
                batchfilename = getbatchfilename("FailedUpload");
                helper.logBatchFile(responseinfo, true, batchfilename);
                return response("Error", responseinfo);
            } else if (!validationErrors.isEmpty()) {
                batchfilename = getbatchfilename("FailedUpload");
                helper.logBatchFile(responseinfo, true, batchfilename);
                return response("Error", "Already Existing PO numbers and Line Items: " + String.join(", ", validationErrors));
            } else {
                return response("Success", "Complete");
            }
        } catch (NumberFormatException | JSONException exc) {
            net.minidev.json.JSONObject response = new net.minidev.json.JSONObject();

            return response("Error", exc.getMessage());
        }
    }

    //=================================NEW PO UPL END POINT FOR NEW UPL FORMAT ====
    @PostMapping(value = "/createpoupl")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> createpoupl(@RequestBody String req) throws ParseException, ParseException, ParseException {
        String batchfilename = "";
        JSONArray jsonArrayresponse = new JSONArray();
        long recordNo = 0;
        List<String> missingPoNumbers = new ArrayList<>();
        String poId = "";
        String poDate = "";
        String supplierId = "";
        String termsAndConditions = "";
        String deliveryLocationId = "";
        String createdBy = "";
        String status = "";
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the pattern as per your date format
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String requesttime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            JSONArray jsonArray = new JSONArray(req);
            String responseinfo = "Failed to save or data";
            //VALIDATE FIRST HERE 
            Set<String> validRegions = new HashSet<>(Arrays.asList("East", "West", "North", "South", "Central"));
            JSONArray jsonArrayValidate = new JSONArray(req);
            List<String> validationErrors = new ArrayList<>();

            // Validation loop
            for (int i = 0; i < jsonArrayValidate.length(); i++) {
                JSONObject jsonObject = jsonArrayValidate.getJSONObject(i);
                String region = jsonObject.getString("regionalApprover").trim();
                String dptApprover1 = jsonObject.optString("dptApprover1", "").trim();
                List<tb_PurchaseOrderUPL> validateUPLCreation = purchaseOrderUPLRepo.findByPoNumberAndPoLineNumberAndUplLine(jsonObject.getString("poNumber"), jsonObject.getString("poLineNumber"), jsonObject.getString("uplLine"));

                if (!validateUPLCreation.isEmpty()) {
                    validationErrors.add("UPL Having UPL line " + jsonObject.getString("uplLine") + " in record  " + (i + 1) + ": already exists: PO Nunber " + jsonObject.getString("poNumber"));
                }
                // Validate region
                if (!validRegions.contains(region)) {
                    validationErrors.add("Invalid region in record " + (i + 1) + ": " + region);
                }
                // Validate dptApprover1
                if (dptApprover1.isEmpty()) {
                    validationErrors.add("Missing Department Approver 1 in record " + (i + 1));
                }
            }

            // Check if there are any validation errors
            if (!validationErrors.isEmpty()) {
                return response("Error", "Validation failed: " + String.join("; ", validationErrors));
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                recordNo = Integer.parseInt(jsonObject.getString("recordNo"));
                List<tbPurchaseOrder> validatePoList = PurchaseOrderRepo.findByPoNumber(jsonObject.getString("poNumber"));
                if (!validatePoList.isEmpty()) {
                    tb_PurchaseOrderUPL spldt = purchaseOrderUPLRepo.findByRecordNo(recordNo);
                    if (spldt != null) {
                        java.util.Date parsedDate = dateFormat.parse(now.toString());
                        java.sql.Date newDate = new java.sql.Date(parsedDate.getTime());
                        spldt.setRecordDatetime(newDate);
                        spldt.setVendor(jsonObject.getString("vendor").trim());
                        spldt.setManufacturer(jsonObject.getString("manufacturer").trim());
                        spldt.setCountryOfOrigin(jsonObject.getString("countryOfOrigin").trim());
                        spldt.setProjectName(jsonObject.getString("projectName").trim());
                        spldt.setPoType(jsonObject.getString("poType").trim());
                        spldt.setReleaseNumber(jsonObject.getString("releaseNumber").trim());
                        spldt.setPoNumber(jsonObject.getString("poNumber").trim());
                        spldt.setPoLineNumber(jsonObject.getString("poLineNumber").trim());
                        spldt.setUplLine(jsonObject.getString("uplLine").trim());
                        spldt.setPoLineItemType(jsonObject.getString("poLineItemType").trim());
                        spldt.setPoLineItemCode(jsonObject.getString("poLineItemCode").trim());
                        spldt.setPoLineDescription(jsonObject.getString("poLineDescription").trim());
                        spldt.setUplLineItemType(jsonObject.getString("uplLineItemType").trim());
                        spldt.setUplLineItemCode(jsonObject.getString("uplLineItemCode").trim());
                        spldt.setUplLineDescription(jsonObject.getString("uplLineDescription").trim());
                        spldt.setZainItemCategoryCode(jsonObject.getString("zainItemCategoryCode").trim());
                        spldt.setZainItemCategoryDescription(jsonObject.getString("zainItemCategoryDescription").trim());
                        spldt.setUplItemSerialized(jsonObject.getString("uplItemSerialized").trim());
                        spldt.setActiveOrPassive(jsonObject.getString("activeOrPassive").trim());
                        spldt.setUom(jsonObject.getString("uom").trim());
                        spldt.setCurrency(jsonObject.getString("currency").trim());
                        spldt.setPoLineQuantity(jsonObject.getInt("poLineQuantity"));
                        spldt.setPoLineUnitPrice(jsonObject.getDouble("poLineUnitPrice"));
                        spldt.setUplLineQuantity(jsonObject.getInt("uplLineQuantity"));
                        spldt.setUplLineUnitPrice(jsonObject.getDouble("uplLineUnitPrice"));
                        spldt.setSubstituteItemCode(jsonObject.getString("substituteItemCode"));
                        spldt.setRemarks(jsonObject.getString("remarks").trim());
                        spldt.setDptApprover1(jsonObject.getString("dptApprover1").trim());
                        spldt.setDptApprover2(jsonObject.getString("dptApprover2").trim());
                        spldt.setDptApprover3(jsonObject.getString("dptApprover3").trim());
                        spldt.setDptApprover4(jsonObject.getString("dptApprover4").trim());
                        spldt.setRegionalApprover(jsonObject.getString("regionalApprover").trim());
                        spldt.setCreatedBy(jsonObject.getInt("createdById"));
                        spldt.setCreatedByName(jsonObject.getString("createdByName").trim());
                        try {
                            purchaseOrderUPLRepo.save(spldt);
                            responseinfo = "Record Updated Success";
                        } catch (Exception excc) {

                            System.out.println(excc.getMessage());
                            helper.logToFile(genHeader("N/A", "ErrorAddingPO_HD", "API") + "ErrorAddingPO_HD PO_HD RecordNo "
                                    + recordNo, "INFO");
                            responseinfo = excc.toString();
                        }
                    } else {
                        tb_PurchaseOrderUPL nwspldt = new tb_PurchaseOrderUPL();
                        java.util.Date parsedDate = dateFormat.parse(now.toString());
                        java.sql.Date newDate = new java.sql.Date(parsedDate.getTime());
                        nwspldt.setRecordDatetime(newDate);
                        nwspldt.setVendor(jsonObject.getString("vendor").trim());
                        nwspldt.setManufacturer(jsonObject.getString("manufacturer").trim());
                        nwspldt.setCountryOfOrigin(jsonObject.getString("countryOfOrigin").trim());
                        nwspldt.setProjectName(jsonObject.getString("projectName").trim());
                        nwspldt.setPoType(jsonObject.getString("poType").trim());
                        nwspldt.setReleaseNumber(jsonObject.getString("releaseNumber").trim());
                        nwspldt.setPoNumber(jsonObject.getString("poNumber").trim());
                        nwspldt.setPoLineNumber(jsonObject.getString("poLineNumber").trim());
                        nwspldt.setUplLine(jsonObject.getString("uplLine").trim());
                        nwspldt.setPoLineItemType(jsonObject.getString("poLineItemType").trim());
                        nwspldt.setPoLineItemCode(jsonObject.getString("poLineItemCode").trim());
                        nwspldt.setPoLineDescription(jsonObject.getString("poLineDescription").trim());
                        nwspldt.setUplLineItemType(jsonObject.getString("uplLineItemType").trim());
                        nwspldt.setUplLineItemCode(jsonObject.getString("uplLineItemCode").trim());
                        nwspldt.setUplLineDescription(jsonObject.getString("uplLineDescription").trim());
                        nwspldt.setZainItemCategoryCode(jsonObject.getString("zainItemCategoryCode").trim());
                        nwspldt.setZainItemCategoryDescription(jsonObject.getString("zainItemCategoryDescription").trim());
                        nwspldt.setUplItemSerialized(jsonObject.getString("uplItemSerialized").trim());
                        nwspldt.setActiveOrPassive(jsonObject.getString("activeOrPassive").trim());
                        nwspldt.setUom(jsonObject.getString("uom").trim());
                        nwspldt.setCurrency(jsonObject.getString("currency").trim());
                        nwspldt.setPoLineQuantity(jsonObject.getInt("poLineQuantity"));
                        nwspldt.setPoLineUnitPrice(jsonObject.getDouble("poLineUnitPrice"));
                        nwspldt.setUplLineQuantity(jsonObject.getInt("uplLineQuantity"));
                        nwspldt.setUplLineUnitPrice(jsonObject.getDouble("uplLineUnitPrice"));
                        nwspldt.setSubstituteItemCode(jsonObject.getString("substituteItemCode").trim());
                        nwspldt.setRemarks(jsonObject.getString("remarks").trim());
                        nwspldt.setDptApprover1(jsonObject.getString("dptApprover1").trim());
                        nwspldt.setDptApprover2(jsonObject.getString("dptApprover2").trim());
                        nwspldt.setDptApprover3(jsonObject.getString("dptApprover3").trim());
                        nwspldt.setDptApprover4(jsonObject.getString("dptApprover4").trim());
                        nwspldt.setRegionalApprover(jsonObject.getString("regionalApprover").trim());
                        nwspldt.setCreatedBy(jsonObject.getInt("createdById"));
                        nwspldt.setCreatedByName(jsonObject.getString("createdByName").trim());
                        try {
                            purchaseOrderUPLRepo.save(nwspldt);
                            responseinfo = "Record Created Success";
                        } catch (JSONException excc) {

                            System.out.println(excc.getMessage());
                            helper.logToFile(genHeader("N/A", "ErrorAddingPO_HD", "API") + "ErrorAddingPO_HD PO_HD RecordNo "
                                    + recordNo, "INFO");
                            responseinfo = excc.toString();
                        }

                    }
                } else {
                    String missingPoNumber = jsonObject.getString("poNumber");
                    missingPoNumbers.add(missingPoNumber);
                    System.out.println("PO NUMBER DOEST EXISTS " + jsonObject.getString("poNumber"));
                }
            }
            if (!missingPoNumbers.isEmpty()) {
                batchfilename = getbatchfilename("FailedUpload");
                helper.logBatchFile(responseinfo, true, batchfilename);
                return response("Error", "Missing PO Numbers: " + String.join(", ", missingPoNumbers));
            } else if (!responseinfo.contains("Success")) {
                batchfilename = getbatchfilename("FailedUpload");
                helper.logBatchFile(responseinfo, true, batchfilename);
                return response("Error", responseinfo);
            } else {
                return response("Success", "Complete");
            }
        } catch (NumberFormatException | JSONException exc) {
            return response("Error", exc.getMessage());
        }
    }

    //======================================PO MANAGEMENT  ================= 
    @PostMapping(value = "/postpohdln")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> postpohd(@RequestBody String req) throws ParseException, ParseException, ParseException {
        String batchfilename = "";//getbatchfilename("POHDLN_BATCHUPLOAD");
//        helper.logBatchFile(req, true, batchfilename);
//        helper.logToFile(genHeader("N/A", "POHDLN_BATCHUPLOAD", "POHDLN_BATCHUPLOAD") + "POHDLN_BATCHUPLOAD file name /home/app/logs/ALM/BatchFiles/" + batchfilename, "INFO");
        JSONArray jsonArrayresponse = new JSONArray();
        long recordNo = 0;
        String poId = "";
        String poDate = "";
        String supplierId = "";
        String termsAndConditions = "";
        String deliveryLocationId = "";
        String createdBy = "";
        String status = "";

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the pattern as per your date format

            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String requesttime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            JSONArray jsonArray = new JSONArray(req);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                recordNo = Integer.parseInt(jsonObject.getString("recordNo"));
                poId = jsonObject.getString("poId");
                poDate = jsonObject.getString("poDate");
                supplierId = jsonObject.getString("supplierId");
                termsAndConditions = jsonObject.getString("termsAndConditions");
                deliveryLocationId = jsonObject.getString("deliveryLocationId");
                createdBy = jsonObject.getString("createdBy");
                status = jsonObject.getString("status");

                JSONArray po_line_data = jsonObject.getJSONArray("polineItems");
                String jsresp = "";
                String addporesp = AddEditPOHD(recordNo, helper.getKenyaDateTimeString(), poId, poDate, supplierId, termsAndConditions,
                        deliveryLocationId, createdBy, status, jsonObject);

                if (addporesp.contains("Success")) {
                    //check the length of the Array first, can be 0 or more 
                    if (po_line_data.length() > 0) {
                        String response = postpoln(po_line_data.toString());
                    }

                } else {
                    net.minidev.json.JSONObject responsedata = new net.minidev.json.JSONObject();
                    responsedata.put("recordNo", recordNo);
                    responsedata.put("poId", poId);
                    responsedata.put("DBresponse", jsresp);
                    jsonArrayresponse.put(responsedata.toJSONString());
                }
            }
            if (jsonArrayresponse.length() > 0) {
                batchfilename = getbatchfilename("FailedUpload");
                helper.logBatchFile(jsonArrayresponse.toString(), true, batchfilename);
                return response("Error", jsonArrayresponse.toString());
            } else {
                return response("Success", "Complete");
            }
        } catch (NumberFormatException | JSONException exc) {
            net.minidev.json.JSONObject response = new net.minidev.json.JSONObject();
            response.put("poId", poId);
            response.put("supplierId", supplierId);
            return response("Error", jsonArrayresponse.toString());
        }
    }

    //===============DELETE PO =======
    @PostMapping(value = "/deletePo")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> deletePO(@RequestBody String req) throws ParseException, ParseException, ParseException {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        String supplierId = obj.get("poId").getAsString();
        pohddata spldt = pohd_repo.findByPoId(supplierId);
        HashMap<String, String> response = new HashMap<>();

        if (spldt != null) {

            spldt.setStatus("deleted");

            try {
                pohd_repo.save(spldt);
                response.put("responseCode", "0");
                response.put("responseDesc", "Record Deleted Success");
            } catch (Exception ex) {

                response.put("responseCode", "1");
                response.put("responseDesc", "An error occured while processing your request");
                response.put("error", ex.getMessage());

            }

        }
        return response;

    }

    private String getformatted(String datetoconvert, String inputformat, String outputformat) {
        String formdate = null;
        try {
            SimpleDateFormat inputDateFormat = new SimpleDateFormat(inputformat);
            // Parse the input date string into a Date object.
            Date inputDate = inputDateFormat.parse(datetoconvert);
            // Create a SimpleDateFormat object with the output date format.
            SimpleDateFormat outputDateFormat = new SimpleDateFormat(outputformat);
            formdate = outputDateFormat.format(inputDate);;
        } catch (Exception exd) {

        }
        return formdate;
    }

    private String dateformatter(String datevalue) {
        try {
            SimpleDateFormat oldDateFormat = new SimpleDateFormat("MM/DD/YYYY");
            // Parse the date string into a Date object.
            Date date = oldDateFormat.parse("03/09/2013");
            // Create a new SimpleDateFormat object with the new date format as the constructor parameter.
            SimpleDateFormat newDateFormat = new SimpleDateFormat("YYYY-MM-DD");
            // Format the Date object to a string using the new date format.
            String convertedDateString = newDateFormat.format(date);
            return convertedDateString;
        } catch (ParseException exc) {
            return "Failed";
        }
    }

    private String getbatchfilename(String filetype) {
        String batchfilename = filetype + "_" + System.currentTimeMillis() + ".json";
        return batchfilename;
    }

//    @PostMapping(value = "/postdcc")
//    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
//    public Map<String, String> postdcc(@RequestBody String req) {
    @PostMapping(value = "/postdcc")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> postdcc(@RequestPart(value = "file", required = false) List<MultipartFile> files, @RequestPart("data") String req) {
        loggger.info("| Request details " + req);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the pattern as per your date format

        // Save the file to a directory
        JSONArray jsonArray = new JSONArray(req);
        if (jsonArray.length() == 0) {
            return response("Error", "No data provided");
        }
        JSONObject firstRecord = jsonArray.getJSONObject(0);
        String poNumber = firstRecord.getString("poNumber");

        // Directory to save the file
        String uploadDir = "/home/app/logs/ALM/POUPL/";
        // Directory to save the files
        if (files != null) {
            for (MultipartFile file : files) {
                String originalFileName = file.getOriginalFilename();
                String fileExtension = "";
                if (originalFileName != null) {
                    int dotIndex = originalFileName.lastIndexOf('.');
                    if (dotIndex > 0) {
                        fileExtension = originalFileName.substring(dotIndex);
                    }
                }
                String newFileName = poNumber + "_" + System.currentTimeMillis() + fileExtension;

                File destinationFile = new File(uploadDir + newFileName);
                if (destinationFile.exists()) {
                    return response("Error", "File already exists: " + newFileName);
                }
                try {
                    Files.copy(file.getInputStream(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    FileRecord filerc = new FileRecord();
                    filerc.setFileName(newFileName);
                    filerc.setPoNumber(poNumber);
                    filerc.setFilePath(destinationFile.getAbsolutePath());

                    fileRepo.save(filerc);
                } catch (IOException e) {
                    return response("Error", "File upload failed: " + e.getMessage());
                }
            }
        }

        String batchfilename = getbatchfilename("DCC_BATCHUPLOAD");
        helper.logBatchFile(req, true, batchfilename);
        helper.logToFile(genHeader("N/A", "DCC_BATCHUPLOAD", "DCC_BATCHUPLOAD") + "DCC_BatchUpload file name /home/app/logs/ALM/BatchFiles/" + batchfilename, "INFO");
        JSONArray jsonArrayresponse = new JSONArray();
        long recordNo = 0;

        String vendorName = "";
        String vendorEmail = "";
        String boqId = "";
        String projectName = "";
        String acceptanceType = "";
        String status = "";
        String createdDate = "";
        String dccId = "";
        String currency = "";
        Integer createdBy = 0;

        try {
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            java.util.Date parsedDate = dateFormat.parse(now.toString());
            java.sql.Date newDate = new java.sql.Date(parsedDate.getTime());
            String requesttime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                recordNo = Integer.parseInt(jsonObject.getString("recordNo"));
                //  dccId = jsonObject.getString("dccId");
                poNumber = jsonObject.getString("poNumber");
                vendorName = jsonObject.getString("vendorName");
                //    boqId = jsonObject.getString("boqId");
                // vendorEmail = jsonObject.getString("vendorEmail");
                // projectName = jsonObject.getString("projectName");
                //     acceptanceType = jsonObject.getString("acceptanceType");
                status = jsonObject.getString("status");
                createdBy = jsonObject.getInt("createdById");
                // createdBy
                //currency = jsonObject.getString("currency");
                JSONArray dcc_line_data = jsonObject.getJSONArray("lineItems");
                String result = addEditDCC(recordNo, poNumber, vendorName,
                        status, jsonObject);
                DCC topRecord = dccrepo.findTopByPoNumber(poNumber);
                String recordId = topRecord != null ? String.valueOf(topRecord.getRecordNo()) : "";
                //insert into the main table 
                if (!status.equalsIgnoreCase("incomplete")) {
                    tb_Arc_ApprovalRecords approval = new tb_Arc_ApprovalRecords();
                    approval.setRecordDatetime(newDate);
                    approval.setRecordId(Integer.parseInt(recordId));
                    approval.setRecordTable("tb_DCC");
                    approval.setApprovalStatus("pending");
                    approval.setCreatedBy(createdBy);
                    arcApprovalRecordsRepo.save(approval);
                }
                if (result.contains("Success")) {
                    //  if (dcc_line_data.length() > 0) {
                    postdccln(poNumber, recordId, status, createdBy, dcc_line_data.toString());

                    //lets change this for now 
//                    net.minidev.json.JSONObject params = new net.minidev.json.JSONObject();
//                    params.put("approvalTypeId", "1");
//                    params.put("recordId", recordId);
//                    params.put("createdBy", createdBy);
//                    requestMap = utils.httpPOST(params.toString(), "http://" + ipaddress + ":8080/alm-zain-ksa/workflow/create", requestMap);
                    //requestMap = utils.httpPOST(params.toString(), "http://194.164.123.221:8080/alm-zain-ksa/workflow/create", requestMap);
                } else {
                    net.minidev.json.JSONObject responsedata = new net.minidev.json.JSONObject();
                    responsedata.put("dccId", dccId);
                    responsedata.put("recordNo", recordNo);
                    responsedata.put("DBresponse", result);
                    jsonArrayresponse.put(responsedata.toJSONString());
                }
            }
            if (jsonArrayresponse.length() > 0) {
                batchfilename = getbatchfilename("FailedUpload");
                helper.logBatchFile(jsonArrayresponse.toString(), true, batchfilename);
                return response("Error", jsonArrayresponse.toString());
            } else {
                return response("Success", "Complete");
            }
        } catch (NumberFormatException | ParseException | JSONException excc) {

            System.out.println(excc.toString());
        }
        return response("Error", jsonArrayresponse.toString());
    }

    public String getIPAddress() {
        String ipAddress = "";
        try {
            // Get the local host address
            InetAddress inetAddress = InetAddress.getLocalHost();

            // Get the IP address as a string
            ipAddress = inetAddress.getHostAddress();
            System.out.println("ipAddress " + ipAddress);

        } catch (UnknownHostException ex) {
            helper.logBatchFile(ex.getMessage(), true, "");
        }
        return ipAddress;
    }

    private String addEditDCC(long recordno, String poNumber, String vendorName, String status, JSONObject jsonObject) {
        String dataadded = "Failed to save";

        try {

            DCC checkdcc = dccrepo.findByRecordNo(recordno);
            if (checkdcc != null) {
                checkdcc.setPoNumber(jsonObject.getString("poNumber"));
                checkdcc.setProjectName(jsonObject.getString("projectName"));
                checkdcc.setAcceptanceType(jsonObject.getString("acceptanceType"));
                checkdcc.setStatus(jsonObject.getString("status"));
                checkdcc.setVendorName(jsonObject.getString("vendorName"));
                checkdcc.setVendorNumber(jsonObject.getString("vendorNumber"));

                try {
                    dccrepo.save(checkdcc);
                    dataadded = "Data updated Success";
                } catch (Exception exc) {
                    dataadded = exc.getCause().toString();
                }
            } else {
                DCC nwcheckdcc = new DCC();
                nwcheckdcc.setPoNumber(jsonObject.getString("poNumber"));
                nwcheckdcc.setProjectName(jsonObject.getString("projectName"));
                nwcheckdcc.setAcceptanceType(jsonObject.getString("acceptanceType"));
                nwcheckdcc.setStatus(jsonObject.getString("status"));
                nwcheckdcc.setVendorName(jsonObject.getString("vendorName"));
                nwcheckdcc.setVendorNumber(jsonObject.getString("vendorNumber"));
                try {
                    dccrepo.save(nwcheckdcc);
                    dataadded = "Data updated Success";
                } catch (Exception exc) {
                    dataadded = exc.getCause().toString();
                }
            }
        } catch (JSONException excc) {
            dataadded = "Error " + excc.getCause().toString();
        }
        return dataadded;
    }

    //Supplier Data Listener
    @PostMapping(value = "/postsupplier")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> postsupplier(@RequestBody String req) {
        String batchfilename = getbatchfilename("SUPPLIER_BATCHUPLOAD");
        helper.logBatchFile(req, true, batchfilename);
        helper.logToFile(genHeader("N/A", "SUPPLIER_BATCHUPLOAD", "SUPPLIER_BATCHUPLOAD") + "Supplier_BatchUpload file name /home/app/logs/ALM/BatchFiles/" + batchfilename, "INFO");
        JSONArray jsonArrayresponse = new JSONArray();
        long recordNo = 0;
        String supplierId = "";
        String supplierName = "";
        String address = "";
        String supplierEmail = "";
        String supplierPhone = "";
        String contactPerson = "";
        String contactPersonPhone = "";
        String contactPersonEmail = "";
        String status = "";
        String description = "";
        String createdOn = "";
        String createdBy = "";
        String lastUpdate = "";
        String lastUpdateBy = "";
        try {
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String requesttime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            JSONArray jsonArray = new JSONArray(req);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                recordNo = Integer.parseInt(jsonObject.getString("recordNo"));
                supplierId = jsonObject.getString("supplierId");
                supplierName = jsonObject.getString("supplierName");
                address = jsonObject.getString("address");
                supplierEmail = jsonObject.getString("supplierEmail");
                supplierPhone = jsonObject.getString("supplierPhone");
                status = jsonObject.getString("status");
                contactPerson = jsonObject.getString("contactPerson");
                contactPersonPhone = jsonObject.getString("contactPersonPhone");
                contactPersonEmail = jsonObject.getString("contactPersonEmail");
                description = jsonObject.getString("description");
                createdOn = jsonObject.getString("createdOn");
                createdBy = jsonObject.getString("createdBy");
                lastUpdate = jsonObject.getString("lastUpdate");
                lastUpdateBy = jsonObject.getString("lastUpdateBy");

                String jsresp = AddEditSupplier(recordNo, helper.getKenyaDateTimeString(), supplierId, supplierName, address, supplierEmail, supplierPhone, contactPerson, contactPersonPhone,
                        contactPersonEmail, status, description, createdOn, createdBy, lastUpdate, lastUpdateBy);
                if (jsresp.contains("Success")) {
                } else {
                    net.minidev.json.JSONObject responsedata = new net.minidev.json.JSONObject();
                    responsedata.put("recordNo", recordNo);
                    responsedata.put("supplierId", supplierId);
                    responsedata.put("DBresponse", jsresp);
                    jsonArrayresponse.put(responsedata.toJSONString());
                }
            }
            if (jsonArrayresponse.length() > 0) {
                batchfilename = getbatchfilename("FailedUpload");
                helper.logBatchFile(jsonArrayresponse.toString(), true, batchfilename);
                return response("Error", jsonArrayresponse.toString());
            } else {
                return response("Success", "Complete");
            }
        } catch (Exception exc) {
            net.minidev.json.JSONObject response = new net.minidev.json.JSONObject();
            response.put("supplierId", supplierId);
            response.put("supplierName", supplierName);
            return response("Error", jsonArrayresponse.toString());
        }
    }

    //Add Edit DCC
    private String postdccln(String poNumber, String recordId, String status, Integer createdBy, String req) {
        //   String batchfilename = getbatchfilename("DCCLN_BATCHUPLOAD");
        // helper.logBatchFile(req, true, batchfilename);
        // helper.logToFile(genHeader("N/A", "DCCLN_BATCHUPLOAD", "DCCLN_BATCHUPLOAD") + "DCCLN_BATCHUPLOAD file name /home/app/logs/ALM/BatchFiles/" + batchfilename, "INFO");
        JSONArray jsonArrayresponse = new JSONArray();
        long recordNo = 0;
        String productName = "";
        String productSerialNo = "";
        int deliveredQty = 0;
        String locationName = "";
        String inserviceDate = "";
        String scopeOfWork = "";
        String remarks = "";
        BigDecimal unitPrice;
        String dccId = "";
        String itemCode = "";
        try {
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String requesttime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            JSONArray jsonArray = new JSONArray(req);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                recordNo = Integer.parseInt(jsonObject.getString("recordNo"));

                String jsresp = addDCCLineItem(poNumber, recordNo, recordId, status, createdBy, jsonObject);
                if (!jsresp.contains("Success")) {
                    net.minidev.json.JSONObject responsedata = new net.minidev.json.JSONObject();
                    responsedata.put("recordNo", recordNo);
                    responsedata.put("dccId", dccId);
                    responsedata.put("DBresponse", jsresp);
                    jsonArrayresponse.put(responsedata.toJSONString());
                }
            }
            if (jsonArrayresponse.length() > 0) {
                //     batchfilename = getbatchfilename("FailedUpload");
                // helper.logBatchFile(jsonArrayresponse.toString(), true, batchfilename);
                return (jsonArrayresponse.toString());
            } else {
                return ("Complete");
            }
        } catch (NumberFormatException | JSONException exc) {
            net.minidev.json.JSONObject response = new net.minidev.json.JSONObject();
            response.put("productSerialNo", productSerialNo);
            response.put("dccId", dccId);
            return (jsonArrayresponse.toString());
        }
    }

    public String addDCCLineItem(String poNumber, long recordno, String recordId, String status, Integer createdBy, JSONObject jsonObject) {

        System.out.println("DCC LIne " + jsonObject.toString());

        loggger.info("| DCC LIne " + jsonObject.toString());

        String result = "Failed to save";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the pattern as per your date format
        LocalDateTime now = LocalDateTime.now();

        DCCLineItem eddccLineItem = dcclnrepo.findByRecordNo(recordno);
        if (eddccLineItem != null) {
            eddccLineItem.setDccId(recordId);
            eddccLineItem.setLineNumber(jsonObject.getString("poLineNumber"));
            eddccLineItem.setUplLineNumber(jsonObject.getString("uplLineNumber"));
            eddccLineItem.setItemCode(jsonObject.getString("itemCode"));
            eddccLineItem.setSerialNumber(jsonObject.getString("serialNumber"));
            Integer delivered = Integer.parseInt(jsonObject.getString("deliveredQty"));

            eddccLineItem.setPoId(poNumber);
            eddccLineItem.setDeliveredQty(delivered);
            eddccLineItem.setLocationName(jsonObject.getString("locationName"));
            String dateInServiceString = jsonObject.getString("dateInService");
            if (jsonObject.has("uplItemCode")) {
                eddccLineItem.setUplItemCode(jsonObject.getString("uplItemCode"));
            }
            if (jsonObject.has("uplItemDescription")) {
                eddccLineItem.setUplItemDescription(jsonObject.getString("uplItemDescription"));
            }
            try {
                java.util.Date parsedDate = dateFormat.parse(dateInServiceString);
                java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
                eddccLineItem.setDateInService(sqlDate);
            } catch (ParseException ex) {
                loggger.info("Exception " + ex.getMessage());

                java.util.logging.Logger.getLogger(APIController.class.getName()).log(Level.SEVERE, null, ex);
            }
            eddccLineItem.setScopeOfWork(jsonObject.getString("scopeOfWork"));
            eddccLineItem.setRemarks(jsonObject.getString("remarks"));

            try {
                dcclnrepo.save(eddccLineItem);
                result = "Record update Success";
            } catch (Exception ex) {
                loggger.info("Exception " + ex.getMessage());

                System.out.println(ex.getMessage());
                result = ex.getCause().toString();
            }

        } else {
            DCCLineItem dccLineItem = new DCCLineItem();
            dccLineItem.setDccId(recordId);
            dccLineItem.setLineNumber(jsonObject.getString("poLineNumber"));
            dccLineItem.setUplLineNumber(jsonObject.getString("uplLineNumber"));
            dccLineItem.setItemCode(jsonObject.getString("itemCode"));
            dccLineItem.setSerialNumber(jsonObject.getString("serialNumber"));
            dccLineItem.setLocationName(jsonObject.getString("locationName"));
            String dateInServiceString = jsonObject.getString("dateInService");
            Integer delivered = Integer.parseInt(jsonObject.getString("deliveredQty"));
            dccLineItem.setPoId(poNumber);
            dccLineItem.setDeliveredQty(delivered);
            try {
                java.util.Date parsedDate = dateFormat.parse(dateInServiceString);
                java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
                dccLineItem.setDateInService(sqlDate);
            } catch (ParseException ex) {
                java.util.logging.Logger.getLogger(APIController.class.getName()).log(Level.SEVERE, null, ex);
            }
            dccLineItem.setScopeOfWork(jsonObject.getString("scopeOfWork"));
            dccLineItem.setRemarks(jsonObject.getString("remarks"));

            System.out.println("DCC LIne save  " + dccLineItem);

            try {
                dcclnrepo.save(dccLineItem);
                result = "Record add Success";
                //NOW HERE INSERT IN THE APPROVAL TABLE  

                tb_Arc_ApprovalRecords topRecordNo = arcApprovalRecordsRepo.findTopByRecordId(Integer.parseInt(recordId));
                String approvalRecordNo = topRecordNo != null ? String.valueOf(topRecordNo.getRecordNo()) : "";

                java.util.Date parsedDate = dateFormat.parse(now.toString());
                java.sql.Date newDate = new java.sql.Date(parsedDate.getTime());

                //check UP BASED 
                if (jsonObject.getString("uplLineNumber").length() > 0 && !status.equalsIgnoreCase("incomplete")) {
                    tb_PurchaseOrderUPL topRecord = purchaseOrderUPLRepo.findTopByPoNumberAndUplLine(poNumber, jsonObject.getString("uplLineNumber"));
                    String approver1 = topRecord != null ? String.valueOf(topRecord.getDptApprover1()) : "";
                    String approver2 = topRecord != null ? String.valueOf(topRecord.getDptApprover2()) : "";
                    String approver3 = topRecord != null ? String.valueOf(topRecord.getDptApprover3()) : "";
                    String approver4 = topRecord != null ? String.valueOf(topRecord.getDptApprover4()) : "";
                    String regionalApprover = topRecord != null ? String.valueOf(topRecord.getRegionalApprover()) : "";

                    System.out.println("approver1 " + approver1);
                    System.out.println("approver2 " + approver2);
                    System.out.println("approver3 " + approver3);
                    System.out.println("approver4 " + approver4);
                    System.out.println("regionalApprover " + regionalApprover);
                    if (approver1.length() > 1) {
                        tb_Approval_Log newApprovalLog = new tb_Approval_Log();
                        newApprovalLog.setRecordDatetime(newDate);
                        newApprovalLog.setApprovalRecordId(Integer.parseInt(approvalRecordNo));
                        newApprovalLog.setRecordType("ACCEPTANCE");
                        newApprovalLog.setPONumber(poNumber);
                        newApprovalLog.setStatus("Pending");
                        newApprovalLog.setRegion("");
                        newApprovalLog.setCreatedBy(createdBy);
                        newApprovalLog.setApprover(approver1);
                        approvalLogRepo.save(newApprovalLog);
                    }
                    if (approver2.length() > 1) {
                        tb_Approval_Log newApprovalLog = new tb_Approval_Log();
                        newApprovalLog.setRecordDatetime(newDate);
                        newApprovalLog.setApprovalRecordId(Integer.parseInt(approvalRecordNo));
                        newApprovalLog.setRecordType("ACCEPTANCE");
                        newApprovalLog.setPONumber(poNumber);
                        newApprovalLog.setStatus("Pending");
                        newApprovalLog.setRegion("");
                        newApprovalLog.setCreatedBy(createdBy);
                        newApprovalLog.setApprover(approver2);
                        approvalLogRepo.save(newApprovalLog);
                    }
                    if (approver3.length() > 1) {
                        tb_Approval_Log newApprovalLog = new tb_Approval_Log();
                        newApprovalLog.setRecordDatetime(newDate);
                        newApprovalLog.setApprovalRecordId(Integer.parseInt(approvalRecordNo));
                        newApprovalLog.setRecordType("ACCEPTANCE");
                        newApprovalLog.setPONumber(poNumber);
                        newApprovalLog.setStatus("Pending");
                        newApprovalLog.setRegion("");
                        newApprovalLog.setCreatedBy(createdBy);
                        newApprovalLog.setApprover(approver3);
                        approvalLogRepo.save(newApprovalLog);
                    }
                    if (approver4.length() > 1) {
                        tb_Approval_Log newApprovalLog = new tb_Approval_Log();
                        newApprovalLog.setRecordDatetime(newDate);
                        newApprovalLog.setApprovalRecordId(Integer.parseInt(approvalRecordNo));
                        newApprovalLog.setRecordType("ACCEPTANCE");
                        newApprovalLog.setPONumber(poNumber);
                        newApprovalLog.setStatus("Pending");
                        newApprovalLog.setRegion("");
                        newApprovalLog.setCreatedBy(createdBy);
                        newApprovalLog.setApprover(approver4);
                        approvalLogRepo.save(newApprovalLog);
                    }
                    if (regionalApprover.length() > 1) {
                        tb_Approval_Log newApprovalLog = new tb_Approval_Log();
                        newApprovalLog.setRecordDatetime(newDate);
                        newApprovalLog.setApprovalRecordId(Integer.parseInt(approvalRecordNo));
                        newApprovalLog.setRecordType("ACCEPTANCE");
                        newApprovalLog.setPONumber(poNumber);
                        newApprovalLog.setStatus("Pending");
                        newApprovalLog.setRegion("");
                        newApprovalLog.setCreatedBy(createdBy);
                        newApprovalLog.setApprover(regionalApprover);
                        approvalLogRepo.save(newApprovalLog);
                    }
                }  //NON UPL BASED 
                if (jsonObject.getString("uplLineNumber").length() < 0 && !status.equalsIgnoreCase("incomplete")) {
                    tbPurchaseOrder topRecord = PurchaseOrderRepo.findTopByPoNumberAndLineNumber(jsonObject.getString("poNumber"), jsonObject.getString("poLineNumber"));
                    String businessOwner = topRecord != null ? String.valueOf(topRecord.getBusinessOwner()) : "";

                    if (businessOwner.length() > 1) {
                        tb_Approval_Log newApprovalLog = new tb_Approval_Log();
                        newApprovalLog.setRecordDatetime(newDate);
                        newApprovalLog.setApprovalRecordId(Integer.parseInt(approvalRecordNo));
                        newApprovalLog.setRecordType("ACCEPTANCE");
                        newApprovalLog.setPONumber(poNumber);
                        newApprovalLog.setStatus("Pending");
                        newApprovalLog.setRegion("");
                        newApprovalLog.setCreatedBy(createdBy);
                        newApprovalLog.setApprover(businessOwner);
                        approvalLogRepo.save(newApprovalLog);
                    }
                }

            } catch (NumberFormatException | ParseException | JSONException exc) {
                System.out.println(exc.getMessage());
                result = exc.getCause().toString();
            }
        }
        return result;
    }

    private String addeditDccStatus(long recordno, String status, String userId, String dccId, int lnRecordNo) {
        String dataadded = "Failed to save";
        try {
            dccstatus checkdcc = dccstatrepo.findByRecordNo(recordno);
            if (checkdcc != null) {
                checkdcc.setDccId(dccId);
                checkdcc.setStatus(status);
                checkdcc.setUserId(userId);
                checkdcc.setLnRecordNo(lnRecordNo);
                try {
                    dccstatrepo.save(checkdcc);
                    dataadded = "Data updated Success";
                } catch (Exception exc) {
                    dataadded = exc.getCause().toString();
                }
            } else {
                dccstatus nwcheckdcc = new dccstatus();
                nwcheckdcc.setDccId(dccId);
                nwcheckdcc.setStatus(status);
                nwcheckdcc.setUserId(userId);
                nwcheckdcc.setStatusDate(helper.getKenyaDateTimeString());
                nwcheckdcc.setLnRecordNo(lnRecordNo);
                try {
                    dccstatrepo.save(nwcheckdcc);
                    dataadded = "Data updated Success";
                } catch (Exception exc) {
                    dataadded = exc.getCause().toString();
                }
            }
        } catch (Exception excc) {
            dataadded = "Error " + excc.getCause().toString();
        }
        return dataadded;
    }

    private void updatedccdata(String dccid, long recordno, String status) {
        DCC checkdcc = dccrepo.findByRecordNo(recordno);
        if (checkdcc != null) {
            checkdcc.setStatus(status);
            try {
                dccrepo.save(checkdcc);
            } catch (Exception exc) {
                exc.getCause().toString();
            }
        }
    }

    @PostMapping(value = "/postdccstatus")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> postdccstatus(@RequestBody String req) {
        String batchfilename = getbatchfilename("DCCSTATUS_BATCHUPLOAD");
        helper.logBatchFile(req, true, batchfilename);
        helper.logToFile(genHeader("N/A", "DCCSTATUS_BATCHUPLOAD", "DCCSTATUS_BATCHUPLOAD") + "DDCCSTATUS_BatchUpload file name /home/app/logs/ALM/BatchFiles/" + batchfilename, "INFO");
        JSONArray jsonArrayresponse = new JSONArray();
        long recordNo = 0;

        String dccId = "";
        String userId = "";
        String status = "";
        int lnRecordNo = 0;
        long ddcRecordNo = 0;
        try {
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String requesttime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            JSONArray jsonArray = new JSONArray(req);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                recordNo = Integer.parseInt(jsonObject.getString("recordNo"));
                ddcRecordNo = Integer.parseInt(jsonObject.getString("ddcRecordNo"));
                dccId = jsonObject.getString("dccId");
                userId = jsonObject.getString("userId");
                status = jsonObject.getString("status");
                lnRecordNo = jsonObject.getInt("lnRecordNo");
                String result = addeditDccStatus(recordNo, status, userId, dccId, lnRecordNo);
                updatedccdata(dccId, ddcRecordNo, status);
                if (result.contains("Success")) {

                } else {
                    net.minidev.json.JSONObject responsedata = new net.minidev.json.JSONObject();
                    responsedata.put("dccId", dccId);
                    responsedata.put("recordNo", recordNo);
                    responsedata.put("DBresponse", result);
                    jsonArrayresponse.put(responsedata.toJSONString());
                }
            }
            if (jsonArrayresponse.length() > 0) {
                batchfilename = getbatchfilename("FailedUpload");
                helper.logBatchFile(jsonArrayresponse.toString(), true, batchfilename);
                return response("Error", jsonArrayresponse.toString());
            } else {
                return response("Success", "Complete");
            }
        } catch (Exception excc) {

        }
        return response("Error", jsonArrayresponse.toString());
    }

    @Autowired
    supplierrepo suprepo;

    //Supplier CRUD
    public String AddEditSupplier(long recordNo, String recordDatetime, String supplierId, String supplierName, String address, String supplierEmail, String supplierPhone, String contactPerson, String contactPersonPhone,
            String contactPersonEmail, String status, String description, String createdOn, String createdBy, String lastUpdate, String lastUpdateBy) {
        String responseinfo = "Failed to save or data";
        if (recordNo > 0) {
            supplierdata spldt = suprepo.findByRecordNo(recordNo);
            if (spldt != null) {
                spldt.setAddress(address);
                spldt.setContactPerson(contactPerson);
                spldt.setSupplierId(supplierId);
                spldt.setRecordDatetime(recordDatetime);
                spldt.setCreatedBy(createdBy);
                spldt.setDescription(description);
                spldt.setAddress(address);
                spldt.setStatus(status);
                spldt.setContactPersonEmail(contactPersonEmail);
                spldt.setSupplierName(supplierName);
                spldt.setCreatedOn(createdOn);
                spldt.setSupplierPhone(supplierPhone);
                spldt.setSupplierEmail(supplierEmail);
                spldt.setContactPerson(contactPerson);
                spldt.setLastUpdate(lastUpdate);
                spldt.setLastUpdateBy(lastUpdateBy);
                spldt.setSupplierPhone(supplierPhone);
                try {
                    suprepo.save(spldt);
                    responseinfo = "Record Update Success";
                } catch (Exception excc) {
                    helper.logToFile(genHeader("N/A", "ErrorUpdateSupplier", "ErrorUpdateSupplier") + "ErrorUpdateSupplier Supplier RecordNo " + recordNo, "INFO");
                }
            } else {
                supplierdata nwspldt = new supplierdata();
                nwspldt.setAddress(address);
                nwspldt.setContactPerson(contactPerson);
                nwspldt.setSupplierId(supplierId);
                nwspldt.setRecordDatetime(recordDatetime);
                nwspldt.setCreatedBy(createdBy);
                nwspldt.setDescription(description);
                nwspldt.setAddress(address);
                nwspldt.setStatus(status);
                nwspldt.setContactPersonEmail(contactPersonEmail);
                nwspldt.setSupplierName(supplierName);
                nwspldt.setCreatedOn(createdOn);
                nwspldt.setSupplierPhone(supplierPhone);
                nwspldt.setSupplierEmail(supplierEmail);
                nwspldt.setContactPerson(contactPerson);
                nwspldt.setLastUpdate(lastUpdate);
                nwspldt.setLastUpdateBy(lastUpdateBy);
                nwspldt.setSupplierPhone(supplierPhone);
                try {
                    suprepo.save(nwspldt);
                    responseinfo = "Record Create Success";
                } catch (Exception excc) {
                    helper.logToFile(genHeader("N/A", "ErrorAddingSupplier", "ErrorAddingSupplier") + "ErrorAddingSupplier Supplier Supplier ID " + supplierId, "INFO");
                }
            }
        } else {
            supplierdata nwspldt = new supplierdata();
            nwspldt.setAddress(address);
            nwspldt.setContactPerson(contactPerson);
            nwspldt.setSupplierId(supplierId);
            nwspldt.setRecordDatetime(recordDatetime);
            nwspldt.setCreatedBy(createdBy);
            nwspldt.setDescription(description);
            nwspldt.setAddress(address);
            nwspldt.setStatus(status);
            nwspldt.setContactPersonEmail(contactPersonEmail);
            nwspldt.setSupplierName(supplierName);
            nwspldt.setCreatedOn(createdOn);
            nwspldt.setSupplierPhone(supplierPhone);
            nwspldt.setSupplierEmail(supplierEmail);
            nwspldt.setContactPerson(contactPerson);
            nwspldt.setLastUpdate(lastUpdate);
            nwspldt.setLastUpdateBy(lastUpdateBy);
            nwspldt.setSupplierPhone(supplierPhone);
            try {
                suprepo.save(nwspldt);
                responseinfo = "Record Created";
            } catch (Exception excc) {
                helper.logToFile(genHeader("N/A", "ErrorAddingSupplier", "ErrorAddingSupplier") + "ErrorAddingSupplier Supplier Supplier ID " + supplierId, "INFO");
            }
        }
        return responseinfo;
    }

    //PO Manager
    public String AddEditPOHD(long recordNo, String recordDatetime, String poId, String poDate, String supplierId,
            String termsAndConditions, String deliveryLocationId, String createdBy, String status, JSONObject jsonObject) {
        String responseinfo = "Failed to save or data";
        pohddata spldt = pohd_repo.findByRecordNo(recordNo);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the pattern as per your date format

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

            if (jsonObject.has("typeLookUpCode")) {
                spldt.setTypeLookUpCode(jsonObject.getString("typeLookUpCode"));
            }
            if (jsonObject.has("releaseNum")) {
                spldt.setReleaseNum(jsonObject.getString("releaseNum"));
            }
            if (jsonObject.has("prNum")) {
                spldt.setPrNum(jsonObject.getString("prNum"));
            }
            if (jsonObject.has("pnSubAllow")) {
                spldt.setPnSubAllow(jsonObject.getString("pnSubAllow"));
            }
            if (jsonObject.has("countryOfOrigin")) {
                spldt.setCountryOfOrigin(jsonObject.getString("countryOfOrigin"));
            }
            if (jsonObject.has("currencyCode")) {
                spldt.setCurrencyCode(jsonObject.getString("currencyCode"));
            }
            if (jsonObject.has("subInventoryCode")) {
                spldt.setSubInventoryCode(jsonObject.getString("subInventoryCode"));
            }
            if (jsonObject.has("receiptRouting")) {
                spldt.setReceiptRouting(jsonObject.getString("receiptRouting"));
            }
            if (jsonObject.has("poClosureStatus")) {
                spldt.setProjectNumber(jsonObject.getString("poClosureStatus"));
            }
            if (jsonObject.has("chargeAccount")) {
                spldt.setChargeAccount(jsonObject.getString("chargeAccount"));
            }
            if (jsonObject.has("serialControl")) {
                spldt.setSerialControl(jsonObject.getString("serialControl"));
            }
            try {
                spldt.setDateInService((java.sql.Date) dateFormat.parse(dateInServiceString));
            } catch (ParseException ex) {
                java.util.logging.Logger.getLogger(APIController.class.getName()).log(Level.SEVERE, null, ex);
            }
            spldt.setCurrency(jsonObject.getString("currency"));
            spldt.setUnitPrice(jsonObject.getDouble("unitPrice"));
            spldt.setPartNumber(jsonObject.getString("partNumber"));
            spldt.setCostCenter(jsonObject.getString("costCenter"));

            try {
                pohd_repo.save(spldt);
                responseinfo = "Record Updated Success";
            } catch (Exception excc) {

                System.out.println(excc.getMessage());
                helper.logToFile(genHeader("N/A", "ErrorAddingPO_HD", "API") + "ErrorAddingPO_HD PO_HD RecordNo "
                        + recordNo, "INFO");
                responseinfo = excc.toString();
            }
        } else {
            pohddata nwspldt = new pohddata();
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
            if (jsonObject.has("typeLookUpCode")) {
                nwspldt.setTypeLookUpCode(jsonObject.getString("typeLookUpCode"));
            }
            if (jsonObject.has("releaseNum")) {
                nwspldt.setReleaseNum(jsonObject.getString("releaseNum"));
            }
            if (jsonObject.has("prNum")) {
                nwspldt.setPrNum(jsonObject.getString("prNum"));
            }
            if (jsonObject.has("pnSubAllow")) {
                nwspldt.setPnSubAllow(jsonObject.getString("pnSubAllow"));
            }
            if (jsonObject.has("countryOfOrigin")) {
                nwspldt.setCountryOfOrigin(jsonObject.getString("countryOfOrigin"));
            }
            if (jsonObject.has("currencyCode")) {
                nwspldt.setCurrencyCode(jsonObject.getString("currencyCode"));
            }
            if (jsonObject.has("subInventoryCode")) {
                nwspldt.setSubInventoryCode(jsonObject.getString("subInventoryCode"));
            }
            if (jsonObject.has("receiptRouting")) {
                nwspldt.setReceiptRouting(jsonObject.getString("receiptRouting"));
            }
            if (jsonObject.has("poClosureStatus")) {
                nwspldt.setProjectNumber(jsonObject.getString("poClosureStatus"));
            }
            if (jsonObject.has("chargeAccount")) {
                nwspldt.setChargeAccount(jsonObject.getString("chargeAccount"));
            }
            if (jsonObject.has("serialControl")) {
                nwspldt.setSerialControl(jsonObject.getString("serialControl"));
            }
//                  String dateInServiceString = jsonObject.getString("dateInService");
//                    try {
//                          spldt.setDateInService(dateFormat.parse(dateInServiceString));
//                dateInService = dateFormat.parse(dateInServiceString);
//            } catch (ParseException ex) {
//                java.util.logging.Logger.getLogger(APIController.class.getName()).log(Level.SEVERE, null, ex);
//            }
            nwspldt.setCurrency(jsonObject.getString("currency"));
            nwspldt.setUnitPrice(jsonObject.getDouble("unitPrice"));
            nwspldt.setPartNumber(jsonObject.getString("partNumber"));
            nwspldt.setCostCenter(jsonObject.getString("costCenter"));
            try {
                pohd_repo.save(nwspldt);
                responseinfo = "Record Created Success";
            } catch (Exception excc) {
                System.out.println(excc.getMessage());
                helper.logToFile(genHeader("N/A", "ErrorAddingPO_HD", "ErrorAddingPO_HD")
                        + "ErrorAddingPO_HD PO ID " + poId + " Supplier ID " + supplierId, "INFO");
                responseinfo = excc.toString();
            }
        }
        return responseinfo;
    }

    public String AddEditPOLN(JSONObject jsonObject, long recordNo, String recordDatetime, String poId, long lineNumber, String itemCode, String UoM,
            int orderQuantity, BigDecimal unitPrice, BigDecimal VAT, BigDecimal linePrice) {
        String responseinfo = "Failed to save or data";
        polndata spldt = poln_repo.findByRecordNo(recordNo);
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

            if (jsonObject.has("quantityDueOld")) {
                spldt.setQuantityDueOld(jsonObject.getString("quantityDueOld"));
            }
            if (jsonObject.has("quantityDueNew")) {
                spldt.setQuantityDueNew(jsonObject.getString("quantityDueNew"));
            }
            if (jsonObject.has("quantityBilled")) {
                spldt.setQuantityBilled(jsonObject.getString("quantityBilled"));
            }
            if (jsonObject.has("unitPriceInSAR")) {
                spldt.setUnitPriceInSAR(jsonObject.getBigDecimal("unitPriceInSAR"));
            }
            if (jsonObject.has("linePriceInPoCurrency")) {
                spldt.setLinePriceInPoCurrency(jsonObject.getBigDecimal("linePriceInPoCurrency"));
            }
            if (jsonObject.has("linePriceInSAR")) {
                spldt.setLinePriceInSAR(jsonObject.getBigDecimal("linePriceInSAR"));
            }
            if (jsonObject.has("amountReceived")) {
                spldt.setAmountReceived(jsonObject.getBigDecimal("amountReceived"));
            }
            if (jsonObject.has("amountDue")) {
                spldt.setAmountDue(jsonObject.getBigDecimal("amountDue"));
            }
            if (jsonObject.has("amountDueNew")) {
                spldt.setAmountDue(jsonObject.getBigDecimal("amountDueNew"));
            }
            if (jsonObject.has("amountBilled")) {
                spldt.setAmountBilled(jsonObject.getBigDecimal("amountBilled"));
            }
            if (jsonObject.has("poLineType")) {
                spldt.setPoLineType(jsonObject.getString("poLineType"));
            }
            if (jsonObject.has("itemType")) {
                spldt.setItemType(jsonObject.getString("itemType"));
            }
            if (jsonObject.has("itemCategoryInventory")) {
                spldt.setItemCategoryInventory(jsonObject.getString("itemCategoryInventory"));
            }
            if (jsonObject.has("categoryDescription")) {
                spldt.setCategoryDescription(jsonObject.getString("categoryDescription"));
            }
            if (jsonObject.has("itemCategoryFA")) {
                spldt.setItemCategoryFA(jsonObject.getString("itemCategoryFA"));
            }
            if (jsonObject.has("FACategoryDescription")) {
                spldt.setFACategoryDescription(jsonObject.getString("FACategoryDescription"));
            }
            if (jsonObject.has("itemCategoryPurchasing")) {
                spldt.setItemCategoryPurchasing(jsonObject.getString("itemCategoryPurchasing"));
            }
            if (jsonObject.has("PurchasingCategoryDescription")) {
                spldt.setPurchasingCategoryDescription(jsonObject.getString("PurchasingCategoryDescription"));
            }

            try {
                poln_repo.save(spldt);
                responseinfo = "Record Updated Success";
            } catch (Exception excc) {
                helper.logToFile(genHeader("N/A", "ErrorAddingPO_HD", "API") + "ErrorAddingPO_HD PO_HD RecordNo " + recordNo, "INFO");
                responseinfo = excc.toString();
            }
        } else {
            polndata nwspldt = new polndata();
            nwspldt.setRecordDatetime(recordDatetime);
            nwspldt.setPoId(poId);
            nwspldt.setLineNumber(lineNumber);
            nwspldt.setItemCode(itemCode);
            nwspldt.setUnitPrice(unitPrice);
            nwspldt.setOrderQuantity(orderQuantity);
            nwspldt.setVAT(VAT);
            nwspldt.setLinePrice(linePrice);
            nwspldt.setUoM(UoM);

            if (jsonObject.has("quantityDueOld")) {
                nwspldt.setQuantityDueOld(jsonObject.getString("quantityDueOld"));
            }
            if (jsonObject.has("quantityDueNew")) {
                nwspldt.setQuantityDueNew(jsonObject.getString("quantityDueNew"));
            }
            if (jsonObject.has("quantityBilled")) {
                nwspldt.setQuantityBilled(jsonObject.getString("quantityBilled"));
            }
            if (jsonObject.has("unitPriceInSAR")) {
                nwspldt.setUnitPriceInSAR(jsonObject.getBigDecimal("unitPriceInSAR"));
            }
            if (jsonObject.has("linePriceInPoCurrency")) {
                nwspldt.setLinePriceInPoCurrency(jsonObject.getBigDecimal("linePriceInPoCurrency"));
            }
            if (jsonObject.has("linePriceInSAR")) {
                nwspldt.setLinePriceInSAR(jsonObject.getBigDecimal("linePriceInSAR"));
            }
            if (jsonObject.has("amountReceived")) {
                nwspldt.setAmountReceived(jsonObject.getBigDecimal("amountReceived"));
            }
            if (jsonObject.has("amountDue")) {
                nwspldt.setAmountDue(jsonObject.getBigDecimal("amountDue"));
            }
            if (jsonObject.has("amountDueNew")) {
                nwspldt.setAmountDue(jsonObject.getBigDecimal("amountDueNew"));
            }
            if (jsonObject.has("amountBilled")) {
                nwspldt.setAmountBilled(jsonObject.getBigDecimal("amountBilled"));
            }

            if (jsonObject.has("poLineType")) {
                nwspldt.setPoLineType(jsonObject.getString("poLineType"));
            }
            if (jsonObject.has("itemType")) {
                nwspldt.setItemType(jsonObject.getString("itemType"));
            }
            if (jsonObject.has("itemCategoryInventory")) {
                nwspldt.setItemCategoryInventory(jsonObject.getString("itemCategoryInventory"));
            }
            if (jsonObject.has("categoryDescription")) {
                nwspldt.setCategoryDescription(jsonObject.getString("categoryDescription"));
            }
            if (jsonObject.has("itemCategoryFA")) {
                nwspldt.setItemCategoryFA(jsonObject.getString("itemCategoryFA"));
            }
            if (jsonObject.has("FACategoryDescription")) {
                nwspldt.setFACategoryDescription(jsonObject.getString("FACategoryDescription"));
            }
            if (jsonObject.has("itemCategoryPurchasing")) {
                nwspldt.setItemCategoryPurchasing(jsonObject.getString("itemCategoryPurchasing"));
            }
            if (jsonObject.has("PurchasingCategoryDescription")) {
                nwspldt.setPurchasingCategoryDescription(jsonObject.getString("PurchasingCategoryDescription"));
            }

            try {
                poln_repo.save(nwspldt);
                responseinfo = "Record Created Success";
            } catch (Exception excc) {
                helper.logToFile(genHeader("N/A", "ErrorAddingPO_LN", "ErrorAddingPO_LN") + "ErrorAddingPO_LN PO ID "
                        + poId + " Item Code " + itemCode, "INFO");
                responseinfo = excc.toString();
            }
        }
        return responseinfo;
    }

    @PostMapping(value = "/postupl")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> postupl(@RequestBody String req) {
        String batchfilename = getbatchfilename("UPL_BATCHUPLOAD");
        helper.logBatchFile(req, true, batchfilename);
        helper.logToFile(genHeader("N/A", "UPL_BATCHUPLOAD", "UPL_BATCHUPLOAD")
                + "UPL_BATCHUPLOAD file name /home/app/logs/ALM/BatchFiles/" + batchfilename, "INFO");
        JSONArray jsonArrayresponse = new JSONArray();
        long recordNo = 0;
        String projectName = "";
        String poId = "";
        String customerItemType = "";
        String localContent = "";
        String Scope = "";
        String subScope = "";
        String poLine = "";
        String uplLine = "";
        String vendorItemCode = "";
        String poLineItemDescription = "";
        String erpItemDescription = "";
        String zainItemCategory = "";
        String serialized = "";
        String activePassive = "";
        String UOM = "";
        double quantity;
        String unit = "";
        String currency = "";
        double discount;
        double unitPriceBeforeDiscount;
        double poTotalAmtBeforeDiscount;
        double finalTotalPriceAfterDiscount;
        String huaweiComments = "";
        String amuComments = "";
        String procurementComments = "";

        try {
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String requesttime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            JSONArray jsonArray = new JSONArray(req);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                recordNo = Integer.parseInt(jsonObject.getString("recordNo"));
                poId = jsonObject.getString("poId");
                projectName = jsonObject.getString("projectName");
                customerItemType = jsonObject.getString("customerItemType");
                localContent = jsonObject.getString("localContent");
                Scope = jsonObject.getString("Scope");
                subScope = jsonObject.getString("subScope");
                poLine = jsonObject.getString("poLine");
                uplLine = jsonObject.getString("uplLine");
                serialized = jsonObject.getString("serialized");
                vendorItemCode = jsonObject.getString("vendorItemCode");
                poLineItemDescription = jsonObject.getString("poLineItemDescription");
                erpItemDescription = jsonObject.getString("erpItemDescription");
                zainItemCategory = jsonObject.getString("zainItemCategory");
                activePassive = jsonObject.getString("activePassive");
                UOM = jsonObject.getString("UOM");
                quantity = jsonObject.getInt("quantity");
                discount = jsonObject.getDouble("discount");
                unitPriceBeforeDiscount = jsonObject.getDouble("unitPriceBeforeDiscount");
                poTotalAmtBeforeDiscount = jsonObject.getDouble("poTotalAmtBeforeDiscount");
                finalTotalPriceAfterDiscount = jsonObject.getDouble("finalTotalPriceAfterDiscount");
                unit = jsonObject.getString("unit");
                currency = jsonObject.getString("currency");
                huaweiComments = jsonObject.getString("huaweiComments");
                amuComments = jsonObject.getString("amuComments");
                procurementComments = jsonObject.getString("procurementComments");
                String jsresp = "";
                String addporesp = addeditupl(recordNo, projectName, poId, customerItemType, localContent,
                        Scope, subScope, poLine, uplLine, vendorItemCode, poLineItemDescription,
                        erpItemDescription, zainItemCategory, serialized, activePassive, UOM,
                        quantity, unit, currency, discount, unitPriceBeforeDiscount,
                        poTotalAmtBeforeDiscount, finalTotalPriceAfterDiscount,
                        huaweiComments, amuComments, procurementComments, jsonObject);

                if (addporesp.contains("Success")) {

                } else {
                    net.minidev.json.JSONObject responsedata = new net.minidev.json.JSONObject();
                    responsedata.put("recordNo", recordNo);
                    responsedata.put("poId", poId);
                    responsedata.put("DBresponse", jsresp);
                    jsonArrayresponse.put(responsedata.toJSONString());
                }
            }
            if (jsonArrayresponse.length() > 0) {
                batchfilename = getbatchfilename("FailedUpload");
                helper.logBatchFile(jsonArrayresponse.toString(), true, batchfilename);
                return response("Error", jsonArrayresponse.toString());
            } else {
                return response("Success", "Complete");
            }
        } catch (NumberFormatException | JSONException exc) {
            net.minidev.json.JSONObject response = new net.minidev.json.JSONObject();
            response.put("poId", poId);
            return response("Error", jsonArrayresponse.toString());
        }
    }

    private String addeditupl(long recordNo, String projectName, String poId, String customerItemType, String localContent,
            String Scope, String subScope, String poLine, String uplLine, String vendorItemCode, String poLineItemDescription,
            String erpItemDescription, String zainItemCategory, String serialized, String activePassive, String UOM,
            double quantity, String unit, String currency, double discount, double unitPriceBeforeDiscount,
            double poTotalAmtBeforeDiscount, double finalTotalPriceAfterDiscount,
            String huaweiComments, String amuComments, String procurementComments, JSONObject jsonObject) {
        String resultsave = "Failed to save";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the pattern as per your date format
        LocalDateTime now = LocalDateTime.now();

        upldata updt = uprepo.findByRecordNo(recordNo);
        if (updt != null) {
            updt.setActivePassive(activePassive);
            updt.setCurrency(currency);
            updt.setAmuComments(amuComments);
            updt.setPoId(poId);
            updt.setDiscount(discount);
            updt.setPoId(poId);
            updt.setProjectName(projectName);
            updt.setCustomerItemType(customerItemType);
            updt.setLocalContent(localContent);
            updt.setScope(Scope);
            updt.setSubScope(subScope);
            updt.setPoLine(poLine);
            updt.setUplLine(uplLine);
            updt.setVendorItemCode(vendorItemCode);
            updt.setPoLineItemDescription(poLineItemDescription);
            updt.setErpItemDescription(erpItemDescription);
            updt.setZainItemCategory(zainItemCategory);
            updt.setSerialized(serialized);
            updt.setQuantity(quantity);
            updt.setUnit(unit);
            updt.setUnitPriceBeforeDiscount(unitPriceBeforeDiscount);
            updt.setUOM(UOM);
            updt.setPoTotalAmtBeforeDiscount(poTotalAmtBeforeDiscount);
            updt.setFinalTotalPriceAfterDiscount(finalTotalPriceAfterDiscount);
            updt.setHuaweiComments(huaweiComments);
            updt.setProcurementComments(procurementComments);
            updt.setDptApprover1(jsonObject.getString("dptApprover1"));
            updt.setDptApprover2(jsonObject.getString("dptApprover2"));
            updt.setDptApprover3(jsonObject.getString("dptApprover3"));
            updt.setDptApprover4(jsonObject.getString("dptApprover4"));
            updt.setRegionalApprover(jsonObject.getString("regionalApprover"));
            updt.setStatus("updated");
            try {
                uprepo.save(updt);

                resultsave = "Record update Success";
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                helper.logToFile(genHeader("N/A", "ErrorUpdatingUPL", "ErrorUpdatingUPL") + "ErrorUpdatingUPL PO ID "
                        + poId + " Serialized " + serialized, "INFO");
            }
        } else {
            upldata nwupdt = new upldata();
            nwupdt.setRecordDatetime(helper.getKenyaDateTimeString());
            nwupdt.setActivePassive(activePassive);
            nwupdt.setCurrency(currency);
            nwupdt.setAmuComments(amuComments);
            nwupdt.setPoId(poId);
            nwupdt.setDiscount(discount);
            nwupdt.setPoId(poId);
            nwupdt.setProjectName(projectName);
            nwupdt.setCustomerItemType(customerItemType);
            nwupdt.setLocalContent(localContent);
            nwupdt.setScope(Scope);
            nwupdt.setSubScope(subScope);
            nwupdt.setPoLine(poLine);
            nwupdt.setUplLine(uplLine);
            nwupdt.setVendorItemCode(vendorItemCode);
            nwupdt.setPoLineItemDescription(poLineItemDescription);
            nwupdt.setErpItemDescription(erpItemDescription);
            nwupdt.setZainItemCategory(zainItemCategory);
            nwupdt.setSerialized(serialized);
            nwupdt.setQuantity(quantity);
            nwupdt.setUnit(unit);
            nwupdt.setUnitPriceBeforeDiscount(unitPriceBeforeDiscount);
            nwupdt.setUOM(UOM);
            nwupdt.setPoTotalAmtBeforeDiscount(poTotalAmtBeforeDiscount);
            nwupdt.setFinalTotalPriceAfterDiscount(finalTotalPriceAfterDiscount);
            nwupdt.setHuaweiComments(huaweiComments);
            nwupdt.setProcurementComments(procurementComments);
            nwupdt.setStatus("created");
            nwupdt.setDptApprover1(jsonObject.getString("dptApprover1"));
            nwupdt.setDptApprover2(jsonObject.getString("dptApprover2"));
            nwupdt.setDptApprover3(jsonObject.getString("dptApprover3"));
            nwupdt.setDptApprover4(jsonObject.getString("dptApprover4"));
            nwupdt.setRegionalApprover(jsonObject.getString("regionalApprover"));
            try {
                uprepo.save(nwupdt);
                resultsave = "Record add Success";
                //HERE CHECK THE APPROVERS 
                //ADD APPROVAL RECORD 
                upldata topRecord = uprepo.findTopByPoNumber(jsonObject.getString("poId"));
                String newstoredrecordId = topRecord != null ? String.valueOf(topRecord.getRecordNo()) : "";

                java.util.Date parsedDate = dateFormat.parse(now.toString());
                java.sql.Date newDate = new java.sql.Date(parsedDate.getTime());
                if (!jsonObject.getString("dptApprover1").equalsIgnoreCase("")) {
                    tb_Approval_Log newApprovalLog = new tb_Approval_Log();
                    newApprovalLog.setRecordDatetime(newDate);
                    newApprovalLog.setApprovalRecordId(Integer.parseInt(newstoredrecordId));
                    newApprovalLog.setRecordType("UPL");
                    newApprovalLog.setPONumber(jsonObject.getString("poId"));
                    newApprovalLog.setStatus("Pending");
                    newApprovalLog.setCreatedBy(jsonObject.getInt("createdBy"));
                    newApprovalLog.setRegion(jsonObject.getString("regionalApprover"));
                    newApprovalLog.setApprover(jsonObject.getString("dptApprover1"));
                    approvalLogRepo.save(newApprovalLog);
                } else if (!jsonObject.getString("dptApprover2").equalsIgnoreCase("")) {
                    tb_Approval_Log newApprovalLog1 = new tb_Approval_Log();
                    newApprovalLog1.setRecordDatetime(newDate);
                    newApprovalLog1.setApprovalRecordId(Integer.parseInt(newstoredrecordId));
                    newApprovalLog1.setRecordType("UPL");
                    newApprovalLog1.setPONumber(jsonObject.getString("poId"));
                    newApprovalLog1.setStatus("Pending");
                    newApprovalLog1.setCreatedBy(jsonObject.getInt("createdBy"));
                    newApprovalLog1.setRegion(jsonObject.getString("regionalApprover"));
                    newApprovalLog1.setApprover(jsonObject.getString("dptApprover2"));
                    approvalLogRepo.save(newApprovalLog1);
                } else if (!jsonObject.getString("dptApprover3").equalsIgnoreCase("")) {
                    tb_Approval_Log newApprovalLog3 = new tb_Approval_Log();
                    newApprovalLog3.setRecordDatetime(newDate);
                    newApprovalLog3.setApprovalRecordId(Integer.parseInt(newstoredrecordId));
                    newApprovalLog3.setRecordType("UPL");
                    newApprovalLog3.setPONumber(jsonObject.getString("poId"));
                    newApprovalLog3.setStatus("Pending");
                    newApprovalLog3.setCreatedBy(jsonObject.getInt("createdBy"));
                    newApprovalLog3.setRegion(jsonObject.getString("regionalApprover"));
                    newApprovalLog3.setApprover(jsonObject.getString("dptApprover3"));
                    approvalLogRepo.save(newApprovalLog3);
                } else if (!jsonObject.getString("dptApprover4").equalsIgnoreCase("")) {
                    tb_Approval_Log newApprovalLog4 = new tb_Approval_Log();
                    newApprovalLog4.setRecordDatetime(newDate);
                    newApprovalLog4.setApprovalRecordId(Integer.parseInt(newstoredrecordId));
                    newApprovalLog4.setRecordType("UPL");
                    newApprovalLog4.setPONumber(jsonObject.getString("poId"));
                    newApprovalLog4.setStatus("Pending");
                    newApprovalLog4.setCreatedBy(jsonObject.getInt("createdBy"));

                    newApprovalLog4.setRegion(jsonObject.getString("regionalApprover"));
                    newApprovalLog4.setApprover(jsonObject.getString("dptApprover4"));
                    approvalLogRepo.save(newApprovalLog4);
                } else if (!jsonObject.getString("regionalApprover").equalsIgnoreCase("")) {
                    tb_Approval_Log newApprovalLog5 = new tb_Approval_Log();
                    newApprovalLog5.setRecordDatetime(newDate);
                    newApprovalLog5.setApprovalRecordId(Integer.parseInt(newstoredrecordId));
                    newApprovalLog5.setRecordType("UPL");
                    newApprovalLog5.setPONumber(jsonObject.getString("poId"));
                    newApprovalLog5.setStatus("Pending");
                    newApprovalLog5.setCreatedBy(jsonObject.getInt("createdBy"));
                    newApprovalLog5.setRegion(jsonObject.getString("regionalApprover"));
                    newApprovalLog5.setApprover(jsonObject.getString("regionalApprover"));
                    approvalLogRepo.save(newApprovalLog5);
                }

            } catch (NumberFormatException | ParseException | JSONException ex) {

                System.out.println(ex.getMessage());
                helper.logToFile(genHeader("N/A", "ErrorAddingUPL", "ErrorAddingUPL") + "ErrorAddingUPL PO ID "
                        + poId + " Serialized " + serialized, "INFO");
            }
        }
        return resultsave;
    }

    @PostMapping(value = "/deleteUpl")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> deleteUpl(@RequestBody String req) throws ParseException, ParseException, ParseException {
        JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
        Integer uplLine = obj.get("uplLine").getAsInt();

        upldata updt = uprepo.findByUplLine(uplLine);
        HashMap<String, String> response = new HashMap<>();

        if (updt != null) {

            updt.setStatus("deleted");

            try {
                uprepo.save(updt);
                response.put("responseCode", "0");
                response.put("responseDesc", "Record Deleted Success");
            } catch (Exception ex) {

                response.put("responseCode", "1");
                response.put("responseDesc", "An error occured while processing your request");
                response.put("error", ex.getMessage());

            }

        }
        return response;

    }

    public String postpoln(String req) {
        String resultcode = "Failed";
        String batchfilename = getbatchfilename("POLN_BATCHUPLOAD");
        helper.logBatchFile(req, true, batchfilename);
        helper.logToFile(genHeader("N/A", "POLN_BATCHUPLOAD", "POLN_BATCHUPLOAD") + "POLN_BATCHUPLOAD file name /home/app/logs/ALM/BatchFiles/" + batchfilename, "INFO");
        JSONArray jsonArrayresponse = new JSONArray();
        long recordNo = 0;
        String poId = "";
        long lineNumber = 0;
        String itemCode = "";
        String UoM = "";
        int orderQuantity = 0;
        BigDecimal unitPrice;
        BigDecimal VAT;
        BigDecimal linePrice;
        try {
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String requesttime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            JSONArray jsonArray = new JSONArray(req);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                recordNo = Integer.parseInt(jsonObject.getString("recordNo"));
                poId = jsonObject.getString("poId");
                lineNumber = jsonObject.getLong("lineNumber");
                itemCode = jsonObject.getString("itemCode");
                UoM = jsonObject.getString("UoM");
                orderQuantity = jsonObject.getInt("orderQuantity");
                unitPrice = jsonObject.getBigDecimal("unitPrice");
                VAT = jsonObject.getBigDecimal("VAT");
                linePrice = jsonObject.getBigDecimal("linePrice");
                String jsresp = AddEditPOLN(jsonObject, recordNo, helper.getKenyaDateTimeString(), poId, lineNumber, itemCode, UoM, orderQuantity, unitPrice, VAT, linePrice);
                if (jsresp.contains("Success")) {
                    resultcode = ("Complete");
                } else {
                    net.minidev.json.JSONObject responsedata = new net.minidev.json.JSONObject();
                    responsedata.put("itemCode", itemCode);
                    responsedata.put("poId", poId);
                    responsedata.put("DBresponse", jsresp);
                    jsonArrayresponse.put(responsedata.toJSONString());
                }
            }
            if (jsonArrayresponse.length() > 0) {
                batchfilename = getbatchfilename("FailedUpload");
                helper.logBatchFile(jsonArrayresponse.toString(), true, batchfilename);
                resultcode = (jsonArrayresponse.toString());
            } else {
                resultcode = ("Complete");
            }
        } catch (Exception exc) {
            net.minidev.json.JSONObject response = new net.minidev.json.JSONObject();
            response.put("poId", poId);
            resultcode = (jsonArrayresponse.toString());
        }
        return resultcode;
    }

    //Approve PO
    @PostMapping(value = "/approvepo")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> approvepo(@RequestBody String req) {
        String batchfilename = getbatchfilename("APPROVEPOHD_BATCHUPLOAD");
        helper.logBatchFile(req, true, batchfilename);
        helper.logToFile(genHeader("N/A", "APPROVEPOHD_BATCHUPLOAD", "APPROVEPOHD_BATCHUPLOAD") + "APPROVEPOHD_BATCHUPLOAD file name /home/app/logs/ALM/BatchFiles/" + batchfilename, "INFO");
        JSONArray jsonArrayresponse = new JSONArray();
        long recordNo = 0;
        String poId = "";
        String approvedBy = "";
        String approvalDate = "";
        String status = "";
        boolean approved = false;
        boolean delivered = false;

        try {
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String requesttime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            JSONArray jsonArray = new JSONArray(req);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                //recordNo =Integer.parseInt( jsonObject.getString("recordNo"));
                poId = jsonObject.getString("poId");
                approvedBy = jsonObject.getString("approvedBy");
                approvalDate = jsonObject.getString("approvalDate");
                status = jsonObject.getString("status");
                approved = jsonObject.getBoolean("approved");
                delivered = jsonObject.getBoolean("delivered");
                //String jsresp = MasterDataController.poapprove(  poId,approvedBy, approvalDate, status, approved, delivered);
                net.minidev.json.JSONObject responsedata = new net.minidev.json.JSONObject();
                responsedata.put("poId", poId);
                responsedata.put("status", status);
                responsedata.put("DBresponse", "");
                jsonArrayresponse.put(responsedata.toJSONString());
            }
            if (jsonArrayresponse.length() > 0) {
                batchfilename = getbatchfilename("FailedUpload");
                helper.logBatchFile(jsonArrayresponse.toString(), true, batchfilename);
                return response("Error", jsonArrayresponse.toString());
            } else {
                return response("Success", "Complete");
            }
        } catch (Exception exc) {
            net.minidev.json.JSONObject response = new net.minidev.json.JSONObject();
            response.put("poId", poId);
            response.put("status", status);
            return response("Error", jsonArrayresponse.toString());
        }
    }

    @PostMapping(value = "/fetchpodata")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> fetchpodata(@RequestBody String req) {
        helper.logToFile(genHeader("N/A", "FetchPOData", "API") + "FetchPOData Request Payload" + req, "INFO");
        JSONArray jsonArrayresponse = new JSONArray();
        long recordNo = 0;
        String poId = "";
        String supplierId = "";
        try {
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String requesttime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            JsonObject jsonObject = new JsonParser().parse(req).getAsJsonObject();
            recordNo = Integer.parseInt(jsonObject.get("recordNo").getAsString());
            poId = jsonObject.get("poId").getAsString();
            supplierId = jsonObject.get("supplierId").getAsString();
            List<pohddata> polist = pohd_repo.findByPoIdAndSupplierId(poId, supplierId);
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(polist);
            return response("Success", json);

        } catch (JsonProcessingException | JsonSyntaxException | NumberFormatException exc) {
            net.minidev.json.JSONObject response = new net.minidev.json.JSONObject();
            response.put("poId", poId);
            return response("Error", exc.toString());
        }
    }

}
