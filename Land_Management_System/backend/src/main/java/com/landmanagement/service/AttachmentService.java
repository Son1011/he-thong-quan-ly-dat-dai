package com.landmanagement.service;

import com.landmanagement.dto.response.AttachmentResponse;
import com.landmanagement.entity.DossierAttachment;
import com.landmanagement.entity.UserAccount;
import com.landmanagement.repository.DossierAttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final DossierAttachmentRepository attachmentRepository;

    private final DossierService dossierService;

    @Value("${app.uploads.dir:data/uploads}")
    private String uploadsDir;

    @Transactional
    public AttachmentResponse uploadAttachment(Long dossierId, MultipartFile file, UserAccount currentUser)
            throws IOException {

        dossierService.verifyDossierAccess(dossierId, currentUser);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn tệp để upload");
        }

        Path uploadsPath = Paths.get(uploadsDir)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(uploadsPath);

        String extension = getFileExtension(file.getOriginalFilename());
        String safeFilename = UUID.randomUUID().toString() + extension;

        Path filePath = uploadsPath.resolve(safeFilename)
                .normalize();

        file.transferTo(filePath.toFile());

        DossierAttachment attachment = DossierAttachment.builder()
                .dossierId(dossierId)
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .storagePath(safeFilename)
                .uploadedByUserId(currentUser.getId())
                .uploadedAt(LocalDateTime.now())
                .build();

        DossierAttachment saved = attachmentRepository.save(attachment);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> listAttachments(Long dossierId) {
        List<DossierAttachment> attachments = attachmentRepository.findByDossierIdOrderByUploadedAtDesc(dossierId);

        return attachments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public @NonNull DossierAttachment getAttachmentEntity(long attachmentId) {
        return Objects.requireNonNull(
                attachmentRepository.findById(attachmentId)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu")));
    }

    @Transactional(readOnly = true)
    public @NonNull DossierAttachment getAttachmentEntity(long attachmentId, long dossierId) {
        return Objects.requireNonNull(
                attachmentRepository.findByIdAndDossierId(attachmentId, dossierId)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu cho hồ sơ này")));
    }

    public String getAttachmentPath(long attachmentId) throws IOException {
        DossierAttachment attachment = getAttachmentEntity(attachmentId);

        Path uploadsPath = Paths.get(uploadsDir)
                .toAbsolutePath()
                .normalize();

        Path filePath = uploadsPath.resolve(attachment.getStoragePath())
                .normalize();

        if (!Files.exists(filePath)) {
            throw new IOException("Tệp không tồn tại trên server");
        }

        return filePath.toString();
    }

    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."))
                    .toLowerCase(Locale.ROOT);
        }

        return "";
    }

    private AttachmentResponse toResponse(DossierAttachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .dossierId(attachment.getDossierId())
                .originalFilename(attachment.getOriginalFilename())
                .contentType(attachment.getContentType())
                .uploadedByUserId(attachment.getUploadedByUserId())
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }
}