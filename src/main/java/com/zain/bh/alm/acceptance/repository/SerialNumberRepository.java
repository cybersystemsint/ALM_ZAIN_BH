package com.zain.bh.alm.acceptance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zain.bh.alm.acceptance.entity.SerialNumber;

public interface SerialNumberRepository extends JpaRepository<SerialNumber, Long> {

	List<SerialNumber> findBySerialNumber(String serialNumber);

	List<SerialNumber> findBySerialNumberAndItemCode(String serialNumber, String itemCode);

}
