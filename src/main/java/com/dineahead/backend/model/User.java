package com.dineahead.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String phone;

    @JsonIgnore
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.CUSTOMER;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer noShowCount = 0;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isBlocked = false;

    private String fcmToken;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum UserRole {
        CUSTOMER, RESTAURANT_OWNER, ADMIN
    }

    private boolean cashPaymentEnabled = true;
}