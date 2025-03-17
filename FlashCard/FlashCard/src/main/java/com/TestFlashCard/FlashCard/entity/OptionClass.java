package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "optionClass")
public class OptionClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "questionID", nullable = false)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "optionID", nullable = false)
    private Option option;
}