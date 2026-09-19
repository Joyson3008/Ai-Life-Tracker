package com.joyson.ai_life_tracker.controller;

import com.joyson.ai_life_tracker.dto.MobileDeviceRegistrationRequest;
import com.joyson.ai_life_tracker.entity.MobileDevice;
import com.joyson.ai_life_tracker.entity.MobileSyncStatus;
import com.joyson.ai_life_tracker.service.MobileDeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/mobile")
public class MobileDeviceController {

    private final MobileDeviceService mobileDeviceService;

    public MobileDeviceController(MobileDeviceService mobileDeviceService) {
        this.mobileDeviceService = mobileDeviceService;
    }

    @PostMapping("/{userId}/register-device")
    public ResponseEntity<?> registerDevice(
            @PathVariable Long userId,
            @RequestBody MobileDeviceRegistrationRequest request
    ) {
        try {
            MobileDevice saved = mobileDeviceService.registerDevice(userId, request);
            return ResponseEntity.ok(Map.of(
                    "deviceId", saved.getDeviceId(),
                    "deviceName", saved.getDeviceName(),
                    "status", "registered"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Device registration failed"));
        }
    }

    @GetMapping("/{userId}/sync-status")
    public ResponseEntity<?> getSyncStatus(
            @PathVariable Long userId,
            @RequestParam String deviceId
    ) {
        try {
            Optional<MobileSyncStatus> result = mobileDeviceService.getSyncStatus(userId, deviceId);
            return result
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
