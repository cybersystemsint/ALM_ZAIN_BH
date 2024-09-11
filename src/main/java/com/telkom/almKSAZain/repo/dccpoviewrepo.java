package com.telkom.almKSAZain.repo;

import com.telkom.almKSAZain.model.dccpoview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface dccpoviewrepo extends JpaRepository<dccpoview,Long> {

    List<dccpoview> findBySupplierId(String supplierId);
    List<dccpoview> findAll();
}
