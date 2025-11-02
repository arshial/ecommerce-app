package com.example.webdisgn.controller;

import com.example.webdisgn.dto.request.UpdateOrderStatusRequest;
import com.example.webdisgn.dto.response.OrderResponse;
import com.example.webdisgn.model.OrderStatus;
import com.example.webdisgn.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<OrderResponse> getMyOrders() {
        return orderService.getOrdersByUser(currentUsername());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateOrderStatus(@PathVariable String id, @RequestBody @Valid UpdateOrderStatusRequest request) {
        orderService.updateOrderStatus(id, request.getStatus());
    }

    @GetMapping(params = "status")
    @PreAuthorize("hasRole('USER')")
    public List<OrderResponse> getOrdersByStatus(@RequestParam OrderStatus status) {
        return orderService.getOrdersByUserAndStatus(currentUsername(), status);
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER')")
    public OrderResponse checkout(@RequestParam(required = false) String couponCode) {
        return orderService.checkout(currentUsername(), couponCode);
    }

    @GetMapping("/filter-by-date")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersByDateRange(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam("status") OrderStatus status) {

        List<OrderResponse> orders = orderService.getOrdersByDateRange(start, end, status);
        return ResponseEntity.ok(orders);
    }



}
