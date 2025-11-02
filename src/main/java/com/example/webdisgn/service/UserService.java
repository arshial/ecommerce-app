package com.example.webdisgn.service;

import com.example.webdisgn.dto.request.ChangePasswordRequest;
import com.example.webdisgn.dto.request.UserRequest;
import com.example.webdisgn.dto.response.AuthResponse;
import com.example.webdisgn.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {
    AuthResponse login(String name, String password, HttpServletRequest request);
    void forgotPassword(String email, HttpServletRequest request);
    void changePassword(String username, ChangePasswordRequest changeRequest, HttpServletRequest httpRequest);
    UserResponse signUp(UserRequest request);
    List<UserResponse> getAllUsers();
    UserResponse getUserByIdOrName(String id, String name);
    void deleteUser(String id);
    void promoteToAdmin(String id);
    void logout(String refreshToken);
}
