package com.telkom.almBHZain.service;
import com.telkom.almBHZain.dto.WorkflowDto;
import com.telkom.almBHZain.dto.Response.PageResult;
import com.telkom.almBHZain.dto.Request.SearchRequest;
import com.telkom.almBHZain.model.Workflow;
import com.telkom.almBHZain.repository.WorkflowRepository;
import com.telkom.almBHZain.specification.WorkflowSpecification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkflowService {

    @Autowired
    private WorkflowRepository workflowRepository;

    public PageResult<WorkflowDto> getWorkflows(boolean updatedStatusIsNull, SearchRequest request) {
        int pageNo = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 100 : request.getSize();
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by("id").descending());

        Specification<Workflow> spec = WorkflowSpecification.fromSearchRequest(updatedStatusIsNull, request);

        Page<Workflow> workflowPage = workflowRepository.findAll(spec, pageable);

        List<WorkflowDto> dtos = workflowPage.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PageResult<>(
                workflowPage.getTotalElements(),
                workflowPage.getTotalPages(),
                workflowPage.getNumber(),
                workflowPage.getSize(),
                dtos
        );
    }

    private WorkflowDto toDto(Workflow w) {
        WorkflowDto d = new WorkflowDto();
        // d.setId(w.getId());
        d.setPoNumber(w.getPoNumber());
        d.setRecordNo(w.getRecordNo());
        d.setOldPoNumber(w.getOldPoNumber());
        d.setNewPoNumber(w.getNewPoNumber());
        d.setOriginalStatus(w.getOriginalStatus());
        d.setUpdatedStatus(w.getUpdatedStatus());
        d.setProcessId(w.getProcessId());
        d.setInsertedBy(w.getInsertedBy());

        // convert java.util.Date -> LocalDate (null-safe)
        Date ins = w.getInsertDate();
        if (ins != null) {
            d.setInsertDate(ins.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        d.setChangedBy(w.getChangedBy());
        Date ch = w.getChangeDate();
        if (ch != null) {
            d.setChangeDate(ch.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        d.setComments(w.getComments());
        return d;
    }
}