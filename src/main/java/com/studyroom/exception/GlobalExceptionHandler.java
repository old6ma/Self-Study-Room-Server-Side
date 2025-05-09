package com.studyroom.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import io.jsonwebtoken.ExpiredJwtException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception ex) {
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiError> handleExpiredJwtException(Exception ex) {
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, "登录信息已过期", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }
}