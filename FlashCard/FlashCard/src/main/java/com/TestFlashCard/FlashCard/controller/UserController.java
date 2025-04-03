package com.TestFlashCard.FlashCard.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TestFlashCard.FlashCard.DTO.LoginResponse;
import com.TestFlashCard.FlashCard.Enum.Role;
import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.request.UserCreateRequest;
import com.TestFlashCard.FlashCard.request.UserLoginRequest;
import com.TestFlashCard.FlashCard.security.JwtTokenProvider;
import com.TestFlashCard.FlashCard.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
     @Autowired
     private final UserService userService;
     @Autowired
     private JwtTokenProvider jwtTokenProvider;

     @GetMapping
     public ResponseEntity<List<User>> getAllUsers() {
          List<User> users = userService.getAllUsers();
          return ResponseEntity.ok(users);
     }
     @PostMapping("/create")
     public ResponseEntity<?> createUser(@RequestBody @Valid UserCreateRequest request) {
          if (userService.checkExistedAccountName(request.getAccountName())) {
               return new ResponseEntity<>("Failed to create user. Account name has been existed!",
                         HttpStatus.BAD_REQUEST);
          }
          if (userService.checkExistedEmail(request.getEmail())) {
               return new ResponseEntity<>("Failed to create user. Email has been registed!", HttpStatus.BAD_REQUEST);
          }
          User newUser = new User();

          newUser.setAccountName(request.getAccountName());
          newUser.setBirthday(request.getBirthday());
          newUser.setEmail(request.getEmail());
          newUser.setFullName(request.getFullName());
          newUser.setVerificationCode("check");
          newUser.setPassWord(request.getPassWord());
          if (request.getRole() == null) {
               return new ResponseEntity<>("User's Role cannot be null", HttpStatus.BAD_REQUEST);
          }
          newUser.setRole(request.getRole());
          
          userService.createUser(newUser);

          return new ResponseEntity<>("create new User success", HttpStatus.OK);
     }

     @PostMapping("/register")
     public ResponseEntity<?> registerUser(@RequestBody @Valid UserCreateRequest request) {
          if (userService.checkExistedAccountName(request.getAccountName())) {
               return new ResponseEntity<>("Failed to create user. Account name has been existed!",
                         HttpStatus.BAD_REQUEST);
          }
          if (userService.checkExistedEmail(request.getEmail())) {
               return new ResponseEntity<>("Failed to create user. Email has been registed!", HttpStatus.BAD_REQUEST);
          }
          User newUser = new User();

          newUser.setAccountName(request.getAccountName());
          newUser.setBirthday(request.getBirthday());
          newUser.setEmail(request.getEmail());
          newUser.setFullName(request.getFullName());
          newUser.setPassWord(request.getPassWord());
          newUser.setVerificationCode("check");
          newUser.setRole(Role.USER);

          userService.createUser(newUser);

          return new ResponseEntity<>("create new User success", HttpStatus.OK);
     }

     @PostMapping("/login")
     public ResponseEntity<?> loginUser(@RequestBody @Valid UserLoginRequest request) {
          User user = userService.getUserByAccountName(request.getAccountName());
          if (user == null) {
               return new ResponseEntity<>("Account name doesn't exist.", HttpStatus.BAD_REQUEST);
          }

          if (!userService.checkPassword(request.getPassWord(), user.getPassWord())) {
               return new ResponseEntity<>("Invalid Password", HttpStatus.UNAUTHORIZED);
          }
          // Create token
          String token = jwtTokenProvider.generateToken(user);
          LoginResponse response = new LoginResponse(token, user.getId(), user.getAccountName(),
                    user.getRole().toString());
          return new ResponseEntity<LoginResponse>(response, HttpStatus.OK);
     }
}