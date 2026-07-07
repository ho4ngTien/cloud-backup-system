package com.cloudsafe.audit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudsafe.audit.entity.AuditLog;
import com.cloudsafe.audit.repository.AuditLogRepository;
import com.cloudsafe.auth.entity.User;
import com.cloudsafe.auth.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final jakarta.servlet.http.HttpServletRequest request;

    @Transactional
    public void log(String email, AuditLog.EventType eventType, String details) {
        User user = null;
        if (email != null && !email.isBlank()) {
            user = userRepository.findByEmail(email).orElse(null);
        }

        String ipAddress = getClientIp();

        AuditLog logEntry = AuditLog.builder()
                .user(user)
                .eventType(eventType)
                .details(details)
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(logEntry);
        log.info("Audit: user={} | event={} | ip={} | details={}",
                email, eventType, ipAddress, details);
    }

    private String getClientIp() {
        try {
            if (request == null || request.getRequestURI() == null) {
                return "SYSTEM";
            }
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return "SYSTEM"; // Fallback outside request scope (e.g. Scheduler)
        }
    }
}
