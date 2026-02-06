package com.example.springaitutorial.rag.application.result;

public final class DocumentIndexingResult {

	private final String documentId;
	private final int chunkCount;

	public DocumentIndexingResult(String documentId, int chunkCount) {
		this.documentId = documentId;
		this.chunkCount = chunkCount;
	}

	public String getDocumentId() {
		return documentId;
	}

	public int getChunkCount() {
		return chunkCount;
	}
}
