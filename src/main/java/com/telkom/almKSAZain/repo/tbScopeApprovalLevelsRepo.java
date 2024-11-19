/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.telkom.almKSAZain.repo;

import com.telkom.almKSAZain.model.tbScopeApprovalLevels;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 *
 * @author jgithu
 */

public interface tbScopeApprovalLevelsRepo extends JpaRepository<tbScopeApprovalLevels, Long> {

    List<tbScopeApprovalLevels> findByScope(Integer scope);

}
