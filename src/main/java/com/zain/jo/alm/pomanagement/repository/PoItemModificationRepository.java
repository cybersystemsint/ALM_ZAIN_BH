package com.zain.jo.alm.pomanagement.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zain.jo.alm.pomanagement.entity.PoItemModification;

@Repository
public interface PoItemModificationRepository extends JpaRepository<PoItemModification, Long> {
    PoItemModification findByPoNumber(String poNumber);
    PoItemModification findByRecordNo(long recordNo);
    PoItemModification findByPoNumberAndRecordNo(String poNumber, Long recordNo);

}
