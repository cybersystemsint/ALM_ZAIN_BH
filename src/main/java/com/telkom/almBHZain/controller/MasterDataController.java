package com.telkom.almBHZain.controller;

import com.telkom.almBHZain.repo.deptsrepo;
import com.telkom.almBHZain.model.departmentsdata;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.telkom.almBHZain.helper.helper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MasterDataController {

    static String genHeader(String msisdn, String reqid, String Channel) {
        return " | " + reqid + " | " + Channel + " | " + msisdn + " | ";
    }

    private String getbatchfilename(String filetype) {
        String batchfilename = filetype + "_" + System.currentTimeMillis() + ".json";
        return batchfilename;
    }

    private Map<String, String> response(String result, String msg) {
        HashMap<String, String> map = new HashMap<>();
        map.put("responseCode", result.equalsIgnoreCase("success") ? "0" : "1001");
        map.put("responseMessage", msg);
        return map;
    }

    @Autowired
    deptsrepo dptrepo;

    @PostMapping(value = "/masterdata/postdepts")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> postdepts(@RequestBody String req) {
        String batchfilename = getbatchfilename("Depts_BATCHUPLOAD");
        helper.logBatchFile(req, true, batchfilename);
        helper.logToFile(genHeader("N/A", "Depts_BATCHUPLOAD", "Depts_BATCHUPLOAD")
                + "Depts_BATCHUPLOAD file name /home/app/logs/ALM/BatchFiles/" + batchfilename, "INFO");
        JSONArray jsonArrayresponse = new JSONArray();
        long recordNo = 0;
        String deptName = "";
        boolean sysStatus = false;
        try {
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String requesttime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            JSONArray jsonArray = new JSONArray(req);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                recordNo = Integer.parseInt(jsonObject.getString("recordNo"));
                deptName = jsonObject.getString("deptName");
                sysStatus = jsonObject.getBoolean("sysStatus");
                departmentsdata applvl = dptrepo.findByRecordNo(recordNo);
                if (applvl != null) {
                    applvl.setDeptName(deptName);
                    applvl.setSysStatus(sysStatus);
                    try {
                        dptrepo.save(applvl);
                    } catch (Exception exc) {
                        net.minidev.json.JSONObject responsedata = new net.minidev.json.JSONObject();
                        responsedata.put("deptName", deptName);
                        responsedata.put("recordNo", recordNo);
                        responsedata.put("DBresponse", exc.getCause().toString());
                        jsonArrayresponse.put(responsedata.toJSONString());
                    }
                } else {
                    departmentsdata nwapplvl = new departmentsdata();
                    nwapplvl.setDeptName(deptName);
                    nwapplvl.setSysStatus(sysStatus);
                    try {
                        dptrepo.save(nwapplvl);
                    } catch (Exception exc) {
                        net.minidev.json.JSONObject responsedata = new net.minidev.json.JSONObject();
                        responsedata.put("deptName", deptName);
                        responsedata.put("recordNo", recordNo);
                        responsedata.put("DBresponse", exc.getCause().toString());
                        jsonArrayresponse.put(responsedata.toJSONString());
                    }
                }
            }
            if (jsonArrayresponse.length() > 0) {
                batchfilename = getbatchfilename("FailedUpload");
                helper.logBatchFile(jsonArrayresponse.toString(), true, batchfilename);
                return response("Error", jsonArrayresponse.toString());
            } else {
                return response("Success", "Complete");
            }
        } catch (Exception excc) {
            net.minidev.json.JSONObject responsedata = new net.minidev.json.JSONObject();
            responsedata.put("deptName", deptName);
            responsedata.put("recordNo", recordNo);
            responsedata.put("DBresponse", excc.getCause().toString());
            jsonArrayresponse.put(responsedata.toJSONString());
            String errorcode = excc.toString();
        }
        return response("Error", jsonArrayresponse.toString());
    }

    @PostMapping(value = "/masterdata/getdepts")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public String getdepts(@RequestBody String req) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Gson gsondt = new Gson();
            helper.logToFile(genHeader("N/A", "getdepts", "getdepts")
                    + "getdepts " + req, "INFO");
            JsonObject obj = new JsonParser().parse(req).getAsJsonObject();
            boolean statusactive = obj.get("sysStatus").getAsBoolean();
            List<departmentsdata> gtdata = dptrepo.findBySysStatus(statusactive);
            if (!gtdata.isEmpty()) {
                return (gsondt.toJson(gtdata).toString());
            } else {
                return ("No Depts Data found.");
            }
        } catch (Exception exc) {
            String err = exc.toString();
            helper.logToFile(genHeader("N/A", "getdepts", "getdepts") + "getdepts error " + err, "INFO");
        }
        return null;
    }

}
