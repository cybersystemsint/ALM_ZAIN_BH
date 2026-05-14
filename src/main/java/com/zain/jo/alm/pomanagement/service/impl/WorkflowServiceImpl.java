package com.zain.jo.alm.pomanagement.service.impl;

import com.zain.jo.alm.pomanagement.dto.WorkflowDto;
import com.zain.jo.alm.pomanagement.dto.request.SearchRequest;
import com.zain.jo.alm.pomanagement.dto.response.PageResult;
import com.zain.jo.alm.pomanagement.entity.Workflow;
import com.zain.jo.alm.pomanagement.repository.WorkflowRepository;
import com.zain.jo.alm.pomanagement.service.WorkflowService;
import com.zain.jo.alm.pomanagement.specification.WorkflowSpecification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRepository workflowRepo;

    @Autowired
    public WorkflowServiceImpl(WorkflowRepository workflowRepo) {
        this.workflowRepo = workflowRepo;
    }

    @Override
    public PageResult<WorkflowDto> getWorkflows(boolean pendingOnly, SearchRequest request) {
        int page = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 100 : request.getSize();
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Specification<Workflow> spec = WorkflowSpecification.fromSearchRequest(pendingOnly, request);
        Page<Workflow> pageResult    = workflowRepo.findAll(spec, pageable);

        List<WorkflowDto> dtos = pageResult.stream().map(this::toDto).collect(Collectors.toList());

        return new PageResult<>(
                pageResult.getTotalElements(), pageResult.getTotalPages(),
                pageResult.getNumber(),        pageResult.getSize(), dtos);
    }

    private WorkflowDto toDto(Workflow w) {
        WorkflowDto d = new WorkflowDto();
        d.setPoNumber(w.getPoNumber());
        d.setRecordNo(w.getRecordNo());
        d.setOldPoNumber(w.getOldPoNumber());
        d.setNewPoNumber(w.getNewPoNumber());
        d.setOriginalStatus(w.getOriginalStatus());
        d.setUpdatedStatus(w.getUpdatedStatus());
        d.setProcessId(w.getProcessId());
        d.setInsertedBy(w.getInsertedBy());
        d.setChangedBy(w.getChangedBy());
        d.setComments(w.getComments());

        LocalDateTime ins = w.getInsertDate();
        if (ins != null) d.setInsertDate(ins.toLocalDate());

        LocalDateTime ch = w.getChangeDate();
        if (ch != null) d.setChangeDate(ch.toLocalDate());

        return d;
    }
}