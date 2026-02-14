package com.zain.bh.alm.acceptance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zain.bh.alm.acceptance.entity.PoNumber;

public interface PoNumberRepository extends JpaRepository<PoNumber, Long> {

	PoNumber findByPoNumber(String PoNumber);
}