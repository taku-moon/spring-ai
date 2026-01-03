package com.example.springaitutorial.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {

	private final boolean success;
	private final T data;
	private final String error;

	private ApiResponseDto(boolean success, T data, String error) {
		this.success = success;
		this.data = data;
		this.error = error;
	}

	public static <T> ApiResponseDto<T> ok(T data) {
		return new ApiResponseDto<>(true, data, null);
	}

	public static <T> ApiResponseDto<T> fail(String error) {
		return new ApiResponseDto<>(false, null, error);
	}

	public boolean isSuccess() {
		return success;
	}

	public T getData() {
		return data;
	}

	public String getError() {
		return error;
	}
}
