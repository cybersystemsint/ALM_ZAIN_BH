package com.zain.bh.alm.acceptance.service.impl;

import static com.zain.bh.alm.acceptance.constant.AppConstants.*;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.zain.bh.alm.acceptance.entity.Po;
import com.zain.bh.alm.acceptance.entity.PoNumber;
import com.zain.bh.alm.acceptance.repository.PoNumberRepository;
import com.zain.bh.alm.acceptance.repository.PoRepository;
import com.zain.bh.alm.acceptance.service.BahrainPoService;
import com.zain.bh.alm.acceptance.util.ResponseUtil;

@Service
public class BahrainPoServiceImpl implements BahrainPoService {

	private static final Logger LOGGER = LogManager.getLogger(BahrainPoServiceImpl.class);

	private final PoRepository poRepo;
	private final PoNumberRepository poNumberRepo;

	public BahrainPoServiceImpl(PoRepository poRepo, PoNumberRepository poNumberRepo) {
		this.poRepo = poRepo;
		this.poNumberRepo = poNumberRepo;
	}

	@Override
	public Map<String, String> createOrUpdatePo(String jsonRequest) {
		try {
			LOGGER.debug("Processing PO request");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			JSONArray jsonArray = new JSONArray(jsonRequest);
			List<String> validationErrors = new ArrayList<>();
			boolean hasSuccess = false;

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				long recordNo = Long.parseLong(jsonObject.getString("recordNo"));

				Po existingPo = poRepo.findByRecordNo(recordNo);
				if (existingPo != null) {
					updatePo(existingPo, jsonObject, dateFormat);
					hasSuccess = true;
				} else {
					hasSuccess = createNewPo(jsonObject, dateFormat, validationErrors) || hasSuccess;
				}
			}

			if (!validationErrors.isEmpty()) {
				return ResponseUtil.createResponse(ERROR,
						"PO numbers and Line Items: " + String.join(", ", validationErrors)
								+ " are already uploaded. Duplicates not allowed");
			}
			return hasSuccess ? ResponseUtil.createResponse(SUCCESS, COMPLETE)
					: ResponseUtil.createResponse(ERROR, "Failed to save data");
		} catch (Exception exception) {
			LOGGER.error("Error creating/updating PO", exception);
			return ResponseUtil.createResponse(ERROR, exception.getMessage());
		}
	}

	private void updatePo(Po po, JSONObject json, SimpleDateFormat dateFormat) throws ParseException {
		setPoFields(po, json, dateFormat);
		po.setUpdatedBy(json.getString("updatedBy").trim());
		poRepo.save(po);
		LOGGER.debug("Updated PO: {}", po.getPoNumber());
	}

	private boolean createNewPo(JSONObject json, SimpleDateFormat dateFormat, List<String> validationErrors)
			throws ParseException {
		String poNumber = json.getString("poNumber");
		int poLine = json.getInt("poLine");

		Po existingPoLine = poRepo.findTopByPoNumberAndPoLine(poNumber, poLine);
		if (existingPoLine != null) {
			validationErrors.add(poNumber + " " + poLine);
			return false;
		}

		PoNumber existingPoNumber = poNumberRepo.findByPoNumber(poNumber);
		if (existingPoNumber == null) {
			PoNumber newPoNumber = new PoNumber();
			newPoNumber.setPoNumber(poNumber);
			poNumberRepo.save(newPoNumber);
		}

		Po newPo = new Po();
		setPoFields(newPo, json, dateFormat);
		newPo.setCreatedBy(json.getString("createdBy").trim());
		poRepo.save(newPo);
		LOGGER.debug("Created PO: {} line: {}", poNumber, poLine);
		return true;
	}

	private void setPoFields(Po po, JSONObject json, SimpleDateFormat dateFormat) throws ParseException {
		po.setPoNumber(json.getString("poNumber").trim());
		po.setModelNumber(json.getString("modelNumber").trim());
		po.setUom(json.getString("uom"));
		po.setQtyPerSite(json.getInt("qtyPerSite"));
		po.setTotalNumberOfSites(json.getInt("totalNumberOfSites"));
		po.setTotalQty(json.getInt("totalQty"));
		po.setAccumulatedDepreciation(json.getDouble("accumulatedDepreciation"));
		po.setSalvageValue(json.getDouble("salvageValue"));
		po.setFaCategoryNew(json.getString("faCategoryNew").trim());
		po.setL1(json.getString("l1"));
		po.setL2(json.getString("l2").trim());
		po.setL3(json.getString("l3").trim());
		po.setL4(json.getString("l4"));
		po.setOldFaCategory(json.getString("oldFaCategory").trim());
		po.setAccumulatedDepreciationCode(json.getString("accumulatedDepreciationCode"));
		po.setDepreciationCode(json.getString("depreciationCode"));
		po.setLifeYearsNew(json.getInt("lifeYearsNew"));
		po.setVendorName(json.getString("vendorName"));
		po.setVendorNumber(json.getString("vendorNumber"));
		po.setProjectNumber(json.getString("projectNumber"));
		po.setCurrency(json.getString("currency"));
		po.setUnitPrice(json.getDouble("unitPrice"));
		po.setPoLine(json.getInt("poLine"));
		po.setLevel1Description(json.getString("level1Description"));
		po.setPartNumber(json.getString("partNumber"));
		po.setL3Description(json.getString("l3Description"));
		po.setCostCenter(json.getString("costCenter").trim());
		po.setDatePlacedInService(parseDate(json.getString("datePlacedInService"), dateFormat));
		po.setPoDate(parseDate(json.getString("poDate"), dateFormat));
	}

	private Date parseDate(String dateStr, SimpleDateFormat dateFormat) throws ParseException {
		return new Date(dateFormat.parse(dateStr).getTime());
	}
}
