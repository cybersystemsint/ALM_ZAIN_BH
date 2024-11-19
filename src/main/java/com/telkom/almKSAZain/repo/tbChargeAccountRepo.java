/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.telkom.almKSAZain.repo;

import com.telkom.almKSAZain.model.tb_ChargeAccount;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author jgithu
 */
public interface tbChargeAccountRepo extends JpaRepository<tb_ChargeAccount, Long> {

    tb_ChargeAccount findByRecordNo(long recordNo);

    tb_ChargeAccount findByChargeAccount(String chargeAccount);

}
