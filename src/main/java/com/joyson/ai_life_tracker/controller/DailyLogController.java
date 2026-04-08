package com.joyson.ai_life_tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.joyson.ai_life_tracker.entity.DailyLog;
import com.joyson.ai_life_tracker.service.DailyLogService;
import com.joyson.ai_life_tracker.service.PdfService;
@RestController
@RequestMapping("/api/daily")
@CrossOrigin(origins = "http://localhost:5173")
public class DailyLogController {


    @Autowired
    private DailyLogService dailyLogService;

    // 🔥 Create log for specific user
    @PostMapping("/{userId}")
    public DailyLog createLog(@PathVariable Long userId, @RequestBody DailyLog log) {
        return dailyLogService.saveLog(userId, log);
    }

    // 🔥 Get all logs (admin/testing purpose)
    @GetMapping
    public List<DailyLog> getLogs() {
        return dailyLogService.getAllLogs();
    }

    // 🔥 NEW: Get logs by user
    @GetMapping("/user/{userId}")
    public List<DailyLog> getUserLogs(@PathVariable Long userId) {
        return dailyLogService.getLogsByUser(userId);
    }
    @Autowired
    private PdfService pdfService;

    @GetMapping("/pdf/{logId}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long logId) {

        DailyLog log = dailyLogService.getLogById(logId);

        byte[] pdf = pdfService.generatePdf(log);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=report.pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }
}