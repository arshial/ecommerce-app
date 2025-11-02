package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.dto.response.AdminAnalyticsResponse;
import com.example.webdisgn.dto.response.AdminDashboardResponse;
import com.example.webdisgn.model.Order;
import com.example.webdisgn.model.OrderItem;
import com.example.webdisgn.model.OrderStatus;
import com.example.webdisgn.model.Product;
import com.example.webdisgn.repository.OrderRepository;
import com.example.webdisgn.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final OrderRepository orderRepository;

    // ✅ STATISTICHE GENERALI (totali + stato)
    @Override
    public AdminDashboardResponse getDashboardStats() {
        AdminDashboardResponse response = new AdminDashboardResponse();

        long totalOrders = orderRepository.count();

        double totalRevenue = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.PENDING && o.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getTotal)
                .sum();

        Map<String, Long> statusCount = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            long count = orderRepository.countByStatus(status);
            statusCount.put(status.name(), count);
        }

        response.setTotalOrders(totalOrders);
        response.setTotalRevenue(totalRevenue);
        response.setOrdersByStatus(statusCount);

        log.info("📊 Dashboard admin: {} ordini - €{}", totalOrders, totalRevenue);
        return response;
    }

    // ✅ ANALYTICS AVANZATE
    @Override
    public AdminAnalyticsResponse getAnalytics() {
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .toList();

        AdminAnalyticsResponse response = new AdminAnalyticsResponse();

        Map<String, Long> ordersPerDay = new HashMap<>();
        Map<String, Long> ordersPerMonth = new HashMap<>();
        Map<String, Long> quantityPerCategory = new HashMap<>();
        Map<String, Long> productSales = new HashMap<>();
        double totalRevenue = 0;

        for (Order order : orders) {
            // 📅 Date keys
            String dayKey = order.getCreatedAt().toLocalDate().toString(); // es: 2024-04-16
            String monthKey = order.getCreatedAt().getYear() + "-" + String.format("%02d", order.getCreatedAt().getMonthValue());

            ordersPerDay.merge(dayKey, 1L, Long::sum);
            ordersPerMonth.merge(monthKey, 1L, Long::sum);

            totalRevenue += order.getTotal();

            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                String productName = product.getName();
                String categoryName = product.getCategory().getName();

                productSales.merge(productName, (long) item.getQuantity(), Long::sum);
                quantityPerCategory.merge(categoryName, (long) item.getQuantity(), Long::sum);
            }
        }

        String topProduct = productSales.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nessuno");

        long topSold = productSales.getOrDefault(topProduct, 0L);

        response.setOrdersPerDay(ordersPerDay);
        response.setOrdersPerMonth(ordersPerMonth);
        response.setTopProductName(topProduct);
        response.setTopProductSold(topSold);
        response.setQuantityPerCategory(quantityPerCategory);
        response.setTotalRevenue(totalRevenue);

        log.info("📈 Analytics generate: Top: {} ({} pezzi) - Totale: €{}", topProduct, topSold, totalRevenue);
        return response;
    }
}