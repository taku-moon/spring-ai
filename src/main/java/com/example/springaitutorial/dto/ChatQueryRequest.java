package com.example.springaitutorial.dto;

public class ChatQueryRequest {

	private final String model;
	private final String userMessage;

	public ChatQueryRequest(String model, String userMessage) {
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
