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

import com.telkom.almBHZain.model.tb_Po;
import com.telkom.almBHZain.repo.tbPoRepo;

@Service
public class TbPoService {

    @Autowired
    private tbPoRepo poRepo;

public Map<String, Object> getPoItems(Integer page, int size, String searchTerm, Long afterId, String supplierId) {
    Map<String, Object> result = new HashMap<>();
    // Normalize supplierId
    boolean hasSupplier = org.springframework.util.StringUtils.hasText(supplierId) && !"0".equals(supplierId);
    if (size < 1) size = 100;
    if (size > 500) size = 500;

    String searchTermNorm = (searchTerm == null || searchTerm.isBlank()) ? null : searchTerm.toLowerCase();

    if (afterId != null) {
        // Keyset pagination
        Pageable pageable = PageRequest.of(0, size, Sort.by("recordNo").descending());
        Page<tb_Po> pageResult = poRepo.findAfterId(
                hasSupplier ? supplierId : null,
                searchTermNorm,
                afterId,
                pageable
        );
        List<tb_Po> list = pageResult.getContent();
        boolean hasMore = list.size() == size;
        Long nextAfterId = !list.isEmpty() ? list.get(list.size() - 1).getRecordNo() : null;

        result.put("data", list);
        result.put("hasMore", hasMore);
        result.put("nextAfterId", nextAfterId);
        result.put("status", "success");
    } else {
        // Offset pagination
        int pageNo = (page == null || page < 0) ? 0 : page;
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by("recordNo").descending());
        Page<tb_Po> poPage = poRepo.searchAllColumns(
                hasSupplier ? supplierId : null,
                searchTermNorm,
                pageable
        );
        result.put("data", poPage.getContent());
        result.put("currentPage", poPage.getNumber());
        result.put("totalItems", poPage.getTotalElements());
        result.put("totalPages", poPage.getTotalPages());
        result.put("hasNext", poPage.hasNext());
        // For infinite scroll UI
        List<tb_Po> items = poPage.getContent();
        result.put("nextAfterId", !items.isEmpty() ? items.get(items.size() - 1).getRecordNo() : null);
        result.put("status", "success");
    }
    return result;
}
}
