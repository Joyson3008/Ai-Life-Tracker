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
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    // ✅ REGISTER
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    // ✅ GET USERS
    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    // ✅ LOGIN (WORKING)
    @PostMapping("/login")
    public User login(@RequestBody User user) {
        return userService.login(user.getEmail(), user.getPassword());
    }

    // 🔥 CHANGE PASSWORD
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        boolean updated = userService.changePassword(id, currentPassword, newPassword);

        if (!updated) {
            return ResponseEntity.badRequest().body("❌ Current password is incorrect");
        }

        return ResponseEntity.ok("✅ Password updated successfully");
    }

    // 🔥 DELETE ACCOUNT
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("✅ User deleted successfully");
    }
}
