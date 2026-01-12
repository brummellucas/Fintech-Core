package com.gateway.payment.service;

import com.gateway.payment.domain.entity.Account;
import com.gateway.payment.domain.entity.User;
import com.gateway.payment.domain.enums.Role;
import com.gateway.payment.dto.auth.LoginRequest;
import com.gateway.payment.dto.auth.LoginResponse;
import com.gateway.payment.dto.auth.RegisterRequest;
import com.gateway.payment.exception.BusinessException;
import com.gateway.payment.repository.AccountRepository;
import com.gateway.payment.repository.UserRepository;
import com.gateway.payment.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);

        // Cria conta automaticamente para merchants
        if (savedUser.getRole() == Role.MERCHANT || savedUser.getRole() == Role.CLIENT) {
            Account account = Account.builder()
                    .user(savedUser)
                    .balance(BigDecimal.ZERO)
                    .build();
            accountRepository.save(account);
        }
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String jwt = jwtTokenProvider.generateToken(authentication);
        User user = (User) authentication.getPrincipal();

        return new LoginResponse(
                jwt,
                user.getEmail(),
                user.getRole(),
                user.getName()
        );
    }
}