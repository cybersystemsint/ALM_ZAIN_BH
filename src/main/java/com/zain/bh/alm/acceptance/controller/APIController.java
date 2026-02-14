package com.zain.bh.alm.acceptance.controller;

import static com.zain.bh.alm.acceptance.constant.AppConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zain.bh.alm.acceptance.entity.PurchaseOrderHeader;
import com.zain.bh.alm.acceptance.util.FileLogUtil;
import com.zain.bh.alm.acceptance.service.ChargeAccountService;
import com.zain.bh.alm.acceptance.service.DCCManagementService;
import com.zain.bh.alm.acceptance.service.DCCStatusService;
import com.zain.bh.alm.acceptance.service.ErrorMessageService;
import com.zain.bh.alm.acceptance.service.FileService;
import com.zain.bh.alm.acceptance.service.ItemCodeSubstituteService;
import com.zain.bh.alm.acceptance.service.POCreationService;
import com.zain.bh.alm.acceptance.service.POHDLNService;
import com.zain.bh.alm.acceptance.service.POService;
import com.zain.bh.alm.acceptance.service.POUPLCreationService;
import com.zain.bh.alm.acceptance.service.SupplierService;
import com.zain.bh.alm.acceptance.service.UPLManagementService;
import com.zain.bh.alm.acceptance.service.UPLService;
import com.zain.bh.alm.acceptance.util.ResponseUtil;

@RestController
public class APIController {

	private static final Logger LOGGER = LogManager.getLogger(APIController.class);

	private final ChargeAccountService chargeAccountService;
	private final ItemCodeSubstituteService itemCodeSubstituteService;
	private final ErrorMessageService errorMessageService;
	private final SupplierService supplierService;
	private final POService poService;
	private final UPLService uplService;
	private final FileService fileService;
	private final DCCStatusService dccStatusService;
	private final POHDLNService pohdlnService;
	private final UPLManagementService uplManagementService;
	private final POCreationService poCreationService;
	private final POUPLCreationService pouplCreationService;
	private final DCCManagementService dccManagementService;

	public APIController(ChargeAccountService chargeAccountService, ItemCodeSubstituteService itemCodeSubstituteService,
			ErrorMessageService errorMessageService, SupplierService supplierService, POService poService,
			UPLService uplService, FileService fileService, DCCStatusService dccStatusService,
			POHDLNService pohdlnService, UPLManagementService uplManagementService, POCreationService poCreationService,
			POUPLCreationService pouplCreationService, DCCManagementService dccManagementService) {
		this.chargeAccountService = chargeAccountService;
		this.itemCodeSubstituteService = itemCodeSubstituteService;
		this.errorMessageService = errorMessageService;
		this.supplierService = supplierService;
		this.poService = poService;
		this.uplService = uplService;
		this.fileService = fileService;
		this.dccStatusService = dccStatusService;
		this.pohdlnService = pohdlnService;
		this.uplManagementService = uplManagementService;
		this.poCreationService = poCreationService;
		this.pouplCreationService = pouplCreationService;
		this.dccManagementService = dccManagementService;
	}

	// Creates or updates item code substitutes
	@PostMapping(value = "/createItemCodeSubstitutes")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> createItemCodeSubstitutes(@RequestBody String request) {
		LOGGER.info("createItemCodeSubstitutes Req | {}", request);
		try {
			itemCodeSubstituteService.createOrUpdateFromJson(request);
			return ResponseUtil.createResponse(SUCCESS, COMPLETE);
		} catch (Exception exception) {
			LOGGER.error("Error creating item code substitutes", exception);
			return ResponseUtil.createResponse(ERROR, exception.getMessage());
		}
	}

	// Creates or updates error messages
	@PostMapping(value = "/createErrorMessage")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> createErrorMessage(@RequestBody String request) {
		LOGGER.info("createErrorMessage Req | {}", request);
		try {
			errorMessageService.createOrUpdateFromJson(request);
			return ResponseUtil.createResponse(SUCCESS, COMPLETE);
		} catch (Exception exception) {
			LOGGER.error("Error creating error message", exception);
			return ResponseUtil.createResponse(ERROR, exception.getMessage());
		}
	}

