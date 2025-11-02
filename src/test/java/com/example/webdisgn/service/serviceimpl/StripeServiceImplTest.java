package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.model.*;
import com.example.webdisgn.repository.*;
import com.example.webdisgn.service.CouponService;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StripeServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private CartRepository cartRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private CouponService couponService;

    @InjectMocks
    private StripeServiceImpl stripeService;

    private User user;
    private Cart cart;
    private Product product;
    private CartItem item;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setName("testUser");

        product = new Product();
        product.setName("Tastiera");
        product.setPrice(49.99);
        product.setStock(10);

        item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);

        cart = new Cart();
        cart.setUser(user);
        cart.setItems(List.of(item));

        ReflectionTestUtils.setField(stripeService, "successUrl", "http://localhost/success");
        ReflectionTestUtils.setField(stripeService, "cancelUrl", "http://localhost/cancel");
    }

    @Test
    void createCheckoutSession_success() {
        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        Session fakeSession = mock(Session.class);
        when(fakeSession.getId()).thenReturn("sess_123");
        when(fakeSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/sess_123");

        try (MockedStatic<Session> mockedStatic = mockStatic(Session.class)) {
            mockedStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(fakeSession);

            String url = stripeService.createCheckoutSession("testUser", null);

            assertThat(url).isEqualTo("https://checkout.stripe.com/pay/sess_123");

            verify(orderRepository).save(argThat(order ->
                    order.getUser().equals(user)
                            && order.getStatus() == OrderStatus.PENDING
                            && order.getItems().size() == 1
                            && order.getItems().get(0).getProduct().getName().equals("Tastiera")
            ));
        }
    }

    @Test
    void createCheckoutSession_withCoupon_success() {
        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(couponService.syncCouponToStripe("SAVEMORE")).thenReturn("stripe-coupon-id");

        Session fakeSession = mock(Session.class);
        when(fakeSession.getId()).thenReturn("sess_456");
        when(fakeSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/sess_456");

        try (MockedStatic<Session> mockedStatic = mockStatic(Session.class)) {
            mockedStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(fakeSession);

            String url = stripeService.createCheckoutSession("testUser", "SAVEMORE");

            assertThat(url).isEqualTo("https://checkout.stripe.com/pay/sess_456");
            verify(couponService).syncCouponToStripe("SAVEMORE");
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Test
    void createCheckoutSession_shouldThrowIfUserNotFound() {
        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                stripeService.createCheckoutSession("testUser", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Utente non trovato");
    }

    @Test
    void createCheckoutSession_shouldThrowIfCartEmpty() {
        cart.setItems(List.of());
        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() ->
                stripeService.createCheckoutSession("testUser", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Il carrello è vuoto");
    }
}
