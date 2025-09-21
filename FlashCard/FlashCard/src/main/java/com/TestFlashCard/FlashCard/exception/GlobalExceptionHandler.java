package com.TestFlashCard.FlashCard.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParseException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import com.TestFlashCard.FlashCard.response.ApiResponse;
// đổi import allowed types theo service bạn đang dùng:
import com.TestFlashCard.FlashCard.service.MinIO_MediaService;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Helper để build ResponseEntity<ApiResponse<?>>
    private <T> ResponseEntity<ApiResponse<T>> build(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status)
                .body(ApiResponse.of(status.value(), message, data));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleAllExceptions(Exception ex) {
        logger.error("Unhandled exception", ex);
        Map<String, Object> data = Map.of("detail", ex.getMessage());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", data);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage(),
                        (a, b) -> a, // nếu trùng field, lấy message đầu tiên
                        LinkedHashMap::new));
        return build(HttpStatus.BAD_REQUEST, "Validation error", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (ex.getCause() instanceof JsonParseException) {
            data.put("message", "Invalid JSON format");
        } else if (ex.getCause() instanceof InvalidFormatException ife) {
            String field = (ife.getPath() != null && !ife.getPath().isEmpty())
                    ? ife.getPath().get(0).getFieldName()
                    : "unknown";
            data.put("message", "Invalid value for field '" + field + "'");
            data.put("invalidValue", String.valueOf(ife.getValue()));
        } else {
            data.put("message", "Malformed request body");
        }
        return build(HttpStatus.BAD_REQUEST, "Invalid request format", data);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(BindException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage(),
                        (a, b) -> a,
                        LinkedHashMap::new));
        return build(HttpStatus.BAD_REQUEST, "Validation error", errors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, Object> data = Map.of(
                "errorCode", "RESOURCE_NOT_FOUND",
                "detail", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "Resource Not Found", data);
    }

    @ExceptionHandler(InvalidImageException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleInvalidImageException(InvalidImageException ex) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("errorCode", "IMAGE_001");
        data.put("allowedTypes", MinIO_MediaService.ALLOWED_IMAGE_TYPES);
        return build(HttpStatus.BAD_REQUEST, "Invalid Image", data);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleStorageException(StorageException ex) {
        Map<String, Object> data = Map.of("errorCode", "STORAGE_001");
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Storage Error", data);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex) {

        logger.error("Media type not supported: {}", ex.getContentType());
        logger.error("Supported types: {}", ex.getSupportedMediaTypes());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("timestamp", LocalDateTime.now());
        data.put("requestedType", ex.getContentType());
        data.put("supportedTypes", ex.getSupportedMediaTypes());

        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Media type not supported", data);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleBadRequestExceptions(BadRequestException exception) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("detail", exception.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Bad Request", data);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleUnauthorizedException(
            AuthorizationDeniedException exception) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("detail", exception.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", data);
    }
}
