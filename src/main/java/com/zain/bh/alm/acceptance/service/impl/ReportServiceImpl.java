package com.zain.bh.alm.acceptance.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.zain.bh.alm.acceptance.entity.DccPoStatusCombinedView;
import com.zain.bh.alm.acceptance.entity.UPL;
import com.zain.bh.alm.acceptance.repository.DccPoStatusCombinedViewRepository;
import com.zain.bh.alm.acceptance.repository.UPLRepository;
import com.zain.bh.alm.acceptance.service.ReportService;

@Service
public class ReportServiceImpl implements ReportService {

	private static final Logger LOGGER = LogManager.getLogger(ReportServiceImpl.class);

	private final JdbcTemplate jdbcTemplate;
	private final UPLRepository uplRepo;
	private final DccPoStatusCombinedViewRepository dccPoCombinedViewRepo;

	public ReportServiceImpl(JdbcTemplate jdbcTemplate, UPLRepository uplRepo, DccPoStatusCombinedViewRepository dccPoCombinedViewRepo) {
		this.jdbcTemplate = jdbcTemplate;
		this.uplRepo = uplRepo;
		this.dccPoCombinedViewRepo = dccPoCombinedViewRepo;
	}

	@Override
	public Map<String, Object> getPurchaseOrders(String supplierId, int page, int size) {
		LOGGER.debug("Fetching purchase orders for supplierId: {}, page: {}, size: {}", supplierId, page, size);
		page = Math.max(page, 0);
		size = Math.max(size, 0);

		String countSql = "SELECT COUNT(*) FROM tb_Po PO";
		if (!supplierId.equalsIgnoreCase("0")) {
			countSql += " WHERE PO.vendorNumber='" + supplierId + "'";
		}
		int totalRecords = jdbcTemplate.queryForObject(countSql, Integer.class);

		String paginationSql = buildPaginationSql(page, size, totalRecords);
		String sql = "SELECT * FROM tb_Po PO";
		if (!supplierId.equalsIgnoreCase("0")) {
			sql += " WHERE PO.vendorNumber='" + supplierId + "'";
		}

		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql + paginationSql);

