package com.joyson.ai_life_tracker.repository;

import com.joyson.ai_life_tracker.entity.DailyVocabulary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyVocabularyRepository extends JpaRepository<DailyVocabulary, Long> {
}
