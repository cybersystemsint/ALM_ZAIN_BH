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

import com.zain.bh.alm.acceptance.dto.ItemCodeSubstituteDTO;
import com.zain.bh.alm.acceptance.entity.ItemCodeSubstitute;
import com.zain.bh.alm.acceptance.repository.ItemCodeSubstituteRepository;
import com.zain.bh.alm.acceptance.service.ItemCodeSubstituteService;

@Service
public class ItemCodeSubstituteServiceImpl implements ItemCodeSubstituteService {

    private static final Logger LOGGER = LogManager.getLogger(ItemCodeSubstituteServiceImpl.class);

    private final ItemCodeSubstituteRepository itemCodeSubstituteRepo;

    public ItemCodeSubstituteServiceImpl(ItemCodeSubstituteRepository itemCodeSubstituteRepo) {
        this.itemCodeSubstituteRepo = itemCodeSubstituteRepo;
    }

    @Override
    @Transactional
    public void createOrUpdateBatch(List<ItemCodeSubstituteDTO> dtoList) {
        LOGGER.debug("Processing {} item code substitute records", dtoList.size());
        Date now = new Date(System.currentTimeMillis());

        for (ItemCodeSubstituteDTO dto : dtoList) {
            ItemCodeSubstitute entity = itemCodeSubstituteRepo.findByRecordNo(dto.getRecordNo());

            if (entity != null) {
                entity.setItemCode(dto.getItemCode().trim());
                entity.setRelatedItemCode(dto.getRelatedItemCode().trim());
                entity.setReciprocalFlag(dto.getReciprocalFlag().trim());
                entity.setUpdatedBy(dto.getUpdatedBy());
            } else {
                entity = new ItemCodeSubstitute();
                entity.setRecordDateTime(now);
                entity.setCreatedDatetime(now);
                entity.setItemCode(dto.getItemCode().trim());
                entity.setRelatedItemCode(dto.getRelatedItemCode().trim());
                entity.setReciprocalFlag(dto.getReciprocalFlag().trim());
                entity.setCreatedBy(dto.getCreatedBy());
            }

            itemCodeSubstituteRepo.save(entity);
        }
    }

    @Override
    @Transactional
    public void createOrUpdateFromJson(String jsonRequest) {
        JSONArray jsonArray = new JSONArray(jsonRequest);
        List<ItemCodeSubstituteDTO> dtos = new ArrayList<>();
        
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            ItemCodeSubstituteDTO dto = new ItemCodeSubstituteDTO();
            dto.setRecordNo(jsonObject.getLong("recordNo"));
            dto.setItemCode(jsonObject.getString("itemCode"));
            dto.setRelatedItemCode(jsonObject.getString("relatedItemCode"));
            dto.setReciprocalFlag(jsonObject.getString("reciprocalFlag"));
            dto.setCreatedBy(jsonObject.optString("createdBy", null));
            dto.setUpdatedBy(jsonObject.optString("updatedBy", null));
            dtos.add(dto);
        }
        
        createOrUpdateBatch(dtos);
    }
}
