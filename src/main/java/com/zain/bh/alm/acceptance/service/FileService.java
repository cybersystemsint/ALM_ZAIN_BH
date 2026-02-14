package com.zain.bh.alm.acceptance.service;

import java.util.List;
import java.util.Map;

public interface FileService {
    List<Map<String, String>> getAttachments(String poNumber, Integer dccId);
}
