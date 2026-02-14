package com.zain.bh.alm.acceptance.service;

import com.zain.bh.alm.acceptance.dto.ErrorMessageDTO;
import java.util.List;

public interface ErrorMessageService {
    void createOrUpdateBatch(List<ErrorMessageDTO> dtoList);
    void createOrUpdateFromJson(String jsonRequest);
}
