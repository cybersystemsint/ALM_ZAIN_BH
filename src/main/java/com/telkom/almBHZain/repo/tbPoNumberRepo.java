/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.telkom.almBHZain.repo;

import com.telkom.almBHZain.model.tbPoNumber;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author jgithu
 */
public interface tbPoNumberRepo extends JpaRepository<tbPoNumber, Long> {
    
      tbPoNumber findByPoNumber(String PoNumber);

}
