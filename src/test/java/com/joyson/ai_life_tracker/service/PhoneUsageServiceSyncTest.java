package com.joyson.ai_life_tracker.service;

import com.joyson.ai_life_tracker.dto.AppUsageData;
import com.joyson.ai_life_tracker.dto.PhoneUsageResponse;
import com.joyson.ai_life_tracker.dto.PhoneUsageSyncRequest;
import com.joyson.ai_life_tracker.entity.AppCategory;
import com.joyson.ai_life_tracker.entity.PhoneAppUsage;
import com.joyson.ai_life_tracker.entity.PhoneUsage;
import com.joyson.ai_life_tracker.entity.User;
import com.joyson.ai_life_tracker.repository.AppCategoryRepository;
import com.joyson.ai_life_tracker.repository.PhoneAppUsageRepository;
import com.joyson.ai_life_tracker.repository.PhoneUsageRepository;
import com.joyson.ai_life_tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.*;

class PhoneUsageServiceSyncTest {

    @Test
    void syncPhoneUsage_shouldUpdateExistingAppUsageInsteadOfDuplicatingIt() {
        PhoneUsageRepository phoneUsageRepository = mock(PhoneUsageRepository.class);
        PhoneAppUsageRepository phoneAppUsageRepository = mock(PhoneAppUsageRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        AppCategoryRepository appCategoryRepository = mock(AppCategoryRepository.class);

        User user = new User();
        user.setId(10L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encoded");

        PhoneUsage existingUsage = new PhoneUsage(user, LocalDate.of(2026, 9, 17));
        existingUsage.setScreenTimeLimit(240);

        PhoneAppUsage existingAppUsage = new PhoneAppUsage(
                existingUsage,
                "YouTube",
                "com.google.android.youtube",
                60,
                "DISTRACTING"
        );

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(phoneUsageRepository.findByUserAndDate(user, LocalDate.of(2026, 9, 17)))
                .thenReturn(Optional.of(existingUsage));
        when(phoneAppUsageRepository.findByPhoneUsageAndPackageName(existingUsage, "com.google.android.youtube"))
                .thenReturn(Optional.of(existingAppUsage));
        when(phoneAppUsageRepository.findByPhoneUsageOrderByMinutesDesc(existingUsage))
                .thenReturn(List.of(existingAppUsage));
        when(appCategoryRepository.findByPackageName("com.google.android.youtube"))
                .thenReturn(Optional.empty());
        when(appCategoryRepository.save(any(AppCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(phoneUsageRepository.save(any(PhoneUsage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(phoneAppUsageRepository.save(any(PhoneAppUsage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PhoneUsageService service = new PhoneUsageService(
                phoneUsageRepository,
                phoneAppUsageRepository,
                userRepository,
                appCategoryRepository
        );

        PhoneUsageSyncRequest request = new PhoneUsageSyncRequest();
        request.setDate(LocalDate.of(2026, 9, 17));
        request.setScreenTimeLimit(240);
        request.setApps(List.of(
                new AppUsageData("YouTube", "com.google.android.youtube", 90, "DISTRACTING")
        ));

        PhoneUsageResponse response = service.syncPhoneUsage(10L, request);

        assertNotNull(response);
        assertEquals(90, response.getApps().get(0).getUsageMinutes());
        assertEquals(90, response.getTotalScreenTime());
        assertEquals("YouTube", response.getMostUsedApp());

        verify(phoneAppUsageRepository, never()).save(new PhoneAppUsage());
    }

    @Test
    void syncPhoneUsage_shouldPersistDailyUsageBeforeSavingAppUsages() {
        PhoneUsageRepository phoneUsageRepository = mock(PhoneUsageRepository.class);
        PhoneAppUsageRepository phoneAppUsageRepository = mock(PhoneAppUsageRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        AppCategoryRepository appCategoryRepository = mock(AppCategoryRepository.class);

        User user = new User();
        user.setId(10L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encoded");

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(phoneUsageRepository.findByUserAndDate(user, LocalDate.of(2026, 9, 17)))
                .thenReturn(Optional.empty());
        when(phoneUsageRepository.save(any(PhoneUsage.class)))
                .thenAnswer(invocation -> {
                    PhoneUsage usage = invocation.getArgument(0);
                    usage.setId(77L);
                    return usage;
                });
        when(phoneAppUsageRepository.findByPhoneUsageAndPackageName(any(PhoneUsage.class), eq("com.google.android.youtube")))
                .thenReturn(Optional.empty());
        when(phoneAppUsageRepository.findByPhoneUsageOrderByMinutesDesc(any(PhoneUsage.class)))
                .thenReturn(List.of());
        when(phoneAppUsageRepository.save(any(PhoneAppUsage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PhoneUsageService service = new PhoneUsageService(
                phoneUsageRepository,
                phoneAppUsageRepository,
                userRepository,
                appCategoryRepository
        );

        PhoneUsageSyncRequest request = new PhoneUsageSyncRequest();
        request.setDate(LocalDate.of(2026, 9, 17));
        request.setApps(List.of(
                new AppUsageData("YouTube", "com.google.android.youtube", 90, "DISTRACTING")
        ));

        service.syncPhoneUsage(10L, request);

        InOrder inOrder = inOrder(phoneUsageRepository, phoneAppUsageRepository);
        inOrder.verify(phoneUsageRepository).save(any(PhoneUsage.class));
        inOrder.verify(phoneAppUsageRepository).save(any(PhoneAppUsage.class));
    }
}
