package com.example.springaitutorial.rag.controller.dto;

public class RagMessageRequest {

	private final String model;
	private final String userMessage;
	private final String documentId;

	public RagMessageRequest(String model, String userMessage, String documentId) {
		this.model = model;
		this.userMessage = userMessage;
		this.documentId = documentId;
	}

	public String getModel() {
		return model;
	}

	public String getUserMessage() {
		return userMessage;
	}

	public String getDocumentId() {
		return documentId;
	}
}
