package com.joyson.ai_life_tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.joyson.ai_life_tracker.entity.User;
import com.joyson.ai_life_tracker.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
// ✅ CORS handled globally in WebConfig — no @CrossOrigin needed here
public class UserController {

    @Autowired
    private UserService userService;

    // ─────────────────────────────────────────────
    // ✅ REGISTER
    // POST /api/users
    // Body: { "name": "...", "email": "...", "password": "..." }
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            Map<String, Object> response = userService.saveUser(user);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // 400 for validation errors (duplicate email, blank password)
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Registration failed"));
        }
    }

    // ─────────────────────────────────────────────
    // ✅ LOGIN
    // POST /api/users/login
    // Body: { "email": "...", "password": "..." }
    // Returns: { "id": 1, "name": "...", "email": "..." }
    // ─────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            Map<String, Object> response = userService.login(user.getEmail(), user.getPassword());

            if (response == null) {
                // 401 for invalid credentials — never reveal which field is wrong
                return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Login failed"));
        }
    }

    // ─────────────────────────────────────────────
    // ✅ GET USER BY ID
    // GET /api/users/{id}
    // ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────
    // ✅ GET ALL USERS (admin / dev use)
    // GET /api/users
    // ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getUsers() {
        try {
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Could not fetch users"));
        }
    }

    // ─────────────────────────────────────────────
    // 🔥 CHANGE PASSWORD
    // PUT /api/users/{id}/password
    // Body: { "currentPassword": "...", "newPassword": "..." }
    // ─────────────────────────────────────────────
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String currentPassword = body.get("currentPassword");
            String newPassword = body.get("newPassword");

            boolean updated = userService.changePassword(id, currentPassword, newPassword);

            if (!updated) {
                return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
            }

            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Password update failed"));
        }
    }

    // ─────────────────────────────────────────────
    // 🔥 DELETE ACCOUNT
    // DELETE /api/users/{id}
    // ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Delete failed"));
        }
    }
}
