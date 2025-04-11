package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "flashCard_topic")
@Data
public class FlashCardTopic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @CreationTimestamp
    @Column(name = "createAt", updatable = false)
    private Date createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", nullable = false)
    private User user;
}