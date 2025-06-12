package com.TestFlashCard.FlashCard.response;

import java.util.List;

import lombok.Data;

@Data
public class QuestionReviewResponse {
    private Integer questionId;
    private String detail;
    private String userAnswer;
    private String correctAnswer;
    private boolean isCorrect;
    private List<OptionResponse> options;
    
    @Data
    public static class OptionResponse {
        private String mark;   // A/B/C/D
        private String detail; // nội dung câu trả lời
    }
}
