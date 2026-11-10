package com.landmanagement.service;

import com.landmanagement.dto.request.UnitCreateRequest;
import com.landmanagement.dto.response.UnitResponse;
import com.landmanagement.entity.AdministrativeUnit;
import com.landmanagement.enums.UnitLevel;
import com.landmanagement.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;

    @Transactional
    public Long createUnit(UnitCreateRequest request) {
        if (request.getLevel() == UnitLevel.CENTRAL) {
            boolean existingCentral = unitRepository.findByLevelAndIsActive(UnitLevel.CENTRAL, true).isPresent();
            if (existingCentral) {
                throw new IllegalArgumentException("Đã tồn tại đơn vị Trung ương. Không thể tạo thêm CENTRAL.");
            }
        }

        if (request.getLevel() != UnitLevel.CENTRAL && request.getParentId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn đơn vị cấp trên (parent_id)");
        }

        if (request.getParentId() != null) {
            AdministrativeUnit parent = unitRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị cấp trên"));

            UnitLevel expectedParentLevel = null;
            if (request.getLevel() == UnitLevel.PROVINCE) {
                expectedParentLevel = UnitLevel.CENTRAL;
            } else if (request.getLevel() == UnitLevel.COMMUNE) {
                expectedParentLevel = UnitLevel.PROVINCE;
            }

            if (expectedParentLevel != null && parent.getUnitLevel() != expectedParentLevel) {
                throw new IllegalArgumentException("Cấp đơn vị cấp trên không hợp lệ");
            }
        }

        if (request.getLevel() != UnitLevel.COMMUNE && request.getKind() != null) {
            throw new IllegalArgumentException("Trường 'kind' chỉ áp dụng cho cấp Xã/Phường");
        }

        AdministrativeUnit unit = AdministrativeUnit.builder()
                .name(request.getName())
                .unitLevel(request.getLevel())
                .unitKind(request.getKind())
                .parentId(request.getParentId())
                .isActive(true)
                .build();

        AdministrativeUnit saved = unitRepository.save(unit);
        return saved.getId();
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> listProvinces(String q) {
        List<AdministrativeUnit> units;

        if (q != null && !q.isEmpty()) {
            units = unitRepository.findByNameContainingIgnoreCaseAndLevelAndIsActive(q, UnitLevel.PROVINCE, true);
        } else {
            units = unitRepository.findByLevelAndIsActiveOrderByName(UnitLevel.PROVINCE, true);
        }

        return units.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> listChildren(Long parentId, String q) {
        AdministrativeUnit parent = unitRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị cấp trên"));

        if (parent.getUnitLevel() != UnitLevel.PROVINCE) {
            throw new IllegalArgumentException("Chỉ hỗ trợ xem danh sách xã/phường theo tỉnh");
        }

        List<AdministrativeUnit> units = unitRepository.findByParentIdAndLevelAndIsActiveOrderByName(parentId,
                UnitLevel.COMMUNE, true);
        if (q != null && !q.isEmpty()) {
            String lowerQ = q.toLowerCase(Locale.ROOT);
            units = units.stream()
                    .filter(u -> u.getName() != null && u.getName().toLowerCase(Locale.ROOT).contains(lowerQ))
                    .toList();
        }

        return units.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdministrativeUnit getUnitEntityById(Long unitId) {
        return unitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị"));
    }

    @Transactional(readOnly = true)
    public AdministrativeUnit getCentralUnit() {
        return unitRepository.findByLevelAndIsActive(UnitLevel.CENTRAL, true)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị Trung ương"));
    }

    @Transactional(readOnly = true)
    public AdministrativeUnit getParentUnit(AdministrativeUnit unit) {
        if (unit.getParentId() == null) {
            throw new IllegalArgumentException("Đơn vị này không có cấp trên");
        }
        return unitRepository.findById(unit.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị cấp trên"));
    }

    @Transactional(readOnly = true)
    public List<Long> getChildCommuneIds(Long provinceId) {
        return unitRepository.findByParentIdAndLevelAndIsActiveOrderByName(provinceId, UnitLevel.COMMUNE, true)
                .stream()
                .map(AdministrativeUnit::getId)
                .toList();
    }

    private UnitResponse toResponse(AdministrativeUnit unit) {
        return UnitResponse.builder()
                .id(unit.getId())
                .name(unit.getName())
                .level(unit.getUnitLevel())
                .kind(unit.getUnitKind())
                .parentId(unit.getParentId())
                .build();
    }
}
