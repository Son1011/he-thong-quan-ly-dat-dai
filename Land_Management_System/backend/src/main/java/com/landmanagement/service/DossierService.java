package com.landmanagement.service;

import com.landmanagement.dto.request.DossierActionRequest;
import com.landmanagement.dto.request.DossierCreateRequest;
import com.landmanagement.dto.response.DossierResponse;
import com.landmanagement.entity.AdministrativeUnit;
import com.landmanagement.entity.ApprovalHistory;
import com.landmanagement.entity.Dossier;
import com.landmanagement.entity.DossierType;
import com.landmanagement.entity.UserAccount;
import com.landmanagement.enums.ApprovalAction;
import com.landmanagement.enums.DossierStatus;
import com.landmanagement.enums.UnitLevel;
import com.landmanagement.enums.UserRole;
import com.landmanagement.repository.ApprovalHistoryRepository;
import com.landmanagement.repository.DossierRepository;
import com.landmanagement.repository.DossierTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DossierService {

    private final DossierRepository dossierRepository;

    private final ApprovalHistoryRepository approvalHistoryRepository;

    private final UnitService unitService;

    private final DossierTypeRepository dossierTypeRepository;

    @Transactional
    public DossierResponse createDossier(DossierCreateRequest request, UserAccount currentUser) {
        if (currentUser.getRole() == UserRole.CENTRAL_OFFICER) {
            throw new IllegalArgumentException("Cấp Trung ương không được tạo hồ sơ");
        }

        if (currentUser.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("Admin không tham gia tạo hồ sơ");
        }

        AdministrativeUnit originUnit = unitService.getUnitEntityById(currentUser.getUnitId());

        Long assignedTo;
        DossierStatus status;
        Boolean sentToCentral;

        if (currentUser.getRole() == UserRole.COMMUNE_OFFICER) {
            if (originUnit.getUnitLevel() != UnitLevel.COMMUNE) {
                throw new IllegalArgumentException("Tài khoản không đúng cấp đơn vị (xã/phường)");
            }

            AdministrativeUnit provinceUnit = unitService.getParentUnit(originUnit);
            assignedTo = provinceUnit.getId();
            status = DossierStatus.PENDING;
            sentToCentral = false;

        } else if (currentUser.getRole() == UserRole.PROVINCE_OFFICER) {
            if (originUnit.getUnitLevel() != UnitLevel.PROVINCE) {
                throw new IllegalArgumentException("Tài khoản không đúng cấp đơn vị (tỉnh)");
            }

            AdministrativeUnit centralUnit = unitService.getCentralUnit();
            assignedTo = centralUnit.getId();
            status = DossierStatus.ESCALATED;
            sentToCentral = true;

        } else {
            throw new IllegalArgumentException("Bạn không có quyền tạo hồ sơ");
        }

        DossierType dossierType = dossierTypeRepository.findById(request.getDossierTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại hồ sơ"));

        String dossierCode = "HS-" + System.currentTimeMillis();

        Dossier dossier = Dossier.builder()
                .title(request.getTitle())
                .dossierCode(dossierCode)
                .citizenName(request.getCitizenName())
                .citizenIdentityNumber(request.getCitizenIdentityNumber())
                .citizenPhone(request.getCitizenPhone())
                .citizenAddress(request.getCitizenAddress())
                .dossierType(dossierType)
                .status(status)
                .sentToCentral(sentToCentral)
                .originUnitId(currentUser.getUnitId())
                .createdByUserId(currentUser.getId())
                .assignedToUnitId(assignedTo)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Dossier saved = dossierRepository.save(dossier);

        ApprovalHistory hist = ApprovalHistory.builder()
                .dossierId(saved.getId())
                .action(ApprovalAction.CREATE)
                .note(null)
                .signatureBase64Png(null)
                .actorUserId(currentUser.getId())
                .actorUnitId(currentUser.getUnitId())
                .fromUnitId(null)
                .toUnitId(assignedTo)
                .createdAt(LocalDateTime.now())
                .build();

        approvalHistoryRepository.save(hist);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DossierResponse getDossierById(Long dossierId, UserAccount currentUser) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ"));

        if (!canViewDossier(currentUser, dossier)) {
            throw new IllegalArgumentException("Bạn không có quyền xem hồ sơ này");
        }

        return toResponse(dossier);
    }

    @Transactional(readOnly = true)
    public void verifyDossierAccess(Long dossierId, UserAccount currentUser) {
        getDossierById(dossierId, currentUser);
    }

    @Transactional(readOnly = true)
    public List<DossierResponse> listDossiers(
            DossierStatus status,
            Long unitId,
            boolean includeChildren,
            Boolean sentToCentral,
            UserAccount currentUser) {
        List<Long> allowedOriginIds = getAllowedOriginUnitIds(currentUser);

        List<Dossier> dossiers;

        if (unitId != null) {
            AdministrativeUnit unit = unitService.getUnitEntityById(unitId);
            List<Long> filterIds = new ArrayList<>(List.of(unit.getId()));

            if (includeChildren && unit.getUnitLevel() == UnitLevel.PROVINCE) {
                List<Long> childIds = unitService.getChildCommuneIds(unit.getId());
                filterIds.addAll(childIds);
            }

            if (allowedOriginIds != null) {
                filterIds = filterIds.stream()
                        .filter(allowedOriginIds::contains)
                        .toList();
            }

            if (filterIds.isEmpty()) {
                return List.of();
            }

            dossiers = dossierRepository.searchByOriginUnitIds(filterIds, status, sentToCentral);

        } else {
            if (currentUser.getRole() == UserRole.CENTRAL_OFFICER) {
                dossiers = dossierRepository.searchByAssignedToUnitId(
                        currentUser.getUnitId(),
                        status,
                        sentToCentral);
            } else if (allowedOriginIds != null) {
                if (allowedOriginIds.isEmpty()) {
                    return List.of();
                }

                dossiers = dossierRepository.searchByOriginUnitIds(
                        allowedOriginIds,
                        status,
                        sentToCentral);
            } else {
                dossiers = dossierRepository.searchAll(status, sentToCentral);
            }
        }

        return dossiers.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DossierResponse> getInbox(UserAccount currentUser) {
        List<Dossier> dossiers = dossierRepository.findByAssignedToUnitIdOrderByUpdatedAtDesc(
                currentUser.getUnitId());

        return dossiers.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DossierResponse> getCentralDecisions(UserAccount currentUser) {
        if (currentUser.getRole() != UserRole.ADMIN
                && currentUser.getRole() != UserRole.CENTRAL_OFFICER) {
            throw new IllegalArgumentException("Bạn không có quyền");
        }

        return dossierRepository
                .findByLatestApprovalActionInOrderByUpdatedAtDesc(
                        List.of(ApprovalAction.APPROVE, ApprovalAction.RETURN))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DossierResponse actionDossier(
            Long dossierId,
            DossierActionRequest request,
            UserAccount currentUser) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ"));

        if (!java.util.Objects.equals(dossier.getAssignedToUnitId(), currentUser.getUnitId())) {
            throw new IllegalArgumentException("Hồ sơ này không thuộc đơn vị của bạn để xử lý");
        }

        if (request.getAction() == null || request.getAction().isBlank()) {
            throw new IllegalArgumentException("Thao tác không hợp lệ");
        }

        String action = request.getAction().strip().toLowerCase();
        Long fromUnitId = dossier.getAssignedToUnitId();
        Long toUnitId;

        if ("return".equals(action)
                && (request.getNote() == null || request.getNote().strip().isEmpty())) {
            throw new IllegalArgumentException("Vui lòng ghi rõ lý do trả lại hồ sơ");
        }

        if (currentUser.getRole() == UserRole.PROVINCE_OFFICER) {
            handleProvinceAction(dossier, action, currentUser);
            toUnitId = dossier.getAssignedToUnitId();

        } else if (currentUser.getRole() == UserRole.CENTRAL_OFFICER) {
            handleCentralAction(dossier, action);
            toUnitId = dossier.getAssignedToUnitId();

        } else if (currentUser.getRole() == UserRole.COMMUNE_OFFICER) {
            if (!"submit".equals(action)) {
                throw new IllegalArgumentException("Thao tác không hợp lệ (cấp xã/phường)");
            }

            if (dossier.getStatus() != DossierStatus.RETURNED) {
                throw new IllegalArgumentException("Chỉ hồ sơ bị trả lại mới được gửi lại");
            }

            AdministrativeUnit originUnit = unitService.getUnitEntityById(currentUser.getUnitId());
            AdministrativeUnit provinceUnit = unitService.getParentUnit(originUnit);

            dossier.setStatus(DossierStatus.PENDING);
            dossier.setAssignedToUnitId(provinceUnit.getId());

            toUnitId = provinceUnit.getId();

        } else {
            throw new IllegalArgumentException("Bạn không có quyền");
        }

        dossier.setUpdatedAt(LocalDateTime.now());
        Dossier updated = dossierRepository.save(dossier);

        ApprovalHistory hist = ApprovalHistory.builder()
                .dossierId(updated.getId())
                .action(mapActionToApprovalAction(action))
                .note(request.getNote())
                .signatureBase64Png(request.getSignatureBase64Png())
                .actorUserId(currentUser.getId())
                .actorUnitId(currentUser.getUnitId())
                .fromUnitId(fromUnitId)
                .toUnitId(toUnitId)
                .createdAt(LocalDateTime.now())
                .build();

        approvalHistoryRepository.save(hist);

        return toResponse(updated);
    }

    private ApprovalAction mapActionToApprovalAction(String action) {
        switch (action) {
            case "create":
                return ApprovalAction.CREATE;
            case "approve":
                return ApprovalAction.APPROVE;
            case "return":
                return ApprovalAction.RETURN;
            case "escalate":
                return ApprovalAction.ESCALATE;
            case "submit":
                return ApprovalAction.SUBMIT;
            default:
                throw new IllegalArgumentException("Thao tác không hợp lệ: " + action);
        }
    }

    private void handleProvinceAction(Dossier dossier, String action, UserAccount currentUser) {
        List<Long> childCommunes = unitService.getChildCommuneIds(currentUser.getUnitId());

        boolean isCommuneOrigin = childCommunes.contains(dossier.getOriginUnitId());
        boolean isProvinceOrigin = java.util.Objects.equals(
                dossier.getOriginUnitId(),
                currentUser.getUnitId());

        if (isCommuneOrigin) {
            if ("approve".equals(action)) {
                dossier.setStatus(DossierStatus.APPROVED);
                dossier.setAssignedToUnitId(dossier.getOriginUnitId());

            } else if ("return".equals(action)) {
                dossier.setStatus(DossierStatus.RETURNED);
                dossier.setAssignedToUnitId(dossier.getOriginUnitId());

            } else if ("escalate".equals(action)) {
                AdministrativeUnit centralUnit = unitService.getCentralUnit();

                dossier.setStatus(DossierStatus.ESCALATED);
                dossier.setSentToCentral(true);
                dossier.setAssignedToUnitId(centralUnit.getId());

            } else {
                throw new IllegalArgumentException("Thao tác không hợp lệ (cấp tỉnh)");
            }

        } else if (isProvinceOrigin) {
            if (!"escalate".equals(action)) {
                throw new IllegalArgumentException(
                        "Hồ sơ do tỉnh tạo chỉ được gửi lại Trung ương sau khi bị trả về");
            }

            AdministrativeUnit centralUnit = unitService.getCentralUnit();

            dossier.setStatus(DossierStatus.ESCALATED);
            dossier.setSentToCentral(true);
            dossier.setAssignedToUnitId(centralUnit.getId());

        } else {
            throw new IllegalArgumentException("Hồ sơ không thuộc phạm vi quản lý của bạn");
        }
    }

    private void handleCentralAction(Dossier dossier, String action) {
        if (!"approve".equals(action) && !"return".equals(action)) {
            throw new IllegalArgumentException("Thao tác không hợp lệ (cấp Trung ương)");
        }

        AdministrativeUnit originUnit = unitService.getUnitEntityById(dossier.getOriginUnitId());
        Long targetUnitId = originUnit.getId();

        if ("approve".equals(action)) {
            dossier.setStatus(DossierStatus.APPROVED);
        } else {
            dossier.setStatus(DossierStatus.RETURNED);
        }

        dossier.setSentToCentral(false);
        dossier.setAssignedToUnitId(targetUnitId);
    }

    private boolean canViewDossier(UserAccount currentUser, Dossier dossier) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return true;
        }

        if (currentUser.getRole() == UserRole.COMMUNE_OFFICER) {
            return java.util.Objects.equals(dossier.getOriginUnitId(), currentUser.getUnitId());
        }

        if (currentUser.getRole() == UserRole.PROVINCE_OFFICER) {
            if (java.util.Objects.equals(dossier.getOriginUnitId(), currentUser.getUnitId())) {
                return true;
            }

            List<Long> childCommunes = unitService.getChildCommuneIds(currentUser.getUnitId());
            return childCommunes.contains(dossier.getOriginUnitId());
        }

        if (currentUser.getRole() == UserRole.CENTRAL_OFFICER) {
            return true;
        }

        return false;
    }

    private List<Long> getAllowedOriginUnitIds(UserAccount currentUser) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return null;
        }

        if (currentUser.getRole() == UserRole.COMMUNE_OFFICER) {
            return currentUser.getUnitId() != null
                    ? List.of(currentUser.getUnitId())
                    : List.of();
        }

        if (currentUser.getRole() == UserRole.PROVINCE_OFFICER) {
            List<Long> result = new ArrayList<>();

            if (currentUser.getUnitId() != null) {
                result.add(currentUser.getUnitId());
            }

            result.addAll(unitService.getChildCommuneIds(currentUser.getUnitId()));

            return result;
        }

        if (currentUser.getRole() == UserRole.CENTRAL_OFFICER) {
            return null;
        }

        return List.of();
    }

    private DossierResponse toResponse(Dossier dossier) {
        return DossierResponse.builder()
                .id(dossier.getId())
                .title(dossier.getTitle())
                .status(dossier.getStatus())
                .sentToCentral(dossier.getSentToCentral())
                .originUnitId(dossier.getOriginUnitId())
                .createdByUserId(dossier.getCreatedByUserId())
                .assignedToUnitId(dossier.getAssignedToUnitId())
                .createdAt(dossier.getCreatedAt())
                .updatedAt(dossier.getUpdatedAt())
                .dossierTypeId(dossier.getDossierType().getId())
                .dossierTypeName(dossier.getDossierType().getName())
                .build();
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<DossierResponse> listDossiersWithPagination(
            DossierStatus status,
            Long unitId,
            boolean includeChildren,
            Boolean sentToCentral,
            UserAccount currentUser,
            com.landmanagement.dto.request.PaginationRequest pagination) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pagination.getPageOrDefault(),
                pagination.getSizeOrDefault());

        List<Long> allowedOriginUnitIds = getAllowedOriginUnitIds(currentUser);

        if (unitId != null) {
            if (allowedOriginUnitIds != null && !allowedOriginUnitIds.contains(unitId)) {
                throw new IllegalArgumentException("Bạn không có quyền xem hồ sơ từ đơn vị này");
            }

            List<Long> searchUnitIds = new ArrayList<>();
            searchUnitIds.add(unitId);

            if (includeChildren && currentUser.getRole() == UserRole.PROVINCE_OFFICER) {
                searchUnitIds.addAll(unitService.getChildCommuneIds(unitId));
            }

            return dossierRepository
                    .searchByOriginUnitIdsPageable(searchUnitIds, status, sentToCentral, pageable)
                    .map(this::toResponse);
        }

        if (allowedOriginUnitIds != null && allowedOriginUnitIds.isEmpty()) {
            return new org.springframework.data.domain.PageImpl<>(
                    new ArrayList<>(),
                    pageable,
                    0);
        }

        if (allowedOriginUnitIds != null) {
            return dossierRepository
                    .searchByOriginUnitIdsPageable(
                            allowedOriginUnitIds,
                            status,
                            sentToCentral,
                            pageable)
                    .map(this::toResponse);
        }

        return dossierRepository
                .searchAllPageable(status, sentToCentral, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<DossierResponse> getInboxWithPagination(
            UserAccount currentUser,
            com.landmanagement.dto.request.PaginationRequest pagination) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pagination.getPageOrDefault(),
                pagination.getSizeOrDefault());

        return dossierRepository
                .findInboxPageable(currentUser.getUnitId(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<DossierResponse> getCentralDecisionsWithPagination(
            UserAccount currentUser,
            com.landmanagement.dto.request.PaginationRequest pagination) {
        if (currentUser.getRole() != UserRole.CENTRAL_OFFICER) {
            throw new IllegalArgumentException("Chỉ cấp Trung ương có thể xem quyết định Trung ương");
        }

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pagination.getPageOrDefault(),
                pagination.getSizeOrDefault());

        return dossierRepository
                .findCentralDecisionsPageable(pageable)
                .map(this::toResponse);
    }
}