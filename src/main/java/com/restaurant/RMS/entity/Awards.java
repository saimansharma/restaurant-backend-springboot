package com.restaurant.RMS.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "Awards")
@NoArgsConstructor
@AllArgsConstructor
public class Awards {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String imageUrl;

    private String title;

    private String subtitle;

}
