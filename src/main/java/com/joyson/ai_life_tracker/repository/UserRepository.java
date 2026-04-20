package com.joyson.ai_life_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.joyson.ai_life_tracker.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ Indexed query — ensure `email` column has a DB index for O(log n) lookup
    Optional<User> findByEmail(String email);

    // ✅ Existence check without loading entity (avoids object hydration)
    boolean existsByEmail(String email);

    // ✅ Lightweight projection — only fetch id, name, email (no password)
    @Query("SELECT u.id AS id, u.name AS name, u.email AS email FROM User u WHERE u.id = :id")
    Optional<UserProjection> findProjectionById(@Param("id") Long id);

    // 🔥 Projection interface for lightweight responses
    interface UserProjection {
        Long getId();
        String getName();
        String getEmail();
    }
}
