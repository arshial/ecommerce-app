package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.dto.request.CouponRequest;
import com.example.webdisgn.dto.response.CouponResponse;
import com.example.webdisgn.exeption.*;
import com.example.webdisgn.model.Coupon;
import com.example.webdisgn.model.DiscountType;
import com.example.webdisgn.repository.CouponRepository;
import com.stripe.param.CouponCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CouponServiceImplTest {

    @Mock private CouponRepository couponRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    private CouponRequest request;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new CouponRequest();
        request.setCode("SAVE10");
        request.setDiscountType(DiscountType.PERCENTAGE);
        request.setDiscountValue(10.0);
        request.setExpiresAt(LocalDateTime.now().plusDays(3));
        request.setSingleUse(false);
        request.setMaxUses(5);
        request.setActive(true);

        coupon = new Coupon();
        coupon.setId("c1");
        coupon.setCode("SAVE10");
        coupon.setDiscountType(DiscountType.PERCENTAGE);
        coupon.setDiscountValue(10.0);
        coupon.setExpiresAt(request.getExpiresAt());
        coupon.setSingleUse(false);
        coupon.setMaxUses(5);
        coupon.setUsed(false);
        coupon.setUsedCount(0);
        coupon.setActive(true);
    }

    @Test
    void createCoupon_shouldSaveAndReturnResponse() {
        when(couponRepository.existsByCode("SAVE10")).thenReturn(false);
        when(couponRepository.save(any())).thenReturn(coupon);

        CouponResponse res = couponService.createCoupon(request);

        assertThat(res.getCode()).isEqualTo("SAVE10");
        verify(couponRepository).save(any());
    }

    @Test
    void createCoupon_shouldThrowIfCodeExists() {
        when(couponRepository.existsByCode("SAVE10")).thenReturn(true);

        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("già");
    }

    @Test
    void validateCoupon_shouldReturnValid() {
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));

        CouponResponse res = couponService.validateCoupon("SAVE10");

        assertThat(res.getDiscountType()).isEqualTo(DiscountType.PERCENTAGE);
    }

    @Test
    void validateCoupon_shouldThrowIfExpired() {
        coupon.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.validateCoupon("SAVE10"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("scaduto");
    }

    @Test
    void validateCoupon_shouldThrowIfUsedOrInactive() {
        coupon.setUsed(true);
        coupon.setSingleUse(true);
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.validateCoupon("SAVE10"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("già utilizzato");
    }

    @Test
    void validateCoupon_shouldThrowIfNotFound() {
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.validateCoupon("SAVE10"))
                .isInstanceOf(GlobalExceptionHandler.ResourceNotFoundException.class);
    }

    @Test
    void syncCouponToStripe_shouldReturnExistingId() throws Exception {
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));

        com.stripe.model.Coupon fakeStripe = mock(com.stripe.model.Coupon.class);
        when(fakeStripe.getId()).thenReturn("stripe-save10");

        try (MockedStatic<com.stripe.model.Coupon> stripeMock = mockStatic(com.stripe.model.Coupon.class)) {
            stripeMock.when(() -> com.stripe.model.Coupon.retrieve("save10")).thenReturn(fakeStripe);

            String stripeId = couponService.syncCouponToStripe("SAVE10");

            assertThat(stripeId).isEqualTo("stripe-save10");
        }
    }

    @Test
    void syncCouponToStripe_shouldCreateIfNotExists() throws Exception {
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));

        com.stripe.model.Coupon createdCoupon = mock(com.stripe.model.Coupon.class);
        when(createdCoupon.getId()).thenReturn("new-coupon-id");

        try (MockedStatic<com.stripe.model.Coupon> stripeMock = mockStatic(com.stripe.model.Coupon.class)) {
            stripeMock.when(() -> com.stripe.model.Coupon.retrieve("save10")).thenThrow(new RuntimeException("not found"));
            stripeMock.when(() -> com.stripe.model.Coupon.create(any(CouponCreateParams.class)))
                    .thenReturn(createdCoupon);

            String result = couponService.syncCouponToStripe("SAVE10");

            assertThat(result).isEqualTo("new-coupon-id");
        }
    }
}
