package com.example.springaitutorial.rag.application.result;

public class RagChatResult {

	private final String provider;
	private final String model;
	private final String answer;
	private final String documentId;
	private final int retrievedCount;

	public RagChatResult(String provider, String model, String answer, String documentId, int retrievedCount) {
		this.provider = provider;
		this.model = model;
		this.answer = answer;
		this.documentId = documentId;
		this.retrievedCount = retrievedCount;
	}

	public String getProvider() {
		return provider;
	}

	public String getModel() {
		return model;
	}

	public String getAnswer() {
		return answer;
	}

	public String getDocumentId() {
		return documentId;
	}

	public int getRetrievedCount() {
		return retrievedCount;
	}
}
