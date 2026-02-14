package com.zain.bh.alm.acceptance.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zain.bh.alm.acceptance.entity.UPL;
import com.zain.bh.alm.acceptance.repository.UPLRepository;
import com.zain.bh.alm.acceptance.service.UPLService;

@Service
public class UPLServiceImpl implements UPLService {

    private static final Logger LOGGER = LogManager.getLogger(UPLServiceImpl.class);

    private final UPLRepository uplRepo;

    public UPLServiceImpl(UPLRepository uplRepo) {
        this.uplRepo = uplRepo;
    }

    @Override
    @Transactional
    public void deleteUPL(Integer uplLine) {
        UPL entity = uplRepo.findByUplLine(uplLine);
        if (entity == null) {
            throw new IllegalArgumentException("UPL not found: " + uplLine);
        }
        entity.setStatus("deleted");
        uplRepo.save(entity);
        LOGGER.debug("Deleted UPL: {}", uplLine);
    }
}