	// Creates or updates charge account
	@PostMapping(value = "/createChargeAccount")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> createChargeAccount(@RequestBody String request) {
		LOGGER.info("ChargeAccountRequest | {}", request);
		try {
			chargeAccountService.createOrUpdateFromJson(request);
			return ResponseUtil.createResponse(SUCCESS, COMPLETE);
		} catch (Exception exception) {
			LOGGER.error("Error creating charge account", exception);
			return ResponseUtil.createResponse(ERROR, exception.getMessage());
		}
	}

	// Soft deletes a charge account by record number
	@PostMapping(value = "/deleteChargeAccount")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> deleteChargeAccount(@RequestBody String request) {
		LOGGER.info("Delete Charge Account Request | {}", request);
		try {
			JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
			Long recordNo = jsonObject.get("recordNo").getAsLong();
			chargeAccountService.delete(recordNo);
			return ResponseUtil.createResponse(SUCCESS, "Record Deleted Successfully");
		} catch (Exception exception) {
			LOGGER.error("Error deleting charge account", exception);
			return ResponseUtil.createResponse(ERROR, exception.getMessage());
		}
	}

	// Creates or updates purchase orders with validation
	@PostMapping(value = "/createpo")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> createpo(@RequestBody String request) {
		String batchFilename = "";
		LOGGER.info("PO CREATE REQUEST | {}", request);
		try {
			Map<String, Object> result = poCreationService.processFromJson(request);
			String responseInfo = (String) result.get("responseinfo");
			List<String> validationErrors = (List<String>) result.get("validationErrors");

			LOGGER.info("PO CREATE RESPONSE | {}", responseInfo);
			LOGGER.info("VALIDATION RESPONSE | {}", validationErrors);

			if (!validationErrors.isEmpty()) {
				batchFilename = getbatchfilename(FAILED_UPLOAD);
				FileLogUtil.logBatchFile(responseInfo, true, batchFilename);
				return ResponseUtil.createResponse(ERROR, "PO numbers and Line Items: "
						+ String.join(", ", validationErrors) + " are already uploaded. Duplicates not allowed");
			} else if (!responseInfo.contains(SUCCESS)) {
				batchFilename = getbatchfilename(FAILED_UPLOAD);
				FileLogUtil.logBatchFile(responseInfo, true, batchFilename);
				return ResponseUtil.createResponse(ERROR, responseInfo);
			} else {
				return ResponseUtil.createResponse(SUCCESS, COMPLETE);
			}
		} catch (Exception exception) {
			LOGGER.error("Error creating PO", exception);
			return ResponseUtil.createResponse(ERROR, exception.getMessage());
		}
	}

	// Creates or updates PO UPL records with validation
	@PostMapping(value = "/createpoupl")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> createpoupl(@RequestBody String request) {
		String batchFilename = "";
		LOGGER.info("UPL CREATE REQUEST | {}", request);
		try {
			Map<String, Object> result = pouplCreationService.processFromJson(request);
			String responseInfo = (String) result.get("responseinfo");
			List<String> missingPoNumbers = (List<String>) result.get("missingPoNumbers");

			LOGGER.info("UPL CREATE RESPONSE | {}", responseInfo);

			if (!missingPoNumbers.isEmpty()) {
				batchFilename = getbatchfilename(FAILED_UPLOAD);
				FileLogUtil.logBatchFile(responseInfo, true, batchFilename);
				return ResponseUtil.createResponse(ERROR,
						"Missing PO Numbers: " + String.join(", ", missingPoNumbers));
			} else if (!responseInfo.contains(SUCCESS)) {
				batchFilename = getbatchfilename(FAILED_UPLOAD);
				FileLogUtil.logBatchFile(responseInfo, true, batchFilename);
				return ResponseUtil.createResponse(ERROR, responseInfo);
			} else {
				return ResponseUtil.createResponse(SUCCESS, COMPLETE);
			}
		} catch (Exception exception) {
			LOGGER.error("Error creating PO UPL", exception);
			return ResponseUtil.createResponse(ERROR, exception.getMessage());
		}
	}

