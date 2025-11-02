package com.example.webdisgn.controller;

import com.example.webdisgn.dto.request.ChangePasswordRequest;
import com.example.webdisgn.dto.request.LogoutRequest;
import com.example.webdisgn.dto.request.RefreshTokenRequest;
import com.example.webdisgn.dto.request.UserRequest;
import com.example.webdisgn.dto.response.AuthResponse;
import com.example.webdisgn.dto.response.UserResponse;
import com.example.webdisgn.model.RefreshToken;
import com.example.webdisgn.model.User;
import com.example.webdisgn.repository.RefreshTokenRepository;
import com.example.webdisgn.repository.InvalidatedTokenRepository;
import com.example.webdisgn.service.RefreshTokenService;
import com.example.webdisgn.service.UserService;
import com.example.webdisgn.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/test-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String onlyAdmin() {
        return "✅ Accesso ADMIN riuscito!";
    }

    @PostMapping("/signup")
    public UserResponse signUp(@Valid @RequestBody UserRequest userDto) {
        return userService.signUp(userDto);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestParam String name,
                              @RequestParam String password,
                              HttpServletRequest request) {
        return userService.login(name, password, request);
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestParam String email,
                               HttpServletRequest request) {
        userService.forgotPassword(email, request);
    }

    @PutMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request,
                               HttpServletRequest httpRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.changePassword(username, request, httpRequest);
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/find")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse findUser(@RequestParam(required = false) String id,
                                 @RequestParam(required = false) String name) {
        return userService.getUserByIdOrName(id, name);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
    }

    @PutMapping("/promote/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void promoteToAdmin(@PathVariable String id) {
        userService.promoteToAdmin(id);
    }

    @PostMapping("/refresh")
    public AuthResponse refreshToken(@RequestBody RefreshTokenRequest request) {
        if (invalidatedTokenRepository.existsByToken(request.getRefreshToken())) {
            throw new RuntimeException("Refresh token non valido o già usato.");
        }

        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Token non trovato o già usato"));

        token = refreshTokenService.verifyExpiration(token);
        User user = token.getUser();
        String newJwt = jwtUtil.generateToken(user.getName(), user.getRole().name());
        return new AuthResponse(newJwt, token.getToken(), user.getName(), user.getRole().name());
    }

    @PostMapping("/logout")
    public void logout(@RequestBody LogoutRequest request) {
        userService.logout(request.getRefreshToken());
    }
}