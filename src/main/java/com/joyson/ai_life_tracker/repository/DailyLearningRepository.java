package com.joyson.ai_life_tracker.repository;

import com.joyson.ai_life_tracker.entity.DailyLearning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyLearningRepository extends JpaRepository<DailyLearning, Long> {
    Optional<DailyLearning> findByUserIdAndDate(Long userId, LocalDate date);
}
