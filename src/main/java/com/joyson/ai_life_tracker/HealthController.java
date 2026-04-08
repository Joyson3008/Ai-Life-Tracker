package com.joyson.ai_life_tracker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "OK";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
