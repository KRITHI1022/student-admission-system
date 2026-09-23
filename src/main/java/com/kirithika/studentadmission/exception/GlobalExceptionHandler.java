package com.kirithika.studentadmission.exception;

import com.kirithika.studentadmission.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(
            ResourceAlreadyExistsException ex,
            HttpServletRequest req) {

        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                req
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest req) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                req
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest req) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password",
                req
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest req) {

        String message =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error ->
                                error.getField()
                                        + ": "
                                        + error.getDefaultMessage()
                        )
                        .collect(Collectors.joining(", "));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message,
                req
        );
    }

    @ExceptionHandler(EligibilityException.class)
    public ResponseEntity<ErrorResponse> handleEligibility(
            EligibilityException ex,
            HttpServletRequest req) {

        return buildResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage(),
                req
        );
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorage(
            FileStorageException ex,
            HttpServletRequest req) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                req
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(
            MaxUploadSizeExceededException ex,
            HttpServletRequest req) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "File size must not exceed 5MB",
                req
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest req) {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                req
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest req) {

        ErrorResponse error = new ErrorResponse();

        error.setTimestamp(LocalDateTime.now());
        error.setStatus(status.value());
        error.setError(status.getReasonPhrase());
        error.setMessage(message);
        error.setPath(req.getRequestURI());

        return ResponseEntity
                .status(status)
                .body(error);
    }
}