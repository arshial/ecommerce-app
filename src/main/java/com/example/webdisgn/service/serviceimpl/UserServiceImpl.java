package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.dto.request.ChangePasswordRequest;
import com.example.webdisgn.dto.request.UserRequest;
import com.example.webdisgn.dto.response.AuthResponse;
import com.example.webdisgn.dto.response.UserResponse;
import com.example.webdisgn.exeption.*;
import com.example.webdisgn.model.InvalidatedToken;
import com.example.webdisgn.model.Role;
import com.example.webdisgn.model.User;
import com.example.webdisgn.model.VerificationToken;
import com.example.webdisgn.repository.InvalidatedTokenRepository;
import com.example.webdisgn.repository.RefreshTokenRepository;
import com.example.webdisgn.repository.UserRepository;
import com.example.webdisgn.repository.VerificationTokenRepository;
import com.example.webdisgn.service.AuditService;
import com.example.webdisgn.service.RefreshTokenService;
import com.example.webdisgn.service.UserService;
import com.example.webdisgn.util.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RateLimiterService rateLimiterService;
    private final RequestUtil requestUtil;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JavaMailSender mailSender,
                           JwtUtil jwtUtil,
                           RefreshTokenService refreshTokenService,
                           AuditService auditService,
                           VerificationTokenRepository verificationTokenRepository,
                           RateLimiterService rateLimiterService,
                           RequestUtil requestUtil,
                           InvalidatedTokenRepository invalidatedTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.auditService = auditService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.rateLimiterService = rateLimiterService;
        this.requestUtil = requestUtil;
        this.invalidatedTokenRepository = invalidatedTokenRepository;
    }

    @Override
    public UserResponse signUp(UserRequest request) {
        if (userRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Nome utente già in uso");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email già registrata");
        }
        // Mappa i campi base
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        // Codifichiamo la password qui (non nel mapper!)
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(Role.USER); // default
        newUser.setEnabled(false);
        newUser.setDeleted(false);
        userRepository.save(newUser);

        // Genera token di verifica email
        String token = UUID.randomUUID().toString();
        VerificationToken verification = new VerificationToken();
        verification.setToken(token);
        verification.setUser(newUser);
        verificationTokenRepository.save(verification);

        // Invia mail di conferma
        String link = "http://localhost:8080/api/users/verify?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(newUser.getEmail());
        message.setSubject("Conferma registrazione");
        message.setText("Clicca qui per attivare il tuo account:\n" + link);
        mailSender.send(message);

        log.info("Nuovo utente registrato: {}", newUser.getEmail());
        return MapperUser.toResponse(newUser);
    }

    @Override
    public AuthResponse login(String name, String password, HttpServletRequest request) {
        User user = userRepository.findByNameAndDeletedFalse(name)
                .orElseThrow(() -> new RuntimeException("Utente non trovato o eliminato: " + name));
        if (!user.isEnabled()) {
            throw new RuntimeException("Email non verificata, impossibile effettuare il login.");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Password errata per utente: {}", name);
            throw new RuntimeException("Credenziali non valide.");
        }
        String ip = requestUtil.getClientIp(request);
        String userAgent = requestUtil.getUserAgent(request);
        log.info("Login da IP: {}, Device: {}", ip, userAgent);

        String token = jwtUtil.generateToken(user.getName(), user.getRole().name());
        var refreshToken = refreshTokenService.createRefreshToken(user);

        audit(user.getName(), "LOGIN", "Login da IP: " + ip);
        return new AuthResponse(token, refreshToken.getToken(), user.getName(), user.getRole().name());
    }

    @Override
    public void forgotPassword(String email, HttpServletRequest request) {
        var bucket = rateLimiterService.resolveBucket(email);
        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit superato per forgot-password su: {}", email);
            throw new RuntimeException("Hai superato il numero massimo di richieste. Riprova tra qualche minuto.");
        }
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato o eliminato: " + email));
        String ip = requestUtil.getClientIp(request);
        String userAgent = requestUtil.getUserAgent(request);
        log.info("Reset password richiesto da IP: {}, Device: {}", ip, userAgent);

        String newPassword = generateRandomPassword(10);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        sendResetEmail(user, newPassword);
        audit(user.getName(), "RESET_PASSWORD", email);
    }

    @Override
    public void changePassword(String username, ChangePasswordRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByNameAndDeletedFalse(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato o eliminato: " + username));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("La password attuale non è corretta.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        String ip = requestUtil.getClientIp(httpRequest);
        String agent = requestUtil.getUserAgent(httpRequest);
        log.info("Cambio password per {} da IP: {}, Device: {}", username, ip, agent);
        audit(username, "CHANGE_PASSWORD", ip);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.info("Recupero utenti (solo non eliminati)");
        return userRepository.findByDeletedFalse().stream()
                .map(MapperUser::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserByIdOrName(String id, String name) {
        if (id != null) {
            return userRepository.findByIdAndDeletedFalse(id)
                    .map(MapperUser::toResponse)
                    .orElseThrow(() -> new RuntimeException("Utente non trovato o eliminato con ID: " + id));
        } else if (name != null) {
            return userRepository.findByNameAndDeletedFalse(name)
                    .map(MapperUser::toResponse)
                    .orElseThrow(() -> new RuntimeException("Utente non trovato o eliminato con name: " + name));
        } else {
            throw new IllegalArgumentException("Devi specificare id o name.");
        }
    }

    @Override
    public void deleteUser(String id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Utente non trovato o già eliminato: " + id));
        user.setDeleted(true);
        userRepository.save(user);
        log.warn("Soft delete eseguito sull'utente con ID: {}", id);
        audit(currentUser(), "SOFT_DELETE_USER", id);
    }

    @Override
    public void promoteToAdmin(String id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Utente non trovato o eliminato con ID: " + id));
        if (user.getRole() == Role.ADMIN) {
            log.info("Utente {} è già ADMIN", user.getName());
            return;
        }
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        log.info("Utente {} promosso ad ADMIN", user.getName());
        audit(currentUser(), "PROMOTE_TO_ADMIN", user.getName());
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        return sb.toString();
    }

    private void sendResetEmail(User user, String newPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Password Reset");
        message.setText("Ciao " + user.getName() + ",\n\n"
                + "La tua nuova password temporanea è: " + newPassword + "\n"
                + "Ti consigliamo di cambiarla subito dopo il login.\n\n"
                + "Saluti,\nIl Team di Webdisgn");
        mailSender.send(message);
        log.debug("Email inviata a {} con nuova password.", user.getEmail());
    }
    public void logout(String refreshToken) {
        if (refreshTokenService.verifyToken(refreshToken)) {
            InvalidatedToken invalidated = new InvalidatedToken();
            invalidated.setToken(refreshToken);
            invalidatedTokenRepository.save(invalidated);

            refreshTokenService.deleteByToken(refreshToken);
            log.info("✅ Refresh token invalidato al logout.");
        } else {
            throw new RuntimeException("Token non valido o già usato.");
        }
    }


    private void audit(String username, String action, String target) {
        auditService.log(username, action, target);
    }

    private String currentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
