package com.joyson.ai_life_tracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.http.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyson.ai_life_tracker.dto.AIResponse;
import com.joyson.ai_life_tracker.dto.DailyLearningContent;

import java.util.*;
import java.time.LocalDate;

@Service
public class AIService {

    private static final String MODEL = "qwen/qwen3.8-27b";

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    public AIResponse analyzeText(String text) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
   String prompt =
            "Analyze this daily log and return ONLY a valid JSON object. No extra text.\n\n" +
            "Daily Log:\n" + text + "\n\n" +
            "Rules:\n" +
            "- Each review: 5-6 sentences max\n" +
            "- finalSummary: 5 sentences\n" +
            "- motivation: 3 sentences\n" +
            "- score: integer 1-10\n" +
            "- Speak like a mentor, be specific to what was mentioned\n" +
            "- If a field is empty/null, still write a 2-sentence note\n\n" +
  "SMART ANALYSIS:\n" +
        	    "- Explain mentioned items briefly with real meaning\n" +
        	    "- Examples:\n" +
        	    "  • John Chapter → Word, light, spiritual meaning\n" +
        	    "  • Atomic Habits → habit building, consistency\n" +
        	    "  • Spring Boot → backend, REST APIs, real-world skills\n" +
        	    "  • OOP → encapsulation, inheritance, real-world modeling\n" +
        	    "  • Movie → give insight, not story\n\n" +

        	    "QUALITY STYLE:\n" +
        	    "- Speak like a mentor\n" +
        	    "- Be practical and realistic\n" +
        	    "- Add small improvement suggestions naturally\n" +
        	    "- Make it feel personal\n\n" +

            "Return ONLY this JSON (no markdown, no explanation):\n" +
        	    "{\n" +
        	    "  \"score\": 8,\n" +
        	    "  \"bibleReview\": \"5-6 sentences with \\n\",\n" +
        	    "  \"bookReview\": \"3-4 sentences with \\n\",\n" +
        	    "  \"codingReview\": \"3-4 sentences\",\n" +
        	    "  \"csTopicReview\": \"3-4 sentences\",\n" +
        	    "  \"collegeReview\": \"3-4 sentences\",\n" +
        	    "  \"diaryReview\": \"3-4 sentences\",\n" +
        	    "  \"expensesReview\": \"2-3 sentences and use rupees\",\n" +
        	    "  \"movieReview\": \"2-3 sentences\",\n" +
        	    "  \"phoneUsageReview\": \"2-3 sentences\",\n" +
        	    "  \"finalSummary\": \"4-5 sentences overall day analysis\",\n" +
        	    "  \"motivation\": \"2-3 powerful realistic lines\"\n" +
        	    "}";

