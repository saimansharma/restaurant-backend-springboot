package com.restaurant.RMS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserProfileDTO {
    private int statusCode;
    private String message;
    private String name;
    private String email;
    private String mobileNo;
    private String oldPassword;
    private String newPassword;

    public UserProfileDTO(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public UserProfileDTO(int statusCode, String message, String name, String email, String mobileNo) {
        this.statusCode = statusCode;
        this.message = message;
        this.name = name;
        this.email = email;
        this.mobileNo = mobileNo;
    }
}


