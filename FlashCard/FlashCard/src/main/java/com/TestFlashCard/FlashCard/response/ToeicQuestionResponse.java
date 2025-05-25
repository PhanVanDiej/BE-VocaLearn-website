package com.TestFlashCard.FlashCard.response;

public record ToeicQuestionResponse(
    int id,
    String part,
    String detail,
    String result,
    String image,
    String audio
) {}
