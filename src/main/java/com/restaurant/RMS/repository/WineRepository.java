package com.restaurant.RMS.repository;

import com.restaurant.RMS.entity.Wines;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WineRepository extends JpaRepository<Wines, Long> {

    @Query(value = "SELECT * FROM Wines WHERE status = 'active'", nativeQuery = true)
    public List<Wines> findActiveWines();
}
