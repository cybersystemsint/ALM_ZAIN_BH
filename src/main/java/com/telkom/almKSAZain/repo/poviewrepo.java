package com.telkom.almKSAZain.repo;

import com.telkom.almKSAZain.model.poview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface poviewrepo extends JpaRepository<poview,Long> {
    List<poview> findBySupplierId(String supplierId);
}
