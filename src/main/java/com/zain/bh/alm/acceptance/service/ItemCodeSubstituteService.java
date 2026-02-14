package com.zain.bh.alm.acceptance.service;

import com.zain.bh.alm.acceptance.dto.ItemCodeSubstituteDTO;
import java.util.List;

public interface ItemCodeSubstituteService {
    void createOrUpdateBatch(List<ItemCodeSubstituteDTO> dtoList);
    void createOrUpdateFromJson(String jsonRequest);
}
