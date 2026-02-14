package com.zain.bh.alm.acceptance.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.zain.bh.alm.acceptance.entity.DCC;
import com.zain.bh.alm.acceptance.entity.DCCStatus;
import com.zain.bh.alm.acceptance.repository.DCCRepository;
import com.zain.bh.alm.acceptance.repository.DCCStatusRepository;
import com.zain.bh.alm.acceptance.service.DCCStatusService;
import com.zain.bh.alm.acceptance.util.DateTimeUtil;

@Service
public class DCCStatusServiceImpl implements DCCStatusService {

    private static final Logger LOGGER = LogManager.getLogger(DCCStatusServiceImpl.class);

    private final DCCStatusRepository dccStatusRepo;
    private final DCCRepository dccRepo;

    public DCCStatusServiceImpl(DCCStatusRepository dccStatusRepo, DCCRepository dccRepo) {
        this.dccStatusRepo = dccStatusRepo;
        this.dccRepo = dccRepo;
    }

    @Override
    public void processFromJson(String jsonData) {
        LOGGER.debug("Processing DCC status update");
        JSONArray jsonArray = new JSONArray(jsonData);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            long recordNo = jsonObject.getLong("recordNo");
            long ddcRecordNo = jsonObject.getLong("ddcRecordNo");
            String dccId = jsonObject.getString("dccId");
            String userId = jsonObject.getString("userId");
            String status = jsonObject.getString("status");
            int lnRecordNo = jsonObject.getInt("lnRecordNo");
            
            addOrUpdateDccStatus(recordNo, status, userId, dccId, lnRecordNo);
            updateDccData(ddcRecordNo, status);
        }
    }

    private void addOrUpdateDccStatus(long recordno, String status, String userId, String dccId, int lnRecordNo) {
        DCCStatus checkdcc = dccStatusRepo.findByRecordNo(recordno);
        if (checkdcc != null) {
            checkdcc.setDccId(dccId);
            checkdcc.setStatus(status);
            checkdcc.setUserId(userId);
            checkdcc.setLnRecordNo(lnRecordNo);
            dccStatusRepo.save(checkdcc);
        } else {
            DCCStatus nwcheckdcc = new DCCStatus();
            nwcheckdcc.setDccId(dccId);
            nwcheckdcc.setStatus(status);
            nwcheckdcc.setUserId(userId);
            nwcheckdcc.setStatusDate(DateTimeUtil.getFormattedDateTime());
            nwcheckdcc.setLnRecordNo(lnRecordNo);
            dccStatusRepo.save(nwcheckdcc);
        }
    }

    private void updateDccData(long recordno, String status) {
        DCC checkdcc = dccRepo.findByRecordNo(recordno);
        if (checkdcc != null) {
            checkdcc.setStatus(status);
            dccRepo.save(checkdcc);
        }
    }
}
