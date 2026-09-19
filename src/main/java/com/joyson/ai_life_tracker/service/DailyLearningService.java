package com.joyson.ai_life_tracker.service;

import com.joyson.ai_life_tracker.dto.DailyLearningContent;
import com.joyson.ai_life_tracker.entity.DailyLearning;
import com.joyson.ai_life_tracker.entity.DailyVocabulary;
import com.joyson.ai_life_tracker.entity.User;
import com.joyson.ai_life_tracker.repository.DailyLearningRepository;
import com.joyson.ai_life_tracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class DailyLearningService {

    private final DailyLearningRepository dailyLearningRepository;
    private final UserRepository userRepository;
    private final AIService aiService;

    public DailyLearningService(
            DailyLearningRepository dailyLearningRepository,
            UserRepository userRepository,
            AIService aiService
    ) {
        this.dailyLearningRepository = dailyLearningRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
    }

    @Transactional
    public DailyLearning getOrGenerateToday(Long userId) {
        LocalDate today = LocalDate.now();
        DailyLearning existing = dailyLearningRepository.findByUserIdAndDate(userId, today).orElse(null);
        if (existing == null) return generate(userId, today);
        if (!hasValidRomanWords(existing, "HINDI")
                || !hasValidRomanWords(existing, "TELUGU")
                || !hasValidRomanWords(existing, "MALAYALAM")
                || !hasValidRomanWords(existing, "ENGLISH")) {
            return repair(existing);
        }
        return existing;
    }

    private DailyLearning repair(DailyLearning learning) {
        DailyLearningContent content = aiService.generateDailyLearning();
        learning.getVocabulary().clear();
        addWords(learning, "HINDI", content.getHindiWords(), content.getHindiWord(), content.getHindiMeaning(), content.getHindiExample());
        addWords(learning, "TELUGU", content.getTeluguWords(), content.getTeluguWord(), content.getTeluguMeaning(), content.getTeluguExample());
        addWords(learning, "MALAYALAM", content.getMalayalamWords(), content.getMalayalamWord(), content.getMalayalamMeaning(), content.getMalayalamExample());
        addWords(learning, "ENGLISH", content.getVocabulary(), "practice", "to improve through repetition", "I practice every day.");
        return dailyLearningRepository.save(learning);
    }

    private boolean hasValidRomanWords(DailyLearning learning, String language) {
        Set<String> words = new HashSet<>();
        long count = learning.getVocabulary().stream()
                .filter(item -> language.equals(item.getLanguage()))
            .count();
        return count == 5 && learning.getVocabulary().stream()
            .filter(item -> language.equals(item.getLanguage()))
            .allMatch(item -> item.getWord() != null
                        && item.getWord().trim().matches("[a-z][a-z '-]*")
                        && words.add(item.getWord().trim().toLowerCase(Locale.ROOT)));
    }

    @Transactional
    public DailyLearning refreshLanguage(Long userId, String language) {
        DailyLearning learning = getOrGenerateToday(userId);
        String normalized = language.toUpperCase();
        if (!List.of("HINDI", "TELUGU", "MALAYALAM", "ENGLISH").contains(normalized)) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }

        DailyLearningContent content = aiService.generateDailyLearning();
        learning.getVocabulary().removeIf(word -> normalized.equals(word.getLanguage()));
        switch (normalized) {
            case "HINDI" -> addWords(learning, normalized, content.getHindiWords(), content.getHindiWord(), content.getHindiMeaning(), content.getHindiExample());
            case "TELUGU" -> addWords(learning, normalized, content.getTeluguWords(), content.getTeluguWord(), content.getTeluguMeaning(), content.getTeluguExample());
            case "MALAYALAM" -> addWords(learning, normalized, content.getMalayalamWords(), content.getMalayalamWord(), content.getMalayalamMeaning(), content.getMalayalamExample());
            case "ENGLISH" -> addWords(learning, normalized, content.getVocabulary(), "practice", "to improve through repetition", "I practice every day.");
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        }
        return dailyLearningRepository.save(learning);
    }

    private DailyLearning generate(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        DailyLearningContent content = aiService.generateDailyLearning();

        DailyLearning learning = new DailyLearning();
        learning.setUser(user);
        learning.setDate(date);
        learning.setCsTopic(required(content.getCsTopic(), "Daily CS topic unavailable"));
        learning.setCsExplanation(required(content.getCsExplanation(), "Study the topic and connect it to a small project."));
        learning.setCsExample(required(content.getCsExample(), "Build a small example to reinforce the idea."));
        learning.setHindiWord(required(content.getHindiWord(), "seekhna"));
        learning.setHindiMeaning(required(content.getHindiMeaning(), "learn"));
        learning.setHindiExample(required(content.getHindiExample(), "I learn every day."));
        learning.setTeluguWord(required(content.getTeluguWord(), "nerchukovadam"));
        learning.setTeluguMeaning(required(content.getTeluguMeaning(), "learn"));
        learning.setTeluguExample(required(content.getTeluguExample(), "I learn every day."));
        learning.setMalayalamWord(required(content.getMalayalamWord(), "padikkuka"));
        learning.setMalayalamMeaning(required(content.getMalayalamMeaning(), "learn"));
        learning.setMalayalamExample(required(content.getMalayalamExample(), "I learn every day."));

        addWords(learning, "HINDI", content.getHindiWords(), content.getHindiWord(), content.getHindiMeaning(), content.getHindiExample());
        addWords(learning, "TELUGU", content.getTeluguWords(), content.getTeluguWord(), content.getTeluguMeaning(), content.getTeluguExample());
        addWords(learning, "MALAYALAM", content.getMalayalamWords(), content.getMalayalamWord(), content.getMalayalamMeaning(), content.getMalayalamExample());
        addWords(learning, "ENGLISH", content.getVocabulary(), "practice", "to improve through repetition", "I practice every day.");
        return dailyLearningRepository.save(learning);
    }

    private void addWords(
            DailyLearning learning,
            String language,
            List<DailyLearningContent.VocabularyItem> source,
            String fallbackWord,
            String fallbackMeaning,
            String fallbackExample
    ) {
        List<DailyLearningContent.VocabularyItem> items = source == null ? List.of() : source;
        for (int index = 0; index < 5; index++) {
            DailyLearningContent.VocabularyItem item = index < items.size() ? items.get(index) : null;
            DailyVocabulary vocabulary = new DailyVocabulary();
            vocabulary.setDailyLearning(learning);
            vocabulary.setLanguage(language);
            vocabulary.setWord(required(item == null ? fallbackWord : item.getWord(), fallbackWord));
            vocabulary.setMeaningEnglish(required(item == null ? fallbackMeaning : item.getMeaningEnglish(), fallbackMeaning));
            vocabulary.setMeaningTamil(required(item == null ? "மேம்பட மீண்டும் செய்வது" : item.getMeaningTamil(), "மேம்பட மீண்டும் செய்வது"));
            vocabulary.setExampleEnglish(required(item == null ? fallbackExample : item.getExampleEnglish(), fallbackExample));
            vocabulary.setExampleTamil(required(item == null ? "நான் தினமும் பயிற்சி செய்கிறேன்." : item.getExampleTamil(), "நான் தினமும் பயிற்சி செய்கிறேன்."));
            vocabulary.setExampleRoman(required(item == null ? fallbackExample : item.getExampleRoman(), fallbackExample));
            vocabulary.setMeaning(vocabulary.getMeaningEnglish());
            vocabulary.setExample(vocabulary.getExampleRoman());
            learning.getVocabulary().add(vocabulary);
        }
    }

    private String required(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
