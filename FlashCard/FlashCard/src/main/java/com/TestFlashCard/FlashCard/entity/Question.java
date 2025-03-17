package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "question")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "examID", nullable = false)
    private Exam exam;

    @Column(name = "part")
    private String part;

    @Column(name = "questionDetail", nullable = false)
    private String questionDetail;

    @Column(name = "result")
    private String result;

    @Column(name = "image")
    private String image;

    @Column(name = "audio")
    private String audio;

    @Column(name = "createAt", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createAt;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionClass> optionClasses;
}