package com.telkom.almBHZain.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.telkom.almBHZain.dto.POItemDto;
import com.telkom.almBHZain.dto.Request.SearchRequest;
import com.telkom.almBHZain.dto.Response.PageResult;
import com.telkom.almBHZain.model.POItem;
import com.telkom.almBHZain.repository.POItemRepository;
import com.telkom.almBHZain.specification.POItemSpecification;

@Service
public class POItemService {

    private final POItemRepository repo;

    @Autowired
    public POItemService(POItemRepository repo) {
        this.repo = repo;
    }

    public PageResult<POItemDto> getPOItems(SearchRequest request) {
        int page = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 100 : request.getSize();
        if (page < 0) page = 0;
        if (size <= 0) size = 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDateTime"));

        org.springframework.data.jpa.domain.Specification<POItem> spec = POItemSpecification.fromSearchRequest(request);

        Page<POItem> resultPage;
        if (spec == null) {
            resultPage = repo.findAll(pageable);
        } else {
            resultPage = repo.findAll(spec, pageable);
        }

        List<POItemDto> dtos = resultPage.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PageResult<>(
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.getNumber(),
                resultPage.getSize(),
                dtos
        );
    }

private POItemDto toDto(POItem e) {
    POItemDto d = new POItemDto();

    d.setRecordNo(e.getRecordNo());
    if (e.getRecordDateTime() != null) {
        d.setRecordDateTime(e.getRecordDateTime().toLocalDate());
    }
    d.setPoNumber(e.getPoNumber());
    d.setModelNumber(e.getModelNumber());
    d.setUom(e.getUom());
    d.setQtyPerSite(e.getQtyPerSite());
    d.setTotalNumberOfSites(e.getTotalNumberOfSites());
    d.setTotalQty(e.getTotalQty());
    d.setAccumulatedDepreciation(e.getAccumulatedDepreciation());
    d.setSalvageValue(e.getSalvageValue());
    d.setFaCategoryNew(e.getFaCategoryNew());
    d.setL1(e.getL1());
    d.setL2(e.getL2());
    d.setL3(e.getL3());
    d.setL4(e.getL4());
    d.setOldFaCategory(e.getOldFaCategory());
    d.setAccumulatedDepreciationCode(e.getAccumulatedDepreciationCode());
    d.setDepreciationCode(e.getDepreciationCode());
    d.setLifeYearsNew(e.getLifeYearsNew());
    d.setVendorName(e.getVendorName());
    d.setVendorNumber(e.getVendorNumber());
    d.setProjectNumber(e.getProjectNumber());
    if (e.getDatePlacedInService() != null) {
        d.setDatePlacedInService(e.getDatePlacedInService().toLocalDate());
    }
    if (e.getPoDate() != null) {
        d.setPoDate(e.getPoDate().toLocalDate());
    }
    d.setCurrency(e.getCurrency());
    d.setUnitPrice(e.getUnitPrice());
    d.setPoLine(e.getPoLine());
    d.setLevel1Description(e.getLevel1Description());
    d.setPartNumber(e.getPartNumber());
    d.setL3Description(e.getL3Description());
    d.setCostCenter(e.getCostCenter());
    d.setApprovalStatus(e.getApprovalStatus());
    d.setCreatedBy(e.getCreatedBy());
    if (e.getCreatedDateTime() != null) {
        d.setCreatedDateTime(e.getCreatedDateTime().toLocalDate());
    }
    d.setUpdatedBy(e.getUpdatedBy());
    if (e.getUpdatedDatetime() != null) {
        d.setUpdatedDatetime(e.getUpdatedDatetime().toLocalDate());
    }

    return d;
}
}