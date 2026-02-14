package com.zain.bh.alm.acceptance.controller;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zain.bh.alm.acceptance.entity.DccPoStatusCombinedView;
import com.zain.bh.alm.acceptance.entity.UPL;
import com.zain.bh.alm.acceptance.util.FileLogUtil;
import com.zain.bh.alm.acceptance.service.ReportService;

@RestController
public class ReportsController {

	private static final Logger LOGGER = LogManager.getLogger(ReportsController.class);

	private final ReportService reportService;

	public ReportsController(ReportService reportService) {
		this.reportService = reportService;
	}

	// Retrieves Bahrain purchase orders with pagination
	@PostMapping(value = "/reports/getPurchaseOrders", produces = "application/json")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, Object> getPurchaseOrders(@RequestBody String request) {
		LOGGER.info("GET PURCHASE ORDERS REQUEST | {}", request);
		JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
		String supplierId = jsonObject.get("supplierId").getAsString();
		int page = jsonObject.has("page") ? jsonObject.get("page").getAsInt() : 1;
		int size = jsonObject.has("size") ? jsonObject.get("size").getAsInt() : 20000;
		return reportService.getPurchaseOrders(supplierId, page, size);
	}

	// Retrieves acceptance report data
	@PostMapping(value = "/reports/acceptanceReport", produces = "application/json")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public List<Map<String, Object>> acceptanceReport(@RequestBody String request) {
		LOGGER.info("ACCEPTANCE REPORT REQUEST | {}", request);
		JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
		String poNumber = jsonObject.get("poNumber").getAsString();
		return reportService.getAcceptanceReport(poNumber);
	}

	// Retrieves capitalization report data
	@PostMapping(value = "/reports/capitalizationReport", produces = "application/json")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public List<Map<String, Object>> capitalizationReport(@RequestBody String request) {
		LOGGER.info("CAPITALIZATION REPORT REQUEST | {}", request);
		JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
		String poNumber = jsonObject.get("poNumber").getAsString();
		return reportService.getCapitalizationReport(poNumber);
	}

	// Retrieves all item code substitutes with pagination
	@PostMapping(value = "/reports/getAllItemCodeSubstitutes", produces = "application/json")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, Object> getAllItemCodeSubstitutes(@RequestBody String request) {
		LOGGER.info("GET ALL ITEM CODE SUBSTITUTES REQUEST | {}", request);
		JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
		int recordNo = jsonObject.get("recordNo").getAsInt();
		int page = jsonObject.has("page") ? jsonObject.get("page").getAsInt() : 1;
		int size = jsonObject.has("size") ? jsonObject.get("size").getAsInt() : 20000;
		return reportService.getAllItemCodeSubstitutes(recordNo, page, size);
	}

	// Retrieves all charge accounts
	@PostMapping(value = "/reports/getAllChargeAccounts", produces = "application/json")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public List<Map<String, Object>> getAllChargeAccounts(@RequestBody String request) {
		LOGGER.info("GET ALL CHARGE ACCOUNTS REQUEST | {}", request);
		JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
		int recordNo = jsonObject.get("recordNo").getAsInt();
		return reportService.getAllChargeAccounts(recordNo);
	}

	// Retrieves all purchase orders with pagination
	@PostMapping(value = "/reports/getAllPurchaseOrders", produces = "application/json")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, Object> getAllPurchaseOrders(@RequestBody String request) {
		LOGGER.info("GET ALL PURCHASE ORDERS REQUEST | {}", request);
		JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
		String supplierId = jsonObject.get("supplierId").getAsString();
		int page = jsonObject.has("page") ? jsonObject.get("page").getAsInt() : 1;
		int size = jsonObject.has("size") ? jsonObject.get("size").getAsInt() : 20000;
		return reportService.getAllPurchaseOrders(supplierId, page, size);
	}

	// Retrieves nested purchase orders with line items
	@PostMapping(value = "/reports/getNestedPurchaseOrders", produces = "application/json")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, Object> getNestedPurchaseOrders(@RequestBody String request) {
		LOGGER.info("GET NESTED PURCHASE ORDERS REQUEST | {}", request);
		JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
		String supplierId = jsonObject.get("supplierId").getAsString();
		String poNumber = jsonObject.get("poNumber").getAsString();
		int page = jsonObject.has("page") ? jsonObject.get("page").getAsInt() : 1;
		int size = jsonObject.has("size") ? jsonObject.get("size").getAsInt() : 20000;
		return reportService.getNestedPurchaseOrders(supplierId, poNumber, page, size);
	}

