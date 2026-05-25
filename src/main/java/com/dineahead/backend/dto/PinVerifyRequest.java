package com.dineahead.backend.dto;

import lombok.Data;
import org.hibernate.validator.constraints.UUID;

@Data
public class PinVerifyRequest {
    private String pin;
    private UUID restaurantId;
}
