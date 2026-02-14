package com.zain.bh.alm.acceptance.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zain.bh.alm.acceptance.entity.PurchaseOrderHeader;
import com.zain.bh.alm.acceptance.repository.PurchaseOrderHeaderRepository;
import com.zain.bh.alm.acceptance.service.POService;

@Service
public class POServiceImpl implements POService {

    private static final Logger LOGGER = LogManager.getLogger(POServiceImpl.class);

    private final PurchaseOrderHeaderRepository pohdRepo;

    public POServiceImpl(PurchaseOrderHeaderRepository pohdRepo) {
        this.pohdRepo = pohdRepo;
    }

    @Override
    @Transactional
    public void deletePO(String poId) {
        PurchaseOrderHeader entity = pohdRepo.findByPoId(poId);
        if (entity == null) {
            throw new IllegalArgumentException("PO not found: " + poId);
        }
        entity.setStatus("deleted");
        pohdRepo.save(entity);
        LOGGER.debug("Deleted PO: {}", poId);
    }

    @Override
    public List<PurchaseOrderHeader> fetchPOData(String poId, String supplierId) {
        LOGGER.debug("Fetching PO data for poId: {}, supplierId: {}", poId, supplierId);
        return pohdRepo.findByPoIdAndSupplierId(poId, supplierId);
    }
}
