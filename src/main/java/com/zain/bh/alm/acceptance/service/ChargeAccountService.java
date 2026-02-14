package com.zain.bh.alm.acceptance.service;

import com.zain.bh.alm.acceptance.dto.ChargeAccountDTO;
import java.util.List;

public interface ChargeAccountService {
    List<ChargeAccountDTO> createOrUpdateBatch(List<ChargeAccountDTO> dtos);
    void createOrUpdateFromJson(String jsonRequest);
    void delete(Long recordNo);
}
