package com.restaurant.RMS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateEmailDTO {
    private int statusCode;
    private String message;
    private String currentEmail;
    private String newEmail;

    public UpdateEmailDTO(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}
