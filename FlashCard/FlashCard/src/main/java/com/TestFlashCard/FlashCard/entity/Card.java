package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
@Data
@Entity
@Table(name = "card")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "terminology", nullable = false, length = 255)
    private String terminology;

    @Column(name = "definition")
    private String definition;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "audio", length = 255)
    private String audio;

    @Column(name = "pronounce", length = 255)
    private String pronounce;

    @Column(name = "level", columnDefinition = "INT DEFAULT 1")
    private Integer level = 1;
    
    @Column(name = "partOfSpeech",nullable = false, length = 50)
    private String partOfSpeech;

    @Column(name = "example")
    private String example;

    @CreationTimestamp
    @Column(name = "createAt", updatable = false)
    private LocalDateTime createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashCardID", nullable = false)
    private FlashCard flashCard;
}