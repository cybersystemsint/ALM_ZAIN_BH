package com.zain.bh.alm.acceptance.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zain.bh.alm.acceptance.dto.SupplierDTO;
import com.zain.bh.alm.acceptance.entity.Supplier;
import com.zain.bh.alm.acceptance.repository.SupplierRepository;
import com.zain.bh.alm.acceptance.service.SupplierService;
import com.zain.bh.alm.acceptance.util.DateTimeUtil;

@Service
public class SupplierServiceImpl implements SupplierService {

    private static final Logger LOGGER = LogManager.getLogger(SupplierServiceImpl.class);

    private final SupplierRepository supplierRepo;

    public SupplierServiceImpl(SupplierRepository supplierRepo) {
        this.supplierRepo = supplierRepo;
    }

    @Override
    @Transactional
    public void createOrUpdateBatch(List<SupplierDTO> dtoList) {
        LOGGER.debug("Processing {} supplier records", dtoList.size());
        String timestamp = DateTimeUtil.getFormattedDateTime();
        
        for (SupplierDTO dto : dtoList) {
            Supplier entity = dto.getRecordNo() > 0 ? supplierRepo.findByRecordNo(dto.getRecordNo()) : null;

            if (entity != null) {
                updateEntity(entity, dto, timestamp);
            } else {
                entity = new Supplier();
                entity.setRecordDatetime(timestamp);
                updateEntity(entity, dto, timestamp);
            }
            supplierRepo.save(entity);
        }
    }

    @Override
    @Transactional
    public void createOrUpdateFromJson(String jsonRequest) {
        JSONArray jsonArray = new JSONArray(jsonRequest);
        List<SupplierDTO> dtos = new ArrayList<>();
        
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            SupplierDTO dto = new SupplierDTO();
            dto.setRecordNo(jsonObject.getLong("recordNo"));
            dto.setSupplierId(jsonObject.getString("supplierId"));
            dto.setSupplierName(jsonObject.getString("supplierName"));
            dto.setAddress(jsonObject.getString("address"));
            dto.setSupplierEmail(jsonObject.getString("supplierEmail"));
            dto.setSupplierPhone(jsonObject.getString("supplierPhone"));
            dto.setContactPerson(jsonObject.getString("contactPerson"));
            dto.setContactPersonPhone(jsonObject.getString("contactPersonPhone"));
            dto.setContactPersonEmail(jsonObject.getString("contactPersonEmail"));
            dto.setStatus(jsonObject.getString("status"));
            dto.setDescription(jsonObject.getString("description"));
            dto.setCreatedBy(jsonObject.getString("createdBy"));
            dto.setLastUpdateBy(jsonObject.getString("lastUpdateBy"));
            dtos.add(dto);
        }
        
        createOrUpdateBatch(dtos);
    }

    private void updateEntity(Supplier entity, SupplierDTO dto, String timestamp) {
        entity.setSupplierId(dto.getSupplierId());
        entity.setSupplierName(dto.getSupplierName());
        entity.setAddress(dto.getAddress());
        entity.setSupplierEmail(dto.getSupplierEmail());
        entity.setSupplierPhone(dto.getSupplierPhone());
        entity.setContactPerson(dto.getContactPerson());
        entity.setContactPersonPhone(dto.getContactPersonPhone());
        entity.setContactPersonEmail(dto.getContactPersonEmail());
        entity.setStatus(dto.getStatus());
        entity.setDescription(dto.getDescription());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setLastUpdateBy(dto.getLastUpdateBy());
        entity.setCreatedOn(timestamp);
        entity.setLastUpdate(timestamp);
    }
}
