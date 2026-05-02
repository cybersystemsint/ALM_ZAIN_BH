package com.telkom.almBHZain.dto.response;

import java.util.List;
import java.util.Map;


public class WorkflowActionResponse {

    private String status;

    private String message;
    private List<Map<String, String>> results;

    public String getStatus()                          { return status; }
    public void   setStatus(String status)             { this.status = status; }

    public String getMessage()                         { return message; }
    public void   setMessage(String message)           { this.message = message; }

    public List<Map<String, String>> getResults()      { return results; }
    public void setResults(List<Map<String, String>> results) { this.results = results; }
}