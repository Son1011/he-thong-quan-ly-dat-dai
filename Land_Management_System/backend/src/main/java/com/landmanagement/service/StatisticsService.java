package com.landmanagement.service;

import com.landmanagement.dto.response.StatisticsResponse;
import com.landmanagement.entity.AdministrativeUnit;
import com.landmanagement.entity.Dossier;
import com.landmanagement.enums.DossierStatus;
import com.landmanagement.enums.UnitLevel;
import com.landmanagement.repository.DossierAttachmentRepository;
import com.landmanagement.repository.DossierRepository;
import com.landmanagement.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final DossierRepository dossierRepository;

    private final DossierAttachmentRepository attachmentRepository;

    private final UnitRepository unitRepository;

    @Transactional(readOnly = true)
    public List<StatisticsResponse> getStatisticsByProvinces() {
        List<AdministrativeUnit> provinces = unitRepository.findByLevelAndIsActiveOrderByName(UnitLevel.PROVINCE, true);

        return provinces.stream()
                .map(province -> {
                    List<Long> originUnitIds = getProvinceOriginUnitIds(province);
                    List<Dossier> dossiers = dossierRepository.findByOriginUnitIdInOrderByUpdatedAtDesc(originUnitIds);
                    long totalAttachments = countAttachments(dossiers);

                    return StatisticsResponse.builder()
                            .unitId(province.getId())
                            .name(province.getName())
                            .kind(null)
                            .totalDossiers(dossiers.size())
                            .approved(countByStatus(dossiers, DossierStatus.APPROVED))
                            .returned(countByStatus(dossiers, DossierStatus.RETURNED))
                            .pending(countPending(dossiers))
                            .totalAttachments((int) totalAttachments)
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StatisticsResponse> getStatisticsByChildren(Long provinceId) {
        AdministrativeUnit province = unitRepository.findById(provinceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tỉnh"));

        if (province.getUnitLevel() != UnitLevel.PROVINCE) {
            throw new IllegalArgumentException("Không phải đơn vị cấp tỉnh");
        }

        List<AdministrativeUnit> communes = unitRepository.findByParentIdAndLevelAndIsActiveOrderByName(provinceId,
                UnitLevel.COMMUNE, true);

        return communes.stream()
                .map(commune -> {
                    List<Dossier> dossiers = dossierRepository.findByOriginUnitIdOrderByUpdatedAtDesc(commune.getId());
                    long totalAttachments = countAttachments(dossiers);

                    return StatisticsResponse.builder()
                            .unitId(commune.getId())
                            .name(commune.getName())
                            .kind(commune.getUnitKind() != null ? commune.getUnitKind().toString() : null)
                            .totalDossiers(dossiers.size())
                            .approved(countByStatus(dossiers, DossierStatus.APPROVED))
                            .returned(countByStatus(dossiers, DossierStatus.RETURNED))
                            .pending(countPending(dossiers))
                            .totalAttachments((int) totalAttachments)
                            .build();
                })
                .toList();
    }

    private List<Long> getProvinceOriginUnitIds(AdministrativeUnit province) {
        List<Long> originUnitIds = new ArrayList<>(unitRepository
                .findByParentIdAndLevelAndIsActiveOrderByName(province.getId(), UnitLevel.COMMUNE, true)
                .stream()
                .map(AdministrativeUnit::getId)
                .toList());
        originUnitIds.add(province.getId());
        return originUnitIds;
    }

    private int countByStatus(List<Dossier> dossiers, DossierStatus status) {
        return (int) dossiers.stream()
                .filter(dossier -> dossier.getStatus() == status)
                .count();
    }

    private int countPending(List<Dossier> dossiers) {
        return (int) dossiers.stream()
                .filter(dossier -> dossier.getStatus() == DossierStatus.PENDING
                        || dossier.getStatus() == DossierStatus.ESCALATED)
                .count();
    }

    private long countAttachments(List<Dossier> dossiers) {
        if (dossiers.isEmpty()) {
            return 0;
        }
        List<Long> dossierIds = dossiers.stream()
                .map(Dossier::getId)
                .toList();
        return attachmentRepository.countByDossierIdIn(dossierIds);
    }
}
