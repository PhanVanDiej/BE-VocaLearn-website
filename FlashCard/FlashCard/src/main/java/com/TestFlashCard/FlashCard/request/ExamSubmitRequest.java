package com.TestFlashCard.FlashCard.request;

import java.util.List;

import lombok.Data;

@Data
public class ExamSubmitRequest {
    private Integer examId;
    private Integer duration;
    private List<ToeicQuestionRecord> answers;
}
