package com.telkom.almKSAZain.repo;

import com.telkom.almKSAZain.model.DCCLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DccLineRepo extends JpaRepository<DCCLineItem,Long> {
    DCCLineItem findByRecordNo(long recordNo);
    

    
}
