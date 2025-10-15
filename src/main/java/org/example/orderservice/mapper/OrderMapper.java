package org.example.orderservice.mapper;

import org.example.orderservice.dto.request.CreateOrderRequest;
import org.example.orderservice.dto.request.OrderItemRequest;
import org.example.orderservice.dto.response.CreateOrderResponse;
import org.example.orderservice.dto.response.OrderItemResponse;
import org.example.orderservice.dto.response.OrderResponse;
import org.example.orderservice.entities.Order;
import org.example.orderservice.entities.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public Order toEntity(CreateOrderRequest createOrderRequest, String customerId) {
        final Order order = new Order(customerId);
        final List<OrderItem> orderItems = createOrderRequest.items()
                .stream()
                .map(this::toEntity)
                .toList();

        order.addItems(orderItems);
        return order;
    }

    private OrderItem toEntity(OrderItemRequest itemRequest) {
        return new OrderItem(
                itemRequest.productId(),
                itemRequest.productName(),
                itemRequest.productQuantity(),
                itemRequest.productPrice()
        );
    }

    public CreateOrderResponse toCreateOrderResponse(Order order){
        return  new CreateOrderResponse(order.getId());
    }


    public OrderResponse toGetOrderResponse(Order order){
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductName(),
                        item.getProductPrice(),
                        item.getQuantity(),
                        item.getProductId()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.calculateTotalAmount(),
                order.getCreatedAt(),
                items
        );
    }
}
