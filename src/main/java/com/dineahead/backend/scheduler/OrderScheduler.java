package com.dineahead.backend.scheduler;

import com.dineahead.backend.model.Order;
import com.dineahead.backend.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderScheduler {

    private final OrderRepository orderRepository;

    public OrderScheduler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void autoStartCooking() {

        LocalDateTime now = LocalDateTime.now();

        List<Order> pendingOrders =
                orderRepository.findByStatus(Order.OrderStatus.PENDING);

        for (Order order : pendingOrders) {

            if (order.getKitchenFireTime() == null) {
                continue;
            }

            if (!now.isBefore(order.getKitchenFireTime())) {

                order.setStatus(Order.OrderStatus.PREPARING);

                orderRepository.save(order);

                System.out.println(
                        "Auto started cooking for PIN "
                                + order.getPin());
            }
        }
    }
}