package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.dto.request.CartItemRequest;
import com.example.webdisgn.dto.request.CartRequest;
import com.example.webdisgn.dto.response.CartResponse;
import com.example.webdisgn.model.*;
import com.example.webdisgn.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setName("testUser");

        product = new Product();
        product.setId(1L);
        product.setName("Mouse");
        product.setPrice(29.99);
        product.setStock(10);

        cart = new Cart();
        cart.setId("1");
        cart.setUser(user);
        cart.setItems(new ArrayList<>());
        cart.setTotal(0.0);
    }

    @Test
    void addItem_shouldAddNewItem() {
        CartItemRequest req = new CartItemRequest();
        req.setProductId(1L);
        req.setQuantity(2);

        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = cartService.addItem("testUser", req);

        assertThat(response).isNotNull();
        assertThat(response.getTotal()).isEqualTo(59.98);
        assertThat(cart.getItems()).hasSize(1);
    }

    @Test
    void updateCart_shouldReplaceAllItems() {
        CartRequest req = new CartRequest();
        CartItemRequest itemReq = new CartItemRequest();
        itemReq.setProductId(1L);
        itemReq.setQuantity(3);
        req.setItems(List.of(itemReq));

        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = cartService.updateCart("testUser", req);

        assertThat(response.getTotal()).isEqualTo(3 * product.getPrice());
        assertThat(cart.getItems()).hasSize(1);
    }

    @Test
    void removeItem_shouldDeleteOneItem() {
        CartItem item = new CartItem();
        item.setId(100L);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(1);

        cart.getItems().add(item);

        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(item));

        cartService.removeItem("testUser", 100L);

        assertThat(cart.getItems()).isEmpty();
        verify(cartItemRepository).delete(item);
    }

    @Test
    void clearCart_shouldRemoveAllItems() {
        CartItem item1 = new CartItem();
        item1.setId(1L);
        item1.setProduct(product);
        item1.setCart(cart);
        item1.setQuantity(1);

        cart.getItems().add(item1);

        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        cartService.clearCart("testUser");

        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.getTotal()).isZero();
        verify(cartItemRepository).deleteAllByCart(cart);
    }

    @Test
    void getUserCart_shouldReturnCart() {
        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.getUserCart("testUser");

        assertThat(response).isNotNull();
        assertThat(response.getTotal()).isEqualTo(0.0);
    }

    @Test
    void getUserCart_shouldCreateIfNotFound() {
        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = cartService.getUserCart("testUser");

        assertThat(response).isNotNull();
        assertThat(response.getTotal()).isEqualTo(0.0);
        verify(cartRepository).save(any());
    }
}
