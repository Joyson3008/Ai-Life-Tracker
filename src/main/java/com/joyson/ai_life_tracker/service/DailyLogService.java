package com.joyson.ai_life_tracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDate;

import com.joyson.ai_life_tracker.dto.AIResponse;
import com.joyson.ai_life_tracker.entity.DailyLog;
import com.joyson.ai_life_tracker.entity.User;
import com.joyson.ai_life_tracker.repository.DailyLogRepository;
import com.joyson.ai_life_tracker.repository.UserRepository;

@Service
public class DailyLogService {

    @Autowired
    private AIService aiService;

    @Autowired
    private DailyLogRepository dailyLogRepository;

    @Autowired
    private UserRepository userRepository;

    // 🔥 SAVE LOG WITH HYBRID SCORING
    public DailyLog saveLog(Long userId, DailyLog log) {

        // ✅ Step 1: Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Step 2: Set user
        log.setUser(user);

        // 🔥 Step 3: Prepare text for AI
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

        // 🔥 Step 5: HYBRID SCORING
        int aiScore = aiResponse.getScore();
        int systemScore = calculateScore(log);

        int finalScore = (aiScore + systemScore) / 2;
        log.setScore(finalScore);

        // 🔥 Step 6: Map AI → Entity
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

        // 🔥 Step 7: Save
        return dailyLogRepository.save(log);
    }

    private int calculateScore(DailyLog log) {

        int score = 5; // base score

        // 📖 Learning
        if (log.getBibleReading() != null && !log.getBibleReading().isEmpty()) score += 1;
        if (log.getBookReading() != null && !log.getBookReading().isEmpty()) score += 1;
        if (log.getCsTopic() != null && !log.getCsTopic().isEmpty()) score += 1;

        // 💻 Productivity
        if (log.getCodingWork() != null && !log.getCodingWork().isEmpty()) score += 2;

        // 🎓 College
        if (log.getCollegeActivity() != null && !log.getCollegeActivity().isEmpty()) score += 1;

        // 🧠 Reflection
        if (log.getDiary() != null && !log.getDiary().isEmpty()) score += 1;

        // 📱 Phone usage (String → int)
        if (log.getPhoneUsage() != null && !log.getPhoneUsage().isEmpty()) {
            try {
                int phoneUsage = Integer.parseInt(log.getPhoneUsage());

                if (phoneUsage > 180) score -= 2;
                else if (phoneUsage > 100) score -= 1;

            } catch (Exception e) {
                // ignore
            }
        }

        // 💰 Expenses (Double → direct use)
        if (log.getExpenses() != null) {
            if (log.getExpenses() > 500) {
                score -= 1;
            }
        }

        // 🔒 Clamp
        if (score > 10) score = 10;
        if (score < 1) score = 1;

        return score;
    }

    // ✅ Get all logs
    public List<DailyLog> getAllLogs() {
        return dailyLogRepository.findAll();
    }

    // ✅ Get logs by user
    public List<DailyLog> getLogsByUser(Long userId) {
        return dailyLogRepository.findByUserId(userId);
    }

    // ✅ Get log by ID
    public DailyLog getLogById(Long id) {
        return dailyLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log not found"));
    }
}