package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "toeicQuestionOption")
@Data
public class ToeicQuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String detail;

    @CreationTimestamp
    @Column(name = "createAt", updatable = false)
    private Date createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toeicQuestionID", nullable = false)
    private ToeicQuestion toeicQuestion;
}