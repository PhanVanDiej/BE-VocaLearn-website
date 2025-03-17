package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "flashCard")
public class FlashCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "tag")
    private String tag;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('private', 'public') DEFAULT 'public'")
    private FlashCardStatus status;

    @OneToMany(mappedBy = "flashCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFlashCards> userFlashCards;

    @OneToMany(mappedBy = "flashCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlashCardCards> flashCardCards;
}

enum FlashCardStatus {
    PRIVATE, PUBLIC
}