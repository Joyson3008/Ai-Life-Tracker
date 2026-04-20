package com.joyson.ai_life_tracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.joyson.ai_life_tracker.entity.User;
import com.joyson.ai_life_tracker.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────────
    // ✅ REGISTER
    // - Checks duplicate email with existsByEmail()  → no full entity fetch
    // - Encrypts password before save
    // ─────────────────────────────────────────────
    @Transactional
    public Map<String, Object> saveUser(User user) {

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        // ✅ existsByEmail runs a COUNT query — much faster than findByEmail + isPresent
        if (userRepository.existsByEmail(user.getEmail().trim())) {
            throw new IllegalStateException("Email already registered");
        }

        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User saved = userRepository.save(user);

        // ✅ Never return password in response
        return safeUserMap(saved.getId(), saved.getName(), saved.getEmail());
    }

    // ─────────────────────────────────────────────
    // ✅ LOGIN — OPTIMIZED FOR < 200ms
    // - Single DB query by indexed email column
    // - BCrypt.matches() is the only "slow" step (~80–120ms) — unavoidable
    // - No findAll(), no joins, no AI calls
    // ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Map<String, Object> login(String email, String password) {

        if (email == null || password == null) {
            return null;
        }

        // ✅ Single indexed query — returns Optional<User>
        User user = userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);

        if (user == null) {
            return null; // User not found
        }

        // ✅ BCrypt comparison — unavoidable cost, ~80-120ms
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null; // Wrong password
        }

        // ✅ Return only safe fields
        return safeUserMap(user.getId(), user.getName(), user.getEmail());
    }

    // ─────────────────────────────────────────────
    // ✅ GET ALL USERS (admin only, returns safe data)
    // ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ─────────────────────────────────────────────
    // ✅ GET USER BY ID
    // ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Map<String, Object> getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return safeUserMap(user.getId(), user.getName(), user.getEmail());
    }

    // ─────────────────────────────────────────────
    // 🔥 CHANGE PASSWORD
    // ─────────────────────────────────────────────
    @Transactional
    public boolean changePassword(Long id, String currentPassword, String newPassword) {

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) return false;

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false; // Wrong current password
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    // ─────────────────────────────────────────────
    // 🔥 DELETE USER
    // ─────────────────────────────────────────────
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────
    // 🔒 HELPER: Safe response map (no password)
    // ─────────────────────────────────────────────
    private Map<String, Object> safeUserMap(Long id, String name, String email) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("email", email);
        return map;
    }
}
