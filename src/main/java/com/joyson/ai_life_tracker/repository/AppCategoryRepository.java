package com.joyson.ai_life_tracker.repository;

import com.joyson.ai_life_tracker.entity.AppCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppCategoryRepository extends JpaRepository<AppCategory, Long> {

    Optional<AppCategory> findByPackageName(String packageName);

    Optional<AppCategory> findByAppName(String appName);
}
