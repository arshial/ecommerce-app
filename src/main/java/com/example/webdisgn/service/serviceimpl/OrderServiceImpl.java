package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.dto.response.OrderResponse;
import com.example.webdisgn.exeption.GlobalExceptionHandler;
import com.example.webdisgn.model.*;
import com.example.webdisgn.repository.*;
import com.example.webdisgn.service.OrderService;
import com.example.webdisgn.util.MapperOrder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final JavaMailSender mailSender;

    @Override
    @Transactional
    public OrderResponse checkout(String username, String couponCode) {
        User user = userRepository.findByNameAndDeletedFalse(username)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Utente non trovato"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Carrello non trovato"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Il carrello è vuoto");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);
        order.setItems(new ArrayList<>());

        double total = 0;
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (cartItem.getQuantity() > product.getStock()) {
                throw new RuntimeException("Quantità non disponibile per il prodotto: " + product.getName());
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(cartItem.getQuantity());
            item.setPriceAtPurchase(product.getPrice());
            total += cartItem.getQuantity() * product.getPrice();
            order.getItems().add(item);

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        if (couponCode != null && !couponCode.isBlank()) {
            Coupon coupon = couponRepository.findByCode(couponCode.toUpperCase())
                    .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Coupon non trovato"));

            if (!coupon.isActive()) throw new RuntimeException("Coupon non attivo");
            if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now()))
                throw new RuntimeException("Coupon scaduto");
            if (coupon.isSingleUse() && coupon.isUsed()) throw new RuntimeException("Coupon già usato");
            if (coupon.getUsedCount() >= coupon.getMaxUses()) throw new RuntimeException("Coupon già utilizzato troppe volte");

            if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
                total -= total * (coupon.getDiscountValue() / 100);
            } else if (coupon.getDiscountType() == DiscountType.FIXED) {
                total -= coupon.getDiscountValue();
            }

            total = Math.max(total, 0);

            coupon.setUsedCount(coupon.getUsedCount() + 1);
            if (coupon.isSingleUse() || coupon.getUsedCount() >= coupon.getMaxUses()) {
                coupon.setUsed(true);
                coupon.setActive(false);
            }
            couponRepository.save(coupon);

            order.setCouponCode(coupon.getCode());
        }

        order.setTotal(total);
        Order saved = orderRepository.save(order);

        cartItemRepository.deleteAllByCart(cart);
        cart.setItems(new ArrayList<>());
        cart.setTotal(0);
        cartRepository.save(cart);

        sendConfirmationEmail(user, saved);

        return MapperOrder.toResponse(saved);
    }

    @Override
    public List<OrderResponse> getOrdersByUser(String username) {
        User user = userRepository.findByNameAndDeletedFalse(username)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Utente non trovato"));
        return orderRepository.findByUser(user).stream()
                .map(MapperOrder::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByUserAndStatus(String username, OrderStatus status) {
        User user = userRepository.findByNameAndDeletedFalse(username)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Utente non trovato"));
        return orderRepository.findByUserAndStatus(user, status).stream()
                .map(MapperOrder::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Ordine non trovato"));

        OrderStatus current = order.getStatus();

        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) {
            throw new RuntimeException("Ordine già completato o annullato. Stato non modificabile.");
        }

        if (current == OrderStatus.PAID && newStatus == OrderStatus.PENDING) {
            throw new RuntimeException("Impossibile tornare da PAID a PENDING.");
        }

        if (current == OrderStatus.SHIPPED && newStatus == OrderStatus.PAID) {
            throw new RuntimeException("Impossibile tornare da SHIPPED a PAID.");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
        log.info("Ordine {} aggiornato da {} a {}", orderId, current, newStatus);
    }

    private void sendConfirmationEmail(User user, Order order) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(user.getEmail());
        msg.setSubject("Conferma Ordine");
        msg.setText("Grazie per il tuo ordine!\n\nID: " + order.getId() +
                "\nTotale: €" + order.getTotal() +
                "\nStato: " + order.getStatus() +
                (order.getCouponCode() != null ? "\nCoupon applicato: " + order.getCouponCode() : ""));
        mailSender.send(msg);
        log.info("📧 Email conferma ordine inviata a {}", user.getEmail());
    }

    @Override
    public List<OrderResponse> getOrdersByDateRange(LocalDateTime start, LocalDateTime end, OrderStatus status) {
        List<Order> orders = orderRepository.findByDateRangeAndStatus(start, end, status);
        return orders.stream()
                .map(MapperOrder::toResponse)  // MapperOrder è il tuo mapper che converte Order a OrderResponse
                .collect(Collectors.toList());
    }

}

