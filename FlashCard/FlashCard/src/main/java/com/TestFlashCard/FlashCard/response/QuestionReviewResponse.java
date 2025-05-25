package com.TestFlashCard.FlashCard.response;

import lombok.Data;

@Data
public class QuestionReviewResponse {
    private Integer questionId;
    private String userAnswer;
    private String correctAnswer;
    private boolean isCorrect;
}
