package com.zain.bh.alm.acceptance.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.zain.bh.alm.acceptance.entity.DCC;
import com.zain.bh.alm.acceptance.entity.DCCLineItem;
import com.zain.bh.alm.acceptance.entity.FileRecord;
import com.zain.bh.alm.acceptance.entity.Scope;
import com.zain.bh.alm.acceptance.entity.PurchaseOrderUPL;
import com.zain.bh.alm.acceptance.entity.Region;
import com.zain.bh.alm.acceptance.entity.Site;
import com.zain.bh.alm.acceptance.repository.DCCRepository;
import com.zain.bh.alm.acceptance.repository.DCCLineItemRepository;
import com.zain.bh.alm.acceptance.repository.FileRecordRepository;
import com.zain.bh.alm.acceptance.repository.ArcApprovalRecordsRepository;
import com.zain.bh.alm.acceptance.repository.ItemCodeSubstituteRepository;
import com.zain.bh.alm.acceptance.repository.NodeRepository;
import com.zain.bh.alm.acceptance.repository.PurchaseOrderRepository;
import com.zain.bh.alm.acceptance.repository.PurchaseOrderUPLRepository;
import com.zain.bh.alm.acceptance.repository.RegionRepository;
import com.zain.bh.alm.acceptance.repository.ScopeApprovalLevelRepository;
import com.zain.bh.alm.acceptance.repository.ScopeRepository;
import com.zain.bh.alm.acceptance.repository.SerialNumberRepository;
import com.zain.bh.alm.acceptance.repository.SiteRepository;
import com.zain.bh.alm.acceptance.service.DCCManagementService;
import com.zain.bh.alm.acceptance.util.Httpcall;

@Service
public class DCCManagementServiceImpl implements DCCManagementService {

    private static final Logger LOGGER = LogManager.getLogger(DCCManagementServiceImpl.class);

    private final DCCRepository dccRepo;
    private final DCCLineItemRepository dccLineRepo;
    private final PurchaseOrderUPLRepository purchaseOrderUPLRepo;
    private final PurchaseOrderRepository purchaseOrderRepo;
    private final ItemCodeSubstituteRepository itemCodeSubstituteRepo;
    private final SerialNumberRepository serialNumberRepo;
    private final ScopeRepository scopeRepo;
    private final ScopeApprovalLevelRepository scopeApprovalLevelsRepo;
    private final NodeRepository nodeRepo;
    private final FileRecordRepository fileRepo;
    private final SiteRepository siteRepo;
    private final RegionRepository regionRepo;
    private final ArcApprovalRecordsRepository arcApprovalRecordsRepo;
    private final Httpcall utils;

    public DCCManagementServiceImpl(DCCRepository dccRepo, DCCLineItemRepository dccLineRepo,
                                    PurchaseOrderUPLRepository purchaseOrderUPLRepo, PurchaseOrderRepository purchaseOrderRepo,
                                    ItemCodeSubstituteRepository itemCodeSubstituteRepo, SerialNumberRepository serialNumberRepo,
                                    ScopeRepository scopeRepo, ScopeApprovalLevelRepository scopeApprovalLevelsRepo,
                                    NodeRepository nodeRepo, FileRecordRepository fileRepo, SiteRepository siteRepo,
                                    RegionRepository regionRepo, ArcApprovalRecordsRepository arcApprovalRecordsRepo,
                                    Httpcall utils) {
        this.dccRepo = dccRepo;
        this.dccLineRepo = dccLineRepo;
        this.purchaseOrderUPLRepo = purchaseOrderUPLRepo;
        this.purchaseOrderRepo = purchaseOrderRepo;
        this.itemCodeSubstituteRepo = itemCodeSubstituteRepo;
        this.serialNumberRepo = serialNumberRepo;
        this.scopeRepo = scopeRepo;
        this.scopeApprovalLevelsRepo = scopeApprovalLevelsRepo;
        this.nodeRepo = nodeRepo;
        this.fileRepo = fileRepo;
        this.siteRepo = siteRepo;
        this.regionRepo = regionRepo;
        this.arcApprovalRecordsRepo = arcApprovalRecordsRepo;
        this.utils = utils;
    }

