package com.gateway.payment.dto.auth;

import com.gateway.payment.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String email;
    private Role role;
    private String name;
}