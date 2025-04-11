package com.TestFlashCard.FlashCard.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation error");

        // Thêm chi tiết lỗi từ validation
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            problemDetail.setProperty(
                    error.getField(),
                    error.getDefaultMessage());
        });
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleAllExceptions(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

}
