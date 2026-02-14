package com.zain.bh.alm.acceptance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zain.bh.alm.acceptance.entity.Site;

public interface SiteRepository extends JpaRepository<Site, Long> {

	Site findByrecordNo(long recordNo);

	Site findFirstBySiteId(String siteId);

}
