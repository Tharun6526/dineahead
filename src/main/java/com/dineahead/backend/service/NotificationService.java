package com.dineahead.backend.service;

import com.dineahead.backend.model.Order;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void notifyCustomer(Order order, String message) {
        // TODO: Integrate Firebase FCM here
        // Send push notification to order.getUser().getFcmToken()
        System.out.println("[FCM → Customer] " + order.getUser().getEmail() + ": " + message);
    }

    public void notifyKitchen(Order order) {
        String message = "NEW ORDER #" + order.getPin() +
                " | Table " + order.getTableNumber() +
                " | Start preparing now!";
        // TODO: Send via WebSocket to restaurant dashboard
        System.out.println("[KITCHEN ALERT] " + order.getRestaurant().getName() + ": " + message);
    }

    public void notifyRestaurant(Order order, String message) {
        System.out.println("[RESTAURANT ALERT] " + order.getRestaurant().getName() + ": " + message);
    }
}
