package com.zain.bh.alm.acceptance.service.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zain.bh.alm.acceptance.dto.ChargeAccountDTO;
import com.zain.bh.alm.acceptance.entity.ChargeAccount;
import com.zain.bh.alm.acceptance.repository.ChargeAccountRepository;
import com.zain.bh.alm.acceptance.service.ChargeAccountService;

@Service
public class ChargeAccountServiceImpl implements ChargeAccountService {

    private static final Logger LOGGER = LogManager.getLogger(ChargeAccountServiceImpl.class);

    private final ChargeAccountRepository chargeAccountRepo;

    public ChargeAccountServiceImpl(ChargeAccountRepository chargeAccountRepo) {
        this.chargeAccountRepo = chargeAccountRepo;
    }

    @Override
    @Transactional
    public List<ChargeAccountDTO> createOrUpdateBatch(List<ChargeAccountDTO> dtos) {
        LOGGER.debug("Processing {} charge account records", dtos.size());
        Date now = new Date(System.currentTimeMillis());
        List<ChargeAccountDTO> results = new ArrayList<>();

        for (ChargeAccountDTO dto : dtos) {
            ChargeAccount entity = chargeAccountRepo.findByRecordNo(dto.getRecordNo());
            
            if (entity != null) {
                updateEntity(entity, dto, now);
            } else {
                entity = createEntity(dto, now);
            }
            
            chargeAccountRepo.save(entity);
            results.add(dto);
        }
        
        return results;
    }

    @Override
    @Transactional
    public void createOrUpdateFromJson(String jsonRequest) {
        JSONArray jsonArray = new JSONArray(jsonRequest);
        List<ChargeAccountDTO> dtos = new ArrayList<>();
        
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            ChargeAccountDTO dto = new ChargeAccountDTO();
            dto.setRecordNo(jsonObject.getLong("recordNo"));
            dto.setChargeAccount(jsonObject.getString("chargeAccount"));
            dto.setOrgCode(jsonObject.getString("orgCode"));
            dto.setOrgName(jsonObject.getString("orgName"));
            dto.setSubInventory(jsonObject.getString("subInventory"));
            dto.setCreatedBy(jsonObject.optString("createdBy", null));
            dto.setUpdatedBy(jsonObject.optString("updatedBy", null));
            dtos.add(dto);
        }
        
        createOrUpdateBatch(dtos);
    }

    @Override
    @Transactional
    public void delete(Long recordNo) {
        ChargeAccount entity = chargeAccountRepo.findByRecordNo(recordNo);
        if (entity == null) {
            throw new IllegalArgumentException("Record not found for recordNo: " + recordNo);
        }
        chargeAccountRepo.delete(entity);
        LOGGER.debug("Deleted charge account recordNo: {}", recordNo);
    }

    private void updateEntity(ChargeAccount entity, ChargeAccountDTO dto, Date now) {
        entity.setChargeAccount(dto.getChargeAccount().trim());
        entity.setOrgCode(dto.getOrgCode().trim());
        entity.setOrgName(dto.getOrgName().trim());
        entity.setSubInventory(dto.getSubInventory().trim());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(now);
    }

    private ChargeAccount createEntity(ChargeAccountDTO dto, Date now) {
        ChargeAccount entity = new ChargeAccount();
        entity.setRecordDatetime(now);
        entity.setChargeAccount(dto.getChargeAccount().trim());
        entity.setOrgCode(dto.getOrgCode().trim());
        entity.setOrgName(dto.getOrgName().trim());
        entity.setSubInventory(dto.getSubInventory().trim());
        entity.setCreatedBy(dto.getCreatedBy());
        return entity;
    }
}