    @Override
    public Map<String, Object> processFromJson(String jsonRequest, List<MultipartFile> files) throws Exception {
        LOGGER.info("Processing DCC request");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        JSONArray jsonArray = new JSONArray(jsonRequest);
        String poNumber = jsonArray.getJSONObject(0).getString("poNumber");
        
        // Validate files
        String fileError = validateFiles(files);
        if (fileError != null) {
            return createErrorResult(fileError);
        }

        // Validate DCC data
        Map<String, List<String>> validationResult = validateDCCData(jsonArray, poNumber, dateFormat);
        String validationError = checkValidationErrors(validationResult);
        if (validationError != null) {
            return createErrorResult(validationError);
        }

        // Process DCC records
        JSONArray jsonArrayresponse = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            long recordNo = Long.parseLong(jsonObject.getString("recordNo"));
            String status = jsonObject.getString("status");
            Integer createdBy = jsonObject.getInt("createdById");
            String createdByName = jsonObject.getString("createdByName");
            String vendorName = jsonObject.getString("vendorName");
            
            String result = addEditDCC(recordNo, jsonObject, dateFormat);
            DCC topRecord = dccRepo.findTopByPoNumber(poNumber);
            String recordId = topRecord != null ? String.valueOf(topRecord.getRecordNo()) : "";
            
            DCC checkdcc = dccRepo.findByRecordNo(recordNo);
            String newRecordNo = checkdcc != null ? String.valueOf(recordNo) : recordId;

            // Handle file uploads
            if (files != null) {
                String uploadError = uploadFiles(files, poNumber, newRecordNo);
                if (uploadError != null) {
                    return createErrorResult(uploadError);
                }
            }

            if (result.contains("Success")) {
                JSONArray dcc_line_data = jsonObject.getJSONArray("lineItems");
                postdccln(poNumber, newRecordNo, status, createdBy, createdByName, vendorName, dcc_line_data.toString());
            } else {
                JSONObject responsedata = new JSONObject();
                responsedata.put("recordNo", recordNo);
                responsedata.put("DBresponse", result);
                jsonArrayresponse.put(responsedata);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", jsonArrayresponse.length() == 0);
        result.put("errors", jsonArrayresponse.toString());
        return result;
    }

    private String validateFiles(List<MultipartFile> files) {
        if (files == null) return null;
        
        List<String> allowedExtensions = Arrays.asList(".pdf", ".docx", ".xlsx", ".jpg", ".png");
        String uploadDir = "/home/app/logs/ALM/POUPL/";
        
        for (MultipartFile file : files) {
            String originalFileName = file.getOriginalFilename();
            if (originalFileName != null) {
                int dotIndex = originalFileName.lastIndexOf('.');
                if (dotIndex > 0) {
                    String fileExtension = originalFileName.substring(dotIndex).toLowerCase();
                    if (!allowedExtensions.contains(fileExtension)) {
                        return "Invalid file type: " + fileExtension;
                    }
                }
                String newFileName = originalFileName + "_" + System.currentTimeMillis() + originalFileName.substring(dotIndex);
                File destinationFile = new File(uploadDir + newFileName);
                if (destinationFile.exists()) {
                    return "File already exists: " + newFileName;
                }
            }
        }
        return null;
    }

    private Map<String, List<String>> validateDCCData(JSONArray jsonArray, String poNumber, SimpleDateFormat dateFormat) throws Exception {
        Map<String, List<String>> errors = new HashMap<>();
        errors.put("alreadyCreatedDCC", new ArrayList<>());
        errors.put("missingApprovalLevel", new ArrayList<>());
        errors.put("existingSerialNumbers", new ArrayList<>());
        errors.put("oldDateService", new ArrayList<>());
        errors.put("ScopeOfworkList", new ArrayList<>());
        errors.put("locationList", new ArrayList<>());
        errors.put("validateInventory", new ArrayList<>());
        errors.put("missingItemCode", new ArrayList<>());

        List<Integer> recordNumbers = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject validatejsonObject = jsonArray.getJSONObject(i);
            long recordNoValidate = Long.parseLong(validatejsonObject.getString("recordNo"));
            JSONArray dcclineRequest = validatejsonObject.getJSONArray("lineItems");

            for (int j = 0; j < dcclineRequest.length(); j++) {
                JSONObject dcclinejsonObject = dcclineRequest.getJSONObject(j);
                
                if (recordNoValidate != 0) {
                    recordNumbers.add(Integer.parseInt(dcclinejsonObject.getString("recordNo")));
                }

                String actualItemCode = dcclinejsonObject.optString("actualItemCode", "");
                String itemCode = actualItemCode.length() > 1 ? actualItemCode : dcclinejsonObject.getString("itemCode");
                
                if (actualItemCode.length() > 1 && itemCodeSubstituteRepo.findByRelatedItemCode(actualItemCode).isEmpty()) {
                    errors.get("missingItemCode").add(actualItemCode);
                }

                String serialNumber = dcclinejsonObject.getString("serialNumber");
                String upllineitem = dcclinejsonObject.getString("uplLineNumber");
                String polineitem = dcclinejsonObject.getString("poLineNumber");
                String scopeofWork = dcclinejsonObject.getString("scopeOfWork");
                String localName = dcclinejsonObject.getString("locationName");

                if (scopeofWork.length() > 1) errors.get("ScopeOfworkList").add(scopeofWork);
                if (localName.length() > 1) errors.get("locationList").add(localName);

                // Validate date in service
                String dateInServiceString = dcclinejsonObject.getString("dateInService");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                LocalDateTime dateInService = LocalDateTime.parse(dateInServiceString, formatter);
                if (dateInService.isBefore(LocalDateTime.now().minus(6, ChronoUnit.MONTHS))) {
                    errors.get("oldDateService").add(upllineitem);
                }

                // Validate serial numbers
                if (serialNumber.length() > 1 && itemCode.length() > 1) {
                    if (!serialNumberRepo.findBySerialNumberAndItemCode(serialNumber, itemCode).isEmpty()) {
                        errors.get("existingSerialNumbers").add(serialNumber);
                    }
                }

                // Validate scope approval levels
                Scope scopeRecord = scopeRepo.findByScope(scopeofWork.trim());
                if (scopeRecord != null) {
                    if (scopeApprovalLevelsRepo.findByScope((int) scopeRecord.getRecordNo()).isEmpty()) {
                        errors.get("missingApprovalLevel").add(scopeofWork);
                    }
                }

                // Validate inventory for serialized items
                if (upllineitem.length() > 1) {
                    PurchaseOrderUPL topRecord = purchaseOrderUPLRepo.findTopByPoNumberAndPoLineNumberAndUplLine(poNumber, polineitem, upllineitem);
                    if (topRecord != null && "Yes".equalsIgnoreCase(topRecord.getUplItemSerialized()) && 
                        "Active".equalsIgnoreCase(topRecord.getActiveOrPassive())) {
                        if (serialNumber.length() > 1 && itemCode.length() > 1) {
                            if (nodeRepo.findByPartNumberAndSerialNumber(itemCode, serialNumber).isEmpty()) {
                                errors.get("validateInventory").add(serialNumber);
                            }
                        }
                    }
                }

                // Check for already created DCC
                if (serialNumber.length() > 1 && itemCode.length() > 1) {
                    List<DCCLineItem> validateDCCLineList = dccLineRepo.findBySerialNumberAndItemCode(serialNumber, itemCode);
                    if (!validateDCCLineList.isEmpty()) {
                        DCCLineItem topRecordNo = dccLineRepo.findTopBySerialNumberAndItemCode(serialNumber, itemCode);
                        if (topRecordNo != null) {
                            DCC dccRecord = dccRepo.findByRecordNo(Integer.parseInt(String.valueOf(topRecordNo.getDccId())));
                            if (dccRecord != null) {
                                String dccStatus = dccRecord.getStatus();
                                if (Arrays.asList("inprocess", "approved", "returned", "request-info").contains(dccStatus.toLowerCase())) {
                                    errors.get("alreadyCreatedDCC").add(serialNumber);
                                }
                            }
                        }
                    }
                }
            }

            // Delete orphaned line items for updates
            if (recordNoValidate != 0) {
                List<DCCLineItem> dccLineItems = dccLineRepo.findAllByDccId(String.valueOf(recordNoValidate));
                List<Long> toDelete = dccLineItems.stream()
                        .map(DCCLineItem::getRecordNo)
                        .filter(recordNoNew -> !recordNumbers.contains(recordNoNew.intValue()))
                        .collect(Collectors.toList());
                if (!toDelete.isEmpty()) {
                    dccLineRepo.deleteAllByIdInBatch(toDelete);
                }
            }
        }

        return errors;
    }

