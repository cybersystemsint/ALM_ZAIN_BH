package com.telkom.almBHZain.service;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.telkom.almBHZain.dto.BulkDeleteRequest;
import com.telkom.almBHZain.dto.PurchaseOrderDto;
import com.telkom.almBHZain.dto.Request.SearchRequest;
import com.telkom.almBHZain.dto.Response.PageResult;
import com.telkom.almBHZain.model.PurchaseOrder;
import com.telkom.almBHZain.model.Workflow;
import com.telkom.almBHZain.repository.POItemRepository;
import com.telkom.almBHZain.repository.PurchaseOrderRepository;
import com.telkom.almBHZain.repository.WorkflowRepository;
import com.telkom.almBHZain.specification.PurchaseOrderSpecification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository repo;
       private final PurchaseOrderRepository purchaseOrderRepository;
    private final POItemRepository poItemRepository;
    private final WorkflowRepository workflowRepository;


    @Autowired
    public PurchaseOrderService(PurchaseOrderRepository repo, POItemRepository poItemRepository, WorkflowRepository workflowRepository) {
        this.repo = repo;
        this.purchaseOrderRepository = repo;
        this.poItemRepository = poItemRepository;
        this.workflowRepository = workflowRepository;
    }

    public PageResult<PurchaseOrderDto> getPurchaseOrders(SearchRequest request) {
        int page = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 100 : request.getSize();
        if (page < 0) page = 0;
        if (size <= 0) size = 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<PurchaseOrder> spec = PurchaseOrderSpecification.fromSearchRequest(request);

        Page<PurchaseOrder> resultPage;
        if (spec == null) {
            resultPage = repo.findAll(pageable);
        } else {
            resultPage = repo.findAll(spec, pageable);
        }

        List<PurchaseOrderDto> dtos = resultPage.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PageResult<>(
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.getNumber(),
                resultPage.getSize(),
                dtos
        );
    }

private PurchaseOrderDto toDto(PurchaseOrder e) {
    PurchaseOrderDto d = new PurchaseOrderDto();
    d.setId(e.getId());
    d.setPoNumber(e.getPoNumber());
    d.setApprovalStatus(e.getApprovalStatus());
    d.setCreatedBy(e.getCreatedBy());
    // convert createdAt (LocalDateTime) to LocalDate (null-safe)
    if (e.getCreatedAt() != null) {
        d.setCreatedAt(e.getCreatedAt().toLocalDate());
    }
    d.setUpdatedBy(e.getUpdatedBy());
    if (e.getUpdatedAt() != null) {
        d.setUpdatedAt(e.getUpdatedAt().toLocalDate());
    }
    return d;
}

  @Transactional
    public Map<String, Object> processBulkDelete(BulkDeleteRequest request) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> details = new ArrayList<>();
        int successCount = 0;

        List<Long> recordNos = request.getRecordNos();
        String requestedBy = (request.getRequestedBy() != null && !request.getRequestedBy().trim().isEmpty())
                ? request.getRequestedBy().trim()
                : null;

        if (recordNos == null || recordNos.isEmpty()) {
            result.put("status", "Error");
            result.put("message", "recordNos is required and cannot be empty");
            return result;
        }

        for (Long id : recordNos) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("recordNo", id);

            try {
                PurchaseOrder po = purchaseOrderRepository.findById(id).orElse(null);
                if (po == null) {
                    entry.put("status", "Error");
                    entry.put("message", "PurchaseOrder with id " + id + " not found");
                    details.add(entry);
                    continue;
                }

                String poNum = po.getPoNumber();

                // 1) ensure no unapproved items
                long notApprovedCount = poItemRepository.countNotApprovedItems(poNum);
                if (notApprovedCount > 0) {
                    entry.put("status", "Error");
                    entry.put("message", "Contains " + notApprovedCount + " item(s) not 'Approved'");
                    details.add(entry);
                    continue;
                }

                // 2) attempt atomic update (mark pending) using repository method
                int updatedRows = purchaseOrderRepository.markPendingDeletionIfNotPendingById(id, "Pending Deletion", requestedBy);
                if (updatedRows == 0) {
                    // nothing updated -> already pending or changed concurrently
                    entry.put("status", "Error");
                    entry.put("message", "Already in a pending state or cannot be updated");
                    details.add(entry);
                    continue;
                }

                // insert workflow using the preferred insertedBy (requestedBy -> po.updatedBy -> po.createdBy -> System)
                String insertedBy = (requestedBy != null)
                        ? requestedBy
                        : (po.getUpdatedBy() != null && !po.getUpdatedBy().trim().isEmpty()
                            ? po.getUpdatedBy()
                            : (po.getCreatedBy() != null && !po.getCreatedBy().trim().isEmpty()
                                ? po.getCreatedBy()
                                : "System"));

                Workflow workflow = new Workflow();
                workflow.setPoNumber(poNum);
                workflow.setOriginalStatus("Pending Deletion");
                workflow.setProcessId(generateProcessId());
                workflow.setInsertedBy(insertedBy);
                workflow.setInsertDate(new Date());
                workflowRepository.save(workflow);

                entry.put("status", "Success");
                entry.put("message", "Marked for deletion");
                details.add(entry);
                successCount++;

            } catch (Exception ex) {
                entry.put("status", "Error");
                entry.put("message", "Exception: " + ex.getMessage());
                details.add(entry);
                // continue processing other ids
            }
        }

        result.put("status", successCount == recordNos.size() ? "Success" : "Partial");
        result.put("requested", recordNos.size());
        result.put("succeeded", successCount);
        result.put("failed", recordNos.size() - successCount);
        result.put("details", details);
        return result;
    }

    private String generateProcessId() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomDigit = String.valueOf((int) (Math.random() * 10));
        return timestamp.substring(Math.max(0, timestamp.length() - 6)) + randomDigit;
    }
}