package com.zain.bh.alm.acceptance.service;

import com.zain.bh.alm.acceptance.dto.SupplierDTO;
import java.util.List;

public interface SupplierService {
    void createOrUpdateBatch(List<SupplierDTO> dtoList);
    void createOrUpdateFromJson(String jsonRequest);
}
