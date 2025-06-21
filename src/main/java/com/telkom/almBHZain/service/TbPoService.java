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

import com.telkom.almBHZain.model.tb_Po;
import com.telkom.almBHZain.repo.tbPoRepo;

@Service
public class TbPoService {

    @Autowired
    private tbPoRepo poRepo;

    public Map<String, Object> getPoItemsHybrid(Integer page, int size, String searchTerm, Long afterId, String supplierId) {
        Map<String, Object> result = new HashMap<>();
        // Normalize supplierId for logic
        boolean hasSupplier = StringUtils.hasText(supplierId) && !"0".equals(supplierId);

        if (afterId != null) {
            // Keyset
            Pageable pageable = PageRequest.of(0, size, Sort.by("recordNo").descending());
            Page<tb_Po> pageResult = poRepo.findAfterId(
                hasSupplier ? supplierId : null,
                searchTerm == null ? null : searchTerm.toLowerCase(),
                afterId,
                pageable
            );
            List<tb_Po> list = pageResult.getContent();
            result.put("data", list);
            result.put("nextAfterId", !list.isEmpty() ? list.get(list.size() - 1).getRecordNo() : null);
            result.put("totalItems", pageResult.getTotalElements());
            result.put("totalPages", pageResult.getTotalPages());
        } else {
            // Offset
            int pageNo = page == null ? 0 : page;
            Pageable pageable = PageRequest.of(pageNo, size, Sort.by("recordNo").descending());
            Page<tb_Po> poPage = poRepo.searchAllColumns(
                hasSupplier ? supplierId : null,
                searchTerm == null ? null : searchTerm.toLowerCase(),
                pageable
            );
            result.put("data", poPage.getContent());
            result.put("currentPage", poPage.getNumber());
            result.put("totalItems", poPage.getTotalElements());
            result.put("totalPages", poPage.getTotalPages());
        }
        return result;
    }
}
