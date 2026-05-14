package com.zain.jo.alm.pomanagement.repository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zain.jo.alm.pomanagement.entity.Workflow;

@Repository

public interface WorkflowRepository extends JpaRepository<Workflow, Long>, JpaSpecificationExecutor<Workflow> {
  
    List<Workflow> findByPoNumber(String poNumber);
    Workflow findByPoNumberAndProcessId(String poNumber, String processId);
    public Workflow findTopByPoNumberOrderByInsertDateDesc(String poNumber);
    public Workflow findByProcessId(long recordNo);
    List<Workflow> findByUpdatedStatusIsNull();

    public List<Workflow> findByUpdatedStatus(String updatedStatus);

    public List<Workflow> findByPoNumberAndOriginalStatus(String poNumber, String pending_Deletion);

    public Workflow findByPoNumberAndRecordNoAndOriginalStatus(String poNumber, Long recordNo, String originalStatus);

    List<Workflow> findByUpdatedStatusIsNotNull();

   

    @Query("SELECT w FROM Workflow w WHERE (:updatedStatusIsNull = TRUE AND w.updatedStatus IS NULL) OR (:updatedStatusIsNull = FALSE AND w.updatedStatus IS NOT NULL)")
    Page<Workflow> findAllByUpdatedStatusIsNullOrNot(@Param("updatedStatusIsNull") boolean updatedStatusIsNull, Pageable pageable);


    // Keyset pagination
    @Query("SELECT w FROM Workflow w WHERE ((:updatedStatusIsNull = TRUE AND w.updatedStatus IS NULL) OR (:updatedStatusIsNull = FALSE AND w.updatedStatus IS NOT NULL)) AND w.id < :afterId")
    Page<Workflow> findAfterId(@Param("updatedStatusIsNull") boolean updatedStatusIsNull,
                               @Param("afterId") Long afterId,
                               Pageable pageable);


}
