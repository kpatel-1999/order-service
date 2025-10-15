package org.example.orderservice.service;

import jakarta.transaction.Transactional;
import org.example.orderservice.entities.Order;
import org.example.orderservice.entities.OrderStatus;
import org.example.orderservice.exception.OrderCancellationException;
import org.example.orderservice.exception.ResourceNotFoundException;
import org.example.orderservice.exception.UnAuthorizedException;
import org.example.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order createOrder(Order order) {
        logger.info("[Order Service] Creating order for customerId={} with {} items",
                order.getCustomerId(), order.getItems().size());

        Order createdOrder = orderRepository.save(order);

        logger.info("[Order Service] Order created successfully: orderId={}, customerId={}",
                createdOrder.getId(), createdOrder.getCustomerId());

        return createdOrder;
    }

    public Order getOrderById(Long orderId) {
        logger.info("[Order Service] Fetching order for orderId={}", orderId);

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order with ID " + orderId + " not found"));
    }

    public List<Order> getAllOrders(OrderStatus status) {
        logger.info("[Order Service] Fetching all orders  status={}", status);

        final List<Order> orders;
        if (status == null) {
            orders = orderRepository.findAll();
        } else {
            orders = orderRepository.findOrderByStatus(status);
        }

        logger.info("[Order Service] Retrieved {} orders, status={}", orders.size(), status);
        return orders;
    }

    @Transactional
    public void cancelOrder(Long orderId, String customerId) {
        logger.info("[Order Service] Cancelling order for  orderId={}, customerId={}", orderId, customerId);

        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order with ID " + orderId + " not found"));

        if (!existingOrder.getCustomerId().equals(customerId)) {
            logger.warn("[Order Service] Unauthorized cancellation attempt for  orderId={}, customerId={}", orderId, customerId);
            throw new UnAuthorizedException("You are not authorized to cancel this order");
        }

        if (existingOrder.getStatus() != OrderStatus.PENDING) {
            logger.warn("[Order Service] Cannot cancel order for  orderId={}, currentStatus={}", orderId, existingOrder.getStatus());
            throw new OrderCancellationException("Order cannot be cancelled as it is already " + existingOrder.getStatus());
        }

        existingOrder.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(existingOrder);

        logger.info("[Order Service] Order cancelled successfully for orderId={}", orderId);
    }

    @Transactional
    public int updateOrderStatus(OrderStatus currentStatus, OrderStatus newStatus) {
        logger.info("[Order Service] Updating orders from status {} to {}", currentStatus, newStatus);

        int updatedCount = orderRepository.updateStatus(currentStatus, newStatus);

        logger.info("[Order Service]Updated {} orders from {} to {}", updatedCount, currentStatus, newStatus);

        return updatedCount;
    }


}
