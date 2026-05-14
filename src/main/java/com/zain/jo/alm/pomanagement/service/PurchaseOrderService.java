package com.zain.jo.alm.pomanagement.service;

import com.zain.jo.alm.pomanagement.dto.PurchaseOrderDto;
import com.zain.jo.alm.pomanagement.dto.request.BulkDeleteRequest;
import com.zain.jo.alm.pomanagement.dto.request.SearchRequest;
import com.zain.jo.alm.pomanagement.dto.response.PageResult;
import com.zain.jo.alm.pomanagement.dto.response.WorkflowActionResponse;

import java.util.Map;

/**
 * Contract for all Purchase Order business operations.
 */
public interface PurchaseOrderService {


    PageResult<PurchaseOrderDto> getPurchaseOrders(SearchRequest request);

    Map<String, String> createPONumbers(String rawJsonArray);

    Map<String, String> updatePONumber(String poNumber, Map<String, Object> payload);

    Map<String, String> requestSingleDelete(String poNumber, Map<String, Object> payload);

    Map<String, Object> processBulkDelete(BulkDeleteRequest request);


    WorkflowActionResponse processWorkflowAction(Map<String, Object> requestBody, String action);
}