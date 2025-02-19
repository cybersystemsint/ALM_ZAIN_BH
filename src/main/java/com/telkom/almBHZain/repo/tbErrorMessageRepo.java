/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.telkom.almBHZain.repo;

import com.telkom.almBHZain.model.tb_ErrorMessage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author jgithu
 */
public interface tbErrorMessageRepo extends JpaRepository<tb_ErrorMessage, Long> {

    tb_ErrorMessage findByRecordNo(long recordNo);

    tb_ErrorMessage findByModuleAndErrorCodeAndOperation(String module, String errorCode, String operation);
}