	// Processes PO header and line items
	@PostMapping(value = "/postpohdln")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> postpohd(@RequestBody String request) {
		LOGGER.info("Post PO HD LN Request");
		try {
			String result = pohdlnService.processFromJson(request);
			if (result.equals(COMPLETE)) {
				return ResponseUtil.createResponse(SUCCESS, COMPLETE);
			} else {
				String batchFilename = getbatchfilename(FAILED_UPLOAD);
				FileLogUtil.logBatchFile(result, true, batchFilename);
				return ResponseUtil.createResponse(ERROR, result);
			}
		} catch (Exception exception) {
			LOGGER.error("Error processing PO HD LN", exception);
			return ResponseUtil.createResponse(ERROR, exception.getMessage());
		}
	}

	// Soft deletes a purchase order by PO ID
	@PostMapping(value = "/deletePo")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> deletePO(@RequestBody String request) {
		LOGGER.info("Delete PO Request | {}", request);
		try {
			JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
			String poId = jsonObject.get("poId").getAsString();
			poService.deletePO(poId);
			return ResponseUtil.createResponse(SUCCESS, "Record Deleted Success");
		} catch (Exception exception) {
			LOGGER.error("Error deleting PO", exception);
			return ResponseUtil.createResponse(ERROR, exception.getMessage());
		}
	}

	private String getbatchfilename(String filetype) {
		return filetype + "_" + System.currentTimeMillis() + ".json";
	}

