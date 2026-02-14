package com.zain.bh.alm.acceptance.service;

import com.zain.bh.alm.acceptance.entity.PurchaseOrderHeader;
import java.util.List;

public interface POService {
    void deletePO(String poId);
    List<PurchaseOrderHeader> fetchPOData(String poId, String supplierId);
}
