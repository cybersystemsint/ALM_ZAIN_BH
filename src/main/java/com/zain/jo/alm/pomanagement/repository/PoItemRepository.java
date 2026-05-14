package com.zain.jo.alm.pomanagement.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zain.jo.alm.pomanagement.entity.PoItem;

public interface PoItemRepository extends JpaRepository<PoItem, Long>, JpaSpecificationExecutor<PoItem> {

    List<PoItem> findByPoNumberAndVendorNumber(String poNumber, String vendorNumber);

    List<PoItem> findAllByPoNumber(String poNumber);

    List<PoItem> findByPoNumber(String poNumber);

    PoItem findByRecordNo(long recordNo);
   
    PoItem findByPoNumberAndModelNumber(String poNumber, String modelNumber);

    List<PoItem> findByPoNumberEntity_PoNumber(String poNumber);

    void deleteByPoNumber(String poNumber);

        @Query(value = "SELECT * FROM (SELECT * FROM tb_Po d WHERE d.poNumber = :poNumber ORDER BY d.recordNo DESC) WHERE ROWNUM = 1", nativeQuery = true)
    PoItem findTopByPoNumber(@Param("poNumber") String poNumber);

    @Query(value = "SELECT * FROM (SELECT * FROM tb_Po d WHERE d.poNumber = :poNumber AND d.poLine = :poLine ORDER BY d.recordNo DESC) WHERE ROWNUM = 1", nativeQuery = true)
    PoItem findTopByPoNumberAndPoLine(@Param("poNumber") String poNumber, @Param("poLine") Integer poLine);

    PoItem findByPoNumberAndRecordNo(String poNumber, Long recordNo);

    @Query("SELECT d FROM PoItem d WHERE d.poNumber = :poNumber AND d.approvalStatus = :approvalStatus")
    List<PoItem> findByPoNumberAndApprovalStatus(@Param("poNumber") String poNumber, @Param("approvalStatus") String approvalStatus);

    boolean existsByModelNumber(String modelNumber);

    List<PoItem> findByPoNumberIn(List<String> poNumbers);

    @Query("SELECT DISTINCT p.poNumber FROM PoItem p")
    Page<String> findDistinctPoNumbers(Pageable pageable);

    PoItem findByPoNumberAndPoLine(String poNumber, int poLine);

        @Query("SELECT COUNT(i) FROM PoItem i WHERE i.poNumber = :poNumber AND (i.approvalStatus IS NULL OR LOWER(i.approvalStatus) <> 'approved')")
    long countNotApprovedItems(@Param("poNumber") String poNumber);
}