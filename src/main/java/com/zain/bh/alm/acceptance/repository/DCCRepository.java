package com.zain.bh.alm.acceptance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zain.bh.alm.acceptance.entity.DCC;

public interface DCCRepository extends JpaRepository<DCC, Long> {

	DCC findByRecordNo(long recordNo);

	@Query(value = "SELECT * FROM tb_DCC d WHERE d.poNumber = :poNumber ORDER BY d.recordNo DESC LIMIT 1", nativeQuery = true)
	DCC findTopByPoNumber(@Param("poNumber") String poNumber);
}