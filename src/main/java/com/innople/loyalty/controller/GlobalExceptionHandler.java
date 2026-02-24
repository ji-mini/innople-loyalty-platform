package com.innople.loyalty.controller;

import com.innople.loyalty.domain.common.TenantMismatchException;
import com.innople.loyalty.service.member.MemberExceptions;
import com.innople.loyalty.service.points.PointExceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ApiErrorResponse.of(message));
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            TenantMismatchException.class,
            PointExceptions.InvalidPointAmountException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ApiErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler({
            PointExceptions.InsufficientPointsException.class,
            PointExceptions.PointAccountNotFoundException.class,
            MemberExceptions.MemberAlreadyExistsException.class,
            MemberExceptions.MemberNotFoundException.class,
            MemberExceptions.InvalidMemberStatusException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBusiness(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse.of(ex.getMessage()));
    }

    private String formatFieldError(FieldError error) {
        String field = error.getField();
        String msg = error.getDefaultMessage();
        return field + ": " + msg;
    }
}

