package com.dineahead.backend.repository;

import com.dineahead.backend.model.MenuItem;
import com.dineahead.backend.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findByRestaurant(Restaurant restaurant);

    List<MenuItem> findByRestaurantAndIsAvailableTrue(Restaurant restaurant);
}
