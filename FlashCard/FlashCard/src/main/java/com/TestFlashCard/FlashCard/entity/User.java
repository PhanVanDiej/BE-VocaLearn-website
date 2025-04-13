package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import org.hibernate.annotations.CreationTimestamp;
import com.TestFlashCard.FlashCard.Enum.Role;

@Data
@Getter
@Setter
@Entity
@Table (name = "user")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "fullName", nullable = false, length = 100)
    private String fullName;

    @Column(name = "birthday")
    private Date birthday;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "accountName", nullable = false, unique = true, length = 50)
    private String accountName;

    @Column(name = "passWord", nullable = false)
    private String passWord;

    @Column(name = "createAt", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private Date createAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "ENUM('USER', 'ADMIN') DEFAULT 'USER'")
    private Role role;
}