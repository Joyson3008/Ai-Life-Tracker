package com.joyson.ai_life_tracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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

    // ─────────────────────────────────────────────
    // 🔥 SAVE LOG
    // - Fetches user by ID (single PK lookup — O(1))
    // - Calls AI for analysis with safe fallback
    // - Blends AI score with local system score
    // ─────────────────────────────────────────────
    @Transactional
    public DailyLog saveLog(Long userId, DailyLog log) {

        // ✅ Single PK lookup — fastest possible query
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        DailyLog dailyLog = dailyLogRepository
            .findByUserIdAndDate(userId, LocalDate.now())
            .orElse(log);

        if (dailyLog != log) {
            dailyLog.setBibleReading(log.getBibleReading());
            dailyLog.setBookReading(log.getBookReading());
            dailyLog.setCsTopic(log.getCsTopic());
            dailyLog.setCodingWork(log.getCodingWork());
            dailyLog.setMovie(log.getMovie());
            dailyLog.setCollegeActivity(log.getCollegeActivity());
            dailyLog.setPhoneUsage(log.getPhoneUsage());
            dailyLog.setDiary(log.getDiary());
            dailyLog.setExpenses(log.getExpenses());
        }

        dailyLog.setUser(user);
        dailyLog.setDate(LocalDate.now());

        // ✅ Build AI prompt text
        String text = buildPromptText(dailyLog);

        // ✅ Safe AI call — never crash the save if AI is down
        AIResponse aiResponse = null;
        try {
            aiResponse = aiService.analyzeText(text);
        } catch (Exception e) {
            System.err.println("[DailyLogService] AI call failed: " + e.getMessage());
        }

        // ✅ Blend AI score + local score
        int systemScore = calculateScore(dailyLog);
        int aiScore = (aiResponse != null) ? aiResponse.getScore() : systemScore;
        int finalScore = (aiScore + systemScore) / 2;

        // ✅ Clamp to valid range
        finalScore = Math.max(1, Math.min(10, finalScore));
        dailyLog.setScore(finalScore);

        // ✅ Map AI fields safely
        if (aiResponse != null) {
            dailyLog.setBibleReview(aiResponse.getBibleReview());
            dailyLog.setBookReview(aiResponse.getBookReview());
            dailyLog.setCodingReview(aiResponse.getCodingReview());
            dailyLog.setCsTopicReview(aiResponse.getCsTopicReview());
            dailyLog.setCollegeReview(aiResponse.getCollegeReview());
            dailyLog.setDiaryReview(aiResponse.getDiaryReview());
            dailyLog.setExpensesReview(aiResponse.getExpensesReview());
            dailyLog.setMovieReview(aiResponse.getMovieReview());
            dailyLog.setPhoneUsageReview(aiResponse.getPhoneUsageReview());
            dailyLog.setFinalSummary(aiResponse.getFinalSummary());
            dailyLog.setMotivation(aiResponse.getMotivation());
        } else {
            // ✅ Meaningful fallback if AI fails
            dailyLog.setFinalSummary("AI analysis unavailable. Your score was calculated from your activity data.");
            dailyLog.setMotivation("Stay consistent. Every day you show up is a win 💪");
        }

        return dailyLogRepository.save(dailyLog);
    }

    // ─────────────────────────────────────────────
    // ✅ GET LOGS BY USER
    // - Uses indexed userId FK — efficient query
    // - Does NOT load all logs then filter in Java
    // ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<DailyLog> getLogsByUser(Long userId) {
        return dailyLogRepository.findByUserId(userId);
    }

    // ─────────────────────────────────────────────
    // ✅ GET LOG BY ID (for PDF etc.)
    // ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public DailyLog getLogById(Long id) {
        return dailyLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log not found with id: " + id));
    }

    // ─────────────────────────────────────────────
    // ✅ GET ALL LOGS (admin / testing only)
    // ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<DailyLog> getAllLogs() {
        return dailyLogRepository.findAll();
    }

    // ─────────────────────────────────────────────
    // 🔥 BUILD AI PROMPT TEXT
    // ─────────────────────────────────────────────
    private String buildPromptText(DailyLog log) {
        return "Bible Reading: " + nullSafe(log.getBibleReading()) + "\n" +
               "Book Reading: "  + nullSafe(log.getBookReading())  + "\n" +
               "CS Topic: "      + nullSafe(log.getCsTopic())       + "\n" +
               "Coding Work: "   + nullSafe(log.getCodingWork())    + "\n" +
               "College Activity: " + nullSafe(log.getCollegeActivity()) + "\n" +
               "Diary: "         + nullSafe(log.getDiary())         + "\n" +
               "Expenses: "      + (log.getExpenses() != null ? log.getExpenses() : 0) + "\n" +
               "Movie: "         + nullSafe(log.getMovie())         + "\n" +
               "Phone Usage: "   + nullSafe(log.getPhoneUsage());
    }

    // ─────────────────────────────────────────────
    // 🔥 SYSTEM SCORE LOGIC (local fallback + blend)
    // ─────────────────────────────────────────────
    private int calculateScore(DailyLog log) {

        int score = 5; // base

        // 📖 Spiritual
        if (notEmpty(log.getBibleReading())) score += 1;

        // 📚 Learning
        if (notEmpty(log.getBookReading())) score += 1;
        if (notEmpty(log.getCsTopic()))     score += 1;

        // 💻 Coding (high value)
        if (notEmpty(log.getCodingWork()))  score += 2;

        // 🎓 College
        if (notEmpty(log.getCollegeActivity())) score += 1;

        // 📔 Reflection
        if (notEmpty(log.getDiary())) score += 1;

        // 📱 Phone usage penalty
        if (notEmpty(log.getPhoneUsage())) {
            try {
                // Extract first number from string like "Instagram 45min, YouTube 90min"
                String digits = log.getPhoneUsage().replaceAll("[^0-9]", " ").trim().split("\\s+")[0];
                int minutes = Integer.parseInt(digits);
                if (minutes > 180) score -= 2;
                else if (minutes > 100) score -= 1;
            } catch (Exception ignored) {
                // Non-parseable phone usage string — no penalty
            }
        }

        // 💰 Expense penalty
        if (log.getExpenses() != null && log.getExpenses() > 500) score -= 1;

        return Math.max(1, Math.min(10, score));
    }

    // ─────────────────────────────────────────────
    // 🔧 HELPERS
    // ─────────────────────────────────────────────
    private boolean notEmpty(String s) {
        return s != null && !s.isBlank();
    }

    private String nullSafe(String s) {
        return s != null ? s : "None";
    }
}
