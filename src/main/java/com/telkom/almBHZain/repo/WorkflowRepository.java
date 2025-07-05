package com.telkom.almBHZain.repo;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.telkom.almBHZain.model.Workflow;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long>, WorkflowRepositoryCustom {
  
    List<Workflow> findByPoNumber(String poNumber);
    Workflow findByPoNumberAndProcessId(String poNumber, String processId);
    public Workflow findTopByPoNumberOrderByInsertDateDesc(String poNumber);
    public Workflow findByProcessId(long recordNo);
    List<Workflow> findByUpdatedStatusIsNull();

    public List<Workflow> findByUpdatedStatus(String updatedStatus);

    public List<Workflow> findByPoNumberAndOriginalStatus(String poNumber, String pending_Deletion);

    public Workflow findByPoNumberAndRecordNoAndOriginalStatus(String poNumber, Long recordNo, String originalStatus);

    List<Workflow> findByUpdatedStatusIsNotNull();

    
//      // Offset search
    @Query("SELECT w FROM Workflow w WHERE " +
           "(:updatedStatusIsNull = true AND w.updatedStatus IS NULL OR :updatedStatusIsNull = false AND w.updatedStatus IS NOT NULL) AND (" +
           ":searchTerm IS NULL OR " +
           "LOWER(w.poNumber) LIKE %:searchTerm% OR " +
           "LOWER(w.oldPoNumber) LIKE %:searchTerm% OR " +
           "LOWER(w.newPoNumber) LIKE %:searchTerm% OR " +
           "LOWER(w.originalStatus) LIKE %:searchTerm% OR " +
           "LOWER(w.updatedStatus) LIKE %:searchTerm% OR " +
           "LOWER(w.processId) LIKE %:searchTerm% OR " +
           "LOWER(w.insertedBy) LIKE %:searchTerm% OR " +
           "LOWER(w.changedBy) LIKE %:searchTerm% OR " +
           "LOWER(w.comments) LIKE %:searchTerm% " +
           ")")
    Page<Workflow> searchAllColumns(
            @Param("updatedStatusIsNull") boolean updatedStatusIsNull,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    // Keyset search
    @Query("SELECT w FROM Workflow w WHERE " +
           "(:updatedStatusIsNull = true AND w.updatedStatus IS NULL OR :updatedStatusIsNull = false AND w.updatedStatus IS NOT NULL) AND " +
           "w.id < :afterId AND (" +
           ":searchTerm IS NULL OR " +
           "LOWER(w.poNumber) LIKE %:searchTerm% OR " +
           "LOWER(w.oldPoNumber) LIKE %:searchTerm% OR " +
           "LOWER(w.newPoNumber) LIKE %:searchTerm% OR " +
           "LOWER(w.originalStatus) LIKE %:searchTerm% OR " +
           "LOWER(w.updatedStatus) LIKE %:searchTerm% OR " +
           "LOWER(w.processId) LIKE %:searchTerm% OR " +
           "LOWER(w.insertedBy) LIKE %:searchTerm% OR " +
           "LOWER(w.changedBy) LIKE %:searchTerm% OR " +
           "LOWER(w.comments) LIKE %:searchTerm% " +
           ")")
    Page<Workflow> findAfterId(
            @Param("updatedStatusIsNull") boolean updatedStatusIsNull,
            @Param("searchTerm") String searchTerm,
            @Param("afterId") Long afterId,
            Pageable pageable);


    @Query("SELECT w FROM Workflow w WHERE (:updatedStatusIsNull = TRUE AND w.updatedStatus IS NULL) OR (:updatedStatusIsNull = FALSE AND w.updatedStatus IS NOT NULL)")
    Page<Workflow> findAllByUpdatedStatusIsNullOrNot(@Param("updatedStatusIsNull") boolean updatedStatusIsNull, Pageable pageable);


    // Keyset pagination
    @Query("SELECT w FROM Workflow w WHERE ((:updatedStatusIsNull = TRUE AND w.updatedStatus IS NULL) OR (:updatedStatusIsNull = FALSE AND w.updatedStatus IS NOT NULL)) AND w.id < :afterId")
    Page<Workflow> findAfterId(@Param("updatedStatusIsNull") boolean updatedStatusIsNull,
                               @Param("afterId") Long afterId,
                               Pageable pageable);


}
