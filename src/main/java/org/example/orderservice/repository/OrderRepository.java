package org.example.orderservice.repository;


import org.example.orderservice.entities.Order;
import org.example.orderservice.entities.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Modifying
    @Query("update Order o set o.status = :newStatus where o.status = :currentStatus")
    int updateStatus(@Param("currentStatus") OrderStatus currentStatus,
                          @Param("newStatus") OrderStatus newStatus);

    List<Order> findOrderByStatus(OrderStatus status);
}

