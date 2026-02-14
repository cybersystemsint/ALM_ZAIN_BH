package com.zain.bh.alm.acceptance.repository;

import com.zain.bh.alm.acceptance.entity.PurchaseOrderView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderViewRepository extends JpaRepository<PurchaseOrderView, Long> {
	List<PurchaseOrderView> findBySupplierId(String supplierId);
}
