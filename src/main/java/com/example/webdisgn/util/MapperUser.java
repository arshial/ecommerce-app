package com.example.webdisgn.util;

import com.example.webdisgn.dto.request.UserRequest;
import com.example.webdisgn.dto.response.UserResponse;
import com.example.webdisgn.model.User;

public class MapperUser {

    public static User toEntity(UserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        return user;
    }

    public static UserResponse toResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
