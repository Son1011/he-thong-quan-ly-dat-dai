package com.landmanagement.controller;

import com.landmanagement.dto.response.StatisticsResponse;
import com.landmanagement.entity.UserAccount;
import com.landmanagement.enums.UserRole;
import com.landmanagement.service.CurrentUserService;
import com.landmanagement.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    private final CurrentUserService currentUserService;

    @GetMapping("/provinces")
    public ResponseEntity<List<StatisticsResponse>> getStatisticsByProvinces(@RequestAttribute("userId") Long userId) {
        UserAccount currentUser = currentUserService.getRequired(userId);

        if (currentUser.getRole() != UserRole.ADMIN && currentUser.getRole() != UserRole.CENTRAL_OFFICER) {
            throw new IllegalArgumentException("Forbidden");
        }

        List<StatisticsResponse> stats = statisticsService.getStatisticsByProvinces();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/provinces/{provinceId}/children")
    public ResponseEntity<List<StatisticsResponse>> getStatisticsByChildren(@PathVariable Long provinceId,
            @RequestAttribute("userId") Long userId) {
        UserAccount currentUser = currentUserService.getRequired(userId);

        if (currentUser.getRole() == UserRole.PROVINCE_OFFICER && !currentUser.getUnitId().equals(provinceId)) {
            throw new IllegalArgumentException("Forbidden");
        }

        if (currentUser.getRole() != UserRole.ADMIN && currentUser.getRole() != UserRole.CENTRAL_OFFICER
                && currentUser.getRole() != UserRole.PROVINCE_OFFICER) {
            throw new IllegalArgumentException("Forbidden");
        }

        List<StatisticsResponse> stats = statisticsService.getStatisticsByChildren(provinceId);
        return ResponseEntity.ok(stats);
    }
}

