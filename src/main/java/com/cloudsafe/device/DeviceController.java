package com.cloudsafe.device;

import com.cloudsafe.device.dto.CreateDeviceRequest;
import com.cloudsafe.device.dto.DeviceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "Device Management", description = "Register and manage client devices")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    @Operation(summary = "Register a new client device")
    public ResponseEntity<DeviceDto> register(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateDeviceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deviceService.register(principal.getUsername(), request));
    }

    @GetMapping
    @Operation(summary = "List all devices for the current user")
    public ResponseEntity<List<DeviceDto>> list(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(deviceService.listByUser(principal.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device details")
    public ResponseEntity<DeviceDto> get(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String id) {
        return ResponseEntity.ok(deviceService.getById(id, principal.getUsername()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update device settings (watch path, auto-backup)")
    public ResponseEntity<DeviceDto> update(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String id,
            @Valid @RequestBody CreateDeviceRequest request) {
        return ResponseEntity.ok(deviceService.update(id, principal.getUsername(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Unregister (delete) a device")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String id) {
        deviceService.delete(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/heartbeat")
    @Operation(summary = "Update device online status (called by the agent)")
    public ResponseEntity<Void> heartbeat(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String id,
            @RequestBody Map<String, Boolean> body) {
        boolean online = Boolean.TRUE.equals(body.get("online"));
        deviceService.heartbeat(id, principal.getUsername(), online);
        return ResponseEntity.noContent().build();
    }
}
