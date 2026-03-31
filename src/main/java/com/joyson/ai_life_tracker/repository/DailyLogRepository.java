package com.joyson.ai_life_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.joyson.ai_life_tracker.entity.DailyLog;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {
}