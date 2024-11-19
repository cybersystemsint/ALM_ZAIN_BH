/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.telkom.almKSAZain.repo;

import com.telkom.almKSAZain.model.tb_Region;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author jgithu
 */
public interface tbRegionRepo extends JpaRepository<tb_Region, Long> {

    tb_Region findByRecordNo(long recordNo);

}
