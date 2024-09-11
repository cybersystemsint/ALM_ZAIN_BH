package com.telkom.almKSAZain.repo;

import com.telkom.almKSAZain.model.dccstatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface dccstatusrepo extends JpaRepository<dccstatus,Long> {
    dccstatus findByRecordNo(long recordno);
}
