package com.zain.bh.alm.acceptance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zain.bh.alm.acceptance.entity.DccPoCombinedView;

public interface DccPoCombinedViewRepository extends JpaRepository<DccPoCombinedView, Long> {

	List<DccPoCombinedView> findBySupplierId(String supplierId);

	List<DccPoCombinedView> findAll();
}