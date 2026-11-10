package com.landmanagement.controller;

import com.landmanagement.dto.response.AttachmentResponse;
import com.landmanagement.entity.DossierAttachment;
import com.landmanagement.entity.UserAccount;
import com.landmanagement.service.AttachmentService;
import com.landmanagement.service.CurrentUserService;
import com.landmanagement.service.DossierService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/dossiers/{dossierId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

        private final AttachmentService attachmentService;

        private final DossierService dossierService;

        private final CurrentUserService currentUserService;

        @PostMapping
        public ResponseEntity<AttachmentResponse> uploadAttachment(
                        @PathVariable Long dossierId,
                        @RequestParam("file") MultipartFile file,
                        @RequestAttribute("userId") Long userId) throws IOException {
                UserAccount currentUser = currentUserService.getRequired(userId);
                AttachmentResponse response = attachmentService.uploadAttachment(dossierId, file, currentUser);
                return ResponseEntity.ok(response);
        }

        @GetMapping
        public ResponseEntity<List<AttachmentResponse>> listAttachments(@PathVariable Long dossierId,
                        @RequestAttribute("userId") Long userId) {
                UserAccount currentUser = currentUserService.getRequired(userId);
                dossierService.verifyDossierAccess(dossierId, currentUser);

                List<AttachmentResponse> attachments = attachmentService.listAttachments(dossierId);
                return ResponseEntity.ok(attachments);
        }

        @GetMapping("/{attachmentId}/download")
        public ResponseEntity<FileSystemResource> downloadAttachment(@PathVariable long dossierId,
                        @PathVariable long attachmentId,
                        @RequestAttribute("userId") Long userId) throws IOException {
                UserAccount currentUser = currentUserService.getRequired(userId);

                dossierService.verifyDossierAccess(dossierId, currentUser);
                DossierAttachment attachment = attachmentService.getAttachmentEntity(attachmentId, dossierId);
                String filePath = Objects.requireNonNull(
                                attachmentService.getAttachmentPath(attachmentId),
                                "Attachment path must not be null");

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + attachment.getOriginalFilename() + "\"")
                                .header(HttpHeaders.CONTENT_TYPE, attachment.getContentType())
                                .body(new FileSystemResource(filePath));
        }
}
