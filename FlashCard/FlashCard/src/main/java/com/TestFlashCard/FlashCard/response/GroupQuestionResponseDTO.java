package com.TestFlashCard.FlashCard.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupQuestionResponseDTO {
    private Integer id;
    private String part;
    private String title;
    private String content;
    private String questionRange;
    private Integer examId;
    private List<String> images;
    private List<String> audios;
    private List<ToeicQuestionResponse> questions;
}
