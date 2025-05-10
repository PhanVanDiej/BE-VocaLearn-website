package com.TestFlashCard.FlashCard.request;

import lombok.Data;

@Data
public class CardCreateRequest {
    private String terminology;
    private String definition;
    private String audio;
    private String pronounce;
    private String partOfSpeech;
    private Integer level;
    private String example;
    private Integer flashCardID;
}
