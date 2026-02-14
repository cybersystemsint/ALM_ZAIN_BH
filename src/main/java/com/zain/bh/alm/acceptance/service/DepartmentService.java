package com.zain.bh.alm.acceptance.service;

import java.util.List;
import java.util.Map;

import com.zain.bh.alm.acceptance.entity.Department;

public interface DepartmentService {
	Map<String, String> createOrUpdateDepartments(String jsonRequest);
	List<Department> getDepartmentsByStatus(boolean sysStatus);
}
