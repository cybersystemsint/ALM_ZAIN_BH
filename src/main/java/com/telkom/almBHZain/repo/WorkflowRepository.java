package com.telkom.almBHZain.repo;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.telkom.almBHZain.model.Workflow;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {

    List<Workflow> findByPoNumber(String poNumber);
    Workflow findByPoNumberAndProcessId(String poNumber, String processId);
    public Workflow findTopByPoNumberOrderByInsertDateDesc(String poNumber);
    public Workflow findByProcessId(long recordNo);
    List<Workflow> findByUpdatedStatusIsNull();

    public List<Workflow> findByUpdatedStatus(String updatedStatus);

    public List<Workflow> findByPoNumberAndOriginalStatus(String poNumber, String pending_Deletion);

    public Workflow findByPoNumberAndRecordNoAndOriginalStatus(String poNumber, Long recordNo, String originalStatus);

    List<Workflow> findByUpdatedStatusIsNotNull();
}
