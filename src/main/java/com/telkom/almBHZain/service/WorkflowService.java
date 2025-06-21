package com.telkom.almBHZain.service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.telkom.almBHZain.model.Workflow;
import com.telkom.almBHZain.repo.WorkflowRepository;

@Service
public class WorkflowService {

    @Autowired
    private WorkflowRepository workflowRepository;

    public Map<String, Object> getWorkflowsHybrid(boolean updatedStatusIsNull, Integer page, int size, String searchTerm, Long afterId) {
        Map<String, Object> result = new HashMap<>();
        String search = StringUtils.hasText(searchTerm) ? searchTerm.toLowerCase() : null;

        if (afterId != null) {
            Pageable pageable = PageRequest.of(0, size, Sort.by("id").descending());
            Page<Workflow> pageResult = workflowRepository.findAfterId(updatedStatusIsNull, search, afterId, pageable);
            List<Workflow> list = pageResult.getContent();
            result.put("data", list);
            result.put("nextAfterId", !list.isEmpty() ? list.get(list.size() - 1).getId() : null);
            result.put("paginationType", "keyset");
            result.put("totalItems", pageResult.getTotalElements());
            result.put("totalPages", pageResult.getTotalPages());
        } else {
            int pageNo = page == null ? 0 : page;
            Pageable pageable = PageRequest.of(pageNo, size, Sort.by("id").descending());
            Page<Workflow> workflowPage = workflowRepository.searchAllColumns(updatedStatusIsNull, search, pageable);
            result.put("data", workflowPage.getContent());
            result.put("currentPage", workflowPage.getNumber());
            result.put("totalItems", workflowPage.getTotalElements());
            result.put("totalPages", workflowPage.getTotalPages());
            result.put("paginationType", "offset");
        }
        return result;
    }
}
