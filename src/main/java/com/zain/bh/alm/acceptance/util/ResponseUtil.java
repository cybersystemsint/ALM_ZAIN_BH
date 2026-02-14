package com.zain.bh.alm.acceptance.util;

import java.util.HashMap;
import java.util.Map;

public class ResponseUtil {

    private ResponseUtil() {
        // Private constructor to prevent instantiation
    }

    public static Map<String, String> createResponse(String result, String msg) {
        Map<String, String> map = new HashMap<>();
        map.put("responseCode", result.equalsIgnoreCase("success") ? "0" : "1001");
        map.put("responseMessage", msg);
        return map;
    }
}
