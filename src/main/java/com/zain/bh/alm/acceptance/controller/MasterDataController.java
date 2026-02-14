package com.zain.bh.alm.acceptance.controller;

import static com.zain.bh.alm.acceptance.constant.AppConstants.ERROR;
import static com.zain.bh.alm.acceptance.constant.AppConstants.FAILED_UPLOAD;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zain.bh.alm.acceptance.entity.Department;
import com.zain.bh.alm.acceptance.util.FileLogUtil;
import com.zain.bh.alm.acceptance.service.DepartmentService;

@RestController
public class MasterDataController {

	private static final Logger LOGGER = LogManager.getLogger(MasterDataController.class);

	private final DepartmentService departmentService;

	public MasterDataController(DepartmentService departmentService) {
		this.departmentService = departmentService;
	}

	// Creates or updates departments
	@PostMapping(value = "/masterdata/postdepts")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> postdepts(@RequestBody String request) {
		String batchFilename = FAILED_UPLOAD + "_" + System.currentTimeMillis() + ".json";
		FileLogUtil.logBatchFile(request, true, batchFilename);
		FileLogUtil.logToFile("Depts_BATCHUPLOAD file name /home/app/logs/ALM/BatchFiles/" + batchFilename, "INFO");
		LOGGER.info("POST DEPTS REQUEST | {}", request);
		Map<String, String> response = departmentService.createOrUpdateDepartments(request);
		if (ERROR.equals(response.get("responseCode"))) {
			FileLogUtil.logBatchFile(response.get("responseMessage"), true, batchFilename);
		}
		return response;
	}

	// Retrieves departments by status
	@PostMapping(value = "/masterdata/getdepts")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public String getdepts(@RequestBody String request) {
		LOGGER.info("GET DEPTS REQUEST | {}", request);
		try {
			JsonObject jsonObject = new JsonParser().parse(request).getAsJsonObject();
			boolean sysStatus = jsonObject.get("sysStatus").getAsBoolean();
			List<Department> departments = departmentService.getDepartmentsByStatus(sysStatus);
			if (!departments.isEmpty()) {
				return new Gson().toJson(departments);
			} else {
				return "No Depts Data found.";
			}
		} catch (Exception exception) {
			LOGGER.error("Error getting departments", exception);
			return null;
		}
	}
}
