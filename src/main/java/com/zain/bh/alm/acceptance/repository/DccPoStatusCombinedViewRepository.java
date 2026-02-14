package com.zain.bh.alm.acceptance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zain.bh.alm.acceptance.entity.DccPoStatusCombinedView;

public interface DccPoStatusCombinedViewRepository extends JpaRepository<DccPoStatusCombinedView, Long> {

	List<DccPoStatusCombinedView> findBySupplierId(String supplierID);

	List<DccPoStatusCombinedView> findBySupplierIdAndDccStatus(String supplierID, String dccstatus);
}