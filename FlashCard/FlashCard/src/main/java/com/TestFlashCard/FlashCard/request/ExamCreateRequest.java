package com.TestFlashCard.FlashCard.request;

import com.TestFlashCard.FlashCard.Enum.ExamCollection;
import com.TestFlashCard.FlashCard.Enum.ExamType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExamCreateRequest {
    @NotNull(message = "duration cannot be null")
    private Integer duration;

    @NotNull
    @NotBlank
    @NotEmpty
    private String title;

    @NotNull
    private Integer year;

    @NotNull
    private ExamType type;

    @NotNull
    private ExamCollection collection;
}
