package com.restaurant.RMS.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Table(name = "Wines")
@AllArgsConstructor
@NoArgsConstructor
public class Wines {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;

    private double price;

    @ElementCollection
    @CollectionTable(name="wine_tags")
    private List<String> wineTags;

    private String status;

    private String displayHomePage;

}
