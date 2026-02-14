package com.zain.bh.alm.acceptance.service;

import java.util.Map;

public interface POUPLCreationService {
    Map<String, Object> processFromJson(String jsonRequest) throws Exception;
}
