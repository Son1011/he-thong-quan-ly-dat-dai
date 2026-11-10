package com.landmanagement.service;

import com.landmanagement.entity.AuditLog;
import com.landmanagement.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for audit logging of sensitive operations.
 * Tracks user actions for compliance, debugging, and security auditing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log a sensitive operation
     */
    @Transactional
    public void logAction(Long userId, String username, String actionType, String entityType,
            Long entityId, String description) {
        logAction(userId, username, actionType, entityType, entityId, null, null, null, description, true, null);
    }

    /**
     * Log an operation with old and new values
     */
    @Transactional
    public void logAction(Long userId, String username, String actionType, String entityType,
            Long entityId, String oldValue, String newValue, Long dossierId,
            String description, boolean success, String errorMessage) {
        try {
            HttpServletRequest request = getCurrentRequest();
            String ipAddress = request != null ? getClientIpAddress(request) : "UNKNOWN";
            String userAgent = request != null ? request.getHeader("User-Agent") : "UNKNOWN";

            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .username(username)
                    .actionType(actionType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .dossierId(dossierId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .status(success ? "SUCCESS" : "FAILED")
                    .errorMessage(errorMessage)
                    .description(description)
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit logged - User: {}, Action: {}, Entity: {}, Status: {}",
                    username, actionType, entityType, success ? "SUCCESS" : "FAILED");
        } catch (Exception e) {
            log.error("Failed to log audit action", e);
            // Don't throw exception - audit logging failure shouldn't affect main operation
        }
    }

    /**
     * Log failed operation
     */
    @Transactional
    public void logFailedAction(Long userId, String username, String actionType, String entityType,
            Long entityId, String description, String errorMessage) {
        logAction(userId, username, actionType, entityType, entityId, null, null, null,
                description, false, errorMessage);
    }

    /**
     * Get audit logs for a dossier
     */
    public Page<AuditLog> getDossierAuditLogs(Long dossierId, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return auditLogRepository.findByDossierId(dossierId, pageable);
    }

    /**
     * Get audit logs for a user
     */
    public Page<AuditLog> getUserAuditLogs(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return auditLogRepository.findByUserId(userId, pageable);
    }

    /**
     * Get audit logs by action type
     */
    public Page<AuditLog> getAuditLogsByActionType(String actionType, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return auditLogRepository.findByActionType(actionType, pageable);
    }

    /**
     * Get audit logs within date range
     */
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Get audit logs for specific dossier and action
     */
    public List<AuditLog> getDossierActionLogs(Long dossierId, String actionType) {
        return auditLogRepository.findByDossierIdAndActionType(dossierId, actionType);
    }

    /**
     * Get current HTTP request
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.debug("Failed to get current request", e);
            return null;
        }
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = { "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR" };
        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.isEmpty() && !"unknown".equalsIgnoreCase(value)) {
                return value.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
