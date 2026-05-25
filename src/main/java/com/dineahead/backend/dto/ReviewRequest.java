// ─── RegisterRequest.java ──────────────────────────────────────────────────
package com.dineahead.backend.dto;

import lombok.Data;
import org.hibernate.validator.constraints.UUID;


@Data
public class ReviewRequest {
    private UUID orderId;
    private UUID restaurantId;
    private Integer rating;
    private String comment;
}


