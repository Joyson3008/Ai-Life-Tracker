package com.joyson.ai_life_tracker.repository;

import com.joyson.ai_life_tracker.entity.MobileDevice;
import com.joyson.ai_life_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MobileDeviceRepository extends JpaRepository<MobileDevice, Long> {

    Optional<MobileDevice> findByDeviceIdAndUser(String deviceId, User user);

    Optional<MobileDevice> findByDeviceId(String deviceId);

    Optional<MobileDevice> findByRegistrationToken(String registrationToken);

    boolean existsByDeviceIdAndUser(String deviceId, User user);
}
