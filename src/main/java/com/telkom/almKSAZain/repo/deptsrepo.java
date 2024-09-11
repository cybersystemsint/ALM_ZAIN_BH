package com.telkom.almKSAZain.repo;

import com.telkom.almKSAZain.model.departmentsdata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface deptsrepo extends JpaRepository<departmentsdata,Long> {
    departmentsdata findByRecordNo(long recordno);
    List<departmentsdata> findBySysStatus(boolean active);
}
