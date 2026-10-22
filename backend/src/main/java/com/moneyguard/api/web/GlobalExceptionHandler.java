package com.moneyguard.api.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiResponse<Object>> handle(ApiException e) {
    return ResponseEntity.status(e.getStatus())
        .body(ApiResponse.fail(new ApiError(e.getCode(), e.getMessage(), e.getDetails())));
  }

  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<Object>> conflict(Exception e) {
    return ResponseEntity.status(409)
        .body(
            ApiResponse.fail(
                new ApiError(
                    "CONFLICT", "This operation conflicts with an existing record", null)));
  }

  @ExceptionHandler({
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
    org.springframework.http.converter.HttpMessageNotReadableException.class,
    org.springframework.web.bind.MethodArgumentNotValidException.class
  })
  public ResponseEntity<ApiResponse<Object>> invalid(Exception e) {
    return ResponseEntity.badRequest()
        .body(
            ApiResponse.fail(
                new ApiError("INVALID_INPUT", "Check the request fields and formats", null)));
  }

  @ExceptionHandler(org.springframework.dao.OptimisticLockingFailureException.class)
  public ResponseEntity<ApiResponse<Object>> stale(Exception e) {
    return ResponseEntity.status(409)
        .body(
            ApiResponse.fail(
                new ApiError(
                    "STALE_TRANSACTION",
                    "This transaction changed while you were working. Reload and retry.",
                    null)));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleAny(Exception e) {
    return ResponseEntity.status(500)
        .body(ApiResponse.fail(new ApiError("INTERNAL_ERROR", "Unexpected error", null)));
  }
}
