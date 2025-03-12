package com.telkom.almBHZain.repo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.telkom.almBHZain.model.tb_Po_Modification;

@Repository
public interface tb_Po_ModificationRepository extends JpaRepository<tb_Po_Modification, Long> {
    tb_Po_Modification findByPoNumber(String poNumber);
    tb_Po_Modification findByRecordNo(long recordNo);
    tb_Po_Modification findByPoNumberAndRecordNo(String poNumber, Long recordNo);

}
