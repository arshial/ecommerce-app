package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.dto.response.AdminAnalyticsResponse;
import com.example.webdisgn.dto.response.AdminDashboardResponse;
import com.example.webdisgn.model.*;
import com.example.webdisgn.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceImplTest {

    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Product product1 = new Product();
        product1.setName("Mouse");
        product1.setPrice(29.99);
        Category cat1 = new Category();
        cat1.setName("Accessori");
        product1.setCategory(cat1);

        OrderItem item1 = new OrderItem();
        item1.setProduct(product1);
        item1.setQuantity(2);
        item1.setPriceAtPurchase(29.99);

        order1 = new Order();
        order1.setId("o1");
        order1.setStatus(OrderStatus.PAID);
        order1.setTotal(59.98);
        order1.setCreatedAt(LocalDateTime.of(2024, 4, 15, 12, 0));
        order1.setItems(List.of(item1));

        order2 = new Order();
        order2.setId("o2");
        order2.setStatus(OrderStatus.CANCELLED);
        order2.setTotal(0.0);
        order2.setCreatedAt(LocalDateTime.of(2024, 4, 15, 14, 0));
        order2.setItems(new ArrayList<>());
    }

    @Test
    void getDashboardStats_shouldReturnAggregatedStats() {
        when(orderRepository.count()).thenReturn(2L);
        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));
        when(orderRepository.countByStatus(OrderStatus.PAID)).thenReturn(1L);
        when(orderRepository.countByStatus(OrderStatus.CANCELLED)).thenReturn(1L);
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(0L);
        when(orderRepository.countByStatus(OrderStatus.SHIPPED)).thenReturn(0L);
        when(orderRepository.countByStatus(OrderStatus.DELIVERED)).thenReturn(0L);

        AdminDashboardResponse result = adminService.getDashboardStats();

        assertThat(result.getTotalOrders()).isEqualTo(2);
        assertThat(result.getTotalRevenue()).isEqualTo(59.98);
        assertThat(result.getOrdersByStatus().get("PAID")).isEqualTo(1L);
        assertThat(result.getOrdersByStatus().get("CANCELLED")).isEqualTo(1L);
    }

    @Test
    void getAnalytics_shouldReturnDetailedStats() {
        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

        AdminAnalyticsResponse result = adminService.getAnalytics();

        assertThat(result.getOrdersPerDay()).containsEntry("2024-04-15", 1L);
        assertThat(result.getOrdersPerMonth()).containsEntry("2024-04", 1L);
        assertThat(result.getTopProductName()).isEqualTo("Mouse");
        assertThat(result.getTopProductSold()).isEqualTo(2L);
        assertThat(result.getQuantityPerCategory()).containsEntry("Accessori", 2L);
        assertThat(result.getTotalRevenue()).isEqualTo(59.98);
    }
}
