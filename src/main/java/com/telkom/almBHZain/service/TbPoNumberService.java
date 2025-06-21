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

import com.telkom.almBHZain.model.tbPoNumber;
import com.telkom.almBHZain.repo.tbPoNumberRepo;


@Service
public class TbPoNumberService {

    @Autowired
    private tbPoNumberRepo poNumberRepo;

    // Threshold: switch to keyset after 5000 records (not currently used, but kept for possible extension)
    private static final int KEYSET_THRESHOLD = 5000;

    public Map<String, Object> getPoNumbersHybrid(Integer page, int size, String searchTerm, Long afterId) {
        Map<String, Object> result = new HashMap<>();

 if (afterId != null) {
    // Keyset pagination (fast for deep pages)
    Pageable pageable = PageRequest.of(0, size, Sort.by("id").descending());
    Page<tbPoNumber> pageResult = poNumberRepo.findAfterId(
        searchTerm == null ? null : searchTerm.toLowerCase(),
        afterId,
        pageable
    );
    List<tbPoNumber> list = pageResult.getContent();

    result.put("data", list);
    result.put("nextAfterId", !list.isEmpty() ? list.get(list.size() - 1).getId() : null);
    result.put("prevAfterId", !list.isEmpty() ? list.get(0).getId() : null);
    result.put("totalItems", pageResult.getTotalElements());
    result.put("totalPages", pageResult.getTotalPages());
} else {
            // Offset pagination (for shallow pages)
            int pageNo = page == null ? 0 : page;
            Pageable pageable = PageRequest.of(pageNo, size, Sort.by("id").descending());
            Page<tbPoNumber> poNumbersPage = StringUtils.hasText(searchTerm)
                ? poNumberRepo.searchAllColumns(searchTerm.toLowerCase(), pageable)
                : poNumberRepo.findAll(pageable);

            result.put("data", poNumbersPage.getContent());
            result.put("currentPage", poNumbersPage.getNumber());
            result.put("totalItems", poNumbersPage.getTotalElements());
            result.put("totalPages", poNumbersPage.getTotalPages());
        }
        return result;
    }
}
