package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.dto.request.UserRequest;
import com.example.webdisgn.dto.response.UserResponse;
import com.example.webdisgn.model.Role;
import com.example.webdisgn.model.User;
import com.example.webdisgn.repository.UserRepository;
import com.example.webdisgn.repository.VerificationTokenRepository;
import com.example.webdisgn.service.AuditService;
import com.example.webdisgn.service.RefreshTokenService;
import com.example.webdisgn.util.JwtUtil;
import com.example.webdisgn.util.MapperUser;
import com.example.webdisgn.util.RateLimiterService;
import com.example.webdisgn.util.RequestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JavaMailSender mailSender;
    @Mock private JwtUtil jwtUtil;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private AuditService auditService;
    @Mock private VerificationTokenRepository verificationTokenRepository;
    @Mock private RateLimiterService rateLimiterService;
    @Mock private RequestUtil requestUtil;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Simulazione di un utente loggato in SecurityContextHolder
        var authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testUser");
        var securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // Test per registrazione utente
    @Test
    void testSignUpSuccess() {
        UserRequest request = new UserRequest();
        request.setName("test");
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.findByName("test")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encoded");

        User user = MapperUser.toEntity(request);
        user.setId(UUID.randomUUID().toString());

        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.signUp(request);

        assertEquals("test", response.getName());
        assertEquals("test@example.com", response.getEmail());
        verify(userRepository).save(any(User.class));  // Verifica che il repository abbia salvato l'utente
    }

    // Test per tentativo di registrazione con nome già in uso
    @Test
    void testSignUpUserAlreadyExists() {
        UserRequest request = new UserRequest();
        request.setName("test");
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.findByName("test")).thenReturn(Optional.of(new User()));  // Simula che l'utente esiste già

        assertThrows(RuntimeException.class, () -> userService.signUp(request));  // Dovrebbe lanciare eccezione
    }

    // Test per recuperare tutti gli utenti
    @Test
    void testGetAllUsers() {
        User user = new User();
        user.setId("1");
        user.setName("test");
        user.setEmail("test@example.com");

        when(userRepository.findByDeletedFalse()).thenReturn(Collections.singletonList(user));

        List<UserResponse> users = userService.getAllUsers();
        assertEquals(1, users.size());
        assertEquals("test", users.get(0).getName());
        assertEquals("test@example.com", users.get(0).getEmail());
    }

    // Test per eliminazione soft dell'utente
    @Test
    void testDeleteUser() {
        User user = new User();
        user.setId("1");
        user.setName("test");

        when(userRepository.findByIdAndDeletedFalse("1")).thenReturn(Optional.of(user));

        userService.deleteUser("1");

        verify(userRepository).save(user);  // Verifica che l'utente sia stato salvato con 'deleted = true'
        assertTrue(user.isDeleted());  // Verifica che 'deleted' sia effettivamente true
    }

    // Test per promuovere un utente a admin
    @Test
    void testPromoteToAdmin() {
        User user = new User();
        user.setId("1");
        user.setRole(Role.USER);

        when(userRepository.findByIdAndDeletedFalse("1")).thenReturn(Optional.of(user));

        userService.promoteToAdmin("1");

        assertEquals(Role.ADMIN, user.getRole());  // Verifica che il ruolo sia cambiato in 'ADMIN'
        verify(userRepository).save(user);  // Verifica che il repository abbia salvato l'utente
    }

    // Test per promuovere un utente che non esiste
    @Test
    void testPromoteToAdminNotFound() {
        when(userRepository.findByIdAndDeletedFalse("1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.promoteToAdmin("1"));  // Verifica che l'utente non esiste
    }

    // Test per ottenere un utente per ID
    @Test
    void testGetUserById() {
        User user = new User();
        user.setId("1");
        user.setName("test");
        user.setEmail("test@example.com");
        user.setDeleted(false);  // Assicurati che l'utente non sia eliminato

        when(userRepository.findByIdAndDeletedFalse("1")).thenReturn(Optional.of(user));

        // Usa il metodo esistente `getUserByIdOrName`
        UserResponse response = userService.getUserByIdOrName("1", null);

        assertNotNull(response);
        assertEquals("test", response.getName());
        assertEquals("test@example.com", response.getEmail());
    }


    // Test per ottenere un utente che non esiste
    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserByIdOrName("2", null));  // Verifica che l'utente non esiste
    }
}
