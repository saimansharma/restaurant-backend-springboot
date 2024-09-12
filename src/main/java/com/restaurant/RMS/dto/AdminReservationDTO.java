package com.restaurant.RMS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminReservationDTO {

    private long reservationId;

    private String tableDesc;

    private LocalDate reservationDate;

    private LocalTime timeSlot;

    private String username;

    private String phoneNo;

    private String status;

}
