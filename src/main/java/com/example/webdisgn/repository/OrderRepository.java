package com.example.webdisgn.repository;

import com.example.webdisgn.model.Order;
import com.example.webdisgn.model.OrderStatus;
import com.example.webdisgn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUser(User user);
    List<Order> findByUserAndStatus(User user, OrderStatus status);
    long countByStatus(OrderStatus status);
    List<Order> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = :status")
    List<Order> findByDateRangeAndStatus(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         @Param("status") OrderStatus status);



}
