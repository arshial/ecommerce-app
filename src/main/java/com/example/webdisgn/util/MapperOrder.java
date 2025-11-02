package com.example.webdisgn.util;

import com.example.webdisgn.dto.response.OrderItemResponse;
import com.example.webdisgn.dto.response.OrderResponse;
import com.example.webdisgn.model.Order;

import java.util.List;
import java.util.stream.Collectors;

public class MapperOrder {

    public static OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setTotal(order.getTotal());
        response.setCreatedAt(order.getCreatedAt());
        response.setStatus(order.getStatus().name());

        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(MapperOrderItem::toResponse)
                .collect(Collectors.toList());

        response.setItems(itemResponses);
        return response;
    }
}
