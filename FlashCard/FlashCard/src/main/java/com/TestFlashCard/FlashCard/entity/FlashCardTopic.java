package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import com.TestFlashCard.FlashCard.Enum.FlashCardTopicStatus;

import java.util.Date;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('PRIVATE', 'PUBLIC') DEFAULT 'PUBLIC'")
    private FlashCardTopicStatus status;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private List<FlashCard>flashCards;
}