package com.landmanagement.controller;

import com.landmanagement.dto.request.UserCreateRequest;
import com.landmanagement.dto.request.UserUpdateRequest;
import com.landmanagement.dto.response.PaginatedResponse;
import com.landmanagement.dto.response.UserResponse;
import com.landmanagement.enums.UserRole;
import com.landmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for user management (Admin only).
 * Provides endpoints for creating, listing, and updating user accounts.
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints (Admin only)")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user account")
    public ResponseEntity<Map<String, Long>> createUser(@Valid @RequestBody UserCreateRequest request) {
        Long userId = userService.createUser(request).getId();
        return ResponseEntity.ok(Map.of("id", userId));
    }

    @GetMapping
    @Operation(summary = "List users with pagination and filters")
    public ResponseEntity<PaginatedResponse<UserResponse>> listUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Long unitId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserResponse> result = userService.listUsersWithPagination(q, role, unitId, page, size);
        PaginatedResponse<UserResponse> response = PaginatedResponse.of(
                result.getContent(),
                result.getTotalElements(),
                result.getNumber(),
                result.getSize()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update a user account")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse updated = userService.updateUser(userId, request);
        return ResponseEntity.ok(updated);
    }
}
