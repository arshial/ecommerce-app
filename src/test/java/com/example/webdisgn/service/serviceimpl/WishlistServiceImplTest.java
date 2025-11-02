package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.dto.response.ProductResponse;
import com.example.webdisgn.exeption.*;
import com.example.webdisgn.model.Product;
import com.example.webdisgn.model.User;
import com.example.webdisgn.model.Wishlist;
import com.example.webdisgn.repository.ProductRepository;
import com.example.webdisgn.repository.UserRepository;
import com.example.webdisgn.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WishlistServiceImplTest {

    @Mock private WishlistRepository wishlistRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setName("testUser");

        product = new Product();
        product.setId(1L);
        product.setName("Tastiera");
        product.setPrice(49.99);
    }

    @Test
    void addToWishlist_shouldAddSuccessfully() {
        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(wishlistRepository.existsByUserAndProduct(user, product)).thenReturn(false);

        wishlistService.addToWishlist("testUser", 1L);

        verify(wishlistRepository).save(any());
    }

    @Test
    void addToWishlist_shouldThrowIfAlreadyExists() {
        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(wishlistRepository.existsByUserAndProduct(user, product)).thenReturn(true);

        assertThatThrownBy(() -> wishlistService.addToWishlist("testUser", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Prodotto già nei preferiti.");
    }

    @Test
    void removeFromWishlist_shouldRemoveSuccessfully() {
        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));

        wishlistService.removeFromWishlist("testUser", 1L);

        verify(wishlistRepository).deleteByUserAndProduct(user, product);
    }

    @Test
    void getWishlist_shouldReturnListOfProducts() {
        Wishlist w = Wishlist.builder().user(user).product(product).build();

        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.of(user));
        when(wishlistRepository.findByUser(user)).thenReturn(List.of(w));

        List<ProductResponse> wishlist = wishlistService.getWishlist("testUser");

        assertThat(wishlist).hasSize(1);
        assertThat(wishlist.get(0).getName()).isEqualTo("Tastiera");
    }

    @Test
    void getWishlist_shouldThrowIfUserNotFound() {
        when(userRepository.findByNameAndDeletedFalse("testUser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.getWishlist("testUser"))
                .isInstanceOf(GlobalExceptionHandler.ResourceNotFoundException.class)
                .hasMessageContaining("Utente non trovato");
    }
}
