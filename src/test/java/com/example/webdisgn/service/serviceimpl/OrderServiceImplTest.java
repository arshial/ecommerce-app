package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.dto.response.OrderResponse;
import com.example.webdisgn.model.*;
import com.example.webdisgn.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private JavaMailSender mailSender;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Cart cart;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setName("testUser");
        user.setEmail("user@example.com");

        product = new Product();
        product.setId(1L);
        product.setName("Mouse");
        product.setPrice(50.0);
        product.setStock(10);

        cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        cart = new Cart();
        cart.setUser(user);
        cart.setItems(List.of(cartItem));
    }

    @Test
    void checkout_withValidCart_shouldCreateOrder() {
        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId("ORD123");
            return o;
        });

        OrderResponse response = orderService.checkout("testUser", null);

        assertThat(response).isNotNull();
        assertThat(response.getTotal()).isEqualTo(100.0);
        verify(productRepository).save(any());
        verify(orderRepository).save(any());
        verify(cartItemRepository).deleteAllByCart(cart);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void getOrdersByUser_shouldReturnList() {
        Order order = new Order();
        order.setId("1");
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);
        order.setItems(new ArrayList<>()); // evita NPE

        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(orderRepository.findByUser(user)).thenReturn(List.of(order));

        List<OrderResponse> list = orderService.getOrdersByUser("testUser");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo("1");
    }

    @Test
    void getOrdersByUserAndStatus_shouldReturnFiltered() {
        Order order = new Order();
        order.setId("1");
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);
        order.setItems(new ArrayList<>());

        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(orderRepository.findByUserAndStatus(user, OrderStatus.PAID)).thenReturn(List.of(order));

        List<OrderResponse> list = orderService.getOrdersByUserAndStatus("testUser", OrderStatus.PAID);

        assertThat(list).hasSize(1);
        OrderStatus status = OrderStatus.valueOf(list.get(0).getStatus());
        assertThat(status).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void updateOrderStatus_shouldChangeStatus() {
        Order order = new Order();
        order.setId("ORD123");
        order.setStatus(OrderStatus.PAID);
        order.setItems(new ArrayList<>());

        when(orderRepository.findById("ORD123")).thenReturn(Optional.of(order));

        orderService.updateOrderStatus("ORD123", OrderStatus.SHIPPED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        verify(orderRepository).save(order);
    }

    @Test
    void getOrdersByDateRange_shouldReturnList() {
        Order order = new Order();
        order.setId("ORD456");
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PAID);
        order.setItems(new ArrayList<>());

        when(orderRepository.findByDateRangeAndStatus(any(), any(), eq(OrderStatus.PAID)))
                .thenReturn(List.of(order));

        List<OrderResponse> list = orderService.getOrdersByDateRange(
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now(),
                OrderStatus.PAID
        );

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo("ORD456");
    }

    @Test
    void checkout_shouldThrowIfCartEmpty() {
        cart.setItems(new ArrayList<>());

        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.checkout("testUser", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Il carrello è vuoto");
    }
}
