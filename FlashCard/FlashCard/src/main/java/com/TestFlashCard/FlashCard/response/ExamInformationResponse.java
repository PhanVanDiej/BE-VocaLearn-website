package com.TestFlashCard.FlashCard.response;

public record ExamInformationResponse(
    int id,
    int duration,
    int parts,
    int questions,
    String title,
    int year,
    String type,
    String collection
) {}