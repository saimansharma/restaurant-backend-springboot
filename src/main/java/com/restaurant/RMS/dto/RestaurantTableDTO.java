package com.restaurant.RMS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantTableDTO {

    private int id;
    private String tableDesc;
    private String tableImg;
    private List<AvailableSlotDTO> availableSlots;

}
