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
public class DailyLogController {

    @Autowired
    private DailyLogService dailyLogService;

    @Autowired
    private PdfService pdfService;

    // 🔥 CREATE LOG (FINAL CLEAN VERSION)
    @PostMapping("/{userId}")
    public ResponseEntity<?> createLog(@PathVariable Long userId, @RequestBody DailyLog log) {
        try {
            DailyLog savedLog = dailyLogService.saveLog(userId, log);
            return ResponseEntity.ok(savedLog);

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 VERY IMPORTANT (check Render logs)
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // 🔥 GET ALL LOGS
    @GetMapping
    public ResponseEntity<?> getLogs() {
        try {
            return ResponseEntity.ok(dailyLogService.getAllLogs());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching logs");
        }
    }

    // 🔥 GET USER LOGS
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserLogs(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(dailyLogService.getLogsByUser(userId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching user logs");
        }
    }

    // 🔥 DOWNLOAD PDF
    @GetMapping("/pdf/{logId}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long logId) {
        try {
            DailyLog log = dailyLogService.getLogById(logId);
            byte[] pdf = pdfService.generatePdf(log);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=report.pdf")
                    .header("Content-Type", "application/pdf")
                    .body(pdf);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
