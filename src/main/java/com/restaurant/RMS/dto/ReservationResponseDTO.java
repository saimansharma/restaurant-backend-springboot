package com.restaurant.RMS.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationResponseDTO {

    private LocalDate date;

    private List<RestaurantTableDTO> restaurantTables;

}
