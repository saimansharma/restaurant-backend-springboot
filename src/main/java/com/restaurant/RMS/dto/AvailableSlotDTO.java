package com.restaurant.RMS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailableSlotDTO {

    private LocalTime timeSlot;
    private String status;

}

