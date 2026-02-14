package com.zain.bh.alm.acceptance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zain.bh.alm.acceptance.entity.Po;

public interface PoRepository extends JpaRepository<Po, Long> {

	List<Po> findByPoNumberAndVendorNumber(String poId, String supplierId);

	List<Po> findByPoNumber(String PoNumber);

	Po findByRecordNo(long recordNo);

	@Query(value = "SELECT * FROM tb_Po d WHERE d.poNumber = :poNumber ORDER BY d.recordNo DESC LIMIT 1", nativeQuery = true)
	Po findTopByPoNumber(@Param("poNumber") String poNumber);

	@Query(value = "SELECT * FROM tb_Po d WHERE d.poNumber = :poNumber AND d.poLine = :poLine  ORDER BY d.recordNo DESC LIMIT 1", nativeQuery = true)
	Po findTopByPoNumberAndPoLine(@Param("poNumber") String poNumber, @Param("poLine") Integer poLine);
}