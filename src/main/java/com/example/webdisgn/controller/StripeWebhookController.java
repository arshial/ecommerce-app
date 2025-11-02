package com.example.webdisgn.controller;

import com.example.webdisgn.model.Order;
import com.example.webdisgn.model.OrderStatus;
import com.example.webdisgn.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/stripe/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final OrderRepository orderRepository;
    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader(name = "Stripe-Signature") String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            Session session = extractSession(event);

            if (session == null) {
                return ResponseEntity.badRequest().body("❌ Sessione Stripe non valida");
            }

            String sessionId = session.getId();

            switch (event.getType()) {
                case "checkout.session.completed" ->
                        orderRepository.findById(sessionId).ifPresent(order -> {
                            if (order.getStatus() == OrderStatus.PENDING) {
                                order.setStatus(OrderStatus.PAID);
                                orderRepository.save(order);
                                sendConfirmationEmail(order);
                                log.info("✅ Ordine {} aggiornato a PAID", sessionId);
                            }
                        });

                case "checkout.session.expired" ->
                        orderRepository.findById(sessionId).ifPresent(order -> {
                            if (order.getStatus() == OrderStatus.PENDING) {
                                order.setStatus(OrderStatus.CANCELLED);
                                orderRepository.save(order);
                                log.warn("⚠️ Ordine {} annullato per scadenza sessione", sessionId);
                            }
                        });

                default -> log.info("⚠️ Evento ignorato: {}", event.getType());
            }

            return ResponseEntity.ok("✅ Webhook gestito con successo");

        } catch (SignatureVerificationException e) {
            log.error("❌ Firma Stripe non valida: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Firma non valida");
        } catch (Exception e) {
            log.error("❌ Errore durante la gestione del webhook: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Errore interno");
        }
    }

    private Session extractSession(Event event) {
        try {
            return objectMapper.convertValue(
                    event.getDataObjectDeserializer().getObject().orElse(null),
                    Session.class
            );
        } catch (Exception e) {
            log.error("❌ Errore nel parsing della session Stripe: {}", e.getMessage());
            return null;
        }
    }

    private void sendConfirmationEmail(Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(order.getUser().getEmail());
        message.setSubject("Conferma pagamento");

        StringBuilder body = new StringBuilder();
        body.append("Ciao ").append(order.getUser().getName()).append(",\n\n")
                .append("Grazie per il tuo ordine!\n")
                .append("Totale: €").append(order.getTotal()).append("\n")
                .append("Stato: ").append(order.getStatus());

        if (order.getCouponCode() != null) {
            body.append("\nCoupon applicato: ").append(order.getCouponCode());
        }

        message.setText(body.toString());
        mailSender.send(message);
    }
}
