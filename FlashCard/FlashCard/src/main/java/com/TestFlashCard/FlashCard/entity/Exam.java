package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

@Data
@Entity
@Table(name = "exam")
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false, length = 100)
    private String set;

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private Date createdAt;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL)
    private List<ToeicQuestion> questions;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL)
    private List<ExamReview> examReviews;
}