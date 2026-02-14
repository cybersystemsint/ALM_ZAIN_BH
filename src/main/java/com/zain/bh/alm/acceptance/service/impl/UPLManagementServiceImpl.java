package com.zain.bh.alm.acceptance.service.impl;

import java.sql.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.zain.bh.alm.acceptance.entity.ApprovalLog;
import com.zain.bh.alm.acceptance.entity.UPL;
import com.zain.bh.alm.acceptance.repository.ApprovalLogRepository;
import com.zain.bh.alm.acceptance.repository.UPLRepository;
import com.zain.bh.alm.acceptance.service.UPLManagementService;
import com.zain.bh.alm.acceptance.util.DateTimeUtil;

@Service
public class UPLManagementServiceImpl implements UPLManagementService {

    private static final Logger LOGGER = LogManager.getLogger(UPLManagementServiceImpl.class);

    private final UPLRepository uplRepo;
    private final ApprovalLogRepository approvalLogRepo;

    public UPLManagementServiceImpl(UPLRepository uplRepo, ApprovalLogRepository approvalLogRepo) {
        this.uplRepo = uplRepo;
        this.approvalLogRepo = approvalLogRepo;
    }

    @Override
    public String processFromJson(String jsonRequest) {
        LOGGER.debug("Processing UPL management request");
        JSONArray jsonArrayresponse = new JSONArray();
        JSONArray jsonArray = new JSONArray(jsonRequest);
        
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            long recordNo = jsonObject.getLong("recordNo");
            String poId = jsonObject.getString("poId");
            
            String addporesp = addEditUpl(recordNo, jsonObject);
            
            if (!addporesp.contains("Success")) {
                JSONObject responsedata = new JSONObject();
                responsedata.put("recordNo", recordNo);
                responsedata.put("poId", poId);
                responsedata.put("DBresponse", addporesp);
                jsonArrayresponse.put(responsedata);
            }
        }
        
        return jsonArrayresponse.length() > 0 ? jsonArrayresponse.toString() : "Complete";
    }

    private String addEditUpl(long recordNo, JSONObject json) {
        UPL updt = uplRepo.findByRecordNo(recordNo);
        if (updt != null) {
            setUPLFields(updt, json);
            updt.setStatus("updated");
            try {
                uplRepo.save(updt);
                LOGGER.debug("Updated UPL: {}", json.getString("poId"));
                return "Record update Success";
            } catch (Exception ex) {
                LOGGER.error("Error updating UPL", ex);
                return "Failed to save";
            }
        } else {
            UPL nwupdt = new UPL();
            nwupdt.setRecordDatetime(DateTimeUtil.getFormattedDateTime());
            setUPLFields(nwupdt, json);
            nwupdt.setStatus("created");
            try {
                uplRepo.save(nwupdt);
                createApprovalLog(json);
                LOGGER.debug("Created UPL: {}", json.getString("poId"));
                return "Record add Success";
            } catch (Exception ex) {
                LOGGER.error("Error creating UPL", ex);
                return "Failed to save";
            }
        }
    }

    private void setUPLFields(UPL upl, JSONObject json) {
        upl.setActivePassive(json.getString("activePassive"));
        upl.setCurrency(json.getString("currency"));
        upl.setAmuComments(json.getString("amuComments"));
        upl.setPoId(json.getString("poId"));
        upl.setDiscount(json.getDouble("discount"));
        upl.setProjectName(json.getString("projectName"));
        upl.setCustomerItemType(json.getString("customerItemType"));
        upl.setLocalContent(json.getString("localContent"));
        upl.setScope(json.getString("Scope"));
        upl.setSubScope(json.getString("subScope"));
        upl.setPoLine(json.getString("poLine"));
        upl.setUplLine(json.getString("uplLine"));
        upl.setVendorItemCode(json.getString("vendorItemCode"));
        upl.setPoLineItemDescription(json.getString("poLineItemDescription"));
        upl.setErpItemDescription(json.getString("erpItemDescription"));
        upl.setZainItemCategory(json.getString("zainItemCategory"));
        upl.setSerialized(json.getString("serialized"));
        upl.setQuantity(json.getDouble("quantity"));
        upl.setUnit(json.getString("unit"));
        upl.setUnitPriceBeforeDiscount(json.getDouble("unitPriceBeforeDiscount"));
        upl.setUOM(json.getString("UOM"));
        upl.setPoTotalAmtBeforeDiscount(json.getDouble("poTotalAmtBeforeDiscount"));
        upl.setFinalTotalPriceAfterDiscount(json.getDouble("finalTotalPriceAfterDiscount"));
        upl.setHuaweiComments(json.getString("huaweiComments"));
        upl.setProcurementComments(json.getString("procurementComments"));
        upl.setDptApprover1(json.getString("dptApprover1"));
        upl.setDptApprover2(json.getString("dptApprover2"));
        upl.setDptApprover3(json.getString("dptApprover3"));
        upl.setDptApprover4(json.getString("dptApprover4"));
        upl.setRegionalApprover(json.getString("regionalApprover"));
    }

    private void createApprovalLog(JSONObject json) {
        UPL topRecord = uplRepo.findTopByPoNumber(json.getString("poId"));
        if (topRecord == null) return;

        Date now = new Date(System.currentTimeMillis());
        String[] approvers = {
            json.getString("dptApprover1"),
            json.getString("dptApprover2"),
            json.getString("dptApprover3"),
            json.getString("dptApprover4"),
            json.getString("regionalApprover")
        };

        for (String approver : approvers) {
            if (!approver.isEmpty()) {
                ApprovalLog log = new ApprovalLog();
                log.setRecordDatetime(now);
                log.setApprovalRecordId((int) topRecord.getRecordNo());
                log.setRecordType("UPL");
                log.setPoNumber(json.getString("poId"));
                log.setStatus("Pending");
                log.setCreatedBy(json.getInt("createdBy"));
                log.setRegion(json.getString("regionalApprover"));
                log.setApprover(approver);
                approvalLogRepo.save(log);
                break;
            }
        }
    }
}
