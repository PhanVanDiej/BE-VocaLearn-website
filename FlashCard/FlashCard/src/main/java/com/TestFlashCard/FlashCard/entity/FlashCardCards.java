package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "flashCard_cards")
public class FlashCardCards {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "flashCardID", nullable = false)
    private FlashCard flashCard;

    @ManyToOne
    @JoinColumn(name = "cardID", nullable = false)
    private Card card;
}