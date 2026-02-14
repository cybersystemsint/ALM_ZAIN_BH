package com.zain.bh.alm.acceptance.service.impl;

import static com.zain.bh.alm.acceptance.constant.AppConstants.*;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.zain.bh.alm.acceptance.entity.Department;
import com.zain.bh.alm.acceptance.repository.DepartmentRepository;
import com.zain.bh.alm.acceptance.service.DepartmentService;
import com.zain.bh.alm.acceptance.util.ResponseUtil;

@Service
public class DepartmentServiceImpl implements DepartmentService {

	private static final Logger LOGGER = LogManager.getLogger(DepartmentServiceImpl.class);

	private final DepartmentRepository departmentRepo;

	public DepartmentServiceImpl(DepartmentRepository departmentRepo) {
		this.departmentRepo = departmentRepo;
	}

	@Override
	public Map<String, String> createOrUpdateDepartments(String jsonRequest) {
		JSONArray errorArray = new JSONArray();
		try {
			LOGGER.debug("Processing department request");
			JSONArray jsonArray = new JSONArray(jsonRequest);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				long recordNo = jsonObject.getLong("recordNo");
				String deptName = jsonObject.getString("deptName");
				boolean sysStatus = jsonObject.getBoolean("sysStatus");

				Department existingDept = departmentRepo.findByRecordNo(recordNo);
				if (existingDept != null) {
					updateDepartment(existingDept, deptName, sysStatus, recordNo, errorArray);
				} else {
					createDepartment(deptName, sysStatus, recordNo, errorArray);
				}
			}

			return errorArray.length() > 0 ? ResponseUtil.createResponse(ERROR, errorArray.toString())
					: ResponseUtil.createResponse(SUCCESS, COMPLETE);
		} catch (Exception exception) {
			LOGGER.error("Error processing departments", exception);
			return ResponseUtil.createResponse(ERROR, exception.getMessage());
		}
	}

	@Override
	public List<Department> getDepartmentsByStatus(boolean sysStatus) {
		return departmentRepo.findBySysStatus(sysStatus);
	}

	private void updateDepartment(Department dept, String deptName, boolean sysStatus, long recordNo,
			JSONArray errorArray) {
		dept.setDeptName(deptName);
		dept.setSysStatus(sysStatus);
		try {
			departmentRepo.save(dept);
		} catch (Exception exception) {
			LOGGER.error("Error updating department", exception);
			addError(errorArray, deptName, recordNo, exception);
		}
	}

	private void createDepartment(String deptName, boolean sysStatus, long recordNo, JSONArray errorArray) {
		Department newDept = new Department();
		newDept.setDeptName(deptName);
		newDept.setSysStatus(sysStatus);
		try {
			departmentRepo.save(newDept);
		} catch (Exception exception) {
			LOGGER.error("Error creating department", exception);
			addError(errorArray, deptName, recordNo, exception);
		}
	}

	private void addError(JSONArray errorArray, String deptName, long recordNo, Exception exception) {
		JSONObject errorObject = new JSONObject();
		errorObject.put("deptName", deptName);
		errorObject.put("recordNo", recordNo);
		errorObject.put("DBresponse",
				exception.getCause() != null ? exception.getCause().toString() : exception.getMessage());
		errorArray.put(errorObject);
	}
}
