package com.telkom.almBHZain.service;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.telkom.almBHZain.model.tb_Po;
import com.telkom.almBHZain.repo.tbPoRepo;


@Service
public class PoService {

    @Autowired
    private tbPoRepo poRepo;

    /**
     * Returns a paginated list of distinct poNumbers and their associated PO items, 
     * optionally filtered by supplierId and/or searchTerm.
     */
    public Map<String, Object> getPoNumbersWithItems(String supplierId, int page, int size, String searchTerm) {
        // 1. Get distinct poNumbers page (filtered)
        Page<String> poNumbersPage;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("poNumber"));
        if (searchTerm != null && !searchTerm.isBlank()) {
            if (supplierId != null && !"0".equals(supplierId)) {
                poNumbersPage = poRepo.findDistinctPoNumbersBySupplierAndSearch(supplierId, "%" + searchTerm.toLowerCase() + "%", pageable);
            } else {
                poNumbersPage = poRepo.findDistinctPoNumbersBySearch("%" + searchTerm.toLowerCase() + "%", pageable);
            }
        } else {
            if (supplierId != null && !"0".equals(supplierId)) {
                poNumbersPage = poRepo.findDistinctPoNumbersBySupplier(supplierId, pageable);
            } else {
                poNumbersPage = poRepo.findDistinctPoNumbers(pageable);
            }
        }

        List<String> poNumberList = poNumbersPage.getContent();

        // 2. Fetch all PO items for these poNumbers (in one query)
        List<tb_Po> poItems = poNumberList.isEmpty() ? Collections.emptyList() : poRepo.findByPoNumberIn(poNumberList);

        // 3. Group
        Map<String, List<tb_Po>> grouped = poItems.stream()
            .collect(Collectors.groupingBy(tb_Po::getPoNumber, LinkedHashMap::new, Collectors.toList()));

        // 4. Response
        Map<String, Object> result = new HashMap<>();
        result.put("data", grouped);
        result.put("totalRecords", poNumbersPage.getTotalElements());
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("totalPages", poNumbersPage.getTotalPages());
        return result;
    }
}