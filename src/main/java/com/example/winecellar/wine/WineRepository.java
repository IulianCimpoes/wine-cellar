package com.example.winecellar.wine;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WineRepository extends JpaRepository<Wine, Long> {
    // Derived query method: Spring builds the query from the method name
    List<Wine> findByCountryIgnoreCase(String country);

    @Query("""
            select w
            from Wine w
            join fetch w.wineryRef
            """)
    List<Wine> findAllWithWinery();

    @Query("""
            select w
            from Wine w
            join fetch w.wineryRef
            where lower(w.country) = lower(:country)
            """)
    List<Wine> findByCountryWithWinery(@Param("country") String country);

    Page<Wine> findAll(Pageable pageable);

    long countByWineryRef_Id(Long wineryId);

    List<Wine> findByWineryRef_Id(Long wineryId);

    Page<Wine> findByWineryRef_Id(Long wineryId, Pageable pageable);

}
