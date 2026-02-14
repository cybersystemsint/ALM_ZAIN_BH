package com.zain.bh.alm.acceptance.service.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zain.bh.alm.acceptance.dto.ErrorMessageDTO;
import com.zain.bh.alm.acceptance.entity.ErrorMessage;
import com.zain.bh.alm.acceptance.enums.Severity;
import com.zain.bh.alm.acceptance.repository.ErrorMessageRepository;
import com.zain.bh.alm.acceptance.service.ErrorMessageService;

@Service
public class ErrorMessageServiceImpl implements ErrorMessageService {

    private static final Logger LOGGER = LogManager.getLogger(ErrorMessageServiceImpl.class);

    private final ErrorMessageRepository errorMessageRepo;

    public ErrorMessageServiceImpl(ErrorMessageRepository errorMessageRepo) {
        this.errorMessageRepo = errorMessageRepo;
    }

    @Override
    @Transactional
    public void createOrUpdateBatch(List<ErrorMessageDTO> dtoList) {
        LOGGER.debug("Processing {} error message records", dtoList.size());
        Date now = new Date(System.currentTimeMillis());

        for (ErrorMessageDTO dto : dtoList) {
            ErrorMessage entity = errorMessageRepo.findByRecordNo(dto.getRecordNo());

            if (entity != null) {
                entity.setModule(dto.getModule().trim());
                entity.setErrorCode(dto.getErrorCode().trim());
                entity.setErrorMessage(dto.getErrorMessage().trim());
                entity.setOperation(dto.getOperation().trim());
                entity.setSeverity(Severity.valueOf(dto.getSeverity().trim().toUpperCase(Locale.ROOT)));
                entity.setUpdatedBy(dto.getUpdatedBy());
                entity.setUpdatedDatetime(now);
            } else {
                entity = new ErrorMessage();
                entity.setModule(dto.getModule().trim());
                entity.setErrorCode(dto.getErrorCode().trim());
                entity.setErrorMessage(dto.getErrorMessage().trim());
                entity.setOperation(dto.getOperation().trim());
                entity.setSeverity(Severity.valueOf(dto.getSeverity().trim().toUpperCase(Locale.ROOT)));
                entity.setCreatedBy(dto.getCreatedBy());
            }

            errorMessageRepo.save(entity);
        }
    }

    @Override
    @Transactional
    public void createOrUpdateFromJson(String jsonRequest) {
        JSONArray jsonArray = new JSONArray(jsonRequest);
        List<ErrorMessageDTO> dtos = new ArrayList<>();
        
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            ErrorMessageDTO dto = new ErrorMessageDTO();
            dto.setRecordNo(jsonObject.getLong("recordNo"));
            dto.setModule(jsonObject.getString("module"));
            dto.setErrorCode(jsonObject.getString("errorCode"));
            dto.setErrorMessage(jsonObject.getString("errorMessage"));
            dto.setOperation(jsonObject.getString("operation"));
            dto.setSeverity(jsonObject.getString("severity"));
            dto.setCreatedBy(jsonObject.optString("createdBy", null));
            dto.setUpdatedBy(jsonObject.optString("updatedBy", null));
            dtos.add(dto);
        }
        
        createOrUpdateBatch(dtos);
    }
}
