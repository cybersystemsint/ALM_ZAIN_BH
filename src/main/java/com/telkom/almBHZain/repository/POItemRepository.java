package com.telkom.almBHZain.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.telkom.almBHZain.model.POItem;

public interface POItemRepository extends JpaRepository<POItem, Long>, JpaSpecificationExecutor<POItem> {

    List<POItem> findByPoNumberAndVendorNumber(String poNumber, String vendorNumber);

    List<POItem> findAllByPoNumber(String poNumber);

    List<POItem> findByPoNumber(String poNumber);

    POItem findByRecordNo(long recordNo);

    POItem findByPoNumberAndModelNumber(String poNumber, String modelNumber);

    // Using property navigation: poNumberEntity.poNumber
    List<POItem> findByPoNumberEntity_PoNumber(String poNumber);

    void deleteByPoNumber(String poNumber);

    // keep native query where you used LIMIT (MySQL). Native is fine.
    @Query(value = "SELECT * FROM tb_Po d WHERE d.poNumber = :poNumber ORDER BY d.recordNo DESC LIMIT 1", nativeQuery = true)
    POItem findTopByPoNumber(@Param("poNumber") String poNumber);

    @Query(value = "SELECT * FROM tb_Po d WHERE d.poNumber = :poNumber AND d.poLine = :poLine  ORDER BY d.recordNo DESC LIMIT 1", nativeQuery = true)
    POItem findTopByPoNumberAndPoLine(@Param("poNumber") String poNumber, @Param("poLine") Integer poLine);

    POItem findByPoNumberAndRecordNo(String poNumber, Long recordNo);

    // Use entity name and Java property in JPQL (not table name)
    @Query("SELECT d FROM POItem d WHERE d.poNumber = :poNumber AND d.approvalStatus = :approvalStatus")
    List<POItem> findByPoNumberAndApprovalStatus(@Param("poNumber") String poNumber, @Param("approvalStatus") String approvalStatus);

    boolean existsByModelNumber(String modelNumber);

    List<POItem> findByPoNumberIn(List<String> poNumbers);

    // Distinct poNumbers using entity name
    @Query("SELECT DISTINCT p.poNumber FROM POItem p")
    Page<String> findDistinctPoNumbers(Pageable pageable);

    POItem findByPoNumberAndPoLine(String poNumber, int poLine);

        @Query("SELECT COUNT(i) FROM POItem i WHERE i.poNumber = :poNumber AND (i.approvalStatus IS NULL OR LOWER(i.approvalStatus) <> 'approved')")
    long countNotApprovedItems(@Param("poNumber") String poNumber);
}