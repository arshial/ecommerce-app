package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.dto.request.CouponRequest;
import com.example.webdisgn.dto.response.CouponResponse;
import com.example.webdisgn.exeption.GlobalExceptionHandler.ResourceNotFoundException;
import com.example.webdisgn.model.Coupon;
import com.example.webdisgn.model.DiscountType;
import com.example.webdisgn.repository.CouponRepository;
import com.example.webdisgn.service.CouponService;
import com.stripe.exception.StripeException;
import com.stripe.param.CouponCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Il codice coupon esiste già.");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode().toUpperCase());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setExpiresAt(request.getExpiresAt());
        coupon.setSingleUse(request.isSingleUse());
        coupon.setMaxUses(request.getMaxUses());
        coupon.setActive(request.isActive());

        Coupon saved = couponRepository.save(coupon);
        log.info("🎫 Coupon creato: {}", saved.getCode());

        return toResponse(saved);
    }

    @Override
    public CouponResponse validateCoupon(String code) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon non trovato"));

        if (!coupon.isActive()) {
            throw new RuntimeException("Coupon disattivato.");
        }

        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Coupon scaduto.");
        }

        if (coupon.isSingleUse() && coupon.isUsed()) {
            throw new RuntimeException("Coupon già utilizzato.");
        }

        if (coupon.getUsedCount() >= coupon.getMaxUses()) {
            throw new RuntimeException("Numero massimo di utilizzi raggiunto.");
        }

        return toResponse(coupon);
    }

    private CouponResponse toResponse(Coupon coupon) {
        CouponResponse response = new CouponResponse();
        response.setId(coupon.getId());
        response.setCode(coupon.getCode());
        response.setDiscountType(coupon.getDiscountType());
        response.setDiscountValue(coupon.getDiscountValue());
        response.setExpiresAt(coupon.getExpiresAt());
        response.setSingleUse(coupon.isSingleUse());
        response.setUsedCount(coupon.getUsedCount());
        response.setMaxUses(coupon.getMaxUses());
        response.setUsed(coupon.isUsed());
        response.setActive(coupon.isActive());
        return response;
    }

    @Override
    public String syncCouponToStripe(String code) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon non trovato"));

        try {
            // Prova a recuperare il coupon da Stripe
            com.stripe.model.Coupon stripeCoupon = com.stripe.model.Coupon.retrieve(code.toLowerCase());
            return stripeCoupon.getId();

        } catch (Exception e) {
            log.info("➡️ Creazione coupon su Stripe: {}", code);

            CouponCreateParams.Builder builder = CouponCreateParams.builder()
                    .setId(code.toLowerCase())
                    .setName("Sconto " + code)
                    .setCurrency("eur")
                    .setDuration(CouponCreateParams.Duration.ONCE);

            if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
                builder.setPercentOff(BigDecimal.valueOf(coupon.getDiscountValue()));
            } else if (coupon.getDiscountType() == DiscountType.FIXED) {
                builder.setAmountOff((long) (coupon.getDiscountValue() * 100));
            }

            try {
                com.stripe.model.Coupon newStripeCoupon = com.stripe.model.Coupon.create(builder.build());
                return newStripeCoupon.getId();
            } catch (StripeException ex) {
                throw new RuntimeException("Errore creazione coupon Stripe: " + ex.getMessage());
            }
        }
    }
}
