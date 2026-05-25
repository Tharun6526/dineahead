package com.dineahead.backend.controller;

import com.dineahead.backend.dto.ApiResponse;
import com.dineahead.backend.dto.DelayRequest;
import com.dineahead.backend.dto.OrderRequest;
import com.dineahead.backend.dto.PinVerifyRequest;
import com.dineahead.backend.model.Order;
import com.dineahead.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired private OrderService orderService;

    @PostMapping
    public ResponseEntity<?> placeOrder(
            @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Order order = orderService.placeOrder(
                    userDetails.getUsername(), request);
            return ResponseEntity.ok(
                    ApiResponse.ok("Order placed! PIN: " + order.getPin(), order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify-pin")
    public ResponseEntity<?> verifyPin(@RequestBody PinVerifyRequest request) {
        try {
            Order order = orderService.verifyPin(request);
            return ResponseEntity.ok(
                    ApiResponse.ok("Food served successfully!", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/delay")
    public ResponseEntity<?> delayOrder(
            @PathVariable UUID id,
            @RequestBody DelayRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Order order = orderService.delayOrder(
                    id, userDetails.getUsername(), request);
            return ResponseEntity.ok(
                    ApiResponse.ok("Arrival time updated", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Order order = orderService.cancelOrder(
                    id, userDetails.getUsername());
            return ResponseEntity.ok(ApiResponse.ok("Order cancelled", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> myOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Orders fetched",
                orderService.getMyOrders(userDetails.getUsername())));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<?> restaurantOrders(@PathVariable UUID restaurantId) {
        return ResponseEntity.ok(ApiResponse.ok("Orders fetched",
                orderService.getRestaurantOrders(restaurantId)));
    }
}