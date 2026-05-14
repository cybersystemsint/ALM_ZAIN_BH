package com.zain.jo.alm.pomanagement.dto.response;

import java.util.ArrayList;
import java.util.List;

import com.zain.jo.alm.pomanagement.entity.PoItem;
import com.zain.jo.alm.pomanagement.entity.PoItemModification;

public class BulkPoItemResult {
    private List<PoItem> addedRows = new ArrayList<>();
    private List<PoItemModification> modifiedRows = new ArrayList<>();
    private int addedCount;
    private int modifiedCount;
    private int failedAdditionCount;
    private int failedModificationCount;
    private List<String> additionErrors = new ArrayList<>();
    private List<String> modificationErrors = new ArrayList<>();
    
    public List<PoItem> getAddedRows() {
        return addedRows;
    }
    public void setAddedRows(List<PoItem> addedRows) {
        this.addedRows = addedRows;
    }
    public List<PoItemModification> getModifiedRows() {
        return modifiedRows;
    }
    public void setModifiedRows(List<PoItemModification> modifiedRows) {
        this.modifiedRows = modifiedRows;
    }
    public int getAddedCount() {
        return addedCount;
    }
    public void setAddedCount(int addedCount) {
        this.addedCount = addedCount;
    }
    public int getModifiedCount() {
        return modifiedCount;
    }
    public void setModifiedCount(int modifiedCount) {
        this.modifiedCount = modifiedCount;
    }
    public int getFailedAdditionCount() {
        return failedAdditionCount;
    }
    public void setFailedAdditionCount(int failedAdditionCount) {
        this.failedAdditionCount = failedAdditionCount;
    }
    public int getFailedModificationCount() {
        return failedModificationCount;
    }
    public void setFailedModificationCount(int failedModificationCount) {
        this.failedModificationCount = failedModificationCount;
    }
    public List<String> getAdditionErrors() {
        return additionErrors;
    }
    public void setAdditionErrors(List<String> additionErrors) {
        this.additionErrors = additionErrors;
    }
    public List<String> getModificationErrors() {
        return modificationErrors;
    }
    public void setModificationErrors(List<String> modificationErrors) {
        this.modificationErrors = modificationErrors;
    }
    

    
}
