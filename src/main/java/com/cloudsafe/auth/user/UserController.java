package com.cloudsafe.auth.user;

import com.cloudsafe.auth.user.dto.UpdateUserRequest;
import com.cloudsafe.auth.user.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile and admin endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userService.getByEmail(principal.getUsername()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile or password")
    public ResponseEntity<UserDto> updateMe(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(principal.getUsername(), request));
    }

    // ===== Admin endpoints =====

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] List all users")
    public ResponseEntity<List<UserDto>> listAll() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Enable a user account")
    public ResponseEntity<Void> enable(@PathVariable String id) {
        userService.setEnabled(id, true);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Disable a user account")
    public ResponseEntity<Void> disable(@PathVariable String id) {
        userService.setEnabled(id, false);
        return ResponseEntity.noContent().build();
    }
}
