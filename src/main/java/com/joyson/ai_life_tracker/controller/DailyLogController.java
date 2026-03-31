package com.joyson.ai_life_tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.joyson.ai_life_tracker.entity.DailyLog;
import com.joyson.ai_life_tracker.service.DailyLogService;

@RestController
@RequestMapping("/api/daily")
public class DailyLogController {

    @Autowired
    private DailyLogService dailyLogService;

    @PostMapping
    public DailyLog createLog(@RequestBody DailyLog log) {
        return dailyLogService.saveLog(log);
    }

    @GetMapping
    public List<DailyLog> getLogs() {
        return dailyLogService.getAllLogs();
    }
}