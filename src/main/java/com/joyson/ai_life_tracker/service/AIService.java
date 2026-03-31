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

        // 🔐 Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 🧠 IMPROVED PROMPT (REDUCED SIZE → avoids truncation)
        String prompt =
        	    "You are an INTELLIGENT and HUMAN-LIKE AI productivity analyst.\n\n" +

        	    "CRITICAL RULES:\n" +
        	    "1. Each review must contain 3-4 meaningful sentences\n" +  // 🔥 reduced slightly
        	    "2. Each sentence must be insightful and specific\n" +
        	    "3. DO NOT repeat ideas\n" +
        	    "4. NEVER say 'no information provided'\n" +
        	    "5. Use \\n to separate lines inside each review\n\n" +

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

        	    "STRICT RULE:\n" +
        	    "- Return ONLY valid JSON\n" +
        	    "- DO NOT add explanation outside JSON\n\n" +

        	    "Daily Log:\n" + text + "\n\n" +

        	    "Return JSON in this format:\n" +
        	    "{\n" +
        	    "  \"score\": 8,\n" +
        	    "  \"bibleReview\": \"3-4 sentences with \\n\",\n" +
        	    "  \"bookReview\": \"3-4 sentences with \\n\",\n" +
        	    "  \"codingReview\": \"3-4 sentences\",\n" +
        	    "  \"csTopicReview\": \"3-4 sentences\",\n" +
        	    "  \"collegeReview\": \"3-4 sentences\",\n" +
        	    "  \"diaryReview\": \"3-4 sentences\",\n" +
        	    "  \"expensesReview\": \"2-3 sentences\",\n" +
        	    "  \"movieReview\": \"2-3 sentences\",\n" +
        	    "  \"phoneUsageReview\": \"2-3 sentences\",\n" +
        	    "  \"finalSummary\": \"4-5 sentences overall day analysis\",\n" +
        	    "  \"motivation\": \"2-3 powerful realistic lines\"\n" +
        	    "}";
        // 📦 Request body
        Map<String, Object> body = new HashMap<>();
        body.put("model", "llama-3.1-8b-instant");
        body.put("temperature", 0.3);
        body.put("max_tokens", 4000); // 🔥 increased

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content",
                "Generate valid JSON only. No extra text.");

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);

        messages.add(systemMsg);
        messages.add(userMsg);

        body.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // 🚀 API Call
        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

        // 🔍 Extract response
        Map responseBody = response.getBody();
        List choices = (List) responseBody.get("choices");
        Map firstChoice = (Map) choices.get(0);
        Map message = (Map) firstChoice.get("message");

        String aiText = (String) message.get("content");

        // 🧼 Clean markdown
        aiText = aiText.replace("```json", "")
                       .replace("```", "")
                       .trim();

        // 🔥 Fix score string → int
        aiText = aiText.replaceAll("\"score\"\\s*:\\s*\"(\\d+)\"", "\"score\": $1");

        // 🔥 DEBUG
        System.out.println("===== RAW AI RESPONSE =====");
        System.out.println(aiText);
        System.out.println("===========================");

        try {
            // ✅ SAFETY CHECK (IMPORTANT)
            if (!aiText.startsWith("{") || !aiText.endsWith("}")) {
                System.out.println("⚠️ Invalid or incomplete JSON!");

                AIResponse fallback = new AIResponse();
                fallback.setFinalSummary("AI response incomplete. Try again.");
                fallback.setScore(5);
                return fallback;
            }

            ObjectMapper mapper = new ObjectMapper();
            AIResponse parsed = mapper.readValue(aiText, AIResponse.class);

            return parsed;

        } catch (Exception e) {
            e.printStackTrace();

            // ✅ SAFE FALLBACK (NO CRASH)
            AIResponse fallback = new AIResponse();
            fallback.setFinalSummary("AI parsing failed. Showing raw response:\n" + aiText);
            fallback.setScore(5);

            return fallback;
        }
    }
}