	// Retrieves file attachments for a DCC by PO number and DCC ID
	@PostMapping(value = "/getAttachments")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public ResponseEntity<Map<String, Object>> getFiles(@RequestBody Map<String, Object> requestBody) {
		Map<String, Object> response = new HashMap<>();
		String poNumber = (String) requestBody.get("poNumber");
		Integer dccId = (Integer) requestBody.get("dccId");

		LOGGER.info("Fetching files for poNumber: {} and dccId: {}", poNumber, dccId);

		if (poNumber == null || dccId == null) {
			response.put("responseCode", "1");
			response.put("responseDesc", "poNumber and dccId are required");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		try {
			List<Map<String, String>> files = fileService.getAttachments(poNumber, dccId);
			response.put("responseCode", "0");
			response.put("responseDesc", "Record Fetched Success");
			response.put("files", files);
			return ResponseEntity.ok(response);
		} catch (Exception exception) {
			LOGGER.error("Error fetching attachments", exception);
			response.put("responseCode", "1");
			response.put("responseDesc", exception.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}

	// Creates DCC acceptance request with file uploads and validation
	@PostMapping(value = "/postdcc")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> postdcc(@RequestPart(value = "file", required = false) List<MultipartFile> files,
			@RequestPart("data") String request) {
		LOGGER.info("| CREATE ACCEPTANCE REQUEST {}", request);
		String batchFilename = "";

		try {
			JSONArray jsonArray = new JSONArray(request);
			if (jsonArray.length() == 0) {
				return ResponseUtil.createResponse(ERROR, "No data provided");
			}

			batchFilename = getbatchfilename("DCC_BATCHUPLOAD");
			FileLogUtil.logBatchFile(request, true, batchFilename);
			FileLogUtil.logToFile("DCC_BatchUpload file name /home/app/logs/ALM/BatchFiles/" + batchFilename, "INFO");

			Map<String, Object> result = dccManagementService.processFromJson(request, files);
			boolean success = (boolean) result.get("success");
			String errors = (String) result.get("errors");

			if (!success) {
				if (!errors.isEmpty() && !errors.equals("[]")) {
					batchFilename = getbatchfilename(FAILED_UPLOAD);
					FileLogUtil.logBatchFile(errors, true, batchFilename);
				}
				return ResponseUtil.createResponse(ERROR, errors);
			} else {
				return ResponseUtil.createResponse(SUCCESS, COMPLETE);
			}
		} catch (Exception exception) {
			LOGGER.error("Error processing DCC", exception);
			return ResponseUtil.createResponse(ERROR, exception.getMessage());
		}
	}

	// Creates or updates supplier information
	@PostMapping(value = "/postsupplier")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> postsupplier(@RequestBody String request) {
		LOGGER.info("Supplier Request | {}", request);
		try {
			supplierService.createOrUpdateFromJson(request);
			return ResponseUtil.createResponse(SUCCESS, COMPLETE);
		} catch (Exception exception) {
			LOGGER.error("Error creating/updating supplier", exception);
			return ResponseUtil.createResponse(ERROR, exception.getMessage());
		}
	}

	// Updates DCC status
	@PostMapping(value = "/postdccstatus")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> postdccstatus(@RequestBody String request) {
		LOGGER.info("DCC Status Request | {}", request);
		try {
			dccStatusService.processFromJson(request);
			return ResponseUtil.createResponse(SUCCESS, COMPLETE);
		} catch (Exception exception) {
			LOGGER.error("Error processing DCC status", exception);
			return ResponseUtil.createResponse(ERROR, exception.getMessage());
		}
	}

	// Creates or updates UPL records with approval workflow
	@PostMapping(value = "/postupl")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> postupl(@RequestBody String request) {
		String batchFilename = getbatchfilename("UPL_BATCHUPLOAD");
		FileLogUtil.logBatchFile(request, true, batchFilename);
		FileLogUtil.logToFile("UPL_BATCHUPLOAD file name /home/app/logs/ALM/BatchFiles/" + batchFilename, "INFO");
		LOGGER.info("Post UPL Request");
		try {
			String result = uplManagementService.processFromJson(request);
			if (result.equals(COMPLETE)) {
				return ResponseUtil.createResponse(SUCCESS, COMPLETE);
			} else {
				batchFilename = getbatchfilename(FAILED_UPLOAD);
				FileLogUtil.logBatchFile(result, true, batchFilename);
				return ResponseUtil.createResponse(ERROR, result);
			}
		} catch (Exception exception) {
			LOGGER.error("Error processing UPL", exception);
			return ResponseUtil.createResponse(ERROR, exception.getMessage());
		}
	}

	// Soft deletes a UPL record by UPL line number
	@PostMapping(value = "/deleteUpl")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> deleteUpl(@RequestBody String request) {
		LOGGER.info("Delete UPL Request | {}", request);
		try {
			JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
			Integer uplLine = jsonObject.get("uplLine").getAsInt();
			uplService.deleteUPL(uplLine);
			return ResponseUtil.createResponse(SUCCESS, "Record Deleted Success");
		} catch (Exception exception) {
			LOGGER.error("Error deleting UPL", exception);
			return ResponseUtil.createResponse(ERROR, exception.getMessage());
		}
	}

	// Approves a purchase order
	@PostMapping(value = "/approvepo")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> approvepo(@RequestBody String request) {
		LOGGER.info("Approve PO Request | {}", request);
		return ResponseUtil.createResponse(SUCCESS, COMPLETE);
	}

	// Fetches PO data by PO ID and supplier ID
	@PostMapping(value = "/fetchpodata")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> fetchpodata(@RequestBody String request) {
		LOGGER.info("FetchPOData Request: {}", request);
		try {
			JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
			String poId = jsonObject.get("poId").getAsString();
			String supplierId = jsonObject.get("supplierId").getAsString();

			List<PurchaseOrderHeader> poList = poService.fetchPOData(poId, supplierId);
			ObjectMapper mapper = new ObjectMapper();
			String jsonResponse = mapper.writeValueAsString(poList);
			return ResponseUtil.createResponse(SUCCESS, jsonResponse);
		} catch (Exception exception) {
			LOGGER.error("Error fetching PO data", exception);
			return ResponseUtil.createResponse(ERROR, exception.getMessage());
		}
	}

}
