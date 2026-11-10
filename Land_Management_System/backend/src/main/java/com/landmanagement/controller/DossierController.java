package com.landmanagement.controller;

import com.landmanagement.dto.request.DossierActionRequest;
import com.landmanagement.dto.request.DossierCreateRequest;
import com.landmanagement.dto.request.PaginationRequest;
import com.landmanagement.dto.response.DossierResponse;
import com.landmanagement.dto.response.PaginatedResponse;
import com.landmanagement.entity.UserAccount;
import com.landmanagement.enums.DossierStatus;
import com.landmanagement.service.CurrentUserService;
import com.landmanagement.service.DossierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for dossier management.
 * Provides endpoints for creating, listing, and managing land dossiers.
 */
@RestController
@RequestMapping("/dossiers")
@RequiredArgsConstructor
@Tag(name = "Dossiers", description = "Dossier management endpoints")
public class DossierController {

        private final DossierService dossierService;
        private final CurrentUserService currentUserService;

        @PostMapping
        @Operation(summary = "Create a new dossier")
        public ResponseEntity<DossierResponse> createDossier(@Valid @RequestBody DossierCreateRequest request,
                        @RequestAttribute("userId") Long userId) {
                UserAccount currentUser = currentUserService.getRequired(userId);
                DossierResponse response = dossierService.createDossier(request, currentUser);
                return ResponseEntity.ok(response);
        }

        @GetMapping
        @Operation(summary = "List dossiers with pagination")
        public ResponseEntity<PaginatedResponse<DossierResponse>> listDossiers(
                        @RequestParam(required = false) DossierStatus status,
                        @RequestParam(name = "unit_id", required = false) Long unitId,
                        @RequestParam(name = "include_children", required = false, defaultValue = "false") boolean includeChildren,
                        @RequestParam(name = "sent_to_central", required = false) Boolean sentToCentral,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestAttribute("userId") Long userId) {
                UserAccount currentUser = currentUserService.getRequired(userId);
                PaginationRequest pagination = PaginationRequest.builder()
                                .page(page)
                                .size(size)
                                .build();
                Page<DossierResponse> result = dossierService.listDossiersWithPagination(
                                status, unitId, includeChildren, sentToCentral, currentUser, pagination);
                PaginatedResponse<DossierResponse> response = PaginatedResponse.of(
                                result.getContent(),
                                result.getTotalElements(),
                                result.getNumber(),
                                result.getSize());
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{dossierId}")
        @Operation(summary = "Get a specific dossier by ID")
        public ResponseEntity<DossierResponse> getDossier(@PathVariable Long dossierId,
                        @RequestAttribute("userId") Long userId) {
                UserAccount currentUser = currentUserService.getRequired(userId);
                DossierResponse response = dossierService.getDossierById(dossierId, currentUser);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/{dossierId}/actions")
        @Operation(summary = "Perform an action on a dossier (approve, reject, return, escalate)")
        public ResponseEntity<DossierResponse> actionDossier(@PathVariable Long dossierId,
                        @Valid @RequestBody DossierActionRequest request,
                        @RequestAttribute("userId") Long userId) {
                UserAccount currentUser = currentUserService.getRequired(userId);
                DossierResponse response = dossierService.actionDossier(dossierId, request, currentUser);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/inbox")
        @Operation(summary = "Get user's inbox with pagination")
        public ResponseEntity<PaginatedResponse<DossierResponse>> getInbox(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestAttribute("userId") Long userId) {
                UserAccount currentUser = currentUserService.getRequired(userId);
                PaginationRequest pagination = PaginationRequest.builder()
                                .page(page)
                                .size(size)
                                .build();
                Page<DossierResponse> result = dossierService.getInboxWithPagination(currentUser, pagination);
                PaginatedResponse<DossierResponse> response = PaginatedResponse.of(
                                result.getContent(),
                                result.getTotalElements(),
                                result.getNumber(),
                                result.getSize());
                return ResponseEntity.ok(response);
        }

        @GetMapping("/central/decisions")
        @Operation(summary = "Get central decisions with pagination")
        public ResponseEntity<PaginatedResponse<DossierResponse>> getCentralDecisions(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestAttribute("userId") Long userId) {
                UserAccount currentUser = currentUserService.getRequired(userId);
                PaginationRequest pagination = PaginationRequest.builder()
                                .page(page)
                                .size(size)
                                .build();
                Page<DossierResponse> result = dossierService.getCentralDecisionsWithPagination(currentUser,
                                pagination);
                PaginatedResponse<DossierResponse> response = PaginatedResponse.of(
                                result.getContent(),
                                result.getTotalElements(),
                                result.getNumber(),
                                result.getSize());
                return ResponseEntity.ok(response);
        }
}
