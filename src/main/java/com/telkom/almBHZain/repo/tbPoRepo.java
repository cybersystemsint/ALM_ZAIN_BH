/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.telkom.almBHZain.repo;

import com.telkom.almBHZain.model.tb_Po;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author jgithu
 */
public interface tbPoRepo extends JpaRepository<tb_Po, Long> {

    List<tb_Po> findByPoNumberAndVendorNumber(String poId, String supplierId);

    List<tb_Po> findByPoNumber(String PoNumber);

    tb_Po findByRecordNo(long recordNo);

    @Query(value = "SELECT * FROM tb_Po d WHERE d.poNumber = :poNumber ORDER BY d.recordNo DESC LIMIT 1", nativeQuery = true)
    tb_Po findTopByPoNumber(@Param("poNumber") String poNumber);

    @Query(value = "SELECT * FROM tb_Po d WHERE d.poNumber = :poNumber AND d.poLine = :poLine  ORDER BY d.recordNo DESC LIMIT 1", nativeQuery = true)
    tb_Po findTopByPoNumberAndPoLine(@Param("poNumber") String poNumber, @Param("poLine") Integer poLine);

//    @Query(value = "SELECT * FROM tb_PurchaseOrder d WHERE d.poNumber = :poNumber AND d.lineNumber = :lineNumber AND d.releaseNum = :releaseNum  ORDER BY d.recordNo DESC LIMIT 1", nativeQuery = true)
//    tbPurchaseOrder findTopByPoNumberAndLineNumberAndReleaseNum(@Param("poNumber") String poNumber, @Param("lineNumber") String lineNumber, @Param("releaseNum") String releaseNum);
}
