package com.telkom.almBHZain.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.telkom.almBHZain.dto.PageResult;
import com.telkom.almBHZain.dto.SearchRequest;
import com.telkom.almBHZain.model.Workflow;
import com.telkom.almBHZain.repo.WorkflowRepository;

@Service
public class WorkflowService {

    @Autowired
    private WorkflowRepository workflowRepository;


    public PageResult<Workflow> getWorkflows(boolean updatedStatusIsNull, SearchRequest request) {
        int pageNo = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 100 : request.getSize();
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by("id").descending());
        Page<Workflow> workflowPage;

        // Keyset pagination not included for simplicity; add if needed

        if (request.getSearchTerm() != null && !request.getSearchTerm().isEmpty()) {
            workflowPage = workflowRepository.searchAllColumns(
                    updatedStatusIsNull,
                    request.getSearchTerm().toLowerCase(),
                    pageable
            );
        } else if (
            request.getSearchQuery() != null &&
            request.getColumnName() != null &&
            !request.getSearchQuery().isEmpty() &&
            !request.getColumnName().isEmpty()
        ) {
            workflowPage = workflowRepository.searchByColumn(
                    updatedStatusIsNull,
                    request.getColumnName(),
                    request.getSearchQuery(),
                    pageable
            );
        } else {
            workflowPage = workflowRepository.findAllByUpdatedStatusIsNullOrNot(updatedStatusIsNull, pageable);
        }

        return new PageResult<>(
                workflowPage.getTotalElements(),
                workflowPage.getTotalPages(),
                pageNo,
                size,
                workflowPage.getContent()
        );
    }
}
