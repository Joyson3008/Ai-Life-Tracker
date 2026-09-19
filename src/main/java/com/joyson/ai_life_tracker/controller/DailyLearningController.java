package com.joyson.ai_life_tracker.controller;

import com.joyson.ai_life_tracker.entity.DailyLearning;
import com.joyson.ai_life_tracker.entity.DailyVocabulary;
import com.joyson.ai_life_tracker.service.DailyLearningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/daily-learning")
public class DailyLearningController {

    private final DailyLearningService dailyLearningService;

    public DailyLearningController(DailyLearningService dailyLearningService) {
        this.dailyLearningService = dailyLearningService;
    }

    @GetMapping("/{userId}/today")
    public ResponseEntity<?> getToday(@PathVariable Long userId) {
        try {
            DailyLearning learning = dailyLearningService.getOrGenerateToday(userId);
            return ResponseEntity.ok(toResponse(learning));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Daily learning generation failed"));
        }
    }

    @PostMapping("/{userId}/today/refresh/{language}")
    public ResponseEntity<?> refresh(
            @PathVariable Long userId,
            @PathVariable String language
    ) {
        try {
            return ResponseEntity.ok(toResponse(dailyLearningService.refreshLanguage(userId, language)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Daily learning refresh failed"));
        }
    }

    private Map<String, String> toVocabulary(DailyVocabulary vocabulary) {
        return Map.of(
            "word", vocabulary.getWord(),
                "meaningEnglish", valueOrDefault(vocabulary.getMeaningEnglish(), "Meaning will be refreshed today."),
                "meaningTamil", valueOrDefault(vocabulary.getMeaningTamil(), "இன்றைய புதுப்பிப்பில் தமிழ் அர்த்தம் வரும்."),
                "exampleEnglish", valueOrDefault(vocabulary.getExampleEnglish(), "Practice this word in a short sentence."),
                "exampleTamil", valueOrDefault(vocabulary.getExampleTamil(), "இந்த வார்த்தையை ஒரு சிறிய வாக்கியத்தில் பயிற்சி செய்யுங்கள்."),
                "exampleRoman", valueOrDefault(vocabulary.getExampleRoman(), "Practice this word in a daily sentence.")
        );
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private Map<String, Object> toResponse(DailyLearning learning) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", learning.getId());
        response.put("date", learning.getDate());
        response.put("csTopic", learning.getCsTopic());
        response.put("csExplanation", learning.getCsExplanation());
        response.put("csExample", learning.getCsExample());
        response.put("hindiWords", wordsFor(learning, "HINDI"));
        response.put("teluguWords", wordsFor(learning, "TELUGU"));
        response.put("malayalamWords", wordsFor(learning, "MALAYALAM"));
        response.put("vocabulary", wordsFor(learning, "ENGLISH"));
        return response;
    }

    private List<Map<String, String>> wordsFor(DailyLearning learning, String language) {
        return learning.getVocabulary().stream()
                .filter(word -> language.equals(word.getLanguage()))
                .limit(5)
                .map(this::toVocabulary)
                .collect(Collectors.toList());
    }
}
