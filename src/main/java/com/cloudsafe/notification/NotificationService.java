package com.cloudsafe.notification;

import com.cloudsafe.model.*;
import com.cloudsafe.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;

/**
 * Handles all outgoing email notifications via Spring Mail + Thymeleaf templates.
 * All send methods are @Async to avoid blocking the HTTP request thread.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationRepository notificationRepository;

    @Async
    public void sendBackupSuccess(User user, BackupJob job) {
        Context ctx = new Context();
        ctx.setVariable("displayName", user.getDisplayName());
        ctx.setVariable("jobId", job.getId());
        ctx.setVariable("fileCount", job.getFileCount());
        ctx.setVariable("totalSizeMb", String.format("%.2f", job.getTotalSizeBytes() / 1_048_576.0));
        ctx.setVariable("backupType", job.getBackupType().name());
        ctx.setVariable("completedAt", job.getCompletedAt() != null
                ? job.getCompletedAt().toString() : Instant.now().toString());
        ctx.setVariable("sourcePath", job.getSourcePath());

        String html = templateEngine.process("email/backup-success", ctx);
        sendEmail(user.getEmail(), "✅ CloudSafe – Backup thành công", html);
        saveNotification(user, Notification.EventType.BACKUP_SUCCESS,
                "Backup job " + job.getId() + " completed successfully.");
    }

    @Async
    public void sendBackupFailed(User user, BackupJob job, String errorMessage) {
        Context ctx = new Context();
        ctx.setVariable("displayName", user.getDisplayName());
        ctx.setVariable("jobId", job.getId());
        ctx.setVariable("errorMessage", errorMessage);
        ctx.setVariable("sourcePath", job.getSourcePath());

        String html = templateEngine.process("email/backup-failed", ctx);
        sendEmail(user.getEmail(), "❌ CloudSafe – Backup thất bại", html);
        saveNotification(user, Notification.EventType.BACKUP_FAILED,
                "Backup job " + job.getId() + " failed: " + errorMessage);
    }

    @Async
    public void sendRestoreSuccess(User user, RestoreHistory restore) {
        Context ctx = new Context();
        ctx.setVariable("displayName", user.getDisplayName());
        ctx.setVariable("restoreId", restore.getId());
        ctx.setVariable("targetPath", restore.getTargetPath());
        ctx.setVariable("completedAt", Instant.now().toString());

        String html = templateEngine.process("email/restore-success", ctx);
        sendEmail(user.getEmail(), "✅ CloudSafe – Restore thành công", html);
        saveNotification(user, Notification.EventType.RESTORE_SUCCESS,
                "Restore " + restore.getId() + " completed successfully.");
    }

    @Async
    public void sendStorageWarning(User user) {
        double usedPct = user.getStorageQuotaBytes() == 0 ? 100
                : (user.getStorageUsedBytes() * 100.0 / user.getStorageQuotaBytes());

        Context ctx = new Context();
        ctx.setVariable("displayName", user.getDisplayName());
        ctx.setVariable("usedPercent", String.format("%.1f", usedPct));
        ctx.setVariable("usedGb", String.format("%.2f", user.getStorageUsedBytes() / 1_073_741_824.0));
        ctx.setVariable("quotaGb", String.format("%.2f", user.getStorageQuotaBytes() / 1_073_741_824.0));

        String html = templateEngine.process("email/storage-warning", ctx);
        sendEmail(user.getEmail(), "⚠️ CloudSafe – Dung lượng sắp đầy", html);
        saveNotification(user, Notification.EventType.STORAGE_WARNING,
                "Storage usage at " + String.format("%.1f", usedPct) + "%");
    }

    // =================== private helpers ===================

    private void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to: {} | subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private void saveNotification(User user, Notification.EventType type, String message) {
        Notification n = Notification.builder()
                .user(user)
                .eventType(type)
                .message(message)
                .status(Notification.NotificationStatus.SENT)
                .sentAt(Instant.now())
                .build();
        notificationRepository.save(n);
    }
}
