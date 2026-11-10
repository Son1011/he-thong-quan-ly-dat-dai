package com.landmanagement.controller;

import com.landmanagement.dto.request.ChangePasswordRequest;
import com.landmanagement.dto.request.LoginRequest;
import com.landmanagement.dto.response.MeResponse;
import com.landmanagement.dto.response.TokenResponse;
import com.landmanagement.entity.UserAccount;
import com.landmanagement.repository.UserRepository;
import com.landmanagement.security.JwtTokenProvider;
import com.landmanagement.service.CurrentUserService;
import com.landmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    private final UserService userService;

    private final CurrentUserService currentUserService;

    private final JwtTokenProvider jwtTokenProvider;

    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        UserAccount user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!user.getIsActive()) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // Nếu mật khẩu đã quá hạn thì đánh dấu mustChangePassword=true nhưng vẫn cho
        // phép login
        if (user.getPasswordExpiresAt() != null && LocalDateTime.now().isAfter(user.getPasswordExpiresAt())) {
            if (!Boolean.TRUE.equals(user.getMustChangePassword())) {
                user.setMustChangePassword(true);
                userRepository.save(user);
            }
        }

        String token = jwtTokenProvider.generateToken(user);
        return ResponseEntity.ok(TokenResponse.builder()
                .accessToken(token)
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@RequestAttribute("userId") Long userId) {
        UserAccount user = currentUserService.getRequired(userId);
        return ResponseEntity.ok(MeResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .unitId(user.getUnitId())
                .mustChangePassword(user.getMustChangePassword())
                .build());
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Boolean>> changePassword(@RequestAttribute("userId") Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }

        if (request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }

        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());

        return ResponseEntity.ok(Map.of("ok", true));
    }
}