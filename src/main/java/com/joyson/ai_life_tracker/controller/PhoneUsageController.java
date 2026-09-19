package com.joyson.ai_life_tracker.controller;

import com.joyson.ai_life_tracker.dto.PhoneUsageResponse;
import com.joyson.ai_life_tracker.dto.PhoneUsageSyncRequest;
import com.joyson.ai_life_tracker.entity.PhoneAppUsage;
import com.joyson.ai_life_tracker.entity.PhoneUsage;
import com.joyson.ai_life_tracker.entity.User;
import com.joyson.ai_life_tracker.service.MobileDeviceService;
import com.joyson.ai_life_tracker.service.PhoneUsageService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/phone-usage")
public class PhoneUsageController {

    private final PhoneUsageService phoneUsageService;
    private final MobileDeviceService mobileDeviceService;

    public PhoneUsageController(
            PhoneUsageService phoneUsageService,
            MobileDeviceService mobileDeviceService
    ) {
        this.phoneUsageService = phoneUsageService;
        this.mobileDeviceService = mobileDeviceService;
    }


    // =========================================================
    // SAVE / UPDATE DAILY PHONE USAGE
    // =========================================================

    /**
     * Save or update daily phone usage summary.
     *
     * POST /api/phone-usage/{userId}
     *
     * Example request:
     *
     * {
     *     "date": "2026-08-23",
     *     "totalScreenTime": 240,
     *     "productiveTime": 100,
     *     "distractingTime": 140,
     *     "screenTimeLimit": 240
     * }
     */
    @PostMapping("/{userId}")
    public ResponseEntity<PhoneUsage> saveDailyUsage(
            @PathVariable Long userId,
            @RequestBody PhoneUsageRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request body is required."
            );
        }

        if (request.getDate() == null) {
            throw new IllegalArgumentException(
                    "Date is required."
            );
        }

        PhoneUsage usage =
                phoneUsageService.saveDailyUsage(
                        userId,
                        request.getDate(),
                        request.getTotalScreenTime(),
                        request.getProductiveTime(),
                        request.getDistractingTime(),
                        request.getScreenTimeLimit()
                );

        return ResponseEntity.ok(usage);
    }

    // =========================================================
    // BULK SYNC FROM ANDROID APP
    // =========================================================

    @PostMapping("/{userId}/sync")
    public ResponseEntity<?> syncPhoneUsage(
            @PathVariable Long userId,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @RequestHeader(value = "X-Device-Token", required = false) String registrationToken,
            @RequestBody PhoneUsageSyncRequest request
    ) {
        try {
            if (request == null) {
                throw new IllegalArgumentException("Request body is required.");
            }
            if (request.getDate() == null) {
                throw new IllegalArgumentException("Date is required.");
            }
            if (request.getApps() == null) {
                request.setApps(List.of());
            }

            if (!mobileDeviceService.validateDeviceAccess(userId, deviceId, registrationToken)) {
                mobileDeviceService.updateSyncStatus(
                        phoneUsageService.getUserById(userId),
                        deviceId,
                        "FAILED",
                        "Device not authorized for this user"
                );
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized device for this user."));
            }

            mobileDeviceService.updateSyncStatus(
                    phoneUsageService.getUserById(userId),
                    deviceId,
                    "SUCCESS",
                    null
            );

            PhoneUsageResponse response = phoneUsageService.syncPhoneUsage(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Phone usage sync failed"));
        }
    }


    // =========================================================
    // GET TODAY'S USAGE
    // =========================================================

    /**
     * Get today's phone usage.
     *
     * GET /api/phone-usage/{userId}/today
     */
    @GetMapping("/{userId}/today")
        public ResponseEntity<?> getTodayUsage(
            @PathVariable Long userId,
            @RequestParam(required = false) LocalDate date
    ) {

        Optional<PhoneUsageResponse> usage = date == null
                ? phoneUsageService.getTodayUsageResponse(userId)
                : phoneUsageService.getUsageResponse(userId, date);

        return usage
            .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }


    // =========================================================
    // GET USAGE BY DATE
    // =========================================================

    /**
     * Get phone usage for a specific date.
     *
     * GET /api/phone-usage/{userId}/date/2026-08-23
     */
    @GetMapping("/{userId}/date/{date}")
    public ResponseEntity<PhoneUsage> getUsageByDate(
            @PathVariable Long userId,
            @PathVariable LocalDate date
    ) {

        Optional<PhoneUsage> usage =
                phoneUsageService.getUsageByDate(
                        userId,
                        date
                );

        return usage
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }


    // =========================================================
    // GET USAGE HISTORY
    // =========================================================

    /**
     * Get complete phone usage history.
     *
     * GET /api/phone-usage/{userId}/history
     */
    @GetMapping("/{userId}/history")
    public ResponseEntity<List<PhoneUsage>> getUsageHistory(
            @PathVariable Long userId
    ) {

        List<PhoneUsage> usage =
                phoneUsageService.getUsageHistory(userId);

        return ResponseEntity.ok(usage);
    }


    // =========================================================
    // GET USAGE BETWEEN DATES
    // =========================================================

    /**
     * Get phone usage between two dates.
     *
     * GET /api/phone-usage/{userId}/range
     *
     * Example:
     *
     * /api/phone-usage/1/range
     * ?startDate=2026-08-17
     * &endDate=2026-08-23
     */
    @GetMapping("/{userId}/range")
    public ResponseEntity<List<PhoneUsage>> getUsageBetweenDates(
            @PathVariable Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {

        List<PhoneUsage> usage =
                phoneUsageService.getUsageBetweenDates(
                        userId,
                        startDate,
                        endDate
                );

        return ResponseEntity.ok(usage);
    }


    // =========================================================
    // SAVE APP USAGE
    // =========================================================

    /**
     * Save or update individual application usage.
     *
     * POST /api/phone-usage/{userId}/apps
     *
     * Example request:
     *
     * {
     *     "date": "2026-08-23",
     *     "appName": "Instagram",
     *     "usageMinutes": 90,
     *     "category": "DISTRACTING"
     * }
     */
    @PostMapping("/{userId}/apps")
    public ResponseEntity<PhoneAppUsage> saveAppUsage(
            @PathVariable Long userId,
            @RequestBody PhoneAppUsageRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request body is required."
            );
        }

        if (request.getDate() == null) {
            throw new IllegalArgumentException(
                    "Date is required."
            );
        }

        if (request.getAppName() == null ||
                request.getAppName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "App name is required."
            );
        }

        if (request.getUsageMinutes() == null ||
                request.getUsageMinutes() < 0) {

            throw new IllegalArgumentException(
                    "Usage minutes cannot be negative."
            );
        }

        PhoneAppUsage appUsage =
                phoneUsageService.saveAppUsage(
                        userId,
                        request.getDate(),
                        request.getAppName(),
                        request.getUsageMinutes(),
                        request.getCategory()
                );

        return ResponseEntity.ok(appUsage);
    }


    // =========================================================
    // GET APP USAGE FOR A DATE
    // =========================================================

    /**
     * Get all application usage for a particular day.
     *
     * GET /api/phone-usage/{userId}/apps/2026-08-23
     */
    @GetMapping("/{userId}/apps/{date}")
    public ResponseEntity<List<PhoneAppUsage>> getAppUsageForDate(
            @PathVariable Long userId,
            @PathVariable LocalDate date
    ) {

        List<PhoneAppUsage> appUsage =
                phoneUsageService.getAppUsageForDate(
                        userId,
                        date
                );

        return ResponseEntity.ok(appUsage);
    }


    // =========================================================
    // GET APP USAGE HISTORY
    // =========================================================

    /**
     * Get all individual application usage records
     * belonging to the user.
     *
     * GET /api/phone-usage/{userId}/apps
     */
    @GetMapping("/{userId}/apps")
    public ResponseEntity<List<PhoneAppUsage>> getAppUsageHistory(
            @PathVariable Long userId
    ) {

        List<PhoneAppUsage> appUsage =
                phoneUsageService.getAppUsageHistory(userId);

        return ResponseEntity.ok(appUsage);
    }


    // =========================================================
    // RECALCULATE DAILY USAGE
    // =========================================================

    /**
     * Recalculate the daily PhoneUsage summary
     * from individual PhoneAppUsage records.
     *
     * POST /api/phone-usage/{userId}/recalculate/{date}
     *
     * Example:
     *
     * POST /api/phone-usage/1/recalculate/2026-08-23
     */
    @PostMapping("/{userId}/recalculate/{date}")
    public ResponseEntity<PhoneUsage> recalculateDailyUsage(
            @PathVariable Long userId,
            @PathVariable LocalDate date
    ) {
        PhoneUsage usage = phoneUsageService.recalculateDailyUsage(userId, date);
        return ResponseEntity.ok(usage);
    }


    // =========================================================
    // DAILY PHONE USAGE REQUEST
    // =========================================================

    /**
     * Request body used when saving daily phone usage.
     */
    public static class PhoneUsageRequest {

        private LocalDate date;

        private Integer totalScreenTime;

        private Integer productiveTime;

        private Integer distractingTime;

        private Integer screenTimeLimit;


        public PhoneUsageRequest() {
        }


        // =====================================================
        // GETTERS
        // =====================================================

        public LocalDate getDate() {
            return date;
        }

        public Integer getTotalScreenTime() {
            return totalScreenTime;
        }

        public Integer getProductiveTime() {
            return productiveTime;
        }

        public Integer getDistractingTime() {
            return distractingTime;
        }

        public Integer getScreenTimeLimit() {
            return screenTimeLimit;
        }


        // =====================================================
        // SETTERS
        // =====================================================

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public void setTotalScreenTime(
                Integer totalScreenTime
        ) {
            this.totalScreenTime = totalScreenTime;
        }

        public void setProductiveTime(
                Integer productiveTime
        ) {
            this.productiveTime = productiveTime;
        }

        public void setDistractingTime(
                Integer distractingTime
        ) {
            this.distractingTime = distractingTime;
        }

        public void setScreenTimeLimit(
                Integer screenTimeLimit
        ) {
            this.screenTimeLimit = screenTimeLimit;
        }
    }


    // =========================================================
    // APP USAGE REQUEST
    // =========================================================

    /**
     * Request body used when saving individual
     * application usage.
     */
    public static class PhoneAppUsageRequest {

        private LocalDate date;

        private String appName;

        private Integer usageMinutes;

        private String category;


        public PhoneAppUsageRequest() {
        }


        // =====================================================
        // GETTERS
        // =====================================================

        public LocalDate getDate() {
            return date;
        }

        public String getAppName() {
            return appName;
        }

        public Integer getUsageMinutes() {
            return usageMinutes;
        }

        public String getCategory() {
            return category;
        }


        // =====================================================
        // SETTERS
        // =====================================================

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public void setUsageMinutes(
                Integer usageMinutes
        ) {
            this.usageMinutes = usageMinutes;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }
}