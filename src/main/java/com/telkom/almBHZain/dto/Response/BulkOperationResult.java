package com.telkom.almBHZain.dto.response;

import com.telkom.almBHZain.model.POItem;
import com.telkom.almBHZain.model.tb_Po_Modification;

import java.util.ArrayList;
import java.util.List;


public class BulkOperationResult {

    private List<POItem>             addedRows           = new ArrayList<>();
    private List<tb_Po_Modification> modifiedRows        = new ArrayList<>();
    private int                      addedCount;
    private int                      modifiedCount;
    private int                      failedAdditionCount;
    private int                      failedModificationCount;
    private List<String>             additionErrors      = new ArrayList<>();
    private List<String>             modificationErrors  = new ArrayList<>();

    public List<POItem>             getAddedRows()                          { return addedRows; }
    public void                     setAddedRows(List<POItem> r)            { this.addedRows = r; }

    public List<tb_Po_Modification> getModifiedRows()                       { return modifiedRows; }
    public void                     setModifiedRows(List<tb_Po_Modification> r) { this.modifiedRows = r; }

    public int  getAddedCount()                                             { return addedCount; }
    public void setAddedCount(int n)                                        { this.addedCount = n; }

    public int  getModifiedCount()                                          { return modifiedCount; }
    public void setModifiedCount(int n)                                     { this.modifiedCount = n; }

    public int  getFailedAdditionCount()                                    { return failedAdditionCount; }
    public void setFailedAdditionCount(int n)                               { this.failedAdditionCount = n; }

    public int  getFailedModificationCount()                                { return failedModificationCount; }
    public void setFailedModificationCount(int n)                           { this.failedModificationCount = n; }

    public List<String> getAdditionErrors()                                 { return additionErrors; }
    public void         setAdditionErrors(List<String> e)                   { this.additionErrors = e; }

    public List<String> getModificationErrors()                             { return modificationErrors; }
    public void         setModificationErrors(List<String> e)               { this.modificationErrors = e; }
}