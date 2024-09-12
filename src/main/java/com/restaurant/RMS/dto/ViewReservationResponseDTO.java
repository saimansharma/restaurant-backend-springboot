package com.restaurant.RMS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewReservationResponseDTO {

    private long userId;

    private List<ReservationDetailsDTO> reservationDetails;
}
