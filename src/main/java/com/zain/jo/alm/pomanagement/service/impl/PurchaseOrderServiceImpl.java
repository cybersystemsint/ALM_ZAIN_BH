package com.zain.jo.alm.pomanagement.service.impl;

import com.zain.jo.alm.pomanagement.dto.PurchaseOrderDto;
import com.zain.jo.alm.pomanagement.dto.request.BulkDeleteRequest;
import com.zain.jo.alm.pomanagement.dto.request.SearchRequest;
import com.zain.jo.alm.pomanagement.dto.response.PageResult;
import com.zain.jo.alm.pomanagement.dto.response.WorkflowActionResponse;
import com.zain.jo.alm.pomanagement.entity.PurchaseOrder;
import com.zain.jo.alm.pomanagement.entity.Workflow;
import com.zain.jo.alm.pomanagement.repository.PoItemRepository;
import com.zain.jo.alm.pomanagement.repository.PurchaseOrderRepository;
import com.zain.jo.alm.pomanagement.repository.WorkflowRepository;
import com.zain.jo.alm.pomanagement.service.PurchaseOrderService;
import com.zain.jo.alm.pomanagement.specification.PurchaseOrderSpecification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderServiceImpl.class);

    private final PurchaseOrderRepository purchaseOrderRepo;
    private final PoItemRepository        poItemRepo;
    private final WorkflowRepository      workflowRepo;

    @Autowired
    public PurchaseOrderServiceImpl(PurchaseOrderRepository purchaseOrderRepo,
                                    PoItemRepository poItemRepo,
                                    WorkflowRepository workflowRepo) {
        this.purchaseOrderRepo = purchaseOrderRepo;
        this.poItemRepo        = poItemRepo;
        this.workflowRepo      = workflowRepo;
    }

    // ──────────────────────────────────────────────
    // Queries
    // ──────────────────────────────────────────────

    @Override
    public PageResult<PurchaseOrderDto> getPurchaseOrders(SearchRequest request) {
        int page = request.getPage() == null ? 0 : Math.max(0, request.getPage());
        int size = request.getSize() == null ? 100 : (request.getSize() <= 0 ? 100 : request.getSize());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<PurchaseOrder> spec = PurchaseOrderSpecification.fromSearchRequest(request);

        Page<PurchaseOrder> resultPage = (spec == null)
                ? purchaseOrderRepo.findAll(pageable)
                : purchaseOrderRepo.findAll(spec, pageable);

        List<PurchaseOrderDto> dtos = resultPage.stream().map(this::toDto).collect(Collectors.toList());
        return new PageResult<>(
                resultPage.getTotalElements(), resultPage.getTotalPages(),
                resultPage.getNumber(),        resultPage.getSize(), dtos);
    }

    // ──────────────────────────────────────────────
    // Mutations – single record
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public Map<String, String> createPONumbers(String rawJsonArray) {
        List<String> created = new ArrayList<>();
        List<String> errors  = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(rawJsonArray);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (!obj.has("poNumber")) {
                    errors.add("poNumber is required at index " + i);
                    continue;
                }
                String poNumber  = obj.getString("poNumber").trim();
                String createdBy = obj.optString("createdBy", "System").trim();
                if (poNumber.isEmpty()) { errors.add("poNumber cannot be empty at index " + i); continue; }
                if (createdBy.isEmpty()) createdBy = "System";

                if (purchaseOrderRepo.findByPoNumber(poNumber) != null) {
                    errors.add("PO number already exists: " + poNumber);
                    continue;
                }
                try {
                    PurchaseOrder po = new PurchaseOrder();
                    po.setPoNumber(poNumber);
                    po.setApprovalStatus("Pending Addition");
                    po.setCreatedBy(createdBy);
                    purchaseOrderRepo.save(po);

                    Workflow wf = buildWorkflow(poNumber, "Pending Addition", createdBy);
                    workflowRepo.save(wf);
                    created.add(poNumber);
                } catch (Exception ex) {
                    logger.error("Failed to save PO number {}", poNumber, ex);
                    errors.add("Failed to save: " + poNumber);
                }
            }
        } catch (JSONException e) {
            return errorMap("Invalid JSON: " + e.getMessage());
        }
        if (!errors.isEmpty()) {
            return errorMap("Some errors occurred: " + String.join(", ", errors));
        }
        return successMap("Created PO number(s): " + String.join(", ", created));
    }

    @Override
    @Transactional
    public Map<String, String> updatePONumber(String poNumber, Map<String, Object> payload) {
        if (poNumber == null || poNumber.trim().isEmpty())
            return errorMap("PO number is required");

        PurchaseOrder existing = purchaseOrderRepo.findByPoNumber(poNumber.trim());
        if (existing == null)
            return errorMap("PO number '" + poNumber + "' does not exist");

        String updatedBy = extractString(payload, "updatedBy");
        if (updatedBy == null || updatedBy.isEmpty())
            return errorMap("updatedBy is required in payload");

        if (payload.containsKey("approvalStatus") && payload.get("approvalStatus") != null) {
            existing.setApprovalStatus(payload.get("approvalStatus").toString());
        }
        existing.setUpdatedBy(updatedBy);
        purchaseOrderRepo.save(existing);

        Workflow wf = buildWorkflow(poNumber, existing.getApprovalStatus(), updatedBy);
        workflowRepo.save(wf);

        return successMap("PO number '" + poNumber + "' updated");
    }

    @Override
    @Transactional
    public Map<String, String> requestSingleDelete(String poNumber, Map<String, Object> payload) {
        if (poNumber == null || poNumber.trim().isEmpty())
            return errorMap("PO number is required");

        String poNum = poNumber.trim();
        PurchaseOrder existing = purchaseOrderRepo.findByPoNumber(poNum);
        if (existing == null)
            return errorMap("PO number '" + poNum + "' does not exist");

        long unapproved = poItemRepo.countNotApprovedItems(poNum);
        if (unapproved > 0) {
            return errorMap("PO number '" + poNum + "' contains " + unapproved
                    + " item(s) that are not 'Approved'. All items must be approved before deletion.");
        }

        String updatedBy = (payload != null) ? extractString(payload, "updatedBy") : null;
        int updatedRows  = purchaseOrderRepo.markPendingDeletionIfNotPending(poNum, "Pending Deletion", updatedBy);
        if (updatedRows == 0)
            return errorMap("PO number '" + poNum + "' is already in a pending state and cannot be deleted");

        String insertedBy = updatedBy != null ? updatedBy
                : resolveInsertedBy(existing.getUpdatedBy(), existing.getCreatedBy());
        workflowRepo.save(buildWorkflow(poNum, "Pending Deletion", insertedBy));

        return successMap("PO number '" + poNum + "' is marked for deletion");
    }

    // ──────────────────────────────────────────────
    // Mutations – bulk
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public Map<String, Object> processBulkDelete(BulkDeleteRequest request) {
        Map<String, Object> result   = new HashMap<>();
        List<Map<String, Object>> details = new ArrayList<>();
        int successCount = 0;

        List<Long> recordNos  = request.getRecordNos();
        String     requestedBy = (request.getRequestedBy() != null && !request.getRequestedBy().trim().isEmpty())
                ? request.getRequestedBy().trim() : null;

        if (recordNos == null || recordNos.isEmpty()) {
            result.put("status",  "Error");
            result.put("message", "recordNos is required and cannot be empty");
            return result;
        }

        for (Long id : recordNos) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("recordNo", id);
            try {
                PurchaseOrder po = purchaseOrderRepo.findById(id).orElse(null);
                if (po == null) {
                    entry.put("status",  "Error");
                    entry.put("message", "PurchaseOrder with id " + id + " not found");
                    details.add(entry);
                    continue;
                }
                String poNum = po.getPoNumber();

                long notApproved = poItemRepo.countNotApprovedItems(poNum);
                if (notApproved > 0) {
                    entry.put("status",  "Error");
                    entry.put("message", "Contains " + notApproved + " item(s) not 'Approved'");
                    details.add(entry);
                    continue;
                }

                int updated = purchaseOrderRepo.markPendingDeletionIfNotPendingById(id, "Pending Deletion", requestedBy);
                if (updated == 0) {
                    entry.put("status",  "Error");
                    entry.put("message", "Already in a pending state or cannot be updated");
                    details.add(entry);
                    continue;
                }

                String insertedBy = requestedBy != null ? requestedBy
                        : resolveInsertedBy(po.getUpdatedBy(), po.getCreatedBy());

                workflowRepo.save(buildWorkflow(poNum, "Pending Deletion", insertedBy));
                entry.put("status",  "Success");
                entry.put("message", "Marked for deletion");
                details.add(entry);
                successCount++;

            } catch (Exception ex) {
                entry.put("status",  "Error");
                entry.put("message", "Exception: " + ex.getMessage());
                details.add(entry);
            }
        }

        result.put("status",    successCount == recordNos.size() ? "Success" : "Partial");
        result.put("requested", recordNos.size());
        result.put("succeeded", successCount);
        result.put("failed",    recordNos.size() - successCount);
        result.put("details",   details);
        return result;
    }

    // ──────────────────────────────────────────────
    // Approval / Rejection
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public WorkflowActionResponse processWorkflowAction(Map<String, Object> requestBody, String action) {
        WorkflowActionResponse response = new WorkflowActionResponse();
        List<Map<String, String>> results = new ArrayList<>();

        String changedBy = extractString(requestBody, "changedBy");
        if (changedBy == null || changedBy.isEmpty()) {
            response.setStatus("Error");
            response.setMessage("changedBy is required in request payload");
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
                Object poObj = row.get("poNumber");
                if (poObj == null || poObj.toString().trim().isEmpty())
                    throw new IllegalArgumentException("poNumber is missing in row.");
                String poNumber    = poObj.toString().trim();
                Object rtObj       = row.get("requestType");
                if (rtObj == null || rtObj.toString().trim().isEmpty())
                    throw new IllegalArgumentException("requestType is missing for poNumber: " + poNumber);
                String requestType = rtObj.toString().trim();
                if (!"addition".equalsIgnoreCase(requestType) && !"deletion".equalsIgnoreCase(requestType))
                    throw new IllegalArgumentException("Invalid requestType '" + requestType
                            + "'. Must be 'addition' or 'deletion'.");

                result.put("poNumber", poNumber);
                Map<String, String> actionResult = dispatchPOAction(poNumber, requestType, action, changedBy);
                result.putAll(actionResult);
            } catch (Exception e) {
                hasErrors = true;
                result.put("status",  "Error");
                result.put("message", e.getMessage());
            }
            results.add(result);
        }

        response.setResults(results);
        response.setStatus(hasErrors ? "PartialFailure" : "Success");
        return response;
    }

    private Map<String, String> dispatchPOAction(String poNumber, String requestType,
                                                  String action, String changedBy) {
        switch (requestType.toLowerCase()) {
            case "addition": return processAddition(poNumber, action, changedBy);
            case "deletion": return processDeletion(poNumber, action, changedBy);
            default:
                return errorMapWithPO("Invalid requestType: " + requestType, poNumber);
        }
    }

    private Map<String, String> processAddition(String poNumber, String action, String changedBy) {
        PurchaseOrder po = purchaseOrderRepo.findByPoNumber(poNumber);
        if (po == null)
            return errorMapWithPO("PO Number does not exist: " + poNumber, poNumber);
        if (!"Pending Addition".equals(po.getApprovalStatus()))
            return errorMapWithPO("PO Number is not in 'Pending Addition' status: " + poNumber, poNumber);

        if ("approve".equalsIgnoreCase(action)) {
            po.setApprovalStatus("Approved");
            purchaseOrderRepo.save(po);
            updateWorkflow(poNumber, "Pending Addition", "Addition Approved", "PO number addition approved.", changedBy);
            return successMapWithPO("PO number approved.", poNumber);
        } else {
            purchaseOrderRepo.delete(po);
            updateWorkflow(poNumber, "Pending Addition", "Addition Rejected", "PO number addition rejected and deleted.", changedBy);
            return successMapWithPO("PO number rejected and deleted.", poNumber);
        }
    }

    private Map<String, String> processDeletion(String poNumber, String action, String changedBy) {
        PurchaseOrder po = purchaseOrderRepo.findByPoNumber(poNumber);
        if (po == null)
            return errorMapWithPO("PO Number does not exist: " + poNumber, poNumber);
        if (!"Pending Deletion".equals(po.getApprovalStatus()))
            return errorMapWithPO("PO Number is not in 'Pending Deletion' status: " + poNumber, poNumber);

        if ("approve".equalsIgnoreCase(action)) {
            purchaseOrderRepo.delete(po);
            updateWorkflow(poNumber, "Pending Deletion", "Deletion Approved", "PO number deletion approved and removed.", changedBy);
            return successMapWithPO("PO number deletion approved and removed.", poNumber);
        } else {
            po.setApprovalStatus("Approved");
            purchaseOrderRepo.save(po);
            updateWorkflow(poNumber, "Pending Deletion", "Deletion Rejected", "PO number deletion rejected.", changedBy);
            return successMapWithPO("PO number deletion rejected.", poNumber);
        }
    }

    // ──────────────────────────────────────────────
    // Workflow helpers
    // ──────────────────────────────────────────────

    private Workflow buildWorkflow(String poNumber, String originalStatus, String insertedBy) {
        Workflow wf = new Workflow();
        wf.setPoNumber(poNumber);
        wf.setOriginalStatus(originalStatus);
        wf.setProcessId(generateProcessId());
        wf.setInsertedBy(insertedBy != null && !insertedBy.trim().isEmpty() ? insertedBy : "System");
        wf.setInsertDate(LocalDateTime.now());
        return wf;
    }

    private void updateWorkflow(String poNumber, String originalStatus,
                                String updatedStatus, String comments, String changedBy) {
        List<Workflow> workflows = workflowRepo.findByPoNumberAndOriginalStatus(poNumber, originalStatus);
        if (!workflows.isEmpty()) {
            for (Workflow wf : workflows) {
                wf.setUpdatedStatus(updatedStatus);
                wf.setChangedBy(changedBy != null ? changedBy : "System");
                wf.setChangeDate(LocalDateTime.now());
                wf.setComments(comments);
                workflowRepo.save(wf);
            }
        } else {
            logger.warn("No existing workflow for PO {} with original status {}", poNumber, originalStatus);
            Workflow wf = buildWorkflow(poNumber, originalStatus, "System");
            wf.setUpdatedStatus(updatedStatus);
            wf.setChangedBy(changedBy != null ? changedBy : "System");
            wf.setChangeDate(LocalDateTime.now());
            wf.setComments(comments);
            workflowRepo.save(wf);
        }
    }

    // ──────────────────────────────────────────────
    // Mapping / utilities
    // ──────────────────────────────────────────────

    private PurchaseOrderDto toDto(PurchaseOrder e) {
        PurchaseOrderDto d = new PurchaseOrderDto();
        d.setId(e.getId());
        d.setPoNumber(e.getPoNumber());
        d.setApprovalStatus(e.getApprovalStatus());
        d.setCreatedBy(e.getCreatedBy());
        if (e.getCreatedAt() != null) d.setCreatedAt(e.getCreatedAt().toLocalDate());
        d.setUpdatedBy(e.getUpdatedBy());
        if (e.getUpdatedAt() != null) d.setUpdatedAt(e.getUpdatedAt().toLocalDate());
        return d;
    }

    private String generateProcessId() {
        String ts = String.valueOf(System.currentTimeMillis());
        return ts.substring(Math.max(0, ts.length() - 6)) + (int)(Math.random() * 10);
    }

    private String resolveInsertedBy(String primary, String fallback) {
        if (primary  != null && !primary.trim().isEmpty())  return primary.trim();
        if (fallback != null && !fallback.trim().isEmpty()) return fallback.trim();
        return "System";
    }

    private String extractString(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private Map<String, String> successMap(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("status", "Success"); m.put("message", message); return m;
    }

    private Map<String, String> errorMap(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("status", "Error"); m.put("message", message); return m;
    }

    private Map<String, String> successMapWithPO(String message, String poNumber) {
        Map<String, String> m = successMap(message);
        m.put("poNumber", poNumber); return m;
    }

    private Map<String, String> errorMapWithPO(String message, String poNumber) {
        Map<String, String> m = errorMap(message);
        m.put("poNumber", poNumber); return m;
    }
}