package com.example.autthentication2.dto;

import com.example.autthentication2.entity.AppUser;
import com.example.autthentication2.entity.Role;

import java.util.stream.Collectors;

public record UserView(
        Long id,
        String username,
        String fullName,
        String email,
        boolean enabled,
        String rolesFormatted
) {
    public static UserView from(AppUser u) {
        String rolesStr = u.getRoles() != null
                ? u.getRoles().stream().map(Role::getName).collect(Collectors.joining(", "))
                : "";
        return new UserView(
                u.getId(),
                u.getUsername(),
                u.getFullName() != null ? u.getFullName() : "",
                u.getEmail() != null ? u.getEmail() : "",
                u.isEnabled(),
                rolesStr
        );
    }
}
