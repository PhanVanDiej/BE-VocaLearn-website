package com.TestFlashCard.FlashCard.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ExamReviewResponse {
    private Integer examID;
    private Integer userID;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer duration;
    private LocalDateTime createdAt;
    private List<QuestionReviewResponse> questionReviews;
}
