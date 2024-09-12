package com.restaurant.RMS.repository;

import com.restaurant.RMS.entity.Awards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AwardRepository extends JpaRepository<Awards, Long> {

}
