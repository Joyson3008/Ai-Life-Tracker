package com.joyson.ai_life_tracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import com.joyson.ai_life_tracker.entity.User;
import com.joyson.ai_life_tracker.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ✅ REGISTER
    public User saveUser(User user) {

        // 🔒 Validate password
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("Password cannot be empty");
        }

        // 🔥 Prevent duplicate email
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // 🔐 Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    // ✅ GET USERS
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ LOGIN (FIXED)
    public User login(String email, String password) {

        // 🔥 FIX: don't throw exception
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return null;
        }

        // 🔐 Compare encrypted password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }

        return user;
    }

    // 🔥 CHANGE PASSWORD
    public boolean changePassword(Long id, String currentPassword, String newPassword) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) return false;

        // 🔐 check current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        // 🔐 set new password (encrypted)
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return true;
    }

    // 🔥 DELETE USER
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }

        userRepository.deleteById(id);
    }
}