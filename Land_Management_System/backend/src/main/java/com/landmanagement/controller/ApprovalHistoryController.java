package com.landmanagement.controller;

import com.landmanagement.dto.response.ApprovalHistoryResponse;
import com.landmanagement.entity.UserAccount;
import com.landmanagement.service.ApprovalHistoryService;
import com.landmanagement.service.CurrentUserService;
import com.landmanagement.service.DossierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dossiers/{dossierId}/history")
@RequiredArgsConstructor
public class ApprovalHistoryController {

    private final ApprovalHistoryService approvalHistoryService;

    private final DossierService dossierService;

    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<ApprovalHistoryResponse>> getDossierHistory(@PathVariable Long dossierId,
            @RequestAttribute("userId") Long userId) {
        UserAccount currentUser = currentUserService.getRequired(userId);

        dossierService.verifyDossierAccess(dossierId, currentUser);

        List<ApprovalHistoryResponse> histories = approvalHistoryService.getDossierHistory(dossierId);
        return ResponseEntity.ok(histories);
    }
}

