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

    // 🔥 SAVE LOG (FINAL SAFE VERSION)
    public DailyLog saveLog(Long userId, DailyLog log) {

        // ✅ Step 1: Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.setUser(user);

        // 🔥 Step 2: Prepare text for AI
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

        AIResponse aiResponse = null;

        // 🔥 Step 3: Safe AI call (prevents 500 crash)
        try {
            aiResponse = aiService.analyzeText(text);
        } catch (Exception e) {
            System.out.println("AI ERROR: " + e.getMessage());
        }

        // 🔥 Step 4: Safe scoring
        int aiScore = 5; // default fallback
        if (aiResponse != null) {
            aiScore = aiResponse.getScore();
        }

        int systemScore = calculateScore(log);
        int finalScore = (aiScore + systemScore) / 2;

        log.setScore(finalScore);

        // 🔥 Step 5: Map AI response safely
        if (aiResponse != null) {
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
        } else {
            // ✅ fallback if AI fails
            log.setFinalSummary("AI service unavailable. Basic score generated.");
            log.setMotivation("Keep going! Stay consistent 💪");
        }

        // ✅ Step 6: Ensure date is always set
        log.setDate(LocalDate.now());

        // ✅ Step 7: Save to DB
        return dailyLogRepository.save(log);
    }

    // 🔥 SYSTEM SCORE LOGIC
    private int calculateScore(DailyLog log) {

        int score = 5; // base score

        // 📖 Learning
        if (log.getBibleReading() != null && !log.getBibleReading().isEmpty()) score += 1;
        if (log.getBookReading() != null && !log.getBookReading().isEmpty()) score += 1;
        if (log.getCsTopic() != null && !log.getCsTopic().isEmpty()) score += 1;

        // 💻 Coding
        if (log.getCodingWork() != null && !log.getCodingWork().isEmpty()) score += 2;

        // 🎓 College
        if (log.getCollegeActivity() != null && !log.getCollegeActivity().isEmpty()) score += 1;

        // 🧠 Reflection
        if (log.getDiary() != null && !log.getDiary().isEmpty()) score += 1;

        // 📱 Phone usage
        if (log.getPhoneUsage() != null && !log.getPhoneUsage().isEmpty()) {
            try {
                int phoneUsage = Integer.parseInt(log.getPhoneUsage());

                if (phoneUsage > 180) score -= 2;
                else if (phoneUsage > 100) score -= 1;

            } catch (Exception e) {
                // ignore invalid input
            }
        }

        // 💰 Expenses
        if (log.getExpenses() != null) {
            if (log.getExpenses() > 500) {
                score -= 1;
            }
        }

        // 🔒 Clamp score
        if (score > 10) score = 10;
        if (score < 1) score = 1;

        return score;
    }

    // ✅ GET ALL LOGS
    public List<DailyLog> getAllLogs() {
        return dailyLogRepository.findAll();
    }

    // ✅ GET LOGS BY USER
    public List<DailyLog> getLogsByUser(Long userId) {
        return dailyLogRepository.findByUserId(userId);
    }

    // ✅ GET LOG BY ID
    public DailyLog getLogById(Long id) {
        return dailyLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log not found"));
    }
}
