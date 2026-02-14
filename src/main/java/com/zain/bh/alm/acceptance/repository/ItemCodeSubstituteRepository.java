package com.zain.bh.alm.acceptance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zain.bh.alm.acceptance.entity.ItemCodeSubstitute;

public interface ItemCodeSubstituteRepository extends JpaRepository<ItemCodeSubstitute, Long> {

	ItemCodeSubstitute findByRecordNo(long recordNo);

	List<ItemCodeSubstitute> findByItemCodeAndRelatedItemCode(String itemCode, String relatedItemCode);

	List<ItemCodeSubstitute> findByRelatedItemCode(String relatedItemCode);
}