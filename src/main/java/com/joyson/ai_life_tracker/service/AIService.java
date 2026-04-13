package com.joyson.ai_life_tracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyson.ai_life_tracker.dto.AIResponse;

import java.util.*;

@Service
public class AIService {

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
        body.put("model", "llama-3.1-8b-instant");
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
        } catch (Exception e) {
            System.out.println("❌ API CALL FAILED: " + e.getMessage());

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

    // 🔥 FALLBACK METHOD
    private AIResponse fallback(String message) {
        AIResponse fallback = new AIResponse();
        fallback.setScore(5);
        fallback.setFinalSummary(message);
        fallback.setMotivation("Keep going! Stay consistent 💪");
        return fallback;
    }
}
