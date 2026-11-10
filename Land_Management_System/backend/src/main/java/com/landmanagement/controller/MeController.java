package com.landmanagement.controller;

import com.landmanagement.dto.response.MeResponse;
import com.landmanagement.entity.UserAccount;
import com.landmanagement.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MeController {

    private final CurrentUserService currentUserService;

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
}