        // 🔥 REQUEST BODY
        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("temperature", 0.3);
        body.put("max_tokens", 800);

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "You are a JSON-only API. Return only valid JSON.");

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);

        messages.add(systemMsg);
        messages.add(userMsg);

        body.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response;

        // 🔥 SAFE API CALL
        try {
            response = restTemplate.postForEntity(apiUrl, request, Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("[AIService] Groq returned HTTP " + response.getStatusCode()
                        + ": " + response.getBody());
                return fallback("AI provider returned HTTP " + response.getStatusCode().value() + ".");
            }
        } catch (HttpStatusCodeException e) {
            System.err.println("[AIService] Groq HTTP " + e.getStatusCode().value()
                    + " response: " + e.getResponseBodyAsString());
            return fallback("AI provider returned HTTP " + e.getStatusCode().value() + ".");
        } catch (Exception e) {
            System.err.println("[AIService] Groq request failed: " + e.getClass().getSimpleName()
                    + ": " + e.getMessage());

            return fallback("AI service unreachable.");
        }

        Map responseBody = response.getBody();

        System.out.println("🔥 RAW API RESPONSE: " + responseBody);

        // 🔥 VALIDATION
        if (responseBody == null || !responseBody.containsKey("choices")) {
            return fallback("Invalid AI response.");
        }

        List choices = (List) responseBody.get("choices");

        if (choices == null || choices.isEmpty()) {
            return fallback("Empty AI response.");
        }

        Map firstChoice = (Map) choices.get(0);
        Map message = (Map) firstChoice.get("message");

        if (message == null || !message.containsKey("content")) {
            return fallback("Missing AI content.");
        }

        String aiText = (String) message.get("content");

        // 🧼 CLEAN RESPONSE
        aiText = aiText.replace("```json", "")
                       .replace("```", "")
                       .trim();

        System.out.println("===== CLEANED AI RESPONSE =====");
        System.out.println(aiText);

        try {
            ObjectMapper mapper = new ObjectMapper();

            mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
            mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);

            return mapper.readValue(aiText, AIResponse.class);

        } catch (Exception e) {
            System.out.println("❌ JSON PARSE FAILED: " + e.getMessage());
            return fallback("AI parsing failed.");
        }
    }

    public DailyLearningContent generateDailyLearning() {
        return generateDailyLearning(LocalDate.now());
    }

    public DailyLearningContent generateDailyLearning(LocalDate date) {
        String prompt = "Generate today's beginner learning pack. Return ONLY valid JSON, no markdown. " +
                "This pack is for date " + date + ". Do not reuse words from another date. " +
                "Choose one new practical computer-science topic and exactly five different English vocabulary words. " +
                "For Hindi, Telugu, and Malayalam provide exactly five beginner spoken words, written ONLY with Roman/English letters. NEVER use Devanagari, Telugu, Malayalam, or any native script. " +
                "For every word provide an English meaning, Tamil meaning in Tamil script, an English example, a Tamil example in Tamil script, and a natural example in the target language written in Roman letters. " +
                "Focus on practical daily conversation for a Tamil speaker. " +
                "Use this exact schema: " +
                "{\"csTopic\":\"\",\"csExplanation\":\"\",\"csExample\":\"\", " +
                "\"hindiWord\":\"\",\"hindiMeaning\":\"\",\"hindiExample\":\"\", " +
                "\"teluguWord\":\"\",\"teluguMeaning\":\"\",\"teluguExample\":\"\", " +
                "\"malayalamWord\":\"\",\"malayalamMeaning\":\"\",\"malayalamExample\":\"\", " +
                "\"hindiWords\":[{\"word\":\"\",\"meaningEnglish\":\"\",\"meaningTamil\":\"\",\"exampleEnglish\":\"\",\"exampleTamil\":\"\",\"exampleRoman\":\"\"}], " +
                "\"teluguWords\":[{\"word\":\"\",\"meaningEnglish\":\"\",\"meaningTamil\":\"\",\"exampleEnglish\":\"\",\"exampleTamil\":\"\",\"exampleRoman\":\"\"}], " +
                "\"malayalamWords\":[{\"word\":\"\",\"meaningEnglish\":\"\",\"meaningTamil\":\"\",\"exampleEnglish\":\"\",\"exampleTamil\":\"\",\"exampleRoman\":\"\"}], " +
                "\"vocabulary\":[{\"word\":\"\",\"meaningEnglish\":\"\",\"meaningTamil\":\"\",\"exampleEnglish\":\"\",\"exampleTamil\":\"\",\"exampleRoman\":\"\"}]}";

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("model", MODEL);
            body.put("temperature", 0.7);
            body.put("max_tokens", 1200);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", "You return JSON only."),
                    Map.of("role", "user", "content", prompt)
            ));

                ResponseEntity<Map> response;
                try {
                response = restTemplate.postForEntity(
                    apiUrl,
                    new HttpEntity<>(body, headers),
                    Map.class
                );
                } catch (HttpStatusCodeException e) {
                System.err.println("[AIService] Daily learning Groq HTTP " + e.getStatusCode().value()
                    + ": " + e.getResponseBodyAsString());
                return fallbackDailyLearning(date);
                }
            Map responseBody = response.getBody();
            List choices = responseBody == null ? List.of() : (List) responseBody.get("choices");
            Map firstChoice = choices.isEmpty() ? Map.of() : (Map) choices.get(0);
            Map message = (Map) firstChoice.get("message");
            String content = message == null ? "" : String.valueOf(message.get("content"));
            content = content.replace("```json", "").replace("```", "").trim();

            DailyLearningContent result = new ObjectMapper().readValue(content, DailyLearningContent.class);
            return normalizeDailyLearning(result);
        } catch (Exception e) {
            System.err.println("[AIService] Daily learning generation failed: " + e.getMessage());
            return fallbackDailyLearning(date);
        }
    }

    private DailyLearningContent normalizeDailyLearning(DailyLearningContent content) {
        if (content == null) return fallbackDailyLearning(LocalDate.now());
        content.setHindiWords(validWords(content.getHindiWords(), hindiFallback()));
        content.setTeluguWords(validWords(content.getTeluguWords(), teluguFallback()));
        content.setMalayalamWords(validWords(content.getMalayalamWords(), malayalamFallback()));
        content.setVocabulary(validWords(content.getVocabulary(), englishFallback()));
        return content;
    }

    private List<DailyLearningContent.VocabularyItem> validWords(
            List<DailyLearningContent.VocabularyItem> words,
            List<DailyLearningContent.VocabularyItem> fallback
    ) {
        if (words == null || words.size() < 5) return fallback;
        List<DailyLearningContent.VocabularyItem> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (DailyLearningContent.VocabularyItem word : words) {
            String value = word == null ? "" : word.getWord();
            String key = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
                if (key.isBlank()
                    || !key.matches("[a-z][a-z '-]*")
                    || !isAsciiText(word.getMeaningEnglish())
                    || !isAsciiText(word.getExampleEnglish())
                    || !isAsciiText(word.getExampleRoman())
                    || !seen.add(key)) return fallback;
            result.add(word);
            if (result.size() == 5) break;
        }
        return result.size() == 5 ? result : fallback;
    }

    private boolean isAsciiText(String value) {
        return value != null && !value.isBlank() && value.chars().allMatch(character -> character < 128);
    }

    private List<DailyLearningContent.VocabularyItem> romanWords(String[][] values) {
        List<DailyLearningContent.VocabularyItem> words = new ArrayList<>();
        for (String[] value : values) {
            DailyLearningContent.VocabularyItem item = new DailyLearningContent.VocabularyItem();
            item.setWord(value[0]);
            item.setMeaningEnglish(value[1]);
            item.setMeaningTamil(value[2]);
            item.setExampleEnglish(value[3]);
            item.setExampleTamil(value[4]);
            item.setExampleRoman(value[5]);
            words.add(item);
        }
        return words;
    }

    private List<DailyLearningContent.VocabularyItem> hindiFallback() {
        return romanWords(new String[][] {
                {"kya", "what", "என்ன", "What is this?", "இது என்ன?", "Yeh kya hai?"},
                {"haan", "yes", "ஆம்", "Yes, I understand.", "ஆம், எனக்கு புரிகிறது.", "Haan, mujhe samajh aaya."},
                {"nahin", "no", "இல்லை", "No, thank you.", "இல்லை, நன்றி.", "Nahin, dhanyavaad."},
                {"paani", "water", "தண்ணீர்", "Please give me water.", "தயவுசெய்து எனக்கு தண்ணீர் கொடுங்கள்.", "Mujhe paani dijiye."},
                {"aaj", "today", "இன்று", "Today is a good day.", "இன்று நல்ல நாள்.", "Aaj achha din hai."}
        });
    }

    private List<DailyLearningContent.VocabularyItem> teluguFallback() {
        return romanWords(new String[][] {
                {"emi", "what", "என்ன", "What is this?", "இது என்ன?", "Idi emi?"},
                {"avunu", "yes", "ஆம்", "Yes, I understand.", "ஆம், எனக்கு புரிகிறது.", "Avunu, naaku artham ayyindi."},
                {"kaadu", "no", "இல்லை", "No, thank you.", "இல்லை, நன்றி.", "Kaadu, dhanyavaadalu."},
                {"neellu", "water", "தண்ணீர்", "Please give me water.", "தயவுசெய்து எனக்கு தண்ணீர் கொடுங்கள்.", "Naaku neellu ivvandi."},
                {"ee roju", "today", "இன்று", "Today is a good day.", "இன்று நல்ல நாள்.", "Ee roju manchi roju."}
        });
    }

    private List<DailyLearningContent.VocabularyItem> malayalamFallback() {
        return romanWords(new String[][] {
                {"entha", "what", "என்ன", "What is this?", "இது என்ன?", "Ithu entha?"},
                {"athe", "yes", "ஆம்", "Yes, I understand.", "ஆம், எனக்கு புரிகிறது.", "Athe, enikku manassilaayi."},
                {"illa", "no", "இல்லை", "No, thank you.", "இல்லை, நன்றி.", "Illa, nandi."},
                {"vellam", "water", "தண்ணீர்", "Please give me water.", "தயவுசெய்து எனக்கு தண்ணீர் கொடுங்கள்.", "Enikku vellam tharumo?"},
                {"innu", "today", "இன்று", "Today is a good day.", "இன்று நல்ல நாள்.", "Innu nalla divasam aanu."}
        });
    }

    private List<DailyLearningContent.VocabularyItem> englishFallback() {
        return romanWords(new String[][] {
                {"ask", "to request information", "கேட்க", "I want to ask a question.", "நான் ஒரு கேள்வி கேட்க விரும்புகிறேன்.", "I want to ask a question."},
                {"answer", "a reply to a question", "பதில்", "Please answer me.", "தயவுசெய்து எனக்கு பதில் சொல்லுங்கள்.", "Please answer me."},
                {"help", "to make something easier for someone", "உதவி", "Can you help me?", "நீங்கள் எனக்கு உதவ முடியுமா?", "Can you help me?"},
                {"ready", "prepared to do something", "தயார்", "I am ready now.", "நான் இப்போது தயார்.", "I am ready now."},
                {"understand", "to know the meaning", "புரிந்துகொள்", "I understand the idea.", "எனக்கு இந்த யோசனை புரிகிறது.", "I understand the idea."}
        });
    }

    private DailyLearningContent fallbackDailyLearning(LocalDate date) {
        DailyLearningContent content = new DailyLearningContent();
        content.setCsTopic("HTTP and REST APIs");
        content.setCsExplanation("A REST API lets applications communicate using HTTP requests and responses.");
        content.setCsExample("A mobile app sends a GET request to fetch today's data.");
        content.setHindiWord("seekhna");
        content.setHindiMeaning("to learn");
        content.setHindiExample("Main har din kuch naya seekhta hoon.");
        content.setTeluguWord("nerchukovadam");
        content.setTeluguMeaning("to learn");
        content.setTeluguExample("Nenu prathi roju kotha vishayam nerchukuntaanu.");
        content.setMalayalamWord("padikkuka");
        content.setMalayalamMeaning("to learn");
        content.setMalayalamExample("Njaan ella divasavum padikkunnu.");
        List<DailyLearningContent.VocabularyItem> words = new ArrayList<>();
        String[][] values = {
                {"adapt", "to adjust to a new situation", "We adapt when requirements change."},
                {"clarify", "to make something easier to understand", "Please clarify the question."},
                {"reliable", "consistently dependable", "This backup is reliable."},
                {"observe", "to watch carefully", "Observe how the program behaves."},
            {"progress", "forward movement or improvement", "Small steps create progress."},
            {"explore", "to investigate or learn about", "Explore the new feature."},
            {"improve", "to make something better", "We improve with practice."},
            {"patient", "able to wait calmly", "Be patient while the app loads."},
            {"reflect", "to think carefully about something", "Reflect on the result."},
            {"adjust", "to change slightly for a better result", "Adjust the setting slowly."},
            {"balance", "a steady relationship between parts", "Balance work and rest."},
            {"focus", "to give attention to something", "Focus on one task."},
            {"notice", "to become aware of something", "Notice the small difference."},
            {"prepare", "to get ready", "Prepare the data first."},
            {"review", "to examine again", "Review the code before sharing."}
        };
        for (String[] value : values) {
            DailyLearningContent.VocabularyItem item = new DailyLearningContent.VocabularyItem();
            item.setWord(value[0]);
            item.setMeaningEnglish(value[1]);
            item.setMeaningTamil("மேம்பட மீண்டும் செய்வது");
            item.setExampleEnglish(value[2]);
            item.setExampleTamil("நான் தினமும் பயிற்சி செய்கிறேன்.");
            item.setExampleRoman(value[2]);
            words.add(item);
        }
        int start = Math.floorMod(date.getDayOfYear() - 1, words.size());
        List<DailyLearningContent.VocabularyItem> dailyWords = new ArrayList<>();
        for (int index = 0; index < 5; index++) {
            dailyWords.add(words.get((start + index) % words.size()));
        }
        content.setVocabulary(dailyWords);
        content.setHindiWords(hindiFallback());
        content.setTeluguWords(teluguFallback());
        content.setMalayalamWords(malayalamFallback());
        return content;
    }

    // 🔥 FALLBACK METHOD
    private AIResponse fallback(String message) {
        AIResponse fallback = new AIResponse();
        fallback.setScore(5);
        fallback.setFinalSummary(message);
        fallback.setMotivation("Keep going! Stay consistent 💪");
        return fallback;
    }
}
