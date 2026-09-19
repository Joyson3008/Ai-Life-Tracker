package com.joyson.ai_life_tracker.repository;

import com.joyson.ai_life_tracker.entity.MobileSyncStatus;
import com.joyson.ai_life_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MobileSyncStatusRepository extends JpaRepository<MobileSyncStatus, Long> {

    Optional<MobileSyncStatus> findByUser(User user);

    Optional<MobileSyncStatus> findByUserAndDeviceId(User user, String deviceId);
}
