package com.joyson.ai_life_tracker.repository;

import com.joyson.ai_life_tracker.entity.PhoneUsage;
import com.joyson.ai_life_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhoneUsageRepository extends JpaRepository<PhoneUsage, Long> {

    // =========================================================
    // FIND BY USER & DATE
    // =========================================================

    /**
     * Find a user's phone usage record for a specific date using User entity.
     */
    Optional<PhoneUsage> findByUserAndDate(User user, LocalDate date);

    /**
     * Find a user's phone usage record for a specific date using User ID.
     */
    Optional<PhoneUsage> findByUser_IdAndDate(Long userId, LocalDate date);

    // =========================================================
    // HISTORY & RANGE QUERIES
    // =========================================================

    /**
     * Get all phone usage records for a user, ordered newest first.
     */
    List<PhoneUsage> findByUser_IdOrderByDateDesc(Long userId);

    /**
     * Get all phone usage records for a user entity, ordered newest first.
     */
    List<PhoneUsage> findByUserOrderByDateDesc(User user);

    /**
     * Get phone usage records for a user between two dates (inclusive), ordered chronologically.
     */
    List<PhoneUsage> findByUser_IdAndDateBetweenOrderByDateAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * Get phone usage records for a user entity between two dates (inclusive).
     */
    List<PhoneUsage> findByUserAndDateBetweenOrderByDateAsc(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );

    // =========================================================
    // EXISTENCE & DELETION
    // =========================================================

    /**
     * Check if a phone usage record already exists for a user on a given date.
     */
    boolean existsByUser_IdAndDate(Long userId, LocalDate date);

    /**
     * Delete all phone usage records belonging to a user.
     */
    void deleteByUser(User user);
}