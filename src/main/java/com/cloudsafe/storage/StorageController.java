package com.cloudsafe.storage;

import com.cloudsafe.exception.ResourceNotFoundException;
import com.cloudsafe.model.User;
import com.cloudsafe.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Tag(name = "Storage", description = "Storage usage and signed URL generation")
public class StorageController {

    private final StorageService storageService;
    private final UserRepository userRepository;

    @GetMapping("/usage")
    @Operation(summary = "Get current user storage usage and quota")
    public ResponseEntity<Map<String, Object>> usage(
            @AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.getUsername()));
        return ResponseEntity.ok(Map.of(
                "usedBytes", user.getStorageUsedBytes(),
                "quotaBytes", user.getStorageQuotaBytes(),
                "availableBytes", user.getStorageQuotaBytes() - user.getStorageUsedBytes(),
                "usedPercent", user.getStorageQuotaBytes() == 0 ? 0
                        : (user.getStorageUsedBytes() * 100.0 / user.getStorageQuotaBytes())
        ));
    }

    @GetMapping("/signed-url")
    @Operation(summary = "Generate a signed download URL for a storage key (valid 15 min)")
    public ResponseEntity<Map<String, String>> signedUrl(
            @RequestParam String storageKey) {
        URL url = storageService.generateSignedUrl(storageKey, Duration.ofMinutes(15));
        return ResponseEntity.ok(Map.of("url", url.toString(), "expiresInMinutes", "15"));
    }

    @GetMapping("/list")
    @Operation(summary = "List objects under a storage prefix")
    public ResponseEntity<List<String>> list(@RequestParam String prefix) {
        return ResponseEntity.ok(storageService.listObjects(prefix));
    }
}
