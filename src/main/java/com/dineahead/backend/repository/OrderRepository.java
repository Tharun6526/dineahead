package com.dineahead.backend.repository;

import com.dineahead.backend.model.Order;
import com.dineahead.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByPin(String pin);

    List<Order> findByUser(User user);

    List<Order> findByRestaurantRestaurantId(UUID restaurantId);

    List<Order> findByStatusAndKitchenFireTimeBefore(Order.OrderStatus status, LocalDateTime time);

    boolean existsByPinAndStatusNot(String pin, Order.OrderStatus status);

    List<Order> findByStatusAndArrivalTimeBefore(Order.OrderStatus status, LocalDateTime time);
}
