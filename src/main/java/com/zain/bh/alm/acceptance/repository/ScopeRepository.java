package com.zain.bh.alm.acceptance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zain.bh.alm.acceptance.entity.Scope;

public interface ScopeRepository extends JpaRepository<Scope, Long> {

	Scope findByScope(String scope);

}
