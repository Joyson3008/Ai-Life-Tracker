package com.joyson.ai_life_tracker.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.joyson.ai_life_tracker.entity.User;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
}