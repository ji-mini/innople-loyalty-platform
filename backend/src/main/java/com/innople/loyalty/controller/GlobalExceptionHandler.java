package com.innople.loyalty.controller;

import com.innople.loyalty.config.AdminRoleResolver;
import com.innople.loyalty.domain.common.TenantMismatchException;
import com.innople.loyalty.service.admin.AdminAuthExceptions;
import com.innople.loyalty.service.admin.AdminUserManagementExceptions;
import com.innople.loyalty.service.code.CommonCodeExceptions;
import com.innople.loyalty.service.member.MemberExceptions;
import com.innople.loyalty.service.member.MembershipGradeExceptions;
import com.innople.loyalty.service.points.PointExceptions;
import com.innople.loyalty.service.points.PointPolicyExceptions;
import com.innople.loyalty.service.tenant.TenantAdminExceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ApiErrorResponse.of(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleBadJson(HttpMessageNotReadableException ex) {
        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        return ResponseEntity.badRequest().body(ApiErrorResponse.of(msg != null ? msg : "Invalid request body"));
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            TenantMismatchException.class,
            PointExceptions.InvalidPointAmountException.class,
            AdminUserManagementExceptions.InvalidAdminUserPhoneNumberException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ApiErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler({
            PointExceptions.InsufficientPointsException.class,
            PointExceptions.PointAccountNotFoundException.class,
            MemberExceptions.MemberAlreadyExistsException.class,
            MemberExceptions.MemberNotFoundException.class,
            MemberExceptions.InvalidMemberStatusException.class,
            AdminAuthExceptions.AdminUserAlreadyExistsException.class,
            AdminUserManagementExceptions.AdminUserNotFoundException.class,
            CommonCodeExceptions.CommonCodeAlreadyExistsException.class,
            CommonCodeExceptions.CommonCodeNotFoundException.class,
            PointPolicyExceptions.PointPolicyAlreadyExistsException.class,
            PointPolicyExceptions.PointPolicyNotFoundException.class,
            TenantAdminExceptions.TenantNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBusiness(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(AdminAuthExceptions.InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(AdminAuthExceptions.InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(AdminRoleResolver.AdminAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(AdminRoleResolver.AdminAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler({
            MembershipGradeExceptions.MembershipGradeNotFoundException.class,
            MembershipGradeExceptions.LevelAlreadyExistsException.class
    })
    public ResponseEntity<ApiErrorResponse> handleMembershipGrade(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleInternalServerError(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        log.error("Unhandled exception: {}", msg, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiErrorResponse.of(msg));
    }

    private String formatFieldError(FieldError error) {
        String field = error.getField();
        String msg = error.getDefaultMessage();
        return field + ": " + msg;
    }
}

