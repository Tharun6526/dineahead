package com.dineahead.backend.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class PinVerifyRequest {

    private String pickupOrderNumber;
    private String pin;
    private UUID restaurantId;
}