    private String checkValidationErrors(Map<String, List<String>> errors) {
        Set<String> uniqueScope = new HashSet<>(errors.get("ScopeOfworkList"));
        if (uniqueScope.size() > 1) {
            return "You can only raise an acceptance request for one unique scope of work. The passed scopes are " + String.join(", ", uniqueScope);
        }

        Set<String> uniquelocation = new HashSet<>(errors.get("locationList"));
        if (uniquelocation.size() > 1) {
            return "You can only raise an acceptance request for one unique location. The passed locations are " + String.join(", ", uniquelocation);
        }

        if (!errors.get("missingItemCode").isEmpty()) {
            return "Actual Item Codes " + String.join(", ", errors.get("missingItemCode")) + " are not valid. Please use existing related item codes";
        }

        if (!errors.get("validateInventory").isEmpty()) {
            return "An acceptance request cannot be raised for serial number " + String.join(", ", errors.get("validateInventory")) + " since they are missing in the active inventory";
        }

        if (!errors.get("alreadyCreatedDCC").isEmpty()) {
            return "Acceptance request for serial numbers " + String.join(", ", errors.get("alreadyCreatedDCC")) + " has already been raised. Duplicates records not allowed";
        }

        if (!errors.get("missingApprovalLevel").isEmpty()) {
            return "Missing Approval Level Configurations for Scope(s): " + String.join(", ", errors.get("missingApprovalLevel")) + " Please contact the OPCO.";
        }

        if (!errors.get("oldDateService").isEmpty()) {
            return "The dateInService for Upl Line Items(s) " + errors.get("oldDateService") + " is more than 6 months old. Validation failed.";
        }

        if (!errors.get("existingSerialNumbers").isEmpty()) {
            return "The following serial Numbers " + String.join(", ", errors.get("existingSerialNumbers")) + " have already been received. Kindly raise an acceptance request for a different serial Number";
        }

        return null;
    }

