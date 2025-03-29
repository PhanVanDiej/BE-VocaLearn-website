package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.TestFlashCard.FlashCard.Enum.Role;

@Data
@Entity
@Table (name = "user_table")
@Getter
@Setter
@NoArgsConstructor // Tạo constructor không tham số
@AllArgsConstructor // Tạo constructor với tất cả tham số
@Builder // Hỗ trợ pattern Builder
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fullName", nullable = false)
    private String fullName;

    @Column(name = "birthday")
    private Date birthday;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "accountName", nullable = false, unique = true)
    private String accountName;

    @Column(name = "passWord", nullable = false)
    private String passWord;

    @Column(name = "createAt", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private Date createAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "ENUM('USER', 'ADMIN') DEFAULT 'USER'")
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFlashCards> userFlashCards;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestResult> testResults;

    @Column(name="VERIFICATIONCODE", columnDefinition="varchar(100) not null", nullable=false)
    private String verificationCode;
}