package org.example.orderservice.service;

import org.example.orderservice.entities.Order;
import org.example.orderservice.entities.OrderItem;
import org.example.orderservice.entities.OrderStatus;
import org.example.orderservice.exception.OrderCancellationException;
import org.example.orderservice.exception.ResourceNotFoundException;
import org.example.orderservice.exception.UnAuthorizedException;
import org.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class OrderServiceTest {
    private OrderRepository orderRepository;
    private OrderService orderService;


    @BeforeEach
    void setup() {
        orderRepository = mock(OrderRepository.class);
        this.orderService = new OrderService(orderRepository);
    }

    @Test
    void createOrderSuccessfully() {
        Order order = new Order();
        order.setCustomerId("123");
        OrderItem item = new OrderItem();
        item.setProductId(1);
        item.setProductName("Laptop");
        item.setQuantity(2);
        item.setProductPrice(new BigDecimal("5000.00"));
        order.setItems(List.of(item));

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setCustomerId("123");
        savedOrder.setItems(List.of(item));

        when(orderRepository.save(argThat(o ->
                o.getCustomerId().equals("123") &&
                        o.getItems().size() == 1 &&
                        o.getItems().get(0).getProductName().equals("Laptop")
        ))).thenReturn(savedOrder);


        Order createdOrder = orderService.createOrder(order);

        Assertions.assertEquals(1L, createdOrder.getId());
        verify(orderRepository, times(1)).save(argThat(o ->
                o.getCustomerId().equals("123") &&
                        o.getItems().size() == 1 &&
                        o.getItems().get(0).getProductName().equals("Laptop") &&
                        o.getItems().get(0).getQuantity() == 2
        ));
    }

    @Test
    void getOrderByIdSuccessfully() {

        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId("123");
        order.setStatus(OrderStatus.PENDING);
        OrderItem item = new OrderItem();
        item.setProductId(10);
        item.setProductName("Laptop");
        item.setQuantity(2);
        item.setProductPrice(new BigDecimal("5000.00"));
        order.setItems(List.of(item));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order fetchedOrder = orderService.getOrderById(orderId);


        Assertions.assertEquals(1L, fetchedOrder.getId());
        Assertions.assertEquals("123", fetchedOrder.getCustomerId());
        Assertions.assertEquals(OrderStatus.PENDING, fetchedOrder.getStatus());
        Assertions.assertEquals("Laptop", fetchedOrder.getItems().get(0).getProductName());
        Assertions.assertEquals(new BigDecimal("10000.00"), fetchedOrder.calculateTotalAmount());

        verify(orderRepository, times(1)).findById(orderId);

    }


    @Test
    void throwResourceNotFoundExceptionWhenOrderNotFound() {
        Long orderId = 99L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderById(orderId)
        );

        Assertions.assertEquals("Order with ID 99 not found", exception.getMessage());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getAllOrdersSuccessfully() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId("123");
        order.setStatus(OrderStatus.PENDING);

        OrderItem item = new OrderItem();
        item.setProductId(10);
        item.setProductName("Laptop");
        item.setQuantity(2);
        item.setProductPrice(new BigDecimal("5000.00"));
        order.setItems(List.of(item));

        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> orders = orderService.getAllOrders(null);

        // then
        Assertions.assertEquals(1, orders.size());
        Assertions.assertEquals(1L, orders.get(0).getId());
        Assertions.assertEquals("123", orders.get(0).getCustomerId());
        Assertions.assertEquals(OrderStatus.PENDING, orders.get(0).getStatus());
        Assertions.assertEquals(new BigDecimal("10000.00"), orders.get(0).calculateTotalAmount());
        Assertions.assertEquals("Laptop", orders.get(0).getItems().get(0).getProductName());

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getAllOrdersByStatusSuccessfully() {
        Order order1 = new Order();
        order1.setId(1L);
        order1.setCustomerId("123");
        order1.setStatus(OrderStatus.PROCESSING);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setCustomerId("456");
        order2.setStatus(OrderStatus.PENDING);

        when(orderRepository.findOrderByStatus(OrderStatus.PROCESSING)).thenReturn(List.of(order1));

        List<Order> orders = orderService.getAllOrders(OrderStatus.PROCESSING);

        Assertions.assertEquals(1, orders.size());
        Assertions.assertEquals(OrderStatus.PROCESSING, orders.get(0).getStatus());

        verify(orderRepository, times(1)).findOrderByStatus(OrderStatus.PROCESSING);
    }
    @Test
    void cancelOrderSuccessfully() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId("123");
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1L, "123");

        Assertions.assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void shouldThrowResourceNotFoundWhenCancellingNonExistentOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.cancelOrder(1L, "123")
        );

        Assertions.assertEquals("Order with ID 1 not found", exception.getMessage());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowUnAuthorizedWhenCustomerDoesNotMatch() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId("123");
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        UnAuthorizedException exception = Assertions.assertThrows(
                UnAuthorizedException.class,
                () -> orderService.cancelOrder(1L, "999")
        );

        Assertions.assertEquals("You are not authorized to cancel this order", exception.getMessage());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowOrderCancellationExceptionWhenOrderIsNotPending() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId("123");
        order.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderCancellationException exception = Assertions.assertThrows(
                OrderCancellationException.class,
                () -> orderService.cancelOrder(1L, "123")
        );

        Assertions.assertEquals("Order cannot be cancelled as it is already PROCESSING", exception.getMessage());
    }


    @Test
    void updateOrderStatusSuccessfully() {
        when(orderRepository.updateStatus(OrderStatus.PENDING, OrderStatus.PROCESSING)).thenReturn(3);

        int updatedCount = orderService.updateOrderStatus(OrderStatus.PENDING, OrderStatus.PROCESSING);

        Assertions.assertEquals(3, updatedCount);
        verify(orderRepository, times(1)).updateStatus(OrderStatus.PENDING, OrderStatus.PROCESSING);
    }


}
