/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.telkom.almBHZain.repo;

import com.telkom.almBHZain.model.tbCategoryApprovalLevels;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author jgithu
 */

public interface tbCategoryApprovalLevelRepo extends JpaRepository<tbCategoryApprovalLevels, Long> {

    List<tbCategoryApprovalLevels> findByItemCategoryCode(String itemCategoryCode);

}
