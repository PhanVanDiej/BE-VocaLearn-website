package com.TestFlashCard.FlashCard.response;

import java.util.List;

public record ExamInformationResponse(
    int id,
    int duration,
    int parts,
    int size,
    String title,
    int year,
    String type,
    String collection,
    List<ToeicQuestionResponse> questions
) {}