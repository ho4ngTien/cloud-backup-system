package com.cloudsafe.backup.presentation.controller;

import com.cloudsafe.backup.application.dto.*;
import com.cloudsafe.backup.application.service.BackupService;
import com.cloudsafe.backup.presentation.request.CommitBackupRequest;
import com.cloudsafe.backup.presentation.request.StartBackupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backups")
@RequiredArgsConstructor
@Tag(name = "Backup", description = "Start, commit and query backup jobs")
public class BackupController {

    private final BackupService backupService;

    @PostMapping("/start")
    @Operation(summary = "Initiate a backup session (returns jobId for the CLI)")
    public ResponseEntity<BackupJobDto> start(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody StartBackupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(backupService.startBackup(principal.getUsername(), request));
    }

    @PostMapping("/commit")
    @Operation(summary = "Finalize backup after CLI has uploaded all files to GCS")
    public ResponseEntity<BackupJobDto> commit(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CommitBackupRequest request) {
        return ResponseEntity.ok(backupService.commitBackup(principal.getUsername(), request));
    }

    @PostMapping("/{id}/fail")
    @Operation(summary = "Mark a backup job as failed (called by CLI on error)")
    public ResponseEntity<Void> fail(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        backupService.failBackup(id, principal.getUsername(), body.get("errorMessage"));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List backup history (paginated)")
    public ResponseEntity<Page<BackupJobDto>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(backupService.listJobs(principal.getUsername(), page, size));
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "List all versions of a backup job")
    public ResponseEntity<List<BackupVersionDto>> versions(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String id) {
        return ResponseEntity.ok(backupService.listVersions(id, principal.getUsername()));
    }

    @GetMapping("/versions/{versionId}/files")
    @Operation(summary = "List files in a specific backup version")
    public ResponseEntity<List<BackupFileDto>> files(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String versionId) {
        return ResponseEntity.ok(backupService.listFiles(versionId, principal.getUsername()));
    }
}
