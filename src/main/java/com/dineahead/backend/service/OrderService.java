package com.dineahead.backend.service;

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
        order.setPin(generateUniquePIN());
        order.setArrivalTime(request.getArrivalTime());
        order.setKitchenFireTime(
                request.getArrivalTime().minusMinutes(maxPrepTime + 2));
        order.setPaymentType(
                Order.PaymentType.valueOf(request.getPaymentType().toUpperCase()));
        order.setTotalAmount(totalAmount);
        order.setTableNumber(request.getTableNumber());
        order.setStatus(Order.OrderStatus.PENDING);

        if (order.getPaymentType() == Order.PaymentType.CASH) {
            order.setDepositAmount(50.0);
        }

        Order saved = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(saved);
        }

        notificationService.notifyCustomer(saved,
                "Order confirmed! Your PIN is: " + saved.getPin());

        return saved;
    }

    public Order verifyPin(PinVerifyRequest request) {
        Order order = orderRepository.findByPin(request.getPin())
                .orElseThrow(() -> new RuntimeException("Invalid PIN"));

        if (!order.getRestaurant().getRestaurantId()
                .equals(request.getRestaurantId())) {
            throw new RuntimeException("PIN does not belong to this restaurant");
        }

        if (order.getStatus() != Order.OrderStatus.READY) {
            throw new RuntimeException("Order not ready yet. Status: "
                    + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.SERVED);
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
}