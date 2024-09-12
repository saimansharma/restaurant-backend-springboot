package com.restaurant.RMS.dto;

import lombok.Data;

import java.util.List;

@Data
public class CocktailsDTO {

    private long id;
    private String title;
    private double price;
    private List<String> cocktailTags;
    private String status;
    private String displayHomePage;
}
