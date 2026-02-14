package com.zain.bh.alm.acceptance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zain.bh.alm.acceptance.entity.FileRecord;

public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {

	FileRecord findByRecordNo(long recordNo);

	List<FileRecord> findByPoNumberAndDccId(String poNumber, int dccId);
}