package com.restaurant.RMS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookTableRequestDTO {

    private int tableId;

    private LocalTime timeSlot;

    private LocalDate reservationDate;

}
