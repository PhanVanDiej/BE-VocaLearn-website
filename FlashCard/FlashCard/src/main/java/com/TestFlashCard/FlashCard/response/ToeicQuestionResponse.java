package com.TestFlashCard.FlashCard.response;

import java.util.List;

public record ToeicQuestionResponse(
    int id,
    String part,
    String detail,
    String result,
    String image,
    String audio,
    List<OptionResponse> options
) {
    public record OptionResponse(
        String mark,
        String detail
    ) {}
}
