package com.restaurant.RMS.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDTO {
    private LocalDate requestedDate;
}
