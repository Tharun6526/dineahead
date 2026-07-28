package com.dineahead.backend.dto;

import lombok.Data;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderRequest {
    private UUID restaurantId;
    private List<OrderItemRequest> items;
    private LocalDateTime arrivalTime;
    private String paymentType;
    private Integer tableNumber;
    private LocalDateTime customKitchenFireTime;

}
