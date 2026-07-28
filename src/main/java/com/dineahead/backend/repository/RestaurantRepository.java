package com.dineahead.backend.repository;

import com.dineahead.backend.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;
import com.dineahead.backend.model.User;
import java.util.Optional;
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    List<Restaurant> findByIsActiveTrue();

    List<Restaurant> findByCuisineTypeContainingIgnoreCase(String cuisineType);
    Optional<Restaurant> findByOwner(User owner);
    @Query("SELECT r FROM Restaurant r WHERE " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) * " +
            "cos(radians(r.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(r.latitude)))) < :radiusKm")
    List<Restaurant> findNearby(double lat, double lng, double radiusKm);
}