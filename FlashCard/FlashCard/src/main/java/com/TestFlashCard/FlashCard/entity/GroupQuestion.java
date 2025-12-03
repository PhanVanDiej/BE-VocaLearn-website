package com.TestFlashCard.FlashCard.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_question")
@Data
public class GroupQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 20)
    private String part;  // 3,4,7

    @Column(length = 255)
    private String title; // mô tả ngắn

    @Column(columnDefinition = "TEXT")
    private String content; // hội thoại/bài nói/bài đọc

    @Column(length = 50)
    private String questionRange; // "32-35"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ToeicQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupQuestionImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupQuestionAudio> audios = new ArrayList<>();
}

