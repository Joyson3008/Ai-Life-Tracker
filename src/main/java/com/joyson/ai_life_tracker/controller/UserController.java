package com.joyson.ai_life_tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.joyson.ai_life_tracker.entity.User;
import com.joyson.ai_life_tracker.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // ✅ REGISTER
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User savedUser = userService.saveUser(user);

            // 🔥 Return only safe data (no password)
            return ResponseEntity.ok(Map.of(
                    "id", savedUser.getId(),
                    "name", savedUser.getName(),
                    "email", savedUser.getEmail()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("❌ Error creating user: " + e.getMessage());
        }
    }

    // ✅ GET ALL USERS (⚠️ Not recommended for large data)
    @GetMapping
    public ResponseEntity<?> getUsers() {
        try {
            List<User> users = userService.getAllUsers();

            // 🔥 Remove password from response
            List<Map<String, Object>> safeUsers = users.stream()
                    .map(u -> Map.of(
                            "id", u.getId(),
                            "name", u.getName(),
                            "email", u.getEmail()
                    ))
                    .toList();

            return ResponseEntity.ok(safeUsers);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("❌ Error fetching users");
        }
    }

    // ✅ GET USER BY ID (🔥 IMPORTANT FOR PERFORMANCE)
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);

            if (user == null) {
                return ResponseEntity.status(404).body("❌ User not found");
            }

            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("❌ Error fetching user");
        }
    }

    // ✅ LOGIN (⚡ OPTIMIZED)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            User loggedInUser = userService.login(
                    user.getEmail(),
                    user.getPassword()
            );

            if (loggedInUser == null) {
                return ResponseEntity.status(401)
                        .body("❌ Invalid email or password");
            }

            // 🔥 Return only necessary data (FASTER + SECURE)
            return ResponseEntity.ok(Map.of(
                    "id", loggedInUser.getId(),
                    "name", loggedInUser.getName(),
                    "email", loggedInUser.getEmail()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("❌ Login error: " + e.getMessage());
        }
    }

    // 🔥 CHANGE PASSWORD
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        try {
            String currentPassword = body.get("currentPassword");
            String newPassword = body.get("newPassword");

            boolean updated = userService.changePassword(
                    id,
                    currentPassword,
                    newPassword
            );

            if (!updated) {
                return ResponseEntity.badRequest()
                        .body("❌ Current password is incorrect");
            }

            return ResponseEntity.ok("✅ Password updated successfully");

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("❌ Error updating password");
        }
    }

    // 🔥 DELETE ACCOUNT
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("✅ User deleted successfully");

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("❌ Error deleting user");
        }
    }
}
