package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.model.*;
import com.example.webdisgn.repository.*;
import com.example.webdisgn.service.CouponService;
import com.example.webdisgn.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CouponService couponService;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Override
    public String createCheckoutSession(String username, String couponCode) {
        User user = userRepository.findByNameAndDeletedFalse(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Carrello non trovato"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Il carrello è vuoto");
        }

        List<SessionCreateParams.LineItem> lineItems = cart.getItems().stream()
                .map(this::toLineItem)
                .toList();

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .addAllLineItem(lineItems);

        // Se è stato inserito un codice coupon, aggiungilo alla sessione Stripe
        if (couponCode != null && !couponCode.isBlank()) {
            String stripeCouponId = couponService.syncCouponToStripe(couponCode);
            paramsBuilder.addDiscount(
                    SessionCreateParams.Discount.builder()
                            .setCoupon(stripeCouponId)
                            .build()
            );
        }

        try {
            Session session = Session.create(paramsBuilder.build());

            // Salva ordine PENDING nel DB collegato al sessionId di Stripe
            Order order = new Order();
            order.setId(session.getId()); // uso sessionId come ID ordine
            order.setUser(user);
            order.setStatus(OrderStatus.PENDING);
            order.setCreatedAt(LocalDateTime.now());
            order.setItems(new ArrayList<>());

            double total = 0;
            for (CartItem item : cart.getItems()) {
                Product product = item.getProduct();

                if (item.getQuantity() > product.getStock()) {
                    throw new RuntimeException("Stock insufficiente per: " + product.getName());
                }

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(item.getQuantity());
                orderItem.setPriceAtPurchase(product.getPrice());
                order.getItems().add(orderItem);

                total += item.getQuantity() * product.getPrice();
            }

            order.setTotal(total);

            if (couponCode != null && !couponCode.isBlank()) {
                order.setCouponCode(couponCode.toUpperCase());
            }

            orderRepository.save(order);

            return session.getUrl();

        } catch (StripeException e) {
            throw new RuntimeException("Errore nella creazione della sessione Stripe", e);
        }
    }

    private SessionCreateParams.LineItem toLineItem(CartItem item) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity((long) item.getQuantity())
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount((long) (item.getProduct().getPrice() * 100))
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(item.getProduct().getName())
                                                .build())
                                .build()
                )
                .build();
    }
}
