package com.zain.jo.alm.pomanagement.service;

import com.zain.jo.alm.pomanagement.dto.WorkflowDto;
import com.zain.jo.alm.pomanagement.dto.request.SearchRequest;
import com.zain.jo.alm.pomanagement.dto.response.PageResult;


public interface WorkflowService {


    PageResult<WorkflowDto> getWorkflows(boolean pendingOnly, SearchRequest request);
}