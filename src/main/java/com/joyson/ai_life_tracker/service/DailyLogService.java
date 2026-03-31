package com.joyson.ai_life_tracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.joyson.ai_life_tracker.dto.AIResponse;
import com.joyson.ai_life_tracker.entity.DailyLog;
import com.joyson.ai_life_tracker.repository.DailyLogRepository;

@Service
public class DailyLogService {

    @Autowired
    private DailyLogRepository dailyLogRepository;

    @Autowired
    private AIService aiService;

    // ✅ FIXED METHOD
    public DailyLog saveLog(DailyLog log) {

        // 🔥 Step 1: Prepare text
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

        // 🔥 Step 2: Call AI
        AIResponse aiResponse = aiService.analyzeText(text);

        // 🔥 Step 3: Store result
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

        // 🔥 Step 4: Save
        return dailyLogRepository.save(log);
    }
    // Get all logs
    public List<DailyLog> getAllLogs() {
        return dailyLogRepository.findAll();
    }
}