package com.dineahead.backend.repository;

import com.dineahead.backend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByRestaurantRestaurantId(UUID restaurantId);

    boolean existsByOrderOrderId(UUID orderId);
}
