package com.telkom.almBHZain.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.telkom.almBHZain.model.Workflow;

public interface WorkflowRepositoryCustom {
    Page<Workflow> searchByColumn(boolean updatedStatusIsNull, String columnName, String searchQuery, Pageable pageable);
}
