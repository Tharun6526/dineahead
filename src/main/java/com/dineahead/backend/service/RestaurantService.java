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

        existing.setName(updated.getName());
        existing.setAddress(updated.getAddress());
        existing.setLatitude(updated.getLatitude());
        existing.setLongitude(updated.getLongitude());
        existing.setTotalTables(updated.getTotalTables());
        existing.setOpeningTime(updated.getOpeningTime());
        existing.setClosingTime(updated.getClosingTime());
        existing.setCuisineType(updated.getCuisineType());
        existing.setImageUrl(updated.getImageUrl());

        return restaurantRepository.save(existing);
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

    public MenuItem updateMenuItem(UUID restaurantId,
                                   UUID itemId,
                                   MenuItem updated,
                                   String ownerEmail) {

        Restaurant restaurant = getById(restaurantId);

        if (!restaurant.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        MenuItem existing = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        if (!existing.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new RuntimeException("Menu item does not belong to this restaurant");
        }

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setPrepTimeMinutes(updated.getPrepTimeMinutes());
        existing.setCategory(updated.getCategory());
        existing.setImageUrl(updated.getImageUrl());
        existing.setIsAvailable(updated.getIsAvailable());
        existing.setIsVeg(updated.getIsVeg());

        return menuItemRepository.save(existing);
    }

    public MenuItem toggleMenuItemAvailability(UUID itemId) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        item.setIsAvailable(!item.getIsAvailable());
        return menuItemRepository.save(item);
    }

    public void deleteMenuItem(UUID restaurantId, UUID itemId, String ownerEmail) {

        Restaurant restaurant = getById(restaurantId);

        if (!restaurant.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        if (!item.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new RuntimeException("Menu item does not belong to this restaurant");
        }

        menuItemRepository.delete(item);
    }
}



