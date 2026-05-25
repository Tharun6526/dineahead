package com.dineahead.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID itemId;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer prepTimeMinutes;

    private String category;
    private String imageUrl;

    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isAvailable = true;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isVeg = false;
}
