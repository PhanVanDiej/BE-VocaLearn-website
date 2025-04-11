package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "evaluate")
@Data
public class Evaluate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer star;

    @Column(length = 255)
    private String image;

    @CreationTimestamp
    @Column(name = "createAt", updatable = false)
    private Date createAt;
}