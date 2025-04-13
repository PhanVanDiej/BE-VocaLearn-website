package com.TestFlashCard.FlashCard.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TestFlashCard.FlashCard.Enum.Role;
import com.TestFlashCard.FlashCard.config.JwtConfig;
import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.request.GetUserByFilterRequest;
import com.TestFlashCard.FlashCard.request.UserCreateRequest;
import com.TestFlashCard.FlashCard.request.UserLoginRequest;
import com.TestFlashCard.FlashCard.request.UserUpdateRequest;
import com.TestFlashCard.FlashCard.response.LoginResponse;
import com.TestFlashCard.FlashCard.response.renewalTokenResponse;
import com.TestFlashCard.FlashCard.security.JwtTokenProvider;
import com.TestFlashCard.FlashCard.service.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
     @Autowired
     private final UserService userService;
     @Autowired
     private JwtTokenProvider jwtTokenProvider;
     @Autowired
     private JwtConfig jwtConfig;

     @GetMapping("/getAllUsers")
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
          newUser.setRole(Role.USER);

          return userService.createUser(newUser);
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
          String accessToken = jwtTokenProvider.generateAccessToken(user);
          String renewalToken = jwtTokenProvider.generateRenewalToken(user.getId());
          LoginResponse response = new LoginResponse(accessToken, renewalToken, user.getId(), user.getAccountName(),
                    user.getRole().toString());
          return new ResponseEntity<LoginResponse>(response, HttpStatus.OK);
     }

     @GetMapping("/renewalToken")
     public ResponseEntity<?> renewalToken(@RequestHeader("Authorization") String authHeader) {
          try {
               String renewalToken = authHeader.replace("Bearer ", "");
               Claims claims = Jwts.parserBuilder()
                         .setSigningKey(jwtConfig.getSecret().getBytes())
                         .build()
                         .parseClaimsJws(renewalToken) // Kiểm tra chữ ký và thời hạn renewal token
                         .getBody();

               int userId = Integer.parseInt(claims.getSubject());
               User user = userService.getUserById(userId);
               if (user != null) {
                    String token = jwtTokenProvider.generateAccessToken(user);
                    return new ResponseEntity<renewalTokenResponse>(new renewalTokenResponse(token),
                              HttpStatus.ACCEPTED);
               } else
                    return new ResponseEntity<>("The token is valid but the user identity cannot be determined.",
                              HttpStatus.UNAUTHORIZED);
          } catch (ExpiredJwtException e) {
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Renewal token expired. Please login again.");
          } catch (Exception e) {
               return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
          }
     }

     @PutMapping("/update")
     public ResponseEntity<?> updateUser(@RequestBody @Valid UserUpdateRequest request) {
          User user = userService.getUserById(request.getId());
          if (request.getAccountName() != null)
               user.setAccountName(request.getAccountName());
          if (request.getBirthday() != null)
               user.setBirthday(request.getBirthday());
          if (request.getEmail() != null)
               user.setEmail(request.getEmail());
          if (request.getPassWord() != null)
               user.setPassWord(request.getPassWord());
          if (request.getFullName() != null)
               user.setFullName(request.getFullName());

          return userService.updateUser(request.getId());
     }

     @PostMapping("/getUserByFilter")
     public ResponseEntity<?> getUserByFilter(@RequestBody @Valid GetUserByFilterRequest request) {
          if (request.getId() != null) {
               User user = userService.getUserById(request.getId());
               if (user == null)
                    return new ResponseEntity<>("Cannot find user with id: " + request.getId(), HttpStatus.NOT_FOUND);
               return new ResponseEntity<User>(user, HttpStatus.OK);
          }
          if (request.getAccountName() != null) {
               User user = userService.getUserByAccountName(request.getAccountName());
               if (user == null)
                    return new ResponseEntity<>("Cannot find user with account name: " + request.getAccountName(),
                              HttpStatus.NOT_FOUND);
               return new ResponseEntity<User>(user, HttpStatus.OK);
          }
          ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
          problemDetail.setTitle("Invalid request");
          problemDetail.setProperty("message", "At least one field (id or accountName) must be provided");
          return ResponseEntity.badRequest().body(problemDetail);
     }
}