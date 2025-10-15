package org.example.orderservice.jobs;

import org.example.orderservice.entities.OrderStatus;
import org.example.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderProcessingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OrderProcessingScheduler.class);
    private final OrderService orderService;

    public OrderProcessingScheduler(OrderService orderService){
        this.orderService = orderService;
    }

    @Scheduled(cron = "0 0/5 * * * *")
    public void processPendingOrders() {
        logger.info("Scheduled Task: Starting processing of pending orders at {}", LocalDateTime.now());

        try {
            int updatedCount = orderService.updateOrderStatus(OrderStatus.PENDING, OrderStatus.PROCESSING);
            if (updatedCount > 0) {
                logger.info("Scheduled Task: Successfully updated {} orders from PENDING to PROCESSING", updatedCount);
            } else {
                logger.info("Scheduled Task: No pending orders found to process");
            }
        } catch (Exception ex) {
            logger.error("Scheduled Task: Error while processing pending orders", ex);
        }

        logger.info("Scheduled Task: Finished processing pending orders at {}", LocalDateTime.now());
    }

}
