package org.example.orderservice.helper;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.example.orderservice.dto.ApiResponse;
import org.example.orderservice.exception.OrderCancellationException;
import org.example.orderservice.exception.ResourceNotFoundException;
import org.example.orderservice.exception.UnAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHelper {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHelper.class);

    private ApiResponse<Map<String, String>> buildErrorResponse(String message, Map<String, String> errors, HttpStatus status) {
        logger.error("[ExceptionHandler] {}  errors={}", message, errors);
        return new ApiResponse<>(message, errors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        logger.warn("[ValidationException] {}", errors);
        return buildErrorResponse("Validation failed", errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Map<String, String>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        logger.warn("[ResourceNotFoundException] {}", ex.getMessage());
        return buildErrorResponse("Resource not found", errors, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> errors = new HashMap<>();
        String paramName = ex.getName();
        errors.put(paramName, "Invalid value. Please provide a valid value.");
        logger.warn("[TypeMismatchException] param={} value={} message={}", paramName, ex.getValue(), ex.getMessage());
        return buildErrorResponse("Invalid parameter", errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnAuthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Map<String, String>> handleUnAuthorizedException(UnAuthorizedException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        logger.warn("[UnAuthorizedException] {}", ex.getMessage());
        return buildErrorResponse("Unauthorized access", errors, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, String> errors = new HashMap<>();
        Throwable cause = ex.getCause();

        while (cause != null) {
            if (cause instanceof UnrecognizedPropertyException) {
                UnrecognizedPropertyException upe = (UnrecognizedPropertyException) cause;
                String fieldName = upe.getPropertyName();
                errors.put(fieldName, "Unknown field");
                logger.warn("[UnrecognizedPropertyException] {}", upe.getMessage(), upe);
                return buildErrorResponse("Malformed request body", errors, HttpStatus.BAD_REQUEST);
            }
            else if (cause instanceof MismatchedInputException) {
                MismatchedInputException mie = (MismatchedInputException) cause;
                String fieldName = mie.getPath() != null && !mie.getPath().isEmpty()
                        ? mie.getPath().get(0).getFieldName()
                        : "unknownField";
                errors.put(fieldName, "Invalid value for field");
                logger.warn("[MismatchedInputException] {}", mie.getMessage(), mie);
                return buildErrorResponse("Invalid input", errors, HttpStatus.BAD_REQUEST);
            }
            cause = cause.getCause();
        }

        errors.put("error", "Malformed or missing request body");
        logger.warn("[HttpMessageNotReadableException] {}", ex.getMessage(), ex);
        return buildErrorResponse("Invalid request", errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OrderCancellationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleOrderCancellationException(OrderCancellationException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        logger.warn("[OrderCancellationException] {}", ex.getMessage());
        return buildErrorResponse("Order cancellation failed", errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Unexpected error occurred please try again after sometime");
        logger.error("[GenericException] {}", ex.getMessage(), ex);
        return buildErrorResponse("Internal server error", errors, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
