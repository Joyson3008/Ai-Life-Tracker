package com.joyson.ai_life_tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.joyson.ai_life_tracker.entity.DailyLog;
import com.joyson.ai_life_tracker.entity.User;
import com.joyson.ai_life_tracker.repository.UserRepository;
import com.joyson.ai_life_tracker.service.DailyLogService;
import com.joyson.ai_life_tracker.service.PdfService;

@RestController
@RequestMapping("/api/daily")
@CrossOrigin(origins = "*") // 🔥 allow deployed frontend
public class DailyLogController {

    @Autowired
    private DailyLogService dailyLogService;

    @Autowired
    private UserRepository userRepository; // ✅ ADD THIS

    // 🔥 CREATE LOG (FIXED)
    @PostMapping("/{userId}")
    public ResponseEntity<?> createLog(@PathVariable Long userId, @RequestBody DailyLog log) {
        try {
            // ✅ GET USER
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ SET USER (MOST IMPORTANT FIX)
            log.setUser(user);

            // ✅ SAVE
            DailyLog savedLog = dailyLogService.saveLog(userId, log);

            return ResponseEntity.ok(savedLog);

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 shows error in Render logs
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // 🔥 GET ALL LOGS
    @GetMapping
    public List<DailyLog> getLogs() {
        return dailyLogService.getAllLogs();
    }

    // 🔥 GET USER LOGS
    @GetMapping("/user/{userId}")
    public List<DailyLog> getUserLogs(@PathVariable Long userId) {
        return dailyLogService.getLogsByUser(userId);
    }

    @Autowired
    private PdfService pdfService;

    // 🔥 DOWNLOAD PDF
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
