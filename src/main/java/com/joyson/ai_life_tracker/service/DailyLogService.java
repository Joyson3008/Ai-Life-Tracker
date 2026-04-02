package com.joyson.ai_life_tracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.joyson.ai_life_tracker.dto.AIResponse;
import com.joyson.ai_life_tracker.entity.DailyLog;
import com.joyson.ai_life_tracker.entity.User;
import com.joyson.ai_life_tracker.repository.DailyLogRepository;
import com.joyson.ai_life_tracker.repository.UserRepository;
import java.time.LocalDate;
@Service
public class DailyLogService {

    @Autowired
    private AIService aiService;

    @Autowired
    private DailyLogRepository dailyLogRepository;

    @Autowired
    private UserRepository userRepository;

    // 🔥 SAVE LOG WITH USER
    public DailyLog saveLog(Long userId, DailyLog log) {

        // ✅ Step 1: Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Step 2: Set user
        log.setUser(user);

        // 🔥 Step 3: Prepare text
        String text =
                "Bible Reading: " + log.getBibleReading() + "\n" +
                "Book Reading: " + log.getBookReading() + "\n" +
                "CS Topic: " + log.getCsTopic() + "\n" +
                "Coding Work: " + log.getCodingWork() + "\n" +
                "College Activity: " + log.getCollegeActivity() + "\n" +
                "Diary: " + log.getDiary() + "\n" +
                "Expenses: " + log.getExpenses() + "\n" +
                "Movie: " + log.getMovie() + "\n" +
                "Phone Usage: " + log.getPhoneUsage();

        // 🔥 Step 4: AI call
        AIResponse aiResponse = aiService.analyzeText(text);

        // 🔥 Step 5: Map AI → Entity
        log.setScore(aiResponse.getScore());

        log.setBibleReview(aiResponse.getBibleReview());
        log.setBookReview(aiResponse.getBookReview());
        log.setCodingReview(aiResponse.getCodingReview());
        log.setCsTopicReview(aiResponse.getCsTopicReview());
        log.setCollegeReview(aiResponse.getCollegeReview());
        log.setDiaryReview(aiResponse.getDiaryReview());

        log.setExpensesReview(aiResponse.getExpensesReview());
        log.setMovieReview(aiResponse.getMovieReview());
        log.setPhoneUsageReview(aiResponse.getPhoneUsageReview());

        log.setFinalSummary(aiResponse.getFinalSummary());
        log.setMotivation(aiResponse.getMotivation());

log.setDate(LocalDate.now());

        // 🔥 Step 6: Save
        return dailyLogRepository.save(log);
    }

    // ✅ Get all logs
    public List<DailyLog> getAllLogs() {
        return dailyLogRepository.findAll();
    }

    // 🔥 NEW METHOD (THIS FIXES YOUR ERROR)
    public List<DailyLog> getLogsByUser(Long userId) {
        return dailyLogRepository.findByUserId(userId);
    }

    public DailyLog getLogById(Long id) {
        return dailyLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log not found"));
    }
}