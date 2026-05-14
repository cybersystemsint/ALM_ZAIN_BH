
package com.zain.jo.alm.pomanagement.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zain.jo.alm.pomanagement.entity.PurchaseOrder;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {    
      PurchaseOrder findByPoNumber(String PoNumber);
      
    @Modifying
    @Query("UPDATE PurchaseOrder p " +
           "SET p.approvalStatus = :newStatus, " +
           "    p.updatedBy = CASE WHEN :updatedBy IS NOT NULL THEN :updatedBy ELSE p.updatedBy END " +
           "WHERE p.poNumber = :poNumber " +
           "  AND (p.approvalStatus IS NULL OR LOWER(p.approvalStatus) NOT LIKE 'pending%')")
    int markPendingDeletionIfNotPending(@Param("poNumber") String poNumber,
                                       @Param("newStatus") String newStatus,
                                       @Param("updatedBy") String updatedBy);


    @Modifying
    @Query("UPDATE PurchaseOrder p " +
           "SET p.approvalStatus = :newStatus, " +
           "    p.updatedBy = CASE WHEN :updatedBy IS NOT NULL THEN :updatedBy ELSE p.updatedBy END " +
           "WHERE p.id = :id " +
           "  AND (p.approvalStatus IS NULL OR LOWER(p.approvalStatus) NOT LIKE 'pending%')")
    int markPendingDeletionIfNotPendingById(@Param("id") Long id,
                                            @Param("newStatus") String newStatus,
                                            @Param("updatedBy") String updatedBy);
     
}
