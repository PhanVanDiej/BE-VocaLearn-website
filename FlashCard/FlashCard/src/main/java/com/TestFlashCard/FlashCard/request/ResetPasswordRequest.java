package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotNull (message = "New Password cannot be null")
    @NotEmpty(message = "New Password cannot be empty")
    @NotBlank(message = "New Password cannot be blank")
    public String newPassword;
    @NotNull (message = "Email cannot be null")
    @NotEmpty (message = "Email cannot be empty")
    @NotBlank (message = "Email cannot be blank")
    public String email;
}
