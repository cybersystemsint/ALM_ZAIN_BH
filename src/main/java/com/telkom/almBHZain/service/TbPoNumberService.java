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

import com.telkom.almBHZain.model.tbPoNumber;
import com.telkom.almBHZain.repo.tbPoNumberRepo;


@Service
public class TbPoNumberService {

    @Autowired
    private tbPoNumberRepo poNumberRepo;

public Map<String, Object> getPoNumbers(Integer page, int size, String searchTerm, Long afterId) {
    Map<String, Object> result = new HashMap<>();
    if (size < 1) size = 100;
    if (size > 500) size = 500;
    String searchTermNorm = (searchTerm == null || searchTerm.isBlank()) ? null : searchTerm.toLowerCase();

    if (afterId != null) {
        // Keyset pagination (fast for deep pages)
        Pageable pageable = PageRequest.of(0, size, Sort.by("id").descending());
        Page<tbPoNumber> pageResult = poNumberRepo.findAfterId(
            searchTermNorm,
            afterId,
            pageable
        );
        List<tbPoNumber> list = pageResult.getContent();
        boolean hasMore = list.size() == size;
        Long nextAfterId = !list.isEmpty() ? list.get(list.size() - 1).getId() : null;

        result.put("data", list);
        result.put("hasMore", hasMore);
        result.put("nextAfterId", nextAfterId);
        result.put("status", "success");
    } else {
        // Offset pagination (for shallow pages)
        int pageNo = (page == null || page < 0) ? 0 : page;
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by("id").descending());
        Page<tbPoNumber> poNumbersPage = (searchTermNorm != null)
            ? poNumberRepo.searchAllColumns(searchTermNorm, pageable)
            : poNumberRepo.findAll(pageable);

        result.put("data", poNumbersPage.getContent());
        result.put("currentPage", poNumbersPage.getNumber());
        result.put("totalItems", poNumbersPage.getTotalElements());
        result.put("totalPages", poNumbersPage.getTotalPages());
        result.put("hasNext", poNumbersPage.hasNext());
        // For infinite scroll UI
        List<tbPoNumber> items = poNumbersPage.getContent();
        result.put("nextAfterId", !items.isEmpty() ? items.get(items.size() - 1).getId() : null);
        result.put("status", "success");
    }
    return result;
}
}
