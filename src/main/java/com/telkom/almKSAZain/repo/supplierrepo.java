package com.telkom.almKSAZain.repo;

import com.telkom.almKSAZain.model.supplierdata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface supplierrepo extends JpaRepository<supplierdata,Long> {
    List<supplierdata> findBySupplierIdAndSupplierPhone(String supplierId, String supplierPhone);

    supplierdata findBySupplierId(String SupplierId);
    supplierdata findByRecordNo(long recordNo);
}
