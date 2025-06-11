package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EvaluateUpdateByUserRequest {
    private String content;
    @NotNull
    private Integer star;
}
