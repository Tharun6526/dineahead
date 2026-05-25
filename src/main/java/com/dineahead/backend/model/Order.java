package com.dineahead.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    @Column(unique = true, nullable = false, length = 4)
    private String pin;

    @Column(nullable = false)
    private LocalDateTime arrivalTime;

    @Column(nullable = false)
    private LocalDateTime kitchenFireTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;

    @Column(columnDefinition = "DECIMAL DEFAULT 0")
    private Double depositAmount = 0.0;

    @Column(nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    private Integer tableNumber;
    private String razorpayOrderId;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PaymentType {
        ONLINE, CASH
    }

    public enum OrderStatus {
        PENDING, PREPARING, READY, SERVED, CANCELLED, NO_SHOW
    }
}
