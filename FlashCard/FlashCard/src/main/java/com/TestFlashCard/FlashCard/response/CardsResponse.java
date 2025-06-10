package com.TestFlashCard.FlashCard.response;

public record CardsResponse(
    int id,
    String terminology,
    String definition,
    String image,
    String audio,
    String pronounce,
    int level,
    int isRemember,
    String partOfSpeech,
    String example
){}
