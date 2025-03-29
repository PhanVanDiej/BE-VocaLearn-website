package com.TestFlashCard.FlashCard.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.repository.IUser_Repository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class UserService {

    @Autowired
    private final IUser_Repository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // CREATE
    public ResponseEntity<?> createUser(User user) {
        try {
            user.setPassWord(passwordEncoder.encode(user.getPassWord()));
            userRepository.save(user);
            return ResponseEntity.ok("Create user successfully");
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public boolean checkExistedAccountName(String accountName) {
        return userRepository.findByAccountName(accountName) != null;
    }

    public boolean checkExistedEmail(String email) {
        return userRepository.findByEmail(email) != null;
    }

    // UPDATE
    public User updateUser(User userDetails, Long id) {
        return userRepository.findById(id).map(user -> {
            user.setAccountName(userDetails.getAccountName());
            user.setBirthday(userDetails.getBirthday());
            user.setEmail(userDetails.getEmail());
            user.setPassWord(userDetails.getPassWord());
            return userRepository.save(user);
        }).orElse(null);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByAccountName(String accountName) {
        return userRepository.findByAccountName(accountName);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        System.out.println("Raw Password: " + rawPassword); // Phải là "123"
        System.out.println("Encoded Password: " + encodedPassword); // Phải có dạng BCrypt
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
