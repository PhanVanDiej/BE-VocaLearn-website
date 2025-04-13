package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "flashCard")
public class FlashCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('private', 'public') DEFAULT 'public'")
    private FlashCardStatus status;

    @Column(name = "reviewDate")
    private Date reviewDate;

    @Column(name = "createdAt", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topicID", nullable = false)
    private FlashCardTopic topic;

    public enum FlashCardStatus {
        PRIVATE, PUBLIC
    }
}

