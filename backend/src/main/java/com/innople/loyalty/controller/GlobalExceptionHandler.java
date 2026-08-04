package com.innople.loyalty.controller;

import com.innople.loyalty.config.AdminRoleResolver;
import com.innople.loyalty.domain.common.TenantMismatchException;
import com.innople.loyalty.service.admin.AdminAuthExceptions;
import com.innople.loyalty.service.admin.AdminUserManagementExceptions;
import com.innople.loyalty.service.batch.BatchExceptions;
import com.innople.loyalty.service.code.CommonCodeExceptions;
import com.innople.loyalty.service.member.MemberExceptions;
import com.innople.loyalty.service.memberauth.MemberAuthExceptions;
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
            AdminUserManagementExceptions.InvalidAdminUserPhoneNumberException.class,
            AdminUserManagementExceptions.InvalidAdminUserStatusTransitionException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ApiErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler({
            PointExceptions.InsufficientPointsException.class,
            PointExceptions.PointAccountNotFoundException.class,
            PointExceptions.DuplicatePointTransactionException.class,
            MemberExceptions.MemberAlreadyExistsException.class,
            AdminAuthExceptions.AdminUserAlreadyExistsException.class,
            AdminUserManagementExceptions.AdminUserNotFoundException.class,
            CommonCodeExceptions.CommonCodeAlreadyExistsException.class,
            CommonCodeExceptions.CommonCodeNotFoundException.class,
            PointPolicyExceptions.PointPolicyAlreadyExistsException.class,
            PointPolicyExceptions.PointPolicyNotFoundException.class,
            TenantAdminExceptions.TenantNotFoundException.class,
            TenantAdminExceptions.TenantDeleteConflictException.class,
            MemberAuthExceptions.MemberAlreadyExistsException.class,
            MemberAuthExceptions.MemberCredentialAlreadyExistsException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBusiness(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(MemberExceptions.MemberVerificationRequiredException.class)
    public ResponseEntity<ApiErrorResponse> handleMemberVerificationRequired(MemberExceptions.MemberVerificationRequiredException ex) {
        return ResponseEntity.badRequest().body(ApiErrorResponse.of(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MemberExceptions.MemberPhoneAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleMemberPhoneAlreadyExists(MemberExceptions.MemberPhoneAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse.of(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MemberExceptions.MemberStatusNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleMemberStatusNotAllowed(MemberExceptions.MemberStatusNotAllowedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse.of(ex.getCode(), ex.getMessage()));
    }

    // 프론트가 메시지 문자열이 아닌 머신 판별용 code 로 구분할 수 있도록 전용 핸들러로 분리한다.
    // HTTP 상태코드는 기존과 동일하게 409(CONFLICT)를 유지한다(404 전환은 별도 백로그).
    @ExceptionHandler(MemberExceptions.MemberNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMemberNotFound(MemberExceptions.MemberNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(MemberExceptions.MemberNotFoundException.CODE, ex.getMessage()));
    }

    @ExceptionHandler(MemberExceptions.InvalidMemberStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidMemberStatus(MemberExceptions.InvalidMemberStatusException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(MemberExceptions.InvalidMemberStatusException.CODE, ex.getMessage()));
    }

    @ExceptionHandler(MembershipGradeExceptions.MembershipGradeNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMembershipGradeNotFound(MembershipGradeExceptions.MembershipGradeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(MembershipGradeExceptions.MembershipGradeNotFoundException.CODE, ex.getMessage()));
    }

    // 배치 설정 부재는 실제 404 로 반환한다(레거시 NotFound→409 관례와 달리, 신규 배치 API 는 정확한 404 로 설계).
    @ExceptionHandler(BatchExceptions.BatchConfigNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleBatchConfigNotFound(BatchExceptions.BatchConfigNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(BatchExceptions.BatchConfigNotFoundException.CODE, ex.getMessage()));
    }

    @ExceptionHandler({
            BatchExceptions.BatchConfigAlreadyExistsException.class,
            BatchExceptions.BatchAlreadyRunningException.class,
            BatchExceptions.BatchDisabledException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBatchConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(AdminAuthExceptions.InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(AdminAuthExceptions.InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler({
            MemberAuthExceptions.InvalidCredentialsException.class,
            MemberAuthExceptions.MemberCredentialNotFoundException.class,
            MemberAuthExceptions.AppLoginDisabledException.class
    })
    public ResponseEntity<ApiErrorResponse> handleMemberUnauthorized(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(AdminRoleResolver.AdminAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(AdminRoleResolver.AdminAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(MembershipGradeExceptions.LevelAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleMembershipGradeLevelConflict(MembershipGradeExceptions.LevelAlreadyExistsException ex) {
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

