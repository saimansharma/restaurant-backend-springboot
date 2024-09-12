package com.restaurant.RMS.repository;

import com.restaurant.RMS.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Integer> {

    @Query("SELECT ts FROM TimeSlot ts WHERE CAST(ts.timeSlot AS TIME) = CAST(:timeSlot AS TIME)")
    TimeSlot findByTimeSlot(@Param("timeSlot") LocalTime timeSlot);

//    TimeSlot findByTimeSlot(LocalTime timeSlot);
}
