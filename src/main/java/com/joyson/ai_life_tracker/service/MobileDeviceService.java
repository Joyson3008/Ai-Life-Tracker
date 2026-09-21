package com.joyson.ai_life_tracker.service;

import com.joyson.ai_life_tracker.dto.MobileDeviceRegistrationRequest;
import com.joyson.ai_life_tracker.entity.MobileDevice;
import com.joyson.ai_life_tracker.entity.MobileSyncStatus;
import com.joyson.ai_life_tracker.entity.User;
import com.joyson.ai_life_tracker.repository.MobileDeviceRepository;
import com.joyson.ai_life_tracker.repository.MobileSyncStatusRepository;
import com.joyson.ai_life_tracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MobileDeviceService {

    private final UserRepository userRepository;
    private final MobileDeviceRepository mobileDeviceRepository;
    private final MobileSyncStatusRepository mobileSyncStatusRepository;

    public MobileDeviceService(
            UserRepository userRepository,
            MobileDeviceRepository mobileDeviceRepository,
            MobileSyncStatusRepository mobileSyncStatusRepository
    ) {
        this.userRepository = userRepository;
        this.mobileDeviceRepository = mobileDeviceRepository;
        this.mobileSyncStatusRepository = mobileSyncStatusRepository;
    }

    @Transactional
    public MobileDevice registerDevice(Long userId, MobileDeviceRegistrationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration payload is required.");
        }
        if (request.getDeviceId() == null || request.getDeviceId().isBlank()) {
            throw new IllegalArgumentException("Device ID is required.");
        }
        if (request.getRegistrationToken() == null || request.getRegistrationToken().isBlank()) {
            throw new IllegalArgumentException("Registration token is required.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Optional<MobileDevice> existing = mobileDeviceRepository.findByDeviceId(request.getDeviceId());
        if (existing.isPresent()) {
            MobileDevice device = existing.get();
            if (!device.getUser().getId().equals(userId)) {
                throw new IllegalStateException("This device is already associated with another account.");
            }
            device.setDeviceName(request.getDeviceName());
            device.setRegistrationToken(request.getRegistrationToken());
            device.setActive(true);
            device.setLastSeenAt(LocalDateTime.now());
            return mobileDeviceRepository.save(device);
        }

        MobileDevice device = new MobileDevice(user, request.getDeviceId(), request.getDeviceName(), request.getRegistrationToken());
        MobileDevice saved = mobileDeviceRepository.save(device);
        updateSyncStatus(user, request.getDeviceId(), "CONNECTED", null);
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<MobileDevice> findDeviceForUser(Long userId, String deviceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return mobileDeviceRepository.findByDeviceIdAndUser(deviceId, user);
    }

    @Transactional(readOnly = true)
    public boolean validateDeviceAccess(Long userId, String deviceId, String registrationToken) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("Device ID is required.");
        }
        if (registrationToken == null || registrationToken.isBlank()) {
            throw new IllegalArgumentException("Registration token is required.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Optional<MobileDevice> device = mobileDeviceRepository.findByDeviceIdAndUser(deviceId, user);
        if (device.isEmpty()) {
            return false;
        }

        MobileDevice mobileDevice = device.get();
        if (!mobileDevice.isActive()) {
            return false;
        }

        return registrationToken.equals(mobileDevice.getRegistrationToken());
    }

    @Transactional
    public void updateSyncStatus(User user, String deviceId, String status, String errorMessage) {
        if (user == null) {
            return;
        }

        MobileSyncStatus syncStatus = mobileSyncStatusRepository.findByUserAndDeviceId(user, deviceId)
                .orElseGet(() -> new MobileSyncStatus(user, deviceId));

        syncStatus.setUser(user);
        syncStatus.setDeviceId(deviceId);
        syncStatus.setLastSyncAttempt(LocalDateTime.now());
        syncStatus.setSyncStatus(status);
        syncStatus.setErrorMessage(errorMessage);
        if ("SUCCESS".equalsIgnoreCase(status)) {
            syncStatus.setLastSuccessfulSync(LocalDateTime.now());
        }
        mobileSyncStatusRepository.save(syncStatus);
    }

    @Transactional
    public void updateSyncStatusSafely(Long userId, String deviceId, String status, String errorMessage) {
        if (userId == null || deviceId == null || deviceId.isBlank()) {
            return;
        }
        userRepository.findById(userId).ifPresent(user -> updateSyncStatus(user, deviceId, status, errorMessage));
    }

    @Transactional(readOnly = true)
    public Optional<MobileSyncStatus> getSyncStatus(Long userId, String deviceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return mobileSyncStatusRepository.findByUserAndDeviceId(user, deviceId);
    }
}
