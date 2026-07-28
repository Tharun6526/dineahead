// ─── AuthController.java ───────────────────────────────────────────────────
package com.dineahead.backend.controller;

import com.dineahead.backend.dto.ApiResponse;
import com.dineahead.backend.model.MenuItem;
import com.dineahead.backend.model.Restaurant;
import com.dineahead.backend.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*")
public class RestaurantController {

    @Autowired private RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("Restaurants fetched",
                restaurantService.getAllActive()));
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> getNearby(@RequestParam double lat,
                                       @RequestParam double lng,
                                       @RequestParam(defaultValue = "5") double radius) {
        return ResponseEntity.ok(ApiResponse.ok("Nearby restaurants",
                restaurantService.getNearby(lat, lng, radius)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Restaurant fetched",
                    restaurantService.getById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Restaurant restaurant,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Restaurant created",
                    restaurantService.create(restaurant, userDetails.getUsername())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable UUID id,
            @RequestBody Restaurant restaurant,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            return ResponseEntity.ok(
                    ApiResponse.ok(
                            "Restaurant updated",
                            restaurantService.update(
                                    id,
                                    restaurant,
                                    userDetails.getUsername()
                            )
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<?> getMenu(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Menu fetched",
                restaurantService.getMenu(id)));
    }

    @PostMapping("/{id}/menu")
    public ResponseEntity<?> addMenuItem(@PathVariable UUID id,
                                         @RequestBody MenuItem item,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Item added",
                    restaurantService.addMenuItem(id, item, userDetails.getUsername())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{restaurantId}/menu/{itemId}")
    public ResponseEntity<?> updateMenuItem(@PathVariable UUID restaurantId,
                                            @PathVariable UUID itemId,
                                            @RequestBody MenuItem item,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Item updated",
                    restaurantService.updateMenuItem(restaurantId, itemId, item,
                            userDetails.getUsername())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/menu/{itemId}/toggle")
    public ResponseEntity<?> toggleItem(@PathVariable UUID itemId) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Availability toggled",
                    restaurantService.toggleMenuItemAvailability(itemId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    @DeleteMapping("/{restaurantId}/menu/{itemId}")
    public ResponseEntity<?> deleteMenuItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            restaurantService.deleteMenuItem(
                    restaurantId,
                    itemId,
                    userDetails.getUsername());

            return ResponseEntity.ok(
                    ApiResponse.ok("Menu item deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
