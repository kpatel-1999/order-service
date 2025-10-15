package org.example.orderservice.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "customer_id")
    private String customerId;


    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    public Long getId() {
        return id;
    }

    public Order(String customerId, List<OrderItem> items) {
        this.customerId = customerId;
        this.items = items;
    }

    public Order(String customerId) {
        this.customerId = customerId;
    }

    public Order() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public BigDecimal calculateTotalAmount() {
        return items.stream()
                .map(item -> item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.stream()
                .filter(existing -> existing.getProductId() == item.getProductId())
                .findFirst()
                .ifPresentOrElse(existing -> {
                    existing.setQuantity(existing.getQuantity() + item.getQuantity());
                }, () -> this.items.add(item));
    }
    public void addItems(List<OrderItem> items) {
        items.forEach(this::addItem);
    }
}
