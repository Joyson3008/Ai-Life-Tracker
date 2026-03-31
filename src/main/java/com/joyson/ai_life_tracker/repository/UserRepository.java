package com.joyson.ai_life_tracker.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.joyson.ai_life_tracker.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}