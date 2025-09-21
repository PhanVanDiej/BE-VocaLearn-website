package com.TestFlashCard.FlashCard.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.Enum.Role;
import com.TestFlashCard.FlashCard.config.JwtConfig;
import com.TestFlashCard.FlashCard.entity.PasswordResetToken;
import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IPasswordResetToken_Repository;
import com.TestFlashCard.FlashCard.repository.IUser_Repository;
import com.TestFlashCard.FlashCard.request.ForgetPasswordRequest;
import com.TestFlashCard.FlashCard.request.ResetPasswordRequest;
import com.TestFlashCard.FlashCard.request.UserCreateRequest;
import com.TestFlashCard.FlashCard.request.UserLoginRequest;
import com.TestFlashCard.FlashCard.request.UserUpdateProfileRequest;
import com.TestFlashCard.FlashCard.request.UserUpdateRequest;
import com.TestFlashCard.FlashCard.response.ApiResponse;
import com.TestFlashCard.FlashCard.response.LoginResponse;
import com.TestFlashCard.FlashCard.response.UserResponse;
import com.TestFlashCard.FlashCard.response.renewalTokenResponse;
import com.TestFlashCard.FlashCard.security.JwtTokenProvider;
import com.TestFlashCard.FlashCard.service.DigitalOceanStorageService;
import com.TestFlashCard.FlashCard.service.EmailService;
import com.TestFlashCard.FlashCard.service.MediaService;
import com.TestFlashCard.FlashCard.service.MinIO_MediaService;
import com.TestFlashCard.FlashCard.service.UserService;
import com.TestFlashCard.FlashCard.service.VisitLogService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
     private final IUser_Repository user_Repository;
     @Autowired
     private JwtTokenProvider jwtTokenProvider;
     @Autowired
     private JwtConfig jwtConfig;
     @Autowired
     private final IPasswordResetToken_Repository passwordResetToken_Repository;
     @Autowired
     private final EmailService emailService;
     @Autowired
     private final MediaService mediaService;
     @Autowired
     private PasswordEncoder passwordEncoder;
     private final VisitLogService visitLogService;
     @Autowired
     private ObjectMapper objectMapper;

     @Autowired
     private final DigitalOceanStorageService storageService;
     @Autowired
     private MinIO_MediaService minIO_MediaService;

     @GetMapping("/getAllUsers")
     public ResponseEntity<?> getAllUsers() {
          List<UserResponse> users = userService.getAllUsers();
          return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(users));
     }

     @PostMapping("/create")
     public ResponseEntity<?> createUser(@RequestPart String dataJson,
               @RequestPart(required = false) MultipartFile avatar) throws IOException {

          if (dataJson == null) {
               throw new BadRequestException("data for create cannot be null");
          }

          UserCreateRequest request = objectMapper.readValue(dataJson, UserCreateRequest.class);

          if (userService.checkExistedAccountName(request.getAccountName())) {
               throw new BadRequestException("Failed to create user. Account name has been existed!");
          }
          if (userService.checkExistedEmail(request.getEmail())) {
               throw new BadRequestException("Failed to create user. Email has been registed!");
          }
          User newUser = new User();

          newUser.setAccountName(request.getAccountName());
          newUser.setEmail(request.getEmail());
          newUser.setFullName(request.getFullName());
          newUser.setPassWord(request.getPassWord());
          newUser.setAddress(request.getAddress());
          newUser.setPhoneNumber(request.getPhoneNumber());

          String uniqueName = minIO_MediaService.uploadFile(avatar);
          newUser.setAvatar(uniqueName);


          if (request.getRole() == null) {
               throw new BadRequestException("User's Role cannot be null");
          }
          newUser.setRole(request.getRole());
          return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(HttpStatus.OK.value(), "Create user successfully", null));
     }

     @PostMapping("/register")
     public ResponseEntity<?> registerUser(@RequestBody @Valid UserCreateRequest request) throws IOException{
          if (userService.checkExistedAccountName(request.getAccountName())) {
               throw new BadRequestException("Failed to create user. Account name has been existed!");
          }
          if (userService.checkExistedEmail(request.getEmail())) {
               throw new BadRequestException("Failed to create user. Email has been used!");
          }
          User newUser = new User();

          newUser.setAccountName(request.getAccountName());
          newUser.setEmail(request.getEmail());
          newUser.setFullName(request.getFullName());
          newUser.setPassWord(request.getPassWord());
          newUser.setAddress(request.getAddress());
          newUser.setPhoneNumber(request.getPhoneNumber());
          newUser.setRole(Role.USER);

          return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(HttpStatus.OK.value(), "Create user successfully", null));
     }

     @PostMapping("/login")
     public ResponseEntity<?> loginUser(@RequestBody @Valid UserLoginRequest request) throws IOException {
          User user = userService.getUserByAccountName(request.getAccountName());
          if (user == null) {
               throw new BadRequestException("Account name doesn't exist.");
          }
          if(user.isDeleted())
               throw new BadRequestException("Account has been deleted!");

          if (!userService.checkPassword(request.getPassWord(), user.getPassWord())) {
               throw new BadRequestException("Invalid Password");
          }
          // Create token
          String accessToken = jwtTokenProvider.generateAccessToken(user);
          String renewalToken = jwtTokenProvider.generateRenewalToken(user.getId());
          LoginResponse response = new LoginResponse(accessToken, renewalToken, user.getId(), user.getAccountName(),
                    user.getRole().toString());

          visitLogService.create();
          return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(200, "Login success!", response));
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
                    visitLogService.create();
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(new renewalTokenResponse(token)));
               } else
                    throw new AuthorizationDeniedException("The token is valid but the user identity cannot be determined.");
          } catch (ExpiredJwtException e) {
               throw new AuthorizationDeniedException("Renewal token expired. Please login again.");
          } catch (Exception e) {
               throw e;
          }
     }

     // User update
     @PutMapping(value = "/updateProfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
     public ResponseEntity<?> changeProfile(@RequestPart String dataJson,
               @RequestPart(required = false) MultipartFile avatar, Principal principal) throws IOException {
          if (dataJson == null) {
               throw new BadRequestException("data for create cannot be null");
          }

          UserUpdateProfileRequest request = objectMapper.readValue(dataJson, UserUpdateProfileRequest.class);

          User user = userService.getUserByAccountName(principal.getName());
          if (request.getAccountName() != null){
               if(userService.getUserByAccountName(request.getAccountName())!=null)
                    throw new BadRequestException("The Account name: '" + request.getAccountName() + "' has been existed!");
               user.setAccountName(request.getAccountName());
          }
          if (request.getBirthday() != null){
               if(request.getBirthday().isBefore(LocalDate.now()))
                    throw new BadRequestException("Invalid Birthday value!");
               user.setBirthday(request.getBirthday());}
          if (request.getEmail() != null)
               user.setEmail(request.getEmail());
          if (request.getFullName() != null)
               user.setFullName(request.getFullName());
          if (request.getAddress() != null)
               user.setAddress(request.getAddress());
          if (request.getPhoneNumber() != null)
               user.setPhoneNumber(request.getPhoneNumber());

          if (avatar != null) {
               if (user.getAvatar() != null)
                    minIO_MediaService.deleteFile(user.getAvatar());
               user.setAvatar(minIO_MediaService.uploadFile(avatar));
          }

          userService.updateUser(user);

          return ResponseEntity.status(HttpStatus.OK).body(null);
     }

     // Admin update
     @PutMapping("/update")
     public ResponseEntity<?> update(@RequestPart String dataJson, @RequestPart(required = false) MultipartFile avatar)
               throws IOException {

          if (dataJson == null) {
               throw new BadRequestException("data for create cannot be null");
          }

          UserUpdateRequest request = objectMapper.readValue(dataJson, UserUpdateRequest.class);

          if (request.getId() == null) {
               throw new BadRequestException("id cannot be null");
          }
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
          if (request.getAddress() != null)
               user.setAddress(request.getAddress());
          if (request.getPhoneNumber() != null)
               user.setPhoneNumber(request.getPhoneNumber());

          if (avatar != null) {
               if (user.getAvatar() != null)
                    storageService.deleteImage(user.getAvatar());
               user.setAvatar(mediaService.getImageUrl(avatar));
          }
          userService.updateUser(user);
          return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
     }

     @GetMapping("/getUserByFilter")
     public ResponseEntity<?> getUserByFilter(@RequestParam(required = false) Integer userID,
               @RequestParam(required = false) String accountName) throws IOException {
          if (userID != null) {
               User user = userService.getUserById(userID);
               if (user == null){
                    throw new ResourceNotFoundException("Cannot find user with id: " + userID);
               }
               return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(user));
          }
          if (accountName != null && !accountName.isEmpty()) {
               User user = userService.getUserByAccountName(accountName);
               if (user == null)
                    throw new ResourceNotFoundException("Cannot find user with account name: " + accountName);
                    
               return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(user));
          }
          
          throw new BadRequestException("At least One filter: Id or AccountName have been provided!");
     }

     @DeleteMapping("/delete/{id}")
     public ResponseEntity<?> deleteUserById(@PathVariable Integer id) throws Exception {
          userService.deleteUser(id);
          return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
     }

     @PostMapping("/forgot-password")
     public ResponseEntity<?> sendResetCode(@RequestBody @Valid ForgetPasswordRequest request) throws IOException {
          try {
               String email = request.getEmail();
               User user = userService.getUserByEmail(email);

               if (user == null) {
                    throw new ResourceNotFoundException("Email doesn't exist");
               }

               if (!user.getAccountName().equals(request.getAccountName())) {
                    throw new ResourceNotFoundException("Cannot find the User with accountName: "
                              + request.getAccountName() + ". Wrong AccountName!");
               }
               String token = String.valueOf((int) (Math.random() * 900000) + 100000); // 6 chữ số
               PasswordResetToken resetToken = new PasswordResetToken();
               resetToken.setEmail(email);
               resetToken.setToken(token);
               passwordResetToken_Repository.save(resetToken);

               // Luu token lam password tam thoi
               user.setPassWord(token);
               user_Repository.save(user);

               // Gửi email
               emailService.send(
                         email,
                         "Yêu cầu đổi mật khẩu - Vocabulary English FlashCard account",
                         "Mật khẩu mới cho tài khoản " + user.getAccountName() + ": " + token +
                                   "\n\nĐây chỉ là mật khẩu tạm thời.\nĐể đảm bảo bảo mật, vui lòng cập nhật mật khẩu của bạn.");
               return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
          } catch (Exception exception) {
               throw new IOException("Intenal Server Error: " + exception.getMessage());
          }
     }

     @PostMapping("/verify-reset-code")
     public ResponseEntity<?> verifyCode(@RequestBody String email, @RequestParam String token) throws IOException{
          PasswordResetToken latest = passwordResetToken_Repository.findTopByEmailOrderByCreatedAtDesc(email)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find the verified token"));

          if (latest.isUsed())
               throw new BadRequestException("This token has been used!");
          if (!latest.getToken().equals(token))
               throw new BadRequestException("Incorrect verified token!");

          // Gắn flag used nếu cần
          latest.setUsed(true);
          passwordResetToken_Repository.save(latest);

          return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
     }

     @PostMapping("/reset-password")
     public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request, Principal principal)
               throws IOException {
          User user = userService.getUserByAccountName(principal.getName());

          if (!userService.checkPassword(request.getOldPassword(), user.getPassWord())) {
               throw new BadRequestException("Incorrect Password!!!");
          }

          user.setPassWord(passwordEncoder.encode(request.getNewPassword()));
          user_Repository.save(user);

          return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
     }
}