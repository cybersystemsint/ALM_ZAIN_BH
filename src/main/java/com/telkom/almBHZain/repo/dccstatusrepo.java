package com.telkom.almBHZain.repo;

import com.telkom.almBHZain.model.dccstatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface dccstatusrepo extends JpaRepository<dccstatus,Long> {
    dccstatus findByRecordNo(long recordno);
}
