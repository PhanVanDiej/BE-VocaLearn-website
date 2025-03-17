package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;
@Data
@Entity
@Table(name = "card")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terminology", nullable = false)
    private String terminology;

    @Column(name = "definition")
    private String definition;

    @Column(name = "image")
    private String image;

    @Column(name = "audio")
    private String audio;

    @Column(name = "level", columnDefinition = "INT DEFAULT 1")
    private int level;

    @Column(name = "nearestDateLearn")
    private Date nearestDateLearn;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlashCardCards> flashCardCards;
}