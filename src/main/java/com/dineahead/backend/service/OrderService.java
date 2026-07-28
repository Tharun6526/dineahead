package com.dineahead.backend.service;

import com.dineahead.backend.dto.ApiResponse;
import com.dineahead.backend.dto.DelayRequest;
import com.dineahead.backend.dto.OrderRequest;
import com.dineahead.backend.dto.PinVerifyRequest;
import com.dineahead.backend.model.*;
import com.dineahead.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RestaurantRepository restaurantRepository;
    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private NotificationService notificationService;

    public Order placeOrder(String userEmail, OrderRequest request) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getIsBlocked()) {
            throw new RuntimeException("Your account is blocked");
        }

        // Convert payment type only once
        Order.PaymentType paymentType =
                Order.PaymentType.valueOf(request.getPaymentType().toUpperCase());

        // Prevent cash payment after repeated no-shows
        if (paymentType == Order.PaymentType.CASH
                && !user.isCashPaymentEnabled()) {

            throw new RuntimeException(
                    "Cash payment is disabled because of repeated no-shows. Please choose Online Payment."
            );
        }

        Restaurant restaurant = restaurantRepository
                .findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (!restaurant.getIsActive()) {
            throw new RuntimeException("Restaurant is not accepting orders");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0;
        int maxPrepTime = 0;

        for (var itemRequest : request.getItems()) {

            MenuItem menuItem = menuItemRepository
                    .findById(itemRequest.getItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));

            if (!menuItem.getIsAvailable()) {
                throw new RuntimeException(menuItem.getName() + " is unavailable");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtOrder(menuItem.getPrice());

            orderItems.add(orderItem);

            totalAmount += menuItem.getPrice() * itemRequest.getQuantity();
            maxPrepTime = Math.max(maxPrepTime, menuItem.getPrepTimeMinutes());
        }

        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setPaymentType(paymentType);
        order.setPin(generateUniquePIN());
        order.setPickupOrderNumber(generateUniquePickupOrderNumber());
        order.setArrivalTime(request.getArrivalTime());

        if (request.getCustomKitchenFireTime() != null) {

            LocalDateTime latestAllowed =
                    request.getArrivalTime().minusMinutes(maxPrepTime);

            if (request.getCustomKitchenFireTime().isAfter(latestAllowed)) {
                throw new RuntimeException(
                        "Cooking start time is too late for the selected items");
            }

            order.setKitchenFireTime(request.getCustomKitchenFireTime());
            order.setCustomCookingTime(true);

        } else {

            order.setKitchenFireTime(
                    request.getArrivalTime().minusMinutes(maxPrepTime + 2));

            order.setCustomCookingTime(false);
        }

        order.setTotalAmount(totalAmount);
        order.setTableNumber(request.getTableNumber());
        order.setStatus(Order.OrderStatus.PENDING);

        if (paymentType == Order.PaymentType.CASH) {
            order.setDepositAmount(50.0);
        }

        // Link Order and OrderItems (required for CascadeType.ALL)
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }

        order.setItems(orderItems);

        // Save Order and OrderItems together
        Order saved = orderRepository.save(order);

        notificationService.notifyCustomer(
                saved,
                "Order confirmed!\n" +
                        "Pickup Order Number: " + saved.getPickupOrderNumber() +
                        "\nPIN: " + saved.getPin()
        );

        return saved;
    }

    public Order verifyPin(PinVerifyRequest request) {

        Order order = orderRepository
                .findByRestaurantRestaurantIdAndPickupOrderNumber(
                request.getRestaurantId(),
                request.getPickupOrderNumber().trim().toUpperCase()
        )
                .orElseThrow(() ->
                        new RuntimeException("Invalid Pickup Order Number"));

        if (request.getPin() == null || !order.getPin().equals(request.getPin())) {
            throw new RuntimeException("Invalid PIN");
        }

        if (order.getStatus() != Order.OrderStatus.READY) {
            throw new RuntimeException(
                    "Order not ready yet. Status: " + order.getStatus()
            );
        }

        order.setStatus(Order.OrderStatus.SERVED);

        return orderRepository.save(order);
    }
    public Order updateStatus(UUID orderId, String status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        order.setStatus(Order.OrderStatus.valueOf(status));

        return orderRepository.save(order);
    }

    public Order delayOrder(UUID orderId, String userEmail,
                            DelayRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Cannot delay — kitchen already started");
        }

        order.setArrivalTime(
                order.getArrivalTime().plusMinutes(request.getDelayMinutes()));
        order.setKitchenFireTime(
                order.getKitchenFireTime().plusMinutes(request.getDelayMinutes()));

        notificationService.notifyRestaurant(order,
                "Customer is running " + request.getDelayMinutes() + " mins late");

        return orderRepository.save(order);
    }

    public Order cancelOrder(UUID orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        if (order.getStatus() == Order.OrderStatus.PREPARING
                || order.getStatus() == Order.OrderStatus.READY) {
            throw new RuntimeException("Cannot cancel — food already being prepared");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    public List<Order> getMyOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUser(user);
    }

    public List<Order> getRestaurantOrders(UUID restaurantId) {
        return orderRepository.findByRestaurantRestaurantId(restaurantId);
    }

    private String generateUniquePIN() {
        String pin;
        do {
            int raw = (int) (Math.random() * 9000) + 1000;
            pin = String.valueOf(raw);
        } while (orderRepository.existsByPinAndStatusNot(
                pin, Order.OrderStatus.SERVED));
        return pin;
    }
    private String generateUniquePickupOrderNumber() {
        String orderNumber;

        do {
            int raw = (int) (Math.random() * 9000) + 1000;
            orderNumber = "DA-" + raw;
        } while (orderRepository.existsByPickupOrderNumber(orderNumber));

        return orderNumber;
    }
    public ApiResponse markNoShow(UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Validate current status BEFORE changing it
        if (order.getStatus() == Order.OrderStatus.SERVED) {
            throw new RuntimeException("Food has already been served");
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already cancelled");
        }

        if (order.getStatus() == Order.OrderStatus.NO_SHOW) {
            throw new RuntimeException("Order is already marked as NO_SHOW");
        }

        if (order.getStatus() != Order.OrderStatus.READY) {
            throw new RuntimeException("Only READY orders can be marked as NO_SHOW");
        }

        // Update order
        order.setStatus(Order.OrderStatus.NO_SHOW);

        // Update customer
        User user = order.getUser();
        user.setNoShowCount(user.getNoShowCount() + 1);

        if (user.getNoShowCount() >= 3) {
            user.setCashPaymentEnabled(false);
        }

        userRepository.save(user);
        orderRepository.save(order);

        return new ApiResponse(
                true,
                "Order marked as NO_SHOW successfully",
                null
        );    }
}