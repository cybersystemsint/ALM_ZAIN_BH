package com.telkom.almKSAZain.repo;

import com.telkom.almKSAZain.model.pohddata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface pohdrepo extends JpaRepository<pohddata, Long> {

    List<pohddata> findByPoIdAndSupplierId(String poId, String supplierId);

    pohddata findByPoId(String PoId);

    pohddata findByRecordNo(long recordNo);

}
