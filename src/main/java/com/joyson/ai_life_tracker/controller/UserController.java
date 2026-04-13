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
// ❌ REMOVE @CrossOrigin (handled globally now)
public class UserController {

    @Autowired
    private UserService userService;

    // ✅ REGISTER
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User savedUser = userService.saveUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating user: " + e.getMessage());
        }
    }

    // ✅ GET USERS
    @GetMapping
    public ResponseEntity<?> getUsers() {
        try {
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching users");
        }
    }

    // ✅ LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            User loggedInUser = userService.login(user.getEmail(), user.getPassword());

            if (loggedInUser == null) {
                return ResponseEntity.status(401).body("❌ Invalid email or password");
            }

            return ResponseEntity.ok(loggedInUser);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Login error: " + e.getMessage());
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

            boolean updated = userService.changePassword(id, currentPassword, newPassword);

            if (!updated) {
                return ResponseEntity.badRequest().body("❌ Current password is incorrect");
            }

            return ResponseEntity.ok("✅ Password updated successfully");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating password");
        }
    }

    // 🔥 DELETE ACCOUNT
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("✅ User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting user");
        }
    }
}
