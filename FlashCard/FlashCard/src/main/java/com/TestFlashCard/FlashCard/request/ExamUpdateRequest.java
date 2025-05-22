package com.TestFlashCard.FlashCard.request;

import com.TestFlashCard.FlashCard.Enum.ExamCollection;
import com.TestFlashCard.FlashCard.Enum.ExamType;

import lombok.Data;

@Data
public class ExamUpdateRequest {
    private Integer duration;
    private String title;
    private Integer year;
    private ExamType type;
    private ExamCollection collection;
}
