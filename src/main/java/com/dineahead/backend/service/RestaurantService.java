// ─── RestaurantService.java ────────────────────────────────────────────────
package com.dineahead.backend.service;

import com.dineahead.backend.model.MenuItem;
import com.dineahead.backend.model.Restaurant;
import com.dineahead.backend.model.User;
import com.dineahead.backend.repository.MenuItemRepository;
import com.dineahead.backend.repository.RestaurantRepository;
import com.dineahead.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RestaurantService {

    @Autowired private RestaurantRepository restaurantRepository;
    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private UserRepository userRepository;

    public List<Restaurant> getAllActive() {
        return restaurantRepository.findByIsActiveTrue();
    }

    public List<Restaurant> getNearby(double lat, double lng, double radiusKm) {
        return restaurantRepository.findNearby(lat, lng, radiusKm);
    }

    public Restaurant getById(UUID id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
    }

    public Restaurant create(Restaurant restaurant, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        restaurant.setOwner(owner);
        restaurant.setAvailableTables(restaurant.getTotalTables());
        return restaurantRepository.save(restaurant);
    }

    public Restaurant update(UUID id, Restaurant updated, String ownerEmail) {
        Restaurant existing = getById(id);
        if (!existing.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized — not the owner");
        }
        updated.setRestaurantId(id);
        updated.setOwner(existing.getOwner());
        return restaurantRepository.save(updated);
    }

    public List<MenuItem> getMenu(UUID restaurantId) {
        Restaurant restaurant = getById(restaurantId);
        return menuItemRepository.findByRestaurantAndIsAvailableTrue(restaurant);
    }

    public MenuItem addMenuItem(UUID restaurantId, MenuItem item, String ownerEmail) {
        Restaurant restaurant = getById(restaurantId);
        if (!restaurant.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized");
        }
        item.setRestaurant(restaurant);
        return menuItemRepository.save(item);
    }

    public MenuItem updateMenuItem(UUID restaurantId, UUID itemId,
                                   MenuItem updated, String ownerEmail) {
        Restaurant restaurant = getById(restaurantId);
        if (!restaurant.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized");
        }
        updated.setItemId(itemId);
        updated.setRestaurant(restaurant);
        return menuItemRepository.save(updated);
    }

    public MenuItem toggleMenuItemAvailability(UUID itemId) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        item.setIsAvailable(!item.getIsAvailable());
        return menuItemRepository.save(item);
    }
}



