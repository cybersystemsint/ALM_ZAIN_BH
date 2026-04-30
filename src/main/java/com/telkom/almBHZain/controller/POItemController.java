package com.telkom.almBHZain.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.exception.ConstraintViolationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telkom.almBHZain.dto.BulkDeleteRequest;
import com.telkom.almBHZain.dto.POItemDto;
import com.telkom.almBHZain.dto.Request.FilterOperator;
import com.telkom.almBHZain.dto.Request.FilterRequest;
import com.telkom.almBHZain.dto.Request.SearchRequest;
import com.telkom.almBHZain.dto.Response.PageResult;
import com.telkom.almBHZain.model.POItem;
import com.telkom.almBHZain.model.PurchaseOrder;
import com.telkom.almBHZain.model.Workflow;
import com.telkom.almBHZain.model.tb_Po_Modification;
import com.telkom.almBHZain.repository.POItemRepository;
import com.telkom.almBHZain.repository.PurchaseOrderRepository;
import com.telkom.almBHZain.repository.WorkflowRepository;
import com.telkom.almBHZain.repository.tb_Po_ModificationRepository;
import com.telkom.almBHZain.response.BulkPoItemResult;
import com.telkom.almBHZain.service.POItemService;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public class POItemController {

    private static final Logger logger = LoggerFactory.getLogger(POItemController.class);

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private POItemRepository poRepo;
    @Autowired
    private PurchaseOrderRepository poNumberRepo;
    @Autowired
    private WorkflowRepository workflowRepository;
    @Autowired
    private tb_Po_ModificationRepository poModificationRepo;
    @Autowired
    private POItemService service;

    private static final String WF_PENDING_ADDITION = "Pending POItem Addition";
    private static final String WF_PENDING_MODIFICATION = "Pending POItem Modification";
    private static final String WF_PENDING_DELETION = "Pending POItem Deletion";
    private static final String WF_UNKNOWN_STATUS = "Unknown";

    @PostMapping(value = "/po-items", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.ALL_VALUE)
    public PageResult<POItemDto> fetchPOItems(
            @RequestBody(required = false) String rawBody,
            @RequestParam(value = "poNumber", required = false) String poNumberParam, // optional fallback
            @RequestParam(value = "searchQuery", required = false) String searchQueryParam,
            @RequestParam(value = "searchColumn", required = false) String searchColumnParam,
            @RequestParam(value = "page", required = false) Integer pageParam,
            @RequestParam(value = "size", required = false) Integer sizeParam,
            HttpServletRequest servletRequest) {

        SearchRequest parsed = parseRequestBodyIfJson(rawBody, servletRequest);
        SearchRequest request = (parsed != null) ? parsed : new SearchRequest();

        // prefer values already present in request (body); otherwise use query params
        if ((request.getSearchQuery() == null || request.getSearchQuery().trim().isEmpty())
                && searchQueryParam != null && !searchQueryParam.trim().isEmpty()) {
            request.setSearchQuery(searchQueryParam);
        }
        if ((request.getSearchColumn() == null || request.getSearchColumn().trim().isEmpty())
                && searchColumnParam != null && !searchColumnParam.trim().isEmpty()) {
            request.setSearchColumn(searchColumnParam);
        }
        if (request.getPage() == null && pageParam != null) request.setPage(pageParam);
        if (request.getSize() == null && sizeParam != null) request.setSize(sizeParam);

        // prefer poNumber in body; if absent fall back to query param for compatibility
        String effectivePoNumber = (request.getPoNumber() != null && !request.getPoNumber().trim().isEmpty())
                ? request.getPoNumber().trim()
                : (poNumberParam != null && !poNumberParam.trim().isEmpty() ? poNumberParam.trim() : null);

        injectPoNumberFilterIfNeeded(effectivePoNumber, request);
        return service.getPOItems(request);
    }

    private String generateProcessId() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomDigit = String.valueOf((int) (Math.random() * 10));
        // last 6 digits + random
        return timestamp.substring(Math.max(0, timestamp.length() - 6)) + randomDigit;
    }

    // Helper: detect any pending approval status
    private boolean isPendingStatus(String status) {
        return status != null && status.trim().toLowerCase().startsWith("pending");
    }

    // Centralized error helper that logs full details and returns a sanitized {status, message}
    private Map<String, String> buildErrorResponse(String userMessage, Exception ex, Object... context) {
        String errorId = UUID.randomUUID().toString().substring(0, 8); // short id
        try {
            logger.error("ErrorId {}: {} | Context: {} ", errorId, userMessage, Arrays.toString(context), ex);
        } catch (Exception logEx) {
            logger.error("Error while logging error ({}): {}", errorId, logEx.getMessage());
        }

        String userFacingMessage = userMessage;

        if (ex instanceof DataIntegrityViolationException) {
            // Try to detect constraint if available
            Throwable cause = ex.getCause();
            String constraint = null;
            if (cause instanceof ConstraintViolationException) {
                constraint = ((ConstraintViolationException) cause).getConstraintName();
            }
            if (constraint != null && !constraint.trim().isEmpty()) {
                userFacingMessage = userMessage + " — database constraint violation: " + constraint;
            } else {
                userFacingMessage = userMessage + " — database error.";
            }
            userFacingMessage += " (errorId: " + errorId + ")";
        } else if (ex instanceof ConstraintViolationException) {
            String constraint = ((ConstraintViolationException) ex).getConstraintName();
            userFacingMessage = userMessage + " — database constraint violation" + (constraint != null ? ": " + constraint : "") + " (errorId: " + errorId + ")";
        } else if (ex instanceof ParseException) {
            userFacingMessage = "Invalid date format. Expected yyyy-MM-dd. (errorId: " + errorId + ")";
        } else if (ex instanceof JSONException) {
            userFacingMessage = "Invalid JSON input: " + ex.getMessage();
        } else if (ex instanceof IllegalArgumentException) {
            // likely already user-friendly (e.g., missing field)
            userFacingMessage = ex.getMessage();
        } else {
            userFacingMessage = userMessage + " — unexpected error (errorId: " + errorId + ")";
        }

        Map<String, String> resp = new HashMap<>();
        resp.put("status", "Error");
        resp.put("message", userFacingMessage);
        return resp;
    }

    @PostMapping(value = "/poItems", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public ResponseEntity<Map<String, String>> addSinglePoItem(@RequestBody String req) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            JSONObject jsonObject = new JSONObject(req);
            String poNumber = jsonObject.getString("poNumber").trim();
            // using optInt to avoid exception when missing; keep default 0 if not provided
            int poLine = jsonObject.optInt("poLine", 0);

            // normalize model (store N/A for null/empty)
            String modelNumber = normalizeModel(jsonObject.optString("modelNumber", null));

            // Check if PO number exists
            PurchaseOrder existsPoNumber = poNumberRepo.findByPoNumber(poNumber);
            if (existsPoNumber == null) {
                return response("Error", "PO Number " + poNumber + " does not exist, provide an existing PO Number");
            }

            // Prevent adding POItem if parent PO is pending
            String poApprovalStatus = null;
            try { poApprovalStatus = existsPoNumber.getApprovalStatus(); } catch (Exception ignore) {}
            if (isPendingStatus(poApprovalStatus)) {
                return response("Error", "Cannot add PO item because Purchase Order " + poNumber + " is in pending state: " + poApprovalStatus);
            }

            // Uniqueness: only enforce when model is provided and not "N/A"
            POItem existing = null;
            if (!"N/A".equalsIgnoreCase(modelNumber)) {
                existing = poRepo.findByPoNumberAndModelNumber(poNumber, modelNumber);
            }

            if (existing != null) {
                String status = existing.getApprovalStatus();
                if (isPendingStatus(status)) {
                    return response("Error", "PO item already exists for poNumber " + poNumber + " and model " + modelNumber + " and is currently in pending state: " + status + ".");
                } else {
                    return response("Error", "PO item already exists for poNumber " + poNumber + " and model " + modelNumber + ".");
                }
            }

            // Create and save new PO item (mapToPo normalizes model to "N/A")
            POItem newPoItem = mapToPo(jsonObject, dateFormat);
            poRepo.save(newPoItem);

            // Create workflow entry with insertedBy from payload/entity
            String insertedBy = newPoItem.getCreatedBy() != null && !newPoItem.getCreatedBy().trim().isEmpty()
                    ? newPoItem.getCreatedBy() : "System";
            workflowRepository.save(buildWorkflow(newPoItem.getPoNumber(), newPoItem.getRecordNo(), WF_PENDING_ADDITION, insertedBy));

            return response("Success", "PO Item for PO Number " + poNumber + ", Model " + newPoItem.getModelNumber() + " added successfully.");
        } catch (JSONException e) {
            Map<String, String> err = buildErrorResponse("Invalid JSON input", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        } catch (ParseException e) {
            Map<String, String> err = buildErrorResponse("Invalid date format in payload", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        } catch (Exception e) {
            Map<String, String> err = buildErrorResponse("Unexpected error while adding PO item", e, "payload", req);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    // bulk add or modify poitems
    @PostMapping(value = "/poItems/bulk", produces = "application/json")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public ResponseEntity<BulkPoItemResult> addOrUpdatePoItems(@RequestBody String req) {
        BulkPoItemResult result = new BulkPoItemResult();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            JSONArray jsonArray = new JSONArray(req);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                try {
                    String poNumber = jsonObject.getString("poNumber").trim();
                    int poLine = jsonObject.optInt("poLine", 0);

                    // normalize model and decide existing based on poNumber + model (only when model != N/A)
                    String modelNumber = normalizeModel(jsonObject.optString("modelNumber", null));
                    POItem existing = null;
                    if (!"N/A".equalsIgnoreCase(modelNumber)) {
                        existing = poRepo.findByPoNumberAndModelNumber(poNumber, modelNumber);
                    }

                    if (existing != null) {
                        // Cannot modify if POItem already pending
                        String status = existing.getApprovalStatus();
                        if (isPendingStatus(status)) {
                            result.getModificationErrors().add("PO Number " + poNumber + " and Model " + modelNumber + " is already in pending state: " + status + ".");
                            continue;
                        }

                        // Create mod request against the found record
                        tb_Po_Modification mod = mapToModification(existing.getRecordNo(), jsonObject);
                        poModificationRepo.save(mod);

                        // mark main record pending and record who requested
                        existing.setApprovalStatus("Pending Modification");
                        if (mod.getUpdatedBy() != null && !mod.getUpdatedBy().trim().isEmpty()) {
                            existing.setUpdatedBy(mod.getUpdatedBy()); // reflect who requested modification
                        }
                        poRepo.save(existing);

                        // Prefer updatedBy for workflow insertedBy, else createdBy, else System
                        String insertedBy = (mod.getUpdatedBy() != null && !mod.getUpdatedBy().trim().isEmpty())
                                ? mod.getUpdatedBy()
                                : (mod.getCreatedBy() != null && !mod.getCreatedBy().trim().isEmpty() ? mod.getCreatedBy() : "System");

                        workflowRepository.save(buildWorkflow(existing.getPoNumber(), existing.getRecordNo(), WF_PENDING_MODIFICATION, insertedBy));
                        result.getModifiedRows().add(mod);
                    } else {
                        // Addition path: first check parent PO exists
                        PurchaseOrder existsPoNumber = poNumberRepo.findByPoNumber(poNumber);
                        if (existsPoNumber == null) {
                            result.getAdditionErrors().add("PO Number " + poNumber + " does not exist, Provide an existing PO Number ");
                            continue;
                        }

                        // Prevent adding POItem if parent PO is pending
                        String poApprovalStatus = null;
                        try { poApprovalStatus = existsPoNumber.getApprovalStatus(); } catch (Exception ignore) {}
                        if (isPendingStatus(poApprovalStatus)) {
                            result.getAdditionErrors().add("Cannot add PO item for PO Number " + poNumber + " because the Purchase Order is in pending state: " + poApprovalStatus);
                            continue;
                        }

                        // If model is provided and not N/A, double-check uniqueness again
                        if (!"N/A".equalsIgnoreCase(modelNumber)) {
                            POItem dup = poRepo.findByPoNumberAndModelNumber(poNumber, modelNumber);
                            if (dup != null) {
                                String status = dup.getApprovalStatus();
                                if (isPendingStatus(status)) {
                                    result.getAdditionErrors().add("PO Number " + poNumber + " and Model " + modelNumber + " already exists and is in pending state: " + status + ".");
                                } else {
                                    result.getAdditionErrors().add("PO Number " + poNumber + " and Model " + modelNumber + " already exists.");
                                }
                                continue;
                            }
                        }

                        POItem newItem = mapToPo(jsonObject, dateFormat);
                        poRepo.save(newItem);

                        String insertedBy = newItem.getCreatedBy() != null && !newItem.getCreatedBy().trim().isEmpty() ? newItem.getCreatedBy() : "System";
                        workflowRepository.save(buildWorkflow(newItem.getPoNumber(), newItem.getRecordNo(), WF_PENDING_ADDITION, insertedBy));
                        result.getAddedRows().add(newItem);
                    }
                } catch (Exception e) {
                    Map<String, String> err = buildErrorResponse("Processing error for item", e, "payload", jsonObject.toString());
                    result.getAdditionErrors().add(err.get("message"));
                }
            }

            // Set summary counts
            result.setAddedCount(result.getAddedRows().size());
            result.setModifiedCount(result.getModifiedRows().size());
            result.setFailedAdditionCount(result.getAdditionErrors().size());
            result.setFailedModificationCount(result.getModificationErrors().size());

            return ResponseEntity.ok(result);
        } catch (JSONException e) {
            Map<String, String> err = buildErrorResponse("Invalid JSON format for bulk request", e, "payload", req);
            result.getAdditionErrors().add(err.get("message"));
            result.setFailedAdditionCount(result.getAdditionErrors().size());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }

    // deletion request of po item
    @PostMapping(value = "/poItems/{recordNo}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public ResponseEntity<Map<String, String>> requestDeletePoItemPost(
            @PathVariable long recordNo,
            @RequestBody(required = false) Map<String, Object> payload) {

        logger.info("PO DELETE (POST) REQUEST | recordNo: {}", recordNo);

        try {
            // Fetch the existing PO item
            POItem poItem = poRepo.findByRecordNo(recordNo);
            if (poItem == null) {
                return response("Error", "PO item with recordNo " + recordNo + " does not exist");
            }

            // Check if the PO item is already in a pending state
            String currentStatus = poItem.getApprovalStatus();
            if (isPendingStatus(currentStatus)) {
                return response("Error", "PO item with recordNo " + recordNo + " is already in a pending state (" + currentStatus + ") and cannot be requested for deletion");
            }

            // Update approval status to Pending Deletion
            poItem.setApprovalStatus("Pending Deletion");
            poRepo.save(poItem);

            // Create workflow entry
            Workflow workflow = new Workflow();
            workflow.setPoNumber(poItem.getPoNumber());
            workflow.setRecordNo(poItem.getRecordNo());
            workflow.setOriginalStatus("Pending POItem Deletion");
            workflow.setProcessId(generateProcessId());

            // Determine insertedBy: payload.requestedBy -> updatedBy -> createdBy -> System
            String requestedBy = null;
            if (payload != null && payload.get("requestedBy") != null) {
                requestedBy = payload.get("requestedBy").toString().trim();
                if (requestedBy.isEmpty()) requestedBy = null;
            }

            String insertedBy = (requestedBy != null) ? requestedBy
                    : (poItem.getUpdatedBy() != null && !poItem.getUpdatedBy().trim().isEmpty() ? poItem.getUpdatedBy()
                            : (poItem.getCreatedBy() != null && !poItem.getCreatedBy().trim().isEmpty() ? poItem.getCreatedBy() : "System"));

            workflow.setInsertedBy(insertedBy);
            workflow.setInsertDate(new Date());
            workflowRepository.save(workflow);

            return response("Success", "PO item with recordNo " + recordNo + " is pending deletion");

        } catch (Exception ex) {
            Map<String, String> err = buildErrorResponse("Failed to request deletion of PO item: " + recordNo, ex, "recordNo", recordNo);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

// bulk delete
@PostMapping(value = "/poItems/delete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@Transactional
public ResponseEntity<Map<String, Object>> requestDeletePoItemsBulk(@RequestBody BulkDeleteRequest req) {
    logger.info("PO BULK DELETE REQUEST | recordNos: {} | requestedBy: {}", req != null ? req.getRecordNos() : null, req != null ? req.getRequestedBy() : null);

    Map<String, Object> resp = new HashMap<>();
    List<Map<String, Object>> results = new ArrayList<>();

    if (req == null || req.getRecordNos() == null || req.getRecordNos().isEmpty()) {
        resp.put("status", "Error");
        resp.put("message", "recordNos must be provided as a non-empty array");
        return ResponseEntity.badRequest().body(resp);
    }

    // dedupe and remove nulls/negatives
    List<Long> requested = req.getRecordNos().stream()
            .filter(r -> r != null && r > 0)
            .distinct()
            .collect(Collectors.toList());

    if (requested.isEmpty()) {
        resp.put("status", "Error");
        resp.put("message", "No valid recordNos provided");
        return ResponseEntity.badRequest().body(resp);
    }

    // Load all POItems in one DB call
    List<POItem> items = poRepo.findAllById(requested);
    Map<Long, POItem> found = items.stream().collect(Collectors.toMap(POItem::getRecordNo, p -> p));

    List<POItem> toSave = new ArrayList<>();
    List<Workflow> workflows = new ArrayList<>();

    for (Long recordNo : requested) {
        Map<String, Object> itemResult = new HashMap<>();
        itemResult.put("recordNo", recordNo);
        try {
            POItem poItem = found.get(recordNo);
            if (poItem == null) {
                itemResult.put("status", "Error");
                itemResult.put("message", "PO item not found");
                results.add(itemResult);
                continue;
            }

            String currentStatus = poItem.getApprovalStatus();
            if (isPendingStatus(currentStatus)) {
                itemResult.put("status", "Error");
                itemResult.put("message", "Already pending (" + currentStatus + ")");
                results.add(itemResult);
                continue;
            }

            // mark pending deletion
            poItem.setApprovalStatus("Pending Deletion");
            toSave.add(poItem);

            // build workflow entry
            Workflow wf = new Workflow();
            wf.setPoNumber(poItem.getPoNumber());
            wf.setRecordNo(poItem.getRecordNo());
            wf.setOriginalStatus("Pending POItem Deletion");
            wf.setProcessId(generateProcessId());
            // requestedBy overrides entity updatedBy/createdBy; else use per-item fallback
            String requestedBy = req.getRequestedBy();
            if (requestedBy == null || requestedBy.trim().isEmpty()) {
                String updatedBy = poItem.getUpdatedBy();
                String createdBy = poItem.getCreatedBy();
                requestedBy = (updatedBy != null && !updatedBy.trim().isEmpty()) ? updatedBy
                        : (createdBy != null && !createdBy.trim().isEmpty() ? createdBy : "System");
            }
            wf.setInsertedBy(requestedBy);
            wf.setInsertDate(new Date());
            workflows.add(wf);

            itemResult.put("status", "Success");
            itemResult.put("message", "Marked pending deletion");
        } catch (Exception e) {
            Map<String, String> err = buildErrorResponse("Processing error for recordNo: " + recordNo, e, "recordNo", recordNo);
            itemResult.put("status", "Error");
            itemResult.put("message", err.get("message"));
        }
        results.add(itemResult);
    }

    // Persist updated POItems and Workflows in bulk
    try {
        if (!toSave.isEmpty()) poRepo.saveAll(toSave);
        if (!workflows.isEmpty()) workflowRepository.saveAll(workflows);
    } catch (Exception e) {
        // If bulk save fails we should surface error per item (best effort)
        logger.error("Bulk save failed for pending-deletion PO items", e);
        resp.put("status", "Error");
        resp.put("message", "Failed to persist pending deletion changes: " + e.getMessage());
        resp.put("results", results);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
    }

    // build summary
    long successCount = results.stream().filter(r -> "Success".equals(r.get("status"))).count();
    long failureCount = results.size() - successCount;

    resp.put("status", failureCount == 0 ? "Success" : (successCount > 0 ? "PartialFailure" : "Error"));
    resp.put("successCount", successCount);
    resp.put("failureCount", failureCount);
    resp.put("results", results);

    return ResponseEntity.ok(resp);
}
    

    @PutMapping(value = "/poItems/{recordNo}")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> updatePoItem(@PathVariable long recordNo, @RequestBody String req) {
        logger.info("PO UPDATE REQUEST | Record No: {} | Payload: {}", recordNo, req);

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            JSONObject jsonObject = new JSONObject(req);
            // Fetch the existing PO record
            POItem existingPo = poRepo.findByRecordNo(recordNo);
            if (existingPo == null) {
                return simpleMap("Error", "Record does not exist for recordNo: " + recordNo);
            }
            // Check if the PO item is in a pending state
            String currentStatus = existingPo.getApprovalStatus();
            if (isPendingStatus(currentStatus)) {
                return simpleMap("Error", "PO item with recordNo " + recordNo + " is already in a pending state (" + currentStatus + ") and cannot be updated");
            }
            // Ensure the poNumber is not altered
            String poNumber = jsonObject.getString("poNumber").trim();
            if (!existingPo.getPoNumber().equals(poNumber)) {
                return simpleMap("Error", "Cannot alter poNumber for recordNo: " + recordNo);
            }
            // Create a new tb_Po_Modification entity and populate it with the request data
            tb_Po_Modification modification = new tb_Po_Modification();
            modification.setRecordNo(recordNo);
            modification.setPoNumber(poNumber);
            // normalize modelNumber for modification as well
            modification.setModelNumber(normalizeModel(jsonObject.optString("modelNumber", null)));
            modification.setUom(jsonObject.optString("uom", null));
            modification.setQtyPerSite(jsonObject.optInt("qtyPerSite", 0));
            modification.setTotalNumberOfSites(jsonObject.optInt("totalNumberOfSites", 0));
            modification.setTotalQty(jsonObject.optInt("totalQty", 0));
            modification.setAccumulatedDepreciation(jsonObject.optDouble("accumulatedDepreciation", 0.0));
            modification.setSalvageValue(jsonObject.optDouble("salvageValue", 0.0));
            modification.setFaCategoryNew(jsonObject.optString("faCategoryNew", null));
            modification.setL1(jsonObject.optString("l1", null));
            modification.setL2(jsonObject.optString("l2", null));
            modification.setL3(jsonObject.optString("l3", null));
            modification.setL4(jsonObject.optString("l4", null));
            modification.setOldFaCategory(jsonObject.optString("oldFaCategory", null));
            modification.setAccumulatedDepreciationCode(jsonObject.optString("accumulatedDepreciationCode", null));
            modification.setDepreciationCode(jsonObject.optString("depreciationCode", null));
            modification.setLifeYearsNew(jsonObject.optInt("lifeYearsNew", 0));
            modification.setVendorName(jsonObject.optString("vendorName", null));
            modification.setVendorNumber(jsonObject.optString("vendorNumber", null));
            modification.setProjectNumber(jsonObject.optString("projectNumber", null));
            modification.setCurrency(jsonObject.optString("currency", null));
            modification.setUnitPrice(jsonObject.optDouble("unitPrice", 0.0));
            modification.setPoLine(jsonObject.optInt("poLine", 0));
            modification.setLevel1Description(jsonObject.optString("level1Description", null));
            modification.setPartNumber(jsonObject.optString("partNumber", null));
            modification.setCostCenter(jsonObject.optString("costCenter", null));
            modification.setUpdatedBy(jsonObject.optString("updatedBy", null));
            modification.setApprovalStatus("Pending Modification");
            modification.setRecordDateTime(new Date());
            // Use createdBy from payload if present
            modification.setCreatedBy(jsonObject.optString("createdBy", "System"));

            try {
                // Save the modification request
                poModificationRepo.save(modification);
                // Update the existing PO record's approval status and record who requested update
                existingPo.setApprovalStatus("Pending Modification");
                if (modification.getUpdatedBy() != null && !modification.getUpdatedBy().trim().isEmpty()) {
                    existingPo.setUpdatedBy(modification.getUpdatedBy());
                }
                poRepo.save(existingPo);

                // Insert into Workflow Table using updatedBy -> createdBy -> System
                try {
                    logger.info("Creating workflow entry for PO Number: {}", existingPo.getPoNumber());
                    String insertedBy = (modification.getUpdatedBy() != null && !modification.getUpdatedBy().trim().isEmpty())
                            ? modification.getUpdatedBy()
                            : (modification.getCreatedBy() != null && !modification.getCreatedBy().trim().isEmpty() ? modification.getCreatedBy() : "System");

                    workflowRepository.save(buildWorkflow(modification.getPoNumber(), modification.getRecordNo(), WF_PENDING_MODIFICATION, insertedBy));
                    logger.info("Workflow entry saved successfully for PO Number: {}", existingPo.getPoNumber());
                } catch (Exception e) {
                    Map<String, String> err = buildErrorResponse("Failed to save workflow entry for PO Number: " + existingPo.getPoNumber(), e, "poNumber", existingPo.getPoNumber(), "recordNo", recordNo);
                    // return user-friendly message
                    return err;
                }
                logger.info("Modification request saved for PO Number: {}, Record No: {}", poNumber, recordNo);
                return simpleMap("Success", "Modification request submitted successfully for approval.");
            } catch (Exception e) {
                Map<String, String> err = buildErrorResponse("Failed to save modification for recordNo: " + recordNo, e, "recordNo", recordNo);
                return err;
            }
        } catch (JSONException e) {
            Map<String, String> err = buildErrorResponse("Invalid JSON input", e);
            return err;
        } catch (Exception e) {
            Map<String, String> err = buildErrorResponse("Failed to process update request", e, "recordNo", recordNo);
            return err;
        }
    }

    // Approve PO items
 @PostMapping(value = "/poItems/approve")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@Transactional
public ResponseEntity<Map<String, Object>> approvePoItems(@RequestBody Map<String, Object> requestBody) {
    logger.info("Approve PO Items request | Request Body: {}", requestBody);

    Map<String, Object> response = new HashMap<>();
    List<Map<String, String>> results = new ArrayList<>();

    String changedBy = requestBody.containsKey("changedBy") && requestBody.get("changedBy") != null
            ? requestBody.get("changedBy").toString().trim() : null;
    if (changedBy == null || changedBy.isEmpty()) {
        response.put("status", "Error");
        response.put("message", "changedBy is required in payload");
        return ResponseEntity.badRequest().body(response);
    }

    List<Map<String, Object>> selectedRows =
            (List<Map<String, Object>>) requestBody.get("selectedRows");
    if (selectedRows == null || selectedRows.isEmpty()) {
        response.put("status", "Error");
        response.put("message", "No rows selected.");
        return ResponseEntity.badRequest().body(response);
    }

    // requestType is now read PER ROW — no top-level requestType needed
    boolean hasErrors = false;
    for (Map<String, Object> row : selectedRows) {
        Map<String, String> result = new HashMap<>();
        try {
            String poNumber = (String) row.get("poNumber");
            if (poNumber == null || poNumber.isEmpty())
                throw new IllegalArgumentException("Missing poNumber");

            Long recordNo = row.get("recordNo") != null
                    ? Long.parseLong(row.get("recordNo").toString()) : null;

            // Read requestType from the row itself
            Object rtObj = row.get("requestType");
            if (rtObj == null || rtObj.toString().trim().isEmpty())
                throw new IllegalArgumentException("Missing requestType in row for poNumber: " + poNumber);

            String rowRequestType = rtObj.toString().trim();
            if (!"addition".equalsIgnoreCase(rowRequestType)
                    && !"modification".equalsIgnoreCase(rowRequestType)
                    && !"deletion".equalsIgnoreCase(rowRequestType)) {
                throw new IllegalArgumentException(
                        "Invalid requestType '" + rowRequestType + "' for poNumber: " + poNumber);
            }

            Map<String, String> approveResult =
                    processSinglePoItemApprove(poNumber, recordNo, rowRequestType, changedBy);
            result.putAll(approveResult);
            if (!result.containsKey("status")) result.put("status", "Success");

        } catch (Exception e) {
            hasErrors = true;
            Map<String, String> err = buildErrorResponse(
                    "Failed to process approval row", e, "row", row != null ? row.toString() : "null");
            result.putAll(err);
        }
        results.add(result);
    }

    response.put("results", results);
    if (hasErrors) {
        response.put("status", "PartialFailure");
        return ResponseEntity.status(207).body(response);
    }
    response.put("status", "Success");
    return ResponseEntity.ok(response);
}

    private Map<String, String> processSinglePoItemApprove(String poNumber, Long recordNo, String requestType, String changedBy) {
        Map<String, String> result = new HashMap<>();
        result.put("poNumber", poNumber);
        if (recordNo != null) result.put("recordNo", String.valueOf(recordNo));
        try {
            switch (requestType.toLowerCase()) {
                case "addition":
                    return approveAddition(poNumber, recordNo, changedBy);
                case "modification":
                    return approveModification(poNumber, recordNo, changedBy);
                case "deletion":
                    return approveDeletion(poNumber, recordNo, changedBy);
                default:
                    result.put("status", "Error");
                    result.put("message", "Invalid request type: " + requestType);
                    return result;
            }
        } catch (Exception ex) {
            Map<String, String> err = buildErrorResponse("Failed to process approval", ex, "poNumber", poNumber, "recordNo", recordNo);
            // Ensure poNumber/recordNo are present
            err.putIfAbsent("poNumber", poNumber);
            if (recordNo != null) err.putIfAbsent("recordNo", String.valueOf(recordNo));
            return err;
        }
    }

    private Map<String, String> approveAddition(String poNumber, Long recordNo, String changedBy) {
        Map<String, String> result = new HashMap<>();
        result.put("poNumber", poNumber);
        result.put("recordNo", String.valueOf(recordNo));
        try {
            POItem poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
            if (poItem == null) {
                return simpleResultWithContext("Error", "PO item with PO Number: " + poNumber + " and Record No: " + recordNo + " does not exist", poNumber, recordNo);
            }
            if (!"Pending Addition".equals(poItem.getApprovalStatus())) {
                return simpleResultWithContext("Error", "PO item is not 'Pending Addition'", poNumber, recordNo);
            }

            // Before approving addition, if model != "N/A", ensure no duplicate exists (shouldn't happen normally)
            String model = normalizeModel(poItem.getModelNumber());
            if (!"N/A".equalsIgnoreCase(model)) {
                POItem other = poRepo.findByPoNumberAndModelNumber(poNumber, model);
                if (other != null && other.getRecordNo() != poItem.getRecordNo()) {
                    return simpleResultWithContext("Error", "Cannot approve addition: another PO item under the same PO already uses model " + model, poNumber, recordNo);
                }
            }

            poItem.setApprovalStatus("Approved");
            poRepo.save(poItem);
            // Update workflow with changedBy
            updateWorkflow(poNumber, recordNo, "Pending POItem Addition", "Addition Approved", "PO item addition approved.", changedBy);
            return simpleResultWithContext("Success", "PO item addition approved.", poNumber, recordNo);
        } catch (Exception ex) {
            Map<String, String> err = buildErrorResponse("Failed to approve addition for recordNo: " + recordNo, ex, "poNumber", poNumber, "recordNo", recordNo);
            err.putIfAbsent("poNumber", poNumber);
            err.putIfAbsent("recordNo", String.valueOf(recordNo));
            return err;
        }
    }

    private Map<String, String> approveModification(String poNumber, Long recordNo, String changedBy) {
        Map<String, String> result = new HashMap<>();
        result.put("poNumber", poNumber);
        result.put("recordNo", String.valueOf(recordNo));
        try {
            tb_Po_Modification modification = poModificationRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
            if (modification == null) {
                return simpleResultWithContext("Error", "No modification request found", poNumber, recordNo);
            }
            POItem poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
            if (poItem == null) {
                return simpleResultWithContext("Error", "PO item does not exist", poNumber, recordNo);
            }
            if (!"Pending Modification".equals(modification.getApprovalStatus())) {
                return simpleResultWithContext("Error", "Modification request is not pending approval", poNumber, recordNo);
            }

            // Normalize model in modification
            String newModel = normalizeModel(modification.getModelNumber());
            // If newModel is provided (not N/A) ensure no other POItem under same PO has same model
            if (!"N/A".equalsIgnoreCase(newModel)) {
                POItem other = poRepo.findByPoNumberAndModelNumber(poNumber, newModel);
                if (other != null && other.getRecordNo() != poItem.getRecordNo()) {
                    return simpleResultWithContext("Error", "Cannot approve modification: another PO item under the same PO already uses model " + newModel, poNumber, recordNo);
                }
            }

            // apply changes (only copy fields that are relevant)
            poItem.setModelNumber(newModel);
            poItem.setUom(modification.getUom());
            poItem.setQtyPerSite(modification.getQtyPerSite());
            poItem.setTotalNumberOfSites(modification.getTotalNumberOfSites());
            poItem.setTotalQty(modification.getTotalQty());
            poItem.setAccumulatedDepreciation(modification.getAccumulatedDepreciation());
            poItem.setSalvageValue(modification.getSalvageValue());
            poItem.setFaCategoryNew(modification.getFaCategoryNew());
            poItem.setL1(modification.getL1());
            poItem.setL2(modification.getL2());
            poItem.setL3(modification.getL3());
            poItem.setL4(modification.getL4());
            poItem.setOldFaCategory(modification.getOldFaCategory());
            poItem.setAccumulatedDepreciationCode(modification.getAccumulatedDepreciationCode());
            poItem.setDepreciationCode(modification.getDepreciationCode());
            poItem.setLifeYearsNew(modification.getLifeYearsNew());
            poItem.setVendorName(modification.getVendorName());
            poItem.setVendorNumber(modification.getVendorNumber());
            poItem.setProjectNumber(modification.getProjectNumber());
            poItem.setCurrency(modification.getCurrency());
            poItem.setUnitPrice(modification.getUnitPrice());
            poItem.setPoLine(modification.getPoLine());
            poItem.setLevel1Description(modification.getLevel1Description());
            poItem.setPartNumber(modification.getPartNumber());
            poItem.setCostCenter(modification.getCostCenter());
            poItem.setUpdatedBy(modification.getUpdatedBy());
            poItem.setApprovalStatus("Approved");

            poRepo.save(poItem);
            updateWorkflow(poNumber, recordNo, "Pending POItem Modification", "Modification Approved", "PO item modification approved.", changedBy);
            poModificationRepo.delete(modification);
            return simpleResultWithContext("Success", "PO item modification approved.", poNumber, recordNo);
        } catch (Exception ex) {
            Map<String, String> err = buildErrorResponse("Failed to approve modification for recordNo: " + recordNo, ex, "poNumber", poNumber, "recordNo", recordNo);
            err.putIfAbsent("poNumber", poNumber);
            err.putIfAbsent("recordNo", String.valueOf(recordNo));
            return err;
        }
    }

    private Map<String, String> approveDeletion(String poNumber, Long recordNo, String changedBy) {
        Map<String, String> result = new HashMap<>();
        result.put("poNumber", poNumber);
        result.put("recordNo", String.valueOf(recordNo));
        try {
            POItem poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
            if (poItem == null) {
                return simpleResultWithContext("Error", "PO item does not exist", poNumber, recordNo);
            }
            if (!"Pending Deletion".equals(poItem.getApprovalStatus())) {
                return simpleResultWithContext("Error", "PO item is not 'Pending Deletion'", poNumber, recordNo);
            }
            poRepo.delete(poItem);
            updateWorkflow(poNumber, recordNo, "Pending POItem Deletion", "Deletion Approved", "PO item deletion approved.", changedBy);
            return simpleResultWithContext("Success", "PO item deletion approved.", poNumber, recordNo);
        } catch (Exception ex) {
            Map<String, String> err = buildErrorResponse("Failed to approve deletion for recordNo: " + recordNo, ex, "poNumber", poNumber, "recordNo", recordNo);
            err.putIfAbsent("poNumber", poNumber);
            err.putIfAbsent("recordNo", String.valueOf(recordNo));
            return err;
        }
    }

    // Reject PO items
 @PostMapping(value = "/poItems/reject")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@Transactional
public ResponseEntity<Map<String, Object>> rejectPoItems(@RequestBody Map<String, Object> requestBody) {
    logger.info("Reject PO Items request | Request Body: {}", requestBody);

    Map<String, Object> response = new HashMap<>();
    List<Map<String, String>> results = new ArrayList<>();

    String changedBy = requestBody.containsKey("changedBy") && requestBody.get("changedBy") != null
            ? requestBody.get("changedBy").toString().trim() : null;
    if (changedBy == null || changedBy.isEmpty()) {
        response.put("status", "Error");
        response.put("message", "changedBy is required in payload");
        return ResponseEntity.badRequest().body(response);
    }

    List<Map<String, Object>> selectedRows =
            (List<Map<String, Object>>) requestBody.get("selectedRows");
    if (selectedRows == null || selectedRows.isEmpty()) {
        response.put("status", "Error");
        response.put("message", "No rows selected.");
        return ResponseEntity.badRequest().body(response);
    }

    boolean hasErrors = false;
    for (Map<String, Object> row : selectedRows) {
        Map<String, String> result = new HashMap<>();
        try {
            String poNumber = (String) row.get("poNumber");
            if (poNumber == null || poNumber.isEmpty())
                throw new IllegalArgumentException("Missing poNumber");

            Long recordNo = row.get("recordNo") != null
                    ? Long.parseLong(row.get("recordNo").toString()) : null;

            Object rtObj = row.get("requestType");
            if (rtObj == null || rtObj.toString().trim().isEmpty())
                throw new IllegalArgumentException("Missing requestType in row for poNumber: " + poNumber);

            String rowRequestType = rtObj.toString().trim();
            if (!"addition".equalsIgnoreCase(rowRequestType)
                    && !"modification".equalsIgnoreCase(rowRequestType)
                    && !"deletion".equalsIgnoreCase(rowRequestType)) {
                throw new IllegalArgumentException(
                        "Invalid requestType '" + rowRequestType + "' for poNumber: " + poNumber);
            }

            Map<String, String> rejectResult =
                    processSinglePoItemReject(poNumber, recordNo, rowRequestType, changedBy);
            result.putAll(rejectResult);
            if (!result.containsKey("status")) result.put("status", "Success");

        } catch (Exception e) {
            hasErrors = true;
            Map<String, String> err = buildErrorResponse(
                    "Failed to process rejection row", e, "row", row != null ? row.toString() : "null");
            result.putAll(err);
        }
        results.add(result);
    }

    response.put("results", results);
    if (hasErrors) {
        response.put("status", "PartialFailure");
        return ResponseEntity.status(207).body(response);
    }
    response.put("status", "Success");
    return ResponseEntity.ok(response);
}


    private Map<String, String> processSinglePoItemReject(String poNumber, Long recordNo, String requestType, String changedBy) {
        Map<String, String> result = new HashMap<>();
        result.put("poNumber", poNumber);
        if (recordNo != null) result.put("recordNo", String.valueOf(recordNo));
        try {
            switch (requestType.toLowerCase()) {
                case "addition":
                    return rejectAddition(poNumber, recordNo, changedBy);
                case "modification":
                    return rejectModification(poNumber, recordNo, changedBy);
                case "deletion":
                    return rejectDeletion(poNumber, recordNo, changedBy);
                default:
                    result.put("status", "Error");
                    result.put("message", "Invalid request type: " + requestType);
                    return result;
            }
        } catch (Exception ex) {
            Map<String, String> err = buildErrorResponse("Failed to process rejection", ex, "poNumber", poNumber, "recordNo", recordNo);
            err.putIfAbsent("poNumber", poNumber);
            if (recordNo != null) err.putIfAbsent("recordNo", String.valueOf(recordNo));
            return err;
        }
    }

    private Map<String, String> rejectAddition(String poNumber, Long recordNo, String changedBy) {
        try {
            POItem poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
            if (poItem == null) {
                return simpleResultWithContext("Error", "PO item does not exist", poNumber, recordNo);
            }
            if (!"Pending Addition".equals(poItem.getApprovalStatus())) {
                return simpleResultWithContext("Error", "PO item is not 'Pending Addition'", poNumber, recordNo);
            }
            poRepo.delete(poItem);
            updateWorkflow(poNumber, recordNo, "Pending POItem Addition", "Addition Rejected", "PO item addition rejected.", changedBy);
            return simpleResultWithContext("Success", "PO item addition rejected.", poNumber, recordNo);
        } catch (Exception ex) {
            Map<String, String> err = buildErrorResponse("Failed to reject addition for recordNo: " + recordNo, ex, "poNumber", poNumber, "recordNo", recordNo);
            err.putIfAbsent("poNumber", poNumber);
            err.putIfAbsent("recordNo", String.valueOf(recordNo));
            return err;
        }
    }

    private Map<String, String> rejectModification(String poNumber, Long recordNo, String changedBy) {
        try {
            tb_Po_Modification modification = poModificationRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
            if (modification == null) {
                return simpleResultWithContext("Error", "No modification request found", poNumber, recordNo);
            }
            POItem poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
            if (poItem == null) {
                return simpleResultWithContext("Error", "PO item does not exist", poNumber, recordNo);
            }
            if (!"Pending Modification".equals(modification.getApprovalStatus())) {
                return simpleResultWithContext("Error", "Modification request is not pending approval", poNumber, recordNo);
            }
            // reject => keep main record as Approved (or previous state)
            poItem.setApprovalStatus("Approved");
            poRepo.save(poItem);
            updateWorkflow(poNumber, recordNo, "Pending POItem Modification", "Modification Rejected", "PO item modification rejected.", changedBy);
            poModificationRepo.delete(modification);
            return simpleResultWithContext("Success", "PO item modification rejected.", poNumber, recordNo);
        } catch (Exception ex) {
            Map<String, String> err = buildErrorResponse("Failed to reject modification for recordNo: " + recordNo, ex, "poNumber", poNumber, "recordNo", recordNo);
            err.putIfAbsent("poNumber", poNumber);
            err.putIfAbsent("recordNo", String.valueOf(recordNo));
            return err;
        }
    }

    private Map<String, String> rejectDeletion(String poNumber, Long recordNo, String changedBy) {
        try {
            POItem poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
            if (poItem == null) {
                return simpleResultWithContext("Error", "PO item does not exist", poNumber, recordNo);
            }
            if (!"Pending Deletion".equals(poItem.getApprovalStatus())) {
                return simpleResultWithContext("Error", "PO item is not 'Pending Deletion'", poNumber, recordNo);
            }
            poItem.setApprovalStatus("Approved");
            poRepo.save(poItem);
            updateWorkflow(poNumber, recordNo, "Pending POItem Deletion", "Deletion Rejected", "PO item deletion rejected.", changedBy);
            return simpleResultWithContext("Success", "PO item deletion rejected.", poNumber, recordNo);
        } catch (Exception ex) {
            Map<String, String> err = buildErrorResponse("Failed to reject deletion for recordNo: " + recordNo, ex, "poNumber", poNumber, "recordNo", recordNo);
            err.putIfAbsent("poNumber", poNumber);
            err.putIfAbsent("recordNo", String.valueOf(recordNo));
            return err;
        }
    }

    // updateWorkflow helper unchanged (keeps logging)
    private void updateWorkflow(String poNumber, Long recordNo, String originalStatus, String updatedStatus, String comments, String changedBy) {
        try {
            Workflow workflow = workflowRepository.findByPoNumberAndRecordNoAndOriginalStatus(poNumber, recordNo, originalStatus);

            if (workflow == null) {
                workflow = new Workflow();
                workflow.setPoNumber(poNumber);
                workflow.setRecordNo(recordNo);
                workflow.setOriginalStatus(originalStatus);
                workflow.setProcessId(generateProcessId());
                workflow.setInsertedBy("System"); // insertedBy unknown here; creation flows set insertedBy earlier
                workflow.setInsertDate(new Date());
            }

            workflow.setUpdatedStatus(updatedStatus);
            workflow.setComments(comments);
            workflow.setChangedBy(changedBy != null ? changedBy : "System");
            workflow.setChangeDate(new Date());
            workflowRepository.save(workflow);
        } catch (Exception ex) {
            logger.error("Failed to update workflow for PO Number: {} and Record No: {}", poNumber, recordNo, ex);
        }
    }

    // Mapping Helpers
    private POItem mapToPo(JSONObject obj, SimpleDateFormat dateFormat) throws ParseException {
        POItem po = new POItem();
        po.setPoNumber(obj.optString("poNumber", "").trim());
        // normalize model: store as "N/A" when null/empty
        po.setModelNumber(normalizeModel(obj.optString("modelNumber", null)));
        po.setUom(obj.optString("uom", null));
        po.setQtyPerSite(obj.optInt("qtyPerSite", 0));
        po.setTotalNumberOfSites(obj.optInt("totalNumberOfSites", 0));
        po.setTotalQty(obj.optInt("totalQty", 0));
        po.setAccumulatedDepreciation(obj.optDouble("accumulatedDepreciation", 0.0));
        po.setSalvageValue(obj.optDouble("salvageValue", 0.0));
        po.setFaCategoryNew(obj.optString("faCategoryNew", null));
        po.setL1(obj.optString("l1", null));
        po.setL2(obj.optString("l2", null));
        po.setL3(obj.optString("l3", null));
        po.setL4(obj.optString("l4", null));
        po.setOldFaCategory(obj.optString("oldFaCategory", null));
        po.setAccumulatedDepreciationCode(obj.optString("accumulatedDepreciationCode", null));
        po.setDepreciationCode(obj.optString("depreciationCode", null));
        po.setLifeYearsNew(obj.optInt("lifeYearsNew", 0));
        po.setVendorName(obj.optString("vendorName", null));
        po.setVendorNumber(obj.optString("vendorNumber", null));
        po.setProjectNumber(obj.optString("projectNumber", null));
        po.setCurrency(obj.optString("currency", null));
        po.setUnitPrice(obj.optDouble("unitPrice", 0.0));
        po.setPoLine(obj.optInt("poLine", 0));
        po.setLevel1Description(obj.optString("level1Description", null));
        po.setPartNumber(obj.optString("partNumber", null));
        po.setCostCenter(obj.optString("costCenter", null));
        po.setCreatedBy(obj.optString("createdBy", "System"));
        po.setApprovalStatus("Pending Addition");

        String poDate = obj.optString("poDate", "").trim();
        if (!poDate.isEmpty()) {
            try {
                po.setPoDate(new java.sql.Date(dateFormat.parse(poDate).getTime()));
            } catch (ParseException ex) {
                logger.warn("Ignored invalid poDate: {}", poDate);
                throw ex;
            }
        }

        String dpis = obj.optString("datePlacedInService", "").trim();
        if (!dpis.isEmpty()) {
            try {
                po.setDatePlacedInService(new java.sql.Date(dateFormat.parse(dpis).getTime()));
            } catch (ParseException ex) {
                logger.warn("Ignored invalid datePlacedInService: {}", dpis);
                throw ex;
            }
        }

        po.setRecordDateTime(new java.sql.Date(System.currentTimeMillis()));
        return po;
    }

    private tb_Po_Modification mapToModification(long recordNo, JSONObject obj) {
        tb_Po_Modification mod = new tb_Po_Modification();
        mod.setRecordNo(recordNo);
        mod.setPoNumber(obj.optString("poNumber", null));
        // normalize model for modification
        mod.setModelNumber(normalizeModel(obj.optString("modelNumber", null)));
        mod.setUom(obj.optString("uom", null));
        mod.setQtyPerSite(obj.optInt("qtyPerSite", 0));
        mod.setTotalNumberOfSites(obj.optInt("totalNumberOfSites", 0));
        mod.setTotalQty(obj.optInt("totalQty", 0));
        mod.setAccumulatedDepreciation(obj.optDouble("accumulatedDepreciation", 0.0));
        mod.setSalvageValue(obj.optDouble("salvageValue", 0.0));
        mod.setFaCategoryNew(obj.optString("faCategoryNew", null));
        mod.setL1(obj.optString("l1", null));
        mod.setL2(obj.optString("l2", null));
        mod.setL3(obj.optString("l3", null));
        mod.setL4(obj.optString("l4", null));
        mod.setOldFaCategory(obj.optString("oldFaCategory", null));
        mod.setAccumulatedDepreciationCode(obj.optString("accumulatedDepreciationCode", null));
        mod.setDepreciationCode(obj.optString("depreciationCode", null));
        mod.setLifeYearsNew(obj.optInt("lifeYearsNew", 0));
        mod.setVendorName(obj.optString("vendorName", null));
        mod.setVendorNumber(obj.optString("vendorNumber", null));
        mod.setProjectNumber(obj.optString("projectNumber", null));
        mod.setCurrency(obj.optString("currency", null));
        mod.setUnitPrice(obj.optDouble("unitPrice", 0.0));
        mod.setPoLine(obj.optInt("poLine", 0));
        mod.setLevel1Description(obj.optString("level1Description", null));
        mod.setPartNumber(obj.optString("partNumber", null));
        mod.setCostCenter(obj.optString("costCenter", null));
        mod.setUpdatedBy(obj.optString("updatedBy", "System"));

        // Use createdBy from payload if provided; otherwise default to updatedBy -> System
        String createdBy = obj.optString("createdBy", null);
        String updatedBy = obj.optString("updatedBy", null);
        if (createdBy == null || createdBy.trim().isEmpty()) {
            createdBy = (updatedBy != null && !updatedBy.trim().isEmpty()) ? updatedBy : "System";
        }
        mod.setCreatedBy(createdBy);

        mod.setApprovalStatus("Pending Modification");
        mod.setRecordDateTime(new Date());
        return mod;
    }

    private Workflow buildWorkflow(String poNumber, long recordNo, String originalStatus, String insertedBy) {
        Workflow wf = new Workflow();
        wf.setPoNumber(poNumber);
        wf.setRecordNo(recordNo);

        // normalize originalStatus - prefer provided, otherwise mark as unknown so it's searchable
        String status = (originalStatus != null && !originalStatus.trim().isEmpty())
                ? originalStatus.trim()
                : WF_UNKNOWN_STATUS;
        wf.setOriginalStatus(status);

        wf.setProcessId(generateProcessId());
        wf.setInsertedBy(insertedBy != null && !insertedBy.trim().isEmpty() ? insertedBy : "System");
        wf.setInsertDate(new Date());
        return wf;
    }

    private ResponseEntity<Map<String, String>> response(String status, String message) {
        Map<String, String> res = new HashMap<>();
        res.put("status", status);
        res.put("message", message);
        return ResponseEntity.ok(res);
    }

    private Map<String, String> simpleMap(String status, String message) {
        Map<String, String> res = new HashMap<>();
        res.put("status", status);
        res.put("message", message);
        return res;
    }

    private Map<String, String> simpleResultWithContext(String status, String message, String poNumber, Long recordNo) {
        Map<String, String> res = new HashMap<>();
        res.put("status", status);
        res.put("message", message);
        if (poNumber != null) res.put("poNumber", poNumber);
        if (recordNo != null) res.put("recordNo", String.valueOf(recordNo));
        return res;
    }

    // ---- Helpers for flexible POST parsing and filter injection ----
    private SearchRequest parseRequestBodyIfJson(String rawBody, HttpServletRequest servletRequest) {
        try {
            String contentType = servletRequest.getContentType();
            if (rawBody == null || rawBody.trim().isEmpty()) {
                return null;
            }
            if ((contentType != null && contentType.toLowerCase().contains("json")) || looksLikeJson(rawBody)) {
                return objectMapper.readValue(rawBody, SearchRequest.class);
            } else {
                return null;
            }
        } catch (Exception ex) {
            logger.warn("Failed to parse SearchRequest body, falling back to empty SearchRequest: {}", ex.getMessage());
            return null;
        }
    }

    private boolean looksLikeJson(String s) {
        if (s == null) return false;
        String t = s.trim();
        return t.startsWith("{") || t.startsWith("[");
    }

    // Reusable injection of poNumber filter (keeps existing filters and avoids duplicates)
    private void injectPoNumberFilterIfNeeded(String poNumber, SearchRequest request) {
        if (poNumber == null || poNumber.trim().isEmpty() || request == null) return;
        if (request.getFilterBy() != null) {
            for (FilterRequest f : request.getFilterBy()) {
                if (f != null && "poNumber".equals(f.getColumn())) return; // already filtered
            }
        }
        FilterRequest poFilter = new FilterRequest();
        poFilter.setColumn("poNumber");
        poFilter.setOperator(FilterOperator.EQUALS);
        poFilter.setValue(poNumber);
        List<FilterRequest> filters = request.getFilterBy() == null ? new ArrayList<>() : new ArrayList<>(request.getFilterBy());
        filters.add(poFilter);
        request.setFilterBy(filters);
    }

    // helper: normalize model value to "N/A" when null/empty
    private String normalizeModel(String model) {
        if (model == null) return "N/A";
        String m = model.trim();
        return m.isEmpty() ? "N/A" : m;
    }
}