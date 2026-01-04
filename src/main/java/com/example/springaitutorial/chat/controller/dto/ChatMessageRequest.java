package com.example.springaitutorial.chat.controller.dto;

public class ChatMessageRequest {

	private final String model;
	private final String userMessage;

	public ChatMessageRequest(String model, String userMessage) {
		this.model = model;
		this.userMessage = userMessage;
	}

	public String getModel() {
		return model;
	}

	public String getUserMessage() {
		return userMessage;
	}
}
