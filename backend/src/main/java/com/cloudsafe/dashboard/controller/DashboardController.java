package com.cloudsafe.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.cloudsafe.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard and statistics endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get user summary statistics (total devices, backups, used storage)")
    public ResponseEntity<Map<String, Object>> getSummary(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(dashboardService.getSummary(principal.getUsername()));
    }

    @GetMapping("/storage/statistics")
    @Operation(summary = "Get historical storage usage stats for charts")
    public ResponseEntity<Map<String, Object>> getStorageStatistics(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(dashboardService.getStorageStatistics(principal.getUsername()));
    }
}
