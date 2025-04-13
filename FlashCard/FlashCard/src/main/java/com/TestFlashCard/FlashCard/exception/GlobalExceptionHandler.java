package com.TestFlashCard.FlashCard.exception;

import org.springframework.boot.json.JsonParseException;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        System.out.println("Exception caught: " + ex.getClass().getName());
        ex.printStackTrace();
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation error");

        // Thêm chi tiết lỗi từ validation
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            problemDetail.setProperty(
                    error.getField(),
                    error.getDefaultMessage());
        });
        System.out.println("La tai ai");
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        System.out.println("Exception caught: " + ex.getClass().getName());
        ex.printStackTrace();
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Invalid request format");

        // Kiểm tra nguyên nhân gốc (vd: JSON parse error)
        if (ex.getCause() instanceof JsonParseException) {
            problemDetail.setProperty("message", "Invalid JSON format");
        } else if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) ex.getCause();
            problemDetail.setProperty("message",
                    "Invalid value for field '" + ife.getPath().get(0).getFieldName() + "': " + ife.getValue());
        } else {
            problemDetail.setProperty("message", "Malformed request body");
        }

        return problemDetail;
    }

    @ExceptionHandler(BindException.class)
    public ProblemDetail handleBindException(BindException ex) {
        System.out.println("Exception caught: " + ex.getClass().getName());
        ex.printStackTrace();
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation error");

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            problemDetail.setProperty(
                    error.getField(),
                    error.getDefaultMessage());
        });
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleAllExceptions(Exception ex) {
        System.out.println("Exception caught: " + ex.getClass().getName());
        ex.printStackTrace();
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

}
