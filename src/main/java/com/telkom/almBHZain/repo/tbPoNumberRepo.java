
package com.telkom.almBHZain.repo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.telkom.almBHZain.model.tbPoNumber;

public interface tbPoNumberRepo extends JpaRepository<tbPoNumber, Long> {
    
      tbPoNumber findByPoNumber(String PoNumber);
     
          @Query("SELECT p FROM tbPoNumber p WHERE " +
           "LOWER(p.poNumber) LIKE %:searchTerm% OR " +
           "LOWER(p.approvalStatus) LIKE %:searchTerm%")
    Page<tbPoNumber> searchAllColumns(@Param("searchTerm") String searchTerm, Pageable pageable);

    
    // Keyset pagination for search (fast for deep paging)
 @Query("SELECT p FROM tbPoNumber p WHERE " +
       "(:searchTerm IS NULL OR LOWER(p.poNumber) LIKE %:searchTerm% OR LOWER(p.approvalStatus) LIKE %:searchTerm%) " +
       "AND p.id < :afterId " +
       "ORDER BY p.id DESC")
Page<tbPoNumber> findAfterId(@Param("searchTerm") String searchTerm, @Param("afterId") Long afterId, Pageable pageable);
}
