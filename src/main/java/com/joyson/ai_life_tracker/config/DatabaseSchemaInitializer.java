package com.joyson.ai_life_tracker.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void widenDailyLogPhoneUsageColumn() {
        jdbcTemplate.execute(
                "ALTER TABLE daily_logs ALTER COLUMN phone_usage TYPE TEXT"
        );
    }
}