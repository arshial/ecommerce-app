package com.example.webdisgn.util;

import com.example.webdisgn.dto.response.OrderItemResponse;
import com.example.webdisgn.model.OrderItem;

public class MapperOrderItem {

    public static OrderItemResponse toResponse(OrderItem item) {
        OrderItemResponse dto = new OrderItemResponse();
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPriceAtPurchase(item.getPriceAtPurchase());
        dto.setSubtotal(item.getQuantity() * item.getPriceAtPurchase());
        return dto;
    }
}
