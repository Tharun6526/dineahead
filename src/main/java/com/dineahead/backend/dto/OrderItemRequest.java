package com.dineahead.backend.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class OrderItemRequest {
    private UUID itemId;
    private Integer quantity;
}