    private String uploadFiles(List<MultipartFile> files, String poNumber, String recordNo) {
        String uploadDir = "/home/app/logs/ALM/POUPL/";
        for (MultipartFile file : files) {
            String originalFileName = file.getOriginalFilename();
            if (originalFileName != null) {
                int dotIndex = originalFileName.lastIndexOf('.');
                String fileExtension = dotIndex > 0 ? originalFileName.substring(dotIndex) : "";
                String newFileName = originalFileName + "_" + System.currentTimeMillis() + fileExtension;
                File destinationFile = new File(uploadDir + newFileName);
                
                try {
                    Files.copy(file.getInputStream(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    FileRecord fileRecord = new FileRecord();
                    fileRecord.setFileName(newFileName);
                    fileRecord.setPoNumber(poNumber);
                    fileRecord.setFilePath(destinationFile.getAbsolutePath());
                    fileRecord.setDccId(Integer.parseInt(recordNo));
                    fileRepo.save(fileRecord);
                } catch (Exception e) {
                    return "File upload failed: " + e.getMessage();
                }
            }
        }
        return null;
    }

    private String addEditDCC(long recordno, JSONObject jsonObject, SimpleDateFormat dateFormat) {
        try {
            DCC checkdcc = dccRepo.findByRecordNo(recordno);
            if (checkdcc != null) {
                checkdcc.setCreatedBy(jsonObject.getString("createdByName"));
                checkdcc.setPoNumber(jsonObject.getString("poNumber"));
                checkdcc.setProjectName(jsonObject.getString("projectName"));
                checkdcc.setAcceptanceType(jsonObject.getString("acceptanceType"));
                checkdcc.setStatus(jsonObject.getString("status").equalsIgnoreCase("request-info") ? "inprocess" : jsonObject.getString("status"));
                if (jsonObject.has("vendorComment")) {
                    checkdcc.setVendorComment(jsonObject.getString("vendorComment"));
                }
                checkdcc.setVendorName(jsonObject.getString("vendorName"));
                checkdcc.setVendorNumber(jsonObject.getString("vendorNumber"));
                dccRepo.save(checkdcc);
                return "Data updated Success";
            } else {
                DCC nwcheckdcc = new DCC();
                nwcheckdcc.setCreatedBy(jsonObject.getString("createdByName"));
                nwcheckdcc.setPoNumber(jsonObject.getString("poNumber"));
                nwcheckdcc.setProjectName(jsonObject.getString("projectName"));
                nwcheckdcc.setAcceptanceType(jsonObject.getString("acceptanceType"));
                nwcheckdcc.setStatus(jsonObject.getString("status"));
                nwcheckdcc.setVendorName(jsonObject.getString("vendorName"));
                nwcheckdcc.setVendorNumber(jsonObject.getString("vendorNumber"));
                
                LocalDateTime now = LocalDateTime.now();
                ZoneId eatZone = ZoneId.of("Africa/Nairobi");
                ZonedDateTime eatZonedDateTime = now.atZone(ZoneId.systemDefault()).withZoneSameInstant(eatZone);
                Timestamp timestamp = Timestamp.valueOf(eatZonedDateTime.toLocalDateTime());
                nwcheckdcc.setCreatedDate(timestamp);
                
                dccRepo.save(nwcheckdcc);
                return "Data updated Success";
            }
        } catch (Exception excc) {
            LOGGER.error("Error in addEditDCC", excc);
            return "Error " + excc.getMessage();
        }
    }

    private void postdccln(String poNumber, String recordId, String status, Integer createdBy, String createdByName, String vendorName, String req) {
        try {
            JSONArray jsonArray = new JSONArray(req);
            List<String> ItemCategoryCodes = new ArrayList<>();
            String locationName = "";

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                long recordNo = Long.parseLong(jsonObject.getString("recordNo"));
                locationName = jsonObject.getString("locationName");
                String scopeOfWork = jsonObject.getString("scopeOfWork");
                
                if (scopeOfWork.length() > 1) {
                    ItemCategoryCodes.add(scopeOfWork);
                }

                addDCCLineItem(poNumber, recordNo, recordId, jsonObject, new SimpleDateFormat("yyyy-MM-dd"));
            }

            // Initiate workflow
            Site topRecord = siteRepo.findFirstBySiteId(locationName);
            Integer regionrecordId = topRecord != null ? topRecord.getRegionId() : null;
            Region regionRecord = regionRepo.findByRecordNo(regionrecordId);

            Set<String> uniqueItemCategoryCodes = new LinkedHashSet<>(ItemCategoryCodes);
            JSONArray jsonArraynew = new JSONArray();

            for (String newitemCategoryCode : uniqueItemCategoryCodes) {
                JSONObject params = new JSONObject();
                params.put("acceptanceRequestRecordNo", recordId);
                params.put("tableName", "tb_DCC");
                params.put("poNumber", poNumber);
                params.put("scope", newitemCategoryCode);
                params.put("requestedBy", createdByName);
                params.put("vendorName", vendorName);
                params.put("createdBy", createdBy.toString());
                params.put("regions", regionRecord.getRegionName());
                params.put("status", status.equalsIgnoreCase("request-info") ? "request-info" : "");
                jsonArraynew.put(params);
            }

            String ipaddress = getIPAddress();
            if (!status.equalsIgnoreCase("incomplete")) {
                CompletableFuture.runAsync(() -> {
                    try {
                        utils.httpPOST(jsonArraynew.toString(), "http://" + ipaddress + ":8080/alm-zain-ksa/workflow/initialize-approval");
                    } catch (Exception ex) {
                        LOGGER.error("Error in workflow initialization", ex);
                    }
                });
            }
        } catch (Exception exc) {
            LOGGER.error("Error processing DCC line items", exc);
        }
    }

    private void addDCCLineItem(String poNumber, long recordno, String recordId, JSONObject jsonObject, SimpleDateFormat dateFormat) throws Exception {
        DCCLineItem eddccLineItem = dccLineRepo.findByRecordNo(recordno);
        if (eddccLineItem != null) {
            setDCCLineItemFields(eddccLineItem, poNumber, jsonObject, dateFormat);
            dccLineRepo.save(eddccLineItem);
        } else {
            DCCLineItem dccLineItem = new DCCLineItem();
            dccLineItem.setDccId(recordId);
            setDCCLineItemFields(dccLineItem, poNumber, jsonObject, dateFormat);
            dccLineRepo.save(dccLineItem);
        }
    }

    private void setDCCLineItemFields(DCCLineItem item, String poNumber, JSONObject json, SimpleDateFormat dateFormat) throws Exception {
        item.setLineNumber(json.getString("poLineNumber"));
        item.setUplLineNumber(json.getString("uplLineNumber"));
        item.setItemCode(json.getString("itemCode"));
        item.setSerialNumber(json.getString("serialNumber"));
        item.setDeliveredQty(Double.parseDouble(json.getString("deliveredQty")));
        if (json.has("actualItemCode")) {
            item.setActualItemCode(json.getString("actualItemCode"));
        }
        item.setPoId(poNumber);
        item.setLocationName(json.getString("locationName"));
        item.setDateInService(new Date(dateFormat.parse(json.getString("dateInService")).getTime()));
        if (json.has("uplLineItemCode")) {
            item.setUplItemCode(json.getString("uplLineItemCode"));
        }
        if (json.has("uplLineDescription")) {
            item.setUplItemDescription(json.getString("uplLineDescription"));
        }
        item.setScopeOfWork(json.getString("scopeOfWork"));
        item.setRemarks(json.getString("remarks"));
        item.setLinkId(json.getString("linkId"));
        item.setTagNumber(json.getString("tagNumber"));
    }

    private String getIPAddress() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            return "localhost";
        }
    }

    private Map<String, Object> createErrorResult(String error) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("errors", error);
        return result;
    }
}
