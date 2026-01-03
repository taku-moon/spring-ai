package com.example.springaitutorial.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatRequestDto {

	private final String userMessage;
	private final String model;

	@JsonCreator
	public ChatRequestDto(
		@JsonProperty("userMessage") String userMessage,
		@JsonProperty("model") String model
	) {
		this.userMessage = userMessage;
		this.model = (model == null || model.isBlank()) ? "gpt-4.1-nano" : model;
	}

	public String getUserMessage() {
		return userMessage;
	}

	public String getModel() {
		return model;
	}
}
