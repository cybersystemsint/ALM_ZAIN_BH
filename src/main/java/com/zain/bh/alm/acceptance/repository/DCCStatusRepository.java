package com.zain.bh.alm.acceptance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zain.bh.alm.acceptance.entity.DCCStatus;

public interface DCCStatusRepository extends JpaRepository<DCCStatus, Long> {

	DCCStatus findByRecordNo(long recordno);
}