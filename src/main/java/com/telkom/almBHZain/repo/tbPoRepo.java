/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.telkom.almBHZain.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.telkom.almBHZain.model.tb_Po;

/**
 *
 * @author jgithu
 */
public interface tbPoRepo extends JpaRepository<tb_Po, Long> {

    List<tb_Po> findByPoNumberAndVendorNumber(String poId, String supplierId);

    List<tb_Po> findAllByPoNumber(String PoNumber);
    List<tb_Po> findByPoNumber(String poNumber); 
    // tb_Po findByPoNumber(String poNumber); 
    tb_Po findByRecordNo(long recordNo);
    // New method using poNumberEntity relationship
    List<tb_Po> findByPoNumberEntity_PoNumber(String poNumber);
    void deleteByPoNumber(String poNumber); // Method to delete PO items by poNumber
    @Query(value = "SELECT * FROM tb_Po d WHERE d.poNumber = :poNumber ORDER BY d.recordNo DESC LIMIT 1", nativeQuery = true)
    tb_Po findTopByPoNumber(@Param("poNumber") String poNumber);

    @Query(value = "SELECT * FROM tb_Po d WHERE d.poNumber = :poNumber AND d.poLine = :poLine  ORDER BY d.recordNo DESC LIMIT 1", nativeQuery = true)
    tb_Po findTopByPoNumberAndPoLine(@Param("poNumber") String poNumber, @Param("poLine") Integer poLine);

    public tb_Po findByPoNumberAndRecordNo(String poNumber, Long recordNo);

      @Query("SELECT d FROM tb_Po d WHERE d.poNumber = :poNumber AND d.Approval_Status = :Approval_Status")
    tb_Po findByPoNumberAndApprovalStatus(@Param("poNumber") String poNumber, @Param("Approval_Status") String Approval_Status);
    
    boolean existsByModelNumber(String modelNumber);

// In PoRepository
tb_Po findByPoNumberAndPoLine(String poNumber, int poLine);

     // Offset search: all string columns + supplierId filter
    @Query("SELECT p FROM tb_Po p " +
           "WHERE (:supplierId IS NULL OR p.vendorNumber = :supplierId) AND (" +
           ":searchTerm IS NULL OR " +
           "LOWER(p.poNumber) LIKE %:searchTerm% OR " +
           "LOWER(p.modelNumber) LIKE %:searchTerm% OR " +
           "LOWER(p.uom) LIKE %:searchTerm% OR " +
           "LOWER(p.faCategoryNew) LIKE %:searchTerm% OR " +
           "LOWER(p.L1) LIKE %:searchTerm% OR " +
           "LOWER(p.L2) LIKE %:searchTerm% OR " +
           "LOWER(p.L3) LIKE %:searchTerm% OR " +
           "LOWER(p.L4) LIKE %:searchTerm% OR " +
           "LOWER(p.oldFaCategory) LIKE %:searchTerm% OR " +
           "LOWER(p.accumulatedDepreciationCode) LIKE %:searchTerm% OR " +
           "LOWER(p.depreciationCode) LIKE %:searchTerm% OR " +
           "LOWER(p.vendorName) LIKE %:searchTerm% OR " +
           "LOWER(p.vendorNumber) LIKE %:searchTerm% OR " +
           "LOWER(p.projectNumber) LIKE %:searchTerm% OR " +
           "LOWER(p.currency) LIKE %:searchTerm% OR " +
           "LOWER(p.Level1Description) LIKE %:searchTerm% OR " +
           "LOWER(p.partNumber) LIKE %:searchTerm% OR " +
           "LOWER(p.l3Description) LIKE %:searchTerm% OR " +
           "LOWER(p.costCenter) LIKE %:searchTerm% OR " +
           "LOWER(p.Approval_Status) LIKE %:searchTerm% OR " +
           "LOWER(p.createdBy) LIKE %:searchTerm% OR " +
           "LOWER(p.updatedBy) LIKE %:searchTerm%" +
           ")")
    Page<tb_Po> searchAllColumns(
            @Param("supplierId") String supplierId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    // Keyset search: all columns + supplierId filter + afterId
    @Query("SELECT p FROM tb_Po p " +
           "WHERE (:supplierId IS NULL OR p.vendorNumber = :supplierId) AND " +
           "p.recordNo < :afterId AND (" +
           ":searchTerm IS NULL OR " +
           "LOWER(p.poNumber) LIKE %:searchTerm% OR " +
           "LOWER(p.modelNumber) LIKE %:searchTerm% OR " +
           "LOWER(p.uom) LIKE %:searchTerm% OR " +
           "LOWER(p.faCategoryNew) LIKE %:searchTerm% OR " +
           "LOWER(p.L1) LIKE %:searchTerm% OR " +
           "LOWER(p.L2) LIKE %:searchTerm% OR " +
           "LOWER(p.L3) LIKE %:searchTerm% OR " +
           "LOWER(p.L4) LIKE %:searchTerm% OR " +
           "LOWER(p.oldFaCategory) LIKE %:searchTerm% OR " +
           "LOWER(p.accumulatedDepreciationCode) LIKE %:searchTerm% OR " +
           "LOWER(p.depreciationCode) LIKE %:searchTerm% OR " +
           "LOWER(p.vendorName) LIKE %:searchTerm% OR " +
           "LOWER(p.vendorNumber) LIKE %:searchTerm% OR " +
           "LOWER(p.projectNumber) LIKE %:searchTerm% OR " +
           "LOWER(p.currency) LIKE %:searchTerm% OR " +
           "LOWER(p.Level1Description) LIKE %:searchTerm% OR " +
           "LOWER(p.partNumber) LIKE %:searchTerm% OR " +
           "LOWER(p.l3Description) LIKE %:searchTerm% OR " +
           "LOWER(p.costCenter) LIKE %:searchTerm% OR " +
           "LOWER(p.Approval_Status) LIKE %:searchTerm% OR " +
           "LOWER(p.createdBy) LIKE %:searchTerm% OR " +
           "LOWER(p.updatedBy) LIKE %:searchTerm%" +
           ")")
    Page<tb_Po> findAfterId(
            @Param("supplierId") String supplierId,
            @Param("searchTerm") String searchTerm,
            @Param("afterId") Long afterId,
            Pageable pageable);
}
