package com.TestFlashCard.FlashCard.response;

import java.util.Date;

public record ListFlashCardsResponse(
    int id,
    String title,
    Date reviewDate,
    String learningStatus
) {}