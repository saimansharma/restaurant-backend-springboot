package com.restaurant.RMS.repository;

import com.restaurant.RMS.entity.Reservation;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByReservationDate(LocalDate date);

    List<Reservation> findByReservationDateAndRestaurantTable_IdAndStatus(LocalDate date, int tableId, String status);

    List<Reservation> findByUser_IdAndStatus(long userId, String status);

//    List<Reservation> findByReservationDateBeforeOrReservationDateAndTimeSlot_TimeSlotBeforeAndStatusNot(LocalDate beforeToday, LocalDate dateToday, LocalTime time, String status);

    @Query(value = "SELECT r.id, r.reservation_date, r.table_id, r.status, r.time_slot_id, r.user_id " +
            "FROM [dbo].[reservation] r " +
            "LEFT JOIN [dbo].[time_slot] ts ON ts.id = r.time_slot_id " +
            "WHERE r.reservation_date < :beforeToday " +
            "   OR (r.reservation_date = :dateToday " +
            "       AND CAST(ts.time_slot AS TIME) = CAST(:time AS TIME) " +
            "       AND r.status <> :status)",
            nativeQuery = true)
    List<Reservation> findByReservationDateBeforeOrReservationDateAndTimeSlot_TimeSlotBeforeAndStatusNot(@Param("beforeToday") LocalDate beforeToday,
                                                          @Param("dateToday") LocalDate dateToday,
                                                          @Param("time") LocalTime time,
                                                          @Param("status") String status);

    List<Reservation> findByUser_IdAndStatusNot(long userId, String status);

    Reservation findByIdAndStatus(long reservationId, String status);

    List<Reservation> findAllByStatus(String status, Sort sort);

    List<Reservation> findByStatusAndUser_NameContainingOrStatusAndUser_MobileNoContaining(String status1, String username, String status2, String mobileNo);

}
