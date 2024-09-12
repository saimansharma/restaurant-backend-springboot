package com.restaurant.RMS.repository;

import com.restaurant.RMS.entity.Cocktails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CocktailRepository extends JpaRepository<Cocktails, Long> {

    @Query(value = "Select * from cocktails where status='active'", nativeQuery = true)
    public List<Cocktails> findActiveCocktails();
}
