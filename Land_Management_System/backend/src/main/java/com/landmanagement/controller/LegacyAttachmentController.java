package com.landmanagement.controller;

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
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class LegacyAttachmentController {

        private final AttachmentService attachmentService;

        private final DossierService dossierService;

        private final CurrentUserService currentUserService;

        @GetMapping("/attachments/{attachmentId}/download")
        public ResponseEntity<FileSystemResource> downloadAttachment(
                        @PathVariable long attachmentId,
                        @RequestAttribute("userId") Long userId) throws IOException {
                UserAccount currentUser = currentUserService.getRequired(userId);

                DossierAttachment attachment = attachmentService.getAttachmentEntity(attachmentId);
                dossierService.verifyDossierAccess(attachment.getDossierId(), currentUser);
                String filePath = attachmentService.getAttachmentPath(attachmentId);
                String contentType = Objects.requireNonNullElse(attachment.getContentType(),
                                "application/octet-stream");

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + attachment.getOriginalFilename() + "\"")
                                .header(HttpHeaders.CONTENT_TYPE, contentType)
                                .body(new FileSystemResource(Objects.requireNonNull(filePath)));
        }
}
