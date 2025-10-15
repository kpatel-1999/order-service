package org.example.orderservice.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private int productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_quantity", nullable = false)
    private int quantity;

    @Column(name = "product_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal productPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public OrderItem(int productId, String productName, int quantity, BigDecimal productPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.productPrice = productPrice;
    }

    public OrderItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long itemId) {
        this.id = itemId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
