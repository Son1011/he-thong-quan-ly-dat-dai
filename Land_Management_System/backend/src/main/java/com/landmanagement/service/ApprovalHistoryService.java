package com.landmanagement.service;

import com.landmanagement.dto.response.ApprovalHistoryResponse;
import com.landmanagement.entity.ApprovalHistory;
import com.landmanagement.repository.ApprovalHistoryRepository;
import com.landmanagement.repository.UnitRepository;
import com.landmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalHistoryService {

    private final ApprovalHistoryRepository approvalHistoryRepository;

    private final UserRepository userRepository;

    private final UnitRepository unitRepository;

    @Transactional(readOnly = true)
    public List<ApprovalHistoryResponse> getDossierHistory(Long dossierId) {
        List<ApprovalHistory> histories = approvalHistoryRepository.findByDossierIdOrderByCreatedAtAsc(dossierId);
        return histories.stream()
                .map(this::toResponse)
                .toList();
    }

    private ApprovalHistoryResponse toResponse(ApprovalHistory history) {
        return ApprovalHistoryResponse.builder()
                .id(history.getId())
                .dossierId(history.getDossierId())
                .action(history.getAction())
                .note(history.getNote())
                .signatureBase64Png(history.getSignatureBase64Png())
                .actorUserId(history.getActorUserId())
                .actorUnitId(history.getActorUnitId())
                .actorUsername(findUsername(history.getActorUserId()))
                .actorUnitName(findUnitName(history.getActorUnitId()))
                .fromUnitId(history.getFromUnitId())
                .toUnitId(history.getToUnitId())
                .createdAt(history.getCreatedAt())
                .build();
    }

    private String findUsername(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
                .map(user -> user.getUsername())
                .orElse(null);
    }

    private String findUnitName(Long unitId) {
        if (unitId == null) {
            return null;
        }
        return unitRepository.findById(unitId)
                .map(unit -> unit.getName())
                .orElse(null);
    }
}
