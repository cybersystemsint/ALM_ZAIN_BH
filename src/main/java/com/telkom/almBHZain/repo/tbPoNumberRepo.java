/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.telkom.almBHZain.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.telkom.almBHZain.model.tbPoNumber;

/**
 *
 * @author jgithu
 */
public interface tbPoNumberRepo extends JpaRepository<tbPoNumber, Long> {
    
      tbPoNumber findByPoNumber(String PoNumber);
     
}