		return buildPaginatedResponse(result, totalRecords, page, size);
	}

	@Override
	public List<Map<String, Object>> getAcceptanceReport(String poNumber) {
		LOGGER.debug("Fetching acceptance report for poNumber: {}", poNumber);
		String sql = "SELECT * FROM `acceptanceReport` WHERE 1";
		if (!poNumber.equalsIgnoreCase("0")) {
			sql += " AND poNumber='" + poNumber + "'";
		}
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
		addRecordNumbers(result);
		return result;
	}

	@Override
	public List<Map<String, Object>> getCapitalizationReport(String poNumber) {
		LOGGER.debug("Fetching capitalization report for poNumber: {}", poNumber);
		jdbcTemplate.execute("SET SESSION sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))");
		String sql = "SELECT * FROM `capitalizationReport` WHERE 1";
		if (!poNumber.equalsIgnoreCase("0")) {
			sql += " AND poNumber='" + poNumber + "'";
		}
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
		addRecordNumbers(result);
		return result;
	}

	@Override
	public Map<String, Object> getAllItemCodeSubstitutes(int recordNo, int page, int size) {
		page = Math.max(page, 0);
		size = Math.max(size, 0);

		String countSql = "SELECT COUNT(*) FROM tb_ItemCodeSubstitute PO";
		if (recordNo != 0) {
			countSql += " WHERE recordNo = '" + recordNo + "'";
		}
		int totalRecords = jdbcTemplate.queryForObject(countSql, Integer.class);

		String paginationSql = buildPaginationSql(page, size, totalRecords);
		String sql = "SELECT recordNo, recordDateTime, itemCode, relatedItemCode, reciprocalFlag, createdBy, createdDatetime, updatedBy, updatedDateTime FROM tb_ItemCodeSubstitute";
		if (recordNo != 0) {
			sql += " WHERE recordNo = '" + recordNo + "'";
		}

		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql + paginationSql);
		return buildPaginatedResponse(result, totalRecords, page, size);
	}

	@Override
	public List<Map<String, Object>> getAllChargeAccounts(int recordNo) {
		String sql = "SELECT recordNo, recordDatetime, chargeAccount, orgCode, orgName, subInventory, createdBy, createdDatetime, updatedBy, updatedDate AS updatedDatetime FROM tb_ChargeAccount";
		if (recordNo != 0) {
			sql += " WHERE recordNo='" + recordNo + "'";
		}
		return jdbcTemplate.queryForList(sql);
	}

	@Override
	public Map<String, Object> getAllPurchaseOrders(String supplierId, int page, int size) {
		page = Math.max(page, 0);
		size = Math.max(size, 0);

		String countSql = "SELECT COUNT(*) FROM tb_PurchaseOrder PO";
		if (!supplierId.equalsIgnoreCase("0")) {
			countSql += " WHERE PO.vendorNumber='" + supplierId + "'";
		}
		int totalRecords = jdbcTemplate.queryForObject(countSql, Integer.class);

		String paginationSql = buildPaginationSql(page, size, totalRecords);
		String sql = buildPurchaseOrderSelectSql();
		if (!supplierId.equalsIgnoreCase("0")) {
			sql += " WHERE PO.vendorNumber='" + supplierId + "'";
		}

		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql + paginationSql);
		return buildPaginatedResponse(result, totalRecords, page, size);
	}

	@Override
	public Map<String, Object> getNestedPurchaseOrders(String supplierId, String poId, int page, int size) {
		page = Math.max(page, 1);
		size = Math.max(size, 1);

		String conditionSql = buildConditionSql(supplierId, poId);
		String uniquePOsSql = "SELECT DISTINCT PO.poNumber FROM tb_PurchaseOrder PO " + conditionSql;
		List<String> uniquePONumbers = jdbcTemplate.queryForList(uniquePOsSql, String.class);

		if (page == 1 && size == 20000) {
			return buildNestedPurchaseOrdersResponse(uniquePONumbers, uniquePONumbers, page, size);
		}

		String uniquePOsSql2 = uniquePOsSql + " LIMIT " + size + " OFFSET " + (page - 1) * size;
		List<String> uniquePONumbers2 = jdbcTemplate.queryForList(uniquePOsSql2, String.class);

		if (uniquePONumbers2.isEmpty()) {
			return buildEmptyPaginatedResponse(page, size);
		}

		return buildNestedPurchaseOrdersResponse(uniquePONumbers, uniquePONumbers2, page, size);
	}

	@Override
	public Map<String, Object> getPoUplPerSupplierAndPoNumber(String supplierId, String poId, int page, int size) {
		page = Math.max(page, 0);
		size = Math.max(size, 0);

		String conditionSql = buildCombinedViewConditionSql(supplierId, poId);
		String countSql = "SELECT COUNT(*) FROM combinedPurchaseOrderView " + conditionSql;
		int totalRecords = jdbcTemplate.queryForObject(countSql, Integer.class);

		String paginationSql = buildPaginationSql(page, size, totalRecords);
		String sql = "SELECT * from combinedPurchaseOrderView" + conditionSql;

		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql + paginationSql);
		return buildPaginatedResponse(result, totalRecords, page, size);
	}

	@Override
	public Map<String, Object> getAllCreatedUPLs(String poNumber, int page, int size) {
		page = Math.max(page, 0);
		size = Math.max(size, 0);

		String countSql = "SELECT COUNT(*) FROM tb_PurchaseOrderUPL PO";
		if (!poNumber.equalsIgnoreCase("0")) {
			countSql += " WHERE PO.poNumber='" + poNumber + "'";
		}
		int totalRecords = jdbcTemplate.queryForObject(countSql, Integer.class);

		String paginationSql = buildPaginationSql(page, size, totalRecords);
		String sql = buildUPLSelectSql(poNumber);

		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql + paginationSql);
		return buildPaginatedResponse(result, totalRecords, page, size);
	}

	@Override
	public Map<String, Object> getDccData(String supplierId, int page, int size) {
		page = Math.max(page, 0);
		size = Math.max(size, 0);

		jdbcTemplate.execute("SET SESSION sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))");

		String countSql = "SELECT COUNT(*) FROM dccPOCombinedView PO";
		if (!supplierId.equalsIgnoreCase("0")) {
			countSql += " WHERE PO.supplierid='" + supplierId + "'";
		}
		int totalRecords = jdbcTemplate.queryForObject(countSql, Integer.class);

		String paginationSql = buildPaginationSql(page, size, totalRecords);
		String sql = "SELECT * FROM ALM_ZAIN_KSA.dccPOCombinedView";
		if (!supplierId.equalsIgnoreCase("0")) {
			sql += " where supplierid='" + supplierId + "'";
		}

		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql + paginationSql);
		return buildPaginatedResponse(result, totalRecords, page, size);
	}

	@Override
	public List<Map<String, Object>> getDccPerRecordNo(String supplierId, int recordNo) {
		jdbcTemplate.execute("SET SESSION sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))");
		String sql = "SELECT * FROM ALM_ZAIN_KSA.dccPOCombinedView";
		if (!supplierId.equalsIgnoreCase("0")) {
			sql += " where supplierid='" + supplierId + "' and dccRecordNo  = '" + recordNo + "' ";
		}
		return jdbcTemplate.queryForList(sql);
	}

	@Override
	public List<DccPoStatusCombinedView> getDccStatusData(String supplierId) {
		LOGGER.debug("Fetching DCC status data for supplierId: {}", supplierId);
		return dccPoCombinedViewRepo.findBySupplierIdAndDccStatus(supplierId, "inprocess");
	}

	@Override
	public List<UPL> getUplData(String poId) {
		LOGGER.debug("Fetching UPL data for poId: {}", poId);
		return uplRepo.findByPoId(poId);
	}

	@Override
	public List<UPL> getAllUpls() {
		return uplRepo.findAll();
	}

	private String buildPaginationSql(int page, int size, int totalRecords) {
		if (page == 0 && size == 0) {
			return "";
		} else if (page == 1 && size == 20000) {
			page = Math.max(totalRecords > 0 ? 1 : 0, 1);
			size = Math.max(totalRecords, 1);
			int offset = (page - 1) * size;
			return " LIMIT " + size + " OFFSET " + offset;
		} else {
			page = Math.max(page, 1);
			size = Math.max(size, 1);
			int offset = (page - 1) * size;
			return " LIMIT " + size + " OFFSET " + offset;
		}
	}

	private Map<String, Object> buildPaginatedResponse(List<Map<String, Object>> data, int totalRecords, int page, int size) {
		Map<String, Object> response = new HashMap<>();
		response.put("data", data);
		response.put("totalRecords", totalRecords);
		response.put("currentPage", page);
		response.put("pageSize", size);
		response.put("totalPages", size > 0 ? (int) Math.ceil((double) totalRecords / size) : 0);
		return response;
	}

	private Map<String, Object> buildEmptyPaginatedResponse(int page, int size) {
		Map<String, Object> response = new HashMap<>();
		response.put("currentPage", page);
		response.put("pageSize", size);
		response.put("totalRecords", 0);
		response.put("totalPages", 0);
		response.put("data", new ArrayList<>());
		return response;
	}

	private void addRecordNumbers(List<Map<String, Object>> result) {
		AtomicInteger counter = new AtomicInteger(1);
		result.forEach(row -> row.put("recordNo", counter.getAndIncrement()));
	}

	private String buildConditionSql(String supplierId, String poId) {
		if (!supplierId.equalsIgnoreCase("0") && !poId.equalsIgnoreCase("0")) {
			return " WHERE PO.vendorNumber='" + supplierId + "' AND PO.poNumber='" + poId + "'";
		} else if (!supplierId.equalsIgnoreCase("0")) {
			return " WHERE PO.vendorNumber='" + supplierId + "'";
		} else if (!poId.equalsIgnoreCase("0")) {
			return " WHERE PO.poNumber='" + poId + "'";
		}
		return "";
	}

	private String buildCombinedViewConditionSql(String supplierId, String poId) {
		if (!supplierId.equalsIgnoreCase("0") && !poId.equalsIgnoreCase("0")) {
			return " WHERE poVendorNumber='" + supplierId + "' AND poNumber='" + poId + "'";
		} else if (!supplierId.equalsIgnoreCase("0")) {
			return " WHERE poVendorNumber='" + supplierId + "'";
		} else if (!poId.equalsIgnoreCase("0")) {
			return " WHERE poNumber='" + poId + "'";
		}
		return "";
	}

	private String buildPurchaseOrderSelectSql() {
		return "SELECT PO.recordNo, PO.poNumber, PO.typeLookUpCode, PO.blanketTotalAmount, PO.releaseNum, PO.lineNumber, "
				+ "PO.prNum, PO.projectName, PO.lineCancelFlag, PO.cancelReason, PO.itemPartNumber, PO.prSubAllow, "
				+ "PO.countryOfOrigin, PO.poOrderQuantity, PO.poQtyNew, PO.quantityReceived, PO.quantityDueOld, PO.quantityDueNew, "
				+ "PO.quantityBilled, PO.currencyCode, PO.unitPriceInPoCurrency, PO.unitPriceInSAR, PO.linePriceInPoCurrency, "
				+ "PO.linePriceInSAR, PO.amountReceived, PO.amountDue, PO.amountDueNew, PO.amountBilled, PO.poLineDescription, "
				+ "PO.organizationName, PO.organizationCode, PO.subInventoryCode, PO.receiptRouting, PO.authorisationStatus, "
				+ "PO.poClosureStatus, PO.departmentName, PO.businessOwner, PO.poLineType, PO.acceptanceType, PO.costCenter, "
				+ "PO.chargeAccount, PO.serialControl, PO.vendorSerialNumberYN, PO.itemType, PO.itemCategoryInventory, "
				+ "PO.inventoryCategoryDescription, PO.itemCategoryFA, PO.FACategoryDescription, PO.itemCategoryPurchasing, "
				+ "PO.PurchasingCategoryDescription, PO.vendorName, PO.vendorNumber, PO.approvedDate, PO.createdDate, "
				+ "CASE WHEN `PO`.`lineCancelFlag` = 0 AND `PO`.`authorisationStatus` = 'APPROVED' AND `PO`.`poClosureStatus` = 'OPEN' "
				+ "THEN 'YES' ELSE 'NO' END AS `canRaiseAcceptance`, PO.createdByName, PO.descopedLinePriceInPoCurrency, "
				+ "PO.newLinePriceInPoCurrency FROM tb_PurchaseOrder PO";
	}

	private String buildUPLSelectSql(String poNumber) {
		String sql = "SELECT UPL.recordNo, UPL.recordDatetime, UPL.vendor, UPL.manufacturer, UPL.countryOfOrigin, UPL.projectName, UPL.poType, UPL.releaseNumber, UPL.poNumber, UPL.poLineNumber, UPL.uplLine, UPL.poLineItemType, UPL.poLineItemCode, UPL.poLineDescription, UPL.uplLineItemType,UPL.uplLineItemCode,UPL.uplLineDescription,UPL.zainItemCategoryCode, UPL.zainItemCategoryDescription, UPL.uplItemSerialized, UPL.activeOrPassive, UPL.uom, UPL.currency, UPL.poLineQuantity, UPL.poLineUnitPrice, UPL.uplLineQuantity, UPL.uplLineUnitPrice, UPL.substituteItemCode, UPL.remarks, UPL.dptApprover1, UPL.dptApprover2, UPL.dptApprover3, UPL.dptApprover4,UPL.regionalApprover,UPL.createdBy, UPL.createdByName, UPL.uplModifiedBy as updatedByName, UPL.uplModifiedDate AS updatedDatetime FROM tb_PurchaseOrderUPL UPL";
		if (!poNumber.equalsIgnoreCase("0")) {
			sql += " WHERE UPL.poNumber='" + poNumber + "'";
		}
		return sql;
	}

	private Map<String, Object> buildNestedPurchaseOrdersResponse(List<String> allUniquePoNumbers, List<String> pagedPoNumbers, int page, int size) {
		String lineItemsSql = "SELECT * FROM tb_PurchaseOrder PO WHERE PO.poNumber IN ("
				+ String.join(",", pagedPoNumbers.stream().map(po -> "'" + po + "'").collect(Collectors.toList())) + ")";
		List<Map<String, Object>> lineItems = jdbcTemplate.queryForList(lineItemsSql);

		Map<String, Map<String, Object>> groupedResults = new LinkedHashMap<>();
		for (Map<String, Object> lineItem : lineItems) {
			String poNumber = (String) lineItem.get("poNumber");

			if (!groupedResults.containsKey(poNumber)) {
				Map<String, Object> groupedRow = new LinkedHashMap<>(lineItem);
				removeUnnecessaryFields(groupedRow);
				initializeTotals(groupedRow);
				groupedRow.put("POlineItems", new ArrayList<Map<String, Object>>());
				groupedResults.put(poNumber, groupedRow);
			}

			Map<String, Object> poLineItem = buildPoLineItem(lineItem);
			((List<Map<String, Object>>) groupedResults.get(poNumber).get("POlineItems")).add(poLineItem);
			updateTotals(groupedResults.get(poNumber), lineItem);
		}

		Map<String, Object> response = new HashMap<>();
		response.put("currentPage", page);
		response.put("pageSize", page == 1 && size == 20000 ? allUniquePoNumbers.size() : size);
		response.put("totalRecords", allUniquePoNumbers.size());
		response.put("totalPages", page == 1 && size == 20000 ? 1 : (int) Math.ceil((double) allUniquePoNumbers.size() / size));
		response.put("data", new ArrayList<>(groupedResults.values()));

		return response;
	}

	private void removeUnnecessaryFields(Map<String, Object> row) {
		row.remove("recordNo");
		row.remove("lineNumber");
		row.remove("countryOfOrigin");
		row.remove("poOrderQuantity");
		row.remove("poQtyNew");
		row.remove("quantityReceived");
		row.remove("quantityDueOld");
		row.remove("quantityDueNew");
		row.remove("quantityBilled");
		row.remove("unitPriceInPoCurrency");
		row.remove("unitPriceInSAR");
		row.remove("linePriceInPoCurrency");
		row.remove("linePriceInSAR");
		row.remove("amountReceived");
		row.remove("amountDue");
		row.remove("amountDueNew");
		row.remove("amountBilled");
		row.remove("poLineDescription");
		row.remove("vendorSerialNumberYN");
		row.remove("itemCategoryInventory");
		row.remove("inventoryCategoryDescription");
		row.remove("itemCategoryFA");
		row.remove("FACategoryDescription");
		row.remove("descopedLinePriceInPoCurrency");
		row.remove("newLinePriceInPoCurrency");
	}

	private void initializeTotals(Map<String, Object> row) {
		row.put("totalPoQtyNew", 0.0);
		row.put("totalQuantityReceived", 0.0);
		row.put("totalQuantityDueOld", 0.0);
		row.put("totalQuantityDueNew", 0.0);
		row.put("totalQuantityBilled", 0.0);
		row.put("totalpoOrderQuantity", 0.0);
		row.put("totalunitPriceInPoCurrency", 0.0);
		row.put("totalunitPriceInSAR", 0.0);
		row.put("totallinePriceInPoCurrency", 0.0);
		row.put("totallinePriceInSAR", 0.0);
		row.put("totalamountReceived", 0.0);
		row.put("totalamountDue", 0.0);
		row.put("totalamountDueNew", 0.0);
		row.put("totalamountBilled", 0.0);
		row.put("totalDescopedLinePriceInPoCurrency", 0.0);
		row.put("totalNewLinePriceInPoCurrency", 0.0);
	}

	private Map<String, Object> buildPoLineItem(Map<String, Object> lineItem) {
		Map<String, Object> poLineItem = new LinkedHashMap<>();
		poLineItem.put("recordNo", lineItem.get("recordNo"));
		poLineItem.put("poNumber", lineItem.get("poNumber"));
		poLineItem.put("lineNumber", lineItem.get("lineNumber"));
		poLineItem.put("itemPartNumber", lineItem.get("itemPartNumber"));
		poLineItem.put("countryOfOrigin", lineItem.get("countryOfOrigin"));
		poLineItem.put("poOrderQuantity", lineItem.get("poOrderQuantity"));
		poLineItem.put("poQtyNew", lineItem.get("poQtyNew"));
		poLineItem.put("quantityReceived", lineItem.get("quantityReceived"));
		poLineItem.put("quantityDueOld", lineItem.get("quantityDueOld"));
		poLineItem.put("quantityDueNew", lineItem.get("quantityDueNew"));
		poLineItem.put("quantityBilled", lineItem.get("quantityBilled"));
		poLineItem.put("unitPriceInPoCurrency", lineItem.get("unitPriceInPoCurrency"));
		poLineItem.put("unitPriceInSAR", lineItem.get("unitPriceInSAR"));
		poLineItem.put("linePriceInPoCurrency", lineItem.get("linePriceInPoCurrency"));
		poLineItem.put("linePriceInSAR", lineItem.get("linePriceInSAR"));
		poLineItem.put("amountReceived", lineItem.get("amountReceived"));
		poLineItem.put("amountDue", lineItem.get("amountDue"));
		poLineItem.put("amountDueNew", lineItem.get("amountDueNew"));
		poLineItem.put("amountBilled", lineItem.get("amountBilled"));
		poLineItem.put("poLineDescription", lineItem.get("poLineDescription"));
		poLineItem.put("vendorSerialNumberYN", lineItem.get("vendorSerialNumberYN"));
		poLineItem.put("itemCategoryInventory", lineItem.get("itemCategoryInventory"));
		poLineItem.put("inventoryCategoryDescription", lineItem.get("inventoryCategoryDescription"));
		poLineItem.put("itemCategoryFA", lineItem.get("itemCategoryFA"));
		poLineItem.put("FACategoryDescription", lineItem.get("FACategoryDescription"));
		poLineItem.put("descopedLinePriceInPoCurrency", lineItem.get("descopedLinePriceInPoCurrency"));
		poLineItem.put("newLinePriceInPoCurrency", lineItem.get("newLinePriceInPoCurrency"));
		return poLineItem;
	}

	private void updateTotals(Map<String, Object> groupedRow, Map<String, Object> lineItem) {
		groupedRow.put("totalPoQtyNew", (Double) groupedRow.get("totalPoQtyNew") + getDoubleValue(lineItem, "poQtyNew"));
		groupedRow.put("totalQuantityReceived", (Double) groupedRow.get("totalQuantityReceived") + getDoubleValue(lineItem, "quantityReceived"));
		groupedRow.put("totalQuantityDueOld", (Double) groupedRow.get("totalQuantityDueOld") + getDoubleValue(lineItem, "quantityDueOld"));
		groupedRow.put("totalQuantityDueNew", (Double) groupedRow.get("totalQuantityDueNew") + getDoubleValue(lineItem, "quantityDueNew"));
		groupedRow.put("totalQuantityBilled", (Double) groupedRow.get("totalQuantityBilled") + getDoubleValue(lineItem, "quantityBilled"));
		groupedRow.put("totalpoOrderQuantity", (Double) groupedRow.get("totalpoOrderQuantity") + getDoubleValue(lineItem, "poOrderQuantity"));
		groupedRow.put("totalunitPriceInPoCurrency", (Double) groupedRow.get("totalunitPriceInPoCurrency") + getDoubleValue(lineItem, "unitPriceInPoCurrency"));
		groupedRow.put("totalunitPriceInSAR", (Double) groupedRow.get("totalunitPriceInSAR") + getDoubleValue(lineItem, "unitPriceInSAR"));
		groupedRow.put("totallinePriceInPoCurrency", (Double) groupedRow.get("totallinePriceInPoCurrency") + getDoubleValue(lineItem, "linePriceInPoCurrency"));
		groupedRow.put("totallinePriceInSAR", (Double) groupedRow.get("totallinePriceInSAR") + getDoubleValue(lineItem, "linePriceInSAR"));
		groupedRow.put("totalamountReceived", (Double) groupedRow.get("totalamountReceived") + getDoubleValue(lineItem, "amountReceived"));
		groupedRow.put("totalamountDue", (Double) groupedRow.get("totalamountDue") + getDoubleValue(lineItem, "amountDue"));
		groupedRow.put("totalamountDueNew", (Double) groupedRow.get("totalamountDueNew") + getDoubleValue(lineItem, "amountDueNew"));
		groupedRow.put("totalamountBilled", (Double) groupedRow.get("totalamountBilled") + getDoubleValue(lineItem, "amountBilled"));
		groupedRow.put("totalDescopedLinePriceInPoCurrency", (Double) groupedRow.get("totalDescopedLinePriceInPoCurrency") + getDoubleValue(lineItem, "descopedLinePriceInPoCurrency"));
		groupedRow.put("totalNewLinePriceInPoCurrency", (Double) groupedRow.get("totalNewLinePriceInPoCurrency") + getDoubleValue(lineItem, "newLinePriceInPoCurrency"));
	}

	private Double getDoubleValue(Map<String, Object> map, String key) {
		Object value = map.get(key);
		return value != null ? ((Number) value).doubleValue() : 0.0;
	}
}
