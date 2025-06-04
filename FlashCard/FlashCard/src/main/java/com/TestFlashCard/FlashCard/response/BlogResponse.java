package com.TestFlashCard.FlashCard.response;

public record BlogResponse(
    int id,
    String title,
    String category,
    String shortDetail,
    String image,
    String detail
) {}