	// Retrieves PO and UPL data per supplier and PO number
	@PostMapping(value = "/reports/poUplPerSupplierAndPoNumber", produces = "application/json")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, Object> poUplPerSupplierAndPoNumber(@RequestBody String request) {
		LOGGER.info("PO UPL PER SUPPLIER AND PO NUMBER REQUEST | {}", request);
		JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
		String supplierId = jsonObject.get("supplierId").getAsString();
		String poId = jsonObject.get("poId").getAsString();
		int page = jsonObject.has("page") ? jsonObject.get("page").getAsInt() : 1;
		int size = jsonObject.has("size") ? jsonObject.get("size").getAsInt() : 20000;
		return reportService.getPoUplPerSupplierAndPoNumber(supplierId, poId, page, size);
	}

	// Retrieves all created UPLs with pagination
	@PostMapping(value = "/reports/getAllCreatedUPLs", produces = "application/json")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, Object> getAllCreatedUPLs(@RequestBody String request) {
		LOGGER.info("GET ALL CREATED UPLS REQUEST | {}", request);
		JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
		String poNumber = jsonObject.get("poNumber").getAsString();
		int page = jsonObject.has("page") ? jsonObject.get("page").getAsInt() : 1;
		int size = jsonObject.has("size") ? jsonObject.get("size").getAsInt() : 20000;
		return reportService.getAllCreatedUPLs(poNumber, page, size);
	}

	// Retrieves DCC data per supplier with pagination
	@PostMapping(value = "/reports/getdccdata", produces = "application/json")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, Object> getdccdata(@RequestBody String request) {
		LOGGER.info("GET DCC DATA REQUEST | {}", request);
		JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
		String supplierId = jsonObject.get("supplierId").getAsString();
		int page = jsonObject.has("page") ? jsonObject.get("page").getAsInt() : 1;
		int size = jsonObject.has("size") ? jsonObject.get("size").getAsInt() : 20000;
		return reportService.getDccData(supplierId, page, size);
	}

	// Retrieves DCC data per supplier and record number
	@PostMapping(value = "/reports/getdccperrecordNo", produces = "application/json")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public List<Map<String, Object>> getdccperrecordNo(@RequestBody String request) {
		LOGGER.info("GET DCC PER RECORD NO REQUEST | {}", request);
		JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
		String supplierId = jsonObject.get("supplierId").getAsString();
		int recordNo = jsonObject.get("recordNo").getAsInt();
		return reportService.getDccPerRecordNo(supplierId, recordNo);
	}

	// Retrieves DCC status data by supplier
	@PostMapping(value = "/reports/getdccstatusdata")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public String getdccstatusdata(@RequestBody String request) {
		try {
			FileLogUtil.logToFile("GetDCCStatusRequest: " + request, "INFO");
			LOGGER.info("GET DCC STATUS REQUEST | {}", request);
			JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
			String supplierId = jsonObject.get("supplierId").getAsString();
			List<DccPoStatusCombinedView> dccPoStatus = reportService.getDccStatusData(supplierId);
			if (!dccPoStatus.isEmpty()) {
				return new Gson().toJson(dccPoStatus);
			} else {
				return "No DCC Status Data found.";
			}
		} catch (Exception exception) {
			LOGGER.error("Error getting DCC status data", exception);
			FileLogUtil.logToFile("GetDCCStatusData error: " + exception.toString(), "INFO");
		}
		return null;
	}

	// Retrieves UPL data by PO ID
	@PostMapping(value = "/reports/getupldata")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public String getupldata(@RequestBody String request) {
		try {
			FileLogUtil.logToFile("GetUPLDataRequest: " + request, "INFO");
			LOGGER.info("GET UPL DATA REQUEST | {}", request);
			JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
			String poId = jsonObject.get("poId").getAsString();
			List<UPL> uplData = reportService.getUplData(poId);
			if (!uplData.isEmpty()) {
				return new Gson().toJson(uplData);
			} else {
				return "No UPL Data found.";
			}
		} catch (Exception exception) {
			LOGGER.error("Error getting UPL data", exception);
			FileLogUtil.logToFile("GetUPLData error: " + exception.toString(), "INFO");
		}
		return null;
	}

	// Retrieves all UPLs
	@GetMapping(value = "/reports/getallupls")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public String getallupls() {
		try {
			List<UPL> uplData = reportService.getAllUpls();
			if (!uplData.isEmpty()) {
				return new Gson().toJson(uplData);
			} else {
				return "No UPL Data found.";
			}
		} catch (Exception exception) {
			LOGGER.error("Error getting all UPLs", exception);
		}
		return null;
	}
}
