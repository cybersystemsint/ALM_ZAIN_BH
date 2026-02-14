package com.zain.bh.alm.acceptance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zain.bh.alm.acceptance.entity.Node;

public interface NodeRepository extends JpaRepository<Node, Long> {

	List<Node> findByPartNumberAndSerialNumber(String partNumber, String serialNumber);
}