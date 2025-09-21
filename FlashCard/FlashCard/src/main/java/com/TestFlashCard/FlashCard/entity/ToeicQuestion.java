package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "toeicQuestion")
@Data
public class ToeicQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private Integer indexNumber;

    @Column(length = 50)
    private String part;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String detail;

    @Column(nullable = false, length = 1)
    private String result;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String clarify;

    @Column(length = 255)
    private String image;

    @Column(length = 255)
    private String audio;

    @Column
    private String conversation;

    @CreationTimestamp
    @Column(name = "createAt", updatable = false)
    private LocalDateTime createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examID", nullable = false)
    private Exam exam;

    @OneToMany(mappedBy = "toeicQuestion", cascade = CascadeType.ALL)
    private List<ToeicQuestionOption> options;

    @OneToMany(mappedBy = "toeicQuestion", cascade = CascadeType.ALL)
    private List<QuestionReview> questionReviews;
}