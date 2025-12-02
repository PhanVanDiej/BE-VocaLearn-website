package com.TestFlashCard.FlashCard.controller;

import com.TestFlashCard.FlashCard.entity.ToeicQuestion;
import com.TestFlashCard.FlashCard.request.ToeicQuestionRequestDTO;
import com.TestFlashCard.FlashCard.response.ApiResponse;
import com.TestFlashCard.FlashCard.response.ToeicQuestionResponse;
import com.TestFlashCard.FlashCard.service.ToeicQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/toeic-question")
@RequiredArgsConstructor
public class ToeicQuestionController {

    private final ToeicQuestionService toeicQuestionService;

    @PostMapping
    public ApiResponse<?> create(@RequestBody ToeicQuestionRequestDTO request) {
        try {
            ToeicQuestion created = toeicQuestionService.createToeicQuestion(request);

            // Convert sang Response DTO
            ToeicQuestionResponse response = toeicQuestionService.convertQuestionToResponse(created);

            return new ApiResponse<>(HttpStatus.CREATED.value(), "Tạo thành công câu hỏi", response);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(),
                    "Tạo câu hỏi thất bại vì: " + e.getMessage());
        }
    }

}
