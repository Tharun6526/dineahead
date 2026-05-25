package com.dineahead.backend.service;

import com.dineahead.backend.model.Order;
import com.dineahead.backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KitchenTimerService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private NotificationService notificationService;

    // Runs every 60 seconds — fires orders to kitchen at the right time
    @Scheduled(fixedRate = 60000)
    public void fireOrdersToKitchen() {
        List<Order> pendingOrders = orderRepository
                .findByStatusAndKitchenFireTimeBefore(
                        Order.OrderStatus.PENDING,
                        LocalDateTime.now()
                );

        for (Order order : pendingOrders) {
            order.setStatus(Order.OrderStatus.PREPARING);
            orderRepository.save(order);
            notificationService.notifyKitchen(order);
            notificationService.notifyCustomer(order,
                    "Your food is being prepared! We'll see you soon.");
        }
    }

    // Runs every 5 minutes — marks cash orders as NO_SHOW if customer never arrived
    @Scheduled(fixedRate = 300000)
    public void handleNoShows() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        List<Order> lateOrders = orderRepository
                .findByStatusAndArrivalTimeBefore(Order.OrderStatus.READY, cutoff);

        for (Order order : lateOrders) {
            if (order.getPaymentType() == Order.PaymentType.CASH) {
                order.setStatus(Order.OrderStatus.NO_SHOW);
                orderRepository.save(order);

                // Increment no-show count on user
                var user = order.getUser();
                user.setNoShowCount(user.getNoShowCount() + 1);
                if (user.getNoShowCount() >= 3) {
                    user.setIsBlocked(true);
                }
            }
        }
    }
}
