package com.telkom.almBHZain.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.telkom.almBHZain.model.Workflow;
import com.telkom.almBHZain.repo.WorkflowRepository;
import com.telkom.almBHZain.service.TbPoNumberService;
import com.telkom.almBHZain.service.TbPoService;
import com.telkom.almBHZain.service.WorkflowService;

@RestController
public class WorkflowController {

    private final Logger logger = LogManager.getLogger(WorkflowController.class);

    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
     WorkflowRepository workflowRepository;

    @Autowired
    private TbPoNumberService poNumberService;

    @Autowired
    private TbPoService tbPoService;

     @Autowired
    private WorkflowService workflowService;


     // get all approval requests
     @GetMapping("/workflow")
     public ResponseEntity<List<Workflow>> getAllWorkflows() {
         return ResponseEntity.ok(workflowRepository.findAll());
     }


    // Pending workflows (updatedStatus IS NULL)
    @GetMapping("/pendingWorkflows")
    public Map<String, Object> getPendingWorkflows(
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Long afterId
    ) {
        return workflowService.getWorkflowsHybrid(true, page, size, searchTerm, afterId);
    }

    // Processed workflows (updatedStatus IS NOT NULL)
    @GetMapping("/POUpdatedWorkflow")
    public Map<String, Object> getProcessedWorkflows(
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Long afterId
    ) {
        return workflowService.getWorkflowsHybrid(false, page, size, searchTerm, afterId);
    }

      //get all ponumbers
         @GetMapping(value = "/poNumbers", produces = "application/json")
         public Map<String, Object> getPONumbers(
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Long afterId // for keyset
    ) {
        logger.info("Fetching PONumbers | page={}, size={}, afterId={}, searchTerm={}", page, size, afterId, searchTerm);
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = poNumberService.getPoNumbersHybrid(page, size, searchTerm, afterId);
            response.put("status", "Success");
            response.put("message", "PONumbers fetched successfully");
            response.putAll(data);
        } catch (Exception ex) {
            logger.error("Exception: ", ex);
            response.put("status", "Error");
            response.put("message", "Failed to fetch PONumbers: " + ex.getMessage());
        }
        return response;
    }
         

       //get all poitems  
        @GetMapping(value = "/poItems", produces = "application/json")
        public Map<String, Object> getPurchaseOrders(
            @RequestParam(required = false) String supplierId,
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Long afterId // for keyset
    ) {
        logger.info("Fetching poItems | page={}, size={}, afterId={}, searchTerm={}, supplierId={}", page, size, afterId, searchTerm, supplierId);
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = tbPoService.getPoItemsHybrid(page, size, searchTerm, afterId, supplierId);
            response.put("status", "Success");
            response.put("message", "PO Items fetched successfully");
            response.putAll(data);
        } catch (Exception ex) {
            logger.error("Exception: ", ex);
            response.put("status", "Error");
            response.put("message", "Failed to fetch PO Items: " + ex.getMessage());
        }
        return response;
    }
        
       //get poNumbers and their poItems
         @GetMapping(value = "/poItem", produces = "application/json")
         @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
         public Map<String, Object> getPurchaseOrder(
                 @RequestParam(name = "supplierId", defaultValue = "0") String supplierId,
                 @RequestParam(name = "page", defaultValue = "1") int page,
                 @RequestParam(name = "size", defaultValue = "20000") int size) {
         
             page = Math.max(page, 1); // Ensure page is at least 1
             size = Math.max(size, 1); // Ensure size is at least 1
         
             // Base SQL for fetching all records
             String sql = "SELECT * FROM tb_Po PO";
             if (!supplierId.equalsIgnoreCase("0")) {
                 sql += " WHERE PO.vendorNumber='" + supplierId + "'";
             }
             sql += " ORDER BY PO.poNumber";
         
             // Execute the query to fetch all records
             List<Map<String, Object>> allRecords = jdbcTemplate.queryForList(sql);
         
             // Group PO items by poNumber
             Map<String, List<Map<String, Object>>> groupedResult = new HashMap<>();
             for (Map<String, Object> row : allRecords) {
                 String poNumber = (String) row.get("poNumber");
                 groupedResult.computeIfAbsent(poNumber, k -> new ArrayList<>()).add(row);
             }
         
             // Convert groupedResult to a list of entries for pagination
             List<Map.Entry<String, List<Map<String, Object>>>> groupedList = new ArrayList<>(groupedResult.entrySet());
         
             // Calculate pagination details
             int totalRecords = groupedList.size();
             int totalPages = (int) Math.ceil((double) totalRecords / size);
             int fromIndex = (page - 1) * size;
             int toIndex = Math.min(fromIndex + size, totalRecords);
         
             // Apply pagination to the grouped list
             List<Map.Entry<String, List<Map<String, Object>>>> paginatedList = groupedList.subList(fromIndex, toIndex);
         
             // Convert paginated list back to a map
             Map<String, List<Map<String, Object>>> paginatedResult = new HashMap<>();
             for (Map.Entry<String, List<Map<String, Object>>> entry : paginatedList) {
                 paginatedResult.put(entry.getKey(), entry.getValue());
             }
         
             // Prepare the response
             Map<String, Object> response = new HashMap<>();
             response.put("data", paginatedResult);
             response.put("totalRecords", totalRecords);
             response.put("currentPage", page);
             response.put("pageSize", size);
             response.put("totalPages", totalPages);
         
             return response;
         }
           
          }
         