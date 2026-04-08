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
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // ✅ COMPACT PROMPT — avoids truncation by keeping output short
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


        Map<String, Object> body = new HashMap<>();
        body.put("model", "llama-3.1-8b-instant");
        body.put("temperature", 0.3);
        body.put("max_tokens", 1200); // ✅ Reduced — 1200 is enough for short reviews

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content",
            "You are a JSON-only API. Return valid, complete JSON with no extra text, no markdown, no explanation. " +
            "Always close every JSON object with }. Keep all string values concise (2-3 sentences max).");

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);

        messages.add(systemMsg);
        messages.add(userMsg);

        body.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

        Map responseBody = response.getBody();
        List choices = (List) responseBody.get("choices");
        Map firstChoice = (Map) choices.get(0);
        Map message = (Map) firstChoice.get("message");

        String aiText = (String) message.get("content");

        // 🧼 Strip markdown fences
     // 🧼 CLEAN RESPONSE (FINAL FIX)
        aiText = aiText.replace("```json", "")
                       .replace("```", "")
                       .trim();

        // 🔥 FIX score string → number
   

        System.out.println("===== FINAL CLEANED AI RESPONSE =====");
        System.out.println(aiText);
        System.out.println("====================================");

        // 🔧 REPAIR truncated JSON before parsing
        if (!aiText.endsWith("}")) {
            System.out.println("⚠️ JSON appears truncated — attempting repair...");
            int lastComma = aiText.lastIndexOf(",");
            int lastClosingBrace = aiText.lastIndexOf("}");

            if (lastComma > lastClosingBrace) {
                // Remove the trailing incomplete field and close the object
                aiText = aiText.substring(0, lastComma) + "\n}";
            } else if (!aiText.endsWith("}")) {
                // Try naively closing a hanging string value
                if (aiText.endsWith("\"")) {
                    aiText = aiText + "}";
                } else {
                    aiText = aiText + "\"}";
                }
            }
            System.out.println("🔧 Repaired JSON:");
            System.out.println(aiText);
        }

        try {
            if (!aiText.startsWith("{")) {
                System.out.println("⚠️ Response does not start with { — invalid JSON");
                AIResponse fallback = new AIResponse();
                fallback.setFinalSummary("AI response was invalid. Please try again.");
                fallback.setScore(5);
                return fallback;
            }

            ObjectMapper mapper = new ObjectMapper();

         // 🔥 KEY FIXES (ONLY THESE)
         mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
         mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);

         AIResponse parsed = mapper.readValue(aiText, AIResponse.class);
         return parsed;

        } catch (Exception e) {
            System.out.println("⚠️ JSON parsing failed: " + e.getMessage());
            e.printStackTrace();

            AIResponse fallback = new AIResponse();
            fallback.setFinalSummary("AI parsing failed. Raw response:\n" + aiText);
            fallback.setScore(5);
            return fallback;
        }
    }
}