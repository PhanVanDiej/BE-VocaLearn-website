package com.TestFlashCard.FlashCard.response;

import java.util.List;

import lombok.Data;

@Data
public class QuestionReviewResponse {
    private Integer questionId;
    private Integer indexNumber;
    private String conversation;
    private String detail;
    private List<String> images;
    private String audio;
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
