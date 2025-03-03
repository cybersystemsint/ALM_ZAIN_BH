/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.telkom.almBHZain.controller;

import org.apache.logging.log4j.LogManager;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.telkom.almBHZain.helper.helper;
import com.telkom.almBHZain.model.tbPoNumber;
import com.telkom.almBHZain.model.tb_Po;
import com.telkom.almBHZain.repo.tbPoNumberRepo;
import com.telkom.almBHZain.repo.tbPoRepo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import java.io.*;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author jgithu
 */
@RestController
public class BarhainController {

    private final org.apache.logging.log4j.Logger loggger = LogManager.getLogger(APIController.class);
    @Autowired
    tbPoRepo poRepo;

    @Autowired
    tbPoNumberRepo poNumberRepo;

    private Map<String, String> response(String result, String msg) {
        HashMap<String, String> map = new HashMap<>();
        map.put("responseCode", result.equalsIgnoreCase("success") ? "0" : "1001");
        map.put("responseMessage", msg);
        return map;
    }

    //=================================NEW PO END POINT FOR BARHAIN ====
    @PostMapping(value = "/createPo")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> createPo(@RequestBody String req) throws ParseException, ParseException, ParseException {
        String batchfilename = "";
        LocalDateTime now = LocalDateTime.now();
        long recordNo = 0;
        loggger.info("PO CREATE REQUEST |  " + req);
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the pattern as per your date format
            JSONArray jsonArray = new JSONArray(req);
            String responseinfo = "Failed to save or data";

            List<String> validationErrors = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                recordNo = Integer.parseInt(jsonObject.getString("recordNo"));

                tb_Po spldt = poRepo.findByRecordNo(recordNo);
                if (spldt != null) {

                    spldt.setPoNumber(jsonObject.getString("poNumber").trim());
                    spldt.setModelNumber(jsonObject.getString("modelNumber").trim());
                    spldt.setUom(jsonObject.getString("uom"));
                    spldt.setQtyPerSite(jsonObject.getInt("qtyPerSite"));
                    spldt.setTotalNumberOfSites(jsonObject.getInt("totalNumberOfSites"));
                    spldt.setTotalQty(jsonObject.getInt("totalQty"));
                    spldt.setAccumulatedDepreciation(jsonObject.getDouble("accumulatedDepreciation"));
                    spldt.setSalvageValue(jsonObject.getDouble("salvageValue"));
                    spldt.setFaCategoryNew(jsonObject.getString("faCategoryNew").trim());
                    spldt.setL1(jsonObject.getString("l1"));
                    spldt.setL2(jsonObject.getString("l2").trim());
                    spldt.setL3(jsonObject.getString("l3").trim());
                    spldt.setL4(jsonObject.getString("l4"));
                    spldt.setOldFaCategory(jsonObject.getString("oldFaCategory").trim());
                    spldt.setAccumulatedDepreciationCode(jsonObject.getString("accumulatedDepreciationCode"));
                    spldt.setDepreciationCode(jsonObject.getString("depreciationCode"));
                    spldt.setLifeYearsNew(jsonObject.getInt("lifeYearsNew"));
                    spldt.setVendorName(jsonObject.getString("vendorName"));
                    spldt.setVendorNumber(jsonObject.getString("vendorNumber"));
                    spldt.setProjectNumber(jsonObject.getString("projectNumber"));
                    String datePlacedInService = jsonObject.getString("datePlacedInService");
                    String poDate = jsonObject.getString("poDate");
                    try {
                        java.util.Date parsedDate = dateFormat.parse(datePlacedInService);
                        java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
//                        java.util.Date today = dateFormat.parse(now.toString());
//                        java.sql.Date createdDate = new java.sql.Date(today.getTime());
                        java.util.Date newDate = dateFormat.parse(poDate);
                        java.sql.Date sqlcreatedDate = new java.sql.Date(newDate.getTime());
                        spldt.setDatePlacedInService(sqlDate);
                        spldt.setPoDate(sqlcreatedDate);
                    } catch (ParseException ex) {
                        java.util.logging.Logger.getLogger(APIController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    spldt.setCurrency(jsonObject.getString("currency"));
                    spldt.setUnitPrice(jsonObject.getDouble("unitPrice"));
                    spldt.setPoLine(jsonObject.getInt("poLine"));
                    spldt.setLevel1Description(jsonObject.getString("level1Description"));
                    spldt.setPartNumber(jsonObject.getString("partNumber"));
                    spldt.setL3Description(jsonObject.getString("level1Description"));
                    spldt.setCostCenter(jsonObject.getString("costCenter").trim());
                    spldt.setUpdatedBy(jsonObject.getString("updatedBy").trim());

                    try {
                        poRepo.save(spldt);
                        responseinfo = "Record Updated Successfully";
                    } catch (Exception excc) {
                        loggger.info("Exception |  " + excc.toString());
                        responseinfo = excc.toString();
                    }
                } else {

                    tbPoNumber existsPoNumber = poNumberRepo.findByPoNumber(jsonObject.getString("poNumber"));
                    String createdpoNum = existsPoNumber != null ? String.valueOf(existsPoNumber.getPoNumber()) : "";

                    tb_Po topRecord = poRepo.findTopByPoNumberAndPoLine(jsonObject.getString("poNumber"), jsonObject.getInt("poLine"));
                    String poNum = topRecord != null ? String.valueOf(topRecord.getPoNumber()) : "";

                    if (createdpoNum.length() < 1) {
                        tbPoNumber newpo = new tbPoNumber();
                        newpo.setPoNumber(jsonObject.getString("poNumber"));
                        poNumberRepo.save(newpo);
                    }

                    if (poNum.length() < 1) {

                        tb_Po nwspldt = new tb_Po();

                        nwspldt.setPoNumber(jsonObject.getString("poNumber").trim());
                        nwspldt.setModelNumber(jsonObject.getString("modelNumber").trim());
                        nwspldt.setUom(jsonObject.getString("uom"));
                        nwspldt.setQtyPerSite(jsonObject.getInt("qtyPerSite"));
                        nwspldt.setTotalNumberOfSites(jsonObject.getInt("totalNumberOfSites"));
                        nwspldt.setTotalQty(jsonObject.getInt("totalQty"));
                        nwspldt.setAccumulatedDepreciation(jsonObject.getDouble("accumulatedDepreciation"));
                        nwspldt.setSalvageValue(jsonObject.getDouble("salvageValue"));
                        nwspldt.setFaCategoryNew(jsonObject.getString("faCategoryNew").trim());
                        nwspldt.setL1(jsonObject.getString("l1"));
                        nwspldt.setL2(jsonObject.getString("l2").trim());
                        nwspldt.setL3(jsonObject.getString("l3").trim());
                        nwspldt.setL4(jsonObject.getString("l4"));
                        nwspldt.setOldFaCategory(jsonObject.getString("oldFaCategory").trim());
                        nwspldt.setAccumulatedDepreciationCode(jsonObject.getString("accumulatedDepreciationCode"));
                        nwspldt.setDepreciationCode(jsonObject.getString("depreciationCode"));
                        nwspldt.setLifeYearsNew(jsonObject.getInt("lifeYearsNew"));
                        nwspldt.setVendorName(jsonObject.getString("vendorName"));
                        nwspldt.setVendorNumber(jsonObject.getString("vendorNumber"));
                        nwspldt.setProjectNumber(jsonObject.getString("projectNumber"));
                        String datePlacedInService = jsonObject.getString("datePlacedInService");
                        String poDate = jsonObject.getString("poDate");
                        try {
                            java.util.Date parsedDate = dateFormat.parse(datePlacedInService);
                            java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
                            java.util.Date today = dateFormat.parse(now.toString());
                            java.sql.Date createdDate = new java.sql.Date(today.getTime());
                            java.util.Date newDate = dateFormat.parse(poDate);
                            java.sql.Date sqlcreatedDate = new java.sql.Date(newDate.getTime());
                            nwspldt.setDatePlacedInService(sqlDate);
                            nwspldt.setPoDate(sqlcreatedDate);
                            nwspldt.setRecordDateTime(createdDate);
                        } catch (ParseException ex) {
                            java.util.logging.Logger.getLogger(APIController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        nwspldt.setCurrency(jsonObject.getString("currency"));
                        nwspldt.setUnitPrice(jsonObject.getDouble("unitPrice"));
                        nwspldt.setPoLine(jsonObject.getInt("poLine"));
                        nwspldt.setLevel1Description(jsonObject.getString("level1Description"));
                        nwspldt.setPartNumber(jsonObject.getString("partNumber"));
                        nwspldt.setL3Description(jsonObject.getString("level1Description"));
                        nwspldt.setCostCenter(jsonObject.getString("costCenter").trim());
                        nwspldt.setCreatedBy(jsonObject.getString("createdBy").trim());

                        try {
                            poRepo.save(nwspldt);
                            responseinfo = "Record Created Success";

                        } catch (JSONException excc) {

                            loggger.info("Exception |  " + excc.toString());

                            responseinfo = excc.toString();
                        }

                        // }
                    } else {
                        validationErrors.add(jsonObject.getString("poNumber") + " " + jsonObject.getInt("lineNumber"));
                    }

                }

            }
            loggger.info("PO CREATE RESPONSE |  " + responseinfo);
            loggger.info("VALIDATION RESPONSE |  " + validationErrors);
            if (!validationErrors.isEmpty()) {
                batchfilename = getbatchfilename("FailedUpload");
                helper.logBatchFile(responseinfo, true, batchfilename);
                return response("Error", "PO numbers and Line Items: " + String.join(", ", validationErrors) + " are already uploaded. Duplicates not allowed");
            } else if (!responseinfo.contains("Success")) {
                batchfilename = getbatchfilename("FailedUpload");
                helper.logBatchFile(responseinfo, true, batchfilename);
                return response("Error", responseinfo);
            } else {
                return response("Success", "Complete");
            }

        } catch (NumberFormatException | JSONException exc) {
            loggger.info("Exception |  " + exc.toString());
            return response("Error", exc.getMessage());
        }
    }

    private String getbatchfilename(String filetype) {
        String batchfilename = filetype + "_" + System.currentTimeMillis() + ".json";
        return batchfilename;
    }

    @DeleteMapping(value = "/deletePo/{recordNo}")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> deletePo(@PathVariable long recordNo) {
        loggger.info("PO DELETE REQUEST | recordNo: " + recordNo);
        try {
            tb_Po po = poRepo.findByRecordNo(recordNo);
            if (po != null) {
                poRepo.delete(po);
                loggger.info("PO DELETE RESPONSE | Record Deleted Successfully");
                return response("Success", "Record Deleted Successfully");
            } else {
                loggger.info("PO DELETE RESPONSE | Record Not Found");
                return response("Error", "Record Not Found");
            }
        } catch (Exception ex) {
            loggger.info("Exception | " + ex.toString());
            return response("Error", ex.getMessage());
        }
    }

}

