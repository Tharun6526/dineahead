package com.dineahead.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID restaurantId;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Integer totalTables;

    @Column(nullable = false)
    private Integer availableTables;

    private LocalTime openingTime;
    private LocalTime closingTime;

    private String cuisineType;
    private String imageUrl;
    private Double rating = 0.0;
    private Integer totalReviews = 0;

    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<MenuItem> menuItems;
}