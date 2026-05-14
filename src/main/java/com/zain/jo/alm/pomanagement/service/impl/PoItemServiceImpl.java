package com.zain.jo.alm.pomanagement.service.impl;

import com.zain.jo.alm.pomanagement.dto.PoItemDto;
import com.zain.jo.alm.pomanagement.dto.request.BulkDeleteRequest;
import com.zain.jo.alm.pomanagement.dto.request.FilterOperator;
import com.zain.jo.alm.pomanagement.dto.request.FilterRequest;
import com.zain.jo.alm.pomanagement.dto.request.SearchRequest;
import com.zain.jo.alm.pomanagement.dto.response.BulkOperationResult;
import com.zain.jo.alm.pomanagement.dto.response.PageResult;
import com.zain.jo.alm.pomanagement.dto.response.WorkflowActionResponse;
import com.zain.jo.alm.pomanagement.entity.PoItem;
import com.zain.jo.alm.pomanagement.entity.PurchaseOrder;
import com.zain.jo.alm.pomanagement.entity.Workflow;
import com.zain.jo.alm.pomanagement.entity.PoItemModification;
import com.zain.jo.alm.pomanagement.repository.PoItemRepository;
import com.zain.jo.alm.pomanagement.repository.PurchaseOrderRepository;
import com.zain.jo.alm.pomanagement.repository.WorkflowRepository;
import com.zain.jo.alm.pomanagement.repository.PoItemModificationRepository;
import com.zain.jo.alm.pomanagement.service.PoItemService;
import com.zain.jo.alm.pomanagement.specification.PoItemSpecification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PoItemServiceImpl implements PoItemService {

    private static final Logger logger = LoggerFactory.getLogger(PoItemServiceImpl.class);

    // Workflow status constants
    private static final String WF_PENDING_ADDITION     = "Pending POItem Addition";
    private static final String WF_PENDING_MODIFICATION = "Pending POItem Modification";
    private static final String WF_PENDING_DELETION     = "Pending POItem Deletion";

    private final PoItemRepository            poItemRepo;
    private final PurchaseOrderRepository     purchaseOrderRepo;
    private final WorkflowRepository          workflowRepo;
    private final PoItemModificationRepository modificationRepo;

    @Autowired
    public PoItemServiceImpl(PoItemRepository poItemRepo,
                             PurchaseOrderRepository purchaseOrderRepo,
                             WorkflowRepository workflowRepo,
                             PoItemModificationRepository modificationRepo) {
        this.poItemRepo        = poItemRepo;
        this.purchaseOrderRepo = purchaseOrderRepo;
        this.workflowRepo      = workflowRepo;
        this.modificationRepo  = modificationRepo;
    }

    // ──────────────────────────────────────────────
    // Queries
    // ──────────────────────────────────────────────

    @Override
    public PageResult<PoItemDto> getPOItems(SearchRequest request) {
        int page = request.getPage() == null ? 0 : Math.max(0, request.getPage());
        int size = request.getSize() == null ? 100 : (request.getSize() <= 0 ? 100 : request.getSize());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDateTime"));
        Specification<PoItem> spec = PoItemSpecification.fromSearchRequest(request);

        Page<PoItem> resultPage = (spec == null)
                ? poItemRepo.findAll(pageable)
                : poItemRepo.findAll(spec, pageable);

        List<PoItemDto> dtos = resultPage.stream().map(this::toDto).collect(Collectors.toList());

        return new PageResult<>(
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.getNumber(),
                resultPage.getSize(),
                dtos
        );
    }

    // ──────────────────────────────────────────────
    // Single mutations
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public Map<String, String> addSinglePOItem(String rawJson) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            JSONObject json = new JSONObject(rawJson);
            String poNumber    = json.getString("poNumber").trim();
            String modelNumber = normalizeModel(json.optString("modelNumber", null));

            PurchaseOrder parentPO = purchaseOrderRepo.findByPoNumber(poNumber);
            if (parentPO == null) {
                return errorMap("PO Number " + poNumber + " does not exist, provide an existing PO Number");
            }
            if (isPending(parentPO.getApprovalStatus())) {
                return errorMap("Cannot add PO item: Purchase Order " + poNumber
                        + " is in pending state: " + parentPO.getApprovalStatus());
            }
            if (!"NA".equalsIgnoreCase(modelNumber)) {
                PoItem existing = poItemRepo.findByPoNumberAndModelNumber(poNumber, modelNumber);
                if (existing != null) {
                    String msg = isPending(existing.getApprovalStatus())
                            ? "PO item already exists for PO " + poNumber + " / model " + modelNumber
                                + " and is in pending state: " + existing.getApprovalStatus()
                            : "PO item already exists for PO " + poNumber + " / model " + modelNumber;
                    return errorMap(msg);
                }
            }
            PoItem newItem = mapToPOItem(json, dateFormat);
            poItemRepo.save(newItem);
            workflowRepo.save(buildWorkflow(
                    newItem.getPoNumber(), newItem.getRecordNo(),
                    WF_PENDING_ADDITION, resolveInsertedBy(newItem.getCreatedBy(), null)));

            return successMap("PO Item for PO " + poNumber + " / model " + newItem.getModelNumber() + " added successfully.");
        } catch (JSONException e) {
            return errorMap("Invalid JSON: " + e.getMessage());
        } catch (ParseException e) {
            return errorMap("Invalid date format. Expected yyyy-MM-dd.");
        } catch (Exception e) {
            logger.error("Unexpected error adding PO item", e);
            return errorMap("Unexpected error: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Map<String, String> updatePOItem(long recordNo, String rawJson) {
        try {
            JSONObject json = new JSONObject(rawJson);
            PoItem existing = poItemRepo.findByRecordNo(recordNo);
            if (existing == null) {
                return errorMap("Record does not exist for recordNo: " + recordNo);
            }
            if (isPending(existing.getApprovalStatus())) {
                return errorMap("PO item " + recordNo + " is already in pending state: "
                        + existing.getApprovalStatus() + " and cannot be updated");
            }
            String poNumber = json.getString("poNumber").trim();
            if (!existing.getPoNumber().equals(poNumber)) {
                return errorMap("Cannot alter poNumber for recordNo: " + recordNo);
            }
            PoItemModification mod = mapToModification(recordNo, json);
            modificationRepo.save(mod);

            existing.setApprovalStatus("Pending Modification");
            if (mod.getUpdatedBy() != null && !mod.getUpdatedBy().trim().isEmpty()) {
                existing.setUpdatedBy(mod.getUpdatedBy());
            }
            poItemRepo.save(existing);

            String insertedBy = resolveInsertedBy(mod.getUpdatedBy(), mod.getCreatedBy());
            workflowRepo.save(buildWorkflow(mod.getPoNumber(), mod.getRecordNo(), WF_PENDING_MODIFICATION, insertedBy));

            return successMap("Modification request submitted successfully for approval.");
        } catch (JSONException e) {
            return errorMap("Invalid JSON: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating PO item {}", recordNo, e);
            return errorMap("Failed to process update: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Map<String, String> requestSingleDelete(long recordNo, String requestedBy) {
        PoItem item = poItemRepo.findByRecordNo(recordNo);
        if (item == null) {
            return errorMap("PO item with recordNo " + recordNo + " does not exist");
        }
        if (isPending(item.getApprovalStatus())) {
            return errorMap("PO item " + recordNo + " is already in pending state ("
                    + item.getApprovalStatus() + ") and cannot be requested for deletion");
        }
        item.setApprovalStatus("Pending Deletion");
        poItemRepo.save(item);

        String insertedBy = (requestedBy != null && !requestedBy.trim().isEmpty())
                ? requestedBy.trim()
                : resolveInsertedBy(item.getUpdatedBy(), item.getCreatedBy());

        workflowRepo.save(buildWorkflow(item.getPoNumber(), item.getRecordNo(), WF_PENDING_DELETION, insertedBy));
        return successMap("PO item with recordNo " + recordNo + " is pending deletion");
    }

    // ──────────────────────────────────────────────
    // Bulk mutations
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public BulkOperationResult addOrUpdateBulk(String rawJsonArray) {
        BulkOperationResult result = new BulkOperationResult();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            JSONArray arr = new JSONArray(rawJsonArray);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject json = arr.getJSONObject(i);
                try {
                    processSingleBulkItem(json, dateFormat, result);
                } catch (Exception e) {
                    result.getAdditionErrors().add("Processing error for item[" + i + "]: " + e.getMessage());
                }
            }
        } catch (JSONException e) {
            result.getAdditionErrors().add("Invalid JSON array: " + e.getMessage());
        }
        result.setAddedCount(result.getAddedRows().size());
        result.setModifiedCount(result.getModifiedRows().size());
        result.setFailedAdditionCount(result.getAdditionErrors().size());
        result.setFailedModificationCount(result.getModificationErrors().size());
        return result;
    }

    private void processSingleBulkItem(JSONObject json, SimpleDateFormat dateFormat, BulkOperationResult result) {
        String poNumber    = json.getString("poNumber").trim();
        String modelNumber = normalizeModel(json.optString("modelNumber", null));

        PoItem existing = "NA".equalsIgnoreCase(modelNumber)
                ? null
                : poItemRepo.findByPoNumberAndModelNumber(poNumber, modelNumber);

        if (existing != null) {
            // Modification path
            if (isPending(existing.getApprovalStatus())) {
                result.getModificationErrors().add("PO " + poNumber + " / model " + modelNumber
                        + " is already in pending state: " + existing.getApprovalStatus());
                return;
            }
            PoItemModification mod = mapToModification(existing.getRecordNo(), json);
            modificationRepo.save(mod);

            existing.setApprovalStatus("Pending Modification");
            if (mod.getUpdatedBy() != null && !mod.getUpdatedBy().trim().isEmpty()) {
                existing.setUpdatedBy(mod.getUpdatedBy());
            }
            poItemRepo.save(existing);

            String insertedBy = resolveInsertedBy(mod.getUpdatedBy(), mod.getCreatedBy());
            workflowRepo.save(buildWorkflow(existing.getPoNumber(), existing.getRecordNo(), WF_PENDING_MODIFICATION, insertedBy));
            result.getModifiedRows().add(mod);

        } else {
            // Addition path
            PurchaseOrder parentPO = purchaseOrderRepo.findByPoNumber(poNumber);
            if (parentPO == null) {
                result.getAdditionErrors().add("PO Number " + poNumber + " does not exist");
                return;
            }
            if (isPending(parentPO.getApprovalStatus())) {
                result.getAdditionErrors().add("Cannot add PO item: PO " + poNumber
                        + " is in pending state: " + parentPO.getApprovalStatus());
                return;
            }
            if (!"NA".equalsIgnoreCase(modelNumber)) {
                PoItem dup = poItemRepo.findByPoNumberAndModelNumber(poNumber, modelNumber);
                if (dup != null) {
                    result.getAdditionErrors().add("PO " + poNumber + " / model " + modelNumber
                            + (isPending(dup.getApprovalStatus()) ? " already exists (pending: " + dup.getApprovalStatus() + ")" : " already exists"));
                    return;
                }
            }
            try {
                PoItem newItem = mapToPOItem(json, dateFormat);
                poItemRepo.save(newItem);
                String insertedBy = resolveInsertedBy(newItem.getCreatedBy(), null);
                workflowRepo.save(buildWorkflow(newItem.getPoNumber(), newItem.getRecordNo(), WF_PENDING_ADDITION, insertedBy));
                result.getAddedRows().add(newItem);
            } catch (ParseException e) {
                result.getAdditionErrors().add("Invalid date format for PO " + poNumber + ": " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public Map<String, Object> requestBulkDelete(BulkDeleteRequest request) {
        Map<String, Object> resp   = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();

        if (request == null || request.getRecordNos() == null || request.getRecordNos().isEmpty()) {
            resp.put("status", "Error");
            resp.put("message", "recordNos must be provided as a non-empty array");
            return resp;
        }

        List<Long> requested = request.getRecordNos().stream()
                .filter(r -> r != null && r > 0)
                .distinct()
                .collect(Collectors.toList());

        if (requested.isEmpty()) {
            resp.put("status", "Error");
            resp.put("message", "No valid recordNos provided");
            return resp;
        }

        Map<Long, PoItem> found = poItemRepo.findAllById(requested)
                .stream()
                .collect(Collectors.toMap(PoItem::getRecordNo, p -> p));

        List<PoItem>    toSave    = new ArrayList<>();
        List<Workflow>  workflows = new ArrayList<>();

        for (Long recordNo : requested) {
            Map<String, Object> itemResult = new HashMap<>();
            itemResult.put("recordNo", recordNo);
            try {
                PoItem item = found.get(recordNo);
                if (item == null) {
                    itemResult.put("status", "Error");
                    itemResult.put("message", "PO item not found");
                    results.add(itemResult);
                    continue;
                }
                if (isPending(item.getApprovalStatus())) {
                    itemResult.put("status", "Error");
                    itemResult.put("message", "Already pending (" + item.getApprovalStatus() + ")");
                    results.add(itemResult);
                    continue;
                }
                item.setApprovalStatus("Pending Deletion");
                toSave.add(item);

                String requestedBy = resolveRequestedBy(
                        request.getRequestedBy(), item.getUpdatedBy(), item.getCreatedBy());
                workflows.add(buildWorkflow(item.getPoNumber(), item.getRecordNo(), WF_PENDING_DELETION, requestedBy));

                itemResult.put("status",  "Success");
                itemResult.put("message", "Marked pending deletion");
            } catch (Exception e) {
                itemResult.put("status",  "Error");
                itemResult.put("message", e.getMessage());
            }
            results.add(itemResult);
        }

        poItemRepo.saveAll(toSave);
        workflowRepo.saveAll(workflows);

        long successCount = results.stream().filter(r -> "Success".equals(r.get("status"))).count();
        long failureCount = results.size() - successCount;

        resp.put("status",       failureCount == 0 ? "Success" : (successCount > 0 ? "PartialFailure" : "Error"));
        resp.put("successCount", successCount);
        resp.put("failureCount", failureCount);
        resp.put("results",      results);
        return resp;
    }

    // ──────────────────────────────────────────────
    // Approval / Rejection
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public WorkflowActionResponse processWorkflowAction(Map<String, Object> requestBody, String action) {
        WorkflowActionResponse response = new WorkflowActionResponse();
        List<Map<String, String>> results = new ArrayList<>();

        String changedBy = extractStringField(requestBody, "changedBy");
        if (changedBy == null || changedBy.isEmpty()) {
            response.setStatus("Error");
            response.setMessage("changedBy is required in payload");
            return response;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> selectedRows = (List<Map<String, Object>>) requestBody.get("selectedRows");
        if (selectedRows == null || selectedRows.isEmpty()) {
            response.setStatus("Error");
            response.setMessage("No rows selected.");
            return response;
        }

        boolean hasErrors = false;
        for (Map<String, Object> row : selectedRows) {
            Map<String, String> result = new HashMap<>();
            try {
                String poNumber    = requireString(row, "poNumber");
                Long   recordNo    = parseLong(row.get("recordNo"));
                String requestType = requireString(row, "requestType");
                validateRequestType(requestType, "addition", "modification", "deletion");

                Map<String, String> actionResult = dispatchPOItemAction(poNumber, recordNo, requestType, action, changedBy);
                result.putAll(actionResult);
            } catch (Exception e) {
                hasErrors = true;
                result.put("status",  "Error");
                result.put("message", e.getMessage());
            }
            results.add(result);
        }

        response.setResults(results);
        if (hasErrors) {
            response.setStatus("PartialFailure");
        } else {
            response.setStatus("Success");
        }
        return response;
    }

    private Map<String, String> dispatchPOItemAction(String poNumber, Long recordNo,
                                                      String requestType, String action, String changedBy) {
        switch (requestType.toLowerCase()) {
            case "addition":
                return "approve".equalsIgnoreCase(action)
                        ? approveAddition(poNumber, recordNo, changedBy)
                        : rejectAddition(poNumber, recordNo, changedBy);
            case "modification":
                return "approve".equalsIgnoreCase(action)
                        ? approveModification(poNumber, recordNo, changedBy)
                        : rejectModification(poNumber, recordNo, changedBy);
            case "deletion":
                return "approve".equalsIgnoreCase(action)
                        ? approveDeletion(poNumber, recordNo, changedBy)
                        : rejectDeletion(poNumber, recordNo, changedBy);
            default:
                return errorResultWithContext("Invalid requestType: " + requestType, poNumber, recordNo);
        }
    }

    // ── Addition ──────────────────────────────────
    private Map<String, String> approveAddition(String poNumber, Long recordNo, String changedBy) {
        PoItem item = poItemRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (item == null)
            return errorResultWithContext("PO item not found", poNumber, recordNo);
        if (!"Pending Addition".equals(item.getApprovalStatus()))
            return errorResultWithContext("PO item is not 'Pending Addition'", poNumber, recordNo);

        String model = normalizeModel(item.getModelNumber());
        if (!"NA".equalsIgnoreCase(model)) {
            PoItem other = poItemRepo.findByPoNumberAndModelNumber(poNumber, model);
            if (other != null && other.getRecordNo() != item.getRecordNo())
                return errorResultWithContext("Another PO item under the same PO already uses model " + model, poNumber, recordNo);
        }
        item.setApprovalStatus("Approved");
        poItemRepo.save(item);
        updateWorkflow(poNumber, recordNo, WF_PENDING_ADDITION, "Addition Approved", "PO item addition approved.", changedBy);
        return successResultWithContext("PO item addition approved.", poNumber, recordNo);
    }

    private Map<String, String> rejectAddition(String poNumber, Long recordNo, String changedBy) {
        PoItem item = poItemRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (item == null)
            return errorResultWithContext("PO item not found", poNumber, recordNo);
        if (!"Pending Addition".equals(item.getApprovalStatus()))
            return errorResultWithContext("PO item is not 'Pending Addition'", poNumber, recordNo);
        poItemRepo.delete(item);
        updateWorkflow(poNumber, recordNo, WF_PENDING_ADDITION, "Addition Rejected", "PO item addition rejected.", changedBy);
        return successResultWithContext("PO item addition rejected.", poNumber, recordNo);
    }

    // ── Modification ──────────────────────────────
    private Map<String, String> approveModification(String poNumber, Long recordNo, String changedBy) {
        PoItemModification mod = modificationRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (mod == null)
            return errorResultWithContext("No modification request found", poNumber, recordNo);
        if (!"Pending Modification".equals(mod.getApprovalStatus()))
            return errorResultWithContext("Modification request is not pending approval", poNumber, recordNo);

        PoItem item = poItemRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (item == null)
            return errorResultWithContext("PO item not found", poNumber, recordNo);

        String newModel = normalizeModel(mod.getModelNumber());
        if (!"NA".equalsIgnoreCase(newModel)) {
            PoItem other = poItemRepo.findByPoNumberAndModelNumber(poNumber, newModel);
            if (other != null && other.getRecordNo() != item.getRecordNo())
                return errorResultWithContext("Another PO item under the same PO already uses model " + newModel, poNumber, recordNo);
        }
        applyModification(item, mod);
        item.setApprovalStatus("Approved");
        poItemRepo.save(item);
        updateWorkflow(poNumber, recordNo, WF_PENDING_MODIFICATION, "Modification Approved", "PO item modification approved.", changedBy);
        modificationRepo.delete(mod);
        return successResultWithContext("PO item modification approved.", poNumber, recordNo);
    }

    private Map<String, String> rejectModification(String poNumber, Long recordNo, String changedBy) {
        PoItemModification mod = modificationRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (mod == null)
            return errorResultWithContext("No modification request found", poNumber, recordNo);
        if (!"Pending Modification".equals(mod.getApprovalStatus()))
            return errorResultWithContext("Modification request is not pending approval", poNumber, recordNo);

        PoItem item = poItemRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (item == null)
            return errorResultWithContext("PO item not found", poNumber, recordNo);

        item.setApprovalStatus("Approved");
        poItemRepo.save(item);
        updateWorkflow(poNumber, recordNo, WF_PENDING_MODIFICATION, "Modification Rejected", "PO item modification rejected.", changedBy);
        modificationRepo.delete(mod);
        return successResultWithContext("PO item modification rejected.", poNumber, recordNo);
    }

    // ── Deletion ──────────────────────────────────
    private Map<String, String> approveDeletion(String poNumber, Long recordNo, String changedBy) {
        PoItem item = poItemRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (item == null)
            return errorResultWithContext("PO item not found", poNumber, recordNo);
        if (!"Pending Deletion".equals(item.getApprovalStatus()))
            return errorResultWithContext("PO item is not 'Pending Deletion'", poNumber, recordNo);
        poItemRepo.delete(item);
        updateWorkflow(poNumber, recordNo, WF_PENDING_DELETION, "Deletion Approved", "PO item deletion approved.", changedBy);
        return successResultWithContext("PO item deletion approved.", poNumber, recordNo);
    }

    private Map<String, String> rejectDeletion(String poNumber, Long recordNo, String changedBy) {
        PoItem item = poItemRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (item == null)
            return errorResultWithContext("PO item not found", poNumber, recordNo);
        if (!"Pending Deletion".equals(item.getApprovalStatus()))
            return errorResultWithContext("PO item is not 'Pending Deletion'", poNumber, recordNo);
        item.setApprovalStatus("Approved");
        poItemRepo.save(item);
        updateWorkflow(poNumber, recordNo, WF_PENDING_DELETION, "Deletion Rejected", "PO item deletion rejected.", changedBy);
        return successResultWithContext("PO item deletion rejected.", poNumber, recordNo);
    }

    // ──────────────────────────────────────────────
    // Workflow helpers
    // ──────────────────────────────────────────────

    private void updateWorkflow(String poNumber, Long recordNo, String originalStatus,
                                String updatedStatus, String comments, String changedBy) {
        Workflow wf = workflowRepo.findByPoNumberAndRecordNoAndOriginalStatus(poNumber, recordNo, originalStatus);
        if (wf == null) {
            wf = new Workflow();
            wf.setPoNumber(poNumber);
            wf.setRecordNo(recordNo);
            wf.setOriginalStatus(originalStatus);
            wf.setProcessId(generateProcessId());
            wf.setInsertedBy("System");
            wf.setInsertDate(LocalDateTime.now());
        }
        wf.setUpdatedStatus(updatedStatus);
        wf.setComments(comments);
        wf.setChangedBy(changedBy != null ? changedBy : "System");
        wf.setChangeDate(LocalDateTime.now());
        workflowRepo.save(wf);
    }

    private Workflow buildWorkflow(String poNumber, long recordNo, String originalStatus, String insertedBy) {
        Workflow wf = new Workflow();
        wf.setPoNumber(poNumber);
        wf.setRecordNo(recordNo);
        wf.setOriginalStatus(originalStatus != null ? originalStatus : "Unknown");
        wf.setProcessId(generateProcessId());
        wf.setInsertedBy(insertedBy != null && !insertedBy.trim().isEmpty() ? insertedBy : "System");
        wf.setInsertDate(LocalDateTime.now());
        return wf;
    }

    // ──────────────────────────────────────────────
    // Mapping helpers
    // ──────────────────────────────────────────────

    private PoItem mapToPOItem(JSONObject obj, SimpleDateFormat dateFormat) throws ParseException {
        PoItem po = new PoItem();
        po.setPoNumber(obj.optString("poNumber", "").trim());
        po.setModelNumber(normalizeModel(obj.optString("modelNumber", null)));
        po.setUom(obj.optString("uom", null));
        po.setQtyPerSite(obj.optInt("qtyPerSite", 0));
        po.setTotalNumberOfSites(obj.optInt("totalNumberOfSites", 0));
        po.setTotalQty(obj.optInt("totalQty", 0));
        po.setAccumulatedDepreciation(new java.math.BigDecimal(obj.optDouble("accumulatedDepreciation", 0.0)));
        po.setSalvageValue(new java.math.BigDecimal(obj.optDouble("salvageValue", 0.0)));
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
        po.setUnitPrice(new java.math.BigDecimal(obj.optDouble("unitPrice", 0.0)));
        po.setPoLine(obj.optInt("poLine", 0));
        po.setLevel1Description(obj.optString("level1Description", null));
        po.setPartNumber(obj.optString("partNumber", null));
        po.setCostCenter(obj.optString("costCenter", null));
        po.setCreatedBy(obj.optString("createdBy", "System"));
        po.setApprovalStatus("Pending Addition");
        po.setRecordDateTime(LocalDateTime.now());

        String poDate = obj.optString("poDate", "").trim();
        if (!poDate.isEmpty()) {
            po.setPoDate(dateFormat.parse(poDate).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }
        String dpis = obj.optString("datePlacedInService", "").trim();
        if (!dpis.isEmpty()) {
            po.setDatePlacedInService(dateFormat.parse(dpis).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }
        return po;
    }

    private PoItemModification mapToModification(long recordNo, JSONObject obj) {
        PoItemModification mod = new PoItemModification();
        mod.setRecordNo(recordNo);
        mod.setPoNumber(obj.optString("poNumber", null));
        mod.setModelNumber(normalizeModel(obj.optString("modelNumber", null)));
        mod.setUom(obj.optString("uom", null));
        mod.setQtyPerSite(obj.optInt("qtyPerSite", 0));
        mod.setTotalNumberOfSites(obj.optInt("totalNumberOfSites", 0));
        mod.setTotalQty(obj.optInt("totalQty", 0));
        mod.setAccumulatedDepreciation(new java.math.BigDecimal(obj.optDouble("accumulatedDepreciation", 0.0)));
        mod.setSalvageValue(new java.math.BigDecimal(obj.optDouble("salvageValue", 0.0)));
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
        mod.setUnitPrice(new java.math.BigDecimal(obj.optDouble("unitPrice", 0.0)));
        mod.setPoLine(obj.optInt("poLine", 0));
        mod.setLevel1Description(obj.optString("level1Description", null));
        mod.setPartNumber(obj.optString("partNumber", null));
        mod.setCostCenter(obj.optString("costCenter", null));

        String updatedBy = obj.optString("updatedBy", null);
        String createdBy = obj.optString("createdBy", null);
        mod.setUpdatedBy(updatedBy);
        mod.setCreatedBy((createdBy != null && !createdBy.trim().isEmpty()) ? createdBy
                : (updatedBy != null && !updatedBy.trim().isEmpty()) ? updatedBy : "System");
        mod.setApprovalStatus("Pending Modification");
        mod.setRecordDateTime(LocalDateTime.now());
        return mod;
    }

    private void applyModification(PoItem item, PoItemModification mod) {
        item.setModelNumber(normalizeModel(mod.getModelNumber()));
        item.setUom(mod.getUom());
        item.setQtyPerSite(mod.getQtyPerSite());
        item.setTotalNumberOfSites(mod.getTotalNumberOfSites());
        item.setTotalQty(mod.getTotalQty());
        item.setAccumulatedDepreciation(mod.getAccumulatedDepreciation());
        item.setSalvageValue(mod.getSalvageValue());
        item.setFaCategoryNew(mod.getFaCategoryNew());
        item.setL1(mod.getL1());
        item.setL2(mod.getL2());
        item.setL3(mod.getL3());
        item.setL4(mod.getL4());
        item.setOldFaCategory(mod.getOldFaCategory());
        item.setAccumulatedDepreciationCode(mod.getAccumulatedDepreciationCode());
        item.setDepreciationCode(mod.getDepreciationCode());
        item.setLifeYearsNew(mod.getLifeYearsNew());
        item.setVendorName(mod.getVendorName());
        item.setVendorNumber(mod.getVendorNumber());
        item.setProjectNumber(mod.getProjectNumber());
        item.setCurrency(mod.getCurrency());
        item.setUnitPrice(mod.getUnitPrice());
        item.setPoLine(mod.getPoLine());
        item.setLevel1Description(mod.getLevel1Description());
        item.setPartNumber(mod.getPartNumber());
        item.setCostCenter(mod.getCostCenter());
        item.setUpdatedBy(mod.getUpdatedBy());
    }

    private PoItemDto toDto(PoItem e) {
        PoItemDto d = new PoItemDto();
        d.setRecordNo(e.getRecordNo());
        if (e.getRecordDateTime() != null)    d.setRecordDateTime(e.getRecordDateTime().toLocalDate());
        d.setPoNumber(e.getPoNumber());
        d.setModelNumber(e.getModelNumber());
        d.setUom(e.getUom());
        d.setQtyPerSite(e.getQtyPerSite());
        d.setTotalNumberOfSites(e.getTotalNumberOfSites());
        d.setTotalQty(e.getTotalQty());
        if (e.getAccumulatedDepreciation() != null) d.setAccumulatedDepreciation(e.getAccumulatedDepreciation().doubleValue());
        if (e.getSalvageValue() != null) d.setSalvageValue(e.getSalvageValue().doubleValue());
        d.setFaCategoryNew(e.getFaCategoryNew());
        d.setL1(e.getL1()); d.setL2(e.getL2()); d.setL3(e.getL3()); d.setL4(e.getL4());
        d.setOldFaCategory(e.getOldFaCategory());
        d.setAccumulatedDepreciationCode(e.getAccumulatedDepreciationCode());
        d.setDepreciationCode(e.getDepreciationCode());
        d.setLifeYearsNew(e.getLifeYearsNew());
        d.setVendorName(e.getVendorName());
        d.setVendorNumber(e.getVendorNumber());
        d.setProjectNumber(e.getProjectNumber());
        if (e.getDatePlacedInService() != null) d.setDatePlacedInService(e.getDatePlacedInService().toLocalDate());
        if (e.getPoDate()              != null) d.setPoDate(e.getPoDate().toLocalDate());
        d.setCurrency(e.getCurrency());
        if (e.getUnitPrice() != null) d.setUnitPrice(e.getUnitPrice().doubleValue());
        d.setPoLine(e.getPoLine());
        d.setLevel1Description(e.getLevel1Description());
        d.setPartNumber(e.getPartNumber());
        d.setL3Description(e.getL3Description());
        d.setCostCenter(e.getCostCenter());
        d.setApprovalStatus(e.getApprovalStatus());
        d.setCreatedBy(e.getCreatedBy());
        if (e.getCreatedDateTime()  != null) d.setCreatedDateTime(e.getCreatedDateTime().toLocalDate());
        d.setUpdatedBy(e.getUpdatedBy());
        if (e.getUpdatedDatetime()  != null) d.setUpdatedDatetime(e.getUpdatedDatetime().toLocalDate());
        return d;
    }

    // ──────────────────────────────────────────────
    // Utility helpers
    // ──────────────────────────────────────────────

    private boolean isPending(String status) {
        return status != null && status.trim().toLowerCase().startsWith("pending");
    }
private String normalizeModel(String model) {
    if (model == null) return "NA";

    String m = model.trim();
    if (m.isEmpty()) return "NA";

    String normalized = m.replaceAll("[^A-Za-z0-9]", "");
    if ("NA".equalsIgnoreCase(normalized)) {
        return "NA";
    }

    return m;
}

    private String generateProcessId() {
        String ts = String.valueOf(System.currentTimeMillis());
        return ts.substring(Math.max(0, ts.length() - 6)) + (int)(Math.random() * 10);
    }

    private String resolveInsertedBy(String primary, String fallback) {
        if (primary != null && !primary.trim().isEmpty()) return primary.trim();
        if (fallback != null && !fallback.trim().isEmpty()) return fallback.trim();
        return "System";
    }

    private String resolveRequestedBy(String explicit, String updatedBy, String createdBy) {
        if (explicit != null && !explicit.trim().isEmpty()) return explicit.trim();
        return resolveInsertedBy(updatedBy, createdBy);
    }

    private Map<String, String> successMap(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("status", "Success"); m.put("message", message); return m;
    }

    private Map<String, String> errorMap(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("status", "Error"); m.put("message", message); return m;
    }

    private Map<String, String> successResultWithContext(String message, String poNumber, Long recordNo) {
        Map<String, String> m = successMap(message);
        if (poNumber != null)  m.put("poNumber",  poNumber);
        if (recordNo  != null) m.put("recordNo",  String.valueOf(recordNo));
        return m;
    }

    private Map<String, String> errorResultWithContext(String message, String poNumber, Long recordNo) {
        Map<String, String> m = errorMap(message);
        if (poNumber != null)  m.put("poNumber",  poNumber);
        if (recordNo  != null) m.put("recordNo",  String.valueOf(recordNo));
        return m;
    }

    private String extractStringField(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private String requireString(Map<String, Object> map, String key) {
        String v = extractStringField(map, key);
        if (v == null || v.isEmpty())
            throw new IllegalArgumentException("Missing required field: " + key);
        return v;
    }

    private Long parseLong(Object obj) {
        if (obj == null) return null;
        try { return Long.parseLong(obj.toString()); } catch (NumberFormatException e) { return null; }
    }

    private void validateRequestType(String value, String... allowed) {
        for (String a : allowed) {
            if (a.equalsIgnoreCase(value)) return;
        }
        throw new IllegalArgumentException("Invalid requestType '" + value
                + "'. Must be one of: " + Arrays.toString(allowed));
    }
}