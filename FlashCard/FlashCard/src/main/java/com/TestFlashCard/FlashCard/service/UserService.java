package com.TestFlashCard.FlashCard.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IUser_Repository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class UserService {

    @Autowired
    private final IUser_Repository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    public ResponseEntity<?> updateUser(User user) {
        try {
            userRepository.save(user);
            return new ResponseEntity<>("Update success User with id: " + user.getId(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteUser(int id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find user with id : " + id));
        userRepository.deleteById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByAccountName(String accountName) throws UsernameNotFoundException {
        return userRepository.findByAccountName(accountName);
    }

    public User getUserById(int id) {
        return userRepository.findById(id).orElse(null);
    }
    public User getUserByEmail(String email){
        User user=userRepository.findByEmail(email);
        return user;
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
