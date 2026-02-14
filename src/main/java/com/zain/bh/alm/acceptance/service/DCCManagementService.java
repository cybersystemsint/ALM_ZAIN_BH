package com.zain.bh.alm.acceptance.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface DCCManagementService {
    Map<String, Object> processFromJson(String jsonRequest, List<MultipartFile> files) throws Exception;
}
