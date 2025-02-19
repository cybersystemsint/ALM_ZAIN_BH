package com.telkom.almBHZain.repo;

import com.telkom.almBHZain.model.poview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface poviewrepo extends JpaRepository<poview,Long> {
    List<poview> findBySupplierId(String supplierId);
}
