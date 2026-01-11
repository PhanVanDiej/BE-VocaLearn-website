package com.TestFlashCard.FlashCard.response;

import java.util.List;

public record ExamInformationResponse(
    int id,
    Integer duration,
    Integer parts,
    Integer size,
    String title,
    Integer year,
    String type,
    String collection,
    Integer attemps,
    Integer numberOfComment,
    String fileImportName,
    List<ToeicQuestionResponse> questions,       // Câu đơn: part 1,2,5
    List<GroupQuestionResponseDTO> groupQuestions // Câu nhóm: part 3,4,7
) {}
