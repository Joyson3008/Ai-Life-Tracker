package com.joyson.ai_life_tracker.repository;

import com.joyson.ai_life_tracker.entity.PhoneAppUsage;
import com.joyson.ai_life_tracker.entity.PhoneUsage;
import com.joyson.ai_life_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhoneAppUsageRepository extends JpaRepository<PhoneAppUsage, Long> {

    // =========================================================
    // FIND BY PHONE USAGE RECORD
    // =========================================================

    /**
     * Get all app usage records belonging to a particular daily PhoneUsage record,
     * ordered by usage time descending (most used first).
     */
    List<PhoneAppUsage> findByPhoneUsageOrderByMinutesDesc(PhoneUsage phoneUsage);

    /**
     * Find an app usage record by its parent PhoneUsage and package name.
     */
    Optional<PhoneAppUsage> findByPhoneUsageAndPackageName(PhoneUsage phoneUsage, String packageName);

    /**
     * Find an app usage record by its parent PhoneUsage and app name.
     */
    Optional<PhoneAppUsage> findByPhoneUsageAndAppName(PhoneUsage phoneUsage, String appName);

    // =========================================================
    // FIND BY USER AND DATE
    // =========================================================

    /**
     * Get all app usage records for a user on a given date, ordered by usage minutes descending.
     */
    List<PhoneAppUsage> findByPhoneUsage_User_IdAndPhoneUsage_DateOrderByMinutesDesc(Long userId, LocalDate date);

    /**
     * Get all app usage records for a user entity on a given date.
     */
    List<PhoneAppUsage> findByPhoneUsage_UserAndPhoneUsage_DateOrderByMinutesDesc(User user, LocalDate date);

    // =========================================================
    // FIND BY DATE RANGE & HISTORY
    // =========================================================

    /**
     * Get all app usage records for a user between two dates.
     */
    List<PhoneAppUsage> findByPhoneUsage_User_IdAndPhoneUsage_DateBetweenOrderByPhoneUsage_DateAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * Get all app usage records for a user, ordered newest first.
     */
    List<PhoneAppUsage> findByPhoneUsage_User_IdOrderByPhoneUsage_DateDesc(Long userId);

    /**
     * Get all app usage records for a user, ordered newest first.
     */
    List<PhoneAppUsage> findByPhoneUsage_UserOrderByPhoneUsage_DateDesc(User user);

    // =========================================================
    // CLEANUP & DELETION
    // =========================================================

    /**
     * Delete all app usage records belonging to a parent PhoneUsage record.
     */
    void deleteByPhoneUsage(PhoneUsage phoneUsage);

    /**
     * Delete all app usage records belonging to a user.
     */
    void deleteByPhoneUsage_User(User user);
}