package com.cloudsafe.restore;

import com.cloudsafe.restore.dto.RestoreJobDto;
import com.cloudsafe.restore.dto.StartRestoreRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/restore")
@RequiredArgsConstructor
@Tag(name = "Restore", description = "Initiate and track restore jobs")
public class RestoreController {

    private final RestoreService restoreService;

    @PostMapping("/start")
    @Operation(summary = "Start a restore – returns signed download URLs for the CLI")
    public ResponseEntity<RestoreJobDto> start(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody StartRestoreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restoreService.startRestore(principal.getUsername(), request));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Mark a restore as completed or failed (called by CLI)")
    public ResponseEntity<Void> complete(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        boolean success = Boolean.TRUE.equals(body.get("success"));
        String errorMsg = (String) body.get("errorMessage");
        restoreService.completeRestore(id, principal.getUsername(), success, errorMsg);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    @Operation(summary = "List restore history (paginated)")
    public ResponseEntity<Page<RestoreJobDto>> history(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(restoreService.listHistory(principal.getUsername(), page, size));
    }
}
