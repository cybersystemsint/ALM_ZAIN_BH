package com.zain.bh.alm.acceptance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zain.bh.alm.acceptance.entity.ArcApprovalRecords;

public interface ArcApprovalRecordsRepository extends JpaRepository<ArcApprovalRecords, Long> {

	ArcApprovalRecords findByRecordId(String approvalRecordId);

	ArcApprovalRecords findByRecordNo(long recordNo);

	@Query(value = "SELECT * FROM tb_Arc_ApprovalRecords d WHERE d.recordId = :recordId ORDER BY d.recordNo DESC LIMIT 1", nativeQuery = true)
	ArcApprovalRecords findTopByRecordId(@Param("recordId") Integer recordId);
}