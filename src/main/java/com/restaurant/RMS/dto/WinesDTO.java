package com.restaurant.RMS.dto;

import lombok.Data;

import java.util.List;

@Data
public class WinesDTO {

    private long id;
    private String title;
    private double price;
    private List<String> wineTags;
    private String status;
    private String displayHomePage;
}
