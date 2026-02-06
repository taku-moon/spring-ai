package com.example.springaitutorial.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Object>> handleBadRequest(IllegalArgumentException exception) {
		log.warn("BadRequest: {}", exception.getMessage(), exception);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ApiResponse.fail(exception.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Object>> handleServerError(Exception exception) {
		log.error("InternalServerError: {}", exception.getMessage(), exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ApiResponse.fail("요청을 처리하는 중 오류가 발생했습니다."));
	}
}
