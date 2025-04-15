package com.TestFlashCard.FlashCard.request;

import com.TestFlashCard.FlashCard.Enum.FlashCardTopicStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FlashCardTopicCreateRequest {
    @NotNull(message = "Title cannot be null")
    @NotBlank(message = "Title cannot be blank")
    @NotEmpty(message = "Title cannot be empty")
    private String title;

    @NotNull (message = "User's ID cannot be null")
    @Min(value = 1)
    private Integer userID;
    @NotNull(message = "Topic's status cannot be null")
    private FlashCardTopicStatus status;
}
