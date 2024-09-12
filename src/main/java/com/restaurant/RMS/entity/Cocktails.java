package com.restaurant.RMS.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "Cocktails")
public class Cocktails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;

    private double price;

    @ElementCollection
    @CollectionTable(name = "cocktail_tags")
    private List<String> cocktailTags;

    private String status;

    private String displayHomePage;

}
