package com.zain.bh.alm.acceptance.service;

import java.util.List;
import java.util.Map;

import com.zain.bh.alm.acceptance.entity.DccPoStatusCombinedView;
import com.zain.bh.alm.acceptance.entity.UPL;

public interface ReportService {
	Map<String, Object> getPurchaseOrders(String supplierId, int page, int size);
	List<Map<String, Object>> getAcceptanceReport(String poNumber);
	List<Map<String, Object>> getCapitalizationReport(String poNumber);
	Map<String, Object> getAllItemCodeSubstitutes(int recordNo, int page, int size);
	List<Map<String, Object>> getAllChargeAccounts(int recordNo);
	Map<String, Object> getAllPurchaseOrders(String supplierId, int page, int size);
	Map<String, Object> getNestedPurchaseOrders(String supplierId, String poNumber, int page, int size);
	Map<String, Object> getPoUplPerSupplierAndPoNumber(String supplierId, String poId, int page, int size);
	Map<String, Object> getAllCreatedUPLs(String poNumber, int page, int size);
	Map<String, Object> getDccData(String supplierId, int page, int size);
	List<Map<String, Object>> getDccPerRecordNo(String supplierId, int recordNo);
	List<DccPoStatusCombinedView> getDccStatusData(String supplierId);
	List<UPL> getUplData(String poId);
	List<UPL> getAllUpls();
}
