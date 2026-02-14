package com.zain.bh.alm.acceptance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zain.bh.alm.acceptance.entity.CategoryApprovalLevels;

public interface CategoryApprovalLevelRepository extends JpaRepository<CategoryApprovalLevels, Long> {

	List<CategoryApprovalLevels> findByItemCategoryCode(String itemCategoryCode);
}