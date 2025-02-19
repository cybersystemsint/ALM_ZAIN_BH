package com.telkom.almBHZain.repo;

import com.telkom.almBHZain.model.DccPoCombinedView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DccCombinedViewrepo extends JpaRepository<DccPoCombinedView,Long> {
    List<DccPoCombinedView> findBySupplierId(String supplierID);
    List<DccPoCombinedView> findBySupplierIdAndDccStatus(String supplierID,String dccstatus);
}
