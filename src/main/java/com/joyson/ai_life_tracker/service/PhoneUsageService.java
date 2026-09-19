package com.joyson.ai_life_tracker.service;

import com.joyson.ai_life_tracker.dto.AppUsageData;
import com.joyson.ai_life_tracker.dto.PhoneUsageResponse;
import com.joyson.ai_life_tracker.dto.PhoneUsageSyncRequest;
import com.joyson.ai_life_tracker.entity.AppCategory;
import com.joyson.ai_life_tracker.entity.PhoneAppUsage;
import com.joyson.ai_life_tracker.entity.PhoneUsage;
import com.joyson.ai_life_tracker.entity.User;
import com.joyson.ai_life_tracker.repository.AppCategoryRepository;
import com.joyson.ai_life_tracker.repository.PhoneAppUsageRepository;
import com.joyson.ai_life_tracker.repository.PhoneUsageRepository;
import com.joyson.ai_life_tracker.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PhoneUsageService {

    private static final int DEFAULT_SCREEN_TIME_LIMIT = 240;

    private final PhoneUsageRepository phoneUsageRepository;
    private final PhoneAppUsageRepository phoneAppUsageRepository;
    private final UserRepository userRepository;
    private final AppCategoryRepository appCategoryRepository;

    public PhoneUsageService(
            PhoneUsageRepository phoneUsageRepository,
            PhoneAppUsageRepository phoneAppUsageRepository,
            UserRepository userRepository,
            AppCategoryRepository appCategoryRepository
    ) {
        this.phoneUsageRepository = phoneUsageRepository;
        this.phoneAppUsageRepository = phoneAppUsageRepository;
        this.userRepository = userRepository;
        this.appCategoryRepository = appCategoryRepository;
    }

    @Transactional
    public PhoneUsageResponse syncPhoneUsage(
            Long userId,
            PhoneUsageSyncRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        LocalDate date = request.getDate();
        validateDate(date);

        User user = getUser(userId);
    PhoneUsage phoneUsage = phoneUsageRepository
        .findByUserAndDate(user, date)
        .orElseGet(() -> phoneUsageRepository.save(new PhoneUsage(user, date)));

    if (phoneUsage.getId() == null) {
        phoneUsage = phoneUsageRepository.save(phoneUsage);
    }

        phoneUsage.setUser(user);
        phoneUsage.setDate(date);

        if (request.getScreenTimeLimit() != null) {
            phoneUsage.setScreenTimeLimit(request.getScreenTimeLimit());
        }

        List<AppUsageData> apps = request.getApps() == null ? List.of() : request.getApps();
        for (AppUsageData appUsageData : apps) {
            if (appUsageData == null) {
                continue;
            }
            upsertAppUsage(phoneUsage, appUsageData);
        }

        recalculateDailyUsage(userId, date, true);
        PhoneUsage saved = phoneUsageRepository.findByUserAndDate(user, date)
                .orElse(phoneUsage);

        List<PhoneAppUsage> appUsages = phoneAppUsageRepository.findByPhoneUsageOrderByMinutesDesc(saved);
        return PhoneUsageResponse.fromEntity(saved, appUsages);
    }

    @Transactional
    public PhoneUsage saveDailyUsage(
            Long userId,
            LocalDate date,
            Integer totalScreenTime,
            Integer productiveTime,
            Integer distractingTime,
            Integer screenTimeLimit
    ) {
        validateDate(date);
        validateUsageValues(
                totalScreenTime,
                productiveTime,
                distractingTime,
                screenTimeLimit
        );

        User user = getUser(userId);

        PhoneUsage phoneUsage = phoneUsageRepository
                .findByUserAndDate(user, date)
                .orElseGet(PhoneUsage::new);

        phoneUsage.setUser(user);
        phoneUsage.setDate(date);
        phoneUsage.setTotalScreenTime(totalScreenTime);
        phoneUsage.setProductiveTime(productiveTime);
        phoneUsage.setDistractingTime(distractingTime);

        if (screenTimeLimit != null) {
            phoneUsage.setScreenTimeLimit(screenTimeLimit);
        }

        int validLimit = getValidScreenTimeLimit(phoneUsage);

        phoneUsage.setWellbeingScore(
                calculateWellbeingScore(
                        totalScreenTime,
                        productiveTime,
                        distractingTime,
                        validLimit
                )
        );

        PhoneUsage savedUsage = phoneUsageRepository.save(phoneUsage);

        updateMostUsedApp(savedUsage);

        return phoneUsageRepository.save(savedUsage);
    }

    @Transactional(readOnly = true)
    public Optional<PhoneUsage> getTodayUsage(Long userId) {
        User user = getUser(userId);

        return phoneUsageRepository.findByUserAndDate(
                user,
                LocalDate.now()
        );
    }

        @Transactional(readOnly = true)
        public Optional<PhoneUsageResponse> getTodayUsageResponse(Long userId) {
        User user = getUser(userId);
        Optional<PhoneUsage> usage = phoneUsageRepository.findByUserAndDate(user, LocalDate.now());

        return usage.map(today -> PhoneUsageResponse.fromEntity(
            today,
            phoneAppUsageRepository.findByPhoneUsage_User_IdAndPhoneUsage_DateOrderByMinutesDesc(
                userId,
                today.getDate()
            )
        ));
        }

    @Transactional(readOnly = true)
    public Optional<PhoneUsage> getUsageByDate(
            Long userId,
            LocalDate date
    ) {
        validateDate(date);

        return phoneUsageRepository.findByUserAndDate(
                getUser(userId),
                date
        );
    }

    @Transactional(readOnly = true)
    public List<PhoneUsage> getUsageHistory(Long userId) {
        return phoneUsageRepository.findByUserOrderByDateDesc(
                getUser(userId)
        );
    }

    @Transactional(readOnly = true)
    public List<PhoneUsage> getUsageBetweenDates(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateDateRange(startDate, endDate);

        return phoneUsageRepository
                .findByUserAndDateBetweenOrderByDateAsc(
                        getUser(userId),
                        startDate,
                        endDate
                );
    }

    @Transactional
    public PhoneAppUsage saveAppUsage(
            Long userId,
            LocalDate date,
            String appName,
            Integer usageMinutes,
            String category
    ) {
        validateDate(date);
        validateAppUsage(appName, usageMinutes);

        User user = getUser(userId);

        PhoneUsage phoneUsage = phoneUsageRepository
                .findByUserAndDate(user, date)
                .orElseGet(() -> createEmptyDailyUsage(user, date));

        String cleanedAppName = appName.trim();
        String cleanedCategory = normalizeCategory(category);

        Optional<PhoneAppUsage> existingAppUsage =
                phoneAppUsageRepository.findByPhoneUsageAndAppName(
                        phoneUsage,
                        cleanedAppName
                );

        PhoneAppUsage appUsage = existingAppUsage
                .orElseGet(PhoneAppUsage::new);

        appUsage.setPhoneUsage(phoneUsage);
        appUsage.setAppName(cleanedAppName);
        appUsage.setCategory(cleanedCategory);
        appUsage.setMinutes(usageMinutes);

        PhoneAppUsage savedAppUsage = phoneAppUsageRepository.save(appUsage);

        recalculateDailyUsage(userId, date);

        return savedAppUsage;
    }

    @Transactional(readOnly = true)
    public List<PhoneAppUsage> getAppUsageForDate(
            Long userId,
            LocalDate date
    ) {
        validateDate(date);

        return phoneAppUsageRepository
            .findByPhoneUsage_User_IdAndPhoneUsage_DateOrderByMinutesDesc(userId, date);
    }

    @Transactional(readOnly = true)
    public List<PhoneAppUsage> getAppUsageHistory(Long userId) {
        return phoneAppUsageRepository
                .findByPhoneUsage_UserOrderByPhoneUsage_DateDesc(
                        getUser(userId)
                );
    }

    @Transactional
    public PhoneUsage recalculateDailyUsage(
            Long userId,
            LocalDate date
    ) {
        return recalculateDailyUsage(userId, date, false);
    }

    @Transactional
    public PhoneUsage recalculateDailyUsage(
            Long userId,
            LocalDate date,
            boolean persistEmptyRecord
    ) {
        validateDate(date);

        User user = getUser(userId);

        PhoneUsage phoneUsage = phoneUsageRepository
                .findByUserAndDate(user, date)
                .orElseGet(() -> persistEmptyRecord ? new PhoneUsage(user, date) : createEmptyDailyUsage(user, date));

        if (phoneUsage.getId() == null) {
            phoneUsage.setUser(user);
            phoneUsage.setDate(date);
        }

        List<PhoneAppUsage> appUsages = phoneAppUsageRepository
                .findByPhoneUsageOrderByMinutesDesc(phoneUsage);

        int totalScreenTime = 0;
        int productiveTime = 0;
        int distractingTime = 0;

        for (PhoneAppUsage appUsage : appUsages) {
            int minutes = appUsage.getMinutes() == null
                    ? 0
                    : appUsage.getMinutes();

            totalScreenTime += minutes;

            if ("PRODUCTIVE".equalsIgnoreCase(appUsage.getCategory())) {
                productiveTime += minutes;
            } else if ("DISTRACTING".equalsIgnoreCase(appUsage.getCategory())) {
                distractingTime += minutes;
            }
        }

        phoneUsage.setTotalScreenTime(totalScreenTime);
        phoneUsage.setProductiveTime(productiveTime);
        phoneUsage.setDistractingTime(distractingTime);

        int validLimit = getValidScreenTimeLimit(phoneUsage);

        phoneUsage.setWellbeingScore(
                calculateWellbeingScore(
                        totalScreenTime,
                        productiveTime,
                        distractingTime,
                        validLimit
                )
        );

        if (appUsages.isEmpty()) {
            phoneUsage.setMostUsedApp(null);
        } else {
            phoneUsage.setMostUsedApp(appUsages.get(0).getAppName());
        }

        if (phoneUsage.getScreenTimeLimit() == null || phoneUsage.getScreenTimeLimit() <= 0) {
            phoneUsage.setScreenTimeLimit(DEFAULT_SCREEN_TIME_LIMIT);
        }

        return phoneUsageRepository.save(phoneUsage);
    }

    private void upsertAppUsage(PhoneUsage phoneUsage, AppUsageData appUsageData) {
        if (appUsageData == null) {
            return;
        }

        if (phoneUsage.getId() == null) {
            phoneUsage = phoneUsageRepository.save(phoneUsage);
        }

        String cleanAppName = sanitizeRequired(appUsageData.getAppName(), "App name is required.");
        String packageName = sanitizeOptional(appUsageData.getPackageName());
        Integer usageMinutes = appUsageData.getUsageMinutes() == null ? 0 : appUsageData.getUsageMinutes();
        if (usageMinutes < 0) {
            throw new IllegalArgumentException("Usage minutes cannot be negative.");
        }

        String category = resolveCategory(cleanAppName, packageName, appUsageData.getCategory());

        Optional<PhoneAppUsage> existingAppUsage = packageName != null && !packageName.isBlank()
                ? phoneAppUsageRepository.findByPhoneUsageAndPackageName(phoneUsage, packageName)
                : phoneAppUsageRepository.findByPhoneUsageAndAppName(phoneUsage, cleanAppName);

        PhoneAppUsage appUsage = existingAppUsage.orElseGet(PhoneAppUsage::new);
        appUsage.setPhoneUsage(phoneUsage);
        appUsage.setAppName(cleanAppName);
        appUsage.setPackageName(packageName);
        appUsage.setCategory(category);
        appUsage.setMinutes(usageMinutes);
        phoneAppUsageRepository.save(appUsage);
    }

    private String resolveCategory(String appName, String packageName, String suppliedCategory) {
        if (suppliedCategory != null && !suppliedCategory.trim().isEmpty()) {
            return normalizeCategory(suppliedCategory);
        }

        if (packageName != null && !packageName.isBlank()) {
            Optional<AppCategory> byPackage = appCategoryRepository.findByPackageName(packageName.trim());
            if (byPackage.isPresent()) {
                return normalizeCategory(byPackage.get().getCategory());
            }
        }

        if (appName != null && !appName.isBlank()) {
            Optional<AppCategory> byAppName = appCategoryRepository.findByAppName(appName.trim());
            if (byAppName.isPresent()) {
                return normalizeCategory(byAppName.get().getCategory());
            }
        }

        String normalizedPackage = packageName == null ? "" : packageName.toLowerCase();
        String normalizedAppName = appName == null ? "" : appName.toLowerCase();
        String combined = normalizedPackage + " " + normalizedAppName;

        if (combined.contains("instagram") || combined.contains("youtube") || combined.contains("facebook") ||
                combined.contains("twitter") || combined.contains("tiktok") || combined.contains("reddit") ||
                combined.contains("snapchat") || combined.contains("discord") || combined.contains("netflix") ||
                combined.contains("whatsapp") || combined.contains("telegram") || combined.contains("hulu") ||
                combined.contains("spotify") || combined.contains("prime video") || combined.contains("chrome")) {
            return "DISTRACTING";
        }

        if (combined.contains("vscode") || combined.contains("code") || combined.contains("docs") ||
                combined.contains("notion") || combined.contains("git") || combined.contains("github") ||
                combined.contains("excel") || combined.contains("word") || combined.contains("slides") ||
                combined.contains("drive") || combined.contains("calendar") || combined.contains("meeting") ||
                combined.contains("study") || combined.contains("android studio")) {
            return "PRODUCTIVE";
        }

        return "OTHER";
    }

    private PhoneUsage createEmptyDailyUsage(
            User user,
            LocalDate date
    ) {
        PhoneUsage phoneUsage = new PhoneUsage();

        phoneUsage.setUser(user);
        phoneUsage.setDate(date);
        phoneUsage.setTotalScreenTime(0);
        phoneUsage.setProductiveTime(0);
        phoneUsage.setDistractingTime(0);
        phoneUsage.setScreenTimeLimit(DEFAULT_SCREEN_TIME_LIMIT);
        phoneUsage.setWellbeingScore(100);

        return phoneUsageRepository.save(phoneUsage);
    }

    private void updateMostUsedApp(PhoneUsage phoneUsage) {
        List<PhoneAppUsage> appUsages = phoneAppUsageRepository
                .findByPhoneUsageOrderByMinutesDesc(phoneUsage);

        if (appUsages.isEmpty()) {
            phoneUsage.setMostUsedApp(null);
            return;
        }

        phoneUsage.setMostUsedApp(appUsages.get(0).getAppName());
    }

    public User getUserById(Long userId) {
        return getUser(userId);
    }

    private User getUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required.");
        }

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + userId)
                );
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
    }

    private void validateDateRange(
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateDate(startDate);
        validateDate(endDate);

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date."
            );
        }
    }

    private void validateUsageValues(
            Integer totalScreenTime,
            Integer productiveTime,
            Integer distractingTime,
            Integer screenTimeLimit
    ) {
        if (totalScreenTime == null || totalScreenTime < 0) {
            throw new IllegalArgumentException(
                    "Total screen time cannot be negative."
            );
        }

        if (productiveTime == null || productiveTime < 0) {
            throw new IllegalArgumentException(
                    "Productive time cannot be negative."
            );
        }

        if (distractingTime == null || distractingTime < 0) {
            throw new IllegalArgumentException(
                    "Distracting time cannot be negative."
            );
        }

        if (productiveTime + distractingTime > totalScreenTime) {
            throw new IllegalArgumentException(
                    "Productive and distracting time cannot exceed total screen time."
            );
        }

        if (screenTimeLimit != null && screenTimeLimit <= 0) {
            throw new IllegalArgumentException(
                    "Screen-time limit must be greater than zero."
            );
        }
    }

    private void validateAppUsage(
            String appName,
            Integer usageMinutes
    ) {
        if (appName == null || appName.trim().isEmpty()) {
            throw new IllegalArgumentException("App name is required.");
        }

        if (usageMinutes == null || usageMinutes < 0) {
            throw new IllegalArgumentException(
                    "Usage minutes cannot be negative."
            );
        }
    }

    private int getValidScreenTimeLimit(PhoneUsage phoneUsage) {
        Integer screenTimeLimit = phoneUsage.getScreenTimeLimit();

        if (screenTimeLimit == null || screenTimeLimit <= 0) {
            phoneUsage.setScreenTimeLimit(DEFAULT_SCREEN_TIME_LIMIT);
            return DEFAULT_SCREEN_TIME_LIMIT;
        }

        return screenTimeLimit;
    }

    private String sanitizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String sanitizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String sanitized = value.trim();
        return sanitized.isEmpty() ? null : sanitized;
    }

    private String normalizeCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "OTHER";
        }

        return category.trim().toUpperCase();
    }

    private int calculateWellbeingScore(
            int totalScreenTime,
            int productiveTime,
            int distractingTime,
            int screenTimeLimit
    ) {
        double score = 100.0;

        if (totalScreenTime > screenTimeLimit) {
            double exceededPercentage =
                    (totalScreenTime - screenTimeLimit)
                            / (double) screenTimeLimit;

            score -= exceededPercentage * 40.0;
        }

        if (totalScreenTime > 0) {
            double distractingPercentage =
                    distractingTime / (double) totalScreenTime;

            score -= distractingPercentage * 40.0;

            double productivePercentage =
                    productiveTime / (double) totalScreenTime;

            score += productivePercentage * 10.0;
        }

        return Math.max(0, Math.min(100, (int) Math.round(score)));
    }
}