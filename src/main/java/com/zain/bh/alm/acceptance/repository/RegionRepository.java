package com.zain.bh.alm.acceptance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zain.bh.alm.acceptance.entity.Region;

public interface RegionRepository extends JpaRepository<Region, Long> {

	Region findByRecordNo(long recordNo);

}
