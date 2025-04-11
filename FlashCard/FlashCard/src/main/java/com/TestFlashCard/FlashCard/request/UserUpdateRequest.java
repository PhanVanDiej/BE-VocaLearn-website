package com.TestFlashCard.FlashCard.request;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotNull( message = "User's ID cannot be null")
    @Min (value = 1, message = "User's ID must be at least 1")
    private Integer id;

    @Size(min = 1, max = 50)
    private String accountName;

    @Size (min = 1, max = 100)
    private String fullName;

    @Past(message = "Birth date must be in the past")
    @JsonFormat(pattern = "yyyy/MM/dd")
    private Date birthday;

    @Email (message = "Email should be valid")
    private String email;

    private String passWord;
}
